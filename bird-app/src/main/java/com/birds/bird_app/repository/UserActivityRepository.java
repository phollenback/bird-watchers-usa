package com.birds.bird_app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.birds.bird_app.model.UserActivity;
import com.birds.bird_app.model.UserEntity;

import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    List<UserActivity> findByUserOrderByCreatedAtDesc(UserEntity user);
    Page<UserActivity> findByUserOrderByCreatedAtDesc(UserEntity user, Pageable pageable);
    List<UserActivity> findTopByOrderByCreatedAtDesc(Pageable pageable);
} 