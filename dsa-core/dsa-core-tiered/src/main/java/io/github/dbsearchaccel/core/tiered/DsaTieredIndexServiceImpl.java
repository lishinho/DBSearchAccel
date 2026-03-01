package io.github.dbsearchaccel.core.tiered;

import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.buffer.ImmutableRoaringBitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 分层索引服务实现类.
 * <p>
 * 实现内存/SSD/ES三级存储架构，自动管理数据晋升与降级.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaTieredIndexServiceImpl implements DsaTieredIndexService {

    private static final Logger log = LoggerFactory.getLogger(DsaTieredIndexServiceImpl.class);

    /**
     * 热点判定阈值.
     */
    private static final int HOT_THRESHOLD = 10;

    /**
     * 热缓存.
     */
    private final DsaHotCache hotCache;

    /**
     * 温存储.
     */
    private final DsaWarmStore warmStore;

    /**
     * 统计信息.
     */
    private final ConcurrentHashMap<String, DsaTieredStats> statsMap;

    /**
     * 查询模式频率统计.
     */
    private final ConcurrentHashMap<String, DsaQueryPattern> patternStats;

    /**
     * 定时任务执行器.
     */
    private final ScheduledExecutorService scheduler;

    /**
     * 配置.
     */
    private final DsaTieredConfig config;

    /**
     * 构造方法.
     *
     * @param config 配置
     */
    public DsaTieredIndexServiceImpl(DsaTieredConfig config) {
        this.config = config != null ? config : new DsaTieredConfig();
        this.hotCache = new DsaHotCache(this.config.getHotCacheSize(), this.config.getHotCacheExpireSeconds());
        this.warmStore = new DsaWarmStore(this.config.getWarmStoreDir());
        this.statsMap = new ConcurrentHashMap<>();
        this.patternStats = new ConcurrentHashMap<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();

        startBackgroundTasks();

        log.info("DsaTieredIndexService initialized");
    }

    /**
     * 启动后台任务.
     */
    private void startBackgroundTasks() {
        scheduler.scheduleAtFixedRate(this::autoPromote, 1, 1, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(this::autoDemote, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public DsaTieredResult queryBitmap(String sceneCode, String fieldName, Object fieldValue) {
        DsaTieredStats stats = getOrCreateStats(sceneCode);
        stats.incrementQuery();

        String indexKey = buildIndexKey(fieldName, fieldValue);
        long startTime = System.currentTimeMillis();

        recordPattern(sceneCode, fieldName, fieldValue);

        RoaringBitmap bitmap = hotCache.get(sceneCode, indexKey);
        if (bitmap != null) {
            stats.incrementHotHit();
            return buildResult(bitmap, DsaStorageLevel.HOT, startTime, true, sceneCode, indexKey);
        }

        bitmap = warmStore.get(sceneCode, indexKey);
        if (bitmap != null) {
            stats.incrementWarmHit();
            hotCache.put(sceneCode, indexKey, bitmap);
            return buildResult(bitmap, DsaStorageLevel.WARM, startTime, true, sceneCode, indexKey);
        }

        stats.incrementColdHit();
        return buildResult(new RoaringBitmap(), DsaStorageLevel.COLD, startTime, false, sceneCode, indexKey);
    }

    @Override
    public List<DsaTieredResult> queryBitmapBatch(String sceneCode, Map<String, Object> conditions) {
        List<DsaTieredResult> results = new ArrayList<>();
        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            results.add(queryBitmap(sceneCode, entry.getKey(), entry.getValue()));
        }
        return results;
    }

    @Override
    public DsaTieredResult intersect(String sceneCode, Map<String, Object> conditions) {
        long startTime = System.currentTimeMillis();
        List<RoaringBitmap> bitmaps = new ArrayList<>();

        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            DsaTieredResult result = queryBitmap(sceneCode, entry.getKey(), entry.getValue());
            if (!result.isHit()) {
                return buildResult(new RoaringBitmap(), DsaStorageLevel.COLD, startTime, false, sceneCode, "intersect");
            }
            bitmaps.add(result.getBitmap());
        }

        RoaringBitmap result = bitmaps.get(0);
        for (int i = 1; i < bitmaps.size(); i++) {
            result = RoaringBitmap.and(result, bitmaps.get(i));
        }

        return buildResult(result, DsaStorageLevel.HOT, startTime, true, sceneCode, "intersect");
    }

    @Override
    public DsaTieredResult union(String sceneCode, Map<String, Object> conditions) {
        long startTime = System.currentTimeMillis();
        RoaringBitmap result = new RoaringBitmap();

        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            DsaTieredResult queryResult = queryBitmap(sceneCode, entry.getKey(), entry.getValue());
            if (queryResult.isHit()) {
                result.or(queryResult.getBitmap());
            }
        }

        return buildResult(result, DsaStorageLevel.HOT, startTime, true, sceneCode, "union");
    }

    @Override
    public void putBitmap(String sceneCode, String fieldName, Object fieldValue, RoaringBitmap bitmap, DsaStorageLevel level) {
        if (bitmap == null || bitmap.isEmpty()) {
            return;
        }

        String indexKey = buildIndexKey(fieldName, fieldValue);
        DsaTieredStats stats = getOrCreateStats(sceneCode);

        switch (level) {
            case HOT:
                hotCache.put(sceneCode, indexKey, bitmap);
                stats.incrementHotCache();
                stats.addHotCacheBytes(bitmap.getSizeInBytes());
                break;
            case WARM:
                warmStore.put(sceneCode, indexKey, bitmap);
                stats.incrementWarmStore();
                stats.addWarmStoreBytes(bitmap.getSizeInBytes());
                break;
            case COLD:
            default:
                break;
        }
    }

    @Override
    public boolean promote(String sceneCode, String indexKey, DsaStorageLevel fromLevel, DsaStorageLevel toLevel) {
        DsaTieredStats stats = getOrCreateStats(sceneCode);
        RoaringBitmap bitmap = null;

        switch (fromLevel) {
            case WARM:
                bitmap = warmStore.get(sceneCode, indexKey);
                break;
            case COLD:
                break;
            default:
                return false;
        }

        if (bitmap == null) {
            return false;
        }

        switch (toLevel) {
            case HOT:
                hotCache.put(sceneCode, indexKey, bitmap);
                stats.incrementPromote();
                log.debug("Promoted {} from {} to {}", indexKey, fromLevel, toLevel);
                return true;
            case WARM:
                warmStore.put(sceneCode, indexKey, bitmap);
                stats.incrementPromote();
                log.debug("Promoted {} from {} to {}", indexKey, fromLevel, toLevel);
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean demote(String sceneCode, String indexKey, DsaStorageLevel fromLevel, DsaStorageLevel toLevel) {
        DsaTieredStats stats = getOrCreateStats(sceneCode);

        switch (fromLevel) {
            case HOT:
                RoaringBitmap bitmap = hotCache.get(sceneCode, indexKey);
                if (bitmap == null) {
                    return false;
                }
                hotCache.remove(sceneCode, indexKey);
                if (toLevel == DsaStorageLevel.WARM) {
                    warmStore.put(sceneCode, indexKey, bitmap);
                }
                stats.incrementDemote();
                log.debug("Demoted {} from {} to {}", indexKey, fromLevel, toLevel);
                return true;
            case WARM:
                if (toLevel == DsaStorageLevel.COLD) {
                    warmStore.remove(sceneCode, indexKey);
                    stats.incrementDemote();
                    log.debug("Demoted {} from {} to {}", indexKey, fromLevel, toLevel);
                    return true;
                }
                break;
            default:
                break;
        }

        return false;
    }

    @Override
    public int warmup(String sceneCode, List<DsaQueryPattern> patterns) {
        int count = 0;
        for (DsaQueryPattern pattern : patterns) {
            String indexKey = buildIndexKey(pattern.getFieldName(), pattern.getFieldValue());

            RoaringBitmap bitmap = warmStore.get(sceneCode, indexKey);
            if (bitmap != null) {
                hotCache.put(sceneCode, indexKey, bitmap);
                count++;
            }
        }

        log.info("Warmup completed for scene: {}, count: {}", sceneCode, count);
        return count;
    }

    @Override
    public void clear(String sceneCode, DsaStorageLevel level) {
        switch (level) {
            case HOT:
                hotCache.clear(sceneCode);
                break;
            case WARM:
                warmStore.clear(sceneCode);
                break;
            case COLD:
                break;
        }
    }

    @Override
    public void clearAll(String sceneCode) {
        hotCache.clear(sceneCode);
        warmStore.clear(sceneCode);
        statsMap.remove(sceneCode);
        log.info("Cleared all data for scene: {}", sceneCode);
    }

    @Override
    public DsaTieredStats getStats(String sceneCode) {
        return getOrCreateStats(sceneCode);
    }

    @Override
    public Map<DsaStorageLevel, Long> getCacheSizes(String sceneCode) {
        Map<DsaStorageLevel, Long> sizes = new HashMap<>();
        sizes.put(DsaStorageLevel.HOT, hotCache.size());
        sizes.put(DsaStorageLevel.WARM, warmStore.count(sceneCode));
        sizes.put(DsaStorageLevel.COLD, 0L);
        return sizes;
    }

    /**
     * 自动晋升热点数据.
     */
    private void autoPromote() {
        for (Map.Entry<String, DsaQueryPattern> entry : patternStats.entrySet()) {
            DsaQueryPattern pattern = entry.getValue();
            if (pattern.isHot(HOT_THRESHOLD)) {
                String indexKey = buildIndexKey(pattern.getFieldName(), pattern.getFieldValue());
                promote(pattern.getSceneCode(), indexKey, DsaStorageLevel.WARM, DsaStorageLevel.HOT);
            }
        }
    }

    /**
     * 自动降级冷数据.
     */
    private void autoDemote() {
        long now = System.currentTimeMillis();
        long threshold = config.getDemoteThresholdMs();

        for (Map.Entry<String, DsaQueryPattern> entry : patternStats.entrySet()) {
            DsaQueryPattern pattern = entry.getValue();
            if (now - pattern.getLastAccessTime() > threshold) {
                String indexKey = buildIndexKey(pattern.getFieldName(), pattern.getFieldValue());
                demote(pattern.getSceneCode(), indexKey, DsaStorageLevel.HOT, DsaStorageLevel.WARM);
            }
        }
    }

    /**
     * 记录查询模式.
     */
    private void recordPattern(String sceneCode, String fieldName, Object fieldValue) {
        String key = sceneCode + ":" + fieldName + ":" + fieldValue;
        DsaQueryPattern pattern = patternStats.computeIfAbsent(key,
                k -> new DsaQueryPattern(sceneCode, fieldName, fieldValue));
        pattern.incrementFrequency();
    }

    /**
     * 构建索引键.
     */
    private String buildIndexKey(String fieldName, Object fieldValue) {
        return fieldName + ":" + String.valueOf(fieldValue);
    }

    /**
     * 构建结果.
     */
    private DsaTieredResult buildResult(RoaringBitmap bitmap, DsaStorageLevel level,
                                         long startTime, boolean fromCache,
                                         String sceneCode, String indexKey) {
        DsaTieredResult result = new DsaTieredResult(bitmap, level);
        result.setQueryTimeMs(System.currentTimeMillis() - startTime);
        result.setFromCache(fromCache);
        result.setSceneCode(sceneCode);
        result.setIndexKey(indexKey);
        return result;
    }

    /**
     * 获取或创建统计信息.
     */
    private DsaTieredStats getOrCreateStats(String sceneCode) {
        return statsMap.computeIfAbsent(sceneCode, DsaTieredStats::new);
    }

    /**
     * 关闭服务.
     */
    public void shutdown() {
        scheduler.shutdown();
        warmStore.close();
        log.info("DsaTieredIndexService shutdown");
    }
}
