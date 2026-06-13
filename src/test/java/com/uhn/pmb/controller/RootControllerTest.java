package com.uhn.pmb.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RootControllerTest {

    @InjectMocks
    private RootController rootController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(rootController).build();
    }

    @Test
    @DisplayName("GET / - redirects to index.html")
    void getRoot_redirectsToIndex() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index.html"));
    }

    @Test
    @DisplayName("GET /dashboard-camaba - redirects to dashboard-camaba.html")
    void getDashboardCamaba_redirectsToDashboardCamabaHtml() throws Exception {
        mockMvc.perform(get("/dashboard-camaba"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard-camaba.html"));
    }
}