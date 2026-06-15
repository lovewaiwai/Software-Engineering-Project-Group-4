package com.swapcampus.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swapcampus.chat.entity.ChatMessageEntity;
import com.swapcampus.chat.mapper.ChatMessageMapper;
import com.swapcampus.common.enums.ReportStatus;
import com.swapcampus.common.enums.ReportTargetType;
import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.exception.ErrorCode;
import com.swapcampus.product.entity.ProductEntity;
import com.swapcampus.product.mapper.ProductMapper;
import com.swapcampus.report.dto.CreateReportRequest;
import com.swapcampus.report.entity.ReportEntity;
import com.swapcampus.report.mapper.ReportMapper;
import com.swapcampus.report.service.ReportService;
import com.swapcampus.report.vo.ReportResponse;
import com.swapcampus.user.entity.UserEntity;
import com.swapcampus.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;

    public ReportServiceImpl(ReportMapper reportMapper,
                             ChatMessageMapper chatMessageMapper,
                             UserMapper userMapper,
                             ProductMapper productMapper) {
        this.reportMapper = reportMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.userMapper = userMapper;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional
    public ReportResponse createReport(Long reporterId, CreateReportRequest request) {
        if (request.getTargetType() == ReportTargetType.CHAT_MESSAGE) {
            ChatMessageEntity message = chatMessageMapper.selectById(request.getTargetId());
            if (message == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "被举报消息不存在");
            }
            if (request.getSessionId() == null) {
                request.setSessionId(message.getSessionId());
            }
            if (request.getReportedUserId() == null) {
                request.setReportedUserId(message.getSenderId());
            }
        } else if (request.getTargetType() == ReportTargetType.USER) {
            UserEntity user = userMapper.selectById(request.getTargetId());
            if (user == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "被举报用户不存在");
            }
            if (request.getReportedUserId() == null) {
                request.setReportedUserId(request.getTargetId());
            }
        } else if (request.getTargetType() == ReportTargetType.PRODUCT) {
            ProductEntity product = productMapper.selectById(request.getTargetId());
            if (product == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "被举报商品不存在");
            }
            if (request.getReportedUserId() == null) {
                request.setReportedUserId(product.getSellerId());
            }
        }

        if (request.getReportedUserId() != null && request.getReportedUserId().equals(reporterId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能举报自己");
        }

        ReportEntity entity = new ReportEntity();
        entity.setReporterId(reporterId);
        entity.setTargetType(request.getTargetType());
        entity.setTargetId(request.getTargetId());
        entity.setSessionId(request.getSessionId());
        entity.setReportedUserId(request.getReportedUserId());
        entity.setReason(request.getReason());
        entity.setDescription(request.getDescription());
        entity.setEvidenceUrl(request.getEvidenceUrl());
        entity.setStatus(ReportStatus.PENDING);
        entity.setCreatedAt(LocalDateTime.now());

        ReportEntity existing = reportMapper.selectOne(new LambdaQueryWrapper<ReportEntity>()
                .eq(ReportEntity::getReporterId, reporterId)
                .eq(ReportEntity::getTargetType, request.getTargetType())
                .eq(ReportEntity::getTargetId, request.getTargetId()));
        if (existing != null) {
            if (existing.getStatus() == ReportStatus.PENDING || existing.getStatus() == ReportStatus.PROCESSING) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "您已举报过该内容，请等待审核员处理");
            }
            existing.setReason(request.getReason());
            existing.setDescription(request.getDescription());
            existing.setEvidenceUrl(request.getEvidenceUrl());
            existing.setSessionId(request.getSessionId());
            existing.setReportedUserId(request.getReportedUserId());
            existing.setStatus(ReportStatus.PENDING);
            existing.setRejectReason(null);
            existing.setCreatedAt(LocalDateTime.now());
            reportMapper.updateById(existing);
            return ReportResponse.from(existing);
        }
        reportMapper.insert(entity);
        return ReportResponse.from(entity);
    }

    @Override
    public List<ReportResponse> listMyReports(Long reporterId) {
        return reportMapper.selectList(new LambdaQueryWrapper<ReportEntity>()
                        .eq(ReportEntity::getReporterId, reporterId)
                        .orderByDesc(ReportEntity::getCreatedAt))
                .stream()
                .map(ReportResponse::from)
                .toList();
    }
}
