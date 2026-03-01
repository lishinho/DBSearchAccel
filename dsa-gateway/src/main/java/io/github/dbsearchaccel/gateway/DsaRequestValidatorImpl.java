package io.github.dbsearchaccel.gateway;

import io.github.dbsearchaccel.common.model.request.DsaRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

/**
 * 请求校验器实现.
 * <p>
 * 负责校验请求的签名、令牌、参数等.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Component
public class DsaRequestValidatorImpl implements DsaRequestValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DsaRequestValidatorImpl.class);

    @Autowired
    private DsaGatewayProperties gatewayProperties;

    @Override
    public boolean validateSignature(DsaRequest request, String sign) {
        LOGGER.debug("Validating request signature");

        if (!gatewayProperties.getRequestValidation().isValidateSignature()) {
            return true;
        }

        String secretKey = gatewayProperties.getRequestValidation().getSecretKey();
        if (secretKey == null || secretKey.isEmpty()) {
            LOGGER.warn("Secret key not configured");
            return false;
        }

        String calculatedSign = calculateSignature(request, secretKey);
        boolean valid = calculatedSign.equals(sign);

        if (!valid) {
            LOGGER.warn("Signature validation failed: expected={}, actual={}", calculatedSign, sign);
        }

        return valid;
    }

    @Override
    public boolean validateToken(String token) {
        LOGGER.debug("Validating access token");

        if (!gatewayProperties.getRequestValidation().isValidateToken()) {
            return true;
        }

        if (token == null || token.isEmpty()) {
            LOGGER.warn("Token is empty");
            return false;
        }

        return true;
    }

    @Override
    public ValidationResult validateParams(DsaRequest request) {
        LOGGER.debug("Validating request params");

        if (request == null) {
            return ValidationResult.failure("INVALID_REQUEST", "Request is null");
        }

        if (request.getSceneCode() == null || request.getSceneCode().isEmpty()) {
            return ValidationResult.failure("MISSING_SCENE_CODE", "Scene code is required");
        }

        return ValidationResult.success();
    }

    @Override
    public ValidationResult validate(DsaRequest request) {
        LOGGER.debug("Validating request");

        if (!gatewayProperties.getRequestValidation().isEnabled()) {
            return ValidationResult.success();
        }

        ValidationResult paramsResult = validateParams(request);
        if (!paramsResult.isValid()) {
            return paramsResult;
        }

        return ValidationResult.success();
    }

    /**
     * 计算签名.
     *
     * @param request   请求对象
     * @param secretKey 密钥
     * @return 签名
     */
    private String calculateSignature(DsaRequest request, String secretKey) {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("sceneCode", request.getSceneCode());
        if (request.getQueryParams() != null) {
            for (Map.Entry<String, Object> entry : request.getQueryParams().entrySet()) {
                params.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String key : params.keySet()) {
            sb.append(key).append("=").append(params.get(key)).append("&");
        }
        sb.append("key=").append(secretKey);

        return DigestUtils.md5DigestAsHex(sb.toString().getBytes(StandardCharsets.UTF_8));
    }
}
