package com.uhn.pmb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uhn.pmb.dto.CicilanRequestDTO;
import com.uhn.pmb.service.AdminCicilanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminCicilanControllerTest {

    @Mock
    private AdminCicilanService adminCicilanService;

    @InjectMocks
    private AdminCicilanController adminCicilanController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminCicilanController).build();
    }

    @Test
    @DisplayName("GET /admin/cicilan/pending - returns 200")
    void getPendingRequests_returns200() throws Exception {
        when(adminCicilanService.getPendingRequests(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/admin/cicilan/pending"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/cicilan/by-status/PENDING - returns 200")
    void getByStatus_returns200() throws Exception {
        when(adminCicilanService.getByStatus(eq("PENDING"), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/admin/cicilan/by-status/PENDING"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/cicilan/{id}/approve - approves cicilan 200")
    void approveCicilan_returns200() throws Exception {
        AdminCicilanService.ApproveRequest approveReq = new AdminCicilanService.ApproveRequest();
        approveReq.setApprovedBy("admin");
        CicilanRequestDTO dto = CicilanRequestDTO.builder().id(1L).build();
        when(adminCicilanService.approveCicilanRequest(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(put("/admin/cicilan/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveReq)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/cicilan/{id}/approve - not found returns 400")
    void approveCicilan_notFound_returns400() throws Exception {
        when(adminCicilanService.approveCicilanRequest(eq(999L), any()))
                .thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(put("/admin/cicilan/999/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approvedBy\": \"admin\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /admin/cicilan/{id}/reject - success returns 200")
    void rejectCicilan_success_returns200() throws Exception {
        CicilanRequestDTO dto = CicilanRequestDTO.builder().id(1L).build();
        AdminCicilanService.RejectRequest req = new AdminCicilanService.RejectRequest();
        req.setReason("Invalid documents");
        req.setStudentEmail("s@test.com");
        when(adminCicilanService.rejectCicilanRequest(eq(1L), any())).thenReturn(dto);
        mockMvc.perform(put("/admin/cicilan/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/cicilan/{id}/reject - RuntimeException returns 400")
    void rejectCicilan_exception_returns400() throws Exception {
        AdminCicilanService.RejectRequest req = new AdminCicilanService.RejectRequest();
        req.setReason("Invalid");
        when(adminCicilanService.rejectCicilanRequest(eq(99L), any()))
                .thenThrow(new RuntimeException("Not found"));
        mockMvc.perform(put("/admin/cicilan/99/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /admin/cicilan/{id} - success returns 200")
    void deleteCicilan_success_returns200() throws Exception {
        doNothing().when(adminCicilanService).deleteCicilanRequest(1L);
        mockMvc.perform(delete("/admin/cicilan/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /admin/cicilan/{id} - RuntimeException returns 400")
    void deleteCicilan_exception_returns400() throws Exception {
        doThrow(new RuntimeException("Not found")).when(adminCicilanService).deleteCicilanRequest(99L);
        mockMvc.perform(delete("/admin/cicilan/99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/cicilan/pending - exception returns 400")
    void getPendingRequests_exception_returns400() throws Exception {
        when(adminCicilanService.getPendingRequests(any())).thenThrow(new RuntimeException("DB error"));
        mockMvc.perform(get("/admin/cicilan/pending"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/cicilan/by-status/{status} - exception returns 400")
    void getByStatus_exception_returns400() throws Exception {
        when(adminCicilanService.getByStatus(eq("PENDING"), any()))
                .thenThrow(new RuntimeException("DB error"));
        mockMvc.perform(get("/admin/cicilan/by-status/PENDING"))
                .andExpect(status().isBadRequest());
    }
}
