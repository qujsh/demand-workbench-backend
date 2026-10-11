# Demand Workbench Backend

Java 21 + Spring MVC + Spring AI + MyBatis + MySQL。后端以异步任务调用 DeepSeek，将需求文本拆解为四级工作项树。

## 项目规范

- [架构与依赖方向](docs/architecture.md)
- [Java/Spring/MyBatis 编码规范](docs/coding-standards.md)
- [AI 接入与提示词规范](docs/ai-integration.md)
- [接口契约](docs/api.md)

根目录 `AGENTS.md` 是提供给 AI 编码工具的强约束入口，详细说明仍以 `docs/` 为准。

## 环境变量

```bash
export MYSQL_URL='jdbc:mysql://localhost:3306/demand_workbench?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false'
export MYSQL_USERNAME='demand'
export MYSQL_PASSWORD='demand_dev'
export DEEPSEEK_API_KEY='你的 DeepSeek API Key'
export DEEPSEEK_MODEL='deepseek-chat'
```

## Docker Compose 管理 MySQL

以下命令均在项目根目录执行：

```bash
cd /Users/mainto/javafile/demand-workbench-backend
```

### 创建并启动

```bash
docker compose up -d mysql
```

查看容器状态，MySQL 正常启动后应显示为 `healthy`：

```bash
docker compose ps
```

查看启动日志：

```bash
docker compose logs -f mysql
```

### 停止

停止容器但不删除容器：

```bash
docker compose stop mysql
```

重新启动已停止的容器：

```bash
docker compose start mysql
```

### 删除容器

停止并删除当前 Compose 项目创建的容器和网络：

```bash
docker compose down
```

上述命令不会删除 `docker/mysql/data` 中的数据库文件。再次执行 `docker compose up -d mysql` 后会继续使用原有数据。

当前项目使用宿主机目录映射，而不是 Docker 命名卷，因此 `docker compose down -v` 也不会清理 `docker/mysql/data`。如需重置数据库，应先执行 `docker compose down`，确认数据不再需要并完成必要备份后，再手动删除该目录中的数据库文件。

## 启动后端

```bash
./mvnw spring-boot:run
```

`mvnw` 在 macOS 上会自动选择本机的 Java 21，避免当前终端仍指向旧 Java 8。

接口：

```text
POST /api/v1/requirements/decompose
GET  /api/v1/requirements/decompose?taskId=DEC-xxx
```

提交示例：

```bash
curl -X POST http://localhost:8080/api/v1/requirements/decompose \
  -H 'Content-Type: application/json' \
  -d '{"content":"需要拆解的需求文本"}'
```
