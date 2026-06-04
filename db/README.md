# 数据库脚本与演示账号

`db/migrations/` 存放结构迁移脚本，`db/seeds/` 存放演示数据脚本。执行下面命令时，`db-init` 会重建 `SwapCampus` 数据库，并按文件名顺序执行迁移和种子数据：

```powershell
docker compose up -d db-init
```

如果使用全套 Docker 一键启动，也会执行同样的数据库初始化流程：

```powershell
docker compose --profile app up -d --build
```

注意：当前初始化流程会清空并重建 `SwapCampus` 数据库，已有业务数据会被覆盖。

## 演示账号

`db/seeds/V001__demo_accounts.sql` 会写入以下账号：

| 用途 | 用户名 | 密码 | 说明 |
| --- | --- | --- | --- |
| 普通买家 | `demo_buyer` | `User1234!` | 测试用户端、个人页、积分、从商品页联系卖家、发起聊天 |
| 普通卖家 | `demo_seller` | `Seller1234!` | 测试聊天另一端、卖家身份、接收买家消息 |
| 管理员/审核员 | `reviewer` | `Admin1234!` | 测试 `/admin` 后台、举报审核、用户封禁/禁言管理 |
| 禁言用户 | `muted_user` | `Muted1234!` | 测试登录正常但发送聊天消息被拦截 |
| 封禁用户 | `banned_user` | `Banned1234!` | 测试登录被拒绝，接口返回 403 |

全新初始化后的常见用户 ID：

```text
1 demo_buyer
2 demo_seller
3 muted_user
4 banned_user
5 reviewer
```

演示商品由 `demo_seller` 发布。建议测试聊天时使用两个浏览器窗口：

- 普通窗口登录 `demo_buyer`
- 无痕窗口登录 `demo_seller`

然后用 `demo_buyer` 打开商品详情页，点击“联系卖家”发起聊天。
