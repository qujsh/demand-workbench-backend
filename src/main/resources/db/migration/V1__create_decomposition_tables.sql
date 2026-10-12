CREATE TABLE decomposition_record (
    id VARCHAR(40) PRIMARY KEY COMMENT '拆解任务编号',
    source_content LONGTEXT NOT NULL COMMENT '待拆解的原始需求内容',
    status VARCHAR(24) NOT NULL COMMENT '任务状态编码',
    stage VARCHAR(32) NOT NULL COMMENT '当前处理阶段编码',
    model VARCHAR(64) COMMENT '调用的模型名称',
    prompt_version VARCHAR(32) NOT NULL COMMENT '提示词版本',
    raw_response LONGTEXT COMMENT '模型原始响应',
    error_code VARCHAR(64) COMMENT '任务失败错误码',
    error_message VARCHAR(500) COMMENT '任务失败说明',
    prompt_tokens INT COMMENT '输入 Token 数量',
    completion_tokens INT COMMENT '输出 Token 数量',
    created_at DATETIME(3) NOT NULL COMMENT '任务创建时间',
    started_at DATETIME(3) COMMENT '任务开始处理时间',
    completed_at DATETIME(3) COMMENT '任务完成时间',
    INDEX idx_decomposition_status_created (status, created_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='需求拆解任务记录';

CREATE TABLE work_item (
    id VARCHAR(40) PRIMARY KEY COMMENT '工作项编号',
    decomposition_id VARCHAR(40) NOT NULL COMMENT '所属拆解任务编号',
    parent_id VARCHAR(40) COMMENT '父工作项编号，根节点为空',
    type VARCHAR(16) NOT NULL COMMENT '工作项类型编码',
    name VARCHAR(255) NOT NULL COMMENT '工作项名称',
    description TEXT COMMENT '工作项描述',
    status VARCHAR(24) NOT NULL COMMENT '工作项状态',
    role_name VARCHAR(64) COMMENT '负责角色',
    priority VARCHAR(16) COMMENT '优先级编码',
    start_date DATE COMMENT '计划开始日期',
    end_date DATE COMMENT '计划结束日期',
    dependency_text VARCHAR(500) COMMENT '依赖说明',
    sort_order INT NOT NULL COMMENT '树节点排序序号',
    created_at DATETIME(3) NOT NULL COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL COMMENT '更新时间',
    INDEX idx_work_item_decomposition_order (decomposition_id, sort_order),
    INDEX idx_work_item_parent (parent_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='需求拆解工作项';
