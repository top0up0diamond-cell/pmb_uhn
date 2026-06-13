package com.uhn.pmb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uhn.pmb.dto.LoginRequest;
import com.uhn.pmb.dto.RegisterRequest;
import com.uhn.pmb.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    @DisplayName("POST /api/auth/register - success returns 201")
    void register_success_returns200() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@test.com");
        req.setPassword("pass123");
        req.setConfirmPassword("pass123");
        req.setFullName("Test");

        com.uhn.pmb.dto.AuthResponse authResp = new com.uhn.pmb.dto.AuthResponse();
        authResp.setSuccess(true);
        when(authService.register(any())).thenReturn(authResp);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/auth/register - duplicate email returns 400")
    void register_duplicateEmail_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("dup@test.com");
        req.setPassword("pass123");
        req.setConfirmPassword("pass123");

        when(authService.register(any())).thenThrow(new RuntimeException("Email sudah terdaftar"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login - success returns token")
    void login_success_returnsToken() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("u@test.com");
        req.setPassword("pass");

        com.uhn.pmb.dto.AuthResponse response = new com.uhn.pmb.dto.AuthResponse();
        response.setToken("jwt.token.here");
        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/auth/login - bad credentials returns 401")
    void login_badCreds_returns401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("u@test.com");
        req.setPassword("wrong");

        when(authService.login(any())).thenThrow(
                new org.springframework.security.authentication.BadCredentialsException("bad"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/auth/verify-email - success returns 200")
    void verifyEmail_success_returns200() throws Exception {
        com.uhn.pmb.dto.AuthResponse resp = new com.uhn.pmb.dto.AuthResponse();
        resp.setSuccess(true);
        when(authService.verifyEmail("valid-token")).thenReturn(resp);

        mockMvc.perform(get("/api/auth/verify-email")
                        .param("token", "valid-token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/auth/verify-email - invalid token returns 400")
    void verifyEmail_invalid_returns400() throws Exception {
        when(authService.verifyEmail("bad-token")).thenThrow(new RuntimeException("Token invalid"));

        mockMvc.perform(get("/api/auth/verify-email")
                        .param("token", "bad-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/resend-verification - success returns 200")
    void resendVerification_success_returns200() throws Exception {
        com.uhn.pmb.dto.AuthResponse resp = new com.uhn.pmb.dto.AuthResponse();
        resp.setSuccess(true);
        when(authService.resendVerificationEmail(anyString())).thenReturn(resp);

        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"u@test.com\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/auth/resend-verification - error returns 400")
    void resendVerification_error_returns400() throws Exception {
        when(authService.resendVerificationEmail(anyString()))
                .thenThrow(new RuntimeException("Email not found"));

        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"none@test.com\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password - success returns 200")
    void forgotPassword_success_returns200() throws Exception {
        com.uhn.pmb.dto.AuthResponse resp = new com.uhn.pmb.dto.AuthResponse();
        resp.setSuccess(true);
        when(authService.forgotPassword(anyString())).thenReturn(resp);

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"u@test.com\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password - user not found returns 400")
    void forgotPassword_userNotFound_returns400() throws Exception {
        when(authService.forgotPassword(anyString()))
                .thenThrow(new RuntimeException("Email tidak ditemukan"));

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"none@test.com\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/reset-password - success returns 200")
    void resetPassword_success_returns200() throws Exception {
        com.uhn.pmb.dto.AuthResponse resp = new com.uhn.pmb.dto.AuthResponse();
        resp.setSuccess(true);
        when(authService.resetPassword(anyString(), anyString(), anyString())).thenReturn(resp);

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"t\",\"password\":\"pass123\",\"confirmPassword\":\"pass123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/auth/reset-password - error returns 400")
    void resetPassword_error_returns400() throws Exception {
        when(authService.resetPassword(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Token expired"));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"bad\",\"password\":\"pass123\",\"confirmPassword\":\"pass123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/validate-token - valid token returns 200")
    void validateToken_valid_returns200() throws Exception {
        when(authService.validateToken("jwt.token")).thenReturn(true);

        mockMvc.perform(post("/api/auth/validate-token")
                        .header("Authorization", "Bearer jwt.token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/auth/validate-token - invalid token returns 401")
    void validateToken_invalid_returns401() throws Exception {
        when(authService.validateToken("bad.token")).thenReturn(false);

        mockMvc.perform(post("/api/auth/validate-token")
                        .header("Authorization", "Bearer bad.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/auth/health - returns 200")
    void healthCheck_returns200() throws Exception {
        mockMvc.perform(get("/api/auth/health"))
                .andExpect(status().isOk());
    }
}
