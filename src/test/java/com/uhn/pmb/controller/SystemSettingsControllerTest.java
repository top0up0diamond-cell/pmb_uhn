package com.uhn.pmb.controller;

import com.uhn.pmb.entity.SystemLink;
import com.uhn.pmb.service.SystemSettingsService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SystemSettingsControllerTest {

    @Mock private SystemSettingsService systemSettingsService;
    @InjectMocks private SystemSettingsController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /admin/api/settings/contact-info - returns 200")
    void getContactInfo_returns200() throws Exception {
        when(systemSettingsService.getContactInfo()).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/api/settings/contact-info"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/settings/system-links - returns 200")
    void getAllSystemLinks_returns200() throws Exception {
        when(systemSettingsService.getAllSystemLinks()).thenReturn(List.of(new SystemLink()));

        mockMvc.perform(get("/admin/api/settings/system-links"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/settings/system-links/active - returns 200")
    void getActiveSystemLinks_returns200() throws Exception {
        when(systemSettingsService.getActiveSystemLinks()).thenReturn(List.of());

        mockMvc.perform(get("/admin/api/settings/system-links/active"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /admin/api/settings/system-links/{id} - returns 200")
    void deleteSystemLink_returns200() throws Exception {
        doNothing().when(systemSettingsService).deleteSystemLink(1);

        mockMvc.perform(delete("/admin/api/settings/system-links/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /admin/api/settings/contact-info - returns 200")
    void updateContactInfo_returns200() throws Exception {
        com.uhn.pmb.entity.ContactInfo ci = new com.uhn.pmb.entity.ContactInfo();
        when(systemSettingsService.saveContactInfo(any())).thenReturn(ci);

        mockMvc.perform(post("/admin/api/settings/contact-info")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/settings/system-links/{id} - returns 200")
    void getSystemLink_returns200() throws Exception {
        when(systemSettingsService.getSystemLinkById(1)).thenReturn(Optional.of(new SystemLink()));

        mockMvc.perform(get("/admin/api/settings/system-links/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /admin/api/settings/system-links - returns 200")
    void createSystemLink_returns200() throws Exception {
        when(systemSettingsService.createSystemLink(any())).thenReturn(new SystemLink());

        mockMvc.perform(post("/admin/api/settings/system-links")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"test\",\"url\":\"http://test.com\",\"isActive\":true}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/api/settings/system-links/{id} - returns 200")
    void updateSystemLink_returns200() throws Exception {
        when(systemSettingsService.updateSystemLink(eq(1), any())).thenReturn(new SystemLink());

        mockMvc.perform(put("/admin/api/settings/system-links/1")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"test\",\"url\":\"http://test.com\",\"isActive\":true}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/settings/contact-info - empty returns 200 with null data")
    void getContactInfo_empty_returns200WithNullData() throws Exception {
        when(systemSettingsService.getContactInfo()).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/api/settings/contact-info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("GET /admin/api/settings/system-links/{id} - found returns 200 with data")
    void getSystemLink_found_returns200WithData() throws Exception {
        SystemLink link = new SystemLink();
        when(systemSettingsService.getSystemLinkById(1)).thenReturn(Optional.of(link));

        mockMvc.perform(get("/admin/api/settings/system-links/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /admin/api/settings/system-links/{id} - not found returns 200 with message")
    void getSystemLink_notFound_returns200WithMessage() throws Exception {
        when(systemSettingsService.getSystemLinkById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/api/settings/system-links/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Link not found"));
    }

    @Test
    @DisplayName("POST /admin/api/settings/contact-info - service exception returns 200 with error")
    void updateContactInfo_serviceException_returns200WithError() throws Exception {
        when(systemSettingsService.saveContactInfo(any())).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(post("/admin/api/settings/contact-info")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /admin/api/settings/system-links - empty list returns 200")
    void getAllSystemLinks_empty_returns200WithEmptyList() throws Exception {
        when(systemSettingsService.getAllSystemLinks()).thenReturn(List.of());

        mockMvc.perform(get("/admin/api/settings/system-links"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    @DisplayName("DELETE /admin/api/settings/system-links/{id} - service exception returns 200 with error")
    void deleteSystemLink_serviceException_returns200WithError() throws Exception {
        doThrow(new RuntimeException("Delete error")).when(systemSettingsService).deleteSystemLink(1);

        mockMvc.perform(delete("/admin/api/settings/system-links/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ===== ITERASI 6 ADDITIONS =====

    @Test
    @DisplayName("GET /admin/api/settings/contact-info - present returns 200 with data")
    void getContactInfo_present_returns200WithData() throws Exception {
        com.uhn.pmb.entity.ContactInfo ci = new com.uhn.pmb.entity.ContactInfo();
        ci.setAddress("Jl. Test");
        ci.setPhone("0812");
        ci.setEmail("contact@test.com");
        ci.setOperatingHours("08:00-17:00");

        when(systemSettingsService.getContactInfo()).thenReturn(Optional.of(ci));

        mockMvc.perform(get("/admin/api/settings/contact-info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /admin/api/settings/system-links/active - exception returns 200 with error")
    void getActiveSystemLinks_serviceException_returns200WithError() throws Exception {
        when(systemSettingsService.getActiveSystemLinks()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/admin/api/settings/system-links/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /admin/api/settings/system-links - exception returns 200 with error")
    void createSystemLink_serviceException_returns200WithError() throws Exception {
        when(systemSettingsService.createSystemLink(any())).thenThrow(new RuntimeException("Save error"));

        mockMvc.perform(post("/admin/api/settings/system-links")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"test\",\"url\":\"http://test.com\",\"isActive\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("PUT /admin/api/settings/system-links/{id} - exception returns 200 with error")
    void updateSystemLink_serviceException_returns200WithError() throws Exception {
        when(systemSettingsService.updateSystemLink(eq(1), any())).thenThrow(new RuntimeException("Update error"));

        mockMvc.perform(put("/admin/api/settings/system-links/1")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"test\",\"url\":\"http://test.com\",\"isActive\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /admin/api/settings/system-links - exception returns 200 with error")
    void getAllSystemLinks_serviceException_returns200WithError() throws Exception {
        when(systemSettingsService.getAllSystemLinks()).thenThrow(new RuntimeException("Fetch error"));

        mockMvc.perform(get("/admin/api/settings/system-links"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /admin/api/settings/system-links/{id} - exception returns 200 with error")
    void getSystemLinkById_serviceException_returns200WithError() throws Exception {
        when(systemSettingsService.getSystemLinkById(1)).thenThrow(new RuntimeException("Fetch error"));

        mockMvc.perform(get("/admin/api/settings/system-links/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

}
