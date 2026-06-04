USE SwapCampus;
GO

IF COL_LENGTH('chat_messages', 'seq_no') IS NULL
BEGIN
  ALTER TABLE chat_messages ADD seq_no BIGINT NULL;
END
GO

IF COL_LENGTH('chat_messages', 'status') IS NULL
BEGIN
  ALTER TABLE chat_messages ADD status NVARCHAR(20) NOT NULL DEFAULT 'SENT';
END
GO

IF COL_LENGTH('reports', 'session_id') IS NULL
BEGIN
  ALTER TABLE reports ADD session_id BIGINT NULL;
END
GO

IF COL_LENGTH('reports', 'reported_user_id') IS NULL
BEGIN
  ALTER TABLE reports ADD reported_user_id BIGINT NULL;
END
GO

IF COL_LENGTH('reports', 'reject_reason') IS NULL
BEGIN
  ALTER TABLE reports ADD reject_reason NVARCHAR(500) NULL;
END
GO

IF COL_LENGTH('reports', 'evidence_url') IS NULL
BEGIN
  ALTER TABLE reports ADD evidence_url NVARCHAR(500) NULL;
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'user_mutes')
BEGIN
  CREATE TABLE user_mutes (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    muted_by BIGINT NULL,
    reason NVARCHAR(200) NULL,
    muted_until DATETIME2(0) NOT NULL,
    created_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT fk_user_mutes_user FOREIGN KEY (user_id) REFERENCES users(id)
  );
  CREATE INDEX idx_user_mutes_user_until ON user_mutes(user_id, muted_until);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'user_blocks')
BEGIN
  CREATE TABLE user_blocks (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    blocker_id BIGINT NOT NULL,
    blocked_id BIGINT NOT NULL,
    created_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT ux_user_blocks UNIQUE (blocker_id, blocked_id),
    CONSTRAINT fk_user_blocks_blocker FOREIGN KEY (blocker_id) REFERENCES users(id),
    CONSTRAINT fk_user_blocks_blocked FOREIGN KEY (blocked_id) REFERENCES users(id)
  );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ux_reports_reporter_target')
BEGIN
  CREATE UNIQUE INDEX ux_reports_reporter_target ON reports(reporter_id, target_type, target_id);
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_chat_sessions_participants')
BEGIN
  CREATE INDEX idx_chat_sessions_participants ON chat_sessions(buyer_id, seller_id, product_id);
END
GO
