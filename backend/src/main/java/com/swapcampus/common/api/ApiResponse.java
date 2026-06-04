package com.swapcampus.common.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.swapcampus.common.exception.ErrorCode;

import java.util.Collections;

public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return of(ErrorCode.OK.getCode(), ErrorCode.OK.getMessage(), data);
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        return of(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return of(code, message, null);
    }

    public static <T> ApiResponse<T> of(int code, String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonIgnore
    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @JsonProperty("data")
    public Object getResponseData() {
        return data == null ? Collections.emptyMap() : data;
    }
}
