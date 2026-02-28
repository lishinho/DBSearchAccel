# DBSearchAccel 模块划分

## 一、模块总览

```
db-search-accel/
├── dsa-common/                    # 公共模块
│   ├── dsa-common-core/           # 核心公共类
│   ├── dsa-common-model/          # 公共数据模型
│   └── dsa-common-config/         # 公共配置
│
├── dsa-gateway/                   # 接入网关模块
│
├── dsa-core/                      # 核心业务模块
│   ├── dsa-core-route/            # 路由层
│   ├── dsa-core-filter/           # 过滤规则层
│   ├── dsa-core-aggregate/        # 数据聚合层
│   └── dsa-core-index/            # 索引层
│
├── dsa-infrastructure/            # 基础设施模块
│   ├── dsa-infra-es/              # Elasticsearch操作封装
│   ├── dsa-infra-redis/           # Redis操作封装
│   ├── dsa-infra-db/              # 数据库操作封装
│   └── dsa-infra-mq/              # 消息队列封装
│
├── dsa-sync/                      # 数据同步模块
│   ├── dsa-sync-full/             # 全量同步
│   └── dsa-sync-increment/        # 增量同步
│
├── dsa-cache/                     # 实时缓存模块
│
├── dsa-fallback/                  # 降级熔断模块
│
├── dsa-plugin/                    # 插件中心模块
│
├── dsa-consistency/               # 数据一致性校验模块
│
├── dsa-monitor/                   # 监控告警模块
│
├── dsa-sdk/                       # 业务接入SDK
│
└── dsa-admin/                     # 运维控制台
```

---

## 二、模块详细设计

### 2.1 dsa-common（公共模块）

#### dsa-common-core
| 类名 | 职责 |
|------|------|
| `DsaConstants` | 全局常量定义 |
| `DsaResult<T>` | 统一返回结果封装 |
| `DsaException` | 统一异常定义 |
| `DsaSceneType` | 业务场景枚举 |
| `DsaDegradeLevel` | 降级级别枚举 |
| `DsaCheckType` | 一致性校验类型枚举 |

#### dsa-common-model
| 类名 | 职责 |
|------|------|
| `DsaRequest` | 统一请求参数实体 |
| `DsaResponse` | 统一返回结果实体 |
| `DsaPageInfo` | 分页信息实体 |
| `DsaDsl` | ES DSL封装实体 |
| `DsaSceneConfig` | 场景配置实体 |

#### dsa-common-config
| 类名 | 职责 |
|------|------|
| `DsaGlobalConfig` | 全局配置 |
| `DsaEsConfig` | ES连接配置 |
| `DsaRedisConfig` | Redis连接配置 |

---

### 2.2 dsa-gateway（接入网关模块）

| 类名 | 职责 |
|------|------|
| `DsaGatewayFilter` | 网关过滤器 |
| `DsaRequestValidator` | 请求校验器 |
| `DsaRateLimiter` | 限流器（Sentinel集成） |
| `DsaLoadBalancer` | 负载均衡器 |

**核心功能**：
- 统一HTTP/SDK接入入口
- 请求校验（签名、令牌、参数）
- 接口级限流
- 负载均衡

---

### 2.3 dsa-core（核心业务模块）

#### dsa-core-route（路由层）

| 类/接口 | 职责 |
|---------|------|
| `DsaRouteService` | 统一路由接口 |
| `DsaRouteServiceImpl` | 路由实现 |
| `DsaSceneRouter` | 场景路由器 |
| `DsaParamNormalizer` | 参数标准化器 |

**核心功能**：
- 场景匹配与路由
- 请求参数标准化
- 返回结果格式化

#### dsa-core-filter（过滤规则层）

| 类/接口 | 职责 |
|---------|------|
| `DsaFilterChain` | 过滤器链 |
| `DsaGrayFilter` | 灰度过滤器 |
| `DsaDslBuilder` | ES DSL构建器 |
| `DsaQueryValidator` | 查询校验器 |

**核心功能**：
- 灰度准入校验（白名单/黑名单）
- ES DSL动态拼接
- 查询条件校验

#### dsa-core-aggregate（数据聚合层）

