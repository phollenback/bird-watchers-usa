package com.birds.bird_app.config;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.birds.bird_app.service.S3Service;
import software.amazon.awssdk.services.s3.S3Client;

@Service
@Primary
public class TestS3Service extends S3Service {
    public TestS3Service() {
        super(S3Client.builder().build(), "test-bucket", "test-region", "test-endpoint");
    }

    @Override
    public String uploadFile(MultipartFile file) {
        return "https://test-bucket.s3.amazonaws.com/test-image.jpg";
    }
} 