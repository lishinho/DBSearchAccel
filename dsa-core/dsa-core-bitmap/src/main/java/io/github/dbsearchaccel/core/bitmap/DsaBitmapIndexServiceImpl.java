package io.github.dbsearchaccel.core.bitmap;

import org.roaringbitmap.RoaringBitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 位图索引服务实现类.
 * <p>
 * 基于内存的Roaring Bitmap实现，支持高性能的倒排索引查询.
 * 采用ConcurrentHashMap保证线程安全，支持分区存储降低内存碎片.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaBitmapIndexServiceImpl implements DsaBitmapIndexService {

    private static final Logger log = LoggerFactory.getLogger(DsaBitmapIndexServiceImpl.class);

    /**
     * 索引存储结构.
     * Map<sceneCode, Map<indexKey, RoaringBitmap>>
     * indexKey = fieldName:fieldValue
     */
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, RoaringBitmap>> indexStore;

    /**
     * 场景统计信息.
     */
    private final ConcurrentHashMap<String, DsaBitmapStats> statsMap;

    /**
     * 位图分区器.
     */
    private final DsaBitmapPartitioner partitioner;

    /**
     * 位图合并器.
     */
    private final DsaBitmapMerger merger;

    /**
     * 配置.
     */
    private final DsaBitmapConfig config;

    /**
     * 构造方法（使用默认配置）.
     */
    public DsaBitmapIndexServiceImpl() {
        this(new DsaBitmapConfig());
    }

    /**
     * 构造方法.
     *
     * @param config 配置
     */
    public DsaBitmapIndexServiceImpl(DsaBitmapConfig config) {
        this.config = config != null ? config : new DsaBitmapConfig();
        this.indexStore = new ConcurrentHashMap<>();
        this.statsMap = new ConcurrentHashMap<>();
        this.partitioner = new DsaBitmapPartitioner(this.config.getPartitionSize());
        this.merger = new DsaBitmapMerger();
        log.info("DsaBitmapIndexService initialized, partitionSize: {}", this.config.getPartitionSize());
    }

    @Override
    public boolean buildIndex(String sceneCode, String fieldName, Object fieldValue, List<Long> docIds) {
        if (sceneCode == null || fieldName == null || fieldValue == null || docIds == null) {
            return false;
        }

        try {
            String indexKey = buildIndexKey(fieldName, fieldValue);
            RoaringBitmap bitmap = createBitmap(docIds);

            ConcurrentHashMap<String, RoaringBitmap> sceneIndex = indexStore.computeIfAbsent(
                    sceneCode, k -> new ConcurrentHashMap<>());

            RoaringBitmap oldBitmap = sceneIndex.put(indexKey, bitmap);

            updateStats(sceneCode, docIds.size(), bitmap.getSizeInBytes(), oldBitmap != null);

            log.debug("Built index: scene={}, key={}, docCount={}",
                    sceneCode, indexKey, docIds.size());

            return true;

        } catch (Exception e) {
            log.error("Build index failed: scene={}, field={}, value={}",
                    sceneCode, fieldName, fieldValue, e);
            return false;
        }
    }

    @Override
    public int buildIndexBatch(String sceneCode, String fieldName, Map<Object, List<Long>> valueToIds) {
        if (sceneCode == null || fieldName == null || valueToIds == null || valueToIds.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (Map.Entry<Object, List<Long>> entry : valueToIds.entrySet()) {
            if (buildIndex(sceneCode, fieldName, entry.getKey(), entry.getValue())) {
                successCount++;
            }
        }

        log.info("Batch built index: scene={}, field={}, successCount={}/{}",
                sceneCode, fieldName, successCount, valueToIds.size());

        return successCount;
    }

    @Override
    public RoaringBitmap queryBitmap(String sceneCode, String fieldName, Object fieldValue) {
        DsaBitmapStats stats = getOrCreateStats(sceneCode);
        stats.incrementQueryCount();

        if (sceneCode == null || fieldName == null || fieldValue == null) {
            return new RoaringBitmap();
        }

        try {
            String indexKey = buildIndexKey(fieldName, fieldValue);
            ConcurrentHashMap<String, RoaringBitmap> sceneIndex = indexStore.get(sceneCode);

            if (sceneIndex == null) {
                log.debug("Scene not found: {}", sceneCode);
                return new RoaringBitmap();
            }

            RoaringBitmap bitmap = sceneIndex.get(indexKey);
            if (bitmap != null) {
                stats.incrementHitCount();
                log.debug("Query bitmap hit: scene={}, key={}, cardinality={}",
                        sceneCode, indexKey, bitmap.getLongCardinality());
                return bitmap.clone();
            }

            log.debug("Query bitmap miss: scene={}, key={}", sceneCode, indexKey);
            return new RoaringBitmap();

        } catch (Exception e) {
            log.error("Query bitmap failed: scene={}, field={}, value={}",
                    sceneCode, fieldName, fieldValue, e);
            return new RoaringBitmap();
        }
    }

    @Override
    public RoaringBitmap intersect(String sceneCode, Map<String, Object> conditions) {
        if (sceneCode == null || conditions == null || conditions.isEmpty()) {
            return new RoaringBitmap();
        }

        List<RoaringBitmap> bitmaps = new ArrayList<>();
        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            RoaringBitmap bitmap = queryBitmap(sceneCode, entry.getKey(), entry.getValue());
            if (bitmap.isEmpty()) {
                return new RoaringBitmap();
            }
            bitmaps.add(bitmap);
        }

        return merger.intersect(bitmaps);
    }

    @Override
    public RoaringBitmap union(String sceneCode, Map<String, Object> conditions) {
        if (sceneCode == null || conditions == null || conditions.isEmpty()) {
            return new RoaringBitmap();
        }

        List<RoaringBitmap> bitmaps = new ArrayList<>();
        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            RoaringBitmap bitmap = queryBitmap(sceneCode, entry.getKey(), entry.getValue());
            if (!bitmap.isEmpty()) {
                bitmaps.add(bitmap);
            }
        }

        return merger.union(bitmaps);
    }

    @Override
    public RoaringBitmap difference(String sceneCode, Map<String, Object> include, Map<String, Object> exclude) {
        if (sceneCode == null || include == null || include.isEmpty()) {
            return new RoaringBitmap();
        }

        RoaringBitmap includeBitmap = intersect(sceneCode, include);
        if (includeBitmap.isEmpty()) {
            return new RoaringBitmap();
        }

        if (exclude == null || exclude.isEmpty()) {
            return includeBitmap;
        }

        List<RoaringBitmap> excludeBitmaps = new ArrayList<>();
        for (Map.Entry<String, Object> entry : exclude.entrySet()) {
            RoaringBitmap bitmap = queryBitmap(sceneCode, entry.getKey(), entry.getValue());
            if (!bitmap.isEmpty()) {
                excludeBitmaps.add(bitmap);
            }
        }

        return merger.difference(includeBitmap, excludeBitmaps);
    }

    @Override
    public List<Long> bitmapToIds(RoaringBitmap bitmap, int offset, int limit) {
        List<Long> result = new ArrayList<>();
        if (bitmap == null || bitmap.isEmpty()) {
            return result;
        }

        long cardinality = bitmap.getLongCardinality();
        if (offset >= cardinality) {
            return result;
        }

        int count = 0;
        int added = 0;
        for (Integer docId : bitmap) {
            if (count >= offset) {
                result.add(docId.longValue());
                added++;
                if (added >= limit) {
                    break;
                }
            }
            count++;
        }

        return result;
    }

    @Override
    public long getCardinality(RoaringBitmap bitmap) {
        if (bitmap == null) {
            return 0;
        }
        return bitmap.getLongCardinality();
    }

    @Override
    public boolean addToIndex(String sceneCode, String fieldName, Object fieldValue, List<Long> docIds) {
        if (sceneCode == null || fieldName == null || fieldValue == null || docIds == null || docIds.isEmpty()) {
            return false;
        }

        try {
            String indexKey = buildIndexKey(fieldName, fieldValue);
            ConcurrentHashMap<String, RoaringBitmap> sceneIndex = indexStore.get(sceneCode);

            if (sceneIndex == null) {
                return buildIndex(sceneCode, fieldName, fieldValue, docIds);
            }

            RoaringBitmap bitmap = sceneIndex.computeIfAbsent(indexKey, k -> new RoaringBitmap());
            for (Long docId : docIds) {
                bitmap.add(docId.intValue());
            }

            getOrCreateStats(sceneCode).addToTotalDocs(docIds.size());

            log.debug("Added to index: scene={}, key={}, count={}", sceneCode, indexKey, docIds.size());
            return true;

        } catch (Exception e) {
            log.error("Add to index failed: scene={}, field={}, value={}",
                    sceneCode, fieldName, fieldValue, e);
            return false;
        }
    }

    @Override
    public boolean removeFromIndex(String sceneCode, String fieldName, Object fieldValue, List<Long> docIds) {
        if (sceneCode == null || fieldName == null || fieldValue == null || docIds == null || docIds.isEmpty()) {
            return false;
        }

        try {
            String indexKey = buildIndexKey(fieldName, fieldValue);
            ConcurrentHashMap<String, RoaringBitmap> sceneIndex = indexStore.get(sceneCode);

            if (sceneIndex == null) {
                return true;
            }

            RoaringBitmap bitmap = sceneIndex.get(indexKey);
            if (bitmap != null) {
                for (Long docId : docIds) {
                    bitmap.remove(docId.intValue());
                }
            }

            log.debug("Removed from index: scene={}, key={}, count={}", sceneCode, indexKey, docIds.size());
            return true;

        } catch (Exception e) {
            log.error("Remove from index failed: scene={}, field={}, value={}",
                    sceneCode, fieldName, fieldValue, e);
            return false;
        }
    }

    @Override
    public boolean deleteIndex(String sceneCode, String fieldName, Object fieldValue) {
        if (sceneCode == null || fieldName == null || fieldValue == null) {
            return false;
        }

        try {
            String indexKey = buildIndexKey(fieldName, fieldValue);
            ConcurrentHashMap<String, RoaringBitmap> sceneIndex = indexStore.get(sceneCode);

            if (sceneIndex != null) {
                RoaringBitmap removed = sceneIndex.remove(indexKey);
                if (removed != null) {
                    getOrCreateStats(sceneCode).decrementBitmapCount();
                    log.debug("Deleted index: scene={}, key={}", sceneCode, indexKey);
                }
            }

            return true;

        } catch (Exception e) {
            log.error("Delete index failed: scene={}, field={}, value={}",
                    sceneCode, fieldName, fieldValue, e);
            return false;
        }
    }

    @Override
    public boolean clearScene(String sceneCode) {
        if (sceneCode == null) {
            return false;
        }

        try {
            indexStore.remove(sceneCode);
            statsMap.remove(sceneCode);
            log.info("Cleared scene: {}", sceneCode);
            return true;

        } catch (Exception e) {
            log.error("Clear scene failed: scene={}", sceneCode, e);
            return false;
        }
    }

    @Override
    public DsaBitmapStats getStats(String sceneCode) {
        return getOrCreateStats(sceneCode);
    }

    /**
     * 构建索引键.
     *
     * @param fieldName  字段名
     * @param fieldValue 字段值
     * @return 索引键
     */
    private String buildIndexKey(String fieldName, Object fieldValue) {
        return fieldName + ":" + String.valueOf(fieldValue);
    }

    /**
     * 创建位图.
     *
     * @param docIds 文档ID列表
     * @return 位图
     */
    private RoaringBitmap createBitmap(List<Long> docIds) {
        RoaringBitmap bitmap = new RoaringBitmap();
        for (Long docId : docIds) {
            bitmap.add(docId.intValue());
        }
        bitmap.runOptimize();
        return bitmap;
    }

    /**
     * 获取或创建统计信息.
     *
     * @param sceneCode 场景编码
     * @return 统计信息
     */
    private DsaBitmapStats getOrCreateStats(String sceneCode) {
        return statsMap.computeIfAbsent(sceneCode, DsaBitmapStats::new);
    }

    /**
     * 更新统计信息.
     *
     * @param sceneCode   场景编码
     * @param docCount    文档数量
     * @param memoryBytes 内存占用
     * @param isUpdate    是否更新
     */
    private void updateStats(String sceneCode, int docCount, long memoryBytes, boolean isUpdate) {
        DsaBitmapStats stats = getOrCreateStats(sceneCode);
        if (!isUpdate) {
            stats.incrementBitmapCount();
        }
        stats.addToTotalDocs(docCount);
        stats.addToMemoryBytes(memoryBytes);
    }

    /**
     * 获取所有场景的统计信息.
     *
     * @return 场景编码到统计信息的映射
     */
    public Map<String, DsaBitmapStats> getAllStats() {
        return new HashMap<>(statsMap);
    }

    /**
     * 获取场景的索引键数量.
     *
     * @param sceneCode 场景编码
     * @return 索引键数量
     */
    public int getIndexCount(String sceneCode) {
        ConcurrentHashMap<String, RoaringBitmap> sceneIndex = indexStore.get(sceneCode);
        return sceneIndex != null ? sceneIndex.size() : 0;
    }
}
