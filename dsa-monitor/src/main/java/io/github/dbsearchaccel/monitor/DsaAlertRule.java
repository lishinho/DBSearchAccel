package io.github.dbsearchaccel.monitor;

import lombok.Data;

import java.io.Serializable;

/**
 * 告警规则实体.
 * <p>
 * 定义告警触发条件、阈值、通知方式等.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Data
public class DsaAlertRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规则ID.
     */
    private String ruleId;

    /**
     * 规则名称.
     */
    private String ruleName;

    /**
     * 告警类型.
     * <p>
     * 如：QUERY_ERROR、ES_ERROR、SYNC_ERROR、FALLBACK_TRIGGERED等
     * </p>
     */
    private String alertType;

    /**
     * 指标名称.
     */
    private String metricName;

    /**
     * 比较操作符.
     * <p>
     * 支持：>、>=、<、<=、==、!=
     * </p>
     */
    private String operator;

    /**
     * 阈值.
     */
    private double threshold;

    /**
     * 持续时间（秒）.
     * <p>
     * 连续多少秒超过阈值才触发告警
     * </p>
     */
    private int durationSeconds;

    /**
     * 告警级别.
     * <p>
     * INFO、WARN、ERROR、CRITICAL
     * </p>
     */
    private String level;

    /**
     * 通知渠道.
     * <p>
     * 如：email、sms、webhook、dingtalk
     * </p>
     */
    private String notifyChannel;

    /**
     * 通知接收人.
     */
    private String notifyReceivers;

    /**
     * 是否启用.
     */
    private boolean enabled = true;

    /**
     * 冷却时间（秒）.
     * <p>
     * 同一告警两次触发之间的最小间隔
     * </p>
     */
    private int cooldownSeconds = 300;

    /**
     * 静态工厂方法.
     *
     * @param ruleId    规则ID
     * @param ruleName  规则名称
     * @param alertType 告警类型
     * @return 告警规则
     */
    public static DsaAlertRule of(String ruleId, String ruleName, String alertType) {
        DsaAlertRule rule = new DsaAlertRule();
        rule.setRuleId(ruleId);
        rule.setRuleName(ruleName);
        rule.setAlertType(alertType);
        return rule;
    }

    /**
     * 判断是否超过阈值.
     *
     * @param value 当前值
     * @return true表示超过阈值
     */
    public boolean isExceeded(double value) {
        switch (operator) {
            case ">":
                return value > threshold;
            case ">=":
                return value >= threshold;
            case "<":
                return value < threshold;
            case "<=":
                return value <= threshold;
            case "==":
                return value == threshold;
            case "!=":
                return value != threshold;
            default:
                return false;
        }
    }
}
