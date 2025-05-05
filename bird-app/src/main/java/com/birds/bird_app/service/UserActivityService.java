package com.birds.bird_app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.birds.bird_app.model.UserActivity;
import com.birds.bird_app.model.UserEntity;
import com.birds.bird_app.model.BirdEntity;
import com.birds.bird_app.model.GroupEntity;
import com.birds.bird_app.repository.UserActivityRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserActivityService {
    
    @Autowired
    private UserActivityRepository userActivityRepository;

    public void trackBirdSpotted(UserEntity user, BirdEntity bird, String imageUrl) {
        UserActivity activity = new UserActivity();
        activity.setUser(user);
        activity.setBird(bird);
        activity.setType("BIRD_SPOTTED");
        activity.setDescription("Spotted a " + bird.getName());
        activity.setActivityUrl(imageUrl);
        activity.setCreatedAt(LocalDateTime.now());
        userActivityRepository.save(activity);
    }

    public void trackGroupSubmission(UserEntity user, String groupName, String birdName, String submissionUrl) {
        UserActivity activity = new UserActivity();
        activity.setUser(user);
        activity.setType("GROUP_SUBMISSION");
        activity.setDescription("Submitted " + birdName + " to " + groupName);
        activity.setActivityUrl(submissionUrl);
        activity.setCreatedAt(LocalDateTime.now());
        userActivityRepository.save(activity);
    }

    public void trackGroupSubmission(UserEntity user, GroupEntity group, String birdName, String submissionUrl) {
        UserActivity activity = new UserActivity();
        activity.setUser(user);
        activity.setGroup(group);
        activity.setType("GROUP_SUBMISSION");
        activity.setDescription("Submitted " + birdName + " to " + group.getName());
        activity.setActivityUrl(submissionUrl);
        activity.setCreatedAt(LocalDateTime.now());
        userActivityRepository.save(activity);
    }

    public List<UserActivity> getRecentActivities(UserEntity user) {
        return userActivityRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Page<UserActivity> getRecentActivities(UserEntity user, Pageable pageable) {
        return userActivityRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    public List<UserActivity> getRecentActivities(int limit) {
        return userActivityRepository.findTopByOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }
} 