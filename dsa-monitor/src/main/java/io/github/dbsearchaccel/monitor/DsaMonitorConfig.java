package io.github.dbsearchaccel.monitor;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 监控模块配置类.
 * <p>
 * 自动配置指标采集器、告警管理器等组件.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Configuration
public class DsaMonitorConfig {

    /**
     * 配置指标采集器.
     *
     * @param meterRegistry Meter注册器
     * @return 指标采集器实例
     */
    @Bean
    @ConditionalOnBean(MeterRegistry.class)
    @ConditionalOnMissingBean(DsaMetricsCollector.class)
    public DsaMetricsCollector dsaMetricsCollector(MeterRegistry meterRegistry) {
        return new DsaMetricsCollectorImpl(meterRegistry);
    }

    /**
     * 配置告警管理器.
     *
     * @return 告警管理器实例
     */
    @Bean
    @ConditionalOnMissingBean(DsaAlertManager.class)
    public DsaAlertManager dsaAlertManager() {
        DsaAlertManagerImpl alertManager = new DsaAlertManagerImpl();

        DsaAlertRule queryErrorRule = DsaAlertRule.of(
                "query_error_rate",
                "查询错误率告警",
                "QUERY_ERROR"
        );
        queryErrorRule.setMetricName("dsa_query_failed");
        queryErrorRule.setOperator(">");
        queryErrorRule.setThreshold(10);
        queryErrorRule.setLevel("WARN");
        alertManager.addRule(queryErrorRule);

        DsaAlertRule fallbackRule = DsaAlertRule.of(
                "fallback_rate",
                "降级率告警",
                "FALLBACK_TRIGGERED"
        );
        fallbackRule.setMetricName("dsa_fallback_total");
        fallbackRule.setOperator(">");
        fallbackRule.setThreshold(100);
        fallbackRule.setLevel("ERROR");
        alertManager.addRule(fallbackRule);

        return alertManager;
    }
}
