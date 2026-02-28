package io.github.dbsearchaccel.cache;

import java.util.List;

/**
 * 缓存服务接口.
 * <p>
 * 提供实时主键缓存的增删改查操作.
 * 缓存用于存储最近新增/修改的主键ID列表，弥补ES同步延迟.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaCacheService {

    /**
     * 添加主键到缓存.
     *
     * @param sceneCode 场景编码
     * @param pkValue   主键值
     * @return true表示添加成功
     */
    boolean addPk(String sceneCode, String pkValue);

    /**
     * 批量添加主键到缓存.
     *
     * @param sceneCode 场景编码
     * @param pkValues  主键值列表
     * @return 成功添加的数量
     */
    int addPkBatch(String sceneCode, List<String> pkValues);

    /**
     * 从缓存移除主键.
     *
     * @param sceneCode 场景编码
     * @param pkValue   主键值
     * @return true表示移除成功
     */
    boolean removePk(String sceneCode, String pkValue);

    /**
     * 批量移除主键.
     *
     * @param sceneCode 场景编码
     * @param pkValues  主键值列表
     * @return 成功移除的数量
     */
    int removePkBatch(String sceneCode, List<String> pkValues);

    /**
     * 获取缓存中的所有主键.
     *
     * @param sceneCode 场景编码
     * @return 主键列表
     */
    List<String> getAllPks(String sceneCode);

    /**
     * 获取缓存中的主键数量.
     *
     * @param sceneCode 场景编码
     * @return 主键数量
     */
    long getPkCount(String sceneCode);

    /**
     * 清空缓存.
     *
     * @param sceneCode 场景编码
     * @return true表示清空成功
     */
    boolean clear(String sceneCode);

    /**
     * 判断主键是否存在于缓存中.
     *
     * @param sceneCode 场景编码
     * @param pkValue   主键值
     * @return true表示存在
     */
    boolean exists(String sceneCode, String pkValue);

    /**
     * 设置缓存过期时间.
     *
     * @param sceneCode 场景编码
     * @param seconds   过期时间（秒）
     * @return true表示设置成功
     */
    boolean expire(String sceneCode, long seconds);
}
