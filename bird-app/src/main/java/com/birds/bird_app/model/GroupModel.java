package com.birds.bird_app.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class GroupModel {

    private Long id;

    @NotBlank(message = "Group name is required")
    private String name;
    
    @NotNull(message = "Founder is required")
    private UserModel founder;
    
    @NotNull(message = "Bird keeper is required")
    private UserModel birdKeeper;
    
    private String description;
    private GroupSettings settings;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public GroupModel() {}

    // Constructor with required fields
    public GroupModel(String name, UserModel founder, UserModel birdKeeper) {
        this.name = name;
        this.founder = founder;
        this.birdKeeper = birdKeeper;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Static method to convert from Entity to Model
    public static GroupModel fromEntity(GroupEntity entity) {
        GroupModel model = new GroupModel();
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setFounder(new UserModel(
            entity.getFounder().getEmail(),
            entity.getFounder().getName(),
            entity.getFounder().getPassword()
        ));
        model.setBirdKeeper(new UserModel(
            entity.getBirdKeeper().getEmail(),
            entity.getBirdKeeper().getName(),
            entity.getBirdKeeper().getPassword()
        ));
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());
        return model;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UserModel getFounder() {
        return founder;
    }

    public void setFounder(UserModel founder) {
        this.founder = founder;
    }

    public UserModel getBirdKeeper() {
        return birdKeeper;
    }

    public void setBirdKeeper(UserModel birdKeeper) {
        this.birdKeeper = birdKeeper;
    }

    public GroupSettings getSettings() {
        return settings;
    }

    public void setSettings(GroupSettings settings) {
        this.settings = settings;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}