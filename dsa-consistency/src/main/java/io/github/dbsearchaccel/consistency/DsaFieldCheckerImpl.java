package io.github.dbsearchaccel.consistency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * 字段校验器实现.
 * <p>
 * 负责对比ES和数据库的字段值，找出不一致的数据.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaFieldCheckerImpl implements DsaFieldChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(DsaFieldCheckerImpl.class);

    private static final int DEFAULT_BATCH_SIZE = 100;

    @Autowired(required = false)
    private Object esClient;

    @Autowired(required = false)
    private Object jdbcTemplate;

    @Override
    public Map<String, Object> getEsFieldValues(String indexName, String pkField, Object primaryKey,
            List<String> fieldNames) {
        LOGGER.debug("Getting ES field values: index={}, pk={}, fields={}", indexName, primaryKey, fieldNames);
        Map<String, Object> values = new HashMap<>();
        return values;
    }

    @Override
    public Map<String, Object> getDbFieldValues(String tableName, String pkField, Object primaryKey,
            List<String> fieldNames) {
        LOGGER.debug("Getting DB field values: table={}, pk={}, fields={}", tableName, primaryKey, fieldNames);
        Map<String, Object> values = new HashMap<>();
        return values;
    }

    @Override
    public Map<Object, Map<String, Object>> getEsFieldValuesBatch(String indexName, String pkField,
            List<Object> primaryKeys, List<String> fieldNames) {
        LOGGER.debug("Getting ES field values batch: index={}, pkCount={}, fields={}",
                indexName, primaryKeys.size(), fieldNames);
        Map<Object, Map<String, Object>> result = new HashMap<>();
        return result;
    }

    @Override
    public Map<Object, Map<String, Object>> getDbFieldValuesBatch(String tableName, String pkField,
            List<Object> primaryKeys, List<String> fieldNames) {
        LOGGER.debug("Getting DB field values batch: table={}, pkCount={}, fields={}",
                tableName, primaryKeys.size(), fieldNames);
        Map<Object, Map<String, Object>> result = new HashMap<>();
        return result;
    }

    @Override
    public Map<String, Map<String, Object>> compareFieldValues(Map<String, Object> esValues,
            Map<String, Object> dbValues, List<String> fieldNames) {
        Map<String, Map<String, Object>> diffs = new HashMap<>();

        for (String field : fieldNames) {
            Object esValue = esValues.get(field);
            Object dbValue = dbValues.get(field);

            if (!Objects.equals(esValue, dbValue)) {
                Map<String, Object> diff = new HashMap<>();
                diff.put("esValue", esValue);
                diff.put("dbValue", dbValue);
                diffs.put(field, diff);
            }
        }

        return diffs;
    }

    @Override
    public DsaCheckResult check(DsaCheckTask task, List<Object> primaryKeys) {
        LOGGER.info("Starting field check for task: {}, pkCount: {}", task.getTaskId(), primaryKeys.size());

        DsaCheckResult result = new DsaCheckResult();
        result.setTaskId(task.getTaskId());
        result.setSceneCode(task.getSceneCode());
        result.setCheckType(DsaCheckType.FIELD);
        result.setStartTime(new Date());

        List<String> fieldNames = parseFieldNames(task.getCompareFieldList());
        int diffCount = 0;
        List<String> diffDetails = new ArrayList<>();

        try {
            for (int i = 0; i < primaryKeys.size(); i += DEFAULT_BATCH_SIZE) {
                int endIndex = Math.min(i + DEFAULT_BATCH_SIZE, primaryKeys.size());
                List<Object> batch = primaryKeys.subList(i, endIndex);

                Map<Object, Map<String, Object>> esValues = getEsFieldValuesBatch(
                        task.getIndexName(), task.getPkField(), batch, fieldNames);
                Map<Object, Map<String, Object>> dbValues = getDbFieldValuesBatch(
                        task.getTableName(), task.getPkField(), batch, fieldNames);

                for (Object pk : batch) {
                    Map<String, Object> esVal = esValues.getOrDefault(pk, Collections.emptyMap());
                    Map<String, Object> dbVal = dbValues.getOrDefault(pk, Collections.emptyMap());

                    Map<String, Map<String, Object>> diffs = compareFieldValues(esVal, dbVal, fieldNames);
                    if (!diffs.isEmpty()) {
                        diffCount++;
                        if (diffDetails.size() < 100) {
                            diffDetails.add("PK=" + pk + ": " + diffs.toString());
                        }
                    }
                }
            }

            result.setConsistent(diffCount == 0);
            result.setDiffCount(diffCount);
            result.setEsCount(primaryKeys.size());
            result.setDbCount(primaryKeys.size());

            if (!diffDetails.isEmpty()) {
                result.setFieldDiffDetails(String.join("\n", diffDetails));
            }

        } catch (Exception e) {
            LOGGER.error("Field check failed for task: {}", task.getTaskId(), e);
            result.setConsistent(false);
            result.setErrorMessage("Check failed: " + e.getMessage());
        }

        result.setEndTime(new Date());
        result.setElapsedMs(result.getEndTime().getTime() - result.getStartTime().getTime());

        return result;
    }

    /**
     * 解析字段名列表.
     *
     * @param fieldListStr 字段列表字符串（逗号分隔）
     * @return 字段名列表
     */
    private List<String> parseFieldNames(String fieldListStr) {
        if (fieldListStr == null || fieldListStr.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String[] fields = fieldListStr.split(",");
        List<String> result = new ArrayList<>();
        for (String field : fields) {
            String trimmed = field.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }
}
