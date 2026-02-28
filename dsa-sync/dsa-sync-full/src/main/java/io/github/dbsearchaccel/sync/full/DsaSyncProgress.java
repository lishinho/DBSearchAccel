package io.github.dbsearchaccel.sync.full;

import lombok.Data;

import java.io.Serializable;

/**
 * 同步进度实体.
 * <p>
 * 封装同步任务的执行进度，包括当前状态、已处理数量、百分比等信息.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Data
public class DsaSyncProgress implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID.
     */
    private String taskId;

    /**
     * 场景编码.
     */
    private String sceneCode;

    /**
     * 任务状态.
     */
    private DsaSyncStatus status;

    /**
     * 总记录数.
     */
    private long totalCount;

    /**
     * 已处理数量.
     */
    private long processedCount;

    /**
     * 成功数量.
     */
    private long successCount;

    /**
     * 失败数量.
     */
    private long failCount;

    /**
     * 当前批次号.
     */
    private int currentBatch;

    /**
     * 总批次数.
     */
    private int totalBatch;

    /**
     * 进度百分比（0-100）.
     */
    private int percent;

    /**
     * 开始时间.
     */
    private long startTime;

    /**
     * 预计剩余时间（毫秒）.
     */
    private long estimatedRemaining;

    /**
     * 错误信息.
     */
    private String errorMessage;

    /**
     * 计算进度百分比.
     *
     * @return 进度百分比
     */
    public int calculatePercent() {
        if (totalCount <= 0) {
            return 0;
        }
        return (int) (processedCount * 100 / totalCount);
    }

    /**
     * 计算预计剩余时间.
     *
     * @return 预计剩余时间（毫秒）
     */
    public long calculateEstimatedRemaining() {
        if (processedCount <= 0 || startTime <= 0) {
            return 0;
        }
        long elapsed = System.currentTimeMillis() - startTime;
        long avgTimePerRecord = elapsed / processedCount;
        long remaining = totalCount - processedCount;
        return avgTimePerRecord * remaining;
    }

    /**
     * 静态工厂方法.
     *
     * @param taskId    任务ID
     * @param sceneCode 场景编码
     * @return 同步进度
     */
    public static DsaSyncProgress of(String taskId, String sceneCode) {
        DsaSyncProgress progress = new DsaSyncProgress();
        progress.setTaskId(taskId);
        progress.setSceneCode(sceneCode);
        progress.setStatus(DsaSyncStatus.PENDING);
        progress.setStartTime(System.currentTimeMillis());
        return progress;
    }
}
