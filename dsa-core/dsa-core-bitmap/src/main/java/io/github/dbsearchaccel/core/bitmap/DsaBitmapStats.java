package io.github.dbsearchaccel.core.bitmap;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 位图索引统计信息.
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaBitmapStats {

    /**
     * 场景编码.
     */
    private String sceneCode;

    /**
     * 索引字段数量.
     */
    private AtomicLong fieldCount = new AtomicLong(0);

    /**
     * 位图数量.
     */
    private AtomicLong bitmapCount = new AtomicLong(0);

    /**
     * 总文档数.
     */
    private AtomicLong totalDocs = new AtomicLong(0);

    /**
     * 内存占用（字节）.
     */
    private AtomicLong memoryBytes = new AtomicLong(0);

    /**
     * 查询次数.
     */
    private AtomicLong queryCount = new AtomicLong(0);

    /**
     * 命中次数.
     */
    private AtomicLong hitCount = new AtomicLong(0);

    public DsaBitmapStats() {
    }

    public DsaBitmapStats(String sceneCode) {
        this.sceneCode = sceneCode;
    }

    public String getSceneCode() {
        return sceneCode;
    }

    public void setSceneCode(String sceneCode) {
        this.sceneCode = sceneCode;
    }

    public long getFieldCount() {
        return fieldCount.get();
    }

    public void incrementFieldCount() {
        this.fieldCount.incrementAndGet();
    }

    public void decrementFieldCount() {
        this.fieldCount.decrementAndGet();
    }

    public long getBitmapCount() {
        return bitmapCount.get();
    }

    public void incrementBitmapCount() {
        this.bitmapCount.incrementAndGet();
    }

    public void decrementBitmapCount() {
        this.bitmapCount.decrementAndGet();
    }

    public long getTotalDocs() {
        return totalDocs.get();
    }

    public void addToTotalDocs(long count) {
        this.totalDocs.addAndGet(count);
    }

    public long getMemoryBytes() {
        return memoryBytes.get();
    }

    public void addToMemoryBytes(long bytes) {
        this.memoryBytes.addAndGet(bytes);
    }

    public long getQueryCount() {
        return queryCount.get();
    }

    public void incrementQueryCount() {
        this.queryCount.incrementAndGet();
    }

    public long getHitCount() {
        return hitCount.get();
    }

    public void incrementHitCount() {
        this.hitCount.incrementAndGet();
    }

    public double getHitRate() {
        long queries = queryCount.get();
        if (queries == 0) {
            return 0.0;
        }
        return (double) hitCount.get() / queries;
    }

    public double getMemoryMB() {
        return memoryBytes.get() / (1024.0 * 1024.0);
    }

    @Override
    public String toString() {
        return "DsaBitmapStats{" +
                "sceneCode='" + sceneCode + '\'' +
                ", fieldCount=" + fieldCount.get() +
                ", bitmapCount=" + bitmapCount.get() +
                ", totalDocs=" + totalDocs.get() +
                ", memoryMB=" + String.format("%.2f", getMemoryMB()) +
                ", queryCount=" + queryCount.get() +
                ", hitRate=" + String.format("%.2f%%", getHitRate() * 100) +
                '}';
    }
}
