# T-02 SwapCampus Agent 任务拆分建议

版本：v1.0
日期：2026-06-03

目标：让不同 agent 能按模块并行开发，减少互相踩接口和重复设计。

## 1. 总体原则

- 先约定枚举、表结构、接口路径，再分别实现。
- 每个 agent 只负责自己的模块，不直接修改其他模块业务规则。
- 跨模块调用通过 Service 接口完成，例如订单调用支付、柜机、信用分，不直接写其他模块表。
- 支付、柜机、AI 先实现 Mock Adapter，保证演示闭环。
- 所有模块统一使用 SQL Server 字段命名和 D4-D5 的 API 草案。

## 2. 推荐分工

| Agent | 负责范围 | 主要产出 |
| --- | --- | --- |
| Agent A 架构与公共基础 | Spring Boot 工程、统一响应、异常、JWT、安全、审计、SQL Server 连接 | 后端骨架、公共枚举、权限拦截 |
| Agent B 用户与积分 | 用户、实名验证、信用分、签到任务、兑换 | 用户接口、信用/积分流水、单元测试 |
| Agent C 商品与搜索 | 分类、标签、商品、图片、审核状态、搜索筛选、推荐、AI 建议 | 商品接口、推荐接口、AI Mock |
| Agent D 订单支付交付 | 订单状态机、模拟支付、面交、柜机中转、评价 | 订单接口、PaymentAdapter、LockerAdapter |
| Agent E 聊天举报后台 | WebSocket 聊天、举报、后台审核、用户管理、看板 | 聊天页面接口、后台接口、举报流程 |
| Agent F 前端用户端 | 登录、首页、商品详情、发布、订单、聊天、积分 | Vue 用户端页面 |
| Agent G 前端管理端 | 商品审核、举报处理、用户管理、看板、柜机配置 | Vue 管理端页面 |
| Agent H 测试部署数据 | Docker Compose、MinIO、SQL Server 脚本、种子数据、接口测试、k6 | 一键启动、测试报告、演示数据 |

## 3. 后端开发顺序

1. Agent A 建立工程、连接 SQL Server、完成登录鉴权和统一异常。
2. Agent B 完成用户实名、信用分、积分基础能力。
3. Agent C 完成分类、商品发布、审核、搜索、图片上传。
4. Agent D 完成订单、支付 Mock、面交确认，随后补柜机 Mock。
5. Agent E 完成聊天、举报、后台看板。
6. Agent C/D/B 分别补推荐、AI 建议、游戏化积分兑换。
7. Agent H 补种子数据、接口测试、性能测试。

## 4. 前后端契约

前端只依赖 `/api/**` 与 `/ws/chat`，不要直接拼接后端内部字段。

必须稳定的枚举：

```text
UserStatus: ACTIVE, BANNED
Role: USER, ADMIN, SYS_ADMIN
ProductStatus: DRAFT, PENDING_REVIEW, ACTIVE, REVIEW_REJECTED, LOCKED, SOLD, OFFLINE
OrderStatus: CREATED, SELLER_CONFIRMED, PAYMENT_PENDING, PAID, DELIVERY_PENDING, RECEIVED, COMPLETED, CANCELLED, REFUNDING, REFUNDED, DISPUTED, CLOSED
TradeMode: MEETUP, LOCKER
PaymentStatus: CREATED, SUCCESS, FAILED, REFUNDING, REFUNDED
LockerTaskStatus: RESERVED, STORED, PICKED_UP, CANCELLED
ReportStatus: PENDING, PROCESSING, REJECTED, RESOLVED
MessageType: TEXT, IMAGE, EMOJI
```

## 5. 最小演示路径

必须优先保证这条路径可跑：

```text
注册登录 -> 实名验证 -> 卖家发布商品 -> 管理员审核通过
-> 买家搜索商品 -> 聊天 -> 下单 -> 卖家确认
-> 模拟支付 -> 面交/柜机交付 -> 确认收货 -> 评价 -> 信用分更新
-> 举报商品 -> 管理员处理 -> 审计日志
```

## 6. 附加项实现边界

| 附加项 | 默认实现 | 可升级方向 |
| --- | --- | --- |
| 个性化推荐 | 浏览/收藏/分类偏好加权排序 | 协同过滤或向量推荐 |
| AI 分类定价 | 关键词规则 + 同类均价 | 接入真实大模型或机器学习模型 |
| 柜机中转 | Mock 柜机站点、柜门、二维码文本、取件码 | 接入真实柜机开放平台 |
| 信用积分游戏化 | 签到、任务、兑换虚拟权益 | 排行榜、勋章体系 |
| 支付 | Mock 支付单、模拟成功、退款流水 | 支付宝/微信/校园卡 |

## 7. 每个 agent 的验收标准

- 有接口或页面可以演示，不只提交空壳。
- 核心规则有单元测试或接口测试记录。
- 新增表字段同步更新数据库设计。
- 新增接口同步更新接口草案。
- 不破坏最小演示路径。
