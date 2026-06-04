package com.swapcampus.chat.controller;

import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.common.storage.MinioStorageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatUploadController {

    private final MinioStorageService minioStorageService;

    public ChatUploadController(MinioStorageService minioStorageService) {
        this.minioStorageService = minioStorageService;
    }

    @PostMapping("/upload/image")
    public ApiResponse<Map<String, String>> uploadImage(@RequestPart("file") MultipartFile file) {
        String url = minioStorageService.uploadChatImage(file);
        return ApiResponse.ok(Map.of("url", url));
    }
}
