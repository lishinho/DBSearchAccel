package io.github.dbsearchaccel.plugin;

import java.util.List;
import java.util.Map;

/**
 * 同步插件接口.
 * <p>
 * 用于数据同步扩展，可在同步前后执行自定义逻辑.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaSyncPlugin extends DsaPlugin<DsaSyncPlugin.SyncContext, DsaPluginResult> {

    /**
     * 同步插件上下文.
     */
    class SyncContext extends DsaPluginContext {

        private static final long serialVersionUID = 1L;

        /**
         * 同步类型
         */
        private SyncType syncType;

        /**
         * 数据表名
         */
        private String tableName;

        /**
         * 索引名称
         */
        private String indexName;

        /**
         * 同步数据
         */
        private List<Map<String, Object>> data;

        /**
         * 同步阶段
         */
        private SyncPhase phase;

        public SyncContext() {
            super();
        }

        public SyncContext(String sceneCode, SyncType syncType, String tableName, String indexName) {
            super(sceneCode, "SYNC");
            this.syncType = syncType;
            this.tableName = tableName;
            this.indexName = indexName;
        }

        public SyncType getSyncType() {
            return syncType;
        }

        public void setSyncType(SyncType syncType) {
            this.syncType = syncType;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getIndexName() {
            return indexName;
        }

        public void setIndexName(String indexName) {
            this.indexName = indexName;
        }

        public List<Map<String, Object>> getData() {
            return data;
        }

        public void setData(List<Map<String, Object>> data) {
            this.data = data;
        }

        public SyncPhase getPhase() {
            return phase;
        }

        public void setPhase(SyncPhase phase) {
            this.phase = phase;
        }
    }

    /**
     * 同步类型枚举.
     */
    enum SyncType {
        /**
         * 全量同步
         */
        FULL,
        /**
         * 增量同步
         */
        INCREMENT
    }

    /**
     * 同步阶段枚举.
     */
    enum SyncPhase {
        /**
         * 同步前
         */
        PRE_SYNC,
        /**
         * 同步后
         */
        POST_SYNC,
        /**
         * 同步失败
         */
        ON_ERROR
    }

    /**
     * 获取同步阶段.
     *
     * @return 同步阶段
     */
    default SyncPhase getPhase() {
        return SyncPhase.PRE_SYNC;
    }
}
