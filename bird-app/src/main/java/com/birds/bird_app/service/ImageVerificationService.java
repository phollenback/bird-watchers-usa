package com.birds.bird_app.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImageVerificationService {
    private static final Logger logger = LoggerFactory.getLogger(ImageVerificationService.class);

    @Value("${GOOGLE_APPLICATION_CREDENTIALS}")
    private String credentialsPath;

    public boolean isBirdImage(MultipartFile file) throws IOException {
        logger.info("Starting image verification for file: {}", file.getOriginalFilename());
        logger.info("File size: {} bytes, Content type: {}", file.getSize(), file.getContentType());
        logger.info("Using Google Cloud credentials from: {}", credentialsPath);

        try {
            // Load credentials from file
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath));
            logger.info("Successfully loaded Google Cloud credentials");

            // Create the Vision API client with explicit credentials
            ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();
            
            ImageAnnotatorClient vision = ImageAnnotatorClient.create(settings);
            logger.info("Successfully created Vision API client");

            // Convert the image to bytes
            ByteString imgBytes = ByteString.copyFrom(file.getBytes());
            logger.info("Successfully converted image to bytes");

            // Build the image
            Image img = Image.newBuilder().setContent(imgBytes).build();
            logger.info("Successfully built image for Vision API");

            // Perform label detection
            Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feat)
                    .setImage(img)
                    .build();
            logger.info("Sending request to Google Cloud Vision API");

            // Get the response
            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(List.of(request));
            List<AnnotateImageResponse> responses = response.getResponsesList();

            if (responses.isEmpty()) {
                logger.warn("No response received from Vision API");
                return false;
            }

            // Check if any of the labels are related to birds
            List<String> birdRelatedTerms = List.of(
                "bird", "avian", "fowl", "feather", "wing", "beak", "bird of prey",
                "songbird", "waterfowl", "seabird", "raptor", "owl", "eagle", "hawk",
                "falcon", "parrot", "penguin", "duck", "goose", "swan", "chicken",
                "turkey", "pigeon", "dove", "sparrow", "finch", "cardinal", "robin",
                "blue jay", "woodpecker", "hummingbird"
            );

            logger.info("Analyzing labels from Vision API response");
            for (EntityAnnotation annotation : responses.get(0).getLabelAnnotationsList()) {
                String label = annotation.getDescription().toLowerCase();
                float score = annotation.getScore();
                logger.info("Found label: {} with confidence: {}", label, score);
                
                if (birdRelatedTerms.stream().anyMatch(term -> label.contains(term))) {
                    logger.info("Bird-related term found: {}", label);
                    return true;
                }
            }

            logger.warn("No bird-related terms found in the image");
            return false;
        } catch (Exception e) {
            logger.error("Error during image verification: {}", e.getMessage(), e);
            throw e;
        }
    }
} 