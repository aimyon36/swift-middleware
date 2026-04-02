#!/bin/bash
set -e

echo "=========================================="
echo "  SWIFT Middleware 部署脚本"
echo "=========================================="

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "错误: Docker 未安装"
    exit 1
fi

if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "错误: Docker Compose 未安装"
    exit 1
fi

# 使用 docker compose 或 docker-compose
if docker compose version &> /dev/null; then
    COMPOSE="docker compose"
else
    COMPOSE="docker-compose"
fi

echo ""
echo "1. 停止现有服务..."
$COMPOSE down

echo ""
echo "2. 构建并启动所有服务..."
$COMPOSE up -d --build

echo ""
echo "3. 等待服务启动..."
sleep 10

echo ""
echo "4. 检查服务状态..."
$COMPOSE ps

echo ""
echo "5. 检查应用健康状态..."
for i in {1..30}; do
    if curl -s http://localhost:8080/actuator/health &> /dev/null; then
        echo "应用启动成功!"
        break
    fi
    echo "等待应用启动... ($i/30)"
    sleep 2
done

echo ""
echo "=========================================="
echo "  部署完成!"
echo "=========================================="
echo ""
echo "服务地址:"
echo "  - 应用:   http://localhost:8080"
echo "  - 数据库:  localhost:5432"
echo "  - RabbitMQ: localhost:5672"
echo "  - RabbitMQ Management: http://localhost:15672"
echo ""
echo "常用命令:"
echo "  查看日志: $COMPOSE logs -f app"
echo "  停止服务: $COMPOSE down"
echo "  重启服务: $COMPOSE restart app"
echo ""
