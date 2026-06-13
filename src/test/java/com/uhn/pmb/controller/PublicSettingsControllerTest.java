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

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PublicSettingsControllerTest {

    @Mock private SystemSettingsService systemSettingsService;
    @InjectMocks private PublicSettingsController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /api/public/settings/contact-info - returns 200")
    void getContactInfo_returns200() throws Exception {
        com.uhn.pmb.entity.ContactInfo ci = new com.uhn.pmb.entity.ContactInfo();
        when(systemSettingsService.getContactInfo()).thenReturn(Optional.of(ci));

        mockMvc.perform(get("/api/public/settings/contact-info"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/public/settings/system-links - returns 200")
    void getSystemLinks_returns200() throws Exception {
        when(systemSettingsService.getActiveSystemLinks()).thenReturn(List.of(new SystemLink()));

        mockMvc.perform(get("/api/public/settings/system-links"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/public/settings/system-links/name/{name} - returns 200")
    void getSystemLinkByName_returns200() throws Exception {
        SystemLink link = new SystemLink();
        when(systemSettingsService.getSystemLinkByName("portal")).thenReturn(Optional.of(link));

        mockMvc.perform(get("/api/public/settings/system-links/name/portal"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/public/settings/system-links/type/{type} - returns 200")
    void getSystemLinksByType_returns200() throws Exception {
        when(systemSettingsService.getSystemLinksByType("SOCIAL")).thenReturn(List.of(new SystemLink()));

        mockMvc.perform(get("/api/public/settings/system-links/type/SOCIAL"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/public/settings/contact-info - present returns 200")
    void getContactInfo_empty_returns200() throws Exception {
        com.uhn.pmb.entity.ContactInfo ci = new com.uhn.pmb.entity.ContactInfo();
        when(systemSettingsService.getContactInfo()).thenReturn(Optional.of(ci));

        mockMvc.perform(get("/api/public/settings/contact-info"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/public/settings/contact-info - service throws returns 500")
    void getContactInfo_serviceThrows_returns500() throws Exception {
        when(systemSettingsService.getContactInfo()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/public/settings/contact-info"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/public/settings/contact-info - empty Optional returns 200")
    void getContactInfo_emptyOptional_returns200() throws Exception {
        when(systemSettingsService.getContactInfo()).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/public/settings/contact-info"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/public/settings/system-links - service throws returns 500")
    void getSystemLinks_throws_returns500() throws Exception {
        when(systemSettingsService.getActiveSystemLinks()).thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/api/public/settings/system-links"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/public/settings/system-links/name/{name} - not found returns 200")
    void getSystemLinkByName_notFound_returns200() throws Exception {
        when(systemSettingsService.getSystemLinkByName("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/public/settings/system-links/name/unknown"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/public/settings/system-links/name/{name} - throws returns 500")
    void getSystemLinkByName_throws_returns500() throws Exception {
        when(systemSettingsService.getSystemLinkByName(any())).thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/api/public/settings/system-links/name/portal"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/public/settings/system-links/type/{type} - throws returns 500")
    void getSystemLinksByType_throws_returns500() throws Exception {
        when(systemSettingsService.getSystemLinksByType(any())).thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/api/public/settings/system-links/type/SOCIAL"))
                .andExpect(status().isInternalServerError());
    }
}
