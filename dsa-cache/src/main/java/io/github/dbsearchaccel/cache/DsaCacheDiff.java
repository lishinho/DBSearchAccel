package io.github.dbsearchaccel.cache;

/**
 * 缓存差异记录.
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaCacheDiff {

    /**
     * 主键值.
     */
    private String pkValue;

    /**
     * 期望值（来自DB）.
     */
    private Object expectedValue;

    /**
     * 实际值（来自缓存）.
     */
    private Object actualValue;

    /**
     * 差异类型.
     */
    private DsaDiffType diffType;

    /**
     * 检测时间.
     */
    private long detectTime;

    public DsaCacheDiff() {
        this.detectTime = System.currentTimeMillis();
    }

    public DsaCacheDiff(String pkValue, DsaDiffType diffType) {
        this.pkValue = pkValue;
        this.diffType = diffType;
        this.detectTime = System.currentTimeMillis();
    }

    public String getPkValue() {
        return pkValue;
    }

    public void setPkValue(String pkValue) {
        this.pkValue = pkValue;
    }

    public Object getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(Object expectedValue) {
        this.expectedValue = expectedValue;
    }

    public Object getActualValue() {
        return actualValue;
    }

    public void setActualValue(Object actualValue) {
        this.actualValue = actualValue;
    }

    public DsaDiffType getDiffType() {
        return diffType;
    }

    public void setDiffType(DsaDiffType diffType) {
        this.diffType = diffType;
    }

    public long getDetectTime() {
        return detectTime;
    }

    public void setDetectTime(long detectTime) {
        this.detectTime = detectTime;
    }

    @Override
    public String toString() {
        return "DsaCacheDiff{" +
                "pkValue='" + pkValue + '\'' +
                ", diffType=" + diffType +
                ", detectTime=" + detectTime +
                '}';
    }
}
