package io.github.dbsearchaccel.common.core.exception;

/**
 * 中间件统一异常类.
 * <p>
 * 所有中间件自定义异常的基类，包含错误码和错误信息.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码.
     */
    private String code;

    /**
     * 错误信息.
     */
    private String message;

    /**
     * 构造方法.
     *
     * @param message 错误信息
     */
    public DsaException(String message) {
        super(message);
        this.message = message;
        this.code = "9999";
    }

    /**
     * 构造方法.
     *
     * @param code    错误码
     * @param message 错误信息
     */
    public DsaException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 构造方法.
     *
     * @param code    错误码
     * @param message 错误信息
     * @param cause   原因异常
     */
    public DsaException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    /**
     * 获取错误码.
     *
     * @return 错误码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取错误信息.
     *
     * @return 错误信息
     */
    @Override
    public String getMessage() {
        return message;
    }
}
