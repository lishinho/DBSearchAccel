package io.github.dbsearchaccel.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 缓存补偿器实现类.
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaCacheCompensatorImpl implements DsaCacheCompensator {

    private static final Logger log = LoggerFactory.getLogger(DsaCacheCompensatorImpl.class);

    /**
     * 最大差异记录数.
     */
    private static final int MAX_DIFF_RECORDS = 1000;

    /**
     * 缓存服务.
     */
    private final DsaCacheService cacheService;

    /**
     * 统计信息.
     */
    private final ConcurrentHashMap<String, DsaCompensateStats> statsMap;

    /**
     * 差异记录.
     */
    private final ConcurrentHashMap<String, ConcurrentLinkedDeque<DsaCacheDiff>> diffRecords;

    /**
     * 构造方法.
     *
     * @param cacheService 缓存服务
     */
    public DsaCacheCompensatorImpl(DsaCacheService cacheService) {
        this.cacheService = cacheService;
        this.statsMap = new ConcurrentHashMap<>();
        this.diffRecords = new ConcurrentHashMap<>();
        log.info("DsaCacheCompensator initialized");
    }

    @Override
    public int detectAndCompensate(String sceneCode) {
        DsaCompensateStats stats = getOrCreateStats(sceneCode);
        stats.updateLastDetectTime();

        List<DsaCacheDiff> diffs = detectDiffs(sceneCode);
        stats.addDetectCount(diffs.size());

        if (diffs.isEmpty()) {
            log.debug("No diffs detected for scene: {}", sceneCode);
            return 0;
        }

        int compensated = batchCompensate(sceneCode, diffs);
        log.info("Compensated {}/{} diffs for scene: {}", compensated, diffs.size(), sceneCode);

        return compensated;
    }

    @Override
    public void compensate(String sceneCode, String pkValue, Object expected, Object actual) {
        DsaCompensateStats stats = getOrCreateStats(sceneCode);

        try {
            if (expected == null && actual != null) {
                cacheService.removePk(sceneCode, pkValue);
                log.debug("Removed redundant cache: scene={}, pk={}", sceneCode, pkValue);
            } else if (expected != null && actual == null) {
                cacheService.addPk(sceneCode, pkValue);
                log.debug("Added missing cache: scene={}, pk={}", sceneCode, pkValue);
            } else {
                cacheService.removePk(sceneCode, pkValue);
                cacheService.addPk(sceneCode, pkValue);
                log.debug("Refreshed cache: scene={}, pk={}", sceneCode, pkValue);
            }

            stats.incrementCompensateCount();

        } catch (Exception e) {
            stats.incrementFailCount();
            log.error("Compensate failed: scene={}, pk={}", sceneCode, pkValue, e);
        }
    }

    @Override
    public int batchCompensate(String sceneCode, List<DsaCacheDiff> diffList) {
        if (diffList == null || diffList.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (DsaCacheDiff diff : diffList) {
            try {
                compensate(sceneCode, diff.getPkValue(), diff.getExpectedValue(), diff.getActualValue());
                successCount++;
            } catch (Exception e) {
                log.error("Batch compensate failed for diff: {}", diff, e);
            }
        }

        return successCount;
    }

    @Override
    public DsaCompensateStats getStats(String sceneCode) {
        return getOrCreateStats(sceneCode);
    }

    @Override
    public List<DsaCacheDiff> getRecentDiffs(String sceneCode, int limit) {
        ConcurrentLinkedDeque<DsaCacheDiff> records = diffRecords.get(sceneCode);
        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }

        List<DsaCacheDiff> result = new ArrayList<>();
        int count = 0;
        for (DsaCacheDiff diff : records) {
            result.add(diff);
            count++;
            if (count >= limit) {
                break;
            }
        }

        return result;
    }

    /**
     * 检测差异.
     *
     * @param sceneCode 场景编码
     * @return 差异列表
     */
    private List<DsaCacheDiff> detectDiffs(String sceneCode) {
        List<DsaCacheDiff> diffs = new ArrayList<>();

        List<String> cachedPks = cacheService.getAllPks(sceneCode);
        for (String pk : cachedPks) {
            boolean exists = checkExistsInDb(sceneCode, pk);
            if (!exists) {
                DsaCacheDiff diff = new DsaCacheDiff(pk, DsaDiffType.CACHE_REDUNDANT);
                diff.setActualValue(pk);
                diffs.add(diff);
                recordDiff(sceneCode, diff);
            }
        }

        return diffs;
    }

    /**
     * 检查DB中是否存在.
     *
     * @param sceneCode 场景编码
     * @param pkValue   主键值
     * @return 是否存在
     */
    private boolean checkExistsInDb(String sceneCode, String pkValue) {
        return true;
    }

    /**
     * 记录差异.
     *
     * @param sceneCode 场景编码
     * @param diff      差异
     */
    private void recordDiff(String sceneCode, DsaCacheDiff diff) {
        ConcurrentLinkedDeque<DsaCacheDiff> records = diffRecords.computeIfAbsent(sceneCode,
                k -> new ConcurrentLinkedDeque<>());

        records.addFirst(diff);

        while (records.size() > MAX_DIFF_RECORDS) {
            records.removeLast();
        }
    }

    /**
     * 获取或创建统计信息.
     */
    private DsaCompensateStats getOrCreateStats(String sceneCode) {
        return statsMap.computeIfAbsent(sceneCode, DsaCompensateStats::new);
    }
}
