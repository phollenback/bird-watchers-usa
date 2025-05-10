package com.birds.bird_app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "bird_submissions")
public class BirdSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity submittedBy;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @Column(nullable = false)
    private String status; // ACTIVE, WINNER, ARCHIVED

    @Column
    private Integer votes;

    @Column(nullable = false)
    private String birdName;

    @Column(name = "is_big_bird", nullable = false)
    private boolean isBigBird = false;

    @ManyToMany
    @JoinTable(
        name = "submission_votes",
        joinColumns = @JoinColumn(name = "submission_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<UserEntity> votedBy = new HashSet<>();

    @Transient
    private boolean hasVoted = false;

    // Default constructor with initialization
    public BirdSubmission() {
        this.votedBy = new HashSet<>();
        this.votes = 0;
        this.isBigBird = false;
        this.submittedAt = LocalDateTime.now();
        this.status = "ACTIVE";
        this.hasVoted = false;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GroupEntity getGroup() {
        return group;
    }

    public void setGroup(GroupEntity group) {
        this.group = group;
    }

    public UserEntity getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(UserEntity submittedBy) {
        this.submittedBy = submittedBy;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getVotes() {
        return votes;
    }

    public void setVotes(Integer votes) {
        this.votes = votes;
    }

    public String getBirdName() {
        return birdName;
    }

    public void setBirdName(String birdName) {
        this.birdName = birdName;
    }

    public boolean isBigBird() {
        return isBigBird;
    }

    public void setBigBird(boolean isBigBird) {
        this.isBigBird = isBigBird;
    }

    public Set<UserEntity> getVotedBy() {
        return votedBy;
    }

    public void setVotedBy(Set<UserEntity> votedBy) {
        this.votedBy = votedBy;
    }

    public boolean isHasVoted() {
        return hasVoted;
    }

    public void setHasVoted(boolean hasVoted) {
        this.hasVoted = hasVoted;
    }
} 