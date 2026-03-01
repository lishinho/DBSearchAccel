package io.github.dbsearchaccel.consistency;

/**
 * 校验状态枚举.
 * <p>
 * 定义数据一致性校验的状态流转.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public enum DsaCheckStatus {

    /**
     * 待执行
     */
    PENDING("待执行", "校验任务已创建，等待执行"),

    /**
     * 执行中
     */
    RUNNING("执行中", "校验任务正在执行"),

    /**
     * 已完成
     */
    COMPLETED("已完成", "校验任务执行完成"),

    /**
     * 失败
     */
    FAILED("失败", "校验任务执行失败"),

    /**
     * 已取消
     */
    CANCELLED("已取消", "校验任务被取消");

    private final String name;

    private final String description;

    DsaCheckStatus(String name, String description) {
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
