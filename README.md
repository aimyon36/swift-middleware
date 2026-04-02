# SWIFT Middleware

SWIFT 报文中间件系统 - 用于接收、发送、解析 SWIFT MX (ISO20022) 报文，并路由到内部系统。

## 技术栈

- Spring Boot 3.2.3
- MyBatis-Plus
- PostgreSQL 15
- RabbitMQ 3.12
- Docker / Docker Compose

## 快速部署

### 方式一: 一键部署 (推荐)

```bash
# 克隆项目
git clone https://github.com/aimyon36/swift-middleware.git
cd swift-middleware

# 赋予脚本执行权限
chmod +x deploy.sh

# 运行部署脚本
./deploy.sh
```

### 方式二: 手动部署

```bash
# 构建并启动
docker-compose up -d --build

# 查看日志
docker-compose logs -f app
```

## 服务地址

| 服务 | 地址 |
|------|------|
| 应用 API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| PostgreSQL | localhost:5432 |
| RabbitMQ | localhost:5672 |
| RabbitMQ Management | http://localhost:15672 |

## 初始化数据

首次启动时，数据库表会自动创建。如需初始化测试数据，可执行以下 SQL：

```sql
-- 插入默认订阅配置
INSERT INTO system_subscription (system_code, system_name, endpoint_type, endpoint_url, message_types, auth_type, retry_count, timeout_ms, enabled)
VALUES ('CASH_MGMT', '现金管理系统', 'HTTP', 'http://localhost:9000/callback', 'pacs.008.001.08,pain.001.001.09', 'NONE', 3, 5000, true);

-- 插入默认路由规则
INSERT INTO message_route (message_type, business_scenario, target_system, routing_rule, priority, enabled)
VALUES ('pacs.008.001.08', 'CREDIT_TRANSFER', 'CASH_MGMT', 'amount > 0', 1, true);
```

## Docker Compose 服务说明

| 服务 | 说明 | 端口 |
|------|------|------|
| app | Spring Boot 应用 | 8080 |
| postgres | PostgreSQL 数据库 | 5432 |
| rabbitmq | RabbitMQ 消息队列 | 5672, 15672 |

## 环境变量

可通过 `.env` 文件或环境变量覆盖默认配置：

```bash
cp .env.example .env
# 编辑 .env 文件
```

| 变量 | 默认值 | 说明 |
|------|--------|------|
| DB_HOST | postgres | 数据库主机 |
| DB_PORT | 5432 | 数据库端口 |
| DB_NAME | swift_middleware | 数据库名 |
| DB_USERNAME | postgres | 数据库用户名 |
| DB_PASSWORD | postgres | 数据库密码 |
| RABBITMQ_HOST | rabbitmq | RabbitMQ 主机 |
| RABBITMQ_PORT | 5672 | RabbitMQ 端口 |
| RABBITMQ_USERNAME | guest | RabbitMQ 用户名 |
| RABBITMQ_PASSWORD | guest | RabbitMQ 密码 |

## 常用命令

```bash
# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 查看日志
docker-compose logs -f

# 只看应用日志
docker-compose logs -f app

# 重启应用
docker-compose restart app

# 重新构建并启动
docker-compose up -d --build

# 进入数据库
psql -h localhost -U postgres -d swift_middleware

# 进入 RabbitMQ 容器
docker exec -it swift-rabbitmq rabbitmqctl status
```

## API 接口

### 报文接口

- `POST /api/v1/saa/inbound` - SAA 接收报文
- `POST /api/v1/saa/outbound` - SAA 发送报文
- `GET /api/v1/saa/status/{messageId}` - 查询报文状态
- `GET /api/v1/messages` - 分页查询报文列表
- `GET /api/v1/messages/{messageId}` - 查询报文详情

### 管理接口

- `GET /api/v1/admin/routes` - 查询路由规则
- `POST /api/v1/admin/routes` - 新增路由规则
- `PUT /api/v1/admin/routes/{id}` - 更新路由规则
- `DELETE /api/v1/admin/routes/{id}` - 删除路由规则
- `GET /api/v1/admin/subscriptions` - 查询订阅配置
- `POST /api/v1/admin/subscriptions` - 新增订阅配置
- `GET /api/v1/admin/notifications` - 查询通知日志

详细 API 文档请访问: http://localhost:8080/swagger-ui.html

## 集群部署

如需集群部署，可使用 `docker-compose-cluster.yml`:

```bash
docker-compose -f docker-compose-cluster.yml up -d --scale app=3
```

## License

MIT
