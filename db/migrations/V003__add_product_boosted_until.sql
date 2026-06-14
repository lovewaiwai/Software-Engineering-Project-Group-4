USE SwapCampus;
GO

-- 添加商品曝光加速字段
ALTER TABLE products ADD boosted_until DATETIME2(0) NULL;
GO

CREATE INDEX idx_products_boosted ON products(boosted_until);
GO
