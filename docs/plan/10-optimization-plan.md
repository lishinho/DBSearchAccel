# DBSearchAccel 性能优化改造方案

## 一、现状分析

### 1.1 当前架构瓶颈

#### 索引层瓶颈
| 问题 | 现状 | 影响 |
|------|------|------|
| ES查询延迟 | 每次查询需网络IO到ES集群 | 平均延迟10-50ms |
| 多条件交集 | ES bool查询需遍历倒排链 | 复杂度O(n)，亿级文档性能下降 |
| Redis缓存 | 简单Set结构存储主键ID | 无法支持高效位运算 |

#### 缓存层瓶颈
| 问题 | 现状 | 影响 |
|------|------|------|
| 缓存一致性 | 基于Canal增量同步，存在延迟 | 数据不一致窗口期 |
| 无效查询 | 缺乏预判机制 | 大量无效主键穿透到DB |
| TTL策略 | 固定过期时间600秒 | 无法适应不同数据变更频率 |

### 1.2 现有模块依赖关系

```
查询请求 → DsaRouteService → DsaDslBuilder → ES查询主键ID
                                    ↓
                              DsaIndexService
                                    ↓
                         ES IDs + Redis IDs → DsaDataAggregateService
                                                    ↓
                                              DsaDbAccessor → 返回详情数据
```

---

## 二、改造目标

### 2.1 性能目标

| 指标 | 当前值 | 目标值 | 提升比例 |
|------|--------|--------|----------|
| 多条件查询延迟 | 50-200ms | <10ms | 80%+ |
| 缓存命中率 | ~75% | >92% | 22%+ |
| 无效查询比例 | ~15% | <3% | 80%+ |
| 亿级文档交集计算 | ~500ms | <50ms | 90%+ |

### 2.2 功能目标

1. **倒排索引加速**：引入Roaring Bitmap实现O(1)复杂度的多条件交并集
2. **分层存储**：内存热数据 + SSD温数据 + ES冷数据三级架构
3. **缓存补偿**：动态TTL + 布隆过滤器 + 向量时钟三重保障

---

## 三、详细设计方案

### 3.1 Roaring Bitmap 倒排索引加速模块

#### 3.1.1 新增模块结构

```
dsa-core/
└── dsa-core-bitmap/                    # 新增：位图索引模块
    ├── DsaBitmapIndexService.java      # 位图索引服务接口
    ├── DsaBitmapIndexServiceImpl.java  # 位图索引服务实现
    ├── DsaBitmapBuilder.java           # 位图构建器
    ├── DsaBitmapMerger.java            # 位图合并器（交并差运算）
    ├── DsaBitmapPartitioner.java       # 位图分区器
    └── config/
        └── DsaBitmapConfig.java        # 位图配置
```

#### 3.1.2 核心接口设计

```java
/**
 * 位图索引服务接口.
 * <p>
 * 基于Roaring Bitmap实现高性能倒排索引，支持O(1)复杂度的多条件交并集运算.
 * </p>
 */
public interface DsaBitmapIndexService {

    /**
     * 构建字段值到位图的映射.
     *
     * @param sceneCode   场景编码
     * @param fieldName   字段名
     * @param fieldValue  字段值
     * @param docIds      文档ID列表
     * @return 构建结果
     */
    boolean buildIndex(String sceneCode, String fieldName, 
                       Object fieldValue, List<Long> docIds);

    /**
     * 查询字段值对应的位图.
     *
     * @param sceneCode   场景编码
     * @param fieldName   字段名
     * @param fieldValue  字段值
     * @return RoaringBitmap 位图
     */
    RoaringBitmap queryBitmap(String sceneCode, String fieldName, Object fieldValue);

    /**
     * 多条件交集查询.
     *
     * @param sceneCode 场景编码
     * @param conditions 条件列表（fieldName:fieldValue）
     * @return 交集结果位图
     */
    RoaringBitmap intersect(String sceneCode, Map<String, Object> conditions);

    /**
     * 多条件并集查询.
     *
     * @param sceneCode 场景编码
     * @param conditions 条件列表
     * @return 并集结果位图
     */
    RoaringBitmap union(String sceneCode, Map<String, Object> conditions);

    /**
     * 位图转文档ID列表.
     *
     * @param bitmap   位图
     * @param offset   偏移量
     * @param limit    数量限制
     * @return 文档ID列表
     */
    List<Long> bitmapToIds(RoaringBitmap bitmap, int offset, int limit);

    /**
     * 获取位图基数（元素数量）.
     *
     * @param bitmap 位图
     * @return 基数
     */
    long getCardinality(RoaringBitmap bitmap);
}
```

