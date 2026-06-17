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
  (N'demo_seller', @demoPasswordHash, N'seller@swapcampus.local', N'USER', N'ACTIVE', 88, 300, 0),
  (N'muted_user', @demoPasswordHash, N'muted@swapcampus.local', N'USER', N'ACTIVE', 55, 20, 0),
  (N'banned_user', @demoPasswordHash, N'banned@swapcampus.local', N'USER', N'BANNED', 20, 0, 0),
  (N'new_user', @demoPasswordHash, N'new@swapcampus.local', N'USER', N'ACTIVE', 60, 0, 0),
  (N'star_user', @demoPasswordHash, N'star@swapcampus.local', N'USER', N'ACTIVE', 98, 300, 0),
  (N'casual_user', @demoPasswordHash, N'casual@swapcampus.local', N'USER', N'ACTIVE', 72, 45, 0),
  (N'warned_user', @demoPasswordHash, N'warned@swapcampus.local', N'USER', N'ACTIVE', 42, 10, 0),
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
UNION ALL SELECT id, 20, 30, N'完善资料', N'TASK', (SELECT id FROM point_tasks WHERE code = N'COMPLETE_PROFILE'), DATEADD(DAY, -4, SYSDATETIME()) FROM users WHERE username = N'demo_buyer'
UNION ALL SELECT id, 30, 60, N'首次发布商品', N'TASK', (SELECT id FROM point_tasks WHERE code = N'FIRST_PUBLISH'), DATEADD(DAY, -3, SYSDATETIME()) FROM users WHERE username = N'demo_buyer'
UNION ALL SELECT id, 40, 100, N'完成首次交易', N'TASK', (SELECT id FROM point_tasks WHERE code = N'FIRST_TRADE'), DATEADD(DAY, -2, SYSDATETIME()) FROM users WHERE username = N'demo_buyer'
UNION ALL SELECT id, 60, 160, N'平台活动奖励', N'BONUS', NULL, DATEADD(DAY, -1, SYSDATETIME()) FROM users WHERE username = N'demo_buyer'
UNION ALL SELECT id, 20, 20, N'完善资料', N'TASK', (SELECT id FROM point_tasks WHERE code = N'COMPLETE_PROFILE'), DATEADD(DAY, -3, SYSDATETIME()) FROM users WHERE username = N'demo_seller'
UNION ALL SELECT id, 30, 50, N'首次发布商品', N'TASK', (SELECT id FROM point_tasks WHERE code = N'FIRST_PUBLISH'), DATEADD(DAY, -2, SYSDATETIME()) FROM users WHERE username = N'demo_seller'
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
  AND admin_user.username = N'demo_sysadmin';

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

INSERT INTO locker_stations (name, location, status)
VALUES
  (N'主校区服务中心 Mock 柜机', N'主校区服务中心一楼大厅', N'ACTIVE'),
  (N'东区宿舍 Mock 柜机', N'东区 3 号宿舍楼入口', N'ACTIVE');

INSERT INTO locker_boxes (station_id, box_no, size, status)
SELECT station.id, box.box_no, box.size, N'EMPTY'
FROM locker_stations station
CROSS APPLY (VALUES
  (N'M-01', N'M'), (N'M-02', N'M'), (N'M-03', N'M'),
  (N'S-01', N'S'), (N'L-01', N'L')
) AS box(box_no, size)
WHERE station.name IN (N'主校区服务中心 Mock 柜机', N'东区宿舍 Mock 柜机');

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

-- Task C 商品接口演示数据
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

SET NOCOUNT ON;

IF NOT EXISTS (SELECT 1 FROM users WHERE username = N'demo_seller')
BEGIN
  INSERT INTO users (username, password_hash, phone, email, role, status, credit_score, point_balance, is_deleted)
  VALUES
    (N'demo_seller', N'$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5m4nYh1FvO8GZ1vDfQYMSfTPxI2X6', N'13800000001', N'seller@swapcampus.local', N'USER', N'ACTIVE', 80, 100, 0);

  INSERT INTO user_profiles (user_id, real_name, student_no, college, grade, avatar_url, bio, verified_at, contact_masked)
  SELECT id, N'演示卖家', N'2026000001', N'软件学院', N'2023级', NULL, N'用于任务 C 商品接口演示', SYSDATETIME(), N'138****0001'
  FROM users
  WHERE username = N'demo_seller';
