package io.github.dbsearchaccel.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 向量时钟实现类.
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaVectorClockImpl implements DsaVectorClock {

    private static final Logger log = LoggerFactory.getLogger(DsaVectorClockImpl.class);

    /**
     * 向量时钟存储.
     */
    private final ConcurrentHashMap<String, DsaVectorClockValue> clockStore;

    /**
     * 构造方法.
     */
    public DsaVectorClockImpl() {
        this.clockStore = new ConcurrentHashMap<>();
        log.info("DsaVectorClock initialized");
    }

    @Override
    public DsaVectorClockValue increment(String sceneCode, String pkValue, String nodeId) {
        String key = buildKey(sceneCode, pkValue);

        DsaVectorClockValue value = clockStore.compute(key, (k, v) -> {
            DsaVectorClockValue newValue = v != null ? new DsaVectorClockValue(v.getClocks()) : new DsaVectorClockValue();
            long currentClock = newValue.getClock(nodeId);
            newValue.setClock(nodeId, currentClock + 1);
            return newValue;
        });

        log.debug("Vector clock incremented: key={}, nodeId={}, clocks={}", key, nodeId, value.getClocks());
        return value;
    }

    @Override
    public DsaVectorClockValue merge(DsaVectorClockValue v1, DsaVectorClockValue v2) {
        if (v1 == null) {
            return v2 != null ? new DsaVectorClockValue(v2.getClocks()) : new DsaVectorClockValue();
        }
        if (v2 == null) {
            return new DsaVectorClockValue(v1.getClocks());
        }

        DsaVectorClockValue result = new DsaVectorClockValue(v1.getClocks());
        for (Map.Entry<String, Long> entry : v2.getClocks().entrySet()) {
            long maxClock = Math.max(result.getClock(entry.getKey()), entry.getValue());
            result.setClock(entry.getKey(), maxClock);
        }

        log.debug("Vector clock merged: v1={}, v2={}, result={}", v1.getClocks(), v2.getClocks(), result.getClocks());
        return result;
    }

    @Override
    public DsaClockCompareResult compare(DsaVectorClockValue v1, DsaVectorClockValue v2) {
        if (v1 == null && v2 == null) {
            return DsaClockCompareResult.EQUAL;
        }
        if (v1 == null) {
            return DsaClockCompareResult.BEFORE;
        }
        if (v2 == null) {
            return DsaClockCompareResult.AFTER;
        }

        Map<String, Long> clocks1 = v1.getClocks();
        Map<String, Long> clocks2 = v2.getClocks();

        Set<String> allNodes = new HashSet<>();
        allNodes.addAll(clocks1.keySet());
        allNodes.addAll(clocks2.keySet());

        boolean v1Greater = false;
        boolean v2Greater = false;

        for (String node : allNodes) {
            long c1 = clocks1.getOrDefault(node, 0L);
            long c2 = clocks2.getOrDefault(node, 0L);

            if (c1 > c2) {
                v1Greater = true;
            } else if (c2 > c1) {
                v2Greater = true;
            }
        }

        if (v1Greater && v2Greater) {
            return DsaClockCompareResult.CONCURRENT;
        }
        if (v1Greater) {
            return DsaClockCompareResult.AFTER;
        }
        if (v2Greater) {
            return DsaClockCompareResult.BEFORE;
        }
        return DsaClockCompareResult.EQUAL;
    }

    @Override
    public DsaVectorClockValue get(String sceneCode, String pkValue) {
        String key = buildKey(sceneCode, pkValue);
        return clockStore.get(key);
    }

    @Override
    public void set(String sceneCode, String pkValue, DsaVectorClockValue value) {
        String key = buildKey(sceneCode, pkValue);
        if (value != null) {
            clockStore.put(key, value);
            log.debug("Vector clock set: key={}, clocks={}", key, value.getClocks());
        } else {
            clockStore.remove(key);
            log.debug("Vector clock removed: key={}", key);
        }
    }

    @Override
    public DsaVectorClockValue resolveConflict(DsaVectorClockValue v1, DsaVectorClockValue v2) {
        DsaClockCompareResult result = compare(v1, v2);

        switch (result) {
            case AFTER:
                return v1;
            case BEFORE:
                return v2;
            case EQUAL:
                return v1;
            case CONCURRENT:
            default:
                if (v1.getTimestamp() >= v2.getTimestamp()) {
                    return v1;
                }
                return v2;
        }
    }

    /**
     * 构建键.
     */
    private String buildKey(String sceneCode, String pkValue) {
        return sceneCode + ":" + pkValue;
    }

    /**
     * 清空场景的向量时钟.
     *
     * @param sceneCode 场景编码
     */
    public void clear(String sceneCode) {
        String prefix = sceneCode + ":";
        clockStore.keySet().removeIf(key -> key.startsWith(prefix));
        log.info("Vector clock cleared for scene: {}", sceneCode);
    }

    /**
     * 获取存储数量.
     */
    public int size() {
        return clockStore.size();
    }
}
