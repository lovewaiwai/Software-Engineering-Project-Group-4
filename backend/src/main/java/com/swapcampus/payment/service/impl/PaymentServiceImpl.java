package com.swapcampus.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swapcampus.common.enums.OrderStatus;
import com.swapcampus.common.enums.ProductStatus;
import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.exception.ErrorCode;
import com.swapcampus.common.security.CurrentUserContext;
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
import com.swapcampus.product.entity.ProductEntity;
import com.swapcampus.product.mapper.ProductMapper;
import com.swapcampus.user.service.UserVerificationGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final MockPaymentAdapter paymentAdapter;
    private final UserVerificationGuard userVerificationGuard;

    public PaymentServiceImpl(PaymentMapper paymentMapper,
                              OrderMapper orderMapper,
                              ProductMapper productMapper,
                              MockPaymentAdapter paymentAdapter,
                              UserVerificationGuard userVerificationGuard) {
        this.paymentMapper = paymentMapper;
        this.orderMapper = orderMapper;
        this.productMapper = productMapper;
        this.paymentAdapter = paymentAdapter;
        this.userVerificationGuard = userVerificationGuard;
    }

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        Long currentUserId = CurrentUserContext.requireUserId();
        userVerificationGuard.requireVerifiedStudent(currentUserId);

        OrderEntity order = requireOrder(request.getOrderId());
        requireBuyer(order, currentUserId);
        if (!OrderStatus.CREATED.name().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单状态不允许支付");
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
        Long currentUserId = CurrentUserContext.requireUserId();
        PaymentEntity payment = requirePayment(paymentId);
        OrderEntity order = requireOrder(payment.getOrderId());
        requireParticipant(order, currentUserId);
        return PaymentResponse.from(payment);
    }

    @Override
    @Transactional
    public PaymentResponse mockCallback(String providerTradeNo) {
        Long currentUserId = CurrentUserContext.requireUserId();
        LambdaQueryWrapper<PaymentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentEntity::getProviderTradeNo, providerTradeNo);
        PaymentEntity payment = paymentMapper.selectOne(wrapper);
        if (payment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "支付记录不存在");
        }

        OrderEntity order = requireOrder(payment.getOrderId());
        requireBuyer(order, currentUserId);
        userVerificationGuard.requireVerifiedStudent(currentUserId);

        payment.setStatus(PaymentStatus.SUCCESS.name());
        payment.setPaidAt(LocalDateTime.now());
        paymentMapper.updateById(payment);

        order.setStatus(OrderStatus.PAID.name());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        return PaymentResponse.from(payment);
    }

    @Override
    @Transactional
    public PaymentResponse refund(Long orderId) {
        Long currentUserId = CurrentUserContext.requireUserId();
        userVerificationGuard.requireVerifiedStudent(currentUserId);

        OrderEntity order = requireOrder(orderId);
        requireBuyer(order, currentUserId);

        LambdaQueryWrapper<PaymentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentEntity::getOrderId, orderId)
                .eq(PaymentEntity::getStatus, PaymentStatus.SUCCESS.name());
        PaymentEntity payment = paymentMapper.selectOne(wrapper);
        if (payment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到可退款的支付记录");
        }

        payment.setStatus(PaymentStatus.REFUNDED.name());
        paymentMapper.updateById(payment);

        order.setStatus(OrderStatus.REFUNDED.name());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        restoreProductIfLocked(order.getProductId());

        return PaymentResponse.from(payment);
    }

    @Override
    public PaymentResponse getByOrderId(Long orderId) {
        Long currentUserId = CurrentUserContext.requireUserId();
        OrderEntity order = requireOrder(orderId);
        requireParticipant(order, currentUserId);

        LambdaQueryWrapper<PaymentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentEntity::getOrderId, orderId)
                .orderByDesc(PaymentEntity::getCreatedAt);
        List<PaymentEntity> list = paymentMapper.selectList(wrapper);
        if (list.isEmpty()) {
            return null;
        }
        return PaymentResponse.from(list.get(0));
    }

    private PaymentEntity requirePayment(Long paymentId) {
        PaymentEntity payment = paymentMapper.selectById(paymentId);
        if (payment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "支付记录不存在");
        }
        return payment;
    }

    private OrderEntity requireOrder(Long orderId) {
        OrderEntity order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        return order;
    }

    private void requireBuyer(OrderEntity order, Long currentUserId) {
        if (!Objects.equals(order.getBuyerId(), currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该订单支付");
        }
    }

    private void requireParticipant(OrderEntity order, Long currentUserId) {
        if (!Objects.equals(order.getBuyerId(), currentUserId) && !Objects.equals(order.getSellerId(), currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该订单支付");
        }
    }

    private void restoreProductIfLocked(Long productId) {
        ProductEntity product = productMapper.selectById(productId);
        if (product != null && ProductStatus.LOCKED.name().equals(product.getStatus())) {
            product.setStatus(ProductStatus.ACTIVE.name());
            product.setUpdatedAt(LocalDateTime.now());
            productMapper.updateById(product);
        }
    }
}
