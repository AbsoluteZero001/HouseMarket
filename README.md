# 房屋租售系统

一个基于Spring Boot开发的现代化房屋租售系统，支持租客和房东两种角色，提供房源管理、预约看房、收藏管理等功能。

## 🌟 项目特性

- 🎨 **现代化UI设计**：采用渐变背景、粒子动画等现代设计元素
- 👥 **双角色系统**：支持租客和房东两种角色，功能分离清晰
- 🔒 **安全认证**：基于JWT的身份认证和授权
- 📱 **响应式设计**：适配不同屏幕尺寸
- ⚡ **高性能**：使用Spring Boot 3和Java 21，提供卓越性能
- 📦 **完整功能**：包含房源管理、预约看房、收藏管理等核心功能
- 📋 **RESTful API**：提供完整的API文档，便于二次开发

## 🛠️ 技术栈

### 后端
- **框架**：Spring Boot 3.2.4
- **开发语言**：Java 21
- **ORM框架**：MyBatis Plus 3.5.5
- **数据库**：MySQL
- **安全框架**：Spring Security
- **认证方式**：JWT
- **API文档**：Swagger/OpenAPI 3
- **缓存**：Spring Cache
- **构建工具**：Maven

### 前端
- **HTML5/CSS3**：现代化的页面结构和样式
- **JavaScript**：原生JavaScript，无框架依赖
- **图标库**：Font Awesome 6
- **动画效果**：CSS动画和过渡效果

## 📁 项目结构

```
SpringBoot-HouseMarket/
├── src/
│   ├── main/
│   │   ├── java/com/springboot/springboothousemarket/
│   │   │   ├── Config/           # 配置类
│   │   │   ├── Controller/       # 控制器
│   │   │   ├── Entity/           # 实体类
│   │   │   ├── Mapper/           # MyBatis映射接口
│   │   │   ├── Service/          # 业务逻辑层
│   │   │   ├── Util/             # 工具类
│   │   │   ├── dto/              # 数据传输对象
│   │   │   └── SpringBootHouseMarketApplication.java  # 主启动类
│   │   └── resources/
│   │       ├── mapper/           # MyBatis XML映射文件
│   │       ├── static/           # 静态资源
│   │       │   ├── assets/       # 静态资源文件
│   │       │   ├── css/          # CSS样式文件
│   │       │   ├── admin.html     # 管理员页面
│   │       │   ├── house-detail.html  # 房源详情页
│   │       │   ├── landlord.html  # 房东页面
│   │       │   ├── login.html     # 登录页
│   │       │   ├── register.html  # 注册页
│   │       │   └── tenant.html    # 租客页面
│   │       └── application.yml   # 应用配置
│   └── test/                     # 测试代码
├── .gitignore                    # Git忽略文件
├── mvnw                          # Maven包装器（Windows）
├── mvnw.cmd                      # Maven包装器（Linux/Mac）
├── pom.xml                       # Maven依赖配置
└── README.md                     # 项目说明文档
```

## 🚀 快速开始

### 环境要求

- Java 21+
- Maven 3.6+
- MySQL 8.0+

### 安装部署

1. **克隆项目**

   ```bash
   git clone https://github.com/your-username/SpringBoot-HouseMarket.git
   cd SpringBoot-HouseMarket
   ```

2. **配置数据库**

   - 创建名为`房源市场`的数据库
   - 修改`application.yml`中的数据库连接配置（可选，默认从环境变量获取）

   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/房源市场?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
       username: your-username
       password: your-password
   ```

3. **构建项目**

   ```bash
   mvn clean install
   ```

4. **运行项目**

   ```bash
   mvn spring-boot:run
   ```

   或运行打包后的jar文件：

   ```bash
   java -jar target/SpringBoot-HouseMarket-0.0.1-SNAPSHOT.jar
   ```

5. **访问系统**

   项目启动后，会自动打开浏览器访问登录页面：
   ```
   http://localhost:8082/login.html
   ```

### API文档

项目集成了Swagger/OpenAPI，可通过以下地址访问API文档：
```
http://localhost:8082/swagger-ui.html
```

## 📖 功能说明

### 租客功能

- 🔍 **房源搜索**：根据关键词、户型、面积、价格等条件搜索房源
- 💖 **收藏管理**：收藏喜欢的房源，方便后续查看
- 📅 **预约看房**：预约看房时间和地点
- 👤 **个人中心**：修改密码、查看个人信息

### 房东功能

- 🏠 **房源管理**：发布、编辑、删除房源信息
- 📊 **预约管理**：查看、批准或拒绝看房预约
- 👤 **个人中心**：修改密码、查看个人信息

## 🎯 使用指南

### 1. 注册登录

- 访问登录页面：`http://localhost:8082/login.html`
- 点击"注册"链接进入注册页面
- 填写注册信息，选择角色（租客或房东）
- 注册成功后自动跳转到登录页面
- 使用注册的账号密码登录

### 2. 租客操作

- **浏览房源**：登录后进入租客首页，浏览房源列表
- **搜索房源**：使用搜索栏根据条件筛选房源
- **收藏房源**：点击房源卡片上的"收藏"按钮
- **预约看房**：点击"预约看房"按钮，填写预约信息
- **查看预约**：点击"预约记录"查看已预约的看房信息

### 3. 房东操作

- **发布房源**：登录后进入房东首页，点击"发布新房源"
- **管理房源**：点击"房源管理"查看和管理已发布的房源
- **处理预约**：点击"预约记录"查看和处理看房预约

## 🛡️ 安全配置

- **JWT认证**：使用JWT进行身份认证，Token有效期默认24小时
- **密码加密**：密码存储使用BCrypt加密
- **CORS配置**：支持跨域请求
- **SQL注入防护**：使用MyBatis Plus的参数化查询

## ⚙️ 配置说明

### 主要配置项

| 配置项 | 说明 | 默认值 |
|-------|------|--------|
| `server.port` | 服务器端口 | 8082 |
| `spring.datasource.url` | 数据库连接URL | jdbc:mysql://localhost:3306/房源市场 |
| `spring.datasource.username` | 数据库用户名 | - |
| `spring.datasource.password` | 数据库密码 | - |
| `spring.application.name` | 应用名称 | SpringBoot-HouseMarket |
| `springdoc.swagger-ui.path` | Swagger UI路径 | /swagger-ui.html |

## 📝 开发指南

### 代码规范

- 遵循Spring Boot最佳实践
- 使用Lombok简化代码
- 接口返回统一格式
- 异常处理使用@RestControllerAdvice

### 数据库设计

主要实体类：

- `Users`：用户信息
- `Houses`：房源信息
- `Appointment`：预约记录
- `Favorites`：收藏信息

## 🤝 贡献

欢迎提交Issue和Pull Request！

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用MIT许可证，详情请查看[LICENSE](LICENSE)文件。

## 📞 联系方式

如有问题或建议，欢迎通过以下方式联系：

- GitHub Issues: https://github.com/AbsoluteZero001/HouseMarket/issues
- Email: absolutezero.cold200@simplelogin.com

## 📋 更新日志

### v0.0.1-SNAPSHOT

- 初始版本
- 实现租客和房东角色功能
- 房源管理、预约看房、收藏管理等核心功能
- 现代化UI设计

---

**感谢使用房屋租售系统！** 🎉
