package com.uhn.pmb.service;

import com.uhn.pmb.dto.HasilAkhirRegistrationRequest;
import com.uhn.pmb.entity.*;
import com.uhn.pmb.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HasilAkhirServiceTest {

    @Mock private HasilAkhirRepository hasilAkhirRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private UserRepository userRepository;
    @Mock private ReEnrollmentRepository reenrollmentRepository;
    @Mock private AdmissionFormRepository admissionFormRepository;
    @Mock private FormValidationRepository formValidationRepository;
    @Mock private RegistrationPeriodRepository registrationPeriodRepository;
    @Mock private JenisSeleksiRepository jenisSeleksiRepository;

    @InjectMocks
    private HasilAkhirService hasilAkhirService;

    @Test
    @DisplayName("createHasilAkhir - student exists, creates new hasil akhir")
    void createHasilAkhir_studentExists_createsNew() {
        User user = User.builder().id(1L).email("user@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        
        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(hasilAkhirRepository.findByStudent(student)).thenReturn(Optional.empty());
        
        HasilAkhir saved = HasilAkhir.builder()
                .id(1L)
                .student(student)
                .nomorRegistrasi("REG-001")
                .brivaNumber("BRV-001")
                .brivaAmount(BigDecimal.valueOf(5000000))
                .build();
        
        when(hasilAkhirRepository.save(any())).thenReturn(saved);

        HasilAkhir result = hasilAkhirService.createHasilAkhir(10L, "BRV-001", "REG-001",
                BigDecimal.valueOf(5000000), RegistrationPeriod.WaveType.REGULAR_TEST,
                "SNBT", "Teknik Informatika", null, 6);

        assertThat(result).isNotNull();
        assertThat(result.getNomorRegistrasi()).isEqualTo("REG-001");
    }

    @Test
    @DisplayName("createHasilAkhir - student not found throws exception")
    void createHasilAkhir_studentNotFound_throwsException() {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hasilAkhirService.createHasilAkhir(999L, "BRV", "REG",
                BigDecimal.valueOf(5000000), RegistrationPeriod.WaveType.REGULAR_TEST,
                "SNBT", "TI", null, 6))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getHasilAkhirByStudentId - found returns hasil akhir")
    void getHasilAkhirByStudentId_found_returnsHasilAkhir() {
        Student student = Student.builder().id(10L).build();
        HasilAkhir ha = HasilAkhir.builder().id(1L).student(student).build();
        
        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(hasilAkhirRepository.findByStudent(student)).thenReturn(Optional.of(ha));

        Optional<HasilAkhir> result = hasilAkhirService.getHasilAkhirByStudentId(10L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getHasilAkhirByUserId - found returns hasil akhir")
    void getHasilAkhirByUserId_found_returnsHasilAkhir() {
        User user = User.builder().id(1L).build();
        HasilAkhir ha = HasilAkhir.builder().id(1L).user(user).build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(hasilAkhirRepository.findByUser(user)).thenReturn(Optional.of(ha));

        Optional<HasilAkhir> result = hasilAkhirService.getHasilAkhirByUserId(1L);

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("studentHasHasilAkhir - returns true when exists")
    void studentHasHasilAkhir_returnsTrue() {
        Student student = Student.builder().id(10L).build();
        
        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(hasilAkhirRepository.existsByStudent(student)).thenReturn(true);

        boolean result = hasilAkhirService.studentHasHasilAkhir(10L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("studentHasHasilAkhir - returns false when not exists")
    void studentHasHasilAkhir_returnsFalse() {
        Student student = Student.builder().id(10L).build();
        
        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(hasilAkhirRepository.existsByStudent(student)).thenReturn(false);

        boolean result = hasilAkhirService.studentHasHasilAkhir(10L);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("updateStatus - updates status successfully")
    void updateStatus_updatesSuccessfully() {
        Student student = Student.builder().id(10L).build();
        HasilAkhir ha = HasilAkhir.builder().id(1L).status(HasilAkhir.HasilAkhirStatus.PENDING).build();
        
        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(hasilAkhirRepository.findByStudent(student)).thenReturn(Optional.of(ha));
        when(hasilAkhirRepository.save(any())).thenReturn(ha);

        hasilAkhirService.updateStatus(10L, HasilAkhir.HasilAkhirStatus.ACTIVE);

        verify(hasilAkhirRepository).save(any());
    }

    @Test
    @DisplayName("createHasilAkhir - existing with valid BRIVA preserves BRIVA")
    void createHasilAkhir_existingWithValidBriva_preservesBriva() {
        User user = User.builder().id(1L).email("user@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        HasilAkhir existing = HasilAkhir.builder()
                .id(1L).student(student).brivaNumber("BRIVA-999").build();
        
        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(hasilAkhirRepository.findByStudent(student)).thenReturn(Optional.of(existing));
        when(hasilAkhirRepository.save(any())).thenReturn(existing);

        // Pass N/A as brivaNumber — should NOT overwrite BRIVA-999
        HasilAkhir result = hasilAkhirService.createHasilAkhir(10L, "N/A", "REG-001",
                BigDecimal.valueOf(5000000), RegistrationPeriod.WaveType.REGULAR_TEST,
                "SNBT", "Teknik Informatika", null, 6);

        assertThat(existing.getBrivaNumber()).isEqualTo("BRIVA-999");
    }

    @Test
    @DisplayName("createHasilAkhir - existing with null BRIVA updates it")
    void createHasilAkhir_existingWithNullBriva_updatesBriva() {
        User user = User.builder().id(1L).email("user@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        HasilAkhir existing = HasilAkhir.builder()
                .id(1L).student(student).brivaNumber(null).build();

        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(hasilAkhirRepository.findByStudent(student)).thenReturn(Optional.of(existing));
        when(hasilAkhirRepository.save(any())).thenReturn(existing);

        hasilAkhirService.createHasilAkhir(10L, "NEW-BRIVA", "REG-001",
                BigDecimal.valueOf(5000000), RegistrationPeriod.WaveType.REGULAR_TEST,
                "SNBT", "Teknik Informatika", null, 6);

        assertThat(existing.getBrivaNumber()).isEqualTo("NEW-BRIVA");
    }

    @Test
    @DisplayName("getHasilAkhirByStudentId - student not found returns empty")
    void getHasilAkhirByStudentId_studentNotFound_returnsEmpty() {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<HasilAkhir> result = hasilAkhirService.getHasilAkhirByStudentId(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getHasilAkhirByStudentId - no hasil akhir returns empty")
    void getHasilAkhirByStudentId_noHasilAkhir_returnsEmpty() {
        Student student = Student.builder().id(10L).build();
        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(hasilAkhirRepository.findByStudent(student)).thenReturn(Optional.empty());

        Optional<HasilAkhir> result = hasilAkhirService.getHasilAkhirByStudentId(10L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getHasilAkhirByUserId - user not found returns empty")
    void getHasilAkhirByUserId_userNotFound_returnsEmpty() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<HasilAkhir> result = hasilAkhirService.getHasilAkhirByUserId(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getHasilAkhirByUserId - no hasil akhir returns empty")
    void getHasilAkhirByUserId_noHasilAkhir_returnsEmpty() {
        User user = User.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(hasilAkhirRepository.findByUser(user)).thenReturn(Optional.empty());

        Optional<HasilAkhir> result = hasilAkhirService.getHasilAkhirByUserId(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("studentHasHasilAkhir - student not found returns false")
    void studentHasHasilAkhir_studentNotFound_returnsFalse() {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = hasilAkhirService.studentHasHasilAkhir(999L);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("updateStatus - student not found does nothing")
    void updateStatus_studentNotFound_doesNothing() {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        hasilAkhirService.updateStatus(999L, HasilAkhir.HasilAkhirStatus.ACTIVE);

        verify(hasilAkhirRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatus - no hasil akhir does nothing")
    void updateStatus_noHasilAkhir_doesNothing() {
        Student student = Student.builder().id(10L).build();
        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(hasilAkhirRepository.findByStudent(student)).thenReturn(Optional.empty());

        hasilAkhirService.updateStatus(10L, HasilAkhir.HasilAkhirStatus.ACTIVE);

        verify(hasilAkhirRepository, never()).save(any());
    }

    @Test
    @DisplayName("autoPopulateHasilAkhir - student not found throws exception")
    void autoPopulateHasilAkhir_studentNotFound_throws() {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hasilAkhirService.autoPopulateHasilAkhir(999L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("autoPopulateHasilAkhir - success with no existing data")
    void autoPopulateHasilAkhir_success_noExistingData() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        HasilAkhir saved = HasilAkhir.builder().id(1L).student(student).build();

        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(hasilAkhirRepository.findByStudent(student)).thenReturn(Optional.empty());
        when(reenrollmentRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        when(admissionFormRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        when(hasilAkhirRepository.save(any())).thenReturn(saved);

        HasilAkhir result = hasilAkhirService.autoPopulateHasilAkhir(10L);

        assertThat(result).isNotNull();
        verify(hasilAkhirRepository).save(any());
    }

    @Test
    @DisplayName("autoPopulateHasilAkhir - existing valid BRIVA is preserved")
    void autoPopulateHasilAkhir_withExistingBriva_preservesBriva() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        HasilAkhir existing = HasilAkhir.builder()
                .id(1L).student(student).brivaNumber("BRIVA-VALID-123").build();

        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(hasilAkhirRepository.findByStudent(student)).thenReturn(Optional.of(existing));
        when(reenrollmentRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        when(admissionFormRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        when(hasilAkhirRepository.save(any())).thenReturn(existing);

        hasilAkhirService.autoPopulateHasilAkhir(10L);

        verify(hasilAkhirRepository).save(any());
    }

    @Test
    @DisplayName("updateRegistrationNumberAndBriva - updates successfully")
    void updateRegistrationNumberAndBriva_updatesSuccessfully() {
        HasilAkhirRegistrationRequest request = new HasilAkhirRegistrationRequest();
        request.setNomorRegistrasi("REG-2024-001");
        request.setBrivaNumber("BRIVA-2024-001");
        
        User user = User.builder().id(1L).email("student@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        AdmissionForm form = AdmissionForm.builder().id(5L).student(student).build();
        FormValidation validation = FormValidation.builder().id(100L).admissionForm(form).build();
        
        HasilAkhir ha = HasilAkhir.builder()
                .id(1L)
                .nomorRegistrasi("OLD-REG")
                .brivaNumber("OLD-BRIVA")
                .student(student)
                .build();

        when(formValidationRepository.findById(100L)).thenReturn(Optional.of(validation));
        when(hasilAkhirRepository.findByStudent(student)).thenReturn(Optional.of(ha));
        when(hasilAkhirRepository.save(any())).thenReturn(ha);

        HasilAkhir result = hasilAkhirService.updateRegistrationNumberAndBriva(100L, request);

        assertThat(result).isNotNull();
        verify(hasilAkhirRepository).save(any());
    }

    @Test
    @DisplayName("autoPopulateHasilAkhir - creates new with reenrollment data")
    void autoPopulateHasilAkhir_createsNewWithReenrollment() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        ReEnrollment reenroll = ReEnrollment.builder()
                .id(1L)
                .student(student)
                .status(ReEnrollment.ReEnrollmentStatus.SUBMITTED)
                .build();

        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(hasilAkhirRepository.findByStudent(student)).thenReturn(Optional.empty());
        when(reenrollmentRepository.findAll()).thenReturn(java.util.List.of(reenroll));
        when(admissionFormRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        when(hasilAkhirRepository.save(any())).thenReturn(HasilAkhir.builder().id(1L).build());

        HasilAkhir result = hasilAkhirService.autoPopulateHasilAkhir(10L);

        assertThat(result).isNotNull();
        verify(hasilAkhirRepository).save(any());
    }

    @Test
    @DisplayName("autoPopulateHasilAkhir - with admission form and jenis seleksi")
    void autoPopulateHasilAkhir_withAdmissionForm() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        RegistrationPeriod period = RegistrationPeriod.builder()
                .id(1L)
                .name("Gelombang 1")
                .waveType(RegistrationPeriod.WaveType.REGULAR_TEST)
                .build();
        AdmissionForm form = AdmissionForm.builder()
                .id(1L)
                .student(student)
                .period(period)
                .jenisSeleksiId(1L)
                .build();
        JenisSeleksi jenisSeleksi = JenisSeleksi.builder()
                .id(1L)
                .nama("SNBT")
                .build();

        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(hasilAkhirRepository.findByStudent(student)).thenReturn(Optional.empty());
        when(reenrollmentRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        when(admissionFormRepository.findAll()).thenReturn(java.util.List.of(form));
        when(jenisSeleksiRepository.findById(1L)).thenReturn(Optional.of(jenisSeleksi));
        when(hasilAkhirRepository.save(any())).thenReturn(HasilAkhir.builder().id(1L).build());

        HasilAkhir result = hasilAkhirService.autoPopulateHasilAkhir(10L);

        assertThat(result).isNotNull();
        verify(hasilAkhirRepository).save(any());
    }

    @Test
    @DisplayName("autoPopulateHasilAkhir - student not found throws exception")
    void autoPopulateHasilAkhir_studentNotFound_throwsException() {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hasilAkhirService.autoPopulateHasilAkhir(999L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("updateStatus - handles exception gracefully")
    void updateStatus_withException_handlesGracefully() {
        Student student = Student.builder().id(10L).build();
        HasilAkhir ha = HasilAkhir.builder().id(1L).build();

        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(hasilAkhirRepository.findByStudent(student)).thenReturn(Optional.of(ha));
        when(hasilAkhirRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> hasilAkhirService.updateStatus(10L, HasilAkhir.HasilAkhirStatus.ACTIVE))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("createHasilAkhir - with jumlah cicilan updates correctly")
    void createHasilAkhir_withJumlahCicilan_updatesCorrectly() {
        User user = User.builder().id(1L).email("user@test.com").build();
        Student student = Student.builder().id(10L).user(user).build();
        HasilAkhir existing = HasilAkhir.builder()
                .id(1L).student(student).jumlahCicilan(1).build();

        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(hasilAkhirRepository.findByStudent(student)).thenReturn(Optional.of(existing));
        when(hasilAkhirRepository.save(any())).thenReturn(existing);

        HasilAkhir result = hasilAkhirService.createHasilAkhir(10L, "BRIVA-001", "REG-001",
                BigDecimal.valueOf(5000000), RegistrationPeriod.WaveType.REGULAR_TEST,
                "SNBT", "Teknik Informatika", null, 6);

        assertThat(existing.getJumlahCicilan()).isEqualTo(6);
    }
}
