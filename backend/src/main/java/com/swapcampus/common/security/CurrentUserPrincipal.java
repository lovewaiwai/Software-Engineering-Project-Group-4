package com.swapcampus.common.security;

import com.swapcampus.common.enums.Role;

import java.security.Principal;

public class CurrentUserPrincipal implements Principal {

    private final Long userId;
    private final String username;
    private final Role role;

    public CurrentUserPrincipal(Long userId, String username, Role role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public String getName() {
        return username;
    }

    public Role getRole() {
        return role;
    }
}
