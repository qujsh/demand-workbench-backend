CREATE TABLE decomposition_record (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '拆解任务内部主键',
    decomposition_no VARCHAR(40) NOT NULL COMMENT '对外拆解任务编号',
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
    started_at DATETIME(3) COMMENT '任务开始处理时间',
    completed_at DATETIME(3) COMMENT '任务完成时间',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '软删除标记：0未删除，1已删除',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '任务创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_decomposition_no (decomposition_no),
    INDEX idx_decomposition_status_created (deleted, status, created_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='需求拆解任务记录';

CREATE TABLE work_item (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '工作项内部主键',
    work_item_no VARCHAR(96) NOT NULL COMMENT '对外工作项编号',
    decomposition_id BIGINT UNSIGNED NOT NULL COMMENT '所属拆解任务内部主键',
    parent_id BIGINT UNSIGNED COMMENT '父工作项内部主键，根节点为空',
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
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '软删除标记：0未删除，1已删除',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_work_item_no (work_item_no),
    INDEX idx_work_item_decomposition_order (decomposition_id, deleted, sort_order),
    INDEX idx_work_item_parent (parent_id, deleted)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='需求拆解工作项';
