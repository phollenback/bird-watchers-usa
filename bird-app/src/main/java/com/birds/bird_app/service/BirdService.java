package com.birds.bird_app.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.birds.bird_app.model.BirdEntity;

public interface BirdService {
    List<BirdEntity> getAllBirds();
    
    Optional<BirdEntity> getBirdById(Long id);
    
    BirdEntity createBird(BirdEntity bird, MultipartFile image) throws IOException;
    
    Optional<BirdEntity> updateBird(Long id, BirdEntity bird);
    
    boolean deleteBird(Long id);
    
    List<BirdEntity> searchBirds(String query);
}

