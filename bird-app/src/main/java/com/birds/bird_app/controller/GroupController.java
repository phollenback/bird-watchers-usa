package com.birds.bird_app.controller;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.io.IOException;

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

import jakarta.validation.Valid;

import com.birds.bird_app.model.GroupEntity;
import com.birds.bird_app.model.GroupSettings;
import com.birds.bird_app.model.GroupSettings.VisibilityType;
import com.birds.bird_app.model.GroupSettings.DifficultyLevel;
import com.birds.bird_app.model.GroupSettings.PreferredTime;
import com.birds.bird_app.model.GroupSettings.MeetingFrequency;
import com.birds.bird_app.model.GroupSettings.Season;
import com.birds.bird_app.repository.GroupRepository;
import com.birds.bird_app.model.GroupMember;
import com.birds.bird_app.model.UserEntity;
import com.birds.bird_app.repository.GroupMemberRepository;
import com.birds.bird_app.repository.UserRepository;
import com.birds.bird_app.service.S3Service;
import com.birds.bird_app.model.enums.*;

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

    @GetMapping
    public String getAllGroups(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String visibility,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String preferred_time,
            @RequestParam(required = false) String frequency,
            @RequestParam(required = false) String season,
            @RequestParam(required = false) String features,
            @RequestParam(required = false) String membership,
            Model model) {
        
        List<GroupEntity> groups = groupRepository.findAll();

        // Apply filters if they are present
        if (groups != null && !groups.isEmpty()) {
            if (StringUtils.hasText(search)) {
                final String searchLower = search.toLowerCase();
                groups = groups.stream()
                    .filter(group -> group.getName().toLowerCase().contains(searchLower) ||
                            (group.getDescription() != null && 
                             group.getDescription().toLowerCase().contains(searchLower)))
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

            if (StringUtils.hasText(difficulty)) {
                groups = groups.stream()
                    .filter(group -> group.getSettings() != null && 
                            DifficultyLevel.valueOf(difficulty) == group.getSettings().getDifficultyLevel())
                    .collect(Collectors.toList());
            }

            if (StringUtils.hasText(preferred_time)) {
                groups = groups.stream()
                    .filter(group -> group.getSettings() != null && 
                            PreferredTime.valueOf(preferred_time) == group.getSettings().getPreferredTime())
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
        }

        model.addAttribute("groups", groups);
        
        // Add filter options for the dropdowns
        model.addAttribute("regions", List.of("Pacific Northwest", "Northeast", "Midwest", "Southwest", "Southeast"));
        model.addAttribute("visibilityTypes", VisibilityType.values());
        model.addAttribute("difficultyLevels", DifficultyLevel.values());
        model.addAttribute("meetingTimes", PreferredTime.values());
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
    public String viewGroup(@PathVariable Long id, Model model, @AuthenticationPrincipal UserEntity currentUser) {
        try {
            System.out.println("Attempting to load group with ID: " + id);
            
            GroupEntity group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));
            
            System.out.println("Found group: " + group.getName());
                
            // Initialize settings if null
            if (group.getSettings() == null) {
                System.out.println("Group settings were null, initializing new settings");
                group.setSettings(new GroupSettings());
            }
            
            // Check if current user is a member
            boolean isMember = false;
            if (currentUser != null) {
                isMember = groupMemberRepository.findByUserAndGroup(currentUser, group).isPresent();
            }
            model.addAttribute("isMember", isMember);
                
            // Get members
            List<UserEntity> members = group.getMembers().stream()
                .map(GroupMember::getUser)
                .collect(Collectors.toList());
            System.out.println("Found " + members.size() + " members");
                
            // Add attributes to model
            model.addAttribute("group", group);
            model.addAttribute("members", members);
            
            System.out.println("Rendering group home page with theme: " + group.getSettings().getTheme());
            return "groups/groupHome";
            
        } catch (Exception e) {
            System.err.println("Error loading group: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{id}/members")
    public String getGroupMembers(@PathVariable Long id, Model model) {
        System.out.println("Fetching members for group ID: " + id);
        
        GroupEntity group = groupRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Group not found"));
        System.out.println("Found group: " + group.getName());
        
        List<GroupMember> groupMembers = group.getMembers();
        System.out.println("Number of group members: " + groupMembers.size());
        
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
        group.setSettings(settings);
        model.addAttribute("group", group);
        return "groups/create";
    }

    @PostMapping("/create")
    public String createGroup(
            @Valid GroupEntity group,
            BindingResult result,
            @AuthenticationPrincipal UserEntity currentUser,
            @RequestParam(value = "groupImage", required = false) MultipartFile groupImage,
            RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
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
                group.setSettings(new GroupSettings());
            }

            // Handle image upload if provided
            if (groupImage != null && !groupImage.isEmpty()) {
                try {
                    String imageUrl = s3Service.uploadFile(groupImage);
                    group.getSettings().setGroupImageUrl(imageUrl);
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
            result.rejectValue("name", "error.group", "An error occurred while creating the group");
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

            // Check if user is already a member
            if (groupMemberRepository.findByUserAndGroup(currentUser, group).isPresent()) {
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

            // Create new membership
            GroupMember membership = new GroupMember();
            membership.setGroup(group);
            membership.setUser(currentUser);
            membership.setJoinedAt(LocalDateTime.now());
            membership.setRole("MEMBER");
            membership.setStatus("ACTIVE");
            groupMemberRepository.save(membership);

            // Update group member count
            group.setMemberCount(group.getMemberCount() + 1);
            groupRepository.save(group);

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
            @RequestParam(required = false) String difficultyLevel,
            @RequestParam(required = false) String preferredTime,
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
                    String imageUrl = s3Service.uploadFile(groupImage);
                    group.getSettings().setGroupImageUrl(imageUrl);
                } catch (IOException e) {
                    redirectAttributes.addFlashAttribute("error", "Failed to upload image: " + e.getMessage());
                    return "redirect:/groups/" + id;
                }
            }

            // Update other settings if provided
            if (region != null) group.getSettings().setRegion(region);
            if (visibilityType != null) group.getSettings().setVisibilityType(VisibilityType.valueOf(visibilityType));
            if (difficultyLevel != null) group.getSettings().setDifficultyLevel(DifficultyLevel.valueOf(difficultyLevel));
            if (preferredTime != null) group.getSettings().setPreferredTime(PreferredTime.valueOf(preferredTime));
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
}
