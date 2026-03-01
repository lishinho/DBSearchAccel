# DBSearchAccel 第三阶段开发日志

## 一、开发概述

| 项目 | 内容 |
|------|------|
| 阶段名称 | 扩展能力 |
| 开发时间 | 2024-XX-XX |
| 主要内容 | 插件扩展、网关、一致性校验 |
| 状态 | ✅ 已完成 |

---

## 二、模块开发详情

### 2.1 dsa-plugin 模块（插件扩展机制）

#### 2.1.1 功能说明
提供SPI插件机制，支持自定义插件扩展系统的过滤、降级、同步等能力。

#### 2.1.2 核心类设计

| 类名 | 职责 | 说明 |
|------|------|------|
| `DsaPlugin<C, R>` | 插件基础接口 | 定义生命周期方法：initialize、destroy、execute |
| `DsaFilterPlugin` | 过滤插件接口 | 继承DsaPlugin，用于查询过滤扩展 |
| `DsaFallbackPlugin` | 降级插件接口 | 继承DsaPlugin，用于降级逻辑扩展 |
| `DsaSyncPlugin` | 同步插件接口 | 继承DsaPlugin，用于数据同步扩展 |
| `DsaPluginContext` | 插件上下文 | 执行时传递的上下文信息 |
| `DsaPluginResult` | 插件执行结果 | 执行结果封装 |
| `DsaPluginConfig` | 插件配置实体 | 名称、版本、优先级、启用状态 |
| `DsaPluginComponent` | 插件注册注解 | 配合Spring自动注册 |
| `DsaPluginRegistry` | 插件注册中心 | SPI加载和管理插件 |
| `DsaPluginExecutor` | 插件执行器 | 链式执行插件 |
| `DsaPluginLoader` | SPI加载器 | 基于ServiceLoader实现 |

#### 2.1.3 核心功能
- SPI插件机制（Java ServiceLoader）
- 插件注册与管理
- 插件链式执行（责任链模式）
- 按优先级排序执行

#### 2.1.4 使用示例

```java
@DsaPluginComponent(name = "customFilterPlugin", type = PluginType.FILTER, priority = 10)
public class CustomFilterPlugin implements DsaFilterPlugin {
    @Override
    public DsaPluginResult execute(FilterContext context) {
        // 自定义过滤逻辑
        return DsaPluginResult.success(null);
    }
}
```

---

### 2.2 dsa-consistency 模块（数据一致性校验）

#### 2.2.1 功能说明
提供ES与数据库数据一致性校验能力，支持主键校验、字段校验、数量校验。

#### 2.2.2 核心类设计

| 类名 | 职责 | 说明 |
|------|------|------|
| `DsaCheckType` | 校验类型枚举 | PRIMARY_KEY/FIELD/COUNT/FULL |
| `DsaCheckStatus` | 校验状态枚举 | PENDING/RUNNING/COMPLETED/FAILED/CANCELLED |
| `DsaCheckTask` | 校验任务 | 任务配置和状态信息 |
| `DsaCheckResult` | 校验结果 | 单次校验结果 |
| `DsaCheckReport` | 校验报告 | 汇总报告 |
| `DsaConsistencyCheckService` | 校验服务接口 | 定义校验方法 |
| `DsaConsistencyCheckServiceImpl` | 校验服务实现 | 协调校验流程 |
| `DsaPrimaryKeyChecker` | 主键校验器接口 | ES与DB主键对比 |
| `DsaPrimaryKeyCheckerImpl` | 主键校验器实现 | 批量对比主键 |
| `DsaFieldChecker` | 字段校验器接口 | 字段值一致性校验 |
| `DsaFieldCheckerImpl` | 字段校验器实现 | 对比字段值 |
| `DsaCheckReportGenerator` | 报告生成器接口 | 生成可视化报告 |
| `DsaCheckReportGeneratorImpl` | 报告生成器实现 | JSON/CSV/HTML格式 |

#### 2.2.3 核心功能
- 主键一致性校验（ES vs DB）
- 字段值一致性校验
- 数据量对比校验
- 校验结果可视化
- 校验报告导出（JSON/CSV/HTML）

#### 2.2.4 使用示例

```java
// 创建校验任务
DsaCheckTask task = consistencyCheckService.createTask("order_scene", DsaCheckType.PRIMARY_KEY);

// 执行校验
DsaCheckReport report = consistencyCheckService.executeCheck(task.getTaskId());

// 导出报告
String htmlReport = consistencyCheckService.exportReport(task.getTaskId(), "html");
```

---

### 2.3 dsa-gateway 模块（API网关+限流降级）

#### 2.3.1 功能说明
提供API网关能力，支持请求校验、限流、降级到数据库查询。

#### 2.3.2 核心类设计

