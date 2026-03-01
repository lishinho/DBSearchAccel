package io.github.dbsearchaccel.consistency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 一致性校验服务实现.
 * <p>
 * 协调校验流程，管理校验任务和报告.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Service
public class DsaConsistencyCheckServiceImpl implements DsaConsistencyCheckService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DsaConsistencyCheckServiceImpl.class);

    private final Map<String, DsaCheckTask> taskCache = new ConcurrentHashMap<>();

    private final Map<String, DsaCheckReport> reportCache = new ConcurrentHashMap<>();

    @Autowired
    private DsaPrimaryKeyChecker primaryKeyChecker;

    @Autowired
    private DsaFieldChecker fieldChecker;

    @Autowired(required = false)
    private DsaCheckReportGenerator reportGenerator;

    @Override
    public DsaCheckTask createTask(String sceneCode, DsaCheckType checkType) {
        LOGGER.info("Creating check task: sceneCode={}, checkType={}", sceneCode, checkType);

        String taskId = generateTaskId();
        DsaCheckTask task = new DsaCheckTask(sceneCode, checkType);
        task.setTaskId(taskId);
        task.setCreateTime(new Date());

        taskCache.put(taskId, task);

        LOGGER.info("Check task created: taskId={}", taskId);
        return task;
    }

    @Override
    public DsaCheckReport executeCheck(String taskId) {
        LOGGER.info("Executing check task: taskId={}", taskId);

        DsaCheckTask task = taskCache.get(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }

        task.setStatus(DsaCheckStatus.RUNNING);
        task.setStartTime(new Date());

        DsaCheckReport report = new DsaCheckReport(taskId, task.getSceneCode());
        report.setStartTime(new Date());
        report.setStatus(DsaCheckStatus.RUNNING);

        try {
            DsaCheckResult result = executeCheckByType(task);
            report.addResult(result);
            report.setStatus(DsaCheckStatus.COMPLETED);
            task.setStatus(DsaCheckStatus.COMPLETED);

        } catch (Exception e) {
            LOGGER.error("Check task failed: taskId={}", taskId, e);
            report.setStatus(DsaCheckStatus.FAILED);
            task.setStatus(DsaCheckStatus.FAILED);
        }

        report.setEndTime(new Date());
        report.setTotalElapsedMs(report.getEndTime().getTime() - report.getStartTime().getTime());
        report.calculateOverallConsistency();
        report.generateSummary();

        task.setEndTime(new Date());
        task.setReport(report);
        task.setProgress(100);

        reportCache.put(taskId, report);

        LOGGER.info("Check task completed: taskId={}, status={}, consistent={}",
                taskId, task.getStatus(), report.isOverallConsistent());

        return report;
    }

    @Override
    @Async
    public void executeCheckAsync(String taskId) {
        LOGGER.info("Executing async check task: taskId={}", taskId);
        executeCheck(taskId);
    }

    @Override
    public boolean cancelTask(String taskId) {
        LOGGER.info("Cancelling check task: taskId={}", taskId);

        DsaCheckTask task = taskCache.get(taskId);
        if (task == null) {
            return false;
        }

        if (task.isCompleted()) {
            LOGGER.warn("Cannot cancel completed task: taskId={}", taskId);
            return false;
        }

        task.setStatus(DsaCheckStatus.CANCELLED);
        task.setEndTime(new Date());

        LOGGER.info("Check task cancelled: taskId={}", taskId);
        return true;
    }

    @Override
    public DsaCheckTask getTask(String taskId) {
        return taskCache.get(taskId);
    }

    @Override
    public List<DsaCheckTask> getTasksByScene(String sceneCode) {
        List<DsaCheckTask> tasks = new ArrayList<>();
        for (DsaCheckTask task : taskCache.values()) {
            if (sceneCode.equals(task.getSceneCode())) {
                tasks.add(task);
            }
        }
        tasks.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
        return tasks;
    }

    @Override
    public DsaCheckReport getReport(String taskId) {
        return reportCache.get(taskId);
    }

    @Override
    public String exportReport(String taskId, String format) {
        LOGGER.info("Exporting report: taskId={}, format={}", taskId, format);

        DsaCheckReport report = reportCache.get(taskId);
        if (report == null) {
            throw new IllegalArgumentException("Report not found for task: " + taskId);
        }

        if (reportGenerator != null) {
            return reportGenerator.generate(report, format);
        }

        return generateDefaultReport(report, format);
    }

    @Override
    public int getProgress(String taskId) {
        DsaCheckTask task = taskCache.get(taskId);
        if (task == null) {
            return 0;
        }
        return task.getProgress();
    }

    /**
     * 根据校验类型执行校验.
     *
     * @param task 校验任务
     * @return 校验结果
     */
    private DsaCheckResult executeCheckByType(DsaCheckTask task) {
        switch (task.getCheckType()) {
            case PRIMARY_KEY:
                return primaryKeyChecker.check(task);
            case FIELD:
                Set<Object> esKeys = primaryKeyChecker.getEsPrimaryKeys(task.getIndexName(), task.getPkField());
                return fieldChecker.check(task, new ArrayList<>(esKeys));
            case COUNT:
                return executeCountCheck(task);
            case FULL:
                return executeFullCheck(task);
            default:
                throw new IllegalArgumentException("Unknown check type: " + task.getCheckType());
        }
    }

    /**
     * 执行数量校验.
     *
     * @param task 校验任务
     * @return 校验结果
     */
    private DsaCheckResult executeCountCheck(DsaCheckTask task) {
        LOGGER.info("Executing count check for task: {}", task.getTaskId());

        DsaCheckResult result = new DsaCheckResult();
        result.setTaskId(task.getTaskId());
        result.setSceneCode(task.getSceneCode());
        result.setCheckType(DsaCheckType.COUNT);
        result.setStartTime(new Date());

        long esCount = primaryKeyChecker.getEsPrimaryKeyCount(task.getIndexName());
        long dbCount = primaryKeyChecker.getDbPrimaryKeyCount(task.getTableName());

        result.setEsCount(esCount);
        result.setDbCount(dbCount);
        result.setDiffCount(Math.abs(esCount - dbCount));
        result.setConsistent(esCount == dbCount);

        result.setEndTime(new Date());
        result.setElapsedMs(result.getEndTime().getTime() - result.getStartTime().getTime());

        return result;
    }

    /**
     * 执行全量校验.
     *
     * @param task 校验任务
     * @return 校验结果
     */
    private DsaCheckResult executeFullCheck(DsaCheckTask task) {
        LOGGER.info("Executing full check for task: {}", task.getTaskId());

        DsaCheckResult result = primaryKeyChecker.check(task);

        if (!result.isConsistent() && task.isCompareFields()) {
            Set<Object> esKeys = primaryKeyChecker.getEsPrimaryKeys(task.getIndexName(), task.getPkField());
            DsaCheckResult fieldResult = fieldChecker.check(task, new ArrayList<>(esKeys));
            result.setFieldDiffDetails(fieldResult.getFieldDiffDetails());
        }

        return result;
    }

    /**
     * 生成任务ID.
     *
     * @return 任务ID
     */
    private String generateTaskId() {
        return "CHECK-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 生成默认报告.
     *
     * @param report 校验报告
     * @param format 格式
     * @return 报告内容
     */
    private String generateDefaultReport(DsaCheckReport report, String format) {
        if ("json".equalsIgnoreCase(format)) {
            return toJson(report);
        } else if ("csv".equalsIgnoreCase(format)) {
            return toCsv(report);
        } else {
            return toText(report);
        }
    }

    private String toJson(DsaCheckReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"reportId\":\"").append(report.getReportId()).append("\",");
        sb.append("\"taskId\":\"").append(report.getTaskId()).append("\",");
        sb.append("\"sceneCode\":\"").append(report.getSceneCode()).append("\",");
        sb.append("\"status\":\"").append(report.getStatus()).append("\",");
        sb.append("\"overallConsistent\":").append(report.isOverallConsistent()).append(",");
        sb.append("\"totalCheckCount\":").append(report.getTotalCheckCount()).append(",");
        sb.append("\"passedCheckCount\":").append(report.getPassedCheckCount()).append(",");
        sb.append("\"failedCheckCount\":").append(report.getFailedCheckCount()).append(",");
        sb.append("\"summary\":\"").append(report.getSummary()).append("\"");
        sb.append("}");
        return sb.toString();
    }

    private String toCsv(DsaCheckReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("ReportId,TaskId,SceneCode,Status,OverallConsistent,TotalCheckCount,PassedCheckCount,FailedCheckCount,Summary\n");
        sb.append(report.getReportId()).append(",");
        sb.append(report.getTaskId()).append(",");
        sb.append(report.getSceneCode()).append(",");
        sb.append(report.getStatus()).append(",");
        sb.append(report.isOverallConsistent()).append(",");
        sb.append(report.getTotalCheckCount()).append(",");
        sb.append(report.getPassedCheckCount()).append(",");
        sb.append(report.getFailedCheckCount()).append(",");
        sb.append(report.getSummary());
        return sb.toString();
    }

    private String toText(DsaCheckReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 数据一致性校验报告 ===\n");
        sb.append("报告ID: ").append(report.getReportId()).append("\n");
        sb.append("任务ID: ").append(report.getTaskId()).append("\n");
        sb.append("场景编码: ").append(report.getSceneCode()).append("\n");
        sb.append("校验状态: ").append(report.getStatus()).append("\n");
        sb.append("总体一致性: ").append(report.isOverallConsistent() ? "一致" : "不一致").append("\n");
        sb.append("总校验项: ").append(report.getTotalCheckCount()).append("\n");
        sb.append("通过项: ").append(report.getPassedCheckCount()).append("\n");
        sb.append("失败项: ").append(report.getFailedCheckCount()).append("\n");
        sb.append("总耗时: ").append(report.getTotalElapsedMs()).append("ms\n");
        sb.append("摘要: ").append(report.getSummary()).append("\n");
        return sb.toString();
    }
}
