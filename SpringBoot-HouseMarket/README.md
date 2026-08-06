# SpringBoot-HouseMarket

北京房源市场：Spring Boot 3 + Vue 3 的租房信息与预约审批平台。房东发布房源，租客在线预约看房，房东在审批工作台完成审批，审批结果通过事务 Outbox 异步投递，全流程状态一致且可追溯。

已按真实企业项目演进方式落地：

- 登录/注册已移除验证码，改为接口限流保护。
- 预约表带乐观锁 `version`，并发审批不会互相覆盖。
- 预约提交支持幂等键 `requestId`，重复点击不会生成重复订单。
- 通知使用事务 Outbox + 定时异步投递，状态变更和通知入队在同一事务中。
- 租客端和房东端新增“通知中心”，可查看通知历史。
- 房东注册改为“入驻申请 → 管理员审核”，审核通过后才能发布房源。
- 管理端新增“房东审核”页，审核结果进入通知中心闭环。
- 房源详情浏览自动累计 `views`，并与缓存联动刷新。
- 房源列表、详情、首页统计接入 Spring Cache；默认内存缓存，Redis profile 可切到 Redis。
- 提供 `docker-compose.yml`，一条命令启动 MySQL + Redis 的演示环境。
- GitHub Actions CI：后端测试 + 前端构建自动执行。

## 克隆后一键复现

仓库已包含初始化数据、房源图片和全部页面背景，克隆后按下面步骤即可看到与作者机器一致的效果。

### 1. 初始化数据库

前置条件：本机已安装 MySQL 8，`root / 123456` 可连接。

```bash
mysql -uroot -p123456 < src/main/resources/db/schema.sql
mysql -uroot -p123456 < src/main/resources/db/data.sql
```

`schema.sql` 会重建 `housemarket` 库，包含用户、房源、预约、流程轨迹、收藏表；`data.sql` 写入北京各城区房源、用户和预约演示数据。数据库连接配置在
`src/main/resources/application.yml`，如需改密码请同步修改。

### 2. 启动后端

```bash
mvn spring-boot:run
```

默认端口 `8082`，Swagger 地址：http://localhost:8082/swagger-ui.html

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

默认端口 `5173`：http://localhost:5173

### 4. 可选：启用 Redis 缓存与分布式限流

```bash
docker compose up -d redis
```

然后以后端 Redis profile 启动：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=redis
```

不启用 Redis 也能运行，默认使用内存缓存和单机限流。

### 5. 演示账号

| 角色  | 用户名         | 密码         |
|-----|-------------|------------|
| 管理员 | `admin`     | `admin123` |
| 房东  | `landlord1` | `123456`   |
| 租客  | `tenant1`   | `123456`   |

## 资源说明

- 房源图片：`uploads/` 已提交到 Git，数据库中的 `/uploads/*.png` 路径可直接访问。
- 页面背景：`frontend/public/backgrounds/` 已提交，覆盖首页、登录/注册、租客端、房东端和管理端。
- 背景再生成：运行 `python frontend/scripts/generate-backgrounds.py`（需要 Pillow）可重新生成海报级背景。
- 初始化数据：`src/main/resources/db/schema.sql` 与 `src/main/resources/db/data.sql`。

## 核心流程

发布 → 预约 → 审批 → 通知：

1. 房东发布房源，房源进入在线状态。
2. 租客在房源详情页提交预约。
3. 房东在审批工作台批准、拒绝，完成看房后标记完成。
4. 每一步写入 `appointment_flow` 轨迹表，双方可查看完整时间线。
5. 状态流转使用乐观锁版本号 + 事务保证原子性和状态一致性。
6. 通知写入 `notification_outbox`，由 `NotificationOutboxProcessor` 异步投递 WebSocket。

面试技术选型与演进建议见 [docs/INTERVIEW_TECH.md](docs/INTERVIEW_TECH.md)。