| 类名 | 职责 | 说明 |
|------|------|------|
| `DsaGatewayProperties` | 网关配置属性 | 全局开关、限流配置、路由规则 |
| `DsaRouteDefinition` | 路由定义 | 路由规则配置 |
| `DsaRateLimitConfig` | 限流配置 | QPS/并发限流配置 |
| `DsaRequestValidator` | 请求校验器接口 | 签名、令牌、参数校验 |
| `DsaRequestValidatorImpl` | 请求校验器实现 | MD5签名校验 |
| `DsaRateLimiter` | 限流器接口 | QPS/并发限流 |
| `DsaRateLimiterImpl` | 限流器实现 | Sentinel集成 |
| `DsaGatewayFilter` | 网关全局过滤器 | 请求预处理、限流、降级 |

#### 2.3.3 核心功能
- Spring Cloud Gateway集成
- 请求校验（签名、令牌、参数）
- 接口级限流（QPS/并发）
- 全局开关（关闭时直接降级到数据库）
- 限流后降级到数据库查询

#### 2.3.4 降级逻辑

```
请求 → 网关过滤器
         ↓
    全局开关是否开启？
         ↓ 否
    直接降级到数据库查询
         ↓ 是
    限流检查
         ↓ 触发限流
    降级到数据库查询
         ↓ 通过
    正常路由到后端服务
```

#### 2.3.5 配置示例

```yaml
dsa:
  gateway:
    enabled: true
    global-switch: true  # 全局开关，关闭时直接查数据库
    global-rate-limit:
      enabled: true
      strategy: QPS
      qps-threshold: 100
      behavior: DEGRADE  # 限流后降级
    request-validation:
      enabled: true
      validate-signature: false
      validate-token: false
```

---

## 三、模块依赖关系

```
dsa-plugin
    └── dsa-common-model

dsa-consistency
    ├── dsa-common-model
    ├── dsa-infra-es
    └── dsa-infra-db

dsa-gateway
    ├── dsa-common-model
    ├── dsa-core-route
    ├── dsa-fallback
    └── spring-cloud-starter-gateway
```

---

## 四、新增文件清单

### 4.1 dsa-plugin 模块（12个文件）
- `DsaPlugin.java` - 插件基础接口
- `DsaPluginContext.java` - 插件上下文
- `DsaPluginResult.java` - 插件执行结果
- `DsaFilterPlugin.java` - 过滤插件接口
- `DsaFallbackPlugin.java` - 降级插件接口
- `DsaSyncPlugin.java` - 同步插件接口
- `DsaPluginComponent.java` - 插件注册注解
- `DsaPluginConfig.java` - 插件配置实体
- `DsaPluginRegistry.java` - 插件注册中心
- `DsaPluginExecutor.java` - 插件执行器
- `DsaPluginLoader.java` - SPI加载器
- `DsaPluginAutoConfiguration.java` - 自动配置

### 4.2 dsa-consistency 模块（12个文件）
- `DsaCheckType.java` - 校验类型枚举
- `DsaCheckStatus.java` - 校验状态枚举
- `DsaCheckTask.java` - 校验任务
- `DsaCheckResult.java` - 校验结果
- `DsaCheckReport.java` - 校验报告
- `DsaConsistencyCheckService.java` - 校验服务接口
- `DsaConsistencyCheckServiceImpl.java` - 校验服务实现
- `DsaPrimaryKeyChecker.java` - 主键校验器接口
- `DsaPrimaryKeyCheckerImpl.java` - 主键校验器实现
- `DsaFieldChecker.java` - 字段校验器接口
- `DsaFieldCheckerImpl.java` - 字段校验器实现
- `DsaCheckReportGenerator.java` - 报告生成器接口
- `DsaCheckReportGeneratorImpl.java` - 报告生成器实现
- `DsaConsistencyAutoConfiguration.java` - 自动配置

### 4.3 dsa-gateway 模块（10个文件）
- `DsaGatewayProperties.java` - 网关配置属性
- `DsaRouteDefinition.java` - 路由定义
- `DsaRateLimitConfig.java` - 限流配置
- `DsaRequestValidator.java` - 请求校验器接口
- `DsaRequestValidatorImpl.java` - 请求校验器实现
- `DsaRateLimiter.java` - 限流器接口
- `DsaRateLimiterImpl.java` - 限流器实现
- `DsaGatewayFilter.java` - 网关全局过滤器
- `DsaGatewayAutoConfiguration.java` - 自动配置

---

## 五、验收结果

| 验收项 | 状态 | 说明 |
|--------|------|------|
| dsa-plugin 编译通过 | ✅ | 12个类编译成功 |
| dsa-consistency 编译通过 | ✅ | 14个类编译成功 |
| dsa-gateway 编译通过 | ✅ | 10个类编译成功 |
| 项目整体编译通过 | ✅ | 24个模块全部编译成功 |
| Spring自动配置 | ✅ | spring.factories配置完成 |

---

## 六、下一步计划

第四阶段：运维与文档
- dsa-admin 运维控制台
- 完善开发文档、部署文档、接入文档
- 示例项目
