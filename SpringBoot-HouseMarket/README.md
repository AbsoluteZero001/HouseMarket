# SpringBoot-HouseMarket

北京房源市场：Spring Boot 3 + Vue 3 的租房信息与预约审批平台。房东发布房源，租客在线预约看房，房东在审批工作台完成审批，审批结果通过
WebSocket 实时通知，全流程状态一致且可追溯。

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

### 4. 演示账号

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
5. 状态流转使用事务 + 条件更新保证原子性和状态一致性。

面试技术选型与演进建议见 [docs/INTERVIEW_TECH.md](docs/INTERVIEW_TECH.md)。
