package com.uhn.pmb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uhn.pmb.dto.JenisSeleksiRequest;
import com.uhn.pmb.entity.JenisSeleksi;
import com.uhn.pmb.service.JenisSeleksiService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminJenisSeleksiControllerTest {

    @Mock
    private JenisSeleksiService jenisSeleksiService;

    @InjectMocks
    private AdminJenisSeleksiController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private JenisSeleksiRequest buildRequest() {
        JenisSeleksiRequest req = new JenisSeleksiRequest();
        req.setNama("Jalur Mandiri");
        req.setCode("MANDIRI");
        req.setDeskripsi("Jalur Mandiri");
        req.setFasilitas("Fasilitas standar");
        req.setLogoUrl("http://logo.test");
        req.setHarga(new java.math.BigDecimal(500000));
        req.setIsActive(true);
        req.setSortOrder(1);
        return req;
    }

    private JenisSeleksi buildJenisSeleksi(Long id, String nama) {
        return JenisSeleksi.builder()
                .id(id)
                .code("MANDIRI")
                .nama(nama)
                .deskripsi("Jalur Mandiri")
                .fasilitas("Fasilitas standar")
                .logoUrl("http://logo.test")
                .harga(new java.math.BigDecimal(500000))
                .isActive(true)
                .sortOrder(1)
                .build();
    }

    // ===== POST /admin/jenis-seleksi =====

    @Test
    @DisplayName("POST /admin/jenis-seleksi - success returns 201")
    void createJenisSeleksi_success_returns201() throws Exception {
        JenisSeleksi saved = buildJenisSeleksi(1L, "Jalur Mandiri");
        when(jenisSeleksiService.createWithProgramStudi(any())).thenReturn(saved);

        mockMvc.perform(post("/admin/jenis-seleksi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /admin/jenis-seleksi - exception returns 400")
    void createJenisSeleksi_exception_returns400() throws Exception {
        doThrow(new RuntimeException("Nama jenis seleksi sudah ada"))
                .when(jenisSeleksiService).createWithProgramStudi(any());

        mockMvc.perform(post("/admin/jenis-seleksi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Nama jenis seleksi sudah ada"));
    }

    // ===== GET /admin/jenis-seleksi =====

    @Test
    @DisplayName("GET /admin/jenis-seleksi - success returns 200")
    void getAllJenisSeleksi_success_returns200() throws Exception {
        when(jenisSeleksiService.getAll()).thenReturn(List.of(new JenisSeleksi()));

        mockMvc.perform(get("/admin/jenis-seleksi"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/jenis-seleksi - exception returns 400")
    void getAllJenisSeleksi_exception_returns400() throws Exception {
        when(jenisSeleksiService.getAll()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/admin/jenis-seleksi"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===== GET /admin/jenis-seleksi/active =====

    @Test
    @DisplayName("GET /admin/jenis-seleksi/active - success returns 200")
    void getActiveJenisSeleksi_success_returns200() throws Exception {
        when(jenisSeleksiService.getAllActive()).thenReturn(List.of(new JenisSeleksi()));

        mockMvc.perform(get("/admin/jenis-seleksi/active"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/jenis-seleksi/active - exception returns 400")
    void getActiveJenisSeleksi_exception_returns400() throws Exception {
        when(jenisSeleksiService.getAllActive()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/admin/jenis-seleksi/active"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===== GET /admin/jenis-seleksi/{id} =====

    @Test
    @DisplayName("GET /admin/jenis-seleksi/{id} - found returns 200")
    void getJenisSeleksiById_found_returns200() throws Exception {
        when(jenisSeleksiService.getById(1L)).thenReturn(Optional.of(new JenisSeleksi()));

        mockMvc.perform(get("/admin/jenis-seleksi/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/jenis-seleksi/{id} - not found returns 400")
    void getJenisSeleksiById_notFound_returns400() throws Exception {
        when(jenisSeleksiService.getById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/jenis-seleksi/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Jenis Seleksi not found"));
    }

    @Test
    @DisplayName("GET /admin/jenis-seleksi/{id} - exception returns 400")
    void getJenisSeleksiById_exception_returns400() throws Exception {
        when(jenisSeleksiService.getById(1L)).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/admin/jenis-seleksi/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===== PUT /admin/jenis-seleksi/{id} =====

    @Test
    @DisplayName("PUT /admin/jenis-seleksi/{id} - success returns 200")
    void updateJenisSeleksi_success_returns200() throws Exception {
        JenisSeleksi updated = buildJenisSeleksi(1L, "Jalur Mandiri Updated");
        when(jenisSeleksiService.updateWithProgramStudi(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/admin/jenis-seleksi/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PUT /admin/jenis-seleksi/{id} - exception returns 400")
    void updateJenisSeleksi_exception_returns400() throws Exception {
        doThrow(new RuntimeException("Jenis Seleksi tidak ditemukan"))
                .when(jenisSeleksiService).updateWithProgramStudi(eq(99L), any());

        mockMvc.perform(put("/admin/jenis-seleksi/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Jenis Seleksi tidak ditemukan"));
    }

    // ===== DELETE /admin/jenis-seleksi/{id} =====

    @Test
    @DisplayName("DELETE /admin/jenis-seleksi/{id} - success returns 200")
    void deleteJenisSeleksi_success_returns200() throws Exception {
        doNothing().when(jenisSeleksiService).delete(1L);

        mockMvc.perform(delete("/admin/jenis-seleksi/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /admin/jenis-seleksi/{id} - exception returns 400")
    void deleteJenisSeleksi_exception_returns400() throws Exception {
        doThrow(new RuntimeException("Jenis Seleksi masih digunakan"))
                .when(jenisSeleksiService).delete(1L);

        mockMvc.perform(delete("/admin/jenis-seleksi/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Jenis Seleksi masih digunakan"));
    }
}