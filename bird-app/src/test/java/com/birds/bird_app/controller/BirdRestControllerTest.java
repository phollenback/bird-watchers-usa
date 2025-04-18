package com.birds.bird_app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.birds.bird_app.model.BirdModel;
import com.birds.bird_app.service.BirdDataService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class BirdRestControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockBean
    private BirdDataService birdDataService;

    @Autowired
    private ObjectMapper objectMapper;

    private BirdModel testBird;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
            
        testBird = new BirdModel("Test Bird", "Test Species", "Brown", 2, "Test fact", "https://example.com/test.jpg");
        testBird.setId(1L);
    }

    @Test
    @WithMockUser
    void getAllBirds_ShouldReturnBirdList() throws Exception {
        when(birdDataService.getAllBirds())
            .thenReturn(Arrays.asList(testBird));

        mockMvc.perform(get("/api/birds"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Test Bird"))
            .andExpect(jsonPath("$[0].kind").value("Test Species"));
    }

    @Test
    @WithMockUser
    void getBirdById_WithValidId_ShouldReturnBird() throws Exception {
        when(birdDataService.getBirdById(1L))
            .thenReturn(Optional.of(testBird));

        mockMvc.perform(get("/api/birds/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Test Bird"))
            .andExpect(jsonPath("$.kind").value("Test Species"));
    }

    @Test
    @WithMockUser
    void getBirdById_WithInvalidId_ShouldReturn404() throws Exception {
        when(birdDataService.getBirdById(999L))
            .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/birds/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void createBird_WithValidData_ShouldReturnCreatedBird() throws Exception {
        when(birdDataService.createBird(any(BirdModel.class)))
            .thenReturn(testBird);

        mockMvc.perform(post("/api/birds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBird)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Test Bird"))
            .andExpect(jsonPath("$.kind").value("Test Species"));
    }

    @Test
    @WithMockUser
    void updateBird_WithValidData_ShouldReturnUpdatedBird() throws Exception {
        when(birdDataService.updateBird(eq(1L), any(BirdModel.class)))
            .thenReturn(Optional.of(testBird));

        mockMvc.perform(put("/api/birds/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBird)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Test Bird"));
    }

    @Test
    @WithMockUser
    void updateBird_WithInvalidId_ShouldReturn404() throws Exception {
        when(birdDataService.updateBird(eq(999L), any(BirdModel.class)))
            .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/birds/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBird)))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deleteBird_WithValidId_ShouldReturn200() throws Exception {
        when(birdDataService.deleteBird(1L))
            .thenReturn(true);

        mockMvc.perform(delete("/api/birds/1"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void deleteBird_WithInvalidId_ShouldReturn404() throws Exception {
        when(birdDataService.deleteBird(999L))
            .thenReturn(false);

        mockMvc.perform(delete("/api/birds/999"))
            .andExpect(status().isNotFound());
    }
}