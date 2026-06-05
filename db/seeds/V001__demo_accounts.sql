USE SwapCampus;
GO

SET ANSI_NULLS ON;
GO
SET QUOTED_IDENTIFIER ON;
GO

-- ============================================================================
-- SwapCampus 演示账号种子数据 (V001)
-- 8 个普通用户 + 2 个管理账号 + 个人资料 + 信用分/积分/禁言/兑换/分类/商品
-- 所有演示账号密码统一为: demo123
-- ============================================================================

-- ====== 积分任务配置 ======

IF NOT EXISTS (SELECT 1 FROM point_tasks WHERE code = N'DAILY_CHECK_IN')
  INSERT INTO point_tasks (code, name, reward_points, task_type, status)
  VALUES (N'DAILY_CHECK_IN', N'每日签到', 10, N'CHECK_IN', N'ACTIVE');
GO

IF NOT EXISTS (SELECT 1 FROM point_tasks WHERE code = N'COMPLETE_PROFILE')
  INSERT INTO point_tasks (code, name, reward_points, task_type, status)
  VALUES (N'COMPLETE_PROFILE', N'完善资料', 20, N'PROFILE', N'ACTIVE');
GO

IF NOT EXISTS (SELECT 1 FROM point_tasks WHERE code = N'FIRST_PUBLISH')
  INSERT INTO point_tasks (code, name, reward_points, task_type, status)
  VALUES (N'FIRST_PUBLISH', N'首次发布商品', 30, N'PUBLISH', N'ACTIVE');
GO

IF NOT EXISTS (SELECT 1 FROM point_tasks WHERE code = N'FIRST_TRADE')
  INSERT INTO point_tasks (code, name, reward_points, task_type, status)
  VALUES (N'FIRST_TRADE', N'完成首次交易', 40, N'TRADE', N'ACTIVE');
GO


-- ====== 普通用户账号 ======

-- User: demo_buyer (活跃买家)
IF NOT EXISTS (SELECT 1 FROM users WHERE username = N'demo_buyer')
BEGIN
  INSERT INTO users (username, password_hash, email, role, status, credit_score, point_balance, is_deleted)
  VALUES (N'demo_buyer', N'$2a$10$MXUEZ7Milfqrrhb1cYBvI.WqcIppdx9eA8mnYx9sVoR7TJosaRX4e',
          N'buyer@swapcampus.local', N'USER', N'ACTIVE', 92, 160, 0);
  DECLARE @uid BIGINT = SCOPE_IDENTITY();

  INSERT INTO user_profiles (user_id, real_name, student_no, college, grade, bio, verified_at, contact_masked)
  VALUES (@uid, N'演示买家', N'20260001', N'计算机学院', N'2022',
          N'校园二手交易平台的活跃买家，经常浏览数码和图书类商品。已完成多次交易，信用良好。',
          SYSDATETIME(), N'202****01');
END
GO

-- User: demo_seller (活跃卖家)
IF NOT EXISTS (SELECT 1 FROM users WHERE username = N'demo_seller')
BEGIN
  INSERT INTO users (username, password_hash, email, role, status, credit_score, point_balance, is_deleted)
  VALUES (N'demo_seller', N'$2a$10$MXUEZ7Milfqrrhb1cYBvI.WqcIppdx9eA8mnYx9sVoR7TJosaRX4e',
          N'seller@swapcampus.local', N'USER', N'ACTIVE', 88, 80, 0);
  DECLARE @uid BIGINT = SCOPE_IDENTITY();

  INSERT INTO user_profiles (user_id, real_name, student_no, college, grade, bio, verified_at, contact_masked)
  VALUES (@uid, N'演示卖家', N'20260002', N'经济管理学院', N'2021',
          N'经常出售闲置教材和数码配件，信誉可靠。发布商品积极，交易响应及时。',
          SYSDATETIME(), N'202****02');
END
GO

