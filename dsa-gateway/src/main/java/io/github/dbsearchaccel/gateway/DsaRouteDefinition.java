package io.github.dbsearchaccel.gateway;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 网关路由定义.
 * <p>
 * 定义网关的路由规则配置.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaRouteDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 路由ID
     */
    private String routeId;

    /**
     * 路由名称
     */
    private String routeName;

    /**
     * 路径模式
     */
    private String pathPattern;

    /**
     * 目标URI
     */
    private String targetUri;

    /**
     * 是否启用
     */
    private boolean enabled;

    /**
     * 限流配置
     */
    private DsaRateLimitConfig rateLimitConfig;

    /**
     * 过滤器列表
     */
    private List<String> filters;

    /**
     * 元数据
     */
    private List<String> metadata;

    public DsaRouteDefinition() {
        this.enabled = true;
        this.filters = new ArrayList<>();
        this.metadata = new ArrayList<>();
    }

    public DsaRouteDefinition(String routeId, String pathPattern, String targetUri) {
        this();
        this.routeId = routeId;
        this.pathPattern = pathPattern;
        this.targetUri = targetUri;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getPathPattern() {
        return pathPattern;
    }

    public void setPathPattern(String pathPattern) {
        this.pathPattern = pathPattern;
    }

    public String getTargetUri() {
        return targetUri;
    }

    public void setTargetUri(String targetUri) {
        this.targetUri = targetUri;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public DsaRateLimitConfig getRateLimitConfig() {
        return rateLimitConfig;
    }

    public void setRateLimitConfig(DsaRateLimitConfig rateLimitConfig) {
        this.rateLimitConfig = rateLimitConfig;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    public List<String> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<String> metadata) {
        this.metadata = metadata;
    }
}
