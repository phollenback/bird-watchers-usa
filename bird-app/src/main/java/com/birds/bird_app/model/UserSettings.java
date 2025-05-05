package com.birds.bird_app.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "user_settings")
@Data
public class UserSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private UserEntity user;

    @Column(name = "theme", nullable = false)
    private String theme = "default";

    @Column(name = "email_notifications", nullable = false)
    private boolean emailNotifications = true;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Explicit getters
    public UserEntity getUser() {
        return user;
    }

    public String getTheme() {
        return theme;
    }

    public boolean isEmailNotifications() {
        return emailNotifications;
    }

    public boolean isPublic() {
        return isPublic;
    }

    // Explicit setters for fields that need them
    public void setUser(UserEntity user) {
        this.user = user;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public void setEmailNotifications(boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
} 