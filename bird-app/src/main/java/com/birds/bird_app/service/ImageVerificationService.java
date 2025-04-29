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
import java.util.*;

@Service
public class ImageVerificationService {
    private static final Logger logger = LoggerFactory.getLogger(ImageVerificationService.class);

    @Value("${GOOGLE_APPLICATION_CREDENTIALS}")
    private String credentialsPath;

    // Basic list of bird-related terms
    private static final List<String> BIRD_TERMS = List.of(
        "bird", "avian", "fowl", "feather", "wing", "beak", "tail",
        "bird of prey", "songbird", "waterfowl", "seabird", "raptor",
        "owl", "eagle", "hawk", "falcon", "parrot", "penguin", "duck", "goose", "swan",
        "chicken", "turkey", "pigeon", "dove", "sparrow", "finch", "cardinal", "robin",
        "blue jay", "woodpecker", "hummingbird", "warbler", "thrush", "wren", "nuthatch",
        "chickadee", "titmouse", "grosbeak", "tanager", "oriole", "blackbird", "starling",
        "crow", "raven", "jay", "magpie", "kingfisher", "heron", "egret", "crane", "stork"
    );

    public boolean isBirdImage(MultipartFile file) throws IOException {
        logger.info("Starting image verification for file: {}", file.getOriginalFilename());
        logger.info("File size: {} bytes, Content type: {}", file.getSize(), file.getContentType());

        try {
            // Load credentials from file
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath));

            // Create the Vision API client with explicit credentials
            ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();
            
            ImageAnnotatorClient vision = ImageAnnotatorClient.create(settings);
            logger.info("Successfully created Vision API client");

            // Convert the image to bytes
            ByteString imgBytes = ByteString.copyFrom(file.getBytes());

            // Build the image
            Image img = Image.newBuilder().setContent(imgBytes).build();

            // Perform label detection with maxResults of 30
            Feature feat = Feature.newBuilder()
                    .setType(Feature.Type.LABEL_DETECTION)
                    .setMaxResults(30)
                    .build();

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

            // Log all labels and check for bird-related terms
            boolean foundBird = false;
            logger.info("=== Vision API Label Analysis ===");
            logger.info("Total labels detected: {}", responses.get(0).getLabelAnnotationsList().size());
            
            // Sort labels by score
            List<EntityAnnotation> sortedLabels = new ArrayList<>(responses.get(0).getLabelAnnotationsList());
            sortedLabels.sort((a, b) -> Float.compare(b.getScore(), a.getScore()));

            // Log all labels with their confidence scores
            logger.info("Labels by confidence score:");
            for (EntityAnnotation annotation : sortedLabels) {
                String label = annotation.getDescription().toLowerCase();
                float score = annotation.getScore();
                float confidence = score * 100; // Convert to percentage
                
                // Check if this label matches any bird terms
                boolean isBirdTerm = BIRD_TERMS.stream().anyMatch(term -> label.contains(term));
                
                if (isBirdTerm) {
                    logger.info("🦅 [BIRD MATCH] Label: '{}', Confidence: {}%", label, String.format("%.2f", confidence));
                    foundBird = true;
                } else {
                    logger.info("Label: '{}', Confidence: {}%", label, String.format("%.2f", confidence));
                }
            }

            // Log summary
            logger.info("=== Analysis Summary ===");
            if (foundBird) {
                logger.info("✅ Bird detected in image!");
            } else {
                logger.warn("❌ No bird-related terms found in the image");
            }
            logger.info("=====================");

            return foundBird;
        } catch (Exception e) {
            logger.error("Error during image verification: {}", e.getMessage(), e);
            throw e;
        }
    }
} 