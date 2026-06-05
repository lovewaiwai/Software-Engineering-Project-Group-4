package com.swapcampus.delivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swapcampus.common.enums.OrderStatus;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryMapper deliveryMapper;
    private final OrderMapper orderMapper;
    private final MockLockerAdapter lockerAdapter;

    public DeliveryServiceImpl(DeliveryMapper deliveryMapper,
                               OrderMapper orderMapper,
                               MockLockerAdapter lockerAdapter) {
        this.deliveryMapper = deliveryMapper;
        this.orderMapper = orderMapper;
        this.lockerAdapter = lockerAdapter;
    }

    @Override
    @Transactional
    public DeliveryResponse reserveLocker(DeliveryRequest request) {
        OrderEntity order = orderMapper.selectById(request.getOrderId());
        if (order == null) throw new RuntimeException("订单不存在");
        if (!OrderStatus.PAID.name().equals(order.getStatus())) {
            throw new RuntimeException("订单未支付，不能预约柜机");
        }

        LockerReserveResult result = lockerAdapter.reserveBox(
                new LockerReserveCommand(order.getId(), request.getStationId(), request.getSize()));

        DeliveryEntity task = new DeliveryEntity();
        task.setTaskNo(result.lockerTaskNo());
        task.setOrderId(order.getId());
        task.setStationId(request.getStationId());
        task.setBoxId(0L); // Mock 场景下无真实 boxId
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
        DeliveryEntity task = getTaskByNo(taskNo);
        if (!DeliveryStatus.RESERVED.name().equals(task.getStatus())) {
            throw new RuntimeException("当前状态不能确认存入");
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
        DeliveryEntity task = getTaskByNo(taskNo);
        if (!DeliveryStatus.STORED.name().equals(task.getStatus())) {
            throw new RuntimeException("货物尚未存入柜机");
        }
        if (!task.getPickupCode().equals(pickupCode)) {
            throw new RuntimeException("取件码错误");
        }

        lockerAdapter.confirmPickedUp(taskNo, pickupCode);
        task.setStatus(DeliveryStatus.PICKED_UP.name());
        task.setPickedUpAt(LocalDateTime.now());
        deliveryMapper.updateById(task);

        OrderEntity order = orderMapper.selectById(task.getOrderId());
        if (order != null) {
            order.setStatus(OrderStatus.COMPLETED.name());
            order.setUpdatedAt(LocalDateTime.now());
            order.setCompletedAt(LocalDateTime.now());
            orderMapper.updateById(order);
        }

        return DeliveryResponse.from(task);
    }

    @Override
    public DeliveryResponse getByOrderId(Long orderId) {
        LambdaQueryWrapper<DeliveryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeliveryEntity::getOrderId, orderId);
        DeliveryEntity task = deliveryMapper.selectOne(wrapper);
        if (task == null) throw new RuntimeException("未找到柜机任务");
        return DeliveryResponse.from(task);
    }

    private DeliveryEntity getTaskByNo(String taskNo) {
        LambdaQueryWrapper<DeliveryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeliveryEntity::getTaskNo, taskNo);
        DeliveryEntity task = deliveryMapper.selectOne(wrapper);
        if (task == null) throw new RuntimeException("柜机任务不存在");
        return task;
    }
}