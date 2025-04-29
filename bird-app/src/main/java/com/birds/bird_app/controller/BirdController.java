package com.birds.bird_app.controller;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.birds.bird_app.model.BirdEntity;
import com.birds.bird_app.service.BirdService;
import com.birds.bird_app.service.ImageVerificationService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/birds")
public class BirdController {
    private final BirdService birdService;
    private final ImageVerificationService imageVerificationService;
    private static final Logger logger = LoggerFactory.getLogger(BirdController.class);

    @Autowired
    public BirdController(BirdService birdService, ImageVerificationService imageVerificationService) {
        this.birdService = birdService;
        this.imageVerificationService = imageVerificationService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("birds", birdService.getAllBirds());
        return "birds/index";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("bird", new BirdEntity());
        return "birds/create";
    }

    @PostMapping
    public String createBird(
        @ModelAttribute("bird") @Valid BirdEntity bird,
        BindingResult result,
        @RequestParam("image") MultipartFile file,
        RedirectAttributes redirectAttributes
    ) {
        try {
            // Validate input
            if (result.hasErrors()) {
                List<FieldError> errors = result.getFieldErrors();
                for (FieldError error : errors) {
                    redirectAttributes.addFlashAttribute(error.getField() + "Error", error.getDefaultMessage());
                }
                return "redirect:/birds/new";
            }

            // Validate file
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select a file to upload");
                return "redirect:/birds/new";
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("error", "Only image files are allowed");
                return "redirect:/birds/new";
            }
            
            // Check if it's a bird image
            if (!imageVerificationService.isBirdImage(file)) {
                redirectAttributes.addFlashAttribute("error", "🐦 Oops! That's not a bird! " +
                    "I'm a bird expert, and I can tell that's not a bird. " +
                    "Please upload a picture of a bird - we're bird watchers, not logo watchers! 🦅");
                return "redirect:/birds/new";
            }

            // Save the bird with image through the service layer
            birdService.createBird(bird, file);
            
            return "redirect:/birds";
        } catch (IOException e) {
            logger.error("Error creating bird: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to upload: " + e.getMessage());
            return "redirect:/birds/new";
        }
    }
}