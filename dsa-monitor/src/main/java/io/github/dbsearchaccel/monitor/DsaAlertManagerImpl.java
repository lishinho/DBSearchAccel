package io.github.dbsearchaccel.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 告警管理器实现类.
 * <p>
 * 提供告警规则管理、告警触发、告警记录等功能.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaAlertManagerImpl implements DsaAlertManager {

    private static final Logger log = LoggerFactory.getLogger(DsaAlertManagerImpl.class);

    private final List<DsaAlertRule> rules = new CopyOnWriteArrayList<>();
    private final List<DsaAlertRecord> alertRecords = new CopyOnWriteArrayList<>();
    private final Map<String, Long> lastAlertTime = new ConcurrentHashMap<>();

    private static final int MAX_RECORDS = 1000;

    @Override
    public void sendAlert(String alertType, String sceneCode, String message, String level) {
        sendAlert(alertType, sceneCode, message, level, null);
    }

    @Override
    public void sendAlert(String alertType, String sceneCode, String message, String level, Map<String, Object> details) {
        log.warn("Alert triggered: type={}, scene={}, level={}, message={}",
                alertType, sceneCode, level, message);

        DsaAlertRecord record = new DsaAlertRecord();
        record.setRecordId(java.util.UUID.randomUUID().toString().replace("-", ""));
        record.setAlertType(alertType);
        record.setSceneCode(sceneCode);
        record.setMessage(message);
        record.setLevel(level);
        record.setTriggerTime(System.currentTimeMillis());
        record.setDetails(details);
        record.setStatus("PENDING");

        addAlertRecord(record);

        doNotify(record);
    }

    @Override
    public boolean addRule(DsaAlertRule rule) {
        if (rule == null || rule.getRuleId() == null) {
            return false;
        }

        removeRule(rule.getRuleId());
        rules.add(rule);
        log.info("Added alert rule: [{}] {}", rule.getRuleId(), rule.getRuleName());
        return true;
    }

    @Override
    public boolean removeRule(String ruleId) {
        if (ruleId == null) {
            return false;
        }

        boolean removed = rules.removeIf(r -> ruleId.equals(r.getRuleId()));
        if (removed) {
            log.info("Removed alert rule: [{}]", ruleId);
        }
        return removed;
    }

    @Override
    public List<DsaAlertRule> getRules() {
        return new ArrayList<>(rules);
    }

    @Override
    public boolean shouldAlert(DsaAlertRule rule) {
        if (rule == null || !rule.isEnabled()) {
            return false;
        }

        String key = rule.getRuleId();
        Long lastTime = lastAlertTime.get(key);

        if (lastTime != null) {
            long elapsed = System.currentTimeMillis() - lastTime;
            if (elapsed < rule.getCooldownSeconds() * 1000L) {
                return false;
            }
        }

        return true;
    }

    @Override
    public List<DsaAlertRecord> getRecentAlerts(int limit) {
        int size = Math.min(limit, alertRecords.size());
        List<DsaAlertRecord> result = new ArrayList<>();

        for (int i = alertRecords.size() - 1; i >= 0 && result.size() < size; i--) {
            result.add(alertRecords.get(i));
        }

        return result;
    }

    /**
     * 添加告警记录.
     *
     * @param record 告警记录
     */
    private void addAlertRecord(DsaAlertRecord record) {
        if (alertRecords.size() >= MAX_RECORDS) {
            alertRecords.remove(0);
        }
        alertRecords.add(record);
    }

    /**
     * 执行告警通知.
     *
     * @param record 告警记录
     */
    private void doNotify(DsaAlertRecord record) {
        String notifyChannel = "log";

        switch (notifyChannel) {
            case "log":
                logAlert(record);
                break;
            case "webhook":
                sendWebhook(record);
                break;
            case "email":
                sendEmail(record);
                break;
            case "dingtalk":
                sendDingtalk(record);
                break;
            default:
                logAlert(record);
        }

        lastAlertTime.put(record.getAlertType(), System.currentTimeMillis());
    }

    /**
     * 日志方式告警.
     *
     * @param record 告警记录
     */
    private void logAlert(DsaAlertRecord record) {
        log.error("ALERT [{}] {}: scene=[{}], message={}",
                record.getLevel(), record.getAlertType(),
                record.getSceneCode(), record.getMessage());
    }

    /**
     * 发送Webhook通知.
     *
     * @param record 告警记录
     */
    private void sendWebhook(DsaAlertRecord record) {
        log.info("Sending webhook notification for alert: {}", record.getRecordId());
    }

    /**
     * 发送邮件通知.
     *
     * @param record 告警记录
     */
    private void sendEmail(DsaAlertRecord record) {
        log.info("Sending email notification for alert: {}", record.getRecordId());
    }

    /**
     * 发送钉钉通知.
     *
     * @param record 告警记录
     */
    private void sendDingtalk(DsaAlertRecord record) {
        log.info("Sending dingtalk notification for alert: {}", record.getRecordId());
    }

    /**
     * 检查规则并触发告警.
     *
     * @param metricName 指标名称
     * @param value      指标值
     * @param sceneCode  场景编码
     */
    public void checkAndAlert(String metricName, double value, String sceneCode) {
        for (DsaAlertRule rule : rules) {
            if (!metricName.equals(rule.getMetricName())) {
                continue;
            }

            if (rule.isExceeded(value) && shouldAlert(rule)) {
                String message = String.format("Metric [%s] exceeded threshold: %.2f > %.2f",
                        metricName, value, rule.getThreshold());

                DsaAlertRecord record = DsaAlertRecord.of(rule, sceneCode, message, value);
                addAlertRecord(record);
                doNotify(record);
            }
        }
    }
}
