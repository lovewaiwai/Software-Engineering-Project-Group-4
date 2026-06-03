# SwapCampus 校园闲置物品交易平台

SwapCampus 是面向校园师生的闲置物品交易平台。本仓库当前已经完成项目代码骨架，目标是让后续不同成员或 agent 可以按模块并行开发后端、前端、数据库、部署和测试内容。

## 技术栈

- 后端：Spring Boot 3、Spring Security、JWT、MyBatis-Plus、Spring WebSocket、Swagger/OpenAPI、SQL Server JDBC。
- 前端：Vue 3、Vite、TypeScript、Element Plus、Pinia、Vue Router、Axios。
- 数据库：SQL Server 2022 Developer/Express。
- 对象存储：MinIO，本地通过 Docker 启动。
- 部署：Docker Compose，默认提供 SQL Server 和 MinIO；前后端容器通过 `app` profile 预留。
- 测试：JUnit 5、Mockito、Postman/Apifox、k6 预留。

## 目录结构

```text
backend/              Spring Boot 后端工程骨架
frontend/             Vue 3 前端工程骨架
db/migrations/        SQL Server 初始化脚本
infra/                环境变量示例和部署配置
tests/                接口测试、端到端测试、性能测试资产预留
team_docs/            团队设计文档和任务拆分文档
docker-compose.yml    本地基础设施编排
README.md             项目启动和开发说明
```

## 环境准备

建议本机安装：

- Git
- JDK 17 或更高版本，推荐 JDK 17
- Maven 3.9+
- Node.js 20+ 和 npm
- Docker Desktop
- SQL Server Management Studio 或 Azure Data Studio

拉取当前骨架分支：

```powershell
git clone https://github.com/lovewaiwai/Software-Engineering-Project-Group-4.git
cd Software-Engineering-Project-Group-4
git switch feature/project-code-scaffold
```

## 本地基础设施启动

启动 SQL Server 和 MinIO：

```powershell
docker compose up -d sqlserver minio
```

SQL Server 默认连接信息：

```text
host: localhost
port: 1433
database: SwapCampus
username: sa
password: YourStrong!Passw0rd
```

数据库容器启动后，使用 SSMS、Azure Data Studio 或 `sqlcmd` 执行初始化脚本：

```text
db/migrations/V001__init.sql
```

MinIO 控制台：

```text
http://localhost:9001
```

默认账号密码：

```text
minioadmin / minioadmin
```

## 后端启动

后端配置文件：

```text
backend/src/main/resources/application.yml
```

可以通过环境变量覆盖数据库和 MinIO 配置：

```powershell
$env:DB_URL="jdbc:sqlserver://localhost:1433;databaseName=SwapCampus;encrypt=true;trustServerCertificate=true"
$env:DB_USERNAME="sa"
$env:DB_PASSWORD="YourStrong!Passw0rd"
$env:MINIO_ENDPOINT="http://localhost:9000"
$env:MINIO_ACCESS_KEY="minioadmin"
$env:MINIO_SECRET_KEY="minioadmin"
```

启动后端：

```powershell
cd backend
mvn spring-boot:run
```

后端检查：

```powershell
mvn test
mvn -DskipTests package
```

常用地址：

```text
后端服务: http://localhost:8080
Swagger UI: http://localhost:8080/swagger-ui.html
OpenAPI JSON: http://localhost:8080/v3/api-docs
WebSocket 聊天占位: ws://localhost:8080/ws/chat
```

## 前端启动

安装依赖并启动开发服务器：

```powershell
cd frontend
npm install
npm run dev
```

前端构建检查：

```powershell
npm run build
```

默认访问地址：

```text
http://localhost:5173
```

当前前端已预留以下路由：

```text
/login
/verify
/
/products
/products/:id
/products/new
/orders
/orders/:id
/chat
/profile/:id
/points
/admin
/admin/products
/admin/reports
/admin/users
/admin/lockers
```

## 后端模块边界

后端统一包名：

```text
com.swapcampus
```

业务模块：

```text
auth
user
product
search
recommend
order
payment
delivery
chat
review
report
admin
ai
audit
common
```

每个业务模块当前都已建立统一基础结构：

```text
controller/
service/
service/impl/
mapper/
entity/
dto/
vo/
enums/
```

开发约定：

- Controller 负责 HTTP 接口边界、参数接收和响应封装。
- Service 接口作为模块内部和跨模块调用入口。
- Mapper 负责 SQL Server 数据访问。
- Entity、DTO、VO 分别用于数据库实体、请求对象、响应对象。
- 跨模块调用优先通过 Service 或 Adapter，不直接改其他模块内部实现。
- 新增接口、字段和状态枚举时，应同步参考 `team_docs/D4-D5_架构详细数据库接口设计.md`。

## 已预留的公共基础

- `ApiResponse`：统一响应对象。
- `PageResponse`：统一分页响应对象。
- `ErrorCode`：基础错误码。
- `BusinessException` 和 `GlobalExceptionHandler`：业务异常与全局异常处理。
- `JwtTokenProvider`：JWT 生成和解析工具。
- `SecurityConfig`：Spring Security 基础配置。
- `MybatisPlusConfig`：MyBatis-Plus 分页配置。
- `OpenApiConfig`：Swagger/OpenAPI 配置。
- `WebSocketConfig` 和 `ChatWebSocketHandler`：聊天 WebSocket 占位。
- `MinioProperties`：MinIO 配置占位。
- `AuditLogService`：审计日志服务占位。

## Mock Adapter 定位

当前支付、柜机和 AI 都使用 Mock Adapter，目的是保证演示闭环和后续可扩展性。

- `MockPaymentAdapter`：创建模拟支付单、支付链接、支付查询结果和退款结果。
- `MockLockerAdapter`：模拟柜机预约、寄件确认、取件确认。
- `MockAiSuggestAdapter`：根据简单规则返回商品分类、标签和价格区间建议。

后续如果要接入真实服务，只需要新增对应实现类，例如真实支付、真实柜机或真实 AI Provider，并保持 Adapter 接口不变，避免影响商品和订单主流程。

## 后续并行开发建议

建议每个成员从当前骨架分支再切自己的任务分支：

```powershell
git switch feature/project-code-scaffold
git pull
git switch -c feature/user-credit
```

推荐分工：

- 用户与积分：`user`、信用分、积分任务、实名认证。
- 商品与搜索：`product`、`search`、分类、标签、图片上传。
- 推荐与 AI：`recommend`、`ai`、推荐流、AI 分类定价建议。
- 订单支付交付：`order`、`payment`、`delivery`、订单状态机、Mock 支付、柜机中转。
- 聊天举报后台：`chat`、`report`、`admin`、`audit`、WebSocket、举报处理、后台审核。
- 前端用户端：登录、首页、商品、订单、聊天、个人页、积分页。
- 前端管理端：商品审核、举报处理、用户管理、柜机配置、数据看板。
- 测试部署数据：种子数据、接口测试集合、Docker 一键启动、演示脚本、k6。

## 当前 TODO

- 实现真实注册、登录、JWT Filter 和角色鉴权。
- 将占位 Entity 替换为 `V001__init.sql` 对应的真实表实体。
- 按 D4-D5 接口草案实现各模块 Controller 和 Service。
- 增加 SQL Server 种子数据。
- 增加 Postman/Apifox 接口测试集合。
- 增加核心业务单元测试和订单状态机测试。
- 增加 MinIO 图片上传接口。
- 完成 WebSocket 聊天消息持久化和未读数。
- 完成 Docker Compose app profile 的前后端联调验证。
