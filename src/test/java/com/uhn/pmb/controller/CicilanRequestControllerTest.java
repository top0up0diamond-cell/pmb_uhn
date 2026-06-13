package com.uhn.pmb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uhn.pmb.dto.CicilanRequestDTO;
import com.uhn.pmb.request.CicilanRequestSubmitRequest;
import com.uhn.pmb.service.CicilanService;
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

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CicilanRequestControllerTest {

    @Mock
    private CicilanService cicilanService;

    @InjectMocks
    private CicilanRequestController cicilanRequestController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cicilanRequestController).build();
    }

    @Test
    @DisplayName("GET /api/cicilan/my-status - returns cicilan for user")
    void getMyCicilan_returns200() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("u@test.com");
        when(auth.getPrincipal()).thenReturn("u@test.com");
        when(cicilanService.getMyCicilan("u@test.com")).thenReturn(Optional.of(
                com.uhn.pmb.dto.CicilanRequestDTO.builder().id(1L).build()));

        mockMvc.perform(get("/api/cicilan/my-status").principal(auth))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/cicilan/my-status - no cicilan returns 404")
    void getMyCicilan_empty_returns200() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("u@test.com");
        when(auth.getPrincipal()).thenReturn("u@test.com");
        when(cicilanService.getMyCicilan("u@test.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/cicilan/my-status").principal(auth))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/cicilan/my-status - user not found returns 500")
    void getMyCicilan_userNotFound_returns404() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("none@test.com");
        when(auth.getPrincipal()).thenReturn("none@test.com");
        when(cicilanService.getMyCicilan("none@test.com")).thenThrow(
                new RuntimeException("User not found"));

        mockMvc.perform(get("/api/cicilan/my-status").principal(auth))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/cicilan/status/{admissionFormId} - found returns 200")
    void getCicilanByAdmissionFormId_found_returns200() throws Exception {
        when(cicilanService.getCicilanByAdmissionFormId(1L))
                .thenReturn(Optional.of(CicilanRequestDTO.builder().id(1L).build()));
        mockMvc.perform(get("/api/cicilan/status/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/cicilan/status/{admissionFormId} - not found returns 404")
    void getCicilanByAdmissionFormId_notFound_returns404() throws Exception {
        when(cicilanService.getCicilanByAdmissionFormId(99L))
                .thenReturn(Optional.empty());
        mockMvc.perform(get("/api/cicilan/status/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/cicilan/status/{admissionFormId} - exception returns 400")
    void getCicilanByAdmissionFormId_exception_returns400() throws Exception {
        when(cicilanService.getCicilanByAdmissionFormId(99L))
                .thenThrow(new RuntimeException("DB error"));
        mockMvc.perform(get("/api/cicilan/status/99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/cicilan/submit - success returns 200")
    void submitCicilanRequest_success_returns200() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("u@test.com");
        when(auth.getPrincipal()).thenReturn("u@test.com");
        when(cicilanService.submitCicilanRequest(anyString(), any()))
                .thenReturn(CicilanRequestDTO.builder().id(1L).build());
        CicilanRequestSubmitRequest req = new CicilanRequestSubmitRequest();
        mockMvc.perform(post("/api/cicilan/submit").principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/cicilan/submit - RuntimeException returns 400")
    void submitCicilanRequest_exception_returns400() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("u@test.com");
        when(auth.getPrincipal()).thenReturn("u@test.com");
        when(cicilanService.submitCicilanRequest(anyString(), any()))
                .thenThrow(new RuntimeException("Validation error"));
        CicilanRequestSubmitRequest req = new CicilanRequestSubmitRequest();
        mockMvc.perform(post("/api/cicilan/submit").principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/cicilan/submit - null auth returns 400")
    void submitCicilanRequest_nullAuth_returns400() throws Exception {
        mockMvc.perform(post("/api/cicilan/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/cicilan/{id}/payment-submitted - success returns 200")
    void markPaymentSubmitted_success_returns200() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("u@test.com");
        when(auth.getPrincipal()).thenReturn("u@test.com");
        when(cicilanService.markPaymentSubmitted(1L, "u@test.com"))
                .thenReturn(CicilanRequestDTO.builder().id(1L).build());
        mockMvc.perform(put("/api/cicilan/1/payment-submitted").principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/cicilan/{id}/payment-submitted - RuntimeException returns 400")
    void markPaymentSubmitted_exception_returns400() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("u@test.com");
        when(auth.getPrincipal()).thenReturn("u@test.com");
        when(cicilanService.markPaymentSubmitted(99L, "u@test.com"))
                .thenThrow(new RuntimeException("Not found"));
        mockMvc.perform(put("/api/cicilan/99/payment-submitted").principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
