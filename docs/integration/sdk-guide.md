# DSA 接入文档

## 一、SDK接入

### 1.1 添加依赖

```xml
<dependency>
    <groupId>io.github.dbsearchaccel</groupId>
    <artifactId>dsa-sdk</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 1.2 配置SDK

```yaml
dsa:
  client:
    server-url: http://localhost:8080
    connect-timeout: 5000
    read-timeout: 30000
    retry-times: 3
```

### 1.3 使用SDK

```java
@Autowired
private DsaClient dsaClient;

public List<Order> queryOrders(Map<String, Object> params, int pageNum, int pageSize) {
    DsaRequest request = new DsaRequest();
    request.setSceneCode("order_query");
    request.setParams(params);
    request.setPageNum(pageNum);
    request.setPageSize(pageSize);
    
    DsaResponse<Order> response = dsaClient.query(request, Order.class);
    return response.getData();
}
```

## 二、HTTP接入

### 2.1 查询接口

**请求**
```
POST /api/query
Content-Type: application/json

{
    "sceneCode": "order_query",
    "params": {
        "status": "PAID",
        "startTime": "2024-01-01",
        "endTime": "2024-12-31",
        "minAmount": 100,
        "maxAmount": 10000
    },
    "pageNum": 1,
    "pageSize": 20,
    "orderBy": "create_time",
    "orderDirection": "DESC"
}
```

**响应**
```json
{
    "code": 200,
    "message": "success",
    "data": [...],
    "pageInfo": {
        "pageNum": 1,
        "pageSize": 20,
        "total": 1000,
        "pages": 50
    }
}
```

## 三、场景配置

### 3.1 创建场景

通过管理控制台或API创建场景配置：

```json
{
    "sceneCode": "order_query",
    "sceneName": "订单查询",
    "esIndex": "order_index",
    "dbTable": "t_order",
    "pkField": "order_id",
    "dslTemplate": "{\"query\":{\"bool\":{\"must\":[...]}}}",
    "fieldMapping": "{\"status\":\"order_status\",\"amount\":\"order_amount\"}",
    "status": "ENABLED"
}
```

### 3.2 DSL模板

DSL模板支持参数占位符：

```json
{
    "query": {
        "bool": {
            "must": [
                {"term": {"order_status": "${status}"}},
                {"range": {"create_time": {"gte": "${startTime}", "lte": "${endTime}"}}},
                {"range": {"order_amount": {"gte": "${minAmount}", "lte": "${maxAmount}"}}}
            ]
        }
    },
    "size": 10000,
    "_source": ["order_id"]
}
```

### 3.3 字段映射

定义业务参数到ES字段的映射关系：

```json
{
    "status": "order_status",
    "startTime": "create_time",
    "endTime": "create_time",
    "minAmount": "order_amount",
    "maxAmount": "order_amount",
    "userId": "user_id"
}
```

## 四、数据同步

### 4.1 全量同步

首次接入时需要进行全量数据同步：

1. 准备DataX任务配置
2. 通过管理控制台触发全量同步
3. 监控同步进度

### 4.2 增量同步

配置Canal监听数据库变更：

```yaml
canal:
  server: localhost:11111
  destination: example
  filter: "dsa\\.t_order"
```

## 五、最佳实践

### 5.1 场景设计原则

1. **单一职责**：每个场景对应一种查询类型
2. **合理分片**：大数据量场景考虑按时间分索引
3. **字段精简**：ES只存储过滤字段和主键

### 5.2 性能优化

1. **合理设置分页大小**：建议不超过100
2. **使用缓存**：对热点查询启用Redis缓存
3. **异步查询**：大数据量导出使用异步方式

### 5.3 降级策略

1. **配置降级阈值**：根据业务容忍度设置
2. **监控告警**：及时感知降级事件
3. **定期演练**：确保降级逻辑可用
