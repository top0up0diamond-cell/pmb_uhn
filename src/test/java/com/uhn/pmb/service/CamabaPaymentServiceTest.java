package com.uhn.pmb.service;

import com.uhn.pmb.entity.*;
import com.uhn.pmb.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CamabaPaymentServiceTest {

    @Mock private VirtualAccountRepository virtualAccountRepository;
    @Mock private BrivaService brivaService;
    @Mock private StudentRepository studentRepository;
    @Mock private UserRepository userRepository;
    @Mock private RegistrationStatusRepository registrationStatusRepository;
    @Mock private AdmissionFormRepository admissionFormRepository;
    @Mock private FormValidationRepository formValidationRepository;
    @Mock private ExamTokenService examTokenService;
    @Mock private ExamTokenRepository examTokenRepository;
    @Mock private EmailService emailService;
    @Mock private JenisSeleksiRepository jenisSeleksiRepository;
    @Mock private RegistrationPeriodRepository registrationPeriodRepository;
    @Mock private StudentRegistrationService studentRegistrationService;

    @InjectMocks
    private CamabaPaymentService camabaPaymentService;

    @Test
    @DisplayName("createVirtualAccount - user not found throws exception")
    void createVirtualAccount_userNotFound_throwsException() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaPaymentService.createVirtualAccount("none@test.com", 1L, 1L, java.math.BigDecimal.valueOf(1000)))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("createVirtualAccount - student not found throws exception")
    void createVirtualAccount_studentNotFound_throwsException() {
        User user = User.builder().id(1L).email("u@test.com").build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaPaymentService.createVirtualAccount("u@test.com", 1L, 1L, java.math.BigDecimal.valueOf(1000)))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("checkPaymentStatus - user not found throws exception")
    void checkPaymentStatus_userNotFound_throwsException() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaPaymentService.checkPaymentStatus("none@test.com", "VA001"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("checkPaymentStatus - VA not found throws exception")
    void checkPaymentStatus_vaNotFound_throwsException() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(virtualAccountRepository.findByVaNumber("VA999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaPaymentService.checkPaymentStatus("u@test.com", "VA999"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("verifyPayment - user not found throws exception")
    void verifyPayment_userNotFound_throwsException() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaPaymentService.verifyPayment("none@test.com", "VA001"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("verifyPayment - VA not found throws exception")
    void verifyPayment_vaNotFound_throwsException() {
        User user = User.builder().id(1L).email("u@test.com").build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(virtualAccountRepository.findByVaNumber("VA999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaPaymentService.verifyPayment("u@test.com", "VA999"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("verifyPayment - success updates VA and creates new registration status")
    void verifyPayment_success_createsNewRegistrationStatus() {
        User user = User.builder().id(1L).email("u@test.com").build();
        VirtualAccount va = VirtualAccount.builder()
                .id(1L)
                .vaNumber("VA001")
                .status(VirtualAccount.VAStatus.ACTIVE)
                .build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(virtualAccountRepository.findByVaNumber("VA001")).thenReturn(Optional.of(va));
        when(virtualAccountRepository.save(any())).thenReturn(va);
        when(registrationStatusRepository.findByUserAndStage(eq(user),
                eq(RegistrationStatus.RegistrationStage.PAYMENT_BRIVA)))
                .thenReturn(Optional.empty());
        when(registrationStatusRepository.save(any())).thenReturn(new RegistrationStatus());

        Map<String, Object> result = camabaPaymentService.verifyPayment("u@test.com", "VA001");

        assertThat(result).containsKey("success");
        assertThat(result.get("success")).isEqualTo(true);
    }

    @Test
    @DisplayName("verifyPayment - success updates existing registration status")
    void verifyPayment_success_updatesExistingRegistrationStatus() {
        User user = User.builder().id(1L).email("u@test.com").build();
        VirtualAccount va = VirtualAccount.builder()
                .id(1L)
                .vaNumber("VA001")
                .status(VirtualAccount.VAStatus.ACTIVE)
                .build();
        RegistrationStatus existingStatus = new RegistrationStatus();
        existingStatus.setStatus(RegistrationStatus.RegistrationStatus_Enum.MENUNGGU_VERIFIKASI);
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(virtualAccountRepository.findByVaNumber("VA001")).thenReturn(Optional.of(va));
        when(virtualAccountRepository.save(any())).thenReturn(va);
        when(registrationStatusRepository.findByUserAndStage(eq(user),
                eq(RegistrationStatus.RegistrationStage.PAYMENT_BRIVA)))
                .thenReturn(Optional.of(existingStatus));
        when(registrationStatusRepository.save(any())).thenReturn(existingStatus);

        Map<String, Object> result = camabaPaymentService.verifyPayment("u@test.com", "VA001");

        assertThat(result.get("success")).isEqualTo(true);
    }

    @Test
    @DisplayName("buyForm - form already WAITING_PAYMENT throws exception")
    void buyForm_alreadyWaitingPayment_throwsException() {
        AdmissionForm form = new AdmissionForm();
        form.setId(1L);
        form.setStatus(AdmissionForm.FormStatus.WAITING_PAYMENT);
        when(admissionFormRepository.findById(1L)).thenReturn(Optional.of(form));

        assertThatThrownBy(() -> camabaPaymentService.buyForm(1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("buyForm - form not VERIFIED throws exception")
    void buyForm_notVerified_throwsException() {
        AdmissionForm form = new AdmissionForm();
        form.setId(1L);
        form.setStatus(AdmissionForm.FormStatus.DRAFT);
        when(admissionFormRepository.findById(1L)).thenReturn(Optional.of(form));

        assertThatThrownBy(() -> camabaPaymentService.buyForm(1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("buyForm - form not found throws exception")
    void buyForm_formNotFound_throwsException() {
        when(admissionFormRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaPaymentService.buyForm(999L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("simulatePayment - VA not found throws exception")
    void simulatePayment_vaNotFound_throwsException() {
        when(virtualAccountRepository.findByVaNumber("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaPaymentService.simulatePayment("INVALID"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("simulatePayment - success updates VA status")
    void simulatePayment_success_returnsResult() {
        VirtualAccount va = VirtualAccount.builder()
                .id(1L)
                .vaNumber("VA001")
                .status(VirtualAccount.VAStatus.PAID)
                .amount(BigDecimal.valueOf(500000))
                .build();
        when(virtualAccountRepository.findByVaNumber("VA001"))
                .thenReturn(Optional.of(va));
        doNothing().when(brivaService).updatePaymentStatus(anyString(), anyString(), any());
        when(virtualAccountRepository.findByVaNumber("VA001")).thenReturn(Optional.of(va));

        Map<String, Object> result = camabaPaymentService.simulatePayment("VA001");

        assertThat(result.get("success")).isEqualTo(true);
    }

    @Test
    @DisplayName("confirmCicilanPayment - user not found throws exception")
    void confirmCicilanPayment_userNotFound_throwsException() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaPaymentService.confirmCicilanPayment("none@test.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("confirmCicilanPayment - success creates new statuses")
    void confirmCicilanPayment_success_createsStatuses() {
        User user = User.builder().id(1L).email("u@test.com").build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(registrationStatusRepository.findByUserAndStage(eq(user),
                eq(RegistrationStatus.RegistrationStage.PAYMENT_CICILAN_1)))
                .thenReturn(Optional.empty());
        when(registrationStatusRepository.findByUserAndStage(eq(user),
                eq(RegistrationStatus.RegistrationStage.DAFTAR_ULANG)))
                .thenReturn(Optional.empty());
        when(registrationStatusRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = camabaPaymentService.confirmCicilanPayment("u@test.com");

        assertThat(result.get("success")).isEqualTo(true);
    }

    @Test
    @DisplayName("confirmCicilanPayment - success with existing statuses")
    void confirmCicilanPayment_success_updatesExistingStatuses() {
        User user = User.builder().id(1L).email("u@test.com").build();
        RegistrationStatus cicilanStatus = new RegistrationStatus();
        cicilanStatus.setStatus(RegistrationStatus.RegistrationStatus_Enum.MENUNGGU_VERIFIKASI);
        cicilanStatus.setUpdatedAt(LocalDateTime.now());
        RegistrationStatus daftarStatus = new RegistrationStatus();
        daftarStatus.setStatus(RegistrationStatus.RegistrationStatus_Enum.MENUNGGU_VERIFIKASI);
        daftarStatus.setUpdatedAt(LocalDateTime.now());
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(registrationStatusRepository.findByUserAndStage(eq(user),
                eq(RegistrationStatus.RegistrationStage.PAYMENT_CICILAN_1)))
                .thenReturn(Optional.of(cicilanStatus));
        when(registrationStatusRepository.findByUserAndStage(eq(user),
                eq(RegistrationStatus.RegistrationStage.DAFTAR_ULANG)))
                .thenReturn(Optional.of(daftarStatus));
        when(registrationStatusRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = camabaPaymentService.confirmCicilanPayment("u@test.com");

        assertThat(result.get("success")).isEqualTo(true);
    }

    @Test
    @DisplayName("checkPaymentStatus - security exception when student doesn't own VA")
    void checkPaymentStatus_studentDoesntOwnVa_throwsSecurityException() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        Student otherStudent = Student.builder().id(20L).build();
        VirtualAccount va = VirtualAccount.builder()
                .id(1L)
                .vaNumber("VA001")
                .student(otherStudent)
                .status(VirtualAccount.VAStatus.ACTIVE)
                .expiredAt(LocalDateTime.now().plusDays(1))
                .build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(virtualAccountRepository.findByVaNumber("VA001")).thenReturn(Optional.of(va));

        assertThatThrownBy(() -> camabaPaymentService.checkPaymentStatus("u@test.com", "VA001"))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    @DisplayName("checkPaymentStatus - success with active VA")
    void checkPaymentStatus_success_activeVa() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        VirtualAccount va = VirtualAccount.builder()
                .id(1L)
                .vaNumber("VA001")
                .student(student)
                .status(VirtualAccount.VAStatus.ACTIVE)
                .amount(BigDecimal.valueOf(500000))
                .expiredAt(LocalDateTime.now().plusDays(7))
                .build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(virtualAccountRepository.findByVaNumber("VA001")).thenReturn(Optional.of(va));

        Map<String, Object> result = camabaPaymentService.checkPaymentStatus("u@test.com", "VA001");

        assertThat(result.get("success")).isEqualTo(true);
    }

    @Test
    @DisplayName("buyForm - VERIFIED form success path returns map with vaNumber")
    void buyForm_success_returnsVaInfo() throws Exception {
        User user = User.builder().id(1L).email("s@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        AdmissionForm form = new AdmissionForm();
        form.setId(1L);
        form.setStatus(AdmissionForm.FormStatus.VERIFIED);
        form.setStudent(student);

        VirtualAccount va = VirtualAccount.builder()
                .id(1L)
                .vaNumber("88000999888")
                .amount(BigDecimal.valueOf(500000))
                .expiredAt(LocalDateTime.now().plusDays(3))
                .build();

        when(admissionFormRepository.findById(1L)).thenReturn(Optional.of(form));
        when(admissionFormRepository.save(any())).thenReturn(form);
        when(studentRegistrationService.buyFormAndCreateVA(any())).thenReturn(va);

        Map<String, Object> result = camabaPaymentService.buyForm(1L);

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("vaNumber")).isEqualTo("88000999888");
    }

    @Test
    @DisplayName("buyForm - registrationService throws wraps exception")
    void buyForm_registrationServiceThrows_wrapsException() throws Exception {
        User user = User.builder().id(1L).email("s@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        AdmissionForm form = new AdmissionForm();
        form.setId(1L);
        form.setStatus(AdmissionForm.FormStatus.VERIFIED);
        form.setStudent(student);

        when(admissionFormRepository.findById(1L)).thenReturn(Optional.of(form));
        when(admissionFormRepository.save(any())).thenReturn(form);
        when(studentRegistrationService.buyFormAndCreateVA(any())).thenThrow(new RuntimeException("VA gen failed"));

        assertThatThrownBy(() -> camabaPaymentService.buyForm(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Gagal membuat virtual account");
    }

    @Test
    @DisplayName("createVirtualAccount - success returns va info map")
    void createVirtualAccount_success_returnsVaInfo() throws Exception {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        VirtualAccount savedVa = VirtualAccount.builder()
                .id(1L)
                .vaNumber("88001111222")
                .amount(BigDecimal.valueOf(300000))
                .status(VirtualAccount.VAStatus.ACTIVE)
                .build();

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(jenisSeleksiRepository.findById(1L)).thenReturn(Optional.of(new com.uhn.pmb.entity.JenisSeleksi()));
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(new com.uhn.pmb.entity.RegistrationPeriod()));
        when(brivaService.generateVirtualAccount(any())).thenReturn("88001111222");
        when(virtualAccountRepository.save(any())).thenReturn(savedVa);
        when(formValidationRepository.findAll()).thenReturn(java.util.List.of());

        Map<String, Object> result = camabaPaymentService.createVirtualAccount(
                "u@test.com", 1L, 1L, BigDecimal.valueOf(300000));

        assertThat(result.get("success")).isEqualTo(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertThat(data.get("vaNumber")).isEqualTo("88001111222");
    }

    @Test
    @DisplayName("checkPaymentStatus - expired ACTIVE VA sets status to EXPIRED")
    void checkPaymentStatus_expiredActiveVA_setsExpired() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        VirtualAccount va = VirtualAccount.builder()
                .id(1L)
                .vaNumber("VA001")
                .student(student)
                .status(VirtualAccount.VAStatus.ACTIVE)
                .amount(BigDecimal.valueOf(500000))
                .expiredAt(LocalDateTime.now().minusDays(1)) // expired yesterday
                .build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(virtualAccountRepository.findByVaNumber("VA001")).thenReturn(Optional.of(va));
        when(virtualAccountRepository.save(any())).thenReturn(va);

        Map<String, Object> result = camabaPaymentService.checkPaymentStatus("u@test.com", "VA001");

        assertThat(result.get("success")).isEqualTo(true);
        verify(virtualAccountRepository).save(va);
    }

    @Test
    @DisplayName("checkPaymentStatus - PAID VA with admissionForm updates form status to VERIFIED")
    void checkPaymentStatus_paidVAWithAdmissionForm_updatesForm() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        AdmissionForm form = new AdmissionForm();
        form.setId(1L);
        form.setStatus(AdmissionForm.FormStatus.WAITING_PAYMENT);
        VirtualAccount va = VirtualAccount.builder()
                .id(1L)
                .vaNumber("VA001")
                .student(student)
                .admissionForm(form)
                .status(VirtualAccount.VAStatus.PAID)
                .amount(BigDecimal.valueOf(500000))
                .expiredAt(LocalDateTime.now().plusDays(7))
                .build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(virtualAccountRepository.findByVaNumber("VA001")).thenReturn(Optional.of(va));
        when(admissionFormRepository.save(any())).thenReturn(form);

        Map<String, Object> result = camabaPaymentService.checkPaymentStatus("u@test.com", "VA001");

        assertThat(result.get("success")).isEqualTo(true);
        verify(admissionFormRepository).save(form);
    }

    @Test
    @DisplayName("checkPaymentStatus - PAID VA without admissionForm does not update form")
    void checkPaymentStatus_paidVANoAdmissionForm_noFormUpdate() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        VirtualAccount va = VirtualAccount.builder()
                .id(1L)
                .vaNumber("VA001")
                .student(student)
                .status(VirtualAccount.VAStatus.PAID)
                .amount(BigDecimal.valueOf(500000))
                .expiredAt(LocalDateTime.now().plusDays(7))
                .build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(virtualAccountRepository.findByVaNumber("VA001")).thenReturn(Optional.of(va));

        Map<String, Object> result = camabaPaymentService.checkPaymentStatus("u@test.com", "VA001");

        assertThat(result.get("success")).isEqualTo(true);
        verify(admissionFormRepository, never()).save(any());
    }

    @Test
    @DisplayName("confirmCicilanPayment - existing daftarUlang SELESAI status is not overridden")
    void confirmCicilanPayment_existingDaftarUlangSelesai_staysSelesai() {
        User user = User.builder().id(1L).email("u@test.com").build();
        RegistrationStatus cicilanStatus = new RegistrationStatus();
        cicilanStatus.setStatus(RegistrationStatus.RegistrationStatus_Enum.SELESAI);
        cicilanStatus.setUpdatedAt(LocalDateTime.now());
        RegistrationStatus daftarStatus = new RegistrationStatus();
        daftarStatus.setStatus(RegistrationStatus.RegistrationStatus_Enum.SELESAI);
        daftarStatus.setUpdatedAt(LocalDateTime.now());
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(registrationStatusRepository.findByUserAndStage(eq(user),
                eq(RegistrationStatus.RegistrationStage.PAYMENT_CICILAN_1)))
                .thenReturn(Optional.of(cicilanStatus));
        when(registrationStatusRepository.findByUserAndStage(eq(user),
                eq(RegistrationStatus.RegistrationStage.DAFTAR_ULANG)))
                .thenReturn(Optional.of(daftarStatus));
        when(registrationStatusRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = camabaPaymentService.confirmCicilanPayment("u@test.com");

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(daftarStatus.getStatus()).isEqualTo(RegistrationStatus.RegistrationStatus_Enum.SELESAI);
    }
}
