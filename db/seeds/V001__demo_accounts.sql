USE SwapCampus;
GO

SET ANSI_NULLS ON;
GO
SET QUOTED_IDENTIFIER ON;
GO

DECLARE @demoPasswordHash NVARCHAR(100) = N'$2a$10$MXUEZ7Milfqrrhb1cYBvI.WqcIppdx9eA8mnYx9sVoR7TJosaRX4e';

INSERT INTO point_tasks (code, name, reward_points, task_type, status)
VALUES
  (N'DAILY_CHECK_IN', N'每日签到', 10, N'CHECK_IN', N'ACTIVE'),
  (N'COMPLETE_PROFILE', N'完善资料', 20, N'PROFILE', N'ACTIVE'),
  (N'FIRST_PUBLISH', N'首次发布商品', 30, N'PUBLISH', N'ACTIVE'),
  (N'FIRST_TRADE', N'完成首次交易', 40, N'TRADE', N'ACTIVE');

INSERT INTO student_identities (student_no, real_name, college, grade, edu_password_hash, status)
VALUES
  (N'20260001', N'演示买家', N'计算机学院', N'2022', @demoPasswordHash, N'ACTIVE'),
  (N'20260002', N'演示卖家', N'经济管理学院', N'2021', @demoPasswordHash, N'ACTIVE'),
  (N'20260003', N'禁言用户', N'工学院', N'2020', @demoPasswordHash, N'ACTIVE'),
  (N'20260004', N'封禁用户', N'林学院', N'2019', @demoPasswordHash, N'ACTIVE'),
  (N'20260005', N'高分用户', N'计算机学院', N'2022', @demoPasswordHash, N'ACTIVE'),
  (N'20260006', N'新用户测试', N'计算机学院', N'2024', @demoPasswordHash, N'ACTIVE'),
  (N'20260007', N'普通用户', N'文学院', N'2023', @demoPasswordHash, N'ACTIVE'),
  (N'20260008', N'警告用户', N'法学院', N'2021', @demoPasswordHash, N'ACTIVE'),
  (N'20269999', N'未绑定学生', N'测试学院', N'2024', @demoPasswordHash, N'ACTIVE');

INSERT INTO users (username, password_hash, email, role, status, credit_score, point_balance, is_deleted)
VALUES
  (N'demo_buyer', @demoPasswordHash, N'buyer@swapcampus.local', N'USER', N'ACTIVE', 92, 160, 0),
  (N'demo_seller', @demoPasswordHash, N'seller@swapcampus.local', N'USER', N'ACTIVE', 88, 80, 0),
  (N'muted_user', @demoPasswordHash, N'muted@swapcampus.local', N'USER', N'ACTIVE', 55, 20, 0),
  (N'banned_user', @demoPasswordHash, N'banned@swapcampus.local', N'USER', N'BANNED', 20, 0, 0),
  (N'new_user', @demoPasswordHash, N'new@swapcampus.local', N'USER', N'ACTIVE', 60, 0, 0),
  (N'star_user', @demoPasswordHash, N'star@swapcampus.local', N'USER', N'ACTIVE', 98, 300, 0),
  (N'casual_user', @demoPasswordHash, N'casual@swapcampus.local', N'USER', N'ACTIVE', 72, 45, 0),
  (N'warned_user', @demoPasswordHash, N'warned@swapcampus.local', N'USER', N'ACTIVE', 42, 10, 0),
  (N'demo_admin', @demoPasswordHash, N'admin@swapcampus.local', N'ADMIN', N'ACTIVE', 100, 0, 0),
  (N'demo_product_reviewer', @demoPasswordHash, N'product-reviewer@swapcampus.local', N'PRODUCT_REVIEWER', N'ACTIVE', 100, 0, 0),
  (N'demo_sysadmin', @demoPasswordHash, N'sysadmin@swapcampus.local', N'SYS_ADMIN', N'ACTIVE', 100, 0, 0);

INSERT INTO user_profiles (user_id, real_name, student_no, college, grade, bio, verified_at, contact_masked)
SELECT id, N'演示买家', N'20260001', N'计算机学院', N'2022',
       N'校园二手交易平台的活跃买家，经常浏览数码和图书类商品。已完成多次交易，信用良好。',
       SYSDATETIME(), N'202****01'
FROM users WHERE username = N'demo_buyer';

INSERT INTO user_profiles (user_id, real_name, student_no, college, grade, bio, verified_at, contact_masked)
SELECT id, N'演示卖家', N'20260002', N'经济管理学院', N'2021',
       N'经常出售闲置教材和数码配件，信誉可靠。发布商品积极，交易响应及时。',
       SYSDATETIME(), N'202****02'
FROM users WHERE username = N'demo_seller';

INSERT INTO user_profiles (user_id, real_name, student_no, college, grade, bio, verified_at, contact_masked)
SELECT id, N'禁言用户', N'20260003', N'工学院', N'2020',
       N'因聊天中发布不当言论被禁言处理。信用分较低，积分较少。',
       SYSDATETIME(), N'202****03'
