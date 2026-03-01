package io.github.dbsearchaccel.cache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 向量时钟值.
 * <p>
 * 用于解决分布式环境下的数据版本冲突.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaVectorClockValue implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 节点ID -> 计数器映射.
     */
    private final Map<String, Long> clocks;

    /**
     * 时间戳（用于冲突解决兜底）.
     */
    private final long timestamp;

    /**
     * 构造方法.
     */
    public DsaVectorClockValue() {
        this.clocks = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 构造方法.
     *
     * @param clocks 时钟映射
     */
    public DsaVectorClockValue(Map<String, Long> clocks) {
        this.clocks = clocks != null ? new HashMap<>(clocks) : new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 获取指定节点的时钟值.
     *
     * @param nodeId 节点ID
     * @return 时钟值
     */
    public long getClock(String nodeId) {
        return clocks.getOrDefault(nodeId, 0L);
    }

    /**
     * 设置指定节点的时钟值.
     *
     * @param nodeId 节点ID
     * @param value  时钟值
     */
    public void setClock(String nodeId, long value) {
        clocks.put(nodeId, value);
    }

    /**
     * 获取所有时钟.
     *
     * @return 时钟映射
     */
    public Map<String, Long> getClocks() {
        return new HashMap<>(clocks);
    }

    /**
     * 获取时间戳.
     *
     * @return 时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 获取节点数量.
     *
     * @return 节点数量
     */
    public int getNodeCount() {
        return clocks.size();
    }

    @Override
    public String toString() {
        return "DsaVectorClockValue{" +
                "clocks=" + clocks +
                ", timestamp=" + timestamp +
                '}';
    }
}
