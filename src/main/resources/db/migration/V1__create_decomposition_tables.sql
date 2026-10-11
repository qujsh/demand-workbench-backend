CREATE TABLE decomposition_record (
    id VARCHAR(40) PRIMARY KEY,
    source_content LONGTEXT NOT NULL,
    status VARCHAR(24) NOT NULL,
    stage VARCHAR(32) NOT NULL,
    model VARCHAR(64),
    prompt_version VARCHAR(32) NOT NULL,
    raw_response LONGTEXT,
    error_code VARCHAR(64),
    error_message VARCHAR(500),
    prompt_tokens INT,
    completion_tokens INT,
    created_at DATETIME(3) NOT NULL,
    started_at DATETIME(3),
    completed_at DATETIME(3),
    INDEX idx_decomposition_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE work_item (
    id VARCHAR(40) PRIMARY KEY,
    decomposition_id VARCHAR(40) NOT NULL,
    parent_id VARCHAR(40),
    type VARCHAR(16) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(24) NOT NULL,
    role_name VARCHAR(64),
    priority VARCHAR(16),
    start_date DATE,
    end_date DATE,
    dependency_text VARCHAR(500),
    sort_order INT NOT NULL,
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    CONSTRAINT fk_work_item_decomposition FOREIGN KEY (decomposition_id)
        REFERENCES decomposition_record(id) ON DELETE CASCADE,
    INDEX idx_work_item_decomposition_order (decomposition_id, sort_order),
    INDEX idx_work_item_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
