package io.github.dbsearchaccel.sync.full;

import lombok.Data;

import java.io.Serializable;

/**
 * 同步结果实体.
 * <p>
 * 封装同步任务的执行结果，包括成功数量、失败数量、耗时等信息.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Data
public class DsaSyncResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否成功.
     */
    private boolean success;

    /**
     * 任务ID.
     */
    private String taskId;

    /**
     * 场景编码.
     */
    private String sceneCode;

    /**
     * 总记录数.
     */
    private long totalCount;

    /**
     * 成功同步数量.
     */
    private long successCount;

    /**
     * 失败数量.
     */
    private long failCount;

    /**
     * 跳过数量.
     */
    private long skipCount;

    /**
     * 耗时（毫秒）.
     */
    private long costMillis;

    /**
     * 错误信息.
     */
    private String errorMessage;

    /**
     * 静态工厂方法-成功.
     *
     * @param taskId       任务ID
     * @param sceneCode    场景编码
     * @param totalCount   总记录数
     * @param successCount 成功数量
     * @param costMillis   耗时
     * @return 同步结果
     */
    public static DsaSyncResult success(String taskId, String sceneCode,
                                         long totalCount, long successCount, long costMillis) {
        DsaSyncResult result = new DsaSyncResult();
        result.setSuccess(true);
        result.setTaskId(taskId);
        result.setSceneCode(sceneCode);
        result.setTotalCount(totalCount);
        result.setSuccessCount(successCount);
        result.setCostMillis(costMillis);
        return result;
    }

    /**
     * 静态工厂方法-失败.
     *
     * @param taskId       任务ID
     * @param sceneCode    场景编码
     * @param errorMessage 错误信息
     * @return 同步结果
     */
    public static DsaSyncResult fail(String taskId, String sceneCode, String errorMessage) {
        DsaSyncResult result = new DsaSyncResult();
        result.setSuccess(false);
        result.setTaskId(taskId);
        result.setSceneCode(sceneCode);
        result.setErrorMessage(errorMessage);
        return result;
    }
}
