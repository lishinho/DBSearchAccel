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

/**
 * 过滤规则服务实现类.
 * <p>
 * 整合灰度过滤器、DSL构建器、查询校验器，提供完整的过滤规则服务.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaFilterServiceImpl implements DsaFilterService {

    private static final Logger log = LoggerFactory.getLogger(DsaFilterServiceImpl.class);

    private final DsaGrayFilter grayFilter;
    private final DsaDslBuilder dslBuilder;
    private final DsaQueryValidator queryValidator;

    /**
     * 默认构造方法.
     * <p>
     * 使用默认的灰度过滤器、DSL构建器、查询校验器
     * </p>
     */
    public DsaFilterServiceImpl() {
        this.grayFilter = new DsaGrayFilterImpl();
        this.dslBuilder = new DsaDslBuilderImpl();
        this.queryValidator = new DsaQueryValidatorImpl();
    }

    /**
     * 构造方法.
     *
     * @param grayFilter    灰度过滤器
     * @param dslBuilder    DSL构建器
     * @param queryValidator 查询校验器
     */
    public DsaFilterServiceImpl(DsaGrayFilter grayFilter,
                                 DsaDslBuilder dslBuilder,
                                 DsaQueryValidator queryValidator) {
        this.grayFilter = grayFilter != null ? grayFilter : new DsaGrayFilterImpl();
        this.dslBuilder = dslBuilder != null ? dslBuilder : new DsaDslBuilderImpl();
        this.queryValidator = queryValidator != null ? queryValidator : new DsaQueryValidatorImpl();
    }

    @Override
    public boolean checkGrayAccess(DsaRequest request, DsaSceneConfig config) {
        String grayKey = request.getGrayKey();
        if (grayKey == null || grayKey.isEmpty()) {
            log.debug("No gray key provided, allow access");
            return true;
        }

        boolean allowed = grayFilter.allowAccess(request, config);
        log.debug("Gray access check, grayKey: [{}], allowed: {}", grayKey, allowed);
        return allowed;
    }

    @Override
    public DsaDsl buildDsl(DsaRequest request, DsaSceneConfig config) {
        if (dslBuilder instanceof DsaDslBuilderImpl) {
            ((DsaDslBuilderImpl) dslBuilder).clear();
        }

        DsaDsl dsl = dslBuilder.build(request, config);
        log.debug("Built DSL for scene [{}], index [{}]", request.getSceneTypeCode(), config.getEsIndex());
        return dsl;
    }

    @Override
    public boolean validateRequest(DsaRequest request) {
        return queryValidator.validate(request);
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
