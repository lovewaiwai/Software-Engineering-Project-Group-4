USE SwapCampus;
GO

SET NOCOUNT ON;

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
