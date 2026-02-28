package io.github.dbsearchaccel.sync.full;

/**
 * 同步任务状态枚举.
 * <p>
 * 定义同步任务的所有可能状态.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public enum DsaSyncStatus {

    /**
     * 待执行.
     */
    PENDING("PENDING", "待执行"),

    /**
     * 执行中.
     */
    RUNNING("RUNNING", "执行中"),

    /**
     * 已暂停.
     */
    PAUSED("PAUSED", "已暂停"),

    /**
     * 已完成.
     */
    COMPLETED("COMPLETED", "已完成"),

    /**
     * 已失败.
     */
    FAILED("FAILED", "已失败"),

    /**
     * 已取消.
     */
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String desc;

    DsaSyncStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据编码获取枚举.
     *
     * @param code 状态编码
     * @return 枚举实例
     */
    public static DsaSyncStatus fromCode(String code) {
        for (DsaSyncStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
