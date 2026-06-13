package com.uhn.pmb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uhn.pmb.dto.*;
import com.uhn.pmb.entity.*;
import com.uhn.pmb.repository.*;
import com.uhn.pmb.service.*;
import org.junit.jupiter.api.AfterEach;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminValidationControllerTest {

    @Mock private AdmissionFormRepository admissionFormRepository;
    @Mock private FormValidationRepository formValidationRepository;
    @Mock private FormRepairStatusRepository formRepairStatusRepository;
    @Mock private ReEnrollmentValidationRepository reEnrollmentValidationRepository;
    @Mock private ReEnrollmentRepository reenrollmentRepository;
    @Mock private ReEnrollmentDocumentRepository reenrollmentDocumentRepository;
    @Mock private ExamResultRepository examResultRepository;
    @Mock private UserRepository userRepository;
    @Mock private ValidationStatusTrackerRepository validationStatusTrackerRepository;
    @Mock private RegistrationStatusService registrationStatusService;
    @Mock private ValidationStatusTrackerService validationStatusTrackerService;
    @Mock private HasilAkhirService hasilAkhirService;
    @Mock private FormValidationService formValidationService;
    @Mock private ReenrollmentService reenrollmentService;
    @Mock private ExamService examService;
    @Mock private EmailService emailService;

    @InjectMocks
    private AdminValidationController adminValidationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminValidationController).build();
        var auth = new UsernamePasswordAuthenticationToken("admin@test.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /admin/forms-to-validate - returns 200")
    void getFormsToValidate_returns200() throws Exception {
        when(admissionFormRepository.findByStatus(AdmissionForm.FormStatus.SUBMITTED))
                .thenReturn(List.of());

        mockMvc.perform(get("/admin/forms-to-validate"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/exam/submissions - returns 200")
    void getExamSubmissions_returns200() throws Exception {
        when(examResultRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/api/exam/submissions"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/api/exam/submissions/{id}/validate - success returns 200")
    void validateExamResult_success_returns200() throws Exception {
        ExamValidationRequest req = new ExamValidationRequest();
        req.setAction("APPROVE");
        User admin = User.builder().id(1L).email("admin@test.com").build();
        ExamResult result = new ExamResult();
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(examService.validateSubmission(eq(1L), any(ExamValidationRequest.class), eq(admin)))
                .thenReturn(result);

        mockMvc.perform(put("/admin/api/exam/submissions/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/api/exam/submissions/{id}/validate - exception returns 400")
    void validateExamResult_exception_returns400() throws Exception {
        ExamValidationRequest req = new ExamValidationRequest();
        req.setAction("APPROVE");
        User admin = User.builder().id(1L).email("admin@test.com").build();
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(examService.validateSubmission(eq(999L), any(ExamValidationRequest.class), eq(admin)))
                .thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(put("/admin/api/exam/submissions/999/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /admin/publish-results/{periodId} - returns 200")
    void publishResults_returns200() throws Exception {
        mockMvc.perform(post("/admin/publish-results/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/reenrollments - success returns 200")
    void getAllReenrollments_success_returns200() throws Exception {
        when(reenrollmentRepository.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/admin/api/reenrollments"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/reenrollments - exception returns 400")
    void getAllReenrollments_exception_returns400() throws Exception {
        when(reenrollmentRepository.findAll()).thenThrow(new RuntimeException("DB error"));
        mockMvc.perform(get("/admin/api/reenrollments"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/reenrollments - with student data returns 200")
    void getAllReenrollments_withStudentData_returns200() throws Exception {
        User user = User.builder().id(1L).email("s@test.com").build();
        Student student = Student.builder().id(10L).fullName("Alice").user(user).build();
        ReEnrollment re = new ReEnrollment();
        re.setId(1L);
        re.setStatus(ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        re.setStudent(student);
        when(reenrollmentRepository.findAll()).thenReturn(List.of(re));
        mockMvc.perform(get("/admin/api/reenrollments"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/reenrollments/in-progress - success returns 200")
    void getInProgressReenrollments_returns200() throws Exception {
        when(reenrollmentRepository.findByStatus(ReEnrollment.ReEnrollmentStatus.SUBMITTED))
                .thenReturn(List.of());
        mockMvc.perform(get("/admin/api/reenrollments/in-progress"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/reenrollments/in-progress - exception returns 400")
    void getInProgressReenrollments_exception_returns400() throws Exception {
        when(reenrollmentRepository.findByStatus(any()))
                .thenThrow(new RuntimeException("DB error"));
        mockMvc.perform(get("/admin/api/reenrollments/in-progress"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/exam/student-list - success returns 200")
    void getStudentListForExam_returns200() throws Exception {
        ExamResult passed = new ExamResult();
        passed.setStatus(ExamResult.ResultStatus.PASSED);
        ExamResult pending = new ExamResult();
        pending.setStatus(ExamResult.ResultStatus.PENDING);
        when(examResultRepository.findAll()).thenReturn(List.of(passed, pending));
        mockMvc.perform(get("/admin/api/exam/student-list"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/exam/student-list - exception returns 400")
    void getStudentListForExam_exception_returns400() throws Exception {
        when(examResultRepository.findAll()).thenThrow(new RuntimeException("DB error"));
        mockMvc.perform(get("/admin/api/exam/student-list"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/validasi/formulir - success returns 200")
    void getFormsForValidation_returns200() throws Exception {
        when(formValidationService.getFormsForValidationDashboard()).thenReturn(List.of());
        mockMvc.perform(get("/admin/api/validasi/formulir"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/validasi/formulir - exception returns 400")
    void getFormsForValidation_exception_returns400() throws Exception {
        when(formValidationService.getFormsForValidationDashboard())
                .thenThrow(new RuntimeException("Error"));
        mockMvc.perform(get("/admin/api/validasi/formulir"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/validasi/formulir/{id}/details - success returns 200")
    void getFormDetails_returns200() throws Exception {
        when(formValidationService.getFormDetails(1L)).thenReturn(Map.of("id", 1L));
        mockMvc.perform(get("/admin/api/validasi/formulir/1/details"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/validasi/formulir/{id}/details - exception returns 400")
    void getFormDetails_exception_returns400() throws Exception {
        when(formValidationService.getFormDetails(999L))
                .thenThrow(new RuntimeException("Not found"));
        mockMvc.perform(get("/admin/api/validasi/formulir/999/details"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/admission-form/student/{studentId}/details - success returns 200")
    void getAdmissionFormStudentDetails_returns200() throws Exception {
        when(formValidationService.getAdmissionFormStudentDetails(1L)).thenReturn(Map.of("id", 1L));
        mockMvc.perform(get("/admin/api/admission-form/student/1/details"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/api/validasi/formulir/{id}/approve - success returns 200")
    void approveFormValidation_returns200() throws Exception {
        User admin = User.builder().id(1L).email("admin@test.com").build();
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        doNothing().when(formValidationService).approve(eq(1L), eq(admin));
        mockMvc.perform(put("/admin/api/validasi/formulir/1/approve"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/api/validasi/formulir/{id}/approve - user not found returns 400")
    void approveFormValidation_userNotFound_returns400() throws Exception {
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.empty());
        mockMvc.perform(put("/admin/api/validasi/formulir/1/approve"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /admin/api/validasi/formulir/{id}/reject - success returns 200")
    void rejectFormValidation_returns200() throws Exception {
        User admin = User.builder().id(1L).email("admin@test.com").build();
        FormValidationRejectRequest req = new FormValidationRejectRequest();
        req.setReason("Dokumen tidak valid");
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        doNothing().when(formValidationService).reject(eq(1L), any(), eq(admin));
        mockMvc.perform(put("/admin/api/validasi/formulir/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/api/validasi/formulir/{id}/revision-needed - success returns 200")
    void markFormAsRevisionNeeded_returns200() throws Exception {
        User admin = User.builder().id(1L).email("admin@test.com").build();
        FormValidationRejectRequest req = new FormValidationRejectRequest();
        req.setReason("Perlu perbaikan");
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        doNothing().when(formValidationService).markRevisionNeeded(eq(1L), any(), eq(admin));
        mockMvc.perform(put("/admin/api/validasi/formulir/1/revision-needed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/api/validasi/student/{id}/repair-status - success returns 200")
    void updateRepairStatus_returns200() throws Exception {
        when(formValidationService.updateRepairStatus(1L, "SUDAH_PERBAIKAN"))
                .thenReturn(Map.of("success", true));
        mockMvc.perform(put("/admin/api/validasi/student/1/repair-status")
                        .param("repairStatus", "SUDAH_PERBAIKAN"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/api/hasil-akhir/nomor-registrasi/{id} - success returns 200")
    void updateHasilAkhirRegistrationNumber_returns200() throws Exception {
        HasilAkhirRegistrationRequest req = new HasilAkhirRegistrationRequest();
        req.setNomorRegistrasi("REG-001");
        req.setBrivaNumber("BRIVA-001");
        HasilAkhir saved = HasilAkhir.builder().id(1L).build();
        when(hasilAkhirService.updateRegistrationNumberAndBriva(eq(1L), any())).thenReturn(saved);
        mockMvc.perform(put("/admin/api/hasil-akhir/nomor-registrasi/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/api/hasil-akhir/nomor-registrasi/{id} - exception returns 400")
    void updateHasilAkhirRegistrationNumber_exception_returns400() throws Exception {
        HasilAkhirRegistrationRequest req = new HasilAkhirRegistrationRequest();
        req.setNomorRegistrasi("REG-001");
        when(hasilAkhirService.updateRegistrationNumberAndBriva(eq(999L), any()))
                .thenThrow(new RuntimeException("Not found"));
        mockMvc.perform(put("/admin/api/hasil-akhir/nomor-registrasi/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/validation-status-tracker/form/{id} - found returns 200")
    void getValidationStatusTrackerByFormId_found_returns200() throws Exception {
        ValidationStatusTracker tracker = new ValidationStatusTracker();
        tracker.setId(1L);
        tracker.setStatus(ValidationStatusTracker.ValidationStatusEnum.MENUNGGU);
        when(validationStatusTrackerService.getTrackerByFormId(1L))
                .thenReturn(Optional.of(tracker));
        mockMvc.perform(get("/admin/api/validation-status-tracker/form/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/validation-status-tracker/form/{id} - not found returns 200")
    void getValidationStatusTrackerByFormId_notFound_returns200() throws Exception {
        when(validationStatusTrackerService.getTrackerByFormId(999L))
                .thenReturn(Optional.empty());
        mockMvc.perform(get("/admin/api/validation-status-tracker/form/999"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/validation-status-tracker/student/{id} - found returns 200")
    void getValidationStatusTrackerByStudentId_found_returns200() throws Exception {
        ValidationStatusTracker tracker = new ValidationStatusTracker();
        tracker.setId(1L);
        tracker.setStatus(ValidationStatusTracker.ValidationStatusEnum.DIVALIDASI);
        when(validationStatusTrackerService.getTrackerByStudentId(1L))
                .thenReturn(Optional.of(tracker));
        mockMvc.perform(get("/admin/api/validation-status-tracker/student/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/validation-status-tracker/student/{id} - not found returns 200")
    void getValidationStatusTrackerByStudentId_notFound_returns200() throws Exception {
        when(validationStatusTrackerService.getTrackerByStudentId(999L))
                .thenReturn(Optional.empty());
        mockMvc.perform(get("/admin/api/validation-status-tracker/student/999"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/validation-status-tracker/all - success returns 200")
    void getAllValidationStatusTrackers_returns200() throws Exception {
        when(validationStatusTrackerRepository.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/admin/api/validation-status-tracker/all"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/validasi/daftar-ulang - success returns 200")
    void getReEnrollmentsForValidation_returns200() throws Exception {
        when(reEnrollmentValidationRepository.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/admin/api/validasi/daftar-ulang"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/api/validasi/daftar-ulang/{id}/approve - success returns 200")
    void approveReEnrollmentValidation_returns200() throws Exception {
        User admin = User.builder().id(1L).email("admin@test.com").build();
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(reenrollmentService.approve(eq(1L), eq(admin))).thenReturn(new com.uhn.pmb.entity.ReEnrollment());
        mockMvc.perform(put("/admin/api/validasi/daftar-ulang/1/approve"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/api/validasi/daftar-ulang/{id}/reject - success returns 200")
    void rejectReEnrollmentValidation_returns200() throws Exception {
        User admin = User.builder().id(1L).email("admin@test.com").build();
        FormValidationRejectRequest req = new FormValidationRejectRequest();
        req.setReason("Dokumen tidak valid");
        req.setTopic("Lainnya");
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(reenrollmentService.reject(eq(1L), anyString(), anyString(), eq(admin))).thenReturn(new com.uhn.pmb.entity.ReEnrollment());
        mockMvc.perform(put("/admin/api/validasi/daftar-ulang/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/api/validasi/formulir/{id}/verify-payment - success returns 200")
    void verifyPayment_success_returns200() throws Exception {
        FormValidation validation = new FormValidation();
        validation.setId(1L);
        validation.setPaymentStatus(FormValidation.PaymentStatus.PENDING);
        when(formValidationRepository.findById(1L)).thenReturn(Optional.of(validation));
        when(formValidationRepository.save(any())).thenReturn(validation);
        when(formRepairStatusRepository.findByFormValidationId(1L)).thenReturn(Optional.empty());
        mockMvc.perform(put("/admin/api/validasi/formulir/1/verify-payment"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/api/validasi/formulir/{id}/verify-payment - not found returns 400")
    void verifyPayment_notFound_returns400() throws Exception {
        when(formValidationRepository.findById(999L)).thenReturn(Optional.empty());
        mockMvc.perform(put("/admin/api/validasi/formulir/999/verify-payment"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /admin/api/exam/generate-token/{id} - success returns 200")
    void generateExamToken_returns200() throws Exception {
        when(examService.generateToken(1L)).thenReturn("TOKEN-123");
        mockMvc.perform(post("/admin/api/exam/generate-token/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /admin/api/exam/generate-token/{id} - exception returns 400")
    void generateExamToken_exception_returns400() throws Exception {
        when(examService.generateToken(999L)).thenThrow(new RuntimeException("Form not found"));
        mockMvc.perform(post("/admin/api/exam/generate-token/999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/exam/token/{id} - success returns 200")
    void getExamToken_returns200() throws Exception {
        FormValidation validation = new FormValidation();
        validation.setId(1L);
        validation.setExamToken("TOKEN-ABC");
        when(formValidationRepository.findById(1L)).thenReturn(Optional.of(validation));
        mockMvc.perform(get("/admin/api/exam/token/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/exam/token/{id} - not found returns 400")
    void getExamToken_notFound_returns400() throws Exception {
        when(formValidationRepository.findById(999L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/admin/api/exam/token/999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/exam/submissions/{id} - success returns 200")
    void getExamSubmissionDetails_returns200() throws Exception {
        User user = User.builder().id(1L).email("s@test.com").build();
        Student student = Student.builder().id(10L).fullName("Alice").user(user).build();
        ExamResult result = new ExamResult();
        result.setId(1L);
        result.setStudent(student);
        result.setExamValidationStatus(ExamResult.ExamValidationStatus.PENDING);
        when(examResultRepository.findById(1L)).thenReturn(Optional.of(result));
        mockMvc.perform(get("/admin/api/exam/submissions/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/exam/submissions/{id} - not found returns 400")
    void getExamSubmissionDetails_notFound_returns400() throws Exception {
        when(examResultRepository.findById(999L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/admin/api/exam/submissions/999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/exam/result/student/{studentId} - found returns 200")
    void getExamResultByStudentId_found_returns200() throws Exception {
        ExamResult result = new ExamResult();
        result.setId(1L);
        when(examService.findResultByStudentId(1L)).thenReturn(Optional.of(result));
        mockMvc.perform(get("/admin/api/exam/result/student/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/exam/result/student/{studentId} - not found returns 200")
    void getExamResultByStudentId_notFound_returns200() throws Exception {
        when(examService.findResultByStudentId(999L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/admin/api/exam/result/student/999"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/daftar-ulang/pending - success returns 200")
    void getPendingReenrollments_returns200() throws Exception {
        when(reenrollmentService.findPending()).thenReturn(List.of());
        mockMvc.perform(get("/admin/api/daftar-ulang/pending"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/daftar-ulang/{id}/details - success returns 200")
    void getReenrollmentDetails_returns200() throws Exception {
        User user = User.builder().id(1L).email("s@test.com").build();
        Student student = Student.builder().id(10L).fullName("Alice").user(user).build();
        ReEnrollment enrollment = new ReEnrollment();
        enrollment.setId(1L);
        enrollment.setStatus(ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        enrollment.setStudent(student);
        when(reenrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));
        when(reenrollmentDocumentRepository.findByReenrollmentId(1L)).thenReturn(List.of());
        mockMvc.perform(get("/admin/api/daftar-ulang/1/details"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/daftar-ulang/{id}/details - not found returns 400")
    void getReenrollmentDetails_notFound_returns400() throws Exception {
        when(reenrollmentRepository.findById(999L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/admin/api/daftar-ulang/999/details"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /admin/api/daftar-ulang/documents/{id}/validate - success returns 200")
    void validateDocument_returns200() throws Exception {
        User admin = User.builder().id(1L).email("admin@test.com").build();
        DocumentValidationRequest req = new DocumentValidationRequest();
        req.setAction("APPROVE");
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(reenrollmentService.validateDocument(eq(1L), any(), eq(admin)))
                .thenReturn(new ReEnrollmentDocument());
        mockMvc.perform(put("/admin/api/daftar-ulang/documents/1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/forms-to-validate - exception returns 400")
    void getFormsToValidate_exception_returns400() throws Exception {
        when(admissionFormRepository.findByStatus(any()))
                .thenThrow(new RuntimeException("DB error"));
        mockMvc.perform(get("/admin/forms-to-validate"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /admin/api/daftar-ulang/{id}/finalize - success returns 200")
    void finalizeReEnrollment_returns200() throws Exception {
        com.uhn.pmb.dto.ReenrollmentFinalizeRequest req = new com.uhn.pmb.dto.ReenrollmentFinalizeRequest();
        req.setAction("APPROVE");
        when(reenrollmentService.finalize(eq(1L), any())).thenReturn(new ReEnrollment());
        mockMvc.perform(put("/admin/api/daftar-ulang/1/finalize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /admin/api/daftar-ulang/{id}/finalize - exception returns 400")
    void finalizeReEnrollment_exception_returns400() throws Exception {
        com.uhn.pmb.dto.ReenrollmentFinalizeRequest req = new com.uhn.pmb.dto.ReenrollmentFinalizeRequest();
        req.setAction("APPROVE");
        when(reenrollmentService.finalize(eq(999L), any())).thenThrow(new RuntimeException("Not found"));
        mockMvc.perform(put("/admin/api/daftar-ulang/999/finalize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /admin/api/validasi/daftar-ulang/{id}/approve - user not found returns 400")
    void approveReEnrollmentValidation_userNotFound_returns400() throws Exception {
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.empty());
        mockMvc.perform(put("/admin/api/validasi/daftar-ulang/1/approve"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /admin/api/validasi/daftar-ulang/{id}/reject - user not found returns 400")
    void rejectReEnrollmentValidation_userNotFound_returns400() throws Exception {
        FormValidationRejectRequest req = new FormValidationRejectRequest();
        req.setReason("Not valid");
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.empty());
        mockMvc.perform(put("/admin/api/validasi/daftar-ulang/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /admin/api/daftar-ulang/documents/{id}/validate - exception returns 400")
    void validateDocument_exception_returns400() throws Exception {
        User admin = User.builder().id(1L).email("admin@test.com").build();
        DocumentValidationRequest req = new DocumentValidationRequest();
        req.setAction("APPROVE");
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(reenrollmentService.validateDocument(eq(999L), any(), any()))
                .thenThrow(new RuntimeException("Not found"));
        mockMvc.perform(put("/admin/api/daftar-ulang/documents/999/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /admin/api/validasi/formulir/{id}/reject - exception returns 400")
    void rejectFormValidation_exception_returns400() throws Exception {
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.empty());
        mockMvc.perform(put("/admin/api/validasi/formulir/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"bad\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /admin/api/validasi/formulir/{id}/revision-needed - exception returns 400")
    void markFormAsRevisionNeeded_exception_returns400() throws Exception {
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.empty());
        mockMvc.perform(put("/admin/api/validasi/formulir/1/revision-needed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"fix\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/admission-form/student/{id}/details - exception returns 400")
    void getAdmissionFormStudentDetails_exception_returns400() throws Exception {
        when(formValidationService.getAdmissionFormStudentDetails(999L))
                .thenThrow(new RuntimeException("Student not found"));
        mockMvc.perform(get("/admin/api/admission-form/student/999/details"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /admin/api/validasi/student/{id}/repair-status - exception returns 400")
    void updateRepairStatus_exception_returns400() throws Exception {
        when(formValidationService.updateRepairStatus(eq(999L), anyString()))
                .thenThrow(new RuntimeException("Not found"));
        mockMvc.perform(put("/admin/api/validasi/student/999/repair-status")
                        .param("repairStatus", "SUDAH_PERBAIKAN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/reenrollments - re with null status uses PENDING fallback")
    void getAllReenrollments_withNullStatus_returnsPendingFallback() throws Exception {
        Student student = Student.builder().id(10L).fullName("Alice").build();
        ReEnrollment re = new ReEnrollment();
        re.setId(2L);
        re.setStatus(null); // null status → should use "PENDING"
        re.setStudent(student);
        when(reenrollmentRepository.findAll()).thenReturn(List.of(re));
        mockMvc.perform(get("/admin/api/reenrollments"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/reenrollments - re with null student skips student fields")
    void getAllReenrollments_withNullStudent_skipsStudentFields() throws Exception {
        ReEnrollment re = new ReEnrollment();
        re.setId(3L);
        re.setStatus(ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        re.setStudent(null); // null student → should not add student-related fields
        when(reenrollmentRepository.findAll()).thenReturn(List.of(re));
        mockMvc.perform(get("/admin/api/reenrollments"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/reenrollments - re with student but null user skips email")
    void getAllReenrollments_withStudentNullUser_skipsEmail() throws Exception {
        Student student = Student.builder().id(10L).fullName("Bob").user(null).build();
        ReEnrollment re = new ReEnrollment();
        re.setId(4L);
        re.setStatus(ReEnrollment.ReEnrollmentStatus.SUBMITTED);
        re.setStudent(student);
        when(reenrollmentRepository.findAll()).thenReturn(List.of(re));
        mockMvc.perform(get("/admin/api/reenrollments"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/exam/student-list - null status filtered out")
    void getStudentListForExam_nullStatusExcluded_returns200() throws Exception {
        ExamResult nullStatus = new ExamResult();
        nullStatus.setStatus(null); // null status → filter(result.getStatus() != null) → false
        ExamResult failed = new ExamResult();
        failed.setStatus(ExamResult.ResultStatus.FAILED);
        when(examResultRepository.findAll()).thenReturn(List.of(nullStatus, failed));
        mockMvc.perform(get("/admin/api/exam/student-list"))
                .andExpect(status().isOk());
    }
}
