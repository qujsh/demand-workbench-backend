ALTER TABLE requirement
    MODIFY COLUMN stage VARCHAR(24) NOT NULL DEFAULT 'draft'
        COMMENT '需求状态：draft、published、cancelled';

ALTER TABLE published_task
    MODIFY COLUMN status VARCHAR(24) NOT NULL
        COMMENT '发布任务状态：draft、published、cancelled';

UPDATE requirement
SET stage = 'draft'
WHERE stage IN ('analyzing', 'ready_to_publish');

UPDATE requirement r
JOIN published_task t ON t.requirement_id = r.id AND t.deleted = 0
SET r.stage = CASE
    WHEN t.status = 'published' THEN 'published'
    WHEN t.status = 'cancelled' THEN 'cancelled'
    ELSE 'draft'
END
WHERE r.deleted = 0;
