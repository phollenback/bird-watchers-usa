package com.birds.bird_app.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
@Entity
@Table(name = "group_settings")
public class GroupSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "region")
    private String region;

    @URL
    @Column(name = "group_image_url", length = 1000)
    private String groupImageUrl;

    @Column(name = "visibility_type")
    @Enumerated(EnumType.STRING)
    private VisibilityType visibilityType = VisibilityType.PUBLIC;

    @Column(name = "difficulty_level")
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel = DifficultyLevel.BEGINNER;

    @Column(name = "preferred_time")
    @Enumerated(EnumType.STRING)
    private PreferredTime preferredTime = PreferredTime.MORNING;

    @Column(name = "meeting_frequency")
    @Enumerated(EnumType.STRING)
    private MeetingFrequency meetingFrequency = MeetingFrequency.WEEKLY;

    @Column(name = "seasonal_activity")
    @Enumerated(EnumType.STRING)
    private Season seasonalActivity = Season.SPRING;

    @Column(name = "photo_sharing_enabled")
    private boolean photoSharingEnabled = true;

    @Column(name = "guest_viewers_allowed")
    private boolean guestViewersAllowed = true;

    @Column(name = "verification_required")
    private boolean verificationRequired = false;

    @Column(name = "auto_approve_membership")
    private boolean autoApproveMembership = true;

    @Column(name = "theme")
    private String theme = "forest";

    @OneToOne(mappedBy = "settings")
    private GroupEntity group;

    public enum VisibilityType {
        PUBLIC, PRIVATE, INVITE_ONLY
    }

    public enum DifficultyLevel {
        BEGINNER, INTERMEDIATE, EXPERT
    }

    public enum PreferredTime {
        MORNING, AFTERNOON, EVENING
    }

    public enum MeetingFrequency {
        WEEKLY, MONTHLY, CUSTOM
    }

    public enum Season {
        SPRING, SUMMER, FALL, WINTER
    }

    // Constructor with default values
    public GroupSettings() {
        this.visibilityType = VisibilityType.PUBLIC;
        this.difficultyLevel = DifficultyLevel.BEGINNER;
        this.preferredTime = PreferredTime.MORNING;
        this.meetingFrequency = MeetingFrequency.WEEKLY;
        this.seasonalActivity = Season.SPRING;
        this.photoSharingEnabled = true;
        this.guestViewersAllowed = true;
        this.verificationRequired = false;
        this.autoApproveMembership = true;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public VisibilityType getVisibilityType() {
        return visibilityType;
    }

    public PreferredTime getPreferredTime() {
        return preferredTime;
    }

    public MeetingFrequency getMeetingFrequency() {
        return meetingFrequency;
    }

    public Season getSeasonalActivity() {
        return seasonalActivity;
    }

    public String getRegion() {
        return region;
    }

    public boolean isPhotoSharingEnabled() {
        return photoSharingEnabled;
    }

    public boolean isGuestViewersAllowed() {
        return guestViewersAllowed;
    }

    public boolean isVerificationRequired() {
        return verificationRequired;
    }

    public boolean isAutoApproveMembership() {
        return autoApproveMembership;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setVisibilityType(VisibilityType visibilityType) {
        this.visibilityType = visibilityType;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public void setPreferredTime(PreferredTime preferredTime) {
        this.preferredTime = preferredTime;
    }

    public void setMeetingFrequency(MeetingFrequency meetingFrequency) {
        this.meetingFrequency = meetingFrequency;
    }

    public void setSeasonalActivity(Season seasonalActivity) {
        this.seasonalActivity = seasonalActivity;
    }

    public void setPhotoSharingEnabled(boolean photoSharingEnabled) {
        this.photoSharingEnabled = photoSharingEnabled;
    }

    public void setGuestViewersAllowed(boolean guestViewersAllowed) {
        this.guestViewersAllowed = guestViewersAllowed;
    }

    public void setVerificationRequired(boolean verificationRequired) {
        this.verificationRequired = verificationRequired;
    }

    public void setAutoApproveMembership(boolean autoApproveMembership) {
        this.autoApproveMembership = autoApproveMembership;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getGroupImageUrl() {
        return groupImageUrl;
    }

    public void setGroupImageUrl(String groupImageUrl) {
        this.groupImageUrl = groupImageUrl;
    }

    @Override
    public String toString() {
        return "GroupSettings{" +
            "theme='" + theme + '\'' +
            ", region='" + region + '\'' +
            ", visibilityType=" + visibilityType +
            ", difficultyLevel=" + difficultyLevel +
            ", preferredTime=" + preferredTime +
            ", meetingFrequency=" + meetingFrequency +
            ", seasonalActivity=" + seasonalActivity +
            ", photoSharingEnabled=" + photoSharingEnabled +
            ", guestViewersAllowed=" + guestViewersAllowed +
            ", verificationRequired=" + verificationRequired +
            ", autoApproveMembership=" + autoApproveMembership +
            '}';
    }
} 