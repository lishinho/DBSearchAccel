# DSA 运维文档

## 一、日常运维

### 1.1 健康检查

```bash
# 检查服务状态
curl http://localhost:8080/actuator/health

# 检查ES连接
curl http://localhost:8080/api/monitor/health

# 检查指标
curl http://localhost:8080/actuator/prometheus
```

### 1.2 日志查看

```bash
# 查看实时日志
tail -f logs/dsa-admin.log

# 查看错误日志
grep ERROR logs/dsa-admin.log

# 查看特定场景日志
grep "sceneCode=order_query" logs/dsa-admin.log
```

### 1.3 配置更新

```bash
# 热更新配置（需要Nacos支持）
curl -X POST http://localhost:8080/actuator/refresh
```

## 二、监控告警

### 2.1 核心指标

| 指标名称 | 说明 | 告警阈值 |
|----------|------|----------|
| dsa_query_total | 总查询次数 | - |
| dsa_query_latency | 查询延迟 | P99 > 1s |
| dsa_hit_rate | 命中率 | < 90% |
| dsa_degrade_count | 降级次数 | > 10/min |
| dsa_error_count | 错误次数 | > 5/min |

### 2.2 告警规则配置

```yaml
groups:
- name: dsa-alerts
  rules:
  - alert: HighLatency
    expr: histogram_quantile(0.99, dsa_query_latency_bucket) > 1000
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "DSA查询延迟过高"
      
  - alert: HighErrorRate
    expr: rate(dsa_error_count[5m]) > 5
    for: 2m
    labels:
      severity: critical
    annotations:
      summary: "DSA错误率过高"
      
  - alert: FrequentDegrade
    expr: rate(dsa_degrade_count[5m]) > 10
    for: 2m
    labels:
      severity: warning
    annotations:
      summary: "DSA频繁降级"
```

## 三、故障排查

### 3.1 查询超时

**现象**：查询响应时间过长

**排查步骤**：
1. 检查ES集群状态和负载
2. 检查DSL查询是否命中索引
3. 检查数据库连接池状态
4. 查看慢查询日志

**解决方案**：
- 优化DSL查询
- 增加ES资源
- 调整超时配置

### 3.2 数据不一致

**现象**：ES和数据库数据不一致

**排查步骤**：
1. 触发一致性校验
2. 检查同步任务状态
3. 检查Canal连接状态

**解决方案**：
- 重新全量同步
- 修复增量同步
- 手动修复差异数据

### 3.3 频繁降级

**现象**：系统频繁触发降级

**排查步骤**：
1. 检查ES/Redis/DB连接状态
2. 检查系统资源使用情况
3. 查看降级原因日志

**解决方案**：
- 修复底层依赖问题
- 调整降级阈值
- 增加系统资源

## 四、数据维护

### 4.1 索引重建

```bash
# 创建新索引
curl -X PUT "localhost:9200/order_index_v2" -H 'Content-Type: application/json' -d'
{
  "mappings": {
    "properties": {
      "order_id": {"type": "keyword"},
      "order_status": {"type": "keyword"},
      "create_time": {"type": "date"},
      "order_amount": {"type": "double"}
    }
  }
}'

# 数据迁移
# 通过DataX或DSA全量同步功能

# 切换别名
curl -X POST "localhost:9200/_aliases" -H 'Content-Type: application/json' -d'
{
  "actions": [
    {"remove": {"index": "order_index_v1", "alias": "order_index"}},
    {"add": {"index": "order_index_v2", "alias": "order_index"}}
  ]
}'
```

### 4.2 数据清理

```bash
# 清理过期索引
curl -X DELETE "localhost:9200/order_index_2023_*"

# 清理Redis缓存
redis-cli KEYS "dsa:*" | xargs redis-cli DEL
```

## 五、备份恢复

### 5.1 配置备份

```bash
# 备份场景配置
curl http://localhost:8080/api/scene/list > scene_config_backup.json

# 备份告警规则
curl http://localhost:8080/api/monitor/alerts > alert_rules_backup.json
```

### 5.2 ES快照

```bash
# 创建快照仓库
curl -X PUT "localhost:9200/_snapshot/dsa_backup" -H 'Content-Type: application/json' -d'
{
  "type": "fs",
  "settings": {
    "location": "/backup/dsa"
  }
}'

# 创建快照
curl -X PUT "localhost:9200/_snapshot/dsa_backup/snapshot_1?wait_for_completion=true"
```

## 六、安全配置

### 6.1 访问控制

```yaml
# 开启认证
dsa:
  security:
    enabled: true
    api-key: your-api-key
    secret-key: your-secret-key
```

### 6.2 网络隔离

- ES/Redis/MySQL 仅允许内网访问
- 管理控制台配置IP白名单
- 生产环境关闭Actuator端点
