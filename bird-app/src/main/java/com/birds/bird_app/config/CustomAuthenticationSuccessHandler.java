package com.birds.bird_app.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.birds.bird_app.model.UserEntity;
import com.birds.bird_app.repository.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);

    private final UserRepository userRepository;

    public CustomAuthenticationSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        logger.info("Authentication successful for user: {}", authentication.getName());
        
        HttpSession session = request.getSession();
        UserEntity user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found after authentication"));
        
        session.setAttribute("user", user);
        
        logger.info("User session created for: {}", authentication.getName());
        logger.info("Redirecting to /");
        
        response.sendRedirect("/");
    }
} 