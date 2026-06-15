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
import com.swapcampus.delivery.entity.LockerBoxEntity;
import com.swapcampus.delivery.entity.LockerStationEntity;
import com.swapcampus.delivery.enums.DeliveryStatus;
import com.swapcampus.delivery.mapper.DeliveryMapper;
import com.swapcampus.delivery.mapper.LockerBoxMapper;
import com.swapcampus.delivery.mapper.LockerStationMapper;
import com.swapcampus.delivery.service.DeliveryService;
import com.swapcampus.delivery.vo.DeliveryResponse;
import com.swapcampus.delivery.vo.LockerStationResponse;
import com.swapcampus.delivery.vo.LockerTaskResponse;
import com.swapcampus.order.entity.OrderEntity;
import com.swapcampus.order.mapper.OrderMapper;
import com.swapcampus.product.entity.ProductEntity;
import com.swapcampus.product.mapper.ProductMapper;
import com.swapcampus.user.service.UserVerificationGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryMapper deliveryMapper;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final LockerStationMapper lockerStationMapper;
    private final LockerBoxMapper lockerBoxMapper;
    private final MockLockerAdapter lockerAdapter;
    private final UserVerificationGuard userVerificationGuard;

    public DeliveryServiceImpl(DeliveryMapper deliveryMapper,
                               OrderMapper orderMapper,
                               ProductMapper productMapper,
                               LockerStationMapper lockerStationMapper,
                               LockerBoxMapper lockerBoxMapper,
                               MockLockerAdapter lockerAdapter,
                               UserVerificationGuard userVerificationGuard) {
        this.deliveryMapper = deliveryMapper;
        this.orderMapper = orderMapper;
        this.productMapper = productMapper;
        this.lockerStationMapper = lockerStationMapper;
        this.lockerBoxMapper = lockerBoxMapper;
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

        LockerBoxEntity box = reserveMockBox(request.getStationId(), request.getSize());
        LockerReserveResult result = lockerAdapter.reserveBox(
                new LockerReserveCommand(order.getId(), box.getStationId(), box.getSize()));

        DeliveryEntity task = new DeliveryEntity();
        task.setTaskNo(result.lockerTaskNo());
        task.setOrderId(order.getId());
        task.setStationId(box.getStationId());
        task.setBoxId(box.getId());
        task.setPickupCode(result.pickupCode());
        task.setStatus(DeliveryStatus.RESERVED.name());
        task.setCreatedAt(LocalDateTime.now());
        deliveryMapper.insert(task);

        order.setStatus(OrderStatus.DELIVERY_PENDING.name());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        return enrich(DeliveryResponse.from(task));
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
        LockerBoxEntity box = lockerBoxMapper.selectById(task.getBoxId());
        if (box != null) {
            box.setStatus("OCCUPIED");
            lockerBoxMapper.updateById(box);
        }
        task.setStatus(DeliveryStatus.STORED.name());
        task.setStoredAt(LocalDateTime.now());
        deliveryMapper.updateById(task);

        return enrich(DeliveryResponse.from(task));
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
        LockerBoxEntity box = lockerBoxMapper.selectById(task.getBoxId());
        if (box != null) {
            box.setStatus("EMPTY");
            lockerBoxMapper.updateById(box);
        }
        task.setStatus(DeliveryStatus.PICKED_UP.name());
        task.setPickedUpAt(LocalDateTime.now());
        deliveryMapper.updateById(task);

        order.setStatus(OrderStatus.COMPLETED.name());
        order.setUpdatedAt(LocalDateTime.now());
        order.setCompletedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        markProductSold(order.getProductId());

        return enrich(DeliveryResponse.from(task));
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
        return enrich(DeliveryResponse.from(task));
    }

    @Override
    public List<LockerStationResponse> listStations() {
        return lockerStationMapper.selectList(new LambdaQueryWrapper<LockerStationEntity>()
                        .orderByAsc(LockerStationEntity::getId))
                .stream()
                .map(station -> {
                    LockerStationResponse response = LockerStationResponse.from(station);
                    response.setEmptyBoxes(countBoxes(station.getId(), "EMPTY"));
                    response.setReservedBoxes(countBoxes(station.getId(), "RESERVED"));
                    response.setOccupiedBoxes(countBoxes(station.getId(), "OCCUPIED"));
                    return response;
                })
                .toList();
    }

    @Override
    public List<LockerTaskResponse> listTasks() {
        return deliveryMapper.selectList(new LambdaQueryWrapper<DeliveryEntity>()
                        .orderByDesc(DeliveryEntity::getCreatedAt)
                        .last("OFFSET 0 ROWS FETCH NEXT 100 ROWS ONLY"))
                .stream()
                .map(task -> {
                    LockerTaskResponse response = LockerTaskResponse.from(task);
                    LockerStationEntity station = lockerStationMapper.selectById(task.getStationId());
                    LockerBoxEntity box = lockerBoxMapper.selectById(task.getBoxId());
                    response.setStationName(station == null ? "Mock 柜机站点" : station.getName());
                    response.setBoxNo(box == null ? "-" : box.getBoxNo());
                    return response;
                })
                .toList();
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

    private LockerBoxEntity reserveMockBox(Long stationId, String requestedSize) {
        LockerStationEntity station = ensureMockStation(stationId);
        String size = requestedSize == null || requestedSize.isBlank() ? "M" : requestedSize.trim().toUpperCase();
        LockerBoxEntity box = lockerBoxMapper.selectOne(new LambdaQueryWrapper<LockerBoxEntity>()
                .eq(LockerBoxEntity::getStationId, station.getId())
                .eq(LockerBoxEntity::getSize, size)
                .eq(LockerBoxEntity::getStatus, "EMPTY")
                .orderByAsc(LockerBoxEntity::getId)
                .last("OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY"));
        if (box == null) {
            box = new LockerBoxEntity();
            box.setStationId(station.getId());
            box.setSize(size);
            box.setStatus("EMPTY");
            box.setBoxNo(size + "-" + System.currentTimeMillis() % 10000);
            lockerBoxMapper.insert(box);
        }
        box.setStatus("RESERVED");
        lockerBoxMapper.updateById(box);
        return box;
    }

    private LockerStationEntity ensureMockStation(Long stationId) {
        LockerStationEntity station = stationId == null ? null : lockerStationMapper.selectById(stationId);
        if (station != null && "ACTIVE".equals(station.getStatus())) {
            return station;
        }
        station = lockerStationMapper.selectOne(new LambdaQueryWrapper<LockerStationEntity>()
                .eq(LockerStationEntity::getStatus, "ACTIVE")
                .orderByAsc(LockerStationEntity::getId)
                .last("OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY"));
        if (station != null) {
            return station;
        }
        station = new LockerStationEntity();
        station.setName("SwapCampus Mock 柜机");
        station.setLocation("主校区服务中心");
        station.setStatus("ACTIVE");
        lockerStationMapper.insert(station);
        return station;
    }

    private long countBoxes(Long stationId, String status) {
        Long count = lockerBoxMapper.selectCount(new LambdaQueryWrapper<LockerBoxEntity>()
                .eq(LockerBoxEntity::getStationId, stationId)
                .eq(LockerBoxEntity::getStatus, status));
        return count == null ? 0 : count;
    }

    private DeliveryResponse enrich(DeliveryResponse response) {
        LockerStationEntity station = lockerStationMapper.selectById(response.getStationId());
        LockerBoxEntity box = lockerBoxMapper.selectById(response.getBoxId());
        response.setStationName(station == null ? "Mock 柜机站点" : station.getName());
        response.setBoxNo(box == null ? "-" : box.getBoxNo());
        return response;
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