-- User: muted_user (禁言用户)
IF NOT EXISTS (SELECT 1 FROM users WHERE username = N'muted_user')
BEGIN
  INSERT INTO users (username, password_hash, email, role, status, credit_score, point_balance, is_deleted)
  VALUES (N'muted_user', N'$2a$10$MXUEZ7Milfqrrhb1cYBvI.WqcIppdx9eA8mnYx9sVoR7TJosaRX4e',
          N'muted@swapcampus.local', N'USER', N'ACTIVE', 55, 20, 0);
  DECLARE @uid BIGINT = SCOPE_IDENTITY();

  INSERT INTO user_profiles (user_id, real_name, student_no, college, grade, bio, verified_at, contact_masked)
  VALUES (@uid, N'禁言用户', N'20260003', N'工学院', N'2020',
          N'因聊天中发布不当言论被禁言处理。信用分较低，积分较少。',
          SYSDATETIME(), N'202****03');
END
GO

-- User: banned_user (封禁用户)
IF NOT EXISTS (SELECT 1 FROM users WHERE username = N'banned_user')
BEGIN
  INSERT INTO users (username, password_hash, email, role, status, credit_score, point_balance, is_deleted)
  VALUES (N'banned_user', N'$2a$10$MXUEZ7Milfqrrhb1cYBvI.WqcIppdx9eA8mnYx9sVoR7TJosaRX4e',
          N'banned@swapcampus.local', N'USER', N'BANNED', 20, 0, 0);
  DECLARE @uid BIGINT = SCOPE_IDENTITY();

  INSERT INTO user_profiles (user_id, real_name, student_no, college, grade, bio, verified_at, contact_masked)
  VALUES (@uid, N'封禁用户', N'20260004', N'林学院', N'2019',
          N'因严重违规被永久封禁，登录后被拦截无法使用平台功能。',
          SYSDATETIME(), N'202****04');
END
GO

-- User: new_user (新注册用户)
IF NOT EXISTS (SELECT 1 FROM users WHERE username = N'new_user')
BEGIN
  INSERT INTO users (username, password_hash, email, role, status, credit_score, point_balance, is_deleted)
  VALUES (N'new_user', N'$2a$10$MXUEZ7Milfqrrhb1cYBvI.WqcIppdx9eA8mnYx9sVoR7TJosaRX4e',
          N'new@swapcampus.local', N'USER', N'ACTIVE', 60, 0, 0);
  DECLARE @uid BIGINT = SCOPE_IDENTITY();

  INSERT INTO user_profiles (user_id, bio)
  VALUES (@uid, N'刚注册的新用户，尚未完成学生认证。信用分和积分均为默认值。');
END
GO

-- User: star_user (高分模范用户)
IF NOT EXISTS (SELECT 1 FROM users WHERE username = N'star_user')
BEGIN
  INSERT INTO users (username, password_hash, email, role, status, credit_score, point_balance, is_deleted)
  VALUES (N'star_user', N'$2a$10$MXUEZ7Milfqrrhb1cYBvI.WqcIppdx9eA8mnYx9sVoR7TJosaRX4e',
          N'star@swapcampus.local', N'USER', N'ACTIVE', 98, 300, 0);
  DECLARE @uid BIGINT = SCOPE_IDENTITY();

  INSERT INTO user_profiles (user_id, real_name, student_no, college, grade, bio, verified_at, contact_masked)
  VALUES (@uid, N'高分用户', N'20260005', N'计算机学院', N'2022',
          N'平台的模范用户，信用接近满分，积分排名前列。已完成大量交易并保持零投诉记录。',
          SYSDATETIME(), N'202****05');
END
GO

-- User: casual_user (普通浏览用户)
IF NOT EXISTS (SELECT 1 FROM users WHERE username = N'casual_user')
BEGIN
  INSERT INTO users (username, password_hash, email, role, status, credit_score, point_balance, is_deleted)
  VALUES (N'casual_user', N'$2a$10$MXUEZ7Milfqrrhb1cYBvI.WqcIppdx9eA8mnYx9sVoR7TJosaRX4e',
          N'casual@swapcampus.local', N'USER', N'ACTIVE', 72, 45, 0);
  DECLARE @uid BIGINT = SCOPE_IDENTITY();

  INSERT INTO user_profiles (user_id, real_name, student_no, college, grade, bio, verified_at, contact_masked)
  VALUES (@uid, N'普通用户', N'20260007', N'文学院', N'2023',
          N'偶尔使用平台浏览商品，使用频率不高。积分和信用均为中等水平。',
          SYSDATETIME(), N'202****07');
