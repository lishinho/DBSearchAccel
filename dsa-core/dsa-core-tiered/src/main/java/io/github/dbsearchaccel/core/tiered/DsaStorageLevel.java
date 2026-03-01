package io.github.dbsearchaccel.core.tiered;

/**
 * 存储层级枚举.
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public enum DsaStorageLevel {

    /**
     * 热数据层：内存.
     * 延迟：<1ms
     * 命中率目标：60%+
     */
    HOT(1, "memory", "<1ms"),

    /**
     * 温数据层：SSD.
     * 延迟：1-5ms
     * 命中率目标：30%+
     */
    WARM(2, "ssd", "1-5ms"),

    /**
     * 冷数据层：ES.
     * 延迟：10-50ms
     * 命中率目标：10%
     */
    COLD(3, "elasticsearch", "10-50ms");

    private final int level;
    private final String description;
    private final String latency;

    DsaStorageLevel(int level, String description, String latency) {
        this.level = level;
        this.description = description;
        this.latency = latency;
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    public String getLatency() {
        return latency;
    }

    /**
     * 根据层级值获取枚举.
     *
     * @param level 层级值
     * @return 存储层级枚举
     */
    public static DsaStorageLevel fromLevel(int level) {
        for (DsaStorageLevel storageLevel : values()) {
            if (storageLevel.level == level) {
                return storageLevel;
            }
        }
        return COLD;
    }
}
