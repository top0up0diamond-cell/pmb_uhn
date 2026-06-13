package com.uhn.pmb.controller;

import com.uhn.pmb.entity.JenisSeleksi;
import com.uhn.pmb.entity.ProgramStudi;
import com.uhn.pmb.entity.RegistrationPeriod;
import com.uhn.pmb.service.PublicDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PublicApiControllerTest {

    @Mock private PublicDataService publicDataService;
    @InjectMocks private PublicApiController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /api/public/gelombang - returns 200")
    void getAllGelombang_returns200() throws Exception {
        when(publicDataService.getAllGelombang()).thenReturn(List.of(new RegistrationPeriod()));

        mockMvc.perform(get("/api/public/gelombang"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/public/jenis-seleksi - returns 200")
    void getAllJenisSeleksi_returns200() throws Exception {
        when(publicDataService.getAllActiveJenisSeleksi()).thenReturn(List.of(new JenisSeleksi()));

        mockMvc.perform(get("/api/public/jenis-seleksi"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/public/program-studi - returns 200")
    void getAllProgramStudi_returns200() throws Exception {
        when(publicDataService.getAllActiveProgramStudi()).thenReturn(List.of(new ProgramStudi()));

        mockMvc.perform(get("/api/public/program-studi"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/public/program-studi/{id} - found returns 200")
    void getProgramStudiById_found_returns200() throws Exception {
        ProgramStudi ps = new ProgramStudi();
        when(publicDataService.getProgramStudiById(1L)).thenReturn(Optional.of(ps));

        mockMvc.perform(get("/api/public/program-studi/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/public/fakultas - returns 200")
    void getAllFakultas_returns200() throws Exception {
        when(publicDataService.getAllFakultas()).thenReturn(List.of("FT", "FK"));

        mockMvc.perform(get("/api/public/fakultas"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/public/settings - returns 200")
    void getPublicSettings_returns200() throws Exception {
        mockMvc.perform(get("/api/public/settings"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/public/publication-status/{periodId} - returns 200")
    void getPublicationStatus_returns200() throws Exception {
        when(publicDataService.getPublicationStatus(1L)).thenReturn(java.util.Map.of("published", false));

        mockMvc.perform(get("/api/public/publication-status/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/public/program-studi/by-fakultas - returns 200")
    void getProgramStudiByFakultas_returns200() throws Exception {
        when(publicDataService.getProgramStudiByFakultas()).thenReturn(java.util.Map.of());

        mockMvc.perform(get("/api/public/program-studi/by-fakultas"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/public/program-studi/{id} - not found returns 200")
    void getProgramStudiById_notFound_returns200() throws Exception {
        when(publicDataService.getProgramStudiById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/public/program-studi/99"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/public/gelombang - service throws returns 200 with error")
    void getAllGelombang_serviceThrows_returns200() throws Exception {
        when(publicDataService.getAllGelombang()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/public/gelombang"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/public/jenis-seleksi - service throws returns 200 with error")
    void getAllJenisSeleksi_serviceThrows_returns200() throws Exception {
        when(publicDataService.getAllActiveJenisSeleksi()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/public/jenis-seleksi"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/public/program-studi - service throws returns 200 with error")
    void getAllProgramStudi_serviceThrows_returns200() throws Exception {
        when(publicDataService.getAllActiveProgramStudi()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/public/program-studi"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/public/fakultas - service throws returns 200 with error")
    void getAllFakultas_serviceThrows_returns200() throws Exception {
        when(publicDataService.getAllFakultas()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/public/fakultas"))
                .andExpect(status().isOk());
    }
}