#### 3.1.3 Roaring Bitmap 分区策略

```java
/**
 * 位图分区器.
 * <p>
 * 实现动态位图分区，支持亿级文档的高效存储与计算.
 * </p>
 */
public class DsaBitmapPartitioner {

    /**
     * 分区大小：每个分区最多容纳的文档数.
     * 默认100万，可根据内存调整.
     */
    private static final int PARTITION_SIZE = 1_000_000;

    /**
     * 计算文档所属分区.
     *
     * @param docId 文档ID
     * @return 分区号
     */
    public int getPartition(long docId) {
        return (int) (docId / PARTITION_SIZE);
    }

    /**
     * 计算分区内偏移.
     *
     * @param docId 文档ID
     * @return 分区内偏移
     */
    public int getOffset(long docId) {
        return (int) (docId % PARTITION_SIZE);
    }

    /**
     * 合并多分区位图.
     *
     * @param partitions 分区位图Map
     * @return 合并后的位图
     */
    public RoaringBitmap mergePartitions(Map<Integer, RoaringBitmap> partitions) {
        RoaringBitmap result = new RoaringBitmap();
        for (RoaringBitmap bitmap : partitions.values()) {
            result.or(bitmap);
        }
        return result;
    }
}
```

#### 3.1.4 性能对比

| 操作 | 传统ES方案 | Roaring Bitmap方案 | 提升 |
|------|-----------|-------------------|------|
| 单条件查询 | 10-20ms | 1-2ms | 10x |
| 两条件交集 | 30-50ms | 2-5ms | 10x |
| 三条件交集 | 50-100ms | 3-8ms | 15x |
| 亿级文档交集 | 300-500ms | 30-50ms | 10x |

---

### 3.2 分层索引存储架构

#### 3.2.1 三级存储架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        查询请求入口                              │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    L1: 内存热数据层                              │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  ConcurrentSkipListMap<SceneCode, SceneBitmapCache>     │   │
│  │  - 热点场景位图缓存                                       │   │
│  │  - LRU淘汰策略                                           │   │
│  │  - 容量：最多100个场景，每个场景最多1000万文档            │   │
│  └─────────────────────────────────────────────────────────┘   │
│  命中率目标：60%+ | 延迟：<1ms                                   │
└─────────────────────────────────────────────────────────────────┘
                                │ Miss
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    L2: SSD温数据层                               │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  RocksDB / LevelDB                                      │   │
│  │  - Key: scene:field:value                               │   │
│  │  - Value: RoaringBitmap序列化字节                       │   │
│  │  - 支持mmap零拷贝读取                                    │   │
│  └─────────────────────────────────────────────────────────┘   │
│  命中率目标：30%+ | 延迟：1-5ms                                  │
└─────────────────────────────────────────────────────────────────┘
                                │ Miss
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    L3: ES冷数据层                                │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  Elasticsearch Cluster                                  │   │
│  │  - 全量数据存储                                          │   │
│  │  - 异步构建位图索引                                       │   │
│  │  - 作为数据源回填L2/L1                                   │   │
│  └─────────────────────────────────────────────────────────┘   │
│  命中率目标：10% | 延迟：10-50ms                                 │
└─────────────────────────────────────────────────────────────────┘
```

#### 3.2.2 新增模块结构

```
dsa-core/
└── dsa-core-tiered/                    # 新增：分层存储模块
    ├── DsaTieredIndexService.java      # 分层索引服务接口
    ├── DsaTieredIndexServiceImpl.java  # 分层索引服务实现
    ├── DsaHotCache.java                # 内存热缓存
    ├── DsaWarmStore.java               # SSD温存储
    ├── DsaColdSource.java              # ES冷数据源
    ├── DsaCachePromoter.java           # 缓存晋升器
    ├── DsaCacheDemoter.java            # 缓存降级器
    └── config/
        └── DsaTieredConfig.java        # 分层配置
