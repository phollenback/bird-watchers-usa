package com.birds.bird_app.service;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import com.birds.bird_app.model.GroupEntity;
import com.birds.bird_app.model.GroupModel;
import org.springframework.data.domain.PageRequest;

public interface GroupService {
    List<GroupEntity> getAllGroups();
    Optional<GroupEntity> getGroupById(Long id);
    GroupEntity createGroup(GroupModel group);
    Optional<GroupEntity> updateGroup(Long id, GroupModel group);
    boolean deleteGroup(Long id);
    List<GroupEntity> searchGroups(String query);
    List<GroupEntity> getGroupsByRegion(String region);
    List<GroupEntity> getGroupsByVisibility(String visibility);
    List<GroupEntity> getGroupsByVisibilityType(String visibilityType);
    List<GroupEntity> getGroupsByMeetingFrequency(String frequency);
    List<GroupEntity> getGroupsBySeason(String season);
    List<GroupEntity> getGroupsByFeature(String feature);
    List<GroupEntity> getGroupsByMembershipType(String membershipType);
    List<GroupEntity> getActiveGroups(int limit);
} 