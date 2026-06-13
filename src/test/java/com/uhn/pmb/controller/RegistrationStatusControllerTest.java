package com.uhn.pmb.controller;

import com.uhn.pmb.entity.RegistrationStatus;
import com.uhn.pmb.entity.RegistrationStatus.RegistrationStage;
import com.uhn.pmb.service.RegistrationStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RegistrationStatusControllerTest {

    @Mock private RegistrationStatusService registrationStatusService;
    @InjectMocks private RegistrationStatusController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /api/camaba/registration-status/all - returns 200")
    void getAllStatuses_returns200() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("student@test.com");
        when(registrationStatusService.getUserStatusesByEmail("student@test.com")).thenReturn(List.of());

        mockMvc.perform(get("/api/camaba/registration-status/all")
                        .principal(auth))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/registration-status/{stage} - returns 200")
    void getStatus_returns200() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("student@test.com");
        when(registrationStatusService.getStatusByEmail("student@test.com", RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/camaba/registration-status/FORM_SUBMISSION")
                        .principal(auth))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/registration-status/{stage}/can-edit - returns 200")
    void canEdit_returns200() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("student@test.com");
        when(registrationStatusService.canUserEditByEmail("student@test.com", RegistrationStage.PAYMENT_BRIVA))
                .thenReturn(true);

        mockMvc.perform(get("/api/camaba/registration-status/PAYMENT_BRIVA/can-edit")
                        .principal(auth))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/camaba/registration-status/{stage}/complete - returns 200")
    void completeStage_returns200() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("student@test.com");
        RegistrationStatus rs = new RegistrationStatus();
        when(registrationStatusService.markAsCompletedByEmail(any(), any(), any())).thenReturn(rs);

        mockMvc.perform(post("/api/camaba/registration-status/FORM_SUBMISSION/complete")
                        .principal(auth)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/camaba/registration-status/{stage}/update - returns 200")
    void updateStageData_returns200() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("student@test.com");
        when(registrationStatusService.canUserEditByEmail(any(), any())).thenReturn(true);
        RegistrationStatus rs = new RegistrationStatus();
        when(registrationStatusService.updateStatusDataByEmail(any(), any(), any())).thenReturn(rs);

        mockMvc.perform(put("/api/camaba/registration-status/FORM_SUBMISSION/update")
                        .principal(auth)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"data\":\"test\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/registration-status/all - with statuses returns data")
    void getAllStatuses_withStatuses_returns200() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("student@test.com");

        RegistrationStatus rs = new RegistrationStatus();
        rs.setId(1L);
        rs.setStage(RegistrationStage.FORM_SUBMISSION);
        rs.setStatus(RegistrationStatus.RegistrationStatus_Enum.MENUNGGU_VERIFIKASI);
        rs.setCanEdit(true);
        rs.setAdminVerified(false);
        rs.setEditCount(0);

        when(registrationStatusService.getUserStatusesByEmail("student@test.com")).thenReturn(List.of(rs));
        when(registrationStatusService.getEditTimeRemainingByEmail(eq("student@test.com"), any())).thenReturn(24L);

        mockMvc.perform(get("/api/camaba/registration-status/all")
                        .principal(auth))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/camaba/registration-status/{stage} - found returns data")
    void getStatus_found_returnsData() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("student@test.com");

        RegistrationStatus rs = new RegistrationStatus();
        rs.setId(1L);
        rs.setStage(RegistrationStage.FORM_SUBMISSION);
        rs.setStatus(RegistrationStatus.RegistrationStatus_Enum.SELESAI);

        when(registrationStatusService.getStatusByEmail("student@test.com", RegistrationStage.FORM_SUBMISSION))
                .thenReturn(Optional.of(rs));
        when(registrationStatusService.canUserEditByEmail("student@test.com", RegistrationStage.FORM_SUBMISSION))
                .thenReturn(false);
        when(registrationStatusService.getEditTimeRemainingByEmail("student@test.com", RegistrationStage.FORM_SUBMISSION))
                .thenReturn(0L);

        mockMvc.perform(get("/api/camaba/registration-status/FORM_SUBMISSION")
                        .principal(auth))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/camaba/registration-status/{stage}/update - canEdit=false returns 400")
    void updateStageData_cannotEdit_returns400() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("student@test.com");
        when(registrationStatusService.canUserEditByEmail(any(), any())).thenReturn(false);
        when(registrationStatusService.getEditTimeRemainingByEmail(any(), any())).thenReturn(0L);

        mockMvc.perform(put("/api/camaba/registration-status/FORM_SUBMISSION/update")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"data\":\"test\"}"))
                .andExpect(status().isBadRequest());
    }
}
