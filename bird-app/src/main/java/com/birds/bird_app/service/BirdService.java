package com.birds.bird_app.service;

import java.util.List;
import java.util.Optional;

import com.birds.bird_app.model.BirdModel;

public interface BirdService {
    List<BirdModel> getAllBirds();
    Optional<BirdModel> getBirdById(Long id);
    BirdModel createBird(BirdModel bird);
    Optional<BirdModel> updateBird(Long id, BirdModel bird);
    boolean deleteBird(Long id);
    List<BirdModel> searchBirds(String query);
}
