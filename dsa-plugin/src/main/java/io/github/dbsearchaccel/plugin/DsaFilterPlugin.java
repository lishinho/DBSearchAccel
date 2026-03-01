package io.github.dbsearchaccel.plugin;

import io.github.dbsearchaccel.common.model.request.DsaRequest;

/**
 * 过滤插件接口.
 * <p>
 * 用于查询过滤扩展，可在查询前后执行自定义逻辑.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaFilterPlugin extends DsaPlugin<DsaFilterPlugin.FilterContext, DsaPluginResult> {

    /**
     * 过滤插件上下文.
     */
    class FilterContext extends DsaPluginContext {

        private static final long serialVersionUID = 1L;

        /**
         * 查询请求
         */
        private DsaRequest request;

        /**
         * 过滤阶段
         */
        private FilterPhase phase;

        public FilterContext() {
            super();
        }

        public FilterContext(String sceneCode, DsaRequest request, FilterPhase phase) {
            super(sceneCode, "FILTER");
            this.request = request;
            this.phase = phase;
        }

        public DsaRequest getRequest() {
            return request;
        }

        public void setRequest(DsaRequest request) {
            this.request = request;
        }

        public FilterPhase getPhase() {
            return phase;
        }

        public void setPhase(FilterPhase phase) {
            this.phase = phase;
        }
    }

    /**
     * 过滤阶段枚举.
     */
    enum FilterPhase {
        /**
         * 前置过滤
         */
        PRE_FILTER,
        /**
         * 后置过滤
         */
        POST_FILTER
    }

    /**
     * 获取过滤阶段.
     *
     * @return 过滤阶段
     */
    default FilterPhase getPhase() {
        return FilterPhase.PRE_FILTER;
    }
}
