package io.github.dbsearchaccel.cache;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 滑动窗口计数器.
 * <p>
 * 用于统计指定时间窗口内的事件频率.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaSlidingWindowCounter {

    /**
     * 窗口大小（毫秒）.
     */
    private final long windowSizeMs;

    /**
     * 窗口数量.
     */
    private final int windowCount;

    /**
     * 事件时间戳队列.
     */
    private final ConcurrentLinkedDeque<Long> timestamps;

    /**
     * 总计数.
     */
    private final AtomicLong totalCount = new AtomicLong(0);

    /**
     * 构造方法.
     *
     * @param windowSizeMs 窗口大小（毫秒）
     * @param windowCount  窗口数量
     */
    public DsaSlidingWindowCounter(long windowSizeMs, int windowCount) {
        this.windowSizeMs = windowSizeMs > 0 ? windowSizeMs : 3600_000;
        this.windowCount = windowCount > 0 ? windowCount : 24;
        this.timestamps = new ConcurrentLinkedDeque<>();
    }

    /**
     * 增加计数.
     *
     * @param timestamp 时间戳
     */
    public void increment(long timestamp) {
        timestamps.addLast(timestamp);
        totalCount.incrementAndGet();
        cleanup(timestamp);
    }

    /**
     * 获取当前频率（次/小时）.
     *
     * @param currentTime 当前时间戳
     * @return 频率
     */
    public double getFrequency(long currentTime) {
        cleanup(currentTime);

        long count = totalCount.get();
        if (count == 0) {
            return 0.0;
        }

        return (double) count * 3600_000 / windowSizeMs;
    }

    /**
     * 清理过期数据.
     *
     * @param currentTime 当前时间戳
     */
    private void cleanup(long currentTime) {
        long threshold = currentTime - (long) windowCount * windowSizeMs;

        while (!timestamps.isEmpty()) {
            Long oldest = timestamps.peekFirst();
            if (oldest != null && oldest < threshold) {
                timestamps.pollFirst();
                totalCount.decrementAndGet();
            } else {
                break;
            }
        }
    }

    /**
     * 获取当前计数.
     *
     * @return 计数
     */
    public long getCount() {
        return totalCount.get();
    }

    /**
     * 重置计数器.
     */
    public void reset() {
        timestamps.clear();
        totalCount.set(0);
    }
}
