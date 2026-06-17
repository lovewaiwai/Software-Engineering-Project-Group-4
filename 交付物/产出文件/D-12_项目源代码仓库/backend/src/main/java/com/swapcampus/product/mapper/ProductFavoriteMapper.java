package com.swapcampus.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swapcampus.product.entity.ProductFavoriteEntity;
import com.swapcampus.product.entity.ProductEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductFavoriteMapper extends BaseMapper<ProductFavoriteEntity> {

    @Delete("""
            DELETE FROM product_favorites
            WHERE user_id = #{userId} AND product_id = #{productId}
            """)
    int deleteFavorite(@Param("userId") Long userId, @Param("productId") Long productId);

    @Select("""
            SELECT p.*
            FROM product_favorites f
            JOIN products p ON p.id = f.product_id
            WHERE f.user_id = #{userId}
              AND (p.is_deleted = 0 OR p.is_deleted IS NULL)
            ORDER BY f.created_at DESC
            OFFSET #{offset} ROWS FETCH NEXT #{pageSize} ROWS ONLY
            """)
    List<ProductEntity> selectFavoriteProductsByUser(@Param("userId") Long userId,
                                                     @Param("offset") Long offset,
                                                     @Param("pageSize") Long pageSize);

    @Select("""
            SELECT COUNT(1)
            FROM product_favorites f
            JOIN products p ON p.id = f.product_id
            WHERE f.user_id = #{userId}
              AND (p.is_deleted = 0 OR p.is_deleted IS NULL)
            """)
    Long countFavoriteProductsByUser(@Param("userId") Long userId);
}
