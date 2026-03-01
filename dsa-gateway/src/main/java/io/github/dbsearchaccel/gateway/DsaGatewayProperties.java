package io.github.dbsearchaccel.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关配置属性.
 * <p>
 * 从配置文件加载网关相关配置.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "dsa.gateway")
public class DsaGatewayProperties {

    /**
     * 是否启用网关
     */
    private boolean enabled = true;

    /**
     * 全局限流配置
     */
    private DsaRateLimitConfig globalRateLimit = new DsaRateLimitConfig();

    /**
     * 全局开关（关闭时直接降级到数据库）
     */
    private boolean globalSwitch = true;

    /**
     * 路由配置列表
     */
    private List<DsaRouteDefinition> routes = new ArrayList<>();

    /**
     * 请求校验配置
     */
    private RequestValidation requestValidation = new RequestValidation();

    /**
     * 降级配置
     */
    private DegradeConfig degradeConfig = new DegradeConfig();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public DsaRateLimitConfig getGlobalRateLimit() {
        return globalRateLimit;
    }

    public void setGlobalRateLimit(DsaRateLimitConfig globalRateLimit) {
        this.globalRateLimit = globalRateLimit;
    }

    public boolean isGlobalSwitch() {
        return globalSwitch;
    }

    public void setGlobalSwitch(boolean globalSwitch) {
        this.globalSwitch = globalSwitch;
    }

    public List<DsaRouteDefinition> getRoutes() {
        return routes;
    }

    public void setRoutes(List<DsaRouteDefinition> routes) {
        this.routes = routes;
    }

    public RequestValidation getRequestValidation() {
        return requestValidation;
    }

    public void setRequestValidation(RequestValidation requestValidation) {
        this.requestValidation = requestValidation;
    }

    public DegradeConfig getDegradeConfig() {
        return degradeConfig;
    }

    public void setDegradeConfig(DegradeConfig degradeConfig) {
        this.degradeConfig = degradeConfig;
    }

    /**
     * 请求校验配置.
     */
    public static class RequestValidation {
        private boolean enabled = true;
        private boolean validateSignature = false;
        private boolean validateToken = false;
        private String secretKey;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isValidateSignature() {
            return validateSignature;
        }

        public void setValidateSignature(boolean validateSignature) {
            this.validateSignature = validateSignature;
        }

        public boolean isValidateToken() {
            return validateToken;
        }

        public void setValidateToken(boolean validateToken) {
            this.validateToken = validateToken;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }
    }

    /**
     * 降级配置.
     */
    public static class DegradeConfig {
        private boolean enabled = true;
        private int defaultTimeout = 3000;
        private int maxRetry = 3;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getDefaultTimeout() {
            return defaultTimeout;
        }

        public void setDefaultTimeout(int defaultTimeout) {
            this.defaultTimeout = defaultTimeout;
        }

        public int getMaxRetry() {
            return maxRetry;
        }

        public void setMaxRetry(int maxRetry) {
            this.maxRetry = maxRetry;
        }
    }
}
