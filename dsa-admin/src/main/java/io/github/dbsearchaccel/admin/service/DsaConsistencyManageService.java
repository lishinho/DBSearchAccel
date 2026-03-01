package io.github.dbsearchaccel.admin.service;

import io.github.dbsearchaccel.admin.model.DsaCheckTaskInfo;
import java.util.List;

/**
 * DSA一致性校验管理服务接口
 *
 * @author DBSearchAccel Team
 */
public interface DsaConsistencyManageService {

    String triggerCheck(String sceneCode, String checkType);

    List<DsaCheckTaskInfo> listCheckTasks(String sceneCode);

    DsaCheckTaskInfo getCheckTask(String taskId);

    String getCheckReport(String taskId);

    byte[] exportReport(String taskId, String format);
}
