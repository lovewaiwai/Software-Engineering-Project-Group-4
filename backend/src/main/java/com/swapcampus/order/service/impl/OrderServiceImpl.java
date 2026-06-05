package com.swapcampus.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swapcampus.common.enums.OrderStatus;
import com.swapcampus.order.dto.OrderRequest;
import com.swapcampus.order.entity.OrderEntity;
import com.swapcampus.order.mapper.OrderMapper;
import com.swapcampus.order.service.OrderService;
import com.swapcampus.order.vo.OrderResponse;
import com.swapcampus.product.entity.ProductEntity;
import com.swapcampus.product.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    public OrderServiceImpl(OrderMapper orderMapper, ProductMapper productMapper) {
        this.orderMapper = orderMapper;
        this.productMapper = productMapper;
    }

    @Override
    public OrderResponse createOrder(OrderRequest request, Long buyerId) {
        ProductEntity product = productMapper.selectById(request.getProductId());
        if (product == null) throw new RuntimeException("商品不存在");
        if (product.getSellerId().equals(buyerId)) throw new RuntimeException("不能购买自己的商品");

        String orderNo = "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        OrderEntity order = new OrderEntity();
        order.setOrderNo(orderNo);
        order.setProductId(request.getProductId());
        order.setBuyerId(buyerId);
        order.setSellerId(product.getSellerId());
        order.setAmount(product.getPrice());
        order.setStatus(OrderStatus.CREATED.name());
        order.setTradeMode(request.getTradeMode());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        orderMapper.insert(order);
        return OrderResponse.from(order);
    }

    @Override
    public OrderResponse getOrder(Long orderId, Long currentUserId) {
        OrderEntity order = orderMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        return OrderResponse.from(order);
    }

    @Override
    public List<OrderResponse> listMyOrders(Long currentUserId, String role) {
        LambdaQueryWrapper<OrderEntity> wrapper = new LambdaQueryWrapper<>();
        if ("buyer".equals(role)) {
            wrapper.eq(OrderEntity::getBuyerId, currentUserId);
        } else {
            wrapper.eq(OrderEntity::getSellerId, currentUserId);
        }
        wrapper.orderByDesc(OrderEntity::getCreatedAt);
        return orderMapper.selectList(wrapper)
                .stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse cancelOrder(Long orderId, Long currentUserId) {
        OrderEntity order = orderMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        if (!order.getBuyerId().equals(currentUserId)) throw new RuntimeException("无权操作");
        if (!OrderStatus.CREATED.name().equals(order.getStatus())) {
            throw new RuntimeException("当前状态不可取消");
        }
        order.setStatus(OrderStatus.CANCELLED.name());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        return OrderResponse.from(order);
    }

    @Override
    public OrderResponse confirmComplete(Long orderId, Long currentUserId) {
        OrderEntity order = orderMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        if (!order.getBuyerId().equals(currentUserId)) throw new RuntimeException("无权操作");
        if (!OrderStatus.PAID.name().equals(order.getStatus())) {
            throw new RuntimeException("当前状态不可确认完成");
        }
        order.setStatus(OrderStatus.COMPLETED.name());
        order.setUpdatedAt(LocalDateTime.now());
        order.setCompletedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        return OrderResponse.from(order);
    }
}