package io.github.dbsearchaccel.infra.redis;

import java.util.List;

/**
 * Redis客户端接口.
 * <p>
 * 封装Redis的常用操作，主要用于实时主键缓存.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaRedisClient {

    /**
     * 添加单个主键ID到缓存.
     *
     * @param sceneType 场景类型
     * @param id        主键ID
     */
    void addId(String sceneType, String id);

    /**
     * 批量添加主键ID到缓存.
     *
     * @param sceneType 场景类型
     * @param ids       主键ID列表
     */
    void addIds(String sceneType, List<String> ids);

    /**
     * 从缓存移除单个主键ID.
     *
     * @param sceneType 场景类型
     * @param id        主键ID
     */
    void removeId(String sceneType, String id);

    /**
     * 从缓存批量移除主键ID.
     *
     * @param sceneType 场景类型
     * @param ids       主键ID列表
     */
    void removeIds(String sceneType, List<String> ids);

    /**
     * 获取缓存中的所有主键ID.
     *
     * @param sceneType 场景类型
     * @return 主键ID列表
     */
    List<String> getIds(String sceneType);

    /**
     * 清空缓存中的所有主键ID.
     *
     * @param sceneType 场景类型
     */
    void clearIds(String sceneType);

    /**
     * 获取缓存中的主键ID数量.
     *
     * @param sceneType 场景类型
     * @return 主键ID数量
     */
    long countIds(String sceneType);

    /**
     * 设置缓存过期时间.
     *
     * @param sceneType 场景类型
     * @param seconds   过期时间（秒）
     */
    void expire(String sceneType, long seconds);

    /**
     * 设置字符串值.
     *
     * @param key   键
     * @param value 值
     */
    void set(String key, String value);

    /**
     * 获取字符串值.
     *
     * @param key 键
     * @return 值
     */
    String get(String key);

    /**
     * 删除键.
     *
     * @param key 键
     */
    void delete(String key);

    /**
     * 判断键是否存在.
     *
     * @param key 键
     * @return true表示存在
     */
    boolean exists(String key);
}
