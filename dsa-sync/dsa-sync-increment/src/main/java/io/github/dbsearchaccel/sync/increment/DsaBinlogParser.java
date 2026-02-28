package io.github.dbsearchaccel.sync.increment;

import java.util.List;

/**
 * Binlog解析器接口.
 * <p>
 * 负责将原始数据解析为业务可用的数据格式.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaBinlogParser {

    /**
     * 解析原始数据列表.
     *
     * @param rawData 原始数据
     * @return 解析后的数据条目列表
     */
    List<DsaCanalEntry> parse(List<Object> rawData);

    /**
     * 解析行数据.
     *
     * @param rowData   行数据
     * @param eventType 事件类型
     * @param tableName 表名
     * @return 解析后的数据条目列表
     */
    List<DsaCanalEntry> parseRowData(List<Object> rowData, String eventType, String tableName);
}
