package io.github.dbsearchaccel.core.bitmap;

import org.roaringbitmap.RoaringBitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 位图合并器.
 * <p>
 * 提供高效的位图交、并、差运算，支持批量操作.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaBitmapMerger {

    private static final Logger log = LoggerFactory.getLogger(DsaBitmapMerger.class);

    /**
     * 多位图交集运算.
     * 返回同时存在于所有位图中的元素.
     *
     * @param bitmaps 位图列表
     * @return 交集结果
     */
    public RoaringBitmap intersect(List<RoaringBitmap> bitmaps) {
        if (bitmaps == null || bitmaps.isEmpty()) {
            return new RoaringBitmap();
        }

        if (bitmaps.size() == 1) {
            return bitmaps.get(0).clone();
        }

        long startTime = System.currentTimeMillis();
        RoaringBitmap result = bitmaps.get(0).clone();

        for (int i = 1; i < bitmaps.size(); i++) {
            RoaringBitmap bitmap = bitmaps.get(i);
            if (bitmap == null || bitmap.isEmpty() || result.isEmpty()) {
                result = new RoaringBitmap();
                break;
            }
            result.and(bitmap);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.debug("Intersect {} bitmaps, result cardinality: {}, elapsed: {}ms",
                bitmaps.size(), result.getLongCardinality(), elapsed);

        return result;
    }

    /**
     * 多位图并集运算.
     * 返回存在于任意位图中的元素.
     *
     * @param bitmaps 位图列表
     * @return 并集结果
     */
    public RoaringBitmap union(List<RoaringBitmap> bitmaps) {
        if (bitmaps == null || bitmaps.isEmpty()) {
            return new RoaringBitmap();
        }

        if (bitmaps.size() == 1) {
            return bitmaps.get(0).clone();
        }

        long startTime = System.currentTimeMillis();
        RoaringBitmap result = new RoaringBitmap();

        for (RoaringBitmap bitmap : bitmaps) {
            if (bitmap != null && !bitmap.isEmpty()) {
                result.or(bitmap);
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.debug("Union {} bitmaps, result cardinality: {}, elapsed: {}ms",
                bitmaps.size(), result.getLongCardinality(), elapsed);

        return result;
    }

    /**
     * 位图差集运算.
     * 返回在第一个位图中但不在其他位图中的元素.
     *
     * @param include 包含的位图
     * @param exclude 排除的位图列表
     * @return 差集结果
     */
    public RoaringBitmap difference(RoaringBitmap include, List<RoaringBitmap> exclude) {
        if (include == null || include.isEmpty()) {
            return new RoaringBitmap();
        }

        if (exclude == null || exclude.isEmpty()) {
            return include.clone();
        }

        long startTime = System.currentTimeMillis();
        RoaringBitmap result = include.clone();

        for (RoaringBitmap bitmap : exclude) {
            if (bitmap != null && !bitmap.isEmpty()) {
                result.andNot(bitmap);
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.debug("Difference operation, result cardinality: {}, elapsed: {}ms",
                result.getLongCardinality(), elapsed);

        return result;
    }

    /**
     * 位图异或运算.
     * 返回仅存在于一个位图中的元素.
     *
     * @param bitmaps 位图列表
     * @return 异或结果
     */
    public RoaringBitmap xor(List<RoaringBitmap> bitmaps) {
        if (bitmaps == null || bitmaps.isEmpty()) {
            return new RoaringBitmap();
        }

        if (bitmaps.size() == 1) {
            return bitmaps.get(0).clone();
        }

        long startTime = System.currentTimeMillis();
        RoaringBitmap result = new RoaringBitmap();

        for (RoaringBitmap bitmap : bitmaps) {
            if (bitmap != null) {
                result.xor(bitmap);
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.debug("XOR {} bitmaps, result cardinality: {}, elapsed: {}ms",
                bitmaps.size(), result.getLongCardinality(), elapsed);

        return result;
    }

    /**
     * 计算两个位图的Jaccard相似度.
     *
     * @param bitmap1 位图1
     * @param bitmap2 位图2
     * @return Jaccard相似度（0-1）
     */
    public double jaccardSimilarity(RoaringBitmap bitmap1, RoaringBitmap bitmap2) {
        if (bitmap1 == null || bitmap2 == null) {
            return 0.0;
        }

        if (bitmap1.isEmpty() && bitmap2.isEmpty()) {
            return 1.0;
        }

        if (bitmap1.isEmpty() || bitmap2.isEmpty()) {
            return 0.0;
        }

        long intersectionSize = RoaringBitmap.andCardinality(bitmap1, bitmap2);
        long unionSize = RoaringBitmap.orCardinality(bitmap1, bitmap2);

        if (unionSize == 0) {
            return 0.0;
        }

        return (double) intersectionSize / unionSize;
    }

    /**
     * 计算位图优化后的内存大小.
     *
     * @param bitmap 位图
     * @return 内存大小（字节）
     */
    public long estimateMemorySize(RoaringBitmap bitmap) {
        if (bitmap == null) {
            return 0;
        }
        return bitmap.getSizeInBytes();
    }

    /**
     * 优化位图（压缩存储）.
     *
     * @param bitmap 位图
     * @return 优化后的位图
     */
    public RoaringBitmap optimize(RoaringBitmap bitmap) {
        if (bitmap == null) {
            return new RoaringBitmap();
        }
        bitmap.runOptimize();
        return bitmap;
    }
}
