# 项目开发规范

本文规定 `demand-workbench-backend` 的代码、接口、数据访问和测试约定。

- **必须**：新代码必须遵守，代码评审和测试应阻止违规变更。
- **推荐**：默认采用；确有理由不采用时，应保证代码易读且与所在模块一致。

## 1. Java 规范

### 1.1 基础约定

- **必须**使用 Java 21，以及 `pom.xml` 中统一管理的依赖版本。
- **必须**使用 UTF-8 编码和 LF 换行符，禁止通配符 import。
- 类名使用 `UpperCamelCase`，方法、参数和变量使用 `lowerCamelCase`，常量使用 `UPPER_SNAKE_CASE`。
- 一个源文件只定义一个顶级类型，文件名必须与顶级类型一致。
- 一个方法只承担一个主要职责。出现多层嵌套或难以命名的代码块时，应拆分为语义明确的方法。
- 值对象和只读传输对象推荐使用 `record`；普通类的依赖和不可变状态应使用 `final` 字段。
- 方法不得返回 `null` 集合，应返回空集合。仅用于表示“可能没有结果”的返回值可以使用 `Optional`，不得将其用于字段或方法参数。

### 1.2 Lombok

- 允许使用 Lombok 消除没有业务含义的样板代码，但必须按职责选择精确注解。
- MyBatis Record 可以使用 `@Getter` 和 `@Setter`；错误码和业务枚举可以使用 `@Getter`。
- 不可变 DTO 和值对象优先使用 Java `record`，不得仅为使用 Lombok 而改成可变类。
- 禁止默认使用 `@Data`。只有明确需要 getter、setter、`equals`、`hashCode` 和 `toString` 全部行为的纯数据类才允许使用。
- 包含需求原文、模型响应、密钥或其他敏感字段的类禁止自动生成 `toString`。
- `@RequiredArgsConstructor` 仅用于依赖无歧义的 Spring Bean；存在同类型 Bean、`@Qualifier` 或特殊构造逻辑时保留显式构造器。

### 1.3 注释

- Controller 公共接口、应用服务公共方法、Repository 接口、领域规则和复杂算法必须使用中文 Javadoc。
- 公共方法注释应说明业务用途；存在非显然约束时，还应说明参数、返回值和可能抛出的业务异常。
- 简单 getter、setter、record 访问器，以及语义明确的私有方法可以不写注释。
- 行内注释应解释业务原因、限制条件或特殊处理，不得逐行翻译代码。
- `TODO` 必须说明待办事项和处理条件；不得保留大段被注释掉的代码。

示例：

```java
/**
 * 提交需求拆解任务。
 *
 * @param content 已通过接口校验的需求内容
 * @return 新建任务的编号和初始状态
 * @throws DecompositionException 任务无法加入执行队列时抛出
 */
public SubmitResponse submit(String content) {
    // ...
}
```

### 1.4 错误码

- 错误码必须定义为枚举，枚举常量使用 `UPPER_SNAKE_CASE`，禁止在业务代码中使用裸字符串错误码。
- 错误码枚举包含 `code` 和 `msg` 两个不可变字段，并使用 `@Getter` 生成访问器。
- 对外 `code` 使用项目分配的固定编码，当前需求拆解模块使用 `S048101NNN`；编码一经对外使用不得修改或复用。
- `msg` 是安全、稳定的默认中文文案；需要更具体的信息时可以覆盖文案，但不得改变错误码语义。
- 客户端只能依赖 `code` 判断错误，不得依赖可能调整或国际化的 `msg`。
- 捕获异常后不得静默忽略；转换异常时必须保留原始异常作为 `cause`。
- 不得通过异常消息向调用方暴露堆栈、SQL、API Key、AI 服务商原始错误或内部实现细节。
- 线程收到中断时必须恢复中断标志，或继续抛出 `InterruptedException`。
- 未知系统异常统一返回 `INTERNAL_ERROR`，不得把异常原文作为 `errorMsg`。

错误码示例：

```java
@Getter
public enum DecompositionErrorCode {
    TASK_NOT_FOUND("S048101002", "拆解任务不存在");

    private final String code;
    private final String msg;
}
```

### 1.5 业务枚举

