# HouseMarket Linux 生产部署包

本目录包含 HouseMarket 项目的生产部署所需的全部产物与配置，支持两种部署方式：

| 方式 | 目录 | 适用场景 |
| --- | --- | --- |
| **Docker 部署（推荐）** | `docker/` | 环境干净、希望快速启动、数据库/Redis 一并容器化 |
| **原生部署（systemd + Nginx）** | `native/` | 已有 MySQL/Redis、希望精细化控制资源、使用宿主机 Nginx |

---

## 部署包内容

```
deploy/
├── docker/                          # Docker 部署方案
│   ├── docker-compose.yml           # MySQL + Redis + 后端 + 前端编排
│   ├── Dockerfile.backend           # 后端镜像构建
│   ├── Dockerfile.frontend          # 前端镜像构建
│   ├── nginx.conf                   # 前端 Nginx 配置
│   ├── docker-entrypoint.sh         # 后端容器入口（种子图片初始化）
│   ├── maven-settings.xml           # Maven 阿里云镜像
│   └── .env.production.example      # 环境变量模板
├── native/                          # 原生部署方案
│   ├── backend/
│   │   ├── app.jar                  # 后端 Spring Boot JAR（47.63 MB）
│   │   └── application-prod.yml     # 外置配置模板
│   ├── frontend/
│   │   └── dist/                    # 前端生产构建产物（5.26 MB）
│   ├── nginx/
│   │   └── housemarket.conf         # Nginx 生产配置（含 SSL 预留）
│   ├── systemd/
│   │   ├── housemarket.service      # systemd 服务文件
│   │   └── housemarket.env.example  # 环境变量模板
│   └── deploy.sh                    # 一键部署脚本
├── db/                              # 数据库初始化脚本
│   ├── schema.sql                   # 建库建表
│   └── data.sql                     # 演示数据
└── seed-uploads/                    # 房源种子图片
```

---

## 生产环境准备

无论使用哪种部署方式，都需要准备以下环境：

### 服务器要求
- 操作系统：Ubuntu 22.04+ / Debian 11+ / CentOS 7+ / RHEL 8+
- 内存：4 GB 以上（推荐 8 GB）
- 磁盘：20 GB 以上
- 开放端口：80（HTTP）、443（HTTPS）

### 域名与 SSL
- 域名：`house.evezero.cn`
- SSL 证书（推荐 Let's Encrypt 免费证书）：
  ```bash
  apt install certbot python3-certbot-nginx
  certbot --nginx -d house.evezero.cn -d www.house.evezero.cn
  ```

---

## 方式一：Docker 部署（推荐）

详见 [docker/README.md](docker/README.md)

快速步骤：
```bash
# 1. 上传整个项目到服务器
# 2. 配置环境变量
cp deploy/docker/.env.production.example .env
vim .env   # 修改 MYSQL_ROOT_PASSWORD、JWT_SECRET、APP_CORS_ALLOWED_ORIGINS

# 3. 构建并启动
docker compose up -d --build

# 4. 验证
curl http://localhost:8082/api/public/stats
```

---

## 方式二：原生部署（systemd + Nginx）

详见各配置文件内的注释。

### 前置依赖
```bash
# Ubuntu/Debian
apt update
apt install -y openjdk-21-jre nginx mysql-server redis-server

# CentOS/RHEL
yum install -y java-21-openjdk nginx mysql-server redis
```

### 快速步骤
```bash
# 1. 上传 deploy/native/ 到服务器
# 2. 修改配置
vim native/backend/application-prod.yml    # 修改数据库密码、JWT_SECRET、CORS
vim native/systemd/housemarket.env.example # 修改 JAVA_HOME、JWT_SECRET

# 3. 执行部署脚本
cd native
chmod +x deploy.sh
sudo ./deploy.sh

# 4. 配置 Nginx SSL（如需 HTTPS）
certbot --nginx -d house.evezero.cn
```

---

## 生产环境安全检查清单

部署完成后，请确认以下安全项：

- [ ] `JWT_SECRET` 已设置为强随机密钥（`openssl rand -base64 48`）
- [ ] MySQL root 密码非默认值 `123456`
- [ ] Redis 已设置密码（如暴露到公网）
- [ ] `APP_CORS_ALLOWED_ORIGINS` 已设为真实域名，非 `*` 或 `localhost`
- [ ] `SWAGGER_ENABLED=false`（关闭在线接口文档）
- [ ] HTTPS 已启用
- [ ] 后端端口 8082 不直接暴露公网（通过 Nginx 反代）
- [ ] 数据库端口 3306、Redis 端口 6379 不暴露公网

---

## 数据库初始化

首次部署时需要初始化数据库：

```bash
# 方式一：Docker 自动执行
# docker compose up 时会自动执行 schema.sql + data.sql

# 方式二：原生部署手动执行
mysql -u<user> -p<password> < db/schema.sql
mysql -u<user> -p<password> < db/data.sql
```

**注意**：`data.sql` 包含演示数据（10 套房源、演示用户）。生产环境如需清空演示数据，请在导入后手动清理。

---

## 演示账号

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `admin123` |
| 房东 | `landlord1` | `123456` |
| 租客 | `tenant1` | `123456` |

> 生产部署后请立即修改这些默认密码。

---

## 回滚与备份

```bash
# Docker 方式备份数据
docker compose exec mysql mysqldump -uroot -p housemarket > backup.sql

# 原生方式备份
mysqldump -u<user> -p housemarket > backup.sql

# 备份上传文件
tar czf uploads-backup.tar.gz /opt/housemarket/uploads
```
