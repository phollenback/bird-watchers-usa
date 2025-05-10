package com.birds.bird_app.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.birds.bird_app.model.BirdEntity;
import com.birds.bird_app.service.BirdService;
import com.birds.bird_app.service.ImageService;
import com.birds.bird_app.service.ImageVerificationService;
import com.birds.bird_app.service.UserActivityService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.birds.bird_app.model.UserEntity;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/birds")
public class BirdController {
    private final BirdService birdService;
    private final ImageService imageService;
    private final ImageVerificationService imageVerificationService;
    private final UserActivityService userActivityService;
    private static final Logger logger = LoggerFactory.getLogger(BirdController.class);

    @Autowired
    public BirdController(BirdService birdService, 
                         ImageService imageService, 
                         ImageVerificationService imageVerificationService,
                         UserActivityService userActivityService) {
        this.birdService = birdService;
        this.imageService = imageService;
        this.imageVerificationService = imageVerificationService;
        this.userActivityService = userActivityService;
    }

    @GetMapping
    public String index(Model model, @AuthenticationPrincipal UserEntity currentUser, HttpSession session) {
        if (currentUser != null) {
            session.setAttribute("user", currentUser);
        }
        model.addAttribute("birds", birdService.getAllBirds());
        return "birds/index";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, @AuthenticationPrincipal UserEntity currentUser, HttpSession session) {
        if (currentUser != null) {
            session.setAttribute("user", currentUser);
        }
        model.addAttribute("bird", new BirdEntity());
        model.addAttribute("showClassification", false);
        return "birds/create";
    }

    @PostMapping("/create")
    public String createBird(@ModelAttribute BirdEntity bird, 
                           @RequestParam("image") MultipartFile image,
                           @AuthenticationPrincipal UserEntity currentUser,
                           Model model) {
        try {
            // Verify if the image contains a bird
            if (!imageVerificationService.isBirdImage(image)) {
                model.addAttribute("error", "Could not identify any birds in the image. Please try again.");
                model.addAttribute("bird", bird);
                model.addAttribute("showClassification", false);
                return "birds/create";
            }

            // Get bird classification results
            List<Map<String, Object>> birdLabels = imageVerificationService.getTopBirdLabels(image);
            
            if (birdLabels.isEmpty()) {
                model.addAttribute("error", "Could not identify any birds in the image. Please try again.");
                model.addAttribute("bird", bird);
                model.addAttribute("showClassification", false);
                return "birds/create";
            }

            // Save the image and get the URL
            String imageUrl = imageService.saveImage(image);
            bird.setImageUrl(imageUrl);

            // Save the bird
            BirdEntity savedBird = birdService.createBird(bird, image, currentUser);

            // Structure the classification results
            Map<String, Object> classificationResults = new HashMap<>();
            List<Map<String, String>> characteristics = new ArrayList<>();
            List<String> additionalInfo = new ArrayList<>();

            // Process the first bird label (most confident match)
            if (!birdLabels.isEmpty()) {
                Map<String, Object> topMatch = birdLabels.get(0);
                
                // Add characteristics
                if (topMatch.containsKey("characteristics")) {
                    @SuppressWarnings("unchecked")
                    Collection<String> chars = (Collection<String>) topMatch.get("characteristics");
                    for (String characteristic : chars) {
                        Map<String, String> charMap = new HashMap<>();
                        charMap.put("name", characteristic);
                        charMap.put("value", "Detected");
                        characteristics.add(charMap);
                    }
                }

                // Add additional info
                if (topMatch.containsKey("description")) {
                    additionalInfo.add((String) topMatch.get("description"));
                }
                if (topMatch.containsKey("habitat")) {
                    additionalInfo.add("Habitat: " + topMatch.get("habitat"));
                }
                if (topMatch.containsKey("region")) {
                    additionalInfo.add("Region: " + topMatch.get("region"));
                }
            }

            classificationResults.put("characteristics", characteristics);
            classificationResults.put("additionalInfo", additionalInfo);

            // Add success message and classification results to the model
            model.addAttribute("success", "Bird successfully uploaded!");
            model.addAttribute("showClassification", true);
            model.addAttribute("birdLabels", birdLabels);
            model.addAttribute("classificationResults", classificationResults);
            model.addAttribute("bird", savedBird);

            return "birds/create";
        } catch (Exception e) {
            model.addAttribute("error", "Error uploading bird: " + e.getMessage());
            model.addAttribute("bird", bird);
            model.addAttribute("showClassification", false);
            return "birds/create";
        }
    }

    private String getScientificName(String commonName) {
        // This is a placeholder - in a real application, you would have a mapping
        // of common names to scientific names, possibly from a database
        return "Scientific name for " + commonName;
    }
}