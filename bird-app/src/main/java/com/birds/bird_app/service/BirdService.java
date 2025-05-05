package com.birds.bird_app.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import com.birds.bird_app.model.BirdEntity;
import com.birds.bird_app.model.UserEntity;

public interface BirdService {
    List<BirdEntity> getAllBirds();
    
    Optional<BirdEntity> getBirdById(Long id);
    
    BirdEntity createBird(BirdEntity bird, MultipartFile image, UserEntity user) throws IOException;
    
    Optional<BirdEntity> updateBird(Long id, BirdEntity bird);
    
    boolean deleteBird(Long id);
    
    List<BirdEntity> searchBirds(String query);

    List<BirdEntity> getTrendingBirds(int limit);
}

