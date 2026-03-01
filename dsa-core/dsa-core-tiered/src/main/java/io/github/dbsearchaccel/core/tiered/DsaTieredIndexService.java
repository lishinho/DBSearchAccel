package io.github.dbsearchaccel.core.tiered;

import org.roaringbitmap.RoaringBitmap;

import java.util.List;
import java.util.Map;

/**
 * 分层索引服务接口.
 * <p>
 * 实现内存/SSD/ES三级存储架构，自动管理数据晋升与降级.
 * 查询优先级：L1内存 > L2 SSD > L3 ES.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaTieredIndexService {

    /**
     * 分层查询位图.
     * 优先级：L1内存 > L2 SSD > L3 ES.
     *
     * @param sceneCode  场景编码
     * @param fieldName  字段名
     * @param fieldValue 字段值
     * @return 位图结果（含来源层级信息）
     */
    DsaTieredResult queryBitmap(String sceneCode, String fieldName, Object fieldValue);

    /**
     * 批量查询位图.
     *
     * @param sceneCode  场景编码
     * @param conditions 条件列表
     * @return 位图结果列表
     */
    List<DsaTieredResult> queryBitmapBatch(String sceneCode, Map<String, Object> conditions);

    /**
     * 多条件交集查询.
     *
     * @param sceneCode  场景编码
     * @param conditions 条件列表
     * @return 交集结果
     */
    DsaTieredResult intersect(String sceneCode, Map<String, Object> conditions);

    /**
     * 多条件并集查询.
     *
     * @param sceneCode  场景编码
     * @param conditions 条件列表
     * @return 并集结果
     */
    DsaTieredResult union(String sceneCode, Map<String, Object> conditions);

    /**
     * 写入位图到指定层级.
     *
     * @param sceneCode  场景编码
     * @param fieldName  字段名
     * @param fieldValue 字段值
     * @param bitmap     位图数据
     * @param level      目标层级（HOT/WARM/COLD）
     */
    void putBitmap(String sceneCode, String fieldName, Object fieldValue, RoaringBitmap bitmap, DsaStorageLevel level);

    /**
     * 晋升数据到更高层级.
     * 当L2/L3数据访问频率达到阈值时自动晋升.
     *
     * @param sceneCode 场景编码
     * @param key       数据键
     * @param fromLevel 源层级
     * @param toLevel   目标层级
     * @return 晋升结果
     */
    boolean promote(String sceneCode, String key, DsaStorageLevel fromLevel, DsaStorageLevel toLevel);

    /**
     * 降级数据到更低层级.
     * 当L1/L2数据访问频率低于阈值时自动降级.
     *
     * @param sceneCode 场景编码
     * @param key       数据键
     * @param fromLevel 源层级
     * @param toLevel   目标层级
     * @return 降级结果
     */
    boolean demote(String sceneCode, String key, DsaStorageLevel fromLevel, DsaStorageLevel toLevel);

    /**
     * 预热热点数据.
     * 基于查询模式预测，提前加载热点数据到内存.
     *
     * @param sceneCode 场景编码
     * @param patterns  查询模式列表
     * @return 预热数量
     */
    int warmup(String sceneCode, List<DsaQueryPattern> patterns);

    /**
     * 清空指定层级的缓存.
     *
     * @param sceneCode 场景编码
     * @param level     存储层级
     */
    void clear(String sceneCode, DsaStorageLevel level);

    /**
     * 清空场景的所有数据.
     *
     * @param sceneCode 场景编码
     */
    void clearAll(String sceneCode);

    /**
     * 获取分层统计信息.
     *
     * @param sceneCode 场景编码
     * @return 统计信息
     */
    DsaTieredStats getStats(String sceneCode);

    /**
     * 获取各层级缓存大小.
     *
     * @param sceneCode 场景编码
     * @return 层级到缓存大小的映射
     */
    Map<DsaStorageLevel, Long> getCacheSizes(String sceneCode);
}
