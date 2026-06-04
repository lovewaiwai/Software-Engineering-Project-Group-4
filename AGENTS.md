# SwapCampus Agent 协作提示

本文件用于提示后续参与本仓库的代码 agent 或成员。

## 语言约定

- 提交信息、PR 描述、审计说明、交付总结、任务评论优先使用中文。
- Git 提交信息可以保留英文类型前缀，但正文说明请写中文，例如：

```text
feat: 完成用户登录接口
fix: 修复订单状态流转校验
docs: 更新 Docker 一键启动说明
test: 增加支付 Mock 单元测试
chore: 调整数据库初始化脚本
```

## 开发约定

- 新增接口、字段、枚举、表结构时，优先参考 `team_docs/D4-D5_架构详细数据库接口设计.md`。
- 不要删除或重写 `team_docs/` 中已有设计文档。
- 不要把临时文件、账号密码、Token、Cookie、构建产物提交到仓库。
- 跨模块调用优先通过 Service 或 Adapter，不直接改其他模块内部实现。
- 完成代码修改后，尽量说明验证方式，例如后端 `mvn test`、前端 `npm run build`、数据库 `docker compose up -d db-init`。
