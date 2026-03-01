package io.github.dbsearchaccel.admin.model;

import lombok.Data;
import java.io.Serializable;

/**
 * DSA一致性校验任务信息
 * 用于展示校验任务的状态和结果
 *
 * @author DBSearchAccel Team
 */
@Data
public class DsaCheckTaskInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String taskId;

    private String sceneCode;

    private String checkType;

    private String status;

    private Long totalCount;

    private Long mismatchCount;

    private Long missingInEsCount;

    private Long missingInDbCount;

    private String reportPath;

    private Long startTime;

    private Long endTime;
}
