package com.birds.bird_app.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Size;

@Data
@Entity
@Table(name = "bird_groups")
public class GroupEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    @Column(nullable = false)
    private String name;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "founder_id", nullable = false)
    private UserEntity founder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bird_keeper_id", nullable = false)
    private UserEntity birdKeeper;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Size(max = 500, message = "Description must be less than 500 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "keeper_id")
    private Long keeperId;

    @Column(name = "member_count")
    private Integer memberCount = 0;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "settings_id")
    private GroupSettings settings;

    @OneToMany(mappedBy = "group", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<GroupMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private List<BirdSubmission> birdSubmissions = new ArrayList<>();

    public int getActiveBirdCount() {
        return (int) birdSubmissions.stream()
            .filter(submission -> "ACTIVE".equals(submission.getStatus()))
            .count();
    }

    public GroupSettings getSettings() {
        return settings;
    }

    public void setSettings(GroupSettings settings) {
        this.settings = settings;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getId() {
        return id;
    }

    public UserEntity getFounder() {
        return founder;
    }

    public UserEntity getBirdKeeper() {
        return birdKeeper;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<GroupMember> getMembers() {
        return members;
    }

    public void setMembers(List<GroupMember> members) {
        this.members = members;
    }

    // Default constructor
    public GroupEntity() {
        this.settings = new GroupSettings();
        this.memberCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFounder(UserEntity founder) {
        this.founder = founder;
    }

    public void setBirdKeeper(UserEntity birdKeeper) {
        this.birdKeeper = birdKeeper;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    @Override
public String toString() {
    return "GroupEntity{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", memberCount=" + memberCount +
        // Don't include full settings object to avoid recursion
        ", settingsTheme='" + (settings != null ? settings.getTheme() : "null") + '\'' +
        '}';
}
}