package com.swapcampus.delivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swapcampus.common.enums.OrderStatus;
import com.swapcampus.common.enums.ProductStatus;
import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.exception.ErrorCode;
import com.swapcampus.common.security.CurrentUserContext;
import com.swapcampus.delivery.adapter.LockerReserveCommand;
import com.swapcampus.delivery.adapter.LockerReserveResult;
import com.swapcampus.delivery.adapter.MockLockerAdapter;
import com.swapcampus.delivery.dto.DeliveryRequest;
import com.swapcampus.delivery.entity.DeliveryEntity;
import com.swapcampus.delivery.enums.DeliveryStatus;
import com.swapcampus.delivery.mapper.DeliveryMapper;
import com.swapcampus.delivery.service.DeliveryService;
import com.swapcampus.delivery.vo.DeliveryResponse;
import com.swapcampus.order.entity.OrderEntity;
import com.swapcampus.order.mapper.OrderMapper;
import com.swapcampus.product.entity.ProductEntity;
import com.swapcampus.product.mapper.ProductMapper;
import com.swapcampus.user.service.UserVerificationGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryMapper deliveryMapper;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final MockLockerAdapter lockerAdapter;
    private final UserVerificationGuard userVerificationGuard;

    public DeliveryServiceImpl(DeliveryMapper deliveryMapper,
                               OrderMapper orderMapper,
                               ProductMapper productMapper,
                               MockLockerAdapter lockerAdapter,
                               UserVerificationGuard userVerificationGuard) {
        this.deliveryMapper = deliveryMapper;
        this.orderMapper = orderMapper;
        this.productMapper = productMapper;
        this.lockerAdapter = lockerAdapter;
        this.userVerificationGuard = userVerificationGuard;
    }

    @Override
    @Transactional
    public DeliveryResponse reserveLocker(DeliveryRequest request) {
        Long currentUserId = CurrentUserContext.requireUserId();
        userVerificationGuard.requireVerifiedStudent(currentUserId);

        OrderEntity order = requireOrder(request.getOrderId());
        requireSeller(order, currentUserId);
        if (!OrderStatus.SELLER_CONFIRMED.name().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "卖家确认订单后才能预约柜机");
        }

        LockerReserveResult result = lockerAdapter.reserveBox(
                new LockerReserveCommand(order.getId(), request.getStationId(), request.getSize()));

        DeliveryEntity task = new DeliveryEntity();
        task.setTaskNo(result.lockerTaskNo());
        task.setOrderId(order.getId());
        task.setStationId(request.getStationId());
        task.setBoxId(0L);
        task.setPickupCode(result.pickupCode());
        task.setStatus(DeliveryStatus.RESERVED.name());
        task.setCreatedAt(LocalDateTime.now());
        deliveryMapper.insert(task);

        order.setStatus(OrderStatus.DELIVERY_PENDING.name());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        return DeliveryResponse.from(task);
    }

    @Override
    @Transactional
    public DeliveryResponse confirmStored(String taskNo) {
        Long currentUserId = CurrentUserContext.requireUserId();
        userVerificationGuard.requireVerifiedStudent(currentUserId);

        DeliveryEntity task = getTaskByNo(taskNo);
        OrderEntity order = requireOrder(task.getOrderId());
        requireSeller(order, currentUserId);
        if (!DeliveryStatus.RESERVED.name().equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前状态不能确认存入");
        }

        lockerAdapter.confirmStored(taskNo);
        task.setStatus(DeliveryStatus.STORED.name());
        task.setStoredAt(LocalDateTime.now());
        deliveryMapper.updateById(task);

        return DeliveryResponse.from(task);
    }

    @Override
    @Transactional
    public DeliveryResponse confirmPickedUp(String taskNo, String pickupCode) {
        Long currentUserId = CurrentUserContext.requireUserId();
        userVerificationGuard.requireVerifiedStudent(currentUserId);

        DeliveryEntity task = getTaskByNo(taskNo);
        OrderEntity order = requireOrder(task.getOrderId());
        requireBuyer(order, currentUserId);
        if (!DeliveryStatus.STORED.name().equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "货物尚未存入柜机");
        }
        if (!task.getPickupCode().equals(pickupCode)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "取件码错误");
        }

        lockerAdapter.confirmPickedUp(taskNo, pickupCode);
        task.setStatus(DeliveryStatus.PICKED_UP.name());
        task.setPickedUpAt(LocalDateTime.now());
        deliveryMapper.updateById(task);

        order.setStatus(OrderStatus.COMPLETED.name());
        order.setUpdatedAt(LocalDateTime.now());
        order.setCompletedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        markProductSold(order.getProductId());

        return DeliveryResponse.from(task);
    }

    @Override
    public DeliveryResponse getByOrderId(Long orderId) {
        Long currentUserId = CurrentUserContext.requireUserId();
        OrderEntity order = requireOrder(orderId);
        requireParticipant(order, currentUserId);

        LambdaQueryWrapper<DeliveryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeliveryEntity::getOrderId, orderId);
        DeliveryEntity task = deliveryMapper.selectOne(wrapper);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到柜机任务");
        }
        return DeliveryResponse.from(task);
    }

    private DeliveryEntity getTaskByNo(String taskNo) {
        LambdaQueryWrapper<DeliveryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeliveryEntity::getTaskNo, taskNo);
        DeliveryEntity task = deliveryMapper.selectOne(wrapper);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "柜机任务不存在");
        }
        return task;
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
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该柜机任务");
        }
    }

    private void requireSeller(OrderEntity order, Long currentUserId) {
        if (!Objects.equals(order.getSellerId(), currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该柜机任务");
        }
    }

    private void requireParticipant(OrderEntity order, Long currentUserId) {
        if (!Objects.equals(order.getBuyerId(), currentUserId) && !Objects.equals(order.getSellerId(), currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该柜机任务");
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
