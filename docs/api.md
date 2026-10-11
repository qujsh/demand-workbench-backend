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
  "taskId": "DEC-xxx",
  "status": "QUEUED",
  "message": "拆解任务已提交"
}
```

## 查询需求拆解结果

接口：`GET /api/v1/requirements/decompose?taskId=DEC-xxx`

前端通过 Query 参数传入任务 ID，并轮询该接口，直到 `status` 变为 `SUCCESS` 或 `FAILED`。

拆解成功后，响应中包含以下工作项层级：

```text
模块 module -> 功能 feature -> 工作项 work -> 子任务 subtask
```

## 兼容性要求

以下修改属于破坏性变更，必须先获得明确的产品确认：

- 删除或重命名字段。
- 修改字段数据类型。
- 修改工作项层级。
- 修改现有状态值。
- 修改接口路径、请求方式或 Query 查询方式。
