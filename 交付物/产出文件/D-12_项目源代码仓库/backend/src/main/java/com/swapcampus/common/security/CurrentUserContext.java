package com.swapcampus.common.security;

import com.swapcampus.common.enums.Role;
import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class CurrentUserContext {

    private CurrentUserContext() {
    }

    public static Optional<CurrentUserPrincipal> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CurrentUserPrincipal currentUser) {
            return Optional.of(currentUser);
        }
        return Optional.empty();
    }

    public static Long requireUserId() {
        return currentUser()
                .map(CurrentUserPrincipal::getUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }

    public static Optional<Long> currentUserId() {
        return currentUser().map(CurrentUserPrincipal::getUserId);
    }

    public static Optional<String> currentUsername() {
        return currentUser().map(CurrentUserPrincipal::getName);
    }

    public static Optional<Role> currentRole() {
        return currentUser().map(CurrentUserPrincipal::getRole);
    }
}
