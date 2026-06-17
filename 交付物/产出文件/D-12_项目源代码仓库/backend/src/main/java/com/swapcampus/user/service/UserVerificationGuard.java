package com.swapcampus.user.service;

public interface UserVerificationGuard {

    void requireVerifiedStudent(Long userId);

    boolean isVerifiedStudent(Long userId);
}