```

#### 3.2.3 核心接口设计

```java
/**
 * 分层索引服务接口.
 * <p>
 * 实现内存/SSD/ES三级存储架构，自动管理数据晋升与降级.
 * </p>
 */
public interface DsaTieredIndexService {

    /**
     * 分层查询位图.
     * 优先级：L1内存 > L2 SSD > L3 ES.
     *
     * @param sceneCode  场景编码
     * @param fieldName  字段名
     * @param fieldValue 字段值
     * @return 位图结果（含来源层级信息）
     */
    DsaTieredResult queryBitmap(String sceneCode, String fieldName, Object fieldValue);

    /**
     * 写入位图到指定层级.
     *
     * @param sceneCode  场景编码
     * @param fieldName  字段名
     * @param fieldValue 字段值
     * @param bitmap     位图数据
     * @param level      目标层级（HOT/WARM/COLD）
     */
    void putBitmap(String sceneCode, String fieldName, Object fieldValue, 
                   RoaringBitmap bitmap, DsaStorageLevel level);

    /**
     * 晋升数据到更高层级.
     * 当L2/L3数据访问频率达到阈值时自动晋升.
     *
     * @param sceneCode  场景编码
     * @param key        数据键
     * @param fromLevel  源层级
     * @param toLevel    目标层级
     */
    void promote(String sceneCode, String key, DsaStorageLevel fromLevel, DsaStorageLevel toLevel);

    /**
     * 降级数据到更低层级.
     * 当L1/L2数据访问频率低于阈值时自动降级.
     *
     * @param sceneCode  场景编码
     * @param key        数据键
     * @param fromLevel  源层级
     * @param toLevel    目标层级
     */
    void demote(String sceneCode, String key, DsaStorageLevel fromLevel, DsaStorageLevel toLevel);

    /**
     * 预热热点数据.
     * 基于查询模式预测，提前加载热点数据到内存.
     *
     * @param sceneCode 场景编码
     * @param patterns  查询模式列表
     */
    void warmup(String sceneCode, List<DsaQueryPattern> patterns);
}

/**
 * 存储层级枚举.
 */
public enum DsaStorageLevel {
    /**
     * 热数据层：内存.
     */
    HOT(1, "memory"),
    /**
     * 温数据层：SSD.
     */
    WARM(2, "ssd"),
    /**
     * 冷数据层：ES.
     */
    COLD(3, "elasticsearch");

    private final int level;
    private final String description;
}

/**
 * 分层查询结果.
 */
public class DsaTieredResult {
    private RoaringBitmap bitmap;
    private DsaStorageLevel sourceLevel;
    private long queryTimeMs;
    private boolean fromCache;
}
```

#### 3.2.4 查询模式预测与预取

```java
/**
 * 查询模式预测器.
 * <p>
 * 基于历史查询记录预测热点数据，实现主动预取.
 * </p>
 */
public class DsaQueryPatternPredictor {

    /**
     * 查询频率统计窗口大小.
     */
    private static final int WINDOW_SIZE = 1000;

    /**
     * 热点判定阈值（访问次数）.
     */
    private static final int HOT_THRESHOLD = 10;

    /**
     * 预测下一个热点查询.
     *
     * @param sceneCode 场景编码
     * @param recentQueries 最近查询列表
     * @return 预测的热点查询模式
     */
    public List<DsaQueryPattern> predict(String sceneCode, 
                                          List<DsaQueryRecord> recentQueries) {
        // 1. 统计查询频率
        Map<String, Integer> frequencyMap = calculateFrequency(recentQueries);

        // 2. 识别热点查询
        List<DsaQueryPattern> hotPatterns = frequencyMap.entrySet().stream()
            .filter(e -> e.getValue() >= HOT_THRESHOLD)
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(10)
            .map(e -> parsePattern(e.getKey()))
            .collect(Collectors.toList());

        // 3. 基于时间序列预测（可选：ARIMA/LSTM）
        List<DsaQueryPattern> predictedPatterns = timeSeriesPredict(recentQueries);

        // 4. 合并结果
        hotPatterns.addAll(predictedPatterns);
        return hotPatterns;
    }

