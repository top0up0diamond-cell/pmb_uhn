package com.uhn.pmb.service;

import com.uhn.pmb.entity.RegistrationStatus;
import com.uhn.pmb.entity.RegistrationStatus.RegistrationStage;
import com.uhn.pmb.entity.RegistrationStatus.RegistrationStatus_Enum;
import com.uhn.pmb.entity.User;
import com.uhn.pmb.repository.RegistrationStatusRepository;
import com.uhn.pmb.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationStatusServiceTest {

    @Mock private RegistrationStatusRepository registrationStatusRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private RegistrationStatusService registrationStatusService;

    // ===== Helpers =====

    private User buildUser(Long id, String email) {
        return User.builder().id(id).email(email).build();
    }

    private RegistrationStatus buildStatus(User user, RegistrationStage stage,
                                           RegistrationStatus_Enum statusEnum) {
        return RegistrationStatus.builder()
                .id(1L)
                .user(user)
                .stage(stage)
                .status(statusEnum)
                .canEdit(true)
                .adminVerified(false)
                .editCount(0)
                .build();
    }

    private void mockFindByEmail(String email, User user) {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    }

    // ===== getUserByEmail =====

    @Test
    @DisplayName("getUserByEmail - found returns user")
    void getUserByEmail_found_returnsUser() {
        User user = buildUser(1L, "u@test.com");
        mockFindByEmail("u@test.com", user);

        User result = registrationStatusService.getUserByEmail("u@test.com");

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getUserByEmail - not found throws RuntimeException")
    void getUserByEmail_notFound_throwsException() {
        when(userRepository.findByEmail("bad@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationStatusService.getUserByEmail("bad@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User tidak ditemukan");
    }

    // ===== getUserStatusesByEmail =====

    @Test
    @DisplayName("getUserStatusesByEmail - user not found throws RuntimeException")
    void getUserStatusesByEmail_userNotFound_throws() {
        when(userRepository.findByEmail("bad@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationStatusService.getUserStatusesByEmail("bad@test.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getUserStatusesByEmail - delegates to getUserStatuses with resolved user")
    void getUserStatusesByEmail_found_returnsStatuses() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.SELESAI);
        mockFindByEmail("u@test.com", user);
        when(registrationStatusRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(s));

        List<RegistrationStatus> result = registrationStatusService.getUserStatusesByEmail("u@test.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStage()).isEqualTo(RegistrationStage.FORM_SUBMISSION);
    }

    // ===== getStatusByEmail =====

    @Test
    @DisplayName("getStatusByEmail - user not found throws RuntimeException")
    void getStatusByEmail_userNotFound_throws() {
        when(userRepository.findByEmail("bad@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationStatusService.getStatusByEmail(
                "bad@test.com", RegistrationStage.FORM_SUBMISSION))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getStatusByEmail - found returns Optional with status")
    void getStatusByEmail_found_returnsOptional() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.SELESAI);
        mockFindByEmail("u@test.com", user);
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(s));

        Optional<RegistrationStatus> result = registrationStatusService.getStatusByEmail(
                "u@test.com", RegistrationStage.FORM_SUBMISSION);

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("getStatusByEmail - not found returns empty Optional")
    void getStatusByEmail_notFound_returnsEmpty() {
        User user = buildUser(1L, "u@test.com");
        mockFindByEmail("u@test.com", user);
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.empty());

        Optional<RegistrationStatus> result = registrationStatusService.getStatusByEmail(
                "u@test.com", RegistrationStage.FORM_SUBMISSION);

        assertThat(result).isEmpty();
    }

    // ===== canUserEditByEmail =====

    @Test
    @DisplayName("canUserEditByEmail - delegates to canUserEdit with resolved user")
    void canUserEditByEmail_delegatesToCore() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.MENUNGGU_VERIFIKASI);
        mockFindByEmail("u@test.com", user);
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(s));

        boolean result = registrationStatusService.canUserEditByEmail(
                "u@test.com", RegistrationStage.FORM_SUBMISSION);

        assertThat(result).isTrue();
    }

    // ===== getEditTimeRemainingByEmail =====

    @Test
    @DisplayName("getEditTimeRemainingByEmail - delegates to getEditTimeRemaining with resolved user")
    void getEditTimeRemainingByEmail_delegatesToCore() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.SELESAI);
        s.setEditDeadline(LocalDateTime.now().plusHours(12));
        mockFindByEmail("u@test.com", user);
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(s));

        Long result = registrationStatusService.getEditTimeRemainingByEmail(
                "u@test.com", RegistrationStage.FORM_SUBMISSION);

        assertThat(result).isGreaterThan(0L);
    }

    // ===== markAsCompletedByEmail =====

    @Test
    @DisplayName("markAsCompletedByEmail - delegates to markAsCompleted with resolved user")
    void markAsCompletedByEmail_delegatesToCore() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.MENUNGGU_VERIFIKASI);
        mockFindByEmail("u@test.com", user);
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(s));
        when(registrationStatusRepository.save(any())).thenReturn(s);

        RegistrationStatus result = registrationStatusService.markAsCompletedByEmail(
                "u@test.com", RegistrationStage.FORM_SUBMISSION, "{\"key\":\"val\"}");

        assertThat(result.getStatus()).isEqualTo(RegistrationStatus_Enum.SELESAI);
    }

    // ===== updateStatusDataByEmail =====

    @Test
    @DisplayName("updateStatusDataByEmail - delegates to updateStatusData with resolved user")
    void updateStatusDataByEmail_delegatesToCore() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.MENUNGGU_VERIFIKASI);
        mockFindByEmail("u@test.com", user);
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(s));
        when(registrationStatusRepository.save(any())).thenReturn(s);

        RegistrationStatus result = registrationStatusService.updateStatusDataByEmail(
                "u@test.com", RegistrationStage.FORM_SUBMISSION, "{\"updated\":true}");

        assertThat(result.getDataJson()).isEqualTo("{\"updated\":true}");
    }

    // ===== getOrCreateStatus =====

    @Test
    @DisplayName("getOrCreateStatus - existing status is returned without saving")
    void getOrCreateStatus_existing_returnsWithoutSave() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.SELESAI);
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(s));

        RegistrationStatus result = registrationStatusService.getOrCreateStatus(
                user, RegistrationStage.FORM_SUBMISSION);

        assertThat(result.getId()).isEqualTo(1L);
        verify(registrationStatusRepository, never()).save(any());
    }

    @Test
    @DisplayName("getOrCreateStatus - no existing status creates new with MENUNGGU_VERIFIKASI")
    void getOrCreateStatus_notExisting_createsNew() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.MENUNGGU_VERIFIKASI);
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.empty());
        when(registrationStatusRepository.save(any())).thenReturn(s);

        RegistrationStatus result = registrationStatusService.getOrCreateStatus(
                user, RegistrationStage.FORM_SUBMISSION);

        assertThat(result.getStatus()).isEqualTo(RegistrationStatus_Enum.MENUNGGU_VERIFIKASI);
        assertThat(result.getCanEdit()).isTrue();
        assertThat(result.getAdminVerified()).isFalse();
        assertThat(result.getEditCount()).isEqualTo(0);
        verify(registrationStatusRepository).save(any());
    }

    // ===== markAsCompleted =====

    @Test
    @DisplayName("markAsCompleted - sets SELESAI status and 24h edit deadline")
    void markAsCompleted_setsStatusAndDeadline() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.MENUNGGU_VERIFIKASI);
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(s));
        when(registrationStatusRepository.save(any())).thenReturn(s);

        RegistrationStatus result = registrationStatusService.markAsCompleted(
                user, RegistrationStage.FORM_SUBMISSION, "{\"data\":1}");

        assertThat(result.getStatus()).isEqualTo(RegistrationStatus_Enum.SELESAI);
        assertThat(result.getSubmissionDate()).isNotNull();
        assertThat(result.getEditDeadline()).isAfter(LocalDateTime.now());
        assertThat(result.getCanEdit()).isTrue();
        assertThat(result.getDataJson()).isEqualTo("{\"data\":1}");
    }

    @Test
    @DisplayName("markAsCompleted - increments editCount from existing value")
    void markAsCompleted_incrementsEditCount() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.MENUNGGU_VERIFIKASI);
        s.setEditCount(3);
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(s));
        when(registrationStatusRepository.save(any())).thenReturn(s);

        RegistrationStatus result = registrationStatusService.markAsCompleted(
                user, RegistrationStage.FORM_SUBMISSION, null);

        assertThat(result.getEditCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("markAsCompleted - null editCount treated as 0 before incrementing")
    void markAsCompleted_nullEditCount_treatedAsZero() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.MENUNGGU_VERIFIKASI);
        s.setEditCount(null);
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(s));
        when(registrationStatusRepository.save(any())).thenReturn(s);

        RegistrationStatus result = registrationStatusService.markAsCompleted(
                user, RegistrationStage.FORM_SUBMISSION, null);

        assertThat(result.getEditCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("markAsCompleted - editDeadline is approximately 24h from now")
    void markAsCompleted_editDeadlineIs24hFromNow() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.MENUNGGU_VERIFIKASI);
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(s));
        when(registrationStatusRepository.save(any())).thenReturn(s);

        RegistrationStatus result = registrationStatusService.markAsCompleted(
                user, RegistrationStage.FORM_SUBMISSION, null);

        LocalDateTime expectedDeadline = LocalDateTime.now().plusHours(24);
        // Allow 5 seconds tolerance for test execution time
        assertThat(result.getEditDeadline()).isBetween(
                expectedDeadline.minusSeconds(5), expectedDeadline.plusSeconds(5));
    }

    // ===== canUserEdit =====

    @Test
    @DisplayName("canUserEdit - no status found returns false")
    void canUserEdit_noStatus_returnsFalse() {
        User user = buildUser(1L, "u@test.com");
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.empty());

        boolean result = registrationStatusService.canUserEdit(user, RegistrationStage.FORM_SUBMISSION);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("canUserEdit - adminVerified=true returns false regardless of status")
    void canUserEdit_adminVerified_returnsFalse() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.SELESAI);
        s.setAdminVerified(true);
        s.setEditDeadline(LocalDateTime.now().plusHours(10));
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(s));

        boolean result = registrationStatusService.canUserEdit(user, RegistrationStage.FORM_SUBMISSION);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("canUserEdit - MENUNGGU_VERIFIKASI and not adminVerified returns true")
    void canUserEdit_menungguVerifikasi_notAdminVerified_returnsTrue() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.MENUNGGU_VERIFIKASI);
        s.setAdminVerified(false);
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(s));

        boolean result = registrationStatusService.canUserEdit(user, RegistrationStage.FORM_SUBMISSION);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("canUserEdit - SELESAI with future editDeadline returns true")
    void canUserEdit_selesai_futureDeadline_returnsTrue() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.SELESAI);
        s.setEditDeadline(LocalDateTime.now().plusHours(5));
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(s));

        boolean result = registrationStatusService.canUserEdit(user, RegistrationStage.FORM_SUBMISSION);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("canUserEdit - SELESAI with past editDeadline returns false")
    void canUserEdit_selesai_pastDeadline_returnsFalse() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.SELESAI);
        s.setEditDeadline(LocalDateTime.now().minusHours(1));
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(s));

        boolean result = registrationStatusService.canUserEdit(user, RegistrationStage.FORM_SUBMISSION);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("canUserEdit - SELESAI with null editDeadline returns false")
    void canUserEdit_selesai_nullDeadline_returnsFalse() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.SELESAI);
        s.setEditDeadline(null);
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(s));

        boolean result = registrationStatusService.canUserEdit(user, RegistrationStage.FORM_SUBMISSION);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("canUserEdit - REJECTED status returns false")
    void canUserEdit_rejectedStatus_returnsFalse() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.REJECTED);
        s.setAdminVerified(false);
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(s));

        boolean result = registrationStatusService.canUserEdit(user, RegistrationStage.FORM_SUBMISSION);

        assertThat(result).isFalse();
    }

    // ===== getEditTimeRemaining =====

    @Test
    @DisplayName("getEditTimeRemaining - no status found returns 0")
    void getEditTimeRemaining_noStatus_returnsZero() {
        User user = buildUser(1L, "u@test.com");
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.empty());

        Long result = registrationStatusService.getEditTimeRemaining(user, RegistrationStage.FORM_SUBMISSION);

        assertThat(result).isEqualTo(0L);
    }

    @Test
    @DisplayName("getEditTimeRemaining - null editDeadline returns 0")
    void getEditTimeRemaining_nullDeadline_returnsZero() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.SELESAI);
        s.setEditDeadline(null);
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(s));

        Long result = registrationStatusService.getEditTimeRemaining(user, RegistrationStage.FORM_SUBMISSION);

        assertThat(result).isEqualTo(0L);
    }

    @Test
    @DisplayName("getEditTimeRemaining - future deadline returns positive hours remaining")
    void getEditTimeRemaining_futureDeadline_returnsPositiveHours() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.SELESAI);
        s.setEditDeadline(LocalDateTime.now().plusHours(10));
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(s));

        Long result = registrationStatusService.getEditTimeRemaining(user, RegistrationStage.FORM_SUBMISSION);

        assertThat(result).isGreaterThan(0L);
        assertThat(result).isLessThanOrEqualTo(10L);
    }

    @Test
    @DisplayName("getEditTimeRemaining - past deadline returns 0")
    void getEditTimeRemaining_pastDeadline_returnsZero() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.SELESAI);
        s.setEditDeadline(LocalDateTime.now().minusHours(5));
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(s));

        Long result = registrationStatusService.getEditTimeRemaining(user, RegistrationStage.FORM_SUBMISSION);

        assertThat(result).isEqualTo(0L);
    }

    // ===== updateStatusData =====

    @Test
    @DisplayName("updateStatusData - canUserEdit=false throws IllegalStateException")
    void updateStatusData_cannotEdit_throwsException() {
        User user = buildUser(1L, "u@test.com");
        // No status → canUserEdit returns false
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationStatusService.updateStatusData(
                user, RegistrationStage.FORM_SUBMISSION, "{\"new\":\"data\"}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tidak bisa edit");
    }

    @Test
    @DisplayName("updateStatusData - status not found after canEdit check throws IllegalStateException")
    void updateStatusData_adminVerified_throwsException() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.SELESAI);
        s.setAdminVerified(true);
        // canUserEdit returns false because adminVerified=true
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(s));

        assertThatThrownBy(() -> registrationStatusService.updateStatusData(
                user, RegistrationStage.FORM_SUBMISSION, "{}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("updateStatusData - updates dataJson, updatedAt, and increments editCount")
    void updateStatusData_validEdit_updatesFields() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.MENUNGGU_VERIFIKASI);
        s.setEditCount(2);
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(s));
        when(registrationStatusRepository.save(any())).thenReturn(s);

        RegistrationStatus result = registrationStatusService.updateStatusData(
                user, RegistrationStage.FORM_SUBMISSION, "{\"updated\":true}");

        assertThat(result.getDataJson()).isEqualTo("{\"updated\":true}");
        assertThat(result.getUpdatedAt()).isNotNull();
        assertThat(result.getEditCount()).isEqualTo(3);
        verify(registrationStatusRepository).save(any());
    }

    @Test
    @DisplayName("updateStatusData - null editCount treated as 0 before incrementing")
    void updateStatusData_nullEditCount_treatedAsZero() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.MENUNGGU_VERIFIKASI);
        s.setEditCount(null);
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(s));
        when(registrationStatusRepository.save(any())).thenReturn(s);

        RegistrationStatus result = registrationStatusService.updateStatusData(
                user, RegistrationStage.FORM_SUBMISSION, "{}");

        assertThat(result.getEditCount()).isEqualTo(1);
    }

    // ===== rejectByAdmin =====

    @Test
    @DisplayName("rejectByAdmin - status not found throws IllegalStateException")
    void rejectByAdmin_statusNotFound_throwsException() {
        User user = buildUser(1L, "u@test.com");
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationStatusService.rejectByAdmin(
                user, RegistrationStage.FORM_SUBMISSION, "admin@test.com", "Tidak lengkap"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Status tidak ditemukan");
    }

    @Test
    @DisplayName("rejectByAdmin - sets REJECTED status with admin info and disables editing")
    void rejectByAdmin_found_setsRejectedStatus() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.SELESAI);
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(s));
        when(registrationStatusRepository.save(any())).thenReturn(s);

        RegistrationStatus result = registrationStatusService.rejectByAdmin(
                user, RegistrationStage.FORM_SUBMISSION, "admin@test.com", "Dokumen tidak valid");

        assertThat(result.getStatus()).isEqualTo(RegistrationStatus_Enum.REJECTED);
        assertThat(result.getAdminVerified()).isTrue();
        assertThat(result.getVerifiedBy()).isEqualTo("admin@test.com");
        assertThat(result.getAdminNotes()).isEqualTo("Dokumen tidak valid");
        assertThat(result.getCanEdit()).isFalse();
        assertThat(result.getVerificationDate()).isNotNull();
        verify(registrationStatusRepository).save(any());
    }

    // ===== approveByAdmin =====

    @Test
    @DisplayName("approveByAdmin - status not found throws IllegalStateException")
    void approveByAdmin_statusNotFound_throwsException() {
        User user = buildUser(1L, "u@test.com");
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationStatusService.approveByAdmin(
                user, RegistrationStage.FORM_SUBMISSION, "admin@test.com", "OK"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Status tidak ditemukan");
    }

    @Test
    @DisplayName("approveByAdmin - sets adminVerified and admin info, disables editing")
    void approveByAdmin_found_setsApprovedFields() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.SELESAI);
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(s));
        when(registrationStatusRepository.save(any())).thenReturn(s);

        RegistrationStatus result = registrationStatusService.approveByAdmin(
                user, RegistrationStage.FORM_SUBMISSION, "admin@test.com", "Semua lengkap");

        assertThat(result.getAdminVerified()).isTrue();
        assertThat(result.getVerifiedBy()).isEqualTo("admin@test.com");
        assertThat(result.getAdminNotes()).isEqualTo("Semua lengkap");
        assertThat(result.getCanEdit()).isFalse();
        assertThat(result.getVerificationDate()).isNotNull();
        // approveByAdmin does NOT change status enum (unlike rejectByAdmin)
        assertThat(result.getStatus()).isEqualTo(RegistrationStatus_Enum.SELESAI);
        verify(registrationStatusRepository).save(any());
    }

    @Test
    @DisplayName("approveByAdmin - does not set REJECTED status")
    void approveByAdmin_doesNotSetRejectedStatus() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.MENUNGGU_VERIFIKASI);
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(s));
        when(registrationStatusRepository.save(any())).thenReturn(s);

        RegistrationStatus result = registrationStatusService.approveByAdmin(
                user, RegistrationStage.FORM_SUBMISSION, "admin@test.com", "OK");

        assertThat(result.getStatus()).isNotEqualTo(RegistrationStatus_Enum.REJECTED);
    }

    // ===== getUserStatuses =====

    @Test
    @DisplayName("getUserStatuses - returns list from repository ordered by createdAt desc")
    void getUserStatuses_returnsList() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s1 = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.SELESAI);
        when(registrationStatusRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(s1));

        List<RegistrationStatus> result = registrationStatusService.getUserStatuses(user);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getUserStatuses - empty returns empty list")
    void getUserStatuses_empty_returnsEmptyList() {
        User user = buildUser(1L, "u@test.com");
        when(registrationStatusRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of());

        List<RegistrationStatus> result = registrationStatusService.getUserStatuses(user);

        assertThat(result).isEmpty();
    }

    // ===== getStatus =====

    @Test
    @DisplayName("getStatus - found returns Optional with status")
    void getStatus_found_returnsOptional() {
        User user = buildUser(1L, "u@test.com");
        RegistrationStatus s = buildStatus(user, RegistrationStage.FORM_SUBMISSION,
                RegistrationStatus_Enum.SELESAI);
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(s));

        Optional<RegistrationStatus> result = registrationStatusService.getStatus(
                user, RegistrationStage.FORM_SUBMISSION);

        assertThat(result).isPresent();
        assertThat(result.get().getStage()).isEqualTo(RegistrationStage.FORM_SUBMISSION);
    }

    @Test
    @DisplayName("getStatus - not found returns empty Optional")
    void getStatus_notFound_returnsEmpty() {
        User user = buildUser(1L, "u@test.com");
        when(registrationStatusRepository.findByUserAndStage(user, RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.empty());

        Optional<RegistrationStatus> result = registrationStatusService.getStatus(
                user, RegistrationStage.FORM_SUBMISSION);

        assertThat(result).isEmpty();
    }
}