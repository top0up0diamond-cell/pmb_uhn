package com.uhn.pmb.controller;

import com.uhn.pmb.service.CamabaRegistrationService;
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
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CamabaControllerTest {

    @Mock
    private CamabaRegistrationService camabaRegistrationService;

    @InjectMocks
    private CamabaRegistrationController camabaRegistrationController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(camabaRegistrationController).build();
    }

    @Test
    @DisplayName("GET /api/camaba/all-gelombang - returns all periods")
    void getAllGelombang_returns200() throws Exception {
        when(camabaRegistrationService.getAllGelombang()).thenReturn(
                List.of(Map.of("id", 1L, "name", "Gelombang 1")));

        mockMvc.perform(get("/api/camaba/all-gelombang"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/all-gelombang - empty returns 200")
    void getAllGelombang_empty_returns200() throws Exception {
        when(camabaRegistrationService.getAllGelombang()).thenReturn(List.of());

        mockMvc.perform(get("/api/camaba/all-gelombang"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/all-formulas - returns formulas")
    void getAllFormulas_returns200() throws Exception {
        when(camabaRegistrationService.getAllFormulas(null)).thenReturn(List.of(Map.of("formula", "test")));

        mockMvc.perform(get("/api/camaba/all-formulas"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/all-formulas - period not found returns 200")
    void getAllFormulas_notFound_returns404() throws Exception {
        when(camabaRegistrationService.getAllFormulas(null)).thenReturn(List.of());

        mockMvc.perform(get("/api/camaba/all-formulas"))
                .andExpect(status().isOk());
    }
}
