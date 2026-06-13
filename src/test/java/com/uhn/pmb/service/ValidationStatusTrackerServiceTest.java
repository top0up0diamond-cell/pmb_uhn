package com.uhn.pmb.service;

import com.uhn.pmb.entity.AdmissionForm;
import com.uhn.pmb.entity.Student;
import com.uhn.pmb.entity.User;
import com.uhn.pmb.entity.ValidationStatusTracker;
import com.uhn.pmb.repository.AdmissionFormRepository;
import com.uhn.pmb.repository.StudentRepository;
import com.uhn.pmb.repository.ValidationStatusTrackerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidationStatusTrackerServiceTest {

    @Mock private ValidationStatusTrackerRepository validationStatusTrackerRepository;
    @Mock private AdmissionFormRepository admissionFormRepository;
    @Mock private StudentRepository studentRepository;

    @InjectMocks
    private ValidationStatusTrackerService validationStatusTrackerService;

    @Test
    @DisplayName("getOrCreateTracker - existing returns existing tracker")
    void getOrCreateTracker_existing_returnsExisting() {
        ValidationStatusTracker tracker = ValidationStatusTracker.builder().id(1L).build();
        when(validationStatusTrackerRepository.findByAdmissionFormId(1L)).thenReturn(Optional.of(tracker));

        ValidationStatusTracker result = validationStatusTrackerService.getOrCreateTracker(1L);

        assertThat(result.getId()).isEqualTo(1L);
        verify(admissionFormRepository, never()).findById(any());
    }

    @Test
    @DisplayName("getOrCreateTracker - not found creates new tracker")
    void getOrCreateTracker_notExisting_createsNew() {
        when(validationStatusTrackerRepository.findByAdmissionFormId(1L)).thenReturn(Optional.empty());
        AdmissionForm form = AdmissionForm.builder()
                .id(1L)
                .student(Student.builder().id(1L).build())
                .build();
        when(admissionFormRepository.findById(1L)).thenReturn(Optional.of(form));
        ValidationStatusTracker saved = ValidationStatusTracker.builder().id(2L).build();
        when(validationStatusTrackerRepository.save(any())).thenReturn(saved);

        ValidationStatusTracker result = validationStatusTrackerService.getOrCreateTracker(1L);

        assertThat(result).isNotNull();
        verify(validationStatusTrackerRepository).save(any());
    }

    @Test
    @DisplayName("getOrCreateTracker - form not found throws RuntimeException")
    void getOrCreateTracker_formNotFound_throws() {
        when(validationStatusTrackerRepository.findByAdmissionFormId(99L)).thenReturn(Optional.empty());
        when(admissionFormRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validationStatusTrackerService.getOrCreateTracker(99L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getTrackerByFormId - returns optional")
    void getTrackerByFormId_found_returnsPresent() {
        ValidationStatusTracker tracker = new ValidationStatusTracker();
        when(validationStatusTrackerRepository.findByAdmissionFormId(1L)).thenReturn(Optional.of(tracker));

        Optional<ValidationStatusTracker> result = validationStatusTrackerService.getTrackerByFormId(1L);

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("getTrackerByStudentId - returns optional")
    void getTrackerByStudentId_found_returnsPresent() {
        ValidationStatusTracker tracker = new ValidationStatusTracker();
        when(validationStatusTrackerRepository.findByStudentId(1L)).thenReturn(Optional.of(tracker));

        Optional<ValidationStatusTracker> result = validationStatusTrackerService.getTrackerByStudentId(1L);

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("updateStatusToMenunggu - creates tracker and saves")
    void updateStatusToMenunggu_savesStatus() {
        when(validationStatusTrackerRepository.findByAdmissionFormId(1L)).thenReturn(Optional.empty());
        AdmissionForm form = AdmissionForm.builder()
                .id(1L)
                .student(Student.builder().id(1L).build())
                .build();
        when(admissionFormRepository.findById(1L)).thenReturn(Optional.of(form));
        ValidationStatusTracker saved = new ValidationStatusTracker();
        when(validationStatusTrackerRepository.save(any())).thenReturn(saved);

        validationStatusTrackerService.updateStatusToMenunggu(1L);

        verify(validationStatusTrackerRepository, atLeastOnce()).save(any());
    }

    @Test
    @DisplayName("getTrackerByFormId - not found returns empty optional")
    void getTrackerByFormId_notFound_returnsEmpty() {
        when(validationStatusTrackerRepository.findByAdmissionFormId(999L)).thenReturn(Optional.empty());

        Optional<ValidationStatusTracker> result = validationStatusTrackerService.getTrackerByFormId(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getTrackerByStudentId - not found returns empty optional")
    void getTrackerByStudentId_notFound_returnsEmpty() {
        when(validationStatusTrackerRepository.findByStudentId(999L)).thenReturn(Optional.empty());

        Optional<ValidationStatusTracker> result = validationStatusTrackerService.getTrackerByStudentId(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("updateStatusToDivalidasi - updates tracker with divalidasi status")
    void updateStatusToDivalidasi_updatesStatus() {
        ValidationStatusTracker tracker = new ValidationStatusTracker();
        when(validationStatusTrackerRepository.findByAdmissionFormId(1L)).thenReturn(Optional.of(tracker));
        when(validationStatusTrackerRepository.save(any())).thenReturn(tracker);
        User user = User.builder().id(1L).build();

        validationStatusTrackerService.updateStatusToDivalidasi(1L, user);

        verify(validationStatusTrackerRepository).save(argThat(t -> 
            t.getStatus() == ValidationStatusTracker.ValidationStatusEnum.DIVALIDASI &&
            t.getLastAction().equals("APPROVED")
        ));
    }

    @Test
    @DisplayName("updateStatusToDitolak - updates tracker with ditolak status")
    void updateStatusToDitolak_updatesStatus() {
        ValidationStatusTracker tracker = new ValidationStatusTracker();
        when(validationStatusTrackerRepository.findByAdmissionFormId(1L)).thenReturn(Optional.of(tracker));
        when(validationStatusTrackerRepository.save(any())).thenReturn(tracker);
        User user = User.builder().id(1L).build();

        validationStatusTrackerService.updateStatusToDitolak(1L, "Data tidak sesuai", user);

        verify(validationStatusTrackerRepository).save(argThat(t ->
            t.getStatus() == ValidationStatusTracker.ValidationStatusEnum.DITOLAK &&
            t.getLastAction().equals("REJECTED")
        ));
    }

    @Test
    @DisplayName("updateStatusToRevisi - updates tracker with revisi status")
    void updateStatusToRevisi_updatesStatus() {
        ValidationStatusTracker tracker = new ValidationStatusTracker();
        when(validationStatusTrackerRepository.findByAdmissionFormId(1L)).thenReturn(Optional.of(tracker));
        when(validationStatusTrackerRepository.save(any())).thenReturn(tracker);
        User user = User.builder().id(1L).build();

        validationStatusTrackerService.updateStatusToRevisi(1L, "Mohon perbaiki data", user);

        verify(validationStatusTrackerRepository).save(argThat(t ->
            t.getStatus() == ValidationStatusTracker.ValidationStatusEnum.REVISI &&
            t.getLastAction().equals("REVISION_REQUESTED")
        ));
    }

    @Test
    @DisplayName("updateStatusToMenunggu - existing tracker updates status")
    void updateStatusToMenunggu_existingTracker_updatesStatus() {
        ValidationStatusTracker tracker = new ValidationStatusTracker();
        when(validationStatusTrackerRepository.findByAdmissionFormId(1L)).thenReturn(Optional.of(tracker));
        when(validationStatusTrackerRepository.save(any())).thenReturn(tracker);

        validationStatusTrackerService.updateStatusToMenunggu(1L);

        verify(validationStatusTrackerRepository).save(argThat(t ->
            t.getStatus() == ValidationStatusTracker.ValidationStatusEnum.MENUNGGU &&
            t.getLastAction().equals("PAYMENT_VERIFIED")
        ));
    }

}
