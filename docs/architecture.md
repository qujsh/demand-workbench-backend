# 项目架构

## 项目目标

后端接收需求描述，创建异步拆解任务，通过 AI 模型生成四级工作项树，校验结果后保存到 MySQL。

## 依赖方向

```text
controller -> application -> domain
                         ^
infrastructure ---------|
```

- `controller`：处理 HTTP 参数校验、请求接收和响应转换。
- `application`：编排业务用例、控制任务状态流转，并定义对外能力接口。
- `domain`：保存与技术框架、AI 服务商无关的领域模型和业务规则。
- `infrastructure/ai`：实现 Spring AI 和 DeepSeek 接入。
- `infrastructure/persistence`：实现 MyBatis 数据访问和事务持久化。

应用层所需的接口由 `application` 定义，基础设施层负责实现。应用层不得直接依赖具体基础设施实现。

## 需求拆解流程

```text
提交 POST 请求
  -> 创建 QUEUED 任务
  -> 提交后台任务
  -> AI_GENERATING
  -> VALIDATING
  -> SAVING
  -> COMPLETED 或 FAILED
```

当前使用进程内线程池，属于 MVP 阶段方案，并不是可靠任务队列：服务重启会中断正在运行的任务；部署多个应用实例前，需要接入外部调度器或消息队列。

## 工作项层级

系统只接受以下四级结构：

```text
模块 module -> 功能 feature -> 工作项 work -> 子任务 subtask
```

AI 服务商返回的内容必须先转换为领域对象，通过校验后才能持久化。HTTP DTO、MyBatis 数据对象和领域模型不得混用。
