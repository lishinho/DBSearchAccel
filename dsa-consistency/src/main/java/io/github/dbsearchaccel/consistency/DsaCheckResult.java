package io.github.dbsearchaccel.consistency;

import java.io.Serializable;
import java.util.Date;

/**
 * 校验结果实体.
 * <p>
 * 封装单次校验的结果信息.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaCheckResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 结果ID
     */
    private String resultId;

    /**
     * 校验任务ID
     */
    private String taskId;

    /**
     * 场景编码
     */
    private String sceneCode;

    /**
     * 校验类型
     */
    private DsaCheckType checkType;

    /**
     * 是否一致
     */
    private boolean consistent;

    /**
     * ES数据量
     */
    private long esCount;

    /**
     * 数据库数据量
     */
    private long dbCount;

    /**
     * 差异数量
     */
    private long diffCount;

    /**
     * 缺失主键列表（ES有DB无）
     */
    private String missingInDb;

    /**
     * 缺失主键列表（DB有ES无）
     */
    private String missingInEs;

    /**
     * 字段差异详情
     */
    private String fieldDiffDetails;

    /**
     * 校验开始时间
     */
    private Date startTime;

    /**
     * 校验结束时间
     */
    private Date endTime;

    /**
     * 耗时（毫秒）
     */
    private long elapsedMs;

    /**
     * 错误信息
     */
    private String errorMessage;

    public DsaCheckResult() {
    }

    public DsaCheckResult(String taskId, String sceneCode, DsaCheckType checkType) {
        this.taskId = taskId;
        this.sceneCode = sceneCode;
        this.checkType = checkType;
        this.startTime = new Date();
    }

    public String getResultId() {
        return resultId;
    }

    public void setResultId(String resultId) {
        this.resultId = resultId;
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

    public DsaCheckType getCheckType() {
        return checkType;
    }

    public void setCheckType(DsaCheckType checkType) {
        this.checkType = checkType;
    }

    public boolean isConsistent() {
        return consistent;
    }

    public void setConsistent(boolean consistent) {
        this.consistent = consistent;
    }

    public long getEsCount() {
        return esCount;
    }

    public void setEsCount(long esCount) {
        this.esCount = esCount;
    }

    public long getDbCount() {
        return dbCount;
    }

    public void setDbCount(long dbCount) {
        this.dbCount = dbCount;
    }

    public long getDiffCount() {
        return diffCount;
    }

    public void setDiffCount(long diffCount) {
        this.diffCount = diffCount;
    }

    public String getMissingInDb() {
        return missingInDb;
    }

    public void setMissingInDb(String missingInDb) {
        this.missingInDb = missingInDb;
    }

    public String getMissingInEs() {
        return missingInEs;
    }

    public void setMissingInEs(String missingInEs) {
        this.missingInEs = missingInEs;
    }

    public String getFieldDiffDetails() {
        return fieldDiffDetails;
    }

    public void setFieldDiffDetails(String fieldDiffDetails) {
        this.fieldDiffDetails = fieldDiffDetails;
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

    public long getElapsedMs() {
        return elapsedMs;
    }

    public void setElapsedMs(long elapsedMs) {
        this.elapsedMs = elapsedMs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
