package io.github.dbsearchaccel.cache;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 布隆过滤器统计信息.
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaBloomFilterStats {

    private String sceneCode;

    private final AtomicLong putCount = new AtomicLong(0);
    private final AtomicLong checkCount = new AtomicLong(0);
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);
    private final AtomicLong rebuildCount = new AtomicLong(0);

    public DsaBloomFilterStats() {
    }

    public DsaBloomFilterStats(String sceneCode) {
        this.sceneCode = sceneCode;
    }

    public void incrementPutCount() {
        putCount.incrementAndGet();
    }

    public void addPutCount(long count) {
        putCount.addAndGet(count);
    }

    public void incrementCheckCount() {
        checkCount.incrementAndGet();
    }

    public void incrementHitCount() {
        hitCount.incrementAndGet();
    }

    public void incrementMissCount() {
        missCount.incrementAndGet();
    }

    public void incrementRebuildCount() {
        rebuildCount.incrementAndGet();
    }

    public double getHitRate() {
        long checks = checkCount.get();
        return checks > 0 ? (double) hitCount.get() / checks : 0.0;
    }

    public double getMissRate() {
        long checks = checkCount.get();
        return checks > 0 ? (double) missCount.get() / checks : 0.0;
    }

    public String getSceneCode() {
        return sceneCode;
    }

    public void setSceneCode(String sceneCode) {
        this.sceneCode = sceneCode;
    }

    public long getPutCount() {
        return putCount.get();
    }

    public long getCheckCount() {
        return checkCount.get();
    }

    public long getHitCount() {
        return hitCount.get();
    }

    public long getMissCount() {
        return missCount.get();
    }

    public long getRebuildCount() {
        return rebuildCount.get();
    }

    public void setRebuildCount(long rebuildCount) {
        this.rebuildCount.set(rebuildCount);
    }

    @Override
    public String toString() {
        return "DsaBloomFilterStats{" +
                "sceneCode='" + sceneCode + '\'' +
                ", putCount=" + putCount.get() +
                ", checkCount=" + checkCount.get() +
                ", hitCount=" + hitCount.get() +
                ", missCount=" + missCount.get() +
                ", hitRate=" + String.format("%.2f%%", getHitRate() * 100) +
                ", rebuildCount=" + rebuildCount.get() +
                '}';
    }
}
