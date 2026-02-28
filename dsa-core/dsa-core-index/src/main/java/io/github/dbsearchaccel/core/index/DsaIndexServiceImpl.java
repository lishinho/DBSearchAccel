package io.github.dbsearchaccel.core.index;

import io.github.dbsearchaccel.common.model.dsl.DsaDsl;
import io.github.dbsearchaccel.infra.es.DsaEsClient;
import io.github.dbsearchaccel.infra.redis.DsaRedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 索引服务实现类.
 * <p>
 * 封装ES和Redis索引操作，用于查询主键ID和管理实时缓存.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaIndexServiceImpl implements DsaIndexService {

    private static final Logger log = LoggerFactory.getLogger(DsaIndexServiceImpl.class);

    /**
     * ES客户端.
     */
    private final DsaEsClient esClient;

    /**
     * Redis客户端.
     */
    private final DsaRedisClient redisClient;

    /**
     * 构造方法.
     *
     * @param esClient    ES客户端
     * @param redisClient Redis客户端
     */
    public DsaIndexServiceImpl(DsaEsClient esClient, DsaRedisClient redisClient) {
        this.esClient = esClient;
        this.redisClient = redisClient;
    }

    @Override
    public List<String> queryEsIds(DsaDsl dsl) {
        log.debug("Query ES ids, index: {}", dsl.getIndex());
        return esClient.queryIds(dsl);
    }

    @Override
    public List<String> queryRedisIds(String sceneType) {
        log.debug("Query Redis ids, sceneType: {}", sceneType);
        return redisClient.getIds(sceneType);
    }

    @Override
    public void addToRedisCache(String sceneType, String id) {
        log.debug("Add to Redis cache, sceneType: {}, id: {}", sceneType, id);
        redisClient.addId(sceneType, id);
    }

    @Override
    public void addToRedisCache(String sceneType, List<String> ids) {
        log.debug("Add to Redis cache, sceneType: {}, count: {}", sceneType, ids.size());
        redisClient.addIds(sceneType, ids);
    }

    @Override
    public void removeFromRedisCache(String sceneType, String id) {
        log.debug("Remove from Redis cache, sceneType: {}, id: {}", sceneType, id);
        redisClient.removeId(sceneType, id);
    }

    @Override
    public void removeFromRedisCache(String sceneType, List<String> ids) {
        log.debug("Remove from Redis cache, sceneType: {}, count: {}", sceneType, ids.size());
        redisClient.removeIds(sceneType, ids);
    }

    @Override
    public void clearRedisCache(String sceneType) {
        log.debug("Clear Redis cache, sceneType: {}", sceneType);
        redisClient.clearIds(sceneType);
    }
}