    /**
     * 异步预取.
     *
     * @param patterns 预测的查询模式
     */
    @Async
    public void prefetch(List<DsaQueryPattern> patterns) {
        for (DsaQueryPattern pattern : patterns) {
            // 从ES预取到SSD/内存
            RoaringBitmap bitmap = coldSource.queryBitmap(pattern);
            tieredIndexService.putBitmap(pattern.getSceneCode(), 
                pattern.getFieldName(), pattern.getFieldValue(), 
                bitmap, DsaStorageLevel.WARM);
        }
    }
}
```

---

### 3.3 缓存一致性补偿机制

#### 3.3.1 三重保障架构

```
┌─────────────────────────────────────────────────────────────────┐
│                      数据写入流程                                │
└─────────────────────────────────────────────────────────────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        ▼                       ▼                       ▼
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│  MySQL 主库   │     │  Write-Through │     │  向量时钟     │
│  (数据源)     │     │  缓存更新      │     │  版本标记     │
└───────────────┘     └───────────────┘     └───────────────┘
        │                       │                       │
        ▼                       ▼                       ▼
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│  Canal Binlog │     │  动态TTL      │     │  布隆过滤器   │
│  增量同步     │     │  过期策略      │     │  存在性预判   │
└───────────────┘     └───────────────┘     └───────────────┘
```

#### 3.3.2 新增模块结构

```
dsa-cache/
├── DsaCacheService.java               # 现有接口（扩展）
├── DsaCacheServiceImpl.java           # 现有实现（改造）
├── DsaBloomFilter.java                # 新增：布隆过滤器
├── DsaDynamicTtlStrategy.java         # 新增：动态TTL策略
├── DsaVectorClock.java                # 新增：向量时钟
├── DsaCacheCompensator.java           # 新增：缓存补偿器
└── config/
    └── DsaCacheEnhanceConfig.java     # 新增：缓存增强配置
```

#### 3.3.3 布隆过滤器设计

```java
/**
 * 布隆过滤器接口.
 * <p>
 * 用于快速判断主键是否可能存在，降低无效查询穿透.
 * 理论误判率：< 1%.
 * </p>
 */
public interface DsaBloomFilter {

    /**
     * 添加元素.
     *
     * @param sceneCode 场景编码
     * @param value     元素值
     */
    void put(String sceneCode, String value);

    /**
     * 批量添加元素.
     *
     * @param sceneCode 场景编码
     * @param values    元素值列表
     */
    void putAll(String sceneCode, Collection<String> values);

    /**
     * 判断元素可能存在.
     * 注意：返回true不一定存在，返回false一定不存在.
     *
     * @param sceneCode 场景编码
     * @param value     元素值
     * @return true=可能存在，false=一定不存在
     */
    boolean mightContain(String sceneCode, String value);

    /**
     * 重建布隆过滤器.
     * 当误判率超过阈值时触发重建.
     *
     * @param sceneCode 场景编码
     * @param allValues 全量元素值
     */
    void rebuild(String sceneCode, Collection<String> allValues);

    /**
     * 获取当前误判率.
     *
     * @param sceneCode 场景编码
     * @return 误判率
     */
    double getFalsePositiveRate(String sceneCode);
}

/**
 * 基于Guava的布隆过滤器实现.
 */
public class DsaBloomFilterImpl implements DsaBloomFilter {

    private static final double DEFAULT_FPP = 0.01;
    private static final int EXPECTED_INSERTIONS = 10_000_000;

    private final ConcurrentHashMap<String, BloomFilter<String>> filters;
    private final DsaBloomFilterStats stats;

