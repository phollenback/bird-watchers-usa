package com.birds.bird_app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.birds.bird_app.model.UserEntity;
import com.birds.bird_app.repository.UserRepository;
import com.birds.bird_app.service.S3Service;
import com.birds.bird_app.service.UserActivityService;

import jakarta.validation.Valid;

import java.io.IOException;
import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    private final UserActivityService userActivityService;

    @Autowired
    public UserController(UserRepository userRepository, 
                         PasswordEncoder passwordEncoder,
                         S3Service s3Service,
                         UserActivityService userActivityService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.s3Service = s3Service;
        this.userActivityService = userActivityService;
    }

    @GetMapping("/loginForm")
    public String showLoginForm() {
        return "users/loginForm";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        logger.info("Showing registration form");
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserEntity());
        }
        return "users/registerForm";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserEntity user, 
                             BindingResult bindingResult, 
                             Model model,
                             RedirectAttributes redirectAttributes) {
        logger.info("Attempting to register user: {}", user.getEmail());
        
        try {
            // Validate input
            if (bindingResult.hasErrors()) {
                logger.warn("Registration validation errors: {}", bindingResult.getAllErrors());
                model.addAttribute("error", "Please correct the errors in the form");
                return "users/registerForm";
            }

            // Check password length
            if (user.getPassword() == null || user.getPassword().length() < 6) {
                logger.warn("Password too short for user: {}", user.getEmail());
                model.addAttribute("error", "Password must be at least 6 characters long");
                return "users/registerForm";
            }

            // Check if user already exists
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                logger.warn("User already exists: {}", user.getEmail());
                model.addAttribute("error", "An account with this email already exists");
                return "users/registerForm";
            }

            // Encode password and save user
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRole("ROLE_USER");
            user.setEnabled(true);
            
            logger.info("Saving new user: {}", user.getEmail());
            userRepository.save(user);
            
            logger.info("Successfully registered user: {}", user.getEmail());

            // Add success message for the login page
            redirectAttributes.addFlashAttribute("message", "Registration successful! Please login.");
            return "redirect:/users/loginForm";
            
        } catch (Exception e) {
            logger.error("Error during registration for user: " + user.getEmail(), e);
            model.addAttribute("error", "An error occurred during registration. Please try again.");
            return "users/registerForm";
        }
    }

    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal, HttpSession session) {
        if (principal == null) {
            return "redirect:/users/loginForm";
        }
        
        UserEntity user = userRepository.findByEmail(principal.getName())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        model.addAttribute("user", user);
        model.addAttribute("recentActivity", userActivityService.getRecentActivities(user));
        session.setAttribute("user", user);
        return "users/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam(value = "file", required = false) MultipartFile file,
                              @RequestParam("name") String name,
                              @RequestParam("email") String email,
                              @RequestParam(value = "currentPassword", required = false) String currentPassword,
                              @RequestParam(value = "newPassword", required = false) String newPassword,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        try {
            logger.info("Starting profile update for user: {}", principal.getName());
            
            UserEntity user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Update basic info
            user.setName(name);
            user.setEmail(email);

            // Handle password change if provided
            if (currentPassword != null && !currentPassword.isEmpty() && 
                newPassword != null && !newPassword.isEmpty()) {
                if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                    redirectAttributes.addFlashAttribute("error", "Current password is incorrect");
                    return "redirect:/users/profile";
                }
                user.setPassword(passwordEncoder.encode(newPassword));
            }

            // Handle profile picture if provided
            if (file != null && !file.isEmpty()) {
                logger.info("Processing profile picture upload for user: {}", user.getEmail());
                logger.info("File details - Name: {}, Size: {} bytes, Content Type: {}", 
                    file.getOriginalFilename(), file.getSize(), file.getContentType());
                
                // Validate file type
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    logger.warn("Invalid file type uploaded: {}", contentType);
                    redirectAttributes.addFlashAttribute("error", "Only image files are allowed");
                    return "redirect:/users/profile";
                }
                
                try {
                    String imageUrl = s3Service.uploadFile(file);
                    logger.info("Profile picture uploaded successfully to S3, URL: {}", imageUrl);
                    user.setProfilePictureUrl(imageUrl);
                } catch (IOException e) {
                    logger.error("Error uploading profile picture: {}", e.getMessage(), e);
                    redirectAttributes.addFlashAttribute("error", "Error uploading profile picture");
                    return "redirect:/users/profile";
                }
            }

            logger.info("Saving updated user profile to database");
            UserEntity savedUser = userRepository.save(user);
            logger.info("User profile saved to database. ID: {}, Name: {}, ProfilePictureUrl: {}", 
                savedUser.getId(), savedUser.getName(), savedUser.getProfilePictureUrl());
            
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
            return "redirect:/";
            
        } catch (Exception e) {
            logger.error("Error updating profile", e);
            redirectAttributes.addFlashAttribute("error", "Error updating profile");
            return "redirect:/users/profile";
        }
    }
}