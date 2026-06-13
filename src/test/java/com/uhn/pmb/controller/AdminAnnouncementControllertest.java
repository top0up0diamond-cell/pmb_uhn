package com.uhn.pmb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uhn.pmb.dto.CreateAnnouncementRequest;
import com.uhn.pmb.entity.Announcement;
import com.uhn.pmb.service.AnnouncementService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminAnnouncementControllerTest {

    @Mock private AnnouncementService announcementService;
    @InjectMocks private AdminAnnouncementController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /admin/api/announcements - returns 200 with list")
    void getAllAnnouncements_returns200() throws Exception {
        Announcement a = new Announcement();
        a.setId(1L);
        a.setTitle("Test");
        a.setContent("Content");
        a.setIsActive(true);
        when(announcementService.findAllActive()).thenReturn(List.of(a));

        mockMvc.perform(get("/admin/api/announcements"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /admin/api/announcements - creates announcement and returns 201")
    void createAnnouncement_returns201() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin@test.com");

        CreateAnnouncementRequest req = new CreateAnnouncementRequest();
        req.setTitle("Test Announcement");
        req.setContent("Test Content");
        req.setPriority(1);

        mockMvc.perform(post("/admin/api/announcements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(auth))
                .andExpect(status().isCreated());

        verify(announcementService).create(any(), anyString());
    }

    @Test
    @DisplayName("DELETE /admin/api/announcements/{id} - deletes and returns 200")
    void deleteAnnouncement_returns200() throws Exception {
        doNothing().when(announcementService).delete(1L);

        mockMvc.perform(delete("/admin/api/announcements/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/api/announcements/{id}/deactivate - deactivates and returns 200")
    void deactivateAnnouncement_returns200() throws Exception {
        Announcement a = new Announcement();
        when(announcementService.deactivate(1L)).thenReturn(a);

        mockMvc.perform(put("/admin/api/announcements/1/deactivate"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/announcements/paginated - returns 200")
    void getAnnouncementsPaginated_returns200() throws Exception {
        org.springframework.data.domain.Page<Announcement> page =
                new org.springframework.data.domain.PageImpl<>(List.of(new Announcement()));
        when(announcementService.findAllActivePaginated(any())).thenReturn(page);

        mockMvc.perform(get("/admin/api/announcements/paginated")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/announcements/recent - returns 200")
    void getRecentAnnouncements_returns200() throws Exception {
        when(announcementService.findRecent()).thenReturn(List.of());

        mockMvc.perform(get("/admin/api/announcements/recent"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/announcements/urgent - returns 200")
    void getUrgentAnnouncements_returns200() throws Exception {
        when(announcementService.findUrgent()).thenReturn(List.of());

        mockMvc.perform(get("/admin/api/announcements/urgent"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/announcements/{id} - found returns 200")
    void getAnnouncementById_found_returns200() throws Exception {
        Announcement a = new Announcement();
        a.setId(1L);
        a.setTitle("Test");
        a.setContent("Content");
        a.setIsActive(true);
        when(announcementService.findActiveById(1L)).thenReturn(a);

        mockMvc.perform(get("/admin/api/announcements/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/api/announcements/{id} - updates and returns 200")
    void updateAnnouncement_returns200() throws Exception {
        CreateAnnouncementRequest req = new CreateAnnouncementRequest();
        req.setTitle("Updated Title");
        req.setContent("Updated Content");
        req.setPriority(1);
        when(announcementService.update(eq(1L), any())).thenReturn(new com.uhn.pmb.entity.Announcement());

        mockMvc.perform(put("/admin/api/announcements/1")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/announcements/search/{keyword} - returns 200")
    void searchAnnouncements_returns200() throws Exception {
        when(announcementService.search("test")).thenReturn(List.of());

        mockMvc.perform(get("/admin/api/announcements/search/test"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /admin/api/announcements - service exception returns 400")
    void createAnnouncement_serviceException_returns400() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin@test.com");
        doThrow(new RuntimeException("Create failed")).when(announcementService).create(any(), anyString());

        CreateAnnouncementRequest req = new CreateAnnouncementRequest();
        req.setTitle("Test");
        req.setContent("Content");
        req.setPriority(1);

        mockMvc.perform(post("/admin/api/announcements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .principal(auth))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/announcements - service exception returns 400")
    void getAllAnnouncements_serviceException_returns400() throws Exception {
        when(announcementService.findAllActive()).thenThrow(new RuntimeException("Fetch failed"));

        mockMvc.perform(get("/admin/api/announcements"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/announcements/paginated - service exception returns 400")
    void getAnnouncementsPaginated_serviceException_returns400() throws Exception {
        when(announcementService.findAllActivePaginated(any())).thenThrow(new RuntimeException("Fetch failed"));

        mockMvc.perform(get("/admin/api/announcements/paginated"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/announcements/recent - service exception returns 400")
    void getRecentAnnouncements_serviceException_returns400() throws Exception {
        when(announcementService.findRecent()).thenThrow(new RuntimeException("Fetch failed"));

        mockMvc.perform(get("/admin/api/announcements/recent"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/announcements/urgent - service exception returns 400")
    void getUrgentAnnouncements_serviceException_returns400() throws Exception {
        when(announcementService.findUrgent()).thenThrow(new RuntimeException("Fetch failed"));

        mockMvc.perform(get("/admin/api/announcements/urgent"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/announcements/{id} - service exception returns 400")
    void getAnnouncementById_serviceException_returns400() throws Exception {
        when(announcementService.findActiveById(1L)).thenThrow(new RuntimeException("Fetch failed"));

        mockMvc.perform(get("/admin/api/announcements/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /admin/api/announcements/{id} - service exception returns 400")
    void updateAnnouncement_serviceException_returns400() throws Exception {
        when(announcementService.update(eq(1L), any())).thenThrow(new RuntimeException("Update failed"));

        CreateAnnouncementRequest req = new CreateAnnouncementRequest();
        req.setTitle("Updated");
        req.setContent("Content");
        req.setPriority(1);

        mockMvc.perform(put("/admin/api/announcements/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /admin/api/announcements/{id} - service exception returns 400")
    void deleteAnnouncement_serviceException_returns400() throws Exception {
        doThrow(new RuntimeException("Delete failed")).when(announcementService).delete(1L);

        mockMvc.perform(delete("/admin/api/announcements/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /admin/api/announcements/{id}/deactivate - service exception returns 400")
    void deactivateAnnouncement_serviceException_returns400() throws Exception {
        when(announcementService.deactivate(1L)).thenThrow(new RuntimeException("Deactivate failed"));

        mockMvc.perform(put("/admin/api/announcements/1/deactivate"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/announcements/search/{keyword} - service exception returns 400")
    void searchAnnouncements_serviceException_returns400() throws Exception {
        when(announcementService.search("test")).thenThrow(new RuntimeException("Search failed"));

        mockMvc.perform(get("/admin/api/announcements/search/test"))
                .andExpect(status().isBadRequest());
    }
}
