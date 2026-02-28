package io.github.dbsearchaccel.core.index;

import io.github.dbsearchaccel.common.model.dsl.DsaDsl;

import java.util.List;

/**
 * 索引服务接口.
 * <p>
 * 封装ES和Redis索引操作，用于查询主键ID和管理实时缓存.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaIndexService {

    /**
     * 从ES查询主键ID列表.
     *
     * @param dsl DSL封装实体
     * @return 主键ID列表
     */
    List<String> queryEsIds(DsaDsl dsl);

    /**
     * 从Redis获取实时主键ID列表.
     *
     * @param sceneType 场景类型
     * @return 主键ID列表
     */
    List<String> queryRedisIds(String sceneType);

    /**
     * 添加单个主键ID到Redis缓存.
     *
     * @param sceneType 场景类型
     * @param id        主键ID
     */
    void addToRedisCache(String sceneType, String id);

    /**
     * 批量添加主键ID到Redis缓存.
     *
     * @param sceneType 场景类型
     * @param ids       主键ID列表
     */
    void addToRedisCache(String sceneType, List<String> ids);

    /**
     * 从Redis缓存移除单个主键ID.
     *
     * @param sceneType 场景类型
     * @param id        主键ID
     */
    void removeFromRedisCache(String sceneType, String id);

    /**
     * 从Redis缓存批量移除主键ID.
     *
     * @param sceneType 场景类型
     * @param ids       主键ID列表
     */
    void removeFromRedisCache(String sceneType, List<String> ids);

    /**
     * 清空Redis缓存.
     *
     * @param sceneType 场景类型
     */
    void clearRedisCache(String sceneType);
}
