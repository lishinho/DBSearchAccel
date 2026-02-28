package io.github.dbsearchaccel.core.filter;

import io.github.dbsearchaccel.common.model.config.DsaSceneConfig;
import io.github.dbsearchaccel.common.model.dsl.DsaDsl;
import io.github.dbsearchaccel.common.model.request.DsaRequest;

import java.util.Map;

/**
 * 过滤规则服务接口.
 * <p>
 * 提供请求校验、灰度过滤、DSL构建等核心过滤能力.
 * 是查询加速流程中的关键环节.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaFilterService {

    /**
     * 检查灰度访问权限.
     * <p>
     * 根据灰度规则判断请求是否允许通过.
     * 支持白名单/黑名单模式.
     * </p>
     *
     * @param request 请求参数，包含grayKey灰度标识
     * @param config  场景配置，包含灰度规则
     * @return true表示允许访问，false表示禁止访问
     */
    boolean checkGrayAccess(DsaRequest request, DsaSceneConfig config);

    /**
     * 构建ES查询DSL.
     * <p>
     * 将业务请求参数转换为Elasticsearch查询DSL.
     * </p>
     *
     * @param request 请求参数
     * @param config  场景配置
     * @return ES DSL对象
     */
    DsaDsl buildDsl(DsaRequest request, DsaSceneConfig config);

    /**
     * 校验请求参数.
     * <p>
     * 对请求参数进行合法性校验，包括必填项、格式、范围等.
     * </p>
     *
     * @param request 请求参数
     * @return true表示校验通过
     * @throws io.github.dbsearchaccel.common.core.exception.DsaException 校验失败时抛出
     */
    boolean validateRequest(DsaRequest request);

    /**
     * 构建ES查询条件Map.
     *
     * @param request 请求参数
     * @param config  场景配置
     * @return ES查询条件的Map表示
     */
    Map<String, Object> buildEsQuery(DsaRequest request, DsaSceneConfig config);
}
