package com.birds.bird_app.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.birds.bird_app.model.BirdEntity;
import com.birds.bird_app.model.UserEntity;
import com.birds.bird_app.repository.BirdRepository;
import com.birds.bird_app.repository.UserRepository;

class BirdDataServiceTest {

    @Mock
    private BirdRepository birdRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BirdServiceImpl birdService;

    private UserEntity testUser;
    private BirdEntity testBird;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create test user
        testUser = new UserEntity();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setRole("USER");

        // Create test bird
        testBird = new BirdEntity("TestBird", "TestKind", "TestColor", 1, "TestFun", "url1");
        testBird.setUploadedBy(testUser);
    }

    @Test
    void testGetAllBirds() {
        List<BirdEntity> expectedBirds = Arrays.asList(testBird);
        when(birdRepository.findAll()).thenReturn(expectedBirds);

        List<BirdEntity> actualBirds = birdService.getAllBirds();
        assertEquals(expectedBirds, actualBirds);
        verify(birdRepository).findAll();
    }

    @Test
    void testGetBirdById() {
        when(birdRepository.findById(1L)).thenReturn(Optional.of(testBird));

        Optional<BirdEntity> actualBird = birdService.getBirdById(1L);
        assertTrue(actualBird.isPresent());
        assertEquals(testBird, actualBird.get());
        verify(birdRepository).findById(1L);
    }

    @Test
    void testSearchBirds() {
        String query = "Test";
        List<BirdEntity> expectedBirds = Arrays.asList(testBird);
        when(birdRepository.findByNameContainingIgnoreCaseOrKindContainingIgnoreCase(query, query))
            .thenReturn(expectedBirds);

        List<BirdEntity> actualBirds = birdService.searchBirds(query);
        assertEquals(expectedBirds, actualBirds);
        verify(birdRepository).findByNameContainingIgnoreCaseOrKindContainingIgnoreCase(query, query);
    }

    @Test
    void testGetTrendingBirds() {
        List<BirdEntity> expectedBirds = Arrays.asList(testBird);
        when(birdRepository.findTrendingBirds(any(), any())).thenReturn(expectedBirds);

        List<BirdEntity> actualBirds = birdService.getTrendingBirds(10);
        assertEquals(expectedBirds, actualBirds);
        verify(birdRepository).findTrendingBirds(any(), any());
    }
} 