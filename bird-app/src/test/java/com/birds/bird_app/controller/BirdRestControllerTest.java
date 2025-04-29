package com.birds.bird_app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.birds.bird_app.config.SecurityConfig;
import com.birds.bird_app.config.TestSecurityConfig;
import com.birds.bird_app.model.BirdEntity;
import com.birds.bird_app.service.BirdService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(BirdRestController.class)
@Import({SecurityConfig.class, TestSecurityConfig.class})
@WithMockUser(roles = "ADMIN")
class BirdRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BirdService birdService;

    @Autowired
    private ObjectMapper objectMapper;

    private BirdEntity testBird;

    @BeforeEach
    void setUp() {
        testBird = new BirdEntity("TestBird", "TestKind", "TestColor", 1, "TestFun", "testUrl");
    }

    @Test
    void testGetAllBirds() throws Exception {
        when(birdService.getAllBirds()).thenReturn(Arrays.asList(testBird));

        mockMvc.perform(get("/api/birds"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("TestBird"))
            .andExpect(jsonPath("$[0].kind").value("TestKind"));
    }

    @Test
    void testGetBirdById() throws Exception {
        when(birdService.getBirdById(1L)).thenReturn(Optional.of(testBird));

        mockMvc.perform(get("/api/birds/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("TestBird"))
            .andExpect(jsonPath("$.kind").value("TestKind"));
    }

    @Test
    void testCreateBird() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
            "image", 
            "test.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );

        MockMultipartFile birdJson = new MockMultipartFile(
            "bird",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsString(testBird).getBytes()
        );

        when(birdService.createBird(any(BirdEntity.class), any())).thenReturn(testBird);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/birds")
            .file(image)
            .file(birdJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("TestBird"))
            .andExpect(jsonPath("$.kind").value("TestKind"));
    }

    @Test
    void testUpdateBird() throws Exception {
        when(birdService.updateBird(eq(1L), any(BirdEntity.class))).thenReturn(Optional.of(testBird));

        mockMvc.perform(put("/api/birds/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testBird)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("TestBird"))
            .andExpect(jsonPath("$.kind").value("TestKind"));
    }

    @Test
    void testDeleteBird() throws Exception {
        when(birdService.deleteBird(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/birds/1"))
            .andExpect(status().isOk());
    }
}