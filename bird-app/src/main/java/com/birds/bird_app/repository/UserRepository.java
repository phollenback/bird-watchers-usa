package com.birds.bird_app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.birds.bird_app.model.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
} 