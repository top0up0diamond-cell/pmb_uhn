package com.uhn.pmb.service;

import com.uhn.pmb.dto.CreateExamLinkRequest;
import com.uhn.pmb.dto.ExamValidationRequest;
import com.uhn.pmb.entity.*;
import com.uhn.pmb.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
class ExamServiceTest {

    @Mock private ExamLinkRepository examLinkRepository;
    @Mock private ExamResultRepository examResultRepository;
    @Mock private RegistrationPeriodRepository registrationPeriodRepository;
    @Mock private SelectionTypeRepository selectionTypeRepository;
    @Mock private FormValidationRepository formValidationRepository;
    @Mock private FormRepairStatusRepository formRepairStatusRepository;
    @Mock private AdmissionFormRepository admissionFormRepository;

    @InjectMocks
    private ExamService examService;

    @Test
    @DisplayName("createLink - invalid URL format throws exception")
    void createLink_invalidUrl_throwsException() {
        CreateExamLinkRequest req = new CreateExamLinkRequest();
        req.setPeriodId(1L);
        req.setLinkUrl("https://example.com/exam");
        req.setLinkTitle("Test Exam");
        RegistrationPeriod period = new RegistrationPeriod();
        period.setId(1L);
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));

        assertThatThrownBy(() -> examService.createLink(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Google Form");
    }

    @Test
    @DisplayName("createLink - valid Google Form URL creates link")
    void createLink_validGoogleFormUrl_createsLink() {
        CreateExamLinkRequest req = new CreateExamLinkRequest();
        req.setPeriodId(1L);
        req.setLinkUrl("https://forms.google.com/form/123");
        req.setLinkTitle("Test Exam");
        RegistrationPeriod period = new RegistrationPeriod();
        period.setId(1L);
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(examLinkRepository.save(any(ExamLink.class))).thenAnswer(i -> i.getArgument(0));

        ExamLink result = examService.createLink(req);

        assertThat(result).isNotNull();
        assertThat(result.getLinkTitle()).isEqualTo("Test Exam");
    }

    @Test
    @DisplayName("createLink - period not found throws exception")
    void createLink_periodNotFound_throwsException() {
        CreateExamLinkRequest req = new CreateExamLinkRequest();
        req.setPeriodId(999L);
        req.setLinkUrl("https://forms.google.com/123");
        when(registrationPeriodRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> examService.createLink(req))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("findLinksByPeriod - returns exam links")
    void findLinksByPeriod_returnsLinks() {
        ExamLink link = new ExamLink();
        when(examLinkRepository.findByPeriodId(1L)).thenReturn(List.of(link));

        List<ExamLink> result = examService.findLinksByPeriod(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("deleteLink - existing link deleted")
    void deleteLink_existing_deleted() {
        ExamLink link = new ExamLink();
        link.setLinkTitle("Old Link");
        when(examLinkRepository.findById(1L)).thenReturn(Optional.of(link));

        examService.deleteLink(1L);

        verify(examLinkRepository).delete(link);
    }

    @Test
    @DisplayName("deleteLink - not found throws exception")
    void deleteLink_notFound_throwsException() {
        when(examLinkRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> examService.deleteLink(999L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("validateSubmission - APPROVE without token throws exception")
    void validateSubmission_approveWithoutToken_throwsException() {
        ExamResult result = new ExamResult();
        result.setTokenValidated(false);
        ExamValidationRequest req = new ExamValidationRequest();
        req.setAction("APPROVE");
        User admin = User.builder().email("admin@test.com").build();
        when(examResultRepository.findById(1L)).thenReturn(Optional.of(result));

        assertThatThrownBy(() -> examService.validateSubmission(1L, req, admin))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Token");
    }

    @Test
    @DisplayName("validateSubmission - REJECT sets status to REJECTED")
    void validateSubmission_reject_setsRejected() {
        ExamResult result = new ExamResult();
        result.setTokenValidated(true);
        ExamValidationRequest req = new ExamValidationRequest();
        req.setAction("REJECT");
        User admin = User.builder().email("admin@test.com").build();
        when(examResultRepository.findById(1L)).thenReturn(Optional.of(result));
        when(examResultRepository.save(any())).thenReturn(result);

        examService.validateSubmission(1L, req, admin);

        assertThat(result.getExamValidationStatus()).isEqualTo(ExamResult.ExamValidationStatus.REJECTED);
    }

    @Test
    @DisplayName("findResultByStudentId - returns exam result")
    void findResultByStudentId_found_returnsResult() {
        ExamResult result = new ExamResult();
        when(examResultRepository.findByStudent_Id(10L)).thenReturn(Optional.of(result));

        Optional<ExamResult> found = examService.findResultByStudentId(10L);

        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("findResultByStudentId - not found returns empty")
    void findResultByStudentId_notFound_returnsEmpty() {
        when(examResultRepository.findByStudent_Id(999L)).thenReturn(Optional.empty());

        Optional<ExamResult> found = examService.findResultByStudentId(999L);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("generateToken - form not found throws exception")
    void generateToken_formNotFound_throwsException() {
        when(admissionFormRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> examService.generateToken(999L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("generateToken - existing form validation updates token")
    void generateToken_existingValidation_updatesToken() {
        Student student = Student.builder().id(10L).build();
        AdmissionForm form = new AdmissionForm();
        form.setId(1L);
        form.setStudent(student);
        FormValidation validation = new FormValidation();
        validation.setId(3L);
        validation.setAdmissionForm(form);
        validation.setStudent(student);

        when(admissionFormRepository.findById(1L)).thenReturn(Optional.of(form));
        when(formValidationRepository.findAll()).thenReturn(List.of(validation));
        when(formValidationRepository.save(any())).thenReturn(validation);

        String token = examService.generateToken(1L);

        assertThat(token).isNotNull().contains("1-");
    }

    @Test
    @DisplayName("generateToken - no existing validation creates new one")
    void generateToken_noValidation_createsNewValidation() {
        Student student = Student.builder().id(10L).build();
        AdmissionForm form = new AdmissionForm();
        form.setId(2L);
        form.setStudent(student);
        FormValidation newValidation = new FormValidation();
        newValidation.setId(5L);
        newValidation.setAdmissionForm(form);

        when(admissionFormRepository.findById(2L)).thenReturn(Optional.of(form));
        when(formValidationRepository.findAll()).thenReturn(Collections.emptyList());
        when(formValidationRepository.save(any())).thenReturn(newValidation);
        when(formRepairStatusRepository.save(any())).thenReturn(new FormRepairStatus());

        String token = examService.generateToken(2L);

        assertThat(token).isNotNull().contains("2-");
        verify(formValidationRepository, times(2)).save(any());
    }
}
