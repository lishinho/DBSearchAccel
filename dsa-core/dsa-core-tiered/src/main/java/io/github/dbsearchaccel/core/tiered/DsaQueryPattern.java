package io.github.dbsearchaccel.core.tiered;

/**
 * 查询模式.
 * <p>
 * 用于描述一个查询条件，支持查询模式预测和预热.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaQueryPattern {

    /**
     * 场景编码.
     */
    private String sceneCode;

    /**
     * 字段名.
     */
    private String fieldName;

    /**
     * 字段值.
     */
    private Object fieldValue;

    /**
     * 访问频率.
     */
    private int frequency;

    /**
     * 最近访问时间戳.
     */
    private long lastAccessTime;

    public DsaQueryPattern() {
    }

    public DsaQueryPattern(String sceneCode, String fieldName, Object fieldValue) {
        this.sceneCode = sceneCode;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.lastAccessTime = System.currentTimeMillis();
    }

    /**
     * 构建索引键.
     *
     * @return 索引键
     */
    public String toIndexKey() {
        return fieldName + ":" + String.valueOf(fieldValue);
    }

    /**
     * 增加访问频率.
     */
    public void incrementFrequency() {
        this.frequency++;
        this.lastAccessTime = System.currentTimeMillis();
    }

    /**
     * 判断是否为热点.
     *
     * @param threshold 热点阈值
     * @return 是否为热点
     */
    public boolean isHot(int threshold) {
        return frequency >= threshold;
    }

    public String getSceneCode() {
        return sceneCode;
    }

    public void setSceneCode(String sceneCode) {
        this.sceneCode = sceneCode;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(Object fieldValue) {
        this.fieldValue = fieldValue;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    @Override
    public String toString() {
        return "DsaQueryPattern{" +
                "sceneCode='" + sceneCode + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", fieldValue=" + fieldValue +
                ", frequency=" + frequency +
                '}';
    }
}
