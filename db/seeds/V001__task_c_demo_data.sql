USE SwapCampus;
GO

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
