package com.birds.bird_app.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;

import com.birds.bird_app.model.BirdEntity;
import com.birds.bird_app.model.UserEntity;
import com.birds.bird_app.service.BirdService;

@RestController
@RequestMapping("/api/birds")
public class BirdRestController {
    
    private final BirdService birdService;

    public BirdRestController(BirdService birdService) {
        this.birdService = birdService;
    }

    @GetMapping
    public ResponseEntity<List<BirdEntity>> getAllBirds(@AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(birdService.getAllBirds());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BirdEntity> getBirdById(@PathVariable Long id, @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return birdService.getBirdById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BirdEntity> createBird(
        @RequestPart("bird") BirdEntity bird,
        @RequestPart(value = "image", required = false) MultipartFile image,
        @AuthenticationPrincipal UserDetails currentUser
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            BirdEntity createdBird = birdService.createBird(bird, image, (UserEntity) currentUser);
            return ResponseEntity.ok(createdBird);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BirdEntity> updateBird(
        @PathVariable Long id,
        @RequestBody BirdEntity bird,
        @AuthenticationPrincipal UserDetails currentUser
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        if (!currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).build();
        }
        return birdService.updateBird(id, bird)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBird(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails currentUser
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        if (!currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).build();
        }
        if (birdService.deleteBird(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}