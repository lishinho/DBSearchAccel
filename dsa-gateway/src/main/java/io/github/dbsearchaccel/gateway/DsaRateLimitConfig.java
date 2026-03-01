package io.github.dbsearchaccel.gateway;

import java.io.Serializable;

/**
 * 限流配置.
 * <p>
 * 定义接口级别的限流规则.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaRateLimitConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否启用限流
     */
    private boolean enabled;

    /**
     * 限流策略
     */
    private LimitStrategy strategy;

    /**
     * QPS阈值
     */
    private int qpsThreshold;

    /**
     * 并发数阈值
     */
    private int concurrentThreshold;

    /**
     * 限流后的行为
     */
    private LimitBehavior behavior;

    /**
     * 降级场景编码
     */
    private String degradeSceneCode;

    public DsaRateLimitConfig() {
        this.enabled = true;
        this.strategy = LimitStrategy.QPS;
        this.qpsThreshold = 100;
        this.concurrentThreshold = 50;
        this.behavior = LimitBehavior.DEGRADE;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LimitStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(LimitStrategy strategy) {
        this.strategy = strategy;
    }

    public int getQpsThreshold() {
        return qpsThreshold;
    }

    public void setQpsThreshold(int qpsThreshold) {
        this.qpsThreshold = qpsThreshold;
    }

    public int getConcurrentThreshold() {
        return concurrentThreshold;
    }

    public void setConcurrentThreshold(int concurrentThreshold) {
        this.concurrentThreshold = concurrentThreshold;
    }

    public LimitBehavior getBehavior() {
        return behavior;
    }

    public void setBehavior(LimitBehavior behavior) {
        this.behavior = behavior;
    }

    public String getDegradeSceneCode() {
        return degradeSceneCode;
    }

    public void setDegradeSceneCode(String degradeSceneCode) {
        this.degradeSceneCode = degradeSceneCode;
    }

    /**
     * 限流策略枚举.
     */
    public enum LimitStrategy {
        /**
         * QPS限流
         */
        QPS,
        /**
         * 并发数限流
         */
        CONCURRENT,
        /**
         * 混合限流（QPS+并发）
         */
        MIXED
    }

    /**
     * 限流行为枚举.
     */
    public enum LimitBehavior {
        /**
         * 直接拒绝
         */
        REJECT,
        /**
         * 降级处理
         */
        DEGRADE,
        /**
         * 排队等待
         */
        WAIT
    }
}
