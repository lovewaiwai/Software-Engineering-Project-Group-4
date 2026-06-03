package com.swapcampus.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swapcampus.product.entity.ProductEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<ProductEntity> {
    // TODO: Add product SQL Server queries when business tables are implemented.
}
