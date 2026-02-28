package io.github.dbsearchaccel.core.filter;

import io.github.dbsearchaccel.common.model.config.DsaSceneConfig;
import io.github.dbsearchaccel.common.model.request.DsaRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 过滤器链.
 * <p>
 * 实现责任链模式，按顺序执行多个灰度过滤器.
 * 支持动态添加过滤器，按order值排序执行.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaFilterChain {

    private static final Logger log = LoggerFactory.getLogger(DsaFilterChain.class);

    private final List<DsaGrayFilter> filters = new ArrayList<>();

    /**
     * 添加过滤器.
     *
     * @param filter 过滤器实例
     * @return 当前过滤器链
     */
    public DsaFilterChain addFilter(DsaGrayFilter filter) {
        if (filter != null) {
            filters.add(filter);
            filters.sort(Comparator.comparingInt(DsaGrayFilter::getOrder));
            log.debug("Added filter [{}] with order [{}]", filter.getName(), filter.getOrder());
        }
        return this;
    }

    /**
     * 执行过滤器链.
     * <p>
     * 按order值从小到大依次执行过滤器，任一过滤器返回false则终止执行.
     * </p>
     *
     * @param request 请求参数
     * @param config  场景配置
     * @return true表示所有过滤器都通过，false表示被拦截
     */
    public boolean doFilter(DsaRequest request, DsaSceneConfig config) {
        log.debug("Start filter chain, filter count: {}", filters.size());

        for (DsaGrayFilter filter : filters) {
            long startTime = System.currentTimeMillis();
            boolean allowed = filter.allowAccess(request, config);
            long cost = System.currentTimeMillis() - startTime;

            log.debug("Filter [{}] executed, result: {}, cost: {}ms",
                    filter.getName(), allowed, cost);

            if (!allowed) {
                log.info("Request blocked by filter [{}], grayKey: [{}]",
                        filter.getName(), request.getGrayKey());
                return false;
            }
        }

        log.debug("Filter chain passed, all filters allowed");
        return true;
    }

    /**
     * 清空过滤器链.
     */
    public void clear() {
        filters.clear();
        log.debug("Filter chain cleared");
    }

    /**
     * 获取过滤器数量.
     *
     * @return 过滤器数量
     */
    public int size() {
        return filters.size();
    }

    /**
     * 判断过滤器链是否为空.
     *
     * @return true表示为空
     */
    public boolean isEmpty() {
        return filters.isEmpty();
    }
}
