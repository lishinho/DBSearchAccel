package io.github.dbsearchaccel.core.route;

import io.github.dbsearchaccel.common.core.enums.DsaResultCode;
import io.github.dbsearchaccel.common.core.exception.DsaException;
import io.github.dbsearchaccel.common.model.config.DsaSceneConfig;
import io.github.dbsearchaccel.common.model.dsl.DsaDsl;
import io.github.dbsearchaccel.common.model.request.DsaRequest;
import io.github.dbsearchaccel.common.model.response.DsaPageInfo;
import io.github.dbsearchaccel.common.model.response.DsaResponse;
import io.github.dbsearchaccel.core.aggregate.DsaDataAggregateService;
import io.github.dbsearchaccel.core.filter.DsaFilterService;
import io.github.dbsearchaccel.core.index.DsaIndexService;
import io.github.dbsearchaccel.fallback.DsaFallbackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 路由服务实现类.
 * <p>
 * 核心入口服务，负责场景路由、请求处理、结果返回.
 * 实现完整的查询加速流程：参数校验 → 灰度校验 → ES查询 → Redis查询 → 数据聚合 → 数据库查询 → 结果返回.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaRouteServiceImpl implements DsaRouteService {

    private static final Logger log = LoggerFactory.getLogger(DsaRouteServiceImpl.class);

    /**
     * 过滤规则服务.
     */
    private final DsaFilterService filterService;

    /**
     * 索引服务.
     */
    private final DsaIndexService indexService;

    /**
     * 数据聚合服务.
     */
    private final DsaDataAggregateService aggregateService;

    /**
     * 降级服务.
     */
    private final DsaFallbackService fallbackService;

    /**
     * 场景配置缓存.
     */
    private final Map<String, DsaSceneConfig> sceneConfigCache = new ConcurrentHashMap<>();

    /**
     * 构造方法.
     *
     * @param filterService     过滤规则服务
     * @param indexService      索引服务
     * @param aggregateService  数据聚合服务
     * @param fallbackService   降级服务
     */
    public DsaRouteServiceImpl(DsaFilterService filterService,
                                DsaIndexService indexService,
                                DsaDataAggregateService aggregateService,
                                DsaFallbackService fallbackService) {
        this.filterService = filterService;
        this.indexService = indexService;
        this.aggregateService = aggregateService;
        this.fallbackService = fallbackService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> DsaResponse<T> route(DsaRequest request, Class<T> clazz) {
        long startTime = System.currentTimeMillis();
        String traceId = request.getTraceId() != null ? request.getTraceId() : UUID.randomUUID().toString();
        request.setTraceId(traceId);

        log.info("Route request start, traceId: {}, scene: {}", traceId, request.getSceneTypeCode());

        try {
            filterService.validateRequest(request);

            DsaSceneConfig config = getSceneConfig(request.getSceneTypeCode());
            if (config == null) {
                log.error("Scene config not found: {}", request.getSceneTypeCode());
                return DsaResponse.error(DsaResultCode.SCENE_NOT_FOUND);
            }

            if (!config.isEnabled()) {
                log.info("Scene disabled, fallback to db: {}", request.getSceneTypeCode());
                return fallbackService.fallback(request, clazz);
            }

            if (!filterService.checkGrayAccess(request, config)) {
                log.info("Gray access denied, fallback to db: {}", request.getSceneTypeCode());
                return fallbackService.fallback(request, clazz);
            }

            if (request.isForceFallback()) {
                log.info("Force fallback enabled: {}", request.getSceneTypeCode());
                return fallbackService.fallback(request, clazz);
            }

            DsaDsl dsl = filterService.buildDsl(request, config);

            List<String> esIds = indexService.queryEsIds(dsl);
            log.debug("ES ids count: {}", esIds.size());

            List<String> redisIds = Collections.emptyList();
            if (config.isIncrementEnabled()) {
                redisIds = indexService.queryRedisIds(request.getSceneTypeCode());
                log.debug("Redis ids count: {}", redisIds.size());
            }

            List<String> aggregatedIds = aggregateService.aggregateIds(esIds, redisIds, request);
            log.debug("Aggregated ids count: {}", aggregatedIds.size());

            long total = aggregatedIds.size();
            int pageNum = request.getPageNum();
            int pageSize = request.getPageSize();

            int from = (pageNum - 1) * pageSize;
            int to = Math.min(from + pageSize, aggregatedIds.size());

            if (from >= aggregatedIds.size()) {
                DsaResponse<T> response = buildSuccessResponse(null, total, pageNum, pageSize, traceId, startTime);
                return response;
            }

            List<String> pageIds = aggregatedIds.subList(from, to);
            List<?> details = aggregateService.queryDetails(pageIds, config.getDbTable(), config.getPrimaryKeyField(), clazz);

            DsaResponse<T> response = buildSuccessResponse((T) details, total, pageNum, pageSize, traceId, startTime);
            return response;

        } catch (DsaException e) {
            log.error("Route request error, traceId: {}", traceId, e);
            return DsaResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Route request unexpected error, traceId: {}", traceId, e);
            return DsaResponse.error(DsaResultCode.UNKNOWN_ERROR);
        }
    }

    /**
     * 构建成功响应.
     *
     * @param data      数据
     * @param total     总记录数
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @param traceId   链路追踪ID
     * @param startTime 开始时间
     * @param <T>       数据类型
     * @return 成功响应
     */
    private <T> DsaResponse<T> buildSuccessResponse(T data, long total, int pageNum, int pageSize, String traceId, long startTime) {
        DsaResponse<T> response = new DsaResponse<>();
        response.setCode("200");
        response.setMessage("success");
        response.setData(data);
        response.setPageInfo(DsaPageInfo.of(total, pageNum, pageSize));
        response.setTraceId(traceId);
        response.setFromFallback(false);
        response.setCostMillis(System.currentTimeMillis() - startTime);
        return response;
    }

    @Override
    public DsaSceneConfig getSceneConfig(String sceneCode) {
        return sceneConfigCache.get(sceneCode);
    }

    @Override
    public void refreshSceneConfig(String sceneCode) {
        log.info("Refresh scene config: {}", sceneCode);
    }

    /**
     * 注册场景配置.
     *
     * @param sceneCode 场景编码
     * @param config    场景配置
     */
    public void registerSceneConfig(String sceneCode, DsaSceneConfig config) {
        sceneConfigCache.put(sceneCode, config);
        log.info("Registered scene config: {}", sceneCode);
    }
}
