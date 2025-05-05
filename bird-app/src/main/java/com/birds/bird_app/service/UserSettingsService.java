package com.birds.bird_app.service;

import com.birds.bird_app.model.UserEntity;
import com.birds.bird_app.model.UserSettings;
import com.birds.bird_app.repository.UserSettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserSettingsService {
    private static final Logger logger = LoggerFactory.getLogger(UserSettingsService.class);
    
    @Autowired
    private UserSettingsRepository userSettingsRepository;
    
    public UserSettings getUserSettings(Long userId) {
        logger.info("Getting settings for user ID: {}", userId);
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    logger.info("No settings found for user ID: {}, creating default settings", userId);
                    UserSettings newSettings = new UserSettings();
                    UserEntity user = new UserEntity();
                    user.setId(userId);
                    newSettings.setUser(user);
                    return userSettingsRepository.save(newSettings);
                });
        logger.info("Retrieved settings for user ID: {}, theme: {}", userId, settings.getTheme());
        return settings;
    }
    
    public UserSettings updateUserSettings(UserSettings settings) {
        logger.info("Updating settings for user ID: {}, new theme: {}", settings.getUser().getId(), settings.getTheme());
        return userSettingsRepository.save(settings);
    }
} 