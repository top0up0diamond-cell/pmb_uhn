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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminDataExportServiceTest {

    @Mock private FormValidationRepository formValidationRepository;
    @Mock private ReEnrollmentValidationRepository reEnrollmentValidationRepository;
    @Mock private HasilAkhirRepository hasilAkhirRepository;
    @Mock private AdmissionFormRepository admissionFormRepository;
    @Mock private ReEnrollmentRepository reenrollmentRepository;
    @Mock private ReEnrollmentDocumentRepository reenrollmentDocumentRepository;

    @InjectMocks
    private AdminDataExportService adminDataExportService;

    // ===== exportFormAndPayment =====

    @Test
    @DisplayName("exportFormAndPayment - empty list returns empty")
    void exportFormAndPayment_emptyList() {
        when(formValidationRepository.findAll()).thenReturn(List.of());

        List<Map<String, Object>> result = adminDataExportService.exportFormAndPayment();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("exportFormAndPayment - with data maps fields correctly")
    void exportFormAndPayment_withData_mapFields() {
        User user = new User();
        user.setEmail("student@test.com");
        Student student = new Student();
        student.setFullName("Budi");
        student.setUser(user);
        FormValidation fv = new FormValidation();
        fv.setStudent(student);
        fv.setValidationStatus(FormValidation.ValidationStatus.APPROVED);
        fv.setPaymentStatus(FormValidation.PaymentStatus.PAID);
        fv.setVirtualAccountNumber("1234567890");
        fv.setPaymentAmount(500000L);
        fv.setCreatedAt(LocalDateTime.now());

        when(formValidationRepository.findAll()).thenReturn(List.of(fv));

        List<Map<String, Object>> result = adminDataExportService.exportFormAndPayment();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("Nama")).isEqualTo("Budi");
        assertThat(result.get(0).get("Email")).isEqualTo("student@test.com");
    }

    // ===== exportReEnrollmentData =====

    @Test
    @DisplayName("exportReEnrollmentData - empty list returns empty")
    void exportReEnrollmentData_emptyList() {
        when(reEnrollmentValidationRepository.findAll()).thenReturn(List.of());

        List<Map<String, Object>> result = adminDataExportService.exportReEnrollmentData();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("exportReEnrollmentData - with data maps fields correctly")
    void exportReEnrollmentData_withData_mapFields() {
        User user = new User();
        user.setEmail("stu@test.com");
        Student student = new Student();
        student.setFullName("Ani");
        student.setUser(user);
        ReEnrollmentValidation rv = new ReEnrollmentValidation();
        rv.setStudent(student);
        rv.setValidationStatus(ReEnrollmentValidation.ValidationStatus.APPROVED);
        rv.setCreatedAt(LocalDateTime.now());

        when(reEnrollmentValidationRepository.findAll()).thenReturn(List.of(rv));

        List<Map<String, Object>> result = adminDataExportService.exportReEnrollmentData();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("Nama")).isEqualTo("Ani");
    }

    // ===== exportHasilAkhirData =====

    @Test
    @DisplayName("exportHasilAkhirData - empty list returns empty")
    void exportHasilAkhirData_emptyList() {
        when(hasilAkhirRepository.findAll()).thenReturn(List.of());

        List<Map<String, Object>> result = adminDataExportService.exportHasilAkhirData();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("exportHasilAkhirData - with data returns mapped rows")
    void exportHasilAkhirData_withData_mapsFields() {
        User user = new User();
        user.setEmail("ha@test.com");
        Student student = new Student();
        student.setFullName("Citra");
        HasilAkhir ha = new HasilAkhir();
        ha.setStudent(student);
        ha.setUser(user);
        ha.setNomorRegistrasi("REG-001");
        ha.setBrivaNumber("BRV-001");
        ha.setBrivaAmount(BigDecimal.valueOf(1000000));
        ha.setStatus(HasilAkhir.HasilAkhirStatus.ACTIVE);
        ha.setCreatedAt(LocalDateTime.now());

        when(hasilAkhirRepository.findAll()).thenReturn(List.of(ha));

        List<Map<String, Object>> result = adminDataExportService.exportHasilAkhirData();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("Nomor Registrasi")).isEqualTo("REG-001");
    }

    // ===== getAdmissionFormsTable =====

    @Test
    @DisplayName("getAdmissionFormsTable - empty returns empty list")
    void getAdmissionFormsTable_emptyList() {
        when(admissionFormRepository.findAll()).thenReturn(List.of());

        List<Map<String, Object>> result = adminDataExportService.getAdmissionFormsTable();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getAdmissionFormsTable - with data maps all fields")
    void getAdmissionFormsTable_withData_mapsFields() {
        AdmissionForm form = new AdmissionForm();
        form.setId(1L);
        form.setFullName("Dewi");
        form.setNik("1234567890123456");
        form.setEmail("dewi@test.com");
        form.setPhoneNumber("08123456789");
        form.setGender("Perempuan");
        form.setReligion("Kristen");
        form.setCity("Medan");
        form.setProvince("Sumut");
        form.setSchoolOrigin("SMA N 1 Medan");
        form.setProgramStudi1("Teknik Informatika");
        form.setCreatedAt(LocalDateTime.now());

        when(admissionFormRepository.findAll()).thenReturn(List.of(form));

        List<Map<String, Object>> result = adminDataExportService.getAdmissionFormsTable();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("studentName")).isEqualTo("Dewi");
        assertThat(result.get(0).get("email")).isEqualTo("dewi@test.com");
    }

    // ===== getReenrollmentsTable =====

    @Test
    @DisplayName("getReenrollmentsTable - empty returns empty list")
    void getReenrollmentsTable_emptyList() {
        when(reenrollmentRepository.findAll()).thenReturn(List.of());

        List<Map<String, Object>> result = adminDataExportService.getReenrollmentsTable();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getReenrollmentsTable - null student fields handled gracefully")
    void getReenrollmentsTable_nullStudent_handlesGracefully() {
        ReEnrollment re = new ReEnrollment();
        re.setId(1L);
        re.setStudent(null);
        re.setStatus(ReEnrollment.ReEnrollmentStatus.INCOMPLETE);
        re.setCreatedAt(LocalDateTime.now());

        when(reenrollmentRepository.findAll()).thenReturn(List.of(re));

        List<Map<String, Object>> result = adminDataExportService.getReenrollmentsTable();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("studentName")).isEqualTo("N/A");
    }

    @Test
    @DisplayName("getReenrollmentsTable - with documents included in result")
    void getReenrollmentsTable_withDocuments_includesDocumentList() {
        User user = new User();
        user.setEmail("ree@test.com");
        Student student = new Student();
        student.setFullName("Eko");
        student.setNik("9999888877776666");
        student.setUser(user);
        ReEnrollment re = new ReEnrollment();
        re.setId(2L);
        re.setStudent(student);
        re.setStatus(ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        re.setKtpFile("uploads/reenrollment/2/ktp.jpg");
        re.setIjazahFile("uploads/reenrollment/2/ijazah.pdf");
        re.setCreatedAt(LocalDateTime.now());

        when(reenrollmentRepository.findAll()).thenReturn(List.of(re));

        List<Map<String, Object>> result = adminDataExportService.getReenrollmentsTable();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("studentName")).isEqualTo("Eko");
        @SuppressWarnings("unchecked")
        java.util.List<Map<String, String>> docs = (java.util.List<Map<String, String>>) result.get(0).get("documents");
        assertThat(docs).hasSizeGreaterThanOrEqualTo(2);
    }

    // ===== getHasilAkhirTable =====

    @Test
    @DisplayName("getHasilAkhirTable - empty returns empty list")
    void getHasilAkhirTable_emptyList() {
        when(hasilAkhirRepository.findAll()).thenReturn(List.of());

        List<Map<String, Object>> result = adminDataExportService.getHasilAkhirTable();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getHasilAkhirTable - with data returns all rows")
    void getHasilAkhirTable_withData_returnsRows() {
        User user = new User();
        user.setEmail("f@test.com");
        Student student = new Student();
        student.setFullName("Fani");
        student.setNik("1111222233334444");
        student.setUser(user);
        HasilAkhir ha = new HasilAkhir();
        ha.setId(1L);
        ha.setStudent(student);
        ha.setNomorRegistrasi("REG-100");
        ha.setBrivaNumber("BRV-100");
        ha.setBrivaAmount(BigDecimal.valueOf(2000000L));
        ha.setJumlahCicilan(3);
        ha.setWaveType(RegistrationPeriod.WaveType.REGULAR_TEST);
        ha.setSelectionType("SNBT");
        ha.setProgramStudiName("Kedokteran");
        ha.setStatus(HasilAkhir.HasilAkhirStatus.ACTIVE);
        ha.setCreatedAt(LocalDateTime.now());

        when(hasilAkhirRepository.findAll()).thenReturn(List.of(ha));

        List<Map<String, Object>> result = adminDataExportService.getHasilAkhirTable();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("nomorRegistrasi")).isEqualTo("REG-100");
        assertThat(result.get(0).get("waveType")).isEqualTo("REGULAR_TEST");
    }

    // ===== getHasilAkhirByWave =====

    @Test
    @DisplayName("getHasilAkhirByWave - empty returns empty list")
    void getHasilAkhirByWave_emptyList() {
        when(hasilAkhirRepository.findAll()).thenReturn(List.of());

        List<Map<String, Object>> result = adminDataExportService.getHasilAkhirByWave(RegistrationPeriod.WaveType.REGULAR_TEST);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getHasilAkhirByWave - filters by wave type")
    void getHasilAkhirByWave_filtersCorrectly() {
        HasilAkhir ha1 = new HasilAkhir();
        ha1.setId(1L);
        ha1.setWaveType(RegistrationPeriod.WaveType.REGULAR_TEST);
        ha1.setStatus(HasilAkhir.HasilAkhirStatus.ACTIVE);
        Student s1 = new Student();
        User u1 = new User();
        u1.setEmail("a@test.com");
        s1.setFullName("A");
        s1.setUser(u1);
        ha1.setStudent(s1);
        ha1.setNomorRegistrasi("R1");
        ha1.setBrivaNumber("B1");
        ha1.setBrivaAmount(BigDecimal.valueOf(100));
        ha1.setJumlahCicilan(1);
        ha1.setCreatedAt(LocalDateTime.now());

        HasilAkhir ha2 = new HasilAkhir();
        ha2.setId(2L);
        ha2.setWaveType(RegistrationPeriod.WaveType.EARLY_NO_TEST);
        ha2.setStatus(HasilAkhir.HasilAkhirStatus.ACTIVE);
        Student s2 = new Student();
        User u2 = new User();
        u2.setEmail("b@test.com");
        s2.setFullName("B");
        s2.setUser(u2);
        ha2.setStudent(s2);
        ha2.setNomorRegistrasi("R2");
        ha2.setBrivaNumber("B2");
        ha2.setBrivaAmount(BigDecimal.valueOf(200));
        ha2.setJumlahCicilan(2);
        ha2.setCreatedAt(LocalDateTime.now());

        when(hasilAkhirRepository.findAll()).thenReturn(List.of(ha1, ha2));

        List<Map<String, Object>> result = adminDataExportService.getHasilAkhirByWave(RegistrationPeriod.WaveType.REGULAR_TEST);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("waveType")).isEqualTo("REGULAR_TEST");
    }
}
