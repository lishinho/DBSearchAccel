package io.github.dbsearchaccel.common.core.enums;

/**
 * 降级级别枚举.
 * <p>
 * 定义中间件的降级级别，支持接口级、场景级、全局级三级降级.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public enum DsaDegradeLevel {

    /**
     * 接口级降级.
     * <p>
     * 仅对单个接口生效，影响范围最小.
     * </p>
     */
    INTERFACE("INTERFACE", "接口级降级"),

    /**
     * 场景级降级.
     * <p>
     * 对单个业务场景生效，影响范围中等.
     * </p>
     */
    SCENE("SCENE", "场景级降级"),

    /**
     * 全局级降级.
     * <p>
     * 对整个中间件生效，所有请求切回数据库，仅用于极端故障场景.
     * </p>
     */
    GLOBAL("GLOBAL", "全局级降级");

    /**
     * 级别编码.
     */
    private final String code;


    /**
     * 级别描述.
     */
    private final String description;

    /**
     * 构造方法.
     *
     * @param code 级别编码
     * @param desc 级别描述
     */
    DsaDegradeLevel(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取级别编码.
     *
     * @return 级别编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取级别描述.
     *
     * @return 级别描述
     */
    public String getDesc() {
        return desc;
    }
}
