package io.github.dbsearchaccel.core.tiered;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 分层存储统计信息.
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaTieredStats {

    private String sceneCode;

    private final AtomicLong hotCacheCount = new AtomicLong(0);
    private final AtomicLong hotCacheBytes = new AtomicLong(0);
    private final AtomicLong warmStoreCount = new AtomicLong(0);
    private final AtomicLong warmStoreBytes = new AtomicLong(0);

    private final AtomicLong totalQueryCount = new AtomicLong(0);
    private final AtomicLong hotHitCount = new AtomicLong(0);
    private final AtomicLong warmHitCount = new AtomicLong(0);
    private final AtomicLong coldHitCount = new AtomicLong(0);

    private final AtomicLong promoteCount = new AtomicLong(0);
    private final AtomicLong demoteCount = new AtomicLong(0);

    public DsaTieredStats() {
    }

    public DsaTieredStats(String sceneCode) {
        this.sceneCode = sceneCode;
    }

    public void incrementHotCache() {
        hotCacheCount.incrementAndGet();
    }

    public void decrementHotCache() {
        hotCacheCount.decrementAndGet();
    }

    public void addHotCacheBytes(long bytes) {
        hotCacheBytes.addAndGet(bytes);
    }

    public void incrementWarmStore() {
        warmStoreCount.incrementAndGet();
    }

    public void decrementWarmStore() {
        warmStoreCount.decrementAndGet();
    }

    public void addWarmStoreBytes(long bytes) {
        warmStoreBytes.addAndGet(bytes);
    }

    public void incrementQuery() {
        totalQueryCount.incrementAndGet();
    }

    public void incrementHotHit() {
        hotHitCount.incrementAndGet();
    }

    public void incrementWarmHit() {
        warmHitCount.incrementAndGet();
    }

    public void incrementColdHit() {
        coldHitCount.incrementAndGet();
    }

    public void incrementPromote() {
        promoteCount.incrementAndGet();
    }

    public void incrementDemote() {
        demoteCount.incrementAndGet();
    }

    public double getHotHitRate() {
        long total = totalQueryCount.get();
        return total > 0 ? (double) hotHitCount.get() / total : 0.0;
    }

    public double getWarmHitRate() {
        long total = totalQueryCount.get();
        return total > 0 ? (double) warmHitCount.get() / total : 0.0;
    }

    public double getColdHitRate() {
        long total = totalQueryCount.get();
        return total > 0 ? (double) coldHitCount.get() / total : 0.0;
    }

    public double getTotalHitRate() {
        long total = totalQueryCount.get();
        long hits = hotHitCount.get() + warmHitCount.get() + coldHitCount.get();
        return total > 0 ? (double) hits / total : 0.0;
    }

    public double getHotCacheMB() {
        return hotCacheBytes.get() / (1024.0 * 1024.0);
    }

    public double getWarmStoreMB() {
        return warmStoreBytes.get() / (1024.0 * 1024.0);
    }

    public String getSceneCode() {
        return sceneCode;
    }

    public void setSceneCode(String sceneCode) {
        this.sceneCode = sceneCode;
    }

    public long getHotCacheCount() {
        return hotCacheCount.get();
    }

    public long getHotCacheBytes() {
        return hotCacheBytes.get();
    }

    public long getWarmStoreCount() {
        return warmStoreCount.get();
    }

    public long getWarmStoreBytes() {
        return warmStoreBytes.get();
    }

    public long getTotalQueryCount() {
        return totalQueryCount.get();
    }

    public long getHotHitCount() {
        return hotHitCount.get();
    }

    public long getWarmHitCount() {
        return warmHitCount.get();
    }

    public long getColdHitCount() {
        return coldHitCount.get();
    }

    public long getPromoteCount() {
        return promoteCount.get();
    }

    public long getDemoteCount() {
        return demoteCount.get();
    }

    @Override
    public String toString() {
        return "DsaTieredStats{" +
                "sceneCode='" + sceneCode + '\'' +
                ", hotCacheCount=" + hotCacheCount.get() +
                ", hotCacheMB=" + String.format("%.2f", getHotCacheMB()) +
                ", warmStoreCount=" + warmStoreCount.get() +
                ", warmStoreMB=" + String.format("%.2f", getWarmStoreMB()) +
                ", totalQueryCount=" + totalQueryCount.get() +
                ", hotHitRate=" + String.format("%.2f%%", getHotHitRate() * 100) +
                ", warmHitRate=" + String.format("%.2f%%", getWarmHitRate() * 100) +
                ", coldHitRate=" + String.format("%.2f%%", getColdHitRate() * 100) +
                ", promoteCount=" + promoteCount.get() +
                ", demoteCount=" + demoteCount.get() +
                '}';
    }
}
