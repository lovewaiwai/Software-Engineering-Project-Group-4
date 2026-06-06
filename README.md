# SwapCampus 校园闲置物品交易平台

SwapCampus 是面向校园师生的闲置物品交易平台。本仓库当前已经完成项目代码骨架，目标是让后续不同成员或 agent 可以按模块并行开发后端、前端、数据库、部署和测试内容。

## 一键启动

第一次启动前请先安装：

- Git
- Docker Desktop
- JDK 17 或更高版本，推荐 JDK 17
- Maven 3.9+
- Node.js 20+ 和 npm
- SQL Server Management Studio，简称 SSMS，用来查看 Docker 里的 SQL Server

拉取代码：

```powershell
git clone https://github.com/lovewaiwai/Software-Engineering-Project-Group-4.git
cd Software-Engineering-Project-Group-4
git switch main
```

如果只想用 Docker 启动数据库和对象存储，后端与前端仍在本机运行：

```powershell
docker compose up -d
```

这条命令会自动启动：

- `sqlserver`：Docker 中的 SQL Server 2022。
- `db-init`：重建 `SwapCampus` 数据库，并自动执行 `db/migrations/*.sql` 和 `db/seeds/*.sql`。
- `minio`：本地对象存储服务。

如果想一键启动基础设施、后端和前端，全套都通过 Docker 运行：

```powershell
docker compose --profile app up -d --build
```

这条命令会启动：

- `sqlserver`：SQL Server 数据库。
- `db-init`：数据库初始化脚本。
- `minio`：对象存储服务。
- `backend`：Spring Boot 后端服务。
- `frontend`：Vue 前端静态服务。

查看容器状态：

```powershell
docker compose --profile app ps
```

查看日志：

```powershell
docker compose --profile app logs -f backend frontend
```

停止全套 Docker 服务：

```powershell
docker compose --profile app down
```

启动后用 SSMS 连接 Docker 里的 SQL Server：

```text
Server Name: localhost,1433
Authentication: SQL Server 身份验证
User Name: sa
Password: YourStrong!Passw0rd
Trust Server Certificate: 勾选
```

注意：SSMS 里端口写法是 `localhost,1433`，中间是英文逗号，不是冒号。

启动后端：

```powershell
cd backend
mvn spring-boot:run
```

启动前端：

```powershell
cd frontend
npm install
npm run dev
```

常用地址：

```text
前端: http://localhost:5173
后端: http://localhost:8080
Swagger UI: http://localhost:8080/swagger-ui.html
MinIO 控制台: http://localhost:9001
MinIO 默认账号: minioadmin
MinIO 默认密码: minioadmin
WebSocket 聊天占位: ws://localhost:8080/ws/chat
```

## 数据库脚本机制

Docker 启动时会自动运行 `db-init` 服务。默认行为是先删除并重建 `SwapCampus` 数据库，再按文件名顺序执行：

```text
db/migrations/*.sql
db/seeds/*.sql
```

当前建表脚本：

```text
db/migrations/V001__init.sql
```

这意味着每次执行下面命令都会清空 SQL Server 里的 `SwapCampus` 业务数据，然后重新建表和导入种子数据：

```powershell
docker compose up -d db-init
```

后续要加演示数据时，把 SQL 文件放到：

```text
db/seeds/
```

例如：

```text
db/seeds/V001__demo_users.sql
db/seeds/V002__demo_products.sql
```

然后重新执行：

```powershell
docker compose up -d db-init
```

也可以手动触发数据库脚本同步：

```powershell
.\scripts\init-docker-db.ps1
```

如果想连 SQL Server 数据卷和 MinIO 数据卷一起清空：

```powershell
docker compose down -v
docker compose up -d
```

注意：`docker compose down -v` 会删除 SQL Server 和 MinIO 的 Docker 数据卷，容器里的数据库数据会被清空。

## 技术栈

- 后端：Spring Boot 3、Spring Security、JWT、MyBatis-Plus、Spring WebSocket、Swagger/OpenAPI、SQL Server JDBC。
- 前端：Vue 3、Vite、TypeScript、Element Plus、Pinia、Vue Router、Axios。
- 数据库：SQL Server 2022 Developer/Express，默认通过 Docker 运行。
- 对象存储：MinIO，默认通过 Docker 运行。
- 部署：Docker Compose。
- 测试：JUnit 5、Mockito、Postman/Apifox、k6 预留。

## 目录结构

```text
backend/              Spring Boot 后端工程骨架
frontend/             Vue 3 前端工程骨架
db/migrations/        SQL Server 建表和结构迁移脚本
db/seeds/             演示数据和种子数据脚本
infra/                环境变量示例和部署脚本
scripts/              本地辅助脚本
tests/                接口测试、端到端测试、性能测试资产预留
team_docs/            团队设计文档和任务拆分文档
docker-compose.yml    本地基础设施编排
README.md             项目启动和开发说明
```

## 后端说明

后端配置文件：

```text
backend/src/main/resources/application.yml
```

默认数据库配置已经指向 Docker 映射出来的 SQL Server：

```text
jdbc:sqlserver://localhost:1433;databaseName=SwapCampus;encrypt=true;trustServerCertificate=true
```

默认账号密码：

```text
username: sa
password: YourStrong!Passw0rd
```

