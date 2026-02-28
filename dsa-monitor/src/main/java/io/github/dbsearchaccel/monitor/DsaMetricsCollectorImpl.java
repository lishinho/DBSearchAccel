package io.github.dbsearchaccel.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 指标采集器实现类.
 * <p>
 * 基于Micrometer实现指标采集，支持Prometheus格式导出.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaMetricsCollectorImpl implements DsaMetricsCollector {

    private static final Logger log = LoggerFactory.getLogger(DsaMetricsCollectorImpl.class);

    private final MeterRegistry meterRegistry;
    private final Map<String, Long> cacheSizeMap = new ConcurrentHashMap<>();

    /**
     * 构造方法.
     *
     * @param meterRegistry Meter注册器
     */
    public DsaMetricsCollectorImpl(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void recordQueryTotal(String sceneCode) {
        Counter.builder(DsaMetrics.Query.TOTAL)
                .tag("scene", sceneCode)
                .description("Total query count")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordQuerySuccess(String sceneCode) {
        Counter.builder(DsaMetrics.Query.SUCCESS)
                .tag("scene", sceneCode)
                .description("Successful query count")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordQueryFailed(String sceneCode, String errorType) {
        Counter.builder(DsaMetrics.Query.FAILED)
                .tag("scene", sceneCode)
                .tag("error_type", errorType)
                .description("Failed query count")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordQueryLatency(String sceneCode, long duration) {
        Timer.builder(DsaMetrics.Query.LATENCY)
                .tag("scene", sceneCode)
                .description("Query latency")
                .register(meterRegistry)
                .record(duration, TimeUnit.MILLISECONDS);
    }

    @Override
    public void recordEsLatency(String sceneCode, long duration) {
        Timer.builder(DsaMetrics.Query.ES_LATENCY)
                .tag("scene", sceneCode)
                .description("ES query latency")
                .register(meterRegistry)
                .record(duration, TimeUnit.MILLISECONDS);
    }

    @Override
    public void recordDbLatency(String sceneCode, long duration) {
        Timer.builder(DsaMetrics.Query.DB_LATENCY)
                .tag("scene", sceneCode)
                .description("DB query latency")
                .register(meterRegistry)
                .record(duration, TimeUnit.MILLISECONDS);
    }

    @Override
    public void recordFallback(String sceneCode, String level) {
        Counter.builder(DsaMetrics.Fallback.TOTAL)
                .tag("scene", sceneCode)
                .tag("level", level)
                .description("Fallback count")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordEsOperation(String operation, boolean success) {
        String metricName = success ? DsaMetrics.Es.QUERY_TOTAL : DsaMetrics.Es.QUERY_FAILED;
        if ("index".equals(operation)) {
            metricName = success ? DsaMetrics.Es.INDEX_TOTAL : DsaMetrics.Es.INDEX_FAILED;
        } else if ("delete".equals(operation)) {
            metricName = DsaMetrics.Es.DELETE_TOTAL;
        }

        Counter.builder(metricName)
                .tag("operation", operation)
                .tag("success", String.valueOf(success))
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordRedisOperation(String operation, boolean success) {
        String metricName = success ? DsaMetrics.Redis.OPERATION_TOTAL : DsaMetrics.Redis.OPERATION_FAILED;

        Counter.builder(metricName)
                .tag("operation", operation)
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordCacheHit(String sceneCode, boolean hit) {
        String metricName = hit ? DsaMetrics.Cache.HIT : DsaMetrics.Cache.MISS;

        Counter.builder(metricName)
                .tag("scene", sceneCode)
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordSync(String syncType, String sceneCode, long records, long duration) {
        String metricName = "full".equals(syncType)
                ? DsaMetrics.Sync.FULL_SYNC_TOTAL : DsaMetrics.Sync.INCREMENT_SYNC_TOTAL;

        Counter.builder(metricName)
                .tag("scene", sceneCode)
                .register(meterRegistry)
                .increment();

        Counter.builder(DsaMetrics.Sync.FULL_SYNC_RECORDS)
                .tag("scene", sceneCode)
                .tag("type", syncType)
                .register(meterRegistry)
                .increment(records);

        Timer.builder(DsaMetrics.Sync.FULL_SYNC_DURATION)
                .tag("scene", sceneCode)
                .tag("type", syncType)
                .register(meterRegistry)
                .record(duration, TimeUnit.MILLISECONDS);
    }

    @Override
    public void updateCacheSize(String sceneCode, long size) {
        cacheSizeMap.put(sceneCode, size);

        Gauge.builder(DsaMetrics.Cache.SIZE, () -> cacheSizeMap.getOrDefault(sceneCode, 0L))
                .tag("scene", sceneCode)
                .description("Cache size")
                .register(meterRegistry);
    }

    @Override
    public void recordGauge(String name, double value, Map<String, String> tags) {
        String key = name + (tags != null ? tags.toString() : "");
        Gauge.builder(name, () -> value)
                .tag("key", key)
                .register(meterRegistry);
    }

    @Override
    public void incrementCounter(String name, Map<String, String> tags) {
        Counter.Builder builder = Counter.builder(name);

        if (tags != null) {
            tags.forEach(builder::tag);
        }

        builder.register(meterRegistry).increment();
    }

    /**
     * 获取Meter注册器.
     *
     * @return Meter注册器
     */
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }
}
