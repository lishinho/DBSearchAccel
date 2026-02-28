package io.github.dbsearchaccel.sdk;

import io.github.dbsearchaccel.core.aggregate.DsaDataAggregateService;
import io.github.dbsearchaccel.core.aggregate.DsaDataAggregateServiceImpl;
import io.github.dbsearchaccel.core.filter.DsaFilterService;
import io.github.dbsearchaccel.core.filter.DsaFilterServiceImpl;
import io.github.dbsearchaccel.core.index.DsaIndexService;
import io.github.dbsearchaccel.core.index.DsaIndexServiceImpl;
import io.github.dbsearchaccel.core.route.DsaRouteService;
import io.github.dbsearchaccel.core.route.DsaRouteServiceImpl;
import io.github.dbsearchaccel.fallback.DsaFallbackService;
import io.github.dbsearchaccel.fallback.DsaFallbackServiceImpl;
import io.github.dbsearchaccel.infra.db.DsaDbAccessor;
import io.github.dbsearchaccel.infra.es.DsaEsClient;
import io.github.dbsearchaccel.infra.redis.DsaRedisClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DSA自动配置类.
 * <p>
 * Spring Boot Starter自动配置，自动注入所需Bean.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(DsaProperties.class)
public class DsaAutoConfiguration {

    /**
     * 创建索引服务Bean.
     *
     * @param esClient    ES客户端
     * @param redisClient Redis客户端
     * @return DsaIndexService实例
     */
    @Bean
    @ConditionalOnMissingBean
    public DsaIndexService dsaIndexService(DsaEsClient esClient, DsaRedisClient redisClient) {
        return new DsaIndexServiceImpl(esClient, redisClient);
    }

    /**
     * 创建数据聚合服务Bean.
     *
     * @param dbAccessor 数据库访问器
     * @return DsaDataAggregateService实例
     */
    @Bean
    @ConditionalOnMissingBean
    public DsaDataAggregateService dsaDataAggregateService(DsaDbAccessor dbAccessor) {
        return new DsaDataAggregateServiceImpl(dbAccessor);
    }

    /**
     * 创建过滤规则服务Bean.
     *
     * @return DsaFilterService实例
     */
    @Bean
    @ConditionalOnMissingBean
    public DsaFilterService dsaFilterService() {
        return new DsaFilterServiceImpl();
    }

    /**
     * 创建降级服务Bean.
     *
     * @param dbAccessor 数据库访问器
     * @return DsaFallbackService实例
     */
    @Bean
    @ConditionalOnMissingBean
    public DsaFallbackService dsaFallbackService(DsaDbAccessor dbAccessor) {
        return new DsaFallbackServiceImpl(dbAccessor);
    }

    /**
     * 创建路由服务Bean.
     *
     * @param filterService     过滤规则服务
     * @param indexService      索引服务
     * @param aggregateService  数据聚合服务
     * @param fallbackService   降级服务
     * @return DsaRouteService实例
     */
    @Bean
    @ConditionalOnMissingBean
    public DsaRouteService dsaRouteService(DsaFilterService filterService,
                                            DsaIndexService indexService,
                                            DsaDataAggregateService aggregateService,
                                            DsaFallbackService fallbackService) {
        return new DsaRouteServiceImpl(filterService, indexService, aggregateService, fallbackService);
    }

    /**
     * 创建DSA客户端Bean.
     *
     * @param routeService 路由服务
     * @return DsaClient实例
     */
    @Bean
    @ConditionalOnMissingBean
    public DsaClient dsaClient(DsaRouteService routeService) {
        return new DsaClientImpl(routeService);
    }
}
