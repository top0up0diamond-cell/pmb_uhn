package com.uhn.pmb.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminPageControllerTest {

    @InjectMocks private AdminPageController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /admin/dashboard-validasi - redirects to validasi page")
    void dashboardValidasi_redirects() throws Exception {
        mockMvc.perform(get("/admin/dashboard-validasi"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("GET /admin/dashboard-pusat - redirects to pusat page")
    void dashboardPusat_redirects() throws Exception {
        mockMvc.perform(get("/admin/dashboard-pusat"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("GET /admin/reenroll - redirects to reenroll page")
    void adminReenroll_redirects() throws Exception {
        mockMvc.perform(get("/admin/reenroll"))
                .andExpect(status().is3xxRedirection());
    }
}