| 类/接口 | 职责 |
|---------|------|
| `DsaDataAggregateService` | 数据聚合接口 |
| `DsaDataAggregateServiceImpl` | 聚合实现 |
| `DsaIdMerger` | 主键合并器 |
| `DsaIdSorter` | 主键排序器 |
| `DsaPageProcessor` | 分页处理器 |

**核心功能**：
- 全量+增量主键拼合
- 主键去重
- 排序处理
- 内存分页

#### dsa-core-index（索引层）

| 类/接口 | 职责 |
|---------|------|
| `DsaIndexService` | 索引服务接口 |
| `DsaEsIndexService` | ES索引服务 |
| `DsaRedisIndexService` | Redis索引服务 |

**核心功能**：
- ES索引查询
- Redis主键缓存操作
- 索引策略封装

---

### 2.4 dsa-infrastructure（基础设施模块）

#### dsa-infra-es

| 类/接口 | 职责 |
|---------|------|
| `DsaEsClient` | ES客户端封装 |
| `DsaEsTemplate` | ES操作模板 |
| `DsaEsConfig` | ES配置 |

**核心功能**：
- Elasticsearch Rest High Level Client封装
- 索引CRUD操作
- DSL查询执行

#### dsa-infra-redis

| 类/接口 | 职责 |
|---------|------|
| `DsaRedisClient` | Redis客户端封装 |
| `DsaRedisTemplate` | Redis操作模板 |
| `DsaRedisConfig` | Redis配置 |

**核心功能**：
- Spring Data Redis封装
- 主键列表操作
- 过期时间管理

#### dsa-infra-db

| 类/接口 | 职责 |
|---------|------|
| `DsaDbAccessor` | 数据库访问器 |
| `DsaDbConfig` | 数据库配置 |

**核心功能**：
- MyBatis-Plus集成
- 多数据源支持
- 主键批量查询

#### dsa-infra-mq

| 类/接口 | 职责 |
|---------|------|
| `DsaMqProducer` | 消息生产者 |
| `DsaMqConsumer` | 消息消费者 |
| `DsaMqConfig` | MQ配置 |

**核心功能**：
- RocketMQ/Kafka集成
- 消息发送与消费
- 异常重试

---

### 2.5 dsa-sync（数据同步模块）

#### dsa-sync-full（全量同步）

| 类/接口 | 职责 |
|---------|------|
| `DsaFullSyncService` | 全量同步服务 |
| `DsaFullSyncTask` | 全量同步任务 |
| `DsaDataxExecutor` | DataX执行器 |

**核心功能**：
- DataX集成
- 全量数据迁移
- 同步进度监控

#### dsa-sync-increment（增量同步）

| 类/接口 | 职责 |
|---------|------|
| `DsaIncrementSyncService` | 增量同步服务 |
| `DsaCanalClient` | Canal客户端 |
| `DsaBinlogParser` | Binlog解析器 |
| `DsaSyncRetryHandler` | 同步重试处理器 |

**核心功能**：
- Canal集成
- Binlog监听与解析
- 增量数据同步
- 异常重试

---

### 2.6 dsa-cache（实时缓存模块）

| 类/接口 | 职责 |
|---------|------|
| `@DsaRealTimeCache` | 实时缓存注解 |
| `DsaCacheAspect` | 缓存切面 |
| `DsaCacheService` | 缓存服务 |
| `DsaCacheConfig` | 缓存配置 |

**核心功能**：
- AOP拦截业务增删改方法
- 主键ID解析
- Redis缓存更新
- 容量控制（FIFO）

---

### 2.7 dsa-fallback（降级熔断模块）

| 类/接口 | 职责 |
|---------|------|
| `DsaFallbackService` | 降级服务接口 |
| `DsaFallbackServiceImpl` | 降级实现 |
| `DsaCircuitBreaker` | 熔断器（Sentinel） |
| `DsaDegradeManager` | 降级管理器 |

**核心功能**：
- 接口级/场景级/全局级降级
- Sentinel熔断规则
- 原生数据库查询兜底
- 降级状态监控

---

### 2.8 dsa-plugin（插件中心模块）

