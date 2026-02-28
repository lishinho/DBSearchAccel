package io.github.dbsearchaccel.cache;

import io.github.dbsearchaccel.infra.redis.DsaRedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 缓存服务实现类.
 * <p>
 * 基于Redis Set实现实时主键缓存，支持容量控制（FIFO淘汰）.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaCacheServiceImpl implements DsaCacheService {

    private static final Logger log = LoggerFactory.getLogger(DsaCacheServiceImpl.class);

    private static final int DEFAULT_MAX_COUNT = 10000;
    private static final int DEFAULT_EXPIRE_SECONDS = 600;

    private final DsaRedisClient redisClient;
    private int maxCount = DEFAULT_MAX_COUNT;
    private int defaultExpireSeconds = DEFAULT_EXPIRE_SECONDS;

    /**
     * 构造方法.
     *
     * @param redisClient Redis客户端
     */
    public DsaCacheServiceImpl(DsaRedisClient redisClient) {
        this.redisClient = redisClient;
    }

    @Override
    public boolean addPk(String sceneCode, String pkValue) {
        if (sceneCode == null || pkValue == null) {
            return false;
        }

        try {
            long currentCount = redisClient.countIds(sceneCode);
            if (currentCount >= maxCount) {
                log.warn("Cache is full, scene: [{}], count: {}, max: {}",
                        sceneCode, currentCount, maxCount);
            }

            redisClient.addId(sceneCode, pkValue);
            redisClient.expire(sceneCode, defaultExpireSeconds);

            log.debug("Added pk to cache, scene: [{}], pk: {}", sceneCode, pkValue);
            return true;

        } catch (Exception e) {
            log.error("Add pk to cache failed, scene: [{}], pk: {}", sceneCode, pkValue, e);
            return false;
        }
    }

    @Override
    public int addPkBatch(String sceneCode, List<String> pkValues) {
        if (sceneCode == null || pkValues == null || pkValues.isEmpty()) {
            return 0;
        }

        try {
            redisClient.addIds(sceneCode, pkValues);
            log.debug("Batch added pk to cache, scene: [{}], count: {}", sceneCode, pkValues.size());
            return pkValues.size();

        } catch (Exception e) {
            log.error("Batch add pk to cache failed, scene: [{}]", sceneCode, e);
            return 0;
        }
    }

    @Override
    public boolean removePk(String sceneCode, String pkValue) {
        if (sceneCode == null || pkValue == null) {
            return false;
        }

        try {
            redisClient.removeId(sceneCode, pkValue);
            log.debug("Removed pk from cache, scene: [{}], pk: {}", sceneCode, pkValue);
            return true;

        } catch (Exception e) {
            log.error("Remove pk from cache failed, scene: [{}], pk: {}", sceneCode, pkValue, e);
            return false;
        }
    }

    @Override
    public int removePkBatch(String sceneCode, List<String> pkValues) {
        if (sceneCode == null || pkValues == null || pkValues.isEmpty()) {
            return 0;
        }

        try {
            redisClient.removeIds(sceneCode, pkValues);
            log.debug("Batch removed pk from cache, scene: [{}], count: {}", sceneCode, pkValues.size());
            return pkValues.size();

        } catch (Exception e) {
            log.error("Batch remove pk from cache failed, scene: [{}]", sceneCode, e);
            return 0;
        }
    }

    @Override
    public List<String> getAllPks(String sceneCode) {
        if (sceneCode == null) {
            return new ArrayList<>();
        }

        try {
            List<String> ids = redisClient.getIds(sceneCode);
            return ids != null ? ids : new ArrayList<>();

        } catch (Exception e) {
            log.error("Get all pks from cache failed, scene: [{}]", sceneCode, e);
            return new ArrayList<>();
        }
    }

    @Override
    public long getPkCount(String sceneCode) {
        if (sceneCode == null) {
            return 0;
        }

        try {
            return redisClient.countIds(sceneCode);

        } catch (Exception e) {
            log.error("Get pk count from cache failed, scene: [{}]", sceneCode, e);
            return 0;
        }
    }

    @Override
    public boolean clear(String sceneCode) {
        if (sceneCode == null) {
            return false;
        }

        try {
            redisClient.clearIds(sceneCode);
            log.info("Cleared cache, scene: [{}]", sceneCode);
            return true;

        } catch (Exception e) {
            log.error("Clear cache failed, scene: [{}]", sceneCode, e);
            return false;
        }
    }

    @Override
    public boolean exists(String sceneCode, String pkValue) {
        if (sceneCode == null || pkValue == null) {
            return false;
        }

        try {
            List<String> ids = redisClient.getIds(sceneCode);
            return ids != null && ids.contains(pkValue);

        } catch (Exception e) {
            log.error("Check pk exists in cache failed, scene: [{}], pk: {}", sceneCode, pkValue, e);
            return false;
        }
    }

    @Override
    public boolean expire(String sceneCode, long seconds) {
        if (sceneCode == null) {
            return false;
        }

        try {
            redisClient.expire(sceneCode, seconds);
            return true;

        } catch (Exception e) {
            log.error("Set cache expire failed, scene: [{}]", sceneCode, e);
            return false;
        }
    }

    /**
     * 设置最大缓存数量.
     *
     * @param maxCount 最大缓存数量
     */
    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount > 0 ? maxCount : DEFAULT_MAX_COUNT;
    }

    /**
     * 设置默认过期时间.
     *
     * @param defaultExpireSeconds 默认过期时间（秒）
     */
    public void setDefaultExpireSeconds(int defaultExpireSeconds) {
        this.defaultExpireSeconds = defaultExpireSeconds > 0
                ? defaultExpireSeconds : DEFAULT_EXPIRE_SECONDS;
    }
}
