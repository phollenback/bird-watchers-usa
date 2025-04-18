package com.birds.bird_app.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.birds.bird_app.data.BirdsDAO;
import com.birds.bird_app.model.BirdModel;

@ExtendWith(MockitoExtension.class)
class BirdDataServiceTest {
    
    @Mock
    private BirdsDAO birdsDAO;
    
    private BirdDataServiceImpl birdDataService;
    private BirdModel cardinal;
    private BirdModel blueJay;

    @BeforeEach
    void setUp() {
        birdDataService = new BirdDataServiceImpl();
        birdDataService.setBirdsDAO(birdsDAO);
        
        cardinal = new BirdModel("Cardinal", "Northern Cardinal", "Red", 3, "State bird of seven states", "https://example.com/cardinal.jpg");
        cardinal.setId(1L);
        
        blueJay = new BirdModel("Blue Jay", "Blue Jay", "Blue", 2, "Known for intelligence", "https://example.com/bluejay.jpg");
        blueJay.setId(2L);
    }

    @Test
    void getAllBirds_ShouldReturnAllBirds() {
        // Given
        when(birdsDAO.findAll()).thenReturn(Arrays.asList(cardinal, blueJay));

        // When
        List<BirdModel> birds = birdDataService.getAllBirds();

        // Then
        assertNotNull(birds);
        assertEquals(2, birds.size());
        assertTrue(birds.stream().anyMatch(bird -> "Cardinal".equals(bird.getName())));
        assertTrue(birds.stream().anyMatch(bird -> "Blue Jay".equals(bird.getName())));
    }

    @Test
    void getBirdById_WithValidId_ShouldReturnBird() {
        // Given
        when(birdsDAO.findById(1L)).thenReturn(Optional.of(cardinal));

        // When
        var bird = birdDataService.getBirdById(1L);

        // Then
        assertTrue(bird.isPresent());
        assertEquals("Cardinal", bird.get().getName());
    }

    @Test
    void getBirdById_WithInvalidId_ShouldReturnEmpty() {
        // Given
        when(birdsDAO.findById(999L)).thenReturn(Optional.empty());

        // When
        var bird = birdDataService.getBirdById(999L);

        // Then
        assertTrue(bird.isEmpty());
    }

    @Test
    void createBird_ShouldAddNewBird() {
        // Given
        BirdModel newBird = new BirdModel("Owl", "Great Horned Owl", "Brown", 5, "Can rotate head 270 degrees", "https://example.com/owl.jpg");
        when(birdsDAO.save(any(BirdModel.class))).thenReturn(newBird);

        // When
        BirdModel created = birdDataService.createBird(newBird);

        // Then
        assertEquals("Owl", created.getName());
    }

    @Test
    void updateBird_WithValidId_ShouldUpdateBird() {
        // Given
        BirdModel updatedBird = new BirdModel("Cardinal Updated", "Northern Cardinal", "Red", 4, "Updated fact", "https://example.com/cardinal.jpg");
        when(birdsDAO.update(any(BirdModel.class))).thenReturn(true);

        // When
        var result = birdDataService.updateBird(1L, updatedBird);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Cardinal Updated", result.get().getName());
    }

    @Test
    void updateBird_WithInvalidId_ShouldReturnEmpty() {
        // Given
        when(birdsDAO.update(any(BirdModel.class))).thenReturn(false);
        BirdModel updatedBird = new BirdModel("Invalid", "Test", "Red", 1, "Test", "https://example.com/test.jpg");

        // When
        var result = birdDataService.updateBird(999L, updatedBird);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteBird_WithValidId_ShouldDeleteBird() {
        // Given
        when(birdsDAO.delete(1L)).thenReturn(true);

        // When
        boolean deleted = birdDataService.deleteBird(1L);

        // Then
        assertTrue(deleted);
    }

    @Test
    void deleteBird_WithInvalidId_ShouldReturnFalse() {
        // Given
        when(birdsDAO.delete(999L)).thenReturn(false);

        // When
        boolean deleted = birdDataService.deleteBird(999L);

        // Then
        assertFalse(deleted);
    }
} 