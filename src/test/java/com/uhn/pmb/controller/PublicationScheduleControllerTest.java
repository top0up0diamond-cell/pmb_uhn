package com.uhn.pmb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uhn.pmb.service.PublicationScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
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
class PublicationScheduleControllerTest {

    @Mock
    private PublicationScheduleService publicationScheduleService;

    @InjectMocks
    private PublicationScheduleController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private Authentication mockAuth() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin1");
        return auth;
    }

    // ===== GET /admin/api/publication-schedule =====

    @Test
    @DisplayName("GET /admin/api/publication-schedule - returns 200")
    void getAllSchedules_returns200() throws Exception {
        when(publicationScheduleService.getAllSchedules()).thenReturn(List.of());

        mockMvc.perform(get("/admin/api/publication-schedule"))
                .andExpect(status().isOk());
    }

    // ===== GET /admin/api/publication-schedule/{periodId} =====

    @Test
    @DisplayName("GET /admin/api/publication-schedule/{periodId} - returns 200")
    void getScheduleByPeriod_returns200() throws Exception {
        when(publicationScheduleService.getScheduleByPeriod(1L)).thenReturn(Map.of());

        mockMvc.perform(get("/admin/api/publication-schedule/1"))
                .andExpect(status().isOk());
    }

    // ===== POST /admin/api/publication-schedule =====

    @Test
    @DisplayName("POST /admin/api/publication-schedule - success returns 200")
    void createOrUpdateSchedule_success_returns200() throws Exception {
        when(publicationScheduleService.createOrUpdate(eq(1L), eq("2026-07-01T10:00:00"), eq("Catatan"), eq("admin1")))
                .thenReturn(Map.of("success", true));

        Map<String, Object> body = Map.of(
                "periodId", 1,
                "publishDateTime", "2026-07-01T10:00:00",
                "notes", "Catatan"
        );

        mockMvc.perform(post("/admin/api/publication-schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .principal(mockAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /admin/api/publication-schedule - without notes returns 200")
    void createOrUpdateSchedule_withoutNotes_returns200() throws Exception {
        when(publicationScheduleService.createOrUpdate(eq(1L), eq("2026-07-01T10:00:00"), eq(null), eq("admin1")))
                .thenReturn(Map.of("success", true));

        Map<String, Object> body = Map.of(
                "periodId", 1,
                "publishDateTime", "2026-07-01T10:00:00"
        );

        mockMvc.perform(post("/admin/api/publication-schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .principal(mockAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /admin/api/publication-schedule - missing periodId returns 400")
    void createOrUpdateSchedule_missingPeriodId_returns400() throws Exception {
        Map<String, Object> body = Map.of(
                "publishDateTime", "2026-07-01T10:00:00"
        );

        mockMvc.perform(post("/admin/api/publication-schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .principal(mockAuth()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /admin/api/publication-schedule - service exception returns 400")
    void createOrUpdateSchedule_serviceException_returns400() throws Exception {
        when(publicationScheduleService.createOrUpdate(eq(1L), any(), any(), eq("admin1")))
                .thenThrow(new RuntimeException("Periode tidak ditemukan"));

        Map<String, Object> body = Map.of(
                "periodId", 1,
                "publishDateTime", "2026-07-01T10:00:00"
        );

        mockMvc.perform(post("/admin/api/publication-schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .principal(mockAuth()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Periode tidak ditemukan"));
    }

    // ===== POST /admin/api/publication-schedule/{periodId}/publish-now =====

    @Test
    @DisplayName("POST /admin/api/publication-schedule/{periodId}/publish-now - success returns 200")
    void publishNow_success_returns200() throws Exception {
        when(publicationScheduleService.publishNow(eq(1L), eq("admin1")))
                .thenReturn(Map.of("success", true));

        mockMvc.perform(post("/admin/api/publication-schedule/1/publish-now")
                        .principal(mockAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /admin/api/publication-schedule/{periodId}/publish-now - exception returns 400")
    void publishNow_exception_returns400() throws Exception {
        when(publicationScheduleService.publishNow(eq(99L), eq("admin1")))
                .thenThrow(new RuntimeException("Periode tidak ditemukan"));

        mockMvc.perform(post("/admin/api/publication-schedule/99/publish-now")
                        .principal(mockAuth()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Periode tidak ditemukan"));
    }

    // ===== DELETE /admin/api/publication-schedule/{id} =====

    @Test
    @DisplayName("DELETE /admin/api/publication-schedule/{id} - returns 200")
    void deleteSchedule_returns200() throws Exception {
        doNothing().when(publicationScheduleService).deleteSchedule(1L);

        mockMvc.perform(delete("/admin/api/publication-schedule/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Jadwal dihapus"));
    }
}