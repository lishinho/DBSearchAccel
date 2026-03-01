package io.github.dbsearchaccel.core.bitmap;

/**
 * 位图索引配置.
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaBitmapConfig {

    /**
     * 默认分区大小.
     */
    private static final int DEFAULT_PARTITION_SIZE = 1_000_000;

    /**
     * 默认最大场景数.
     */
    private static final int DEFAULT_MAX_SCENES = 100;

    /**
     * 分区大小.
     */
    private int partitionSize = DEFAULT_PARTITION_SIZE;

    /**
     * 最大场景数.
     */
    private int maxScenes = DEFAULT_MAX_SCENES;

    /**
     * 是否启用压缩.
     */
    private boolean enableCompression = true;

    /**
     * 是否启用统计.
     */
    private boolean enableStats = true;

    public DsaBitmapConfig() {
    }

    public int getPartitionSize() {
        return partitionSize;
    }

    public void setPartitionSize(int partitionSize) {
        this.partitionSize = partitionSize > 0 ? partitionSize : DEFAULT_PARTITION_SIZE;
    }

    public int getMaxScenes() {
        return maxScenes;
    }

    public void setMaxScenes(int maxScenes) {
        this.maxScenes = maxScenes > 0 ? maxScenes : DEFAULT_MAX_SCENES;
    }

    public boolean isEnableCompression() {
        return enableCompression;
    }

    public void setEnableCompression(boolean enableCompression) {
        this.enableCompression = enableCompression;
    }

    public boolean isEnableStats() {
        return enableStats;
    }

    public void setEnableStats(boolean enableStats) {
        this.enableStats = enableStats;
    }
}
