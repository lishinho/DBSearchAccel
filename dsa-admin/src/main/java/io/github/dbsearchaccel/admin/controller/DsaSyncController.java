package io.github.dbsearchaccel.admin.controller;

import io.github.dbsearchaccel.admin.model.DsaResult;
import io.github.dbsearchaccel.admin.model.DsaSyncTaskInfo;
import io.github.dbsearchaccel.admin.service.DsaSyncManageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * DSA同步管理控制器
 * 提供同步任务的控制和状态查询接口
 *
 * @author DBSearchAccel Team
 */
@RestController
@RequestMapping("/api/sync")
public class DsaSyncController {

    private final DsaSyncManageService syncManageService;

    public DsaSyncController(DsaSyncManageService syncManageService) {
        this.syncManageService = syncManageService;
    }

    @PostMapping("/full/{sceneCode}")
    public DsaResult<String> triggerFullSync(@PathVariable String sceneCode) {
        return DsaResult.success(syncManageService.triggerFullSync(sceneCode));
    }

    @GetMapping("/increment/{sceneCode}")
    public DsaResult<DsaSyncTaskInfo> getIncrementSyncStatus(@PathVariable String sceneCode) {
        return DsaResult.success(syncManageService.getIncrementSyncStatus(sceneCode));
    }

    @PostMapping("/increment/{sceneCode}/pause")
    public DsaResult<Boolean> pauseIncrementSync(@PathVariable String sceneCode) {
        return DsaResult.success(syncManageService.pauseIncrementSync(sceneCode));
    }

    @PostMapping("/increment/{sceneCode}/resume")
    public DsaResult<Boolean> resumeIncrementSync(@PathVariable String sceneCode) {
        return DsaResult.success(syncManageService.resumeIncrementSync(sceneCode));
    }

    @GetMapping("/tasks")
    public DsaResult<List<DsaSyncTaskInfo>> listSyncTasks(
            @RequestParam(required = false) String sceneCode) {
        return DsaResult.success(syncManageService.listSyncTasks(sceneCode));
    }

    @GetMapping("/task/{taskId}")
    public DsaResult<DsaSyncTaskInfo> getSyncTask(@PathVariable String taskId) {
        DsaSyncTaskInfo task = syncManageService.getSyncTask(taskId);
        return task != null ? DsaResult.success(task) : DsaResult.error(404, "Task not found");
    }

    @PostMapping("/task/{taskId}/cancel")
    public DsaResult<Boolean> cancelSyncTask(@PathVariable String taskId) {
        return DsaResult.success(syncManageService.cancelSyncTask(taskId));
    }
}