    @Override
    public boolean mightContain(String sceneCode, String value) {
        BloomFilter<String> filter = filters.get(sceneCode);
        if (filter == null) {
            return true;
        }
        
        boolean result = filter.mightContain(value);
        
        // 统计用于计算实际误判率
        stats.recordCheck(sceneCode, result);
        
        return result;
    }
}
```

#### 3.3.4 动态TTL策略

```java
/**
 * 动态TTL策略接口.
 * <p>
 * 根据数据变更频率动态调整缓存过期时间.
 * 变更频率高 → TTL短；变更频率低 → TTL长.
 * </p>
 */
public interface DsaDynamicTtlStrategy {

    /**
     * 计算动态TTL.
     *
     * @param sceneCode 场景编码
     * @param pkValue   主键值
     * @return TTL秒数
     */
    long calculateTtl(String sceneCode, String pkValue);

    /**
     * 记录数据变更事件.
     * 用于更新变更频率统计.
     *
     * @param sceneCode 场景编码
     * @param pkValue   主键值
     * @param eventType 变更类型（INSERT/UPDATE/DELETE）
     * @param timestamp 变更时间戳
     */
    void recordChange(String sceneCode, String pkValue, 
                      String eventType, long timestamp);

    /**
     * 获取变更频率统计.
     *
     * @param sceneCode 场景编码
     * @param pkValue   主键值
     * @return 变更频率（次/小时）
     */
    double getChangeFrequency(String sceneCode, String pkValue);
}

/**
 * 动态TTL策略实现.
 */
public class DsaDynamicTtlStrategyImpl implements DsaDynamicTtlStrategy {

    /**
     * TTL范围配置.
     */
    private static final long MIN_TTL = 60;      // 最小60秒
    private static final long MAX_TTL = 3600;    // 最大1小时
    private static final long DEFAULT_TTL = 300; // 默认5分钟

    /**
     * 变更频率阈值.
     */
    private static final double HIGH_FREQUENCY_THRESHOLD = 10.0;  // 10次/小时
    private static final double LOW_FREQUENCY_THRESHOLD = 1.0;    // 1次/小时

    /**
     * 变更频率统计（滑动窗口）.
     */
    private final ConcurrentHashMap<String, DsaSlidingWindowCounter> frequencyCounters;

    @Override
    public long calculateTtl(String sceneCode, String pkValue) {
        double frequency = getChangeFrequency(sceneCode, pkValue);

        if (frequency >= HIGH_FREQUENCY_THRESHOLD) {
            // 高频变更：使用最小TTL
            return MIN_TTL;
        } else if (frequency <= LOW_FREQUENCY_THRESHOLD) {
            // 低频变更：使用最大TTL
            return MAX_TTL;
        } else {
            // 中等频率：线性插值
            double ratio = (frequency - LOW_FREQUENCY_THRESHOLD) 
                / (HIGH_FREQUENCY_THRESHOLD - LOW_FREQUENCY_THRESHOLD);
            return (long) (MAX_TTL - ratio * (MAX_TTL - MIN_TTL));
        }
    }

    @Override
    public void recordChange(String sceneCode, String pkValue, 
                             String eventType, long timestamp) {
        String key = sceneCode + ":" + pkValue;
        frequencyCounters.computeIfAbsent(key, k -> new DsaSlidingWindowCounter(3600, 24))
            .increment(timestamp);
    }
}
```

#### 3.3.5 向量时钟设计

```java
/**
 * 向量时钟接口.
 * <p>
 * 用于解决分布式环境下的数据版本冲突.
 * 每个节点维护自己的时钟计数器，通过比较向量时钟判断数据新旧.
 * </p>
 */
public interface DsaVectorClock {

    /**
     * 生成新版本.
     * 当前节点计数器+1.
     *
     * @param sceneCode 场景编码
     * @param pkValue   主键值
     * @param nodeId    当前节点ID
     * @return 新的向量时钟
     */
    DsaVectorClockValue increment(String sceneCode, String pkValue, String nodeId);

