package com.birds.bird_app.controller;

import java.io.IOException;
import java.util.List;

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

import jakarta.validation.Valid;

@Controller
@RequestMapping("/birds")
public class BirdController {
    private final BirdService birdService;

    @Autowired
    public BirdController(BirdService birdService) {
        this.birdService = birdService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("birds", birdService.getAllBirds());
        model.addAttribute("bird", new BirdEntity());
        return "birds/index";
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
                return "redirect:/birds";
            }

            // Validate file
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select a file to upload");
                return "redirect:/birds";
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("error", "Only image files are allowed");
                return "redirect:/birds";
            }
            
            // Save the bird with image through the service layer
            birdService.createBird(bird, file);
            
            return "redirect:/birds";
        } catch (IOException e) {
            if (e.getMessage().contains("not a bird")) {
                redirectAttributes.addFlashAttribute("error", "🐦 Oops! That's not a bird! " +
                    "I'm a bird expert, and I can tell that's not a bird. " +
                    "Please upload a picture of a bird - we're bird watchers, not logo watchers! 🦅");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to upload: " + e.getMessage());
            }
            return "redirect:/birds";
        }
    }
}