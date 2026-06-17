package com.swapcampus.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swapcampus.product.entity.BrowseRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BrowseRecordMapper extends BaseMapper<BrowseRecordEntity> {

    @Select("""
            SELECT id, user_id, product_id, category_id, created_at
            FROM (
              SELECT br.id,
                     br.user_id,
                     br.product_id,
                     br.category_id,
                     br.created_at,
                     ROW_NUMBER() OVER (PARTITION BY br.product_id ORDER BY br.created_at DESC, br.id DESC) AS rn
              FROM browse_records br
              INNER JOIN products p ON p.id = br.product_id
              WHERE br.user_id = #{userId}
                AND p.is_deleted = 0
            ) latest
            WHERE latest.rn = 1
            ORDER BY created_at DESC, id DESC
            OFFSET #{offset} ROWS FETCH NEXT #{limit} ROWS ONLY
            """)
    List<BrowseRecordEntity> selectLatestByUser(@Param("userId") Long userId,
                                                @Param("offset") long offset,
                                                @Param("limit") long limit);

    @Select("""
            SELECT COUNT(DISTINCT br.product_id)
            FROM browse_records br
            INNER JOIN products p ON p.id = br.product_id
            WHERE br.user_id = #{userId}
              AND p.is_deleted = 0
            """)
    Long countDistinctProductsByUser(@Param("userId") Long userId);
}
