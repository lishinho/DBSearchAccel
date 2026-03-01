package io.github.dbsearchaccel.admin.service;

import io.github.dbsearchaccel.admin.model.DsaSceneConfig;
import java.util.List;

/**
 * DSA场景管理服务接口
 * 提供场景配置的CRUD操作
 *
 * @author DBSearchAccel Team
 */
public interface DsaSceneService {

    /**
     * 创建场景配置
     *
     * @param config 场景配置
     * @return 创建后的配置
     */
    DsaSceneConfig create(DsaSceneConfig config);

    /**
     * 更新场景配置
     *
     * @param config 场景配置
     * @return 更新后的配置
     */
    DsaSceneConfig update(DsaSceneConfig config);

    /**
     * 删除场景配置
     *
     * @param id 场景ID
     * @return 是否成功
     */
    boolean delete(Long id);

    /**
     * 根据ID查询场景配置
     *
     * @param id 场景ID
     * @return 场景配置
     */
    DsaSceneConfig getById(Long id);

    /**
     * 根据场景编码查询
     *
     * @param sceneCode 场景编码
     * @return 场景配置
     */
    DsaSceneConfig getBySceneCode(String sceneCode);

    /**
     * 查询所有场景配置
     *
     * @return 场景配置列表
     */
    List<DsaSceneConfig> listAll();

    /**
     * 根据状态查询场景配置
     *
     * @param status 状态
     * @return 场景配置列表
     */
    List<DsaSceneConfig> listByStatus(String status);

    /**
     * 启用场景
     *
     * @param id 场景ID
     * @return 是否成功
     */
    boolean enable(Long id);

    /**
     * 禁用场景
     *
     * @param id 场景ID
     * @return 是否成功
     */
    boolean disable(Long id);
}
