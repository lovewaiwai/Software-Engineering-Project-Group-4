package com.swapcampus.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swapcampus.order.entity.OrderEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {
}