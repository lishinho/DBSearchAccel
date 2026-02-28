package io.github.dbsearchaccel.common.core.enums;

/**
 * 熔断策略枚举.
 * <p>
 * 定义Sentinel熔断器的熔断策略类型.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public enum DsaCircuitGrade {

    /**
     * 异常比例熔断.
     * <p>
     * 当异常比例超过阈值时触发熔断.
     * </p>
     */
    ERROR_RATIO("ERROR_RATIO", "异常比例熔断"),

    /**
     * 慢调用比例熔断.
     * <p>
     * 当慢调用比例超过阈值时触发熔断.
     * </p>
     */
    SLOW_CALL_RATIO("SLOW_CALL_RATIO", "慢调用比例熔断"),

    /**
     * 异常数熔断.
     * <p>
     * 当异常数超过阈值时触发熔断.
     * </p>
     */
    ERROR_COUNT("ERROR_COUNT", "异常数熔断");

    /**
     * 策略编码.
     */
    private final String code;

    /**
     * 策略描述.
     */
    private final String desc;

    /**
     * 构造方法.
     *
     * @param code 策略编码
     * @param desc 策略描述
     */
    DsaCircuitGrade(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取策略编码.
     *
     * @return 策略编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取策略描述.
     *
     * @return 策略描述
     */
    public String getDesc() {
        return desc;
    }
}
