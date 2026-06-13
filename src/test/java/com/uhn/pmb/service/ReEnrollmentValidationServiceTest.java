package com.uhn.pmb.service;

import com.uhn.pmb.dto.FormValidationRejectRequest;
import com.uhn.pmb.entity.*;
import com.uhn.pmb.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReEnrollmentValidationServiceTest {

    @Mock private ReEnrollmentRepository reenrollmentRepository;
    @Mock private ReEnrollmentValidationRepository reEnrollmentValidationRepository;
    @Mock private ReEnrollmentDocumentRepository reenrollmentDocumentRepository;
    @Mock private RegistrationStatusService registrationStatusService;

    @InjectMocks
    private ReEnrollmentValidationService reEnrollmentValidationService;

    // ===== Helpers =====

    private User buildUser(Long id, String username) {
        return User.builder().id(id).email(username + "@test.com").password("pass").role(User.UserRole.CAMABA).build();
    }

    private Student buildStudent(Long id, User user) {
        return Student.builder().id(id).user(user).build();
    }

    private ReEnrollment buildReEnrollment(Long id, Student student, ReEnrollment.ReEnrollmentStatus status) {
        return ReEnrollment.builder()
                .id(id)
                .student(student)
                .status(status)
                .build();
    }

    private ReEnrollmentDocument buildDocument(Long id, ReEnrollment reEnrollment) {
        return ReEnrollmentDocument.builder()
                .id(id)
                .reenrollment(reEnrollment)
                .documentType(ReEnrollmentDocument.DocumentType.IJAZAH)
                .originalFilename("ijazah.pdf")
                .build();
    }

    private FormValidationRejectRequest buildRejectRequest(String reason) {
        FormValidationRejectRequest req = new FormValidationRejectRequest();
        req.setReason(reason);
        return req;
    }

    // ===== getPendingReEnrollments =====

    @Test
    @DisplayName("getPendingReEnrollments - returns SUBMITTED list from repository")
    void getPendingReEnrollments_returnsList() {
        User user = buildUser(1L, "student1");
        Student student = buildStudent(10L, user);
        ReEnrollment re = buildReEnrollment(1L, student, ReEnrollment.ReEnrollmentStatus.SUBMITTED);

        when(reenrollmentRepository.findByStatus(ReEnrollment.ReEnrollmentStatus.SUBMITTED))
                .thenReturn(List.of(re));

        List<ReEnrollment> result = reEnrollmentValidationService.getPendingReEnrollments();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(ReEnrollment.ReEnrollmentStatus.SUBMITTED);
    }

    @Test
    @DisplayName("getPendingReEnrollments - empty repository returns empty list")
    void getPendingReEnrollments_empty_returnsEmptyList() {
        when(reenrollmentRepository.findByStatus(ReEnrollment.ReEnrollmentStatus.SUBMITTED))
                .thenReturn(List.of());

        List<ReEnrollment> result = reEnrollmentValidationService.getPendingReEnrollments();

        assertThat(result).isEmpty();
    }

    // ===== getInProgressReEnrollments =====

    @Test
    @DisplayName("getInProgressReEnrollments - returns SUBMITTED list from repository")
    void getInProgressReEnrollments_returnsList() {
        User user = buildUser(1L, "student1");
        Student student = buildStudent(10L, user);
        ReEnrollment re = buildReEnrollment(1L, student, ReEnrollment.ReEnrollmentStatus.SUBMITTED);

        when(reenrollmentRepository.findByStatus(ReEnrollment.ReEnrollmentStatus.SUBMITTED))
                .thenReturn(List.of(re));

        List<ReEnrollment> result = reEnrollmentValidationService.getInProgressReEnrollments();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getInProgressReEnrollments - empty returns empty list")
    void getInProgressReEnrollments_empty_returnsEmptyList() {
        when(reenrollmentRepository.findByStatus(ReEnrollment.ReEnrollmentStatus.SUBMITTED))
                .thenReturn(List.of());

        List<ReEnrollment> result = reEnrollmentValidationService.getInProgressReEnrollments();

        assertThat(result).isEmpty();
    }

    // ===== getReEnrollmentById =====

    @Test
    @DisplayName("getReEnrollmentById - found returns entity")
    void getReEnrollmentById_found_returnsEntity() {
        User user = buildUser(1L, "student1");
        Student student = buildStudent(10L, user);
        ReEnrollment re = buildReEnrollment(1L, student, ReEnrollment.ReEnrollmentStatus.SUBMITTED);

        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(re));

        ReEnrollment result = reEnrollmentValidationService.getReEnrollmentById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getReEnrollmentById - not found throws RuntimeException")
    void getReEnrollmentById_notFound_throwsException() {
        when(reenrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reEnrollmentValidationService.getReEnrollmentById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Daftar ulang tidak ditemukan");
    }

    // ===== approveReEnrollment =====

    @Test
    @DisplayName("approveReEnrollment - not found throws RuntimeException")
    void approveReEnrollment_notFound_throwsException() {
        User admin = buildUser(99L, "admin");
        when(reenrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reEnrollmentValidationService.approveReEnrollment(99L, admin))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Daftar ulang tidak ditemukan");
    }

    @Test
    @DisplayName("approveReEnrollment - sets VALIDATED status and validatedAt")
    void approveReEnrollment_found_setsValidatedStatus() {
        User studentUser = buildUser(1L, "student1");
        Student student = buildStudent(10L, studentUser);
        ReEnrollment re = buildReEnrollment(5L, student, ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        User admin = buildUser(2L, "admin");

        when(reenrollmentRepository.findById(5L)).thenReturn(Optional.of(re));
        when(reenrollmentRepository.save(any())).thenReturn(re);
        when(registrationStatusService.approveByAdmin(any(), any(), any(), any()))
                .thenReturn(new RegistrationStatus());

        reEnrollmentValidationService.approveReEnrollment(5L, admin);

        assertThat(re.getStatus()).isEqualTo(ReEnrollment.ReEnrollmentStatus.VALIDATED);
        assertThat(re.getValidatedAt()).isNotNull();
        verify(reenrollmentRepository).save(re);
    }

    @Test
    @DisplayName("approveReEnrollment - sets validationNotes with admin username")
    void approveReEnrollment_setsValidationNotes() {
        User studentUser = buildUser(1L, "student1");
        Student student = buildStudent(10L, studentUser);
        ReEnrollment re = buildReEnrollment(5L, student, ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        User admin = buildUser(2L, "superadmin");

        when(reenrollmentRepository.findById(5L)).thenReturn(Optional.of(re));
        when(reenrollmentRepository.save(any())).thenReturn(re);
        when(registrationStatusService.approveByAdmin(any(), any(), any(), any()))
                .thenReturn(new RegistrationStatus());

        reEnrollmentValidationService.approveReEnrollment(5L, admin);

        assertThat(re.getValidationNotes()).contains("superadmin");
    }

    @Test
    @DisplayName("approveReEnrollment - delegates to registrationStatusService.approveByAdmin with DAFTAR_ULANG stage")
    void approveReEnrollment_delegatesToRegistrationStatusService() {
        User studentUser = buildUser(1L, "student1");
        Student student = buildStudent(10L, studentUser);
        ReEnrollment re = buildReEnrollment(5L, student, ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        User admin = buildUser(2L, "admin");

        when(reenrollmentRepository.findById(5L)).thenReturn(Optional.of(re));
        when(reenrollmentRepository.save(any())).thenReturn(re);
        when(registrationStatusService.approveByAdmin(any(), any(), any(), any()))
                .thenReturn(new RegistrationStatus());

        reEnrollmentValidationService.approveReEnrollment(5L, admin);

        verify(registrationStatusService).approveByAdmin(
                eq(studentUser),
                eq(RegistrationStatus.RegistrationStage.DAFTAR_ULANG),
                eq("admin@test.com"),
                argThat(notes -> notes.contains("admin"))
        );
    }

    // ===== rejectReEnrollment =====

    @Test
    @DisplayName("rejectReEnrollment - not found throws RuntimeException")
    void rejectReEnrollment_notFound_throwsException() {
        User admin = buildUser(2L, "admin");
        FormValidationRejectRequest req = buildRejectRequest("Tidak lengkap");
        when(reenrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reEnrollmentValidationService.rejectReEnrollment(99L, req, admin))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Daftar ulang tidak ditemukan");
    }

    @Test
    @DisplayName("rejectReEnrollment - sets REJECTED status and validatedAt")
    void rejectReEnrollment_found_setsRejectedStatus() {
        User studentUser = buildUser(1L, "student1");
        Student student = buildStudent(10L, studentUser);
        ReEnrollment re = buildReEnrollment(5L, student, ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        User admin = buildUser(2L, "admin");
        FormValidationRejectRequest req = buildRejectRequest("Dokumen tidak valid");

        when(reenrollmentRepository.findById(5L)).thenReturn(Optional.of(re));
        when(reenrollmentRepository.save(any())).thenReturn(re);
        when(registrationStatusService.rejectByAdmin(any(), any(), any(), any()))
                .thenReturn(new RegistrationStatus());

        reEnrollmentValidationService.rejectReEnrollment(5L, req, admin);

        assertThat(re.getStatus()).isEqualTo(ReEnrollment.ReEnrollmentStatus.REJECTED);
        assertThat(re.getValidatedAt()).isNotNull();
        verify(reenrollmentRepository).save(re);
    }

    @Test
    @DisplayName("rejectReEnrollment - sets validationNotes from request reason")
    void rejectReEnrollment_setsValidationNotesFromReason() {
        User studentUser = buildUser(1L, "student1");
        Student student = buildStudent(10L, studentUser);
        ReEnrollment re = buildReEnrollment(5L, student, ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        User admin = buildUser(2L, "admin");
        FormValidationRejectRequest req = buildRejectRequest("Foto buram");

        when(reenrollmentRepository.findById(5L)).thenReturn(Optional.of(re));
        when(reenrollmentRepository.save(any())).thenReturn(re);
        when(registrationStatusService.rejectByAdmin(any(), any(), any(), any()))
                .thenReturn(new RegistrationStatus());

        reEnrollmentValidationService.rejectReEnrollment(5L, req, admin);

        assertThat(re.getValidationNotes()).isEqualTo("Foto buram");
    }

    @Test
    @DisplayName("rejectReEnrollment - null reason defaults to '-' in validationNotes")
    void rejectReEnrollment_nullReason_defaultsToDash() {
        User studentUser = buildUser(1L, "student1");
        Student student = buildStudent(10L, studentUser);
        ReEnrollment re = buildReEnrollment(5L, student, ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        User admin = buildUser(2L, "admin");
        FormValidationRejectRequest req = buildRejectRequest(null);

        when(reenrollmentRepository.findById(5L)).thenReturn(Optional.of(re));
        when(reenrollmentRepository.save(any())).thenReturn(re);
        when(registrationStatusService.rejectByAdmin(any(), any(), any(), any()))
                .thenReturn(new RegistrationStatus());

        reEnrollmentValidationService.rejectReEnrollment(5L, req, admin);

        assertThat(re.getValidationNotes()).isEqualTo("-");
    }

    @Test
    @DisplayName("rejectReEnrollment - delegates to registrationStatusService.rejectByAdmin with DAFTAR_ULANG stage")
    void rejectReEnrollment_delegatesToRegistrationStatusService() {
        User studentUser = buildUser(1L, "student1");
        Student student = buildStudent(10L, studentUser);
        ReEnrollment re = buildReEnrollment(5L, student, ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        User admin = buildUser(2L, "admin");
        FormValidationRejectRequest req = buildRejectRequest("Tidak sesuai");

        when(reenrollmentRepository.findById(5L)).thenReturn(Optional.of(re));
        when(reenrollmentRepository.save(any())).thenReturn(re);
        when(registrationStatusService.rejectByAdmin(any(), any(), any(), any()))
                .thenReturn(new RegistrationStatus());

        reEnrollmentValidationService.rejectReEnrollment(5L, req, admin);

        verify(registrationStatusService).rejectByAdmin(
                eq(studentUser),
                eq(RegistrationStatus.RegistrationStage.DAFTAR_ULANG),
                eq("admin@test.com"),
                argThat(notes -> notes.contains("Tidak sesuai"))
        );
    }

    // ===== markAsInProgress =====

    @Test
    @DisplayName("markAsInProgress - not found throws RuntimeException")
    void markAsInProgress_notFound_throwsException() {
        when(reenrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reEnrollmentValidationService.markAsInProgress(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Daftar ulang tidak ditemukan");
    }

    @Test
    @DisplayName("markAsInProgress - sets status to SUBMITTED and saves")
    void markAsInProgress_found_setsSubmittedStatus() {
        User user = buildUser(1L, "student1");
        Student student = buildStudent(10L, user);
        ReEnrollment re = buildReEnrollment(5L, student, ReEnrollment.ReEnrollmentStatus.VALIDATED);

        when(reenrollmentRepository.findById(5L)).thenReturn(Optional.of(re));
        when(reenrollmentRepository.save(any())).thenReturn(re);

        reEnrollmentValidationService.markAsInProgress(5L);

        assertThat(re.getStatus()).isEqualTo(ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        verify(reenrollmentRepository).save(re);
    }

    // ===== getReEnrollmentDocuments =====

    @Test
    @DisplayName("getReEnrollmentDocuments - returns list from repository")
    void getReEnrollmentDocuments_returnsList() {
        User user = buildUser(1L, "student1");
        Student student = buildStudent(10L, user);
        ReEnrollment re = buildReEnrollment(5L, student, ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        ReEnrollmentDocument doc = buildDocument(1L, re);

        when(reenrollmentDocumentRepository.findByReenrollmentId(5L)).thenReturn(List.of(doc));

        List<ReEnrollmentDocument> result = reEnrollmentValidationService.getReEnrollmentDocuments(5L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDocumentType()).isEqualTo(ReEnrollmentDocument.DocumentType.IJAZAH);
    }

    @Test
    @DisplayName("getReEnrollmentDocuments - empty returns empty list")
    void getReEnrollmentDocuments_empty_returnsEmptyList() {
        when(reenrollmentDocumentRepository.findByReenrollmentId(5L)).thenReturn(List.of());

        List<ReEnrollmentDocument> result = reEnrollmentValidationService.getReEnrollmentDocuments(5L);

        assertThat(result).isEmpty();
    }

    // ===== getReEnrollmentDocument =====

    @Test
    @DisplayName("getReEnrollmentDocument - found returns entity")
    void getReEnrollmentDocument_found_returnsEntity() {
        User user = buildUser(1L, "student1");
        Student student = buildStudent(10L, user);
        ReEnrollment re = buildReEnrollment(5L, student, ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        ReEnrollmentDocument doc = buildDocument(1L, re);

        when(reenrollmentDocumentRepository.findById(1L)).thenReturn(Optional.of(doc));

        ReEnrollmentDocument result = reEnrollmentValidationService.getReEnrollmentDocument(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getReEnrollmentDocument - not found throws RuntimeException")
    void getReEnrollmentDocument_notFound_throwsException() {
        when(reenrollmentDocumentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reEnrollmentValidationService.getReEnrollmentDocument(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Dokumen tidak ditemukan");
    }

    // ===== validateReEnrollmentDocument =====

    @Test
    @DisplayName("validateReEnrollmentDocument - document not found throws RuntimeException")
    void validateReEnrollmentDocument_documentNotFound_throwsException() {
        User validator = buildUser(2L, "validator");
        when(reenrollmentDocumentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                reEnrollmentValidationService.validateReEnrollmentDocument(99L, true, "OK", validator))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Dokumen tidak ditemukan");
    }

    @Test
    @DisplayName("validateReEnrollmentDocument - isValid=true saves APPROVED validation")
    void validateReEnrollmentDocument_validTrue_savesApprovedValidation() {
        User studentUser = buildUser(1L, "student1");
        Student student = buildStudent(10L, studentUser);
        ReEnrollment re = buildReEnrollment(5L, student, ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        ReEnrollmentDocument doc = buildDocument(1L, re);
        User validator = buildUser(2L, "validator");

        when(reenrollmentDocumentRepository.findById(1L)).thenReturn(Optional.of(doc));
        when(reEnrollmentValidationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        reEnrollmentValidationService.validateReEnrollmentDocument(1L, true, "Dokumen valid", validator);

        verify(reEnrollmentValidationRepository).save(argThat(v ->
                v.getValidationStatus() == ReEnrollmentValidation.ValidationStatus.APPROVED
                        && v.getRejectionReason().equals("Dokumen valid")
                        && v.getValidatedBy().equals(validator)
                        && v.getValidatedAt() != null
                        && v.getReEnrollment().equals(re)
                        && v.getStudent().equals(student)
        ));
    }

    @Test
    @DisplayName("validateReEnrollmentDocument - isValid=false saves REJECTED validation")
    void validateReEnrollmentDocument_validFalse_savesRejectedValidation() {
        User studentUser = buildUser(1L, "student1");
        Student student = buildStudent(10L, studentUser);
        ReEnrollment re = buildReEnrollment(5L, student, ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        ReEnrollmentDocument doc = buildDocument(1L, re);
        User validator = buildUser(2L, "validator");

        when(reenrollmentDocumentRepository.findById(1L)).thenReturn(Optional.of(doc));
        when(reEnrollmentValidationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        reEnrollmentValidationService.validateReEnrollmentDocument(1L, false, "Foto buram", validator);

        verify(reEnrollmentValidationRepository).save(argThat(v ->
                v.getValidationStatus() == ReEnrollmentValidation.ValidationStatus.REJECTED
                        && v.getRejectionReason().equals("Foto buram")
        ));
    }

    @Test
    @DisplayName("validateReEnrollmentDocument - null notes is passed as rejectionReason")
    void validateReEnrollmentDocument_nullNotes_savedAsNull() {
        User studentUser = buildUser(1L, "student1");
        Student student = buildStudent(10L, studentUser);
        ReEnrollment re = buildReEnrollment(5L, student, ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        ReEnrollmentDocument doc = buildDocument(1L, re);
        User validator = buildUser(2L, "validator");

        when(reenrollmentDocumentRepository.findById(1L)).thenReturn(Optional.of(doc));
        when(reEnrollmentValidationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        reEnrollmentValidationService.validateReEnrollmentDocument(1L, true, null, validator);

        verify(reEnrollmentValidationRepository).save(argThat(v ->
                v.getRejectionReason() == null
        ));
    }

    @Test
    @DisplayName("validateReEnrollmentDocument - validation links correct reEnrollment and student")
    void validateReEnrollmentDocument_linksCorrectEntities() {
        User studentUser = buildUser(1L, "student1");
        Student student = buildStudent(10L, studentUser);
        ReEnrollment re = buildReEnrollment(5L, student, ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        ReEnrollmentDocument doc = buildDocument(1L, re);
        User validator = buildUser(2L, "validator");

        when(reenrollmentDocumentRepository.findById(1L)).thenReturn(Optional.of(doc));
        when(reEnrollmentValidationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        reEnrollmentValidationService.validateReEnrollmentDocument(1L, true, "OK", validator);

        verify(reEnrollmentValidationRepository).save(argThat(v ->
                v.getReEnrollment().getId().equals(5L)
                        && v.getStudent().getId().equals(10L)
        ));
    }
}