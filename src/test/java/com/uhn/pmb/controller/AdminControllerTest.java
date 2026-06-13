package com.uhn.pmb.controller;

import com.uhn.pmb.entity.*;
import com.uhn.pmb.repository.*;
import com.uhn.pmb.service.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock private StudentRegistrationService registrationService;
    @Mock private EmailService emailService;
    @Mock private JenisSeleksiService jenisSeleksiService;
    @Mock private RegistrationStatusService registrationStatusService;
    @Mock private ValidationStatusTrackerService validationStatusTrackerService;
    @Mock private HasilAkhirService hasilAkhirService;
    @Mock private RegistrationPeriodRepository registrationPeriodRepository;
    @Mock private PeriodJenisSeleksiRepository periodJenisSeleksiRepository;
    @Mock private ProgramStudiRepository programStudiRepository;
    @Mock private SelectionProgramStudiRepository selectionProgramStudiRepository;
    @Mock private JenisSeleksiRepository jenisSeleksiRepository;
    @Mock private AdmissionFormRepository admissionFormRepository;
    @Mock private UserRepository userRepository;
    @Mock private AdminMessageRepository adminMessageRepository;
    @Mock private HasilAkhirRepository hasilAkhirRepository;
    @Mock private SystemConfigurationRepository systemConfigRepository;
    @Mock private SelectionTypeRepository selectionTypeRepository;
    @Mock private ExamLinkRepository examLinkRepository;
    @Mock private ReEnrollmentRepository reenrollmentRepository;
    @Mock private ReEnrollmentDocumentRepository reenrollmentDocumentRepository;
    @Mock private EntityManager entityManager;

    @InjectMocks private AdminController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ===== JENIS SELEKSI =====

    @Test
    @DisplayName("GET /admin/jenis-seleksi/period/{periodId} - period not found returns 400")
    void getJenisSeleksiByPeriod_notFound_returns400() throws Exception {
        when(registrationPeriodRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/jenis-seleksi/period/99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/jenis-seleksi/period/{periodId} - found returns 200")
    void getJenisSeleksiByPeriod_found_returns200() throws Exception {
        RegistrationPeriod period = RegistrationPeriod.builder().id(1L).name("Gel 1").build();
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(periodJenisSeleksiRepository.findByPeriod_IdAndIsActiveTrue(1L)).thenReturn(List.of());

        mockMvc.perform(get("/admin/jenis-seleksi/period/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/reenrollments - returns 200")
    void getReenrollments_returns200() throws Exception {
        mockMvc.perform(get("/admin/reenrollments"))
                .andExpect(status().isOk());
    }

    // ===== PROGRAM STUDI =====

    @Test
    @DisplayName("GET /admin/program-studi/jenis-seleksi/{id} - not found returns 400")
    void getProgramStudiByJenisSeleksi_notFound_returns400() throws Exception {
        when(jenisSeleksiRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/program-studi/jenis-seleksi/99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/program-studi/jenis-seleksi/{id} - found returns 200")
    void getProgramStudiByJenisSeleksi_found_returns200() throws Exception {
        JenisSeleksi js = JenisSeleksi.builder().id(1L).nama("Kedokteran").build();
        when(jenisSeleksiRepository.findById(1L)).thenReturn(Optional.of(js));
        when(selectionProgramStudiRepository.findByJenisSeleksi_IdAndIsActiveTrue(1L)).thenReturn(List.of());

        mockMvc.perform(get("/admin/program-studi/jenis-seleksi/1"))
                .andExpect(status().isOk());
    }

    // ===== PUBLISH RESULTS =====

    @Test
    @DisplayName("POST /admin/period/{periodId}/publish-results - period not found returns 400")
    void publishResults_notFound_returns400() throws Exception {
        when(registrationPeriodRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/admin/period/99/publish-results"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /admin/period/{periodId}/publish-results - found returns 200")
    void publishResults_found_returns200() throws Exception {
        RegistrationPeriod period = RegistrationPeriod.builder().id(1L).name("Gel 1").build();
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        doNothing().when(registrationService).publishExamResults(period);

        mockMvc.perform(post("/admin/period/1/publish-results"))
                .andExpect(status().isOk());
    }

    // ===== FORM VALIDATION =====

    @Test
    @DisplayName("PUT /admin/forms/{formValidationId}/approve - returns 200")
    void approveFormValidation_returns200() throws Exception {
        mockMvc.perform(put("/admin/forms/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/forms/{formValidationId}/reject - returns 200")
    void rejectFormValidation_returns200() throws Exception {
        mockMvc.perform(put("/admin/forms/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"topic\":\"data tidak valid\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/forms/{formValidationId}/revision-needed - returns 200")
    void markFormAsRevisionNeeded_returns200() throws Exception {
        mockMvc.perform(put("/admin/forms/1/revision-needed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"perlu revisi\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/forms/{formValidationId}/details - returns 200")
    void getFormDetails_returns200() throws Exception {
        mockMvc.perform(get("/admin/forms/1/details"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/forms/student/{studentId}/admission-details - returns 200")
    void getAdmissionFormStudentDetails_returns200() throws Exception {
        mockMvc.perform(get("/admin/forms/student/1/admission-details"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/forms/{formId}/validation-status-tracker - returns 200")
    void getValidationStatusTrackerByFormId_returns200() throws Exception {
        mockMvc.perform(get("/admin/forms/1/validation-status-tracker"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/student/{studentId}/validation-status-tracker - returns 200")
    void getValidationStatusTrackerByStudentId_returns200() throws Exception {
        mockMvc.perform(get("/admin/student/1/validation-status-tracker"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/forms/{formValidationId}/hasil-akhir - returns 200")
    void updateHasilAkhirRegistrationNumber_returns200() throws Exception {
        mockMvc.perform(put("/admin/forms/1/hasil-akhir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"registrationNumber\":\"12345\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/forms/student/{studentId}/repair-status - returns 200")
    void updateRepairStatus_returns200() throws Exception {
        mockMvc.perform(put("/admin/forms/student/1/repair-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk());
    }

    // ===== REENROLLMENT =====

    @Test
    @DisplayName("PUT /admin/reenrollments/{reenrollmentId}/validate - returns 200")
    void validateReEnrollment_returns200() throws Exception {
        mockMvc.perform(put("/admin/reenrollments/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPROVED\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/reenrollments/{reEnrollmentId}/approve - returns 200")
    void approveReEnrollmentValidation_returns200() throws Exception {
        mockMvc.perform(put("/admin/reenrollments/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/reenrollments/{reEnrollmentId}/reject - returns 200")
    void rejectReEnrollmentValidation_returns200() throws Exception {
        mockMvc.perform(put("/admin/reenrollments/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"dokumen tidak lengkap\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/reenrollments/{id}/details - not found returns 400")
    void getReerollmentDetailsSimple_notFound_returns400() throws Exception {
        when(reenrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/reenrollments/99/details"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/reenrollments/{id}/full-details - not found returns 400")
    void getReerollmentDetails_notFound_returns400() throws Exception {
        when(reenrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/reenrollments/99/full-details"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("PUT /admin/reenrollments/{reenrollmentId}/finalize - not found returns 400")
    void finalizeReenrollment_notFound_returns400() throws Exception {
        when(reenrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/admin/reenrollments/99/finalize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"approve\",\"validationNotes\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    // ===== HASIL AKHIR =====

    @Test
    @DisplayName("GET /admin/hasil-akhir/wave/{waveType} - invalid wave returns 400")
    void getHasilAkhirByWave_invalidWave_returns400() throws Exception {
        mockMvc.perform(get("/admin/hasil-akhir/wave/INVALID_WAVE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/hasil-akhir/wave/{waveType} - valid wave returns 200")
    void getHasilAkhirByWave_validWave_returns200() throws Exception {
        when(hasilAkhirRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/hasil-akhir/wave/EARLY_NO_TEST"))
                .andExpect(status().isOk());
    }

    // ===== USER MANAGEMENT =====

    @Test
    @DisplayName("PATCH /admin/api/users/{id}/deactivate - auth null returns 400")
    void deactivateUser_authNull_returns400() throws Exception {
        mockMvc.perform(patch("/admin/api/users/1/deactivate"))
                .andExpect(status().isBadRequest());
    }

    // ===== EXAM LINKS =====

    @Test
    @DisplayName("POST /admin/api/exam-links - period not found returns 400")
    void createExamLink_periodNotFound_returns400() throws Exception {
        when(registrationPeriodRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(post("/admin/api/exam-links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"periodId\":99,\"linkTitle\":\"Test\",\"linkUrl\":\"https://forms.google.com/test\",\"description\":\"desc\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/exam-links/period/{periodId} - returns 200")
    void getExamLinksByPeriod_returns200() throws Exception {
        when(examLinkRepository.findByPeriodId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/admin/api/exam-links/period/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/api/exam-links/{id} - not found returns 400")
    void updateExamLink_notFound_returns400() throws Exception {
        when(examLinkRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/admin/api/exam-links/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"periodId\":1,\"linkTitle\":\"Test\",\"linkUrl\":\"https://forms.google.com/test\",\"description\":\"desc\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /admin/api/exam-links/{id} - not found returns 400")
    void deleteExamLink_notFound_returns400() throws Exception {
        when(examLinkRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/admin/api/exam-links/99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /admin/api/exam-links/{id} - found returns 200")
    void deleteExamLink_found_returns200() throws Exception {
        ExamLink link = ExamLink.builder().id(1L).build();
        when(examLinkRepository.findById(1L)).thenReturn(Optional.of(link));
        doNothing().when(examLinkRepository).delete(link);

        mockMvc.perform(delete("/admin/api/exam-links/1"))
                .andExpect(status().isOk());
    }

    // ===== PAYMENT =====

    @Test
    @DisplayName("POST /admin/forms/{id}/verify-payment - returns 200")
    void verifyPayment_returns200() throws Exception {
        mockMvc.perform(post("/admin/forms/1/verify-payment"))
                .andExpect(status().isOk());
    }

    // ===== EXAM TOKEN =====

    @Test
    @DisplayName("POST /admin/exams/{id}/generate-token - returns 200")
    void generateExamToken_returns200() throws Exception {
        mockMvc.perform(post("/admin/exams/1/generate-token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/exams/{id}/token - returns 200")
    void getExamToken_returns200() throws Exception {
        mockMvc.perform(get("/admin/exams/1/token"))
                .andExpect(status().isOk());
    }

    // ===== EXAM RESULTS =====

    @Test
    @DisplayName("GET /admin/exam-results/{id}/details - returns 200")
    void getExamSubmissionDetails_returns200() throws Exception {
        mockMvc.perform(get("/admin/exam-results/1/details"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/exam-results/student/{studentId} - returns 200")
    void getExamResultByStudentId_returns200() throws Exception {
        mockMvc.perform(get("/admin/exam-results/student/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/exam-results/{id}/validate - returns 200")
    void validateExamSubmission_returns200() throws Exception {
        mockMvc.perform(put("/admin/exam-results/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"APPROVE\",\"adminNotes\":\"ok\"}"))
                .andExpect(status().isOk());
    }

    // ===== DOCUMENT VALIDATION =====

    @Test
    @DisplayName("PUT /admin/reenrollments/documents/{docId}/validate - auth null returns 400")
    void validateDocument_authNull_returns400() throws Exception {
        mockMvc.perform(put("/admin/reenrollments/documents/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"APPROVE\",\"adminNotes\":\"ok\"}"))
                .andExpect(status().is5xxServerError());
    }

    // ===== MESSAGING =====

    @Test
    @DisplayName("GET /admin/messages/conversation/{userId} - auth null returns 400")
    void getConversation_authNull_returns400() throws Exception {
        mockMvc.perform(get("/admin/messages/conversation/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /admin/messages/send/{studentEmail} - auth null returns 400")
    void sendMessageToStudent_authNull_returns400() throws Exception {
        mockMvc.perform(post("/admin/messages/send/student@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"messageContent\":\"hello\"}"))
                .andExpect(status().is5xxServerError());
    }

    // ===== VALIDATE FORM WITH BODY =====

    @Test
    @DisplayName("PUT /admin/forms/{formId}/validate - form not found returns 400")
    void validateForm_notFound_returns400() throws Exception {
        when(admissionFormRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/admin/forms/99/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approved\":true,\"rejectionReason\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    // ===== BULK INITIALIZE =====

    @Test
    @DisplayName("POST /admin/jenis-seleksi/bulk-initialize - returns 200")
    void bulkInitializeJenisSeleksi_returns200() throws Exception {
        when(jenisSeleksiRepository.existsByCode(anyString())).thenReturn(false);
        when(jenisSeleksiRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/admin/jenis-seleksi/bulk-initialize"))
                .andExpect(status().isOk());
    }

    // ===== ITERASI 6 ADDITIONS =====

    @Test
    @DisplayName("POST /admin/jenis-seleksi/bulk-initialize - all codes already exist returns 200")
    void bulkInitializeJenisSeleksi_alreadyExists_returns200() throws Exception {
        when(jenisSeleksiRepository.existsByCode(anyString())).thenReturn(true);

        mockMvc.perform(post("/admin/jenis-seleksi/bulk-initialize"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/program-studi/jenis-seleksi/{id} - found with data returns 200")
    void getProgramStudiByJenisSeleksi_foundWithData_returns200() throws Exception {
        ProgramStudi ps = new ProgramStudi();
        ps.setId(1L);
        ps.setKode("TIF");
        ps.setNama("Teknik Informatika");
        ps.setIsMedical(false);

        SelectionProgramStudi sps = new SelectionProgramStudi();
        sps.setId(1L);
        sps.setProgramStudi(ps);
        sps.setIsActive(true);

        JenisSeleksi js = JenisSeleksi.builder().id(1L).nama("Non-Kedokteran").build();
        when(jenisSeleksiRepository.findById(1L)).thenReturn(Optional.of(js));
        when(selectionProgramStudiRepository.findByJenisSeleksi_IdAndIsActiveTrue(1L)).thenReturn(List.of(sps));

        mockMvc.perform(get("/admin/program-studi/jenis-seleksi/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/forms/{formId}/validate - approved path returns 200")
    void validateForm_approved_returns200() throws Exception {
        User user = User.builder().id(1L).email("student@test.com").password("pass").role(User.UserRole.CAMABA).build();
        Student student = Student.builder().id(1L).user(user).fullName("Test Student").nik("123456789").build();
        AdmissionForm form = AdmissionForm.builder().id(1L).student(student).formType(SelectionType.FormType.NON_MEDICAL).build();

        when(admissionFormRepository.findById(1L)).thenReturn(Optional.of(form));
        doNothing().when(emailService).sendSimpleEmail(any(), any(), any());
        when(admissionFormRepository.save(any())).thenReturn(form);

        mockMvc.perform(put("/admin/forms/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approved\":true,\"reason\":\"\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/forms/{formId}/validate - rejected path returns 200")
    void validateForm_rejected_returns200() throws Exception {
        User user = User.builder().id(1L).email("student@test.com").password("pass").role(User.UserRole.CAMABA).build();
        Student student = Student.builder().id(1L).user(user).fullName("Test Student").nik("123456789").build();
        AdmissionForm form = AdmissionForm.builder().id(1L).student(student).formType(SelectionType.FormType.NON_MEDICAL).build();

        when(admissionFormRepository.findById(1L)).thenReturn(Optional.of(form));
        doNothing().when(emailService).sendSimpleEmail(any(), any(), any());
        when(admissionFormRepository.save(any())).thenReturn(form);

        mockMvc.perform(put("/admin/forms/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approved\":false,\"reason\":\"Data tidak lengkap\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/reenrollments/{id}/details - success with no documents returns 200")
    void getReerollmentDetailsSimple_success_noDocuments_returns200() throws Exception {
        User user = User.builder().id(1L).email("s@test.com").password("pass").role(User.UserRole.CAMABA).build();
        Student student = Student.builder().id(1L).user(user).fullName("Tester").nik("111").build();
        ReEnrollment reenrollment = ReEnrollment.builder()
                .id(1L).student(student)
                .status(ReEnrollment.ReEnrollmentStatus.SUBMITTED)
                .parentPhone("081200000").parentEmail("p@test.com")
                .parentAddress("addr").permanentAddress("perm")
                .build();

        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(reenrollment));

        mockMvc.perform(get("/admin/reenrollments/1/details"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/reenrollments/{id}/details - success with modern documents returns 200")
    void getReerollmentDetailsSimple_success_withDocuments_returns200() throws Exception {
        User user = User.builder().id(1L).email("s@test.com").password("pass").role(User.UserRole.CAMABA).build();
        Student student = Student.builder().id(1L).user(user).fullName("Tester").nik("111").build();
        ReEnrollment reenrollment = ReEnrollment.builder()
                .id(1L).student(student)
                .status(ReEnrollment.ReEnrollmentStatus.SUBMITTED)
                .parentPhone("081200000").parentEmail("p@test.com")
                .parentAddress("addr").permanentAddress("perm")
                .build();

        ReEnrollmentDocument doc = ReEnrollmentDocument.builder()
                .id(1L)
                .documentType(ReEnrollmentDocument.DocumentType.IJAZAH)
                .originalFilename("ijazah.pdf")
                .fileSize(1024L)
                .validationStatus(ReEnrollmentDocument.ValidationStatus.PENDING)
                .filePath("uploads/test/ijazah.pdf")
                .build();
        reenrollment.getDocuments().add(doc);

        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(reenrollment));

        mockMvc.perform(get("/admin/reenrollments/1/details"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/reenrollments/{id}/details - success with legacy files returns 200")
    void getReerollmentDetailsSimple_success_withLegacyFiles_returns200() throws Exception {
        User user = User.builder().id(1L).email("s@test.com").password("pass").role(User.UserRole.CAMABA).build();
        Student student = Student.builder().id(1L).user(user).fullName("Tester").nik("111").build();
        ReEnrollment reenrollment = ReEnrollment.builder()
                .id(1L).student(student)
                .status(ReEnrollment.ReEnrollmentStatus.SUBMITTED)
                .parentPhone("081200000").parentEmail("p@test.com")
                .parentAddress("addr").permanentAddress("perm")
                .paktaIntegritasFile("uploads/pakta.pdf")
                .ijazahFile("uploads/ijazah.pdf")
                .build();

        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(reenrollment));

        mockMvc.perform(get("/admin/reenrollments/1/details"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/reenrollments/{id}/full-details - success returns 200")
    void getReerollmentDetails_success_returns200() throws Exception {
        User user = User.builder().id(1L).email("s@test.com").password("pass").role(User.UserRole.CAMABA).build();
        Student student = Student.builder().id(1L).user(user).fullName("Tester").nik("111").build();
        ReEnrollment reenrollment = ReEnrollment.builder()
                .id(1L).student(student)
                .status(ReEnrollment.ReEnrollmentStatus.SUBMITTED)
                .parentPhone("081200000").parentEmail("p@test.com")
                .parentAddress("addr").permanentAddress("perm")
                .build();

        ReEnrollmentDocument doc = ReEnrollmentDocument.builder()
                .id(1L)
                .documentType(ReEnrollmentDocument.DocumentType.SKCK)
                .originalFilename("skck.pdf")
                .fileSize(512L)
                .validationStatus(ReEnrollmentDocument.ValidationStatus.APPROVED)
                .filePath("uploads/test/skck.pdf")
                .build();
        reenrollment.getDocuments().add(doc);

        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(reenrollment));

        mockMvc.perform(get("/admin/reenrollments/1/full-details"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/reenrollments/{id}/full-details - success with legacy files returns 200")
    void getReerollmentDetails_success_withLegacyFiles_returns200() throws Exception {
        User user = User.builder().id(1L).email("s@test.com").password("pass").role(User.UserRole.CAMABA).build();
        Student student = Student.builder().id(1L).user(user).fullName("Tester").nik("111").build();
        ReEnrollment reenrollment = ReEnrollment.builder()
                .id(1L).student(student)
                .status(ReEnrollment.ReEnrollmentStatus.SUBMITTED)
                .parentPhone("081200000").parentEmail("p@test.com")
                .parentAddress("addr").permanentAddress("perm")
                .ijazahFile("uploads/ijazah.pdf")
                .ktpFile("uploads/ktp.pdf")
                .build();

        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(reenrollment));

        mockMvc.perform(get("/admin/reenrollments/1/full-details"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/exam-links/period/{periodId} - with data returns 200")
    void getExamLinksByPeriod_withData_returns200() throws Exception {
        RegistrationPeriod period = RegistrationPeriod.builder().id(1L).name("Gel 1").build();
        SelectionType st = new SelectionType();
        st.setId(1L);
        st.setName("Kedokteran");
        ExamLink link = ExamLink.builder()
                .id(1L).period(period).selectionType(st)
                .linkTitle("Link Ujian").linkUrl("https://forms.google.com/test")
                .description("test desc").isActive(true)
                .build();

        when(examLinkRepository.findByPeriodId(1L)).thenReturn(List.of(link));

        mockMvc.perform(get("/admin/api/exam-links/period/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/exam-links/period/{periodId} - with null selectionType returns 200")
    void getExamLinksByPeriod_withNullSelectionType_returns200() throws Exception {
        RegistrationPeriod period = RegistrationPeriod.builder().id(1L).name("Gel 1").build();
        ExamLink link = ExamLink.builder()
                .id(1L).period(period).selectionType(null)
                .linkTitle("Link Ujian").linkUrl("https://forms.google.com/test")
                .description("test desc").isActive(true)
                .build();

        when(examLinkRepository.findByPeriodId(1L)).thenReturn(List.of(link));

        mockMvc.perform(get("/admin/api/exam-links/period/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /admin/api/exam-links - valid Google Form URL returns 201")
    void createExamLink_validGoogleFormUrl_returns201() throws Exception {
        RegistrationPeriod period = RegistrationPeriod.builder().id(1L).name("Gel 1").build();
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(examLinkRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/admin/api/exam-links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"periodId\":1,\"linkTitle\":\"Test Link\",\"linkUrl\":\"https://forms.google.com/valid\",\"description\":\"desc\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /admin/api/exam-links - invalid URL returns 400")
    void createExamLink_invalidUrl_returns400() throws Exception {
        RegistrationPeriod period = RegistrationPeriod.builder().id(1L).name("Gel 1").build();
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));

        mockMvc.perform(post("/admin/api/exam-links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"periodId\":1,\"linkTitle\":\"Test\",\"linkUrl\":\"https://not-a-google-form.com/test\",\"description\":\"desc\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /admin/api/exam-links/{id} - found with valid URL returns 200")
    void updateExamLink_found_validUrl_returns200() throws Exception {
        ExamLink link = ExamLink.builder().id(1L).isActive(true).build();
        when(examLinkRepository.findById(1L)).thenReturn(Optional.of(link));
        when(examLinkRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/admin/api/exam-links/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"periodId\":1,\"linkTitle\":\"Updated\",\"linkUrl\":\"https://forms.google.com/updated\",\"description\":\"new desc\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/reenrollments/documents/{docId}/validate - APPROVE action returns 200")
    void validateDocument_approveAction_returns200() throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("admin@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            User admin = User.builder().id(1L).email("admin@test.com").password("pass").role(User.UserRole.ADMIN_VALIDASI).build();
            ReEnrollmentDocument doc = ReEnrollmentDocument.builder()
                    .id(1L)
                    .documentType(ReEnrollmentDocument.DocumentType.IJAZAH)
                    .validationStatus(ReEnrollmentDocument.ValidationStatus.PENDING)
                    .filePath("uploads/test.pdf")
                    .build();

            when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
            when(reenrollmentDocumentRepository.findById(1L)).thenReturn(Optional.of(doc));
            when(reenrollmentDocumentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            mockMvc.perform(put("/admin/reenrollments/documents/1/validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"action\":\"APPROVE\",\"adminNotes\":\"OK\"}"))
                    .andExpect(status().isOk());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("PUT /admin/reenrollments/documents/{docId}/validate - REJECT action returns 200")
    void validateDocument_rejectAction_returns200() throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("admin@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            User admin = User.builder().id(1L).email("admin@test.com").password("pass").role(User.UserRole.ADMIN_VALIDASI).build();
            ReEnrollmentDocument doc = ReEnrollmentDocument.builder()
                    .id(1L)
                    .documentType(ReEnrollmentDocument.DocumentType.IJAZAH)
                    .validationStatus(ReEnrollmentDocument.ValidationStatus.PENDING)
                    .filePath("uploads/test.pdf")
                    .build();

            when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
            when(reenrollmentDocumentRepository.findById(1L)).thenReturn(Optional.of(doc));
            when(reenrollmentDocumentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            mockMvc.perform(put("/admin/reenrollments/documents/1/validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"action\":\"REJECT\",\"adminNotes\":\"Not valid\"}"))
                    .andExpect(status().isOk());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("PUT /admin/reenrollments/documents/{docId}/validate - REVISION_NEEDED action returns 200")
    void validateDocument_revisionNeededAction_returns200() throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("admin@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            User admin = User.builder().id(1L).email("admin@test.com").password("pass").role(User.UserRole.ADMIN_VALIDASI).build();
            ReEnrollmentDocument doc = ReEnrollmentDocument.builder()
                    .id(1L)
                    .documentType(ReEnrollmentDocument.DocumentType.SKCK)
                    .validationStatus(ReEnrollmentDocument.ValidationStatus.PENDING)
                    .filePath("uploads/skck.pdf")
                    .build();

            when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
            when(reenrollmentDocumentRepository.findById(1L)).thenReturn(Optional.of(doc));
            when(reenrollmentDocumentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            mockMvc.perform(put("/admin/reenrollments/documents/1/validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"action\":\"REVISION_NEEDED\",\"adminNotes\":\"Please resubmit\"}"))
                    .andExpect(status().isOk());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("PUT /admin/reenrollments/{reenrollmentId}/finalize - approve action returns 200")
    void finalizeReenrollment_approveAction_returns200() throws Exception {
        ReEnrollment reenrollment = ReEnrollment.builder()
                .id(1L).status(ReEnrollment.ReEnrollmentStatus.SUBMITTED)
                .parentPhone("081200000").parentEmail("p@test.com")
                .parentAddress("addr").permanentAddress("perm")
                .build();
        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(reenrollment));
        when(reenrollmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/admin/reenrollments/1/finalize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"approve\",\"validationNotes\":\"All documents verified\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/reenrollments/{reenrollmentId}/finalize - reject action returns 200")
    void finalizeReenrollment_rejectAction_returns200() throws Exception {
        ReEnrollment reenrollment = ReEnrollment.builder()
                .id(1L).status(ReEnrollment.ReEnrollmentStatus.SUBMITTED)
                .parentPhone("081200000").parentEmail("p@test.com")
                .parentAddress("addr").permanentAddress("perm")
                .build();
        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(reenrollment));
        when(reenrollmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/admin/reenrollments/1/finalize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"reject\",\"validationNotes\":\"Documents incomplete\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/hasil-akhir/wave/{waveType} - with HasilAkhir data returns 200")
    void getHasilAkhirByWave_withData_returns200() throws Exception {
        User user = User.builder().id(1L).email("s@test.com").password("pass").role(User.UserRole.CAMABA).build();
        Student student = Student.builder().id(1L).user(user).fullName("Test Student").nik("12345678").build();
        HasilAkhir ha = HasilAkhir.builder()
                .id(1L).student(student).user(user)
                .waveType(RegistrationPeriod.WaveType.EARLY_NO_TEST)
                .brivaNumber("BRI12345")
                .nomorRegistrasi("REG001")
                .status(HasilAkhir.HasilAkhirStatus.ACTIVE)
                .build();

        when(hasilAkhirRepository.findAll()).thenReturn(List.of(ha));

        mockMvc.perform(get("/admin/hasil-akhir/wave/EARLY_NO_TEST"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /admin/api/users/{id}/deactivate - success returns 200")
    void deactivateUser_success_returns200() throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("admin@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            User user = User.builder().id(1L).email("other@test.com").password("pass").role(User.UserRole.CAMABA).isActive(true).build();
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            mockMvc.perform(patch("/admin/api/users/1/deactivate"))
                    .andExpect(status().isOk());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("PATCH /admin/api/users/{id}/deactivate - self deactivation returns 400")
    void deactivateUser_selfDeactivation_returns400() throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("admin@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            User user = User.builder().id(1L).email("admin@test.com").password("pass").role(User.UserRole.ADMIN_PUSAT).isActive(true).build();
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            mockMvc.perform(patch("/admin/api/users/1/deactivate"))
                    .andExpect(status().isBadRequest());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("GET /admin/messages/conversation/{userId} - success with empty messages returns 200")
    void getConversation_success_emptyMessages_returns200() throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("admin@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            User currentUser = User.builder().id(1L).email("admin@test.com").password("pass").role(User.UserRole.ADMIN_VALIDASI).build();
            when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(currentUser));
            when(adminMessageRepository.findConversationBetween(1L, 2L)).thenReturn(List.of());

            mockMvc.perform(get("/admin/messages/conversation/2"))
                    .andExpect(status().isOk());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("GET /admin/messages/conversation/{userId} - success with unread message returns 200")
    void getConversation_success_withUnreadMessage_returns200() throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("admin@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            User currentUser = User.builder().id(1L).email("admin@test.com").password("pass").role(User.UserRole.ADMIN_VALIDASI).build();
            User sender = User.builder().id(2L).email("student@test.com").password("pass").role(User.UserRole.CAMABA).build();

            AdminMessage unreadMsg = AdminMessage.builder()
                    .id(1L).sender(sender).recipient(currentUser)
                    .messageContent("Hello admin")
                    .status(AdminMessage.MessageStatus.UNREAD)
                    .build();

            when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(currentUser));
            when(adminMessageRepository.findConversationBetween(1L, 2L)).thenReturn(new ArrayList<>(List.of(unreadMsg)));
            when(adminMessageRepository.save(any())).thenReturn(unreadMsg);

            mockMvc.perform(get("/admin/messages/conversation/2"))
                    .andExpect(status().isOk());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("POST /admin/messages/send/{studentEmail} - success returns 200")
    void sendMessageToStudent_success_returns200() throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("admin@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            User admin = User.builder().id(1L).email("admin@test.com").password("pass").role(User.UserRole.ADMIN_VALIDASI).build();
            User student = User.builder().id(2L).email("student@test.com").password("pass").role(User.UserRole.CAMABA).build();

            when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
            when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(student));
            when(adminMessageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            mockMvc.perform(post("/admin/messages/send/student@test.com")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"messageContent\":\"Hello student, this is a valid message!\",\"messageType\":\"ANSWER\"}"))
                    .andExpect(status().isOk());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("POST /admin/messages/send/{studentEmail} - empty message returns 400")
    void sendMessageToStudent_emptyMessage_returns400() throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("admin@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            User admin = User.builder().id(1L).email("admin@test.com").password("pass").role(User.UserRole.ADMIN_VALIDASI).build();
            User student = User.builder().id(2L).email("student@test.com").password("pass").role(User.UserRole.CAMABA).build();

            when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
            when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(student));

            mockMvc.perform(post("/admin/messages/send/student@test.com")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"messageContent\":\"\"}"))
                    .andExpect(status().isBadRequest());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("POST /admin/messages/send/{studentEmail} - message too short returns 400")
    void sendMessageToStudent_messageTooShort_returns400() throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("admin@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            User admin = User.builder().id(1L).email("admin@test.com").password("pass").role(User.UserRole.ADMIN_VALIDASI).build();
            User student = User.builder().id(2L).email("student@test.com").password("pass").role(User.UserRole.CAMABA).build();

            when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
            when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(student));

            mockMvc.perform(post("/admin/messages/send/student@test.com")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"messageContent\":\"Hi!\"}"))
                    .andExpect(status().isBadRequest());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    // ===== ADDITIONAL COVERAGE TESTS — HTTP ENDPOINT PARTIAL BRANCHES =====

    @Test
    @DisplayName("GET /admin/jenis-seleksi/period/{periodId} - with non-empty data covers stream map")
    void getJenisSeleksiByPeriod_withData_coversStreamMap() throws Exception {
        RegistrationPeriod period = RegistrationPeriod.builder().id(1L).name("Gel 1").build();
        JenisSeleksi js = JenisSeleksi.builder().id(10L).code("KEDOK").nama("Kedokteran")
                .deskripsi("desc").logoUrl("url").harga(new BigDecimal("1500000")).build();
        PeriodJenisSeleksi pjs = PeriodJenisSeleksi.builder().id(5L).period(period)
                .jenisSeleksi(js).isActive(true).build();

        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(periodJenisSeleksiRepository.findByPeriod_IdAndIsActiveTrue(1L)).thenReturn(List.of(pjs));

        mockMvc.perform(get("/admin/jenis-seleksi/period/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/reenrollments/{id}/validate - null body returns 200")
    void validateReEnrollment_nullBody_returns200() throws Exception {
        mockMvc.perform(put("/admin/reenrollments/1/validate"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/forms/{id}/reject - null body returns 200")
    void rejectFormValidation_nullBody_returns200() throws Exception {
        mockMvc.perform(put("/admin/forms/1/reject"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/forms/{id}/revision-needed - null body returns 200")
    void markFormAsRevisionNeeded_nullBody_returns200() throws Exception {
        mockMvc.perform(put("/admin/forms/1/revision-needed"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/reenrollments/{id}/details - exception thrown returns 400")
    void getReerollmentDetailsSimple_exceptionThrown_returns400() throws Exception {
        when(reenrollmentRepository.findById(99L)).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/admin/reenrollments/99/details"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/hasil-akhir/wave/{waveType} - exception in findAll returns 400")
    void getHasilAkhirByWave_exceptionInFindAll_returns400() throws Exception {
        when(hasilAkhirRepository.findAll()).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get("/admin/hasil-akhir/wave/EARLY_NO_TEST"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/messages/conversation/{userId} - READ message not saved again")
    void getConversation_withReadMessage_returns200() throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("admin@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            User currentUser = User.builder().id(1L).email("admin@test.com").password("pass")
                    .role(User.UserRole.ADMIN_VALIDASI).build();
            User sender = User.builder().id(2L).email("other@test.com").password("pass")
                    .role(User.UserRole.CAMABA).build();
            AdminMessage msg = AdminMessage.builder()
                    .id(1L).sender(sender).recipient(currentUser)
                    .messageContent("Hello").status(AdminMessage.MessageStatus.READ)
                    .createdAt(LocalDateTime.now()).build();

            when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(currentUser));
            when(adminMessageRepository.findConversationBetween(1L, 2L)).thenReturn(List.of(msg));

            mockMvc.perform(get("/admin/messages/conversation/2"))
                    .andExpect(status().isOk());

            verify(adminMessageRepository, never()).save(any());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("POST /admin/messages/send/{studentEmail} - with explicit messageType returns 200")
    void sendMessageToStudent_withMessageType_returns200() throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("admin@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            User admin = User.builder().id(1L).email("admin@test.com").password("pass")
                    .role(User.UserRole.ADMIN_VALIDASI).build();
            User student = User.builder().id(2L).email("student@test.com").password("pass")
                    .role(User.UserRole.CAMABA).build();

            when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
            when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(student));
            when(adminMessageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            mockMvc.perform(post("/admin/messages/send/student@test.com")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"messageContent\":\"Hello this is a long enough message\",\"messageType\":\"QUESTION\"}"))
                    .andExpect(status().isOk());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    // ===== DIRECT INVOCATION TESTS FOR DEAD PUBLIC METHODS =====

    @Test
    @DisplayName("updateUserRole direct - user not found returns error")
    void updateUserRole_direct_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        var result = controller.updateUserRole(1L, Map.of("role", "ADMIN_VALIDASI"));
        assertNotNull(result);
    }

    @Test
    @DisplayName("updateUserRole direct - success updates role")
    void updateUserRole_direct_success() {
        User user = User.builder().id(1L).email("u@test.com").password("p").role(User.UserRole.CAMABA).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        var result = controller.updateUserRole(1L, Map.of("role", "ADMIN_VALIDASI"));
        assertNotNull(result);
    }

    @Test
    @DisplayName("updateUserRole direct - empty role string returns error")
    void updateUserRole_direct_emptyRole() {
        User user = User.builder().id(1L).email("u@test.com").password("p").role(User.UserRole.CAMABA).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        var result = controller.updateUserRole(1L, new HashMap<>());
        assertNotNull(result);
    }

    @Test
    @DisplayName("deleteUser direct - user not found (no auth) returns error")
    void deleteUser_direct_notFound() {
        // No auth set -> auth.getName() throws NPE -> caught -> returns error
        var result = controller.deleteUser(99L);
        assertNotNull(result);
    }

    @Test
    @DisplayName("deleteUser direct - success deletes user")
    void deleteUser_direct_success() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("admin@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            User user = User.builder().id(1L).email("u@test.com").password("p").role(User.UserRole.CAMABA).build();
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            var result = controller.deleteUser(1L);
            assertNotNull(result);
            verify(userRepository).delete(user);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("updateRegistrationPeriod direct - no auth causes NPE caught returning error")
    void updateRegistrationPeriod_direct_notFound() {
        // No auth set -> SecurityContextHolder.getContext().getAuthentication().getName() throws NPE -> caught
        AdminController.RegistrationPeriodRequest req = new AdminController.RegistrationPeriodRequest();
        req.setName("Test");
        var result = controller.updateRegistrationPeriod(99L, req);
        assertNotNull(result);
    }

    @Test
    @DisplayName("updateRegistrationPeriod direct - success with jenisSeleksiIds covers full method")
    void updateRegistrationPeriod_direct_success_withJenisSeleksi() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("admin@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            RegistrationPeriod period = RegistrationPeriod.builder().id(1L).name("Old Name")
                    .waveType(RegistrationPeriod.WaveType.REGULAR_TEST).build();
            JenisSeleksi js = JenisSeleksi.builder().id(10L).code("REG").nama("Regular")
                    .harga(BigDecimal.valueOf(500000)).build();

            when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
            when(jenisSeleksiRepository.findById(10L)).thenReturn(Optional.of(js));

            AdminController.RegistrationPeriodRequest req = new AdminController.RegistrationPeriodRequest();
            req.setName("New Name");
            req.setWaveType(RegistrationPeriod.WaveType.EARLY_NO_TEST);
            req.setJenisSeleksiIds(List.of(10L));

            var result = controller.updateRegistrationPeriod(1L, req);
            assertNotNull(result);
            verify(periodJenisSeleksiRepository).deleteByPeriod_Id(1L);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("deleteRegistrationPeriod direct - no auth causes NPE caught returning error")
    void deleteRegistrationPeriod_direct_notFound() {
        // No auth set -> SecurityContextHolder.getContext().getAuthentication().getName() throws NPE -> caught
        var result = controller.deleteRegistrationPeriod(99L);
        assertNotNull(result);
    }

    @Test
    @DisplayName("deleteRegistrationPeriod direct - success deletes period")
    void deleteRegistrationPeriod_direct_success() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("admin@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            RegistrationPeriod period = RegistrationPeriod.builder().id(1L).name("Test Period")
                    .waveType(RegistrationPeriod.WaveType.REGULAR_TEST).build();
            when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));

            var result = controller.deleteRegistrationPeriod(1L);
            assertNotNull(result);
            verify(registrationPeriodRepository).delete(period);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("getSetting direct - key not found returns empty value")
    void getSetting_direct_notFound() {
        when(systemConfigRepository.findByConfigKey("missing_key")).thenReturn(Optional.empty());
        var result = controller.getSetting("missing_key");
        assertNotNull(result);
    }

    @Test
    @DisplayName("getSetting direct - key found returns value")
    void getSetting_direct_found() {
        SystemConfiguration config = SystemConfiguration.builder()
                .configKey("site_name").configValue("PMB UHN").isActive(true).build();
        when(systemConfigRepository.findByConfigKey("site_name")).thenReturn(Optional.of(config));
        var result = controller.getSetting("site_name");
        assertNotNull(result);
    }

    @Test
    @DisplayName("updateSetting direct - null value throws and returns error")
    void updateSetting_direct_nullValue() {
        var result = controller.updateSetting("test_key", new HashMap<>());
        assertNotNull(result);
    }

    @Test
    @DisplayName("updateSetting direct - new setting created")
    void updateSetting_direct_newSetting() {
        when(systemConfigRepository.findByConfigKey("new_key")).thenReturn(Optional.empty());
        var result = controller.updateSetting("new_key", Map.of("value", "new_value"));
        assertNotNull(result);
        verify(systemConfigRepository).save(any());
    }

    @Test
    @DisplayName("updateSetting direct - existing setting updated")
    void updateSetting_direct_existingSetting() {
        SystemConfiguration existing = SystemConfiguration.builder()
                .configKey("existing_key").configValue("old_value").isActive(true).build();
        when(systemConfigRepository.findByConfigKey("existing_key")).thenReturn(Optional.of(existing));
        var result = controller.updateSetting("existing_key", Map.of("value", "new_value"));
        assertNotNull(result);
        verify(systemConfigRepository).save(existing);
    }

    @Test
    @DisplayName("getSelectionTypesByPeriod direct - period not found returns error")
    void getSelectionTypesByPeriod_direct_notFound() {
        when(registrationPeriodRepository.findById(99L)).thenReturn(Optional.empty());
        var result = controller.getSelectionTypesByPeriod(99L);
        assertNotNull(result);
    }

    @Test
    @DisplayName("getSelectionTypesByPeriod direct - with data covers stream map body")
    void getSelectionTypesByPeriod_direct_withData() {
        RegistrationPeriod period = RegistrationPeriod.builder().id(1L).name("Gel 1").build();
        SelectionType st = SelectionType.builder()
                .id(1L).name("Bebas Testing").description("Desc")
                .formType(SelectionType.FormType.NON_MEDICAL)
                .requireRanking(false).requireTesting(false)
                .price(BigDecimal.valueOf(1000000)).isActive(true).build();

        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(selectionTypeRepository.findByPeriod_Id(1L)).thenReturn(List.of(st));

        var result = controller.getSelectionTypesByPeriod(1L);
        assertNotNull(result);
    }

    @Test
    @DisplayName("updateSelectionType direct - type not found returns error")
    void updateSelectionType_direct_notFound() {
        when(selectionTypeRepository.findById(99L)).thenReturn(Optional.empty());
        AdminController.UpdateSelectionTypeRequest req = new AdminController.UpdateSelectionTypeRequest();
        req.setName("Updated");
        var result = controller.updateSelectionType(99L, req);
        assertNotNull(result);
    }

    @Test
    @DisplayName("updateSelectionType direct - success updates type")
    void updateSelectionType_direct_success() {
        SelectionType st = SelectionType.builder()
                .id(1L).name("Old").formType(SelectionType.FormType.NON_MEDICAL)
                .price(BigDecimal.valueOf(500000)).isActive(true).build();
        when(selectionTypeRepository.findById(1L)).thenReturn(Optional.of(st));

        AdminController.UpdateSelectionTypeRequest req = new AdminController.UpdateSelectionTypeRequest();
        req.setName("Updated");
        req.setDescription("New desc");
        req.setRequireRanking(false);
        req.setRequireTesting(true);
        req.setPrice(BigDecimal.valueOf(600000));
        req.setIsActive(true);

        var result = controller.updateSelectionType(1L, req);
        assertNotNull(result);
        verify(selectionTypeRepository).save(st);
    }

    @Test
    @DisplayName("deleteSelectionType direct - type not found returns error")
    void deleteSelectionType_direct_notFound() {
        when(selectionTypeRepository.findById(99L)).thenReturn(Optional.empty());
        var result = controller.deleteSelectionType(99L);
        assertNotNull(result);
    }

    @Test
    @DisplayName("deleteSelectionType direct - success deletes type")
    void deleteSelectionType_direct_success() {
        SelectionType st = SelectionType.builder()
                .id(1L).name("Test ST").formType(SelectionType.FormType.MEDICAL)
                .price(BigDecimal.valueOf(1000000)).isActive(true).build();
        when(selectionTypeRepository.findById(1L)).thenReturn(Optional.of(st));

        var result = controller.deleteSelectionType(1L);
        assertNotNull(result);
        verify(selectionTypeRepository).delete(st);
    }

    @Test
    @DisplayName("dead finalizeReEnrollment direct - reenrollment not found returns error")
    void deadFinalizeReEnrollment_direct_notFound() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("admin@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            User admin = User.builder().id(1L).email("admin@test.com").password("p")
                    .role(User.UserRole.ADMIN_VALIDASI).build();
            when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
            when(reenrollmentRepository.findById(99L)).thenReturn(Optional.empty());

            AdminController.ReenrollmentFinalizeRequest req = new AdminController.ReenrollmentFinalizeRequest();
            req.setAction("APPROVE");
            var result = controller.finalizeReEnrollment(99L, req);
            assertNotNull(result);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("dead finalizeReEnrollment direct - REJECT action covers success path")
    void deadFinalizeReEnrollment_direct_rejectAction() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("admin@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            User admin = User.builder().id(1L).email("admin@test.com").password("p")
                    .role(User.UserRole.ADMIN_VALIDASI).build();
            ReEnrollment enrollment = ReEnrollment.builder().id(1L)
                    .status(ReEnrollment.ReEnrollmentStatus.SUBMITTED).build();

            when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
            when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));
            when(reenrollmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            AdminController.ReenrollmentFinalizeRequest req = new AdminController.ReenrollmentFinalizeRequest();
            req.setAction("REJECT");
            req.setValidationNotes("Documents incomplete");
            var result = controller.finalizeReEnrollment(1L, req);
            assertNotNull(result);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("dead finalizeReEnrollment direct - APPROVE with all approved docs covers success path")
    void deadFinalizeReEnrollment_direct_approveAllApproved() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("admin@test.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            User admin = User.builder().id(1L).email("admin@test.com").password("p")
                    .role(User.UserRole.ADMIN_VALIDASI).build();
            ReEnrollmentDocument doc = ReEnrollmentDocument.builder()
                    .id(1L).validationStatus(ReEnrollmentDocument.ValidationStatus.APPROVED).build();
            ReEnrollment enrollment = ReEnrollment.builder().id(1L)
                    .status(ReEnrollment.ReEnrollmentStatus.VALIDATED).build();
            enrollment.getDocuments().add(doc);

            when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
            when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));
            when(reenrollmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            AdminController.ReenrollmentFinalizeRequest req = new AdminController.ReenrollmentFinalizeRequest();
            req.setAction("APPROVE");
            req.setValidationNotes("All documents verified");
            var result = controller.finalizeReEnrollment(1L, req);
            assertNotNull(result);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}

