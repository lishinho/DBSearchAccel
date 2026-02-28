package io.github.dbsearchaccel.sync.increment;

import lombok.Data;

import java.io.Serializable;

/**
 * 增量同步状态实体.
 * <p>
 * 封装增量同步任务的运行状态，包括连接状态、处理统计等信息.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Data
public class DsaIncrementSyncStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 场景编码.
     */
    private String sceneCode;

    /**
     * 是否运行中.
     */
    private boolean running;

    /**
     * Canal连接状态.
     */
    private boolean connected;

    /**
     * 最后同步时间.
     */
    private long lastSyncTime;

    /**
     * 最后同步位置（Binlog位置）.
     */
    private String lastPosition;

    /**
     * 总处理事件数.
     */
    private long totalEventCount;

    /**
     * 总插入数量.
     */
    private long insertCount;

    /**
     * 总更新数量.
     */
    private long updateCount;

    /**
     * 总删除数量.
     */
    private long deleteCount;

    /**
     * 总失败数量.
     */
    private long failCount;

    /**
     * 错误信息.
     */
    private String errorMessage;

    /**
     * 启动时间.
     */
    private long startTime;

    /**
     * 静态工厂方法.
     *
     * @param sceneCode 场景编码
     * @return 同步状态
     */
    public static DsaIncrementSyncStatus of(String sceneCode) {
        DsaIncrementSyncStatus status = new DsaIncrementSyncStatus();
        status.setSceneCode(sceneCode);
        status.setRunning(false);
        status.setConnected(false);
        return status;
    }

    /**
     * 增加插入计数.
     */
    public void incrementInsert() {
        this.insertCount++;
        this.totalEventCount++;
        this.lastSyncTime = System.currentTimeMillis();
    }

    /**
     * 增加更新计数.
     */
    public void incrementUpdate() {
        this.updateCount++;
        this.totalEventCount++;
        this.lastSyncTime = System.currentTimeMillis();
    }

    /**
     * 增加删除计数.
     */
    public void incrementDelete() {
        this.deleteCount++;
        this.totalEventCount++;
        this.lastSyncTime = System.currentTimeMillis();
    }

    /**
     * 增加失败计数.
     */
    public void incrementFail() {
        this.failCount++;
        this.lastSyncTime = System.currentTimeMillis();
    }
}
