package io.github.dbsearchaccel.gateway;

import io.github.dbsearchaccel.common.model.request.DsaRequest;
import io.github.dbsearchaccel.common.model.response.DsaResponse;

/**
 * 请求校验器接口.
 * <p>
 * 负责校验请求的签名、令牌、参数等.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaRequestValidator {

    /**
     * 校验请求签名.
     *
     * @param request 请求对象
     * @param sign    签名
     * @return 校验结果
     */
    boolean validateSignature(DsaRequest request, String sign);

    /**
     * 校验访问令牌.
     *
     * @param token 访问令牌
     * @return 校验结果
     */
    boolean validateToken(String token);

    /**
     * 校验请求参数.
     *
     * @param request 请求对象
     * @return 校验结果
     */
    ValidationResult validateParams(DsaRequest request);

    /**
     * 综合校验请求.
     *
     * @param request 请求对象
     * @return 校验结果
     */
    ValidationResult validate(DsaRequest request);

    /**
     * 校验结果.
     */
    class ValidationResult {
        private final boolean valid;
        private final String errorCode;
        private final String errorMessage;

        public ValidationResult(boolean valid) {
            this(valid, null, null);
        }

        public ValidationResult(boolean valid, String errorCode, String errorMessage) {
            this.valid = valid;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true);
        }

        public static ValidationResult failure(String errorCode, String errorMessage) {
            return new ValidationResult(false, errorCode, errorMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
