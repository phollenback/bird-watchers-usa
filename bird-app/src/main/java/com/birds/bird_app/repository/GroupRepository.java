package com.birds.bird_app.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.birds.bird_app.model.GroupEntity;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {
    
    @Query("SELECT g FROM GroupEntity g WHERE g.name LIKE %:query% OR g.description LIKE %:query%")
    List<GroupEntity> findByNameContainingOrDescriptionContaining(@Param("query") String query);
    
    @Query("SELECT g FROM GroupEntity g WHERE g.settings.region = :region")
    List<GroupEntity> findBySettingsRegion(@Param("region") String region);
    
    @Query("SELECT g FROM GroupEntity g WHERE g.settings.visibilityType = :visibilityType")
    List<GroupEntity> findBySettingsVisibilityType(@Param("visibilityType") String visibilityType);
    
    @Query("SELECT g FROM GroupEntity g WHERE g.settings.meetingFrequency = :frequency")
    List<GroupEntity> findBySettingsMeetingFrequency(@Param("frequency") String frequency);
    
    @Query("SELECT g FROM GroupEntity g WHERE g.settings.seasonalActivity = :season")
    List<GroupEntity> findBySettingsSeasonalActivity(@Param("season") String season);
    
    @Query("SELECT g FROM GroupEntity g WHERE g.settings.photoSharingEnabled = true")
    List<GroupEntity> findBySettingsPhotoSharingEnabledTrue();
    
    @Query("SELECT g FROM GroupEntity g WHERE g.settings.guestViewersAllowed = true")
    List<GroupEntity> findBySettingsGuestViewersAllowedTrue();
    
    @Query("SELECT g FROM GroupEntity g WHERE g.settings.verificationRequired = true")
    List<GroupEntity> findBySettingsVerificationRequiredTrue();
    
    @Query("SELECT g FROM GroupEntity g WHERE g.settings.autoApproveMembership = :autoApprove")
    List<GroupEntity> findBySettingsAutoApproveMembership(@Param("autoApprove") boolean autoApprove);

    @Query("SELECT g FROM GroupEntity g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<GroupEntity> findByNameContainingIgnoreCase(String name);

    @Query("SELECT g FROM GroupEntity g " +
           "WHERE EXISTS (SELECT a FROM UserActivity a WHERE a.group = g AND a.createdAt >= :since) " +
           "ORDER BY (SELECT COUNT(a) FROM UserActivity a WHERE a.group = g AND a.createdAt >= :since) DESC")
    List<GroupEntity> findActiveGroups(@Param("since") LocalDateTime since, Pageable pageable);
}
