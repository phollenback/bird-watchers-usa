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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.birds.bird_app.model.UserEntity;
import com.birds.bird_app.repository.UserRepository;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
}