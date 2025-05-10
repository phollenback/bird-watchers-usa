package com.birds.bird_app.service;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

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
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

@Service
public class S3Service {
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);
    
    private final S3Client s3Client;
    private final String bucketName;
    private final String accessKey;
    private final String secretKey;
    private final Map<String, String> fileKeyToUrl = new HashMap<>();

    @Autowired
    public S3Service(S3Client s3Client, 
                    @Value("${cloud.aws.bucketName}") String bucketName,
                    @Value("${cloud.aws.accessKey}") String accessKey,
                    @Value("${cloud.aws.secretKey}") String secretKey) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        logger.info("S3Service initialized with bucket: {}", bucketName);
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        return uploadFile(file, fileName);
    }

    public String uploadFile(MultipartFile file, String fileName) throws IOException {
        logger.info("Uploading file: {} to S3 bucket: {}", fileName, bucketName);
        
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .contentType(file.getContentType())
            .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        logger.info("File successfully uploaded to S3");
        return fileName; // Return just the file key
    }

    public String getPresignedUrl(String fileKey) {
        if (fileKey == null) {
            return null;
        }

        // Extract the file key if a full URL is provided
        if (fileKey.startsWith("http")) {
            String[] parts = fileKey.split("/");
            fileKey = parts[parts.length - 1];
            // Remove any query parameters
            if (fileKey.contains("?")) {
                fileKey = fileKey.substring(0, fileKey.indexOf("?"));
            }
        }

        // Check cache first
        String cachedUrl = fileKeyToUrl.get(fileKey);
        if (cachedUrl != null) {
            return cachedUrl;
        }

        // Generate new presigned URL
        try {
            String presignedUrl = generatePresignedUrl(fileKey);
            fileKeyToUrl.put(fileKey, presignedUrl);
            return presignedUrl;
        } catch (Exception e) {
            logger.error("Error generating presigned URL for file: {}", fileKey, e);
            return null;
        }
    }

    private String generatePresignedUrl(String fileKey) {
        logger.info("Generating presigned URL for file: {}", fileKey);
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        S3Presigner presigner = S3Presigner.builder()
            .region(Region.US_WEST_2)
            .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
            .build();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(fileKey)
            .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofDays(7))
            .getObjectRequest(getObjectRequest)
            .build();

        String presignedUrl = presigner.presignGetObject(presignRequest).url().toString();
        logger.info("Generated presigned URL for file: {}", fileKey);
        return presignedUrl;
    }
} 