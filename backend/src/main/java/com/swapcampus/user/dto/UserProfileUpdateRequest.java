package com.swapcampus.user.dto;

import jakarta.validation.constraints.Size;

public class UserProfileUpdateRequest {

    @Size(max = 50, message = "姓名不能超过50个字符")
    private String realName;

    @Size(max = 80, message = "学院不能超过80个字符")
    private String college;

    @Size(max = 20, message = "年级不能超过20个字符")
    private String grade;

    @Size(max = 500, message = "头像链接不能超过500个字符")
    private String avatarUrl;

    @Size(max = 500, message = "个人简介不能超过500个字符")
    private String bio;

    @Size(max = 100, message = "联系方式不能超过100个字符")
    private String contactMasked;

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
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

    public String getContactMasked() {
        return contactMasked;
    }

    public void setContactMasked(String contactMasked) {
        this.contactMasked = contactMasked;
    }
}