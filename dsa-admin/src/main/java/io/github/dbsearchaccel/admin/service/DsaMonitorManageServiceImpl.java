package io.github.dbsearchaccel.admin.service;

import io.github.dbsearchaccel.admin.model.DsaDashboardData;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * DSA监控管理服务实现
 *
 * @author DBSearchAccel Team
 */
@Service
public class DsaMonitorManageServiceImpl implements DsaMonitorManageService {

    private final AtomicLong totalQueries = new AtomicLong(0);
    private final AtomicLong totalLatency = new AtomicLong(0);
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong degradeCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    private final Map<String, AtomicLong> sceneQueryCount = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> sceneLatency = new ConcurrentHashMap<>();
    private final Map<String, Object> alertRules = new ConcurrentHashMap<>();

    public DsaMonitorManageServiceImpl() {
        initDefaultAlertRules();
    }

    private void initDefaultAlertRules() {
        Map<String, Object> latencyRule = new HashMap<>();
        latencyRule.put("type", "LATENCY");
        latencyRule.put("threshold", 1000);
        latencyRule.put("enabled", true);
        alertRules.put("high_latency", latencyRule);

        Map<String, Object> errorRateRule = new HashMap<>();
        errorRateRule.put("type", "ERROR_RATE");
        errorRateRule.put("threshold", 0.05);
        errorRateRule.put("enabled", true);
        alertRules.put("high_error_rate", errorRateRule);
    }

    @Override
    public DsaDashboardData getDashboardData() {
        DsaDashboardData data = new DsaDashboardData();
        data.setTotalQueries(totalQueries.get());
        long queries = totalQueries.get();
        data.setAvgLatency(queries > 0 ? totalLatency.get() / queries : 0L);
        long hits = hitCount.get();
        data.setHitRate(queries > 0 ? (double) hits / queries * 100 : 0.0);
        data.setDegradeCount(degradeCount.get());
        data.setErrorCount(errorCount.get());

        Map<String, Long> queryCountMap = new HashMap<>();
        sceneQueryCount.forEach((k, v) -> queryCountMap.put(k, v.get()));
        data.setSceneQueryCount(queryCountMap);

        Map<String, Double> latencyMap = new HashMap<>();
        sceneLatency.forEach((k, v) -> {
            long count = sceneQueryCount.getOrDefault(k, new AtomicLong(0)).get();
            latencyMap.put(k, count > 0 ? (double) v.get() / count : 0.0);
        });
        data.setSceneLatency(latencyMap);

        Map<String, Long> esQueryCount = new HashMap<>();
        esQueryCount.put("total", queries);
        data.setEsQueryCount(esQueryCount);

        Map<String, Long> dbQueryCount = new HashMap<>();
        dbQueryCount.put("total", degradeCount.get());
        data.setDbQueryCount(dbQueryCount);

        return data;
    }

    @Override
    public Map<String, Object> getSceneMetrics(String sceneCode) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("sceneCode", sceneCode);
        metrics.put("queryCount", sceneQueryCount.getOrDefault(sceneCode, new AtomicLong(0)).get());
        metrics.put("totalLatency", sceneLatency.getOrDefault(sceneCode, new AtomicLong(0)).get());
        return metrics;
    }

    @Override
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        Map<String, Object> components = new HashMap<>();
        components.put("elasticsearch", Map.of("status", "UP"));
        components.put("database", Map.of("status", "UP"));
        components.put("redis", Map.of("status", "UP"));
        health.put("components", components);
        return health;
    }

    @Override
    public Map<String, Object> getAlertRules() {
        return new HashMap<>(alertRules);
    }

    @Override
    public boolean updateAlertRule(String ruleName, Map<String, Object> config) {
        if (alertRules.containsKey(ruleName)) {
            alertRules.put(ruleName, config);
            return true;
        }
        return false;
    }
}
