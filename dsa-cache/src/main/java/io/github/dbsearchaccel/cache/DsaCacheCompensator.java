package io.github.dbsearchaccel.cache;

import java.util.List;

/**
 * 缓存补偿器接口.
 * <p>
 * 当检测到缓存不一致时，自动触发补偿操作.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaCacheCompensator {

    /**
     * 检测并补偿.
     * 定时任务调用，检测缓存与DB的一致性.
     *
     * @param sceneCode 场景编码
     * @return 补偿记录数
     */
    int detectAndCompensate(String sceneCode);

    /**
     * 单条补偿.
     *
     * @param sceneCode 场景编码
     * @param pkValue   主键值
     * @param expected  期望值（来自DB）
     * @param actual    实际值（来自缓存）
     */
    void compensate(String sceneCode, String pkValue, Object expected, Object actual);

    /**
     * 批量补偿.
     *
     * @param sceneCode 场景编码
     * @param diffList  差异列表
     * @return 成功补偿数量
     */
    int batchCompensate(String sceneCode, List<DsaCacheDiff> diffList);

    /**
     * 获取补偿统计.
     *
     * @param sceneCode 场景编码
     * @return 补偿统计信息
     */
    DsaCompensateStats getStats(String sceneCode);

    /**
     * 获取最近的差异列表.
     *
     * @param sceneCode 场景编码
     * @param limit     数量限制
     * @return 差异列表
     */
    List<DsaCacheDiff> getRecentDiffs(String sceneCode, int limit);
}
