package com.uhn.pmb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uhn.pmb.entity.Sma;
import com.uhn.pmb.service.SmaService;
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
class SmaControllerTest {

    @Mock
    private SmaService smaService;

    @InjectMocks
    private SmaController smaController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(smaController).build();
        objectMapper = new ObjectMapper();
    }

    private Sma buildSma(Long id) {
        return Sma.builder()
                .id(id)
                .nama("SMA Negeri 1")
                .npsn("12345678")
                .kota("Medan")
                .provinsi("Sumatera Utara")
                .isActive(true)
                .build();
    }

    // ===== searchSma =====

    @Test
    @DisplayName("GET /api/sma/search - returns 200 with search results")
    void searchSma_returns200() throws Exception {
        when(smaService.search("SMA")).thenReturn(List.of(buildSma(1L)));

        mockMvc.perform(get("/api/sma/search").param("q", "SMA"))
                .andExpect(status().isOk());

        verify(smaService).search("SMA");
    }

    @Test
    @DisplayName("GET /api/sma/search - empty query returns empty list")
    void searchSma_emptyQuery_returnsEmpty() throws Exception {
        when(smaService.search("")).thenReturn(List.of());

        mockMvc.perform(get("/api/sma/search").param("q", ""))
                .andExpect(status().isOk());
    }

    // ===== searchExternalSekolah =====

    @Test
    @DisplayName("GET /api/sekolah/external - query shorter than 2 chars returns empty")
    void searchExternalSekolah_shortQuery_returnsEmpty() throws Exception {
        mockMvc.perform(get("/api/sekolah/external").param("q", "A"))
                .andExpect(status().isOk());

        verifyNoInteractions(smaService);
    }

    // ===== getAllActiveSma =====

    @Test
    @DisplayName("GET /api/sma - returns 200 with active SMA list")
    void getAllActiveSma_returns200() throws Exception {
        when(smaService.findAllActive()).thenReturn(List.of(buildSma(1L)));

        mockMvc.perform(get("/api/sma"))
                .andExpect(status().isOk());
    }

    // ===== getAllSmaForAdmin =====

    @Test
    @DisplayName("GET /admin/api/sma - returns 200")
    void getAllSmaForAdmin_returns200() throws Exception {
        when(smaService.findAll()).thenReturn(List.of(buildSma(1L)));

        mockMvc.perform(get("/admin/api/sma"))
                .andExpect(status().isOk());
    }

    // ===== createSma =====

    @Test
    @DisplayName("POST /admin/api/sma - success returns 200")
    void createSma_success_returns200() throws Exception {
        Sma sma = buildSma(1L);
        when(smaService.create(any())).thenReturn(sma);

        Map<String, String> body = Map.of("nama", "SMA Negeri 1", "npsn", "12345678");

        mockMvc.perform(post("/admin/api/sma")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /admin/api/sma - IllegalArgumentException returns 400")
    void createSma_illegalArgument_returns400() throws Exception {
        when(smaService.create(any())).thenThrow(new IllegalArgumentException("Nama wajib diisi"));

        mockMvc.perform(post("/admin/api/sma")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /admin/api/sma - generic exception returns 500")
    void createSma_exception_returns500() throws Exception {
        when(smaService.create(any())).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/admin/api/sma")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("nama", "X"))))
                .andExpect(status().isInternalServerError());
    }

    // ===== updateSma =====

    @Test
    @DisplayName("PUT /admin/api/sma/{id} - success returns 200")
    void updateSma_success_returns200() throws Exception {
        Sma sma = buildSma(1L);
        when(smaService.update(eq(1L), any())).thenReturn(sma);

        mockMvc.perform(put("/admin/api/sma/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("nama", "SMA Updated"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/api/sma/{id} - IllegalArgumentException returns 400")
    void updateSma_illegalArgument_returns400() throws Exception {
        when(smaService.update(eq(1L), any())).thenThrow(new IllegalArgumentException("NPSN duplikat"));

        mockMvc.perform(put("/admin/api/sma/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("npsn", "dup"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /admin/api/sma/{id} - generic exception returns 500")
    void updateSma_genericException_returns500() throws Exception {
        when(smaService.update(eq(1L), any())).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(put("/admin/api/sma/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("nama", "Updated"))))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/sekolah/external - valid query attempts external call")
    void searchExternalSekolah_validQuery_returnsResult() throws Exception {
        // Will either succeed or fail with network error; both paths return 200 with a list
        mockMvc.perform(get("/api/sekolah/external").param("q", "SMA Negeri"))
                .andExpect(status().isOk());
    }

    // ===== deleteSma =====

    @Test
    @DisplayName("DELETE /admin/api/sma/{id} - success returns 200")
    void deleteSma_success_returns200() throws Exception {
        doNothing().when(smaService).deactivate(1L);

        mockMvc.perform(delete("/admin/api/sma/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /admin/api/sma/{id} - exception returns 500")
    void deleteSma_exception_returns500() throws Exception {
        doThrow(new RuntimeException("Not found")).when(smaService).deactivate(999L);

        mockMvc.perform(delete("/admin/api/sma/999"))
                .andExpect(status().isInternalServerError());
    }
}
