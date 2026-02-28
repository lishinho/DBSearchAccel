package io.github.dbsearchaccel.monitor;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 指标采集器接口.
 * <p>
 * 提供各类监控指标的采集能力，支持Prometheus格式导出.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaMetricsCollector {

    /**
     * 记录查询总数.
     *
     * @param sceneCode 场景编码
     */
    void recordQueryTotal(String sceneCode);

    /**
     * 记录查询成功.
     *
     * @param sceneCode 场景编码
     */
    void recordQuerySuccess(String sceneCode);

    /**
     * 记录查询失败.
     *
     * @param sceneCode 场景编码
     * @param errorType 错误类型
     */
    void recordQueryFailed(String sceneCode, String errorType);

    /**
     * 记录查询延迟.
     *
     * @param sceneCode 场景编码
     * @param duration  耗时（毫秒）
     */
    void recordQueryLatency(String sceneCode, long duration);

    /**
     * 记录ES查询延迟.
     *
     * @param sceneCode 场景编码
     * @param duration  耗时（毫秒）
     */
    void recordEsLatency(String sceneCode, long duration);

    /**
     * 记录DB查询延迟.
     *
     * @param sceneCode 场景编码
     * @param duration  耗时（毫秒）
     */
    void recordDbLatency(String sceneCode, long duration);

    /**
     * 记录降级查询.
     *
     * @param sceneCode 场景编码
     * @param level     降级级别
     */
    void recordFallback(String sceneCode, String level);

    /**
     * 记录ES操作.
     *
     * @param operation 操作类型（query/index/delete）
     * @param success   是否成功
     */
    void recordEsOperation(String operation, boolean success);

    /**
     * 记录Redis操作.
     *
     * @param operation 操作类型
     * @param success   是否成功
     */
    void recordRedisOperation(String operation, boolean success);

    /**
     * 记录缓存命中.
     *
     * @param sceneCode 场景编码
     * @param hit       是否命中
     */
    void recordCacheHit(String sceneCode, boolean hit);

    /**
     * 记录同步操作.
     *
     * @param syncType  同步类型（full/increment）
     * @param sceneCode 场景编码
     * @param records   同步记录数
     * @param duration  耗时（毫秒）
     */
    void recordSync(String syncType, String sceneCode, long records, long duration);

    /**
     * 更新缓存大小.
     *
     * @param sceneCode 场景编码
     * @param size      缓存大小
     */
    void updateCacheSize(String sceneCode, long size);

    /**
     * 记录自定义指标.
     *
     * @param name   指标名称
     * @param value  指标值
     * @param tags   标签
     */
    void recordGauge(String name, double value, Map<String, String> tags);

    /**
     * 增加计数器.
     *
     * @param name 指标名称
     * @param tags 标签
     */
    void incrementCounter(String name, Map<String, String> tags);
}
