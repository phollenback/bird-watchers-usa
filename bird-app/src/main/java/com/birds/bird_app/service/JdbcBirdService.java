package com.birds.bird_app.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.birds.bird_app.data.BirdsDAO;
import com.birds.bird_app.model.BirdModel;

@Service
public class JdbcBirdService implements BirdService {

    private BirdsDAO birdsDAO;

    @Autowired
    public void setBirdsDAO(BirdsDAO birdsDAO) {
        this.birdsDAO = birdsDAO;
    }

    @Override
    public List<BirdModel> getAllBirds() {
        return birdsDAO.findAll();
    }

    @Override
    public Optional<BirdModel> getBirdById(Long id) {
        return birdsDAO.findById(id);
    }

    @Override
    public BirdModel createBird(BirdModel bird) {
        return birdsDAO.save(bird);
    }

    @Override
    public Optional<BirdModel> updateBird(Long id, BirdModel bird) {
        if (birdsDAO.update(bird)) {
            return Optional.of(bird);
        }
        return Optional.empty();
    }

    @Override
    public boolean deleteBird(Long id) {
        return birdsDAO.delete(id);
    }

    @Override
    public List<BirdModel> searchBirds(String query) {
        return birdsDAO.search(query);
    }
} 