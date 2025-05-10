package com.birds.bird_app.service;

import com.birds.bird_app.model.UserEntity;
import com.birds.bird_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private S3Service s3Service;

    private static final Pattern S3_KEY_PATTERN = Pattern.compile("/([^/]+)$");

    public UserEntity getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void updateProfile(Long userId, String name, String email, String currentPassword, 
                            String newPassword, MultipartFile profilePicture) {
        UserEntity user = getUserById(userId);

        // Update basic info
        user.setName(name);
        user.setEmail(email);

        // Update password if provided
        if (currentPassword != null && !currentPassword.isEmpty() && 
            newPassword != null && !newPassword.isEmpty()) {
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        // Update profile picture if provided
        if (profilePicture != null && !profilePicture.isEmpty()) {
            try {
                String profilePictureUrl = s3Service.uploadFile(profilePicture);
                user.setProfilePictureUrl(profilePictureUrl);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload profile picture", e);
            }
        }

        userRepository.save(user);
    }

    public String getProfilePictureUrl(UserEntity user) {
        if (user == null || user.getProfilePictureUrl() == null) {
            return null;
        }

        String url = user.getProfilePictureUrl();
        
        // If the stored value is already a URL (contains http), return it
        if (url.startsWith("http")) {
            return url;
        }

        // Otherwise, treat it as a file key and get a fresh presigned URL
        return s3Service.getPresignedUrl(url);
    }
} 