package io.github.dbsearchaccel.infra.db;

import io.github.dbsearchaccel.common.core.exception.DsaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库访问器实现类.
 * <p>
 * 基于Spring JdbcTemplate实现，主要用于根据主键ID批量查询详情.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaDbAccessorImpl implements DsaDbAccessor {

    private static final Logger log = LoggerFactory.getLogger(DsaDbAccessorImpl.class);

    /**
     * JdbcTemplate.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * 构造方法.
     *
     * @param jdbcTemplate JdbcTemplate
     */
    public DsaDbAccessorImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public <T> List<T> queryByIds(String table, List<String> ids, String idField, Class<T> clazz) {
        List<Map<String, Object>> results = queryByIdsInternal(table, ids, idField, null);
        return convertToEntityList(results, clazz);
    }

    @Override
    public <T> List<T> queryByIds(String table, List<String> ids, String idField, List<String> fields, Class<T> clazz) {
        List<Map<String, Object>> results = queryByIdsInternal(table, ids, idField, fields);
        return convertToEntityList(results, clazz);
    }

    @Override
    public List<Map<String, Object>> queryByIds(String table, List<String> ids, String idField) {
        return queryByIdsInternal(table, ids, idField, null);
    }

    @Override
    public List<Map<String, Object>> queryByIds(String table, List<String> ids, String idField, List<String> fields) {
        return queryByIdsInternal(table, ids, idField, fields);
    }

    /**
     * 内部方法：根据主键ID列表查询数据.
     *
     * @param table   表名
     * @param ids     主键ID列表
     * @param idField 主键字段名
     * @param fields  查询字段列表
     * @return Map列表
     */
    private List<Map<String, Object>> queryByIdsInternal(String table, List<String> ids, String idField, List<String> fields) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        String fieldStr = (fields == null || fields.isEmpty()) ? "*" : String.join(", ", fields);
        String placeholders = String.join(",", ids.stream().map(id -> "?").toArray(String[]::new));
        String sql = String.format("SELECT %s FROM %s WHERE %s IN (%s)", fieldStr, table, idField, placeholders);

        log.debug("Query by ids sql: {}", sql);
        return jdbcTemplate.query(sql, ids.toArray(), this::resultSetToMap);
    }

    @Override
    public <T> List<T> queryByCondition(String table, Map<String, Object> conditions, Class<T> clazz) {
        List<Map<String, Object>> results = queryByCondition(table, conditions);
        return convertToEntityList(results, clazz);
    }

    @Override
    public List<Map<String, Object>> queryByCondition(String table, Map<String, Object> conditions) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(table);
        List<Object> params = new ArrayList<>();

        if (conditions != null && !conditions.isEmpty()) {
            sql.append(" WHERE ");
            List<String> whereClauses = new ArrayList<>();
            for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                whereClauses.add(entry.getKey() + " = ?");
                params.add(entry.getValue());
            }
            sql.append(String.join(" AND ", whereClauses));
        }

        log.debug("Query by condition sql: {}", sql);
        return jdbcTemplate.query(sql.toString(), params.toArray(), this::resultSetToMap);
    }

    @Override
    public long count(String table, Map<String, Object> conditions) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ").append(table);
        List<Object> params = new ArrayList<>();

        if (conditions != null && !conditions.isEmpty()) {
            sql.append(" WHERE ");
            List<String> whereClauses = new ArrayList<>();
            for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                whereClauses.add(entry.getKey() + " = ?");
                params.add(entry.getValue());
            }
            sql.append(String.join(" AND ", whereClauses));
        }

        log.debug("Count sql: {}", sql);
        Long count = jdbcTemplate.queryForObject(sql.toString(), params.toArray(), Long.class);
        return count != null ? count : 0;
    }

    @Override
    public <T> T queryOne(String table, Map<String, Object> conditions, Class<T> clazz) {
        Map<String, Object> result = queryOne(table, conditions);
        if (result == null || result.isEmpty()) {
            return null;
        }
        try {
            return convertToEntity(result, clazz);
        } catch (Exception e) {
            log.error("Convert result to entity error", e);
            return null;
        }
    }

    @Override
    public Map<String, Object> queryOne(String table, Map<String, Object> conditions) {
        List<Map<String, Object>> results = queryByCondition(table, conditions);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 将ResultSet转换为Map.
     *
     * @param rs      ResultSet
     * @param rowNum  行号
     * @return Map
     * @throws SQLException SQL异常
     */
    private Map<String, Object> resultSetToMap(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        Map<String, Object> map = new HashMap<>(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnLabel(i);
            Object value = rs.getObject(i);
            map.put(columnName, value);
        }
        return map;
    }

    /**
     * 将Map列表转换为实体列表.
     *
     * @param <T>     实体类型
     * @param results Map列表
     * @param clazz   实体类
     * @return 实体列表
     */
    private <T> List<T> convertToEntityList(List<Map<String, Object>> results, Class<T> clazz) {
        List<T> list = new ArrayList<>(results.size());
        for (Map<String, Object> result : results) {
            try {
                T obj = convertToEntity(result, clazz);
                list.add(obj);
            } catch (Exception e) {
                log.error("Convert result to entity error", e);
            }
        }
        return list;
    }

    /**
     * 将Map转换为实体.
     *
     * @param <T>   实体类型
     * @param map   Map
     * @param clazz 实体类
     * @return 实体对象
     */
    @SuppressWarnings("unchecked")
    private <T> T convertToEntity(Map<String, Object> map, Class<T> clazz) {
        if (clazz == Map.class) {
            return (T) map;
        }
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                try {
                    java.lang.reflect.Field field = clazz.getDeclaredField(camelCase(entry.getKey()));
                    field.setAccessible(true);
                    field.set(instance, entry.getValue());
                } catch (NoSuchFieldException ignored) {
                }
            }
            return instance;
        } catch (Exception e) {
            throw new DsaException("Convert to entity failed: " + e.getMessage());
        }
    }

    /**
     * 将下划线命名转换为驼峰命名.
     *
     * @param underscore 下划线命名字符串
     * @return 驼峰命名字符串
     */
    private String camelCase(String underscore) {
        StringBuilder result = new StringBuilder();
        String[] parts = underscore.split("_");
        result.append(parts[0].toLowerCase());
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    result.append(part.substring(1).toLowerCase());
                }
            }
        }
        return result.toString();
    }
}
