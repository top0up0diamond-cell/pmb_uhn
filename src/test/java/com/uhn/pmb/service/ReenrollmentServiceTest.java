package com.uhn.pmb.service;

import com.uhn.pmb.dto.DocumentValidationRequest;
import com.uhn.pmb.dto.ReenrollmentFinalizeRequest;
import com.uhn.pmb.entity.*;
import com.uhn.pmb.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReenrollmentServiceTest {

    @Mock private ReEnrollmentRepository reenrollmentRepository;
    @Mock private ReEnrollmentValidationRepository reEnrollmentValidationRepository;
    @Mock private ReEnrollmentDocumentRepository reenrollmentDocumentRepository;
    @Mock private RegistrationStatusService registrationStatusService;

    private ReenrollmentService reenrollmentService;

    @BeforeEach
    void setUp() {
        reenrollmentService = new ReenrollmentService(
                reenrollmentRepository,
                reEnrollmentValidationRepository,
                reenrollmentDocumentRepository,
                registrationStatusService
        );
    }

    private User buildAdmin() {
        return User.builder().id(99L).email("admin@test.com")
                .role(User.UserRole.ADMIN_PUSAT).build();
    }

    private ReEnrollment buildReEnrollment(Long id, ReEnrollment.ReEnrollmentStatus status) {
        User studentUser = User.builder().id(2L).email("student@test.com").build();
        Student student = Student.builder().id(10L).fullName("Budi").user(studentUser).build();
        ReEnrollment re = new ReEnrollment();
        re.setId(id);
        re.setStudent(student);
        re.setStatus(status);
        return re;
    }

    // ===== findPending =====

    @Test
    @DisplayName("findPending - returns only SUBMITTED status")
    void findPending_returnsOnlySubmitted() {
        ReEnrollment submitted = buildReEnrollment(1L, ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        ReEnrollment validated = buildReEnrollment(2L, ReEnrollment.ReEnrollmentStatus.VALIDATED);
        when(reenrollmentRepository.findAll()).thenReturn(List.of(submitted, validated));

        List<ReEnrollment> result = reenrollmentService.findPending();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findPending - empty list returns empty")
    void findPending_emptyList_returnsEmpty() {
        when(reenrollmentRepository.findAll()).thenReturn(Collections.emptyList());

        List<ReEnrollment> result = reenrollmentService.findPending();

        assertThat(result).isEmpty();
    }

    // ===== findById =====

    @Test
    @DisplayName("findById - found returns Optional")
    void findById_found_returnsOptional() {
        ReEnrollment re = buildReEnrollment(1L, ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(re));

        Optional<ReEnrollment> result = reenrollmentService.findById(1L);

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("findById - not found returns empty")
    void findById_notFound_returnsEmpty() {
        when(reenrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<ReEnrollment> result = reenrollmentService.findById(99L);

        assertThat(result).isEmpty();
    }

    // ===== approve =====

    @Test
    @DisplayName("approve - not found throws RuntimeException")
    void approve_notFound_throws() {
        when(reenrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reenrollmentService.approve(99L, buildAdmin()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("tidak ditemukan");
    }

    @Test
    @DisplayName("approve - existing validation updated to APPROVED")
    void approve_existingValidation_updatesStatus() {
        ReEnrollment re = buildReEnrollment(1L, ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(re));

        ReEnrollmentValidation existing = new ReEnrollmentValidation();
        existing.setValidationStatus(ReEnrollmentValidation.ValidationStatus.PENDING);
        when(reEnrollmentValidationRepository.findByReEnrollmentId(1L))
                .thenReturn(Optional.of(existing));
        when(reEnrollmentValidationRepository.save(any())).thenReturn(existing);
        when(reenrollmentRepository.save(any())).thenReturn(re);

        ReEnrollment result = reenrollmentService.approve(1L, buildAdmin());

        assertThat(result.getStatus()).isEqualTo(ReEnrollment.ReEnrollmentStatus.VALIDATED);
        verify(reEnrollmentValidationRepository).save(any());
        verify(reenrollmentRepository).save(re);
    }

    @Test
    @DisplayName("approve - no existing validation creates new one")
    void approve_noExistingValidation_createsNew() {
        ReEnrollment re = buildReEnrollment(1L, ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(re));
        when(reEnrollmentValidationRepository.findByReEnrollmentId(1L))
                .thenReturn(Optional.empty());
        when(reEnrollmentValidationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reenrollmentRepository.save(any())).thenReturn(re);

        ReEnrollment result = reenrollmentService.approve(1L, buildAdmin());

        assertThat(result.getStatus()).isEqualTo(ReEnrollment.ReEnrollmentStatus.VALIDATED);
        verify(reEnrollmentValidationRepository).save(any());
    }

    @Test
    @DisplayName("approve - registrationStatusService throws, swallowed gracefully")
    void approve_registrationStatusThrows_swallowed() {
        ReEnrollment re = buildReEnrollment(1L, ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(re));
        when(reEnrollmentValidationRepository.findByReEnrollmentId(1L))
                .thenReturn(Optional.empty());
        when(reEnrollmentValidationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reenrollmentRepository.save(any())).thenReturn(re);
        doThrow(new RuntimeException("Status error"))
                .when(registrationStatusService).approveByAdmin(any(), any(), any(), any());

        // should not throw
        ReEnrollment result = reenrollmentService.approve(1L, buildAdmin());

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("approve - null studentUser skips registrationStatus update")
    void approve_nullStudentUser_skipsStatusUpdate() {
        Student student = Student.builder().id(10L).fullName("Budi").user(null).build();
        ReEnrollment re = new ReEnrollment();
        re.setId(1L);
        re.setStudent(student);
        re.setStatus(ReEnrollment.ReEnrollmentStatus.SUBMITTED);

        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(re));
        when(reEnrollmentValidationRepository.findByReEnrollmentId(1L))
                .thenReturn(Optional.empty());
        when(reEnrollmentValidationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reenrollmentRepository.save(any())).thenReturn(re);

        reenrollmentService.approve(1L, buildAdmin());

        verify(registrationStatusService, never()).approveByAdmin(any(), any(), any(), any());
    }

    // ===== reject =====

    @Test
    @DisplayName("reject - not found throws RuntimeException")
    void reject_notFound_throws() {
        when(reenrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reenrollmentService.reject(99L, "alasan", "topik", buildAdmin()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("tidak ditemukan");
    }

    @Test
    @DisplayName("reject - sets status to REJECTED with reason and topic")
    void reject_happyPath_setsRejected() {
        ReEnrollment re = buildReEnrollment(1L, ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(re));
        when(reEnrollmentValidationRepository.findByReEnrollmentId(1L))
                .thenReturn(Optional.empty());
        when(reEnrollmentValidationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reenrollmentRepository.save(any())).thenReturn(re);

        ReEnrollment result = reenrollmentService.reject(1L, "Dokumen kurang", "Administrasi", buildAdmin());

        assertThat(result.getStatus()).isEqualTo(ReEnrollment.ReEnrollmentStatus.REJECTED);
        verify(reEnrollmentValidationRepository).save(any());
    }

    @Test
    @DisplayName("reject - null reason defaults to empty string")
    void reject_nullReason_defaultsToEmpty() {
        ReEnrollment re = buildReEnrollment(1L, ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(re));
        when(reEnrollmentValidationRepository.findByReEnrollmentId(1L))
                .thenReturn(Optional.empty());
        when(reEnrollmentValidationRepository.save(any())).thenAnswer(inv -> {
            ReEnrollmentValidation v = inv.getArgument(0);
            assertThat(v.getRejectionReason()).isEqualTo("");
            return v;
        });
        when(reenrollmentRepository.save(any())).thenReturn(re);

        reenrollmentService.reject(1L, null, null, buildAdmin());
    }

    @Test
    @DisplayName("reject - null topic defaults to Lainnya")
    void reject_nullTopic_defaultsToLainnya() {
        ReEnrollment re = buildReEnrollment(1L, ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(re));
        when(reEnrollmentValidationRepository.findByReEnrollmentId(1L))
                .thenReturn(Optional.empty());
        when(reEnrollmentValidationRepository.save(any())).thenAnswer(inv -> {
            ReEnrollmentValidation v = inv.getArgument(0);
            assertThat(v.getRejectionTopic()).isEqualTo("Lainnya");
            return v;
        });
        when(reenrollmentRepository.save(any())).thenReturn(re);

        reenrollmentService.reject(1L, "alasan", null, buildAdmin());
    }

    @Test
    @DisplayName("reject - registrationStatusService throws, swallowed gracefully")
    void reject_registrationStatusThrows_swallowed() {
        ReEnrollment re = buildReEnrollment(1L, ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(re));
        when(reEnrollmentValidationRepository.findByReEnrollmentId(1L))
                .thenReturn(Optional.empty());
        when(reEnrollmentValidationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reenrollmentRepository.save(any())).thenReturn(re);
        doThrow(new RuntimeException("Status error"))
                .when(registrationStatusService).rejectByAdmin(any(), any(), any(), any());

        ReEnrollment result = reenrollmentService.reject(1L, "alasan", "topik", buildAdmin());

        assertThat(result).isNotNull();
    }

    // ===== validateDocument =====

    @Test
    @DisplayName("validateDocument - not found throws RuntimeException")
    void validateDocument_notFound_throws() {
        when(reenrollmentDocumentRepository.findById(99L)).thenReturn(Optional.empty());

        DocumentValidationRequest req = new DocumentValidationRequest();
        req.setAction("APPROVE");

        assertThatThrownBy(() -> reenrollmentService.validateDocument(99L, req, buildAdmin()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("tidak ditemukan");
    }

    @Test
    @DisplayName("validateDocument - APPROVE sets status APPROVED")
    void validateDocument_approve_setsApproved() {
        ReEnrollmentDocument doc = new ReEnrollmentDocument();
        doc.setId(1L);
        when(reenrollmentDocumentRepository.findById(1L)).thenReturn(Optional.of(doc));
        when(reenrollmentDocumentRepository.save(any())).thenReturn(doc);

        DocumentValidationRequest req = new DocumentValidationRequest();
        req.setAction("APPROVE");
        req.setAdminNotes("OK");

        ReEnrollmentDocument result = reenrollmentService.validateDocument(1L, req, buildAdmin());

        assertThat(result.getValidationStatus())
                .isEqualTo(ReEnrollmentDocument.ValidationStatus.APPROVED);
    }

    @Test
    @DisplayName("validateDocument - REJECT sets status REJECTED")
    void validateDocument_reject_setsRejected() {
        ReEnrollmentDocument doc = new ReEnrollmentDocument();
        doc.setId(2L);
        when(reenrollmentDocumentRepository.findById(2L)).thenReturn(Optional.of(doc));
        when(reenrollmentDocumentRepository.save(any())).thenReturn(doc);

        DocumentValidationRequest req = new DocumentValidationRequest();
        req.setAction("REJECT");
        req.setAdminNotes("Tidak valid");

        ReEnrollmentDocument result = reenrollmentService.validateDocument(2L, req, buildAdmin());

        assertThat(result.getValidationStatus())
                .isEqualTo(ReEnrollmentDocument.ValidationStatus.REJECTED);
    }

    @Test
    @DisplayName("validateDocument - REVISION_NEEDED sets status REVISION_NEEDED")
    void validateDocument_revisionNeeded_setsRevisionNeeded() {
        ReEnrollmentDocument doc = new ReEnrollmentDocument();
        doc.setId(3L);
        when(reenrollmentDocumentRepository.findById(3L)).thenReturn(Optional.of(doc));
        when(reenrollmentDocumentRepository.save(any())).thenReturn(doc);

        DocumentValidationRequest req = new DocumentValidationRequest();
        req.setAction("REVISION_NEEDED");

        ReEnrollmentDocument result = reenrollmentService.validateDocument(3L, req, buildAdmin());

        assertThat(result.getValidationStatus())
                .isEqualTo(ReEnrollmentDocument.ValidationStatus.REVISION_NEEDED);
    }

    @Test
    @DisplayName("validateDocument - invalid action throws RuntimeException")
    void validateDocument_invalidAction_throws() {
        ReEnrollmentDocument doc = new ReEnrollmentDocument();
        doc.setId(4L);
        when(reenrollmentDocumentRepository.findById(4L)).thenReturn(Optional.of(doc));

        DocumentValidationRequest req = new DocumentValidationRequest();
        req.setAction("INVALID_ACTION");

        assertThatThrownBy(() -> reenrollmentService.validateDocument(4L, req, buildAdmin()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Action tidak valid");
    }

    // ===== finalize =====

    @Test
    @DisplayName("finalize - not found throws RuntimeException")
    void finalize_notFound_throws() {
        when(reenrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        ReenrollmentFinalizeRequest req = new ReenrollmentFinalizeRequest();
        req.setAction("APPROVE");

        assertThatThrownBy(() -> reenrollmentService.finalize(99L, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("tidak ditemukan");
    }

    @Test
    @DisplayName("finalize - APPROVE with all docs approved sets VALIDATED")
    void finalize_approveAllDocsApproved_setsValidated() {
        ReEnrollmentDocument doc = new ReEnrollmentDocument();
        doc.setValidationStatus(ReEnrollmentDocument.ValidationStatus.APPROVED);

        ReEnrollment re = buildReEnrollment(1L, ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        re.setDocuments(List.of(doc));
        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(re));
        when(reenrollmentRepository.save(any())).thenReturn(re);

        ReenrollmentFinalizeRequest req = new ReenrollmentFinalizeRequest();
        req.setAction("APPROVE");
        req.setValidationNotes("Semua lengkap");

        ReEnrollment result = reenrollmentService.finalize(1L, req);

        assertThat(result.getStatus()).isEqualTo(ReEnrollment.ReEnrollmentStatus.VALIDATED);
    }

    @Test
    @DisplayName("finalize - APPROVE with not all docs approved throws RuntimeException")
    void finalize_approveNotAllDocsApproved_throws() {
        ReEnrollmentDocument doc = new ReEnrollmentDocument();
        doc.setValidationStatus(ReEnrollmentDocument.ValidationStatus.PENDING);

        ReEnrollment re = buildReEnrollment(1L, ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        re.setDocuments(List.of(doc));
        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(re));

        ReenrollmentFinalizeRequest req = new ReenrollmentFinalizeRequest();
        req.setAction("APPROVE");

        assertThatThrownBy(() -> reenrollmentService.finalize(1L, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Semua dokumen harus disetujui");
    }

    @Test
    @DisplayName("finalize - REJECT sets status REJECTED")
    void finalize_reject_setsRejected() {
        ReEnrollment re = buildReEnrollment(1L, ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        re.setDocuments(List.of());
        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(re));
        when(reenrollmentRepository.save(any())).thenReturn(re);

        ReenrollmentFinalizeRequest req = new ReenrollmentFinalizeRequest();
        req.setAction("REJECT");
        req.setValidationNotes("Tidak memenuhi syarat");

        ReEnrollment result = reenrollmentService.finalize(1L, req);

        assertThat(result.getStatus()).isEqualTo(ReEnrollment.ReEnrollmentStatus.REJECTED);
    }
}