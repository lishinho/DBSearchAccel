package io.github.dbsearchaccel.consistency;

import java.io.Serializable;
import java.util.Date;

/**
 * 校验任务实体.
 * <p>
 * 封装校验任务的配置和状态信息.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaCheckTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 场景编码
     */
    private String sceneCode;

    /**
     * 索引名称
     */
    private String indexName;

    /**
     * 数据表名
     */
    private String tableName;

    /**
     * 主键字段名
     */
    private String pkField;

    /**
     * 校验类型
     */
    private DsaCheckType checkType;

    /**
     * 校验状态
     */
    private DsaCheckStatus status;

    /**
     * 批次大小
     */
    private int batchSize;

    /**
     * 是否比对字段值
     */
    private boolean compareFields;

    /**
     * 需要比对的字段列表
     */
    private String compareFieldList;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 进度百分比
     */
    private int progress;

    /**
     * 已处理数量
     */
    private long processedCount;

    /**
     * 总数量
     */
    private long totalCount;

    /**
     * 校验报告
     */
    private DsaCheckReport report;

    public DsaCheckTask() {
        this.status = DsaCheckStatus.PENDING;
        this.batchSize = 1000;
        this.compareFields = false;
        this.createTime = new Date();
        this.progress = 0;
    }

    public DsaCheckTask(String sceneCode, DsaCheckType checkType) {
        this();
        this.sceneCode = sceneCode;
        this.checkType = checkType;
    }

    /**
     * 计算进度.
     */
    public void calculateProgress() {
        if (totalCount > 0) {
            this.progress = (int) (processedCount * 100 / totalCount);
        }
    }

    /**
     * 是否已完成.
     *
     * @return true表示已完成
     */
    public boolean isCompleted() {
        return status == DsaCheckStatus.COMPLETED
                || status == DsaCheckStatus.FAILED
                || status == DsaCheckStatus.CANCELLED;
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

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getPkField() {
        return pkField;
    }

    public void setPkField(String pkField) {
        this.pkField = pkField;
    }

    public DsaCheckType getCheckType() {
        return checkType;
    }

    public void setCheckType(DsaCheckType checkType) {
        this.checkType = checkType;
    }

    public DsaCheckStatus getStatus() {
        return status;
    }

    public void setStatus(DsaCheckStatus status) {
        this.status = status;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public boolean isCompareFields() {
        return compareFields;
    }

    public void setCompareFields(boolean compareFields) {
        this.compareFields = compareFields;
    }

    public String getCompareFieldList() {
        return compareFieldList;
    }

    public void setCompareFieldList(String compareFieldList) {
        this.compareFieldList = compareFieldList;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
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

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public long getProcessedCount() {
        return processedCount;
    }

    public void setProcessedCount(long processedCount) {
        this.processedCount = processedCount;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public DsaCheckReport getReport() {
        return report;
    }

    public void setReport(DsaCheckReport report) {
        this.report = report;
    }
}
