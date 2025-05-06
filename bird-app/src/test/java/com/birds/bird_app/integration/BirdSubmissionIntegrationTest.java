package com.birds.bird_app.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.birds.bird_app.model.BirdEntity;
import com.birds.bird_app.model.UserEntity;
import com.birds.bird_app.model.GroupEntity;
import com.birds.bird_app.model.GroupMember;
import com.birds.bird_app.model.BirdSubmission;
import com.birds.bird_app.repository.BirdRepository;
import com.birds.bird_app.repository.UserRepository;
import com.birds.bird_app.repository.GroupRepository;
import com.birds.bird_app.repository.GroupMemberRepository;
import com.birds.bird_app.repository.BirdSubmissionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BirdSubmissionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BirdRepository birdRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private BirdSubmissionRepository birdSubmissionRepository;

    private UserEntity testUser;
    private GroupEntity testGroup;
    private BirdEntity testBird;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new UserEntity();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setName("Test User");
        testUser.setRole("USER");
        testUser = userRepository.save(testUser);

        // Create test group
        testGroup = new GroupEntity();
        testGroup.setName("Test Group");
        testGroup.setDescription("Test Group Description");
        testGroup.setFounder(testUser);
        testGroup.setBirdKeeper(testUser);
        testGroup = groupRepository.save(testGroup);

        // Add member to group
        GroupMember member = new GroupMember(testUser, testGroup);
        member.setStatus("ACTIVE");
        groupMemberRepository.save(member);

        // Create test bird
        testBird = new BirdEntity("TestBird", "TestKind", "TestColor", 1, "TestFun", null);
    }

    @Test
    void testUserCanSubmitBirdToGroup() throws Exception {
        // Create test image
        MockMultipartFile image = new MockMultipartFile(
            "image",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        // Create bird submission
        BirdSubmission submission = new BirdSubmission();
        submission.setGroup(testGroup);
        submission.setSubmittedBy(testUser);
        submission.setBirdName(testBird.getName());
        submission.setStatus("ACTIVE");
        submission.setVotes(0);
        submission.setSubmittedAt(LocalDateTime.now());

        MockMultipartFile submissionJson = new MockMultipartFile(
            "submission",
            "",
            "application/json",
            objectMapper.writeValueAsString(submission).getBytes()
        );

        // Submit bird to group
        mockMvc.perform(multipart("/api/groups/{groupId}/submissions", testGroup.getId())
            .file(image)
            .file(submissionJson)
            .with(user(testUser))
            .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.birdName").value("TestBird"))
            .andExpect(jsonPath("$.group.id").value(testGroup.getId()));
    }

    @Test
    void testUserCannotSubmitBirdToNonMemberGroup() throws Exception {
        // Create new group without test user
        GroupEntity otherGroup = new GroupEntity();
        otherGroup.setName("Other Group");
        otherGroup.setDescription("Other Group Description");
        otherGroup.setFounder(testUser);
        otherGroup.setBirdKeeper(testUser);
        otherGroup = groupRepository.save(otherGroup);

        // Create test image
        MockMultipartFile image = new MockMultipartFile(
            "image",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        // Create bird submission
        BirdSubmission submission = new BirdSubmission();
        submission.setGroup(otherGroup);
        submission.setSubmittedBy(testUser);
        submission.setBirdName(testBird.getName());
        submission.setStatus("ACTIVE");
        submission.setVotes(0);
        submission.setSubmittedAt(LocalDateTime.now());

        MockMultipartFile submissionJson = new MockMultipartFile(
            "submission",
            "",
            "application/json",
            objectMapper.writeValueAsString(submission).getBytes()
        );

        // Submit bird to non-member group
        mockMvc.perform(multipart("/api/groups/{groupId}/submissions", otherGroup.getId())
            .file(image)
            .file(submissionJson)
            .with(user(testUser))
            .with(csrf()))
            .andExpect(status().isForbidden());
    }
} 