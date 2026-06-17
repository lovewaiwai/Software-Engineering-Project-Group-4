package com.swapcampus.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swapcampus.product.entity.ProductTagEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductTagMapper extends BaseMapper<ProductTagEntity> {

    @Delete("""
            DELETE FROM product_tags
            WHERE product_id = #{productId}
            """)
    int deleteByProductId(@Param("productId") Long productId);

    @Insert("""
            INSERT INTO product_tags (product_id, tag_id)
            VALUES (#{productId}, #{tagId})
            """)
    int insertTag(@Param("productId") Long productId, @Param("tagId") Long tagId);
}
