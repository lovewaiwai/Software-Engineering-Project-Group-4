package com.swapcampus.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "swapcampus.user.verification")
public class UserVerificationProperties {

    private String studentNoPattern = "^[A-Za-z0-9]{8,12}$";
    private List<String> allowedColleges = new ArrayList<>();

    public String getStudentNoPattern() {
        return studentNoPattern;
    }

    public void setStudentNoPattern(String studentNoPattern) {
        this.studentNoPattern = studentNoPattern;
    }

    public List<String> getAllowedColleges() {
        return allowedColleges;
    }

    public void setAllowedColleges(List<String> allowedColleges) {
        this.allowedColleges = allowedColleges;
    }
}