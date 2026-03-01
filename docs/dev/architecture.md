# DSA 开发文档

## 一、架构概述

DBSearchAccel（DSA）是一个数据库复杂查询加速中间件，核心理念是：
- **用搜索引擎做高性能条件过滤**：通过ES进行复杂条件查询，快速获取主键ID列表
- **用关系型数据库做精准数据详情查询**：通过主键ID批量查询数据库获取完整数据

### 1.1 系统架构

```
┌─────────────────────────────────────────────────────────────────┐
│ 接入层：业务系统、前端应用                                       │
└───────────────────┬─────────────────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────────────────┐
│ 核心业务层：接入网关 → 路由层 → 过滤规则层 → 数据聚合层 → 索引层  │
└───────────────────┬─────────────────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────────────────┐
│ 基础能力层：配置中心、实时缓存、降级熔断、插件中心、数据一致性校验 │
└───────────────────┬─────────────────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────────────────┐
│ 底层依赖层：关系型数据库、Elasticsearch、Redis、Canal、监控告警  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 核心流程

1. **查询请求接入**：业务系统通过SDK或HTTP发起查询请求
2. **场景路由**：根据场景编码路由到对应的处理逻辑
3. **条件过滤**：通过ES进行复杂条件过滤，获取主键ID列表
4. **主键拼合**：合并全量索引和增量缓存的主键
5. **数据查询**：根据主键ID批量查询数据库获取详情
6. **结果返回**：返回分页后的完整数据

## 二、模块详解

### 2.1 dsa-common

公共基础模块，包含：
- **dsa-common-core**：核心枚举、常量、异常定义
- **dsa-common-model**：公共数据模型（Request、Response、PageInfo等）

### 2.2 dsa-core

核心业务模块：
- **dsa-core-route**：场景路由、参数标准化
- **dsa-core-filter**：灰度过滤、DSL构建、查询校验
- **dsa-core-aggregate**：主键拼合、去重、排序、分页
- **dsa-core-index**：ES索引查询、Redis缓存操作

### 2.3 dsa-sync

数据同步模块：
- **dsa-sync-full**：全量同步（DataX集成）
- **dsa-sync-increment**：增量同步（Canal集成）

### 2.4 dsa-fallback

降级熔断模块：
- 多级降级策略（接口级、场景级、全局级）
- Sentinel熔断规则集成
- 原生数据库查询兜底

### 2.5 dsa-gateway

API网关模块：
- Spring Cloud Gateway集成
- 限流控制（Sentinel）
- 请求校验

### 2.6 dsa-plugin

插件扩展模块：
- SPI插件机制
- 过滤插件、降级插件、同步插件
- 链式执行器

### 2.7 dsa-consistency

数据一致性校验模块：
- 主键一致性校验
- 字段一致性校验
- 校验报告生成

### 2.8 dsa-monitor

监控告警模块：
- Prometheus指标采集
- Grafana仪表盘
- AlertManager告警

### 2.9 dsa-admin

运维控制台：
- 场景管理
- 同步管理
- 降级管理
- 监控大盘

## 三、核心接口

### 3.1 查询接口

```java
// 统一查询请求
public class DsaRequest {
    private String sceneCode;           // 场景编码
    private Map<String, Object> params; // 查询参数
    private Integer pageNum;            // 页码
    private Integer pageSize;           // 每页大小
    private String orderBy;             // 排序字段
    private String orderDirection;      // 排序方向
}

// 统一查询响应
public class DsaResponse<T> {
    private Integer code;               // 状态码
    private String message;             // 消息
    private List<T> data;               // 数据列表
    private DsaPageInfo pageInfo;       // 分页信息
}
```

### 3.2 场景配置

```java
public class DsaSceneConfig {
    private Long id;
    private String sceneCode;           // 场景编码
    private String sceneName;           // 场景名称
    private String esIndex;             // ES索引名
    private String dbTable;             // 数据库表名
    private String pkField;             // 主键字段
    private String dslTemplate;         // DSL模板
    private String fieldMapping;        // 字段映射
    private String status;              // 状态
}
```

## 四、扩展开发

### 4.1 自定义插件

```java
@DsaPluginComponent(name = "myPlugin", type = "FILTER")
public class MyFilterPlugin implements DsaFilterPlugin {
    @Override
    public DsaPluginResult doFilter(DsaPluginContext context) {
        // 自定义过滤逻辑
        return DsaPluginResult.success();
    }
}
```

### 4.2 自定义降级策略

```java
public class MyDegradeStrategy implements DsaDegradeStrategy {
    @Override
    public boolean shouldDegrade(String sceneCode, DsaRequest request) {
        // 自定义降级判断逻辑
        return false;
    }
}
```