    /**
     * 合并两个向量时钟.
     * 取各节点计数器的最大值.
     *
     * @param v1 向量时钟1
     * @param v2 向量时钟2
     * @return 合并后的向量时钟
     */
    DsaVectorClockValue merge(DsaVectorClockValue v1, DsaVectorClockValue v2);

    /**
     * 比较两个向量时钟.
     *
     * @param v1 向量时钟1
     * @param v2 向量时钟2
     * @return 比较结果：BEFORE/AFTER/CONCURRENT/EQUAL
     */
    DsaClockCompareResult compare(DsaVectorClockValue v1, DsaVectorClockValue v2);

    /**
     * 获取数据的向量时钟.
     *
     * @param sceneCode 场景编码
     * @param pkValue   主键值
     * @return 向量时钟值
     */
    DsaVectorClockValue get(String sceneCode, String pkValue);
}

/**
 * 向量时钟值.
 */
public class DsaVectorClockValue implements Serializable {

    /**
     * 节点ID -> 计数器映射.
     */
    private final Map<String, Long> clocks;

    /**
     * 时间戳（用于冲突解决兜底）.
     */
    private final long timestamp;

    public DsaVectorClockValue() {
        this.clocks = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }

    public DsaVectorClockValue(Map<String, Long> clocks) {
        this.clocks = new HashMap<>(clocks);
        this.timestamp = System.currentTimeMillis();
    }
}

/**
 * 向量时钟比较结果.
 */
public enum DsaClockCompareResult {
    /**
     * v1在v2之前.
     */
    BEFORE,
    /**
     * v1在v2之后.
     */
    AFTER,
    /**
     * 并发（无法比较）.
     */
    CONCURRENT,
    /**
     * 相等.
     */
    EQUAL
}
```

#### 3.3.6 缓存补偿器

```java
/**
 * 缓存补偿器接口.
 * <p>
 * 当检测到缓存不一致时，自动触发补偿操作.
 * </p>
 */
public interface DsaCacheCompensator {

    /**
     * 检测并补偿.
     * 定时任务调用，检测缓存与DB的一致性.
     *
     * @param sceneCode 场景编码
     * @return 补偿记录数
     */
    int detectAndCompensate(String sceneCode);

    /**
     * 单条补偿.
     *
     * @param sceneCode 场景编码
     * @param pkValue   主键值
     * @param expected  期望值（来自DB）
     * @param actual    实际值（来自缓存）
     */
    void compensate(String sceneCode, String pkValue, Object expected, Object actual);

    /**
     * 批量补偿.
     *
     * @param sceneCode 场景编码
     * @param diffList  差异列表
     * @return 成功补偿数量
     */
    int batchCompensate(String sceneCode, List<DsaCacheDiff> diffList);

    /**
     * 获取补偿统计.
     *
     * @param sceneCode 场景编码
     * @return 补偿统计信息
     */
    DsaCompensateStats getStats(String sceneCode);
}

/**
 * 缓存差异记录.
 */
public class DsaCacheDiff {
    private String pkValue;
    private Object expectedValue;
    private Object actualValue;
    private DsaDiffType diffType;
    private long detectTime;
}

/**
 * 差异类型.
 */
public enum DsaDiffType {
    /**
     * 缓存缺失.
     */
    CACHE_MISS,
    /**
     * 缓存多余.
     */
    CACHE_REDUNDANT,
    /**
     * 值不一致.
     */
    VALUE_MISMATCH
}
```

---

## 四、模块改造清单

### 4.1 新增模块

| 模块 | 路径 | 说明 |
|------|------|------|
| dsa-core-bitmap | dsa-core/dsa-core-bitmap | Roaring Bitmap位图索引 |
| dsa-core-tiered | dsa-core/dsa-core-tiered | 分层存储架构 |

### 4.2 改造模块

| 模块 | 改造内容 |
|------|----------|
| dsa-core-index | 集成位图索引服务 |
| dsa-cache | 新增布隆过滤器、动态TTL、向量时钟、补偿器 |
| dsa-sync-increment | 同步时更新向量时钟 |
| dsa-consistency | 集成缓存补偿器 |

### 4.3 依赖变更

```xml
<!-- 新增依赖 -->
<!-- Roaring Bitmap -->
<dependency>
    <groupId>org.roaringbitmap</groupId>
    <artifactId>RoaringBitmap</artifactId>
    <version>0.9.45</version>
