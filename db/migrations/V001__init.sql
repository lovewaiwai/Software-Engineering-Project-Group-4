IF DB_ID(N'SwapCampus') IS NULL
BEGIN
  CREATE DATABASE SwapCampus;
END
GO

USE SwapCampus;
GO

SET ANSI_NULLS ON;
GO
SET QUOTED_IDENTIFIER ON;
GO

CREATE TABLE users (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  username NVARCHAR(50) NOT NULL UNIQUE,
  password_hash NVARCHAR(100) NOT NULL,
  phone NVARCHAR(20) NULL,
  email NVARCHAR(100) NULL,
  role NVARCHAR(20) NOT NULL DEFAULT 'USER',
  status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  credit_score INT NOT NULL DEFAULT 60,
  point_balance INT NOT NULL DEFAULT 0,
  created_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
  updated_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
  is_deleted BIT NOT NULL DEFAULT 0
);

CREATE TABLE user_profiles (
  user_id BIGINT PRIMARY KEY,
  real_name NVARCHAR(50) NULL,
  student_no NVARCHAR(30) NULL,
  college NVARCHAR(80) NULL,
  grade NVARCHAR(20) NULL,
  avatar_url NVARCHAR(500) NULL,
  bio NVARCHAR(500) NULL,
  verified_at DATETIME2(0) NULL,
  contact_masked NVARCHAR(100) NULL,
  CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE student_identities (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  student_no NVARCHAR(30) NOT NULL,
  real_name NVARCHAR(50) NOT NULL,
  college NVARCHAR(80) NOT NULL,
  grade NVARCHAR(20) NOT NULL,
  edu_password_hash NVARCHAR(100) NOT NULL,
  status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME()
);

CREATE TABLE categories (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  parent_id BIGINT NULL,
  name NVARCHAR(50) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE tags (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  name NVARCHAR(50) NOT NULL UNIQUE,
  status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE products (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  seller_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  title NVARCHAR(120) NOT NULL,
  description NVARCHAR(MAX) NULL,
  price DECIMAL(10,2) NOT NULL,
  original_price DECIMAL(10,2) NULL,
  condition_level NVARCHAR(20) NOT NULL,
  campus NVARCHAR(50) NULL,
  trade_modes NVARCHAR(100) NOT NULL DEFAULT 'MEETUP',
  status NVARCHAR(30) NOT NULL DEFAULT 'DRAFT',
  view_count INT NOT NULL DEFAULT 0,
  favorite_count INT NOT NULL DEFAULT 0,
  audit_reason NVARCHAR(300) NULL,
  created_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
  updated_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
  is_deleted BIT NOT NULL DEFAULT 0,
  CONSTRAINT fk_products_seller FOREIGN KEY (seller_id) REFERENCES users(id),
  CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE product_images (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  product_id BIGINT NOT NULL,
  url NVARCHAR(500) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
  CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE product_tags (
  product_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  PRIMARY KEY (product_id, tag_id),
  CONSTRAINT fk_product_tags_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT fk_product_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id)
);

CREATE TABLE product_favorites (
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  created_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
  PRIMARY KEY (user_id, product_id),
  CONSTRAINT fk_product_favorites_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_product_favorites_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE browse_records (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  user_id BIGINT NULL,
  product_id BIGINT NOT NULL,
  category_id BIGINT NULL,
  created_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
  CONSTRAINT fk_browse_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE orders (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  order_no NVARCHAR(40) NOT NULL UNIQUE,
  product_id BIGINT NOT NULL,
  buyer_id BIGINT NOT NULL,
  seller_id BIGINT NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  status NVARCHAR(30) NOT NULL DEFAULT 'CREATED',
  trade_mode NVARCHAR(20) NOT NULL DEFAULT 'MEETUP',
  created_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
  updated_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
  completed_at DATETIME2(0) NULL,
  CONSTRAINT fk_orders_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT fk_orders_buyer FOREIGN KEY (buyer_id) REFERENCES users(id),
  CONSTRAINT fk_orders_seller FOREIGN KEY (seller_id) REFERENCES users(id)
);

CREATE TABLE order_events (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  order_id BIGINT NOT NULL,
  from_status NVARCHAR(30) NULL,
  to_status NVARCHAR(30) NOT NULL,
  event_type NVARCHAR(50) NOT NULL,
  operator_id BIGINT NULL,
  note NVARCHAR(300) NULL,
  created_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
  CONSTRAINT fk_order_events_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE TABLE payments (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  payment_no NVARCHAR(40) NOT NULL UNIQUE,
  order_id BIGINT NOT NULL,
  provider NVARCHAR(30) NOT NULL DEFAULT 'MOCK',
  provider_trade_no NVARCHAR(80) NULL,
  amount DECIMAL(10,2) NOT NULL,
  status NVARCHAR(30) NOT NULL DEFAULT 'CREATED',
  pay_url NVARCHAR(500) NULL,
  paid_at DATETIME2(0) NULL,
  created_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
  CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE TABLE payment_events (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  payment_id BIGINT NOT NULL,
  event_type NVARCHAR(50) NOT NULL,
  payload NVARCHAR(MAX) NULL,
  created_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
  CONSTRAINT fk_payment_events_payment FOREIGN KEY (payment_id) REFERENCES payments(id)
);

CREATE TABLE locker_stations (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  name NVARCHAR(80) NOT NULL,
  location NVARCHAR(200) NOT NULL,
  status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE locker_boxes (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  station_id BIGINT NOT NULL,
  box_no NVARCHAR(30) NOT NULL,
  size NVARCHAR(20) NOT NULL DEFAULT 'M',
  status NVARCHAR(20) NOT NULL DEFAULT 'EMPTY',
  CONSTRAINT fk_locker_boxes_station FOREIGN KEY (station_id) REFERENCES locker_stations(id)
);

CREATE TABLE locker_tasks (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  task_no NVARCHAR(40) NOT NULL UNIQUE,
  order_id BIGINT NOT NULL,
  station_id BIGINT NOT NULL,
  box_id BIGINT NOT NULL,
  pickup_code NVARCHAR(20) NOT NULL,
  status NVARCHAR(30) NOT NULL DEFAULT 'RESERVED',
  stored_at DATETIME2(0) NULL,
  picked_up_at DATETIME2(0) NULL,
  created_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
  CONSTRAINT fk_locker_tasks_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE TABLE chat_sessions (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  product_id BIGINT NULL,
  order_id BIGINT NULL,
  buyer_id BIGINT NOT NULL,
  seller_id BIGINT NOT NULL,
  last_message_at DATETIME2(0) NULL,
  created_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME()
);

CREATE TABLE chat_messages (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  session_id BIGINT NOT NULL,
  sender_id BIGINT NOT NULL,
  message_type NVARCHAR(20) NOT NULL DEFAULT 'TEXT',
  content NVARCHAR(MAX) NOT NULL,
  image_url NVARCHAR(500) NULL,
  seq_no BIGINT NULL,
  status NVARCHAR(20) NOT NULL DEFAULT 'SENT',
  read_at DATETIME2(0) NULL,
  created_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
  CONSTRAINT fk_chat_messages_session FOREIGN KEY (session_id) REFERENCES chat_sessions(id)
);

CREATE TABLE reviews (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  order_id BIGINT NOT NULL,
  reviewer_id BIGINT NOT NULL,
  reviewee_id BIGINT NOT NULL,
  rating INT NOT NULL,
  content NVARCHAR(500) NULL,
  created_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
  CONSTRAINT fk_reviews_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE TABLE reports (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  reporter_id BIGINT NOT NULL,
  target_type NVARCHAR(30) NOT NULL,
  target_id BIGINT NOT NULL,
  session_id BIGINT NULL,
  reported_user_id BIGINT NULL,
  reason NVARCHAR(100) NOT NULL,
  description NVARCHAR(1000) NULL,
  reject_reason NVARCHAR(500) NULL,
  evidence_url NVARCHAR(500) NULL,
  status NVARCHAR(30) NOT NULL DEFAULT 'PENDING',
  created_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME()
);

CREATE TABLE report_actions (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  report_id BIGINT NOT NULL,
  admin_id BIGINT NOT NULL,
  action_type NVARCHAR(50) NOT NULL,
  note NVARCHAR(500) NULL,
  created_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
  CONSTRAINT fk_report_actions_report FOREIGN KEY (report_id) REFERENCES reports(id)
);

CREATE TABLE credit_records (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  user_id BIGINT NOT NULL,
  delta INT NOT NULL,
  score_after INT NOT NULL,
  reason NVARCHAR(100) NOT NULL,
  ref_type NVARCHAR(30) NULL,
  ref_id BIGINT NULL,
  created_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
  CONSTRAINT fk_credit_records_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE point_tasks (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  code NVARCHAR(50) NOT NULL UNIQUE,
  name NVARCHAR(80) NOT NULL,
  reward_points INT NOT NULL,
  task_type NVARCHAR(30) NOT NULL,
  status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

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

CREATE TABLE ai_suggestion_logs (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title NVARCHAR(120) NOT NULL,
  input_summary NVARCHAR(1000) NULL,
  suggested_category_id BIGINT NULL,
  suggested_tags NVARCHAR(300) NULL,
  suggested_min_price DECIMAL(10,2) NULL,
  suggested_max_price DECIMAL(10,2) NULL,
  provider NVARCHAR(30) NOT NULL DEFAULT 'MOCK',
  created_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME()
);

CREATE TABLE audit_logs (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  operator_id BIGINT NULL,
  action NVARCHAR(80) NOT NULL,
  target_type NVARCHAR(30) NULL,
  target_id BIGINT NULL,
  detail NVARCHAR(MAX) NULL,
  ip NVARCHAR(50) NULL,
  created_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME()
);

CREATE TABLE user_mutes (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  user_id BIGINT NOT NULL,
  muted_by BIGINT NULL,
  reason NVARCHAR(200) NULL,
  muted_until DATETIME2(0) NOT NULL,
  created_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
  CONSTRAINT fk_user_mutes_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE user_blocks (
  id BIGINT IDENTITY(1,1) PRIMARY KEY,
  blocker_id BIGINT NOT NULL,
  blocked_id BIGINT NOT NULL,
  created_at DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
  CONSTRAINT ux_user_blocks UNIQUE (blocker_id, blocked_id),
  CONSTRAINT fk_user_blocks_blocker FOREIGN KEY (blocker_id) REFERENCES users(id),
  CONSTRAINT fk_user_blocks_blocked FOREIGN KEY (blocked_id) REFERENCES users(id)
);

CREATE INDEX idx_products_search ON products(status, category_id, price, created_at);
CREATE INDEX idx_orders_buyer ON orders(buyer_id, created_at);
CREATE INDEX idx_orders_seller ON orders(seller_id, created_at);
CREATE INDEX idx_chat_messages_session ON chat_messages(session_id, created_at);
CREATE INDEX idx_chat_sessions_participants ON chat_sessions(buyer_id, seller_id, product_id);
CREATE INDEX idx_browse_records_user ON browse_records(user_id, created_at);
CREATE UNIQUE INDEX ux_user_profiles_student_no_not_null ON user_profiles(student_no) WHERE student_no IS NOT NULL;
CREATE UNIQUE INDEX ux_student_identities_student_no ON student_identities(student_no);
CREATE INDEX idx_student_identities_status ON student_identities(status);
CREATE INDEX idx_user_mutes_user_until ON user_mutes(user_id, muted_until);
CREATE UNIQUE INDEX ux_reports_reporter_target ON reports(reporter_id, target_type, target_id);
CREATE INDEX idx_credit_records_user_created_at ON credit_records(user_id, created_at);
CREATE INDEX idx_point_records_user_created_at ON point_records(user_id, created_at);
CREATE INDEX idx_point_records_user_ref ON point_records(user_id, ref_type, ref_id);
CREATE INDEX idx_point_redemptions_user_created_at ON point_redemptions(user_id, created_at);
GO
