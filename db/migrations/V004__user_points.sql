USE SwapCampus;
GO

SET ANSI_NULLS ON;
GO
SET QUOTED_IDENTIFIER ON;
GO

IF OBJECT_ID(N'dbo.point_tasks', N'U') IS NULL
BEGIN
  CREATE TABLE point_tasks (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(50) NOT NULL UNIQUE,
    name NVARCHAR(80) NOT NULL,
    reward_points INT NOT NULL,
    task_type NVARCHAR(30) NOT NULL,
    status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
  );
END
GO

IF OBJECT_ID(N'dbo.point_records', N'U') IS NULL
BEGIN
  CREATE TABLE point_records (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    delta INT NOT NULL,
    balance_after INT NOT NULL,
    reason NVARCHAR(100) NOT NULL,
    ref_type NVARCHAR(30) NULL,
    ref_id BIGINT NULL,
    created_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT fk_point_records_user FOREIGN KEY (user_id) REFERENCES users(id)
  );
END
GO

IF OBJECT_ID(N'dbo.point_redemptions', N'U') IS NULL
BEGIN
  CREATE TABLE point_redemptions (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_code NVARCHAR(50) NOT NULL,
    item_name NVARCHAR(80) NOT NULL,
    cost_points INT NOT NULL,
    status NVARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    created_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT fk_point_redemptions_user FOREIGN KEY (user_id) REFERENCES users(id)
  );
END
GO

IF NOT EXISTS (
  SELECT 1
  FROM sys.indexes
  WHERE object_id = OBJECT_ID(N'dbo.credit_records')
    AND name = N'idx_credit_records_user_created_at'
)
BEGIN
  CREATE INDEX idx_credit_records_user_created_at ON credit_records(user_id, created_at);
END
GO

IF NOT EXISTS (
  SELECT 1
  FROM sys.indexes
  WHERE object_id = OBJECT_ID(N'dbo.point_records')
    AND name = N'idx_point_records_user_created_at'
)
BEGIN
  CREATE INDEX idx_point_records_user_created_at ON point_records(user_id, created_at);
END
GO

IF NOT EXISTS (
  SELECT 1
  FROM sys.indexes
  WHERE object_id = OBJECT_ID(N'dbo.point_records')
    AND name = N'idx_point_records_user_ref'
)
BEGIN
  CREATE INDEX idx_point_records_user_ref ON point_records(user_id, ref_type, ref_id);
END
GO

IF NOT EXISTS (
  SELECT 1
  FROM sys.indexes
  WHERE object_id = OBJECT_ID(N'dbo.point_redemptions')
    AND name = N'idx_point_redemptions_user_created_at'
)
BEGIN
  CREATE INDEX idx_point_redemptions_user_created_at ON point_redemptions(user_id, created_at);
END
GO

IF NOT EXISTS (SELECT 1 FROM point_tasks WHERE code = N'DAILY_CHECK_IN')
BEGIN
  INSERT INTO point_tasks (code, name, reward_points, task_type, status)
  VALUES (N'DAILY_CHECK_IN', N'每日签到', 10, N'CHECK_IN', N'ACTIVE');
END
GO

IF NOT EXISTS (SELECT 1 FROM point_tasks WHERE code = N'COMPLETE_PROFILE')
BEGIN
  INSERT INTO point_tasks (code, name, reward_points, task_type, status)
  VALUES (N'COMPLETE_PROFILE', N'完善资料', 20, N'PROFILE', N'ACTIVE');
END
GO