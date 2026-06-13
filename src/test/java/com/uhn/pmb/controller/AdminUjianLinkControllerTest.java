package com.uhn.pmb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uhn.pmb.dto.UjianLinkRequest;
import com.uhn.pmb.entity.GelombangLinkUjian;
import com.uhn.pmb.service.AdminUjianLinkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUjianLinkController.class)
class AdminUjianLinkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminUjianLinkService ujianLinkService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private GelombangLinkUjian dummyLink() {
        GelombangLinkUjian link = new GelombangLinkUjian();
        link.setId(1L);
        return link;
    }

    // ===== GET ALL =====

    @Test
    @DisplayName("GET /admin/api/ujian-links - returns 200 with list")
    @WithMockUser(roles = "ADMIN_PUSAT")
    void getAllUjianLinks_authenticated_returns200() throws Exception {
        when(ujianLinkService.getAllLinks()).thenReturn(List.of(dummyLink()));

        mockMvc.perform(get("/admin/api/ujian-links"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /admin/api/ujian-links - no auth returns 401")
    void getAllUjianLinks_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/admin/api/ujian-links"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /admin/api/ujian-links - service throws returns 500")
    @WithMockUser(roles = "ADMIN_PUSAT")
    void getAllUjianLinks_serviceThrows_returns500() throws Exception {
        when(ujianLinkService.getAllLinks()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/admin/api/ujian-links"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===== GET BY PERIOD =====

    @Test
    @DisplayName("GET /admin/api/ujian-links/by-period/{periodId} - found returns 200")
    @WithMockUser(roles = "ADMIN_PUSAT")
    void getUjianLinkByPeriod_found_returns200() throws Exception {
        when(ujianLinkService.getByPeriodId(1L)).thenReturn(Optional.of(dummyLink()));

        mockMvc.perform(get("/admin/api/ujian-links/by-period/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /admin/api/ujian-links/by-period/{periodId} - not found returns 404")
    @WithMockUser(roles = "ADMIN_PUSAT")
    void getUjianLinkByPeriod_notFound_returns404() throws Exception {
        when(ujianLinkService.getByPeriodId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/api/ujian-links/by-period/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /admin/api/ujian-links/by-period/{periodId} - no auth returns 401")
    void getUjianLinkByPeriod_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/admin/api/ujian-links/by-period/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /admin/api/ujian-links/by-period/{periodId} - service throws returns 500")
    @WithMockUser(roles = "ADMIN_PUSAT")
    void getUjianLinkByPeriod_serviceThrows_returns500() throws Exception {
        when(ujianLinkService.getByPeriodId(1L)).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/admin/api/ujian-links/by-period/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===== POST (CREATE) =====

    @Test
    @DisplayName("POST /admin/api/ujian-links - success returns 200")
    @WithMockUser(roles = "ADMIN_PUSAT")
    void saveUjianLink_success_returns200() throws Exception {
        UjianLinkRequest req = new UjianLinkRequest();
        req.setPeriodId(1L);

        when(ujianLinkService.createLink(any())).thenReturn(dummyLink());

        mockMvc.perform(post("/admin/api/ujian-links")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /admin/api/ujian-links - no auth returns 401")
    void saveUjianLink_noAuth_returns401() throws Exception {
        UjianLinkRequest req = new UjianLinkRequest();

        mockMvc.perform(post("/admin/api/ujian-links")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /admin/api/ujian-links - RuntimeException returns 400")
    @WithMockUser(roles = "ADMIN_PUSAT")
    void saveUjianLink_runtimeException_returns400() throws Exception {
        UjianLinkRequest req = new UjianLinkRequest();
        when(ujianLinkService.createLink(any())).thenThrow(new RuntimeException("Already exists"));

        mockMvc.perform(post("/admin/api/ujian-links")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /admin/api/ujian-links - unexpected exception returns 500")
    @WithMockUser(roles = "ADMIN_PUSAT")
    void saveUjianLink_unexpectedException_returns500() throws Exception {
        UjianLinkRequest req = new UjianLinkRequest();
        // Controller catches RuntimeException as 400, so to hit the 500 branch
        // we must throw a non-RuntimeException checked exception via spy/Answer
        when(ujianLinkService.createLink(any())).thenAnswer(inv -> {
            throw new Exception("Unexpected checked exception");
        });

        mockMvc.perform(post("/admin/api/ujian-links")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===== PUT (UPDATE) =====

    @Test
    @DisplayName("PUT /admin/api/ujian-links - success returns 200")
    @WithMockUser(roles = "ADMIN_PUSAT")
    void updateUjianLink_success_returns200() throws Exception {
        UjianLinkRequest req = new UjianLinkRequest();
        req.setPeriodId(1L);

        when(ujianLinkService.updateLink(any())).thenReturn(dummyLink());

        mockMvc.perform(put("/admin/api/ujian-links")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PUT /admin/api/ujian-links - no auth returns 401")
    void updateUjianLink_noAuth_returns401() throws Exception {
        UjianLinkRequest req = new UjianLinkRequest();

        mockMvc.perform(put("/admin/api/ujian-links")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /admin/api/ujian-links - RuntimeException returns 400")
    @WithMockUser(roles = "ADMIN_PUSAT")
    void updateUjianLink_runtimeException_returns400() throws Exception {
        UjianLinkRequest req = new UjianLinkRequest();
        when(ujianLinkService.updateLink(any())).thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(put("/admin/api/ujian-links")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("PUT /admin/api/ujian-links - unexpected exception returns 500")
    @WithMockUser(roles = "ADMIN_PUSAT")
    void updateUjianLink_unexpectedException_returns500() throws Exception {
        UjianLinkRequest req = new UjianLinkRequest();
        when(ujianLinkService.updateLink(any())).thenAnswer(inv -> {
            throw new Exception("Unexpected checked exception");
        });

        mockMvc.perform(put("/admin/api/ujian-links")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===== DELETE =====

    @Test
    @DisplayName("DELETE /admin/api/ujian-links/{periodId} - success returns 200")
    @WithMockUser(roles = "ADMIN_PUSAT")
    void deleteUjianLink_success_returns200() throws Exception {
        doNothing().when(ujianLinkService).deleteByPeriodId(1L);

        mockMvc.perform(delete("/admin/api/ujian-links/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /admin/api/ujian-links/{periodId} - no auth returns 401")
    void deleteUjianLink_noAuth_returns401() throws Exception {
        mockMvc.perform(delete("/admin/api/ujian-links/1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /admin/api/ujian-links/{periodId} - RuntimeException returns 400")
    @WithMockUser(roles = "ADMIN_PUSAT")
    void deleteUjianLink_runtimeException_returns400() throws Exception {
        doThrow(new RuntimeException("Not found")).when(ujianLinkService).deleteByPeriodId(1L);

        mockMvc.perform(delete("/admin/api/ujian-links/1")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("DELETE /admin/api/ujian-links/{periodId} - unexpected exception returns 500")
    @WithMockUser(roles = "ADMIN_PUSAT")
    void deleteUjianLink_unexpectedException_returns500() throws Exception {
        doAnswer(inv -> { throw new Exception("Unexpected checked exception"); })
                .when(ujianLinkService).deleteByPeriodId(1L);

        mockMvc.perform(delete("/admin/api/ujian-links/1")
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===== POST OFFLINE EXAM =====

    @Test
    @DisplayName("POST /admin/api/ujian-links/offline-exams - success returns 200")
    @WithMockUser(roles = "ADMIN_PUSAT")
    void saveOfflineExam_success_returns200() throws Exception {
        UjianLinkRequest req = new UjianLinkRequest();
        req.setPeriodId(1L);

        when(ujianLinkService.createOfflineExam(any())).thenReturn(dummyLink());

        mockMvc.perform(post("/admin/api/ujian-links/offline-exams")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /admin/api/ujian-links/offline-exams - no auth returns 401")
    void saveOfflineExam_noAuth_returns401() throws Exception {
        UjianLinkRequest req = new UjianLinkRequest();

        mockMvc.perform(post("/admin/api/ujian-links/offline-exams")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /admin/api/ujian-links/offline-exams - RuntimeException returns 400")
    @WithMockUser(roles = "ADMIN_PUSAT")
    void saveOfflineExam_runtimeException_returns400() throws Exception {
        UjianLinkRequest req = new UjianLinkRequest();
        when(ujianLinkService.createOfflineExam(any())).thenThrow(new RuntimeException("Invalid data"));

        mockMvc.perform(post("/admin/api/ujian-links/offline-exams")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /admin/api/ujian-links/offline-exams - unexpected exception returns 500")
    @WithMockUser(roles = "ADMIN_PUSAT")
    void saveOfflineExam_unexpectedException_returns500() throws Exception {
        UjianLinkRequest req = new UjianLinkRequest();
        when(ujianLinkService.createOfflineExam(any())).thenAnswer(inv -> {
            throw new Exception("Unexpected checked exception");
        });

        mockMvc.perform(post("/admin/api/ujian-links/offline-exams")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===== DELETE OFFLINE EXAM =====

    @Test
    @DisplayName("DELETE /admin/api/ujian-links/offline-exams/{periodId} - success returns 200")
    @WithMockUser(roles = "ADMIN_PUSAT")
    void deleteOfflineExam_success_returns200() throws Exception {
        doNothing().when(ujianLinkService).deleteOfflineExam(1L);

        mockMvc.perform(delete("/admin/api/ujian-links/offline-exams/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /admin/api/ujian-links/offline-exams/{periodId} - no auth returns 401")
    void deleteOfflineExam_noAuth_returns401() throws Exception {
        mockMvc.perform(delete("/admin/api/ujian-links/offline-exams/1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /admin/api/ujian-links/offline-exams/{periodId} - RuntimeException returns 400")
    @WithMockUser(roles = "ADMIN_PUSAT")
    void deleteOfflineExam_runtimeException_returns400() throws Exception {
        doThrow(new RuntimeException("Not found")).when(ujianLinkService).deleteOfflineExam(1L);

        mockMvc.perform(delete("/admin/api/ujian-links/offline-exams/1")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("DELETE /admin/api/ujian-links/offline-exams/{periodId} - unexpected exception returns 500")
    @WithMockUser(roles = "ADMIN_PUSAT")
    void deleteOfflineExam_unexpectedException_returns500() throws Exception {
        doAnswer(inv -> { throw new Exception("Unexpected checked exception"); })
                .when(ujianLinkService).deleteOfflineExam(1L);

        mockMvc.perform(delete("/admin/api/ujian-links/offline-exams/1")
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }
}