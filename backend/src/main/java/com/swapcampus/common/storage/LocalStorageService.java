package com.swapcampus.common.storage;

import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.exception.ErrorCode;
import com.swapcampus.common.moderation.ContentModerationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalStorageService {

    private final ContentModerationService contentModerationService;
    private final Path uploadRoot;

    public LocalStorageService(ContentModerationService contentModerationService,
                               @Value("${swapcampus.upload.dir:uploads}") String uploadDir) {
        this.contentModerationService = contentModerationService;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String uploadChatImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择图片");
        }
        String originalName = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
        if (originalName.toLowerCase().contains("blocked")) {
            contentModerationService.checkImageUrl("blocked");
        }
        try {
            Files.createDirectories(uploadRoot);
            String filename = UUID.randomUUID() + "_" + originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path target = uploadRoot.resolve(filename);
            file.transferTo(target);
            contentModerationService.checkImageUrl(filename);
            return "/uploads/" + filename;
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "图片上传失败");
        }
    }
}