- 固定范围的业务状态必须使用枚举，禁止使用字符串直接比较业务状态。
- 业务枚举统一包含 `code`、`msg` 和 `value`：`code` 用于接口和持久化，`msg` 用于中文展示，`value` 用于明确的排序或层级语义。
- Java 枚举常量名使用 `UPPER_SNAKE_CASE`，`code` 使用稳定的 `lower_snake_case`。
- `value` 使用基本类型 `int`，除非业务明确允许空值。
- 接口、数据库和消息中必须使用 `code`，不得使用 `name()`、`ordinal()` 或 `valueOf()` 作为外部转换协议。
- 每个需要从外部值恢复的枚举必须提供严格的 `fromCode`；未知值必须抛出异常，不得静默降级。
- 调整枚举常量名不应影响外部协议；调整已发布的 `code` 属于接口和持久化兼容性变更。

业务枚举示例：

```java
@Getter
public enum TaskStatus {
    QUEUED("queued", "排队中", 0);

    private final String code;
    private final String msg;
    private final int value;
}
```

## 2. 分层与依赖

- 代码先按业务模块组织，再在模块内部划分 `controller`、`application`、`domain` 和 `infrastructure`。
- 依赖方向必须遵守：`controller -> application -> domain`，`infrastructure -> application/domain`。
- `domain` 和 `application` 不得依赖 Spring MVC、MyBatis 或具体 AI 服务商类型。
- Controller 只负责参数校验、调用应用服务和响应转换，不得编写业务规则或直接访问 Mapper。
- 应用服务负责用例编排和状态流转，不得直接调用具体 AI SDK 或 MyBatis Mapper。
- HTTP DTO、应用层用例结果、领域模型和 MyBatis 数据对象不得混用。跨层数据必须显式转换。
- 所有 AI 服务商适配器必须实现 `WorkItemGenerator`，应用服务只能依赖该接口。

## 3. Spring 规范

### 3.1 Bean 与依赖注入

- Spring 管理的组件应使用与职责匹配的注解：HTTP 接口使用 `@RestController`，应用服务使用 `@Service`，持久化适配器使用 `@Repository`，配置使用 `@Configuration`。
- 必需依赖必须使用构造器注入并声明为 `final`。只有一个构造器时不添加 `@Autowired`。
- 禁止字段注入。Setter 注入只用于确实可选且运行时允许替换的依赖。
- 领域对象不得添加 Spring 组件注解。
- Spring 单例 Bean 必须保持无状态，不得保存请求级可变数据。
- 禁止通过循环依赖解决模块设计问题。

### 3.2 配置

- 业务配置必须通过带类型的 `@ConfigurationProperties` 对象读取。
- 配置对象应使用 Bean Validation 校验必填项、范围和格式，使错误配置在启动阶段失败。
- 不得在业务类中直接读取环境变量、系统属性或散落的配置字符串。
- API Key 等密钥只能从外部配置注入，不得写入源码、配置样例、日志或测试数据。

### 3.3 事务与异步任务

- 事务边界必须覆盖一个需要原子完成的业务用例；不得仅因代码位于 Repository 中就机械地创建独立事务。
- 领域层不得依赖 Spring 事务 API。跨多个 Repository 的事务应由应用用例边界或基础设施事务执行器统一控制。
- 需求拆解任务只能使用指定的拆解线程池，不得使用公共线程池或手工创建线程。
- 线程池必须显式配置线程数、队列容量、拒绝策略和线程名称。
- 异步任务必须处理提交失败、执行异常和服务重启后的中断状态；重复执行不得产生重复工作项。
- 在线程中不得透传或记录完整需求原文及其他敏感上下文。

## 4. HTTP 接口规范

### 4.1 统一响应

所有 Controller 必须返回统一响应结构：

```json
{
  "success": true,
  "msg": {},
  "errorCode": "",
  "errorMsg": ""
}
```

- `success` 表示本次 HTTP 请求是否成功处理。
- `msg` 保存成功时的业务数据；请求处理失败时为 `null`。
- `errorCode` 保存稳定的内部错误码；成功时为空字符串。
- `errorMsg` 保存可向调用方展示的错误信息；成功时为空字符串。
- Controller 使用 `ApiResponse.success(...)` 和 `ApiResponse.failure(...)` 创建响应，不得手工拼装 Map。
- HTTP 状态码必须表达真实结果，不得将所有响应都返回 `200 OK`。
- 查询异步任务成功时，即使任务状态为 `failed`，外层 `success` 仍为 `true`；任务状态和失败原因位于 `msg` 中。

### 4.2 请求与兼容性

- Controller 必须使用 Jakarta Bean Validation 校验请求参数，校验消息使用清晰的中文描述。
- JSON 字段统一使用 `lowerCamelCase`。
- 日期和时间必须使用明确、固定的格式，不得依赖服务器默认 Locale 或时区。
- 除非产品接口契约被明确修改，否则必须保留 `POST /api/v1/requirements/decompose` 和 Query 参数轮询方式。
- 删除或重命名字段、修改字段类型、状态值、路径和请求方式均属于破坏性变更，必须同步更新 `docs/api.md` 和契约测试。

