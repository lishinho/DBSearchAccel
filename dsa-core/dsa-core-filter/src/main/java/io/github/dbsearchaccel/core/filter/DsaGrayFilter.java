package io.github.dbsearchaccel.core.filter;

import io.github.dbsearchaccel.common.model.config.DsaSceneConfig;
import io.github.dbsearchaccel.common.model.request.DsaRequest;

/**
 * 灰度过滤器接口.
 * <p>
 * 提供灰度发布场景下的流量控制能力，支持白名单/黑名单校验.
 * 可用于新功能灰度发布、A/B测试等场景.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaGrayFilter {

    /**
     * 检查请求是否允许通过灰度过滤器.
     * <p>
     * 实现类应根据业务需求定义灰度规则，如：
     * - 白名单模式：仅白名单内的grayKey允许通过
     * - 黑名单模式：黑名单内的grayKey禁止通过
     * - 百分比模式：按比例放行流量
     * </p>
     *
     * @param request 请求参数，包含grayKey等灰度标识
     * @param config  场景配置，包含灰度规则配置
     * @return true表示允许通过，false表示禁止通过
     */
    boolean allowAccess(DsaRequest request, DsaSceneConfig config);

    /**
     * 获取过滤器名称.
     *
     * @return 过滤器名称
     */
    String getName();

    /**
     * 获取过滤器顺序.
     * <p>
     * 数值越小优先级越高
     * </p>
     *
     * @return 顺序值
     */
    int getOrder();
}
