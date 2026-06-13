package com.uhn.pmb.controller;

import com.uhn.pmb.repository.UserRepository;
import com.uhn.pmb.service.CamabaPaymentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CamabaPaymentControllerTest {

    @Mock
    private CamabaPaymentService camabaPaymentService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CamabaPaymentController camabaPaymentController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(camabaPaymentController).build();
        var auth = new UsernamePasswordAuthenticationToken("u@test.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /api/camaba/check-payment-status - returns payment status 200")
    void checkPaymentStatus_returns200() throws Exception {
        when(camabaPaymentService.checkPaymentStatus(anyString(), anyString()))
                .thenReturn(Map.of("status", "ACTIVE", "vaNumber", "VA001"));

        mockMvc.perform(get("/api/camaba/check-payment-status")
                        .param("vaNumber", "VA001"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/check-payment-status - VA not found returns 400")
    void checkPaymentStatus_vaNotFound_returns200WithMessage() throws Exception {
        when(camabaPaymentService.checkPaymentStatus(anyString(), anyString()))
                .thenThrow(new RuntimeException("VA not found"));

        mockMvc.perform(get("/api/camaba/check-payment-status")
                        .param("vaNumber", "VA999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/camaba/check-payment-status - no vaNumber param returns 400")
    void checkPaymentStatus_noParam_returns400() throws Exception {
        mockMvc.perform(get("/api/camaba/check-payment-status"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/camaba/verify-payment - success returns 200")
    void verifyPayment_success_returns200() throws Exception {
        when(camabaPaymentService.verifyPayment(anyString(), anyString()))
                .thenReturn(Map.of("success", true, "vaNumber", "VA001"));
        mockMvc.perform(post("/api/camaba/verify-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vaNumber\":\"VA001\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/camaba/verify-payment - exception returns 400")
    void verifyPayment_exception_returns400() throws Exception {
        when(camabaPaymentService.verifyPayment(anyString(), anyString()))
                .thenThrow(new RuntimeException("VA not found"));
        mockMvc.perform(post("/api/camaba/verify-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vaNumber\":\"INVALID\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/camaba/buy-form/{formId} - success returns 200")
    void buyForm_success_returns200() throws Exception {
        when(camabaPaymentService.buyForm(1L))
                .thenReturn(Map.of("success", true, "vaNumber", "VA001"));
        mockMvc.perform(post("/api/camaba/buy-form/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/camaba/buy-form/{formId} - exception returns 400")
    void buyForm_exception_returns400() throws Exception {
        when(camabaPaymentService.buyForm(99L))
                .thenThrow(new RuntimeException("Form already in payment state"));
        mockMvc.perform(post("/api/camaba/buy-form/99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/camaba/create-virtual-account - success returns 200")
    void createVirtualAccount_success_returns200() throws Exception {
        when(camabaPaymentService.createVirtualAccount(anyString(), any(), any(), any()))
                .thenReturn(Map.of("success", true, "data", Map.of("vaNumber", "VA001")));
        mockMvc.perform(post("/api/camaba/create-virtual-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"selectionTypeId\":1,\"periodId\":1,\"amount\":500000}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/camaba/create-virtual-account - exception returns 400")
    void createVirtualAccount_exception_returns400() throws Exception {
        when(camabaPaymentService.createVirtualAccount(anyString(), any(), any(), any()))
                .thenThrow(new RuntimeException("Student not found"));
        mockMvc.perform(post("/api/camaba/create-virtual-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"selectionTypeId\":99,\"periodId\":99,\"amount\":500000}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/camaba/simulate-payment - success returns 200")
    void simulatePayment_success_returns200() throws Exception {
        when(camabaPaymentService.simulatePayment("VA001"))
                .thenReturn(Map.of("success", true, "status", "PAID"));
        mockMvc.perform(post("/api/camaba/simulate-payment")
                        .param("vaNumber", "VA001"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/camaba/simulate-payment - exception returns 400")
    void simulatePayment_exception_returns400() throws Exception {
        when(camabaPaymentService.simulatePayment("INVALID"))
                .thenThrow(new RuntimeException("VA not found"));
        mockMvc.perform(post("/api/camaba/simulate-payment")
                        .param("vaNumber", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/camaba/payment/cicilan-confirm - success returns 200")
    void confirmCicilanPayment_success_returns200() throws Exception {
        when(camabaPaymentService.confirmCicilanPayment(anyString()))
                .thenReturn(Map.of("success", true, "daftarUlangUnlocked", true));
        mockMvc.perform(post("/api/camaba/payment/cicilan-confirm"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/camaba/payment/cicilan-confirm - exception returns 400")
    void confirmCicilanPayment_exception_returns400() throws Exception {
        when(camabaPaymentService.confirmCicilanPayment(anyString()))
                .thenThrow(new RuntimeException("User not found"));
        mockMvc.perform(post("/api/camaba/payment/cicilan-confirm"))
                .andExpect(status().isBadRequest());
    }
}
