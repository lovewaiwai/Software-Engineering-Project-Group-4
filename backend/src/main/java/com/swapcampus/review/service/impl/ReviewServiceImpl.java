package com.swapcampus.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swapcampus.common.enums.OrderStatus;
import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.exception.ErrorCode;
import com.swapcampus.order.entity.OrderEntity;
import com.swapcampus.order.mapper.OrderMapper;
import com.swapcampus.review.dto.ReviewRequest;
import com.swapcampus.review.entity.ReviewEntity;
import com.swapcampus.review.mapper.ReviewMapper;
import com.swapcampus.review.service.ReviewService;
import com.swapcampus.review.vo.ReviewResponse;
import com.swapcampus.user.service.UserService;
import com.swapcampus.user.service.UserVerificationGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final OrderMapper orderMapper;
    private final UserVerificationGuard userVerificationGuard;
    private final UserService userService;

    public ReviewServiceImpl(ReviewMapper reviewMapper,
                             OrderMapper orderMapper,
                             UserVerificationGuard userVerificationGuard,
                             UserService userService) {
        this.reviewMapper = reviewMapper;
        this.orderMapper = orderMapper;
        this.userVerificationGuard = userVerificationGuard;
        this.userService = userService;
    }

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewRequest request, Long reviewerId) {
        userVerificationGuard.requireVerifiedStudent(reviewerId);

        OrderEntity order = orderMapper.selectById(request.getOrderId());
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        if (!OrderStatus.COMPLETED.name().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单未完成，不能评价");
        }

        Long revieweeId;
        if (Objects.equals(order.getBuyerId(), reviewerId)) {
            revieweeId = order.getSellerId();
        } else if (Objects.equals(order.getSellerId(), reviewerId)) {
            revieweeId = order.getBuyerId();
        } else {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权评价此订单");
        }

        Long count = reviewMapper.selectCount(new LambdaQueryWrapper<ReviewEntity>()
                .eq(ReviewEntity::getOrderId, request.getOrderId())
                .eq(ReviewEntity::getReviewerId, reviewerId));
        if (count > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "已评价过此订单");
        }

        ReviewEntity review = new ReviewEntity();
        review.setOrderId(request.getOrderId());
        review.setReviewerId(reviewerId);
        review.setRevieweeId(revieweeId);
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setCreatedAt(LocalDateTime.now());
        reviewMapper.insert(review);

        // 根据评分调整被评价方信用分
        // 4-5星：好评 +1分；1-2星：差评 -2分；3星：中评不变
        int delta = 0;
        if (request.getRating() >= 4) {
            delta = 1;
        } else if (request.getRating() <= 2) {
            delta = -2;
        }
        if (delta != 0) {
            userService.adjustCreditScore(
                    revieweeId,
                    delta,
                    delta > 0 ? "获得好评" : "获得差评",
                    "review",
                    review.getId()
            );
        }

        return ReviewResponse.from(review);
    }

    @Override
    public List<ReviewResponse> getReviewsByUser(Long userId) {
        LambdaQueryWrapper<ReviewEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReviewEntity::getRevieweeId, userId)
                .orderByDesc(ReviewEntity::getCreatedAt);
        return reviewMapper.selectList(wrapper)
                .stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponse> getReviewsByOrder(Long orderId) {
        LambdaQueryWrapper<ReviewEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReviewEntity::getOrderId, orderId);
        return reviewMapper.selectList(wrapper)
                .stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());
    }
}