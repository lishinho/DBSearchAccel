package io.github.dbsearchaccel.common.model.config;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 场景配置实体.
 * <p>
 * 封装单个业务场景的配置信息，包括ES索引、数据库表、灰度规则、过滤插件等.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Data
public class DsaSceneConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 场景编码.
     */
    private String sceneCode;

    /**
     * 场景名称.
     */
    private String sceneName;

    /**
     * 是否启用.
     * <p>
     * 为false时场景禁用，请求走降级
     * </p>
     */
    private boolean enabled = true;

    /**
     * ES索引名称.
     */
    private String esIndex;

    /**
     * 数据库表名.
     */
    private String dbTable;

    /**
     * 主键字段名.
     * <p>
     * 默认值为"id"
     * </p>
     */
    private String primaryKeyField = "id";

    /**
     * 灰度白名单.
     * <p>
     * 在白名单中的grayKey允许访问
     * </p>
     */
    private List<String> grayWhiteList;

    /**
     * 灰度黑名单.
     * <p>
     * 在黑名单中的grayKey禁止访问
     * </p>
     */
    private List<String> grayBlackList;

    /**
     * 过滤插件列表.
     * <p>
     * 按顺序执行的过滤插件类名
     * </p>
     */
    private List<String> filterPlugins;

    /**
     * DSL字段映射.
     * <p>
     * Key为业务字段名，Value为ES字段名
     * </p>
     */
    private Map<String, String> dslFieldMapping;

    /**
     * Redis缓存过期时间（秒）.
     * <p>
     * 默认值为600秒（10分钟）
     * </p>
     */
    private int redisExpireSeconds = 600;

    /**
     * Redis缓存最大主键数.
     * <p>
     * 默认值为10000
     * </p>
     */
    private int redisMaxCount = 10000;

    /**
     * 是否启用增量数据.
     * <p>
     * 为true时从Redis获取实时主键
     * </p>
     */
    private boolean incrementEnabled = true;

    /**
     * 判断是否在黑名单中.
     *
     * @param key 灰度标识
     * @return true表示在黑名单中
     */
    public boolean isInGrayList(String key) {
        if (grayBlackList != null && grayBlackList.contains(key)) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否在白名单中.
     * <p>
     * 若白名单为空，则所有key都在白名单中
     * </p>
     *
     * @param key 灰度标识
     * @return true表示在白名单中
     */
    public boolean isInWhiteList(String key) {
        if (grayWhiteList == null || grayWhiteList.isEmpty()) {
            return true;
        }
        return grayWhiteList.contains(key);
    }

    /**
     * 判断是否允许访问.
     * <p>
     * 先检查黑名单，再检查白名单
     * </p>
     *
     * @param key 灰度标识
     * @return true表示允许访问
     */
    public boolean allowAccess(String key) {
        if (isInGrayList(key)) {
            return false;
        }
        return isInWhiteList(key);
    }
}