如果你修改了 Docker 映射端口，例如 `.env` 里设置 `DB_PORT=11433`，启动后端前也要同步覆盖：

```powershell
$env:DB_URL="jdbc:sqlserver://localhost:11433;databaseName=SwapCampus;encrypt=true;trustServerCertificate=true"
```

后端鉴权已接入最小闭环：

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/logout
GET  /api/users/me
```

注册和登录成功后会返回 `tokenType: Bearer`、`token` 和基础用户信息。调用受保护接口时在请求头携带：

```text
Authorization: Bearer <token>
```

演示账号：

```text
普通买家/卖家: demo_buyer / demo123, demo_seller / demo123
商品审核员: demo_product_reviewer / demo123
默认商品审核员: product_reviewer / Product1234!
系统审核员: reviewer / Admin1234!
管理员: demo_admin / demo123
```

Swagger UI 支持 Bearer Token 认证，打开 `http://localhost:8080/swagger-ui.html` 后点击 Authorize，填入登录接口返回的 JWT 即可调试受保护接口。

开发环境 CORS 已允许前端 `http://localhost:5173` 和 `http://127.0.0.1:5173` 调用后端。JWT 可通过环境变量覆盖：

```powershell
$env:JWT_SECRET="长度足够的本地开发密钥"
$env:JWT_EXPIRATION_MINUTES="120"
```

后端检查：

```powershell
cd backend
mvn test
mvn -DskipTests package
```

## 前端说明

启动：

```powershell
cd frontend
npm install
npm run dev
```

构建检查：

```powershell
npm run build
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
- `JwtAuthenticationFilter` 和 `CurrentUserContext`：Bearer Token 解析与当前用户上下文。
- `SecurityConfig`：Spring Security、CORS、统一认证失败响应配置。
- `MybatisPlusConfig`：MyBatis-Plus 分页配置。
- `OpenApiConfig`：Swagger/OpenAPI 与 Bearer Token 认证配置。
- `WebSocketConfig` 和 `ChatWebSocketHandler`：聊天 WebSocket 占位。
- `MinioProperties`：MinIO 配置占位。
- `AuditLogService`：审计日志公共写入服务，当前提供 `record(operatorId, action, targetType, targetId, detail)`。

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

协作语言约定：

- 提交信息、PR 描述、审计说明、交付总结、任务评论优先使用中文。
- Git 提交信息可以保留英文类型前缀，但正文说明请写中文，例如 `feat: 完成商品发布接口`。
- 后续 agent 请优先阅读根目录 `AGENTS.md`。

## 备用：使用本机 SQL Server

默认推荐使用 Docker SQL Server。如果不使用 Docker 中的 SQL Server，而是使用自己电脑上安装的 SQL Server，并通过 SSMS 管理，可以使用脚本自动执行建库建表：

```powershell
.\scripts\init-local-sqlserver.ps1 -ServerInstance "localhost"
```

SQL Server Express 常见实例名：

```powershell
.\scripts\init-local-sqlserver.ps1 -ServerInstance ".\SQLEXPRESS"
```

如果使用 SQL Server 账号密码登录：

```powershell
.\scripts\init-local-sqlserver.ps1 -ServerInstance "localhost" -SqlAuth -Username "sa" -Password "YourStrong!Passw0rd"
```

本机 SQL Server 方案需要自己同步后端的 `DB_URL`，课程项目默认不走这条路线。

## CI/CD 自动化测试

仓库已添加 GitHub Actions 流水线：`.github/workflows/ci.yml`。每次向 `main` 分支 push，或向 `main` 发起 Pull Request 时，会自动执行：

- 后端测试与覆盖率：进入 `backend` 目录运行 `mvn -B verify`，通过 JaCoCo 生成覆盖率报告，报告位置为 `backend/target/site/jacoco/index.html`。
- 前端构建：进入 `frontend` 目录运行 `npm ci` 和 `npm run build`，覆盖 TypeScript 类型检查与 Vite 构建。
- Docker 镜像构建：仅在 push 到 `main` 时运行 `docker compose --profile app build backend frontend`，验证前后端镜像可以正常打包。

在 GitHub 仓库页面点击 `Actions` 标签页即可查看每次流水线运行结果。后续如果补充更多后端单元测试、前端测试或接口测试，只要把测试命令接入该 workflow，提交代码时就会自动检查。

## 当前 TODO

- Agent A 后续可补充基于方法注解的角色权限矩阵，例如后台接口限制 `ADMIN` / `SYS_ADMIN`。
- MyBatis-Plus 当前依赖 SQL Server 默认值写入 `created_at`、`updated_at`，后续如需要统一更新时间，可补简单 `MetaObjectHandler`。
- `users`、`user_profiles`、`audit_logs` 已替换为真实表实体；其他业务模块占位 Entity 仍需按各自 Agent 任务替换。
- 按 D4-D5 接口草案实现各模块 Controller 和 Service。
- 增加 SQL Server 种子数据。
- 增加 Postman/Apifox 接口测试集合。
- 增加核心业务单元测试和订单状态机测试。
- 增加 MinIO 图片上传接口。
- 完成 WebSocket 聊天消息持久化和未读数。
- 完成 Docker Compose app profile 的前后端联调验证。
