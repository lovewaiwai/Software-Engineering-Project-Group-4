package com.swapcampus.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserStudentVerifyRequest {

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名不能超过50个字符")
    private String realName;

    @NotBlank(message = "学号不能为空")
    @Size(max = 30, message = "学号不能超过30个字符")
    private String studentNo;

    @NotBlank(message = "教务系统密码不能为空")
    @Size(max = 100, message = "教务系统密码不能超过100个字符")
    private String eduPassword;

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

    public String getEduPassword() {
        return eduPassword;
    }

    public void setEduPassword(String eduPassword) {
        this.eduPassword = eduPassword;
    }
}
