CREATE TABLE requirement (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '需求内部主键',
    requirement_no VARCHAR(32) COMMENT '对外需求编号，创建后由标题编码和内部主键生成',
    title_code VARCHAR(6) NOT NULL COMMENT '需求标题检索码，由标题拼音首字母、英文字母和数字生成',
    title VARCHAR(255) NOT NULL COMMENT '需求标题',
    user_description LONGTEXT COMMENT '用户填写的原始需求描述',
    ai_description LONGTEXT COMMENT '人工确认后的 AI 需求描述',
    stage VARCHAR(24) NOT NULL COMMENT '需求当前阶段编码',
    current_decomposition_id BIGINT UNSIGNED COMMENT '当前采用的拆解任务内部主键',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '软删除标记：0未删除，1已删除',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_requirement_no (requirement_no),
    INDEX idx_requirement_stage_updated (stage, updated_at),
    INDEX idx_requirement_deleted_updated (deleted, updated_at),
    INDEX idx_requirement_decomposition (current_decomposition_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='需求收集与分析记录';

CREATE TABLE requirement_attachment (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '附件内部主键',
    requirement_id BIGINT UNSIGNED NOT NULL COMMENT '所属需求内部主键',
    file_name VARCHAR(255) NOT NULL COMMENT '附件原始文件名',
    storage_key VARCHAR(500) NOT NULL COMMENT '文件存储对象标识',
    content_type VARCHAR(128) COMMENT '附件媒体类型',
    file_size BIGINT UNSIGNED NOT NULL COMMENT '附件字节数',
    sort_order INT NOT NULL COMMENT '附件展示顺序',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '软删除标记：0未删除，1已删除',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_requirement_attachment_order (requirement_id, deleted, sort_order)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='需求附件记录';
