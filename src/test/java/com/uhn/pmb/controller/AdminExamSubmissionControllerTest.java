package com.uhn.pmb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uhn.pmb.entity.ExamResult;
import com.uhn.pmb.entity.Student;
import com.uhn.pmb.entity.User;
import com.uhn.pmb.repository.ExamResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminExamSubmissionControllerTest {

    @Mock
    private ExamResultRepository examResultRepository;

    @InjectMocks
    private AdminExamSubmissionController adminExamSubmissionController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminExamSubmissionController).build();
    }

    private ExamResult buildExamResult(Long id, ExamResult.ExamValidationStatus status) {
        User user = new User();
        user.setEmail("student@test.com");

        Student student = new Student();
        student.setId(10L);
        student.setFullName("John Doe");
        student.setUser(user);

        ExamResult examResult = new ExamResult();
        examResult.setId(id);
        examResult.setStudent(student);
        examResult.setGformScore(85.0);
        examResult.setGeneratedToken("ABC123");
        examResult.setStudentInputToken("ABC123");
        examResult.setTokenValidated(true);
        examResult.setProofPhotoPath("/uploads/proof.jpg");
        examResult.setSubmissionDate(LocalDateTime.now());
        examResult.setExamValidationStatus(status);
        return examResult;
    }

    @Test
    @DisplayName("GET /admin/exam-submissions - default status PENDING returns 200")
    void getExamSubmissions_defaultStatus_returns200() throws Exception {
        ExamResult examResult = buildExamResult(1L, ExamResult.ExamValidationStatus.PENDING);
        when(examResultRepository.findByExamValidationStatus(ExamResult.ExamValidationStatus.PENDING))
                .thenReturn(List.of(examResult));

        mockMvc.perform(get("/admin/exam-submissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].studentName").value("John Doe"))
                .andExpect(jsonPath("$[0].studentEmail").value("student@test.com"))
                .andExpect(jsonPath("$[0].validationStatus").value("PENDING"));
    }

    @Test
    @DisplayName("GET /admin/exam-submissions?status=APPROVED - returns 200")
    void getExamSubmissions_withStatusParam_returns200() throws Exception {
        when(examResultRepository.findByExamValidationStatus(ExamResult.ExamValidationStatus.APPROVED))
                .thenReturn(List.of());

        mockMvc.perform(get("/admin/exam-submissions").param("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /admin/exam-submissions?status=INVALID - returns 400")
    void getExamSubmissions_invalidStatus_returns400() throws Exception {
        mockMvc.perform(get("/admin/exam-submissions").param("status", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("GET /admin/exam-submissions - repository exception returns 500")
    void getExamSubmissions_exception_returns500() throws Exception {
        when(examResultRepository.findByExamValidationStatus(any()))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/admin/exam-submissions"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("GET /admin/exam-submissions/{id} - found returns 200")
    void getExamSubmissionDetail_found_returns200() throws Exception {
        ExamResult examResult = buildExamResult(1L, ExamResult.ExamValidationStatus.PENDING);
        when(examResultRepository.findById(1L)).thenReturn(Optional.of(examResult));

        mockMvc.perform(get("/admin/exam-submissions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.studentId").value(10))
                .andExpect(jsonPath("$.gformScore").value(85));
    }

    @Test
    @DisplayName("GET /admin/exam-submissions/{id} - not found returns 404")
    void getExamSubmissionDetail_notFound_returns404() throws Exception {
        when(examResultRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/exam-submissions/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("GET /admin/exam-submissions/{id} - exception returns 404")
    void getExamSubmissionDetail_exception_returns500() throws Exception {
        when(examResultRepository.findById(1L)).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/admin/exam-submissions/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /admin/exam-submissions/{id}/validate - APPROVE returns 200")
    void validateExamSubmission_approve_returns200() throws Exception {
        ExamResult examResult = buildExamResult(1L, ExamResult.ExamValidationStatus.PENDING);
        when(examResultRepository.findById(1L)).thenReturn(Optional.of(examResult));
        when(examResultRepository.save(any(ExamResult.class))).thenReturn(examResult);

        String body = objectMapper.writeValueAsString(java.util.Map.of("action", "APPROVE"));

        mockMvc.perform(post("/admin/exam-submissions/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .principal(mock(Authentication.class)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.validationStatus").value("APPROVED"));

        verify(examResultRepository).save(examResult);
    }

    @Test
    @DisplayName("POST /admin/exam-submissions/{id}/validate - REJECT with reason returns 200")
    void validateExamSubmission_rejectWithReason_returns200() throws Exception {
        ExamResult examResult = buildExamResult(1L, ExamResult.ExamValidationStatus.PENDING);
        when(examResultRepository.findById(1L)).thenReturn(Optional.of(examResult));
        when(examResultRepository.save(any(ExamResult.class))).thenReturn(examResult);

        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "action", "REJECT",
                "adminNotes", "Foto bukti tidak jelas"
        ));

        mockMvc.perform(post("/admin/exam-submissions/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .principal(mock(Authentication.class)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.validationStatus").value("REJECTED"));

        verify(examResultRepository).save(examResult);
    }

    @Test
    @DisplayName("POST /admin/exam-submissions/{id}/validate - REJECT without reason returns 400")
    void validateExamSubmission_rejectWithoutReason_returns400() throws Exception {
        ExamResult examResult = buildExamResult(1L, ExamResult.ExamValidationStatus.PENDING);
        when(examResultRepository.findById(1L)).thenReturn(Optional.of(examResult));

        String body = objectMapper.writeValueAsString(java.util.Map.of("action", "REJECT"));

        mockMvc.perform(post("/admin/exam-submissions/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .principal(mock(Authentication.class)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(examResultRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /admin/exam-submissions/{id}/validate - invalid action returns 400")
    void validateExamSubmission_invalidAction_returns400() throws Exception {
        ExamResult examResult = buildExamResult(1L, ExamResult.ExamValidationStatus.PENDING);
        when(examResultRepository.findById(1L)).thenReturn(Optional.of(examResult));

        String body = objectMapper.writeValueAsString(java.util.Map.of("action", "MAYBE"));

        mockMvc.perform(post("/admin/exam-submissions/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .principal(mock(Authentication.class)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(examResultRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /admin/exam-submissions/{id}/validate - not found returns 404")
    void validateExamSubmission_notFound_returns404() throws Exception {
        when(examResultRepository.findById(99L)).thenReturn(Optional.empty());

        String body = objectMapper.writeValueAsString(java.util.Map.of("action", "APPROVE"));

        mockMvc.perform(post("/admin/exam-submissions/99/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .principal(mock(Authentication.class)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /admin/exam-submissions/{id}/validate - exception returns 500")
    void validateExamSubmission_exception_returns500() throws Exception {
        when(examResultRepository.findById(1L)).thenThrow(new RuntimeException("DB error"));

        String body = objectMapper.writeValueAsString(java.util.Map.of("action", "APPROVE"));

        mockMvc.perform(post("/admin/exam-submissions/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .principal(mock(Authentication.class)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }
}