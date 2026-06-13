package com.uhn.pmb.service;

import com.uhn.pmb.dto.FormValidationRejectRequest;
import com.uhn.pmb.entity.*;
import com.uhn.pmb.repository.*;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FormValidationServiceTest {

    @Mock private FormValidationRepository formValidationRepository;
    @Mock private FormRepairStatusRepository formRepairStatusRepository;
    @Mock private AdmissionFormRepository admissionFormRepository;
    @Mock private RegistrationStatusRepository registrationStatusRepository;
    @Mock private CicilanRequestRepository cicilanRequestRepository;
    @Mock private VirtualAccountRepository virtualAccountRepository;
    @Mock private JenisSeleksiRepository jenisSeleksiRepository;
    @Mock private ReEnrollmentRepository reEnrollmentRepository;
    @Mock private RegistrationStatusService registrationStatusService;
    @Mock private ValidationStatusTrackerService validationStatusTrackerService;
    @Mock private HasilAkhirService hasilAkhirService;
    @Mock private EmailService emailService;

    @InjectMocks
    private FormValidationService formValidationService;

    private FormValidation buildValidation(Long id) {
        FormValidation fv = new FormValidation();
        fv.setId(id);
        fv.setCreatedAt(LocalDateTime.now());
        fv.setValidationStatus(FormValidation.ValidationStatus.PENDING);
        fv.setPaymentStatus(FormValidation.PaymentStatus.PENDING);
        return fv;
    }

    private User buildAdmin() {
        User admin = new User();
        admin.setId(1L);
        admin.setEmail("admin@test.com");
        return admin;
    }

    // ===== findAll =====

    @Test
    @DisplayName("findAll - returns all validations")
    void findAll_returnsAll() {
        when(formValidationRepository.findAll()).thenReturn(List.of(buildValidation(1L)));

        List<FormValidation> result = formValidationService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findAll - empty list returns empty")
    void findAll_empty_returnsEmpty() {
        when(formValidationRepository.findAll()).thenReturn(List.of());

        List<FormValidation> result = formValidationService.findAll();

        assertThat(result).isEmpty();
    }

    // ===== findById =====

    @Test
    @DisplayName("findById - found returns optional with value")
    void findById_found_returnsValue() {
        FormValidation fv = buildValidation(1L);
        when(formValidationRepository.findById(1L)).thenReturn(Optional.of(fv));

        Optional<FormValidation> result = formValidationService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findById - not found returns empty")
    void findById_notFound_returnsEmpty() {
        when(formValidationRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<FormValidation> result = formValidationService.findById(999L);

        assertThat(result).isEmpty();
    }

    // ===== approve =====

    @Test
    @DisplayName("approve - validation not found throws RuntimeException")
    void approve_notFound_throwsException() {
        when(formValidationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> formValidationService.approve(999L, buildAdmin()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("tidak ditemukan");
    }

    @Test
    @DisplayName("approve - no admission form just saves validation")
    void approve_noAdmissionForm_savesValidation() {
        FormValidation fv = buildValidation(1L);
        fv.setAdmissionForm(null);
        when(formValidationRepository.findById(1L)).thenReturn(Optional.of(fv));
        when(formValidationRepository.save(any())).thenReturn(fv);

        formValidationService.approve(1L, buildAdmin());

        verify(formValidationRepository).save(any());
        assertThat(fv.getValidationStatus()).isEqualTo(FormValidation.ValidationStatus.APPROVED);
    }

    @Test
    @DisplayName("approve - with admission form updates form status to VERIFIED")
    void approve_withAdmissionForm_updatesFormStatus() {
        User studentUser = new User();
        studentUser.setId(2L);
        studentUser.setEmail("student@test.com");

        Student student = new Student();
        student.setId(10L);
        student.setUser(studentUser);

        AdmissionForm form = new AdmissionForm();
        form.setId(5L);
        form.setStudent(student);
        form.setStatus(AdmissionForm.FormStatus.SUBMITTED);

        FormValidation fv = buildValidation(1L);
        fv.setAdmissionForm(form);
        fv.setStudent(student);

        when(formValidationRepository.findById(1L)).thenReturn(Optional.of(fv));
        when(formValidationRepository.save(any())).thenReturn(fv);
        when(admissionFormRepository.save(any())).thenReturn(form);

        RegistrationStatus reenrollStatus = new RegistrationStatus();
        reenrollStatus.setStatus(RegistrationStatus.RegistrationStatus_Enum.MENUNGGU_VERIFIKASI);
        when(registrationStatusService.getOrCreateStatus(any(), any())).thenReturn(reenrollStatus);
        when(registrationStatusRepository.save(any())).thenReturn(reenrollStatus);

        formValidationService.approve(1L, buildAdmin());

        assertThat(form.getStatus()).isEqualTo(AdmissionForm.FormStatus.VERIFIED);
        verify(admissionFormRepository).save(form);
    }

    // ===== reject =====

    @Test
    @DisplayName("reject - validation not found throws RuntimeException")
    void reject_notFound_throwsException() {
        when(formValidationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> formValidationService.reject(999L, null, buildAdmin()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("reject - no form just saves rejection")
    void reject_noForm_savesRejection() {
        FormValidation fv = buildValidation(1L);
        fv.setAdmissionForm(null);
        when(formValidationRepository.findById(1L)).thenReturn(Optional.of(fv));
        when(formValidationRepository.save(any())).thenReturn(fv);

        FormValidationRejectRequest req = new FormValidationRejectRequest();
        req.setReason("Data tidak lengkap");
        req.setTopic("Dokumen");

        formValidationService.reject(1L, req, buildAdmin());

        assertThat(fv.getValidationStatus()).isEqualTo(FormValidation.ValidationStatus.REJECTED);
        assertThat(fv.getRejectionReason()).isEqualTo("Data tidak lengkap");
    }

    @Test
    @DisplayName("reject - with form updates form status to REJECTED")
    void reject_withForm_updatesFormStatus() {
        User studentUser = new User();
        studentUser.setId(2L);
        studentUser.setEmail("student@test.com");
        Student student = new Student();
        student.setId(10L);
        student.setUser(studentUser);
        AdmissionForm form = new AdmissionForm();
        form.setId(5L);
        form.setStudent(student);
        form.setStatus(AdmissionForm.FormStatus.SUBMITTED);

        FormValidation fv = buildValidation(1L);
        fv.setAdmissionForm(form);
        fv.setStudent(student);

        when(formValidationRepository.findById(1L)).thenReturn(Optional.of(fv));
        when(formValidationRepository.save(any())).thenReturn(fv);
        when(admissionFormRepository.save(any())).thenReturn(form);

        FormValidationRejectRequest req = new FormValidationRejectRequest();
        req.setReason("Foto tidak jelas");
        req.setTopic("Foto");

        formValidationService.reject(1L, req, buildAdmin());

        assertThat(form.getStatus()).isEqualTo(AdmissionForm.FormStatus.REJECTED);
    }

    @Test
    @DisplayName("reject - null request uses default values")
    void reject_nullRequest_usesDefaults() {
        FormValidation fv = buildValidation(1L);
        fv.setAdmissionForm(null);
        when(formValidationRepository.findById(1L)).thenReturn(Optional.of(fv));
        when(formValidationRepository.save(any())).thenReturn(fv);

        formValidationService.reject(1L, null, buildAdmin());

        assertThat(fv.getRejectionTopic()).isEqualTo("Lainnya");
        assertThat(fv.getRejectionReason()).isEqualTo("");
    }

    // ===== markRevisionNeeded =====

    @Test
    @DisplayName("markRevisionNeeded - not found throws RuntimeException")
    void markRevisionNeeded_notFound_throwsException() {
        when(formValidationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> formValidationService.markRevisionNeeded(999L, null, buildAdmin()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("markRevisionNeeded - sets REVISION_NEEDED status")
    void markRevisionNeeded_setsRevisionStatus() {
        FormValidation fv = buildValidation(1L);
        fv.setAdmissionForm(null);
        when(formValidationRepository.findById(1L)).thenReturn(Optional.of(fv));
        when(formValidationRepository.save(any())).thenReturn(fv);
        when(formRepairStatusRepository.findByFormValidationId(1L)).thenReturn(Optional.empty());

        FormValidationRejectRequest req = new FormValidationRejectRequest();
        req.setReason("Perbaiki NIK");
        req.setTopic("Data Diri");
        req.setRevisionNumber(2);

        formValidationService.markRevisionNeeded(1L, req, buildAdmin());

        assertThat(fv.getValidationStatus()).isEqualTo(FormValidation.ValidationStatus.REVISION_NEEDED);
        assertThat(fv.getRevisionNumber()).isEqualTo(2);
    }

    @Test
    @DisplayName("markRevisionNeeded - with repair status resets to BELUM_PERBAIKAN")
    void markRevisionNeeded_resetsRepairStatus() {
        FormValidation fv = buildValidation(1L);
        fv.setAdmissionForm(null);
        when(formValidationRepository.findById(1L)).thenReturn(Optional.of(fv));
        when(formValidationRepository.save(any())).thenReturn(fv);

        FormRepairStatus repairStatus = new FormRepairStatus();
        repairStatus.setStatus(FormRepairStatus.RepairStatus.SUDAH_PERBAIKAN);
        repairStatus.setUpdatedAt(LocalDateTime.now().minusDays(1));
        when(formRepairStatusRepository.findByFormValidationId(1L)).thenReturn(Optional.of(repairStatus));
        when(formRepairStatusRepository.save(any())).thenReturn(repairStatus);

        formValidationService.markRevisionNeeded(1L, null, buildAdmin());

        assertThat(repairStatus.getStatus()).isEqualTo(FormRepairStatus.RepairStatus.BELUM_PERBAIKAN);
    }

    // ===== getFormsForValidationDashboard =====

    @Test
    @DisplayName("getFormsForValidationDashboard - empty list returns empty")
    void getFormsForValidationDashboard_empty_returnsEmpty() {
        when(formValidationRepository.findAll()).thenReturn(List.of());

        var result = formValidationService.getFormsForValidationDashboard();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getFormsForValidationDashboard - student with null user is filtered out")
    void getFormsForValidationDashboard_nullStudent_filtered() {
        FormValidation fv = buildValidation(1L);
        fv.setStudent(null);
        when(formValidationRepository.findAll()).thenReturn(List.of(fv));

        var result = formValidationService.getFormsForValidationDashboard();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getFormsForValidationDashboard - student with SELESAI form_submission status passes filter and maps")
    void getFormsForValidationDashboard_selesaiStatus_returnsMappedResult() {
        User studentUser = new User();
        studentUser.setId(2L);
        studentUser.setEmail("student@test.com");

        Student student = new Student();
        student.setId(10L);
        student.setFullName("Test Student");
        student.setUser(studentUser);

        AdmissionForm form = new AdmissionForm();
        form.setId(5L);
        form.setStudent(student);
        form.setSubmittedAt(LocalDateTime.now());

        FormValidation fv = buildValidation(1L);
        fv.setAdmissionForm(form);
        fv.setStudent(student);
        fv.setVirtualAccountNumber("-");

        RegistrationStatus formSubmissionStatus = new RegistrationStatus();
        formSubmissionStatus.setStatus(RegistrationStatus.RegistrationStatus_Enum.SELESAI);

        when(formValidationRepository.findAll()).thenReturn(List.of(fv));
        when(registrationStatusRepository.findByUserAndStage(
                eq(studentUser), eq(RegistrationStatus.RegistrationStage.FORM_SUBMISSION)))
                .thenReturn(Optional.of(formSubmissionStatus));
        when(registrationStatusRepository.findByUserAndStage(
                eq(studentUser), eq(RegistrationStatus.RegistrationStage.PAYMENT_BRIVA)))
                .thenReturn(Optional.empty());
        when(registrationStatusRepository.findByUserAndStage(
                eq(studentUser), eq(RegistrationStatus.RegistrationStage.PAYMENT_CICILAN_1)))
                .thenReturn(Optional.empty());
        when(registrationStatusRepository.findByUserAndStage(
                eq(studentUser), eq(RegistrationStatus.RegistrationStage.PSYCHO_EXAM)))
                .thenReturn(Optional.empty());
        when(registrationStatusRepository.findByUserAndStage(
                eq(studentUser), eq(RegistrationStatus.RegistrationStage.DAFTAR_ULANG)))
                .thenReturn(Optional.empty());
        when(cicilanRequestRepository.findByStudentIdAndStatus(eq(10L), any()))
                .thenReturn(List.of());
        when(formRepairStatusRepository.findByFormValidationId(1L)).thenReturn(Optional.empty());
        when(reEnrollmentRepository.findByStudent_Id(10L)).thenReturn(Optional.empty());

        var result = formValidationService.getFormsForValidationDashboard();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("studentName")).isEqualTo("Test Student");
    }

    @Test
    @DisplayName("getFormsForValidationDashboard - PAID payment status is included in result")
    void getFormsForValidationDashboard_paidPayment_includesPaymentStatus() {
        User studentUser = new User();
        studentUser.setId(3L);
        studentUser.setEmail("student2@test.com");

        Student student = new Student();
        student.setId(11L);
        student.setFullName("Student Paid");
        student.setUser(studentUser);

        AdmissionForm form = new AdmissionForm();
        form.setId(6L);
        form.setStudent(student);

        FormValidation fv = buildValidation(2L);
        fv.setAdmissionForm(form);
        fv.setStudent(student);
        fv.setPaymentAmount(500000L);

        RegistrationStatus formSubmissionStatus = new RegistrationStatus();
        formSubmissionStatus.setStatus(RegistrationStatus.RegistrationStatus_Enum.SELESAI);

        RegistrationStatus paymentStatus = new RegistrationStatus();
        paymentStatus.setStatus(RegistrationStatus.RegistrationStatus_Enum.SELESAI);

        RegistrationStatus examStatus = new RegistrationStatus();
        examStatus.setStatus(RegistrationStatus.RegistrationStatus_Enum.SELESAI);

        RegistrationStatus reenrollStatus = new RegistrationStatus();
        reenrollStatus.setStatus(RegistrationStatus.RegistrationStatus_Enum.SELESAI);

        FormRepairStatus repairStatus = new FormRepairStatus();
        repairStatus.setStatus(FormRepairStatus.RepairStatus.SUDAH_PERBAIKAN);

        when(formValidationRepository.findAll()).thenReturn(List.of(fv));
        when(registrationStatusRepository.findByUserAndStage(
                eq(studentUser), eq(RegistrationStatus.RegistrationStage.FORM_SUBMISSION)))
                .thenReturn(Optional.of(formSubmissionStatus));
        when(registrationStatusRepository.findByUserAndStage(
                eq(studentUser), eq(RegistrationStatus.RegistrationStage.PAYMENT_BRIVA)))
                .thenReturn(Optional.of(paymentStatus));
        when(registrationStatusRepository.findByUserAndStage(
                eq(studentUser), eq(RegistrationStatus.RegistrationStage.PAYMENT_CICILAN_1)))
                .thenReturn(Optional.of(paymentStatus));
        when(registrationStatusRepository.findByUserAndStage(
                eq(studentUser), eq(RegistrationStatus.RegistrationStage.PSYCHO_EXAM)))
                .thenReturn(Optional.of(examStatus));
        when(registrationStatusRepository.findByUserAndStage(
                eq(studentUser), eq(RegistrationStatus.RegistrationStage.DAFTAR_ULANG)))
                .thenReturn(Optional.of(reenrollStatus));
        when(cicilanRequestRepository.findByStudentIdAndStatus(eq(11L), any()))
                .thenReturn(List.of());
        when(formRepairStatusRepository.findByFormValidationId(2L))
                .thenReturn(Optional.of(repairStatus));
        when(reEnrollmentRepository.findByStudent_Id(11L)).thenReturn(Optional.empty());

        var result = formValidationService.getFormsForValidationDashboard();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("paymentStatus")).isEqualTo("PAID");
        assertThat(result.get(0).get("examStatus")).isEqualTo("COMPLETED");
    }

    // ===== getFormDetails =====

    @Test
    @DisplayName("getFormDetails - form validation not found throws exception")
    void getFormDetails_notFound_throwsException() {
        when(formValidationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> formValidationService.getFormDetails(999L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getFormDetails - found returns complete details map")
    void getFormDetails_found_returnsCompleteDetails() {
        User studentUser = new User();
        studentUser.setId(2L);
        studentUser.setEmail("student@test.com");

        Student student = new Student();
        student.setId(10L);
        student.setFullName("Budi Santoso");
        student.setUser(studentUser);

        AdmissionForm form = new AdmissionForm();
        form.setId(5L);
        form.setStudent(student);
        form.setFullName("Budi Santoso");
        form.setNik("1234567890123456");
        form.setProgramStudi1("Teknik Informatika");
        form.setProgramStudi2("Sistem Informasi");
        form.setProgramStudi3("Manajemen");
        form.setFormType(SelectionType.FormType.NON_MEDICAL);
        form.setAddressMedan("Jl. Sudirman No. 1");
        form.setResidenceInfo("Kos");
        form.setSubdistrict("Kelurahan A");
        form.setDistrict("Kecamatan B");
        form.setCity("Medan");
        form.setProvince("Sumatera Utara");
        form.setFatherName("Budi Sr");
        form.setFatherNik("1111111111111111");
        form.setMotherName("Ibu Sr");
        form.setMotherNik("2222222222222222");
        form.setParentSubdistrict("Kelurahan C");
        form.setParentCity("Medan");
        form.setParentProvince("Sumatera Utara");
        form.setSchoolOrigin("SMA Negeri 1");
        form.setSchoolMajor("IPA");
        form.setSchoolYear(2022);
        form.setNisn("1234567890");
        form.setSchoolCity("Medan");
        form.setSchoolProvince("Sumatera Utara");
        form.setPhotoIdPath("uploads/photo.jpg");
        form.setCertificatePath("uploads/cert.pdf");
        form.setTranscriptPath("uploads/trans.pdf");
        form.setStatus(AdmissionForm.FormStatus.SUBMITTED);
        form.setSubmittedAt(LocalDateTime.now());
        form.setCreatedAt(LocalDateTime.now());
        form.setUpdatedAt(LocalDateTime.now());

        FormValidation validation = buildValidation(1L);
        validation.setAdmissionForm(form);
        validation.setStudent(student);
        validation.setValidationStatus(FormValidation.ValidationStatus.PENDING);
        validation.setPaymentStatus(FormValidation.PaymentStatus.PENDING);
        validation.setVirtualAccountNumber("1234567890");
        validation.setPaymentAmount(500000L);
        validation.setCreatedAt(LocalDateTime.now());

        when(formValidationRepository.findById(1L)).thenReturn(Optional.of(validation));

        Map<String, Object> result = formValidationService.getFormDetails(1L);

        assertThat(result).isNotNull();
        assertThat(result.get("id")).isEqualTo(1L);
        assertThat(result.get("formId")).isEqualTo(5L);
        assertThat(result.get("studentId")).isEqualTo(10L);
        assertThat(result.get("student")).isNotNull();
        assertThat(result.get("programChoice")).isNotNull();
        assertThat(result.get("address")).isNotNull();
        assertThat(result.get("parents")).isNotNull();
        assertThat(result.get("school")).isNotNull();
        assertThat(result.get("documents")).isNotNull();
        assertThat(result.get("validation")).isNotNull();
    }

    @Test
    @DisplayName("getFormDetails - form with period and jenisSeleksiId includes waveType")
    void getFormDetails_withPeriodAndWaveType_returnsWaveType() {
        User studentUser = new User();
        studentUser.setId(2L);
        studentUser.setEmail("student@test.com");

        Student student = new Student();
        student.setId(10L);
        student.setUser(studentUser);

        RegistrationPeriod period = new RegistrationPeriod();
        period.setId(1L);
        period.setWaveType(RegistrationPeriod.WaveType.RANKING_NO_TEST);

        AdmissionForm form = new AdmissionForm();
        form.setId(5L);
        form.setStudent(student);
        form.setPeriod(period);
        form.setJenisSeleksiId(2L);
        form.setSelectionTypeId(2L);

        FormValidation validation = buildValidation(1L);
        validation.setAdmissionForm(form);
        validation.setStudent(student);

        when(formValidationRepository.findById(1L)).thenReturn(Optional.of(validation));

        Map<String, Object> result = formValidationService.getFormDetails(1L);

        assertThat(result.get("waveType")).isEqualTo("RANKING_NO_TEST");
    }

    // ===== getAdmissionFormStudentDetails =====

    @Test
    @DisplayName("getAdmissionFormStudentDetails - no form throws exception")
    void getAdmissionFormStudentDetails_noForm_throwsException() {
        when(admissionFormRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> formValidationService.getAdmissionFormStudentDetails(99L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getAdmissionFormStudentDetails - found returns details")
    void getAdmissionFormStudentDetails_found_returnsDetails() {
        Student student = new Student();
        student.setId(10L);

        AdmissionForm form = new AdmissionForm();
        form.setId(5L);
        form.setStudent(student);
        form.setProgramStudi1("Teknik Informatika");
        form.setJenisSeleksiId(2L);

        JenisSeleksi js = new JenisSeleksi();
        js.setId(2L);
        js.setNama("Seleksi Reguler");

        when(admissionFormRepository.findAll()).thenReturn(List.of(form));
        when(jenisSeleksiRepository.findById(2L)).thenReturn(Optional.of(js));
        when(cicilanRequestRepository.findByStudentIdAndStatus(eq(10L), any()))
                .thenReturn(List.of());

        Map<String, Object> result = formValidationService.getAdmissionFormStudentDetails(10L);

        assertThat(result).isNotNull();
        assertThat(result.get("programStudiName")).isEqualTo("Teknik Informatika");
        assertThat(result.get("jenisSeleksiName")).isEqualTo("Seleksi Reguler");
    }

    @Test
    @DisplayName("getAdmissionFormStudentDetails - with period returns waveType")
    void getAdmissionFormStudentDetails_withPeriod_returnsWaveType() {
        Student student = new Student();
        student.setId(10L);

        RegistrationPeriod period = new RegistrationPeriod();
        period.setId(1L);
        period.setWaveType(RegistrationPeriod.WaveType.RANKING_NO_TEST);

        AdmissionForm form = new AdmissionForm();
        form.setId(5L);
        form.setStudent(student);
        form.setPeriod(period);

        when(admissionFormRepository.findAll()).thenReturn(List.of(form));
        when(cicilanRequestRepository.findByStudentIdAndStatus(eq(10L), any()))
                .thenReturn(List.of());

        Map<String, Object> result = formValidationService.getAdmissionFormStudentDetails(10L);

        assertThat(result.get("registrationPeriodWaveType")).isEqualTo("RANKING_NO_TEST");
    }

    // ===== updateRepairStatus =====

    @Test
    @DisplayName("updateRepairStatus - no validations returns success=true")
    void updateRepairStatus_noValidations_returnsSuccess() {
        when(formValidationRepository.findAll()).thenReturn(List.of());

        Map<String, Object> result = formValidationService.updateRepairStatus(99L, "SUDAH_PERBAIKAN");

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("message")).isEqualTo("Repair status tidak perlu di-update");
    }

    @Test
    @DisplayName("updateRepairStatus - updates existing repair status to SUDAH_PERBAIKAN")
    void updateRepairStatus_existingRecord_updatesToSudahPerbaikan() {
        Student student = new Student();
        student.setId(10L);

        FormValidation fv = buildValidation(1L);
        fv.setStudent(student);

        FormRepairStatus repairStatus = new FormRepairStatus();
        repairStatus.setStatus(FormRepairStatus.RepairStatus.BELUM_PERBAIKAN);
        repairStatus.setUpdatedAt(LocalDateTime.now().minusDays(1));

        when(formValidationRepository.findAll()).thenReturn(List.of(fv));
        when(formRepairStatusRepository.findByFormValidationId(1L)).thenReturn(Optional.of(repairStatus));
        when(formRepairStatusRepository.save(any())).thenReturn(repairStatus);

        Map<String, Object> result = formValidationService.updateRepairStatus(10L, "SUDAH_PERBAIKAN");

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(repairStatus.getStatus()).isEqualTo(FormRepairStatus.RepairStatus.SUDAH_PERBAIKAN);
    }

    @Test
    @DisplayName("updateRepairStatus - creates new repair status when not present")
    void updateRepairStatus_noExistingRecord_createsNew() {
        Student student = new Student();
        student.setId(10L);

        FormValidation fv = buildValidation(1L);
        fv.setStudent(student);

        when(formValidationRepository.findAll()).thenReturn(List.of(fv));
        when(formRepairStatusRepository.findByFormValidationId(1L)).thenReturn(Optional.empty());
        when(formRepairStatusRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = formValidationService.updateRepairStatus(10L, "BELUM_PERBAIKAN");

        assertThat(result.get("success")).isEqualTo(true);
        verify(formRepairStatusRepository).save(any());
    }

    // ===== sendRevisionNeededEmail (via markRevisionNeeded with form that has student.user) =====

    @Test
    @DisplayName("markRevisionNeeded - with form and student triggers sendRevisionNeededEmail")
    void markRevisionNeeded_withFormAndStudent_sendsRevisionEmail() {
        User studentUser = new User();
        studentUser.setId(2L);
        studentUser.setEmail("student@test.com");

        Student student = new Student();
        student.setId(10L);
        student.setFullName("Budi Santoso");
        student.setUser(studentUser);

        AdmissionForm form = new AdmissionForm();
        form.setId(5L);
        form.setStudent(student);
        form.setStatus(AdmissionForm.FormStatus.SUBMITTED);

        FormValidation fv = buildValidation(1L);
        fv.setAdmissionForm(form);
        fv.setStudent(student);

        when(formValidationRepository.findById(1L)).thenReturn(Optional.of(fv));
        when(formValidationRepository.save(any())).thenReturn(fv);
        when(formRepairStatusRepository.findByFormValidationId(1L)).thenReturn(Optional.empty());
        when(admissionFormRepository.save(any())).thenReturn(form);

        FormValidationRejectRequest req = new FormValidationRejectRequest();
        req.setReason("Perbaiki NIK");
        req.setTopic("Data Diri");
        req.setRevisionNumber(1);

        formValidationService.markRevisionNeeded(1L, req, buildAdmin());

        assertThat(fv.getValidationStatus()).isEqualTo(FormValidation.ValidationStatus.REVISION_NEEDED);
        assertThat(form.getStatus()).isEqualTo(AdmissionForm.FormStatus.SUBMITTED);
    }

    // ===== sendApprovalEmail via approve with student that has ALREADY_FINISHED re-enroll =====

    @Test
    @DisplayName("approve - reenroll already SELESAI does not change status")
    void approve_reenrollAlreadySelesai_doesNotChangeStatus() {
        User studentUser = new User();
        studentUser.setId(2L);
        studentUser.setEmail("student@test.com");

        Student student = new Student();
        student.setId(10L);
        student.setUser(studentUser);

        AdmissionForm form = new AdmissionForm();
        form.setId(5L);
        form.setStudent(student);

        FormValidation fv = buildValidation(1L);
        fv.setAdmissionForm(form);
        fv.setStudent(student);

        RegistrationStatus reenrollStatus = new RegistrationStatus();
        reenrollStatus.setStatus(RegistrationStatus.RegistrationStatus_Enum.SELESAI);

        when(formValidationRepository.findById(1L)).thenReturn(Optional.of(fv));
        when(formValidationRepository.save(any())).thenReturn(fv);
        when(admissionFormRepository.save(any())).thenReturn(form);
        when(registrationStatusService.getOrCreateStatus(any(), any())).thenReturn(reenrollStatus);
        when(registrationStatusRepository.save(any())).thenReturn(reenrollStatus);

        formValidationService.approve(1L, buildAdmin());

        assertThat(reenrollStatus.getStatus()).isEqualTo(RegistrationStatus.RegistrationStatus_Enum.SELESAI);
    }

    @Test
    @DisplayName("approve - reenroll REJECTED does not change status")
    void approve_reenrollRejected_doesNotChangeStatus() {
        User studentUser = new User();
        studentUser.setId(2L);
        studentUser.setEmail("student@test.com");

        Student student = new Student();
        student.setId(10L);
        student.setUser(studentUser);

        AdmissionForm form = new AdmissionForm();
        form.setId(5L);
        form.setStudent(student);

        FormValidation fv = buildValidation(1L);
        fv.setAdmissionForm(form);
        fv.setStudent(student);

        RegistrationStatus reenrollStatus = new RegistrationStatus();
        reenrollStatus.setStatus(RegistrationStatus.RegistrationStatus_Enum.REJECTED);

        when(formValidationRepository.findById(1L)).thenReturn(Optional.of(fv));
        when(formValidationRepository.save(any())).thenReturn(fv);
        when(admissionFormRepository.save(any())).thenReturn(form);
        when(registrationStatusService.getOrCreateStatus(any(), any())).thenReturn(reenrollStatus);
        when(registrationStatusRepository.save(any())).thenReturn(reenrollStatus);

        formValidationService.approve(1L, buildAdmin());

        assertThat(reenrollStatus.getStatus()).isEqualTo(RegistrationStatus.RegistrationStatus_Enum.REJECTED);
    }
}
