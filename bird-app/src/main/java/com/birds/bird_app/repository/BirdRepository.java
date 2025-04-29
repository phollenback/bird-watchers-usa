package com.birds.bird_app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.birds.bird_app.model.BirdEntity;

@Repository
public interface BirdRepository extends JpaRepository<BirdEntity, Long> {
    List<BirdEntity> findByNameContainingIgnoreCaseOrKindContainingIgnoreCase(String name, String kind);
}