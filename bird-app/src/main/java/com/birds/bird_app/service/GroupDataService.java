package com.birds.bird_app.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.birds.bird_app.model.GroupEntity;
import com.birds.bird_app.model.GroupModel;
import com.birds.bird_app.model.UserEntity;
import com.birds.bird_app.repository.GroupRepository;
import com.birds.bird_app.repository.UserRepository;

@Service
public class GroupDataService implements GroupService {
    
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<GroupEntity> getAllGroups() {
        return groupRepository.findAll();
    }

    @Override
    public Optional<GroupEntity> getGroupById(Long id) {
        return groupRepository.findById(id);
    }

    @Override
    public GroupEntity createGroup(GroupModel group) {
        GroupEntity entity = new GroupEntity();
        entity.setName(group.getName());
        entity.setDescription(group.getDescription());
        
        // Convert UserModel to UserEntity for founder
        UserEntity founder = userRepository.findById(Long.valueOf(group.getFounder().getId()))
            .orElseThrow(() -> new IllegalArgumentException("Founder not found"));
        entity.setFounder(founder);
        
        // Convert UserModel to UserEntity for birdKeeper
        UserEntity birdKeeper = userRepository.findById(Long.valueOf(group.getBirdKeeper().getId()))
            .orElseThrow(() -> new IllegalArgumentException("Bird Keeper not found"));
        entity.setBirdKeeper(birdKeeper);
        
        return groupRepository.save(entity);
    }

    @Override
    public Optional<GroupEntity> updateGroup(Long id, GroupModel group) {
        return groupRepository.findById(id)
            .map(existingGroup -> {
                existingGroup.setName(group.getName());
                existingGroup.setDescription(group.getDescription());
                
                // Convert UserModel to UserEntity for birdKeeper
                UserEntity birdKeeper = userRepository.findById(Long.valueOf(group.getBirdKeeper().getId()))
                    .orElseThrow(() -> new IllegalArgumentException("Bird Keeper not found"));
                existingGroup.setBirdKeeper(birdKeeper);
                
                return groupRepository.save(existingGroup);
            });
    }

    @Override
    public boolean deleteGroup(Long id) {
        if (groupRepository.existsById(id)) {
            groupRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<GroupEntity> searchGroups(String query) {
        return groupRepository.findByNameContainingOrDescriptionContaining(query);
    }

    @Override
    public List<GroupEntity> getGroupsByRegion(String region) {
        return groupRepository.findBySettingsRegion(region);
    }

    @Override
    public List<GroupEntity> getGroupsByVisibility(String visibility) {
        return groupRepository.findBySettingsVisibilityType(visibility);
    }

    @Override
    public List<GroupEntity> getGroupsByDifficulty(String difficulty) {
        return groupRepository.findBySettingsDifficultyLevel(difficulty);
    }

    @Override
    public List<GroupEntity> getGroupsByMeetingTime(String preferredTime) {
        return groupRepository.findBySettingsPreferredTime(preferredTime);
    }

    @Override
    public List<GroupEntity> getGroupsByMeetingFrequency(String frequency) {
        return groupRepository.findBySettingsMeetingFrequency(frequency);
    }

    @Override
    public List<GroupEntity> getGroupsBySeason(String season) {
        return groupRepository.findBySettingsSeasonalActivity(season);
    }

    @Override
    public List<GroupEntity> getGroupsByFeature(String feature) {
        switch (feature) {
            case "PHOTO_SHARING":
                return groupRepository.findBySettingsPhotoSharingEnabledTrue();
            case "GUEST_ALLOWED":
                return groupRepository.findBySettingsGuestViewersAllowedTrue();
            case "VERIFICATION":
                return groupRepository.findBySettingsVerificationRequiredTrue();
            default:
                return List.of();
        }
    }

    @Override
    public List<GroupEntity> getGroupsByMembershipType(String membershipType) {
        return groupRepository.findBySettingsAutoApproveMembership(
            membershipType.equals("AUTO_APPROVE")
        );
    }
}
