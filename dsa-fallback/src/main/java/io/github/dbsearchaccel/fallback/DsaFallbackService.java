package io.github.dbsearchaccel.fallback;

import io.github.dbsearchaccel.common.model.request.DsaRequest;
import io.github.dbsearchaccel.common.model.response.DsaResponse;

/**
 * 降级服务接口.
 * <p>
 * 负责处理降级逻辑，当ES/Redis不可用或触发熔断时，回退到数据库查询.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaFallbackService {

    /**
     * 执行降级查询.
     * <p>
     * 直接从数据库查询数据，绕过ES和Redis
     * </p>
     *
     * @param <T>     返回数据类型
     * @param request 请求参数
     * @param clazz   返回数据类型
     * @return 响应结果
     */
    <T> DsaResponse<T> fallback(DsaRequest request, Class<T> clazz);

    /**
     * 判断场景是否正在降级.
     *
     * @param sceneCode 场景编码
     * @return true表示正在降级
     */
    boolean isDegrading(String sceneCode);

    /**
     * 启用场景降级.
     *
     * @param sceneCode 场景编码
     */
    void enableDegrade(String sceneCode);

    /**
     * 禁用场景降级.
     *
     * @param sceneCode 场景编码
     */
    void disableDegrade(String sceneCode);

    /**
     * 判断熔断器是否打开.
     *
     * @param sceneCode 场景编码
     * @return true表示熔断器已打开
     */
    boolean isCircuitBreakerOpen(String sceneCode);
}
