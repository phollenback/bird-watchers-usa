package com.birds.bird_app.controller;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.io.IOException;
import java.util.Optional;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;

import com.birds.bird_app.model.GroupEntity;
import com.birds.bird_app.model.GroupSettings;
import com.birds.bird_app.model.GroupSettings.VisibilityType;
import com.birds.bird_app.model.GroupSettings.MeetingFrequency;
import com.birds.bird_app.model.GroupSettings.Season;
import com.birds.bird_app.repository.GroupRepository;
import com.birds.bird_app.model.GroupMember;
import com.birds.bird_app.model.UserEntity;
import com.birds.bird_app.repository.GroupMemberRepository;
import com.birds.bird_app.repository.UserRepository;
import com.birds.bird_app.service.S3Service;
import com.birds.bird_app.model.BirdSubmission;
import com.birds.bird_app.repository.BirdSubmissionRepository;
import com.birds.bird_app.service.ImageVerificationService;
import com.birds.bird_app.model.BirdEntity;
import com.birds.bird_app.repository.BirdRepository;
import com.birds.bird_app.service.UserActivityService;

@Controller
@RequestMapping("/groups")
public class GroupController {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private BirdSubmissionRepository birdSubmissionRepository;

    @Autowired
    private ImageVerificationService imageVerificationService;

    @Autowired
    private BirdRepository birdRepository;

    @Autowired
    private UserActivityService userActivityService;

    @GetMapping
    public String getAllGroups(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String visibility,
            @RequestParam(required = false) String frequency,
            @RequestParam(required = false) String season,
            @RequestParam(required = false) String features,
            @RequestParam(required = false) String membership,
            Model model,
            @AuthenticationPrincipal UserEntity currentUser,
            HttpSession session) {
        
        if (currentUser != null) {
            session.setAttribute("user", currentUser);
        }
        
        List<GroupEntity> groups = groupRepository.findAll();

        // Get fresh presigned URLs for all group images
        for (GroupEntity group : groups) {
            if (group.getSettings() != null && group.getSettings().getGroupImageUrl() != null) {
                String freshUrl = s3Service.getPresignedUrl(group.getSettings().getGroupImageUrl());
                group.getSettings().setGroupImageUrl(freshUrl);
            }
        }

        if (search != null && !search.isEmpty()) {
            groups = groups.stream()
                .filter(group -> group.getName().toLowerCase().contains(search.toLowerCase()) ||
                               (group.getDescription() != null && 
                                group.getDescription().toLowerCase().contains(search.toLowerCase())))
                .collect(Collectors.toList());
        }

        if (StringUtils.hasText(region)) {
            groups = groups.stream()
                .filter(group -> group.getSettings() != null && 
                        region.equals(group.getSettings().getRegion()))
                .collect(Collectors.toList());
        }

        if (StringUtils.hasText(visibility)) {
            groups = groups.stream()
                .filter(group -> group.getSettings() != null && 
                        VisibilityType.valueOf(visibility) == group.getSettings().getVisibilityType())
                .collect(Collectors.toList());
        }

        if (StringUtils.hasText(frequency)) {
            groups = groups.stream()
                .filter(group -> group.getSettings() != null && 
                        MeetingFrequency.valueOf(frequency) == group.getSettings().getMeetingFrequency())
                .collect(Collectors.toList());
        }

        if (StringUtils.hasText(season)) {
            groups = groups.stream()
                .filter(group -> group.getSettings() != null && 
                        Season.valueOf(season) == group.getSettings().getSeasonalActivity())
                .collect(Collectors.toList());
        }

        if (StringUtils.hasText(features)) {
            groups = groups.stream()
                .filter(group -> group.getSettings() != null && 
                        hasFeature(group, features))
                .collect(Collectors.toList());
        }

        if (StringUtils.hasText(membership)) {
            groups = groups.stream()
                .filter(group -> group.getSettings() != null && 
                        membership.equals(group.getSettings().isAutoApproveMembership() ? 
                            "AUTO_APPROVE" : "MANUAL_APPROVE"))
                .collect(Collectors.toList());
        }

        model.addAttribute("groups", groups);
        
        // Add filter options for the dropdowns
        model.addAttribute("regions", List.of("Pacific Northwest", "Northeast", "Midwest", "Southwest", "Southeast"));
        model.addAttribute("visibilityTypes", VisibilityType.values());
        model.addAttribute("frequencies", MeetingFrequency.values());
        model.addAttribute("seasons", Season.values());
        model.addAttribute("featuresList", List.of("PHOTO_SHARING", "GUEST_ALLOWED", "VERIFICATION"));
        model.addAttribute("membershipTypes", List.of("AUTO_APPROVE", "MANUAL_APPROVE"));
        
        return "groups/index";
    }

