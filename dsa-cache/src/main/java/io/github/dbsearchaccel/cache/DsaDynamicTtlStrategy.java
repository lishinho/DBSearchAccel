package io.github.dbsearchaccel.cache;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态TTL策略接口.
 * <p>
 * 根据数据变更频率动态调整缓存过期时间.
 * 变更频率高 → TTL短；变更频率低 → TTL长.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaDynamicTtlStrategy {

    /**
     * 计算动态TTL.
     *
     * @param sceneCode 场景编码
     * @param pkValue   主键值
     * @return TTL秒数
     */
    long calculateTtl(String sceneCode, String pkValue);

    /**
     * 记录数据变更事件.
     * 用于更新变更频率统计.
     *
     * @param sceneCode 场景编码
     * @param pkValue   主键值
     * @param eventType 变更类型（INSERT/UPDATE/DELETE）
     * @param timestamp 变更时间戳
     */
    void recordChange(String sceneCode, String pkValue, String eventType, long timestamp);

    /**
     * 获取变更频率统计.
     *
     * @param sceneCode 场景编码
     * @param pkValue   主键值
     * @return 变更频率（次/小时）
     */
    double getChangeFrequency(String sceneCode, String pkValue);

    /**
     * 清空场景的频率统计.
     *
     * @param sceneCode 场景编码
     */
    void clear(String sceneCode);
}
