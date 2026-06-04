package com.swapcampus.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swapcampus.admin.dto.HandleReportRequest;
import com.swapcampus.admin.service.AdminModerationService;
import com.swapcampus.admin.vo.AdminDashboardResponse;
import com.swapcampus.admin.vo.AdminReportDetailResponse;
import com.swapcampus.admin.vo.AdminUserSummaryResponse;
import com.swapcampus.audit.service.AuditLogService;
import com.swapcampus.chat.entity.ChatMessageEntity;
import com.swapcampus.chat.mapper.ChatMessageMapper;
import com.swapcampus.chat.vo.ChatMessageResponse;
import com.swapcampus.chat.websocket.ChatWebSocketSessionRegistry;
import com.swapcampus.common.enums.ReportActionType;
import com.swapcampus.common.enums.ReportStatus;
import com.swapcampus.common.enums.ReportTargetType;
import com.swapcampus.common.enums.Role;
import com.swapcampus.common.enums.UserStatus;
import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.exception.ErrorCode;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AdminModerationServiceImpl implements AdminModerationService {

    private final ReportMapper reportMapper;
    private final ReportActionMapper reportActionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserModerationService userModerationService;
    private final ChatWebSocketSessionRegistry sessionRegistry;
    private final AuditLogService auditLogService;

    public AdminModerationServiceImpl(ReportMapper reportMapper,
                                      ReportActionMapper reportActionMapper,
                                      ChatMessageMapper chatMessageMapper,
                                      UserMapper userMapper,
                                      UserProfileMapper userProfileMapper,
                                      UserModerationService userModerationService,
                                      ChatWebSocketSessionRegistry sessionRegistry,
                                      AuditLogService auditLogService) {
        this.reportMapper = reportMapper;
        this.reportActionMapper = reportActionMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.userMapper = userMapper;
        this.userProfileMapper = userProfileMapper;
        this.userModerationService = userModerationService;
        this.sessionRegistry = sessionRegistry;
        this.auditLogService = auditLogService;
    }

    @Override
    public AdminDashboardResponse dashboard() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        AdminDashboardResponse response = new AdminDashboardResponse();
        response.setPendingReports(countReports(ReportStatus.PENDING, null));
        response.setTodayReports(countReports(null, startOfDay));
        response.setActiveChatUsers(countDistinctChatUsers(startOfDay));
        return response;
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
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.SYS_ADMIN) {
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
}
