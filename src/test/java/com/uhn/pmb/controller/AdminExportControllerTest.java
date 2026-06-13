package com.uhn.pmb.controller;

import com.uhn.pmb.entity.RegistrationPeriod;
import com.uhn.pmb.repository.RegistrationPeriodRepository;
import com.uhn.pmb.service.AdminDataExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminExportControllerTest {

    @Mock private AdminDataExportService adminDataExportService;
    @Mock private RegistrationPeriodRepository registrationPeriodRepository;
    @InjectMocks private AdminExportController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /admin/api/export/formulir-pembayaran - returns 200")
    void exportFormAndPayment_returns200() throws Exception {
        when(adminDataExportService.exportFormAndPayment()).thenReturn(List.of());

        mockMvc.perform(get("/admin/api/export/formulir-pembayaran"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/table/admission-forms - returns 200")
    void getAdmissionFormsTable_returns200() throws Exception {
        when(adminDataExportService.getAdmissionFormsTable()).thenReturn(List.of());

        mockMvc.perform(get("/admin/api/table/admission-forms"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/table/hasil-akhir/by-wave/{waveType} - invalid wave returns 400")
    void getHasilAkhirByWave_invalidWave_returns400() throws Exception {
        mockMvc.perform(get("/admin/api/table/hasil-akhir/by-wave/INVALID_WAVE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/table/hasil-akhir/by-wave/{waveType} - valid wave returns 200")
    void getHasilAkhirByWave_validWave_returns200() throws Exception {
        when(adminDataExportService.getHasilAkhirByWave(RegistrationPeriod.WaveType.REGULAR_TEST))
                .thenReturn(List.of());

        mockMvc.perform(get("/admin/api/table/hasil-akhir/by-wave/REGULAR_TEST"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/wave-types - returns 200")
    void getWaveTypes_returns200() throws Exception {
        when(registrationPeriodRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/api/wave-types"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/export/daftar-ulang - returns 200")
    void exportReEnrollmentData_returns200() throws Exception {
        when(adminDataExportService.exportReEnrollmentData()).thenReturn(List.of());

        mockMvc.perform(get("/admin/api/export/daftar-ulang"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/export/hasil-akhir - returns 200")
    void exportHasilAkhirData_returns200() throws Exception {
        when(adminDataExportService.exportHasilAkhirData()).thenReturn(List.of());

        mockMvc.perform(get("/admin/api/export/hasil-akhir"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/table/reenrollments - returns 200")
    void getReenrollmentsTable_returns200() throws Exception {
        when(adminDataExportService.getReenrollmentsTable()).thenReturn(List.of());

        mockMvc.perform(get("/admin/api/table/reenrollments"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/table/hasil-akhir - returns 200")
    void getHasilAkhirTable_returns200() throws Exception {
        when(adminDataExportService.getHasilAkhirTable()).thenReturn(List.of());

        mockMvc.perform(get("/admin/api/table/hasil-akhir"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/api/export/formulir-pembayaran - service exception returns 400")
    void exportFormAndPayment_serviceException_returns400() throws Exception {
        when(adminDataExportService.exportFormAndPayment()).thenThrow(new RuntimeException("Export failed"));

        mockMvc.perform(get("/admin/api/export/formulir-pembayaran"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/export/daftar-ulang - service exception returns 400")
    void exportReEnrollmentData_serviceException_returns400() throws Exception {
        when(adminDataExportService.exportReEnrollmentData()).thenThrow(new RuntimeException("Export failed"));

        mockMvc.perform(get("/admin/api/export/daftar-ulang"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/export/hasil-akhir - service exception returns 400")
    void exportHasilAkhirData_serviceException_returns400() throws Exception {
        when(adminDataExportService.exportHasilAkhirData()).thenThrow(new RuntimeException("Export failed"));

        mockMvc.perform(get("/admin/api/export/hasil-akhir"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/table/admission-forms - service exception returns 400")
    void getAdmissionFormsTable_serviceException_returns400() throws Exception {
        when(adminDataExportService.getAdmissionFormsTable()).thenThrow(new RuntimeException("Fetch failed"));

        mockMvc.perform(get("/admin/api/table/admission-forms"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/table/reenrollments - service exception returns 400")
    void getReenrollmentsTable_serviceException_returns400() throws Exception {
        when(adminDataExportService.getReenrollmentsTable()).thenThrow(new RuntimeException("Fetch failed"));

        mockMvc.perform(get("/admin/api/table/reenrollments"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/table/hasil-akhir - service exception returns 400")
    void getHasilAkhirTable_serviceException_returns400() throws Exception {
        when(adminDataExportService.getHasilAkhirTable()).thenThrow(new RuntimeException("Fetch failed"));

        mockMvc.perform(get("/admin/api/table/hasil-akhir"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/table/hasil-akhir/by-wave/{waveType} - exception returns 400")
    void getHasilAkhirByWave_serviceException_returns400() throws Exception {
        when(adminDataExportService.getHasilAkhirByWave(RegistrationPeriod.WaveType.REGULAR_TEST))
                .thenThrow(new RuntimeException("Fetch failed"));

        mockMvc.perform(get("/admin/api/table/hasil-akhir/by-wave/REGULAR_TEST"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /admin/api/wave-types - service exception returns 400")
    void getWaveTypes_serviceException_returns400() throws Exception {
        when(registrationPeriodRepository.findAll()).thenThrow(new RuntimeException("Fetch failed"));

        mockMvc.perform(get("/admin/api/wave-types"))
                .andExpect(status().isBadRequest());
    }

    // ===== ITERASI 6 ADDITIONS =====

    @Test
    @DisplayName("GET /admin/api/wave-types - with periods having WaveType triggers lambda chain")
    void getWaveTypes_withPeriodsHavingWaveType_returns200() throws Exception {
        RegistrationPeriod period1 = RegistrationPeriod.builder()
                .id(1L).name("Gelombang 1").waveType(RegistrationPeriod.WaveType.EARLY_NO_TEST).build();
        RegistrationPeriod period2 = RegistrationPeriod.builder()
                .id(2L).name("Gelombang 2").waveType(RegistrationPeriod.WaveType.REGULAR_TEST).build();
        RegistrationPeriod period3 = RegistrationPeriod.builder()
                .id(3L).name("Gelombang 3").waveType(null).build();

        when(registrationPeriodRepository.findAll()).thenReturn(List.of(period1, period2, period3));

        mockMvc.perform(get("/admin/api/wave-types"))
                .andExpect(status().isOk());
    }

}
