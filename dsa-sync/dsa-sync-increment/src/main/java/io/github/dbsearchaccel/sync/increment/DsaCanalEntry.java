package io.github.dbsearchaccel.sync.increment;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Canal数据条目.
 * <p>
 * 封装从Canal获取的单条变更数据，包括事件类型、表名、变更前后数据等.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Data
public class DsaCanalEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件类型枚举.
     */
    public enum EventType {
        INSERT,
        UPDATE,
        DELETE,
        CREATE,
        ALTER,
        ERASE,
        QUERY,
        TRUNCATE,
        RENAME,
        CINDEX,
        DINDEX
    }

    /**
     * 批次ID.
     */
    private long batchId;

    /**
     * 数据库名.
     */
    private String database;

    /**
     * 表名.
     */
    private String tableName;

    /**
     * 事件类型.
     */
    private EventType eventType;

    /**
     * 变更前数据.
     */
    private Map<String, Object> beforeData;

    /**
     * 变更后数据.
     */
    private Map<String, Object> afterData;

    /**
     * 主键值列表.
     */
    private List<Object> primaryKeys;

    /**
     * 变更时间戳.
     */
    private long timestamp;

    /**
     * Binlog位置.
     */
    private String position;

    /**
     * 静态工厂方法-插入事件.
     *
     * @param tableName  表名
     * @param afterData  变更后数据
     * @param primaryKeys 主键列表
     * @return Canal条目
     */
    public static DsaCanalEntry ofInsert(String tableName, Map<String, Object> afterData, List<Object> primaryKeys) {
        DsaCanalEntry entry = new DsaCanalEntry();
        entry.setTableName(tableName);
        entry.setEventType(EventType.INSERT);
        entry.setAfterData(afterData);
        entry.setPrimaryKeys(primaryKeys);
        entry.setTimestamp(System.currentTimeMillis());
        return entry;
    }

    /**
     * 静态工厂方法-更新事件.
     *
     * @param tableName   表名
     * @param beforeData  变更前数据
     * @param afterData   变更后数据
     * @param primaryKeys 主键列表
     * @return Canal条目
     */
    public static DsaCanalEntry ofUpdate(String tableName, Map<String, Object> beforeData,
                                          Map<String, Object> afterData, List<Object> primaryKeys) {
        DsaCanalEntry entry = new DsaCanalEntry();
        entry.setTableName(tableName);
        entry.setEventType(EventType.UPDATE);
        entry.setBeforeData(beforeData);
        entry.setAfterData(afterData);
        entry.setPrimaryKeys(primaryKeys);
        entry.setTimestamp(System.currentTimeMillis());
        return entry;
    }

    /**
     * 静态工厂方法-删除事件.
     *
     * @param tableName   表名
     * @param beforeData  变更前数据
     * @param primaryKeys 主键列表
     * @return Canal条目
     */
    public static DsaCanalEntry ofDelete(String tableName, Map<String, Object> beforeData, List<Object> primaryKeys) {
        DsaCanalEntry entry = new DsaCanalEntry();
        entry.setTableName(tableName);
        entry.setEventType(EventType.DELETE);
        entry.setBeforeData(beforeData);
        entry.setPrimaryKeys(primaryKeys);
        entry.setTimestamp(System.currentTimeMillis());
        return entry;
    }

    /**
     * 获取主键值.
     *
     * @return 主键值，若无主键则返回null
     */
    public Object getPrimaryKeyValue() {
        if (primaryKeys != null && !primaryKeys.isEmpty()) {
            return primaryKeys.get(0);
        }
        return null;
    }

    /**
     * 判断是否为数据变更事件.
     *
     * @return true表示为INSERT/UPDATE/DELETE事件
     */
    public boolean isDataChangeEvent() {
        return eventType == EventType.INSERT
                || eventType == EventType.UPDATE
                || eventType == EventType.DELETE;
    }
}
