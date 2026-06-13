package com.uhn.pmb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uhn.pmb.dto.ProgramStudiRequest;
import com.uhn.pmb.entity.ProgramStudi;
import com.uhn.pmb.service.ProgramStudiManagementService;
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

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminProgramStudiControllerTest {

    @Mock
    private ProgramStudiManagementService programStudiManagementService;

    @InjectMocks
    private AdminProgramStudiController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private ProgramStudiRequest buildRequest() {
        ProgramStudiRequest req = new ProgramStudiRequest();
        req.setNama("Teknik Informatika");
        req.setKode("TI");
        req.setDeskripsi("Program Studi Teknik Informatika");
        req.setIsMedical(false);
        req.setIsActive(true);
        req.setSortOrder(1);
        req.setHargaTotalPerTahun(100000000L);
        req.setCicilan1(20000000L);
        req.setCicilan2(20000000L);
        req.setCicilan3(20000000L);
        req.setCicilan4(20000000L);
        req.setCicilan5(10000000L);
        req.setCicilan6(10000000L);
        return req;
    }

    private ProgramStudi buildProgramStudi() {
        return ProgramStudi.builder()
                .id(1L)
                .kode("TI")
                .nama("Teknik Informatika")
                .deskripsi("Program Studi Teknik Informatika")
                .isMedical(false)
                .isActive(true)
                .sortOrder(1)
                .hargaTotalPerTahun(100000000L)
                .cicilan1(20000000L)
                .cicilan2(20000000L)
                .cicilan3(20000000L)
                .cicilan4(20000000L)
                .cicilan5(10000000L)
                .cicilan6(10000000L)
                .build();
    }

    // ===== POST /admin/program-studi =====

    @Test
    @DisplayName("POST /admin/program-studi - success returns 201")
    void createProgramStudi_success_returns201() throws Exception {
        doReturn(buildProgramStudi())
        .when(programStudiManagementService).createProgramStudiFull(any());
        mockMvc.perform(post("/admin/program-studi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /admin/program-studi - IllegalArgumentException returns 400")
    void createProgramStudi_illegalArgument_returns400() throws Exception {
        doThrow(new IllegalArgumentException("Nama program studi sudah ada"))
                .when(programStudiManagementService).createProgramStudiFull(any());

        mockMvc.perform(post("/admin/program-studi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Nama program studi sudah ada"));
    }

    @Test
    @DisplayName("POST /admin/program-studi - general exception returns 400")
    void createProgramStudi_generalException_returns400() throws Exception {
        doThrow(new RuntimeException("DB error"))
                .when(programStudiManagementService).createProgramStudiFull(any());

        mockMvc.perform(post("/admin/program-studi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===== GET /admin/program-studi =====

    @Test
    @DisplayName("GET /admin/program-studi - success returns 200")
    void getAllProgramStudi_success_returns200() throws Exception {
        when(programStudiManagementService.getAllProgramStudiWithDetails()).thenReturn(List.of());

        mockMvc.perform(get("/admin/program-studi"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/program-studi - exception returns 400")
    void getAllProgramStudi_exception_returns400() throws Exception {
        when(programStudiManagementService.getAllProgramStudiWithDetails())
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/admin/program-studi"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===== GET /admin/program-studi/active =====

    @Test
    @DisplayName("GET /admin/program-studi/active - success returns 200")
    void getActiveProgramStudi_success_returns200() throws Exception {
        when(programStudiManagementService.getActiveProgramStudiSimple()).thenReturn(List.of());

        mockMvc.perform(get("/admin/program-studi/active"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/program-studi/active - exception returns 400")
    void getActiveProgramStudi_exception_returns400() throws Exception {
        when(programStudiManagementService.getActiveProgramStudiSimple())
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/admin/program-studi/active"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===== GET /admin/program-studi/{id} =====

    @Test
    @DisplayName("GET /admin/program-studi/{id} - success returns 200")
    void getProgramStudiById_success_returns200() throws Exception {
        when(programStudiManagementService.getProgramStudiById(1L)).thenReturn(buildProgramStudi());

        mockMvc.perform(get("/admin/program-studi/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/program-studi/{id} - exception returns 400")
    void getProgramStudiById_exception_returns400() throws Exception {
        when(programStudiManagementService.getProgramStudiById(99L))
                .thenThrow(new RuntimeException("Data tidak ditemukan"));

        mockMvc.perform(get("/admin/program-studi/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===== PUT /admin/program-studi/{id} =====

    @Test
    @DisplayName("PUT /admin/program-studi/{id} - success returns 200")
    void updateProgramStudi_success_returns200() throws Exception {
        doReturn(buildProgramStudi())
        .when(programStudiManagementService).updateProgramStudiById(eq(1L), any());
        mockMvc.perform(put("/admin/program-studi/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PUT /admin/program-studi/{id} - IllegalArgumentException returns 400")
    void updateProgramStudi_illegalArgument_returns400() throws Exception {
        doThrow(new IllegalArgumentException("Program studi tidak ditemukan"))
                .when(programStudiManagementService).updateProgramStudiById(eq(99L), any());

        mockMvc.perform(put("/admin/program-studi/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Program studi tidak ditemukan"));
    }

    @Test
    @DisplayName("PUT /admin/program-studi/{id} - general exception returns 400")
    void updateProgramStudi_generalException_returns400() throws Exception {
        doThrow(new RuntimeException("DB error"))
                .when(programStudiManagementService).updateProgramStudiById(eq(1L), any());

        mockMvc.perform(put("/admin/program-studi/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===== DELETE /admin/program-studi/{id} =====

    @Test
    @DisplayName("DELETE /admin/program-studi/{id} - success returns 200")
    void deleteProgramStudi_success_returns200() throws Exception {
        doNothing().when(programStudiManagementService).deleteProgramStudiById(1L);

        mockMvc.perform(delete("/admin/program-studi/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /admin/program-studi/{id} - IllegalStateException returns 400")
    void deleteProgramStudi_illegalState_returns400() throws Exception {
        doThrow(new IllegalStateException("Program studi masih digunakan"))
                .when(programStudiManagementService).deleteProgramStudiById(1L);

        mockMvc.perform(delete("/admin/program-studi/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Program studi masih digunakan"));
    }

    @Test
    @DisplayName("DELETE /admin/program-studi/{id} - general exception returns 400")
    void deleteProgramStudi_generalException_returns400() throws Exception {
        doThrow(new RuntimeException("DB error"))
                .when(programStudiManagementService).deleteProgramStudiById(99L);

        mockMvc.perform(delete("/admin/program-studi/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===== GET /admin/api/program-studi/available =====

    @Test
    @DisplayName("GET /admin/api/program-studi/available - success returns 200")
    void getAvailableProgramStudi_success_returns200() throws Exception {
        when(programStudiManagementService.getActiveProgramStudiSimple()).thenReturn(List.of());

        mockMvc.perform(get("/admin/api/program-studi/available"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/program-studi/available - exception returns 400")
    void getAvailableProgramStudi_exception_returns400() throws Exception {
        when(programStudiManagementService.getActiveProgramStudiSimple())
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/admin/api/program-studi/available"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===== GET /admin/api/jenis-seleksi/{jenisSeleksiId}/program-studi =====

    @Test
    @DisplayName("GET /admin/api/jenis-seleksi/{jenisSeleksiId}/program-studi - success returns 200")
    void getProgramStudiByJenisSeleksi_success_returns200() throws Exception {
        when(programStudiManagementService.getProgramStudiByJenisSeleksi(1L)).thenReturn(List.of());

        mockMvc.perform(get("/admin/api/jenis-seleksi/1/program-studi"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/jenis-seleksi/{jenisSeleksiId}/program-studi - exception returns 400")
    void getProgramStudiByJenisSeleksi_exception_returns400() throws Exception {
        when(programStudiManagementService.getProgramStudiByJenisSeleksi(99L))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/admin/api/jenis-seleksi/99/program-studi"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===== POST /admin/program-studi/bulk-initialize =====

    @Test
    @DisplayName("POST /admin/program-studi/bulk-initialize - success returns 200")
    void bulkInitializeProgramStudi_success_returns200() throws Exception {
        when(programStudiManagementService.bulkInitializeProgramStudi()).thenReturn(Map.of("success", true));

        mockMvc.perform(post("/admin/program-studi/bulk-initialize"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /admin/program-studi/bulk-initialize - exception returns 400")
    void bulkInitializeProgramStudi_exception_returns400() throws Exception {
        when(programStudiManagementService.bulkInitializeProgramStudi())
                .thenThrow(new RuntimeException("Bulk insert failed"));

        mockMvc.perform(post("/admin/program-studi/bulk-initialize"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error: Bulk insert failed"));
    }
}