package io.github.dbsearchaccel.admin.service;

import io.github.dbsearchaccel.admin.model.DsaSyncTaskInfo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * DSA同步管理服务实现
 *
 * @author DBSearchAccel Team
 */
@Service
public class DsaSyncManageServiceImpl implements DsaSyncManageService {

    private final Map<String, DsaSyncTaskInfo> taskStore = new ConcurrentHashMap<>();

    private final Map<String, DsaSyncTaskInfo> incrementSyncStatus = new ConcurrentHashMap<>();

    @Override
    public String triggerFullSync(String sceneCode) {
        String taskId = UUID.randomUUID().toString();
        DsaSyncTaskInfo task = new DsaSyncTaskInfo();
        task.setTaskId(taskId);
        task.setSceneCode(sceneCode);
        task.setSyncType("FULL");
        task.setStatus("RUNNING");
        task.setTotalCount(0L);
        task.setSyncedCount(0L);
        task.setProgress(0.0);
        task.setStartTime(System.currentTimeMillis());
        taskStore.put(taskId, task);
        return taskId;
    }

    @Override
    public DsaSyncTaskInfo getIncrementSyncStatus(String sceneCode) {
        return incrementSyncStatus.computeIfAbsent(sceneCode, code -> {
            DsaSyncTaskInfo info = new DsaSyncTaskInfo();
            info.setSceneCode(code);
            info.setSyncType("INCREMENT");
            info.setStatus("RUNNING");
            info.setTotalCount(0L);
            info.setSyncedCount(0L);
            info.setProgress(0.0);
            return info;
        });
    }

    @Override
    public boolean pauseIncrementSync(String sceneCode) {
        DsaSyncTaskInfo info = incrementSyncStatus.get(sceneCode);
        if (info != null) {
            info.setStatus("PAUSED");
            return true;
        }
        return false;
    }

    @Override
    public boolean resumeIncrementSync(String sceneCode) {
        DsaSyncTaskInfo info = incrementSyncStatus.get(sceneCode);
        if (info != null) {
            info.setStatus("RUNNING");
            return true;
        }
        return false;
    }

    @Override
    public List<DsaSyncTaskInfo> listSyncTasks(String sceneCode) {
        return taskStore.values().stream()
                .filter(t -> sceneCode == null || sceneCode.isEmpty() || sceneCode.equals(t.getSceneCode()))
                .sorted((a, b) -> Long.compare(b.getStartTime() != null ? b.getStartTime() : 0, a.getStartTime() != null ? a.getStartTime() : 0))
                .collect(Collectors.toList());
    }

    @Override
    public DsaSyncTaskInfo getSyncTask(String taskId) {
        return taskStore.get(taskId);
    }

    @Override
    public boolean cancelSyncTask(String taskId) {
        DsaSyncTaskInfo task = taskStore.get(taskId);
        if (task != null && "RUNNING".equals(task.getStatus())) {
            task.setStatus("CANCELLED");
            task.setEndTime(System.currentTimeMillis());
            return true;
        }
        return false;
    }
}
