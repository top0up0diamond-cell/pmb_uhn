package com.uhn.pmb.controller;

import com.uhn.pmb.entity.HasilAkhir;
import com.uhn.pmb.entity.Student;
import com.uhn.pmb.entity.User;
import com.uhn.pmb.repository.StudentRepository;
import com.uhn.pmb.repository.UserRepository;
import com.uhn.pmb.service.CamabaReenrollmentService;
import com.uhn.pmb.service.HasilAkhirService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CamabaReenrollmentControllerTest {

    @Mock
    private CamabaReenrollmentService camabaReenrollmentService;
    @Mock
    private HasilAkhirService hasilAkhirService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private CamabaReenrollmentController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("camaba@test.com", "password", List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /api/camaba/reenrollment/submit - success returns 200")
    void submitReenrollment_success_returns200() throws Exception {
        when(camabaReenrollmentService.submitReenrollment(anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), any(), any(), any(), any()))
                .thenReturn(Map.of("success", true, "reenrollmentId", 1L));

        mockMvc.perform(multipart("/api/camaba/reenrollment/submit")
                        .param("parentPhone", "081234567890")
                        .param("parentEmail", "parent@test.com")
                        .param("parentAddress", "Jl. Parent 1")
                        .param("permanentAddress", "Jl. Permanent 1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/camaba/reenrollment/submit - exception returns 500")
    void submitReenrollment_exception_returns500() throws Exception {
        when(camabaReenrollmentService.submitReenrollment(anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(multipart("/api/camaba/reenrollment/submit")
                        .param("parentPhone", "081234567890")
                        .param("parentEmail", "parent@test.com")
                        .param("parentAddress", "Jl. Parent 1")
                        .param("permanentAddress", "Jl. Permanent 1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/camaba/reenrollment/status - success returns 200")
    void getReenrollmentStatus_success_returns200() throws Exception {
        when(camabaReenrollmentService.getReenrollmentStatus(anyString()))
                .thenReturn(Map.of("status", "SUBMITTED"));

        mockMvc.perform(get("/api/camaba/reenrollment/status"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/reenrollment/status - exception returns 500")
    void getReenrollmentStatus_exception_returns500() throws Exception {
        when(camabaReenrollmentService.getReenrollmentStatus(anyString()))
                .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/camaba/reenrollment/status"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /api/camaba/reenrollment/complete - success returns 200")
    void completeReenrollment_success_returns200() throws Exception {
        when(camabaReenrollmentService.completeReenrollment(anyString(), any()))
                .thenReturn(Map.of("success", true));

        mockMvc.perform(post("/api/camaba/reenrollment/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"submittedAt\":\"2024-12-01\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/camaba/reenrollment/complete - exception returns 400")
    void completeReenrollment_exception_returns400() throws Exception {
        when(camabaReenrollmentService.completeReenrollment(anyString(), any()))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(post("/api/camaba/reenrollment/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/camaba/reenrollment - success returns 200")
    void getReenrollmentData_success_returns200() throws Exception {
        when(camabaReenrollmentService.getReenrollmentData(anyString()))
                .thenReturn(Map.of("exists", true));

        mockMvc.perform(get("/api/camaba/reenrollment"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/reenrollment - exception returns 500")
    void getReenrollmentData_exception_returns500() throws Exception {
        when(camabaReenrollmentService.getReenrollmentData(anyString()))
                .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/camaba/reenrollment"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("PUT /api/camaba/reenrollment/{id} - success returns 200")
    void updateReenrollmentData_success_returns200() throws Exception {
        when(camabaReenrollmentService.updateReenrollmentData(anyString(), anyLong(), any()))
                .thenReturn(Map.of("success", true));

        mockMvc.perform(multipart("/api/camaba/reenrollment/1")
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/camaba/reenrollment/{id} - SecurityException returns 403")
    void updateReenrollmentData_unauthorized_returns403() throws Exception {
        when(camabaReenrollmentService.updateReenrollmentData(anyString(), anyLong(), any()))
                .thenThrow(new SecurityException("Unauthorized"));

        mockMvc.perform(multipart("/api/camaba/reenrollment/1")
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/camaba/reenrollment/{id} - exception returns 500")
    void updateReenrollmentData_exception_returns500() throws Exception {
        when(camabaReenrollmentService.updateReenrollmentData(anyString(), anyLong(), any()))
                .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(multipart("/api/camaba/reenrollment/1")
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/camaba/reenrollment/{id}/documents - success returns 200")
    void getReenrollmentDocuments_success_returns200() throws Exception {
        when(camabaReenrollmentService.getReenrollmentDocuments(anyString(), anyLong()))
                .thenReturn(List.of(Map.of("type", "IJAZAH")));

        mockMvc.perform(get("/api/camaba/reenrollment/1/documents"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/reenrollment/{id}/documents - SecurityException returns 403")
    void getReenrollmentDocuments_unauthorized_returns403() throws Exception {
        when(camabaReenrollmentService.getReenrollmentDocuments(anyString(), anyLong()))
                .thenThrow(new SecurityException("Unauthorized"));

        mockMvc.perform(get("/api/camaba/reenrollment/1/documents"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/camaba/reenrollment/{id}/documents - exception returns 500")
    void getReenrollmentDocuments_exception_returns500() throws Exception {
        when(camabaReenrollmentService.getReenrollmentDocuments(anyString(), anyLong()))
                .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/camaba/reenrollment/1/documents"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/camaba/hasil-akhir - user not found returns 500")
    void getHasilAkhir_userNotFound_returns500() throws Exception {
        when(userRepository.findByEmail(anyString())).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/camaba/hasil-akhir"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/camaba/hasil-akhir - user exists, student not found returns 500")
    void getHasilAkhir_studentNotFound_returns500() throws Exception {
        User user = User.builder().id(1L).email("camaba@test.com").build();
        when(userRepository.findByEmail("camaba@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/camaba/hasil-akhir"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/camaba/hasil-akhir - hasil akhir not present returns 200")
    void getHasilAkhir_notPresent_returns200() throws Exception {
        User user = User.builder().id(1L).email("camaba@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        when(userRepository.findByEmail("camaba@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(hasilAkhirService.getHasilAkhirByStudentId(10L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/camaba/hasil-akhir"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/hasil-akhir - hasil akhir exists returns 200")
    void getHasilAkhir_exists_returns200() throws Exception {
        User user = User.builder().id(1L).email("camaba@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        HasilAkhir ha = HasilAkhir.builder()
                .id(1L)
                .status(HasilAkhir.HasilAkhirStatus.ACTIVE)
                .brivaNumber("BRV-001")
                .brivaAmount(BigDecimal.valueOf(5000000))
                .nomorRegistrasi("REG-001")
                .npmSementaraFile("npm-001")
                .ktmSementaraFile("ktm-001")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(userRepository.findByEmail("camaba@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(hasilAkhirService.getHasilAkhirByStudentId(10L)).thenReturn(Optional.of(ha));

        mockMvc.perform(get("/api/camaba/hasil-akhir"))
                .andExpect(status().isOk());
    }
}
