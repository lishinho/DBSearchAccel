package io.github.dbsearchaccel.cache;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存补偿统计信息.
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaCompensateStats {

    private String sceneCode;

    private final AtomicLong detectCount = new AtomicLong(0);
    private final AtomicLong compensateCount = new AtomicLong(0);
    private final AtomicLong failCount = new AtomicLong(0);
    private final AtomicLong lastDetectTime = new AtomicLong(0);

    public DsaCompensateStats() {
    }

    public DsaCompensateStats(String sceneCode) {
        this.sceneCode = sceneCode;
    }

    public void incrementDetectCount() {
        detectCount.incrementAndGet();
    }

    public void addDetectCount(long count) {
        detectCount.addAndGet(count);
    }

    public void incrementCompensateCount() {
        compensateCount.incrementAndGet();
    }

    public void addCompensateCount(long count) {
        compensateCount.addAndGet(count);
    }

    public void incrementFailCount() {
        failCount.incrementAndGet();
    }

    public void updateLastDetectTime() {
        lastDetectTime.set(System.currentTimeMillis());
    }

    public double getSuccessRate() {
        long total = compensateCount.get() + failCount.get();
        return total > 0 ? (double) compensateCount.get() / total : 0.0;
    }

    public String getSceneCode() {
        return sceneCode;
    }

    public void setSceneCode(String sceneCode) {
        this.sceneCode = sceneCode;
    }

    public long getDetectCount() {
        return detectCount.get();
    }

    public long getCompensateCount() {
        return compensateCount.get();
    }

    public long getFailCount() {
        return failCount.get();
    }

    public long getLastDetectTime() {
        return lastDetectTime.get();
    }

    @Override
    public String toString() {
        return "DsaCompensateStats{" +
                "sceneCode='" + sceneCode + '\'' +
                ", detectCount=" + detectCount.get() +
                ", compensateCount=" + compensateCount.get() +
                ", failCount=" + failCount.get() +
                ", successRate=" + String.format("%.2f%%", getSuccessRate() * 100) +
                '}';
    }
}
