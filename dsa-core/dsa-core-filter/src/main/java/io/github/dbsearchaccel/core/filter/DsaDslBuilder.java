package io.github.dbsearchaccel.core.filter;

import io.github.dbsearchaccel.common.model.config.DsaSceneConfig;
import io.github.dbsearchaccel.common.model.dsl.DsaDsl;
import io.github.dbsearchaccel.common.model.request.DsaRequest;

import java.util.Map;

/**
 * ES DSL构建器接口.
 * <p>
 * 负责将业务请求参数转换为Elasticsearch查询DSL.
 * 支持多种查询类型的动态构建，包括term、range、bool等.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaDslBuilder {

    /**
     * 构建ES查询DSL.
     *
     * @param request 请求参数
     * @param config  场景配置
     * @return ES DSL对象
     */
    DsaDsl build(DsaRequest request, DsaSceneConfig config);

    /**
     * 构建ES查询条件Map.
     *
     * @param request 请求参数
     * @param config  场景配置
     * @return ES查询条件的Map表示
     */
    Map<String, Object> buildQueryMap(DsaRequest request, DsaSceneConfig config);

    /**
     * 添加term查询条件.
     *
     * @param fieldName 字段名
     * @param value     字段值
     * @return 当前构建器实例
     */
    DsaDslBuilder addTerm(String fieldName, Object value);

    /**
     * 添加range查询条件.
     *
     * @param fieldName 字段名
     * @param operator  操作符（gt/gte/lt/lte）
     * @param value     字段值
     * @return 当前构建器实例
     */
    DsaDslBuilder addRange(String fieldName, String operator, Object value);

    /**
     * 添加exists查询条件.
     *
     * @param fieldName 字段名
     * @return 当前构建器实例
     */
    DsaDslBuilder addExists(String fieldName);

    /**
     * 添加wildcard模糊查询条件.
     *
     * @param fieldName 字段名
     * @param value     通配符值
     * @return 当前构建器实例
     */
    DsaDslBuilder addWildcard(String fieldName, String value);
}
