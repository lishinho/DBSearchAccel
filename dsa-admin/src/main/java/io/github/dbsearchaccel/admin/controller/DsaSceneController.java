package io.github.dbsearchaccel.admin.controller;

import io.github.dbsearchaccel.admin.model.DsaResult;
import io.github.dbsearchaccel.admin.model.DsaSceneConfig;
import io.github.dbsearchaccel.admin.service.DsaSceneService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * DSA场景管理控制器
 * 提供场景配置的CRUD操作接口
 *
 * @author DBSearchAccel Team
 */
@RestController
@RequestMapping("/api/scene")
public class DsaSceneController {

    private final DsaSceneService sceneService;

    public DsaSceneController(DsaSceneService sceneService) {
        this.sceneService = sceneService;
    }

    @PostMapping
    public DsaResult<DsaSceneConfig> create(@RequestBody DsaSceneConfig config) {
        try {
            return DsaResult.success(sceneService.create(config));
        } catch (Exception e) {
            return DsaResult.error(e.getMessage());
        }
    }

    @PutMapping
    public DsaResult<DsaSceneConfig> update(@RequestBody DsaSceneConfig config) {
        try {
            return DsaResult.success(sceneService.update(config));
        } catch (Exception e) {
            return DsaResult.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public DsaResult<Boolean> delete(@PathVariable Long id) {
        return DsaResult.success(sceneService.delete(id));
    }

    @GetMapping("/{id}")
    public DsaResult<DsaSceneConfig> getById(@PathVariable Long id) {
        DsaSceneConfig config = sceneService.getById(id);
        return config != null ? DsaResult.success(config) : DsaResult.error(404, "Not found");
    }

    @GetMapping("/code/{sceneCode}")
    public DsaResult<DsaSceneConfig> getBySceneCode(@PathVariable String sceneCode) {
        DsaSceneConfig config = sceneService.getBySceneCode(sceneCode);
        return config != null ? DsaResult.success(config) : DsaResult.error(404, "Not found");
    }

    @GetMapping("/list")
    public DsaResult<List<DsaSceneConfig>> listAll() {
        return DsaResult.success(sceneService.listAll());
    }

    @GetMapping("/list/{status}")
    public DsaResult<List<DsaSceneConfig>> listByStatus(@PathVariable String status) {
        return DsaResult.success(sceneService.listByStatus(status));
    }

    @PostMapping("/{id}/enable")
    public DsaResult<Boolean> enable(@PathVariable Long id) {
        return DsaResult.success(sceneService.enable(id));
    }

    @PostMapping("/{id}/disable")
    public DsaResult<Boolean> disable(@PathVariable Long id) {
        return DsaResult.success(sceneService.disable(id));
    }
}
