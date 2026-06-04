USE SwapCampus;
GO

SET ANSI_NULLS ON;
GO
SET QUOTED_IDENTIFIER ON;
GO

DECLARE @constraintName SYSNAME;
DECLARE @sql NVARCHAR(MAX);

SELECT @constraintName = kc.name
FROM sys.key_constraints kc
JOIN sys.index_columns ic
  ON kc.parent_object_id = ic.object_id
 AND kc.unique_index_id = ic.index_id
JOIN sys.columns c
  ON c.object_id = ic.object_id
 AND c.column_id = ic.column_id
WHERE kc.parent_object_id = OBJECT_ID(N'dbo.user_profiles')
  AND kc.type = 'UQ'
  AND c.name = N'student_no';

IF @constraintName IS NOT NULL
BEGIN
  SET @sql = N'ALTER TABLE dbo.user_profiles DROP CONSTRAINT ' + QUOTENAME(@constraintName);
  EXEC(@sql);
END
GO

IF NOT EXISTS (
  SELECT 1
  FROM sys.indexes
  WHERE object_id = OBJECT_ID(N'dbo.user_profiles')
    AND name = N'ux_user_profiles_student_no_not_null'
)
BEGIN
  CREATE UNIQUE INDEX ux_user_profiles_student_no_not_null
    ON dbo.user_profiles(student_no)
    WHERE student_no IS NOT NULL;
END
GO
