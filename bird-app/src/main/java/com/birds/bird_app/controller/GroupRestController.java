package com.birds.bird_app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.birds.bird_app.model.BirdSubmission;
import com.birds.bird_app.model.GroupEntity;
import com.birds.bird_app.model.UserEntity;
import com.birds.bird_app.repository.BirdSubmissionRepository;
import com.birds.bird_app.repository.GroupMemberRepository;
import com.birds.bird_app.repository.GroupRepository;
import com.birds.bird_app.service.ImageVerificationService;
import com.birds.bird_app.service.S3Service;
import com.birds.bird_app.service.UserActivityService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/groups")
public class GroupRestController {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private BirdSubmissionRepository birdSubmissionRepository;

    @Autowired
    private ImageVerificationService imageVerificationService;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private UserActivityService userActivityService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/{groupId}/submissions")
    public ResponseEntity<?> submitBird(
            @PathVariable Long groupId,
            @RequestParam("image") MultipartFile image,
            @RequestParam("submission") MultipartFile submissionJson,
            @AuthenticationPrincipal UserEntity currentUser) {
        try {
            GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

            // Verify user is a member
            if (!groupMemberRepository.findByUserAndGroup(currentUser, group).isPresent()) {
                return ResponseEntity.status(403).body("You must be a member to submit birds");
            }

            // Check if user has already submitted
            if (birdSubmissionRepository.findByGroupAndStatusAndSubmittedBy(group, "ACTIVE", currentUser).isPresent()) {
                return ResponseEntity.badRequest().body("You have already submitted a bird to this meeting");
            }

            // Verify image is a bird
            if (!imageVerificationService.isBirdImage(image)) {
                return ResponseEntity.badRequest().body("The uploaded image must be of a bird");
            }

            // Upload image to S3
            String imageUrl = s3Service.uploadFile(image);

            // Parse submission JSON
            BirdSubmission submission = objectMapper.readValue(submissionJson.getBytes(), BirdSubmission.class);
            submission.setGroup(group);
            submission.setSubmittedBy(currentUser);
            submission.setImageUrl(imageUrl);
            submission.setSubmittedAt(LocalDateTime.now());
            submission.setStatus("ACTIVE");
            submission.setVotes(0);

            BirdSubmission savedSubmission = birdSubmissionRepository.save(submission);

            // Track activity
            userActivityService.trackGroupSubmission(
                currentUser,
                group,
                submission.getBirdName(),
                "/groups/" + groupId
            );

            return ResponseEntity.ok(savedSubmission);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to submit bird: " + e.getMessage());
        }
    }
} 