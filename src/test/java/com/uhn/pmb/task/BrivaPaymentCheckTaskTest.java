package com.uhn.pmb.task;

import com.uhn.pmb.entity.AdmissionForm;
import com.uhn.pmb.entity.ExamToken;
import com.uhn.pmb.entity.FormValidation;
import com.uhn.pmb.entity.Student;
import com.uhn.pmb.entity.User;
import com.uhn.pmb.entity.VirtualAccount;
import com.uhn.pmb.repository.FormValidationRepository;
import com.uhn.pmb.repository.VirtualAccountRepository;
import com.uhn.pmb.service.ExamTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrivaPaymentCheckTaskTest {

    @Mock private VirtualAccountRepository virtualAccountRepository;
    @Mock private FormValidationRepository formValidationRepository;
    @Mock private ExamTokenService examTokenService;

    @InjectMocks
    private BrivaPaymentCheckTask brivaPaymentCheckTask;

    @Test
    @DisplayName("checkBrivaPayments - empty active VA list - does nothing")
    void checkBrivaPayments_emptyActiveVAs_doesNothing() {
        when(virtualAccountRepository.findByStatus(VirtualAccount.VAStatus.ACTIVE))
                .thenReturn(List.of());

        brivaPaymentCheckTask.checkBrivaPayments();

        verify(virtualAccountRepository, never()).save(any());
        verifyNoInteractions(formValidationRepository);
    }

    @Test
    @DisplayName("checkBrivaPayments - null active VA list - does nothing")
    void checkBrivaPayments_nullActiveVAs_doesNothing() {
        when(virtualAccountRepository.findByStatus(VirtualAccount.VAStatus.ACTIVE))
                .thenReturn(null);

        brivaPaymentCheckTask.checkBrivaPayments();

        verify(virtualAccountRepository, never()).save(any());
    }

    @Test
    @DisplayName("checkBrivaPayments - VA already PAID - updates VA and FormValidation")
    void checkBrivaPayments_vaPaid_updatesVaAndForm() {
        User user = User.builder().email("s@test.com").build();
        Student student = Student.builder().id(1L).user(user).fullName("Siswa A").build();
        AdmissionForm form = AdmissionForm.builder().id(10L).build();

        VirtualAccount va = VirtualAccount.builder()
                .vaNumber("88001234")
                .status(VirtualAccount.VAStatus.PAID)
                .student(student)
                .admissionForm(form)
                .build();

        FormValidation fv = FormValidation.builder()
                .id(5L)
                .student(student)
                .admissionForm(form)
                .validationStatus(FormValidation.ValidationStatus.PENDING)
                .build();

        when(virtualAccountRepository.findByStatus(VirtualAccount.VAStatus.ACTIVE))
                .thenReturn(List.of(va));
        when(formValidationRepository.findAll()).thenReturn(List.of(fv));
        when(examTokenService.generateToken(anyLong(), anyLong(), anyInt())).thenReturn(null);

        brivaPaymentCheckTask.checkBrivaPayments();

        verify(virtualAccountRepository).save(va);
        verify(formValidationRepository, atLeastOnce()).save(fv);
    }

    @Test
    @DisplayName("checkBrivaPayments - VA PAID with exam token - syncs token to FormValidation")
    void checkBrivaPayments_vaPaid_syncsExamToken() {
        User user = User.builder().email("s@test.com").build();
        Student student = Student.builder().id(2L).user(user).fullName("Siswa B").build();
        AdmissionForm form = AdmissionForm.builder().id(20L).build();

        VirtualAccount va = VirtualAccount.builder()
                .vaNumber("88005678")
                .status(VirtualAccount.VAStatus.PAID)
                .student(student)
                .admissionForm(form)
                .build();

        FormValidation fv = FormValidation.builder()
                .id(6L)
                .student(student)
                .admissionForm(form)
                .validationStatus(FormValidation.ValidationStatus.PENDING)
                .build();

        ExamToken token = ExamToken.builder().tokenValue("TOKEN-ABC123").build();

        when(virtualAccountRepository.findByStatus(VirtualAccount.VAStatus.ACTIVE))
                .thenReturn(List.of(va));
        when(formValidationRepository.findAll()).thenReturn(List.of(fv));
        when(examTokenService.generateToken(2L, 20L, 120)).thenReturn(token);

        brivaPaymentCheckTask.checkBrivaPayments();

        verify(virtualAccountRepository).save(va);
        verify(formValidationRepository, atLeastOnce()).save(fv);
    }

    @Test
    @DisplayName("checkBrivaPayments - VA ACTIVE (not paid) - skips update")
    void checkBrivaPayments_vaActiveNotPaid_skipsUpdate() {
        VirtualAccount va = VirtualAccount.builder()
                .vaNumber("88009999")
                .status(VirtualAccount.VAStatus.ACTIVE)
                .build();

        when(virtualAccountRepository.findByStatus(VirtualAccount.VAStatus.ACTIVE))
                .thenReturn(List.of(va));

        brivaPaymentCheckTask.checkBrivaPayments();

        verify(virtualAccountRepository, never()).save(any());
        verifyNoInteractions(formValidationRepository);
    }

    @Test
    @DisplayName("checkBrivaPayments - VA PAID but no matching FormValidation - skips form save")
    void checkBrivaPayments_vaPaid_noMatchingForm_skipsFormSave() {
        User user = User.builder().email("s@test.com").build();
        Student student = Student.builder().id(3L).user(user).fullName("Siswa C").build();
        AdmissionForm form = AdmissionForm.builder().id(30L).build();

        VirtualAccount va = VirtualAccount.builder()
                .vaNumber("88007777")
                .status(VirtualAccount.VAStatus.PAID)
                .student(student)
                .admissionForm(form)
                .build();

        // FormValidation exists but for different student
        Student otherStudent = Student.builder().id(99L).build();
        FormValidation fv = FormValidation.builder()
                .id(7L)
                .student(otherStudent)
                .admissionForm(form)
                .validationStatus(FormValidation.ValidationStatus.PENDING)
                .build();

        when(virtualAccountRepository.findByStatus(VirtualAccount.VAStatus.ACTIVE))
                .thenReturn(List.of(va));
        when(formValidationRepository.findAll()).thenReturn(List.of(fv));

        brivaPaymentCheckTask.checkBrivaPayments();

        verify(virtualAccountRepository).save(va);
        verify(formValidationRepository, never()).save(fv);
    }

    @Test
    @DisplayName("checkBrivaPayments - multiple VAs, some paid - processes only PAID ones")
    void checkBrivaPayments_multipleVAs_processesOnlyPaid() {
        User user1 = User.builder().email("a@test.com").build();
        Student student1 = Student.builder().id(4L).user(user1).fullName("A").build();
        AdmissionForm form1 = AdmissionForm.builder().id(40L).build();

        VirtualAccount paidVa = VirtualAccount.builder()
                .vaNumber("88001111")
                .status(VirtualAccount.VAStatus.PAID)
                .student(student1)
                .admissionForm(form1)
                .build();
        VirtualAccount activeVa = VirtualAccount.builder()
                .vaNumber("88002222")
                .status(VirtualAccount.VAStatus.ACTIVE)
                .build();

        FormValidation fv = FormValidation.builder()
                .id(8L)
                .student(student1)
                .admissionForm(form1)
                .validationStatus(FormValidation.ValidationStatus.PENDING)
                .build();

        when(virtualAccountRepository.findByStatus(VirtualAccount.VAStatus.ACTIVE))
                .thenReturn(List.of(paidVa, activeVa));
        when(formValidationRepository.findAll()).thenReturn(List.of(fv));
        when(examTokenService.generateToken(anyLong(), anyLong(), anyInt())).thenReturn(null);

        brivaPaymentCheckTask.checkBrivaPayments();

        verify(virtualAccountRepository, times(1)).save(paidVa);
        // activeVa has ACTIVE status — isPaymentReceived returns false, save never called for it
        verify(virtualAccountRepository, times(1)).save(any(VirtualAccount.class));
    }
}