END
GO

-- User: warned_user (被警告用户)
IF NOT EXISTS (SELECT 1 FROM users WHERE username = N'warned_user')
BEGIN
  INSERT INTO users (username, password_hash, email, role, status, credit_score, point_balance, is_deleted)
  VALUES (N'warned_user', N'$2a$10$MXUEZ7Milfqrrhb1cYBvI.WqcIppdx9eA8mnYx9sVoR7TJosaRX4e',
          N'warned@swapcampus.local', N'USER', N'ACTIVE', 42, 10, 0);
  DECLARE @uid BIGINT = SCOPE_IDENTITY();

  INSERT INTO user_profiles (user_id, real_name, student_no, college, grade, bio, verified_at, contact_masked)
  VALUES (@uid, N'警告用户', N'20260008', N'法学院', N'2021',
          N'因多次交易纠纷被警告，信用分已降至较低水平。积分余额也接近清零。',
          SYSDATETIME(), N'202****08');
END
GO

-- ====== 管理员账号 ======

-- User: demo_admin (管理员)
IF NOT EXISTS (SELECT 1 FROM users WHERE username = N'demo_admin')
BEGIN
  INSERT INTO users (username, password_hash, email, role, status, credit_score, point_balance, is_deleted)
  VALUES (N'demo_admin', N'$2a$10$MXUEZ7Milfqrrhb1cYBvI.WqcIppdx9eA8mnYx9sVoR7TJosaRX4e',
          N'admin@swapcampus.local', N'ADMIN', N'ACTIVE', 100, 0, 0);
  DECLARE @uid BIGINT = SCOPE_IDENTITY();

  INSERT INTO user_profiles (user_id, real_name, student_no, college, grade, bio, verified_at, contact_masked)
  VALUES (@uid, N'系统管理员', N'ADMIN001', N'信息中心', N'2020',
          N'平台管理员账号，负责商品审核、用户管理、举报处理等后台操作。',
          SYSDATETIME(), N'ADMIN****');
END
GO

-- User: demo_sysadmin (超级管理员/审核员)
IF NOT EXISTS (SELECT 1 FROM users WHERE username = N'demo_sysadmin')
BEGIN
  INSERT INTO users (username, password_hash, email, role, status, credit_score, point_balance, is_deleted)
  VALUES (N'demo_sysadmin', N'$2a$10$MXUEZ7Milfqrrhb1cYBvI.WqcIppdx9eA8mnYx9sVoR7TJosaRX4e',
          N'sysadmin@swapcampus.local', N'SYS_ADMIN', N'ACTIVE', 100, 0, 0);
  DECLARE @uid BIGINT = SCOPE_IDENTITY();

  INSERT INTO user_profiles (user_id, real_name, student_no, college, grade, bio, verified_at, contact_masked)
  VALUES (@uid, N'超级管理员', N'SYSADMIN001', N'信息中心', N'2019',
          N'超级管理员/审核员账号，拥有最高权限，可管理管理员账号、审计日志、系统配置等。',
          SYSDATETIME(), N'SYS****');
END
GO

-- ====== 信用分流水 ======

