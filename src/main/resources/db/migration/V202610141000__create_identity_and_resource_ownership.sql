CREATE TABLE sys_user (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户内部主键',
    user_no VARCHAR(32) COMMENT '对外用户编号',
    nickname VARCHAR(64) NOT NULL COMMENT '用户昵称',
    avatar_url VARCHAR(500) COMMENT '头像地址',
    status VARCHAR(24) NOT NULL COMMENT '账号状态：active、restricted、suspended、banned、closing、closed',
    last_login_at DATETIME(3) COMMENT '最后登录时间',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '软删除标记：0未删除，1已删除',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_sys_user_no (user_no),
    INDEX idx_sys_user_status (deleted, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统一用户账号';

CREATE TABLE user_auth_identity (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '登录身份内部主键',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '所属用户内部主键',
    identity_type VARCHAR(24) NOT NULL COMMENT '登录身份类型：phone',
    identifier VARCHAR(190) NOT NULL COMMENT '标准化登录标识',
    verified_at DATETIME(3) COMMENT '身份验证时间',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '软删除标记：0未删除，1已删除',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_auth_identity_type_identifier (identity_type, identifier),
    INDEX idx_auth_identity_user (user_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户登录身份';

CREATE TABLE user_session (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '会话内部主键',
    session_no VARCHAR(32) NOT NULL COMMENT '对外会话编号',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '所属用户内部主键',
    token_hash CHAR(64) NOT NULL COMMENT '会话令牌 SHA-256 摘要',
    expires_at DATETIME(3) NOT NULL COMMENT '会话过期时间',
    revoked_at DATETIME(3) COMMENT '会话撤销时间',
    last_seen_at DATETIME(3) COMMENT '最后访问时间',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '软删除标记：0未删除，1已删除',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_user_session_no (session_no),
    UNIQUE INDEX uk_user_session_token_hash (token_hash),
    INDEX idx_user_session_user_expiry (user_id, revoked_at, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户登录会话';

CREATE TABLE user_business_identity (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '业务身份内部主键',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '所属用户内部主键',
    identity_type VARCHAR(24) NOT NULL COMMENT '业务身份类型：publisher、contractor',
    status VARCHAR(24) NOT NULL COMMENT '身份状态：active、restricted、suspended、disabled',
    activated_at DATETIME(3) COMMENT '身份激活时间',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '软删除标记：0未删除，1已删除',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_user_business_identity (user_id, identity_type),
    INDEX idx_business_identity_type_status (identity_type, status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户业务身份';

CREATE TABLE publisher_profile (
    user_id BIGINT UNSIGNED NOT NULL COMMENT '发布人用户内部主键',
    level_code VARCHAR(8) NOT NULL COMMENT '发布人等级编码：L0至L5',
    growth_points BIGINT NOT NULL DEFAULT 0 COMMENT '累计成长值',
    status VARCHAR(24) NOT NULL COMMENT '发布人状态：active、restricted、suspended、disabled',
    version BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    deleted TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '软删除标记：0未删除，1已删除',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (user_id),
    INDEX idx_publisher_level_status (level_code, status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发布人资料';

INSERT INTO sys_user (id, user_no, nickname, status)
VALUES (1, 'USER-000001', '开发者', 'active');

INSERT INTO user_auth_identity (user_id, identity_type, identifier, verified_at)
VALUES (1, 'phone', '13800000000', CURRENT_TIMESTAMP(3));

INSERT INTO user_business_identity (user_id, identity_type, status, activated_at)
VALUES (1, 'publisher', 'active', CURRENT_TIMESTAMP(3));

INSERT INTO publisher_profile (user_id, level_code, growth_points, status)
VALUES (1, 'L0', 0, 'active');

ALTER TABLE requirement
    ADD COLUMN owner_user_id BIGINT UNSIGNED NULL COMMENT '需求所有者用户内部主键' AFTER id;

ALTER TABLE decomposition_record
    ADD COLUMN owner_user_id BIGINT UNSIGNED NULL COMMENT '拆解任务所有者用户内部主键' AFTER id;

UPDATE requirement SET owner_user_id = 1 WHERE owner_user_id IS NULL;
UPDATE decomposition_record SET owner_user_id = 1 WHERE owner_user_id IS NULL;

ALTER TABLE requirement
    MODIFY COLUMN owner_user_id BIGINT UNSIGNED NOT NULL COMMENT '需求所有者用户内部主键',
    ADD INDEX idx_requirement_owner_updated (owner_user_id, deleted, updated_at);

ALTER TABLE decomposition_record
    MODIFY COLUMN owner_user_id BIGINT UNSIGNED NOT NULL COMMENT '拆解任务所有者用户内部主键',
    ADD INDEX idx_decomposition_owner_created (owner_user_id, deleted, created_at);
