package com.swapcampus.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swapcampus.payment.entity.PaymentEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentMapper extends BaseMapper<PaymentEntity> {
}
