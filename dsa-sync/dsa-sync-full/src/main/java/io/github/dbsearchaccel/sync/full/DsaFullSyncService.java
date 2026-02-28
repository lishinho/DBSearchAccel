package io.github.dbsearchaccel.sync.full;

import java.util.Map;

/**
 * 全量同步服务接口.
 * <p>
 * 提供数据库到Elasticsearch的全量数据同步能力.
 * 支持分批同步、进度监控、断点续传等特性.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaFullSyncService {

    /**
     * 执行全量同步.
     * <p>
     * 从数据库读取全量数据并同步到ES索引.
     * </p>
     *
     * @param sceneCode 场景编码
     * @return 同步结果
     */
    DsaSyncResult sync(String sceneCode);

    /**
     * 执行全量同步（带参数）.
     *
     * @param sceneCode 场景编码
     * @param params    同步参数
     * @return 同步结果
     */
    DsaSyncResult sync(String sceneCode, Map<String, Object> params);

    /**
     * 异步执行全量同步.
     *
     * @param sceneCode 场景编码
     * @return 同步任务ID
     */
    String syncAsync(String sceneCode);

    /**
     * 获取同步进度.
     *
     * @param taskId 同步任务ID
     * @return 同步进度
     */
    DsaSyncProgress getProgress(String taskId);

    /**
     * 取消同步任务.
     *
     * @param taskId 同步任务ID
     * @return true表示取消成功
     */
    boolean cancel(String taskId);

    /**
     * 暂停同步任务.
     *
     * @param taskId 同步任务ID
     * @return true表示暂停成功
     */
    boolean pause(String taskId);

    /**
     * 恢复同步任务.
     *
     * @param taskId 同步任务ID
     * @return true表示恢复成功
     */
    boolean resume(String taskId);
}
