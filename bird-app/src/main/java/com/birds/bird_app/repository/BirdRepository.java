package com.birds.bird_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.birds.bird_app.model.BirdEntity;

@Repository
public interface BirdRepository extends JpaRepository<BirdEntity, Long> {
}