package com.uhn.pmb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uhn.pmb.entity.RegistrationPeriod;
import com.uhn.pmb.entity.SelectionType;
import com.uhn.pmb.entity.User;
import com.uhn.pmb.repository.RegistrationPeriodRepository;
import com.uhn.pmb.repository.SelectionTypeRepository;
import com.uhn.pmb.repository.UserRepository;
import com.uhn.pmb.service.CamabaRegistrationService;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CamabaRegistrationControllerTest {

    @Mock private CamabaRegistrationService camabaRegistrationService;
    @Mock private RegistrationPeriodRepository registrationPeriodRepository;
    @Mock private SelectionTypeRepository selectionTypeRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private CamabaRegistrationController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        // Set up security context with a logged-in user
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("student@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ===== getAllGelombang =====

    @Test
    @DisplayName("GET /api/camaba/all-gelombang - returns 200")
    void getAllGelombang_returns200() throws Exception {
        when(camabaRegistrationService.getAllGelombang()).thenReturn(List.of(Map.of("id", 1L, "name", "G1")));

        mockMvc.perform(get("/api/camaba/all-gelombang"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/all-gelombang - exception returns 400")
    void getAllGelombang_exception_returns400() throws Exception {
        when(camabaRegistrationService.getAllGelombang()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/camaba/all-gelombang"))
                .andExpect(status().isBadRequest());
    }

    // ===== getAllFormulas =====

    @Test
    @DisplayName("GET /api/camaba/all-formulas - returns 200")
    void getAllFormulas_returns200() throws Exception {
        when(camabaRegistrationService.getAllFormulas(null)).thenReturn(List.of());

        mockMvc.perform(get("/api/camaba/all-formulas"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/all-formulas - with periodId returns 200")
    void getAllFormulas_withPeriodId_returns200() throws Exception {
        when(camabaRegistrationService.getAllFormulas(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/camaba/all-formulas").param("periodId", "1"))
                .andExpect(status().isOk());
    }

    // ===== getOpenRegistrationPeriods =====

    @Test
    @DisplayName("GET /api/camaba/registration-periods - returns 200")
    void getOpenRegistrationPeriods_returns200() throws Exception {
        when(registrationPeriodRepository.findByStatusOrderByRegStartDateDesc(RegistrationPeriod.Status.OPEN))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/camaba/registration-periods"))
                .andExpect(status().isOk());
    }

    // ===== getRegistrationPeriodById =====

    @Test
    @DisplayName("GET /api/camaba/registration-periods/{id} - found returns 200")
    void getRegistrationPeriodById_found_returns200() throws Exception {
        RegistrationPeriod period = new RegistrationPeriod();
        period.setId(1L);
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));

        mockMvc.perform(get("/api/camaba/registration-periods/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/registration-periods/{id} - not found returns 404")
    void getRegistrationPeriodById_notFound_returns404() throws Exception {
        when(registrationPeriodRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/camaba/registration-periods/999"))
                .andExpect(status().isNotFound());
    }

    // ===== getProgramStudiByJenisSeleksi =====

    @Test
    @DisplayName("GET /api/camaba/jenis-seleksi/{id}/program-studi - returns 200")
    void getProgramStudiByJenisSeleksi_returns200() throws Exception {
        when(camabaRegistrationService.getProgramStudiByJenisSeleksi(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/camaba/jenis-seleksi/1/program-studi"))
                .andExpect(status().isOk());
    }

    // ===== getSelectionTypes =====

    @Test
    @DisplayName("GET /api/camaba/selection-types/{periodId} - returns 200")
    void getSelectionTypes_returns200() throws Exception {
        when(selectionTypeRepository.findByPeriod_IdAndIsActiveTrue(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/camaba/selection-types/1"))
                .andExpect(status().isOk());
    }

    // ===== getSelectionTypeDetail =====

    @Test
    @DisplayName("GET /api/camaba/selection-types-detail/{id} - found returns 200")
    void getSelectionTypeDetail_found_returns200() throws Exception {
        when(camabaRegistrationService.getSelectionTypeDetail(1L)).thenReturn(Map.of("id", 1L));

        mockMvc.perform(get("/api/camaba/selection-types-detail/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/selection-types-detail/{id} - not found returns 404")
    void getSelectionTypeDetail_notFound_returns404() throws Exception {
        when(camabaRegistrationService.getSelectionTypeDetail(999L))
                .thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/camaba/selection-types-detail/999"))
                .andExpect(status().isNotFound());
    }

    // ===== getJenisSeleksiDetail =====

    @Test
    @DisplayName("GET /api/camaba/jenis-seleksi-detail/{id} - found returns 200")
    void getJenisSeleksiDetail_found_returns200() throws Exception {
        when(camabaRegistrationService.getJenisSeleksiDetail(1L)).thenReturn(Map.of("id", 1L));

        mockMvc.perform(get("/api/camaba/jenis-seleksi-detail/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/jenis-seleksi-detail/{id} - exception returns 404")
    void getJenisSeleksiDetail_exception_returns404() throws Exception {
        when(camabaRegistrationService.getJenisSeleksiDetail(999L))
                .thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/camaba/jenis-seleksi-detail/999"))
                .andExpect(status().isNotFound());
    }

    // ===== getJenisSeleksiById =====

    @Test
    @DisplayName("GET /api/camaba/jenis-seleksi/{id} - found returns 200")
    void getJenisSeleksiById_found_returns200() throws Exception {
        when(camabaRegistrationService.getJenisSeleksiById(1L)).thenReturn(Map.of("id", 1L));

        mockMvc.perform(get("/api/camaba/jenis-seleksi/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/jenis-seleksi/{id} - exception returns 404")
    void getJenisSeleksiById_exception_returns404() throws Exception {
        when(camabaRegistrationService.getJenisSeleksiById(999L))
                .thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/camaba/jenis-seleksi/999"))
                .andExpect(status().isNotFound());
    }

    // ===== selectGelombang =====

    @Test
    @DisplayName("POST /api/camaba/select-gelombang - null gelombangId returns 400")
    void selectGelombang_nullId_returns400() throws Exception {
        mockMvc.perform(post("/api/camaba/select-gelombang")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/camaba/select-gelombang - valid gelombangId returns 200")
    void selectGelombang_valid_returns200() throws Exception {
        User user = new User();
        user.setEmail("student@test.com");
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/camaba/select-gelombang")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("gelombangId", 1))))
                .andExpect(status().isOk());
    }

    // ===== selectFormula =====

    @Test
    @DisplayName("POST /api/camaba/select-formula - null formula returns 400")
    void selectFormula_nullFormula_returns400() throws Exception {
        mockMvc.perform(post("/api/camaba/select-formula")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/camaba/select-formula - valid formula returns 200")
    void selectFormula_validFormula_returns200() throws Exception {
        User user = new User();
        user.setEmail("student@test.com");
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/camaba/select-formula")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("formula", "Kedokteran"))))
                .andExpect(status().isOk());
    }

    // ===== Exception paths for uncovered methods =====

    @Test
    @DisplayName("GET /api/camaba/registration-periods - exception returns 400")
    void getOpenRegistrationPeriods_exception_returns400() throws Exception {
        when(registrationPeriodRepository.findByStatusOrderByRegStartDateDesc(RegistrationPeriod.Status.OPEN))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/camaba/registration-periods"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/camaba/selection-types/{periodId} - exception returns 400")
    void getSelectionTypes_exception_returns400() throws Exception {
        when(selectionTypeRepository.findByPeriod_IdAndIsActiveTrue(1L))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/camaba/selection-types/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/camaba/all-formulas - exception returns 400")
    void getAllFormulas_exception_returns400() throws Exception {
        when(camabaRegistrationService.getAllFormulas(null))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/camaba/all-formulas"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/camaba/jenis-seleksi/{id}/program-studi - exception returns 400")
    void getProgramStudiByJenisSeleksi_exception_returns400() throws Exception {
        when(camabaRegistrationService.getProgramStudiByJenisSeleksi(99L))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/camaba/jenis-seleksi/99/program-studi"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/camaba/registration-periods/{id} - exception returns 400")
    void getRegistrationPeriodById_exception_returns400() throws Exception {
        when(registrationPeriodRepository.findById(2L))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/camaba/registration-periods/2"))
                .andExpect(status().isBadRequest());
    }
}
