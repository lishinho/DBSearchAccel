package io.github.dbsearchaccel.admin.controller;

import io.github.dbsearchaccel.admin.model.DsaDashboardData;
import io.github.dbsearchaccel.admin.model.DsaResult;
import io.github.dbsearchaccel.admin.service.DsaMonitorManageService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * DSA监控管理控制器
 * 提供监控指标的查询接口
 *
 * @author DBSearchAccel Team
 */
@RestController
@RequestMapping("/api/monitor")
public class DsaMonitorController {

    private final DsaMonitorManageService monitorManageService;

    public DsaMonitorController(DsaMonitorManageService monitorManageService) {
        this.monitorManageService = monitorManageService;
    }

    @GetMapping("/dashboard")
    public DsaResult<DsaDashboardData> getDashboardData() {
        return DsaResult.success(monitorManageService.getDashboardData());
    }

    @GetMapping("/metrics/{sceneCode}")
    public DsaResult<Map<String, Object>> getSceneMetrics(@PathVariable String sceneCode) {
        return DsaResult.success(monitorManageService.getSceneMetrics(sceneCode));
    }

    @GetMapping("/health")
    public DsaResult<Map<String, Object>> getHealthStatus() {
        return DsaResult.success(monitorManageService.getHealthStatus());
    }

    @GetMapping("/alerts")
    public DsaResult<Map<String, Object>> getAlertRules() {
        return DsaResult.success(monitorManageService.getAlertRules());
    }

    @PutMapping("/alerts/{ruleName}")
    public DsaResult<Boolean> updateAlertRule(
            @PathVariable String ruleName,
            @RequestBody Map<String, Object> config) {
        return DsaResult.success(monitorManageService.updateAlertRule(ruleName, config));
    }
}
