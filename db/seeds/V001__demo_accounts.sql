USE SwapCampus;
GO

SET ANSI_NULLS ON;
GO
SET QUOTED_IDENTIFIER ON;
GO

IF NOT EXISTS (SELECT 1 FROM users WHERE username = N'demo_buyer')
BEGIN
  INSERT INTO users (username, password_hash, email, role, status, credit_score, point_balance, is_deleted)
  VALUES
    (N'demo_buyer', N'$2a$10$nxfgmK3S7DxblPebdDXy4OFRfZCZrc64Lur1sB9ytT7UImtJHH87C', N'buyer@swapcampus.local', N'USER', N'ACTIVE', 92, 160, 0);

  INSERT INTO user_profiles (user_id, real_name, student_no, college, grade, bio, verified_at, contact_masked)
  SELECT id, N'演示买家', N'20260001', N'计算机学院', N'2022', N'用于测试普通用户、聊天、积分和个人资料。', SYSDATETIME(), N'202****01'
  FROM users WHERE username = N'demo_buyer';

  INSERT INTO credit_records (user_id, delta, score_after, reason, ref_type)
  SELECT id, 32, 92, N'演示信用分初始化', N'SEED'
  FROM users WHERE username = N'demo_buyer';
END
GO

IF NOT EXISTS (SELECT 1 FROM users WHERE username = N'demo_seller')
BEGIN
  INSERT INTO users (username, password_hash, email, role, status, credit_score, point_balance, is_deleted)
  VALUES
    (N'demo_seller', N'$2a$10$9tQXG7HXtY6dJgp2OtBzkORNZJ9/ndqT89cMp0WKFI32bUFnHjUgW', N'seller@swapcampus.local', N'USER', N'ACTIVE', 88, 80, 0);

  INSERT INTO user_profiles (user_id, real_name, student_no, college, grade, bio, verified_at, contact_masked)
  SELECT id, N'演示卖家', N'20260002', N'经济管理学院', N'2021', N'用于测试商品详情页联系卖家和聊天收发。', SYSDATETIME(), N'202****02'
  FROM users WHERE username = N'demo_seller';
END
GO

IF NOT EXISTS (SELECT 1 FROM users WHERE username = N'muted_user')
BEGIN
  INSERT INTO users (username, password_hash, email, role, status, credit_score, point_balance, is_deleted)
  VALUES
    (N'muted_user', N'$2a$10$ZnL1uGXF7YwgFCSzRBKUsueQL20xOTDzQOmnJYSaunNOleEiQ5Vn6', N'muted@swapcampus.local', N'USER', N'ACTIVE', 55, 20, 0);

  INSERT INTO user_profiles (user_id, real_name, student_no, college, grade, bio, verified_at, contact_masked)
  SELECT id, N'禁言用户', N'20260003', N'工学院', N'2020', N'用于测试禁言后无法发送聊天消息。', SYSDATETIME(), N'202****03'
  FROM users WHERE username = N'muted_user';
END
GO

IF NOT EXISTS (SELECT 1 FROM users WHERE username = N'banned_user')
BEGIN
  INSERT INTO users (username, password_hash, email, role, status, credit_score, point_balance, is_deleted)
  VALUES
    (N'banned_user', N'$2a$10$pQe/NtBmZBRqWoLwBbnNneU8irZYprHcD2gIxieIxzUnUeEv71aJ.', N'banned@swapcampus.local', N'USER', N'BANNED', 20, 0, 0);

  INSERT INTO user_profiles (user_id, real_name, student_no, college, grade, bio, verified_at, contact_masked)
  SELECT id, N'封禁用户', N'20260004', N'林学院', N'2019', N'用于测试封禁账号登录拦截。', SYSDATETIME(), N'202****04'
  FROM users WHERE username = N'banned_user';
END
GO

IF OBJECT_ID(N'dbo.user_mutes', N'U') IS NOT NULL
   AND NOT EXISTS (
     SELECT 1
     FROM user_mutes
     WHERE user_id = (SELECT id FROM users WHERE username = N'muted_user')
       AND muted_until > SYSDATETIME()
   )
BEGIN
  INSERT INTO user_mutes (user_id, muted_by, reason, muted_until)
  SELECT muted.id, reviewer.id, N'演示禁言账号', DATEADD(DAY, 7, SYSDATETIME())
  FROM users muted
  LEFT JOIN users reviewer ON reviewer.username = N'reviewer'
  WHERE muted.username = N'muted_user';
END
GO

IF NOT EXISTS (SELECT 1 FROM categories WHERE name = N'数码设备')
BEGIN
  INSERT INTO categories (name, sort_order, status)
  VALUES (N'数码设备', 10, N'ACTIVE');
END
GO

IF NOT EXISTS (SELECT 1 FROM categories WHERE name = N'图书资料')
BEGIN
  INSERT INTO categories (name, sort_order, status)
  VALUES (N'图书资料', 20, N'ACTIVE');
END
GO

IF NOT EXISTS (SELECT 1 FROM products WHERE title = N'校园二手 MacBook 保护壳')
BEGIN
  INSERT INTO products (seller_id, category_id, title, description, price, original_price, condition_level, campus, trade_modes, status)
  SELECT seller.id, category.id, N'校园二手 MacBook 保护壳', N'适合 13 寸 MacBook，轻微使用痕迹。', 39.00, 99.00, N'GOOD', N'主校区', N'MEETUP', N'ON_SALE'
  FROM users seller
  CROSS JOIN categories category
  WHERE seller.username = N'demo_seller'
    AND category.name = N'数码设备';
END
GO

IF NOT EXISTS (SELECT 1 FROM products WHERE title = N'数据结构教材与习题集')
BEGIN
  INSERT INTO products (seller_id, category_id, title, description, price, original_price, condition_level, campus, trade_modes, status)
  SELECT seller.id, category.id, N'数据结构教材与习题集', N'课程复习资料，附少量笔记。', 26.00, 68.00, N'FAIR', N'主校区', N'MEETUP', N'ON_SALE'
  FROM users seller
  CROSS JOIN categories category
  WHERE seller.username = N'demo_seller'
    AND category.name = N'图书资料';
END
GO
