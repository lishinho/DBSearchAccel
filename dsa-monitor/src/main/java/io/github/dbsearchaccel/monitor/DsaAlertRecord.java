package io.github.dbsearchaccel.monitor;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 告警记录实体.
 * <p>
 * 记录每次告警的详细信息，包括触发时间、告警内容等.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Data
public class DsaAlertRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID.
     */
    private String recordId;

    /**
     * 规则ID.
     */
    private String ruleId;

    /**
     * 告警类型.
     */
    private String alertType;

    /**
     * 场景编码.
     */
    private String sceneCode;

    /**
     * 告警消息.
     */
    private String message;

    /**
     * 告警级别.
     */
    private String level;

    /**
     * 触发时间.
     */
    private long triggerTime;

    /**
     * 当前指标值.
     */
    private double metricValue;

    /**
     * 阈值.
     */
    private double threshold;

    /**
     * 详细信息.
     */
    private Map<String, Object> details;

    /**
     * 处理状态.
     * <p>
     * PENDING、PROCESSING、RESOLVED、IGNORED
     * </p>
     */
    private String status;

    /**
     * 处理时间.
     */
    private Long resolvedTime;

    /**
     * 处理人.
     */
    private String resolvedBy;

    /**
     * 静态工厂方法.
     *
     * @param rule      告警规则
     * @param sceneCode 场景编码
     * @param message   告警消息
     * @param value     当前值
     * @return 告警记录
     */
    public static DsaAlertRecord of(DsaAlertRule rule, String sceneCode, String message, double value) {
        DsaAlertRecord record = new DsaAlertRecord();
        record.setRecordId(java.util.UUID.randomUUID().toString().replace("-", ""));
        record.setRuleId(rule.getRuleId());
        record.setAlertType(rule.getAlertType());
        record.setSceneCode(sceneCode);
        record.setMessage(message);
        record.setLevel(rule.getLevel());
        record.setTriggerTime(System.currentTimeMillis());
        record.setMetricValue(value);
        record.setThreshold(rule.getThreshold());
        record.setStatus("PENDING");
        return record;
    }

    /**
     * 标记为已处理.
     *
     * @param resolvedBy 处理人
     */
    public void resolve(String resolvedBy) {
        this.status = "RESOLVED";
        this.resolvedTime = System.currentTimeMillis();
        this.resolvedBy = resolvedBy;
    }
}
