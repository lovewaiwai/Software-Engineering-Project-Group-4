package com.swapcampus.user.service.impl;

import com.swapcampus.common.enums.UserStatus;
import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.exception.ErrorCode;
import com.swapcampus.points.entity.PointRecordEntity;
import com.swapcampus.points.mapper.PointRecordMapper;
import com.swapcampus.user.entity.CreditRecordEntity;
import com.swapcampus.user.entity.UserEntity;
import com.swapcampus.user.mapper.CreditRecordMapper;
import com.swapcampus.user.mapper.UserMapper;
import com.swapcampus.user.service.UserAccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * {@link UserAccountService} 的实现，所有调整操作在同一事务中完成
 * "更新用户分值 + 写入对应流水记录"。
 */
@Service
public class UserAccountServiceImpl implements UserAccountService {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    private final UserMapper userMapper;
    private final CreditRecordMapper creditRecordMapper;
    private final PointRecordMapper pointRecordMapper;

    public UserAccountServiceImpl(UserMapper userMapper,
                                   CreditRecordMapper creditRecordMapper,
                                   PointRecordMapper pointRecordMapper) {
        this.userMapper = userMapper;
        this.creditRecordMapper = creditRecordMapper;
        this.pointRecordMapper = pointRecordMapper;
    }

    @Override
    @Transactional
    public int addCredit(Long userId, int delta, String reason, String refType, Long refId) {
        UserEntity user = loadActiveUser(userId);
        int currentScore = user.getCreditScore() == null ? 60 : user.getCreditScore();
        int scoreAfter = Math.max(0, currentScore + delta);

        user.setCreditScore(scoreAfter);
        userMapper.updateById(user);

        CreditRecordEntity record = new CreditRecordEntity();
        record.setUserId(userId);
        record.setDelta(delta);
        record.setScoreAfter(scoreAfter);
        record.setReason(reason);
        record.setRefType(refType);
        record.setRefId(refId);
        record.setCreatedAt(LocalDateTime.now(SHANGHAI));
        creditRecordMapper.insert(record);

        return scoreAfter;
    }

    @Override
    @Transactional
    public int addPoints(Long userId, int delta, String reason, String refType, Long refId) {
        UserEntity user = loadActiveUser(userId);
        int currentBalance = user.getPointBalance() == null ? 0 : user.getPointBalance();
        int balanceAfter = currentBalance + delta;
        if (balanceAfter < 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "积分余额不足，无法扣减");
        }

        user.setPointBalance(balanceAfter);
        userMapper.updateById(user);

        PointRecordEntity record = new PointRecordEntity();
        record.setUserId(userId);
        record.setDelta(delta);
        record.setBalanceAfter(balanceAfter);
        record.setReason(reason);
        record.setRefType(refType);
        record.setRefId(refId);
        record.setCreatedAt(LocalDateTime.now(SHANGHAI));
        pointRecordMapper.insert(record);

        return balanceAfter;
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
}