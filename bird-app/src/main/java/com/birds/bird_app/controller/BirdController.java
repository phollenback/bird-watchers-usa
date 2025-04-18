package com.birds.bird_app.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.birds.bird_app.model.BirdModel;
import com.birds.bird_app.service.BirdService;

import jakarta.validation.Valid;
import org.owasp.encoder.Encode;

@Controller
public class BirdController {
    
    private static final String UPLOAD_DIRECTORY = System.getProperty("user.dir") + "/uploads";
    
    @Autowired
    private BirdService birdService;

    @RequestMapping("/birds")
    public String getBirds(Model model) {
        List<BirdModel> birds = birdService.getAllBirds();
        model.addAttribute("birds", birds);
        model.addAttribute("bird", new BirdModel());
        return "birds";
    }

    @RequestMapping(value = "/birds", method = RequestMethod.POST)
    public String uploadImage(
        @ModelAttribute("bird") @Valid BirdModel bird,
        BindingResult result,
        ModelMap model,
        @RequestParam("image") MultipartFile file, 
        RedirectAttributes redirectAttributes) 
    {
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
                redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
                return "redirect:/birds";
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("message", "Only image files are allowed");
                return "redirect:/birds";
            }

            // Sanitize filename
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.contains("..")) {
                redirectAttributes.addFlashAttribute("message", "Invalid filename");
                return "redirect:/birds";
            }

            // Create uploads directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIRECTORY);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String filename = UUID.randomUUID().toString() + "_" + 
                originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
            Path filePath = Paths.get(UPLOAD_DIRECTORY, filename);
            
            // Save the file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            

            // Sanitize inputs ******************************************************
            // Encode all bird inputs
            bird.setName(Encode.forHtml(bird.getName()));
            bird.setKind(Encode.forHtml(bird.getKind()));
            bird.setColor(Encode.forHtml(bird.getColor()));
            bird.setFunFact(Encode.forHtml(bird.getFunFact()));
            
            // Set the image URL in the model
            bird.setImageUrl("/uploads/" + Encode.forUriComponent(filename));
            
            // Save the bird data through the service layer
            birdService.createBird(bird);
            
            redirectAttributes.addFlashAttribute("message", "Bird added successfully!");
            return "redirect:/birds";
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", 
                "Failed to upload: " + Encode.forHtml(file.getOriginalFilename()));
        }
        
        return "redirect:/birds";
    }
}