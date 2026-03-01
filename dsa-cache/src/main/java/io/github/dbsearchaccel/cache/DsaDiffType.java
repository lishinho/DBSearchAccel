package io.github.dbsearchaccel.cache;

/**
 * 差异类型枚举.
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public enum DsaDiffType {

    /**
     * 缓存缺失（DB有，缓存无）.
     */
    CACHE_MISS,

    /**
     * 缓存多余（DB无，缓存有）.
     */
    CACHE_REDUNDANT,

    /**
     * 值不一致.
     */
    VALUE_MISMATCH
}
