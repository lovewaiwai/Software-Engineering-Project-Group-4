# SwapCampus 启动说明

本 README 只保留本地启动项目所需的最少步骤。

## 1. 准备环境

请先安装：

- Git
- Docker Desktop

如果只使用 Docker 一键启动，不需要额外安装 JDK、Maven、Node.js。

## 2. 拉取代码

```powershell
git clone https://github.com/lovewaiwai/Software-Engineering-Project-Group-4.git
cd Software-Engineering-Project-Group-4
git switch main
```

如果已经拉取过仓库：

```powershell
git pull origin main
```

## 3. 配置环境变量

第一次启动前，复制环境变量示例文件：

```powershell
Copy-Item infra\.env.example .env
```

默认端口：

```text
前端: http://localhost:5173
后端: http://localhost:8080
MinIO 控制台: http://localhost:9001
SQL Server: localhost,1433
```

如果 Windows 提示 `5173` 端口不可用，可以打开 `.env`，把：

```text
FRONTEND_PORT=5173
```

改成：

```text
FRONTEND_PORT=18080
```

之后访问：

```text
http://localhost:18080
```

## 4. 一键启动

在项目根目录执行：

```powershell
docker compose --profile app up -d --build
```

该命令会自动启动：

- SQL Server 数据库
- 数据库初始化脚本
- MinIO 对象存储
- Spring Boot 后端
- Vue 前端

启动完成后查看状态：

```powershell
docker compose --profile app ps
```

前端状态为 `Up` 后，在浏览器打开：

```text
http://localhost:5173
```

如果你修改过 `.env` 里的 `FRONTEND_PORT`，请使用修改后的端口。

## 5. 常用账号

```text
普通用户/买家: demo_buyer / demo123
普通用户/卖家: demo_seller / demo123
商品审核员: demo_product_reviewer / demo123
系统管理员: demo_sysadmin / demo123
```

## 6. 常用命令

查看日志：

```powershell
docker compose --profile app logs -f backend frontend
```

停止服务：

```powershell
docker compose --profile app down
```

重新初始化数据库：

```powershell
docker compose up -d db-init
```

清空数据库和 MinIO 数据后重新启动：

```powershell
docker compose --profile app down -v
docker compose --profile app up -d --build
```

注意：`down -v` 会删除 Docker 数据卷，数据库和 MinIO 里的数据都会被清空。

## 7. 常见问题

### 端口被占用或被 Windows 保留

如果出现类似错误：

```text
ports are not available
```

请修改 `.env` 中对应端口，例如：

```text
FRONTEND_PORT=18080
DB_PORT=14433
```

然后重新启动：

```powershell
docker compose --profile app up -d --build
```

### 前端启动了但页面访问不了

先查看容器状态：

```powershell
docker compose --profile app ps
```

再查看日志：

```powershell
docker compose --profile app logs -f frontend backend
```

### Docker 构建过慢

可以先确认 Docker Desktop 已正常运行，并保持网络可用。前端依赖安装会使用镜像源：

```text
https://registry.npmmirror.com
```