-- demo_buyer
DECLARE @u BIGINT; SELECT @u = id FROM users WHERE username = N'demo_buyer';
IF @u IS NOT NULL
BEGIN
  IF NOT EXISTS (SELECT 1 FROM credit_records WHERE user_id = @u AND ref_type = N'SEED')
    INSERT INTO credit_records (user_id, delta, score_after, reason, ref_type, ref_id, created_at)
    VALUES (@u, 32, 92, N'演示信用分初始化', N'SEED', NULL, DATEADD(DAY, -10, SYSDATETIME()));
  IF NOT EXISTS (SELECT 1 FROM credit_records WHERE user_id = @u AND ref_type = N'TRADE' AND delta > 0)
    INSERT INTO credit_records (user_id, delta, score_after, reason, ref_type, ref_id, created_at)
    VALUES (@u, 5, 97, N'交易完成获得好评', N'TRADE', 1, DATEADD(DAY, -4, SYSDATETIME()));
  IF NOT EXISTS (SELECT 1 FROM credit_records WHERE user_id = @u AND ref_type = N'TRADE' AND delta < 0)
    INSERT INTO credit_records (user_id, delta, score_after, reason, ref_type, ref_id, created_at)
    VALUES (@u, -5, 92, N'延迟交付扣除信用分', N'TRADE', 2, DATEADD(DAY, -1, SYSDATETIME()));
END
GO

-- demo_seller
DECLARE @u BIGINT; SELECT @u = id FROM users WHERE username = N'demo_seller';
IF @u IS NOT NULL
BEGIN
  IF NOT EXISTS (SELECT 1 FROM credit_records WHERE user_id = @u AND ref_type = N'SEED')
    INSERT INTO credit_records (user_id, delta, score_after, reason, ref_type, ref_id, created_at)
    VALUES (@u, 28, 88, N'演示信用分初始化', N'SEED', NULL, DATEADD(DAY, -10, SYSDATETIME()));
  IF NOT EXISTS (SELECT 1 FROM credit_records WHERE user_id = @u AND ref_type = N'TRADE' AND delta > 0)
    INSERT INTO credit_records (user_id, delta, score_after, reason, ref_type, ref_id, created_at)
    VALUES (@u, 5, 93, N'交易完成获得好评', N'TRADE', 1, DATEADD(DAY, -4, SYSDATETIME()));
  IF NOT EXISTS (SELECT 1 FROM credit_records WHERE user_id = @u AND ref_type = N'TRADE' AND delta < 0)
    INSERT INTO credit_records (user_id, delta, score_after, reason, ref_type, ref_id, created_at)
    VALUES (@u, -5, 88, N'延迟交付扣除信用分', N'TRADE', 2, DATEADD(DAY, -1, SYSDATETIME()));
END
GO

-- muted_user
DECLARE @u BIGINT; SELECT @u = id FROM users WHERE username = N'muted_user';
IF @u IS NOT NULL
BEGIN
  IF NOT EXISTS (SELECT 1 FROM credit_records WHERE user_id = @u AND ref_type = N'SEED')
    INSERT INTO credit_records (user_id, delta, score_after, reason, ref_type, ref_id, created_at)
    VALUES (@u, -5, 55, N'聊天违规扣除信用分', N'SEED', NULL, DATEADD(DAY, -10, SYSDATETIME()));
END
GO

-- banned_user
DECLARE @u BIGINT; SELECT @u = id FROM users WHERE username = N'banned_user';
IF @u IS NOT NULL
BEGIN
  IF NOT EXISTS (SELECT 1 FROM credit_records WHERE user_id = @u AND ref_type = N'SEED')
    INSERT INTO credit_records (user_id, delta, score_after, reason, ref_type, ref_id, created_at)
    VALUES (@u, -40, 20, N'严重违规扣除信用分', N'SEED', NULL, DATEADD(DAY, -10, SYSDATETIME()));
END
GO

-- star_user
DECLARE @u BIGINT; SELECT @u = id FROM users WHERE username = N'star_user';
IF @u IS NOT NULL
BEGIN
  IF NOT EXISTS (SELECT 1 FROM credit_records WHERE user_id = @u AND ref_type = N'SEED')
    INSERT INTO credit_records (user_id, delta, score_after, reason, ref_type, ref_id, created_at)
    VALUES (@u, 38, 98, N'演示信用分初始化', N'SEED', NULL, DATEADD(DAY, -10, SYSDATETIME()));
END
GO

