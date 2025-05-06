package com.birds.bird_app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

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

import com.birds.bird_app.config.TestSecurityConfig;
import com.birds.bird_app.model.BirdEntity;
import com.birds.bird_app.model.UserEntity;
import com.birds.bird_app.service.BirdService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(BirdRestController.class)
@Import(TestSecurityConfig.class)
class BirdRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BirdService birdService;

    @Autowired
    private ObjectMapper objectMapper;

    private BirdEntity testBird;
    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testBird = new BirdEntity("TestBird", "TestKind", "TestColor", 1, "TestFun", "testUrl");
        testUser = new UserEntity();
        testUser.setEmail("test@example.com");
        testUser.setId(1L);
        testUser.setRole("USER");
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUserCanViewBirds() throws Exception {
        when(birdService.getAllBirds()).thenReturn(Arrays.asList(testBird));

        mockMvc.perform(get("/api/birds"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("TestBird"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUserCanSubmitBird() throws Exception {
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

        when(birdService.createBird(any(BirdEntity.class), any(), eq(testUser))).thenReturn(testBird);

        mockMvc.perform(multipart("/api/birds")
            .file(image)
            .file(birdJson)
            .with(user(testUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("TestBird"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUserCannotDeleteBird() throws Exception {
        mockMvc.perform(delete("/api/birds/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUserCannotUpdateBird() throws Exception {
        mockMvc.perform(put("/api/birds/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBird)))
            .andExpect(status().isForbidden());
    }

    @Test
    void testUnauthenticatedUserCannotAccessBirds() throws Exception {
        mockMvc.perform(get("/api/birds"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdminCanDeleteBird() throws Exception {
        when(birdService.deleteBird(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/birds/1"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdminCanUpdateBird() throws Exception {
        when(birdService.updateBird(eq(1L), any(BirdEntity.class))).thenReturn(Optional.of(testBird));

        mockMvc.perform(put("/api/birds/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBird)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("TestBird"));
    }
}