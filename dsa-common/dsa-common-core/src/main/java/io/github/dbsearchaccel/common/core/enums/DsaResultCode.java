package io.github.dbsearchaccel.common.core.enums;

/**
 * 结果状态码枚举.
 * <p>
 * 定义中间件统一返回结果的状态码和消息.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public enum DsaResultCode {

    /**
     * 成功.
     */
    SUCCESS("200", "成功"),

    /**
     * 参数错误.
     */
    PARAM_ERROR("400", "参数错误"),

    /**
     * 场景不存在.
     */
    SCENE_NOT_FOUND("404", "场景不存在"),

    /**
     * ES查询异常.
     */
    ES_QUERY_ERROR("5001", "ES查询异常"),

    /**
     * Redis操作异常.
     */
    REDIS_ERROR("5002", "Redis操作异常"),

    /**
     * 数据库操作异常.
     */
    DB_ERROR("5003", "数据库操作异常"),

    /**
     * 数据同步异常.
     */
    SYNC_ERROR("5004", "数据同步异常"),

    /**
     * 服务降级.
     */
    DEGRADE_ERROR("5005", "服务降级"),

    /**
     * 熔断器打开.
     */
    CIRCUIT_BREAKER_OPEN("5006", "熔断器打开"),

    /**
     * 未知异常.
     */
    UNKNOWN_ERROR("9999", "未知异常");

    /**
     * 状态码.
     */
    private final String code;

    /**
     * 消息.
     */
    private final String message;

    /**
     * 构造方法.
     *
     * @param code    状态码
     * @param message 消息
     */
    DsaResultCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取状态码.
     *
     * @return 状态码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取消息.
     *
     * @return 消息
     */
    public String getMessage() {
        return message;
    }
}
