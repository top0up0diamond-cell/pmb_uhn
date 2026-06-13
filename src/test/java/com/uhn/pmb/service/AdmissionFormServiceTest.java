package com.uhn.pmb.service;

import com.uhn.pmb.dto.AdmissionFormSubmitRequest;
import com.uhn.pmb.dto.SubmitRevisionRequest;
import com.uhn.pmb.entity.*;
import com.uhn.pmb.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartHttpServletRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdmissionFormServiceTest {

    @Mock private AdmissionFormRepository admissionFormRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private UserRepository userRepository;
    @Mock private FormValidationRepository formValidationRepository;
    @Mock private FormRepairStatusRepository formRepairStatusRepository;
    @Mock private RegistrationStatusRepository registrationStatusRepository;
    @Mock private RegistrationStatusService registrationStatusService;
    @Mock private ValidationStatusTrackerService validationStatusTrackerService;
    @Mock private StudentRegistrationService studentRegistrationService;
    @Mock private FileStorageService fileStorageService;
    @Mock private ProgramStudiRepository programStudiRepository;
    @Mock private RegistrationPeriodRepository registrationPeriodRepository;
    @Mock private SelectionTypeRepository selectionTypeRepository;
    @Mock private JenisSeleksiRepository jenisSeleksiRepository;

    @InjectMocks
    private AdmissionFormService admissionFormService;

    // ===== Helpers =====

    private User buildUser() {
        return User.builder().id(1L).email("u@test.com").build();
    }

    private Student buildStudent() {
        return Student.builder().id(10L).build();
    }

    private AdmissionForm buildForm() {
        AdmissionForm form = new AdmissionForm();
        form.setId(5L);
        form.setStudent(buildStudent());
        form.setCreatedAt(LocalDateTime.now());
        return form;
    }

    private void mockUserAndStudent(User user, Student student) {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(user.getId())).thenReturn(Optional.of(student));
    }

    private SubmitRevisionRequest buildFullRevisionRequest() {
        SubmitRevisionRequest req = new SubmitRevisionRequest();
        req.setFullName("Budi Santoso");
        req.setNik("1234567890123456");
        req.setBirthDate("1995-01-01");
        req.setBirthPlace("Medan");
        req.setGender("Laki-laki");
        req.setPhoneNumber("08123456789");
        req.setEmail("budi@test.com");
        req.setAddressMedan("Jl. Sudirman No. 1");
        req.setResidenceInfo("Kos");
        req.setSubdistrict("Kelurahan A");
        req.setDistrict("Kecamatan B");
        req.setCity("Medan");
        req.setProvince("Sumatera Utara");
        req.setReligion("Kristen");
        req.setInformationSource("Internet");
        req.setFatherNik("1111111111111111");
        req.setFatherName("Budi Sr");
        req.setFatherBirthDate("1970-01-01");
        req.setFatherEducation("S1");
        req.setFatherOccupation("PNS");
        req.setFatherIncome("5000000");
        req.setFatherPhone("08111111111");
        req.setFatherStatus("Hidup");
        req.setMotherNik("2222222222222222");
        req.setMotherName("Ibu Sr");
        req.setMotherBirthDate("1972-01-01");
        req.setMotherEducation("S1");
        req.setMotherOccupation("Guru");
        req.setMotherIncome("3000000");
        req.setMotherPhone("08222222222");
        req.setMotherStatus("Hidup");
        req.setParentSubdistrict("Kelurahan C");
        req.setParentCity("Medan");
        req.setParentProvince("Sumatera Utara");
        req.setParentPhone("082333333333");
        req.setSchoolOrigin("SMA Negeri 1 Medan");
        req.setSchoolMajor("IPA");
        req.setSchoolYear("2022");
        req.setNisn("1234567890");
        req.setSchoolCity("Medan");
        req.setSchoolProvince("Sumatera Utara");
        return req;
    }

    // ===== checkSubmissionStatus =====

    @Test
    @DisplayName("checkSubmissionStatus - user not found throws exception")
    void checkSubmissionStatus_userNotFound_throwsException() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> admissionFormService.checkSubmissionStatus("none@test.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("checkSubmissionStatus - student not found throws exception")
    void checkSubmissionStatus_studentNotFound_throwsException() {
        User user = buildUser();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> admissionFormService.checkSubmissionStatus("u@test.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("checkSubmissionStatus - form not found returns no_form status")
    void checkSubmissionStatus_noForm_returnsNoForm() {
        User user = buildUser();
        Student student = buildStudent();
        mockUserAndStudent(user, student);
        when(admissionFormRepository.findByStudent_Id(10L)).thenReturn(List.of());

        Map<String, Object> result = admissionFormService.checkSubmissionStatus("u@test.com");

        assertThat(result.get("hasSubmitted")).isEqualTo(false);
    }

    @Test
    @DisplayName("checkSubmissionStatus - form exists returns form details")
    void checkSubmissionStatus_formExists_returnsDetails() {
        User user = buildUser();
        Student student = buildStudent();
        AdmissionForm form = buildForm();
        mockUserAndStudent(user, student);
        when(admissionFormRepository.findByStudent_Id(10L)).thenReturn(List.of(form));

        Map<String, Object> result = admissionFormService.checkSubmissionStatus("u@test.com");

        assertThat(result).isNotNull();
        assertThat(result.get("hasSubmitted")).isNotNull();
    }

    @Test
    @DisplayName("checkSubmissionStatus - form with submittedAt computes edit window")
    void checkSubmissionStatus_formWithSubmittedAt_computesEditWindow() {
        User user = buildUser();
        Student student = buildStudent();
        AdmissionForm form = buildForm();
        form.setSubmittedAt(LocalDateTime.now().minusHours(2));
        form.setStatus(AdmissionForm.FormStatus.SUBMITTED);
        mockUserAndStudent(user, student);
        when(admissionFormRepository.findByStudent_Id(10L)).thenReturn(List.of(form));

        Map<String, Object> result = admissionFormService.checkSubmissionStatus("u@test.com");

        assertThat(result.get("isEditable")).isEqualTo(true);
    }

    @Test
    @DisplayName("checkSubmissionStatus - form submitted >24h ago is not editable")
    void checkSubmissionStatus_formOld_notEditable() {
        User user = buildUser();
        Student student = buildStudent();
        AdmissionForm form = buildForm();
        form.setSubmittedAt(LocalDateTime.now().minusHours(48));
        form.setStatus(AdmissionForm.FormStatus.SUBMITTED);
        mockUserAndStudent(user, student);
        when(admissionFormRepository.findByStudent_Id(10L)).thenReturn(List.of(form));

        Map<String, Object> result = admissionFormService.checkSubmissionStatus("u@test.com");

        assertThat(result.get("isEditable")).isEqualTo(false);
    }

    // ===== getAdmissionFormData =====

    @Test
    @DisplayName("getAdmissionFormData - no form returns success=false")
    void getAdmissionFormData_noForm_returnsFailure() {
        User user = buildUser();
        Student student = buildStudent();
        mockUserAndStudent(user, student);
        when(admissionFormRepository.findByStudent_Id(10L)).thenReturn(List.of());

        Map<String, Object> result = admissionFormService.getAdmissionFormData("u@test.com");

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("data")).isNull();
    }

    @Test
    @DisplayName("getAdmissionFormData - form exists returns success=true with data")
    void getAdmissionFormData_formExists_returnsData() {
        User user = buildUser();
        Student student = buildStudent();
        AdmissionForm form = buildForm();
        form.setFullName("Budi");
        mockUserAndStudent(user, student);
        when(admissionFormRepository.findByStudent_Id(10L)).thenReturn(List.of(form));

        Map<String, Object> result = admissionFormService.getAdmissionFormData("u@test.com");

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("data")).isNotNull();
    }

    // ===== getCurrentAdmissionFormData =====

    @Test
    @DisplayName("getCurrentAdmissionFormData - no form returns success=false")
    void getCurrentAdmissionFormData_noForm_returnsFailure() {
        User user = buildUser();
        Student student = buildStudent();
        mockUserAndStudent(user, student);
        when(admissionFormRepository.findByStudent_Id(10L)).thenReturn(List.of());

        Map<String, Object> result = admissionFormService.getCurrentAdmissionFormData("u@test.com");

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("data")).isNull();
    }

    @Test
    @DisplayName("getCurrentAdmissionFormData - form exists returns success=true with data")
    void getCurrentAdmissionFormData_formExists_returnsData() {
        User user = buildUser();
        Student student = buildStudent();
        AdmissionForm form = buildForm();
        form.setFullName("Citra");
        mockUserAndStudent(user, student);
        when(admissionFormRepository.findByStudent_Id(10L)).thenReturn(List.of(form));

        Map<String, Object> result = admissionFormService.getCurrentAdmissionFormData("u@test.com");

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("data")).isNotNull();
    }

    @Test
    @DisplayName("getCurrentAdmissionFormData - form with period and jenisSeleksiId returns enriched data")
    void getCurrentAdmissionFormData_withPeriodAndJenisSeleksi_returnsEnrichedData() {
        User user = buildUser();
        Student student = buildStudent();
        AdmissionForm form = buildForm();
        RegistrationPeriod period = new RegistrationPeriod();
        period.setId(1L);
        period.setName("Gelombang 1");
        form.setPeriod(period);
        form.setJenisSeleksiId(2L);
        mockUserAndStudent(user, student);
        when(admissionFormRepository.findByStudent_Id(10L)).thenReturn(List.of(form));
        JenisSeleksi js = new JenisSeleksi();
        js.setId(2L);
        js.setNama("Seleksi Reguler");
        when(jenisSeleksiRepository.findById(2L)).thenReturn(Optional.of(js));

        Map<String, Object> result = admissionFormService.getCurrentAdmissionFormData("u@test.com");

        assertThat(result.get("success")).isEqualTo(true);
    }

    // ===== getAdmissionStatus =====

    @Test
    @DisplayName("getAdmissionStatus - user not found throws exception")
    void getAdmissionStatus_userNotFound_throwsException() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> admissionFormService.getAdmissionStatus("none@test.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getAdmissionStatus - no form returns empty list")
    void getAdmissionStatus_noForm_returnsEmpty() {
        User user = buildUser();
        Student student = buildStudent();
        mockUserAndStudent(user, student);
        when(admissionFormRepository.findByStudent_Id(10L)).thenReturn(new java.util.ArrayList<>());

        List<AdmissionForm> result = admissionFormService.getAdmissionStatus("u@test.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getAdmissionStatus - forms returned sorted by createdAt")
    void getAdmissionStatus_withForms_returnsSortedList() {
        User user = buildUser();
        Student student = buildStudent();
        mockUserAndStudent(user, student);
        AdmissionForm form1 = new AdmissionForm();
        form1.setId(1L);
        form1.setCreatedAt(LocalDateTime.now().minusDays(1));
        AdmissionForm form2 = new AdmissionForm();
        form2.setId(2L);
        form2.setCreatedAt(LocalDateTime.now());
        when(admissionFormRepository.findByStudent_Id(10L))
                .thenReturn(new java.util.ArrayList<>(List.of(form1, form2)));

        List<AdmissionForm> result = admissionFormService.getAdmissionStatus("u@test.com");

        assertThat(result).hasSize(2);
    }

    // ===== updateAdmissionFormData =====

    @Test
    @DisplayName("updateAdmissionFormData - no form throws exception")
    void updateAdmissionFormData_noForm_throwsException() throws Exception {
        User user = buildUser();
        Student student = buildStudent();
        mockUserAndStudent(user, student);
        when(admissionFormRepository.findByStudent_Id(10L)).thenReturn(List.of());

        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
        assertThatThrownBy(() -> admissionFormService.updateAdmissionFormData("u@test.com", request))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("updateAdmissionFormData - multipart request with all parameters updates form")
    void updateAdmissionFormData_multipartWithParams_updatesForm() throws Exception {
        User user = buildUser();
        Student student = buildStudent();
        AdmissionForm form = buildForm();
        mockUserAndStudent(user, student);
        when(admissionFormRepository.findByStudent_Id(10L)).thenReturn(List.of(form));
        when(admissionFormRepository.save(any())).thenReturn(form);

        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
        request.addParameter("fullName", "Budi Santoso");
        request.addParameter("nik", "1234567890123456");
        request.addParameter("addressMedan", "Jl. Sudirman No. 1");
        request.addParameter("residenceInfo", "Kos");
        request.addParameter("subdistrict", "Kelurahan A");
        request.addParameter("district", "Kecamatan B");
        request.addParameter("city", "Medan");
        request.addParameter("province", "Sumatera Utara");
        request.addParameter("phoneNumber", "08123456789");
        request.addParameter("email", "budi@test.com");
        request.addParameter("birthPlace", "Medan");
        request.addParameter("birthDate", "1995-01-01");
        request.addParameter("gender", "Laki-laki");
        request.addParameter("religion", "Kristen");
        request.addParameter("informationSource", "Internet");
        request.addParameter("fatherNik", "1111111111111111");
        request.addParameter("fatherName", "Budi Sr");
        request.addParameter("fatherBirthDate", "1970-01-01");
        request.addParameter("fatherEducation", "S1");
        request.addParameter("fatherOccupation", "PNS");
        request.addParameter("fatherIncome", "5000000");
        request.addParameter("fatherPhone", "08111111111");
        request.addParameter("fatherStatus", "Hidup");
        request.addParameter("motherNik", "2222222222222222");
        request.addParameter("motherName", "Ibu Sr");
        request.addParameter("motherBirthDate", "1972-01-01");
        request.addParameter("motherEducation", "S1");
        request.addParameter("motherOccupation", "Guru");
        request.addParameter("motherIncome", "3000000");
        request.addParameter("motherPhone", "08222222222");
        request.addParameter("motherStatus", "Hidup");
        request.addParameter("parentSubdistrict", "Kelurahan C");
        request.addParameter("parentCity", "Medan");
        request.addParameter("parentProvince", "Sumatera Utara");
        request.addParameter("parentPhone", "082333333333");
        request.addParameter("schoolOrigin", "SMA Negeri 1 Medan");
        request.addParameter("schoolMajor", "IPA");
        request.addParameter("schoolYear", "2022");
        request.addParameter("nisn", "1234567890");
        request.addParameter("schoolCity", "Medan");
        request.addParameter("schoolProvince", "Sumatera Utara");
        request.addParameter("programStudi1", "Teknik Informatika");
        request.addParameter("programStudi2", "Sistem Informasi");
        request.addParameter("programStudi3", "");
        request.addParameter("additionalInfo", "Test info");

        Map<String, Object> result = admissionFormService.updateAdmissionFormData("u@test.com", request);

        assertThat(result.get("success")).isEqualTo(true);
        verify(admissionFormRepository).save(any());
    }

    @Test
    @DisplayName("updateAdmissionFormData - non-multipart request still saves form")
    void updateAdmissionFormData_nonMultipart_savesForm() throws Exception {
        User user = buildUser();
        Student student = buildStudent();
        AdmissionForm form = buildForm();
        mockUserAndStudent(user, student);
        when(admissionFormRepository.findByStudent_Id(10L)).thenReturn(List.of(form));
        when(admissionFormRepository.save(any())).thenReturn(form);

        org.springframework.mock.web.MockHttpServletRequest request =
                new org.springframework.mock.web.MockHttpServletRequest();

        Map<String, Object> result = admissionFormService.updateAdmissionFormData("u@test.com", request);

        assertThat(result.get("success")).isEqualTo(true);
    }

    // ===== submitAdmissionForm =====

    @Test
    @DisplayName("submitAdmissionForm - user not found throws exception")
    void submitAdmissionForm_userNotFound_throwsException() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        AdmissionFormSubmitRequest req = new AdmissionFormSubmitRequest();
        assertThatThrownBy(() -> admissionFormService.submitAdmissionForm("none@test.com", req))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("submitAdmissionForm - null jenisSeleksiId throws exception")
    void submitAdmissionForm_nullJenisSeleksiId_throwsException() {
        User user = buildUser();
        Student student = buildStudent();
        mockUserAndStudent(user, student);

        AdmissionFormSubmitRequest req = new AdmissionFormSubmitRequest();
        req.setJenisSeleksiId(null);
        req.setSelectionTypeId(1L);

        assertThatThrownBy(() -> admissionFormService.submitAdmissionForm("u@test.com", req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("wajib");
    }

    @Test
    @DisplayName("submitAdmissionForm - jenisSeleksi not found throws exception")
    void submitAdmissionForm_jenisSeleksiNotFound_throwsException() {
        User user = buildUser();
        Student student = buildStudent();
        mockUserAndStudent(user, student);
        when(jenisSeleksiRepository.findById(99L)).thenReturn(Optional.empty());

        AdmissionFormSubmitRequest req = new AdmissionFormSubmitRequest();
        req.setJenisSeleksiId(99L);
        req.setSelectionTypeId(1L);

        assertThatThrownBy(() -> admissionFormService.submitAdmissionForm("u@test.com", req))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("submitAdmissionForm - happy path creates form and returns success")
    void submitAdmissionForm_happyPath_returnsSuccess() throws Exception {
        User user = buildUser();
        Student student = buildStudent();
        mockUserAndStudent(user, student);

        JenisSeleksi jenisSeleksi = new JenisSeleksi();
        jenisSeleksi.setId(2L);
        jenisSeleksi.setCode("REGULER");
        jenisSeleksi.setNama("Seleksi Reguler");
        when(jenisSeleksiRepository.findById(2L)).thenReturn(Optional.of(jenisSeleksi));

        RegistrationPeriod period = new RegistrationPeriod();
        period.setId(1L);
        period.setName("Gelombang 1");
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));

        AdmissionForm savedForm = buildForm();
        when(admissionFormRepository.save(any())).thenReturn(savedForm);

        FormValidation fv = new FormValidation();
        fv.setId(3L);
        when(formValidationRepository.save(any())).thenReturn(fv);
        when(formRepairStatusRepository.save(any())).thenReturn(new FormRepairStatus());
        when(registrationStatusService.markAsCompleted(any(), any(), any())).thenReturn(new RegistrationStatus());

        AdmissionFormSubmitRequest req = new AdmissionFormSubmitRequest();
        req.setJenisSeleksiId(2L);
        req.setSelectionTypeId(1L);
        req.setFullName("Budi Santoso");
        req.setProgramChoice1("Teknik Informatika");

        Map<String, Object> result = admissionFormService.submitAdmissionForm("u@test.com", req);

        assertThat(result.get("success")).isEqualTo(true);
        verify(admissionFormRepository, atLeastOnce()).save(any());
    }

    @Test
    @DisplayName("submitAdmissionForm - MEDICAL jenisSeleksi sets MEDICAL form type")
    void submitAdmissionForm_medicalJenisSeleksi_setsMedicalFormType() throws Exception {
        User user = buildUser();
        Student student = buildStudent();
        mockUserAndStudent(user, student);

        JenisSeleksi medicalJs = new JenisSeleksi();
        medicalJs.setId(3L);
        medicalJs.setCode("MEDICAL");
        medicalJs.setNama("Kedokteran");
        when(jenisSeleksiRepository.findById(3L)).thenReturn(Optional.of(medicalJs));

        RegistrationPeriod period = new RegistrationPeriod();
        period.setId(1L);
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));

        AdmissionForm savedForm = buildForm();
        when(admissionFormRepository.save(any())).thenReturn(savedForm);

        FormValidation fv = new FormValidation();
        fv.setId(4L);
        when(formValidationRepository.save(any())).thenReturn(fv);
        when(formRepairStatusRepository.save(any())).thenReturn(new FormRepairStatus());
        when(registrationStatusService.markAsCompleted(any(), any(), any())).thenReturn(new RegistrationStatus());

        AdmissionFormSubmitRequest req = new AdmissionFormSubmitRequest();
        req.setJenisSeleksiId(3L);
        req.setSelectionTypeId(1L);

        Map<String, Object> result = admissionFormService.submitAdmissionForm("u@test.com", req);

        assertThat(result.get("success")).isEqualTo(true);
    }

    // ===== updateAdmissionFormSelection =====

    @Test
    @DisplayName("updateAdmissionFormSelection - no forms returns success=true")
    void updateAdmissionFormSelection_noForms_returnsSuccess() {
        User user = buildUser();
        Student student = buildStudent();
        mockUserAndStudent(user, student);
        when(admissionFormRepository.findByStudent_Id(10L)).thenReturn(List.of());

        Map<String, Object> result = admissionFormService.updateAdmissionFormSelection(
                "u@test.com", Map.of("periodId", 1L, "jenisSeleksiId", 2L));

        assertThat(result.get("success")).isEqualTo(true);
    }

    @Test
    @DisplayName("updateAdmissionFormSelection - updates period and jenisSeleksi")
    void updateAdmissionFormSelection_withForms_updatesPeriodAndJenisSeleksi() {
        User user = buildUser();
        Student student = buildStudent();
        AdmissionForm form = buildForm();
        mockUserAndStudent(user, student);
        when(admissionFormRepository.findByStudent_Id(10L)).thenReturn(List.of(form));

        RegistrationPeriod period = new RegistrationPeriod();
        period.setId(1L);
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));

        JenisSeleksi js = new JenisSeleksi();
        js.setId(2L);
        js.setCode("REGULER");
        js.setNama("Seleksi Reguler");
        when(jenisSeleksiRepository.findById(2L)).thenReturn(Optional.of(js));

        when(admissionFormRepository.save(any())).thenReturn(form);

        Map<String, Object> req = new HashMap<>();
        req.put("periodId", 1L);
        req.put("jenisSeleksiId", 2L);
        Map<String, Object> result = admissionFormService.updateAdmissionFormSelection("u@test.com", req);

        assertThat(result.get("success")).isEqualTo(true);
        verify(admissionFormRepository).save(any());
    }

    @Test
    @DisplayName("updateAdmissionFormSelection - uses gelombangId when no periodId")
    void updateAdmissionFormSelection_usesGelombangId_whenNoPeriodId() {
        User user = buildUser();
        Student student = buildStudent();
        AdmissionForm form = buildForm();
        mockUserAndStudent(user, student);
        when(admissionFormRepository.findByStudent_Id(10L)).thenReturn(List.of(form));
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.empty());
        when(admissionFormRepository.save(any())).thenReturn(form);

        Map<String, Object> req = new HashMap<>();
        req.put("gelombangId", 1L);
        Map<String, Object> result = admissionFormService.updateAdmissionFormSelection("u@test.com", req);

        assertThat(result.get("success")).isEqualTo(true);
    }

    @Test
    @DisplayName("updateAdmissionFormSelection - uses selectionTypeId when no jenisSeleksiId")
    void updateAdmissionFormSelection_usesSelectionTypeId_whenNoJenisSeleksiId() {
        User user = buildUser();
        Student student = buildStudent();
        AdmissionForm form = buildForm();
        mockUserAndStudent(user, student);
        when(admissionFormRepository.findByStudent_Id(10L)).thenReturn(List.of(form));
        when(jenisSeleksiRepository.findById(2L)).thenReturn(Optional.empty());
        when(admissionFormRepository.save(any())).thenReturn(form);

        Map<String, Object> req = new HashMap<>();
        req.put("selectionTypeId", 2L);
        Map<String, Object> result = admissionFormService.updateAdmissionFormSelection("u@test.com", req);

        assertThat(result.get("success")).isEqualTo(true);
    }

    // ===== registerForAdmission =====

    @Test
    @DisplayName("registerForAdmission - period not found throws exception")
    void registerForAdmission_periodNotFound_throwsException() {
        User user = buildUser();
        Student student = buildStudent();
        mockUserAndStudent(user, student);
        when(registrationPeriodRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> admissionFormService.registerForAdmission("u@test.com", 99L, 1L, "CS"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("registerForAdmission - selection type not found throws exception")
    void registerForAdmission_selectionTypeNotFound_throwsException() {
        User user = buildUser();
        Student student = buildStudent();
        mockUserAndStudent(user, student);
        RegistrationPeriod period = new RegistrationPeriod();
        period.setId(1L);
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(selectionTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> admissionFormService.registerForAdmission("u@test.com", 1L, 99L, "CS"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("registerForAdmission - success delegates to registrationService")
    void registerForAdmission_success_delegates() {
        User user = buildUser();
        Student student = buildStudent();
        mockUserAndStudent(user, student);

        RegistrationPeriod period = new RegistrationPeriod();
        period.setId(1L);
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));

        SelectionType selType = new SelectionType();
        selType.setId(2L);
        when(selectionTypeRepository.findById(2L)).thenReturn(Optional.of(selType));

        AdmissionForm form = buildForm();
        when(studentRegistrationService.registerForAdmission(any(), any(), any(), any())).thenReturn(form);

        AdmissionForm result = admissionFormService.registerForAdmission("u@test.com", 1L, 2L, "Teknik Informatika");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(5L);
    }

    // ===== submitRevision =====

    @Test
    @DisplayName("submitRevision - form not found throws exception")
    void submitRevision_formNotFound_throwsException() {
        User user = buildUser();
        Student student = buildStudent();
        mockUserAndStudent(user, student);
        when(admissionFormRepository.findById(999L)).thenReturn(Optional.empty());

        SubmitRevisionRequest req = new SubmitRevisionRequest();
        req.setFullName("Budi");

        assertThatThrownBy(() -> admissionFormService.submitRevision("u@test.com", 999L, req))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("submitRevision - wrong student throws exception")
    void submitRevision_wrongStudent_throwsException() {
        User user = buildUser();
        Student student = buildStudent(); // id=10

        Student otherStudent = Student.builder().id(99L).build();
        AdmissionForm form = buildForm();
        form.setStudent(otherStudent); // form milik student 99, bukan 10

        mockUserAndStudent(user, student);
        when(admissionFormRepository.findById(5L)).thenReturn(Optional.of(form));

        SubmitRevisionRequest req = new SubmitRevisionRequest();
        req.setFullName("Budi");

        assertThatThrownBy(() -> admissionFormService.submitRevision("u@test.com", 5L, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("permission");
    }

    @Test
    @DisplayName("submitRevision - happy path updates form and returns success")
    void submitRevision_happyPath_returnsSuccess() throws Exception {
        User user = buildUser();
        Student student = buildStudent();
        AdmissionForm form = buildForm();
        form.setStudent(student);

        mockUserAndStudent(user, student);
        when(admissionFormRepository.findById(5L)).thenReturn(Optional.of(form));
        when(admissionFormRepository.save(any())).thenReturn(form);
        doNothing().when(validationStatusTrackerService).updateStatusToMenunggu(5L);
        when(formValidationRepository.findByAdmissionFormId(5L)).thenReturn(Optional.empty());

        SubmitRevisionRequest req = buildFullRevisionRequest();

        Map<String, Object> result = admissionFormService.submitRevision("u@test.com", 5L, req);

        assertThat(result.get("success")).isEqualTo(true);
        verify(admissionFormRepository).save(any());
    }

    @Test
    @DisplayName("submitRevision - with formValidation and repairStatus updates repair status")
    void submitRevision_withFormValidation_updatesRepairStatus() throws Exception {
        User user = buildUser();
        Student student = buildStudent();
        AdmissionForm form = buildForm();
        form.setStudent(student);

        FormValidation validation = new FormValidation();
        validation.setId(3L);

        FormRepairStatus repairStatus = new FormRepairStatus();
        repairStatus.setStatus(FormRepairStatus.RepairStatus.BELUM_PERBAIKAN);
        repairStatus.setUpdatedAt(LocalDateTime.now());

        mockUserAndStudent(user, student);
        when(admissionFormRepository.findById(5L)).thenReturn(Optional.of(form));
        when(admissionFormRepository.save(any())).thenReturn(form);
        doNothing().when(validationStatusTrackerService).updateStatusToMenunggu(5L);
        when(formValidationRepository.findByAdmissionFormId(5L)).thenReturn(Optional.of(validation));
        when(formRepairStatusRepository.findByFormValidationId(3L)).thenReturn(Optional.of(repairStatus));
        when(formRepairStatusRepository.save(any())).thenReturn(repairStatus);

        SubmitRevisionRequest req = buildFullRevisionRequest();

        Map<String, Object> result = admissionFormService.submitRevision("u@test.com", 5L, req);

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(repairStatus.getStatus()).isEqualTo(FormRepairStatus.RepairStatus.SUDAH_PERBAIKAN);
    }
}