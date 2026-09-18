#!/bin/bash
# ============================================================
# HouseMarket 原生部署脚本（systemd + Nginx）
# 适用环境：Ubuntu / Debian / CentOS / RHEL
# 前置条件：已安装 Java 21、Nginx、MySQL 8、Redis
# ============================================================

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}=== HouseMarket 部署脚本 ===${NC}"

# 配置变量（根据实际情况修改）
INSTALL_DIR="/opt/housemarket"
SERVICE_USER="housemarket"
DB_NAME="housemarket"
DB_USER="housemarket"
DB_PASS="changeme_strong_password"

# 检查是否 root
if [ "$EUID" -ne 0 ]; then
    echo -e "${RED}请使用 root 运行此脚本${NC}"
    exit 1
fi

# 1. 创建系统用户
echo -e "${YELLOW}[1/7] 创建服务用户...${NC}"
id "$SERVICE_USER" &>/dev/null || useradd -r -s /bin/false "$SERVICE_USER"

# 2. 创建目录
echo -e "${YELLOW}[2/7] 创建目录结构...${NC}"
mkdir -p "$INSTALL_DIR"/{backend,frontend,uploads,logs}
mkdir -p /etc/housemarket

# 3. 部署后端
echo -e "${YELLOW}[3/7] 部署后端 JAR 与配置...${NC}"
cp backend/app.jar "$INSTALL_DIR/backend/app.jar"
cp backend/application-prod.yml "$INSTALL_DIR/backend/application-prod.yml"
cp systemd/housemarket.service /etc/systemd/system/housemarket.service
cp systemd/housemarket.env.example /etc/housemarket/housemarket.env
chown -R "$SERVICE_USER":"$SERVICE_USER" "$INSTALL_DIR"
chmod 600 /etc/housemarket/housemarket.env

# 4. 部署前端
echo -e "${YELLOW}[4/7] 部署前端静态资源...${NC}"
rm -rf "$INSTALL_DIR/frontend/dist"
cp -r frontend/dist "$INSTALL_DIR/frontend/dist"

# 5. 部署 Nginx 配置
echo -e "${YELLOW}[5/7] 部署 Nginx 配置...${NC}"
cp nginx/housemarket.conf /etc/nginx/conf.d/housemarket.conf
nginx -t && systemctl reload nginx

# 6. 初始化数据库（仅首次部署执行）
echo -e "${YELLOW}[6/7] 初始化数据库（如已存在请跳过）...${NC}"
read -p "是否初始化数据库？会清空 $DB_NAME 库 [y/N] " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    mysql -uroot -p <<EOF
CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '$DB_USER'@'localhost' IDENTIFIED BY '$DB_PASS';
GRANT ALL PRIVILEGES ON $DB_NAME.* TO '$DB_USER'@'localhost';
FLUSH PRIVILEGES;
EOF
    mysql -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" < ../db/schema.sql
    mysql -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" < ../db/data.sql
    echo -e "${GREEN}数据库初始化完成${NC}"
fi

# 7. 启动服务
echo -e "${YELLOW}[7/7] 启动后端服务...${NC}"
systemctl daemon-reload
systemctl enable housemarket
systemctl start housemarket

sleep 3
if systemctl is-active --quiet housemarket; then
    echo -e "${GREEN}部署成功！后端服务已启动${NC}"
    echo -e "前端访问: http://$(hostname -I | awk '{print $1}')"
else
    echo -e "${RED}后端服务启动失败，请检查日志：journalctl -u housemarket -n 50${NC}"
    exit 1
fi
