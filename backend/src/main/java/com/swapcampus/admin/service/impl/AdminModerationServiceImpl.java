package com.swapcampus.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swapcampus.admin.dto.HandleReportRequest;
import com.swapcampus.admin.dto.ProductReviewRequest;
import com.swapcampus.admin.service.AdminModerationService;
import com.swapcampus.admin.vo.AdminDashboardResponse;
import com.swapcampus.admin.vo.AdminReportDetailResponse;
import com.swapcampus.admin.vo.AdminUserSummaryResponse;
import com.swapcampus.audit.entity.AuditEntity;
import com.swapcampus.audit.mapper.AuditMapper;
import com.swapcampus.audit.service.AuditLogService;
import com.swapcampus.chat.entity.ChatMessageEntity;
import com.swapcampus.chat.mapper.ChatMessageMapper;
import com.swapcampus.chat.vo.ChatMessageResponse;
import com.swapcampus.chat.websocket.ChatWebSocketSessionRegistry;
import com.swapcampus.common.enums.OrderStatus;
import com.swapcampus.common.enums.ProductStatus;
import com.swapcampus.common.enums.ReportActionType;
import com.swapcampus.common.enums.ReportStatus;
import com.swapcampus.common.enums.ReportTargetType;
import com.swapcampus.common.enums.Role;
import com.swapcampus.common.enums.UserStatus;
import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.exception.ErrorCode;
import com.swapcampus.order.entity.OrderEntity;
import com.swapcampus.order.mapper.OrderMapper;
import com.swapcampus.product.entity.CategoryEntity;
import com.swapcampus.product.entity.ProductEntity;
import com.swapcampus.product.entity.ProductImageEntity;
import com.swapcampus.product.mapper.CategoryMapper;
import com.swapcampus.product.mapper.ProductImageMapper;
import com.swapcampus.product.mapper.ProductMapper;
import com.swapcampus.product.vo.ProductResponse;
import com.swapcampus.report.entity.ReportActionEntity;
import com.swapcampus.report.entity.ReportEntity;
import com.swapcampus.report.mapper.ReportActionMapper;
import com.swapcampus.report.mapper.ReportMapper;
import com.swapcampus.report.vo.ReportResponse;
import com.swapcampus.user.entity.UserEntity;
import com.swapcampus.user.entity.UserProfileEntity;
import com.swapcampus.user.mapper.UserMapper;
import com.swapcampus.user.mapper.UserProfileMapper;
import com.swapcampus.user.service.UserModerationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class AdminModerationServiceImpl implements AdminModerationService {

    private final ReportMapper reportMapper;
    private final ReportActionMapper reportActionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final AuditMapper auditMapper;
    private final UserModerationService userModerationService;
    private final ChatWebSocketSessionRegistry sessionRegistry;
    private final AuditLogService auditLogService;
    private final CategoryMapper categoryMapper;
    private final ProductImageMapper productImageMapper;

    public AdminModerationServiceImpl(ReportMapper reportMapper,
                                      ReportActionMapper reportActionMapper,
                                      ChatMessageMapper chatMessageMapper,
                                      UserMapper userMapper,
                                      UserProfileMapper userProfileMapper,
                                      ProductMapper productMapper,
                                      OrderMapper orderMapper,
                                      AuditMapper auditMapper,
                                      UserModerationService userModerationService,
                                      ChatWebSocketSessionRegistry sessionRegistry,
                                      AuditLogService auditLogService,
                                      CategoryMapper categoryMapper,
                                      ProductImageMapper productImageMapper) {
        this.reportMapper = reportMapper;
        this.reportActionMapper = reportActionMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.userMapper = userMapper;
        this.userProfileMapper = userProfileMapper;
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
        this.auditMapper = auditMapper;
        this.userModerationService = userModerationService;
        this.sessionRegistry = sessionRegistry;
        this.auditLogService = auditLogService;
        this.categoryMapper = categoryMapper;
        this.productImageMapper = productImageMapper;
    }

    @Override
    public AdminDashboardResponse dashboard() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        AdminDashboardResponse response = new AdminDashboardResponse();
        response.setPendingReports(countReports(ReportStatus.PENDING, null));
        response.setTodayReports(countReports(null, startOfDay));
        response.setActiveChatUsers(countDistinctChatUsers(startOfDay));

        response.setTotalUsers(countUsers(Role.USER, null));
        response.setActiveUsers(countUsers(Role.USER, UserStatus.ACTIVE));
        response.setBannedUsers(countUsers(Role.USER, UserStatus.BANNED));
        response.setTodayNewUsers(countUsersSince(Role.USER, startOfDay));
        response.setTodayActiveUsers(countTodayActiveUsers(startOfDay));

        response.setTotalProducts(countProducts(null));
        response.setActiveProducts(countProducts(ProductStatus.ACTIVE));
        response.setTodayNewProducts(countProductsSince(startOfDay));

        response.setTotalOrders(countOrders(null));
        response.setCompletedOrders(countOrders(OrderStatus.COMPLETED));
        response.setTodayNewOrders(countOrdersSince(startOfDay));
        response.setTotalGmv(sumCompletedGmv(null));
        response.setTodayGmv(sumCompletedGmv(startOfDay));
        return response;
    }

    @Override
    public List<ProductResponse> listPendingProducts() {
        return productMapper.selectList(new LambdaQueryWrapper<ProductEntity>()
                        .eq(ProductEntity::getStatus, ProductStatus.PENDING_REVIEW.name())
                        .orderByAsc(ProductEntity::getCreatedAt)
                        .last("OFFSET 0 ROWS FETCH NEXT 100 ROWS ONLY"))
                .stream()
                .map(this::toProductResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProductResponse approveProduct(Long reviewerId, Long productId) {
        ProductEntity product = requireReviewableProduct(productId);
        product.setStatus(ProductStatus.ACTIVE.name());
        product.setAuditReason(null);
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);
        auditLogService.record(reviewerId, "PRODUCT_APPROVE", "PRODUCT", productId, "商品审核通过");
        return toProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse rejectProduct(Long reviewerId, Long productId, ProductReviewRequest request) {
        ProductEntity product = requireReviewableProduct(productId);
        String reason = request == null ? null : normalizeToNull(request.getReason());
        if (reason == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "拒绝原因不能为空");
        }
        product.setStatus(ProductStatus.REVIEW_REJECTED.name());
        product.setAuditReason(reason);
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);
        auditLogService.record(reviewerId, "PRODUCT_REJECT", "PRODUCT", productId, reason);
        return toProductResponse(product);
    }

    @Override
    public List<ReportResponse> listPendingReports() {
        return reportMapper.selectList(new LambdaQueryWrapper<ReportEntity>()
                        .in(ReportEntity::getStatus, ReportStatus.PENDING, ReportStatus.PROCESSING)
                        .orderByDesc(ReportEntity::getCreatedAt))
                .stream()
                .map(ReportResponse::from)
                .toList();
    }

    @Override
    public AdminReportDetailResponse getReportDetail(Long reportId) {
        ReportEntity report = requireReport(reportId);
        AdminReportDetailResponse detail = new AdminReportDetailResponse();
        detail.setReport(ReportResponse.from(report));
        if (report.getTargetType() == ReportTargetType.CHAT_MESSAGE) {
            detail.setContextMessages(loadContextMessages(report.getTargetId()));
        } else if (report.getTargetType() == ReportTargetType.USER && report.getSessionId() != null) {
            detail.setContextMessages(loadRecentSessionMessages(report.getSessionId()));
        } else {
            detail.setContextMessages(List.of());
        }
        if (report.getReportedUserId() != null) {
            detail.setReportedUser(buildUserSummary(report.getReportedUserId()));
        }
        return detail;
    }

    @Override
    @Transactional
    public ReportResponse handleReport(Long adminId, Long reportId, HandleReportRequest request) {
        ReportEntity report = requireReport(reportId);
        if (report.getStatus() == ReportStatus.RESOLVED || report.getStatus() == ReportStatus.REJECTED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该举报已处理");
        }

        ReportActionEntity action = new ReportActionEntity();
        action.setReportId(reportId);
        action.setAdminId(adminId);
        action.setActionType(request.getActionType());
        action.setNote(request.getNote());
        action.setCreatedAt(LocalDateTime.now());
        reportActionMapper.insert(action);

        switch (request.getActionType()) {
            case WARN -> report.setStatus(ReportStatus.RESOLVED);
            case MUTE -> {
                if (report.getReportedUserId() == null) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST, "缺少被举报人");
                }
                int hours = request.getMuteHours() == null ? 24 : request.getMuteHours();
                userModerationService.muteUser(report.getReportedUserId(), adminId, hours, request.getNote());
                report.setStatus(ReportStatus.RESOLVED);
            }
            case BAN -> {
                if (report.getReportedUserId() == null) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST, "缺少被举报人");
                }
                UserEntity user = userMapper.selectById(report.getReportedUserId());
                if (user != null) {
                    user.setStatus(UserStatus.BANNED);
                    userMapper.updateById(user);
                    sessionRegistry.disconnect(report.getReportedUserId());
                }
                report.setStatus(ReportStatus.RESOLVED);
            }
            case REJECT -> {
                report.setStatus(ReportStatus.REJECTED);
                report.setRejectReason(request.getNote());
            }
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的处置方式");
        }

        reportMapper.updateById(report);
        auditLogService.record(adminId, "REPORT_ACTION", "REPORT", reportId,
                request.getActionType().name() + ": " + (request.getNote() == null ? "" : request.getNote()));
        return ReportResponse.from(report);
    }

    @Override
    public List<AdminUserSummaryResponse> listUsers(String keyword) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getRole, Role.USER)
                .orderByDesc(UserEntity::getCreatedAt);
        if (keyword != null && !keyword.isBlank()) {
            String q = keyword.trim();
            wrapper.and(w -> w.like(UserEntity::getUsername, q).or().apply("CAST(id AS NVARCHAR(20)) = {0}", q));
        }
        return userMapper.selectList(wrapper.last("OFFSET 0 ROWS FETCH NEXT 100 ROWS ONLY"))
                .stream()
                .map(user -> buildUserSummary(user.getId()))
                .toList();
    }

    @Override
    public AdminUserSummaryResponse getUserSummary(Long userId) {
        return buildUserSummary(userId);
    }

    @Override
    @Transactional
    public AdminUserSummaryResponse banUser(Long adminId, Long userId, String note) {
        UserEntity user = requireModeratableUser(userId);
        user.setStatus(UserStatus.BANNED);
        userMapper.updateById(user);
        sessionRegistry.disconnect(userId);
        auditLogService.record(adminId, "USER_BAN", "USER", userId, note == null ? "封禁用户" : note);
        return buildUserSummary(userId);
    }

    @Override
    @Transactional
    public AdminUserSummaryResponse unbanUser(Long adminId, Long userId, String note) {
        UserEntity user = requireModeratableUser(userId);
        user.setStatus(UserStatus.ACTIVE);
        userMapper.updateById(user);
        auditLogService.record(adminId, "USER_UNBAN", "USER", userId, note == null ? "解封用户" : note);
        return buildUserSummary(userId);
    }

    @Override
    @Transactional
    public AdminUserSummaryResponse muteUser(Long adminId, Long userId, int hours, String note) {
        requireModeratableUser(userId);
        userModerationService.muteUser(userId, adminId, hours, note);
        auditLogService.record(adminId, "USER_MUTE", "USER", userId, "禁言" + hours + "小时: " + (note == null ? "" : note));
        return buildUserSummary(userId);
    }

    @Override
    @Transactional
    public AdminUserSummaryResponse unmuteUser(Long adminId, Long userId, String note) {
        requireModeratableUser(userId);
        userModerationService.unmuteUser(userId);
        auditLogService.record(adminId, "USER_UNMUTE", "USER", userId, note == null ? "解除禁言" : note);
        return buildUserSummary(userId);
    }

    private UserEntity requireModeratableUser(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (user.getRole() == Role.PRODUCT_REVIEWER || user.getRole() == Role.ADMIN || user.getRole() == Role.SYS_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能处置管理员账号");
        }
        return user;
    }

    private AdminUserSummaryResponse buildUserSummary(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        UserProfileEntity profile = userProfileMapper.selectById(userId);
        LocalDateTime mutedUntil = userModerationService.getActiveMuteUntil(userId);
        AdminUserSummaryResponse summary = new AdminUserSummaryResponse();
        summary.setId(user.getId());
        summary.setUsername(user.getUsername());
        summary.setRealName(profile == null ? null : profile.getRealName());
        summary.setRole(user.getRole());
        summary.setStatus(user.getStatus());
        summary.setMuted(mutedUntil != null);
        summary.setMutedUntil(mutedUntil);
        summary.setCreditScore(user.getCreditScore());
        return summary;
    }

    private ReportEntity requireReport(Long reportId) {
        ReportEntity report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "举报记录不存在");
        }
        return report;
    }

    private ProductEntity requireReviewableProduct(Long productId) {
        ProductEntity product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        if (!ProductStatus.PENDING_REVIEW.name().equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "只能审核待审核商品");
        }
        return product;
    }

    private ProductResponse toProductResponse(ProductEntity product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setSellerId(product.getSellerId());
        response.setCategoryId(product.getCategoryId());
        response.setTitle(product.getTitle());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setOriginalPrice(product.getOriginalPrice());
        response.setConditionLevel(product.getConditionLevel());
        response.setCampus(product.getCampus());
        response.setTradeModes(parseTradeModes(product.getTradeModes()));
        response.setStatus(product.getStatus());
        response.setViewCount(product.getViewCount());
        response.setFavoriteCount(product.getFavoriteCount());
        response.setAuditReason(product.getAuditReason());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        CategoryEntity category = categoryMapper.selectById(product.getCategoryId());
        if (category != null) {
            response.setCategoryName(category.getName());
        }
        response.setImageUrls(productImageMapper.selectList(new LambdaQueryWrapper<ProductImageEntity>()
                        .eq(ProductImageEntity::getProductId, product.getId())
                        .orderByAsc(ProductImageEntity::getSortOrder)
                        .orderByAsc(ProductImageEntity::getId))
                .stream()
                .map(ProductImageEntity::getUrl)
                .toList());
        return response;
    }

    private List<String> parseTradeModes(String tradeModes) {
        if (tradeModes == null || tradeModes.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tradeModes.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }

    private String normalizeToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private List<ChatMessageResponse> loadContextMessages(Long messageId) {
        ChatMessageEntity target = chatMessageMapper.selectById(messageId);
        if (target == null) {
            return List.of();
        }
        List<ChatMessageEntity> before = chatMessageMapper.selectList(new LambdaQueryWrapper<ChatMessageEntity>()
                .eq(ChatMessageEntity::getSessionId, target.getSessionId())
                .lt(ChatMessageEntity::getSeqNo, target.getSeqNo())
                .orderByDesc(ChatMessageEntity::getSeqNo)
                .last("OFFSET 0 ROWS FETCH NEXT 5 ROWS ONLY"));
        List<ChatMessageEntity> after = chatMessageMapper.selectList(new LambdaQueryWrapper<ChatMessageEntity>()
                .eq(ChatMessageEntity::getSessionId, target.getSessionId())
                .gt(ChatMessageEntity::getSeqNo, target.getSeqNo())
                .orderByAsc(ChatMessageEntity::getSeqNo)
                .last("OFFSET 0 ROWS FETCH NEXT 5 ROWS ONLY"));

        List<ChatMessageEntity> all = new ArrayList<>(before);
        all.add(target);
        all.addAll(after);
        all.sort(Comparator.comparing(ChatMessageEntity::getSeqNo));
        return all.stream().map(ChatMessageResponse::from).toList();
    }

    private List<ChatMessageResponse> loadRecentSessionMessages(Long sessionId) {
        return chatMessageMapper.selectList(new LambdaQueryWrapper<ChatMessageEntity>()
                        .eq(ChatMessageEntity::getSessionId, sessionId)
                        .orderByDesc(ChatMessageEntity::getSeqNo)
                        .last("OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY"))
                .stream()
                .sorted(Comparator.comparing(ChatMessageEntity::getSeqNo))
                .map(ChatMessageResponse::from)
                .toList();
    }

    private long countReports(ReportStatus status, LocalDateTime since) {
        LambdaQueryWrapper<ReportEntity> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ReportEntity::getStatus, status);
        }
        if (since != null) {
            wrapper.ge(ReportEntity::getCreatedAt, since);
        }
        Long count = reportMapper.selectCount(wrapper);
        return count == null ? 0 : count;
    }

    private long countDistinctChatUsers(LocalDateTime since) {
        List<ChatMessageEntity> messages = chatMessageMapper.selectList(new LambdaQueryWrapper<ChatMessageEntity>()
                .ge(ChatMessageEntity::getCreatedAt, since));
        return messages.stream().map(ChatMessageEntity::getSenderId).distinct().count();
    }

    private long countUsers(Role role, UserStatus status) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        if (role != null) {
            wrapper.eq(UserEntity::getRole, role);
        }
        if (status != null) {
            wrapper.eq(UserEntity::getStatus, status);
        }
        Long count = userMapper.selectCount(wrapper);
        return count == null ? 0 : count;
    }

    private long countUsersSince(Role role, LocalDateTime since) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<UserEntity>()
                .ge(UserEntity::getCreatedAt, since);
        if (role != null) {
            wrapper.eq(UserEntity::getRole, role);
        }
        Long count = userMapper.selectCount(wrapper);
        return count == null ? 0 : count;
    }

    private long countTodayActiveUsers(LocalDateTime since) {
        Set<Long> activeUserIds = new HashSet<>();

        auditMapper.selectList(new LambdaQueryWrapper<AuditEntity>()
                        .eq(AuditEntity::getAction, "AUTH_LOGIN")
                        .ge(AuditEntity::getCreatedAt, since))
                .forEach(item -> {
                    if (item.getOperatorId() != null) {
                        activeUserIds.add(item.getOperatorId());
                    }
                });

        chatMessageMapper.selectList(new LambdaQueryWrapper<ChatMessageEntity>()
                        .ge(ChatMessageEntity::getCreatedAt, since))
                .forEach(item -> activeUserIds.add(item.getSenderId()));

        orderMapper.selectList(new LambdaQueryWrapper<OrderEntity>()
                        .ge(OrderEntity::getCreatedAt, since))
                .forEach(item -> {
                    if (item.getBuyerId() != null) {
                        activeUserIds.add(item.getBuyerId());
                    }
                    if (item.getSellerId() != null) {
                        activeUserIds.add(item.getSellerId());
                    }
                });

        return activeUserIds.size();
    }

    private long countProducts(ProductStatus status) {
        LambdaQueryWrapper<ProductEntity> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ProductEntity::getStatus, status.name());
        }
        Long count = productMapper.selectCount(wrapper);
        return count == null ? 0 : count;
    }

    private long countProductsSince(LocalDateTime since) {
        Long count = productMapper.selectCount(new LambdaQueryWrapper<ProductEntity>()
                .ge(ProductEntity::getCreatedAt, since));
        return count == null ? 0 : count;
    }

    private long countOrders(OrderStatus status) {
        LambdaQueryWrapper<OrderEntity> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(OrderEntity::getStatus, status.name());
        }
        Long count = orderMapper.selectCount(wrapper);
        return count == null ? 0 : count;
    }

    private long countOrdersSince(LocalDateTime since) {
        Long count = orderMapper.selectCount(new LambdaQueryWrapper<OrderEntity>()
                .ge(OrderEntity::getCreatedAt, since));
        return count == null ? 0 : count;
    }

    private BigDecimal sumCompletedGmv(LocalDateTime since) {
        LambdaQueryWrapper<OrderEntity> wrapper = new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getStatus, OrderStatus.COMPLETED.name());
        if (since != null) {
            wrapper.and(w -> w.ge(OrderEntity::getCompletedAt, since)
                    .or(x -> x.isNull(OrderEntity::getCompletedAt).ge(OrderEntity::getUpdatedAt, since)));
        }
        return orderMapper.selectList(wrapper).stream()
                .map(OrderEntity::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
