package io.github.dbsearchaccel.cache;

import io.github.dbsearchaccel.infra.redis.DsaRedisClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存模块配置类.
 * <p>
 * 自动配置缓存服务、切面、主键解析器等组件.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Configuration
public class DsaCacheConfig {

    /**
     * 配置缓存服务.
     *
     * @param redisClient Redis客户端
     * @return 缓存服务实例
     */
    @Bean
    @ConditionalOnBean(DsaRedisClient.class)
    @ConditionalOnMissingBean(DsaCacheService.class)
    public DsaCacheService dsaCacheService(DsaRedisClient redisClient) {
        return new DsaCacheServiceImpl(redisClient);
    }

    /**
     * 配置主键解析器.
     *
     * @return 主键解析器实例
     */
    @Bean
    @ConditionalOnMissingBean(DsaPkResolver.class)
    public DsaPkResolver dsaPkResolver() {
        return new DsaPkResolverImpl();
    }

    /**
     * 配置缓存切面.
     *
     * @param cacheService 缓存服务
     * @return 缓存切面实例
     */
    @Bean
    @ConditionalOnBean(DsaCacheService.class)
    @ConditionalOnMissingBean(DsaCacheAspect.class)
    public DsaCacheAspect dsaCacheAspect(DsaCacheService cacheService) {
        return new DsaCacheAspect(cacheService);
    }
}
