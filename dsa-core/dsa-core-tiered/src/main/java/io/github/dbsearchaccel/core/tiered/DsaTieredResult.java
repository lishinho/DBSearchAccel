package io.github.dbsearchaccel.core.tiered;

import org.roaringbitmap.RoaringBitmap;

/**
 * 分层查询结果.
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaTieredResult {

    /**
     * 位图数据.
     */
    private RoaringBitmap bitmap;

    /**
     * 数据来源层级.
     */
    private DsaStorageLevel sourceLevel;

    /**
     * 查询耗时（毫秒）.
     */
    private long queryTimeMs;

    /**
     * 是否来自缓存.
     */
    private boolean fromCache;

    /**
     * 场景编码.
     */
    private String sceneCode;

    /**
     * 索引键.
     */
    private String indexKey;

    public DsaTieredResult() {
    }

    public DsaTieredResult(RoaringBitmap bitmap, DsaStorageLevel sourceLevel) {
        this.bitmap = bitmap;
        this.sourceLevel = sourceLevel;
    }

    /**
     * 创建命中结果.
     *
     * @param bitmap     位图
     * @param level      来源层级
     * @param queryTimeMs 查询耗时
     * @return 分层查询结果
     */
    public static DsaTieredResult hit(RoaringBitmap bitmap, DsaStorageLevel level, long queryTimeMs) {
        DsaTieredResult result = new DsaTieredResult(bitmap, level);
        result.setFromCache(true);
        result.setQueryTimeMs(queryTimeMs);
        return result;
    }

    /**
     * 创建未命中结果.
     *
     * @return 分层查询结果
     */
    public static DsaTieredResult miss() {
        return new DsaTieredResult(new RoaringBitmap(), DsaStorageLevel.COLD);
    }

    /**
     * 判断是否命中.
     *
     * @return 是否命中
     */
    public boolean isHit() {
        return bitmap != null && !bitmap.isEmpty();
    }

    /**
     * 获取位图基数.
     *
     * @return 基数
     */
    public long getCardinality() {
        return bitmap != null ? bitmap.getLongCardinality() : 0;
    }

    public RoaringBitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(RoaringBitmap bitmap) {
        this.bitmap = bitmap;
    }

    public DsaStorageLevel getSourceLevel() {
        return sourceLevel;
    }

    public void setSourceLevel(DsaStorageLevel sourceLevel) {
        this.sourceLevel = sourceLevel;
    }

    public long getQueryTimeMs() {
        return queryTimeMs;
    }

    public void setQueryTimeMs(long queryTimeMs) {
        this.queryTimeMs = queryTimeMs;
    }

    public boolean isFromCache() {
        return fromCache;
    }

    public void setFromCache(boolean fromCache) {
        this.fromCache = fromCache;
    }

    public String getSceneCode() {
        return sceneCode;
    }

    public void setSceneCode(String sceneCode) {
        this.sceneCode = sceneCode;
    }

    public String getIndexKey() {
        return indexKey;
    }

    public void setIndexKey(String indexKey) {
        this.indexKey = indexKey;
    }

    @Override
    public String toString() {
        return "DsaTieredResult{" +
                "cardinality=" + getCardinality() +
                ", sourceLevel=" + sourceLevel +
                ", queryTimeMs=" + queryTimeMs +
                ", fromCache=" + fromCache +
                '}';
    }
}
