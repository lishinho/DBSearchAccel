package io.github.dbsearchaccel.core.tiered;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import org.roaringbitmap.RoaringBitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 内存热缓存.
 * <p>
 * 基于Guava Cache实现L1热数据缓存，支持LRU淘汰策略.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaHotCache {

    private static final Logger log = LoggerFactory.getLogger(DsaHotCache.class);

    /**
     * 默认最大缓存条目数.
     */
    private static final int DEFAULT_MAXIMUM_SIZE = 10000;

    /**
     * 默认过期时间（秒）.
     */
    private static final int DEFAULT_EXPIRE_SECONDS = 300;

    /**
     * Guava缓存实例.
     */
    private final Cache<String, RoaringBitmap> cache;

    /**
     * 最大缓存条目数.
     */
    private final int maximumSize;

    /**
     * 构造方法（使用默认配置）.
     */
    public DsaHotCache() {
        this(DEFAULT_MAXIMUM_SIZE, DEFAULT_EXPIRE_SECONDS);
    }

    /**
     * 构造方法.
     *
     * @param maximumSize  最大缓存条目数
     * @param expireSeconds 过期时间（秒）
     */
    public DsaHotCache(int maximumSize, int expireSeconds) {
        this.maximumSize = maximumSize > 0 ? maximumSize : DEFAULT_MAXIMUM_SIZE;
        int expire = expireSeconds > 0 ? expireSeconds : DEFAULT_EXPIRE_SECONDS;

        this.cache = CacheBuilder.newBuilder()
                .maximumSize(this.maximumSize)
                .expireAfterAccess(expire, TimeUnit.SECONDS)
                .recordStats()
                .build();

        log.info("DsaHotCache initialized, maximumSize: {}, expireSeconds: {}", this.maximumSize, expire);
    }

    /**
     * 获取位图.
     *
     * @param sceneCode 场景编码
     * @param indexKey  索引键
     * @return 位图，不存在返回null
     */
    public RoaringBitmap get(String sceneCode, String indexKey) {
        String key = buildKey(sceneCode, indexKey);
        RoaringBitmap bitmap = cache.getIfPresent(key);
        if (bitmap != null) {
            log.debug("Hot cache hit: {}", key);
        }
        return bitmap;
    }

    /**
     * 存入位图.
     *
     * @param sceneCode 场景编码
     * @param indexKey  索引键
     * @param bitmap    位图
     */
    public void put(String sceneCode, String indexKey, RoaringBitmap bitmap) {
        if (bitmap == null || bitmap.isEmpty()) {
            return;
        }
        String key = buildKey(sceneCode, indexKey);
        cache.put(key, bitmap.clone());
        log.debug("Hot cache put: {}, cardinality: {}", key, bitmap.getLongCardinality());
    }

    /**
     * 移除位图.
     *
     * @param sceneCode 场景编码
     * @param indexKey  索引键
     */
    public void remove(String sceneCode, String indexKey) {
        String key = buildKey(sceneCode, indexKey);
        cache.invalidate(key);
        log.debug("Hot cache remove: {}", key);
    }

    /**
     * 清空场景缓存.
     *
     * @param sceneCode 场景编码
     */
    public void clear(String sceneCode) {
        String prefix = sceneCode + ":";
        cache.asMap().keySet().removeIf(key -> key.startsWith(prefix));
        log.info("Hot cache cleared for scene: {}", sceneCode);
    }

    /**
     * 清空所有缓存.
     */
    public void clearAll() {
        cache.invalidateAll();
        log.info("Hot cache cleared all");
    }

    /**
     * 获取缓存大小.
     *
     * @return 缓存大小
     */
    public long size() {
        return cache.size();
    }

    /**
     * 估算内存占用.
     *
     * @return 内存占用（字节）
     */
    public long estimateMemoryBytes() {
        long totalBytes = 0;
        for (RoaringBitmap bitmap : cache.asMap().values()) {
            if (bitmap != null) {
                totalBytes += bitmap.getSizeInBytes();
            }
        }
        return totalBytes;
    }

    /**
     * 获取缓存统计信息.
     *
     * @return 统计信息
     */
    public CacheStats getStats() {
        return cache.stats();
    }

    /**
     * 获取命中率.
     *
     * @return 命中率
     */
    public double getHitRate() {
        return cache.stats().hitRate();
    }

    /**
     * 构建缓存键.
     *
     * @param sceneCode 场景编码
     * @param indexKey  索引键
     * @return 缓存键
     */
    private String buildKey(String sceneCode, String indexKey) {
        return sceneCode + ":" + indexKey;
    }

    public int getMaximumSize() {
        return maximumSize;
    }
}
