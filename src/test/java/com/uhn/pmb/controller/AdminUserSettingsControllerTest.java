package com.uhn.pmb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uhn.pmb.service.AdminUserSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminUserSettingsControllerTest {

    @Mock private AdminUserSettingsService adminUserSettingsService;
    @InjectMocks private AdminUserSettingsController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /admin/api/users - returns 200 with list")
    void getAllUsers_returns200() throws Exception {
        when(adminUserSettingsService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/admin/api/users"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/api/users/{id}/role - returns 200")
    void updateUserRole_returns200() throws Exception {
        doNothing().when(adminUserSettingsService).updateUserRole(1L, "ADMIN_PUSAT");

        mockMvc.perform(put("/admin/api/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "ADMIN_PUSAT"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /admin/api/users/{id} - returns 200")
    void deleteUser_returns200() throws Exception {
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "admin@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        doNothing().when(adminUserSettingsService).deleteUser(1L, "admin@test.com");

        mockMvc.perform(delete("/admin/api/users/1"))
                .andExpect(status().isOk());

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /admin/api/settings/{key} - returns 200")
    void getActiveSetting_returns200() throws Exception {
        when(adminUserSettingsService.getActiveSetting("gform")).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/admin/api/settings/gform"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/settings - returns 200")
    void getSystemSettings_returns200() throws Exception {
        when(adminUserSettingsService.getActiveSettings()).thenReturn(java.util.Map.of("key1", "val1"));

        mockMvc.perform(get("/admin/api/settings"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/api/settings/{key} - saves and returns 200")
    void updateSetting_returns200() throws Exception {
        when(adminUserSettingsService.saveSetting(any(), any())).thenReturn(new com.uhn.pmb.entity.SystemConfiguration());

        mockMvc.perform(put("/admin/api/settings/theme")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("value", "dark"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/gform-link - empty returns 200")
    void getExamGFormLink_empty_returns200() throws Exception {
        when(adminUserSettingsService.getGformLink()).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/admin/api/gform-link"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /admin/api/gform-link - sets link returns 200")
    void setExamGFormLink_returns200() throws Exception {
        when(adminUserSettingsService.saveGformLink(any())).thenReturn(new com.uhn.pmb.entity.SystemConfiguration());

        mockMvc.perform(post("/admin/api/gform-link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("gformLink", "https://forms.google.com/test"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /admin/api/gform-link - deletes and returns 200")
    void deleteExamGFormLink_returns200() throws Exception {
        doNothing().when(adminUserSettingsService).deleteGformLink();

        mockMvc.perform(delete("/admin/api/gform-link"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/users - service throws returns 400")
    void getAllUsers_serviceThrows_returnsBadRequest() throws Exception {
        when(adminUserSettingsService.getAllUsers()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/admin/api/users"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /admin/api/users/{id}/role - service exception returns 400")
    void updateUserRole_serviceException_returns400() throws Exception {
        doThrow(new RuntimeException("Update failed")).when(adminUserSettingsService).updateUserRole(1L, "ADMIN_PUSAT");

        mockMvc.perform(put("/admin/api/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "ADMIN_PUSAT"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /admin/api/users/{id} - service exception returns 400")
    void deleteUser_serviceException_returns400() throws Exception {
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "admin@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        doThrow(new RuntimeException("Delete failed")).when(adminUserSettingsService).deleteUser(1L, "admin@test.com");

        mockMvc.perform(delete("/admin/api/users/1"))
                .andExpect(status().isBadRequest());

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /admin/api/settings - service exception returns 400")
    void getSystemSettings_serviceException_returns400() throws Exception {
        when(adminUserSettingsService.getActiveSettings()).thenThrow(new RuntimeException("Fetch failed"));

        mockMvc.perform(get("/admin/api/settings"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/settings/{key} - service exception returns 200")
    void getSetting_serviceException_returns200() throws Exception {
        when(adminUserSettingsService.getActiveSetting("key")).thenThrow(new RuntimeException("Fetch failed"));

        mockMvc.perform(get("/admin/api/settings/key"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/api/settings/{key} - service exception returns 400")
    void updateSetting_serviceException_returns400() throws Exception {
        when(adminUserSettingsService.saveSetting(any(), any())).thenThrow(new RuntimeException("Save failed"));

        mockMvc.perform(put("/admin/api/settings/theme")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("value", "dark"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/gform-link - service exception returns 400")
    void getExamGFormLink_serviceException_returns400() throws Exception {
        when(adminUserSettingsService.getGformLink()).thenThrow(new RuntimeException("Fetch failed"));

        mockMvc.perform(get("/admin/api/gform-link"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /admin/api/gform-link - service exception returns 400")
    void setExamGFormLink_serviceException_returns400() throws Exception {
        when(adminUserSettingsService.saveGformLink(any())).thenThrow(new RuntimeException("Save failed"));

        mockMvc.perform(post("/admin/api/gform-link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("gformLink", "https://forms.google.com"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /admin/api/gform-link - service exception returns 400")
    void deleteExamGFormLink_serviceException_returns400() throws Exception {
        doThrow(new RuntimeException("Delete failed")).when(adminUserSettingsService).deleteGformLink();

        mockMvc.perform(delete("/admin/api/gform-link"))
                .andExpect(status().isBadRequest());
    }

    // ===== ITERASI 6 ADDITIONS =====

    @Test
    @DisplayName("GET /admin/api/settings/{key} - found returns 200 with key and value")
    void getSetting_found_returns200WithKeyAndValue() throws Exception {
        com.uhn.pmb.entity.SystemConfiguration config = new com.uhn.pmb.entity.SystemConfiguration();
        config.setConfigKey("theme");
        config.setConfigValue("dark");

        when(adminUserSettingsService.getActiveSetting("theme")).thenReturn(java.util.Optional.of(config));

        mockMvc.perform(get("/admin/api/settings/theme"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.key").value("theme"))
                .andExpect(jsonPath("$.value").value("dark"));
    }

    @Test
    @DisplayName("POST /admin/api/gform-link - null gformLink returns 400")
    void setExamGFormLink_nullLink_returns400() throws Exception {
        mockMvc.perform(post("/admin/api/gform-link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /admin/api/gform-link - blank gformLink returns 400")
    void setExamGFormLink_blankLink_returns400() throws Exception {
        mockMvc.perform(post("/admin/api/gform-link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("gformLink", "   "))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/gform-link - present returns 200 with value")
    void getExamGFormLink_present_returns200WithValue() throws Exception {
        com.uhn.pmb.entity.SystemConfiguration config = new com.uhn.pmb.entity.SystemConfiguration();
        config.setConfigKey("exam_gform_link");
        config.setConfigValue("https://forms.google.com/real-link");

        when(adminUserSettingsService.getGformLink()).thenReturn(java.util.Optional.of(config));

        mockMvc.perform(get("/admin/api/gform-link"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.gformLink").value("https://forms.google.com/real-link"));
    }
}
