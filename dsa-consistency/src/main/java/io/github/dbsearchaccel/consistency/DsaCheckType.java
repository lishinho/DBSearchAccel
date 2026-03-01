package io.github.dbsearchaccel.consistency;

/**
 * 校验类型枚举.
 * <p>
 * 定义数据一致性校验的类型.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public enum DsaCheckType {

    /**
     * 主键校验 - 对比ES和数据库的主键列表
     */
    PRIMARY_KEY("主键校验", "对比ES和数据库的主键列表"),

    /**
     * 字段校验 - 对比ES和数据库的字段值
     */
    FIELD("字段校验", "对比ES和数据库的字段值"),

    /**
     * 数量校验 - 对比ES和数据库的数据量
     */
    COUNT("数量校验", "对比ES和数据库的数据量"),

    /**
     * 全量校验 - 包含所有校验类型
     */
    FULL("全量校验", "包含主键、字段、数量校验");

    private final String name;

    private final String description;

    DsaCheckType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
