package io.github.dbsearchaccel.core.filter;

import io.github.dbsearchaccel.common.model.config.DsaSceneConfig;
import io.github.dbsearchaccel.common.model.dsl.DsaDsl;
import io.github.dbsearchaccel.common.model.request.DsaRequest;

import java.util.Map;

public interface DsaFilterService {

    boolean checkGrayAccess(DsaRequest request, DsaSceneConfig config);

    DsaDsl buildDsl(DsaRequest request, DsaSceneConfig config);

    boolean validateRequest(DsaRequest request);

    Map<String, Object> buildEsQuery(DsaRequest request, DsaSceneConfig config);
}
