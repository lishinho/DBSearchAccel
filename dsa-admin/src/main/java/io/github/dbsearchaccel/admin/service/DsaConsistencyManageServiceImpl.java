package io.github.dbsearchaccel.admin.service;

import io.github.dbsearchaccel.admin.model.DsaCheckTaskInfo;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * DSA一致性校验管理服务实现
 *
 * @author DBSearchAccel Team
 */
@Service
public class DsaConsistencyManageServiceImpl implements DsaConsistencyManageService {

    private final Map<String, DsaCheckTaskInfo> taskStore = new ConcurrentHashMap<>();

    private final Map<String, String> reportStore = new ConcurrentHashMap<>();

    @Override
    public String triggerCheck(String sceneCode, String checkType) {
        String taskId = UUID.randomUUID().toString();
        DsaCheckTaskInfo task = new DsaCheckTaskInfo();
        task.setTaskId(taskId);
        task.setSceneCode(sceneCode);
        task.setCheckType(checkType);
        task.setStatus("RUNNING");
        task.setTotalCount(0L);
        task.setMismatchCount(0L);
        task.setMissingInEsCount(0L);
        task.setMissingInDbCount(0L);
        task.setStartTime(System.currentTimeMillis());
        taskStore.put(taskId, task);
        return taskId;
    }

    @Override
    public List<DsaCheckTaskInfo> listCheckTasks(String sceneCode) {
        return taskStore.values().stream()
                .filter(t -> sceneCode == null || sceneCode.isEmpty() || sceneCode.equals(t.getSceneCode()))
                .sorted((a, b) -> Long.compare(b.getStartTime() != null ? b.getStartTime() : 0, a.getStartTime() != null ? a.getStartTime() : 0))
                .collect(Collectors.toList());
    }

    @Override
    public DsaCheckTaskInfo getCheckTask(String taskId) {
        return taskStore.get(taskId);
    }

    @Override
    public String getCheckReport(String taskId) {
        DsaCheckTaskInfo task = taskStore.get(taskId);
        if (task == null) {
            return null;
        }
        String report = reportStore.get(taskId);
        if (report == null) {
            report = generateReport(task);
            reportStore.put(taskId, report);
        }
        return report;
    }

    @Override
    public byte[] exportReport(String taskId, String format) {
        String report = getCheckReport(taskId);
        if (report == null) {
            return new byte[0];
        }
        if ("CSV".equalsIgnoreCase(format)) {
            return convertToCsv(report).getBytes(StandardCharsets.UTF_8);
        } else if ("HTML".equalsIgnoreCase(format)) {
            return convertToHtml(report).getBytes(StandardCharsets.UTF_8);
        }
        return report.getBytes(StandardCharsets.UTF_8);
    }

    private String generateReport(DsaCheckTaskInfo task) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"taskId\": \"").append(task.getTaskId()).append("\",\n");
        sb.append("  \"sceneCode\": \"").append(task.getSceneCode()).append("\",\n");
        sb.append("  \"checkType\": \"").append(task.getCheckType()).append("\",\n");
        sb.append("  \"status\": \"").append(task.getStatus()).append("\",\n");
        sb.append("  \"totalCount\": ").append(task.getTotalCount()).append(",\n");
        sb.append("  \"mismatchCount\": ").append(task.getMismatchCount()).append(",\n");
        sb.append("  \"missingInEsCount\": ").append(task.getMissingInEsCount()).append(",\n");
        sb.append("  \"missingInDbCount\": ").append(task.getMissingInDbCount()).append(",\n");
        sb.append("  \"consistencyRate\": \"").append(calculateConsistencyRate(task)).append("%\"\n");
        sb.append("}");
        return sb.toString();
    }

    private double calculateConsistencyRate(DsaCheckTaskInfo task) {
        if (task.getTotalCount() == null || task.getTotalCount() == 0) {
            return 100.0;
        }
        long errorCount = (task.getMismatchCount() != null ? task.getMismatchCount() : 0)
                + (task.getMissingInEsCount() != null ? task.getMissingInEsCount() : 0)
                + (task.getMissingInDbCount() != null ? task.getMissingInDbCount() : 0);
        return (1.0 - (double) errorCount / task.getTotalCount()) * 100;
    }

    private String convertToCsv(String jsonReport) {
        return "taskId,sceneCode,checkType,status,totalCount,mismatchCount\n" + jsonReport;
    }

    private String convertToHtml(String jsonReport) {
        return "<!DOCTYPE html><html><head><title>Report</title></head><body><pre>" + jsonReport + "</pre></body></html>";
    }
}
