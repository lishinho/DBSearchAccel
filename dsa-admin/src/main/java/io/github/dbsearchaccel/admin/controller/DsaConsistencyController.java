package io.github.dbsearchaccel.admin.controller;

import io.github.dbsearchaccel.admin.model.DsaCheckTaskInfo;
import io.github.dbsearchaccel.admin.model.DsaResult;
import io.github.dbsearchaccel.admin.service.DsaConsistencyManageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * DSA一致性校验控制器
 * 提供校验任务的触发和结果查询接口
 *
 * @author DBSearchAccel Team
 */
@RestController
@RequestMapping("/api/consistency")
public class DsaConsistencyController {

    private final DsaConsistencyManageService consistencyManageService;

    public DsaConsistencyController(DsaConsistencyManageService consistencyManageService) {
        this.consistencyManageService = consistencyManageService;
    }

    @PostMapping("/check")
    public DsaResult<String> triggerCheck(@RequestBody Map<String, String> params) {
        String sceneCode = params.get("sceneCode");
        String checkType = params.getOrDefault("checkType", "PRIMARY_KEY");
        return DsaResult.success(consistencyManageService.triggerCheck(sceneCode, checkType));
    }

    @GetMapping("/tasks")
    public DsaResult<List<DsaCheckTaskInfo>> listCheckTasks(
            @RequestParam(required = false) String sceneCode) {
        return DsaResult.success(consistencyManageService.listCheckTasks(sceneCode));
    }

    @GetMapping("/task/{taskId}")
    public DsaResult<DsaCheckTaskInfo> getCheckTask(@PathVariable String taskId) {
        DsaCheckTaskInfo task = consistencyManageService.getCheckTask(taskId);
        return task != null ? DsaResult.success(task) : DsaResult.error(404, "Task not found");
    }

    @GetMapping("/report/{taskId}")
    public DsaResult<String> getCheckReport(@PathVariable String taskId) {
        String report = consistencyManageService.getCheckReport(taskId);
        return report != null ? DsaResult.success(report) : DsaResult.error(404, "Report not found");
    }

    @GetMapping("/export/{taskId}")
    public ResponseEntity<byte[]> exportReport(
            @PathVariable String taskId,
            @RequestParam(defaultValue = "JSON") String format) {
        byte[] content = consistencyManageService.exportReport(taskId, format);
        if (content == null || content.length == 0) {
            return ResponseEntity.notFound().build();
        }

        String filename = "consistency-report-" + taskId + "." + format.toLowerCase();
        String contentType = "JSON".equalsIgnoreCase(format) ? MediaType.APPLICATION_JSON_VALUE
                : "CSV".equalsIgnoreCase(format) ? "text/csv" : MediaType.TEXT_HTML_VALUE;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(content);
    }
}
