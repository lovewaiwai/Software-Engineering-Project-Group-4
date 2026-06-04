package com.swapcampus.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.exception.ErrorCode;
import com.swapcampus.user.entity.UserBlockEntity;
import com.swapcampus.user.entity.UserMuteEntity;
import com.swapcampus.user.mapper.UserBlockMapper;
import com.swapcampus.user.mapper.UserMuteMapper;
import com.swapcampus.user.service.UserAccountGuard;
import com.swapcampus.user.service.UserModerationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserModerationServiceImpl implements UserModerationService {

    private final UserMuteMapper userMuteMapper;
    private final UserBlockMapper userBlockMapper;
    private final UserAccountGuard userAccountGuard;

    public UserModerationServiceImpl(UserMuteMapper userMuteMapper,
                                     UserBlockMapper userBlockMapper,
                                     UserAccountGuard userAccountGuard) {
        this.userMuteMapper = userMuteMapper;
        this.userBlockMapper = userBlockMapper;
        this.userAccountGuard = userAccountGuard;
    }

    @Override
    public void ensureCanChat(Long userId, Long peerId) {
        userAccountGuard.requireActiveUser(userId);
        if (isMuted(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "您已被禁言，无法发送消息");
        }
        if (isBlocked(peerId, userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "您已被对方拉黑，无法聊天");
        }
    }

    @Override
    public boolean isMuted(Long userId) {
        Long count = userMuteMapper.selectCount(new LambdaQueryWrapper<UserMuteEntity>()
                .eq(UserMuteEntity::getUserId, userId)
                .gt(UserMuteEntity::getMutedUntil, LocalDateTime.now()));
        return count != null && count > 0;
    }

    @Override
    @Transactional
    public void muteUser(Long userId, Long mutedBy, int hours, String reason) {
        UserMuteEntity mute = new UserMuteEntity();
        mute.setUserId(userId);
        mute.setMutedBy(mutedBy);
        mute.setReason(reason);
        mute.setMutedUntil(LocalDateTime.now().plusHours(Math.max(hours, 1)));
        userMuteMapper.insert(mute);
    }

    @Override
    @Transactional
    public void unmuteUser(Long userId) {
        userMuteMapper.delete(new LambdaQueryWrapper<UserMuteEntity>()
                .eq(UserMuteEntity::getUserId, userId)
                .gt(UserMuteEntity::getMutedUntil, LocalDateTime.now()));
    }

    @Override
    public LocalDateTime getActiveMuteUntil(Long userId) {
        List<UserMuteEntity> mutes = userMuteMapper.selectList(new LambdaQueryWrapper<UserMuteEntity>()
                .eq(UserMuteEntity::getUserId, userId)
                .gt(UserMuteEntity::getMutedUntil, LocalDateTime.now())
                .orderByDesc(UserMuteEntity::getMutedUntil)
                .last("OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY"));
        if (mutes.isEmpty()) {
            return null;
        }
        return mutes.get(0).getMutedUntil();
    }

    private boolean isBlocked(Long blockerId, Long blockedId) {
        Long count = userBlockMapper.selectCount(new LambdaQueryWrapper<UserBlockEntity>()
                .eq(UserBlockEntity::getBlockerId, blockerId)
                .eq(UserBlockEntity::getBlockedId, blockedId));
        return count != null && count > 0;
    }
}