FROM users WHERE username = N'muted_user';

INSERT INTO user_profiles (user_id, real_name, student_no, college, grade, bio, verified_at, contact_masked)
SELECT id, N'封禁用户', N'20260004', N'林学院', N'2019',
       N'因严重违规被永久封禁，登录后会被平台拦截。',
       SYSDATETIME(), N'202****04'
FROM users WHERE username = N'banned_user';

INSERT INTO user_profiles (user_id, real_name, student_no, college, grade, bio, verified_at, contact_masked)
SELECT id, N'新用户测试', N'20260006', N'计算机学院', N'2024',
       N'新注册演示用户，已完成学生认证，便于测试普通用户交易链路。',
       SYSDATETIME(), N'202****06'
FROM users WHERE username = N'new_user';

INSERT INTO user_profiles (user_id, real_name, student_no, college, grade, bio, verified_at, contact_masked)
SELECT id, N'高分用户', N'20260005', N'计算机学院', N'2022',
       N'平台的模范用户，信用接近满分，积分排名前列。',
       SYSDATETIME(), N'202****05'
FROM users WHERE username = N'star_user';

INSERT INTO user_profiles (user_id, real_name, student_no, college, grade, bio, verified_at, contact_masked)
SELECT id, N'普通用户', N'20260007', N'文学院', N'2023',
       N'偶尔使用平台浏览商品，积分和信用均为中等水平。',
       SYSDATETIME(), N'202****07'
FROM users WHERE username = N'casual_user';

INSERT INTO user_profiles (user_id, real_name, student_no, college, grade, bio, verified_at, contact_masked)
SELECT id, N'警告用户', N'20260008', N'法学院', N'2021',
       N'因多次交易纠纷被警告，信用分已降至较低水平。',
       SYSDATETIME(), N'202****08'
FROM users WHERE username = N'warned_user';

INSERT INTO user_profiles (user_id, real_name, student_no, college, grade, bio, verified_at, contact_masked)
SELECT id, N'系统管理员', N'ADMIN001', N'信息中心', N'2020',
       N'平台管理员账号，负责举报处理、用户管理和后台配置。',
       SYSDATETIME(), N'ADMIN****'
FROM users WHERE username = N'demo_admin';

INSERT INTO user_profiles (user_id, real_name, student_no, college, grade, bio, verified_at, contact_masked)
SELECT id, N'商品审核员', N'PRODREV001', N'运营中心', N'2020',
       N'商品审核专员账号，仅用于处理待审核商品的通过与拒绝。',
       SYSDATETIME(), N'PROD****'
FROM users WHERE username = N'demo_product_reviewer';

INSERT INTO user_profiles (user_id, real_name, student_no, college, grade, bio, verified_at, contact_masked)
SELECT id, N'超级管理员', N'SYSADMIN001', N'信息中心', N'2019',
       N'超级管理员账号，拥有最高后台权限。',
       SYSDATETIME(), N'SYS****'
FROM users WHERE username = N'demo_sysadmin';

INSERT INTO credit_records (user_id, delta, score_after, reason, ref_type, ref_id, created_at)
SELECT id, 32, 92, N'演示信用分初始化', N'SEED', NULL, DATEADD(DAY, -10, SYSDATETIME()) FROM users WHERE username = N'demo_buyer'
UNION ALL SELECT id, 28, 88, N'演示信用分初始化', N'SEED', NULL, DATEADD(DAY, -10, SYSDATETIME()) FROM users WHERE username = N'demo_seller'
UNION ALL SELECT id, -5, 55, N'聊天违规扣除信用分', N'SEED', NULL, DATEADD(DAY, -10, SYSDATETIME()) FROM users WHERE username = N'muted_user'
UNION ALL SELECT id, -40, 20, N'严重违规扣除信用分', N'SEED', NULL, DATEADD(DAY, -10, SYSDATETIME()) FROM users WHERE username = N'banned_user'
UNION ALL SELECT id, 38, 98, N'演示信用分初始化', N'SEED', NULL, DATEADD(DAY, -10, SYSDATETIME()) FROM users WHERE username = N'star_user'
UNION ALL SELECT id, 12, 72, N'演示信用分初始化', N'SEED', NULL, DATEADD(DAY, -10, SYSDATETIME()) FROM users WHERE username = N'casual_user'
UNION ALL SELECT id, -18, 42, N'多次交易纠纷扣除信用分', N'SEED', NULL, DATEADD(DAY, -10, SYSDATETIME()) FROM users WHERE username = N'warned_user';

