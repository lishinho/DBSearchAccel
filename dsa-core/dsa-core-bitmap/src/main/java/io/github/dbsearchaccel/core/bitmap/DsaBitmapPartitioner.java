package io.github.dbsearchaccel.core.bitmap;

import org.roaringbitmap.RoaringBitmap;

import java.util.HashMap;
import java.util.Map;

/**
 * 位图分区器.
 * <p>
 * 实现动态位图分区，支持亿级文档的高效存储与计算.
 * 每个分区最多容纳指定数量的文档，通过分区降低单次操作内存占用.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaBitmapPartitioner {

    /**
     * 默认分区大小：每个分区最多容纳的文档数.
     */
    private static final int DEFAULT_PARTITION_SIZE = 1_000_000;

    /**
     * 最大分区数.
     */
    private static final int MAX_PARTITIONS = 1000;

    /**
     * 分区大小.
     */
    private final int partitionSize;

    /**
     * 构造方法（使用默认分区大小）.
     */
    public DsaBitmapPartitioner() {
        this(DEFAULT_PARTITION_SIZE);
    }

    /**
     * 构造方法.
     *
     * @param partitionSize 分区大小
     */
    public DsaBitmapPartitioner(int partitionSize) {
        if (partitionSize <= 0) {
            partitionSize = DEFAULT_PARTITION_SIZE;
        }
        this.partitionSize = partitionSize;
    }

    /**
     * 计算文档所属分区.
     *
     * @param docId 文档ID
     * @return 分区号
     */
    public int getPartition(long docId) {
        if (docId < 0) {
            throw new IllegalArgumentException("docId must be non-negative");
        }
        int partition = (int) (docId / partitionSize);
        if (partition >= MAX_PARTITIONS) {
            throw new IllegalArgumentException("docId exceeds maximum partition limit");
        }
        return partition;
    }

    /**
     * 计算分区内偏移.
     *
     * @param docId 文档ID
     * @return 分区内偏移
     */
    public int getOffset(long docId) {
        return (int) (docId % partitionSize);
    }

    /**
     * 根据分区号和偏移量还原文档ID.
     *
     * @param partition 分区号
     * @param offset    分区内偏移
     * @return 文档ID
     */
    public long getDocId(int partition, int offset) {
        return (long) partition * partitionSize + offset;
    }

    /**
     * 将文档ID列表按分区分组.
     *
     * @param docIds 文档ID列表
     * @return 分区到文档ID列表的映射
     */
    public Map<Integer, RoaringBitmap> partitionDocIds(Iterable<Long> docIds) {
        Map<Integer, RoaringBitmap> result = new HashMap<>();
        for (Long docId : docIds) {
            int partition = getPartition(docId);
            int offset = getOffset(docId);
            result.computeIfAbsent(partition, k -> new RoaringBitmap()).add(offset);
        }
        return result;
    }

    /**
     * 合并多分区位图.
     *
     * @param partitions 分区位图Map
     * @return 合并后的位图（包含完整文档ID）
     */
    public RoaringBitmap mergePartitions(Map<Integer, RoaringBitmap> partitions) {
        RoaringBitmap result = new RoaringBitmap();
        if (partitions == null || partitions.isEmpty()) {
            return result;
        }
        for (Map.Entry<Integer, RoaringBitmap> entry : partitions.entrySet()) {
            int partition = entry.getKey();
            RoaringBitmap bitmap = entry.getValue();
            if (bitmap != null && !bitmap.isEmpty()) {
                for (Integer offset : bitmap) {
                    result.add((int) getDocId(partition, offset));
                }
            }
        }
        return result;
    }

    /**
     * 将完整位图拆分为分区位图.
     *
     * @param bitmap 完整位图
     * @return 分区位图Map
     */
    public Map<Integer, RoaringBitmap> splitToPartitions(RoaringBitmap bitmap) {
        Map<Integer, RoaringBitmap> result = new HashMap<>();
        if (bitmap == null || bitmap.isEmpty()) {
            return result;
        }
        for (Integer docId : bitmap) {
            int partition = getPartition(docId);
            int offset = getOffset(docId);
            result.computeIfAbsent(partition, k -> new RoaringBitmap()).add(offset);
        }
        return result;
    }

    /**
     * 获取分区大小.
     *
     * @return 分区大小
     */
    public int getPartitionSize() {
        return partitionSize;
    }

    /**
     * 计算预计分区数量.
     *
     * @param maxDocId 最大文档ID
     * @return 预计分区数量
     */
    public int estimatePartitionCount(long maxDocId) {
        return (int) Math.ceil((double) (maxDocId + 1) / partitionSize);
    }
}
