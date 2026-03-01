package io.github.dbsearchaccel.core.bitmap;

import org.roaringbitmap.RoaringBitmap;

import java.util.List;
import java.util.Map;

/**
 * 位图索引服务接口.
 * <p>
 * 基于Roaring Bitmap实现高性能倒排索引，支持O(1)复杂度的多条件交并集运算.
 * 适用于亿级文档的高效检索场景.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaBitmapIndexService {

    /**
     * 构建字段值到位图的映射.
     *
     * @param sceneCode   场景编码
     * @param fieldName   字段名
     * @param fieldValue  字段值
     * @param docIds      文档ID列表
     * @return 构建结果
     */
    boolean buildIndex(String sceneCode, String fieldName, Object fieldValue, List<Long> docIds);

    /**
     * 批量构建索引.
     *
     * @param sceneCode  场景编码
     * @param fieldName  字段名
     * @param valueToIds 字段值到文档ID列表的映射
     * @return 构建成功的数量
     */
    int buildIndexBatch(String sceneCode, String fieldName, Map<Object, List<Long>> valueToIds);

    /**
     * 查询字段值对应的位图.
     *
     * @param sceneCode   场景编码
     * @param fieldName   字段名
     * @param fieldValue  字段值
     * @return RoaringBitmap 位图，不存在则返回空位图
     */
    RoaringBitmap queryBitmap(String sceneCode, String fieldName, Object fieldValue);

    /**
     * 多条件交集查询.
     * 返回同时满足所有条件的文档ID位图.
     *
     * @param sceneCode  场景编码
     * @param conditions 条件列表（fieldName -> fieldValue）
     * @return 交集结果位图
     */
    RoaringBitmap intersect(String sceneCode, Map<String, Object> conditions);

    /**
     * 多条件并集查询.
     * 返回满足任意条件的文档ID位图.
     *
     * @param sceneCode  场景编码
     * @param conditions 条件列表（fieldName -> fieldValue）
     * @return 并集结果位图
     */
    RoaringBitmap union(String sceneCode, Map<String, Object> conditions);

    /**
     * 多条件差集查询.
     * 返回在第一个条件中但不在其他条件中的文档ID位图.
     *
     * @param sceneCode  场景编码
     * @param include    包含的条件
     * @param exclude    排除的条件
     * @return 差集结果位图
     */
    RoaringBitmap difference(String sceneCode, Map<String, Object> include, Map<String, Object> exclude);

    /**
     * 位图转文档ID列表.
     *
     * @param bitmap 位图
     * @param offset 偏移量
     * @param limit  数量限制
     * @return 文档ID列表
     */
    List<Long> bitmapToIds(RoaringBitmap bitmap, int offset, int limit);

    /**
     * 获取位图基数（元素数量）.
     *
     * @param bitmap 位图
     * @return 基数
     */
    long getCardinality(RoaringBitmap bitmap);

    /**
     * 添加文档ID到现有位图.
     *
     * @param sceneCode   场景编码
     * @param fieldName   字段名
     * @param fieldValue  字段值
     * @param docIds      待添加的文档ID列表
     * @return 添加结果
     */
    boolean addToIndex(String sceneCode, String fieldName, Object fieldValue, List<Long> docIds);

    /**
     * 从现有位图移除文档ID.
     *
     * @param sceneCode   场景编码
     * @param fieldName   字段名
     * @param fieldValue  字段值
     * @param docIds      待移除的文档ID列表
     * @return 移除结果
     */
    boolean removeFromIndex(String sceneCode, String fieldName, Object fieldValue, List<Long> docIds);

    /**
     * 删除指定字段值的索引.
     *
     * @param sceneCode   场景编码
     * @param fieldName   字段名
     * @param fieldValue  字段值
     * @return 删除结果
     */
    boolean deleteIndex(String sceneCode, String fieldName, Object fieldValue);

    /**
     * 清空场景的所有索引.
     *
     * @param sceneCode 场景编码
     * @return 清空结果
     */
    boolean clearScene(String sceneCode);

    /**
     * 获取场景的索引统计信息.
     *
     * @param sceneCode 场景编码
     * @return 统计信息
     */
    DsaBitmapStats getStats(String sceneCode);
}
