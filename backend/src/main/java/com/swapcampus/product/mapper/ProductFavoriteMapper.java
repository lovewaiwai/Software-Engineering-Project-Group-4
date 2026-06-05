package com.swapcampus.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swapcampus.product.entity.ProductFavoriteEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductFavoriteMapper extends BaseMapper<ProductFavoriteEntity> {

    @Delete("""
            DELETE FROM product_favorites
            WHERE user_id = #{userId} AND product_id = #{productId}
            """)
    int deleteFavorite(@Param("userId") Long userId, @Param("productId") Long productId);
}
