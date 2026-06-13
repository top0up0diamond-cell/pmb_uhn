package com.uhn.pmb.controller;

import com.uhn.pmb.entity.UniversityBankAccount;
import com.uhn.pmb.repository.UniversityBankAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private UniversityBankAccountRepository universityBankAccountRepository;

    @InjectMocks
    private PaymentController paymentController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
    }

    @Test
    @DisplayName("GET /api/payment/bank-accounts - returns 200 with list")
    void getActiveBankAccounts_returns200WithList() throws Exception {
        UniversityBankAccount account = new UniversityBankAccount();
        account.setId(1L);
        account.setBankName("BRI");
        account.setAccountNumber("1234567890");
        account.setAccountHolder("UHN");
        account.setIsActive(true);

        when(universityBankAccountRepository.findByIsActiveTrueOrderByBankName())
                .thenReturn(List.of(account));

        mockMvc.perform(get("/api/payment/bank-accounts"))
                .andExpect(status().isOk());

        verify(universityBankAccountRepository).findByIsActiveTrueOrderByBankName();
    }

    @Test
    @DisplayName("GET /api/payment/bank-accounts - empty list returns 200")
    void getActiveBankAccounts_emptyList_returns200() throws Exception {
        when(universityBankAccountRepository.findByIsActiveTrueOrderByBankName())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/payment/bank-accounts"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/payment/bank-accounts - exception returns 500")
    void getActiveBankAccounts_exception_returns500() throws Exception {
        when(universityBankAccountRepository.findByIsActiveTrueOrderByBankName())
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/api/payment/bank-accounts"))
                .andExpect(status().isInternalServerError());
    }
}
