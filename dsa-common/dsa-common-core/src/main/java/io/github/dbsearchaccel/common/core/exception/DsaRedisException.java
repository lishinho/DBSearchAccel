package io.github.dbsearchaccel.common.core.exception;

import io.github.dbsearchaccel.common.core.enums.DsaResultCode;

/**
 * Redis操作异常.
 * <p>
 * 当Redis操作失败时抛出此异常.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaRedisException extends DsaException {

    private static final long serialVersionUID = 1L;

    /**
     * 默认构造方法.
     */
    public DsaRedisException() {
        super(DsaResultCode.REDIS_ERROR.getCode(), DsaResultCode.REDIS_ERROR.getMessage());
    }

    /**
     * 构造方法.
     *
     * @param message 错误信息
     */
    public DsaRedisException(String message) {
        super(DsaResultCode.REDIS_ERROR.getCode(), message);
    }

    /**
     * 构造方法.
     *
     * @param message 错误信息
     * @param cause   原因异常
     */
    public DsaRedisException(String message, Throwable cause) {
        super(DsaResultCode.REDIS_ERROR.getCode(), message, cause);
    }
}
