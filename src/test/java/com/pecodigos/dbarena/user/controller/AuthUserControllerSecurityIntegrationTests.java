package com.pecodigos.dbarena.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pecodigos.dbarena.user.entity.User;
import com.pecodigos.dbarena.user.enums.Rank;
import com.pecodigos.dbarena.user.enums.Role;
import com.pecodigos.dbarena.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthUserControllerSecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void updateAvatarShouldRequireAuthentication() throws Exception {
        User goku = createUser("goku", "goku@example.com", "secret123");

        int responseStatus = mockMvc.perform(put("/api/auth/user/{id}/avatar", goku.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("profilePicturePath", "avatar-new"))))
                .andReturn()
                .getResponse()
                .getStatus();

        assertTrue(responseStatus == 401 || responseStatus == 403);
    }

    @Test
    @WithMockUser(username = "goku")
    void ownerShouldBeAbleToUpdateAvatar() throws Exception {
        User goku = createUser("goku", "goku@example.com", "secret123");

        mockMvc.perform(put("/api/auth/user/{id}/avatar", goku.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("profilePicturePath", "avatar-ssj"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("goku"));

        User updatedUser = userRepository.findById(goku.getId()).orElseThrow();
        assertEquals("avatar-ssj", updatedUser.getProfilePicturePath());
    }

    @Test
    @WithMockUser(username = "vegeta")
    void userShouldNotBeAbleToChangeAnotherUsersPassword() throws Exception {
        User goku = createUser("goku", "goku@example.com", "secret123");
        createUser("vegeta", "vegeta@example.com", "secret123");

        mockMvc.perform(put("/api/auth/user/{id}/password", goku.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "currentPassword", "secret123",
                                "newPassword", "newSecret123"
                        ))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You cannot change another user's password."));
    }

    private User createUser(String username, String email, String rawPassword) {
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .clan("")
                .role(Role.MEMBER)
                .rank(Rank.BABY)
                .ladderRank(0)
                .profilePicturePath("avatar-default")
                .currentLevel(1)
                .highestLevel(1)
                .currentExp(0L)
                .wins(0)
                .loses(0)
                .currentStreak(0)
                .highestStreak(0)
                .build();

        return userRepository.save(user);
    }
}
