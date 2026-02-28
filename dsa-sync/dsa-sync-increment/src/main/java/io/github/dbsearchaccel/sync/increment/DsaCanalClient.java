package io.github.dbsearchaccel.sync.increment;

import io.github.dbsearchaccel.common.model.config.DsaSceneConfig;

import java.util.List;
import java.util.Map;

/**
 * Canal客户端接口.
 * <p>
 * 封装Canal客户端的连接、订阅、消息处理等操作.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaCanalClient {

    /**
     * 连接Canal Server.
     *
     * @return true表示连接成功
     */
    boolean connect();

    /**
     * 断开连接.
     */
    void disconnect();

    /**
     * 判断是否已连接.
     *
     * @return true表示已连接
     */
    boolean isConnected();

    /**
     * 订阅数据库表.
     *
     * @param config 场景配置
     * @return true表示订阅成功
     */
    boolean subscribe(DsaSceneConfig config);

    /**
     * 取消订阅.
     *
     * @param sceneCode 场景编码
     */
    void unsubscribe(String sceneCode);

    /**
     * 获取变更数据.
     *
     * @param batchSize 批次大小
     * @return 变更数据列表
     */
    List<DsaCanalEntry> fetch(int batchSize);

    /**
     * 确认消费.
     *
     * @param batchId 批次ID
     */
    void ack(long batchId);

    /**
     * 回滚消费.
     *
     * @param batchId 批次ID
     */
    void rollback(long batchId);

    /**
     * 获取当前Binlog位置.
     *
     * @return Binlog位置
     */
    String getPosition();
}
