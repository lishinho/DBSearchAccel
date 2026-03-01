package io.github.dbsearchaccel.consistency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * 主键校验器实现.
 * <p>
 * 负责对比ES和数据库的主键列表，找出差异.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaPrimaryKeyCheckerImpl implements DsaPrimaryKeyChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(DsaPrimaryKeyCheckerImpl.class);

    private static final int DEFAULT_BATCH_SIZE = 1000;

    @Autowired(required = false)
    private Object esClient;

    @Autowired(required = false)
    private Object jdbcTemplate;

    @Override
    public Set<Object> getEsPrimaryKeys(String indexName, String pkField) {
        LOGGER.info("Getting ES primary keys for index: {}, pkField: {}", indexName, pkField);
        Set<Object> keys = new HashSet<>();
        int offset = 0;
        List<Object> batch;
        do {
            batch = getEsPrimaryKeysBatch(indexName, pkField, offset, DEFAULT_BATCH_SIZE);
            keys.addAll(batch);
            offset += DEFAULT_BATCH_SIZE;
        } while (batch.size() == DEFAULT_BATCH_SIZE);
        LOGGER.info("Total ES primary keys: {}", keys.size());
        return keys;
    }

    @Override
    public Set<Object> getDbPrimaryKeys(String tableName, String pkField) {
        LOGGER.info("Getting DB primary keys for table: {}, pkField: {}", tableName, pkField);
        Set<Object> keys = new HashSet<>();
        int offset = 0;
        List<Object> batch;
        do {
            batch = getDbPrimaryKeysBatch(tableName, pkField, offset, DEFAULT_BATCH_SIZE);
            keys.addAll(batch);
            offset += DEFAULT_BATCH_SIZE;
        } while (batch.size() == DEFAULT_BATCH_SIZE);
        LOGGER.info("Total DB primary keys: {}", keys.size());
        return keys;
    }

    @Override
    public List<Object> getEsPrimaryKeysBatch(String indexName, String pkField, int offset, int batchSize) {
        List<Object> keys = new ArrayList<>(batchSize);
        LOGGER.debug("Getting ES primary keys batch: index={}, offset={}, batchSize={}",
                indexName, offset, batchSize);
        return keys;
    }

    @Override
    public List<Object> getDbPrimaryKeysBatch(String tableName, String pkField, int offset, int batchSize) {
        List<Object> keys = new ArrayList<>(batchSize);
        LOGGER.debug("Getting DB primary keys batch: table={}, offset={}, batchSize={}",
                tableName, offset, batchSize);
        return keys;
    }

    @Override
    public long getEsPrimaryKeyCount(String indexName) {
        LOGGER.info("Getting ES primary key count for index: {}", indexName);
        return 0L;
    }

    @Override
    public long getDbPrimaryKeyCount(String tableName) {
        LOGGER.info("Getting DB primary key count for table: {}", tableName);
        return 0L;
    }

    @Override
    public DsaCheckResult comparePrimaryKeys(Set<Object> esKeys, Set<Object> dbKeys) {
        LOGGER.info("Comparing primary keys: esKeys={}, dbKeys={}", esKeys.size(), dbKeys.size());

        DsaCheckResult result = new DsaCheckResult();
        result.setCheckType(DsaCheckType.PRIMARY_KEY);
        result.setStartTime(new Date());

        Set<Object> missingInDb = new HashSet<>(esKeys);
        missingInDb.removeAll(dbKeys);

        Set<Object> missingInEs = new HashSet<>(dbKeys);
        missingInEs.removeAll(esKeys);

        result.setEsCount(esKeys.size());
        result.setDbCount(dbKeys.size());
        result.setDiffCount(missingInDb.size() + missingInEs.size());
        result.setConsistent(missingInDb.isEmpty() && missingInEs.isEmpty());

        if (!missingInDb.isEmpty()) {
            result.setMissingInDb(truncateList(missingInDb, 100));
        }
        if (!missingInEs.isEmpty()) {
            result.setMissingInEs(truncateList(missingInEs, 100));
        }

        result.setEndTime(new Date());
        result.setElapsedMs(result.getEndTime().getTime() - result.getStartTime().getTime());

        LOGGER.info("Primary key comparison result: consistent={}, diffCount={}, missingInDb={}, missingInEs={}",
                result.isConsistent(), result.getDiffCount(), missingInDb.size(), missingInEs.size());

        return result;
    }

    @Override
    public DsaCheckResult check(DsaCheckTask task) {
        LOGGER.info("Starting primary key check for task: {}", task.getTaskId());

        DsaCheckResult result = new DsaCheckResult();
        result.setTaskId(task.getTaskId());
        result.setSceneCode(task.getSceneCode());
        result.setCheckType(DsaCheckType.PRIMARY_KEY);
        result.setStartTime(new Date());

        try {
            Set<Object> esKeys = getEsPrimaryKeys(task.getIndexName(), task.getPkField());
            Set<Object> dbKeys = getDbPrimaryKeys(task.getTableName(), task.getPkField());

            result = comparePrimaryKeys(esKeys, dbKeys);
            result.setTaskId(task.getTaskId());
            result.setSceneCode(task.getSceneCode());

        } catch (Exception e) {
            LOGGER.error("Primary key check failed for task: {}", task.getTaskId(), e);
            result.setConsistent(false);
            result.setErrorMessage("Check failed: " + e.getMessage());
        }

        result.setEndTime(new Date());
        result.setElapsedMs(result.getEndTime().getTime() - result.getStartTime().getTime());

        return result;
    }

    /**
     * 截断列表并转换为字符串.
     *
     * @param set      集合
     * @param maxCount 最大数量
     * @return 字符串表示
     */
    private String truncateList(Set<Object> set, int maxCount) {
        List<Object> list = new ArrayList<>(set);
        if (list.size() > maxCount) {
            list = list.subList(0, maxCount);
            return list.toString() + "... (total: " + set.size() + ")";
        }
        return list.toString();
    }
}
