package com.uhn.pmb.service;

import com.uhn.pmb.entity.*;
import com.uhn.pmb.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock private AdmissionFormRepository admissionFormRepository;
    @Mock private ReEnrollmentRepository reenrollmentRepository;
    @Mock private HasilAkhirRepository hasilAkhirRepository;
    @Mock private FormValidationRepository formValidationRepository;

    @InjectMocks
    private ExportService exportService;

    private Student buildStudent(String name, String email, String phone) {
        User user = User.builder().id(1L).email(email).build();
        return Student.builder().id(10L).fullName(name).phoneNumber(phone).user(user).build();
    }

    // ===== exportFormulirPembayaran =====

    @Test
    @DisplayName("exportFormulirPembayaran - empty list returns header only")
    void exportFormulirPembayaran_emptyList_returnsHeaderOnly() {
        when(admissionFormRepository.findByPeriod_Id(1L)).thenReturn(Collections.emptyList());

        byte[] result = exportService.exportFormulirPembayaran(1L);
        String csv = new String(result, StandardCharsets.UTF_8);

        assertThat(csv).contains("No Registrasi,Nama Siswa,Email");
        assertThat(csv.split("\n")).hasSize(1);
    }

    @Test
    @DisplayName("exportFormulirPembayaran - with data and null optional fields")
    void exportFormulirPembayaran_withDataNullFields_returnsCsv() {
        AdmissionForm form = new AdmissionForm();
        form.setStudent(buildStudent("Budi", "budi@test.com", null));
        form.setProgramStudi1(null);
        form.setJenisSeleksiId(null);
        form.setStatus(AdmissionForm.FormStatus.DRAFT);
        form.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        form.setUpdatedAt(LocalDateTime.of(2026, 1, 2, 10, 0));
        when(admissionFormRepository.findByPeriod_Id(1L)).thenReturn(List.of(form));

        byte[] result = exportService.exportFormulirPembayaran(1L);
        String csv = new String(result, StandardCharsets.UTF_8);

        assertThat(csv).contains("\"Budi\"");
        assertThat(csv).contains("budi@test.com");
        assertThat(csv).contains(",-,-,-,");
    }

    @Test
    @DisplayName("exportFormulirPembayaran - with data and all fields populated")
    void exportFormulirPembayaran_withDataAllFields_returnsCsv() {
        AdmissionForm form = new AdmissionForm();
        form.setStudent(buildStudent("Ani", "ani@test.com", "08123456789"));
        form.setProgramStudi1("Informatika");
        form.setJenisSeleksiId(5L);
        form.setStatus(AdmissionForm.FormStatus.VERIFIED);
        form.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        form.setUpdatedAt(LocalDateTime.of(2026, 1, 2, 10, 0));
        when(admissionFormRepository.findByPeriod_Id(2L)).thenReturn(List.of(form));

        byte[] result = exportService.exportFormulirPembayaran(2L);
        String csv = new String(result, StandardCharsets.UTF_8);

        assertThat(csv).contains("\"Ani\"");
        assertThat(csv).contains("08123456789");
        assertThat(csv).contains("Informatika");
        assertThat(csv).contains("5");
        assertThat(csv).contains("VERIFIED");
    }

    // ===== exportDaftarUlang =====

    @Test
    @DisplayName("exportDaftarUlang - empty list returns header only")
    void exportDaftarUlang_emptyList_returnsHeaderOnly() {
        when(reenrollmentRepository.findAll()).thenReturn(Collections.emptyList());

        byte[] result = exportService.exportDaftarUlang(1L);
        String csv = new String(result, StandardCharsets.UTF_8);

        assertThat(csv).contains("No,Nama Siswa,Email,Status Daftar Ulang");
        assertThat(csv.split("\n")).hasSize(1);
    }

    @Test
    @DisplayName("exportDaftarUlang - with data and null optional fields")
    void exportDaftarUlang_withDataNullFields_returnsCsv() {
        ReEnrollment re = new ReEnrollment();
        re.setStudent(buildStudent("Citra", "citra@test.com", null));
        re.setStatus(ReEnrollment.ReEnrollmentStatus.INCOMPLETE);
        re.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        re.setValidatedAt(null);
        re.setValidationNotes(null);
        when(reenrollmentRepository.findAll()).thenReturn(List.of(re));

        byte[] result = exportService.exportDaftarUlang(1L);
        String csv = new String(result, StandardCharsets.UTF_8);

        assertThat(csv).contains("\"Citra\"");
        assertThat(csv).contains("citra@test.com");
        assertThat(csv).contains(",-,-\n");
    }

    @Test
    @DisplayName("exportDaftarUlang - with data and all fields populated")
    void exportDaftarUlang_withDataAllFields_returnsCsv() {
        ReEnrollment re = new ReEnrollment();
        re.setStudent(buildStudent("Dewi", "dewi@test.com", null));
        re.setStatus(ReEnrollment.ReEnrollmentStatus.VALIDATED);
        re.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        re.setValidatedAt(LocalDateTime.of(2026, 1, 3, 10, 0));
        re.setValidationNotes("Dokumen lengkap, ada tanda \"khusus\"");
        when(reenrollmentRepository.findAll()).thenReturn(List.of(re));

        byte[] result = exportService.exportDaftarUlang(1L);
        String csv = new String(result, StandardCharsets.UTF_8);

        assertThat(csv).contains("\"Dewi\"");
        assertThat(csv).contains("VALIDATED");
        assertThat(csv).contains("\"\"khusus\"\"");
    }

    // ===== exportHasilAkhir =====

    @Test
    @DisplayName("exportHasilAkhir - empty list returns header only")
    void exportHasilAkhir_emptyList_returnsHeaderOnly() {
        when(hasilAkhirRepository.findAll()).thenReturn(Collections.emptyList());

        byte[] result = exportService.exportHasilAkhir(1L);
        String csv = new String(result, StandardCharsets.UTF_8);

        assertThat(csv).contains("No Registrasi,Nama Siswa,Program Studi");
        assertThat(csv.split("\n")).hasSize(1);
    }

    @Test
    @DisplayName("exportHasilAkhir - with data and null optional fields")
    void exportHasilAkhir_withDataNullFields_returnsCsv() {
        HasilAkhir result = new HasilAkhir();
        result.setStudent(buildStudent("Eko", "eko@test.com", null));
        result.setProgramStudiName(null);
        result.setSelectionType(null);
        result.setStatus(HasilAkhir.HasilAkhirStatus.ACTIVE);
        result.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        result.setBrivaNumber(null);
        when(hasilAkhirRepository.findAll()).thenReturn(List.of(result));

        byte[] csvBytes = exportService.exportHasilAkhir(1L);
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertThat(csv).contains("\"Eko\"");
        assertThat(csv).contains(",-,-,");
        assertThat(csv).endsWith(",-\n");
    }

    @Test
    @DisplayName("exportHasilAkhir - with data and all fields populated")
    void exportHasilAkhir_withDataAllFields_returnsCsv() {
        HasilAkhir result = new HasilAkhir();
        result.setStudent(buildStudent("Fina", "fina@test.com", null));
        result.setProgramStudiName("Manajemen");
        result.setSelectionType("Reguler");
        result.setStatus(HasilAkhir.HasilAkhirStatus.ACTIVE);
        result.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        result.setBrivaNumber("123456789");
        when(hasilAkhirRepository.findAll()).thenReturn(List.of(result));

        byte[] csvBytes = exportService.exportHasilAkhir(1L);
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertThat(csv).contains("\"Fina\"");
        assertThat(csv).contains("Manajemen");
        assertThat(csv).contains("Reguler");
        assertThat(csv).contains("123456789");
        assertThat(csv).contains("ACTIVE");
    }

    // ===== exportValidationReport =====

    @Test
    @DisplayName("exportValidationReport - empty list returns summary with zeros")
    void exportValidationReport_emptyList_returnsSummaryWithZeros() {
        when(formValidationRepository.findAll()).thenReturn(Collections.emptyList());

        byte[] result = exportService.exportValidationReport(1L);
        String csv = new String(result, StandardCharsets.UTF_8);

        assertThat(csv).contains("Total: 0");
        assertThat(csv).contains("Disetujui: 0");
        assertThat(csv).contains("Ditolak: 0");
        assertThat(csv).contains("Pending: 0");
    }

    @Test
    @DisplayName("exportValidationReport - with mixed statuses computes summary correctly")
    void exportValidationReport_mixedStatuses_computesSummary() {
        FormValidation approved = buildValidation("Gita", FormValidation.ValidationStatus.APPROVED, null, null);
        FormValidation rejected = buildValidation("Hadi", FormValidation.ValidationStatus.REJECTED, "Data tidak valid", null);
        FormValidation pending = buildValidation("Indra", FormValidation.ValidationStatus.PENDING, null, null);
        when(formValidationRepository.findAll()).thenReturn(List.of(approved, rejected, pending));

        byte[] result = exportService.exportValidationReport(1L);
        String csv = new String(result, StandardCharsets.UTF_8);

        assertThat(csv).contains("Total: 3");
        assertThat(csv).contains("Disetujui: 1");
        assertThat(csv).contains("Ditolak: 1");
        assertThat(csv).contains("Pending: 1");
        assertThat(csv).contains("\"Gita\"");
        assertThat(csv).contains("\"Hadi\"");
        assertThat(csv).contains("Data tidak valid");
    }

    @Test
    @DisplayName("exportValidationReport - with validatedBy returns email")
    void exportValidationReport_withValidatedBy_returnsUsername() {
        User validator = User.builder().id(2L).email("admin1@test.com").password("pass").role(User.UserRole.ADMIN_PUSAT).build();
        FormValidation val = buildValidation("Joko", FormValidation.ValidationStatus.APPROVED, null, validator);
        when(formValidationRepository.findAll()).thenReturn(List.of(val));

        byte[] result = exportService.exportValidationReport(1L);
        String csv = new String(result, StandardCharsets.UTF_8);

        assertThat(csv).contains("admin1@test.com");
    }

    @Test
    @DisplayName("exportValidationReport - without validatedBy and rejectionReason returns dash")
    void exportValidationReport_withoutValidatedByAndReason_returnsDash() {
        FormValidation val = buildValidation("Kiki", FormValidation.ValidationStatus.PENDING, null, null);
        when(formValidationRepository.findAll()).thenReturn(List.of(val));

        byte[] result = exportService.exportValidationReport(1L);
        String csv = new String(result, StandardCharsets.UTF_8);

        assertThat(csv).contains(",-,-\n");
    }

    private FormValidation buildValidation(String studentName, FormValidation.ValidationStatus status,
                                            String rejectionReason, User validatedBy) {
        FormValidation val = new FormValidation();
        val.setStudent(buildStudent(studentName, studentName.toLowerCase() + "@test.com", null));
        val.setValidationStatus(status);
        val.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        val.setUpdatedAt(LocalDateTime.of(2026, 1, 2, 10, 0));
        val.setValidatedBy(validatedBy);
        val.setRejectionReason(rejectionReason);
        return val;
    }
}