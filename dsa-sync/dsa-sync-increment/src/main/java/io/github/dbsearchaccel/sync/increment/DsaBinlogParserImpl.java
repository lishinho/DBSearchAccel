package io.github.dbsearchaccel.sync.increment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Binlog解析器实现类.
 * <p>
 * 将原始数据解析为DsaCanalEntry格式，提取变更前后的数据.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaBinlogParserImpl implements DsaBinlogParser {

    private static final Logger log = LoggerFactory.getLogger(DsaBinlogParserImpl.class);

    @Override
    public List<DsaCanalEntry> parse(List<Object> rawData) {
        List<DsaCanalEntry> result = new ArrayList<>();

        if (rawData == null || rawData.isEmpty()) {
            return result;
        }

        for (Object entry : rawData) {
            try {
                if (entry instanceof DsaCanalEntry) {
                    result.add((DsaCanalEntry) entry);
                } else if (entry instanceof Map) {
                    DsaCanalEntry parsedEntry = parseFromMap((Map<?, ?>) entry);
                    if (parsedEntry != null) {
                        result.add(parsedEntry);
                    }
                }
            } catch (Exception e) {
                log.error("Parse entry failed: {}", entry, e);
            }
        }

        return result;
    }

    @Override
    public List<DsaCanalEntry> parseRowData(List<Object> rowDatas,
                                             String eventType,
                                             String tableName) {
        List<DsaCanalEntry> result = new ArrayList<>();

        if (rowDatas == null || rowDatas.isEmpty()) {
            return result;
        }

        for (Object rowData : rowDatas) {
            DsaCanalEntry entry = parseSingleRowData(rowData, eventType, tableName);
            if (entry != null) {
                result.add(entry);
            }
        }

        return result;
    }

    /**
     * 从Map解析数据.
     *
     * @param map 数据Map
     * @return Canal条目
     */
    @SuppressWarnings("unchecked")
    private DsaCanalEntry parseFromMap(Map<?, ?> map) {
        String eventType = (String) map.get("eventType");
        String tableName = (String) map.get("tableName");
        Map<String, Object> beforeData = (Map<String, Object>) map.get("beforeData");
        Map<String, Object> afterData = (Map<String, Object>) map.get("afterData");
        List<Object> primaryKeys = (List<Object>) map.get("primaryKeys");

        DsaCanalEntry entry = new DsaCanalEntry();
        entry.setTableName(tableName);
        entry.setBeforeData(beforeData);
        entry.setAfterData(afterData);
        entry.setPrimaryKeys(primaryKeys);
        entry.setTimestamp(System.currentTimeMillis());

        if ("INSERT".equals(eventType)) {
            entry.setEventType(DsaCanalEntry.EventType.INSERT);
        } else if ("UPDATE".equals(eventType)) {
            entry.setEventType(DsaCanalEntry.EventType.UPDATE);
        } else if ("DELETE".equals(eventType)) {
            entry.setEventType(DsaCanalEntry.EventType.DELETE);
        }

        return entry;
    }

    /**
     * 解析单行数据.
     *
     * @param rowData   行数据
     * @param eventType 事件类型
     * @param tableName 表名
     * @return 解析后的数据条目
     */
    @SuppressWarnings("unchecked")
    private DsaCanalEntry parseSingleRowData(Object rowData,
                                              String eventType,
                                              String tableName) {
        if (!(rowData instanceof Map)) {
            return null;
        }

        Map<String, Object> data = (Map<String, Object>) rowData;
        List<Object> primaryKeys = new ArrayList<>();

        Object pk = data.get("id");
        if (pk != null) {
            primaryKeys.add(pk);
        }

        DsaCanalEntry entry = new DsaCanalEntry();
        entry.setTableName(tableName);
        entry.setPrimaryKeys(primaryKeys);
        entry.setTimestamp(System.currentTimeMillis());

        switch (eventType.toUpperCase()) {
            case "INSERT":
                entry.setEventType(DsaCanalEntry.EventType.INSERT);
                entry.setAfterData(data);
                break;
            case "UPDATE":
                entry.setEventType(DsaCanalEntry.EventType.UPDATE);
                entry.setAfterData(data);
                break;
            case "DELETE":
                entry.setEventType(DsaCanalEntry.EventType.DELETE);
                entry.setBeforeData(data);
                break;
            default:
                log.debug("Unsupported event type: {}", eventType);
                return null;
        }

        return entry;
    }
}
