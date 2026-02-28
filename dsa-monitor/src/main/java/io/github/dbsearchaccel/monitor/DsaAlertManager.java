package io.github.dbsearchaccel.monitor;

import java.util.List;
import java.util.Map;

/**
 * 告警管理器接口.
 * <p>
 * 提供告警规则配置、告警触发、告警通知等功能.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaAlertManager {

    /**
     * 发送告警.
     *
     * @param alertType 告警类型
     * @param sceneCode 场景编码
     * @param message   告警消息
     * @param level     告警级别（INFO/WARN/ERROR/CRITICAL）
     */
    void sendAlert(String alertType, String sceneCode, String message, String level);

    /**
     * 发送告警（带详细信息）.
     *
     * @param alertType 告警类型
     * @param sceneCode 场景编码
     * @param message   告警消息
     * @param level     告警级别
     * @param details   详细信息
     */
    void sendAlert(String alertType, String sceneCode, String message, String level, Map<String, Object> details);

    /**
     * 添加告警规则.
     *
     * @param rule 告警规则
     * @return true表示添加成功
     */
    boolean addRule(DsaAlertRule rule);

    /**
     * 移除告警规则.
     *
     * @param ruleId 规则ID
     * @return true表示移除成功
     */
    boolean removeRule(String ruleId);

    /**
     * 获取所有告警规则.
     *
     * @return 告警规则列表
     */
    List<DsaAlertRule> getRules();

    /**
     * 检查是否应该触发告警.
     * <p>
     * 根据告警规则和当前指标值判断是否需要触发告警
     * </p>
     *
     * @param rule 告警规则
     * @return true表示应该触发告警
     */
    boolean shouldAlert(DsaAlertRule rule);

    /**
     * 获取最近的告警记录.
     *
     * @param limit 数量限制
     * @return 告警记录列表
     */
    List<DsaAlertRecord> getRecentAlerts(int limit);
}
