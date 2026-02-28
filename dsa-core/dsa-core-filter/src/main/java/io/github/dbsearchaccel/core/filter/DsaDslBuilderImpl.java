package io.github.dbsearchaccel.core.filter;

import io.github.dbsearchaccel.common.model.config.DsaSceneConfig;
import io.github.dbsearchaccel.common.model.dsl.DsaDsl;
import io.github.dbsearchaccel.common.model.request.DsaRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ES DSL构建器默认实现.
 * <p>
 * 支持动态构建ES查询DSL，包括：
 * - term精确匹配
 * - range范围查询
 * - exists字段存在性检查
 * - wildcard通配符查询
 * - bool复合查询
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaDslBuilderImpl implements DsaDslBuilder {

    private static final Logger log = LoggerFactory.getLogger(DsaDslBuilderImpl.class);

    private static final String OPERATOR_GT = "gt";
    private static final String OPERATOR_GTE = "gte";
    private static final String OPERATOR_LT = "lt";
    private static final String OPERATOR_LTE = "lte";

    private final List<Map<String, Object>> mustClauses = new ArrayList<>();
    private final List<Map<String, Object>> shouldClauses = new ArrayList<>();
    private final List<Map<String, Object>> mustNotClauses = new ArrayList<>();

    @Override
    public DsaDsl build(DsaRequest request, DsaSceneConfig config) {
        DsaDsl dsl = DsaDsl.of(config.getEsIndex());

        Map<String, Object> queryMap = buildQueryMap(request, config);
        dsl.query(queryMap);

        dsl.from(request.getOffset());
        dsl.size(request.getPageSize());

        if (request.getSortField() != null && !request.getSortField().isEmpty()) {
            dsl.addSort(request.getSortField(), request.getSortOrder());
        }

        String[] includes = config.getPrimaryKeyField() != null
                ? new String[]{config.getPrimaryKeyField()} : null;
        dsl.setSourceIncludes(includes);

        log.debug("Built DSL for scene [{}], index [{}], must clauses: {}",
                request.getSceneTypeCode(), config.getEsIndex(), mustClauses.size());

        return dsl;
    }

    @Override
    public Map<String, Object> buildQueryMap(DsaRequest request, DsaSceneConfig config) {
        Map<String, Object> queryParams = request.getQueryParams();
        Map<String, String> fieldMapping = config.getDslFieldMapping();
        if (fieldMapping == null) {
            fieldMapping = new HashMap<>();
        }

        if (queryParams != null && !queryParams.isEmpty()) {
            for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
                String businessField = entry.getKey();
                Object value = entry.getValue();
                String esField = fieldMapping.getOrDefault(businessField, businessField);
                addTerm(esField, value);
            }
        }

        if (mustClauses.isEmpty() && shouldClauses.isEmpty() && mustNotClauses.isEmpty()) {
            Map<String, Object> matchAll = new HashMap<>();
            matchAll.put("match_all", new HashMap<>());
            return matchAll;
        }

        Map<String, Object> boolQuery = new HashMap<>();
        Map<String, Object> bool = new HashMap<>();

        if (!mustClauses.isEmpty()) {
            bool.put("must", new ArrayList<>(mustClauses));
        }
        if (!shouldClauses.isEmpty()) {
            bool.put("should", new ArrayList<>(shouldClauses));
        }
        if (!mustNotClauses.isEmpty()) {
            bool.put("must_not", new ArrayList<>(mustNotClauses));
        }

        boolQuery.put("bool", bool);
        return boolQuery;
    }

    @Override
    public DsaDslBuilder addTerm(String fieldName, Object value) {
        if (fieldName == null || value == null) {
            return this;
        }

        Map<String, Object> termClause = new HashMap<>();
        Map<String, Object> termValue = new HashMap<>();
        termValue.put(fieldName, value);
        termClause.put("term", termValue);
        mustClauses.add(termClause);

        log.trace("Added term clause: {} = {}", fieldName, value);
        return this;
    }

    @Override
    public DsaDslBuilder addRange(String fieldName, String operator, Object value) {
        if (fieldName == null || operator == null || value == null) {
            return this;
        }

        String normalizedOperator = operator.toLowerCase();
        if (!isValidRangeOperator(normalizedOperator)) {
            log.warn("Invalid range operator: {}, skip", operator);
            return this;
        }

        Map<String, Object> rangeClause = new HashMap<>();
        Map<String, Object> rangeValue = new HashMap<>();
        Map<String, Object> fieldCondition = new HashMap<>();
        fieldCondition.put(normalizedOperator, value);
        rangeValue.put(fieldName, fieldCondition);
        rangeClause.put("range", rangeValue);
        mustClauses.add(rangeClause);

        log.trace("Added range clause: {} {} {}", fieldName, operator, value);
        return this;
    }

    @Override
    public DsaDslBuilder addExists(String fieldName) {
        if (fieldName == null) {
            return this;
        }

        Map<String, Object> existsClause = new HashMap<>();
        Map<String, Object> existsValue = new HashMap<>();
        existsValue.put("field", fieldName);
        existsClause.put("exists", existsValue);
        mustClauses.add(existsClause);

        log.trace("Added exists clause: {}", fieldName);
        return this;
    }

    @Override
    public DsaDslBuilder addWildcard(String fieldName, String value) {
        if (fieldName == null || value == null) {
            return this;
        }

        Map<String, Object> wildcardClause = new HashMap<>();
        Map<String, Object> wildcardValue = new HashMap<>();
        wildcardValue.put(fieldName, value);
        wildcardClause.put("wildcard", wildcardValue);
        mustClauses.add(wildcardClause);

        log.trace("Added wildcard clause: {} = {}", fieldName, value);
        return this;
    }

    /**
     * 添加should条件（或逻辑）.
     *
     * @param fieldName 字段名
     * @param value     字段值
     * @return 当前构建器实例
     */
    public DsaDslBuilder addShould(String fieldName, Object value) {
        if (fieldName == null || value == null) {
            return this;
        }

        Map<String, Object> termClause = new HashMap<>();
        Map<String, Object> termValue = new HashMap<>();
        termValue.put(fieldName, value);
        termClause.put("term", termValue);
        shouldClauses.add(termClause);

        return this;
    }

    /**
     * 添加must_not条件（非逻辑）.
     *
     * @param fieldName 字段名
     * @param value     字段值
     * @return 当前构建器实例
     */
    public DsaDslBuilder addMustNot(String fieldName, Object value) {
        if (fieldName == null || value == null) {
            return this;
        }

        Map<String, Object> termClause = new HashMap<>();
        Map<String, Object> termValue = new HashMap<>();
        termValue.put(fieldName, value);
        termClause.put("term", termValue);
        mustNotClauses.add(termClause);

        return this;
    }

    /**
     * 清空所有条件.
     *
     * @return 当前构建器实例
     */
    public DsaDslBuilder clear() {
        mustClauses.clear();
        shouldClauses.clear();
        mustNotClauses.clear();
        return this;
    }

    /**
     * 校验range操作符是否有效.
     *
     * @param operator 操作符
     * @return true表示有效
     */
    private boolean isValidRangeOperator(String operator) {
        return OPERATOR_GT.equals(operator)
                || OPERATOR_GTE.equals(operator)
                || OPERATOR_LT.equals(operator)
                || OPERATOR_LTE.equals(operator);
    }
}
