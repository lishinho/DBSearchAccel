package io.github.dbsearchaccel.admin.service;

import io.github.dbsearchaccel.admin.model.DsaDegradeStatus;
import java.util.List;

/**
 * DSA降级管理服务接口
 *
 * @author DBSearchAccel Team
 */
public interface DsaDegradeManageService {

    List<DsaDegradeStatus> listAllDegradeStatus();

    DsaDegradeStatus getDegradeStatus(String sceneCode);

    boolean manualDegrade(String sceneCode, String level, String reason);

    boolean manualRecover(String sceneCode);

    DsaDegradeStatus getGlobalDegradeStatus();

    boolean setGlobalDegradeStatus(boolean isDegrading, String reason);
}
