package com.uhn.pmb.controller;

import com.uhn.pmb.entity.Exam;
import com.uhn.pmb.repository.UserRepository;
import com.uhn.pmb.service.CamabaExamService;
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
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CamabaExamControllerTest {

    @Mock
    private CamabaExamService camabaExamService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CamabaExamController camabaExamController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(camabaExamController).build();
        var auth = new UsernamePasswordAuthenticationToken("u@test.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /api/camaba/exam - exam found returns 200")
    void getExamDetails_found_returns200() throws Exception {
        Exam exam = new Exam();
        exam.setId(1L);
        when(camabaExamService.getExamDetails("u@test.com")).thenReturn(Optional.of(exam));

        mockMvc.perform(get("/api/camaba/exam"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/exam - no exam returns 200 with message")
    void getExamDetails_notFound_returns200() throws Exception {
        when(camabaExamService.getExamDetails("u@test.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/camaba/exam"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/exam - exception returns 400")
    void getExamDetails_exception_returns400() throws Exception {
        when(camabaExamService.getExamDetails("u@test.com"))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/camaba/exam"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/camaba/submit-exam - success returns 200")
    void submitExam_success_returns200() throws Exception {
        when(camabaExamService.submitExam("u@test.com"))
                .thenReturn(Map.of("success", true, "examId", 1L));
        mockMvc.perform(post("/api/camaba/submit-exam")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/camaba/submit-exam - exception returns 400")
    void submitExam_exception_returns400() throws Exception {
        when(camabaExamService.submitExam("u@test.com"))
                .thenThrow(new RuntimeException("Student not found"));
        mockMvc.perform(post("/api/camaba/submit-exam")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/camaba/exam-validation-status - success returns 200")
    void getExamValidationStatus_returns200() throws Exception {
        when(camabaExamService.getExamValidationStatus("u@test.com"))
                .thenReturn(Map.of("status", "PENDING"));
        mockMvc.perform(get("/api/camaba/exam-validation-status"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/exam-validation-status - exception returns 400")
    void getExamValidationStatus_exception_returns400() throws Exception {
        when(camabaExamService.getExamValidationStatus("u@test.com"))
                .thenThrow(new RuntimeException("Error"));
        mockMvc.perform(get("/api/camaba/exam-validation-status"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/camaba/exam-token - success=true returns 200")
    void getExamToken_successTrue_returns200() throws Exception {
        when(camabaExamService.getExamToken("u@test.com"))
                .thenReturn(Map.of("success", true, "tokenValue", "TOKEN-ABC"));
        mockMvc.perform(get("/api/camaba/exam-token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/exam-token - success=false returns 404")
    void getExamToken_successFalse_returns404() throws Exception {
        when(camabaExamService.getExamToken("u@test.com"))
                .thenReturn(Map.of("success", false, "message", "Token belum tersedia"));
        mockMvc.perform(get("/api/camaba/exam-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/camaba/exam-token - exception returns 400")
    void getExamToken_exception_returns400() throws Exception {
        when(camabaExamService.getExamToken("u@test.com"))
                .thenThrow(new RuntimeException("Error"));
        mockMvc.perform(get("/api/camaba/exam-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/camaba/trigger-token-generation - success=true returns 200")
    void triggerTokenGeneration_successTrue_returns200() throws Exception {
        when(camabaExamService.triggerTokenGeneration("u@test.com"))
                .thenReturn(Map.of("success", true, "tokenValue", "TOKEN-XYZ"));
        mockMvc.perform(post("/api/camaba/trigger-token-generation"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/camaba/trigger-token-generation - success=false returns 400")
    void triggerTokenGeneration_successFalse_returns400() throws Exception {
        when(camabaExamService.triggerTokenGeneration("u@test.com"))
                .thenReturn(Map.of("success", false, "message", "Pembayaran belum selesai"));
        mockMvc.perform(post("/api/camaba/trigger-token-generation"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/camaba/exam/start - success returns 200")
    void markExamAsStarted_success_returns200() throws Exception {
        when(camabaExamService.markExamAsStarted("u@test.com"))
                .thenReturn(Map.of("success", true, "stage", "PSYCHO_EXAM"));
        mockMvc.perform(post("/api/camaba/exam/start"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/camaba/exam/start - exception returns 400")
    void markExamAsStarted_exception_returns400() throws Exception {
        when(camabaExamService.markExamAsStarted("u@test.com"))
                .thenThrow(new RuntimeException("User not found"));
        mockMvc.perform(post("/api/camaba/exam/start"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/camaba/exam/submit-results - success returns 200")
    void submitExamResults_success_returns200() throws Exception {
        when(camabaExamService.submitExamResults(anyString(), anyString(), anyDouble(), any()))
                .thenReturn(Map.of("success", true));
        mockMvc.perform(multipart("/api/camaba/exam/submit-results")
                        .param("examToken", "TOKEN-ABC")
                        .param("gformScore", "85.0"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/camaba/exam/submit-results - exception returns 500")
    void submitExamResults_exception_returns500() throws Exception {
        when(camabaExamService.submitExamResults(anyString(), anyString(), anyDouble(), any()))
                .thenThrow(new RuntimeException("Error uploading"));
        mockMvc.perform(multipart("/api/camaba/exam/submit-results")
                        .param("examToken", "TOKEN-ABC")
                        .param("gformScore", "85.0"))
                .andExpect(status().isInternalServerError());
    }
}
