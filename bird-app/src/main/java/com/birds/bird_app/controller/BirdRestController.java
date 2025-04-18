package com.birds.bird_app.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.birds.bird_app.model.BirdModel;
import com.birds.bird_app.service.BirdService;

@RestController
@RequestMapping("/api/birds")
public class BirdRestController {
    
    private final BirdService birdService;

    public BirdRestController(BirdService birdService) {
        this.birdService = birdService;
    }

    @GetMapping
    public List<BirdModel> getAllBirds() {
        return birdService.getAllBirds();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BirdModel> getBirdById(@PathVariable Long id) {
        return birdService.getBirdById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public BirdModel createBird(@RequestBody BirdModel bird) {
        return birdService.createBird(bird);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BirdModel> updateBird(@PathVariable Long id, @RequestBody BirdModel bird) {
        return birdService.updateBird(id, bird)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBird(@PathVariable Long id) {
            return birdService.deleteBird(id)
            ? ResponseEntity.ok().build()
            : ResponseEntity.notFound().build();
    }
}