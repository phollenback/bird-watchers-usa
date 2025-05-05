package com.birds.bird_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.birds.bird_app.model.TestimonialModel;

@Repository
public interface TestimonialRepository extends JpaRepository<TestimonialModel, Long> {
} 