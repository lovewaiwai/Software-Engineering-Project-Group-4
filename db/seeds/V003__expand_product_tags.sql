USE SwapCampus;
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

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
