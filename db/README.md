# 数据库脚本说明

本项目当前采用“本地演示库固定重建”的初始化方式。

执行 `db-init` 时会先删除并重建 `SwapCampus` 数据库，然后按文件名顺序执行：

```text
db/migrations/V001__init.sql
db/seeds/V001__demo_accounts.sql
```

因此：

- `db/migrations/V001__init.sql` 是唯一结构初始化脚本，新增表、字段、索引时直接合并到这里。
- `db/seeds/V001__demo_accounts.sql` 面向空库直接插入演示数据，不再写 `IF NOT EXISTS` 幂等判断。
- 每次执行 `docker compose up -d db-init` 都会覆盖本地业务数据。

全套 Docker 启动也会触发同样流程：

```powershell
docker compose --profile app up -d --build
```

常用演示账号密码统一为 `demo123`：

| 用途 | 用户名 |
| --- | --- |
| 普通买家 | `demo_buyer` |
| 普通卖家 | `demo_seller` |
| 新普通用户 | `new_user` |
| 禁言用户 | `muted_user` |
| 封禁用户 | `banned_user` |
| 商品审核员 | `demo_product_reviewer` |
| 系统管理员 | `demo_admin` |
| 超级管理员 | `demo_sysadmin` |

学生认证 mock 学籍也在 seed 中初始化。未绑定测试学籍：

```text
学号: 20269999
姓名: 未绑定学生
教务密码: demo123
```
