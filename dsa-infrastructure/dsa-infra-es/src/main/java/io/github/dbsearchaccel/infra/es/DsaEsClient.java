package io.github.dbsearchaccel.infra.es;

import io.github.dbsearchaccel.common.model.dsl.DsaDsl;

import java.util.List;
import java.util.Map;

/**
 * Elasticsearch客户端接口.
 * <p>
 * 封装Elasticsearch的常用操作，包括查询、索引、删除等.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaEsClient {

    /**
     * 查询主键ID列表.
     * <p>
     * 根据DSL条件查询ES，仅返回主键ID列表
     * </p>
     *
     * @param dsl DSL封装实体
     * @return 主键ID列表
     */
    List<String> queryIds(DsaDsl dsl);

    /**
     * 查询完整数据列表.
     *
     * @param dsl DSL封装实体
     * @return 数据列表（Map形式）
     */
    List<Map<String, Object>> query(DsaDsl dsl);

    /**
     * 单条索引.
     *
     * @param index 索引名称
     * @param id    文档ID
     * @param doc   文档内容
     */
    void index(String index, String id, Map<String, Object> doc);

    /**
     * 批量索引.
     *
     * @param index   索引名称
     * @param docs    文档列表
     * @param idField ID字段名
     */
    void bulkIndex(String index, List<Map<String, Object>> docs, String idField);

    /**
     * 单条删除.
     *
     * @param index 索引名称
     * @param id    文档ID
     */
    void delete(String index, String id);

    /**
     * 批量删除.
     *
     * @param index 索引名称
     * @param ids   文档ID列表
     */
    void bulkDelete(String index, List<String> ids);

    /**
     * 判断文档是否存在.
     *
     * @param index 索引名称
     * @param id    文档ID
     * @return true表示存在
     */
    boolean exists(String index, String id);

    /**
     * 统计文档数量.
     *
     * @param dsl DSL封装实体
     * @return 文档数量
     */
    long count(DsaDsl dsl);

    /**
     * 创建索引.
     *
     * @param index   索引名称
     * @param mapping 映射配置
     */
    void createIndex(String index, Map<String, Object> mapping);

    /**
     * 删除索引.
     *
     * @param index 索引名称
     */
    void deleteIndex(String index);

    /**
     * 判断索引是否存在.
     *
     * @param index 索引名称
     * @return true表示存在
     */
    boolean indexExists(String index);
}
