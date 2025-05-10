package com.birds.bird_app.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BadCredentialsException.class)
    public String handleBadCredentialsException(BadCredentialsException ex, 
                                              RedirectAttributes redirectAttributes) {
        logger.warn("Authentication failed: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", "Invalid email or password");
        return "redirect:/users/loginForm";
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public String handleUsernameNotFoundException(UsernameNotFoundException ex, 
                                               RedirectAttributes redirectAttributes) {
        logger.warn("User not found: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", "User not found");
        return "redirect:/users/loginForm";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex,
                                                     RedirectAttributes redirectAttributes) {
        logger.warn("File upload size exceeded: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", "File size exceeds maximum limit");
        return "redirect:/users/profile";
    }

    @ExceptionHandler(MultipartException.class)
    public String handleMultipartException(MultipartException ex,
                                         RedirectAttributes redirectAttributes) {
        logger.warn("File upload error: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", "Error uploading file");
        return "redirect:/users/profile";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResourceFoundException(NoResourceFoundException ex,
                                               HttpServletRequest request,
                                               RedirectAttributes redirectAttributes) {
        logger.warn("Resource not found: {}", ex.getMessage());
        // For missing static resources, we'll just log the warning and continue
        // The browser will handle the missing resource gracefully
        return null;
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, 
                                       HttpServletRequest request,
                                       RedirectAttributes redirectAttributes) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        redirectAttributes.addFlashAttribute("error", "An unexpected error occurred. Please try again.");
        
        // Determine the appropriate redirect based on the request URL
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/users/profile")) {
            return "redirect:/users/profile";
        } else if (requestURI.contains("/users/login") || requestURI.contains("/users/loginForm")) {
            return "redirect:/users/loginForm";
        } else {
            return "redirect:/";
        }
    }
} 