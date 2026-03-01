package io.github.dbsearchaccel.admin.service;

import io.github.dbsearchaccel.admin.model.DsaDashboardData;
import java.util.Map;

/**
 * DSA监控管理服务接口
 *
 * @author DBSearchAccel Team
 */
public interface DsaMonitorManageService {

    DsaDashboardData getDashboardData();

    Map<String, Object> getSceneMetrics(String sceneCode);

    Map<String, Object> getHealthStatus();

    Map<String, Object> getAlertRules();

    boolean updateAlertRule(String ruleName, Map<String, Object> config);
}
