package com.uhn.pmb.controller;

import com.uhn.pmb.service.AdminHasilAkhirService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminHasilAkhirControllerTest {

    @Mock
    private AdminHasilAkhirService adminHasilAkhirService;

    @InjectMocks
    private AdminHasilAkhirController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@test.com", "password", List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /admin/api/hasil-akhir/all - returns 200")
    void getAllHasilAkhir_returns200() throws Exception {
        when(adminHasilAkhirService.getAllHasilAkhirForAdmin()).thenReturn(List.of(Map.of("id", 1)));

        mockMvc.perform(get("/admin/api/hasil-akhir/all"))
                .andExpect(status().isOk());

        verify(adminHasilAkhirService).getAllHasilAkhirForAdmin();
    }

    @Test
    @DisplayName("GET /admin/api/hasil-akhir/all - exception returns 500")
    void getAllHasilAkhir_throwsException_returns500() throws Exception {
        when(adminHasilAkhirService.getAllHasilAkhirForAdmin()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/admin/api/hasil-akhir/all"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /admin/api/hasil-akhir/{id}/upload-dokumen - success returns 200")
    void uploadDokumenSementara_success_returns200() throws Exception {
        MockMultipartFile npmFile = new MockMultipartFile(
                "npmSementara", "npm.pdf", "application/pdf", "content".getBytes());
        when(adminHasilAkhirService.uploadDokumenSementara(eq(1L), any(), any()))
                .thenReturn(Map.of("success", true));

        mockMvc.perform(multipart("/admin/api/hasil-akhir/1/upload-dokumen").file(npmFile))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /admin/api/hasil-akhir/{id}/upload-dokumen - IllegalArgumentException returns 400")
    void uploadDokumenSementara_illegalArg_returns400() throws Exception {
        MockMultipartFile npmFile = new MockMultipartFile(
                "npmSementara", "npm.pdf", "application/pdf", "content".getBytes());
        when(adminHasilAkhirService.uploadDokumenSementara(eq(1L), any(), any()))
                .thenThrow(new IllegalArgumentException("Both files are null"));

        mockMvc.perform(multipart("/admin/api/hasil-akhir/1/upload-dokumen").file(npmFile))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /admin/api/hasil-akhir/{id}/upload-dokumen - Exception returns 500")
    void uploadDokumenSementara_generalException_returns500() throws Exception {
        MockMultipartFile npmFile = new MockMultipartFile(
                "npmSementara", "npm.pdf", "application/pdf", "content".getBytes());
        when(adminHasilAkhirService.uploadDokumenSementara(eq(1L), any(), any()))
                .thenThrow(new RuntimeException("Upload failed"));

        mockMvc.perform(multipart("/admin/api/hasil-akhir/1/upload-dokumen").file(npmFile))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /admin/api/hasil-akhir/{id}/upload-dokumen - no files returns 200")
    void uploadDokumenSementara_noFiles_returns200() throws Exception {
        when(adminHasilAkhirService.uploadDokumenSementara(eq(2L), any(), any()))
                .thenReturn(Map.of("success", true, "message", "uploaded"));

        mockMvc.perform(multipart("/admin/api/hasil-akhir/2/upload-dokumen"))
                .andExpect(status().isOk());
    }
}
