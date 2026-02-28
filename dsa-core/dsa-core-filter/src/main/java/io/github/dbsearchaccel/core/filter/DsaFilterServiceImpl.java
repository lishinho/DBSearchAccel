package io.github.dbsearchaccel.core.filter;

import io.github.dbsearchaccel.common.core.enums.DsaResultCode;
import io.github.dbsearchaccel.common.core.exception.DsaException;
import io.github.dbsearchaccel.common.model.config.DsaSceneConfig;
import io.github.dbsearchaccel.common.model.dsl.DsaDsl;
import io.github.dbsearchaccel.common.model.request.DsaRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DsaFilterServiceImpl implements DsaFilterService {

    private static final Logger log = LoggerFactory.getLogger(DsaFilterServiceImpl.class);

    @Override
    public boolean checkGrayAccess(DsaRequest request, DsaSceneConfig config) {
        String grayKey = request.getGrayKey();
        if (grayKey == null || grayKey.isEmpty()) {
            log.debug("No gray key provided, allow access");
            return true;
        }

        boolean allowed = config.allowAccess(grayKey);
        log.debug("Gray access check, grayKey: {}, allowed: {}", grayKey, allowed);
        return allowed;
    }

    @Override
    public DsaDsl buildDsl(DsaRequest request, DsaSceneConfig config) {
        DsaDsl dsl = DsaDsl.of(config.getEsIndex());

        Map<String, Object> esQuery = buildEsQuery(request, config);
        dsl.query(esQuery);

        dsl.from(request.getOffset());
        dsl.size(request.getPageSize());

        if (request.getSortField() != null && !request.getSortField().isEmpty()) {
            dsl.addSort(request.getSortField(), request.getSortOrder());
        }

        log.debug("Built DSL for scene: {}, index: {}", request.getSceneTypeCode(), config.getEsIndex());
        return dsl;
    }

    @Override
    public boolean validateRequest(DsaRequest request) {
        if (request.getSceneType() == null && request.getSceneCode() == null) {
            throw new DsaException(DsaResultCode.PARAM_ERROR.getCode(), "Scene type is required");
        }

        if (request.getPageNum() == null || request.getPageNum() < 1) {
            request.setPageNum(1);
        }

        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(10);
        }

        if (request.getPageSize() > 1000) {
            request.setPageSize(1000);
        }

        return true;
    }

    @Override
    public Map<String, Object> buildEsQuery(DsaRequest request, DsaSceneConfig config) {
        Map<String, Object> queryParams = request.getQueryParams();
        if (queryParams == null || queryParams.isEmpty()) {
            Map<String, Object> matchAll = new HashMap<>();
            matchAll.put("match_all", new HashMap<>());
            return matchAll;
        }

        Map<String, Object> boolQuery = new HashMap<>();
        Map<String, Object> bool = new HashMap<>();
        java.util.List<Map<String, Object>> must = new java.util.ArrayList<>();

        Map<String, String> fieldMapping = config.getDslFieldMapping();
        if (fieldMapping == null) {
            fieldMapping = new HashMap<>();
        }

        for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();

            String esFieldName = fieldMapping.getOrDefault(fieldName, fieldName);

            Map<String, Object> termQuery = new HashMap<>();
            Map<String, Object> term = new HashMap<>();
            term.put(esFieldName, value);
            termQuery.put("term", term);
            must.add(termQuery);
        }

        bool.put("must", must);
        boolQuery.put("bool", bool);

        return boolQuery;
    }
}
