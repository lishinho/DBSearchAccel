package io.github.dbsearchaccel.core.route;

import io.github.dbsearchaccel.common.model.config.DsaSceneConfig;
import io.github.dbsearchaccel.common.model.request.DsaRequest;
import io.github.dbsearchaccel.common.model.response.DsaResponse;

public interface DsaRouteService {

    <T> DsaResponse<T> route(DsaRequest request, Class<T> clazz);

    DsaSceneConfig getSceneConfig(String sceneCode);

    void refreshSceneConfig(String sceneCode);
}
