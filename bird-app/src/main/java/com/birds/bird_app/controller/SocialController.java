package com.birds.bird_app.controller;

import com.birds.bird_app.model.UserActivity;
import com.birds.bird_app.service.BirdService;
import com.birds.bird_app.service.GroupService;
import com.birds.bird_app.service.UserActivityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import com.birds.bird_app.model.BirdEntity;
import com.birds.bird_app.model.GroupEntity;

@Controller
@RequestMapping("/social")
public class SocialController {

    private final UserActivityService userActivityService;
    private final BirdService birdService;
    private final GroupService groupService;

    @Autowired
    public SocialController(UserActivityService userActivityService, 
                          BirdService birdService,
                          GroupService groupService) {
        this.userActivityService = userActivityService;
        this.birdService = birdService;
        this.groupService = groupService;
    }

    @GetMapping
    public String socialFeed(Model model) {
        // Get recent activities
        List<UserActivity> activities = userActivityService.getRecentActivities(20);
        model.addAttribute("activities", activities);

        // Get trending birds (most viewed/active in the last week)
        List<BirdEntity> trendingBirds = birdService.getTrendingBirds(5);
        model.addAttribute("trendingBirds", trendingBirds);

        // Get active groups (most active in the last week)
        List<GroupEntity> activeGroups = groupService.getActiveGroups(5);
        model.addAttribute("activeGroups", activeGroups);

        return "social/index";
    }
} 