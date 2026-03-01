package io.github.dbsearchaccel.cache;

/**
 * 向量时钟比较结果.
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public enum DsaClockCompareResult {

    /**
     * v1在v2之前.
     */
    BEFORE,

    /**
     * v1在v2之后.
     */
    AFTER,

    /**
     * 并发（无法比较）.
     */
    CONCURRENT,

    /**
     * 相等.
     */
    EQUAL
}
