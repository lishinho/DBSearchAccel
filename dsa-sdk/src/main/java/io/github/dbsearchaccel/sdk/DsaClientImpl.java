package io.github.dbsearchaccel.sdk;

import io.github.dbsearchaccel.common.model.request.DsaRequest;
import io.github.dbsearchaccel.common.model.response.DsaResponse;
import io.github.dbsearchaccel.core.route.DsaRouteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DsaClientImpl implements DsaClient {

    private static final Logger log = LoggerFactory.getLogger(DsaClientImpl.class);

    private final DsaRouteService routeService;

    public DsaClientImpl(DsaRouteService routeService) {
        this.routeService = routeService;
    }

    @Override
    public <T> DsaResponse<T> query(DsaRequest request, Class<T> clazz) {
        if (request.getTraceId() == null || request.getTraceId().isEmpty()) {
            request.setTraceId(UUID.randomUUID().toString());
        }

        log.debug("DSA client query, traceId: {}, scene: {}", request.getTraceId(), request.getSceneTypeCode());
        return routeService.route(request, clazz);
    }

    @Override
    public DsaResponse<Object> query(DsaRequest request) {
        return query(request, Object.class);
    }
}
