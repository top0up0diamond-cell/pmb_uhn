package com.uhn.pmb.service;

import com.uhn.pmb.entity.*;
import com.uhn.pmb.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentRegistrationServiceTest {

    @Mock private StudentRepository studentRepository;
    @Mock private AdmissionFormRepository admissionFormRepository;
    @Mock private VirtualAccountRepository virtualAccountRepository;
    @Mock private SelectionTypeRepository selectionTypeRepository;
    @Mock private ExamRepository examRepository;
    @Mock private ExamResultRepository examResultRepository;
    @Mock private RegistrationPeriodRepository registrationPeriodRepository;
    @Mock private BrivaService brivaService;
    @Mock private EmailService emailService;

    private StudentRegistrationService studentRegistrationService;

    @BeforeEach
    void setUp() {
        studentRegistrationService = new StudentRegistrationService(
                studentRepository, admissionFormRepository, virtualAccountRepository,
                selectionTypeRepository, examRepository, examResultRepository,
                registrationPeriodRepository, brivaService, emailService
        );
    }

    private User buildUser(Long id) {
        return User.builder().id(id).email("student" + id + "@test.com").build();
    }

    private Student buildStudent(Long id) {
        return Student.builder().id(id).fullName("Student " + id)
                .nik("123456789").user(buildUser(id))
                .birthDate(LocalDate.of(2000, 1, 1))
                .build();
    }

    private RegistrationPeriod buildPeriod(Long id) {
        return RegistrationPeriod.builder().id(id).name("Periode " + id).build();
    }

    // ===== createStudentProfile =====

    @Test
    @DisplayName("createStudentProfile - saves and returns student")
    void createStudentProfile_saves() {
        User user = buildUser(1L);
        Student saved = buildStudent(1L);
        when(studentRepository.save(any())).thenReturn(saved);

        Student result = studentRegistrationService.createStudentProfile(user, "Student 1", "123456");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(studentRepository).save(any());
    }

    // ===== registerForAdmission =====

    @Test
    @DisplayName("registerForAdmission - saves form with DRAFT status")
    void registerForAdmission_savesDraftForm() {
        Student student = buildStudent(1L);
        RegistrationPeriod period = buildPeriod(1L);
        SelectionType selectionType = new SelectionType();
        selectionType.setId(5L);
        selectionType.setFormType(SelectionType.FormType.NON_MEDICAL);

        AdmissionForm saved = AdmissionForm.builder()
                .id(1L).student(student).period(period)
                .status(AdmissionForm.FormStatus.DRAFT).build();
        when(admissionFormRepository.save(any())).thenReturn(saved);

        AdmissionForm result = studentRegistrationService.registerForAdmission(
                student, period, selectionType, "Informatika");

        assertThat(result.getStatus()).isEqualTo(AdmissionForm.FormStatus.DRAFT);
        verify(admissionFormRepository).save(any());
    }

    // ===== buyFormAndCreateVA =====

    @Test
    @DisplayName("buyFormAndCreateVA - selectionType not found throws RuntimeException")
    void buyFormAndCreateVA_selectionTypeNotFound_throws() {
        Student student = buildStudent(1L);
        AdmissionForm form = AdmissionForm.builder()
                .id(1L).student(student).selectionTypeId(99L).build();
        when(selectionTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentRegistrationService.buyFormAndCreateVA(form))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SelectionType not found");
    }

    @Test
    @DisplayName("buyFormAndCreateVA - happy path creates VA with generated number")
    void buyFormAndCreateVA_happyPath_createsVA() throws Exception {
        Student student = buildStudent(1L);
        AdmissionForm form = AdmissionForm.builder()
                .id(1L).student(student).selectionTypeId(5L).build();

        SelectionType selectionType = new SelectionType();
        selectionType.setId(5L);
        selectionType.setPrice(new BigDecimal("500000"));
        when(selectionTypeRepository.findById(5L)).thenReturn(Optional.of(selectionType));
        when(brivaService.generateVirtualAccount(any())).thenReturn("12345678901");
        VirtualAccount saved = VirtualAccount.builder().id(1L).vaNumber("12345678901").build();
        when(virtualAccountRepository.save(any())).thenReturn(saved);

        VirtualAccount result = studentRegistrationService.buyFormAndCreateVA(form);

        assertThat(result.getVaNumber()).isEqualTo("12345678901");
        verify(brivaService).generateVirtualAccount(any());
        verify(virtualAccountRepository).save(any());
    }

    // ===== submitAdmissionForm =====

    @Test
    @DisplayName("submitAdmissionForm - sets SUBMITTED status and submittedAt")
    void submitAdmissionForm_setsSubmitted() {
        Student student = buildStudent(1L);
        AdmissionForm form = AdmissionForm.builder()
                .id(1L).student(student).status(AdmissionForm.FormStatus.DRAFT).build();
        when(admissionFormRepository.save(any())).thenReturn(form);

        studentRegistrationService.submitAdmissionForm(form);

        assertThat(form.getStatus()).isEqualTo(AdmissionForm.FormStatus.SUBMITTED);
        assertThat(form.getSubmittedAt()).isNotNull();
        verify(admissionFormRepository).save(form);
    }

    // ===== createExamRecord =====

    @Test
    @DisplayName("createExamRecord - saves exam with PENDING status and unique number")
    void createExamRecord_savesWithPendingStatus() {
        Student student = buildStudent(1L);
        RegistrationPeriod period = buildPeriod(1L);

        when(examRepository.findByExamNumber(any())).thenReturn(Optional.empty());
        Exam saved = Exam.builder().id(1L).student(student)
                .status(Exam.ExamStatus.PENDING).build();
        when(examRepository.save(any())).thenReturn(saved);

        Exam result = studentRegistrationService.createExamRecord(student, period);

        assertThat(result.getStatus()).isEqualTo(Exam.ExamStatus.PENDING);
        verify(examRepository).save(any());
    }

    // ===== markExamCompleted =====

    @Test
    @DisplayName("markExamCompleted - score >= 60 sets PASSED and generates admission number")
    void markExamCompleted_passingScore_setsPassed() {
        Student student = buildStudent(1L);
        Exam exam = Exam.builder().id(1L).student(student)
                .status(Exam.ExamStatus.PENDING).build();
        when(examRepository.save(any())).thenReturn(exam);
        when(examResultRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        studentRegistrationService.markExamCompleted(exam, 75.0);

        verify(examResultRepository).save(argThat(r ->
                r.getStatus() == ExamResult.ResultStatus.PASSED &&
                r.getAdmissionNumber() != null
        ));
    }

    @Test
    @DisplayName("markExamCompleted - score < 60 sets FAILED")
    void markExamCompleted_failingScore_setsFailed() {
        Student student = buildStudent(1L);
        Exam exam = Exam.builder().id(1L).student(student)
                .status(Exam.ExamStatus.PENDING).build();
        when(examRepository.save(any())).thenReturn(exam);
        when(examResultRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        studentRegistrationService.markExamCompleted(exam, 45.0);

        verify(examResultRepository).save(argThat(r ->
                r.getStatus() == ExamResult.ResultStatus.FAILED
        ));
    }

    @Test
    @DisplayName("markExamCompleted - score exactly 60 is PASSED")
    void markExamCompleted_exactlyPassingScore_setsPassed() {
        Student student = buildStudent(1L);
        Exam exam = Exam.builder().id(1L).student(student)
                .status(Exam.ExamStatus.PENDING).build();
        when(examRepository.save(any())).thenReturn(exam);
        when(examResultRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        studentRegistrationService.markExamCompleted(exam, 60.0);

        verify(examResultRepository).save(argThat(r ->
                r.getStatus() == ExamResult.ResultStatus.PASSED
        ));
    }

    // ===== publishExamResults =====

    @Test
    @DisplayName("publishExamResults - no exams does nothing")
    void publishExamResults_noExams_doesNothing() {
        RegistrationPeriod period = buildPeriod(1L);
        when(examRepository.findByPeriod_Id(1L)).thenReturn(List.of());

        studentRegistrationService.publishExamResults(period);

        verify(examResultRepository, never()).save(any());
    }

    @Test
    @DisplayName("publishExamResults - PASSED result sends notification and publishes")
    void publishExamResults_passedResult_sendsNotification() {
        Student student = buildStudent(1L);
        RegistrationPeriod period = buildPeriod(1L);
        Exam exam = Exam.builder().id(1L).student(student).period(period).build();

        ExamResult result = ExamResult.builder()
                .id(1L).exam(exam).student(student)
                .status(ExamResult.ResultStatus.PASSED)
                .admissionNumber("PMB2026001")
                .admissionPassword("2000-01-01")
                .build();

        when(examRepository.findByPeriod_Id(1L)).thenReturn(List.of(exam));
        when(examResultRepository.findByExam_Id(1L)).thenReturn(Optional.of(result));
        when(examResultRepository.save(any())).thenReturn(result);
        doNothing().when(emailService).sendResultNotification(any(), anyBoolean(), any(), any());

        studentRegistrationService.publishExamResults(period);

        verify(examResultRepository).save(any());
        verify(emailService).sendResultNotification(
                eq("student1@test.com"), eq(true), eq("PMB2026001"), eq("2000-01-01"));
    }

    @Test
    @DisplayName("publishExamResults - already PUBLISHED result is skipped")
    void publishExamResults_alreadyPublished_skipped() {
        Student student = buildStudent(1L);
        RegistrationPeriod period = buildPeriod(1L);
        Exam exam = Exam.builder().id(1L).student(student).period(period).build();

        ExamResult result = ExamResult.builder()
                .id(1L).exam(exam).student(student)
                .status(ExamResult.ResultStatus.PUBLISHED).build();

        when(examRepository.findByPeriod_Id(1L)).thenReturn(List.of(exam));
        when(examResultRepository.findByExam_Id(1L)).thenReturn(Optional.of(result));

        studentRegistrationService.publishExamResults(period);

        verify(examResultRepository, never()).save(any());
        verify(emailService, never()).sendResultNotification(any(), anyBoolean(), any(), any());
    }

    @Test
    @DisplayName("publishExamResults - no exam result for exam is skipped")
    void publishExamResults_noResult_skipped() {
        Student student = buildStudent(1L);
        RegistrationPeriod period = buildPeriod(1L);
        Exam exam = Exam.builder().id(1L).student(student).period(period).build();

        when(examRepository.findByPeriod_Id(1L)).thenReturn(List.of(exam));
        when(examResultRepository.findByExam_Id(1L)).thenReturn(Optional.empty());

        studentRegistrationService.publishExamResults(period);

        verify(examResultRepository, never()).save(any());
    }

    // ===== getStudentAdmissionForms =====

    @Test
    @DisplayName("getStudentAdmissionForms - returns forms for student")
    void getStudentAdmissionForms_returnsForms() {
        Student student = buildStudent(1L);
        AdmissionForm form = AdmissionForm.builder().id(1L).student(student).build();
        when(admissionFormRepository.findByStudent_Id(1L)).thenReturn(List.of(form));

        List<AdmissionForm> result = studentRegistrationService.getStudentAdmissionForms(student);

        assertThat(result).hasSize(1);
    }

    // ===== getStudentExam =====

    @Test
    @DisplayName("getStudentExam - found returns exam")
    void getStudentExam_found_returnsExam() {
        Student student = buildStudent(1L);
        Exam exam = Exam.builder().id(1L).student(student).build();
        when(examRepository.findByStudent_Id(1L)).thenReturn(Optional.of(exam));

        Exam result = studentRegistrationService.getStudentExam(student);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("getStudentExam - not found returns null")
    void getStudentExam_notFound_returnsNull() {
        Student student = buildStudent(1L);
        when(examRepository.findByStudent_Id(1L)).thenReturn(Optional.empty());

        Exam result = studentRegistrationService.getStudentExam(student);

        assertThat(result).isNull();
    }

    // ===== getExamResult =====

    @Test
    @DisplayName("getExamResult - found returns result")
    void getExamResult_found_returnsResult() {
        Student student = buildStudent(1L);
        Exam exam = Exam.builder().id(1L).student(student).build();
        ExamResult examResult = ExamResult.builder().id(1L).exam(exam).build();
        when(examResultRepository.findByExam_Id(1L)).thenReturn(Optional.of(examResult));

        ExamResult result = studentRegistrationService.getExamResult(exam);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("getExamResult - not found returns null")
    void getExamResult_notFound_returnsNull() {
        Exam exam = Exam.builder().id(1L).build();
        when(examResultRepository.findByExam_Id(1L)).thenReturn(Optional.empty());

        ExamResult result = studentRegistrationService.getExamResult(exam);

        assertThat(result).isNull();
    }
}