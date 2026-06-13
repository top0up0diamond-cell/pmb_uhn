package com.uhn.pmb.service;

import com.uhn.pmb.entity.*;
import com.uhn.pmb.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CamabaExamServiceTest {

    @Mock private ExamRepository examRepository;
    @Mock private ExamResultRepository examResultRepository;
    @Mock private ExamTokenService examTokenService;
    @Mock private ExamTokenRepository examTokenRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private UserRepository userRepository;
    @Mock private FormValidationRepository formValidationRepository;
    @Mock private AdmissionFormRepository admissionFormRepository;
    @Mock private RegistrationStatusRepository registrationStatusRepository;

    @InjectMocks
    private CamabaExamService camabaExamService;

    @Test
    @DisplayName("getExamDetails - user not found throws exception")
    void getExamDetails_userNotFound_throwsException() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaExamService.getExamDetails("none@test.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getExamDetails - student not found throws exception")
    void getExamDetails_studentNotFound_throwsException() {
        User user = User.builder().id(1L).email("u@test.com").build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaExamService.getExamDetails("u@test.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getExamDetails - no exam found returns empty")
    void getExamDetails_noExam_returnsEmpty() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(examRepository.findByStudent_Id(10L)).thenReturn(Optional.empty());

        Optional<com.uhn.pmb.entity.Exam> result = camabaExamService.getExamDetails("u@test.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("submitExam - user not found throws exception")
    void submitExam_userNotFound_throwsException() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaExamService.submitExam("none@test.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("submitExam - valid user submits exam")
    void submitExam_validUser_returnsSuccess() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(examRepository.save(any())).thenReturn(new com.uhn.pmb.entity.Exam());

        var result = camabaExamService.submitExam("u@test.com");

        assertThat(result).containsKey("success");
        assertThat(result.get("success")).isEqualTo(true);
    }

    @Test
    @DisplayName("getExamValidationStatus - no exam returns NOT_STARTED")
    void getExamValidationStatus_noExam_returnsNotStarted() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(examRepository.findByStudent_Id(10L)).thenReturn(Optional.empty());

        Map<String, Object> result = camabaExamService.getExamValidationStatus("u@test.com");

        assertThat(result.get("status")).isEqualTo("NOT_STARTED");
    }

    @Test
    @DisplayName("getExamValidationStatus - exam exists but no result returns PENDING")
    void getExamValidationStatus_noResult_returnsPending() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        Exam exam = new Exam();
        exam.setId(1L);
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(examRepository.findByStudent_Id(10L)).thenReturn(Optional.of(exam));
        when(examResultRepository.findByExam_Id(1L)).thenReturn(Optional.empty());

        Map<String, Object> result = camabaExamService.getExamValidationStatus("u@test.com");

        assertThat(result.get("status")).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("getExamValidationStatus - exam with result returns status")
    void getExamValidationStatus_withResult_returnsStatus() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        Exam exam = new Exam();
        exam.setId(1L);
        ExamResult result = new ExamResult();
        result.setExamValidationStatus(ExamResult.ExamValidationStatus.APPROVED);
        result.setGformScore(85.0);
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(examRepository.findByStudent_Id(10L)).thenReturn(Optional.of(exam));
        when(examResultRepository.findByExam_Id(1L)).thenReturn(Optional.of(result));

        Map<String, Object> resultMap = camabaExamService.getExamValidationStatus("u@test.com");

        assertThat(resultMap.get("status")).isEqualTo("APPROVED");
    }

    @Test
    @DisplayName("getExamToken - no tokens returns success=false")
    void getExamToken_noTokens_returnsSuccessFalse() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(examTokenRepository.findAllByStudentId(10L)).thenReturn(List.of());

        Map<String, Object> result = camabaExamService.getExamToken("u@test.com");

        assertThat(result.get("success")).isEqualTo(false);
    }

    @Test
    @DisplayName("getExamToken - active token returns success=true")
    void getExamToken_activeToken_returnsSuccessTrue() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        ExamToken token = new ExamToken();
        token.setTokenValue("TOKEN-ABC");
        token.setStatus(ExamToken.TokenStatus.ACTIVE);
        token.setExpiresAt(java.time.LocalDateTime.now().plusHours(1));
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(examTokenRepository.findAllByStudentId(10L)).thenReturn(List.of(token));

        Map<String, Object> result = camabaExamService.getExamToken("u@test.com");

        assertThat(result.get("success")).isEqualTo(true);
    }

    @Test
    @DisplayName("markExamAsStarted - user not found throws exception")
    void markExamAsStarted_userNotFound_throwsException() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaExamService.markExamAsStarted("none@test.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("markExamAsStarted - success creates new registration status")
    void markExamAsStarted_success_createsStatus() {
        User user = User.builder().id(1L).email("u@test.com").build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(registrationStatusRepository.findByUserAndStage(eq(user),
                eq(RegistrationStatus.RegistrationStage.PSYCHO_EXAM)))
                .thenReturn(Optional.empty());
        RegistrationStatus saved = RegistrationStatus.builder()
                .status(RegistrationStatus.RegistrationStatus_Enum.MENUNGGU_VERIFIKASI)
                .stage(RegistrationStatus.RegistrationStage.PSYCHO_EXAM)
                .build();
        when(registrationStatusRepository.save(any())).thenReturn(saved);

        Map<String, Object> result = camabaExamService.markExamAsStarted("u@test.com");

        assertThat(result.get("success")).isEqualTo(true);
    }

    @Test
    @DisplayName("markExamAsStarted - existing status updated")
    void markExamAsStarted_existingStatus_updatesStatus() {
        User user = User.builder().id(1L).email("u@test.com").build();
        RegistrationStatus existing = RegistrationStatus.builder()
                .status(RegistrationStatus.RegistrationStatus_Enum.MENUNGGU_VERIFIKASI)
                .stage(RegistrationStatus.RegistrationStage.PSYCHO_EXAM)
                .build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(registrationStatusRepository.findByUserAndStage(eq(user),
                eq(RegistrationStatus.RegistrationStage.PSYCHO_EXAM)))
                .thenReturn(Optional.of(existing));
        when(registrationStatusRepository.save(any())).thenReturn(existing);

        Map<String, Object> result = camabaExamService.markExamAsStarted("u@test.com");

        assertThat(result.get("success")).isEqualTo(true);
    }

    @Test
    @DisplayName("triggerTokenGeneration - no validations throws exception")
    void triggerTokenGeneration_noValidations_throwsException() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(formValidationRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> camabaExamService.triggerTokenGeneration("u@test.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("triggerTokenGeneration - payment not paid throws exception")
    void triggerTokenGeneration_paymentNotPaid_throwsException() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        FormValidation fv = new FormValidation();
        fv.setStudent(student);
        fv.setPaymentStatus(FormValidation.PaymentStatus.PENDING);
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(formValidationRepository.findAll()).thenReturn(List.of(fv));
        when(registrationStatusRepository.findByUserAndStage(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaExamService.triggerTokenGeneration("u@test.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("triggerTokenGeneration - PAID status with active token returns token info")
    void triggerTokenGeneration_paidWithActiveToken_returnsSuccess() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        FormValidation fv = new FormValidation();
        fv.setStudent(student);
        fv.setPaymentStatus(FormValidation.PaymentStatus.PAID);
        ExamToken activeToken = new ExamToken();
        activeToken.setTokenValue("ACTIVE-TOKEN");
        activeToken.setStatus(ExamToken.TokenStatus.ACTIVE);
        activeToken.setExpiresAt(java.time.LocalDateTime.now().plusHours(1));
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(formValidationRepository.findAll()).thenReturn(List.of(fv));
        when(registrationStatusRepository.findByUserAndStage(any(), any())).thenReturn(Optional.empty());
        when(examTokenRepository.findAllByStudentId(10L)).thenReturn(List.of(activeToken));

        Map<String, Object> result = camabaExamService.triggerTokenGeneration("u@test.com");

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("tokenValue")).isEqualTo("ACTIVE-TOKEN");
    }

    @Test
    @DisplayName("triggerTokenGeneration - syncs payment status and generates new token")
    void triggerTokenGeneration_syncsPaymentAndGeneratesToken_returnsSuccess() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        com.uhn.pmb.entity.AdmissionForm admissionForm = new com.uhn.pmb.entity.AdmissionForm();
        admissionForm.setId(5L);
        FormValidation fv = new FormValidation();
        fv.setStudent(student);
        fv.setPaymentStatus(FormValidation.PaymentStatus.PENDING);
        fv.setAdmissionForm(admissionForm);
        RegistrationStatus paymentDone = RegistrationStatus.builder()
                .status(RegistrationStatus.RegistrationStatus_Enum.SELESAI)
                .build();
        ExamToken newToken = new ExamToken();
        newToken.setTokenValue("NEW-TOKEN");
        newToken.setExpiresAt(java.time.LocalDateTime.now().plusHours(2));
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(formValidationRepository.findAll()).thenReturn(List.of(fv));
        when(registrationStatusRepository.findByUserAndStage(eq(user),
                eq(RegistrationStatus.RegistrationStage.PAYMENT_BRIVA)))
                .thenReturn(Optional.of(paymentDone));
        when(formValidationRepository.save(any())).thenReturn(fv);
        when(examTokenRepository.findAllByStudentId(10L)).thenReturn(List.of());
        when(examTokenService.generateToken(10L, 5L, 120)).thenReturn(newToken);

        Map<String, Object> result = camabaExamService.triggerTokenGeneration("u@test.com");

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("tokenValue")).isEqualTo("NEW-TOKEN");
    }

    @Test
    @DisplayName("submitExamResults - score negative throws exception")
    void submitExamResults_scoreNegative_throwsException() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> camabaExamService.submitExamResults("u@test.com", "TOKEN", -1.0, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("0-100");
    }

    @Test
    @DisplayName("submitExamResults - score over 100 throws exception")
    void submitExamResults_scoreOver100_throwsException() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> camabaExamService.submitExamResults("u@test.com", "TOKEN", 101.0, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("0-100");
    }

    @Test
    @DisplayName("submitExamResults - validation not found throws exception")
    void submitExamResults_validationNotFound_throwsException() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(formValidationRepository.findByStudentId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaExamService.submitExamResults("u@test.com", "TOKEN", 80.0, null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("submitExamResults - exam exists, success returns map")
    void submitExamResults_examExists_returnsSuccess() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        FormValidation fv = new FormValidation();
        fv.setExamToken("TOKEN-XYZ");
        Exam exam = new Exam();
        exam.setId(1L);
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(formValidationRepository.findByStudentId(10L)).thenReturn(Optional.of(fv));
        when(examRepository.findByStudent_Id(10L)).thenReturn(Optional.of(exam));
        when(examResultRepository.findByExam_Id(1L)).thenReturn(Optional.empty());
        when(examResultRepository.save(any())).thenAnswer(inv -> {
            ExamResult r = inv.getArgument(0);
            r.setId(99L);
            return r;
        });
        when(examRepository.save(any())).thenReturn(exam);
        when(registrationStatusRepository.findByUserAndStage(eq(user),
                eq(RegistrationStatus.RegistrationStage.PSYCHO_EXAM)))
                .thenReturn(Optional.empty());

        Map<String, Object> result = camabaExamService.submitExamResults(
                "u@test.com", "TOKEN-XYZ", 85.0, null);

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("score")).isEqualTo(85.0);
    }

    @Test
    @DisplayName("submitExamResults - no existing exam, creates new Exam from AdmissionForm")
    void submitExamResults_noExam_createsNewExam_returnsSuccess() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        com.uhn.pmb.entity.RegistrationPeriod period = new com.uhn.pmb.entity.RegistrationPeriod();
        com.uhn.pmb.entity.AdmissionForm admForm = new com.uhn.pmb.entity.AdmissionForm();
        admForm.setId(5L);
        admForm.setPeriod(period);
        FormValidation fv = new FormValidation();
        fv.setExamToken(null);
        Exam savedExam = new Exam();
        savedExam.setId(2L);
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(formValidationRepository.findByStudentId(10L)).thenReturn(Optional.of(fv));
        when(examRepository.findByStudent_Id(10L)).thenReturn(Optional.empty());
        when(admissionFormRepository.findByStudent_Id(10L)).thenReturn(List.of(admForm));
        when(examRepository.save(any())).thenReturn(savedExam);
        when(examResultRepository.findByExam_Id(2L)).thenReturn(Optional.empty());
        when(examResultRepository.save(any())).thenAnswer(inv -> {
            ExamResult r = inv.getArgument(0);
            r.setId(88L);
            return r;
        });
        when(registrationStatusRepository.findByUserAndStage(eq(user),
                eq(RegistrationStatus.RegistrationStage.PSYCHO_EXAM)))
                .thenReturn(Optional.empty());

        Map<String, Object> result = camabaExamService.submitExamResults(
                "u@test.com", "WRONG-TOKEN", 60.0, null);

        assertThat(result.get("success")).isEqualTo(true);
    }
}
