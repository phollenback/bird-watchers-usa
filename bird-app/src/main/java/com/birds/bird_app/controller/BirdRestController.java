package com.birds.bird_app.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.birds.bird_app.model.BirdEntity;
import com.birds.bird_app.service.BirdService;

@RestController
@RequestMapping("/api/birds")
public class BirdRestController {
    
    private final BirdService birdService;

    public BirdRestController(BirdService birdService) {
        this.birdService = birdService;
    }

    @GetMapping
    public List<BirdEntity> getAllBirds() {
        return birdService.getAllBirds();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BirdEntity> getBirdById(@PathVariable Long id) {
        return birdService.getBirdById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BirdEntity> createBird(
        @RequestPart("bird") BirdEntity bird,
        @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        try {
            BirdEntity createdBird = birdService.createBird(bird, image);
            return ResponseEntity.ok(createdBird);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<BirdEntity> updateBird(@PathVariable Long id, @RequestBody BirdEntity bird) {
        return birdService.updateBird(id, bird)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBird(@PathVariable Long id) {
        if (birdService.deleteBird(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}