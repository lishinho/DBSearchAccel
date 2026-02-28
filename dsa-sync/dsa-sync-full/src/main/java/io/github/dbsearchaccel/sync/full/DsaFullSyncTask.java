package io.github.dbsearchaccel.sync.full;

import io.github.dbsearchaccel.common.model.config.DsaSceneConfig;
import io.github.dbsearchaccel.infra.db.DsaDbAccessor;
import io.github.dbsearchaccel.infra.es.DsaEsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 全量同步任务.
 * <p>
 * 负责执行数据库到ES的全量数据同步，支持分批处理、进度跟踪.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaFullSyncTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(DsaFullSyncTask.class);

    private static final int DEFAULT_BATCH_SIZE = 1000;

    private final String taskId;
    private final DsaSceneConfig config;
    private final DsaDbAccessor dbAccessor;
    private final DsaEsClient esClient;
    private final DsaSyncProgress progress;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicBoolean paused = new AtomicBoolean(false);

    private int batchSize = DEFAULT_BATCH_SIZE;

    /**
     * 构造方法.
     *
     * @param config     场景配置
     * @param dbAccessor 数据库访问器
     * @param esClient   ES客户端
     */
    public DsaFullSyncTask(DsaSceneConfig config, DsaDbAccessor dbAccessor, DsaEsClient esClient) {
        this.taskId = UUID.randomUUID().toString().replace("-", "");
        this.config = config;
        this.dbAccessor = dbAccessor;
        this.esClient = esClient;
        this.progress = DsaSyncProgress.of(taskId, config.getSceneCode());
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        progress.setStatus(DsaSyncStatus.RUNNING);

        log.info("Full sync task started, taskId: [{}], scene: [{}]", taskId, config.getSceneCode());

        try {
            long totalCount = dbAccessor.count(config.getDbTable(), null);
            progress.setTotalCount(totalCount);
            log.info("Total records to sync: {}", totalCount);

            if (totalCount == 0) {
                progress.setStatus(DsaSyncStatus.COMPLETED);
                log.info("No records to sync, task completed");
                return;
            }

            int totalBatch = (int) Math.ceil((double) totalCount / batchSize);
            progress.setTotalBatch(totalBatch);

            long offset = 0;
            long processedCount = 0;
            long successCount = 0;
            long failCount = 0;
            int currentBatch = 0;

            while (offset < totalCount && !cancelled.get()) {
                while (paused.get()) {
                    Thread.sleep(1000);
                }

                if (cancelled.get()) {
                    break;
                }

                currentBatch++;
                progress.setCurrentBatch(currentBatch);

                List<Map<String, Object>> batchData = fetchBatchData(offset, batchSize);
                if (batchData.isEmpty()) {
                    break;
                }

                try {
                    esClient.bulkIndex(config.getEsIndex(), batchData, config.getPrimaryKeyField());
                    successCount += batchData.size();
                    log.debug("Batch {} synced successfully, count: {}", currentBatch, batchData.size());
                } catch (Exception e) {
                    failCount += batchData.size();
                    log.error("Batch {} sync failed, count: {}", currentBatch, batchData.size(), e);
                }

                processedCount += batchData.size();
                offset += batchSize;

                progress.setProcessedCount(processedCount);
                progress.setSuccessCount(successCount);
                progress.setFailCount(failCount);
                progress.setPercent(progress.calculatePercent());
                progress.setEstimatedRemaining(progress.calculateEstimatedRemaining());
            }

            if (cancelled.get()) {
                progress.setStatus(DsaSyncStatus.CANCELLED);
                log.info("Task cancelled, taskId: [{}]", taskId);
            } else {
                progress.setStatus(DsaSyncStatus.COMPLETED);
                log.info("Task completed, taskId: [{}], success: {}, fail: {}",
                        taskId, successCount, failCount);
            }

        } catch (Exception e) {
            progress.setStatus(DsaSyncStatus.FAILED);
            progress.setErrorMessage(e.getMessage());
            log.error("Task failed, taskId: [{}]", taskId, e);
        }

        long costMillis = System.currentTimeMillis() - startTime;
        progress.setEstimatedRemaining(0);
        log.info("Task finished, taskId: [{}], cost: {}ms", taskId, costMillis);
    }

    /**
     * 获取一批数据.
     *
     * @param offset   偏移量
     * @param batchSize 批次大小
     * @return 数据列表
     */
    private List<Map<String, Object>> fetchBatchData(long offset, int batchSize) {
        try {
            String sql = String.format("SELECT * FROM %s LIMIT %d OFFSET %d",
                    config.getDbTable(), batchSize, offset);

            List<Map<String, Object>> result = new ArrayList<>();
            List<Map<String, Object>> dbData = dbAccessor.queryByCondition(config.getDbTable(), null);

            int start = (int) Math.min(offset, dbData.size());
            int end = (int) Math.min(offset + batchSize, dbData.size());

            if (start < dbData.size()) {
                result = dbData.subList(start, end);
            }

            return result;
        } catch (Exception e) {
            log.error("Fetch batch data failed, offset: {}, batchSize: {}", offset, batchSize, e);
            return new ArrayList<>();
        }
    }

    /**
     * 取消任务.
     */
    public void cancel() {
        cancelled.set(true);
        log.info("Task cancel requested, taskId: [{}]", taskId);
    }

    /**
     * 暂停任务.
     */
    public void pause() {
        paused.set(true);
        progress.setStatus(DsaSyncStatus.PAUSED);
        log.info("Task paused, taskId: [{}]", taskId);
    }

    /**
     * 恢复任务.
     */
    public void resume() {
        paused.set(false);
        progress.setStatus(DsaSyncStatus.RUNNING);
        log.info("Task resumed, taskId: [{}]", taskId);
    }

    /**
     * 获取任务ID.
     *
     * @return 任务ID
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * 获取同步进度.
     *
     * @return 同步进度
     */
    public DsaSyncProgress getProgress() {
        return progress;
    }

    /**
     * 设置批次大小.
     *
     * @param batchSize 批次大小
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize > 0 ? batchSize : DEFAULT_BATCH_SIZE;
    }
}