## 5. 日志与敏感信息

- 日志使用参数化写法，不得使用字符串拼接构造日志消息。
- 异步任务日志应包含任务 ID、阶段、结果和耗时，便于按任务追踪。
- `INFO` 记录关键业务状态变化，`WARN` 记录可恢复异常，`ERROR` 记录需要处理的失败并保留异常栈。
- 不得记录 API Key、完整需求原文、Authorization Header、数据库凭据或包含敏感信息的完整模型响应。
- 不得直接记录 AI 服务商原始响应；排障所需信息应脱敏并限制长度。

## 6. AI 集成规范

- 提示词统一存放在 `src/main/resources/prompts`，不得硬编码在 Java 类中。
- 提示词行为发生变化时必须更新提示词版本号。
- AI 服务商特有的请求、响应和异常转换只能位于 `infrastructure/ai`。
- AI 返回内容必须先转换为内部对象并完成结构、层级、枚举和必填字段校验，校验通过后才能持久化。
- 对外错误必须转换为稳定的内部错误码，不得返回服务商原始错误内容。
- 模型、提示词或结果解析发生变化时，必须更新成功、格式错误和服务商失败场景的测试。

## 7. MyBatis 与数据库规范

### 7.1 Flyway 迁移

- 数据库结构只能通过 `src/main/resources/db/migration` 下的 Flyway 迁移文件修改。
- 项目首次发布前，如果迁移尚未在共享或非本地环境执行，可以直接整理 `V1`，保持初始结构清晰，避免为调试过程保留多份过渡迁移。
- 迁移一旦在共享、测试、预发布或生产环境执行，即视为已经发布，后续不得修改或删除，所有结构变更必须新增迁移文件。
- 迁移文件应描述目标结构，不得保留已经废弃、仅用于开发调试的中间状态。

### 7.2 表结构与约束

- 表名、字段名和索引名统一使用 `lower_snake_case`。
- 数据库只保留主键、必要的非空约束、字段长度和查询索引，不为业务枚举值添加 CHECK 约束。
- 表之间使用逻辑关联，不创建物理外键；应用服务和 Repository 负责保证关联数据的一致性及删除顺序。
- 表和字段必须使用数据库原生中文 `COMMENT` 说明业务含义。
- 中文注释应描述字段业务含义，不重复字段名，不写临时开发说明。
- 删除主记录时，Repository 必须在同一业务操作中显式处理关联记录，不得依赖数据库级联删除。

### 7.3 SQL 与 MyBatis

- 查询条件和更新条件必须使用参数绑定，禁止拼接外部输入生成 SQL。
- 简短且易读的 SQL 可以使用 Mapper 注解；复杂查询应放入 Mapper XML。
- Mapper 和 MyBatis 数据对象只能在基础设施持久化层使用。
- 新增查询时必须检查索引和预期数据量；禁止无条件全表更新或删除。

## 8. 测试规范

- 测试使用 JUnit Jupiter、AssertJ 和 Mockito，并遵循 Arrange、Act、Assert 的清晰结构。
- 测试名称应描述行为和结果，不得使用没有业务含义的编号命名。
- 应用层状态流转使用不启动 Spring 容器的单元测试覆盖。
- 工作项树校验必须覆盖空节点、超过四级、非法类型、非法优先级和重复标识等异常情况。
- Controller 必须覆盖参数校验、HTTP 状态码和统一响应结构测试。
- Mapper SQL 或 Flyway 迁移发生变化时，必须增加基于 MySQL 的集成测试。
- 修复缺陷时必须增加能够复现该缺陷的测试。
- Mock 只用于外部依赖和层间接口，不得通过大量 Mock 重现被测方法的内部实现。

## 9. 提交前检查

提交代码前至少完成以下检查：

1. 运行 `./mvnw test`，确保全部测试通过。
2. 确认没有提交密钥、完整需求内容或模型原始响应。
3. 涉及接口契约时，更新 `docs/api.md` 和 Controller 契约测试。
4. 涉及数据库结构时，按迁移是否已发布决定整理 `V1` 或新增迁移，并增加 MySQL 集成测试。
5. 涉及提示词、模型或 AI 解析时，更新提示词版本和相关测试。
6. 检查代码是否遵守分层依赖，且 Controller、应用服务和基础设施职责清晰。
