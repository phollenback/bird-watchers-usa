package com.birds.bird_app.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.birds.bird_app.model.BirdEntity;
import com.birds.bird_app.model.UserEntity;

@Repository
public interface BirdRepository extends JpaRepository<BirdEntity, Long> {
    List<BirdEntity> findByNameContainingIgnoreCaseOrKindContainingIgnoreCase(String name, String kind);
    List<BirdEntity> findByUploadedByOrderByUploadedAtDesc(UserEntity user);
    List<BirdEntity> findByUploadedByAndNameContainingIgnoreCaseOrderByUploadedAtDesc(UserEntity user, String name);

    @Query("SELECT b FROM BirdEntity b " +
           "WHERE b.uploadedAt >= :since " +
           "ORDER BY (SELECT COUNT(a) FROM UserActivity a WHERE a.bird = b) DESC")
    List<BirdEntity> findTrendingBirds(@Param("since") LocalDateTime since, Pageable pageable);
}