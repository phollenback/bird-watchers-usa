package com.birds.bird_app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.birds.bird_app.model.BirdSubmission;
import com.birds.bird_app.model.GroupEntity;
import com.birds.bird_app.model.UserEntity;

@Repository
public interface BirdSubmissionRepository extends JpaRepository<BirdSubmission, Long> {
    List<BirdSubmission> findByGroupAndStatusOrderByVotesDescSubmittedAtDesc(GroupEntity group, String status);
    Optional<BirdSubmission> findByGroupAndStatusAndSubmittedBy(GroupEntity group, String status, UserEntity submittedBy);
    List<BirdSubmission> findByStatus(String status);
    List<BirdSubmission> findByGroupAndStatus(GroupEntity group, String status);
    List<BirdSubmission> findByGroupAndStatusAndIsBigBirdTrueOrderBySubmittedAtDesc(GroupEntity group, String status);
    List<BirdSubmission> findByGroupAndStatusInOrderByVotesDescSubmittedAtDesc(GroupEntity group, List<String> statuses);
} 