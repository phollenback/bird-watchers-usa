package com.birds.bird_app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AmazonClient {
    
    @Value("${cloud.aws.accessKey}")
    private String accessKey;

    @Value("${cloud.aws.secretKey}")
    private String secretKey;
    
    @Value("${cloud.aws.bucketName}")
    private String bucketName;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        return S3Client.builder()
            .region(Region.US_WEST_2) // Change this to your region
            .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
            .build();
    }
}
