package com.uhn.pmb.service;

import com.uhn.pmb.entity.*;
import com.uhn.pmb.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CamabaReenrollmentServiceTest {

    @Mock private ReEnrollmentRepository reenrollmentRepository;
    @Mock private ReEnrollmentDocumentRepository reenrollmentDocumentRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private UserRepository userRepository;
    @Mock private RegistrationStatusRepository registrationStatusRepository;
    @Mock private RegistrationStatusService registrationStatusService;
    @Mock private ExamRepository examRepository;
    @Mock private ExamResultRepository examResultRepository;
    @Mock private FileStorageService fileStorageService;

    @InjectMocks
    private CamabaReenrollmentService camabaReenrollmentService;

    // ===== submitReenrollment =====

    @Test
    @DisplayName("submitReenrollment - user not found throws RuntimeException")
    void submitReenrollment_userNotFound_throws() {
        when(userRepository.findByEmail("bad@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaReenrollmentService.submitReenrollment(
                "bad@test.com", "0812", "p@test.com", "Addr", "PAddr", "", false, null, null,
                Collections.emptyMap()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("submitReenrollment - student not found throws RuntimeException")
    void submitReenrollment_studentNotFound_throws() {
        User user = User.builder().id(1L).email("u@test.com").build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaReenrollmentService.submitReenrollment(
                "u@test.com", "0812", "p@test.com", "Addr", "PAddr", "", false, null, null,
                Collections.emptyMap()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Student not found");
    }

    @Test
    @DisplayName("submitReenrollment - already submitted throws RuntimeException")
    void submitReenrollment_alreadySubmitted_throws() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        ReEnrollment existing = ReEnrollment.builder()
                .id(1L)
                .student(student)
                .status(ReEnrollment.ReEnrollmentStatus.SUBMITTED)
                .build();

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(examRepository.findByStudent_Id(10L)).thenReturn(Optional.empty());
        when(reenrollmentRepository.findAll()).thenReturn(List.of(existing));

        assertThatThrownBy(() -> camabaReenrollmentService.submitReenrollment(
                "u@test.com", "0812", "p@test.com", "Addr", "PAddr", "", false, null, null,
                Collections.emptyMap()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("sudah melakukan daftar ulang");
    }

    @Test
    @DisplayName("submitReenrollment - success creates new enrollment")
    void submitReenrollment_success_createsEnrollment() throws Exception {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).fullName("John Doe").user(user).build();
        ReEnrollment saved = ReEnrollment.builder()
                .id(5L)
                .student(student)
                .status(ReEnrollment.ReEnrollmentStatus.SUBMITTED)
                .documents(new ArrayList<>())
                .build();

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(examRepository.findByStudent_Id(10L)).thenReturn(Optional.empty());
        when(reenrollmentRepository.findAll()).thenReturn(Collections.emptyList());
        when(reenrollmentRepository.save(any())).thenReturn(saved);
        when(registrationStatusRepository.findByUserAndStage(any(), any())).thenReturn(Optional.empty());

        Map<String, Object> result = camabaReenrollmentService.submitReenrollment(
                "u@test.com", "0812", "p@test.com", "Addr", "PAddr", "", false, null, null,
                Collections.emptyMap());

        assertThat(result).containsKey("success");
        assertThat(result.get("success")).isEqualTo(true);
        verify(reenrollmentRepository).save(any());
    }

    @Test
    @DisplayName("submitReenrollment - with DAFTAR_ULANG status updates stage")
    void submitReenrollment_withDaftarUlangStatus_updatesStage() throws Exception {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).fullName("John Doe").user(user).build();
        ReEnrollment saved = ReEnrollment.builder()
                .id(5L)
                .student(student)
                .status(ReEnrollment.ReEnrollmentStatus.SUBMITTED)
                .documents(new ArrayList<>())
                .build();
        RegistrationStatus regStatus = new RegistrationStatus();

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(examRepository.findByStudent_Id(10L)).thenReturn(Optional.empty());
        when(reenrollmentRepository.findAll()).thenReturn(Collections.emptyList());
        when(reenrollmentRepository.save(any())).thenReturn(saved);
        when(registrationStatusRepository.findByUserAndStage(any(), any()))
                .thenReturn(Optional.of(regStatus));
        when(registrationStatusRepository.save(any())).thenReturn(regStatus);

        Map<String, Object> result = camabaReenrollmentService.submitReenrollment(
                "u@test.com", "0812", "p@test.com", "Addr", "PAddr", "", false, null, null,
                Collections.emptyMap());

        assertThat(result.get("success")).isEqualTo(true);
        verify(registrationStatusRepository).save(any());
    }

    // ===== getReenrollmentStatus =====

    @Test
    @DisplayName("getReenrollmentStatus - user not found throws")
    void getReenrollmentStatus_userNotFound_throws() {
        when(userRepository.findByEmail("bad@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaReenrollmentService.getReenrollmentStatus("bad@test.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getReenrollmentStatus - no enrollment returns NOT_STARTED")
    void getReenrollmentStatus_noEnrollment_returnsNotStarted() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(reenrollmentRepository.findAll()).thenReturn(Collections.emptyList());

        Map<String, Object> result = camabaReenrollmentService.getReenrollmentStatus("u@test.com");

        assertThat(result.get("status")).isEqualTo("NOT_STARTED");
    }

    @Test
    @DisplayName("getReenrollmentStatus - found returns status map")
    void getReenrollmentStatus_found_returnsStatusMap() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        ReEnrollment re = ReEnrollment.builder()
                .id(1L)
                .student(student)
                .status(ReEnrollment.ReEnrollmentStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .documents(new ArrayList<>())
                .build();

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(reenrollmentRepository.findAll()).thenReturn(List.of(re));

        Map<String, Object> result = camabaReenrollmentService.getReenrollmentStatus("u@test.com");

        assertThat(result).containsKey("status");
        assertThat(result.get("status")).isEqualTo("SUBMITTED");
    }

    // ===== completeReenrollment =====

    @Test
    @DisplayName("completeReenrollment - user not found throws")
    void completeReenrollment_userNotFound_throws() {
        when(userRepository.findByEmail("bad@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaReenrollmentService.completeReenrollment("bad@test.com", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("completeReenrollment - student not found throws")
    void completeReenrollment_studentNotFound_throws() {
        User user = User.builder().id(1L).email("u@test.com").build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaReenrollmentService.completeReenrollment("u@test.com", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Student not found");
    }

    @Test
    @DisplayName("completeReenrollment - success marks SELESAI")
    void completeReenrollment_success() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        RegistrationStatus status = new RegistrationStatus();
        status.setStatus(RegistrationStatus.RegistrationStatus_Enum.MENUNGGU_VERIFIKASI);

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(registrationStatusService.getOrCreateStatus(any(), any())).thenReturn(status);
        when(registrationStatusRepository.save(any())).thenReturn(status);

        Map<String, Object> result = camabaReenrollmentService.completeReenrollment("u@test.com", null);

        assertThat(result.get("success")).isEqualTo(true);
        verify(registrationStatusRepository).save(any());
    }

    @Test
    @DisplayName("completeReenrollment - with submittedAt sets dataJson")
    void completeReenrollment_withSubmittedAt_setsDataJson() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        RegistrationStatus status = new RegistrationStatus();

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(registrationStatusService.getOrCreateStatus(any(), any())).thenReturn(status);
        when(registrationStatusRepository.save(any())).thenReturn(status);

        Map<String, Object> result = camabaReenrollmentService.completeReenrollment("u@test.com", "2024-12-01");

        assertThat(result.get("success")).isEqualTo(true);
    }

    // ===== getReenrollmentData =====

    @Test
    @DisplayName("getReenrollmentData - user not found throws")
    void getReenrollmentData_userNotFound_throws() {
        when(userRepository.findByEmail("bad@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaReenrollmentService.getReenrollmentData("bad@test.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getReenrollmentData - no enrollment returns not exists")
    void getReenrollmentData_noEnrollment_returnsNotExists() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(reenrollmentRepository.findAll()).thenReturn(Collections.emptyList());

        Map<String, Object> result = camabaReenrollmentService.getReenrollmentData("u@test.com");

        assertThat(result.get("exists")).isEqualTo(false);
    }

    @Test
    @DisplayName("getReenrollmentData - found returns data map")
    void getReenrollmentData_found_returnsData() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        ReEnrollment re = ReEnrollment.builder()
                .id(1L)
                .student(student)
                .status(ReEnrollment.ReEnrollmentStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .documents(new ArrayList<>())
                .build();

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(reenrollmentRepository.findAll()).thenReturn(List.of(re));

        Map<String, Object> result = camabaReenrollmentService.getReenrollmentData("u@test.com");

        assertThat(result.get("exists")).isEqualTo(true);
        assertThat(result.get("id")).isEqualTo(1L);
    }

    @Test
    @DisplayName("getReenrollmentData - found with file fields returns url-mapped files")
    void getReenrollmentData_withFileFields_returnsMappedFiles() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        ReEnrollment re = ReEnrollment.builder()
                .id(1L)
                .student(student)
                .status(ReEnrollment.ReEnrollmentStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .paktaIntegritasFile("uploads/pakta.pdf")
                .ijazahFile("uploads/ijazah.pdf")
                .documents(new ArrayList<>())
                .build();

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(reenrollmentRepository.findAll()).thenReturn(List.of(re));
        when(fileStorageService.convertPathToUrl(any())).thenAnswer(i -> "/files/" + i.getArgument(0));

        Map<String, Object> result = camabaReenrollmentService.getReenrollmentData("u@test.com");

        assertThat(result.get("exists")).isEqualTo(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> docs = (Map<String, Object>) result.get("documents");
        assertThat(docs).containsKey("PAKTA_INTEGRITAS");
        assertThat(docs).containsKey("IJAZAH");
    }

    // ===== updateReenrollmentData =====

    @Test
    @DisplayName("updateReenrollmentData - user not found throws")
    void updateReenrollmentData_userNotFound_throws() {
        when(userRepository.findByEmail("bad@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaReenrollmentService.updateReenrollmentData(
                "bad@test.com", 1L, new MockHttpServletRequest()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("updateReenrollmentData - reenrollment not found throws")
    void updateReenrollmentData_reenrollmentNotFound_throws() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(reenrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaReenrollmentService.updateReenrollmentData(
                "u@test.com", 99L, new MockHttpServletRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("updateReenrollmentData - unauthorized throws SecurityException")
    void updateReenrollmentData_unauthorized_throwsSecurityException() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        Student otherStudent = Student.builder().id(99L).build();
        ReEnrollment re = ReEnrollment.builder()
                .id(1L)
                .student(otherStudent)
                .status(ReEnrollment.ReEnrollmentStatus.SUBMITTED)
                .build();

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(re));

        assertThatThrownBy(() -> camabaReenrollmentService.updateReenrollmentData(
                "u@test.com", 1L, new MockHttpServletRequest()))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    @DisplayName("updateReenrollmentData - validated status throws RuntimeException")
    void updateReenrollmentData_validatedStatus_throws() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        ReEnrollment re = ReEnrollment.builder()
                .id(1L)
                .student(student)
                .status(ReEnrollment.ReEnrollmentStatus.VALIDATED)
                .build();

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(re));

        assertThatThrownBy(() -> camabaReenrollmentService.updateReenrollmentData(
                "u@test.com", 1L, new MockHttpServletRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("sudah divalidasi");
    }

    @Test
    @DisplayName("updateReenrollmentData - success with non-multipart saves")
    void updateReenrollmentData_success_nonMultipart_saves() throws Exception {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        ReEnrollment re = ReEnrollment.builder()
                .id(1L)
                .student(student)
                .status(ReEnrollment.ReEnrollmentStatus.SUBMITTED)
                .documents(new ArrayList<>())
                .build();

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(re));
        when(reenrollmentRepository.save(any())).thenReturn(re);

        Map<String, Object> result = camabaReenrollmentService.updateReenrollmentData(
                "u@test.com", 1L, new MockHttpServletRequest());

        assertThat(result.get("success")).isEqualTo(true);
        verify(reenrollmentRepository).save(any());
    }

    @Test
    @DisplayName("updateReenrollmentData - multipart request updates fields")
    void updateReenrollmentData_multipartRequest_updatesFields() throws Exception {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        ReEnrollment re = ReEnrollment.builder()
                .id(1L)
                .student(student)
                .status(ReEnrollment.ReEnrollmentStatus.SUBMITTED)
                .documents(new ArrayList<>())
                .build();

        MockMultipartHttpServletRequest multipartRequest = new MockMultipartHttpServletRequest();
        multipartRequest.setParameter("parentName", "John's Parent");
        multipartRequest.setParameter("parentPhone", "081234567890");
        multipartRequest.setParameter("parentEmail", "parent@test.com");

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(re));
        when(reenrollmentRepository.save(any())).thenReturn(re);

        Map<String, Object> result = camabaReenrollmentService.updateReenrollmentData(
                "u@test.com", 1L, multipartRequest);

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(re.getParentName()).isEqualTo("John's Parent");
    }

    // ===== getReenrollmentDocuments =====

    @Test
    @DisplayName("getReenrollmentDocuments - user not found throws")
    void getReenrollmentDocuments_userNotFound_throws() {
        when(userRepository.findByEmail("bad@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaReenrollmentService.getReenrollmentDocuments("bad@test.com", 1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getReenrollmentDocuments - reenrollment not found throws")
    void getReenrollmentDocuments_reenrollmentNotFound_throws() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(reenrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaReenrollmentService.getReenrollmentDocuments("u@test.com", 99L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getReenrollmentDocuments - unauthorized throws SecurityException")
    void getReenrollmentDocuments_unauthorized_throwsSecurityException() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        Student otherStudent = Student.builder().id(99L).build();
        ReEnrollment re = ReEnrollment.builder()
                .id(1L)
                .student(otherStudent)
                .documents(new ArrayList<>())
                .build();

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(re));

        assertThatThrownBy(() -> camabaReenrollmentService.getReenrollmentDocuments("u@test.com", 1L))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    @DisplayName("getReenrollmentDocuments - found returns document list")
    void getReenrollmentDocuments_found_returnsDocumentList() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        ReEnrollmentDocument doc = ReEnrollmentDocument.builder()
                .id(1L)
                .documentType(ReEnrollmentDocument.DocumentType.IJAZAH)
                .originalFilename("ijazah.pdf")
                .uploadedAt(LocalDateTime.now())
                .validationStatus(ReEnrollmentDocument.ValidationStatus.PENDING)
                .build();
        ReEnrollment re = ReEnrollment.builder()
                .id(1L)
                .student(student)
                .documents(new ArrayList<>(List.of(doc)))
                .build();

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(re));

        List<Map<String, Object>> result = camabaReenrollmentService.getReenrollmentDocuments("u@test.com", 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsKey("documentType");
    }

    @Test
    @DisplayName("getReenrollmentDocuments - no documents returns empty list")
    void getReenrollmentDocuments_noDocuments_returnsEmpty() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        ReEnrollment re = ReEnrollment.builder()
                .id(1L)
                .student(student)
                .documents(new ArrayList<>())
                .build();

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(re));

        List<Map<String, Object>> result = camabaReenrollmentService.getReenrollmentDocuments("u@test.com", 1L);

        assertThat(result).isEmpty();
    }

    // ===== Additional branch coverage tests =====

    @Test
    @DisplayName("submitReenrollment - with exam present covers exam lookup path")
    void submitReenrollment_withExamPresent_coversExamLookupPath() throws Exception {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).fullName("John").user(user).build();
        Exam exam = Exam.builder().id(5L).build();
        ExamResult examResult = ExamResult.builder().id(1L)
                .status(ExamResult.ResultStatus.PASSED).build();
        ReEnrollment saved = ReEnrollment.builder()
                .id(5L).student(student)
                .status(ReEnrollment.ReEnrollmentStatus.SUBMITTED)
                .documents(new ArrayList<>()).build();

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(examRepository.findByStudent_Id(10L)).thenReturn(Optional.of(exam));
        when(examResultRepository.findByExam_Id(5L)).thenReturn(Optional.of(examResult));
        when(reenrollmentRepository.findAll()).thenReturn(Collections.emptyList());
        when(reenrollmentRepository.save(any())).thenReturn(saved);
        when(registrationStatusRepository.findByUserAndStage(any(), any())).thenReturn(Optional.empty());

        Map<String, Object> result = camabaReenrollmentService.submitReenrollment(
                "u@test.com", "0812", "p@test.com", "Addr", "PAddr", "", false, null, null,
                Collections.emptyMap());

        assertThat(result.get("success")).isEqualTo(true);
    }

    @Test
    @DisplayName("submitReenrollment - exam present but result not PASSED, sets null examResult")
    void submitReenrollment_withExamPresentFailedResult_setsNullExamResult() throws Exception {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).fullName("John").user(user).build();
        Exam exam = Exam.builder().id(5L).build();
        ExamResult failedResult = ExamResult.builder().id(1L)
                .status(ExamResult.ResultStatus.FAILED).build();
        ReEnrollment saved = ReEnrollment.builder()
                .id(6L).student(student)
                .status(ReEnrollment.ReEnrollmentStatus.SUBMITTED)
                .documents(new ArrayList<>()).build();

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(examRepository.findByStudent_Id(10L)).thenReturn(Optional.of(exam));
        when(examResultRepository.findByExam_Id(5L)).thenReturn(Optional.of(failedResult));
        when(reenrollmentRepository.findAll()).thenReturn(Collections.emptyList());
        when(reenrollmentRepository.save(any())).thenReturn(saved);
        when(registrationStatusRepository.findByUserAndStage(any(), any())).thenReturn(Optional.empty());

        Map<String, Object> result = camabaReenrollmentService.submitReenrollment(
                "u@test.com", "0812", "p@test.com", "Addr", "PAddr", "", false, null, null,
                Collections.emptyMap());

        assertThat(result.get("success")).isEqualTo(true);
    }

    @Test
    @DisplayName("submitReenrollment - file too large is skipped (no write)")
    void submitReenrollment_withLargeFile_skipsFile() throws Exception {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).fullName("John").user(user).build();
        ReEnrollment saved = ReEnrollment.builder()
                .id(7L).student(student)
                .status(ReEnrollment.ReEnrollmentStatus.SUBMITTED)
                .documents(new ArrayList<>()).build();

        // 6 MB file — above the 5 MB limit, will be skipped without writing
        MockMultipartFile largeFile = new MockMultipartFile(
                "documents[IJAZAH]", "ijazah.pdf", "application/pdf", new byte[6 * 1024 * 1024]);
        Map<String, org.springframework.web.multipart.MultipartFile> docs = new java.util.HashMap<>();
        docs.put("documents[IJAZAH]", largeFile);

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(examRepository.findByStudent_Id(10L)).thenReturn(Optional.empty());
        when(reenrollmentRepository.findAll()).thenReturn(Collections.emptyList());
        when(reenrollmentRepository.save(any())).thenReturn(saved);
        when(registrationStatusRepository.findByUserAndStage(any(), any())).thenReturn(Optional.empty());

        Map<String, Object> result = camabaReenrollmentService.submitReenrollment(
                "u@test.com", "0812", "p@test.com", "Addr", "PAddr", "", false, null, null, docs);

        assertThat(result.get("success")).isEqualTo(true);
        // documentsCount == 0 because file was too large and skipped
        assertThat(result.get("documentsCount")).isEqualTo(0);
    }

    @Test
    @DisplayName("submitReenrollment - invalid doc type key is skipped (IllegalArgumentException)")
    void submitReenrollment_withInvalidDocType_skipsInvalidType() throws Exception {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).fullName("John").user(user).build();
        ReEnrollment saved = ReEnrollment.builder()
                .id(8L).student(student)
                .status(ReEnrollment.ReEnrollmentStatus.SUBMITTED)
                .documents(new ArrayList<>()).build();

        // 3-byte file with invalid doc type key → IllegalArgumentException caught internally
        MockMultipartFile smallFile = new MockMultipartFile(
                "documents[INVALID_TYPE_XYZ]", "file.pdf", "application/pdf", new byte[]{1, 2, 3});
        Map<String, org.springframework.web.multipart.MultipartFile> docs = new java.util.HashMap<>();
        docs.put("documents[INVALID_TYPE_XYZ]", smallFile);

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(examRepository.findByStudent_Id(10L)).thenReturn(Optional.empty());
        when(reenrollmentRepository.findAll()).thenReturn(Collections.emptyList());
        when(reenrollmentRepository.save(any())).thenReturn(saved);
        when(registrationStatusRepository.findByUserAndStage(any(), any())).thenReturn(Optional.empty());

        Map<String, Object> result = camabaReenrollmentService.submitReenrollment(
                "u@test.com", "0812", "p@test.com", "Addr", "PAddr", "", false, null, null, docs);

        assertThat(result.get("success")).isEqualTo(true);
        // Invalid type skipped, documentsCount == 0
        assertThat(result.get("documentsCount")).isEqualTo(0);
    }

    @Test
    @DisplayName("getReenrollmentStatus - with approved documents returns correct count")
    void getReenrollmentStatus_withApprovedDocuments_returnsCorrectCount() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        ReEnrollmentDocument approvedDoc = ReEnrollmentDocument.builder()
                .id(1L)
                .documentType(ReEnrollmentDocument.DocumentType.IJAZAH)
                .validationStatus(ReEnrollmentDocument.ValidationStatus.APPROVED)
                .build();
        ReEnrollmentDocument pendingDoc = ReEnrollmentDocument.builder()
                .id(2L)
                .documentType(ReEnrollmentDocument.DocumentType.PAKTA_INTEGRITAS)
                .validationStatus(ReEnrollmentDocument.ValidationStatus.PENDING)
                .build();
        ReEnrollment re = ReEnrollment.builder()
                .id(1L)
                .student(student)
                .status(ReEnrollment.ReEnrollmentStatus.SUBMITTED)
                .submittedAt(java.time.LocalDateTime.now())
                .documents(new ArrayList<>(List.of(approvedDoc, pendingDoc)))
                .build();

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(reenrollmentRepository.findAll()).thenReturn(List.of(re));

        Map<String, Object> result = camabaReenrollmentService.getReenrollmentStatus("u@test.com");

        assertThat(result.get("totalDocuments")).isEqualTo(2);
        assertThat(result.get("approvedDocuments")).isEqualTo(1);
    }

    @Test
    @DisplayName("getReenrollmentData - with ReEnrollmentDocument list returns document URLs")
    void getReenrollmentData_withDocumentList_returnsDocumentUrls() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        ReEnrollmentDocument doc = ReEnrollmentDocument.builder()
                .id(1L)
                .documentType(ReEnrollmentDocument.DocumentType.IJAZAH)
                .filePath("uploads/reenrollment/student/ijazah.pdf")
                .originalFilename("ijazah.pdf")
                .uploadedAt(java.time.LocalDateTime.now())
                .validationStatus(ReEnrollmentDocument.ValidationStatus.PENDING)
                .build();
        ReEnrollment re = ReEnrollment.builder()
                .id(1L)
                .student(student)
                .status(ReEnrollment.ReEnrollmentStatus.SUBMITTED)
                .submittedAt(java.time.LocalDateTime.now())
                .documents(new ArrayList<>(List.of(doc)))
                .build();

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(reenrollmentRepository.findAll()).thenReturn(List.of(re));
        when(fileStorageService.convertPathToUrl(any())).thenReturn("/files/ijazah.pdf");

        Map<String, Object> result = camabaReenrollmentService.getReenrollmentData("u@test.com");

        assertThat(result.get("exists")).isEqualTo(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> docs = (Map<String, Object>) result.get("documents");
        assertThat(docs).containsKey("IJAZAH");
    }

    @Test
    @DisplayName("updateReenrollmentData - REJECTED status throws RuntimeException")
    void updateReenrollmentData_rejectedStatus_throws() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        ReEnrollment re = ReEnrollment.builder()
                .id(1L)
                .student(student)
                .status(ReEnrollment.ReEnrollmentStatus.REJECTED)
                .build();

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(re));

        assertThatThrownBy(() -> camabaReenrollmentService.updateReenrollmentData(
                "u@test.com", 1L, new MockHttpServletRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("sudah divalidasi");
    }

    @Test
    @DisplayName("submitReenrollment - valid small file is written to disk and saved")
    void submitReenrollment_withValidSmallFile_writesFileAndSaves() throws Exception {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).fullName("John").user(user).build();
        ReEnrollment saved = ReEnrollment.builder()
                .id(9L).student(student)
                .status(ReEnrollment.ReEnrollmentStatus.SUBMITTED)
                .documents(new ArrayList<>()).build();

        // Small valid file with a recognised document type
        MockMultipartFile validFile = new MockMultipartFile(
                "documents[IJAZAH]", "ijazah.pdf", "application/pdf", new byte[]{1, 2, 3, 4, 5});
        Map<String, org.springframework.web.multipart.MultipartFile> docs = new java.util.HashMap<>();
        docs.put("documents[IJAZAH]", validFile);

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(examRepository.findByStudent_Id(10L)).thenReturn(Optional.empty());
        when(reenrollmentRepository.findAll()).thenReturn(Collections.emptyList());
        when(reenrollmentRepository.save(any())).thenReturn(saved);
        when(registrationStatusRepository.findByUserAndStage(any(), any())).thenReturn(Optional.empty());

        Map<String, Object> result = camabaReenrollmentService.submitReenrollment(
                "u@test.com", "0812", "p@test.com", "Addr", "PAddr", "", false, null, null, docs);

        assertThat(result.get("success")).isEqualTo(true);
        // File was processed (valid type, small size), documentsCount >= 1
        assertThat((int) result.get("documentsCount")).isGreaterThanOrEqualTo(1);
    }
}
