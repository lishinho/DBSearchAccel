package io.github.dbsearchaccel.common.core.constant;

/**
 * 全局常量定义类.
 * <p>
 * 定义中间件使用的全局常量，包括缓存Key前缀、默认配置值等.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public final class DsaConstants {

    /**
     * 私有构造方法，禁止实例化.
     */
    private DsaConstants() {
    }

    /**
     * DSA统一前缀.
     */
    public static final String DSA_PREFIX = "dsa:";

    /**
     * Redis缓存Key前缀.
     */
    public static final String DSA_REDIS_KEY_PREFIX = DSA_PREFIX + "cache:";

    /**
     * 配置中心配置前缀.
     */
    public static final String DSA_CONFIG_PREFIX = "dsa.";

    /**
     * 默认分页大小.
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大分页大小.
     */
    public static final int MAX_PAGE_SIZE = 1000;

    /**
     * 默认Redis缓存过期时间（秒）.
     */
    public static final int DEFAULT_REDIS_EXPIRE_SECONDS = 600;

    /**
     * 默认Redis缓存最大主键数.
     */
    public static final int DEFAULT_REDIS_MAX_COUNT = 10000;

    /**
     * 默认慢调用阈值（毫秒）.
     */
    public static final int DEFAULT_SLOW_CALL_THRESHOLD_MS = 500;

    /**
     * 默认异常比例阈值.
     */
    public static final double DEFAULT_ERROR_RATIO_THRESHOLD = 0.5;

    /**
     * 默认熔断器超时时间（毫秒）.
     */
    public static final int DEFAULT_CIRCUIT_BREAKER_TIMEOUT_MS = 10000;

    /**
     * TraceID请求头名称.
     */
    public static final String TRACE_ID_HEADER = "X-Dsa-Trace-Id";

    /**
     * 场景类型请求头名称.
     */
    public static final String SCENE_TYPE_HEADER = "X-Dsa-Scene-Type";
}
