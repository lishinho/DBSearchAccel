package io.github.dbsearchaccel.consistency;

import java.util.List;

/**
 * 一致性校验服务接口.
 * <p>
 * 定义数据一致性校验的核心方法.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaConsistencyCheckService {

    /**
     * 创建校验任务.
     *
     * @param sceneCode 场景编码
     * @param checkType 校验类型
     * @return 校验任务
     */
    DsaCheckTask createTask(String sceneCode, DsaCheckType checkType);

    /**
     * 执行校验任务.
     *
     * @param taskId 任务ID
     * @return 校验报告
     */
    DsaCheckReport executeCheck(String taskId);

    /**
     * 异步执行校验任务.
     *
     * @param taskId 任务ID
     */
    void executeCheckAsync(String taskId);

    /**
     * 取消校验任务.
     *
     * @param taskId 任务ID
     * @return 是否取消成功
     */
    boolean cancelTask(String taskId);

    /**
     * 获取校验任务.
     *
     * @param taskId 任务ID
     * @return 校验任务
     */
    DsaCheckTask getTask(String taskId);

    /**
     * 获取场景的所有校验任务.
     *
     * @param sceneCode 场景编码
     * @return 任务列表
     */
    List<DsaCheckTask> getTasksByScene(String sceneCode);

    /**
     * 获取校验报告.
     *
     * @param taskId 任务ID
     * @return 校验报告
     */
    DsaCheckReport getReport(String taskId);

    /**
     * 导出校验报告.
     *
     * @param taskId 任务ID
     * @param format 格式（json/csv/html）
     * @return 报告内容
     */
    String exportReport(String taskId, String format);

    /**
     * 获取任务进度.
     *
     * @param taskId 任务ID
     * @return 进度百分比（0-100）
     */
    int getProgress(String taskId);
}
