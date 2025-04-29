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
import org.springframework.web.multipart.MultipartFile;

import com.birds.bird_app.model.BirdEntity;
import com.birds.bird_app.repository.BirdRepository;

class BirdDataServiceTest {

    @Mock
    private BirdRepository birdRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private ImageVerificationService imageVerificationService;

    @InjectMocks
    private BirdServiceImpl birdService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllBirds() {
        List<BirdEntity> expectedBirds = Arrays.asList(
            new BirdEntity("Bird1", "Kind1", "Color1", 1, "Fun1", "url1"),
            new BirdEntity("Bird2", "Kind2", "Color2", 2, "Fun2", "url2")
        );
        when(birdRepository.findAll()).thenReturn(expectedBirds);

        List<BirdEntity> actualBirds = birdService.getAllBirds();
        assertEquals(expectedBirds, actualBirds);
        verify(birdRepository).findAll();
    }

    @Test
    void testGetBirdById() {
        BirdEntity expectedBird = new BirdEntity("Bird1", "Kind1", "Color1", 1, "Fun1", "url1");
        when(birdRepository.findById(1L)).thenReturn(Optional.of(expectedBird));

        Optional<BirdEntity> actualBird = birdService.getBirdById(1L);
        assertTrue(actualBird.isPresent());
        assertEquals(expectedBird, actualBird.get());
        verify(birdRepository).findById(1L);
    }

    @Test
    void testCreateBird() throws Exception {
        BirdEntity bird = new BirdEntity("Bird1", "Kind1", "Color1", 1, "Fun1", null);
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(imageVerificationService.isBirdImage(image)).thenReturn(true);
        when(s3Service.uploadFile(image)).thenReturn("http://s3.url/image.jpg");
        when(birdRepository.save(any(BirdEntity.class))).thenReturn(bird);

        BirdEntity createdBird = birdService.createBird(bird, image);
        assertNotNull(createdBird);
        assertEquals("http://s3.url/image.jpg", createdBird.getImageUrl());
        verify(s3Service).uploadFile(image);
        verify(birdRepository).save(bird);
    }

    @Test
    void testUpdateBird() {
        BirdEntity existingBird = new BirdEntity("OldBird", "OldKind", "OldColor", 1, "OldFun", "oldUrl");
        BirdEntity updatedBird = new BirdEntity("NewBird", "NewKind", "NewColor", 2, "NewFun", "newUrl");
        when(birdRepository.findById(1L)).thenReturn(Optional.of(existingBird));
        when(birdRepository.save(any(BirdEntity.class))).thenReturn(updatedBird);

        Optional<BirdEntity> result = birdService.updateBird(1L, updatedBird);
        assertTrue(result.isPresent());
        assertEquals(updatedBird.getName(), result.get().getName());
        assertEquals(updatedBird.getKind(), result.get().getKind());
        verify(birdRepository).findById(1L);
        verify(birdRepository).save(any(BirdEntity.class));
    }

    @Test
    void testDeleteBird() {
        when(birdRepository.existsById(1L)).thenReturn(true);
        boolean result = birdService.deleteBird(1L);
        assertTrue(result);
        verify(birdRepository).deleteById(1L);
        verify(birdRepository).existsById(1L);
    }

    @Test
    void testSearchBirds() {
        String query = "test";
        List<BirdEntity> expectedBirds = Arrays.asList(
            new BirdEntity("TestBird", "TestKind", "Color1", 1, "Fun1", "url1")
        );
        when(birdRepository.findByNameContainingIgnoreCaseOrKindContainingIgnoreCase(query, query))
            .thenReturn(expectedBirds);

        List<BirdEntity> actualBirds = birdService.searchBirds(query);
        assertEquals(expectedBirds, actualBirds);
        verify(birdRepository).findByNameContainingIgnoreCaseOrKindContainingIgnoreCase(query, query);
    }
} 