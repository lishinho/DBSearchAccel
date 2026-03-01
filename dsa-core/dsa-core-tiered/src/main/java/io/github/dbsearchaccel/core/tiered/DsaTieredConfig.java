package io.github.dbsearchaccel.core.tiered;

/**
 * 分层存储配置.
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaTieredConfig {

    /**
     * 默认热缓存大小.
     */
    private static final int DEFAULT_HOT_CACHE_SIZE = 10000;

    /**
     * 默认热缓存过期时间（秒）.
     */
    private static final int DEFAULT_HOT_CACHE_EXPIRE = 300;

    /**
     * 默认温存储目录.
     */
    private static final String DEFAULT_WARM_STORE_DIR = "./data/tiered/warm";

    /**
     * 默认降级阈值（毫秒）.
     */
    private static final long DEFAULT_DEMOTE_THRESHOLD_MS = 300000;

    /**
     * 热缓存大小.
     */
    private int hotCacheSize = DEFAULT_HOT_CACHE_SIZE;

    /**
     * 热缓存过期时间（秒）.
     */
    private int hotCacheExpireSeconds = DEFAULT_HOT_CACHE_EXPIRE;

    /**
     * 温存储目录.
     */
    private String warmStoreDir = DEFAULT_WARM_STORE_DIR;

    /**
     * 降级阈值（毫秒）.
     */
    private long demoteThresholdMs = DEFAULT_DEMOTE_THRESHOLD_MS;

    /**
     * 是否启用自动晋升.
     */
    private boolean enableAutoPromote = true;

    /**
     * 是否启用自动降级.
     */
    private boolean enableAutoDemote = true;

    public DsaTieredConfig() {
    }

    public int getHotCacheSize() {
        return hotCacheSize;
    }

    public void setHotCacheSize(int hotCacheSize) {
        this.hotCacheSize = hotCacheSize > 0 ? hotCacheSize : DEFAULT_HOT_CACHE_SIZE;
    }

    public int getHotCacheExpireSeconds() {
        return hotCacheExpireSeconds;
    }

    public void setHotCacheExpireSeconds(int hotCacheExpireSeconds) {
        this.hotCacheExpireSeconds = hotCacheExpireSeconds > 0 ? hotCacheExpireSeconds : DEFAULT_HOT_CACHE_EXPIRE;
    }

    public String getWarmStoreDir() {
        return warmStoreDir;
    }

    public void setWarmStoreDir(String warmStoreDir) {
        this.warmStoreDir = warmStoreDir != null && !warmStoreDir.isEmpty() 
                ? warmStoreDir : DEFAULT_WARM_STORE_DIR;
    }

    public long getDemoteThresholdMs() {
        return demoteThresholdMs;
    }

    public void setDemoteThresholdMs(long demoteThresholdMs) {
        this.demoteThresholdMs = demoteThresholdMs > 0 ? demoteThresholdMs : DEFAULT_DEMOTE_THRESHOLD_MS;
    }

    public boolean isEnableAutoPromote() {
        return enableAutoPromote;
    }

    public void setEnableAutoPromote(boolean enableAutoPromote) {
        this.enableAutoPromote = enableAutoPromote;
    }

    public boolean isEnableAutoDemote() {
        return enableAutoDemote;
    }

    public void setEnableAutoDemote(boolean enableAutoDemote) {
        this.enableAutoDemote = enableAutoDemote;
    }
}
