package com.birds.bird_app.service;

import com.birds.bird_app.model.UserEntity;
import com.birds.bird_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private S3Service s3Service;

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
} 