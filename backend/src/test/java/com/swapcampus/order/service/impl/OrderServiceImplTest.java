package com.swapcampus.order.service.impl;

import com.swapcampus.common.enums.OrderStatus;
import com.swapcampus.common.enums.ProductStatus;
import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.order.dto.OrderRequest;
import com.swapcampus.order.entity.OrderEntity;
import com.swapcampus.order.mapper.OrderMapper;
import com.swapcampus.payment.entity.PaymentEntity;
import com.swapcampus.payment.enums.PaymentStatus;
import com.swapcampus.payment.mapper.PaymentMapper;
import com.swapcampus.product.entity.ProductEntity;
import com.swapcampus.product.mapper.ProductImageMapper;
import com.swapcampus.product.mapper.ProductMapper;
import com.swapcampus.user.service.UserAccountService;
import com.swapcampus.user.service.UserVerificationGuard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderMapper orderMapper;
    @Mock private ProductMapper productMapper;
    @Mock private ProductImageMapper productImageMapper;
    @Mock private PaymentMapper paymentMapper;
    @Mock private UserVerificationGuard userVerificationGuard;
    @Mock private UserAccountService userAccountService;

    private OrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OrderServiceImpl(orderMapper, productMapper, productImageMapper, paymentMapper, userVerificationGuard, userAccountService);
    }

    @Test
    void createOrderLocksActiveProduct() {
        ProductEntity product = product(ProductStatus.ACTIVE);
        when(productMapper.selectById(10L)).thenReturn(product);
        OrderRequest request = new OrderRequest();
        request.setProductId(10L);
        request.setTradeMode("LOCKER");

        assertEquals(OrderStatus.CREATED.name(), service.createOrder(request, 7L).getStatus());

        assertEquals(ProductStatus.LOCKED.name(), product.getStatus());
        verify(orderMapper).insert(any(OrderEntity.class));
        verify(productMapper).updateById(product);
    }

    @Test
    void createOrderRejectsOwnProductAndUnsupportedTradeMode() {
        ProductEntity product = product(ProductStatus.ACTIVE);
        when(productMapper.selectById(10L)).thenReturn(product);
        OrderRequest request = new OrderRequest();
        request.setProductId(10L);
        request.setTradeMode("MEETUP");
        assertThrows(BusinessException.class, () -> service.createOrder(request, 9L));

        request.setTradeMode("MAIL");
        assertThrows(BusinessException.class, () -> service.createOrder(request, 7L));
    }

    @Test
    void getAndListOrdersRequireParticipantAndRole() {
        OrderEntity order = order(OrderStatus.CREATED);
        when(orderMapper.selectById(1L)).thenReturn(order);
        assertEquals(1L, service.getOrder(1L, 7L).getId());
        assertThrows(BusinessException.class, () -> service.getOrder(1L, 99L));

        when(orderMapper.selectList(any())).thenReturn(List.of(order));
        assertEquals(1, service.listMyOrders(7L, "buyer").size());
        assertEquals(1, service.listMyOrders(9L, "seller").size());
        assertThrows(BusinessException.class, () -> service.listMyOrders(7L, "admin"));
    }

    @Test
    void cancelCreatedOrderRestoresLockedProduct() {
        OrderEntity order = order(OrderStatus.CREATED);
        ProductEntity product = product(ProductStatus.LOCKED);
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(productMapper.selectById(10L)).thenReturn(product);

        assertEquals(OrderStatus.CANCELLED.name(), service.cancelOrder(1L, 7L).getStatus());
        assertEquals(ProductStatus.ACTIVE.name(), product.getStatus());
        verify(orderMapper).updateById(order);
    }

    @Test
    void sellerConfirmAndRejectRequirePaidOrder() {
        OrderEntity paid = order(OrderStatus.PAID);
        when(orderMapper.selectById(1L)).thenReturn(paid);
        assertEquals(OrderStatus.SELLER_CONFIRMED.name(), service.sellerConfirm(1L, 9L).getStatus());

        paid.setStatus(OrderStatus.PAID.name());
        PaymentEntity payment = new PaymentEntity();
        payment.setStatus(PaymentStatus.SUCCESS.name());
        when(paymentMapper.selectList(any())).thenReturn(List.of(payment));
        when(productMapper.selectById(10L)).thenReturn(product(ProductStatus.LOCKED));
        assertEquals(OrderStatus.SELLER_REJECTED.name(), service.sellerReject(1L, 9L).getStatus());
        assertEquals(PaymentStatus.REFUNDED.name(), payment.getStatus());
        verify(userAccountService).addCredit(9L, -3, "卖家拒单/爽约", "ORDER", 1L);
    }

    @Test
    void confirmCompleteMarksProductSoldAndAddsSellerCredit() {
        OrderEntity order = order(OrderStatus.DELIVERY_PENDING);
        ProductEntity product = product(ProductStatus.LOCKED);
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(productMapper.selectById(10L)).thenReturn(product);

        assertEquals(OrderStatus.COMPLETED.name(), service.confirmComplete(1L, 7L).getStatus());

        assertNotNull(order.getCompletedAt());
        assertEquals(ProductStatus.SOLD.name(), product.getStatus());
        verify(userAccountService).addCredit(9L, 2, "完成交易", "ORDER", 1L);
    }

    private ProductEntity product(ProductStatus status) {
        ProductEntity product = new ProductEntity();
        product.setId(10L);
        product.setSellerId(9L);
        product.setPrice(new BigDecimal("99.00"));
        product.setStatus(status.name());
        product.setTradeModes("MEETUP,LOCKER");
        return product;
    }

    private OrderEntity order(OrderStatus status) {
        OrderEntity order = new OrderEntity();
        order.setId(1L);
        order.setOrderNo("ORD1");
        order.setProductId(10L);
        order.setBuyerId(7L);
        order.setSellerId(9L);
        order.setAmount(new BigDecimal("99.00"));
        order.setStatus(status.name());
        order.setTradeMode("MEETUP");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }
}
