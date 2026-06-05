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

    @NotBlank(message = "学院不能为空")
    @Size(max = 80, message = "学院不能超过80个字符")
    private String college;

    @NotBlank(message = "年级不能为空")
    @Size(max = 20, message = "年级不能超过20个字符")
    private String grade;

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
}