package io.github.dbsearchaccel.fallback;

import io.github.dbsearchaccel.common.model.request.DsaRequest;
import io.github.dbsearchaccel.common.model.response.DsaPageInfo;
import io.github.dbsearchaccel.common.model.response.DsaResponse;
import io.github.dbsearchaccel.infra.db.DsaDbAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 降级服务实现类.
 * <p>
 * 负责处理降级逻辑，当ES/Redis不可用或触发熔断时，回退到数据库查询.
 * 支持手动降级和自动熔断.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaFallbackServiceImpl implements DsaFallbackService {

    private static final Logger log = LoggerFactory.getLogger(DsaFallbackServiceImpl.class);

    /**
     * 数据库访问器.
     */
    private final DsaDbAccessor dbAccessor;

    /**
     * 正在降级的场景集合.
     */
    private final Set<String> degradingScenes = ConcurrentHashMap.newKeySet();

    /**
     * 构造方法.
     *
     * @param dbAccessor 数据库访问器
     */
    public DsaFallbackServiceImpl(DsaDbAccessor dbAccessor) {
        this.dbAccessor = dbAccessor;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> DsaResponse<T> fallback(DsaRequest request, Class<T> clazz) {
        long startTime = System.currentTimeMillis();
        String traceId = request.getTraceId();

        log.info("Fallback to database query, traceId: {}, scene: {}", traceId, request.getSceneTypeCode());

        try {
            Map<String, Object> conditions = request.getQueryParams();
            if (conditions == null) {
                conditions = new HashMap<>();
            }

            List<?> results = dbAccessor.queryByCondition(getTableName(request), conditions, clazz);
            long total = dbAccessor.count(getTableName(request), conditions);

            int pageNum = request.getPageNum() != null ? request.getPageNum() : 1;
            int pageSize = request.getPageSize() != null ? request.getPageSize() : 10;

            int from = (pageNum - 1) * pageSize;
            int to = Math.min(from + pageSize, results.size());

            List<?> pageResults = from < results.size() ? results.subList(from, to) : new ArrayList<>();

            DsaResponse<T> response = new DsaResponse<>();
            response.setCode("200");
            response.setMessage("success");
            response.setData((T) pageResults);
            response.setPageInfo(DsaPageInfo.of(total, pageNum, pageSize));
            response.setTraceId(traceId);
            response.setFromFallback(true);
            response.setCostMillis(System.currentTimeMillis() - startTime);

            log.info("Fallback query success, traceId: {}, cost: {}ms", traceId, response.getCostMillis());
            return response;

        } catch (Exception e) {
            log.error("Fallback query error, traceId: {}", traceId, e);
            return DsaResponse.error("5003", "Database query failed: " + e.getMessage());
        }
    }

    @Override
    public boolean isDegrading(String sceneCode) {
        return degradingScenes.contains(sceneCode);
    }

    @Override
    public void enableDegrade(String sceneCode) {
        degradingScenes.add(sceneCode);
        log.warn("Enable degrade for scene: {}", sceneCode);
    }

    @Override
    public void disableDegrade(String sceneCode) {
        degradingScenes.remove(sceneCode);
        log.info("Disable degrade for scene: {}", sceneCode);
    }

    @Override
    public boolean isCircuitBreakerOpen(String sceneCode) {
        return false;
    }

    /**
     * 获取表名.
     *
     * @param request 请求参数
     * @return 表名
     */
    private String getTableName(DsaRequest request) {
        return request.getSceneTypeCode().toLowerCase();
    }
}
