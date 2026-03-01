package io.github.dbsearchaccel.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态TTL策略实现类.
 * <p>
 * 根据数据变更频率动态调整缓存过期时间.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaDynamicTtlStrategyImpl implements DsaDynamicTtlStrategy {

    private static final Logger log = LoggerFactory.getLogger(DsaDynamicTtlStrategyImpl.class);

    /**
     * TTL范围配置.
     */
    private static final long MIN_TTL = 60;
    private static final long MAX_TTL = 3600;
    private static final long DEFAULT_TTL = 300;

    /**
     * 变更频率阈值.
     */
    private static final double HIGH_FREQUENCY_THRESHOLD = 10.0;
    private static final double LOW_FREQUENCY_THRESHOLD = 1.0;

    /**
     * 滑动窗口大小（毫秒）.
     */
    private static final long WINDOW_SIZE_MS = 3600_000;

    /**
     * 变更频率统计.
     */
    private final ConcurrentHashMap<String, DsaSlidingWindowCounter> frequencyCounters;

    /**
     * 构造方法.
     */
    public DsaDynamicTtlStrategyImpl() {
        this.frequencyCounters = new ConcurrentHashMap<>();
        log.info("DsaDynamicTtlStrategy initialized");
    }

    @Override
    public long calculateTtl(String sceneCode, String pkValue) {
        double frequency = getChangeFrequency(sceneCode, pkValue);

        long ttl;
        if (frequency >= HIGH_FREQUENCY_THRESHOLD) {
            ttl = MIN_TTL;
        } else if (frequency <= LOW_FREQUENCY_THRESHOLD) {
            ttl = MAX_TTL;
        } else {
            double ratio = (frequency - LOW_FREQUENCY_THRESHOLD) / (HIGH_FREQUENCY_THRESHOLD - LOW_FREQUENCY_THRESHOLD);
            ttl = (long) (MAX_TTL - ratio * (MAX_TTL - MIN_TTL));
        }

        log.debug("Calculated TTL: scene={}, pk={}, frequency={}, ttl={}", sceneCode, pkValue, frequency, ttl);
        return ttl;
    }

    @Override
    public void recordChange(String sceneCode, String pkValue, String eventType, long timestamp) {
        String key = buildKey(sceneCode, pkValue);
        DsaSlidingWindowCounter counter = frequencyCounters.computeIfAbsent(key,
                k -> new DsaSlidingWindowCounter(WINDOW_SIZE_MS, 24));
        counter.increment(timestamp);

        log.debug("Recorded change: key={}, type={}", key, eventType);
    }

    @Override
    public double getChangeFrequency(String sceneCode, String pkValue) {
        String key = buildKey(sceneCode, pkValue);
        DsaSlidingWindowCounter counter = frequencyCounters.get(key);
        if (counter == null) {
            return 0.0;
        }
        return counter.getFrequency(System.currentTimeMillis());
    }

    @Override
    public void clear(String sceneCode) {
        String prefix = sceneCode + ":";
        frequencyCounters.keySet().removeIf(key -> key.startsWith(prefix));
        log.info("Cleared frequency counters for scene: {}", sceneCode);
    }

    /**
     * 构建键.
     */
    private String buildKey(String sceneCode, String pkValue) {
        return sceneCode + ":" + pkValue;
    }

    /**
     * 获取统计数量.
     */
    public int getCounterCount() {
        return frequencyCounters.size();
    }
}
