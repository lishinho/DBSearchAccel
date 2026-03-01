package io.github.dbsearchaccel.gateway;

/**
 * 限流器接口.
 * <p>
 * 负责接口级别的限流控制.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaRateLimiter {

    /**
     * 尝试获取许可（QPS限流）.
     *
     * @param resource 资源标识
     * @return 是否获取成功
     */
    boolean tryAcquire(String resource);

    /**
     * 尝试获取许可（带超时）.
     *
     * @param resource   资源标识
     * @param timeoutMs  超时时间（毫秒）
     * @return 是否获取成功
     */
    boolean tryAcquire(String resource, long timeoutMs);

    /**
     * 尝试获取并发许可.
     *
     * @param resource 资源标识
     * @return 是否获取成功
     */
    boolean tryAcquireConcurrent(String resource);

    /**
     * 释放并发许可.
     *
     * @param resource 资源标识
     */
    void releaseConcurrent(String resource);

    /**
     * 获取当前QPS.
     *
     * @param resource 资源标识
     * @return 当前QPS
     */
    double getCurrentQps(String resource);

    /**
     * 获取当前并发数.
     *
     * @param resource 资源标识
     * @return 当前并发数
     */
    int getCurrentConcurrent(String resource);

    /**
     * 配置限流规则.
     *
     * @param resource 资源标识
     * @param config   限流配置
     */
    void configure(String resource, DsaRateLimitConfig config);

    /**
     * 移除限流规则.
     *
     * @param resource 资源标识
     */
    void remove(String resource);

    /**
     * 判断资源是否被限流.
     *
     * @param resource 资源标识
     * @return 是否被限流
     */
    boolean isRateLimited(String resource);
}
