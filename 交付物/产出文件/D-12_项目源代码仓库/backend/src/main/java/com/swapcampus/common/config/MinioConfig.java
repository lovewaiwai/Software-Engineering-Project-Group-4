package com.swapcampus.common.config;

import com.swapcampus.common.storage.MinioProperties;
import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    @Bean
    public MinioClient minioClient(MinioProperties properties) throws Exception {
        return MinioClient.builder()
                .endpoint(new URL(properties.getEndpoint()))
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }
}