    @GetMapping("/home")
    public String showGroupHome(Model model) {
        // Redirect to the groups list if no specific group is selected
        return "redirect:/groups";
    }

    @GetMapping("/{id}")
    public String getGroupHome(
            @PathVariable Long id,
            @AuthenticationPrincipal UserEntity currentUser,
            Model model) {
        
        try {
            GroupEntity group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));

            // Get fresh presigned URL for group image
            if (group.getSettings() != null && group.getSettings().getGroupImageUrl() != null) {
                String freshUrl = s3Service.getPresignedUrl(group.getSettings().getGroupImageUrl());
                group.getSettings().setGroupImageUrl(freshUrl);
            }

            // Get current meeting submissions
            List<BirdSubmission> currentSubmissions = birdSubmissionRepository
                .findByGroupAndStatusInOrderByVotesDescSubmittedAtDesc(group, List.of("ACTIVE", "WINNER"));

            // Get fresh presigned URLs for all submission images
            for (BirdSubmission submission : currentSubmissions) {
                if (submission.getImageUrl() != null) {
                    String freshUrl = s3Service.getPresignedUrl(submission.getImageUrl());
                    submission.setImageUrl(freshUrl);
                }
            }

            // Check if current user has voted for each submission
            if (currentUser != null) {
                for (BirdSubmission submission : currentSubmissions) {
                    boolean hasVoted = submission.getVotedBy().contains(currentUser);
                    submission.setHasVoted(hasVoted);
                }
            }

            // Get Big Bird from previous meeting
            List<BirdSubmission> bigBirdSubmissions = birdSubmissionRepository
                .findByGroupAndStatusAndIsBigBirdTrueOrderBySubmittedAtDesc(group, "WINNER");
            BirdSubmission bigBird = bigBirdSubmissions.isEmpty() ? null : bigBirdSubmissions.get(0);

            // Get fresh presigned URL for Big Bird image
            if (bigBird != null && bigBird.getImageUrl() != null) {
                String freshUrl = s3Service.getPresignedUrl(bigBird.getImageUrl());
                bigBird.setImageUrl(freshUrl);
            }

            // Get active group members only
            List<GroupMember> members = groupMemberRepository.findByGroupAndStatus(group, "ACTIVE");

            // Get user's submissions for this group
            Optional<BirdSubmission> userSubmission = birdSubmissionRepository
                .findByGroupAndStatusAndSubmittedBy(group, "ACTIVE", currentUser);
            List<BirdSubmission> userSubmissions = userSubmission.map(List::of).orElse(List.of());

            // Get user's birds
            List<BirdEntity> userBirds = birdRepository.findByUploadedByOrderByUploadedAtDesc(currentUser);

            // Get fresh presigned URLs for all user bird images
            for (BirdEntity bird : userBirds) {
                if (bird.getImageUrl() != null) {
                    // Only generate a new presigned URL if it's not already a full URL
                    if (!bird.getImageUrl().startsWith("http")) {
                    String freshUrl = s3Service.getPresignedUrl(bird.getImageUrl());
                    bird.setImageUrl(freshUrl);
                    }
                }
            }

            model.addAttribute("group", group);
            model.addAttribute("currentSubmissions", currentSubmissions);
            model.addAttribute("bigBird", bigBird);
            model.addAttribute("members", members);
            model.addAttribute("userSubmissions", userSubmissions);
            model.addAttribute("userBirds", userBirds);
            model.addAttribute("isMember", groupMemberRepository.findByUserAndGroupAndStatus(currentUser, group, "ACTIVE").isPresent());

