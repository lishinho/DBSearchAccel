package io.github.dbsearchaccel.infra.redis;

import io.github.dbsearchaccel.common.core.constant.DsaConstants;
import io.github.dbsearchaccel.common.core.exception.DsaRedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis客户端实现类.
 * <p>
 * 基于Spring Data Redis实现，主要用于实时主键缓存.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaRedisClientImpl implements DsaRedisClient {

    private static final Logger log = LoggerFactory.getLogger(DsaRedisClientImpl.class);

    /**
     * StringRedis模板.
     */
    private final StringRedisTemplate redisTemplate;

    /**
     * 缓存最大主键数.
     */
    private final int maxCount;

    /**
     * 默认过期时间（秒）.
     */
    private final int defaultExpireSeconds;

    /**
     * 构造方法.
     *
     * @param redisTemplate        StringRedis模板
     * @param maxCount             缓存最大主键数
     * @param defaultExpireSeconds 默认过期时间（秒）
     */
    public DsaRedisClientImpl(StringRedisTemplate redisTemplate, int maxCount, int defaultExpireSeconds) {
        this.redisTemplate = redisTemplate;
        this.maxCount = maxCount;
        this.defaultExpireSeconds = defaultExpireSeconds;
    }

    /**
     * 构建Redis Key.
     *
     * @param sceneType 场景类型
     * @return Redis Key
     */
    private String buildKey(String sceneType) {
        return DsaConstants.DSA_REDIS_KEY_PREFIX + sceneType;
    }

    @Override
    public void addId(String sceneType, String id) {
        try {
            String key = buildKey(sceneType);
            Long size = redisTemplate.opsForList().size(key);
            if (size != null && size >= maxCount) {
                redisTemplate.opsForList().rightPop(key);
            }
            redisTemplate.opsForList().leftPush(key, id);
            redisTemplate.expire(key, defaultExpireSeconds, TimeUnit.SECONDS);
            log.debug("Redis add id success, sceneType: {}, id: {}", sceneType, id);
        } catch (Exception e) {
            log.error("Redis add id error, sceneType: {}, id: {}", sceneType, id, e);
            throw new DsaRedisException("Redis add id failed", e);
        }
    }

    @Override
    public void addIds(String sceneType, List<String> ids) {
        try {
            String key = buildKey(sceneType);
            for (String id : ids) {
                Long size = redisTemplate.opsForList().size(key);
                if (size != null && size >= maxCount) {
                    redisTemplate.opsForList().rightPop(key);
                }
                redisTemplate.opsForList().leftPush(key, id);
            }
            redisTemplate.expire(key, defaultExpireSeconds, TimeUnit.SECONDS);
            log.debug("Redis add ids success, sceneType: {}, count: {}", sceneType, ids.size());
        } catch (Exception e) {
            log.error("Redis add ids error, sceneType: {}", sceneType, e);
            throw new DsaRedisException("Redis add ids failed", e);
        }
    }

    @Override
    public void removeId(String sceneType, String id) {
        try {
            String key = buildKey(sceneType);
            redisTemplate.opsForList().remove(key, 1, id);
            log.debug("Redis remove id success, sceneType: {}, id: {}", sceneType, id);
        } catch (Exception e) {
            log.error("Redis remove id error, sceneType: {}, id: {}", sceneType, id, e);
            throw new DsaRedisException("Redis remove id failed", e);
        }
    }

    @Override
    public void removeIds(String sceneType, List<String> ids) {
        try {
            String key = buildKey(sceneType);
            for (String id : ids) {
                redisTemplate.opsForList().remove(key, 1, id);
            }
            log.debug("Redis remove ids success, sceneType: {}, count: {}", sceneType, ids.size());
        } catch (Exception e) {
            log.error("Redis remove ids error, sceneType: {}", sceneType, e);
            throw new DsaRedisException("Redis remove ids failed", e);
        }
    }

    @Override
    public List<String> getIds(String sceneType) {
        try {
            String key = buildKey(sceneType);
            List<String> ids = redisTemplate.opsForList().range(key, 0, -1);
            return ids != null ? ids : new ArrayList<>();
        } catch (Exception e) {
            log.error("Redis get ids error, sceneType: {}", sceneType, e);
            throw new DsaRedisException("Redis get ids failed", e);
        }
    }

    @Override
    public void clearIds(String sceneType) {
        try {
            String key = buildKey(sceneType);
            redisTemplate.delete(key);
            log.debug("Redis clear ids success, sceneType: {}", sceneType);
        } catch (Exception e) {
            log.error("Redis clear ids error, sceneType: {}", sceneType, e);
            throw new DsaRedisException("Redis clear ids failed", e);
        }
    }

    @Override
    public long countIds(String sceneType) {
        try {
            String key = buildKey(sceneType);
            Long size = redisTemplate.opsForList().size(key);
            return size != null ? size : 0;
        } catch (Exception e) {
            log.error("Redis count ids error, sceneType: {}", sceneType, e);
            throw new DsaRedisException("Redis count ids failed", e);
        }
    }

    @Override
    public void expire(String sceneType, long seconds) {
        try {
            String key = buildKey(sceneType);
            redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Redis expire error, sceneType: {}", sceneType, e);
            throw new DsaRedisException("Redis expire failed", e);
        }
    }

    @Override
    public void set(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Redis set error, key: {}", key, e);
            throw new DsaRedisException("Redis set failed", e);
        }
    }

    @Override
    public String get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis get error, key: {}", key, e);
            throw new DsaRedisException("Redis get failed", e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Redis delete error, key: {}", key, e);
            throw new DsaRedisException("Redis delete failed", e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("Redis exists error, key: {}", key, e);
            throw new DsaRedisException("Redis exists check failed", e);
        }
    }
}
