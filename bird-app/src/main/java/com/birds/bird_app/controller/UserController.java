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
import com.birds.bird_app.service.UserService;

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
    private final UserService userService;

    @Autowired
    public UserController(UserRepository userRepository, 
                         PasswordEncoder passwordEncoder,
                         S3Service s3Service,
                         UserActivityService userActivityService,
                         UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.s3Service = s3Service;
        this.userActivityService = userActivityService;
        this.userService = userService;
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
        
        if (bindingResult.hasErrors()) {
            logger.warn("Registration validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("error", "Please correct the errors in the form");
            return "users/registerForm";
        }

        if (user.getPassword() == null || user.getPassword().length() < 6) {
            logger.warn("Password too short for user: {}", user.getEmail());
            model.addAttribute("error", "Password must be at least 6 characters long");
            return "users/registerForm";
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            logger.warn("User already exists: {}", user.getEmail());
            model.addAttribute("error", "An account with this email already exists");
            return "users/registerForm";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        user.setEnabled(true);
        
        logger.info("Saving new user: {}", user.getEmail());
        userRepository.save(user);
        
        logger.info("Successfully registered user: {}", user.getEmail());
        redirectAttributes.addFlashAttribute("message", "Registration successful! Please login.");
        return "redirect:/users/loginForm";
    }

    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal, HttpSession session) {
        if (principal == null) {
            return "redirect:/users/loginForm";
        }
        
        UserEntity user = userRepository.findByEmail(principal.getName())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        model.addAttribute("user", user);
        model.addAttribute("userService", userService);
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
                              HttpSession session,
                              RedirectAttributes redirectAttributes) throws IOException {
        logger.info("Starting profile update for user: {}", principal.getName());
        
        UserEntity user = userRepository.findByEmail(principal.getName())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setName(name);
        user.setEmail(email);

        if (currentPassword != null && !currentPassword.isEmpty() && 
            newPassword != null && !newPassword.isEmpty()) {
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Current password is incorrect");
                return "redirect:/users/settings";
            }
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        if (file != null && !file.isEmpty()) {
            logger.info("Processing profile picture upload for user: {}", user.getEmail());
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("error", "Only image files are allowed");
                return "redirect:/users/settings";
            }
            
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String uploadedKey = s3Service.uploadFile(file, fileName);
            logger.info("Profile picture uploaded successfully to S3, file key: {}", uploadedKey);
            String presignedUrl = s3Service.getPresignedUrl(uploadedKey);
            logger.info("Generated presigned URL for profile picture: {}", presignedUrl);
            user.setProfilePictureUrl(presignedUrl);
        }

        logger.info("Saving updated user profile to database");
        userRepository.save(user);
        
        // Update session with new user data
        session.setAttribute("user", user);
        
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
        return "redirect:/users/settings";
    }
}