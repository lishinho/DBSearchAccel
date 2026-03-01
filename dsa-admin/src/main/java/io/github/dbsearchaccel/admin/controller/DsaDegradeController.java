package io.github.dbsearchaccel.admin.controller;

import io.github.dbsearchaccel.admin.model.DsaDegradeStatus;
import io.github.dbsearchaccel.admin.model.DsaResult;
import io.github.dbsearchaccel.admin.service.DsaDegradeManageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * DSA降级管理控制器
 * 提供降级状态的查看和控制接口
 *
 * @author DBSearchAccel Team
 */
@RestController
@RequestMapping("/api/degrade")
public class DsaDegradeController {

    private final DsaDegradeManageService degradeManageService;

    public DsaDegradeController(DsaDegradeManageService degradeManageService) {
        this.degradeManageService = degradeManageService;
    }

    @GetMapping("/list")
    public DsaResult<List<DsaDegradeStatus>> listAllDegradeStatus() {
        return DsaResult.success(degradeManageService.listAllDegradeStatus());
    }

    @GetMapping("/{sceneCode}")
    public DsaResult<DsaDegradeStatus> getDegradeStatus(@PathVariable String sceneCode) {
        return DsaResult.success(degradeManageService.getDegradeStatus(sceneCode));
    }

    @PostMapping("/{sceneCode}/degrade")
    public DsaResult<Boolean> manualDegrade(
            @PathVariable String sceneCode,
            @RequestBody Map<String, String> params) {
        String level = params.getOrDefault("level", "SCENE");
        String reason = params.getOrDefault("reason", "Manual degrade");
        return DsaResult.success(degradeManageService.manualDegrade(sceneCode, level, reason));
    }

    @PostMapping("/{sceneCode}/recover")
    public DsaResult<Boolean> manualRecover(@PathVariable String sceneCode) {
        return DsaResult.success(degradeManageService.manualRecover(sceneCode));
    }

    @GetMapping("/global")
    public DsaResult<DsaDegradeStatus> getGlobalDegradeStatus() {
        return DsaResult.success(degradeManageService.getGlobalDegradeStatus());
    }

    @PostMapping("/global")
    public DsaResult<Boolean> setGlobalDegradeStatus(@RequestBody Map<String, Object> params) {
        Boolean isDegrading = (Boolean) params.get("isDegrading");
        String reason = (String) params.getOrDefault("reason", "Manual operation");
        return DsaResult.success(degradeManageService.setGlobalDegradeStatus(isDegrading, reason));
    }
}
