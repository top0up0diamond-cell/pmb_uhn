package com.uhn.pmb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uhn.pmb.dto.RegistrationPeriodRequest;
import com.uhn.pmb.dto.SelectionTypeRequest;
import com.uhn.pmb.entity.JenisSeleksi;
import com.uhn.pmb.entity.RegistrationPeriod;
import com.uhn.pmb.service.PeriodManagementService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminPeriodControllerTest {

    @Mock private PeriodManagementService periodManagementService;
    @InjectMocks private AdminPeriodController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper.findAndRegisterModules();
    }

    @Test
    @DisplayName("GET /admin/periods - returns 200 with list")
    void getAllPeriods_returns200() throws Exception {
        when(periodManagementService.getAllPeriods()).thenReturn(List.of(new RegistrationPeriod()));

        mockMvc.perform(get("/admin/periods"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /admin/periods - creates and returns 201")
    void createPeriod_returns201() throws Exception {
        RegistrationPeriodRequest req = new RegistrationPeriodRequest();
        req.setName("Gelombang 1");
        when(periodManagementService.createPeriod(any())).thenReturn(new RegistrationPeriod());

        mockMvc.perform(post("/admin/periods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("DELETE /admin/periods/{id} - deletes and returns 200")
    void deletePeriod_returns200() throws Exception {
        doNothing().when(periodManagementService).deletePeriod(1L);

        mockMvc.perform(delete("/admin/periods/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/jenis-seleksi/available - returns 200")
    void getAllAvailableJenisSeleksi_returns200() throws Exception {
        when(periodManagementService.getAllAvailableJenisSeleksi()).thenReturn(List.of(new JenisSeleksi()));

        mockMvc.perform(get("/admin/api/jenis-seleksi/available"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/periods/{id} - updates and returns 200")
    void updatePeriod_returns200() throws Exception {
        RegistrationPeriodRequest req = new RegistrationPeriodRequest();
        req.setName("Updated Gelombang");
        when(periodManagementService.updatePeriod(eq(1L), any())).thenReturn(new RegistrationPeriod());

        mockMvc.perform(put("/admin/periods/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/periods/{periodId}/jenis-seleksi - returns 200")
    void getJenisSeleksiByPeriod_returns200() throws Exception {
        when(periodManagementService.getJenisSeleksiByPeriod(1L)).thenReturn(List.of());

        mockMvc.perform(get("/admin/api/periods/1/jenis-seleksi"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /admin/selection-types - creates and returns 201")
    void createSelectionType_returns201() throws Exception {
        SelectionTypeRequest req = new SelectionTypeRequest();
        req.setName("Jalur Reguler");
        doNothing().when(periodManagementService).createSelectionType(any());

        mockMvc.perform(post("/admin/selection-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("GET /admin/api/selection-types/period/{periodId} - returns 200")
    void getSelectionTypesByPeriod_returns200() throws Exception {
        when(periodManagementService.getSelectionTypesByPeriod(1L)).thenReturn(List.of());

        mockMvc.perform(get("/admin/api/selection-types/period/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /admin/api/selection-types/{id} - deletes and returns 200")
    void deleteSelectionType_returns200() throws Exception {
        doNothing().when(periodManagementService).deleteSelectionType(1L);

        mockMvc.perform(delete("/admin/api/selection-types/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /admin/periods - exception returns 400")
    void createPeriod_exception_returns400() throws Exception {
        RegistrationPeriodRequest req = new RegistrationPeriodRequest();
        req.setName("Gelombang 1");
        when(periodManagementService.createPeriod(any())).thenThrow(new RuntimeException("error"));

        mockMvc.perform(post("/admin/periods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/periods - exception returns 400")
    void getAllPeriods_exception_returns400() throws Exception {
        when(periodManagementService.getAllPeriods()).thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/admin/periods"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /admin/periods/{id} - exception returns 400")
    void updatePeriod_exception_returns400() throws Exception {
        RegistrationPeriodRequest req = new RegistrationPeriodRequest();
        req.setName("Updated");
        when(periodManagementService.updatePeriod(eq(1L), any())).thenThrow(new RuntimeException("error"));

        mockMvc.perform(put("/admin/periods/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /admin/periods/{id} - exception returns 400")
    void deletePeriod_exception_returns400() throws Exception {
        doThrow(new RuntimeException("error")).when(periodManagementService).deletePeriod(1L);

        mockMvc.perform(delete("/admin/periods/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/jenis-seleksi/available - exception returns 400")
    void getAllAvailableJenisSeleksi_exception_returns400() throws Exception {
        when(periodManagementService.getAllAvailableJenisSeleksi()).thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/admin/api/jenis-seleksi/available"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/periods/{periodId}/jenis-seleksi - exception returns 400")
    void getJenisSeleksiByPeriod_exception_returns400() throws Exception {
        when(periodManagementService.getJenisSeleksiByPeriod(1L)).thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/admin/api/periods/1/jenis-seleksi"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /admin/selection-types - exception returns 400")
    void createSelectionType_exception_returns400() throws Exception {
        SelectionTypeRequest req = new SelectionTypeRequest();
        req.setName("Jalur Reguler");
        doThrow(new RuntimeException("error")).when(periodManagementService).createSelectionType(any());

        mockMvc.perform(post("/admin/selection-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/selection-types/period/{periodId} - exception returns 400")
    void getSelectionTypesByPeriod_exception_returns400() throws Exception {
        when(periodManagementService.getSelectionTypesByPeriod(1L)).thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/admin/api/selection-types/period/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /admin/api/selection-types/{id} - exception returns 400")
    void deleteSelectionType_exception_returns400() throws Exception {
        doThrow(new RuntimeException("error")).when(periodManagementService).deleteSelectionType(1L);

        mockMvc.perform(delete("/admin/api/selection-types/1"))
                .andExpect(status().isBadRequest());
    }
}
