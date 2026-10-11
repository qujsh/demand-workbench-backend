# 项目开发约束

## 修改前必读

修改生产代码前，应根据变更范围阅读以下文档：

- 项目架构：[docs/architecture.md](docs/architecture.md)
- 编码规范：[docs/coding-standards.md](docs/coding-standards.md)
- 修改提示词、模型或 AI 结果解析时：[docs/ai-integration.md](docs/ai-integration.md)
- 修改 HTTP 接口契约时：[docs/api.md](docs/api.md)

## 强制规则

- 使用 Java 21，以及 `pom.xml` 中统一管理的依赖版本。
- 代码先按业务模块组织，再在模块内部划分 `controller`、`application`、`domain` 和 `infrastructure`。
- `domain` 和 `application` 层不得依赖 Spring MVC、MyBatis 或具体 AI 服务商的类。
- Controller 只负责传输层参数校验、调用应用服务和响应转换，不编写业务逻辑。
- 所有 AI 服务商必须实现 `WorkItemGenerator`；应用服务不得直接调用服务商 SDK。
- 提示词统一存放在 `src/main/resources/prompts`，提示词行为变化时必须更新版本号。
- 数据库结构只能通过 Flyway 迁移文件变更；已经发布的迁移文件不得修改。
- 不得记录 API Key、完整需求原文或包含敏感信息的完整模型响应。
- 对外返回稳定的内部错误码，不得直接返回 AI 服务商的原始错误内容。
- 状态流转、工作项树校验、接口契约和持久化逻辑变更时，必须增加或更新测试。
- 除非产品接口契约被明确修改，否则必须保留 `POST /api/v1/requirements/decompose` 和 Query 轮询方式。
