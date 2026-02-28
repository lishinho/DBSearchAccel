package io.github.dbsearchaccel.common.core.exception;

import io.github.dbsearchaccel.common.core.enums.DsaResultCode;

/**
 * 服务降级异常.
 * <p>
 * 当服务触发降级时抛出此异常.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaDegradeException extends DsaException {

    private static final long serialVersionUID = 1L;

    /**
     * 默认构造方法.
     */
    public DsaDegradeException() {
        super(DsaResultCode.DEGRADE_ERROR.getCode(), DsaResultCode.DEGRADE_ERROR.getMessage());
    }

    /**
     * 构造方法.
     *
     * @param message 错误信息
     */
    public DsaDegradeException(String message) {
        super(DsaResultCode.DEGRADE_ERROR.getCode(), message);
    }
}
