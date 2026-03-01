package io.github.dbsearchaccel.consistency;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 校验报告实体.
 * <p>
 * 汇总多次校验的结果，生成完整的校验报告.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaCheckReport implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 报告ID
     */
    private String reportId;

    /**
     * 校验任务ID
     */
    private String taskId;

    /**
     * 场景编码
     */
    private String sceneCode;

    /**
     * 校验状态
     */
    private DsaCheckStatus status;

    /**
     * 校验类型列表
     */
    private List<DsaCheckType> checkTypes;

    /**
     * 校验结果列表
     */
    private List<DsaCheckResult> results;

    /**
     * 总体是否一致
     */
    private boolean overallConsistent;

    /**
     * 总校验项数
     */
    private int totalCheckCount;

    /**
     * 通过校验项数
     */
    private int passedCheckCount;

    /**
     * 失败校验项数
     */
    private int failedCheckCount;

    /**
     * 报告生成时间
     */
    private Date generateTime;

    /**
     * 校验开始时间
     */
    private Date startTime;

    /**
     * 校验结束时间
     */
    private Date endTime;

    /**
     * 总耗时（毫秒）
     */
    private long totalElapsedMs;

    /**
     * 摘要信息
     */
    private String summary;

    /**
     * 建议修复方案
     */
    private List<String> repairSuggestions;

    public DsaCheckReport() {
        this.checkTypes = new ArrayList<>();
        this.results = new ArrayList<>();
        this.repairSuggestions = new ArrayList<>();
        this.generateTime = new Date();
    }

    public DsaCheckReport(String taskId, String sceneCode) {
        this();
        this.taskId = taskId;
        this.sceneCode = sceneCode;
        this.status = DsaCheckStatus.PENDING;
    }

    /**
     * 添加校验结果.
     *
     * @param result 校验结果
     */
    public void addResult(DsaCheckResult result) {
        if (this.results == null) {
            this.results = new ArrayList<>();
        }
        this.results.add(result);
        this.totalCheckCount++;
        if (result.isConsistent()) {
            this.passedCheckCount++;
        } else {
            this.failedCheckCount++;
        }
    }

    /**
     * 计算总体一致性.
     */
    public void calculateOverallConsistency() {
        this.overallConsistent = (this.failedCheckCount == 0);
    }

    /**
     * 生成摘要.
     */
    public void generateSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("校验报告: 场景[").append(sceneCode).append("], ");
        sb.append("总校验项: ").append(totalCheckCount).append(", ");
        sb.append("通过: ").append(passedCheckCount).append(", ");
        sb.append("失败: ").append(failedCheckCount).append(", ");
        sb.append("总体结果: ").append(overallConsistent ? "一致" : "不一致");
        this.summary = sb.toString();
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getSceneCode() {
        return sceneCode;
    }

    public void setSceneCode(String sceneCode) {
        this.sceneCode = sceneCode;
    }

    public DsaCheckStatus getStatus() {
        return status;
    }

    public void setStatus(DsaCheckStatus status) {
        this.status = status;
    }

    public List<DsaCheckType> getCheckTypes() {
        return checkTypes;
    }

    public void setCheckTypes(List<DsaCheckType> checkTypes) {
        this.checkTypes = checkTypes;
    }

    public List<DsaCheckResult> getResults() {
        return results;
    }

    public void setResults(List<DsaCheckResult> results) {
        this.results = results;
    }

    public boolean isOverallConsistent() {
        return overallConsistent;
    }

    public void setOverallConsistent(boolean overallConsistent) {
        this.overallConsistent = overallConsistent;
    }

    public int getTotalCheckCount() {
        return totalCheckCount;
    }

    public void setTotalCheckCount(int totalCheckCount) {
        this.totalCheckCount = totalCheckCount;
    }

    public int getPassedCheckCount() {
        return passedCheckCount;
    }

    public void setPassedCheckCount(int passedCheckCount) {
        this.passedCheckCount = passedCheckCount;
    }

    public int getFailedCheckCount() {
        return failedCheckCount;
    }

    public void setFailedCheckCount(int failedCheckCount) {
        this.failedCheckCount = failedCheckCount;
    }

    public Date getGenerateTime() {
        return generateTime;
    }

    public void setGenerateTime(Date generateTime) {
        this.generateTime = generateTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public long getTotalElapsedMs() {
        return totalElapsedMs;
    }

    public void setTotalElapsedMs(long totalElapsedMs) {
        this.totalElapsedMs = totalElapsedMs;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getRepairSuggestions() {
        return repairSuggestions;
    }

    public void setRepairSuggestions(List<String> repairSuggestions) {
        this.repairSuggestions = repairSuggestions;
    }
}