INSERT INTO point_records (user_id, delta, balance_after, reason, ref_type, ref_id, created_at)
SELECT id, 10, 10, N'每日签到', N'CHECK_IN', 1, DATEADD(DAY, -5, SYSDATETIME()) FROM users WHERE username = N'demo_buyer'
UNION ALL SELECT id, 20, 30, N'完善资料', N'TASK', 101, DATEADD(DAY, -4, SYSDATETIME()) FROM users WHERE username = N'demo_buyer'
UNION ALL SELECT id, 30, 60, N'首次发布商品', N'TASK', 103, DATEADD(DAY, -3, SYSDATETIME()) FROM users WHERE username = N'demo_buyer'
UNION ALL SELECT id, 40, 100, N'完成首次交易', N'TASK', 104, DATEADD(DAY, -2, SYSDATETIME()) FROM users WHERE username = N'demo_buyer'
UNION ALL SELECT id, 60, 160, N'平台活动奖励', N'BONUS', NULL, DATEADD(DAY, -1, SYSDATETIME()) FROM users WHERE username = N'demo_buyer'
UNION ALL SELECT id, 20, 20, N'完善资料', N'TASK', 101, DATEADD(DAY, -3, SYSDATETIME()) FROM users WHERE username = N'demo_seller'
UNION ALL SELECT id, 30, 50, N'首次发布商品', N'TASK', 103, DATEADD(DAY, -2, SYSDATETIME()) FROM users WHERE username = N'demo_seller'
UNION ALL SELECT id, 30, 80, N'每日签到奖励', N'CHECK_IN', 2, DATEADD(DAY, -1, SYSDATETIME()) FROM users WHERE username = N'demo_seller'
UNION ALL SELECT id, 20, 20, N'每日签到', N'CHECK_IN', 3, DATEADD(DAY, -1, SYSDATETIME()) FROM users WHERE username = N'muted_user'
UNION ALL SELECT id, 300, 300, N'平台活动奖励', N'BONUS', NULL, DATEADD(DAY, -1, SYSDATETIME()) FROM users WHERE username = N'star_user'
UNION ALL SELECT id, 45, 45, N'浏览商品奖励', N'BONUS', NULL, DATEADD(DAY, -1, SYSDATETIME()) FROM users WHERE username = N'casual_user'
UNION ALL SELECT id, 10, 10, N'每日签到', N'CHECK_IN', 4, DATEADD(DAY, -1, SYSDATETIME()) FROM users WHERE username = N'warned_user';

INSERT INTO user_mutes (user_id, muted_by, reason, muted_until)
SELECT muted.id, admin_user.id, N'多次发布不当言论', DATEADD(DAY, 5, SYSDATETIME())
FROM users muted
CROSS JOIN users admin_user
WHERE muted.username = N'muted_user'
  AND admin_user.username = N'demo_admin';

INSERT INTO point_redemptions (user_id, item_code, item_name, cost_points, status, created_at)
SELECT id, N'COUPON_10', N'10元优惠券', 200, N'SUCCESS', DATEADD(DAY, -2, SYSDATETIME()) FROM users WHERE username = N'demo_buyer'
UNION ALL SELECT id, N'PROFILE_BADGE', N'个人主页徽章', 120, N'SUCCESS', DATEADD(DAY, -1, SYSDATETIME()) FROM users WHERE username = N'demo_seller'
UNION ALL SELECT id, N'COUPON_30', N'30元优惠券', 500, N'SUCCESS', DATEADD(DAY, -5, SYSDATETIME()) FROM users WHERE username = N'star_user'
UNION ALL SELECT id, N'COUPON_10', N'10元优惠券', 200, N'FAILED', DATEADD(DAY, -1, SYSDATETIME()) FROM users WHERE username = N'casual_user';

INSERT INTO categories (name, sort_order, status)
VALUES
  (N'数码设备', 10, N'ACTIVE'),
  (N'图书资料', 20, N'ACTIVE'),
  (N'生活用品', 30, N'ACTIVE');

INSERT INTO products (seller_id, category_id, title, description, price, original_price, condition_level, campus, trade_modes, status)
SELECT seller.id, category.id, N'校园二手 MacBook 保护壳',
       N'适合 13 寸 MacBook，轻微使用痕迹。半透明磨砂材质，附赠键盘膜。',
       39.00, 99.00, N'GOOD', N'主校区', N'MEETUP', N'ACTIVE'
FROM users seller
CROSS JOIN categories category
WHERE seller.username = N'demo_seller'
  AND category.name = N'数码设备';

INSERT INTO products (seller_id, category_id, title, description, price, original_price, condition_level, campus, trade_modes, status)
SELECT seller.id, category.id, N'数据结构教材与习题集',
       N'课程复习资料，附少量笔记。包含课后习题答案和期末复习重点。',
       26.00, 68.00, N'FAIR', N'主校区', N'MEETUP', N'ACTIVE'
FROM users seller
CROSS JOIN categories category
WHERE seller.username = N'demo_seller'
  AND category.name = N'图书资料';

PRINT N'[Seed] 演示账号、学生认证、信用/积分、分类和商品数据已就绪（统一密码: demo123）';
GO
