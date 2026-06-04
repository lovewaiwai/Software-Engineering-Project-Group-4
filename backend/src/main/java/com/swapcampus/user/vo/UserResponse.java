package com.swapcampus.user.vo;

import com.swapcampus.common.enums.Role;
import com.swapcampus.common.enums.UserStatus;
import com.swapcampus.user.entity.UserEntity;
import com.swapcampus.user.entity.UserProfileEntity;

import java.time.LocalDateTime;

public class UserResponse {

    private Long id;
    private String username;
    private String phone;
    private String email;
    private Role role;
    private UserStatus status;
    private Integer creditScore;
    private Integer pointBalance;
    private LocalDateTime createdAt;
    private Profile profile;
    private String module;
    private String moduleStatus;

    public static UserResponse placeholder(String module) {
        UserResponse response = new UserResponse();
        response.setModule(module);
        response.setModuleStatus("TODO");
        return response;
    }

    public static UserResponse from(UserEntity user, UserProfileEntity profile) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setCreditScore(user.getCreditScore());
        response.setPointBalance(user.getPointBalance());
        response.setCreatedAt(user.getCreatedAt());
        if (profile != null) {
            response.setProfile(Profile.from(profile));
        }
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Integer getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }

    public Integer getPointBalance() {
        return pointBalance;
    }

    public void setPointBalance(Integer pointBalance) {
        this.pointBalance = pointBalance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getModuleStatus() {
        return moduleStatus;
    }

    public void setModuleStatus(String moduleStatus) {
        this.moduleStatus = moduleStatus;
    }

    public static class Profile {

        private String realName;
        private String studentNo;
        private String college;
        private String grade;
        private String avatarUrl;
        private String bio;
        private LocalDateTime verifiedAt;
        private String contactMasked;

        public static Profile from(UserProfileEntity entity) {
            Profile profile = new Profile();
            profile.setRealName(entity.getRealName());
            profile.setStudentNo(entity.getStudentNo());
            profile.setCollege(entity.getCollege());
            profile.setGrade(entity.getGrade());
            profile.setAvatarUrl(entity.getAvatarUrl());
            profile.setBio(entity.getBio());
            profile.setVerifiedAt(entity.getVerifiedAt());
            profile.setContactMasked(entity.getContactMasked());
            return profile;
        }

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }

        public String getStudentNo() {
            return studentNo;
        }

        public void setStudentNo(String studentNo) {
            this.studentNo = studentNo;
        }

        public String getCollege() {
            return college;
        }

        public void setCollege(String college) {
            this.college = college;
        }

        public String getGrade() {
            return grade;
        }

        public void setGrade(String grade) {
            this.grade = grade;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        public String getBio() {
            return bio;
        }

        public void setBio(String bio) {
            this.bio = bio;
        }

        public LocalDateTime getVerifiedAt() {
            return verifiedAt;
        }

        public void setVerifiedAt(LocalDateTime verifiedAt) {
            this.verifiedAt = verifiedAt;
        }

        public String getContactMasked() {
            return contactMasked;
        }

        public void setContactMasked(String contactMasked) {
            this.contactMasked = contactMasked;
        }
    }
}
