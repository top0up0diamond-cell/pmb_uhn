package com.uhn.pmb.service;

import com.uhn.pmb.entity.VirtualAccount;
import com.uhn.pmb.repository.VirtualAccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrivaServiceTest {

    @Mock
    private VirtualAccountRepository virtualAccountRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private BrivaService brivaService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(brivaService, "brivaApiKey", "testkey");
        ReflectionTestUtils.setField(brivaService, "brivaApiSecret", "testsecret");
        ReflectionTestUtils.setField(brivaService, "brivaApiUrl", "http://test.briva.api");
        ReflectionTestUtils.setField(brivaService, "brivaClientId", "testclient");
    }

    @Test
    @DisplayName("generateVirtualAccount - creates and saves VA")
    void generateVirtualAccount_validVA_returnsVaNumber() throws Exception {
        VirtualAccount va = new VirtualAccount();
        va.setAmount(BigDecimal.valueOf(500000));

        when(virtualAccountRepository.save(any(VirtualAccount.class))).thenReturn(va);

        String result = brivaService.generateVirtualAccount(va);

        assertThat(result).isNotNull().isNotEmpty();
        assertThat(va.getStatus()).isEqualTo(VirtualAccount.VAStatus.ACTIVE);
        verify(virtualAccountRepository).save(va);
    }

    @Test
    @DisplayName("verifyPayment - PAID status returns true")
    void verifyPayment_paidStatus_returnsTrue() {
        VirtualAccount va = new VirtualAccount();
        va.setStatus(VirtualAccount.VAStatus.PAID);
        when(virtualAccountRepository.findByVaNumber("VA001")).thenReturn(Optional.of(va));

        boolean result = brivaService.verifyPayment("VA001");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("verifyPayment - ACTIVE status returns false")
    void verifyPayment_activeStatus_returnsFalse() {
        VirtualAccount va = new VirtualAccount();
        va.setStatus(VirtualAccount.VAStatus.ACTIVE);
        when(virtualAccountRepository.findByVaNumber("VA002")).thenReturn(Optional.of(va));

        boolean result = brivaService.verifyPayment("VA002");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("verifyPayment - VA not found returns false")
    void verifyPayment_notFound_returnsFalse() {
        when(virtualAccountRepository.findByVaNumber("VA999")).thenReturn(Optional.empty());

        boolean result = brivaService.verifyPayment("VA999");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("updatePaymentStatus - matching amount updates to PAID")
    void updatePaymentStatus_matchingAmount_updatesPaid() {
        VirtualAccount va = new VirtualAccount();
        va.setAmount(BigDecimal.valueOf(500000));
        va.setStatus(VirtualAccount.VAStatus.ACTIVE);
        when(virtualAccountRepository.findByVaNumber("VA001")).thenReturn(Optional.of(va));
        when(virtualAccountRepository.save(any())).thenReturn(va);

        brivaService.updatePaymentStatus("VA001", "TXN123", BigDecimal.valueOf(500000));

        assertThat(va.getStatus()).isEqualTo(VirtualAccount.VAStatus.PAID);
        verify(virtualAccountRepository).save(va);
    }

    @Test
    @DisplayName("updatePaymentStatus - mismatched amount does not update")
    void updatePaymentStatus_mismatchedAmount_doesNotUpdate() {
        VirtualAccount va = new VirtualAccount();
        va.setAmount(BigDecimal.valueOf(500000));
        va.setStatus(VirtualAccount.VAStatus.ACTIVE);
        when(virtualAccountRepository.findByVaNumber("VA001")).thenReturn(Optional.of(va));

        brivaService.updatePaymentStatus("VA001", "TXN123", BigDecimal.valueOf(300000));

        assertThat(va.getStatus()).isEqualTo(VirtualAccount.VAStatus.ACTIVE);
        verify(virtualAccountRepository, never()).save(va);
    }

    @Test
    @DisplayName("cancelVirtualAccount - active VA gets cancelled")
    void cancelVirtualAccount_activeVA_getCancelled() {
        VirtualAccount va = new VirtualAccount();
        va.setStatus(VirtualAccount.VAStatus.ACTIVE);
        when(virtualAccountRepository.findByVaNumber("VA001")).thenReturn(Optional.of(va));
        when(virtualAccountRepository.save(any())).thenReturn(va);

        brivaService.cancelVirtualAccount("VA001");

        assertThat(va.getStatus()).isEqualTo(VirtualAccount.VAStatus.CANCELLED);
        verify(virtualAccountRepository).save(va);
    }

    @Test
    @DisplayName("cancelVirtualAccount - paid VA is not cancelled")
    void cancelVirtualAccount_paidVA_notCancelled() {
        VirtualAccount va = new VirtualAccount();
        va.setStatus(VirtualAccount.VAStatus.PAID);
        when(virtualAccountRepository.findByVaNumber("VA002")).thenReturn(Optional.of(va));

        brivaService.cancelVirtualAccount("VA002");

        assertThat(va.getStatus()).isEqualTo(VirtualAccount.VAStatus.PAID);
        verify(virtualAccountRepository, never()).save(any());
    }

    @Test
    @DisplayName("getVADetails - existing VA returns details map")
    void getVADetails_existingVA_returnsDetailsMap() {
        VirtualAccount va = new VirtualAccount();
        va.setVaNumber("VA001");
        va.setAmount(BigDecimal.valueOf(500000));
        va.setStatus(VirtualAccount.VAStatus.ACTIVE);
        when(virtualAccountRepository.findByVaNumber("VA001")).thenReturn(Optional.of(va));

        java.util.Map<String, Object> details = brivaService.getVADetails("VA001");

        assertThat(details).containsKey("vaNumber");
        assertThat(details).containsKey("amount");
        assertThat(details).containsKey("status");
    }

    @Test
    @DisplayName("getVADetails - VA not found throws exception")
    void getVADetails_notFound_throwsException() {
        when(virtualAccountRepository.findByVaNumber("VA999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> brivaService.getVADetails("VA999"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("inquiryVA - existing VA returns response map")
    void inquiryVA_existingVA_returnsResponseMap() throws Exception {
        VirtualAccount va = new VirtualAccount();
        va.setVaNumber("VA001");
        va.setAmount(BigDecimal.valueOf(500000));
        va.setStatus(VirtualAccount.VAStatus.ACTIVE);
        va.setExpiredAt(java.time.LocalDateTime.now().plusDays(1));
        when(virtualAccountRepository.findByVaNumber("VA001")).thenReturn(Optional.of(va));

        java.util.Map<String, Object> response = brivaService.inquiryVA("VA001");

        assertThat(response).containsKey("success");
        assertThat(response.get("success")).isEqualTo(true);
    }

    @Test
    @DisplayName("inquiryVA - VA not found throws exception")
    void inquiryVA_notFound_throwsException() {
        when(virtualAccountRepository.findByVaNumber("VA999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> brivaService.inquiryVA("VA999"))
                .isInstanceOf(Exception.class);
    }
}
