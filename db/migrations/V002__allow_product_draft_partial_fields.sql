USE SwapCampus;
GO

ALTER TABLE products DROP CONSTRAINT fk_products_category;
GO

ALTER TABLE products ALTER COLUMN category_id BIGINT NULL;
ALTER TABLE products ALTER COLUMN title NVARCHAR(120) NULL;
ALTER TABLE products ALTER COLUMN price DECIMAL(10,2) NULL;
ALTER TABLE products ALTER COLUMN condition_level NVARCHAR(20) NULL;
GO

ALTER TABLE products
ADD CONSTRAINT fk_products_category
FOREIGN KEY (category_id) REFERENCES categories(id);
GO
