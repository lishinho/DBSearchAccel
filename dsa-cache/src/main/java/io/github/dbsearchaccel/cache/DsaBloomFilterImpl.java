package io.github.dbsearchaccel.cache;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 布隆过滤器实现类.
 * <p>
 * 用于快速判断主键是否可能存在，降低无效查询穿透.
 * 理论误判率：< 1%.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaBloomFilterImpl implements DsaBloomFilter {

    private static final Logger log = LoggerFactory.getLogger(DsaBloomFilterImpl.class);

    /**
     * 默认误判率.
     */
    private static final double DEFAULT_FPP = 0.01;

    /**
     * 默认预期插入数量.
     */
    private static final int DEFAULT_EXPECTED_INSERTIONS = 10_000_000;

    /**
     * 场景到布隆过滤器的映射.
     */
    private final ConcurrentHashMap<String, BloomFilter<String>> filters;

    /**
     * 统计信息.
     */
    private final ConcurrentHashMap<String, DsaBloomFilterStats> statsMap;

    /**
     * 默认误判率.
     */
    private final double defaultFpp;

    /**
     * 默认预期插入数量.
     */
    private final int defaultExpectedInsertions;

    /**
     * 构造方法（使用默认配置）.
     */
    public DsaBloomFilterImpl() {
        this(DEFAULT_FPP, DEFAULT_EXPECTED_INSERTIONS);
    }

    /**
     * 构造方法.
     *
     * @param fpp                  误判率
     * @param expectedInsertions   预期插入数量
     */
    public DsaBloomFilterImpl(double fpp, int expectedInsertions) {
        this.defaultFpp = fpp > 0 && fpp < 1 ? fpp : DEFAULT_FPP;
        this.defaultExpectedInsertions = expectedInsertions > 0 ? expectedInsertions : DEFAULT_EXPECTED_INSERTIONS;
        this.filters = new ConcurrentHashMap<>();
        this.statsMap = new ConcurrentHashMap<>();

        log.info("DsaBloomFilter initialized, fpp: {}, expectedInsertions: {}", this.defaultFpp, this.defaultExpectedInsertions);
    }

    @Override
    public void put(String sceneCode, String value) {
        if (sceneCode == null || value == null) {
            return;
        }

        try {
            BloomFilter<String> filter = getOrCreateFilter(sceneCode);
            filter.put(value);
            getOrCreateStats(sceneCode).incrementPutCount();

            log.debug("Bloom filter put: scene={}, value={}", sceneCode, value);

        } catch (Exception e) {
            log.error("Bloom filter put failed: scene={}, value={}", sceneCode, value, e);
        }
    }

    @Override
    public void putAll(String sceneCode, Collection<String> values) {
        if (sceneCode == null || values == null || values.isEmpty()) {
            return;
        }

        try {
            BloomFilter<String> filter = getOrCreateFilter(sceneCode);
            for (String value : values) {
                if (value != null) {
                    filter.put(value);
                }
            }
            getOrCreateStats(sceneCode).addPutCount(values.size());

            log.debug("Bloom filter putAll: scene={}, count={}", sceneCode, values.size());

        } catch (Exception e) {
            log.error("Bloom filter putAll failed: scene={}", sceneCode, e);
        }
    }

    @Override
    public boolean mightContain(String sceneCode, String value) {
        if (sceneCode == null || value == null) {
            return true;
        }

        try {
            BloomFilter<String> filter = filters.get(sceneCode);
            DsaBloomFilterStats stats = getOrCreateStats(sceneCode);
            stats.incrementCheckCount();

            if (filter == null) {
                stats.incrementMissCount();
                return true;
            }

            boolean result = filter.mightContain(value);
            if (result) {
                stats.incrementHitCount();
            } else {
                stats.incrementMissCount();
            }

            log.debug("Bloom filter check: scene={}, value={}, result={}", sceneCode, value, result);
            return result;

        } catch (Exception e) {
            log.error("Bloom filter check failed: scene={}, value={}", sceneCode, value, e);
            return true;
        }
    }

    @Override
    public void rebuild(String sceneCode, Collection<String> allValues) {
        if (sceneCode == null) {
            return;
        }

        try {
            int expectedInsertions = allValues != null ? allValues.size() : defaultExpectedInsertions;
            if (expectedInsertions < 1000) {
                expectedInsertions = 1000;
            }

            BloomFilter<String> newFilter = BloomFilter.create(
                    Funnels.stringFunnel(StandardCharsets.UTF_8),
                    expectedInsertions,
                    defaultFpp);

            if (allValues != null) {
                for (String value : allValues) {
                    if (value != null) {
                        newFilter.put(value);
                    }
                }
            }

            filters.put(sceneCode, newFilter);

            DsaBloomFilterStats stats = getOrCreateStats(sceneCode);
            stats.setRebuildCount(stats.getRebuildCount() + 1);

            log.info("Bloom filter rebuilt: scene={}, count={}", sceneCode, allValues != null ? allValues.size() : 0);

        } catch (Exception e) {
            log.error("Bloom filter rebuild failed: scene={}", sceneCode, e);
        }
    }

    @Override
    public double getFalsePositiveRate(String sceneCode) {
        DsaBloomFilterStats stats = statsMap.get(sceneCode);
        if (stats == null) {
            return 0.0;
        }

        long checks = stats.getCheckCount();
        long misses = stats.getMissCount();

        if (checks == 0) {
            return 0.0;
        }

        return (double) misses / checks;
    }

    /**
     * 获取或创建布隆过滤器.
     *
     * @param sceneCode 场景编码
     * @return 布隆过滤器
     */
    private BloomFilter<String> getOrCreateFilter(String sceneCode) {
        return filters.computeIfAbsent(sceneCode, k -> BloomFilter.create(
                Funnels.stringFunnel(StandardCharsets.UTF_8),
                defaultExpectedInsertions,
                defaultFpp));
    }

    /**
     * 获取或创建统计信息.
     *
     * @param sceneCode 场景编码
     * @return 统计信息
     */
    private DsaBloomFilterStats getOrCreateStats(String sceneCode) {
        return statsMap.computeIfAbsent(sceneCode, DsaBloomFilterStats::new);
    }

    /**
     * 清空指定场景的布隆过滤器.
     *
     * @param sceneCode 场景编码
     */
    public void clear(String sceneCode) {
        filters.remove(sceneCode);
        statsMap.remove(sceneCode);
        log.info("Bloom filter cleared: scene={}", sceneCode);
    }

    /**
     * 获取统计信息.
     *
     * @param sceneCode 场景编码
     * @return 统计信息
     */
    public DsaBloomFilterStats getStats(String sceneCode) {
        return statsMap.get(sceneCode);
    }

    /**
     * 获取过滤器数量.
     *
     * @return 过滤器数量
     */
    public int getFilterCount() {
        return filters.size();
    }
}
