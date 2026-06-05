package com.swapcampus.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swapcampus.common.enums.OrderStatus;
import com.swapcampus.order.entity.OrderEntity;
import com.swapcampus.order.mapper.OrderMapper;
import com.swapcampus.review.dto.ReviewRequest;
import com.swapcampus.review.entity.ReviewEntity;
import com.swapcampus.review.mapper.ReviewMapper;
import com.swapcampus.review.service.ReviewService;
import com.swapcampus.review.vo.ReviewResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final OrderMapper orderMapper;

    public ReviewServiceImpl(ReviewMapper reviewMapper, OrderMapper orderMapper) {
        this.reviewMapper = reviewMapper;
        this.orderMapper = orderMapper;
    }

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewRequest request, Long reviewerId) {
        OrderEntity order = orderMapper.selectById(request.getOrderId());
        if (order == null) throw new RuntimeException("订单不存在");
        if (!OrderStatus.COMPLETED.name().equals(order.getStatus())) {
            throw new RuntimeException("订单未完成，不能评价");
        }

        // 判断评价方是买家还是卖家，确定被评价方
        Long revieweeId;
        if (order.getBuyerId().equals(reviewerId)) {
            revieweeId = order.getSellerId();
        } else if (order.getSellerId().equals(reviewerId)) {
            revieweeId = order.getBuyerId();
        } else {
            throw new RuntimeException("无权评价此订单");
        }

        // 检查是否已经评价过
        Long count = reviewMapper.selectCount(new LambdaQueryWrapper<ReviewEntity>()
                .eq(ReviewEntity::getOrderId, request.getOrderId())
                .eq(ReviewEntity::getReviewerId, reviewerId));
        if (count > 0) throw new RuntimeException("已评价过此订单");

        ReviewEntity review = new ReviewEntity();
        review.setOrderId(request.getOrderId());
        review.setReviewerId(reviewerId);
        review.setRevieweeId(revieweeId);
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setCreatedAt(LocalDateTime.now());
        reviewMapper.insert(review);

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