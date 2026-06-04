package com.swapcampus.common.storage;

import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.exception.ErrorCode;
import com.swapcampus.common.moderation.ContentModerationService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class MinioStorageService {

    private static final Logger log = LoggerFactory.getLogger(MinioStorageService.class);

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final ContentModerationService contentModerationService;

    public MinioStorageService(MinioClient minioClient,
                                 MinioProperties minioProperties,
                                 ContentModerationService contentModerationService) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
        this.contentModerationService = contentModerationService;
    }

    @PostConstruct
    void ensureBucketReady() {
        String bucket = minioProperties.getBucket();
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Created MinIO bucket: {}", bucket);
            }
            applyChatReadPolicy(bucket);
        } catch (Exception ex) {
            log.warn("MinIO bucket initialization skipped: {}", ex.getMessage());
        }
    }

    public String uploadChatImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择图片");
        }
        String originalName = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
        if (originalName.toLowerCase().contains("blocked")) {
            contentModerationService.checkImageUrl("blocked");
        }
        String filename = UUID.randomUUID() + "_" + originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String objectKey = minioProperties.getChatPrefix() + "/" + filename;
        String contentType = StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream";
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectKey)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );
            contentModerationService.checkImageUrl(filename);
            return buildPublicUrl(objectKey);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "图片上传失败，请确认 MinIO 服务已启动");
        }
    }

    private String buildPublicUrl(String objectKey) {
        String base = minioProperties.getPublicEndpoint().replaceAll("/+$", "");
        return base + "/" + minioProperties.getBucket() + "/" + objectKey;
    }

    private void applyChatReadPolicy(String bucket) throws Exception {
        String prefix = minioProperties.getChatPrefix();
        String policy = """
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": {"AWS": ["*"]},
                      "Action": ["s3:GetObject"],
                      "Resource": ["arn:aws:s3:::%s/%s/*"]
                    }
                  ]
                }
                """.formatted(bucket, prefix);
        minioClient.setBucketPolicy(
                SetBucketPolicyArgs.builder().bucket(bucket).config(policy).build()
        );
    }
}
