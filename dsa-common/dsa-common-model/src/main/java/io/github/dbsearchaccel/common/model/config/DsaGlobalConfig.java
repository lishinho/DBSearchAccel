package io.github.dbsearchaccel.common.model.config;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 全局配置实体.
 * <p>
 * 封装中间件的全局配置信息，包括ES、Redis、数据库连接配置以及监控、降级配置.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Data
public class DsaGlobalConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ES地址.
     * <p>
     * 格式：host:port，多个用逗号分隔
     * </p>
     */
    private String esAddr;

    /**
     * ES主机列表.
     */
    private List<String> esHosts;

    /**
     * ES用户名.
     */
    private String esUsername;

    /**
     * ES密码.
     */
    private String esPassword;

    /**
     * Redis地址.
     * <p>
     * 格式：host:port
     * </p>
     */
    private String redisAddr;

    /**
     * Redis主机.
     */
    private String redisHost;

    /**
     * Redis端口.
     * <p>
     * 默认值为6379
     * </p>
     */
    private int redisPort = 6379;

    /**
     * Redis密码.
     */
    private String redisPassword;

    /**
     * Redis数据库索引.
     * <p>
     * 默认值为0
     * </p>
     */
    private int redisDatabase = 0;

    /**
     * Redis缓存过期时间（秒）.
     * <p>
     * 默认值为600秒（10分钟）
     * </p>
     */
    private int redisExpireSeconds = 600;

    /**
     * Redis缓存最大主键数.
     * <p>
     * 默认值为10000
     * </p>
     */
    private int redisMaxCount = 10000;

    /**
     * 是否启用监控.
     * <p>
     * 默认值为true
     * </p>
     */
    private boolean monitorEnabled = true;

    /**
     * 是否启用告警.
     * <p>
     * 默认值为true
     * </p>
     */
    private boolean alertEnabled = true;

    /**
     * 是否启用降级.
     * <p>
     * 默认值为true
     * </p>
     */
    private boolean fallbackEnabled = true;

    /**
     * 慢调用阈值（毫秒）.
     * <p>
     * 默认值为500毫秒
     * </p>
     */
    private int slowCallThresholdMs = 500;

    /**
     * 异常比例阈值.
     * <p>
     * 默认值为0.5（50%）
     * </p>
     */
    private double errorRatioThreshold = 0.5;

    /**
     * 熔断器超时时间（毫秒）.
     * <p>
     * 默认值为10000毫秒（10秒）
     * </p>
     */
    private int circuitBreakerTimeoutMs = 10000;
}
