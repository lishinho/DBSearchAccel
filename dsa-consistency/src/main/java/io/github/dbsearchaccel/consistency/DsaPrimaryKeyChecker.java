package io.github.dbsearchaccel.consistency;

import java.util.List;
import java.util.Set;

/**
 * 主键校验器接口.
 * <p>
 * 负责对比ES和数据库的主键列表，找出差异.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaPrimaryKeyChecker {

    /**
     * 获取ES中的所有主键.
     *
     * @param indexName 索引名称
     * @param pkField   主键字段名
     * @return 主键集合
     */
    Set<Object> getEsPrimaryKeys(String indexName, String pkField);

    /**
     * 获取数据库中的所有主键.
     *
     * @param tableName 表名
     * @param pkField   主键字段名
     * @return 主键集合
     */
    Set<Object> getDbPrimaryKeys(String tableName, String pkField);

    /**
     * 批量获取ES主键.
     *
     * @param indexName 索引名称
     * @param pkField   主键字段名
     * @param offset    偏移量
     * @param batchSize 批次大小
     * @return 主键列表
     */
    List<Object> getEsPrimaryKeysBatch(String indexName, String pkField, int offset, int batchSize);

    /**
     * 批量获取数据库主键.
     *
     * @param tableName 表名
     * @param pkField   主键字段名
     * @param offset    偏移量
     * @param batchSize 批次大小
     * @return 主键列表
     */
    List<Object> getDbPrimaryKeysBatch(String tableName, String pkField, int offset, int batchSize);

    /**
     * 获取ES主键数量.
     *
     * @param indexName 索引名称
     * @return 主键数量
     */
    long getEsPrimaryKeyCount(String indexName);

    /**
     * 获取数据库主键数量.
     *
     * @param tableName 表名
     * @return 主键数量
     */
    long getDbPrimaryKeyCount(String tableName);

    /**
     * 比较主键差异.
     *
     * @param esKeys ES主键集合
     * @param dbKeys 数据库主键集合
     * @return 校验结果
     */
    DsaCheckResult comparePrimaryKeys(Set<Object> esKeys, Set<Object> dbKeys);

    /**
     * 执行主键校验.
     *
     * @param task 校验任务
     * @return 校验结果
     */
    DsaCheckResult check(DsaCheckTask task);
}
