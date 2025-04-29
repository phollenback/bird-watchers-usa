package com.birds.bird_app.service;

import java.io.IOException;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
public class S3Service {
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);
    
    private final S3Client s3Client;
    private final String bucketName;

    @Autowired
    public S3Service(S3Client s3Client, @Value("${cloud.aws.bucketName}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        logger.info("S3Service initialized with bucket: {}", bucketName);
    }

    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            logger.warn("Attempted to upload null or empty file");
            return null;
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        logger.info("Preparing to upload file: {} to S3 bucket: {}", fileName, bucketName);
        
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

            logger.info("Uploading file to S3...");
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            logger.info("File successfully uploaded to S3");

            // Generate a presigned URL that expires in 7 days
            logger.info("Generating presigned URL for file: {}", fileName);
            S3Presigner presigner = S3Presigner.builder()
                .build();

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofDays(7))
                .getObjectRequest(getObjectRequest)
                .build();

            String presignedUrl = presigner.presignGetObject(presignRequest).url().toString();
            logger.info("Generated presigned URL: {}", presignedUrl);
            return presignedUrl;
            
        } catch (Exception e) {
            logger.error("Error uploading file to S3: {}", e.getMessage(), e);
            throw e;
        }
    }
} 