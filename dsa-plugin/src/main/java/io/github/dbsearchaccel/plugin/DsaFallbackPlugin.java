package io.github.dbsearchaccel.plugin;

import io.github.dbsearchaccel.common.model.request.DsaRequest;
import io.github.dbsearchaccel.common.model.response.DsaResponse;

/**
 * 降级插件接口.
 * <p>
 * 用于降级逻辑扩展，可在降级时执行自定义逻辑.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaFallbackPlugin extends DsaPlugin<DsaFallbackPlugin.FallbackContext, DsaPluginResult> {

    /**
     * 降级插件上下文.
     */
    class FallbackContext extends DsaPluginContext {

        private static final long serialVersionUID = 1L;

        /**
         * 原始请求
         */
        private DsaRequest request;

        /**
         * 降级原因
         */
        private FallbackReason reason;

        /**
         * 降级级别
         */
        private String degradeLevel;

        /**
         * 原始异常
         */
        private Throwable cause;

        public FallbackContext() {
            super();
        }

        public FallbackContext(String sceneCode, DsaRequest request, FallbackReason reason) {
            super(sceneCode, "FALLBACK");
            this.request = request;
            this.reason = reason;
        }

        public DsaRequest getRequest() {
            return request;
        }

        public void setRequest(DsaRequest request) {
            this.request = request;
        }

        public FallbackReason getReason() {
            return reason;
        }

        public void setReason(FallbackReason reason) {
            this.reason = reason;
        }

        public String getDegradeLevel() {
            return degradeLevel;
        }

        public void setDegradeLevel(String degradeLevel) {
            this.degradeLevel = degradeLevel;
        }

        public Throwable getCause() {
            return cause;
        }

        public void setCause(Throwable cause) {
            this.cause = cause;
        }
    }

    /**
     * 降级原因枚举.
     */
    enum FallbackReason {
        /**
         * ES不可用
         */
        ES_UNAVAILABLE,
        /**
         * Redis不可用
         */
        REDIS_UNAVAILABLE,
        /**
         * 熔断触发
         */
        CIRCUIT_BREAKER,
        /**
         * 限流触发
         */
        RATE_LIMITED,
        /**
         * 手动降级
         */
        MANUAL_DEGRADE,
        /**
         * 超时
         */
        TIMEOUT,
        /**
         * 其他异常
         */
        OTHER
    }

    /**
     * 执行降级逻辑.
     *
     * @param context 降级上下文
     * @param <T>     返回数据类型
     * @return 降级响应
     */
    default <T> DsaResponse<T> doFallback(FallbackContext context) {
        return null;
    }
}
