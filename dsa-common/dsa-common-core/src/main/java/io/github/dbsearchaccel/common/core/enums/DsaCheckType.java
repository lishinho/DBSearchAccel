package io.github.dbsearchaccel.common.core.enums;

/**
 * 数据一致性校验类型枚举.
 * <p>
 * 定义ES索引与数据库数据一致性校验的类型.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public enum DsaCheckType {

    /**
     * 主键一致性校验.
     * <p>
     * 对比ES索引和数据库的主键ID，找出缺失/多余的主键.
     * </p>
     */
    PRIMARY_KEY("PRIMARY_KEY", "主键一致性校验"),

    /**
     * 字段一致性校验.
     * <p>
     * 对比ES索引和数据库中相同主键的字段值，找出不一致的字段.
     * </p>
     */
    FIELD("FIELD", "字段一致性校验");

    /**
     * 类型编码.
     */
    private final String code;

    /**
     * 类型描述.
     */
    private final String desc;

    /**
     * 构造方法.
     *
     * @param code 类型编码
     * @param desc 类型描述
     */
    DsaCheckType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取类型编码.
     *
     * @return 类型编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取类型描述.
     *
     * @return 类型描述
     */
    public String getDesc() {
        return desc;
    }
}
