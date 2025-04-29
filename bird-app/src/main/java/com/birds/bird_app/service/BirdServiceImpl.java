package com.birds.bird_app.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.birds.bird_app.model.BirdEntity;
import com.birds.bird_app.repository.BirdRepository;

@Service
public class BirdServiceImpl implements BirdService {
    private static final Logger logger = LoggerFactory.getLogger(BirdServiceImpl.class);
    
    private final BirdRepository birdRepository;
    private final S3Service s3Service;
    private final ImageVerificationService imageVerificationService;

    @Autowired
    public BirdServiceImpl(BirdRepository birdRepository, S3Service s3Service, ImageVerificationService imageVerificationService) {
        this.birdRepository = birdRepository;
        this.s3Service = s3Service;
        this.imageVerificationService = imageVerificationService;
    }

    @Override
    public List<BirdEntity> getAllBirds() {
        logger.info("Fetching all birds");
        return birdRepository.findAll();
    }
    
    @Override
    public Optional<BirdEntity> getBirdById(Long id) {
        logger.info("Fetching bird with id: {}", id);
        return birdRepository.findById(id);
    }

    @Override
    public BirdEntity createBird(BirdEntity bird, MultipartFile image) throws IOException {
        logger.info("Creating new bird: {}", bird.getName());
        
        if (image != null && !image.isEmpty()) {
            logger.info("Processing image upload for bird: {}", bird.getName());
            logger.info("Image details - Name: {}, Size: {} bytes, Content Type: {}", 
                image.getOriginalFilename(), image.getSize(), image.getContentType());
            
            // Verify if the image is of a bird
            logger.info("Verifying if image contains a bird");
            if (!imageVerificationService.isBirdImage(image)) {
                logger.warn("Image verification failed - not a bird image");
                throw new IOException("Sorry, but that's not a bird! 🐦 Please upload a picture of a bird.");
            }
            
            logger.info("Image verified as bird, proceeding with S3 upload");
            String imageUrl = s3Service.uploadFile(image);
            logger.info("Image uploaded to S3, URL: {}", imageUrl);
            bird.setImageUrl(imageUrl);
        } else {
            logger.info("No image provided for bird: {}", bird.getName());
        }
        
        logger.info("Saving bird to database: {}", bird.getName());
        BirdEntity savedBird = birdRepository.save(bird);
        logger.info("Bird saved successfully with ID: {}", savedBird.getId());
        return savedBird;
    }

    @Override
    public Optional<BirdEntity> updateBird(Long id, BirdEntity bird) {
        logger.info("Updating bird with id: {}", id);
        return birdRepository.findById(id)
                .map(existingBird -> {
                    logger.info("Found existing bird: {}", existingBird.getName());
                    existingBird.setName(bird.getName());
                    existingBird.setKind(bird.getKind());
                    existingBird.setColor(bird.getColor());
                    existingBird.setAge(bird.getAge());
                    existingBird.setFunFact(bird.getFunFact());
                    existingBird.setImageUrl(bird.getImageUrl());
                    logger.info("Saving updated bird: {}", existingBird.getName());
                    return birdRepository.save(existingBird);
                });
    }

    @Override
    public boolean deleteBird(Long id) {
        logger.info("Attempting to delete bird with id: {}", id);
        if (birdRepository.existsById(id)) {
            logger.info("Bird found, proceeding with deletion");
        birdRepository.deleteById(id);
            return true;
        }
        logger.warn("Bird not found for deletion, id: {}", id);
        return false;
    }

    @Override
    public List<BirdEntity> searchBirds(String query) {
        logger.info("Searching birds with query: {}", query);
        return birdRepository.findByNameContainingIgnoreCaseOrKindContainingIgnoreCase(query, query);
    }
} 