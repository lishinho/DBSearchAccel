package io.github.dbsearchaccel.admin.service;

import io.github.dbsearchaccel.admin.model.DsaDegradeStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * DSA降级管理服务实现
 *
 * @author DBSearchAccel Team
 */
@Service
public class DsaDegradeManageServiceImpl implements DsaDegradeManageService {

    private final Map<String, DsaDegradeStatus> degradeStatusMap = new ConcurrentHashMap<>();

    private final DsaDegradeStatus globalDegradeStatus = new DsaDegradeStatus();

    public DsaDegradeManageServiceImpl() {
        globalDegradeStatus.setSceneCode("GLOBAL");
        globalDegradeStatus.setDegradeLevel("NONE");
        globalDegradeStatus.setIsDegrading(false);
        globalDegradeStatus.setDegradeCount(0);
    }

    @Override
    public List<DsaDegradeStatus> listAllDegradeStatus() {
        return degradeStatusMap.values().stream().collect(Collectors.toList());
    }

    @Override
    public DsaDegradeStatus getDegradeStatus(String sceneCode) {
        return degradeStatusMap.computeIfAbsent(sceneCode, code -> {
            DsaDegradeStatus status = new DsaDegradeStatus();
            status.setSceneCode(code);
            status.setDegradeLevel("NONE");
            status.setIsDegrading(false);
            status.setDegradeCount(0);
            return status;
        });
    }

    @Override
    public boolean manualDegrade(String sceneCode, String level, String reason) {
        DsaDegradeStatus status = getDegradeStatus(sceneCode);
        status.setDegradeLevel(level);
        status.setIsDegrading(true);
        status.setDegradeReason(reason);
        status.setDegradeTime(System.currentTimeMillis());
        status.setDegradeCount(status.getDegradeCount() != null ? status.getDegradeCount() + 1 : 1);
        return true;
    }

    @Override
    public boolean manualRecover(String sceneCode) {
        DsaDegradeStatus status = degradeStatusMap.get(sceneCode);
        if (status != null && status.getIsDegrading()) {
            status.setIsDegrading(false);
            status.setDegradeLevel("NONE");
            status.setDegradeReason(null);
            status.setRecoverTime(System.currentTimeMillis());
            return true;
        }
        return false;
    }

    @Override
    public DsaDegradeStatus getGlobalDegradeStatus() {
        return globalDegradeStatus;
    }

    @Override
    public boolean setGlobalDegradeStatus(boolean isDegrading, String reason) {
        globalDegradeStatus.setIsDegrading(isDegrading);
        if (isDegrading) {
            globalDegradeStatus.setDegradeLevel("GLOBAL");
            globalDegradeStatus.setDegradeReason(reason);
            globalDegradeStatus.setDegradeTime(System.currentTimeMillis());
            globalDegradeStatus.setDegradeCount(globalDegradeStatus.getDegradeCount() != null ? globalDegradeStatus.getDegradeCount() + 1 : 1);
        } else {
            globalDegradeStatus.setDegradeLevel("NONE");
            globalDegradeStatus.setDegradeReason(null);
            globalDegradeStatus.setRecoverTime(System.currentTimeMillis());
        }
        return true;
    }
}
