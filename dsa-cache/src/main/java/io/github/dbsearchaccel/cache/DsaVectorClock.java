package io.github.dbsearchaccel.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 向量时钟接口.
 * <p>
 * 用于解决分布式环境下的数据版本冲突.
 * 每个节点维护自己的时钟计数器，通过比较向量时钟判断数据新旧.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaVectorClock {

    /**
     * 生成新版本.
     * 当前节点计数器+1.
     *
     * @param sceneCode 场景编码
     * @param pkValue   主键值
     * @param nodeId    当前节点ID
     * @return 新的向量时钟
     */
    DsaVectorClockValue increment(String sceneCode, String pkValue, String nodeId);

    /**
     * 合并两个向量时钟.
     * 取各节点计数器的最大值.
     *
     * @param v1 向量时钟1
     * @param v2 向量时钟2
     * @return 合并后的向量时钟
     */
    DsaVectorClockValue merge(DsaVectorClockValue v1, DsaVectorClockValue v2);

    /**
     * 比较两个向量时钟.
     *
     * @param v1 向量时钟1
     * @param v2 向量时钟2
     * @return 比较结果：BEFORE/AFTER/CONCURRENT/EQUAL
     */
    DsaClockCompareResult compare(DsaVectorClockValue v1, DsaVectorClockValue v2);

    /**
     * 获取数据的向量时钟.
     *
     * @param sceneCode 场景编码
     * @param pkValue   主键值
     * @return 向量时钟值
     */
    DsaVectorClockValue get(String sceneCode, String pkValue);

    /**
     * 设置数据的向量时钟.
     *
     * @param sceneCode 场景编码
     * @param pkValue   主键值
     * @param value     向量时钟值
     */
    void set(String sceneCode, String pkValue, DsaVectorClockValue value);

    /**
     * 解决冲突.
     * 当向量时钟比较结果为CONCURRENT时，选择较新的版本.
     *
     * @param v1 向量时钟1
     * @param v2 向量时钟2
     * @return 较新的向量时钟
     */
    DsaVectorClockValue resolveConflict(DsaVectorClockValue v1, DsaVectorClockValue v2);
}
