package com.swapcampus.points.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swapcampus.audit.service.AuditLogService;
import com.swapcampus.common.api.PageResponse;
import com.swapcampus.common.enums.UserStatus;
import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.exception.ErrorCode;
import com.swapcampus.common.security.CurrentUserContext;
import com.swapcampus.points.dto.PointRedeemRequest;
import com.swapcampus.points.entity.PointRedemptionEntity;
import com.swapcampus.points.entity.PointTaskEntity;
import com.swapcampus.points.mapper.PointRecordMapper;
import com.swapcampus.points.mapper.PointRedemptionMapper;
import com.swapcampus.points.entity.PointRecordEntity;
import com.swapcampus.user.entity.UserEntity;
import com.swapcampus.points.mapper.PointTaskMapper;
import com.swapcampus.points.service.PointService;
import com.swapcampus.points.vo.PointItemResponse;
import com.swapcampus.points.vo.PointRedemptionResponse;
import com.swapcampus.points.vo.PointRecordResponse;
import com.swapcampus.points.vo.PointTaskResponse;
import com.swapcampus.user.entity.UserProfileEntity;
import com.swapcampus.user.mapper.UserMapper;
import com.swapcampus.user.mapper.UserProfileMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class PointServiceImpl implements PointService {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final String CHECK_IN_CODE = "DAILY_CHECK_IN";
    private static final String CHECK_IN_REF_TYPE = "CHECK_IN";
    private static final String TASK_REF_TYPE = "TASK";
    private static final String REDEMPTION_REF_TYPE = "REDEMPTION";

    private static final List<PointItemResponse> ITEMS = List.of(
            new PointItemResponse("COUPON_10", "10元优惠券", 200, "用于平台兑换优惠券"),
            new PointItemResponse("COUPON_30", "30元优惠券", 500, "用于平台兑换优惠券"),
            new PointItemResponse("PROFILE_BADGE", "个人主页徽章", 120, "展示在个人主页的游戏化权益")
    );

    private final PointTaskMapper pointTaskMapper;
    private final PointRecordMapper pointRecordMapper;
    private final PointRedemptionMapper pointRedemptionMapper;
    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final AuditLogService auditLogService;
    public PointServiceImpl(PointTaskMapper pointTaskMapper,
                            PointRecordMapper pointRecordMapper,
                            PointRedemptionMapper pointRedemptionMapper,
                            UserMapper userMapper,
                            UserProfileMapper userProfileMapper,
                            AuditLogService auditLogService) {
        this.pointTaskMapper = pointTaskMapper;
        this.pointRecordMapper = pointRecordMapper;
        this.pointRedemptionMapper = pointRedemptionMapper;
        this.userMapper = userMapper;
        this.userProfileMapper = userProfileMapper;        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public PointRecordResponse checkIn() {
        return claimCheckIn(loadTaskByCode(CHECK_IN_CODE));
    }

    @Override
    public List<PointTaskResponse> getTasks() {
        Long userId = CurrentUserContext.requireUserId();
        List<PointTaskEntity> tasks = pointTaskMapper.selectList(new LambdaQueryWrapper<PointTaskEntity>()
                .orderByAsc(PointTaskEntity::getId));
        return tasks.stream()
                .map(task -> PointTaskResponse.from(task, isTaskClaimed(userId, task), isTaskClaimable(userId, task)))
                .toList();
    }

    @Override
    @Transactional
    public PointRecordResponse claimTask(String code) {
        PointTaskEntity task = loadTaskByCode(code);
        if (CHECK_IN_CODE.equals(task.getCode())) {
            return claimCheckIn(task);
        }
        return claimStandardTask(task);
    }

    @Override
    public PageResponse<PointRecordResponse> getRecords(long page, long pageSize) {
        Long userId = CurrentUserContext.requireUserId();
        Page<PointRecordEntity> result = pointRecordMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<PointRecordEntity>()
                        .eq(PointRecordEntity::getUserId, userId)
                        .orderByDesc(PointRecordEntity::getCreatedAt)
                        .orderByDesc(PointRecordEntity::getId)
        );
        List<PointRecordResponse> items = result.getRecords().stream()
                .map(PointRecordResponse::from)
                .toList();
        return new PageResponse<>(items, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public List<PointItemResponse> getItems() {
        return ITEMS;
    }

    @Override
    @Transactional
    public PointRedemptionResponse redeem(PointRedeemRequest request) {
        Long userId = CurrentUserContext.requireUserId();
        UserEntity user = loadActiveUser(userId);
        RedemptionDefinition item = findItem(request.getItemCode());
        if (user.getPointBalance() == null || user.getPointBalance() < item.costPoints()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "积分不足");
        }

        PointRedemptionEntity redemption = new PointRedemptionEntity();
        redemption.setUserId(userId);
        redemption.setItemCode(item.itemCode());
        redemption.setItemName(item.itemName());
        redemption.setCostPoints(item.costPoints());
        redemption.setStatus("SUCCESS");
        redemption.setCreatedAt(LocalDateTime.now(SHANGHAI));
        pointRedemptionMapper.insert(redemption);

        int balanceAfter = user.getPointBalance() - item.costPoints();
        user.setPointBalance(balanceAfter);
        userMapper.updateById(user);

        PointRecordEntity record = createRecord(userId, -item.costPoints(), balanceAfter, "兑换：" + item.itemName(), REDEMPTION_REF_TYPE, redemption.getId());
        pointRecordMapper.insert(record);

        auditLogService.record(userId, "POINT_REDEEM", "USER", userId, item.itemName());

        PointRedemptionResponse response = new PointRedemptionResponse();
        response.setId(redemption.getId());
        response.setItemCode(redemption.getItemCode());
        response.setItemName(redemption.getItemName());
        response.setCostPoints(redemption.getCostPoints());
        response.setStatus(redemption.getStatus());
        response.setBalanceAfter(balanceAfter);
        response.setCreatedAt(redemption.getCreatedAt());
        return response;
    }

    private PointRecordResponse claimCheckIn(PointTaskEntity task) {
        Long userId = CurrentUserContext.requireUserId();
        UserEntity user = loadActiveUser(userId);
        long todayRef = LocalDate.now(SHANGHAI).toEpochDay();
        if (hasPointRecord(userId, CHECK_IN_REF_TYPE, todayRef)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "今日已签到");
        }

        int balanceAfter = addPoints(user, task.getRewardPoints());
        PointRecordEntity record = createRecord(userId, task.getRewardPoints(), balanceAfter, task.getName(), CHECK_IN_REF_TYPE, todayRef);
        pointRecordMapper.insert(record);

        auditLogService.record(userId, "POINT_CHECK_IN", "USER", userId, task.getName());
        return PointRecordResponse.from(record);
    }

    private PointRecordResponse claimStandardTask(PointTaskEntity task) {
        Long userId = CurrentUserContext.requireUserId();
        if (!isTaskClaimable(userId, task)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "任务暂不可领取");
        }

        UserEntity user = loadActiveUser(userId);
        int balanceAfter = addPoints(user, task.getRewardPoints());
        PointRecordEntity record = createRecord(userId, task.getRewardPoints(), balanceAfter, task.getName(), TASK_REF_TYPE, task.getId());
        pointRecordMapper.insert(record);

        auditLogService.record(userId, "POINT_TASK_CLAIM", "USER", userId, task.getCode());
        return PointRecordResponse.from(record);
    }

    private int addPoints(UserEntity user, int delta) {
        int currentBalance = user.getPointBalance() == null ? 0 : user.getPointBalance();
        int balanceAfter = currentBalance + delta;
        user.setPointBalance(balanceAfter);
        userMapper.updateById(user);
        return balanceAfter;
    }

    private PointRecordEntity createRecord(Long userId, int delta, int balanceAfter, String reason, String refType, Long refId) {
        PointRecordEntity record = new PointRecordEntity();
        record.setUserId(userId);
        record.setDelta(delta);
        record.setBalanceAfter(balanceAfter);
        record.setReason(reason);
        record.setRefType(refType);
        record.setRefId(refId);
        record.setCreatedAt(LocalDateTime.now(SHANGHAI));
        return record;
    }

    private PointTaskEntity loadTaskByCode(String code) {
        PointTaskEntity task = pointTaskMapper.selectOne(new LambdaQueryWrapper<PointTaskEntity>()
                .eq(PointTaskEntity::getCode, normalize(code)));
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在");
        }
        if (!"ACTIVE".equalsIgnoreCase(task.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "任务未开放");
        }
        return task;
    }

    private boolean isTaskClaimed(Long userId, PointTaskEntity task) {
        if (CHECK_IN_CODE.equals(task.getCode())) {
            return hasPointRecord(userId, CHECK_IN_REF_TYPE, LocalDate.now(SHANGHAI).toEpochDay());
        }
        return hasPointRecord(userId, TASK_REF_TYPE, task.getId());
    }

    private boolean isTaskClaimable(Long userId, PointTaskEntity task) {
        if (isTaskClaimed(userId, task)) {
            return false;
        }
        if (CHECK_IN_CODE.equals(task.getCode())) {
            return true;
        }
        if ("PROFILE".equalsIgnoreCase(task.getTaskType())) {
            UserProfileEntity profile = userProfileMapper.selectById(userId);
            return profile != null
                    && notBlank(profile.getRealName())
                    && notBlank(profile.getStudentNo())
                    && notBlank(profile.getCollege())
                    && profile.getVerifiedAt() != null;
        }
        return false;
    }

    private boolean hasPointRecord(Long userId, String refType, Long refId) {
        Long count = pointRecordMapper.selectCount(new LambdaQueryWrapper<PointRecordEntity>()
                .eq(PointRecordEntity::getUserId, userId)
                .eq(PointRecordEntity::getRefType, refType)
                .eq(PointRecordEntity::getRefId, refId));
        return count != null && count > 0;
    }

    private UserEntity loadActiveUser(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (UserStatus.BANNED.equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已被封禁");
        }
        return user;
    }

    private RedemptionDefinition findItem(String itemCode) {
        String normalized = normalize(itemCode);
        return ITEMS.stream()
                .filter(item -> item.getItemCode().equals(normalized))
                .findFirst()
                .map(item -> new RedemptionDefinition(item.getItemCode(), item.getItemName(), item.getCostPoints()))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "兑换项不存在"));
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private record RedemptionDefinition(String itemCode, String itemName, int costPoints) {
    }
}