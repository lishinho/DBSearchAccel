package io.github.dbsearchaccel.common.core.exception;

import io.github.dbsearchaccel.common.core.enums.DsaResultCode;

/**
 * Elasticsearch操作异常.
 * <p>
 * 当ES操作失败时抛出此异常.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaEsException extends DsaException {

    private static final long serialVersionUID = 1L;

    /**
     * 默认构造方法.
     */
    public DsaEsException() {
        super(DsaResultCode.ES_QUERY_ERROR.getCode(), DsaResultCode.ES_QUERY_ERROR.getMessage());
    }

    /**
     * 构造方法.
     *
     * @param message 错误信息
     */
    public DsaEsException(String message) {
        super(DsaResultCode.ES_QUERY_ERROR.getCode(), message);
    }

    /**
     * 构造方法.
     *
     * @param message 错误信息
     * @param cause   原因异常
     */
    public DsaEsException(String message, Throwable cause) {
        super(DsaResultCode.ES_QUERY_ERROR.getCode(), message, cause);
    }
}
