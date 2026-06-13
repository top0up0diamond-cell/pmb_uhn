package com.uhn.pmb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uhn.pmb.dto.ExamTokenDTO;
import com.uhn.pmb.entity.ExamToken;
import com.uhn.pmb.entity.Student;
import com.uhn.pmb.entity.User;
import com.uhn.pmb.service.ExamTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ExamTokenControllerTest {

    @Mock private ExamTokenService tokenService;
    @InjectMocks private ExamTokenController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        ReflectionTestUtils.setField(controller, "gformLink", "http://test.form.link");
        objectMapper.findAndRegisterModules();
    }

    @Test
    @DisplayName("POST /admin/api/exam/generate-token - not authenticated returns 401")
    void generateToken_notAuthenticated_returns401() throws Exception {
        ExamTokenDTO.GenerateTokenRequest req = new ExamTokenDTO.GenerateTokenRequest();
        req.setStudentId(1L);
        req.setApprovedFormId(1L);
        req.setExpirationMinutes(120);

        mockMvc.perform(post("/admin/api/exam/generate-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /admin/api/exam/statistics - authenticated returns 200")
    void getStatistics_authenticated_returns200() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(tokenService.getExamStatistics()).thenReturn(Map.of("totalTokens", 0L));

        mockMvc.perform(get("/admin/api/exam/statistics")
                        .principal(auth))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/exam/get-gform-link - returns 200 with link")
    void getGFormLink_returns200() throws Exception {
        mockMvc.perform(get("/api/exam/get-gform-link"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/exam/submission-status/{studentId} - returns 200")
    void getSubmissionStatus_returns200() throws Exception {
        mockMvc.perform(get("/api/exam/submission-status/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/exam/get-validated-students - authenticated returns 200")
    void getValidatedStudents_authenticated_returns200() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(tokenService.getValidatedStudentsWithTokens()).thenReturn(List.of());

        mockMvc.perform(get("/admin/api/exam/get-validated-students")
                        .principal(auth))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/exam/get-validated-students - not authenticated returns 401")
    void getValidatedStudents_notAuthenticated_returns401() throws Exception {
        mockMvc.perform(get("/admin/api/exam/get-validated-students"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /admin/api/exam/revoke-token - authenticated returns 200")
    void revokeToken_authenticated_returns200() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        doNothing().when(tokenService).revokeToken(any(), any());

        ExamTokenDTO.RevokeTokenRequest req = new ExamTokenDTO.RevokeTokenRequest();
        req.setToken("test-token");
        req.setReason("Testing");

        mockMvc.perform(post("/admin/api/exam/revoke-token")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/exam/validate-token - invalid token returns 400")
    void validateToken_invalid_returns400() throws Exception {
        when(tokenService.validateToken(any(), any())).thenThrow(new RuntimeException("Token tidak valid"));

        ExamTokenDTO.ValidateTokenRequest req = new ExamTokenDTO.ValidateTokenRequest();
        req.setToken("invalid-token");
        req.setStudentId(1L);

        mockMvc.perform(post("/api/exam/validate-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/exam/submit-results - valid request returns 200")
    void submitResults_valid_returns200() throws Exception {
        com.uhn.pmb.entity.ExamSubmission sub = new com.uhn.pmb.entity.ExamSubmission();
        sub.setId(1L);
        sub.setSubmittedAt(java.time.LocalDateTime.now());
        sub.setScore(85);
        when(tokenService.submitExamResult(any())).thenReturn(sub);

        ExamTokenDTO.SubmitResultRequest req = new ExamTokenDTO.SubmitResultRequest();
        req.setToken("test-token");
        req.setStudentId(1L);

        mockMvc.perform(post("/api/exam/submit-results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /admin/api/exam/generate-token - authenticated returns 200")
    void generateToken_authenticated_returns200() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);

        User user = User.builder().id(1L).email("s@test.com").build();
        Student student = Student.builder().id(1L).fullName("John Doe").user(user).build();
        ExamToken token = mock(ExamToken.class);
        when(token.getId()).thenReturn(1L);
        when(token.getTokenValue()).thenReturn("abc-token");
        when(token.getExpiresAt()).thenReturn(LocalDateTime.now().plusHours(2));
        when(token.getStudent()).thenReturn(student);
        when(tokenService.generateToken(any(), any(), any())).thenReturn(token);

        ExamTokenDTO.GenerateTokenRequest req = new ExamTokenDTO.GenerateTokenRequest();
        req.setStudentId(1L);
        req.setApprovedFormId(1L);
        req.setExpirationMinutes(120);

        mockMvc.perform(post("/admin/api/exam/generate-token")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/exam/validate-token - valid token returns 200")
    void validateToken_valid_returns200() throws Exception {
        ExamTokenDTO.ValidateTokenResponse resp = ExamTokenDTO.ValidateTokenResponse.builder()
                .valid(true)
                .message("Token valid")
                .token("test-token")
                .expiresAt(LocalDateTime.now().plusHours(1))
                .expirationMinutes(60L)
                .build();
        when(tokenService.validateToken(any(), any())).thenReturn(resp);

        ExamTokenDTO.ValidateTokenRequest req = new ExamTokenDTO.ValidateTokenRequest();
        req.setToken("test-token");
        req.setStudentId(1L);

        mockMvc.perform(post("/api/exam/validate-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /admin/api/exam/generate-token - service exception returns 500")
    void generateToken_serviceException_returns500() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(tokenService.generateToken(any(), any(), any())).thenThrow(new RuntimeException("Generate failed"));

        ExamTokenDTO.GenerateTokenRequest req = new ExamTokenDTO.GenerateTokenRequest();
        req.setStudentId(1L);
        req.setApprovedFormId(1L);
        req.setExpirationMinutes(120);

        mockMvc.perform(post("/admin/api/exam/generate-token")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /admin/api/exam/get-validated-students - service exception returns 500")
    void getValidatedStudents_serviceException_returns500() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(tokenService.getValidatedStudentsWithTokens()).thenThrow(new RuntimeException("Fetch failed"));

        mockMvc.perform(get("/admin/api/exam/get-validated-students")
                        .principal(auth))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /admin/api/exam/statistics - service exception returns 500")
    void getStatistics_serviceException_returns500() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(tokenService.getExamStatistics()).thenThrow(new RuntimeException("Fetch failed"));

        mockMvc.perform(get("/admin/api/exam/statistics")
                        .principal(auth))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /admin/api/exam/revoke-token - service exception returns 500")
    void revokeToken_serviceException_returns500() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        doThrow(new RuntimeException("Revoke failed")).when(tokenService).revokeToken(any(), any());

        ExamTokenDTO.RevokeTokenRequest req = new ExamTokenDTO.RevokeTokenRequest();
        req.setToken("test-token");
        req.setReason("Testing");

        mockMvc.perform(post("/admin/api/exam/revoke-token")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /admin/api/exam/revoke-token - not authenticated returns 401")
    void revokeToken_notAuthenticated_returns401() throws Exception {
        ExamTokenDTO.RevokeTokenRequest req = new ExamTokenDTO.RevokeTokenRequest();
        req.setToken("test-token");
        req.setReason("Testing");

        mockMvc.perform(post("/admin/api/exam/revoke-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/exam/submit-results - service exception returns 400")
    void submitResults_serviceException_returns400() throws Exception {
        when(tokenService.submitExamResult(any())).thenThrow(new RuntimeException("Submit failed"));

        ExamTokenDTO.SubmitResultRequest req = new ExamTokenDTO.SubmitResultRequest();
        req.setToken("test-token");
        req.setStudentId(1L);

        mockMvc.perform(post("/api/exam/submit-results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/exam/get-gform-link - empty link returns 404")
    void getGFormLink_emptyLink_returns404() throws Exception {
        ReflectionTestUtils.setField(controller, "gformLink", "");

        mockMvc.perform(get("/api/exam/get-gform-link"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/exam/get-gform-link - service exception returns 500")
    void getGFormLink_serviceException_returns500() throws Exception {
        ReflectionTestUtils.setField(controller, "gformLink", null);

        mockMvc.perform(get("/api/exam/get-gform-link"))
                .andExpect(status().isNotFound());
    }
}


