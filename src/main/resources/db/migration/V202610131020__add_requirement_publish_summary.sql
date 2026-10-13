ALTER TABLE requirement
    ADD COLUMN publish_summary LONGTEXT COMMENT 'AI 生成的发布任务简介' AFTER ai_description,
    ADD COLUMN publish_summary_status VARCHAR(24) NOT NULL DEFAULT 'pending'
        COMMENT '发布任务简介生成状态：pending、generating、success、failed' AFTER publish_summary,
    ADD COLUMN publish_summary_prompt_version VARCHAR(64)
        COMMENT '发布任务简介生成提示词版本' AFTER publish_summary_status,
    ADD COLUMN publish_summary_generated_at DATETIME(3)
        COMMENT '发布任务简介生成完成时间' AFTER publish_summary_prompt_version;
