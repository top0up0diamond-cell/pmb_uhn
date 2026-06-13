package com.uhn.pmb.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailTemplateServiceTest {

    @Mock private EmailService emailService;

    private EmailTemplateService emailTemplateService;

    @BeforeEach
    void setUp() {
        emailTemplateService = new EmailTemplateService(emailService);
    }

    // ===== sendApprovalEmail =====

    @Test
    @DisplayName("sendApprovalEmail - calls emailService with correct subject")
    void sendApprovalEmail_callsEmailService() {
        doNothing().when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        emailTemplateService.sendApprovalEmail("student@test.com", "Budi");

        verify(emailService).sendHtmlEmail(
                eq("student@test.com"),
                contains("SELAMAT"),
                contains("Budi")
        );
    }

    @Test
    @DisplayName("sendApprovalEmail - emailService throws, no exception propagated")
    void sendApprovalEmail_emailServiceThrows_noException() {
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        // should not throw
        emailTemplateService.sendApprovalEmail("student@test.com", "Budi");

        verify(emailService).sendHtmlEmail(anyString(), anyString(), anyString());
    }

    // ===== sendRejectionEmail =====

    @Test
    @DisplayName("sendRejectionEmail - includes reason in body")
    void sendRejectionEmail_includesReason() {
        doNothing().when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        emailTemplateService.sendRejectionEmail("student@test.com", "Ani", "Dokumen tidak lengkap");

        verify(emailService).sendHtmlEmail(
                eq("student@test.com"),
                contains("Ditolak"),
                contains("Dokumen tidak lengkap")
        );
    }

    @Test
    @DisplayName("sendRejectionEmail - null reason uses dash fallback")
    void sendRejectionEmail_nullReason_usesDash() {
        doNothing().when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        emailTemplateService.sendRejectionEmail("student@test.com", "Ani", null);

        verify(emailService).sendHtmlEmail(
                eq("student@test.com"),
                anyString(),
                contains("-")
        );
    }

    @Test
    @DisplayName("sendRejectionEmail - emailService throws, no exception propagated")
    void sendRejectionEmail_emailServiceThrows_noException() {
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        emailTemplateService.sendRejectionEmail("student@test.com", "Ani", "alasan");

        verify(emailService).sendHtmlEmail(anyString(), anyString(), anyString());
    }

    // ===== sendRevisionNeededEmail =====

    @Test
    @DisplayName("sendRevisionNeededEmail - includes revision number and reason")
    void sendRevisionNeededEmail_includesRevisionAndReason() {
        doNothing().when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        emailTemplateService.sendRevisionNeededEmail("student@test.com", "Citra", "Foto buram", 2);

        verify(emailService).sendHtmlEmail(
                eq("student@test.com"),
                contains("Revisi ke-2"),
                contains("Foto buram")
        );
    }

    @Test
    @DisplayName("sendRevisionNeededEmail - null reason uses dash fallback")
    void sendRevisionNeededEmail_nullReason_usesDash() {
        doNothing().when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        emailTemplateService.sendRevisionNeededEmail("student@test.com", "Citra", null, 1);

        verify(emailService).sendHtmlEmail(
                eq("student@test.com"),
                anyString(),
                contains("-")
        );
    }

    @Test
    @DisplayName("sendRevisionNeededEmail - emailService throws, no exception propagated")
    void sendRevisionNeededEmail_emailServiceThrows_noException() {
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        emailTemplateService.sendRevisionNeededEmail("student@test.com", "Citra", "alasan", 1);

        verify(emailService).sendHtmlEmail(anyString(), anyString(), anyString());
    }

    // ===== sendExamApprovalEmail =====

    @Test
    @DisplayName("sendExamApprovalEmail - calls emailService with student name in body")
    void sendExamApprovalEmail_callsEmailService() {
        doNothing().when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        emailTemplateService.sendExamApprovalEmail("student@test.com", "Dedi");

        verify(emailService).sendHtmlEmail(
                eq("student@test.com"),
                contains("Diterima"),
                contains("Dedi")
        );
    }

    @Test
    @DisplayName("sendExamApprovalEmail - emailService throws, no exception propagated")
    void sendExamApprovalEmail_emailServiceThrows_noException() {
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        emailTemplateService.sendExamApprovalEmail("student@test.com", "Dedi");

        verify(emailService).sendHtmlEmail(anyString(), anyString(), anyString());
    }

    // ===== sendExamRejectionEmail =====

    @Test
    @DisplayName("sendExamRejectionEmail - includes reason in body")
    void sendExamRejectionEmail_includesReason() {
        doNothing().when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        emailTemplateService.sendExamRejectionEmail("student@test.com", "Eko", "Nilai kurang");

        verify(emailService).sendHtmlEmail(
                eq("student@test.com"),
                contains("Ditolak"),
                contains("Nilai kurang")
        );
    }

    @Test
    @DisplayName("sendExamRejectionEmail - null reason uses default message")
    void sendExamRejectionEmail_nullReason_usesDefault() {
        doNothing().when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        emailTemplateService.sendExamRejectionEmail("student@test.com", "Eko", null);

        verify(emailService).sendHtmlEmail(
                eq("student@test.com"),
                anyString(),
                contains("Tidak memenuhi kriteria")
        );
    }

    @Test
    @DisplayName("sendExamRejectionEmail - empty reason uses default message")
    void sendExamRejectionEmail_emptyReason_usesDefault() {
        doNothing().when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        emailTemplateService.sendExamRejectionEmail("student@test.com", "Eko", "");

        verify(emailService).sendHtmlEmail(
                eq("student@test.com"),
                anyString(),
                contains("Tidak memenuhi kriteria")
        );
    }

    @Test
    @DisplayName("sendExamRejectionEmail - emailService throws, no exception propagated")
    void sendExamRejectionEmail_emailServiceThrows_noException() {
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        emailTemplateService.sendExamRejectionEmail("student@test.com", "Eko", "alasan");

        verify(emailService).sendHtmlEmail(anyString(), anyString(), anyString());
    }
}