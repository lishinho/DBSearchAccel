package io.github.dbsearchaccel.monitor;

/**
 * 监控指标常量定义.
 * <p>
 * 定义DSA系统的核心监控指标名称，用于Prometheus采集.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public final class DsaMetrics {

    private DsaMetrics() {
    }

    private static final String PREFIX = "dsa_";

    /**
     * 查询相关指标.
     */
    public static final class Query {
        private Query() {
        }

        public static final String TOTAL = PREFIX + "query_total";
        public static final String SUCCESS = PREFIX + "query_success";
        public static final String FAILED = PREFIX + "query_failed";
        public static final String LATENCY = PREFIX + "query_latency_seconds";
        public static final String FROM_FALLBACK = PREFIX + "query_from_fallback";
        public static final String ES_LATENCY = PREFIX + "query_es_latency_seconds";
        public static final String DB_LATENCY = PREFIX + "query_db_latency_seconds";
    }

    /**
     * ES相关指标.
     */
    public static final class Es {
        private Es() {
        }

        public static final String QUERY_TOTAL = PREFIX + "es_query_total";
        public static final String QUERY_FAILED = PREFIX + "es_query_failed";
        public static final String INDEX_TOTAL = PREFIX + "es_index_total";
        public static final String INDEX_FAILED = PREFIX + "es_index_failed";
        public static final String DELETE_TOTAL = PREFIX + "es_delete_total";
        public static final String CONNECTION_ERROR = PREFIX + "es_connection_error";
    }

    /**
     * Redis相关指标.
     */
    public static final class Redis {
        private Redis() {
        }

        public static final String CACHE_HIT = PREFIX + "redis_cache_hit";
        public static final String CACHE_MISS = PREFIX + "redis_cache_miss";
        public static final String OPERATION_TOTAL = PREFIX + "redis_operation_total";
        public static final String OPERATION_FAILED = PREFIX + "redis_operation_failed";
        public static final String CONNECTION_ERROR = PREFIX + "redis_connection_error";
    }

    /**
     * 同步相关指标.
     */
    public static final class Sync {
        private Sync() {
        }

        public static final String FULL_SYNC_TOTAL = PREFIX + "sync_full_total";
        public static final String FULL_SYNC_RECORDS = PREFIX + "sync_full_records";
        public static final String FULL_SYNC_DURATION = PREFIX + "sync_full_duration_seconds";
        public static final String INCREMENT_SYNC_TOTAL = PREFIX + "sync_increment_total";
        public static final String INCREMENT_SYNC_EVENTS = PREFIX + "sync_increment_events";
        public static final String INCREMENT_SYNC_FAILED = PREFIX + "sync_increment_failed";
        public static final String CANAL_CONNECTION_ERROR = PREFIX + "canal_connection_error";
    }

    /**
     * 降级相关指标.
     */
    public static final class Fallback {
        private Fallback() {
        }

        public static final String TOTAL = PREFIX + "fallback_total";
        public static final String BY_SCENE = PREFIX + "fallback_by_scene";
        public static final String BY_LEVEL = PREFIX + "fallback_by_level";
        public static final String CIRCUIT_BREAKER_OPEN = PREFIX + "circuit_breaker_open";
    }

    /**
     * 缓存相关指标.
     */
    public static final class Cache {
        private Cache() {
        }

        public static final String SIZE = PREFIX + "cache_size";
        public static final String HIT = PREFIX + "cache_hit";
        public static final String MISS = PREFIX + "cache_miss";
        public static final String EVICTION = PREFIX + "cache_eviction";
    }

    /**
     * 场景相关指标.
     */
    public static final class Scene {
        private Scene() {
        }

        public static final String ACTIVE_COUNT = PREFIX + "scene_active_count";
        public static final String DISABLED_COUNT = PREFIX + "scene_disabled_count";
        public static final String QUERY_BY_SCENE = PREFIX + "scene_query_total";
    }
}