| 类/接口 | 职责 |
|---------|------|
| `DsaPlugin` | 插件基础接口 |
| `DsaFilterPlugin` | 过滤插件接口 |
| `DsaFallbackPlugin` | 降级插件接口 |
| `DsaSyncPlugin` | 同步插件接口 |
| `@DsaPlugin` | 插件注册注解 |
| `DsaPluginExecutor` | 插件执行器 |
| `DsaPluginRegistry` | 插件注册中心 |

**核心功能**：
- SPI插件机制
- 插件注册与管理
- 插件链式执行
- 插件隔离

---

### 2.9 dsa-consistency（数据一致性校验模块）

| 类/接口 | 职责 |
|---------|------|
| `DsaConsistencyCheckService` | 一致性校验服务 |
| `DsaPrimaryKeyChecker` | 主键校验器 |
| `DsaFieldChecker` | 字段校验器 |
| `DsaCheckReporter` | 校验报告生成器 |

**核心功能**：
- 主键一致性校验
- 字段一致性校验
- 校验结果可视化
- 数据修复建议

---

### 2.10 dsa-monitor（监控告警模块）

| 类/接口 | 职责 |
|---------|------|
| `DsaMetricsCollector` | 指标采集器 |
| `DsaAlertManager` | 告警管理器 |
| `DsaMonitorConfig` | 监控配置 |

**核心功能**：
- Prometheus指标采集
- Grafana仪表盘
- AlertManager告警
- 多渠道告警推送

---

### 2.11 dsa-sdk（业务接入SDK）

| 类/接口 | 职责 |
|---------|------|
| `DsaClient` | SDK客户端 |
| `DsaClientAutoConfiguration` | 自动配置类 |
| `DsaProperties` | 配置属性类 |

**核心功能**：
- Spring Boot Starter
- 快速接入配置
- SDK调用封装

---

### 2.12 dsa-admin（运维控制台）

| 功能 | 描述 |
|------|------|
| 场景管理 | 场景配置CRUD |
| 同步管理 | 全量/增量同步控制 |
| 降级管理 | 降级状态查看与恢复 |
| 校验管理 | 一致性校验触发与结果查看 |
| 监控大盘 | 核心指标可视化 |

---

## 三、模块依赖关系

```
                    ┌─────────────┐
                    │  dsa-admin  │
                    └──────┬──────┘
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                 │
         ▼                 ▼                 ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  dsa-sdk    │    │ dsa-monitor │    │dsa-consistency│
└──────┬──────┘    └──────┬──────┘    └──────┬──────┘
       │                  │                  │
       └─────────────────┬┴──────────────────┘
                         │
         ┌───────────────┼───────────────┐
         │               │               │
         ▼               ▼               ▼
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│ dsa-gateway │  │ dsa-fallback│  │ dsa-plugin  │
└──────┬──────┘  └──────┬──────┘  └──────┬──────┘
       │                │                │
       └────────────────┼────────────────┘
                        │
         ┌──────────────┼──────────────┐
         │              │              │
         ▼              ▼              ▼
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│  dsa-core   │  │  dsa-sync   │  │  dsa-cache  │
└──────┬──────┘  └──────┬──────┘  └──────┬──────┘
       │                │                │
       └────────────────┼────────────────┘
                        │
                        ▼
              ┌─────────────────┐
              │dsa-infrastructure│
              └────────┬────────┘
                       │
                       ▼
              ┌─────────────────┐
              │   dsa-common    │
              └─────────────────┘
```

---

## 四、模块优先级

| 优先级 | 模块 | 说明 |
|--------|------|------|
| P0 | dsa-common | 基础模块，必须首先开发 |
| P0 | dsa-core-route | 核心路由能力 |
| P0 | dsa-core-index | 核心索引能力 |
| P0 | dsa-core-aggregate | 核心聚合能力 |
| P0 | dsa-fallback | 降级兜底，保障可用性 |
| P1 | dsa-core-filter | 过滤规则 |
| P1 | dsa-sync | 数据同步 |
| P1 | dsa-cache | 实时缓存 |
| P1 | dsa-monitor | 监控告警 |
| P1 | dsa-sdk | 业务接入 |
| P2 | dsa-gateway | 网关（可先用Nginx替代） |
| P2 | dsa-plugin | 插件扩展 |
| P2 | dsa-consistency | 一致性校验 |
| P3 | dsa-admin | 运维控制台 |