</dependency>

<!-- RocksDB (SSD存储) -->
<dependency>
    <groupId>org.rocksdb</groupId>
    <artifactId>rocksdbjni</artifactId>
    <version>8.5.3</version>
</dependency>

<!-- Guava (布隆过滤器) -->
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>32.1.3-jre</version>
</dependency>
```

---

## 五、实施计划

### 5.1 阶段划分

| 阶段 | 内容 | 工期 | 优先级 |
|------|------|------|--------|
| P1 | Roaring Bitmap位图索引模块 | 1周 | P0 |
| P2 | 分层存储架构（L1内存+L2 SSD） | 1周 | P0 |
| P3 | 布隆过滤器集成 | 3天 | P1 |
| P4 | 动态TTL策略 | 3天 | P1 |
| P5 | 向量时钟+缓存补偿器 | 1周 | P1 |
| P6 | 查询模式预测与预取 | 1周 | P2 |

### 5.2 风险与应对

| 风险 | 影响 | 应对措施 |
|------|------|----------|
| Roaring Bitmap内存占用 | 高 | 实施分区策略，控制单分区大小 |
| SSD存储可靠性 | 中 | 实现WAL日志，支持崩溃恢复 |
| 布隆过滤器误判 | 低 | 定期重建，监控误判率 |
| 向量时钟冲突 | 低 | 使用时间戳兜底 |

---

## 六、预期收益

### 6.1 性能收益

```
┌────────────────────────────────────────────────────────────────┐
│                    性能提升对比                                 │
├──────────────────┬─────────────┬─────────────┬────────────────┤
│      指标        │   改造前    │   改造后    │    提升比例    │
├──────────────────┼─────────────┼─────────────┼────────────────┤
│ 单条件查询延迟   │   10-20ms   │    1-2ms    │     10x        │
├──────────────────┼─────────────┼─────────────┼────────────────┤
│ 多条件交集延迟   │   50-200ms  │    5-10ms   │     10-20x     │
├──────────────────┼─────────────┼─────────────┼────────────────┤
│ 缓存命中率       │    ~75%     │    >92%     │     +22%       │
├──────────────────┼─────────────┼─────────────┼────────────────┤
│ 无效查询比例     │    ~15%     │    <3%      │     -80%       │
├──────────────────┼─────────────┼─────────────┼────────────────┤
│ 亿级文档交集     │   300-500ms │   30-50ms   │     10x        │
└──────────────────┴─────────────┴─────────────┴────────────────┘
```

### 6.2 架构收益

1. **查询链路优化**：ES查询 → 位图运算，减少网络IO
2. **存储成本降低**：热数据内存缓存，温数据SSD存储
3. **一致性保障**：三重机制确保缓存与DB最终一致
4. **可扩展性**：分层架构支持水平扩展

---

## 七、附录

### 7.1 Roaring Bitmap 原理

Roaring Bitmap是一种压缩位图，结合了稀疏和密集两种存储模式：
- 稀疏模式：使用数组存储，适合低密度数据
- 密集模式：使用传统位图，适合高密度数据
- 自动切换：根据数据密度自动选择最优存储方式

### 7.2 向量时钟冲突解决

当向量时钟比较结果为CONCURRENT时：
1. 优先使用时间戳较大的版本
2. 若时间戳相同，使用节点ID字典序较大的版本
3. 记录冲突日志，供后续审计

### 7.3 动态TTL算法

```
TTL = MAX_TTL - (frequency - LOW_FREQ) / (HIGH_FREQ - LOW_FREQ) * (MAX_TTL - MIN_TTL)

其中：
- frequency: 数据变更频率（次/小时）
- LOW_FREQ: 低频阈值（默认1次/小时）
- HIGH_FREQ: 高频阈值（默认10次/小时）
- MIN_TTL: 最小TTL（默认60秒）
- MAX_TTL: 最大TTL（默认3600秒）
```
