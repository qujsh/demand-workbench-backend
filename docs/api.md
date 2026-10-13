# 接口契约

## 提交需求拆解任务

接口：`POST /api/v1/requirements/decompose`

请求体：

```json
{
  "content": "最终有效的需求描述"
}
```

接口返回 HTTP `202 Accepted` 和任务 ID。请求直接上传需求文本，不依赖需求 ID。

返回示例：

```json
{
  "success": true,
  "msg": {
    "taskId": "DEC-xxx",
    "status": "queued",
    "message": "拆解任务已提交"
  },
  "errorCode": "",
  "errorMsg": ""
}
```

## 查询需求拆解结果

接口：`GET /api/v1/requirements/decompose?taskId=DEC-xxx`

前端通过 Query 参数传入任务 ID，并轮询该接口，直到 `status` 变为 `success` 或 `failed`。

查询成功时，任务状态和拆解结果位于 `msg` 字段中。即使异步任务状态为 `failed`，只要查询请求本身成功，外层 `success` 仍为 `true`。

## 删除工作项

接口：`POST /api/v1/work-items/delete`

请求体：

```json
{
  "workItemNos": ["DEC-xxx-WORK-001", "DEC-xxx-SUB-001"]
}
```

批量编号使用 JSON 请求体，避免大量编号超过 URL 长度限制。接口会将选中工作项及其全部后代标记为已删除，不物理移除记录；工作项树和数量统计只读取未删除数据。返回的 `deletedCount` 表示本次实际标记删除的工作项数量。

拆解成功后，响应中包含以下工作项层级：

```text
模块 module -> 功能 feature -> 工作项 work -> 子任务 subtask
```

### 枚举值

| 字段 | 可用值 |
|---|---|
| 任务状态 `status` | `queued`、`processing`、`success`、`failed` |
| 处理阶段 `stage` | `queued`、`ai_generating`、`validating`、`saving`、`completed`、`failed` |
| 工作项类型 `type` | `module`、`feature`、`work`、`subtask` |
| 优先级 `priority` | `unset`、`low`、`medium`、`high` |

## 统一响应格式

所有接口使用以下响应结构：

```json
{
  "success": true,
  "msg": {},
  "errorCode": "",
  "errorMsg": ""
}
```

- `success`：HTTP 请求是否成功处理。
- `msg`：成功时的业务响应数据；失败时为 `null`。
- `errorCode`：稳定的内部错误码；成功时为空字符串。
- `errorMsg`：可向调用方展示的错误信息；成功时为空字符串。

失败示例：

```json
{
  "success": false,
  "msg": null,
  "errorCode": "S048101002",
  "errorMsg": "拆解任务不存在"
}
```

### 错误码

| 错误码 | 含义 |
|---|---|
| `S048101001` | 请求内容无效 |
| `S048101002` | 拆解任务不存在 |
| `S048101003` | 任务队列繁忙，请稍后重试 |
| `S048101004` | AI 服务请求失败 |
| `S048101005` | 模型返回结果无效 |
| `S048101006` | 需求拆解失败 |
| `S048101007` | 服务重启导致任务中断 |
| `S048101008` | 系统内部错误 |

## 保存需求收集数据

新增接口：`POST /api/v1/requirements/save`

保存已有需求：`POST /api/v1/requirements/save?requirementNo=REQ-KFYTYY-000025`

请求体：

```json
{
  "title": "开发一套预约服务系统",
  "userDescription": "用户填写的需求描述",
  "aiDescription": "人工确认后的 AI 需求描述",
  "stage": "ready_to_publish",
  "currentDecompositionNo": "DEC-C10344C6F3C942AC93D4",
  "attachments": [
    {
      "fileName": "需求说明.docx",
      "storageKey": "requirements/2026/requirement.docx",
      "contentType": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
      "fileSize": 251904,
      "sortOrder": 1
    }
  ]
}
```

