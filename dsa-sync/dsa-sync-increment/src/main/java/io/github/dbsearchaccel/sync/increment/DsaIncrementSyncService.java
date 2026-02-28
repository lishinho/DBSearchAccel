package io.github.dbsearchaccel.sync.increment;

import io.github.dbsearchaccel.common.model.config.DsaSceneConfig;

import java.util.List;
import java.util.Map;

/**
 * 增量同步服务接口.
 * <p>
 * 提供基于Canal的增量数据同步能力，实时监听数据库变更并同步到ES.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaIncrementSyncService {

    /**
     * 启动增量同步.
     *
     * @param sceneCode 场景编码
     * @return true表示启动成功
     */
    boolean start(String sceneCode);

    /**
     * 停止增量同步.
     *
     * @param sceneCode 场景编码
     * @return true表示停止成功
     */
    boolean stop(String sceneCode);

    /**
     * 判断是否正在运行.
     *
     * @param sceneCode 场景编码
     * @return true表示正在运行
     */
    boolean isRunning(String sceneCode);

    /**
     * 获取同步状态.
     *
     * @param sceneCode 场景编码
     * @return 同步状态
     */
    DsaIncrementSyncStatus getStatus(String sceneCode);

    /**
     * 处理数据变更事件.
     *
     * @param sceneCode 场景编码
     * @param eventType 事件类型（INSERT/UPDATE/DELETE）
     * @param data      变更数据
     * @return true表示处理成功
     */
    boolean handleChange(String sceneCode, String eventType, List<Map<String, Object>> data);

    /**
     * 启动所有场景的增量同步.
     *
     * @return 成功启动的场景数量
     */
    int startAll();

    /**
     * 停止所有场景的增量同步.
     */
    void stopAll();
}
