package com.uhn.pmb.task;

import com.uhn.pmb.entity.*;
import com.uhn.pmb.repository.*;
import com.uhn.pmb.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyReminderTaskTest {

    @Mock private FormValidationRepository formValidationRepository;
    @Mock private AdmissionFormRepository admissionFormRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private EmailService emailService;

    private DailyReminderTask dailyReminderTask;

    @BeforeEach
    void setUp() {
        dailyReminderTask = new DailyReminderTask();
        ReflectionTestUtils.setField(dailyReminderTask, "formValidationRepository", formValidationRepository);
        ReflectionTestUtils.setField(dailyReminderTask, "admissionFormRepository", admissionFormRepository);
        ReflectionTestUtils.setField(dailyReminderTask, "studentRepository", studentRepository);
        ReflectionTestUtils.setField(dailyReminderTask, "emailService", emailService);
    }

    // ===== Helpers =====

    private FormValidation buildFormValidation(Long id,
                                                FormValidation.ValidationStatus validationStatus,
                                                FormValidation.PaymentStatus paymentStatus) {
        User user = User.builder().id(1L).email("student" + id + "@test.com").build();
        Student student = Student.builder().id(id).fullName("Student " + id).user(user).build();

        FormValidation fv = new FormValidation();
        fv.setId(id);
        fv.setStudent(student);
        fv.setValidationStatus(validationStatus);
        fv.setPaymentStatus(paymentStatus);
        return fv;
    }

    private Student buildStudent(Long id, String email) {
        User user = User.builder().id(id).email(email).build();
        return Student.builder().id(id).fullName("Student " + id).user(user).build();
    }

    // ===== sendDailyReminders (integration of all 4 sub-tasks) =====

    @Test
    @DisplayName("sendDailyReminders - all empty lists, no email sent")
    void sendDailyReminders_allEmpty_noEmailSent() {
        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.PENDING))
                .thenReturn(Collections.emptyList());
        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.APPROVED))
                .thenReturn(Collections.emptyList());
        when(studentRepository.findAll()).thenReturn(Collections.emptyList());
        when(formValidationRepository.findAll()).thenReturn(Collections.emptyList());

        dailyReminderTask.sendDailyReminders();

        verify(emailService, never()).sendHtmlEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("sendDailyReminders - repository throws, exception swallowed")
    void sendDailyReminders_repositoryThrows_exceptionSwallowed() {
        when(formValidationRepository.findByValidationStatus(any()))
                .thenThrow(new RuntimeException("DB error"));

        // should not propagate
        dailyReminderTask.sendDailyReminders();
    }

    // ===== sendPendingReminders =====

    @Test
    @DisplayName("sendPendingReminders - PAID status sends email")
    void sendPendingReminders_paidStatus_sendsEmail() throws Exception {
        FormValidation paid = buildFormValidation(1L,
                FormValidation.ValidationStatus.PENDING, FormValidation.PaymentStatus.PAID);

        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.PENDING))
                .thenReturn(List.of(paid));
        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.APPROVED))
                .thenReturn(Collections.emptyList());
        when(studentRepository.findAll()).thenReturn(Collections.emptyList());
        when(formValidationRepository.findAll()).thenReturn(Collections.emptyList());
        doNothing().when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        dailyReminderTask.sendDailyReminders();

        verify(emailService, atLeastOnce()).sendHtmlEmail(
                eq("student1@test.com"),
                contains("Verifikasi"),
                anyString()
        );
    }

    @Test
    @DisplayName("sendPendingReminders - VERIFIED status sends email")
    void sendPendingReminders_verifiedStatus_sendsEmail() throws Exception {
        FormValidation verified = buildFormValidation(2L,
                FormValidation.ValidationStatus.PENDING, FormValidation.PaymentStatus.VERIFIED);

        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.PENDING))
                .thenReturn(List.of(verified));
        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.APPROVED))
                .thenReturn(Collections.emptyList());
        when(studentRepository.findAll()).thenReturn(Collections.emptyList());
        when(formValidationRepository.findAll()).thenReturn(Collections.emptyList());
        doNothing().when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        dailyReminderTask.sendDailyReminders();

        verify(emailService, atLeastOnce()).sendHtmlEmail(
                eq("student2@test.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("sendPendingReminders - PENDING payment status skipped (goes to notPaid instead)")
    void sendPendingReminders_pendingPayment_notSentAsPending() throws Exception {
        FormValidation notPaid = buildFormValidation(3L,
                FormValidation.ValidationStatus.PENDING, FormValidation.PaymentStatus.PENDING);

        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.PENDING))
                .thenReturn(List.of(notPaid));
        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.APPROVED))
                .thenReturn(Collections.emptyList());
        when(studentRepository.findAll()).thenReturn(Collections.emptyList());
        when(formValidationRepository.findAll()).thenReturn(Collections.emptyList());
        doNothing().when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        dailyReminderTask.sendDailyReminders();

        // notPaid reminder sent (not pending reminder), subject contains "Pembayaran"
        verify(emailService, atLeastOnce()).sendHtmlEmail(
                eq("student3@test.com"),
                contains("Pembayaran"),
                anyString()
        );
    }

    @Test
    @DisplayName("sendPendingReminders - null payment status goes to notPaid")
    void sendPendingReminders_nullPayment_goesToNotPaid() throws Exception {
        FormValidation nullPayment = buildFormValidation(4L,
                FormValidation.ValidationStatus.PENDING, null);

        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.PENDING))
                .thenReturn(List.of(nullPayment));
        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.APPROVED))
                .thenReturn(Collections.emptyList());
        when(studentRepository.findAll()).thenReturn(Collections.emptyList());
        when(formValidationRepository.findAll()).thenReturn(Collections.emptyList());
        doNothing().when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        dailyReminderTask.sendDailyReminders();

        verify(emailService, atLeastOnce()).sendHtmlEmail(
                eq("student4@test.com"),
                contains("Pembayaran"),
                anyString()
        );
    }

    @Test
    @DisplayName("sendPendingReminders - emailService throws per item, continues to next")
    void sendPendingReminders_emailThrows_continuesNextItem() {
        FormValidation paid1 = buildFormValidation(1L,
                FormValidation.ValidationStatus.PENDING, FormValidation.PaymentStatus.PAID);
        FormValidation paid2 = buildFormValidation(2L,
                FormValidation.ValidationStatus.PENDING, FormValidation.PaymentStatus.PAID);

        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.PENDING))
                .thenReturn(List.of(paid1, paid2));
        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.APPROVED))
                .thenReturn(Collections.emptyList());
        when(studentRepository.findAll()).thenReturn(Collections.emptyList());
        when(formValidationRepository.findAll()).thenReturn(Collections.emptyList());
        doThrow(new RuntimeException("SMTP error"))
                .doNothing()
                .when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        // should not throw
        dailyReminderTask.sendDailyReminders();

        verify(emailService, times(2)).sendHtmlEmail(anyString(), anyString(), anyString());
    }

    // ===== sendApprovedReminders =====

    @Test
    @DisplayName("sendApprovedReminders - APPROVED + PAID sends exam reminder email")
    void sendApprovedReminders_approvedAndPaid_sendsEmail() throws Exception {
        FormValidation approved = buildFormValidation(5L,
                FormValidation.ValidationStatus.APPROVED, FormValidation.PaymentStatus.PAID);

        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.PENDING))
                .thenReturn(Collections.emptyList());
        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.APPROVED))
                .thenReturn(List.of(approved));
        when(studentRepository.findAll()).thenReturn(Collections.emptyList());
        when(formValidationRepository.findAll()).thenReturn(Collections.emptyList());
        doNothing().when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        dailyReminderTask.sendDailyReminders();

        verify(emailService, atLeastOnce()).sendHtmlEmail(
                eq("student5@test.com"),
                contains("Ujian"),
                anyString()
        );
    }

    @Test
    @DisplayName("sendApprovedReminders - APPROVED but null payment skipped")
    void sendApprovedReminders_approvedNullPayment_skipped() throws Exception {
        FormValidation approved = buildFormValidation(6L,
                FormValidation.ValidationStatus.APPROVED, null);

        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.PENDING))
                .thenReturn(Collections.emptyList());
        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.APPROVED))
                .thenReturn(List.of(approved));
        when(studentRepository.findAll()).thenReturn(Collections.emptyList());
        when(formValidationRepository.findAll()).thenReturn(Collections.emptyList());

        dailyReminderTask.sendDailyReminders();

        verify(emailService, never()).sendHtmlEmail(anyString(), anyString(), anyString());
    }

    // ===== sendIncompleteReminders =====

    @Test
    @DisplayName("sendIncompleteReminders - student with no form gets incomplete reminder")
    void sendIncompleteReminders_studentNoForm_sendsReminder() throws Exception {
        Student student = buildStudent(10L, "noform@test.com");

        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.PENDING))
                .thenReturn(Collections.emptyList());
        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.APPROVED))
                .thenReturn(Collections.emptyList());
        when(studentRepository.findAll()).thenReturn(List.of(student));
        when(formValidationRepository.findAll()).thenReturn(Collections.emptyList());
        doNothing().when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        dailyReminderTask.sendDailyReminders();

        verify(emailService, atLeastOnce()).sendHtmlEmail(
                eq("noform@test.com"),
                contains("Pendaftaran"),
                anyString()
        );
    }

    @Test
    @DisplayName("sendIncompleteReminders - student with existing form is skipped")
    void sendIncompleteReminders_studentHasForm_skipped() throws Exception {
        Student student = buildStudent(11L, "hasform@test.com");
        FormValidation fv = buildFormValidation(11L,
                FormValidation.ValidationStatus.PENDING, FormValidation.PaymentStatus.PAID);

        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.PENDING))
                .thenReturn(List.of(fv));
        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.APPROVED))
                .thenReturn(Collections.emptyList());
        when(studentRepository.findAll()).thenReturn(List.of(student));
        when(formValidationRepository.findAll()).thenReturn(List.of(fv));
        doNothing().when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        dailyReminderTask.sendDailyReminders();

        // Pending reminder sent (student11 has PAID), but NOT incomplete reminder
        verify(emailService, times(1)).sendHtmlEmail(
                eq("student11@test.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("sendIncompleteReminders - mixed students, only incomplete ones get reminder")
    void sendIncompleteReminders_mixed_onlyIncompleteGetReminder() throws Exception {
        Student hasForm = buildStudent(20L, "hasform@test.com");
        Student noForm  = buildStudent(21L, "noform@test.com");

        FormValidation fv = buildFormValidation(20L,
                FormValidation.ValidationStatus.PENDING, null);

        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.PENDING))
                .thenReturn(List.of(fv));
        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.APPROVED))
                .thenReturn(Collections.emptyList());
        when(studentRepository.findAll()).thenReturn(List.of(hasForm, noForm));
        when(formValidationRepository.findAll()).thenReturn(List.of(fv));
        doNothing().when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        dailyReminderTask.sendDailyReminders();

        // student20 → notPaid reminder; student21 → incomplete reminder
        verify(emailService, atLeastOnce()).sendHtmlEmail(
                eq("student20@test.com"), contains("Pembayaran"), anyString());
        verify(emailService, atLeastOnce()).sendHtmlEmail(
                eq("noform@test.com"), contains("Pendaftaran"), anyString());
    }
}