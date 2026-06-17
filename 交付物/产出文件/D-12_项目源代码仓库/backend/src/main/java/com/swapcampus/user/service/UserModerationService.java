package com.swapcampus.user.service;

import java.time.LocalDateTime;

public interface UserModerationService {

    void ensureCanChat(Long userId, Long peerId);

    boolean isMuted(Long userId);

    void muteUser(Long userId, Long mutedBy, int hours, String reason);

    void unmuteUser(Long userId);

    LocalDateTime getActiveMuteUntil(Long userId);
}
