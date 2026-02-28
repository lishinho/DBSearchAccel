package io.github.dbsearchaccel.core.filter;

import io.github.dbsearchaccel.common.core.enums.DsaResultCode;
import io.github.dbsearchaccel.common.core.exception.DsaException;
import io.github.dbsearchaccel.common.model.request.DsaRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 查询校验器默认实现.
 * <p>
 * 提供请求参数的全面校验，包括：
 * - 场景类型必填校验
 * - 分页参数范围校验
 * - 排序字段安全校验
 * - 查询参数格式校验
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaQueryValidatorImpl implements DsaQueryValidator {

    private static final Logger log = LoggerFactory.getLogger(DsaQueryValidatorImpl.class);

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 1000;
    private static final int MAX_QUERY_PARAMS = 50;
    private static final Pattern FIELD_NAME_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    private final List<String> errors = new ArrayList<>();

    private int maxPageSize = MAX_PAGE_SIZE;
    private int maxQueryParams = MAX_QUERY_PARAMS;

    @Override
    public boolean validate(DsaRequest request) {
        clearErrors();

        validateSceneType(request);
        validatePagination(request);
        validateSort(request);
        validateQueryParams(request);

        if (!errors.isEmpty()) {
            log.warn("Request validation failed: {}", errors);
            throw new DsaException(DsaResultCode.PARAM_ERROR.getCode(),
                    "Parameter validation failed: " + String.join("; ", errors));
        }

        normalizeRequest(request);
        return true;
    }

    @Override
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    @Override
    public void clearErrors() {
        errors.clear();
    }

    /**
     * 校验场景类型.
     *
     * @param request 请求参数
     */
    private void validateSceneType(DsaRequest request) {
        if (request.getSceneType() == null
                && (request.getSceneCode() == null || request.getSceneCode().isEmpty())) {
            errors.add("Scene type is required");
        }
    }

    /**
     * 校验分页参数.
     *
     * @param request 请求参数
     */
    private void validatePagination(DsaRequest request) {
        if (request.getPageNum() != null && request.getPageNum() < 1) {
            errors.add("Page number must be greater than 0");
        }

        if (request.getPageSize() != null) {
            if (request.getPageSize() < 1) {
                errors.add("Page size must be greater than 0");
            } else if (request.getPageSize() > maxPageSize) {
                errors.add("Page size must not exceed " + maxPageSize);
            }
        }
    }

    /**
     * 校验排序参数.
     *
     * @param request 请求参数
     */
    private void validateSort(DsaRequest request) {
        if (request.getSortField() != null && !request.getSortField().isEmpty()) {
            if (!FIELD_NAME_PATTERN.matcher(request.getSortField()).matches()) {
                errors.add("Invalid sort field name: " + request.getSortField());
            }
        }

        if (request.getSortOrder() != null) {
            String order = request.getSortOrder().toUpperCase();
            if (!"ASC".equals(order) && !"DESC".equals(order)) {
                errors.add("Sort order must be ASC or DESC");
            }
        }
    }

    /**
     * 校验查询参数.
     *
     * @param request 请求参数
     */
    private void validateQueryParams(DsaRequest request) {
        if (request.getQueryParams() == null) {
            return;
        }

        if (request.getQueryParams().size() > maxQueryParams) {
            errors.add("Query params count must not exceed " + maxQueryParams);
        }

        for (String fieldName : request.getQueryParams().keySet()) {
            if (!FIELD_NAME_PATTERN.matcher(fieldName).matches()) {
                errors.add("Invalid query field name: " + fieldName);
            }
        }
    }

    /**
     * 规范化请求参数.
     * <p>
     * 设置默认值，修正不合规参数
     * </p>
     *
     * @param request 请求参数
     */
    private void normalizeRequest(DsaRequest request) {
        if (request.getPageNum() == null || request.getPageNum() < 1) {
            request.setPageNum(1);
        }

        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        } else if (request.getPageSize() > maxPageSize) {
            request.setPageSize(maxPageSize);
        }

        if (request.getSortOrder() == null || request.getSortOrder().isEmpty()) {
            request.setSortOrder("DESC");
        } else {
            request.setSortOrder(request.getSortOrder().toUpperCase());
        }
    }

    /**
     * 设置最大分页大小.
     *
     * @param maxPageSize 最大分页大小
     */
    public void setMaxPageSize(int maxPageSize) {
        this.maxPageSize = maxPageSize;
    }

    /**
     * 设置最大查询参数数量.
     *
     * @param maxQueryParams 最大查询参数数量
     */
    public void setMaxQueryParams(int maxQueryParams) {
        this.maxQueryParams = maxQueryParams;
    }
}
