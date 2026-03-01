package io.github.dbsearchaccel.gateway;

import io.github.dbsearchaccel.common.model.request.DsaRequest;
import io.github.dbsearchaccel.common.model.response.DsaResponse;
import io.github.dbsearchaccel.fallback.DsaFallbackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 网关全局过滤器.
 * <p>
 * 负责请求预处理、限流、降级等.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Component
public class DsaGatewayFilter implements GlobalFilter, Ordered {

    private static final Logger LOGGER = LoggerFactory.getLogger(DsaGatewayFilter.class);

    private static final String HEADER_SCENE_CODE = "X-Dsa-SceneCode";
    private static final String HEADER_TOKEN = "X-Dsa-Token";
    private static final String HEADER_SIGN = "X-Dsa-Sign";

    @Autowired
    private DsaGatewayProperties gatewayProperties;

    @Autowired
    private DsaRequestValidator requestValidator;

    @Autowired
    private DsaRateLimiter rateLimiter;

    @Autowired(required = false)
    private DsaFallbackService fallbackService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        LOGGER.debug("Processing request: {}", path);

        if (!gatewayProperties.isEnabled()) {
            LOGGER.info("Gateway disabled, passing through");
            return chain.filter(exchange);
        }

        if (!gatewayProperties.isGlobalSwitch()) {
            LOGGER.warn("Global switch is OFF, degrading to database query");
            return handleDegrade(exchange, "Global switch is OFF");
        }

        String sceneCode = request.getHeaders().getFirst(HEADER_SCENE_CODE);
        if (sceneCode == null || sceneCode.isEmpty()) {
            return handleError(exchange, HttpStatus.BAD_REQUEST, "Missing scene code header");
        }

        DsaRequestValidator.ValidationResult validation = validateRequest(request, sceneCode);
        if (!validation.isValid()) {
            return handleError(exchange, HttpStatus.BAD_REQUEST, validation.getErrorMessage());
        }

        String resource = "scene:" + sceneCode;
        if (!checkRateLimit(resource)) {
            LOGGER.warn("Rate limited for scene: {}", sceneCode);
            return handleDegrade(exchange, "Rate limited");
        }

        return chain.filter(exchange);
    }

    /**
     * 校验请求.
     *
     * @param request   HTTP请求
     * @param sceneCode 场景编码
     * @return 校验结果
     */
    private DsaRequestValidator.ValidationResult validateRequest(ServerHttpRequest request, String sceneCode) {
        DsaRequest dsaRequest = new DsaRequest();
        dsaRequest.setSceneCode(sceneCode);

        if (gatewayProperties.getRequestValidation().isValidateToken()) {
            String token = request.getHeaders().getFirst(HEADER_TOKEN);
            if (!requestValidator.validateToken(token)) {
                return DsaRequestValidator.ValidationResult.failure("INVALID_TOKEN", "Invalid or missing token");
            }
        }

        if (gatewayProperties.getRequestValidation().isValidateSignature()) {
            String sign = request.getHeaders().getFirst(HEADER_SIGN);
            if (!requestValidator.validateSignature(dsaRequest, sign)) {
                return DsaRequestValidator.ValidationResult.failure("INVALID_SIGNATURE", "Invalid signature");
            }
        }

        return requestValidator.validate(dsaRequest);
    }

    /**
     * 检查限流.
     *
     * @param resource 资源标识
     * @return 是否通过
     */
    private boolean checkRateLimit(String resource) {
        DsaRateLimitConfig config = gatewayProperties.getGlobalRateLimit();
        if (config == null || !config.isEnabled()) {
            return true;
        }

        if (!rateLimiter.tryAcquire(resource)) {
            return false;
        }

        if (config.getStrategy() == DsaRateLimitConfig.LimitStrategy.CONCURRENT
                || config.getStrategy() == DsaRateLimitConfig.LimitStrategy.MIXED) {
            if (!rateLimiter.tryAcquireConcurrent(resource)) {
                rateLimiter.releaseConcurrent(resource);
                return false;
            }
        }

        return true;
    }

    /**
     * 处理降级.
     *
     * @param exchange  交换对象
     * @param reason    降级原因
     * @return 响应
     */
    private Mono<Void> handleDegrade(ServerWebExchange exchange, String reason) {
        LOGGER.info("Handling degrade: {}", reason);

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"code\":\"DEGRADED\",\"message\":\"" + reason + "\",\"data\":null}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 处理错误.
     *
     * @param exchange  交换对象
     * @param status    HTTP状态
     * @param message   错误消息
     * @return 响应
     */
    private Mono<Void> handleError(ServerWebExchange exchange, HttpStatus status, String message) {
        LOGGER.warn("Handling error: {}", message);

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"code\":\"ERROR\",\"message\":\"" + message + "\",\"data\":null}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }
}
