# DSA 部署文档

## 一、快速开始

### 1.1 环境要求

| 组件 | 版本要求 |
|------|----------|
| JDK | 11+ |
| Maven | 3.8+ |
| MySQL | 5.7+ / 8.0+ |
| Elasticsearch | 7.17.x |
| Redis | 6.x |

### 1.2 快速启动

```bash
# 克隆项目
git clone https://github.com/lishinho/DBSearchAccel.git
cd DBSearchAccel

# 编译打包
mvn clean package -DskipTests

# 启动Admin控制台
java -jar dsa-admin/target/dsa-admin-1.0.0-SNAPSHOT.jar
```

访问 http://localhost:8080 进入管理控制台。

## 二、Docker部署

### 2.1 构建镜像

```dockerfile
FROM openjdk:11-jre-slim
WORKDIR /app
COPY dsa-admin/target/dsa-admin-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
docker build -t dsa-admin:1.0.0 .
```

### 2.2 Docker Compose

```yaml
version: '3.8'
services:
  dsa-admin:
    image: dsa-admin:1.0.0
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - ES_HOST=elasticsearch
      - REDIS_HOST=redis
      - DB_HOST=mysql
    depends_on:
      - elasticsearch
      - redis
      - mysql

  elasticsearch:
    image: elasticsearch:7.17.9
    environment:
      - discovery.type=single-node
      - ES_JAVA_OPTS=-Xms512m -Xmx512m
    ports:
      - "9200:9200"

  redis:
    image: redis:6-alpine
    ports:
      - "6379:6379"

  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=root123
      - MYSQL_DATABASE=dsa
    ports:
      - "3306:3306"
```

```bash
docker-compose up -d
```

## 三、Kubernetes部署

### 3.1 ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: dsa-config
data:
  application.yml: |
    server:
      port: 8080
    spring:
      datasource:
        url: jdbc:mysql://mysql:3306/dsa
        username: root
        password: root123
      elasticsearch:
        uris: http://elasticsearch:9200
      redis:
        host: redis
        port: 6379
```

### 3.2 Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: dsa-admin
spec:
  replicas: 2
  selector:
    matchLabels:
      app: dsa-admin
  template:
    metadata:
      labels:
        app: dsa-admin
    spec:
      containers:
      - name: dsa-admin
        image: dsa-admin:1.0.0
        ports:
        - containerPort: 8080
        volumeMounts:
        - name: config
          mountPath: /app/config
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
      volumes:
      - name: config
        configMap:
          name: dsa-config
```

### 3.3 Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: dsa-admin
spec:
  selector:
    app: dsa-admin
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

## 四、配置说明

### 4.1 核心配置项

```yaml
# ES配置
dsa:
  elasticsearch:
    uris: http://localhost:9200
    username: elastic
    password: changeme
    connection-timeout: 5000
    socket-timeout: 30000

# Redis配置
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0

# 数据库配置
  datasource:
    url: jdbc:mysql://localhost:3306/dsa
    username: root
    password: root123
    driver-class-name: com.mysql.cj.jdbc.Driver

# 降级配置
  degrade:
    enabled: true
    default-level: SCENE
    circuit-breaker:
      enabled: true
      failure-rate-threshold: 50
      slow-call-rate-threshold: 80
      wait-duration-in-open-state: 30s

# 限流配置
  rate-limit:
    enabled: true
    default-qps: 1000
    default-concurrent: 100
```

## 五、监控配置

### 5.1 Prometheus配置

```yaml
scrape_configs:
  - job_name: 'dsa-admin'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['dsa-admin:8080']
```

### 5.2 Grafana Dashboard

导入 `docs/monitoring/grafana-dashboard.json` 到Grafana。
