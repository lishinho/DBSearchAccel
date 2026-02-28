package io.github.dbsearchaccel.infra.db;

import java.util.List;
import java.util.Map;

/**
 * 数据库访问器接口.
 * <p>
 * 封装数据库的常用操作，主要用于根据主键ID批量查询详情.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaDbAccessor {

    /**
     * 根据主键ID列表查询数据（指定实体类）.
     *
     * @param <T>      实体类型
     * @param table    表名
     * @param ids      主键ID列表
     * @param idField  主键字段名
     * @param clazz    实体类
     * @return 实体列表
     */
    <T> List<T> queryByIds(String table, List<String> ids, String idField, Class<T> clazz);

    /**
     * 根据主键ID列表查询数据（指定字段和实体类）.
     *
     * @param <T>      实体类型
     * @param table    表名
     * @param ids      主键ID列表
     * @param idField  主键字段名
     * @param fields   查询字段列表
     * @param clazz    实体类
     * @return 实体列表
     */
    <T> List<T> queryByIds(String table, List<String> ids, String idField, List<String> fields, Class<T> clazz);

    /**
     * 根据主键ID列表查询数据（返回Map）.
     *
     * @param table   表名
     * @param ids     主键ID列表
     * @param idField 主键字段名
     * @return Map列表
     */
    List<Map<String, Object>> queryByIds(String table, List<String> ids, String idField);

    /**
     * 根据主键ID列表查询数据（指定字段，返回Map）.
     *
     * @param table   表名
     * @param ids     主键ID列表
     * @param idField 主键字段名
     * @param fields  查询字段列表
     * @return Map列表
     */
    List<Map<String, Object>> queryByIds(String table, List<String> ids, String idField, List<String> fields);

    /**
     * 根据条件查询数据（指定实体类）.
     *
     * @param <T>        实体类型
     * @param table      表名
     * @param conditions 查询条件
     * @param clazz      实体类
     * @return 实体列表
     */
    <T> List<T> queryByCondition(String table, Map<String, Object> conditions, Class<T> clazz);

    /**
     * 根据条件查询数据（返回Map）.
     *
     * @param table      表名
     * @param conditions 查询条件
     * @return Map列表
     */
    List<Map<String, Object>> queryByCondition(String table, Map<String, Object> conditions);

    /**
     * 统计记录数.
     *
     * @param table      表名
     * @param conditions 查询条件
     * @return 记录数
     */
    long count(String table, Map<String, Object> conditions);

    /**
     * 查询单条数据（指定实体类）.
     *
     * @param <T>        实体类型
     * @param table      表名
     * @param conditions 查询条件
     * @param clazz      实体类
     * @return 实体对象
     */
    <T> T queryOne(String table, Map<String, Object> conditions, Class<T> clazz);

    /**
     * 查询单条数据（返回Map）.
     *
     * @param table      表名
     * @param conditions 查询条件
     * @return Map对象
     */
    Map<String, Object> queryOne(String table, Map<String, Object> conditions);
}