END

IF NOT EXISTS (SELECT 1 FROM categories WHERE name = N'教材教辅' AND status = N'ACTIVE')
  INSERT INTO categories (parent_id, name, sort_order, status)
  VALUES (NULL, N'教材教辅', 10, N'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM categories WHERE name = N'数码电子' AND status = N'ACTIVE')
  INSERT INTO categories (parent_id, name, sort_order, status)
  VALUES (NULL, N'数码电子', 20, N'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM categories WHERE name = N'生活用品' AND status = N'ACTIVE')
  INSERT INTO categories (parent_id, name, sort_order, status)
  VALUES (NULL, N'生活用品', 30, N'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM categories WHERE name = N'运动户外' AND status = N'ACTIVE')
  INSERT INTO categories (parent_id, name, sort_order, status)
  VALUES (NULL, N'运动户外', 40, N'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM tags WHERE name = N'九成新')
BEGIN
  INSERT INTO tags (name, status)
  VALUES
    (N'九成新', N'ACTIVE'),
    (N'可小刀', N'ACTIVE'),
    (N'当天可取', N'ACTIVE'),
    (N'教材', N'ACTIVE'),
    (N'数码', N'ACTIVE');
END

DECLARE @sellerId BIGINT = (SELECT TOP 1 id FROM users WHERE username = N'demo_seller' ORDER BY id);
DECLARE @bookCategoryId BIGINT = (SELECT TOP 1 id FROM categories WHERE name = N'教材教辅' AND status = N'ACTIVE' ORDER BY id);
DECLARE @digitalCategoryId BIGINT = (SELECT TOP 1 id FROM categories WHERE name = N'数码电子' AND status = N'ACTIVE' ORDER BY id);
DECLARE @lifeCategoryId BIGINT = (SELECT TOP 1 id FROM categories WHERE name = N'生活用品' AND status = N'ACTIVE' ORDER BY id);

IF NOT EXISTS (SELECT 1 FROM products WHERE title = N'九成新高等数学教材')
BEGIN
  INSERT INTO products (seller_id, category_id, title, description, price, original_price, condition_level, campus, trade_modes, status, view_count, favorite_count, is_deleted)
  VALUES
    (@sellerId, @bookCategoryId, N'九成新高等数学教材', N'同济版高等数学上下册，少量划线，适合大一课程复习。', 35.00, 89.00, N'LIKE_NEW', N'主校区', N'MEETUP,LOCKER', N'ACTIVE', 42, 5, 0),
    (@sellerId, @digitalCategoryId, N'蓝牙耳机 支持降噪', N'续航正常，配充电盒，适合通勤和自习室使用。', 99.00, 299.00, N'GOOD', N'主校区', N'MEETUP', N'ACTIVE', 88, 12, 0),
    (@sellerId, @digitalCategoryId, N'机械键盘 87 键', N'茶轴，键帽完整，宿舍自提。', 129.00, 399.00, N'GOOD', N'东校区', N'MEETUP,LOCKER', N'ACTIVE', 61, 9, 0),
    (@sellerId, @lifeCategoryId, N'宿舍收纳箱三件套', N'搬宿舍闲置，干净无破损。', 25.00, 60.00, N'NORMAL', N'主校区', N'MEETUP', N'ACTIVE', 18, 2, 0),
    (@sellerId, @bookCategoryId, N'考研英语真题资料', N'近十年真题和解析，部分做过标记。', 45.00, 120.00, N'GOOD', N'西校区', N'LOCKER', N'ACTIVE', 35, 4, 0),
    (@sellerId, @lifeCategoryId, N'待审核示例商品', N'这个商品用于测试搜索不会返回待审核状态。', 10.00, 30.00, N'NORMAL', N'主校区', N'MEETUP', N'PENDING_REVIEW', 0, 0, 0);
END

IF NOT EXISTS (
  SELECT 1
  FROM product_images pi
  INNER JOIN products p ON p.id = pi.product_id
  WHERE p.title = N'九成新高等数学教材'
)
BEGIN
  INSERT INTO product_images (product_id, url, sort_order)
  SELECT id, N'https://dummyimage.com/600x400/2f6fed/ffffff&text=Math+Book', 0 FROM products WHERE title = N'九成新高等数学教材';
END

IF NOT EXISTS (
  SELECT 1
  FROM product_images pi
  INNER JOIN products p ON p.id = pi.product_id
  WHERE p.title = N'蓝牙耳机 支持降噪'
)
BEGIN
  INSERT INTO product_images (product_id, url, sort_order)
  SELECT id, N'https://dummyimage.com/600x400/111827/ffffff&text=Headphone', 0 FROM products WHERE title = N'蓝牙耳机 支持降噪';
END

IF NOT EXISTS (
  SELECT 1
  FROM product_images pi
  INNER JOIN products p ON p.id = pi.product_id
  WHERE p.title = N'机械键盘 87 键'
)
BEGIN
  INSERT INTO product_images (product_id, url, sort_order)
  SELECT id, N'https://dummyimage.com/600x400/16a34a/ffffff&text=Keyboard', 0 FROM products WHERE title = N'机械键盘 87 键';
END

IF NOT EXISTS (
  SELECT 1
  FROM product_images pi
  INNER JOIN products p ON p.id = pi.product_id
  WHERE p.title = N'宿舍收纳箱三件套'
)
BEGIN
  INSERT INTO product_images (product_id, url, sort_order)
  SELECT id, N'https://dummyimage.com/600x400/f59e0b/ffffff&text=Storage+Box', 0 FROM products WHERE title = N'宿舍收纳箱三件套';
END

IF NOT EXISTS (
  SELECT 1
  FROM product_images pi
  INNER JOIN products p ON p.id = pi.product_id
  WHERE p.title = N'考研英语真题资料'
)
BEGIN
  INSERT INTO product_images (product_id, url, sort_order)
  SELECT id, N'https://dummyimage.com/600x400/ef4444/ffffff&text=English', 0 FROM products WHERE title = N'考研英语真题资料';
END

PRINT N'Task C demo seed data inserted.';
GO

-- 合并原 V002 分类去重修复
DECLARE @digitalDeviceId BIGINT;
DECLARE @otherCategoryId BIGINT;

SELECT TOP 1 @digitalDeviceId = id
FROM categories
WHERE name = N'数码设备'
  AND status = N'ACTIVE'
ORDER BY id;

SELECT TOP 1 @otherCategoryId = id
FROM categories
WHERE name = N'其他'
  AND status = N'ACTIVE'
ORDER BY id;

IF @digitalDeviceId IS NOT NULL
BEGIN
  IF @otherCategoryId IS NULL OR @otherCategoryId = @digitalDeviceId
  BEGIN
    UPDATE categories
    SET name = N'其他',
        sort_order = 60
    WHERE id = @digitalDeviceId;
  END
  ELSE
  BEGIN
    UPDATE products
    SET category_id = @otherCategoryId
    WHERE category_id = @digitalDeviceId;

    UPDATE categories
    SET status = N'INACTIVE'
    WHERE id = @digitalDeviceId;
  END
END
GO

IF NOT EXISTS (SELECT 1 FROM categories WHERE name = N'其他' AND status = N'ACTIVE')
BEGIN
  INSERT INTO categories (parent_id, name, sort_order, status)
  VALUES (NULL, N'其他', 60, N'ACTIVE');
END
GO

IF (SELECT COUNT(*) FROM categories WHERE name = N'生活用品' AND status = N'ACTIVE') > 1
BEGIN
  DECLARE @duplicateLifeId BIGINT;
  SELECT @duplicateLifeId = MAX(id)
  FROM categories
  WHERE name = N'生活用品'
    AND status = N'ACTIVE';

  UPDATE categories
  SET name = N'学习文具',
      sort_order = 50
  WHERE id = @duplicateLifeId;
END
GO

IF NOT EXISTS (SELECT 1 FROM categories WHERE name = N'学习文具' AND status = N'ACTIVE')
BEGIN
  INSERT INTO categories (parent_id, name, sort_order, status)
  VALUES (NULL, N'学习文具', 50, N'ACTIVE');
END
GO

PRINT N'Duplicate category seed fixed.';
GO

-- 合并原 V003 扩展标签数据
DECLARE @tags TABLE (
  name NVARCHAR(50) NOT NULL PRIMARY KEY
);

INSERT INTO @tags (name)
VALUES
  (N'全新'),
  (N'仅拆封'),
  (N'九成新'),
  (N'八成新'),
  (N'轻微瑕疵'),
  (N'功能正常'),
  (N'配件齐全'),
  (N'原包装'),
  (N'保修期内'),
  (N'可小刀'),
  (N'不议价'),
  (N'当天可取'),
  (N'校内自提'),
  (N'可送到楼下'),
  (N'可邮寄'),
  (N'教材'),
  (N'考研资料'),
  (N'课堂笔记'),
  (N'习题集'),
  (N'学习文具'),
  (N'数码'),
  (N'电脑配件'),
  (N'手机配件'),
  (N'耳机音箱'),
  (N'摄影器材'),
  (N'运动装备'),
  (N'宿舍用品'),
  (N'收纳整理'),
  (N'生活用品'),
  (N'美妆护肤'),
  (N'服饰鞋包'),
  (N'票券卡券'),
  (N'手工自制'),
  (N'毕业出清'),
  (N'其他');

INSERT INTO tags (name, status)
SELECT t.name, N'ACTIVE'
FROM @tags t
WHERE NOT EXISTS (
  SELECT 1
  FROM tags existing
  WHERE existing.name = t.name
);
GO

-- 生成不少于 200 条商品种子数据，满足 T-02 选题指南验收要求。
-- 图片使用占位图，重点保证商品列表、搜索、筛选、推荐和浏览历史有足够数据可演示。
DECLARE @generatedProductCount INT = (
  SELECT COUNT(*)
  FROM products
  WHERE title LIKE N'演示商品-%'
);

IF @generatedProductCount < 210
BEGIN
  DECLARE @categories TABLE (
    rn INT IDENTITY(1,1) PRIMARY KEY,
    category_id BIGINT NOT NULL,
    category_name NVARCHAR(50) NOT NULL,
    tag_name NVARCHAR(50) NOT NULL
  );

  INSERT INTO @categories (category_id, category_name, tag_name)
  SELECT id, name,
         CASE
           WHEN name IN (N'教材教辅', N'图书资料') THEN N'教材'
           WHEN name IN (N'数码电子') THEN N'数码'
           WHEN name IN (N'生活用品') THEN N'生活用品'
           WHEN name IN (N'运动户外') THEN N'运动装备'
           WHEN name IN (N'学习文具') THEN N'学习文具'
           ELSE N'其他'
         END
  FROM categories
  WHERE status = N'ACTIVE'
    AND name IN (N'教材教辅', N'图书资料', N'数码电子', N'生活用品', N'运动户外', N'学习文具', N'其他')
  ORDER BY sort_order, id;

  DECLARE @sellers TABLE (
    rn INT IDENTITY(1,1) PRIMARY KEY,
    seller_id BIGINT NOT NULL
  );

  INSERT INTO @sellers (seller_id)
  SELECT id
  FROM users
  WHERE username IN (N'demo_seller', N'star_user', N'casual_user', N'new_user')
    AND status = N'ACTIVE'
  ORDER BY id;

  DECLARE @categoryCount INT = (SELECT COUNT(*) FROM @categories);
  DECLARE @sellerCount INT = (SELECT COUNT(*) FROM @sellers);
  DECLARE @i INT = 1;
  DECLARE @seq NVARCHAR(3);
  DECLARE @categoryRow INT;
  DECLARE @sellerRow INT;
  DECLARE @variant INT;
  DECLARE @categoryId BIGINT;
  DECLARE @categoryName NVARCHAR(50);
  DECLARE @tagName NVARCHAR(50);
  DECLARE @sellerId BIGINT;
  DECLARE @itemName NVARCHAR(80);
  DECLARE @title NVARCHAR(120);
  DECLARE @description NVARCHAR(MAX);
  DECLARE @price DECIMAL(10,2);
  DECLARE @originalPrice DECIMAL(10,2);
  DECLARE @condition NVARCHAR(20);
  DECLARE @campus NVARCHAR(50);
  DECLARE @tradeModes NVARCHAR(100);
  DECLARE @status NVARCHAR(30);
  DECLARE @productId BIGINT;
  DECLARE @tagId BIGINT;

  WHILE @i <= 210 AND @categoryCount > 0 AND @sellerCount > 0
  BEGIN
    SET @seq = RIGHT(N'000' + CAST(@i AS NVARCHAR(3)), 3);
    SET @categoryRow = ((@i - 1) % @categoryCount) + 1;
    SET @sellerRow = ((@i - 1) % @sellerCount) + 1;
    SET @variant = ((@i - 1) % 10) + 1;
    SET @categoryId = NULL;
    SET @categoryName = NULL;
    SET @tagName = NULL;
    SET @sellerId = NULL;
    SET @productId = NULL;
    SET @tagId = NULL;

    SELECT @categoryId = category_id, @categoryName = category_name, @tagName = tag_name
    FROM @categories
    WHERE rn = @categoryRow;

    SELECT @sellerId = seller_id
    FROM @sellers
    WHERE rn = @sellerRow;

    SET @itemName = CASE
      WHEN @categoryName IN (N'教材教辅', N'图书资料') THEN
        CASE @variant
          WHEN 1 THEN N'高等数学教材'
          WHEN 2 THEN N'数据结构教材'
          WHEN 3 THEN N'考研英语真题'
          WHEN 4 THEN N'操作系统课堂笔记'
          WHEN 5 THEN N'线性代数习题集'
          WHEN 6 THEN N'概率论复习资料'
          WHEN 7 THEN N'数据库系统概论'
          WHEN 8 THEN N'计算机网络教材'
          WHEN 9 THEN N'马克思主义原理资料'
          ELSE N'大学物理实验报告册'
        END
      WHEN @categoryName = N'数码电子' THEN
        CASE @variant
          WHEN 1 THEN N'蓝牙耳机'
          WHEN 2 THEN N'机械键盘'
          WHEN 3 THEN N'无线鼠标'
          WHEN 4 THEN N'移动电源'
          WHEN 5 THEN N'护眼台灯'
          WHEN 6 THEN N'手机支架'
          WHEN 7 THEN N'平板保护壳'
          WHEN 8 THEN N'电脑散热支架'
          WHEN 9 THEN N'Type-C 扩展坞'
          ELSE N'迷你音箱'
        END
      WHEN @categoryName = N'生活用品' THEN
        CASE @variant
          WHEN 1 THEN N'宿舍收纳箱'
          WHEN 2 THEN N'床上书桌'
          WHEN 3 THEN N'衣架套装'
          WHEN 4 THEN N'桌面小风扇'
          WHEN 5 THEN N'保温杯'
          WHEN 6 THEN N'台式化妆镜'
          WHEN 7 THEN N'床帘支架'
          WHEN 8 THEN N'插线板'
          WHEN 9 THEN N'小型置物架'
          ELSE N'宿舍地垫'
        END
      WHEN @categoryName = N'运动户外' THEN
        CASE @variant
          WHEN 1 THEN N'篮球'
          WHEN 2 THEN N'羽毛球拍'
          WHEN 3 THEN N'瑜伽垫'
          WHEN 4 THEN N'跑步臂包'
          WHEN 5 THEN N'露营灯'
          WHEN 6 THEN N'跳绳'
          WHEN 7 THEN N'护膝'
          WHEN 8 THEN N'运动水壶'
          WHEN 9 THEN N'乒乓球拍'
          ELSE N'健身弹力带'
        END
      WHEN @categoryName = N'学习文具' THEN
        CASE @variant
          WHEN 1 THEN N'科学计算器'
          WHEN 2 THEN N'绘图画板'
          WHEN 3 THEN N'文件夹套装'
          WHEN 4 THEN N'钢笔'
          WHEN 5 THEN N'便签套装'
          WHEN 6 THEN N'订书机'
          WHEN 7 THEN N'马克笔'
          WHEN 8 THEN N'考试透明笔袋'
          WHEN 9 THEN N'A4 活页纸'
          ELSE N'绘图尺套装'
        END
      ELSE
        CASE @variant
          WHEN 1 THEN N'校园卡套'
          WHEN 2 THEN N'桌游卡牌'
          WHEN 3 THEN N'毕业纪念摆件'
          WHEN 4 THEN N'手工钥匙扣'
          WHEN 5 THEN N'演出票券'
          WHEN 6 THEN N'帆布袋'
          WHEN 7 THEN N'旧书签套装'
          WHEN 8 THEN N'拍立得相纸'
          WHEN 9 THEN N'宿舍门牌'
          ELSE N'其他闲置小物'
        END
    END;

    SET @title = N'演示商品-' + @seq + N' ' + @itemName;
    SET @description = N'用于 SwapCampus 演示的校园闲置商品，来源于同学个人闲置，支持搜索、筛选、推荐和交易流程测试。编号：' + @seq;
    SET @price = CAST(8 + ((@i * 7) % 260) + (@variant * 0.5) AS DECIMAL(10,2));
    SET @originalPrice = CAST(@price + 25 + ((@i * 11) % 180) AS DECIMAL(10,2));
    SET @condition = CASE @i % 4
      WHEN 0 THEN N'NEW'
      WHEN 1 THEN N'LIKE_NEW'
      WHEN 2 THEN N'GOOD'
      ELSE N'FAIR'
    END;
    SET @campus = CASE @i % 4
      WHEN 0 THEN N'主校区'
      WHEN 1 THEN N'东校区'
      WHEN 2 THEN N'西校区'
      ELSE N'北校区'
    END;
    SET @tradeModes = CASE @i % 3
      WHEN 0 THEN N'MEETUP,LOCKER'
      WHEN 1 THEN N'MEETUP'
      ELSE N'LOCKER'
    END;
    SET @status = CASE WHEN @i % 21 = 0 THEN N'PENDING_REVIEW' ELSE N'ACTIVE' END;

    IF NOT EXISTS (SELECT 1 FROM products WHERE title = @title)
    BEGIN
      INSERT INTO products (
        seller_id, category_id, title, description, price, original_price, condition_level,
        campus, trade_modes, status, view_count, favorite_count, created_at, updated_at, is_deleted
      )
      VALUES (
        @sellerId, @categoryId, @title, @description, @price, @originalPrice, @condition,
        @campus, @tradeModes, @status, (@i * 13) % 300, (@i * 5) % 60,
        DATEADD(DAY, -(@i % 45), SYSDATETIME()),
        DATEADD(DAY, -(@i % 20), SYSDATETIME()),
        0
      );

      SET @productId = SCOPE_IDENTITY();
      SET @tagId = (SELECT TOP 1 id FROM tags WHERE name = @tagName AND status = N'ACTIVE' ORDER BY id);

      INSERT INTO product_images (product_id, url, sort_order)
      VALUES (
        @productId,
        N'https://dummyimage.com/640x420/e2e8f0/334155&text=SwapCampus+' + @seq,
        0
      );

      IF @tagId IS NOT NULL
      BEGIN
        INSERT INTO product_tags (product_id, tag_id)
        VALUES (@productId, @tagId);
      END
    END

    SET @i += 1;
  END
END
GO
