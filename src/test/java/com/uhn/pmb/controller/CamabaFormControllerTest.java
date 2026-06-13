package com.uhn.pmb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uhn.pmb.dto.AdmissionFormSubmitRequest;
import com.uhn.pmb.dto.RegistrationRequest;
import com.uhn.pmb.dto.SubmitRevisionRequest;
import com.uhn.pmb.entity.SelectionType;
import com.uhn.pmb.repository.SelectionTypeRepository;
import com.uhn.pmb.repository.UserRepository;
import com.uhn.pmb.service.AdmissionFormService;
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

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CamabaFormControllerTest {

    @Mock
    private AdmissionFormService admissionFormService;
    @Mock
    private SelectionTypeRepository selectionTypeRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CamabaFormController camabaFormController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(camabaFormController).build();
        var auth = new UsernamePasswordAuthenticationToken("u@test.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /api/camaba/submission-status - returns form submission status")
    void checkSubmissionStatus_returns200() throws Exception {
        when(admissionFormService.checkSubmissionStatus("u@test.com")).thenReturn(
                Map.of("hasSubmitted", true));

        mockMvc.perform(get("/api/camaba/submission-status"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/submission-status - no form returns no_form status")
    void checkSubmissionStatus_noForm_returns200() throws Exception {
        when(admissionFormService.checkSubmissionStatus("u@test.com")).thenReturn(
                Map.of("hasSubmitted", false));

        mockMvc.perform(get("/api/camaba/submission-status"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/submission-status - exception returns 400")
    void checkSubmissionStatus_userNotFound_returns404() throws Exception {
        when(admissionFormService.checkSubmissionStatus("u@test.com"))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/camaba/submission-status"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/camaba/admission-form - success returns 200")
    void getAdmissionFormData_returns200() throws Exception {
        when(admissionFormService.getAdmissionFormData("u@test.com"))
                .thenReturn(Map.of("formId", 1L));
        mockMvc.perform(get("/api/camaba/admission-form"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/admission-form - exception returns 500")
    void getAdmissionFormData_exception_returns500() throws Exception {
        when(admissionFormService.getAdmissionFormData("u@test.com"))
                .thenThrow(new RuntimeException("DB error"));
        mockMvc.perform(get("/api/camaba/admission-form"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/camaba/admission-forms/current - success returns 200")
    void getCurrentAdmissionFormData_returns200() throws Exception {
        when(admissionFormService.getCurrentAdmissionFormData("u@test.com"))
                .thenReturn(Map.of("formId", 1L));
        mockMvc.perform(get("/api/camaba/admission-forms/current"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/admission-forms/current - exception returns 500")
    void getCurrentAdmissionFormData_exception_returns500() throws Exception {
        when(admissionFormService.getCurrentAdmissionFormData("u@test.com"))
                .thenThrow(new RuntimeException("Error"));
        mockMvc.perform(get("/api/camaba/admission-forms/current"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/camaba/admission-forms/me - success returns 200")
    void getMyAdmissionForm_returns200() throws Exception {
        when(admissionFormService.getCurrentAdmissionFormData("u@test.com"))
                .thenReturn(Map.of("formId", 1L));
        mockMvc.perform(get("/api/camaba/admission-forms/me"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/camaba/submit-admission-form - success returns 200")
    void submitAdmissionForm_returns200() throws Exception {
        when(admissionFormService.submitAdmissionForm(anyString(), any()))
                .thenReturn(Map.of("success", true));
        mockMvc.perform(post("/api/camaba/submit-admission-form")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/camaba/submit-admission-form - exception returns 400")
    void submitAdmissionForm_exception_returns400() throws Exception {
        when(admissionFormService.submitAdmissionForm(anyString(), any()))
                .thenThrow(new RuntimeException("Validation error"));
        mockMvc.perform(post("/api/camaba/submit-admission-form")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/camaba/admission-forms/update-selection - success returns 200")
    void updateAdmissionFormSelection_returns200() throws Exception {
        when(admissionFormService.updateAdmissionFormSelection(anyString(), any()))
                .thenReturn(Map.of("success", true));
        mockMvc.perform(post("/api/camaba/admission-forms/update-selection")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"selectionTypeId\":1}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/camaba/register-admission - success returns 201")
    void registerAdmission_returns201() throws Exception {
        when(admissionFormService.registerForAdmission(anyString(), any(), any(), any()))
                .thenReturn(new com.uhn.pmb.entity.AdmissionForm());
        RegistrationRequest req = new RegistrationRequest();
        req.setPeriodId(1L);
        req.setSelectionTypeId(1L);
        req.setProgramStudi("Teknik Informatika");
        mockMvc.perform(post("/api/camaba/register-admission")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("GET /api/camaba/admission-status - success returns 200")
    void getAdmissionStatus_returns200() throws Exception {
        when(admissionFormService.getAdmissionStatus("u@test.com"))
                .thenReturn(java.util.List.of());
        mockMvc.perform(get("/api/camaba/admission-status"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/admission-status - exception returns 400")
    void getAdmissionStatus_exception_returns400() throws Exception {
        when(admissionFormService.getAdmissionStatus("u@test.com"))
                .thenThrow(new RuntimeException("Error"));
        mockMvc.perform(get("/api/camaba/admission-status"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/camaba/admission-form - success returns 200")
    void updateAdmissionFormData_returns200() throws Exception {
        when(admissionFormService.updateAdmissionFormData(anyString(), any()))
                .thenReturn(Map.of("success", true));
        mockMvc.perform(multipart(org.springframework.http.HttpMethod.PUT, "/api/camaba/admission-form"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/camaba/admission-form - exception returns 400")
    void updateAdmissionFormData_exception_returns400() throws Exception {
        when(admissionFormService.updateAdmissionFormData(anyString(), any()))
                .thenThrow(new RuntimeException("Update error"));
        mockMvc.perform(multipart(org.springframework.http.HttpMethod.PUT, "/api/camaba/admission-form"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/camaba/test-selection-type - with valid selectionTypeId returns 200")
    void testSelectionType_withValidId_returns200() throws Exception {
        SelectionType st = new SelectionType();
        st.setId(1L);
        when(selectionTypeRepository.findById(1L)).thenReturn(java.util.Optional.of(st));
        mockMvc.perform(post("/api/camaba/test-selection-type")
                        .param("selectionTypeId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("testSelectionType - invalid selectionTypeId returns 400")
    void testSelectionType_noId_returns400() throws Exception {
        mockMvc.perform(post("/api/camaba/test-selection-type")
                        .param("selectionTypeId", "notanumber"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/camaba/admission-forms/update-selection - exception returns 400")
    void updateAdmissionFormSelection_exception_returns400() throws Exception {
        when(admissionFormService.updateAdmissionFormSelection(anyString(), any()))
                .thenThrow(new RuntimeException("Error"));
        mockMvc.perform(post("/api/camaba/admission-forms/update-selection")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"selectionTypeId\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/camaba/register-admission - exception returns 400")
    void registerAdmission_exception_returns400() throws Exception {
        when(admissionFormService.registerForAdmission(anyString(), any(), any(), any()))
                .thenThrow(new RuntimeException("Already registered"));
        RegistrationRequest req = new RegistrationRequest();
        req.setPeriodId(1L);
        req.setSelectionTypeId(1L);
        req.setProgramStudi("Teknik Informatika");
        mockMvc.perform(post("/api/camaba/register-admission")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/camaba/submit-revision/{formId} - success returns 200")
    void submitRevision_returns200() throws Exception {
        when(admissionFormService.submitRevision(anyString(), any(Long.class), any(SubmitRevisionRequest.class)))
                .thenReturn(Map.of("success", true));
        mockMvc.perform(put("/api/camaba/submit-revision/1")
                        .param("fullName", "John Doe")
                        .param("nik", "1234567890123456")
                        .param("birthDate", "2000-01-01")
                        .param("birthPlace", "Medan")
                        .param("gender", "L")
                        .param("phoneNumber", "081234567890")
                        .param("email", "j@test.com")
                        .param("subdistrict", "Medan Baru")
                        .param("district", "Medan Kota")
                        .param("city", "Medan")
                        .param("province", "Sumatera Utara")
                        .param("religion", "Kristen")
                        .param("fatherName", "Father Doe")
                        .param("fatherStatus", "HIDUP")
                        .param("motherName", "Mother Doe")
                        .param("motherStatus", "HIDUP")
                        .param("parentSubdistrict", "Medan Baru")
                        .param("parentCity", "Medan")
                        .param("parentProvince", "Sumatera Utara")
                        .param("schoolOrigin", "SMA Negeri 1 Medan")
                        .param("schoolMajor", "IPA")
                        .param("schoolYear", "2020"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/camaba/submit-revision/{formId} - exception returns 500")
    void submitRevision_exception_returns500() throws Exception {
        when(admissionFormService.submitRevision(anyString(), any(Long.class), any(SubmitRevisionRequest.class)))
                .thenThrow(new RuntimeException("Revision error"));
        mockMvc.perform(put("/api/camaba/submit-revision/1")
                        .param("fullName", "John Doe")
                        .param("nik", "1234567890123456")
                        .param("birthDate", "2000-01-01")
                        .param("birthPlace", "Medan")
                        .param("gender", "L")
                        .param("phoneNumber", "081234567890")
                        .param("email", "j@test.com")
                        .param("subdistrict", "Medan Baru")
                        .param("district", "Medan Kota")
                        .param("city", "Medan")
                        .param("province", "Sumatera Utara")
                        .param("religion", "Kristen")
                        .param("fatherName", "Father Doe")
                        .param("fatherStatus", "HIDUP")
                        .param("motherName", "Mother Doe")
                        .param("motherStatus", "HIDUP")
                        .param("parentSubdistrict", "Medan Baru")
                        .param("parentCity", "Medan")
                        .param("parentProvince", "Sumatera Utara")
                        .param("schoolOrigin", "SMA Negeri 1 Medan")
                        .param("schoolMajor", "IPA")
                        .param("schoolYear", "2020"))
                .andExpect(status().isInternalServerError());
    }
}