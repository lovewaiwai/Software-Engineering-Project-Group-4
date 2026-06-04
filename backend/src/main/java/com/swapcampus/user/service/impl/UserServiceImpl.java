package com.swapcampus.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swapcampus.audit.service.AuditLogService;
import com.swapcampus.common.api.PageResponse;
import com.swapcampus.common.enums.UserStatus;
import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.exception.ErrorCode;
import com.swapcampus.common.security.CurrentUserContext;
import com.swapcampus.user.config.UserVerificationProperties;
import com.swapcampus.user.dto.UserProfileUpdateRequest;
import com.swapcampus.user.dto.UserStudentVerifyRequest;
import com.swapcampus.user.entity.CreditRecordEntity;
import com.swapcampus.user.entity.UserEntity;
import com.swapcampus.user.entity.UserProfileEntity;
import com.swapcampus.user.mapper.CreditRecordMapper;
import com.swapcampus.user.mapper.UserMapper;
import com.swapcampus.user.mapper.UserProfileMapper;
import com.swapcampus.user.service.UserService;
import com.swapcampus.user.vo.CreditRecordResponse;
import com.swapcampus.user.vo.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final CreditRecordMapper creditRecordMapper;
    private final AuditLogService auditLogService;
    private final UserVerificationProperties verificationProperties;

    public UserServiceImpl(UserMapper userMapper,
                           UserProfileMapper userProfileMapper,
                           CreditRecordMapper creditRecordMapper,
                           AuditLogService auditLogService,
                           UserVerificationProperties verificationProperties) {
        this.userMapper = userMapper;
        this.userProfileMapper = userProfileMapper;
        this.creditRecordMapper = creditRecordMapper;
        this.auditLogService = auditLogService;
        this.verificationProperties = verificationProperties;
    }

    @Override
    public String moduleName() {
        return "user";
    }

    @Override
    public UserResponse getCurrentUser() {
        return getUserById(CurrentUserContext.requireUserId());
    }

    @Override
    public UserResponse getUserById(Long userId) {
        UserEntity user = loadUser(userId);
        UserProfileEntity profile = userProfileMapper.selectById(userId);
        return UserResponse.from(user, profile);
    }

    @Override
    @Transactional
    public UserResponse updateCurrentUserProfile(UserProfileUpdateRequest request) {
        Long userId = CurrentUserContext.requireUserId();
        UserEntity user = loadActiveUser(userId);
        UserProfileEntity profile = loadOrCreateProfile(userId);

        applyIfPresent(request.getRealName(), profile::setRealName);
        applyIfPresent(request.getCollege(), profile::setCollege);
        applyIfPresent(request.getGrade(), profile::setGrade);
        applyIfPresent(request.getAvatarUrl(), profile::setAvatarUrl);
        applyIfPresent(request.getBio(), profile::setBio);
        applyIfPresent(request.getContactMasked(), profile::setContactMasked);

        saveProfile(profile);
        auditLogService.record(userId, "USER_PROFILE_UPDATE", "USER", user.getId(), "更新个人资料");
        return getUserById(userId);
    }

    @Override
    @Transactional
    public UserResponse verifyStudent(UserStudentVerifyRequest request) {
        Long userId = CurrentUserContext.requireUserId();
        UserEntity user = loadActiveUser(userId);
        UserProfileEntity profile = loadOrCreateProfile(userId);

        String realName = requireText(request.getRealName(), "姓名不能为空");
        String studentNo = requireText(request.getStudentNo(), "学号不能为空");
        String college = requireText(request.getCollege(), "学院不能为空");

        validateStudentNo(studentNo);
        validateCollege(college);
        ensureStudentNoAvailable(userId, studentNo);
        ensureVerificationCanProceed(profile, studentNo);

        profile.setRealName(realName);
        profile.setStudentNo(studentNo);
        profile.setCollege(college);
        profile.setGrade(normalizeToNull(request.getGrade()));
        profile.setVerifiedAt(LocalDateTime.now(SHANGHAI));
        profile.setContactMasked(maskStudentNo(studentNo));

        saveProfile(profile);
        auditLogService.record(userId, "USER_STUDENT_VERIFY", "USER", user.getId(), "学号实名验证");
        return getUserById(userId);
    }

    @Override
    public PageResponse<CreditRecordResponse> getCreditRecords(long page, long pageSize) {
        Long userId = CurrentUserContext.requireUserId();
        Page<CreditRecordEntity> result = creditRecordMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<CreditRecordEntity>()
                        .eq(CreditRecordEntity::getUserId, userId)
                        .orderByDesc(CreditRecordEntity::getCreatedAt)
                        .orderByDesc(CreditRecordEntity::getId)
        );
        List<CreditRecordResponse> items = result.getRecords().stream()
                .map(CreditRecordResponse::from)
                .toList();
        return new PageResponse<>(items, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    @Transactional
    public void adjustCreditScore(Long userId, int delta, String reason, String refType, Long refId) {
        UserEntity user = loadActiveUser(userId);
        int currentScore = user.getCreditScore() == null ? 0 : user.getCreditScore();
        int updatedScore = Math.max(0, Math.min(100, currentScore + delta));
        user.setCreditScore(updatedScore);
        userMapper.updateById(user);

        CreditRecordEntity record = new CreditRecordEntity();
        record.setUserId(userId);
        record.setDelta(delta);
        record.setScoreAfter(updatedScore);
        record.setReason(requireText(reason, "信用分变动原因不能为空"));
        record.setRefType(normalizeToNull(refType));
        record.setRefId(refId);
        record.setCreatedAt(LocalDateTime.now(SHANGHAI));
        creditRecordMapper.insert(record);
    }

    private UserEntity loadUser(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private UserEntity loadActiveUser(Long userId) {
        UserEntity user = loadUser(userId);
        if (UserStatus.BANNED.equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已被封禁");
        }
        return user;
    }

    private UserProfileEntity loadOrCreateProfile(Long userId) {
        UserProfileEntity profile = userProfileMapper.selectById(userId);
        if (profile == null) {
            profile = new UserProfileEntity();
            profile.setUserId(userId);
        }
        return profile;
    }

    private void saveProfile(UserProfileEntity profile) {
        if (profile.getUserId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户资料缺少归属用户");
        }
        if (userProfileMapper.selectById(profile.getUserId()) == null) {
            userProfileMapper.insert(profile);
        } else {
            userProfileMapper.updateById(profile);
        }
    }

    private void validateStudentNo(String studentNo) {
        if (!studentNo.matches(verificationProperties.getStudentNoPattern())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "学号格式不正确");
        }
    }

    private void validateCollege(String college) {
        List<String> allowedColleges = verificationProperties.getAllowedColleges();
        if (allowedColleges != null && !allowedColleges.isEmpty()
                && allowedColleges.stream().noneMatch(item -> item != null && item.equals(college))) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "学院不在可验证范围内");
        }
    }

    private void ensureStudentNoAvailable(Long userId, String studentNo) {
        Long count = userProfileMapper.selectCount(new LambdaQueryWrapper<UserProfileEntity>()
                .eq(UserProfileEntity::getStudentNo, studentNo)
                .ne(UserProfileEntity::getUserId, userId));
        if (count != null && count > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该学号已被其他账号绑定");
        }
    }

    private void ensureVerificationCanProceed(UserProfileEntity profile, String studentNo) {
        if (profile.getVerifiedAt() != null && profile.getStudentNo() != null
                && !profile.getStudentNo().equals(studentNo)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该账号已完成实名，不能重复绑定其他学号");
        }
    }

    private void applyIfPresent(String value, java.util.function.Consumer<String> setter) {
        if (value != null) {
            setter.accept(normalizeToNull(value));
        }
    }

    private String requireText(String value, String errorMessage) {
        String normalized = normalizeToNull(value);
        if (normalized == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, errorMessage);
        }
        return normalized;
    }

    private String maskStudentNo(String studentNo) {
        if (studentNo == null || studentNo.length() <= 5) {
            return studentNo;
        }
        int visiblePrefix = Math.min(3, studentNo.length() - 2);
        int visibleSuffix = Math.min(2, studentNo.length() - visiblePrefix);
        String prefix = studentNo.substring(0, visiblePrefix);
        String suffix = studentNo.substring(studentNo.length() - visibleSuffix);
        return prefix + "****" + suffix;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeToNull(String value) {
        String normalized = normalize(value);
        return normalized == null || normalized.isEmpty() ? null : normalized;
    }
}
