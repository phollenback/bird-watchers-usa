package com.birds.bird_app.service;

import com.birds.bird_app.model.BirdSpecies;
import com.birds.bird_app.repository.BirdSpeciesRepository;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ImageVerificationService {
    private static final Logger logger = LoggerFactory.getLogger(ImageVerificationService.class);
    private static final float CONFIDENCE_THRESHOLD = 0.6f;

    @Autowired
    private BirdSpeciesRepository birdSpeciesRepository;

    public boolean isBirdImage(MultipartFile file) throws IOException {
        logger.info("Starting image verification for file: {}", file.getOriginalFilename());
        logger.info("File size: {} bytes, Content type: {}", file.getSize(), file.getContentType());

        try {
            List<EntityAnnotation> annotations = getVisionApiResponse(file);
            
            // Log the analysis results
            logger.info("=== Vision API Label Analysis ===");
            logger.info("Total labels detected: {}", annotations.size());
            logger.info("Labels by confidence score:");
            
            boolean isBird = false;
            for (EntityAnnotation annotation : annotations) {
                String label = annotation.getDescription();
                float confidence = annotation.getScore();
                String birdIndicator = isBirdRelated(label) ? "🦅 [BIRD MATCH]" : "";
                logger.info("{} Label: '{}', Confidence: {:.2f}%", 
                    birdIndicator, label, confidence * 100);
                
                if (isBirdRelated(label) && confidence >= CONFIDENCE_THRESHOLD) {
                    isBird = true;
                }
            }
            
            logger.info("=== Analysis Summary ===");
            logger.info("{} Bird detected in image!", isBird ? "✅" : "❌");
            logger.info("=====================");
            
            return isBird;
        } catch (Exception e) {
            logger.error("Error during image verification: {}", e.getMessage(), e);
            return false;
        }
    }

    private List<EntityAnnotation> getVisionApiResponse(MultipartFile file) throws IOException {
        logger.info("Sending request to Google Cloud Vision API");
        
        // Load credentials from the classpath resource
        GoogleCredentials credentials;
        try (InputStream credentialsStream = getClass().getResourceAsStream("/silicon-window-458303-i9-0ab54f3503d1.json")) {
            if (credentialsStream == null) {
                throw new IOException("Could not find credentials file in classpath");
            }
            credentials = GoogleCredentials.fromStream(credentialsStream);
            logger.info("Successfully loaded credentials from classpath");
        }

        // Create the ImageAnnotatorSettings with explicit credentials
        ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
            .setCredentialsProvider(() -> credentials)
            .build();
        
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create(settings)) {
            ByteString imgBytes = ByteString.copyFrom(file.getBytes());
            Image img = Image.newBuilder().setContent(imgBytes).build();
            Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feat)
                    .setImage(img)
                    .build();

            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(List.of(request));
            List<AnnotateImageResponse> responses = response.getResponsesList();

            if (responses.isEmpty()) {
                return List.of();
            }
            
            return responses.get(0).getLabelAnnotationsList();
        }
    }

    private boolean isBirdRelated(String label) {
        String lowerLabel = label.toLowerCase();
        return lowerLabel.contains("bird") || 
               lowerLabel.contains("eagle") || 
               lowerLabel.contains("hawk") || 
               lowerLabel.contains("owl") || 
               lowerLabel.contains("jay") || 
               lowerLabel.contains("sparrow") || 
               lowerLabel.contains("finch") || 
               lowerLabel.contains("warbler") || 
               lowerLabel.contains("thrush") || 
               lowerLabel.contains("wren") || 
               lowerLabel.contains("beak") || 
               lowerLabel.contains("wing") || 
               lowerLabel.contains("feather");
    }

    public List<Map<String, Object>> getTopBirdLabels(MultipartFile file) throws IOException {
        List<Map<String, Object>> birdLabels = new ArrayList<>();
        
        // Get the Vision API response
        List<EntityAnnotation> annotations = getVisionApiResponse(file);
        
        // Create a map to store potential bird matches with their scores
        Map<BirdSpecies, Float> potentialMatches = new HashMap<>();
        
        // Process each annotation
        for (EntityAnnotation annotation : annotations) {
            String label = annotation.getDescription().toLowerCase();
            float confidence = annotation.getScore();
            
            // Skip very general labels that could match many birds
            if (isGeneralBirdLabel(label)) {
                continue;
            }
            
            // Search for matching bird species in our database
            List<BirdSpecies> matches = birdSpeciesRepository.findBySearchTerm(label);
            
            // If no matches found and this looks like a specific bird name, create a new entry
            if (matches.isEmpty() && isSpecificBirdName(label)) {
                BirdSpecies newSpecies = new BirdSpecies();
                newSpecies.setCommonName(capitalizeWords(label));
                newSpecies.setScientificName("Unknown"); // We'll need to look this up later
                newSpecies.setDescription("A bird species detected by our image recognition system.");
                newSpecies.setCharacteristics(new HashSet<>(List.of(label)));
                newSpecies.setHabitat("Unknown");
                newSpecies.setRegion("Unknown");
                
                // Save the new species
                newSpecies = birdSpeciesRepository.save(newSpecies);
                logger.info("Added new bird species to database: {}", newSpecies.getCommonName());
                
                // Add it to our matches
                matches = List.of(newSpecies);
            }
            
            // Add matches to our potential matches map
            for (BirdSpecies species : matches) {
                // Calculate a weighted score based on the match type
                float weightedScore = calculateWeightedScore(species, label, confidence);
                potentialMatches.merge(species, weightedScore, Float::max);
            }
        }
        
        // If we have matches, return them sorted by confidence
        if (!potentialMatches.isEmpty()) {
            return potentialMatches.entrySet().stream()
                .sorted(Map.Entry.<BirdSpecies, Float>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Map<String, Object> result = new HashMap<>();
                    BirdSpecies species = entry.getKey();
                    result.put("label", species.getCommonName());
                    result.put("scientificName", species.getScientificName());
                    result.put("confidence", Math.round(entry.getValue() * 100));
                    result.put("description", species.getDescription());
                    result.put("characteristics", species.getCharacteristics());
                    result.put("habitat", species.getHabitat());
                    result.put("region", species.getRegion());
                    return result;
                })
                .collect(Collectors.toList());
        }
        
        // If no matches found but we detected a bird, create a generic bird entry
        boolean isBird = annotations.stream()
            .anyMatch(annotation -> isBirdRelated(annotation.getDescription()) && 
                                  annotation.getScore() >= CONFIDENCE_THRESHOLD);
        
        if (isBird) {
            Map<String, Object> genericBird = new HashMap<>();
            genericBird.put("label", "Unidentified Bird");
            genericBird.put("scientificName", "Aves (Class)");
            genericBird.put("confidence", 100);
            genericBird.put("description", "This image contains a bird, but we couldn't identify the specific species.");
            
            // Extract potential characteristics from the detected labels
            List<String> detectedLabels = annotations.stream()
                .map(EntityAnnotation::getDescription)
                .filter(label -> !isGeneralBirdLabel(label.toLowerCase()))
                .collect(Collectors.toList());
            
            genericBird.put("characteristics", detectedLabels);
            genericBird.put("habitat", "Unknown");
            genericBird.put("region", "Unknown");
            genericBird.put("isGenericBird", true);
            
            birdLabels.add(genericBird);
        }
        
        return birdLabels;
    }

    private boolean isGeneralBirdLabel(String label) {
        return label.equals("bird") || 
               label.equals("beak") || 
               label.equals("wing") || 
               label.equals("feather") || 
               label.equals("wildlife");
    }

    private float calculateWeightedScore(BirdSpecies species, String label, float baseConfidence) {
        float score = baseConfidence;
        boolean hasGeneralMatch = false;
        
        // Check for general matches first
        if (species.getCommonName().toLowerCase().equals(label)) {
            score *= 2.0f;
            hasGeneralMatch = true;
        } else if (species.getScientificName().toLowerCase().equals(label)) {
            score *= 1.8f;
            hasGeneralMatch = true;
        } else if (species.getAliases().stream().anyMatch(alias -> alias.toLowerCase().equals(label))) {
            score *= 1.5f;
            hasGeneralMatch = true;
        }
        
        // Only consider characteristics if we have a general match
        if (hasGeneralMatch && species.getCharacteristics().stream().anyMatch(characteristic -> 
            label.contains(characteristic.toLowerCase()))) {
            score *= 1.1f;
        }
        
        return Math.min(score, 1.0f); // Cap at 1.0
    }

    private boolean isSpecificBirdName(String label) {
        // Check if the label looks like a specific bird name
        // This is a simple heuristic - we could make this more sophisticated
        String[] words = label.split("\\s+");
        if (words.length < 2) return false; // Single words are usually too general
        
        // Check for common bird name patterns
        return label.contains("booby") ||
               label.contains("gannet") ||
               label.contains("eagle") ||
               label.contains("hawk") ||
               label.contains("owl") ||
               label.contains("jay") ||
               label.contains("sparrow") ||
               label.contains("finch") ||
               label.contains("warbler") ||
               label.contains("thrush") ||
               label.contains("wren");
    }

    private String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        
        for (char c : text.toCharArray()) {
            if (Character.isSpaceChar(c) || c == '-' || c == '.') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                c = Character.toUpperCase(c);
                capitalizeNext = false;
            }
            result.append(c);
        }
        
        return result.toString();
    }
} 