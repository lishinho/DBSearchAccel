package io.github.dbsearchaccel.admin.model;

import lombok.Data;
import java.io.Serializable;

/**
 * DSA同步任务信息
 * 用于展示同步任务的状态和进度
 *
 * @author DBSearchAccel Team
 */
@Data
public class DsaSyncTaskInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String taskId;

    private String sceneCode;

    private String syncType;

    private String status;

    private Long totalCount;

    private Long syncedCount;

    private Double progress;

    private String errorMessage;

    private Long startTime;

    private Long endTime;
}