-- casual_user
DECLARE @u BIGINT; SELECT @u = id FROM users WHERE username = N'casual_user';
IF @u IS NOT NULL
BEGIN
  IF NOT EXISTS (SELECT 1 FROM credit_records WHERE user_id = @u AND ref_type = N'SEED')
    INSERT INTO credit_records (user_id, delta, score_after, reason, ref_type, ref_id, created_at)
    VALUES (@u, 12, 72, N'演示信用分初始化', N'SEED', NULL, DATEADD(DAY, -10, SYSDATETIME()));
END
GO

-- warned_user
DECLARE @u BIGINT; SELECT @u = id FROM users WHERE username = N'warned_user';
IF @u IS NOT NULL
BEGIN
  IF NOT EXISTS (SELECT 1 FROM credit_records WHERE user_id = @u AND ref_type = N'SEED')
    INSERT INTO credit_records (user_id, delta, score_after, reason, ref_type, ref_id, created_at)
    VALUES (@u, -18, 42, N'多次交易纠纷扣除信用分', N'SEED', NULL, DATEADD(DAY, -10, SYSDATETIME()));
END
GO

-- ====== 积分流水 ======

-- demo_buyer
DECLARE @u BIGINT; SELECT @u = id FROM users WHERE username = N'demo_buyer';
IF @u IS NOT NULL AND NOT EXISTS (SELECT 1 FROM point_records WHERE user_id = @u)
BEGIN
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 10, N'每日签到', N'CHECK_IN', 1, DATEADD(DAY, -8, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 20, N'每日签到', N'CHECK_IN', 2, DATEADD(DAY, -7, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 30, N'每日签到', N'CHECK_IN', 3, DATEADD(DAY, -6, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 40, N'每日签到', N'CHECK_IN', 4, DATEADD(DAY, -5, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 20, 60, N'完善资料', N'TASK', 101, DATEADD(DAY, -4, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 70, N'每日签到', N'CHECK_IN', 5, DATEADD(DAY, -3, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 80, N'每日签到', N'CHECK_IN', 6, DATEADD(DAY, -2, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 30, 110, N'首次发布商品', N'TASK', 103, DATEADD(DAY, -1, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 40, 150, N'完成首次交易', N'TASK', 104, SYSDATETIME());
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 160, N'每日签到', N'CHECK_IN', 7, DATEADD(DAY, 1, SYSDATETIME()));
END
GO

-- demo_seller
DECLARE @u BIGINT; SELECT @u = id FROM users WHERE username = N'demo_seller';
IF @u IS NOT NULL AND NOT EXISTS (SELECT 1 FROM point_records WHERE user_id = @u)
BEGIN
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 10, N'每日签到', N'CHECK_IN', 8, DATEADD(DAY, -3, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 20, 30, N'完善资料', N'TASK', 101, DATEADD(DAY, -2, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 40, N'每日签到', N'CHECK_IN', 9, DATEADD(DAY, -1, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 30, 70, N'首次发布商品', N'TASK', 103, SYSDATETIME());
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 80, N'每日签到', N'CHECK_IN', 10, DATEADD(DAY, 1, SYSDATETIME()));
END
GO

-- muted_user
DECLARE @u BIGINT; SELECT @u = id FROM users WHERE username = N'muted_user';
IF @u IS NOT NULL AND NOT EXISTS (SELECT 1 FROM point_records WHERE user_id = @u)
BEGIN
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 10, N'每日签到', N'CHECK_IN', 11, DATEADD(DAY, -2, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 20, N'每日签到', N'CHECK_IN', 12, DATEADD(DAY, -1, SYSDATETIME()));
END
GO

-- star_user
DECLARE @u BIGINT; SELECT @u = id FROM users WHERE username = N'star_user';
IF @u IS NOT NULL AND NOT EXISTS (SELECT 1 FROM point_records WHERE user_id = @u)
BEGIN
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 10, N'每日签到', N'CHECK_IN', 13, DATEADD(DAY, -15, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 20, N'每日签到', N'CHECK_IN', 14, DATEADD(DAY, -14, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 30, N'每日签到', N'CHECK_IN', 15, DATEADD(DAY, -13, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 40, N'每日签到', N'CHECK_IN', 16, DATEADD(DAY, -12, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 50, N'每日签到', N'CHECK_IN', 17, DATEADD(DAY, -11, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 20, 70, N'完善资料', N'TASK', 101, DATEADD(DAY, -10, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 80, N'每日签到', N'CHECK_IN', 18, DATEADD(DAY, -9, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 90, N'每日签到', N'CHECK_IN', 19, DATEADD(DAY, -8, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 30, 120, N'首次发布商品', N'TASK', 103, DATEADD(DAY, -7, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 130, N'每日签到', N'CHECK_IN', 20, DATEADD(DAY, -6, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 140, N'每日签到', N'CHECK_IN', 21, DATEADD(DAY, -5, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 40, 180, N'完成首次交易', N'TASK', 104, DATEADD(DAY, -4, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 190, N'每日签到', N'CHECK_IN', 22, DATEADD(DAY, -3, SYSDATETIME()));

  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 200, N'每日签到', N'CHECK_IN', 23, DATEADD(DAY, -1, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 60, 260, N'平台活动奖励', N'BONUS', NULL, SYSDATETIME());
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 270, N'每日签到', N'CHECK_IN', 24, DATEADD(DAY, 1, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 280, N'每日签到', N'CHECK_IN', 25, DATEADD(DAY, 2, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 20, 300, N'连续签到奖励', N'BONUS', NULL, DATEADD(DAY, 3, SYSDATETIME()));
END
GO

-- casual_user
DECLARE @u BIGINT; SELECT @u = id FROM users WHERE username = N'casual_user';
IF @u IS NOT NULL AND NOT EXISTS (SELECT 1 FROM point_records WHERE user_id = @u)
BEGIN
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 10, N'每日签到', N'CHECK_IN', 26, DATEADD(DAY, -5, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 20, N'每日签到', N'CHECK_IN', 27, DATEADD(DAY, -4, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 20, 40, N'完善资料', N'TASK', 101, DATEADD(DAY, -3, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 5, 45, N'浏览商品奖励', N'BONUS', NULL, DATEADD(DAY, -2, SYSDATETIME()));
END
GO

-- warned_user
DECLARE @u BIGINT; SELECT @u = id FROM users WHERE username = N'warned_user';
IF @u IS NOT NULL AND NOT EXISTS (SELECT 1 FROM point_records WHERE user_id = @u)
BEGIN
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 10, N'每日签到', N'CHECK_IN', 28, DATEADD(DAY, -3, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, 10, 20, N'每日签到', N'CHECK_IN', 29, DATEADD(DAY, -2, SYSDATETIME()));
  INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
  VALUES (@u, -10, 10, N'交易纠纷扣分', N'PENALTY', NULL, DATEADD(DAY, -1, SYSDATETIME()));
END
GO

-- ====== 禁言记录 ======

IF OBJECT_ID(N'dbo.user_mutes', N'U') IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM user_mutes WHERE user_id = (SELECT id FROM users WHERE username = N'muted_user') AND muted_until > SYSDATETIME())
BEGIN
  DECLARE @m_id BIGINT, @a_id BIGINT;
  SELECT @m_id = id FROM users WHERE username = N'muted_user';
  SELECT @a_id = id FROM users WHERE username = N'demo_admin';
  IF @m_id IS NOT NULL
    INSERT INTO user_mutes (user_id, muted_by, reason, muted_until)
    VALUES (@m_id, @a_id, N'多次发布不当言论', DATEADD(DAY, 5, SYSDATETIME()));
END
GO

-- ====== 兑换记录 ======

DECLARE @r BIGINT; SELECT @r = id FROM users WHERE username = N'demo_buyer';
IF @r IS NOT NULL AND NOT EXISTS (SELECT 1 FROM point_redemptions WHERE user_id = @r)
  INSERT INTO point_redemptions (user_id, item_code, item_name, cost_points, status, created_at)
  VALUES (@r, N'COUPON_10', N'10元优惠券', 200, N'SUCCESS', DATEADD(DAY, -2, SYSDATETIME()));
GO

DECLARE @r BIGINT; SELECT @r = id FROM users WHERE username = N'demo_seller';
IF @r IS NOT NULL AND NOT EXISTS (SELECT 1 FROM point_redemptions WHERE user_id = @r)
  INSERT INTO point_redemptions (user_id, item_code, item_name, cost_points, status, created_at)
  VALUES (@r, N'PROFILE_BADGE', N'个人主页徽章', 120, N'SUCCESS', DATEADD(DAY, -1, SYSDATETIME()));
GO

DECLARE @r BIGINT; SELECT @r = id FROM users WHERE username = N'star_user';
IF @r IS NOT NULL AND NOT EXISTS (SELECT 1 FROM point_redemptions WHERE user_id = @r)
  INSERT INTO point_redemptions (user_id, item_code, item_name, cost_points, status, created_at)
  VALUES (@r, N'COUPON_30', N'30元优惠券', 500, N'SUCCESS', DATEADD(DAY, -5, SYSDATETIME()));
GO

DECLARE @r BIGINT; SELECT @r = id FROM users WHERE username = N'casual_user';
IF @r IS NOT NULL AND NOT EXISTS (SELECT 1 FROM point_redemptions WHERE user_id = @r)
  INSERT INTO point_redemptions (user_id, item_code, item_name, cost_points, status, created_at)
  VALUES (@r, N'COUPON_10', N'10元优惠券', 200, N'FAILED', DATEADD(DAY, -1, SYSDATETIME()));
GO

DECLARE @r3 BIGINT; SELECT @r3 = id FROM users WHERE username = N'star_user';
IF @r3 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM point_redemptions WHERE user_id = @r3 AND item_code = N'PROFILE_BADGE')
  INSERT INTO point_redemptions (user_id, item_code, item_name, cost_points, status, created_at)
  VALUES (@r3, N'PROFILE_BADGE', N'个人主页徽章', 120, N'SUCCESS', DATEADD(DAY, -2, SYSDATETIME()));
GO

-- ====== 分类和商品 ======

IF NOT EXISTS (SELECT 1 FROM categories WHERE name = N'数码设备')
  INSERT INTO categories (name, sort_order, status) VALUES (N'数码设备', 10, N'ACTIVE');
GO
IF NOT EXISTS (SELECT 1 FROM categories WHERE name = N'图书资料')
  INSERT INTO categories (name, sort_order, status) VALUES (N'图书资料', 20, N'ACTIVE');
GO
IF NOT EXISTS (SELECT 1 FROM categories WHERE name = N'生活用品')
  INSERT INTO categories (name, sort_order, status) VALUES (N'生活用品', 30, N'ACTIVE');
GO

DECLARE @s BIGINT, @c1 BIGINT, @c2 BIGINT;
SELECT @s  = id FROM users     WHERE username = N'demo_seller';
SELECT @c1 = id FROM categories WHERE name = N'数码设备';
SELECT @c2 = id FROM categories WHERE name = N'图书资料';

IF @s IS NOT NULL AND @c1 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM products WHERE title = N'校园二手 MacBook 保护壳')
  INSERT INTO products (seller_id, category_id, title, description, price, original_price, condition_level, campus, trade_modes, status)
  VALUES (@s, @c1, N'校园二手 MacBook 保护壳', N'适合 13 寸 MacBook，轻微使用痕迹。半透明磨砂材质，附赠键盘膜。', 39.00, 99.00, N'GOOD', N'主校区', N'MEETUP', N'ACTIVE');

IF @s IS NOT NULL AND @c2 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM products WHERE title = N'数据结构教材与习题集')
  INSERT INTO products (seller_id, category_id, title, description, price, original_price, condition_level, campus, trade_modes, status)
  VALUES (@s, @c2, N'数据结构教材与习题集', N'课程复习资料，附少量笔记。包含课后习题答案和期末复习重点。', 26.00, 68.00, N'FAIR', N'主校区', N'MEETUP', N'ACTIVE');
GO

PRINT N'[Seed] 8个普通用户 + 2个管理员账号 + 信用/积分/禁言/兑换/分类/商品数据已就绪（密码: demo123）';
GO