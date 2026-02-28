package io.github.dbsearchaccel.common.core.enums;

/**
 * 业务场景类型枚举.
 * <p>
 * 定义中间件支持的业务场景类型，用于场景路由和配置映射.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public enum DsaSceneType {

    /**
     * 订单查询场景.
     */
    ORDER("ORDER", "订单查询"),

    /**
     * 商品查询场景.
     */
    GOODS("GOODS", "商品查询"),

    /**
     * 用户查询场景.
     */
    USER("USER", "用户查询"),

    /**
     * 自定义场景.
     */
    CUSTOM("CUSTOM", "自定义场景");

    /**
     * 场景编码.
     */
    private final String code;

    /**
     * 场景描述.
     */
    private final String desc;

    /**
     * 构造方法.
     *
     * @param code 场景编码
     * @param desc 场景描述
     */
    DsaSceneType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取场景编码.
     *
     * @return 场景编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取场景描述.
     *
     * @return 场景描述
     */
    public String getDesc() {
        return desc;
    }

    /**
     * 根据编码获取场景类型.
     * <p>
     * 如果编码不存在，返回自定义场景类型.
     * </p>
     *
     * @param code 场景编码
     * @return 场景类型枚举
     */
    public static DsaSceneType fromCode(String code) {
        for (DsaSceneType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return CUSTOM;
    }
}