            return "groups/groupHome";

        } catch (Exception e) {
            model.addAttribute("error", "Failed to load group: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/{id}/members")
    public String getGroupMembers(@PathVariable Long id, Model model) {
        System.out.println("Fetching members for group ID: " + id);
        
        GroupEntity group = groupRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Group not found"));
        System.out.println("Found group: " + group.getName());
        
        List<GroupMember> groupMembers = groupMemberRepository.findByGroupAndStatus(group, "ACTIVE");
        System.out.println("Number of active group members: " + groupMembers.size());
        
        List<UserEntity> members = groupMembers.stream()
            .map(GroupMember::getUser)
            .collect(Collectors.toList());
        System.out.println("Number of users after mapping: " + members.size());
            
        model.addAttribute("members", members);
        return "groups/groupHome :: membersList";
    }

    private boolean hasFeature(GroupEntity group, String feature) {
        switch (feature) {
            case "PHOTO_SHARING":
                return group.getSettings().isPhotoSharingEnabled();
            case "GUEST_ALLOWED":
                return group.getSettings().isGuestViewersAllowed();
            case "VERIFICATION":
                return group.getSettings().isVerificationRequired();
            default:
                return false;
        }
    }

    

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        GroupEntity group = new GroupEntity();
        GroupSettings settings = new GroupSettings();
        settings.setTheme("forest"); // Set default theme
        settings.setVisibilityType(VisibilityType.PUBLIC);
        settings.setMeetingFrequency(MeetingFrequency.WEEKLY);
        settings.setSeasonalActivity(Season.SPRING);
        settings.setPhotoSharingEnabled(true);
        settings.setGuestViewersAllowed(true);
        settings.setVerificationRequired(false);
        settings.setAutoApproveMembership(true);
        group.setSettings(settings);
        
        // Add all necessary options to the model
        model.addAttribute("group", group);
        model.addAttribute("regions", List.of("Pacific Northwest", "Northeast", "Midwest", "Southwest", "Southeast"));
        model.addAttribute("visibilityTypes", VisibilityType.values());
        model.addAttribute("frequencies", MeetingFrequency.values());
        model.addAttribute("seasons", Season.values());
        
        return "groups/create";
    }

    @PostMapping("/create")
    public String createGroup(
            @Valid GroupEntity group,
            BindingResult result,
            @AuthenticationPrincipal UserEntity currentUser,
            @RequestParam(value = "groupImage", required = false) MultipartFile groupImage,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        if (result.hasErrors()) {
            System.out.println("Form validation errors: " + result.getAllErrors());
            // Re-add the regions and other options to the model
            model.addAttribute("regions", List.of("Pacific Northwest", "Northeast", "Midwest", "Southwest", "Southeast"));
            model.addAttribute("visibilityTypes", VisibilityType.values());
            model.addAttribute("frequencies", MeetingFrequency.values());
            model.addAttribute("seasons", Season.values());
            return "groups/create";
        }

        try {
            // Set creation metadata
            group.setCreatedAt(LocalDateTime.now());
            group.setUpdatedAt(LocalDateTime.now());
            group.setMemberCount(1); // Start with 1 member (the founder)
            group.setFounder(currentUser);
            group.setBirdKeeper(currentUser);

            // Initialize settings if not set
            if (group.getSettings() == null) {
                GroupSettings settings = new GroupSettings();
                settings.setTheme("forest");
                settings.setVisibilityType(VisibilityType.PUBLIC);
                settings.setMeetingFrequency(MeetingFrequency.WEEKLY);
                settings.setSeasonalActivity(Season.SPRING);
                settings.setPhotoSharingEnabled(true);
                settings.setGuestViewersAllowed(true);
                settings.setVerificationRequired(false);
                settings.setAutoApproveMembership(true);
                group.setSettings(settings);
            }

            // Validate settings
            if (group.getSettings().getRegion() == null || group.getSettings().getRegion().trim().isEmpty()) {
                result.rejectValue("settings.region", "error.group", "Region is required");
                model.addAttribute("regions", List.of("Pacific Northwest", "Northeast", "Midwest", "Southwest", "Southeast"));
                model.addAttribute("visibilityTypes", VisibilityType.values());
                model.addAttribute("frequencies", MeetingFrequency.values());
                model.addAttribute("seasons", Season.values());
                return "groups/create";
            }

            // Handle image upload if provided
            if (groupImage != null && !groupImage.isEmpty()) {
                try {
                    String fileKey = s3Service.uploadFile(groupImage);
                    String presignedUrl = s3Service.getPresignedUrl(fileKey);
                    group.getSettings().setGroupImageUrl(presignedUrl);
                } catch (IOException e) {
                    redirectAttributes.addFlashAttribute("error", "Failed to upload image: " + e.getMessage());
                    return "redirect:/groups/create";
                }
            }

            // Save the group
            GroupEntity savedGroup = groupRepository.save(group);

            // Create the founder's membership
            GroupMember founderMembership = new GroupMember();
            founderMembership.setGroup(savedGroup);
            founderMembership.setUser(currentUser);
            founderMembership.setJoinedAt(LocalDateTime.now());
            founderMembership.setRole("FOUNDER");
            groupMemberRepository.save(founderMembership);

            redirectAttributes.addFlashAttribute("message", "Group created successfully!");
            return "redirect:/groups/" + savedGroup.getId();

        } catch (Exception e) {
            System.out.println("Error creating group: " + e.getMessage());
            e.printStackTrace();
            result.rejectValue("name", "error.group", "An error occurred while creating the group: " + e.getMessage());
            // Re-add the regions and other options to the model
            model.addAttribute("regions", List.of("Pacific Northwest", "Northeast", "Midwest", "Southwest", "Southeast"));
            model.addAttribute("visibilityTypes", VisibilityType.values());
            model.addAttribute("frequencies", MeetingFrequency.values());
            model.addAttribute("seasons", Season.values());
            return "groups/create";
        }
    }

    @PostMapping("/{id}/join")
    public String joinGroup(@PathVariable Long id, 
                          @AuthenticationPrincipal UserEntity currentUser,
                          RedirectAttributes redirectAttributes) {
        try {
            GroupEntity group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));

            // Check if user is already an active member
            Optional<GroupMember> existingMembership = groupMemberRepository.findByUserAndGroupAndStatus(currentUser, group, "ACTIVE");
            if (existingMembership.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "You are already a member of this group");
                return "redirect:/groups/" + id;
            }

            // Check if group allows auto-approval and is not full
            if (!group.getSettings().isAutoApproveMembership()) {
                redirectAttributes.addFlashAttribute("error", "This group requires manual approval to join");
                return "redirect:/groups/" + id;
            }

            if (group.getMemberCount() >= 100) {
                redirectAttributes.addFlashAttribute("error", "This group has reached its maximum member limit");
                return "redirect:/groups/" + id;
            }

            // Check for existing pending membership
            Optional<GroupMember> pendingMembership = groupMemberRepository.findByUserAndGroupAndStatus(currentUser, group, "PENDING");
            GroupMember membership;
            
            if (pendingMembership.isPresent()) {
                // Update existing pending membership to active
                membership = pendingMembership.get();
                membership.setStatus("ACTIVE");
            } else {
                // Create new membership
                membership = new GroupMember();
                membership.setGroup(group);
                membership.setUser(currentUser);
                membership.setJoinedAt(LocalDateTime.now());
                membership.setRole("MEMBER");
                membership.setStatus("ACTIVE");
            }
            
            groupMemberRepository.save(membership);

            // Update group member count only if this is a new active member
            if (!pendingMembership.isPresent()) {
                group.setMemberCount(group.getMemberCount() + 1);
                groupRepository.save(group);
            }

            redirectAttributes.addFlashAttribute("message", "Successfully joined the group!");
            return "redirect:/groups/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to join group: " + e.getMessage());
            return "redirect:/groups/" + id;
        }
    }

    @PostMapping("/{id}/settings")
    public String updateGroupSettings(
            @PathVariable Long id,
            @RequestParam(value = "image", required = false) MultipartFile groupImage,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String visibilityType,
            @RequestParam(required = false) String meetingFrequency,
            @RequestParam(required = false) String seasonalActivity,
            @RequestParam(required = false) Boolean photoSharingEnabled,
            @RequestParam(required = false) Boolean guestViewersAllowed,
            @RequestParam(required = false) Boolean verificationRequired,
            @RequestParam(required = false) Boolean autoApproveMembership,
            @RequestParam(required = false) String theme,
            @AuthenticationPrincipal UserEntity currentUser,
            RedirectAttributes redirectAttributes) {
        
        try {
            GroupEntity group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));

            // Check if user has permission to update settings
            if (!currentUser.equals(group.getFounder()) && !currentUser.equals(group.getBirdKeeper())) {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to update group settings");
                return "redirect:/groups/" + id;
            }

            // Initialize settings if not set
            if (group.getSettings() == null) {
                group.setSettings(new GroupSettings());
            }

            // Handle image upload if provided
            if (groupImage != null && !groupImage.isEmpty()) {
                try {
                    String fileKey = s3Service.uploadFile(groupImage);
                    String presignedUrl = s3Service.getPresignedUrl(fileKey);
                    group.getSettings().setGroupImageUrl(presignedUrl);
                } catch (IOException e) {
                    redirectAttributes.addFlashAttribute("error", "Failed to upload image: " + e.getMessage());
                    return "redirect:/groups/" + id;
                }
            }

            // Update other settings if provided
            if (region != null) group.getSettings().setRegion(region);
            if (visibilityType != null) group.getSettings().setVisibilityType(VisibilityType.valueOf(visibilityType));
            if (meetingFrequency != null) group.getSettings().setMeetingFrequency(MeetingFrequency.valueOf(meetingFrequency));
            if (seasonalActivity != null) group.getSettings().setSeasonalActivity(Season.valueOf(seasonalActivity));
            if (photoSharingEnabled != null) group.getSettings().setPhotoSharingEnabled(photoSharingEnabled);
            if (guestViewersAllowed != null) group.getSettings().setGuestViewersAllowed(guestViewersAllowed);
            if (verificationRequired != null) group.getSettings().setVerificationRequired(verificationRequired);
            if (autoApproveMembership != null) group.getSettings().setAutoApproveMembership(autoApproveMembership);
            if (theme != null) group.getSettings().setTheme(theme);

            // Update the group
            group.setUpdatedAt(LocalDateTime.now());
            groupRepository.save(group);

            redirectAttributes.addFlashAttribute("message", "Group settings updated successfully!");
            return "redirect:/groups/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update group settings: " + e.getMessage());
            return "redirect:/groups/" + id;
        }
    }

    @PostMapping("/{id}/submit")
    public String submitBird(
            @PathVariable Long id,
            @RequestParam("birdImage") MultipartFile birdImage,
            @RequestParam("birdName") String birdName,
            @AuthenticationPrincipal UserEntity currentUser,
            RedirectAttributes redirectAttributes) {
        
        try {
            GroupEntity group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));

            // Verify user is a member
            if (!groupMemberRepository.findByUserAndGroup(currentUser, group).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "You must be a member to submit birds");
                return "redirect:/groups/" + id;
            }

            // Check if user has already submitted
            boolean hasSubmitted = birdSubmissionRepository
                .findByGroupAndStatusAndSubmittedBy(group, "ACTIVE", currentUser)
                .isPresent();
            if (hasSubmitted) {
                redirectAttributes.addFlashAttribute("error", "You have already submitted a bird to this meeting");
                return "redirect:/groups/" + id;
            }

            // Verify image is a bird using Vision API
            if (!imageVerificationService.isBirdImage(birdImage)) {
                redirectAttributes.addFlashAttribute("error", "That's not a bird! 🐦 This is a bird-watching app, not a 'whatever-you-just-uploaded' watching app! Please try again with a proper bird photo.");
                return "redirect:/groups/" + id;
            }

            // Validate bird name
            if (birdName == null || birdName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Every bird deserves a name! 🐦 Please give your bird a name.");
                return "redirect:/groups/" + id;
            }

            // Upload image to S3 and get file key
            String fileKey = s3Service.uploadFile(birdImage);

            // Create bird submission
            BirdSubmission submission = new BirdSubmission();
            submission.setGroup(group);
            submission.setSubmittedBy(currentUser);
            submission.setImageUrl(fileKey); // Store just the file key
            submission.setSubmittedAt(LocalDateTime.now());
            submission.setStatus("ACTIVE");
            submission.setVotes(0);
            submission.setBirdName(birdName.trim());
            birdSubmissionRepository.save(submission);

            // Track the activity
            userActivityService.trackGroupSubmission(
                currentUser, 
                group,
                birdName.trim(), 
                fileKey
            );

            redirectAttributes.addFlashAttribute("message", "Bird successfully submitted! 🐦");
            return "redirect:/groups/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to submit bird: " + e.getMessage());
            return "redirect:/groups/" + id;
        }
    }

    @GetMapping("/{id}/submit-bird")
    @Transactional
    public String submitExistingBird(
            @PathVariable Long id,
            @RequestParam Long birdId,
            @AuthenticationPrincipal UserEntity currentUser,
            RedirectAttributes redirectAttributes) {
        
        try {
            GroupEntity group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));

            // Check if user is a member
            if (!groupMemberRepository.findByUserAndGroupAndStatus(currentUser, group, "ACTIVE").isPresent()) {
                redirectAttributes.addFlashAttribute("error", "You must be a member to submit birds");
                return "redirect:/groups/" + id;
            }

            // Check if user has already submitted a bird
            Optional<BirdSubmission> existingSubmission = birdSubmissionRepository
                .findByGroupAndStatusAndSubmittedBy(group, "ACTIVE", currentUser);
            if (existingSubmission.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "You have already submitted a bird for this meeting");
                return "redirect:/groups/" + id;
            }

            // Get the bird
            BirdEntity bird = birdRepository.findById(birdId)
                .orElseThrow(() -> new RuntimeException("Bird not found"));

            // Verify the bird belongs to the user
            if (!bird.getUploadedBy().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "You can only submit your own birds");
                return "redirect:/groups/" + id;
            }

            // Create submission
            BirdSubmission submission = new BirdSubmission();
            submission.setGroup(group);
            submission.setSubmittedBy(currentUser);
            submission.setImageUrl(bird.getImageUrl()); // Store the file key
            submission.setBirdName(bird.getName());
            submission.setSubmittedAt(LocalDateTime.now());
            submission.setStatus("ACTIVE");
            submission.setVotes(0);
            birdSubmissionRepository.save(submission);

            // Track the activity
            userActivityService.trackGroupSubmission(
                currentUser, 
                group,
                bird.getName(), 
                bird.getImageUrl()
            );

            redirectAttributes.addFlashAttribute("message", "Bird successfully submitted! 🐦");
            return "redirect:/groups/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to submit bird: " + e.getMessage());
            return "redirect:/groups/" + id;
        }
    }

    @PostMapping("/{id}/delete-submission/{submissionId}")
    public String deleteSubmission(
            @PathVariable Long id,
            @PathVariable Long submissionId,
            @AuthenticationPrincipal UserEntity currentUser,
            RedirectAttributes redirectAttributes) {
        
        try {
            GroupEntity group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));

            // Check if user is admin
            boolean isAdmin = groupMemberRepository.findByUserAndGroup(currentUser, group)
                .map(member -> 
                    member.getRole().equals("FOUNDER") || 
                    member.getRole().equals("ADMIN") || 
                    currentUser.equals(group.getBirdKeeper())
                )
                .orElse(false);

            if (!isAdmin) {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to delete submissions");
                return "redirect:/groups/" + id;
            }

            // Delete the submission
            BirdSubmission submission = birdSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

            if (!submission.getGroup().getId().equals(id)) {
                redirectAttributes.addFlashAttribute("error", "Invalid submission for this group");
                return "redirect:/groups/" + id;
            }

            birdSubmissionRepository.delete(submission);
            redirectAttributes.addFlashAttribute("message", "Submission deleted successfully");
            return "redirect:/groups/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete submission: " + e.getMessage());
            return "redirect:/groups/" + id;
        }
    }

    @PostMapping("/{id}/vote/{submissionId}")
    public String voteForSubmission(
            @PathVariable Long id,
            @PathVariable Long submissionId,
            @AuthenticationPrincipal UserEntity currentUser,
            RedirectAttributes redirectAttributes) {
        
        try {
            GroupEntity group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));

            // Verify user is a member
            if (!groupMemberRepository.findByUserAndGroup(currentUser, group).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "You must be a member to vote");
                return "redirect:/groups/" + id;
            }

            // Get the submission
            BirdSubmission submission = birdSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

            // Verify submission belongs to this group
            if (!submission.getGroup().getId().equals(id)) {
                redirectAttributes.addFlashAttribute("error", "Invalid submission for this group");
                return "redirect:/groups/" + id;
            }

            // Check if user has already voted
            if (submission.getVotedBy().contains(currentUser)) {
                redirectAttributes.addFlashAttribute("error", "You have already voted for this submission");
                return "redirect:/groups/" + id;
            }

            // Add vote
            submission.getVotedBy().add(currentUser);
            submission.setVotes(submission.getVotes() + 1);
            
            // Check if this submission is now the winner (has the most votes)
            List<BirdSubmission> currentSubmissions = birdSubmissionRepository
                .findByGroupAndStatusOrderByVotesDescSubmittedAtDesc(group, "ACTIVE");
            
            if (!currentSubmissions.isEmpty() && currentSubmissions.get(0).getId().equals(submissionId)) {
                // This submission is now the winner
                submission.setStatus("WINNER");
                
                // Mark all other submissions as archived
                currentSubmissions.stream()
                    .filter(sub -> !sub.getId().equals(submissionId))
                    .forEach(sub -> {
                        sub.setStatus("ARCHIVED");
                        birdSubmissionRepository.save(sub);
                    });
                
                // Set this submission as the Big Bird for the next meeting
                submission.setBigBird(true);
            }
            
            birdSubmissionRepository.save(submission);

            redirectAttributes.addFlashAttribute("message", "Vote recorded successfully!");
            return "redirect:/groups/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to record vote: " + e.getMessage());
            return "redirect:/groups/" + id;
        }
    }
}
