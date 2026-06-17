package com.swapcampus.user.service;

import com.swapcampus.common.api.PageResponse;
import com.swapcampus.user.dto.UserProfileUpdateRequest;
import com.swapcampus.user.dto.UserStudentVerifyRequest;
import com.swapcampus.user.vo.CreditRecordResponse;
import com.swapcampus.user.vo.UserResponse;

public interface UserService {

    UserResponse getCurrentUser();

    UserResponse getUserById(Long userId);

    UserResponse updateCurrentUserProfile(UserProfileUpdateRequest request);

    UserResponse verifyStudent(UserStudentVerifyRequest request);

    PageResponse<CreditRecordResponse> getCreditRecords(long page, long pageSize);
}
