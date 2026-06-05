package com.swapcampus.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swapcampus.common.enums.OrderStatus;
import com.swapcampus.order.entity.OrderEntity;
import com.swapcampus.order.mapper.OrderMapper;
import com.swapcampus.payment.adapter.MockPaymentAdapter;
import com.swapcampus.payment.adapter.PaymentCreateCommand;
import com.swapcampus.payment.adapter.PaymentCreateResult;
import com.swapcampus.payment.dto.PaymentRequest;
import com.swapcampus.payment.entity.PaymentEntity;
import com.swapcampus.payment.enums.PaymentStatus;
import com.swapcampus.payment.mapper.PaymentMapper;
import com.swapcampus.payment.service.PaymentService;
import com.swapcampus.payment.vo.PaymentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final MockPaymentAdapter paymentAdapter;

    public PaymentServiceImpl(PaymentMapper paymentMapper,
                              OrderMapper orderMapper,
                              MockPaymentAdapter paymentAdapter) {
        this.paymentMapper = paymentMapper;
        this.orderMapper = orderMapper;
        this.paymentAdapter = paymentAdapter;
    }

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        OrderEntity order = orderMapper.selectById(request.getOrderId());
        if (order == null) throw new RuntimeException("订单不存在");
        if (!OrderStatus.CREATED.name().equals(order.getStatus())) {
            throw new RuntimeException("订单状态不允许支付");
        }

        String paymentNo = "PAY" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        PaymentCreateResult result = paymentAdapter.createPayment(
                new PaymentCreateCommand(order.getId(), order.getOrderNo(),
                        order.getAmount(), "SwapCampus订单" + order.getOrderNo()));

        PaymentEntity payment = new PaymentEntity();
        payment.setPaymentNo(paymentNo);
        payment.setOrderId(order.getId());
        payment.setProvider(result.provider());
        payment.setProviderTradeNo(result.providerTradeNo());
        payment.setAmount(result.amount());
        payment.setStatus(PaymentStatus.CREATED.name());
        payment.setPayUrl(result.payUrl());
        payment.setCreatedAt(LocalDateTime.now());
        paymentMapper.insert(payment);

        return PaymentResponse.from(payment);
    }

    @Override
    public PaymentResponse queryPayment(Long paymentId) {
        PaymentEntity payment = paymentMapper.selectById(paymentId);
        if (payment == null) throw new RuntimeException("支付记录不存在");
        return PaymentResponse.from(payment);
    }

    @Override
    @Transactional
    public PaymentResponse mockCallback(String providerTradeNo) {
        LambdaQueryWrapper<PaymentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentEntity::getProviderTradeNo, providerTradeNo);
        PaymentEntity payment = paymentMapper.selectOne(wrapper);
        if (payment == null) throw new RuntimeException("支付记录不存在");

        payment.setStatus(PaymentStatus.SUCCESS.name());
        payment.setPaidAt(LocalDateTime.now());
        paymentMapper.updateById(payment);

        OrderEntity order = orderMapper.selectById(payment.getOrderId());
        if (order != null) {
            order.setStatus(OrderStatus.PAID.name());
            order.setUpdatedAt(LocalDateTime.now());
            orderMapper.updateById(order);
        }

        return PaymentResponse.from(payment);
    }

    @Override
    @Transactional
    public PaymentResponse refund(Long orderId) {
        LambdaQueryWrapper<PaymentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentEntity::getOrderId, orderId)
                .eq(PaymentEntity::getStatus, PaymentStatus.SUCCESS.name());
        PaymentEntity payment = paymentMapper.selectOne(wrapper);
        if (payment == null) throw new RuntimeException("未找到可退款的支付记录");

        payment.setStatus(PaymentStatus.REFUNDED.name());
        paymentMapper.updateById(payment);

        OrderEntity order = orderMapper.selectById(orderId);
        if (order != null) {
            order.setStatus(OrderStatus.REFUNDED.name());
            order.setUpdatedAt(LocalDateTime.now());
            orderMapper.updateById(order);
        }

        return PaymentResponse.from(payment);
    }

    @Override
    public PaymentResponse getByOrderId(Long orderId) {
        LambdaQueryWrapper<PaymentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentEntity::getOrderId, orderId)
                .orderByDesc(PaymentEntity::getCreatedAt);
        List<PaymentEntity> list = paymentMapper.selectList(wrapper);
        if (list.isEmpty()) return null;
        return PaymentResponse.from(list.get(0));
    }
}