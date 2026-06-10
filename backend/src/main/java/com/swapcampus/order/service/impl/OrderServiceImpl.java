package com.swapcampus.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swapcampus.common.enums.OrderStatus;
import com.swapcampus.common.enums.ProductStatus;
import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.exception.ErrorCode;
import com.swapcampus.order.dto.OrderRequest;
import com.swapcampus.order.entity.OrderEntity;
import com.swapcampus.order.mapper.OrderMapper;
import com.swapcampus.order.service.OrderService;
import com.swapcampus.order.vo.OrderResponse;
import com.swapcampus.payment.entity.PaymentEntity;
import com.swapcampus.payment.enums.PaymentStatus;
import com.swapcampus.payment.mapper.PaymentMapper;
import com.swapcampus.product.entity.ProductEntity;
import com.swapcampus.product.mapper.ProductMapper;
import com.swapcampus.user.service.UserVerificationGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final PaymentMapper paymentMapper;
    private final UserVerificationGuard userVerificationGuard;
    private final com.swapcampus.user.service.UserService userService;

    public OrderServiceImpl(OrderMapper orderMapper,
                            ProductMapper productMapper,
                            PaymentMapper paymentMapper,
                            UserVerificationGuard userVerificationGuard,
                            com.swapcampus.user.service.UserService userService) {
        this.orderMapper = orderMapper;
        this.productMapper = productMapper;
        this.paymentMapper = paymentMapper;
        this.userVerificationGuard = userVerificationGuard;
        this.userService = userService;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request, Long buyerId) {
        userVerificationGuard.requireVerifiedStudent(buyerId);
        ProductEntity product = requireProduct(request.getProductId());
        if (!ProductStatus.ACTIVE.name().equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "只有已上架商品可以购买");
        }
        if (Objects.equals(product.getSellerId(), buyerId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能购买自己发布的商品");
        }

        String tradeMode = normalizeTradeMode(request.getTradeMode(), product);
        String orderNo = "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        OrderEntity order = new OrderEntity();
        order.setOrderNo(orderNo);
        order.setProductId(request.getProductId());
        order.setBuyerId(buyerId);
        order.setSellerId(product.getSellerId());
        order.setAmount(product.getPrice());
        order.setStatus(OrderStatus.CREATED.name());
        order.setTradeMode(tradeMode);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        orderMapper.insert(order);

        product.setStatus(ProductStatus.LOCKED.name());
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);

        return OrderResponse.from(order);
    }

    @Override
    public OrderResponse getOrder(Long orderId, Long currentUserId) {
        OrderEntity order = requireOrder(orderId);
        requireParticipant(order, currentUserId);
        return OrderResponse.from(order);
    }

    @Override
    public List<OrderResponse> listMyOrders(Long currentUserId, String role) {
        LambdaQueryWrapper<OrderEntity> wrapper = new LambdaQueryWrapper<>();
        if ("buyer".equals(role)) {
            wrapper.eq(OrderEntity::getBuyerId, currentUserId);
        } else if ("seller".equals(role)) {
            wrapper.eq(OrderEntity::getSellerId, currentUserId);
        } else {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单角色只能是 buyer 或 seller");
        }
        wrapper.orderByDesc(OrderEntity::getCreatedAt);
        return orderMapper.selectList(wrapper)
                .stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long currentUserId) {
        userVerificationGuard.requireVerifiedStudent(currentUserId);
        OrderEntity order = requireOrder(orderId);
        requireBuyer(order, currentUserId);
        // 只允许未支付时取消
        if (!OrderStatus.CREATED.name().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "已支付订单不可直接取消，请申请退款");
        }
        order.setStatus(OrderStatus.CANCELLED.name());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        restoreProductIfLocked(order.getProductId());
        return OrderResponse.from(order);
    }

    @Override
    @Transactional
    public OrderResponse sellerConfirm(Long orderId, Long sellerId) {
        userVerificationGuard.requireVerifiedStudent(sellerId);
        OrderEntity order = requireOrder(orderId);
        requireSeller(order, sellerId);
        // 买家已支付后卖家才能确认
        if (!OrderStatus.PAID.name().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "买家支付后才能确认订单");
        }
        order.setStatus(OrderStatus.SELLER_CONFIRMED.name());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        return OrderResponse.from(order);
    }

    @Override
    @Transactional
    public OrderResponse sellerReject(Long orderId, Long sellerId) {
        userVerificationGuard.requireVerifiedStudent(sellerId);
        OrderEntity order = requireOrder(orderId);
        requireSeller(order, sellerId);
        if (!OrderStatus.PAID.name().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "买家支付后才能拒绝订单");
        }

        // 自动退款
        LambdaQueryWrapper<PaymentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentEntity::getOrderId, orderId)
                .eq(PaymentEntity::getStatus, PaymentStatus.SUCCESS.name());
        List<PaymentEntity> payments = paymentMapper.selectList(wrapper);
        for (PaymentEntity payment : payments) {
            payment.setStatus(PaymentStatus.REFUNDED.name());
            paymentMapper.updateById(payment);
        }

        // 订单状态改为卖家拒绝
        order.setStatus(OrderStatus.SELLER_REJECTED.name());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        restoreProductIfLocked(order.getProductId());
        return OrderResponse.from(order);
    }

    @Override
    @Transactional
    public OrderResponse confirmComplete(Long orderId, Long currentUserId) {
        userVerificationGuard.requireVerifiedStudent(currentUserId);
        OrderEntity order = requireOrder(orderId);
        requireBuyer(order, currentUserId);
        List<String> completableStatuses = List.of(
                OrderStatus.SELLER_CONFIRMED.name(),
                OrderStatus.DELIVERY_PENDING.name()
        );
        if (!completableStatuses.contains(order.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前状态不可确认完成");
        }
        order.setStatus(OrderStatus.COMPLETED.name());
        order.setUpdatedAt(LocalDateTime.now());
        order.setCompletedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        markProductSold(order.getProductId());

        // 订单完成，卖家信用分 +1
        userService.adjustCreditScore(
                order.getSellerId(), 1, "完成交易", "order", orderId
        );

        return OrderResponse.from(order);
    }

    // ---- 私有方法 ----

    private ProductEntity requireProduct(Long productId) {
        ProductEntity product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        return product;
    }

    private OrderEntity requireOrder(Long orderId) {
        OrderEntity order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        return order;
    }

    private void requireParticipant(OrderEntity order, Long currentUserId) {
        if (!Objects.equals(order.getBuyerId(), currentUserId)
                && !Objects.equals(order.getSellerId(), currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该订单");
        }
    }

    private void requireBuyer(OrderEntity order, Long currentUserId) {
        if (!Objects.equals(order.getBuyerId(), currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该订单");
        }
    }

    private void requireSeller(OrderEntity order, Long currentUserId) {
        if (!Objects.equals(order.getSellerId(), currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该订单");
        }
    }

    private String normalizeTradeMode(String requestedMode, ProductEntity product) {
        String tradeMode = StringUtils.hasText(requestedMode) ? requestedMode.trim() : "MEETUP";
        List<String> supported = Arrays.stream(
                        (product.getTradeModes() == null ? "" : product.getTradeModes()).split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
        if (!supported.isEmpty() && !supported.contains(tradeMode)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "商品不支持该交易方式");
        }
        return tradeMode;
    }

    private void restoreProductIfLocked(Long productId) {
        ProductEntity product = productMapper.selectById(productId);
        if (product != null && ProductStatus.LOCKED.name().equals(product.getStatus())) {
            product.setStatus(ProductStatus.ACTIVE.name());
            product.setUpdatedAt(LocalDateTime.now());
            productMapper.updateById(product);
        }
    }

    private void markProductSold(Long productId) {
        ProductEntity product = productMapper.selectById(productId);
        if (product != null) {
            product.setStatus(ProductStatus.SOLD.name());
            product.setUpdatedAt(LocalDateTime.now());
            productMapper.updateById(product);
        }
    }
}