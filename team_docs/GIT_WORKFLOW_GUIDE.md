# Git 上推与分支协作指南

## 1. 仓库信息

远程仓库：

```powershell
https://github.com/lovewaiwai/Software-Engineering-Project-Group-4.git
```

主分支：

```text
main
```

`main` 只放稳定内容。初始化阶段可以由组长直接推送；进入开发后，建议通过任务分支和 Pull Request 合并。

## 2. 第一次拉取项目

选择一个本地目录，例如 `D:\AA_new project`，执行：

```powershell
cd "D:\AA_new project"
git clone https://github.com/lovewaiwai/Software-Engineering-Project-Group-4.git
cd Software-Engineering-Project-Group-4
```

确认当前分支：

```powershell
git status
git branch
```

正常情况下应位于 `main`。

## 3. 每天开始工作前

先同步远程最新内容：

```powershell
git switch main
git pull --rebase origin main
```

如果你正在某个任务分支上，也应先更新 `main`，再从最新 `main` 新建任务分支。

## 4. 分支命名规则

不要长期维护一个巨大的个人分支。每个任务开一个短分支，任务完成后合并并删除。

推荐命名：

```text
docs/srs
docs/database-design
feature/product-publish
feature/order-flow
feature/chat
feature/admin-review
fix/login-error
fix/order-status
```

含义：

- `docs/*`：文档任务。
- `feature/*`：功能开发。
- `fix/*`：缺陷修复。

## 5. 新建任务分支

从最新 `main` 新建：

```powershell
git switch main
git pull --rebase origin main
git switch -c feature/product-publish
```

写完后查看变化：

```powershell
git status
git diff
```

提交：

```powershell
git add .
git commit -m "feat: add product publishing flow"
```

推送分支：

```powershell
git push -u origin feature/product-publish
```

然后在 GitHub 上发 Pull Request，请至少 1 位组员 Review。

## 6. 合并与删除分支

PR 合并后，本地同步并删除已完成分支：

```powershell
git switch main
git pull --rebase origin main
git branch -d feature/product-publish
```

如果远程分支也需要删除：

```powershell
git push origin --delete feature/product-publish
```

## 7. 提交信息规范

推荐格式：

```text
feat: add order creation API
fix: correct credit score update
docs: update SRS use cases
test: add order status tests
chore: update docker compose
```

常用类型：

- `feat`：新增功能。
- `fix`：修复问题。
- `docs`：文档。
- `test`：测试。
- `chore`：配置、构建、杂项。

## 8. 上推前审计清单

每次 push 或 PR 前检查：

- 没有提交 `~$*.docx`、`~$*.xlsx` 等 Office 临时文件。
- 没有提交账号、密码、Token、Cookie。
- 没有把粗糙草稿误放到 `docs/`。
- 修改了业务流程时，同步更新相关文档。
- 修改了数据库结构时，同步更新数据库设计说明。
- 代码能在本地启动或至少通过对应模块测试。
- PR 描述写清楚做了什么、影响了什么、如何验证。

## 9. 文档协作规则

Word 文档不适合多人同时编辑同一个文件。建议：

- 每份 Word 文档指定 1 名负责人。
- 其他人通过评论、Markdown 草稿或 PR 描述提出修改建议。
- 负责人统一合并，避免 `.docx` 冲突。

重要文档节点可以打标签：

```powershell
git tag srs-v1.0
git push origin srs-v1.0
```

建议标签：

- `kickoff`
- `srs-v1.0`
- `design-v1.0`
- `demo-ready`
- `final-v1.0`

## 10. 常见问题

如果 push 时提示权限属于错误账号，例如 `denied to litrixios`，说明 Git Credential Manager 还缓存了旧 GitHub 账号。

可执行：

```powershell
git credential-manager github logout litrixios --no-ui
git push -u origin main
```

然后在弹出的 GitHub 登录窗口里登录有权限的账号。

如果长期出现 TLS 警告，建议恢复证书校验：

```powershell
git config --global http.sslVerify true
```

## 11. 推荐工作节奏

每天：

1. `git pull --rebase origin main`。
2. 创建当天任务分支。
3. 小步提交，不要一天只交一次大包。
4. 当天结束前 push 到远程任务分支。
5. 关键功能完成后发 PR。

组长每天检查：

- main 是否稳定。
- PR 是否有人 Review。
- 文档和代码是否一致。
- 是否有临时文件、敏感信息或无关文件混入。
