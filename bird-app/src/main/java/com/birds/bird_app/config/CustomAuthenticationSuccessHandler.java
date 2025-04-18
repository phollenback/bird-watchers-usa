package com.birds.bird_app.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.birds.bird_app.model.UserEntity;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        logger.info("Authentication successful for user: {}", authentication.getName());
        
        UserEntity user = (UserEntity) authentication.getPrincipal();
        HttpSession session = request.getSession();
        session.setAttribute("userName", user.getName());
        
        logger.info("User session created for: {} ({})", user.getName(), user.getEmail());
        logger.info("Redirecting to /birds");
        
        response.sendRedirect("/birds");
    }
} 