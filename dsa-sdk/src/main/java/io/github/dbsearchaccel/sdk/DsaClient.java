package io.github.dbsearchaccel.sdk;

import io.github.dbsearchaccel.common.model.request.DsaRequest;
import io.github.dbsearchaccel.common.model.response.DsaResponse;

public interface DsaClient {

    <T> DsaResponse<T> query(DsaRequest request, Class<T> clazz);

    DsaResponse<Object> query(DsaRequest request);
}