- 新增需求时不传 `requirementNo`，服务端写入数据取得内部主键后生成业务编号。
- 编号格式为 `REQ-{TITLE_CODE}-{ID}`，例如 `REQ-KFYTYY-000025`。
- `TITLE_CODE` 取标题中中文拼音首字母、英文字母和数字，转为大写并最多保留 6 位；无法生成时使用 `COMMON`。
- 需求编号只在首次创建时生成，修改标题不会改变已有编号。
- `currentDecompositionNo` 可以为空；传入时，对应拆解任务必须存在且状态为 `success`。
- `attachments` 只保存已经上传完成的文件元数据，本接口不接收文件二进制。

需求阶段：

| 字段 | 可用值 |
|---|---|
| 需求阶段 `stage` | `draft`、`analyzing`、`ready_to_publish`、`published` |

## 查询需求

详情接口：`GET /api/v1/requirements/detail?requirementNo=REQ-KFYTYY-000025`

列表接口：`GET /api/v1/requirements/list`

删除接口：`POST /api/v1/requirements/delete?requirementNo=REQ-KFYTYY-000025`

删除接口将需求标记为已删除，不物理删除需求、附件、拆解记录或工作项。列表与详情接口只返回未删除需求。列表按最后更新时间倒序返回。详情和列表返回需求标题、两类需求描述、当前阶段、当前拆解任务、模块数、工作项数、附件及创建和更新时间。工作项数统计 `work` 和 `subtask` 两类可执行节点。

## 发布任务简介

查询接口：`GET /api/v1/requirements/publish-summary?requirementNo=REQ-KFYTYY-000025`

查询结果包含 `pending`、`generating`、`success`、`failed` 四种状态，以及已完成时的 `content`、`promptVersion` 和 `generatedAt`。前端只有在没有成功内容时才请求生成接口。

流式生成接口：`POST /api/v1/requirements/publish-summary/generate?requirementNo=REQ-KFYTYY-000025`

生成接口返回 `text/event-stream`，事件类型如下：

```text
event: content
data: {"text":"流式文本片段"}

event: complete
data: {"requirementNo":"REQ-...","status":"success","content":"完整任务简介"}

event: error
data: {"errorCode":"S048102006","message":"任务简介生成失败"}
```

正式结果只在模型流完整结束后保存。已有正式结果时不会再次调用 AI；生成失败时状态更新为 `failed`，前端可以重新请求生成。需求没有 `ai_description`、只有 `user_description` 时，直接将 `user_description` 保存并返回，来源版本记录为 `user-description-v1`，不会调用 AI。

生成时以用户原始需求作为范围判断基准，并使用需求分析中的已确认部分补充核心范围。进入“可选/P1”或“技术风险”章节后的内容不会发送给简介生成模型。任务简介仅概括目标、能力范围和成果物，详细功能与技术实现由工作项承载。

### 需求接口错误码

| 错误码 | 含义 |
|---|---|
| `S048102000` | 需求内容无效 |
| `S048102001` | 需求不存在 |
| `S048102002` | 拆解任务不存在 |
| `S048102003` | 拆解任务尚未完成 |
| `S048102004` | 需求保存失败 |
| `S048102005` | 需求内容为空，无法生成任务简介 |
| `S048102006` | 任务简介生成失败 |
| `S048102007` | 发布任务内容无效 |
| `S048102008` | 发布任务保存失败 |

## 保存和发布任务

查询草稿或已发布任务：`GET /api/v1/requirements/publish-task?requirementNo=REQ-...`

保存草稿或确认发布：`POST /api/v1/requirements/publish-task/save?requirementNo=REQ-...`

请求体中的 `status` 可为 `draft` 或 `published`。草稿允许内容不完整并按来源需求覆盖保存；正式发布要求任务标题、任务简介、招募人数有效且来源需求已有可用拆解结果。发布任务直接绑定发布时采用的完整拆解记录，不单独提交工作项编号。正式发布成功后，来源需求阶段同步更新为 `published`。

## 兼容性要求

以下修改属于破坏性变更，必须先获得明确的产品确认：

- 删除或重命名字段。
- 修改字段数据类型。
- 修改工作项层级。
- 修改现有状态值。
- 修改接口路径、请求方式或 Query 查询方式。
