package com.swapcampus.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "swapcampus.user.verification")
public class UserVerificationProperties {

    private String studentNoPattern = "^[A-Za-z0-9]{5,20}$";

    public String getStudentNoPattern() {
        return studentNoPattern;
    }

    public void setStudentNoPattern(String studentNoPattern) {
        this.studentNoPattern = studentNoPattern;
    }
}