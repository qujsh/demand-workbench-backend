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
- 数据库结构只能通过 Flyway 迁移文件变更；首次发布前可整理尚未在共享环境执行的 `V1`，已经发布的迁移文件不得修改或删除。
- Flyway 文件统一使用 `VyyyyMMddHHmm__逻辑说明.sql` 格式，例如 `V202610120100__create_decomposition_tables.sql`；同一分钟存在多份迁移时顺延分钟版本，确保执行顺序明确。
- 数据库不创建物理外键或业务枚举 CHECK 约束；所有表和字段必须包含数据库原生中文注释。
- 不得记录 API Key、完整需求原文或包含敏感信息的完整模型响应。
- 对外返回稳定的内部错误码，不得直接返回 AI 服务商的原始错误内容。
- 状态流转、工作项树校验、接口契约和持久化逻辑变更时，必须增加或更新测试。
- 除非产品接口契约被明确修改，否则必须保留 `POST /api/v1/requirements/decompose` 和 Query 轮询方式。
- 对外业务接口只允许使用 `GET` 和 `POST`；资源编号必须使用 Query 参数传递，禁止使用路径占位符和 `@PathVariable`。
- Query 参数只传递编号和简单筛选条件，复杂业务数据必须使用 JSON 请求体。

## 验证分级

- 用户表达“先看效果”“粗版”“初版”“快速调整”“先生成文件”等意图时，默认采用快速迭代模式，只检查本次变更直接涉及的文件、语法和局部契约，不自动运行完整测试。
- 普通开发验证只运行与本次变更直接相关的最小测试；不得仅因存在完整测试命令就默认执行全部测试、启动完整应用或检查无关模块。
- 只有用户明确要求“定稿”“完整检查”“准备提交”“准备部署”“验收”，或者当前工作已进入交付阶段时，才执行 `./mvnw test` 及相应的完整检查。
- 涉及删除或重命名字段、修改字段类型、收紧非空或唯一约束、迁移存量数据、改变主键或索引语义等高风险数据库变更时，不受快速迭代模式限制，必须执行真实 MySQL 迁移验证并说明原因。
- 新增表、增加允许为空的字段或普通索引等低风险 Flyway 初稿，在快速迭代阶段可以只生成迁移文件并进行静态检查；MySQL 集成测试最迟在定稿或提交前补齐。
