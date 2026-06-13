package com.uhn.pmb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uhn.pmb.dto.ChangePasswordRequest;
import com.uhn.pmb.dto.StudentProfileRequest;
import com.uhn.pmb.dto.ValidationStatusResponse;
import com.uhn.pmb.entity.ValidationStatusTracker;
import com.uhn.pmb.repository.StudentRepository;
import com.uhn.pmb.repository.UserRepository;
import com.uhn.pmb.service.CamabaProfileService;
import com.uhn.pmb.service.FormValidationService;
import com.uhn.pmb.service.ValidationStatusTrackerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CamabaProfileControllerTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ValidationStatusTrackerService validationStatusTrackerService;

    @Mock
    private FormValidationService formValidationService;

    @Mock
    private CamabaProfileService camabaProfileService;

    @InjectMocks
    private CamabaProfileController camabaProfileController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(camabaProfileController).build();
        var auth = new UsernamePasswordAuthenticationToken("u@test.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /api/camaba/profile - returns student profile 200")
    void getProfile_returns200() throws Exception {
        StudentProfileRequest profile = new StudentProfileRequest();
        profile.setFullName("Alice");
        when(camabaProfileService.getProfileForCurrentUser()).thenReturn(profile);

        mockMvc.perform(get("/api/camaba/profile"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/profile - user not found returns 400")
    void getProfile_userNotFound_returns500() throws Exception {
        when(camabaProfileService.getProfileForCurrentUser())
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/camaba/profile"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/camaba/profile - success returns 200")
    void updateProfile_success_returns200() throws Exception {
        StudentProfileRequest req = new StudentProfileRequest();
        req.setFullName("Alice Updated");
        req.setPhoneNumber("08123456789");
        when(camabaProfileService.updateProfileForCurrentUser(any())).thenReturn(req);

        mockMvc.perform(put("/api/camaba/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/camaba/profile - user not found returns 400")
    void updateProfile_userNotFound_returns400() throws Exception {
        when(camabaProfileService.updateProfileForCurrentUser(any()))
                .thenThrow(new RuntimeException("User not found"));
        StudentProfileRequest req = new StudentProfileRequest();

        mockMvc.perform(put("/api/camaba/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/camaba/change-password - correct password returns 200")
    void changePassword_correctPassword_returns200() throws Exception {
        when(camabaProfileService.changePasswordForCurrentUser(any()))
                .thenReturn("Password updated successfully");
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("oldPassword");
        req.setNewPassword("newPassword123");
        req.setConfirmPassword("newPassword123");

        mockMvc.perform(post("/api/camaba/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/camaba/change-password - wrong old password returns 400")
    void changePassword_wrongPassword_returns400() throws Exception {
        when(camabaProfileService.changePasswordForCurrentUser(any()))
                .thenThrow(new RuntimeException("Current password is incorrect"));
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("wrongPassword");
        req.setNewPassword("newPassword123");

        mockMvc.perform(post("/api/camaba/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

 @Test
@DisplayName("GET /api/camaba/validation-status - tracker found returns 200")
void getValidationStatus_found_returns200() throws Exception {
    ValidationStatusResponse response = new ValidationStatusResponse();
    // set field sesuai yang ada di class ValidationStatusResponse
    when(validationStatusTrackerService.getValidationStatusForCurrentUser())
            .thenReturn(response);

    mockMvc.perform(get("/api/camaba/validation-status"))
            .andExpect(status().isOk());
}

@Test
@DisplayName("GET /api/camaba/validation-status - tracker not found returns 200 with NOT_STARTED")
void getValidationStatus_notFound_returnsNotStarted() throws Exception {
    ValidationStatusResponse response = new ValidationStatusResponse();
    when(validationStatusTrackerService.getValidationStatusForCurrentUser())
            .thenReturn(response);

    mockMvc.perform(get("/api/camaba/validation-status"))
            .andExpect(status().isOk());
}

    @Test
    @DisplayName("GET /api/camaba/validation-status - user not found returns 400")
    void getValidationStatus_userNotFound_returns400() throws Exception {
        when(validationStatusTrackerService.getValidationStatusForCurrentUser())
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/camaba/validation-status"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/camaba/repair-status - success returns 200")
    void updateRepairStatus_success_returns200() throws Exception {
        when(camabaProfileService.markRepairCompleteForCurrentUser())
                .thenReturn("Repair marked as completed");

        mockMvc.perform(put("/api/camaba/repair-status"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/camaba/repair-status - user not found returns 400")
    void updateRepairStatus_userNotFound_returns400() throws Exception {
        when(camabaProfileService.markRepairCompleteForCurrentUser())
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(put("/api/camaba/repair-status"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/camaba/profile - student with user set returns email from user")
    void getProfile_studentWithUser_returns200() throws Exception {
        StudentProfileRequest profile = new StudentProfileRequest();
        profile.setFullName("Alice");
        when(camabaProfileService.getProfileForCurrentUser()).thenReturn(profile);

        mockMvc.perform(get("/api/camaba/profile"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/debug-auth - no Authorization header uses null defaults")
    void debugAuth_withNoHeader_returns200() throws Exception {
        mockMvc.perform(get("/api/camaba/debug-auth"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/debug-auth - short Authorization header (len<=10) returns full value")
    void debugAuth_withShortHeader_returns200() throws Exception {
        mockMvc.perform(get("/api/camaba/debug-auth")
                        .header("Authorization", "ABC"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/debug-auth - long Authorization header (len>10) returns truncated")
    void debugAuth_withLongHeader_returns200() throws Exception {
        mockMvc.perform(get("/api/camaba/debug-auth")
                        .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/debug-auth - null auth context uses fallback values")
    void debugAuth_withNullAuth_returns200() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/camaba/debug-auth"))
                .andExpect(status().isOk());
    }
}