package com.uhn.pmb.service;

import com.uhn.pmb.entity.Notification;
import com.uhn.pmb.entity.User;
import com.uhn.pmb.repository.NotificationRepository;
import com.uhn.pmb.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private EmailService emailService;

    @Test
    @DisplayName("sendSimpleEmail - sends without exception")
    void sendSimpleEmail_sendsWithoutException() {
        assertThatCode(() -> emailService.sendSimpleEmail("user@test.com", "Test", "Body"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sendHtmlEmail - sends without exception")
    void sendHtmlEmail_sendsWithoutException() {
        assertThatCode(() -> emailService.sendHtmlEmail("user@test.com", "Test", "<p>Body</p>"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sendRegistrationConfirmation - sends successfully")
    void sendRegistrationConfirmation_sendSuccessfully() {
        assertThatCode(() -> emailService.sendRegistrationConfirmation("student@test.com", "John"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sendVirtualAccountInfo - sends successfully")
    void sendVirtualAccountInfo_sendSuccessfully() {
        assertThatCode(() -> emailService.sendVirtualAccountInfo("student@test.com", "123456", "5000000", "2024-12-31"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sendExamNotification - sends successfully")
    void sendExamNotification_sendSuccessfully() {
        assertThatCode(() -> emailService.sendExamNotification("student@test.com", "EXM-001", "2024-12-25", "https://gform"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sendResultNotification - passed sends successfully")
    void sendResultNotification_passedSendSuccessfully() {
        assertThatCode(() -> emailService.sendResultNotification("student@test.com", true, "ADM-001", "pass"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sendResultNotification - failed sends successfully")
    void sendResultNotification_failedSendSuccessfully() {
        assertThatCode(() -> emailService.sendResultNotification("student@test.com", false, "ADM-002", "fail"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sendFormApprovedEmail - sends successfully")
    void sendFormApprovedEmail_sendSuccessfully() {
        assertThatCode(() -> emailService.sendFormApprovedEmail("student@test.com", "John", "token", "2024-12-31"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sendFormRejectedEmail - sends successfully")
    void sendFormRejectedEmail_sendSuccessfully() {
        assertThatCode(() -> emailService.sendFormRejectedEmail("student@test.com", "John", "Invalid"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sendExamCompletedEmail - passed sends successfully")
    void sendExamCompletedEmail_passedSendSuccessfully() {
        assertThatCode(() -> emailService.sendExamCompletedEmail("student@test.com", "John", 85, true))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sendExamCompletedEmail - failed sends successfully")
    void sendExamCompletedEmail_failedSendSuccessfully() {
        assertThatCode(() -> emailService.sendExamCompletedEmail("student@test.com", "John", 45, false))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("recordNotification - creates notification")
    void recordNotification_createsNotification() {
        User user = User.builder().id(1L).build();
        
        assertThatCode(() -> emailService.recordNotification(user, "Subject", "Message", Notification.NotificationType.REGISTRATION_CONFIRMATION))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("recordNotification - saves to notificationRepository")
    void recordNotification_savesToRepository() {
        User user = User.builder().id(1L).email("user@test.com").build();
        when(notificationRepository.save(any())).thenReturn(new Notification());

        emailService.recordNotification(user, "Test Subject", "Test Message", Notification.NotificationType.EXAM_READY);

        verify(notificationRepository).save(argThat(notification ->
                notification.getUser().equals(user) &&
                notification.getSubject().equals("Test Subject") &&
                notification.getMessage().equals("Test Message") &&
                notification.getType() == Notification.NotificationType.EXAM_READY
        ));
    }

    @Test
    @DisplayName("recordNotification - with different notification types saves correct type")
    void recordNotification_withDifferentTypes_savesCorrectType() {
        User user = User.builder().id(1L).build();
        when(notificationRepository.save(any())).thenReturn(new Notification());

        emailService.recordNotification(user, "Subj", "Msg", Notification.NotificationType.RESULT_PUBLISHED);

        verify(notificationRepository).save(argThat(n -> n.getType() == Notification.NotificationType.RESULT_PUBLISHED));
    }

    @Test
    @DisplayName("sendSimpleEmail - with special characters in subject")
    void sendSimpleEmail_withSpecialCharacters() {
        assertThatCode(() -> emailService.sendSimpleEmail("user@test.com", "Test & Émail <Subject>", "Body"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sendHtmlEmail - with complex HTML content")
    void sendHtmlEmail_withComplexContent() {
        String complexHtml = "<html><head><style>body{font-family:Arial}</style></head><body><div class='container'><p>Test</p></div></body></html>";
        assertThatCode(() -> emailService.sendHtmlEmail("user@test.com", "Complex", complexHtml))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("recordNotification - saves all notification types")
    void recordNotification_savesAllTypes() {
        User user = User.builder().id(1L).build();
        when(notificationRepository.save(any())).thenReturn(new Notification());

        for (Notification.NotificationType type : Notification.NotificationType.values()) {
            emailService.recordNotification(user, "Subject", "Message", type);
        }

        verify(notificationRepository, times(Notification.NotificationType.values().length)).save(any());
    }

    @Test
    @DisplayName("sendVirtualAccountInfo - with large amount")
    void sendVirtualAccountInfo_withLargeAmount() {
        assertThatCode(() -> emailService.sendVirtualAccountInfo("student@test.com", "VA-123", "999999999", "2025-12-31"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sendExamCompletedEmail - with score 0 (failed)")
    void sendExamCompletedEmail_withScoreZero() {
        assertThatCode(() -> emailService.sendExamCompletedEmail("student@test.com", "John", 0, false))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sendExamCompletedEmail - with perfect score (100)")
    void sendExamCompletedEmail_withPerfectScore() {
        assertThatCode(() -> emailService.sendExamCompletedEmail("student@test.com", "Jane", 100, true))
                .doesNotThrowAnyException();
    }

    // ===== Brevo API path tests (using injected RestTemplate) =====

    @Test
    @DisplayName("sendSimpleEmail - with valid apiKey invokes Brevo and succeeds")
    void sendSimpleEmail_withValidApiKey_callsBrevoApi() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(mockRestTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("OK"));
        ReflectionTestUtils.setField(emailService, "restTemplate", mockRestTemplate);
        ReflectionTestUtils.setField(emailService, "brevoApiKey", "test-api-key-123");
        ReflectionTestUtils.setField(emailService, "fromEmail", "no-reply@test.com");
        ReflectionTestUtils.setField(emailService, "senderName", "Test Sender");

        assertThatCode(() -> emailService.sendSimpleEmail("user@test.com", "Subject", "Body"))
                .doesNotThrowAnyException();

        verify(mockRestTemplate).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    @DisplayName("sendHtmlEmail - with valid apiKey and RestTemplate exception logs error")
    void sendHtmlEmail_withValidApiKey_restTemplateThrows_logsError() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(mockRestTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("Network timeout"));
        ReflectionTestUtils.setField(emailService, "restTemplate", mockRestTemplate);
        ReflectionTestUtils.setField(emailService, "brevoApiKey", "test-api-key-456");
        ReflectionTestUtils.setField(emailService, "fromEmail", "no-reply@test.com");
        ReflectionTestUtils.setField(emailService, "senderName", "Test Sender");

        // Exception is caught internally; should NOT propagate
        assertThatCode(() -> emailService.sendHtmlEmail("user@test.com", "Subject", "<p>Content</p>"))
                .doesNotThrowAnyException();

        verify(mockRestTemplate).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    @DisplayName("sendRegistrationConfirmation - with valid apiKey invokes Brevo")
    void sendRegistrationConfirmation_withValidApiKey_invokesBrevo() {
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(mockRestTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("Accepted"));
        ReflectionTestUtils.setField(emailService, "restTemplate", mockRestTemplate);
        ReflectionTestUtils.setField(emailService, "brevoApiKey", "real-key-789");
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@uhn.ac.id");
        ReflectionTestUtils.setField(emailService, "senderName", "PMB UHN");

        assertThatCode(() -> emailService.sendRegistrationConfirmation("student@test.com", "Alice"))
                .doesNotThrowAnyException();

        verify(mockRestTemplate).postForEntity(anyString(), any(), eq(String.class));
    }
}
