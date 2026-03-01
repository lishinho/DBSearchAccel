package io.github.dbsearchaccel.consistency;

import java.util.List;
import java.util.Map;

/**
 * 字段校验器接口.
 * <p>
 * 负责对比ES和数据库的字段值，找出不一致的数据.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaFieldChecker {

    /**
     * 获取ES中的字段值.
     *
     * @param indexName   索引名称
     * @param pkField     主键字段名
     * @param primaryKey  主键值
     * @param fieldNames  字段名列表
     * @return 字段值映射
     */
    Map<String, Object> getEsFieldValues(String indexName, String pkField, Object primaryKey, List<String> fieldNames);

    /**
     * 获取数据库中的字段值.
     *
     * @param tableName   表名
     * @param pkField     主键字段名
     * @param primaryKey  主键值
     * @param fieldNames  字段名列表
     * @return 字段值映射
     */
    Map<String, Object> getDbFieldValues(String tableName, String pkField, Object primaryKey, List<String> fieldNames);

    /**
     * 批量获取ES字段值.
     *
     * @param indexName   索引名称
     * @param pkField     主键字段名
     * @param primaryKeys 主键列表
     * @param fieldNames  字段名列表
     * @return 主键到字段值的映射
     */
    Map<Object, Map<String, Object>> getEsFieldValuesBatch(String indexName, String pkField,
            List<Object> primaryKeys, List<String> fieldNames);

    /**
     * 批量获取数据库字段值.
     *
     * @param tableName   表名
     * @param pkField     主键字段名
     * @param primaryKeys 主键列表
     * @param fieldNames  字段名列表
     * @return 主键到字段值的映射
     */
    Map<Object, Map<String, Object>> getDbFieldValuesBatch(String tableName, String pkField,
            List<Object> primaryKeys, List<String> fieldNames);

    /**
     * 比较字段值.
     *
     * @param esValues   ES字段值
     * @param dbValues   数据库字段值
     * @param fieldNames 字段名列表
     * @return 差异详情
     */
    Map<String, Map<String, Object>> compareFieldValues(Map<String, Object> esValues,
            Map<String, Object> dbValues, List<String> fieldNames);

    /**
     * 执行字段校验.
     *
     * @param task       校验任务
     * @param primaryKeys 待校验的主键列表
     * @return 校验结果
     */
    DsaCheckResult check(DsaCheckTask task, List<Object> primaryKeys);
}
