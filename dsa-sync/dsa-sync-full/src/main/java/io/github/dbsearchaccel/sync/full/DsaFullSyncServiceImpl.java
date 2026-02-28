package io.github.dbsearchaccel.sync.full;

import io.github.dbsearchaccel.common.model.config.DsaSceneConfig;
import io.github.dbsearchaccel.infra.db.DsaDbAccessor;
import io.github.dbsearchaccel.infra.es.DsaEsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 全量同步服务实现类.
 * <p>
 * 提供数据库到ES的全量数据同步能力，支持异步执行、进度跟踪、任务管理.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaFullSyncServiceImpl implements DsaFullSyncService {

    private static final Logger log = LoggerFactory.getLogger(DsaFullSyncServiceImpl.class);

    private final Map<String, DsaSceneConfig> sceneConfigCache = new ConcurrentHashMap<>();
    private final Map<String, DsaFullSyncTask> taskCache = new ConcurrentHashMap<>();
    private final Map<String, Future<?>> taskFutures = new ConcurrentHashMap<>();

    private DsaDbAccessor dbAccessor;
    private DsaEsClient esClient;
    private ExecutorService executorService;

    /**
     * 默认构造方法.
     */
    public DsaFullSyncServiceImpl() {
        this.executorService = Executors.newFixedThreadPool(4);
    }

    /**
     * 构造方法.
     *
     * @param dbAccessor 数据库访问器
     * @param esClient   ES客户端
     */
    public DsaFullSyncServiceImpl(DsaDbAccessor dbAccessor, DsaEsClient esClient) {
        this.dbAccessor = dbAccessor;
        this.esClient = esClient;
        this.executorService = Executors.newFixedThreadPool(4);
    }

    @Override
    public DsaSyncResult sync(String sceneCode) {
        return sync(sceneCode, null);
    }

    @Override
    public DsaSyncResult sync(String sceneCode, Map<String, Object> params) {
        log.info("Start full sync, scene: [{}]", sceneCode);

        DsaSceneConfig config = sceneConfigCache.get(sceneCode);
        if (config == null) {
            log.error("Scene config not found: [{}]", sceneCode);
            return DsaSyncResult.fail(null, sceneCode, "Scene config not found");
        }

        DsaFullSyncTask task = new DsaFullSyncTask(config, dbAccessor, esClient);

        if (params != null && params.containsKey("batchSize")) {
            task.setBatchSize((Integer) params.get("batchSize"));
        }

        long startTime = System.currentTimeMillis();

        try {
            task.run();

            DsaSyncProgress progress = task.getProgress();
            DsaSyncResult result = DsaSyncResult.success(
                    task.getTaskId(),
                    sceneCode,
                    progress.getTotalCount(),
                    progress.getSuccessCount(),
                    System.currentTimeMillis() - startTime
            );
            result.setFailCount(progress.getFailCount());

            return result;

        } catch (Exception e) {
            log.error("Full sync failed, scene: [{}]", sceneCode, e);
            return DsaSyncResult.fail(task.getTaskId(), sceneCode, e.getMessage());
        }
    }

    @Override
    public String syncAsync(String sceneCode) {
        log.info("Start async full sync, scene: [{}]", sceneCode);

        DsaSceneConfig config = sceneConfigCache.get(sceneCode);
        if (config == null) {
            log.error("Scene config not found: [{}]", sceneCode);
            return null;
        }

        DsaFullSyncTask task = new DsaFullSyncTask(config, dbAccessor, esClient);
        String taskId = task.getTaskId();

        taskCache.put(taskId, task);

        Future<?> future = executorService.submit(() -> {
            try {
                task.run();
            } finally {
                taskCache.remove(taskId);
                taskFutures.remove(taskId);
            }
        });

        taskFutures.put(taskId, future);

        log.info("Async task submitted, taskId: [{}]", taskId);
        return taskId;
    }

    @Override
    public DsaSyncProgress getProgress(String taskId) {
        DsaFullSyncTask task = taskCache.get(taskId);
        if (task == null) {
            log.warn("Task not found: [{}]", taskId);
            return null;
        }
        return task.getProgress();
    }

    @Override
    public boolean cancel(String taskId) {
        DsaFullSyncTask task = taskCache.get(taskId);
        if (task == null) {
            log.warn("Task not found for cancel: [{}]", taskId);
            return false;
        }

        task.cancel();

        Future<?> future = taskFutures.get(taskId);
        if (future != null) {
            future.cancel(true);
        }

        return true;
    }

    @Override
    public boolean pause(String taskId) {
        DsaFullSyncTask task = taskCache.get(taskId);
        if (task == null) {
            log.warn("Task not found for pause: [{}]", taskId);
            return false;
        }

        task.pause();
        return true;
    }

    @Override
    public boolean resume(String taskId) {
        DsaFullSyncTask task = taskCache.get(taskId);
        if (task == null) {
            log.warn("Task not found for resume: [{}]", taskId);
            return false;
        }

        task.resume();
        return true;
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
     * 设置数据库访问器.
     *
     * @param dbAccessor 数据库访问器
     */
    public void setDbAccessor(DsaDbAccessor dbAccessor) {
        this.dbAccessor = dbAccessor;
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
     * 设置执行器服务.
     *
     * @param executorService 执行器服务
     */
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * 关闭服务.
     */
    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
        }
        log.info("Full sync service shutdown");
    }
}
