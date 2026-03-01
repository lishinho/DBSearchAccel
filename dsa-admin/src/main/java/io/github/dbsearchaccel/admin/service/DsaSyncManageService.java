package io.github.dbsearchaccel.admin.service;

import io.github.dbsearchaccel.admin.model.DsaSyncTaskInfo;
import java.util.List;

/**
 * DSA同步管理服务接口
 *
 * @author DBSearchAccel Team
 */
public interface DsaSyncManageService {

    String triggerFullSync(String sceneCode);

    DsaSyncTaskInfo getIncrementSyncStatus(String sceneCode);

    boolean pauseIncrementSync(String sceneCode);

    boolean resumeIncrementSync(String sceneCode);

    List<DsaSyncTaskInfo> listSyncTasks(String sceneCode);

    DsaSyncTaskInfo getSyncTask(String taskId);

    boolean cancelSyncTask(String taskId);
}
