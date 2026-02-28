package io.github.dbsearchaccel.infra.redis;

import io.github.dbsearchaccel.common.core.constant.DsaConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis配置类.
 * <p>
 * 配置Redis客户端连接信息.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Configuration
public class DsaRedisConfig {

    /**
     * 缓存最大主键数.
     */
    @Value("${dsa.redis.max-count:" + DsaConstants.DEFAULT_REDIS_MAX_COUNT + "}")
    private int maxCount;

    /**
     * 默认过期时间（秒）.
     */
    @Value("${dsa.redis.expire-seconds:" + DsaConstants.DEFAULT_REDIS_EXPIRE_SECONDS + "}")
    private int defaultExpireSeconds;

    /**
     * 创建StringRedis模板Bean.
     *
     * @param connectionFactory Redis连接工厂
     * @return StringRedisTemplate实例
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    /**
     * 创建DSA Redis客户端Bean.
     *
     * @param stringRedisTemplate StringRedis模板
     * @return DsaRedisClient实例
     */
    @Bean
    public DsaRedisClient dsaRedisClient(StringRedisTemplate stringRedisTemplate) {
        return new DsaRedisClientImpl(stringRedisTemplate, maxCount, defaultExpireSeconds);
    }
}
