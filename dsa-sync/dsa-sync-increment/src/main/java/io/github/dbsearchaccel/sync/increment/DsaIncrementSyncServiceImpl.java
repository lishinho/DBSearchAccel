package io.github.dbsearchaccel.sync.increment;

import io.github.dbsearchaccel.common.model.config.DsaSceneConfig;
import io.github.dbsearchaccel.infra.es.DsaEsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 增量同步服务实现类.
 * <p>
 * 基于Canal实现数据库到ES的增量数据同步，支持实时监听数据库变更.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaIncrementSyncServiceImpl implements DsaIncrementSyncService {

    private static final Logger log = LoggerFactory.getLogger(DsaIncrementSyncServiceImpl.class);

    private static final int DEFAULT_BATCH_SIZE = 1000;
    private static final long POLL_INTERVAL_MS = 1000;

    private final Map<String, DsaSceneConfig> sceneConfigCache = new ConcurrentHashMap<>();
    private final Map<String, DsaIncrementSyncStatus> statusCache = new ConcurrentHashMap<>();
    private final Map<String, DsaCanalClient> canalClientCache = new ConcurrentHashMap<>();
    private final Map<String, Thread> syncThreadCache = new ConcurrentHashMap<>();

    private DsaEsClient esClient;
    private DsaSyncRetryHandler retryHandler;
    private ExecutorService executorService;

    private volatile boolean globalRunning = false;

    /**
     * 默认构造方法.
     */
    public DsaIncrementSyncServiceImpl() {
        this.retryHandler = new DsaSyncRetryHandler();
        this.executorService = Executors.newCachedThreadPool();
    }

    /**
     * 构造方法.
     *
     * @param esClient ES客户端
     */
    public DsaIncrementSyncServiceImpl(DsaEsClient esClient) {
        this.esClient = esClient;
        this.retryHandler = new DsaSyncRetryHandler();
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public boolean start(String sceneCode) {
        log.info("Start increment sync, scene: [{}]", sceneCode);

        DsaSceneConfig config = sceneConfigCache.get(sceneCode);
        if (config == null) {
            log.error("Scene config not found: [{}]", sceneCode);
            return false;
        }

        DsaIncrementSyncStatus status = statusCache.computeIfAbsent(sceneCode, DsaIncrementSyncStatus::of);

        if (status.isRunning()) {
            log.warn("Increment sync already running: [{}]", sceneCode);
            return true;
        }

        DsaCanalClient canalClient = createCanalClient(config);
        if (!canalClient.connect()) {
            log.error("Connect to Canal failed: [{}]", sceneCode);
            return false;
        }

        if (!canalClient.subscribe(config)) {
            log.error("Subscribe to Canal failed: [{}]", sceneCode);
            canalClient.disconnect();
            return false;
        }

        canalClientCache.put(sceneCode, canalClient);

        status.setRunning(true);
        status.setConnected(true);
        status.setStartTime(System.currentTimeMillis());

        Thread syncThread = new Thread(() -> processSync(sceneCode), "dsa-sync-" + sceneCode);
        syncThread.setDaemon(true);
        syncThread.start();
        syncThreadCache.put(sceneCode, syncThread);

        retryHandler.start();

        log.info("Increment sync started, scene: [{}]", sceneCode);
        return true;
    }

    @Override
    public boolean stop(String sceneCode) {
        log.info("Stop increment sync, scene: [{}]", sceneCode);

        DsaIncrementSyncStatus status = statusCache.get(sceneCode);
        if (status != null) {
            status.setRunning(false);
            status.setConnected(false);
        }

        DsaCanalClient canalClient = canalClientCache.remove(sceneCode);
        if (canalClient != null) {
            canalClient.disconnect();
        }

        Thread syncThread = syncThreadCache.remove(sceneCode);
        if (syncThread != null) {
            syncThread.interrupt();
        }

        log.info("Increment sync stopped, scene: [{}]", sceneCode);
        return true;
    }

    @Override
    public boolean isRunning(String sceneCode) {
        DsaIncrementSyncStatus status = statusCache.get(sceneCode);
        return status != null && status.isRunning();
    }

    @Override
    public DsaIncrementSyncStatus getStatus(String sceneCode) {
        return statusCache.get(sceneCode);
    }

    @Override
    public boolean handleChange(String sceneCode, String eventType, List<Map<String, Object>> data) {
        log.debug("Handle change, scene: [{}], event: {}, count: {}", sceneCode, eventType, data.size());

        DsaSceneConfig config = sceneConfigCache.get(sceneCode);
        if (config == null) {
            log.error("Scene config not found: [{}]", sceneCode);
            return false;
        }

        DsaIncrementSyncStatus status = statusCache.get(sceneCode);
        if (status == null) {
            return false;
        }

        try {
            String index = config.getEsIndex();
            String idField = config.getPrimaryKeyField();

            switch (eventType.toUpperCase()) {
                case "INSERT":
                    esClient.bulkIndex(index, data, idField);
                    status.incrementInsert();
                    break;

                case "UPDATE":
                    esClient.bulkIndex(index, data, idField);
                    status.incrementUpdate();
                    break;

                case "DELETE":
                    List<String> ids = new ArrayList<>();
                    for (Map<String, Object> row : data) {
                        Object id = row.get(idField);
                        if (id != null) {
                            ids.add(String.valueOf(id));
                        }
                    }
                    if (!ids.isEmpty()) {
                        esClient.bulkDelete(index, ids);
                    }
                    status.incrementDelete();
                    break;

                default:
                    log.warn("Unknown event type: {}", eventType);
                    return false;
            }

            status.setLastSyncTime(System.currentTimeMillis());
            return true;

        } catch (Exception e) {
            log.error("Handle change failed, scene: [{}], event: {}", sceneCode, eventType, e);
            status.incrementFail();
            return false;
        }
    }

    @Override
    public int startAll() {
        log.info("Start all increment sync");

        globalRunning = true;
        int count = 0;

        for (String sceneCode : sceneConfigCache.keySet()) {
            if (start(sceneCode)) {
                count++;
            }
        }

        log.info("Started {} increment sync tasks", count);
        return count;
    }

    @Override
    public void stopAll() {
        log.info("Stop all increment sync");

        globalRunning = false;

        for (String sceneCode : sceneConfigCache.keySet()) {
            stop(sceneCode);
        }

        if (retryHandler != null) {
            retryHandler.stop();
        }

        log.info("All increment sync tasks stopped");
    }

    /**
     * 处理同步循环.
     *
     * @param sceneCode 场景编码
     */
    private void processSync(String sceneCode) {
        log.info("Sync thread started, scene: [{}]", sceneCode);

        DsaCanalClient canalClient = canalClientCache.get(sceneCode);
        DsaIncrementSyncStatus status = statusCache.get(sceneCode);
        DsaSceneConfig config = sceneConfigCache.get(sceneCode);

        while (status != null && status.isRunning() && canalClient != null) {
            try {
                List<DsaCanalEntry> entries = canalClient.fetch(DEFAULT_BATCH_SIZE);

                if (entries.isEmpty()) {
                    Thread.sleep(POLL_INTERVAL_MS);
                    continue;
                }

                for (DsaCanalEntry entry : entries) {
                    if (!entry.isDataChangeEvent()) {
                        continue;
                    }

                    String eventType = entry.getEventType().name();
                    List<Map<String, Object>> data = new ArrayList<>();

                    if ("INSERT".equals(eventType) || "UPDATE".equals(eventType)) {
                        data.add(entry.getAfterData());
                    } else if ("DELETE".equals(eventType)) {
                        data.add(entry.getBeforeData());
                    }

                    boolean success = handleChange(sceneCode, eventType, data);

                    if (!success) {
                        Map<String, Object> retryData = entry.getAfterData() != null
                                ? entry.getAfterData() : entry.getBeforeData();
                        if (retryData != null) {
                            retryHandler.addRetryTask(sceneCode, eventType, retryData,
                                    this::handleChangeWithRetry);
                        }
                    }
                }

                if (!entries.isEmpty()) {
                    canalClient.ack(entries.get(0).getBatchId());
                }

                status.setLastPosition(canalClient.getPosition());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Process sync error, scene: [{}]", sceneCode, e);
                status.setErrorMessage(e.getMessage());
            }
        }

        log.info("Sync thread stopped, scene: [{}]", sceneCode);
    }

    /**
     * 带重试的变更处理.
     *
     * @param sceneCode 场景编码
     * @param eventType 事件类型
     * @param data      数据
     * @return 处理结果
     */
    private boolean handleChangeWithRetry(String sceneCode, String eventType, Map<String, Object> data) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        dataList.add(data);
        return handleChange(sceneCode, eventType, dataList);
    }

    /**
     * 创建Canal客户端.
     *
     * @param config 场景配置
     * @return Canal客户端
     */
    private DsaCanalClient createCanalClient(DsaSceneConfig config) {
        return new DsaCanalClientImpl("localhost", 11111, "example", "", "");
    }

    /**
     * 注册场景配置.
     *
     * @param sceneCode 场景编码
     * @param config    场景配置
     */
    public void registerSceneConfig(String sceneCode, DsaSceneConfig config) {
        sceneConfigCache.put(sceneCode, config);
        log.info("Registered scene config: [{}]", sceneCode);
    }

    /**
     * 设置ES客户端.
     *
     * @param esClient ES客户端
     */
    public void setEsClient(DsaEsClient esClient) {
        this.esClient = esClient;
    }

    /**
     * 设置重试处理器.
     *
     * @param retryHandler 重试处理器
     */
    public void setRetryHandler(DsaSyncRetryHandler retryHandler) {
        this.retryHandler = retryHandler;
    }
}
