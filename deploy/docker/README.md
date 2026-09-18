# HouseMarket Docker 生产部署（推荐）

## 架构说明

```
Internet
   │
   ▼
house.evezero.cn :80/:443
   │
   ▼
宿主机 Nginx（已有）  ──反向代理──▶  127.0.0.1:5173
                                        │
                                        ▼
                              frontend 容器 (nginx:alpine)
                              /api,/uploads,/ws,/user → backend:8082
                                        │
                                        ▼
                              backend 容器 (Spring Boot)
                              → mysql:3306 / redis:6379
```

**端口策略**：
- 仅 `frontend` 容器映射到宿主机 `127.0.0.1:5173`（绑定回环地址，不监听 0.0.0.0，不占用 80/443）
- `backend`、`mysql`、`redis` 均不映射宿主机端口，仅 Docker 内部网络可访问

## 安全约束

- **生产 `.env` 严禁提交 Git**：`.env` 已在 `.gitignore` 中排除，仅保留 `.env.production.example` 模板
- **必填变量缺失即停止**：`MYSQL_ROOT_PASSWORD` 与 `JWT_SECRET` 未在 `.env` 中设置时，`docker compose config` / `up` 会直接报错退出，不会使用任何弱默认值
- **禁止 `docker compose down -v`**：`-v` 参数会删除全部 named volume（MySQL/Redis/uploads 数据全部丢失），生产环境只能用 `docker compose down`（保留数据）

## 前置条件
- 已安装 Docker Engine 20+ 与 Docker Compose v2
- 服务器 4G 内存以上
- 宿主机 Nginx 已配置反向代理到 `127.0.0.1:5173`

## 部署步骤

### 1. 上传项目到服务器

Docker 配置文件均位于项目根目录：
```
Dockerfile              # 后端镜像构建
docker-compose.yml      # 服务编排（唯一配置源）
docker-entrypoint.sh    # 后端容器入口
maven-settings.xml      # Maven 阿里云镜像
frontend/Dockerfile     # 前端镜像构建
frontend/nginx.conf     # 前端容器内 Nginx 配置
src/main/resources/db/  # 数据库初始化脚本
seed-uploads/           # 房源种子图片
pom.xml / src/          # 后端源码
```

### 2. 配置环境变量
```bash
cp deploy/docker/.env.production.example .env
vim .env   # 修改密码、JWT_SECRET、CORS 域名
```

**必须设置的项（缺失时 compose 直接报错停止）：**
- `MYSQL_ROOT_PASSWORD`：MySQL root 密码，建议 `openssl rand -base64 16`
- `JWT_SECRET`：JWT 签名密钥，执行 `openssl rand -base64 48` 生成

**可选覆盖项：**
- `APP_CORS_ALLOWED_ORIGINS`：默认已设为 `https://house.evezero.cn`，如需其他域名可覆盖
- `FRONTEND_PORT`：默认 `5173`，不可设为 80/443

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

# 健康检查（backend 未暴露宿主机端口，通过容器内执行）
docker compose exec backend curl -fsS http://127.0.0.1:8082/api/public/stats

# 前端健康检查
curl http://127.0.0.1:5173/
```

### 5. 宿主机 Nginx 反向代理配置

在宿主机 Nginx 中添加（由运维侧操作，不在容器内）：

```nginx
server {
    listen 80;
    server_name house.evezero.cn www.house.evezero.cn;
    # HTTP → HTTPS 重定向（启用 SSL 后取消注释）
    # return 301 https://$host$request_uri;

    location / {
        proxy_pass http://127.0.0.1:5173;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        # WebSocket 升级（通知/聊天功能依赖）
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

## 数据持久化
- MySQL 数据：Docker volume `housemarket-mysql-data`
- Redis 数据：Docker volume `housemarket-redis-data`
- 上传文件：Docker volume `housemarket-uploads`

> 三个 named volume 在容器重建/镜像更新时均不会丢失数据。
> 生产运维禁止使用 `docker compose down -v`（会删除全部数据卷）。

## 常用运维命令
```bash
# 停止服务（保留数据）
docker compose down

# 更新代码后重新构建并启动
docker compose up -d --build

# 查看各服务日志
docker compose logs -f mysql
docker compose logs -f redis
docker compose logs -f backend
docker compose logs -f frontend

# 重启单个服务
docker compose restart backend

# 进入后端容器
docker compose exec backend sh
```
