package io.github.dbsearchaccel.cache;

/**
 * 布隆过滤器接口.
 * <p>
 * 用于快速判断主键是否可能存在，降低无效查询穿透.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaBloomFilter {

    /**
     * 添加元素.
     *
     * @param sceneCode 场景编码
     * @param value     元素值
     */
    void put(String sceneCode, String value);

    /**
     * 批量添加元素.
     *
     * @param sceneCode 场景编码
     * @param values    元素值列表
     */
    void putAll(String sceneCode, java.util.Collection<String> values);

    /**
     * 判断元素可能存在.
     * 注意：返回true不一定存在，返回false一定不存在.
     *
     * @param sceneCode 场景编码
     * @param value     元素值
     * @return true=可能存在，false=一定不存在
     */
    boolean mightContain(String sceneCode, String value);

    /**
     * 重建布隆过滤器.
     * 当误判率超过阈值时触发重建.
     *
     * @param sceneCode 场景编码
     * @param allValues 全量元素值
     */
    void rebuild(String sceneCode, java.util.Collection<String> allValues);

    /**
     * 获取当前误判率.
     *
     * @param sceneCode 场景编码
     * @return 误判率
     */
    double getFalsePositiveRate(String sceneCode);
}
