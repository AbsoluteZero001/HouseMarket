# HouseMarket Docker 生产部署（推荐）

## 优势
- 环境隔离，不依赖宿主机 Java/Node 版本
- 一键启动 MySQL + Redis + 后端 + 前端
- 自动初始化数据库与种子数据

## 前置条件
- 已安装 Docker Engine 20+ 与 Docker Compose v2
- 服务器 4G 内存以上

## 部署步骤

### 1. 上传部署包
将整个项目（或至少以下文件）上传到服务器：
```
Dockerfile
docker-compose.yml
docker-entrypoint.sh
maven-settings.xml
frontend/Dockerfile
frontend/nginx.conf
src/main/resources/db/schema.sql
src/main/resources/db/data.sql
seed-uploads/
pom.xml
src/
```

### 2. 配置环境变量
```bash
cp deploy/docker/.env.production.example .env
vim .env   # 修改密码、JWT_SECRET、CORS 域名
```

**必须修改的项：**
- `MYSQL_ROOT_PASSWORD`：MySQL root 密码
- `JWT_SECRET`：执行 `openssl rand -base64 48` 生成
- `APP_CORS_ALLOWED_ORIGINS`：改为 `https://house.evezero.cn`

### 3. 构建并启动
```bash
docker compose up -d --build
```

### 4. 验证
```bash
# 查看服务状态
docker compose ps

# 查看后端日志
docker compose logs -f backend

# 健康检查
curl http://localhost:8082/api/public/stats
```

### 5. 配置外部 Nginx 反向代理（可选）
如果服务器上已有 Nginx 或使用域名访问，可配置反向代理到容器前端端口：

```nginx
server {
    listen 80;
    server_name house.evezero.cn;
    location / {
        proxy_pass http://127.0.0.1:5173;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 数据持久化
- MySQL 数据：Docker volume `housemarket-mysql-data`
- Redis 数据：Docker volume `housemarket-redis-data`
- 上传文件：Docker volume `housemarket-uploads`

## 常用运维命令
```bash
# 停止服务（保留数据）
docker compose down

# 停止并删除数据卷（清空所有数据！）
docker compose down -v

# 更新镜像后重启
docker compose pull && docker compose up -d

# 查看各服务日志
docker compose logs -f mysql
docker compose logs -f redis
docker compose logs -f backend
docker compose logs -f frontend
```
