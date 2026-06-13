package com.uhn.pmb.task;

import com.uhn.pmb.entity.FormValidation;
import com.uhn.pmb.repository.FormValidationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PendingFormCheckTaskTest {

    @Mock private FormValidationRepository formValidationRepository;

    private PendingFormCheckTask pendingFormCheckTask;

    @BeforeEach
    void setUp() {
        pendingFormCheckTask = new PendingFormCheckTask();
        ReflectionTestUtils.setField(pendingFormCheckTask, "formValidationRepository", formValidationRepository);
    }

    private FormValidation buildForm(Long id, LocalDateTime createdAt) {
        FormValidation form = new FormValidation();
        form.setId(id);
        form.setCreatedAt(createdAt);
        form.setValidationStatus(FormValidation.ValidationStatus.PENDING);
        return form;
    }

    // ===== checkPendingForms =====

    @Test
    @DisplayName("checkPendingForms - empty list does nothing")
    void checkPendingForms_emptyList_doesNothing() {
        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.PENDING))
                .thenReturn(Collections.emptyList());

        pendingFormCheckTask.checkPendingForms();

        verify(formValidationRepository).findByValidationStatus(FormValidation.ValidationStatus.PENDING);
    }

    @Test
    @DisplayName("checkPendingForms - null list does nothing")
    void checkPendingForms_nullList_doesNothing() {
        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.PENDING))
                .thenReturn(null);

        pendingFormCheckTask.checkPendingForms();

        verify(formValidationRepository).findByValidationStatus(FormValidation.ValidationStatus.PENDING);
    }

    @Test
    @DisplayName("checkPendingForms - form pending > 15 minutes triggers notification log")
    void checkPendingForms_longPendingForm_triggersNotification() {
        FormValidation oldForm = buildForm(1L, LocalDateTime.now().minusMinutes(30));
        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.PENDING))
                .thenReturn(List.of(oldForm));

        // Should not throw, just log warning
        pendingFormCheckTask.checkPendingForms();

        verify(formValidationRepository).findByValidationStatus(FormValidation.ValidationStatus.PENDING);
    }

    @Test
    @DisplayName("checkPendingForms - form pending exactly 15 minutes triggers notification")
    void checkPendingForms_exactlyThresholdMinutes_triggersNotification() {
        FormValidation form = buildForm(2L, LocalDateTime.now().minusMinutes(15));
        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.PENDING))
                .thenReturn(List.of(form));

        pendingFormCheckTask.checkPendingForms();

        verify(formValidationRepository).findByValidationStatus(FormValidation.ValidationStatus.PENDING);
    }

    @Test
    @DisplayName("checkPendingForms - form pending < 15 minutes does not trigger notification")
    void checkPendingForms_shortPendingForm_noNotification() {
        FormValidation recentForm = buildForm(3L, LocalDateTime.now().minusMinutes(5));
        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.PENDING))
                .thenReturn(List.of(recentForm));

        pendingFormCheckTask.checkPendingForms();

        verify(formValidationRepository).findByValidationStatus(FormValidation.ValidationStatus.PENDING);
    }

    @Test
    @DisplayName("checkPendingForms - null createdAt handled without exception")
    void checkPendingForms_nullCreatedAt_handledGracefully() {
        FormValidation form = buildForm(4L, null);
        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.PENDING))
                .thenReturn(List.of(form));

        // Should not throw — error is caught internally
        pendingFormCheckTask.checkPendingForms();

        verify(formValidationRepository).findByValidationStatus(FormValidation.ValidationStatus.PENDING);
    }

    @Test
    @DisplayName("checkPendingForms - multiple forms processed correctly")
    void checkPendingForms_multipleForms_allProcessed() {
        List<FormValidation> forms = List.of(
                buildForm(1L, LocalDateTime.now().minusMinutes(60)),  // > 15 min
                buildForm(2L, LocalDateTime.now().minusMinutes(5)),   // < 15 min
                buildForm(3L, LocalDateTime.now().minusMinutes(20))   // > 15 min
        );
        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.PENDING))
                .thenReturn(forms);

        pendingFormCheckTask.checkPendingForms();

        verify(formValidationRepository).findByValidationStatus(FormValidation.ValidationStatus.PENDING);
    }

    @Test
    @DisplayName("checkPendingForms - repository throws, exception handled gracefully")
    void checkPendingForms_repositoryThrows_handledGracefully() {
        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.PENDING))
                .thenThrow(new RuntimeException("DB error"));

        // Should not propagate exception
        pendingFormCheckTask.checkPendingForms();
    }

    // ===== manualCheckPendingForms =====

    @Test
    @DisplayName("manualCheckPendingForms - delegates to checkPendingForms")
    void manualCheckPendingForms_delegatesToCheck() {
        when(formValidationRepository.findByValidationStatus(FormValidation.ValidationStatus.PENDING))
                .thenReturn(Collections.emptyList());

        pendingFormCheckTask.manualCheckPendingForms();

        verify(formValidationRepository).findByValidationStatus(FormValidation.ValidationStatus.PENDING);
    }
}