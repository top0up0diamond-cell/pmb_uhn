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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CamabaRegistrationServiceTest {

    @Mock private RegistrationPeriodRepository registrationPeriodRepository;
    @Mock private SelectionTypeRepository selectionTypeRepository;
    @Mock private JenisSeleksiRepository jenisSeleksiRepository;
    @Mock private PeriodJenisSeleksiRepository periodJenisSeleksiRepository;
    @Mock private SelectionProgramStudiRepository selectionProgramStudiRepository;

    @InjectMocks
    private CamabaRegistrationService camabaRegistrationService;

    private RegistrationPeriod openPeriod() {
        RegistrationPeriod p = new RegistrationPeriod();
        p.setId(1L);
        p.setName("Gelombang 1");
        p.setStatus(RegistrationPeriod.Status.OPEN);
        p.setRegStartDate(LocalDateTime.now().minusDays(5));
        p.setRegEndDate(LocalDateTime.now().plusDays(5));
        return p;
    }

    // ===== getAllGelombang =====

    @Test
    @DisplayName("getAllGelombang - open period has displayStatus=open")
    void getAllGelombang_openPeriod_displayStatusOpen() {
        when(registrationPeriodRepository.findAll()).thenReturn(new java.util.ArrayList<>(List.of(openPeriod())));

        List<Map<String, Object>> result = camabaRegistrationService.getAllGelombang();

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).get("displayStatus")).isEqualTo("open");
    }

    @Test
    @DisplayName("getAllGelombang - empty list returns empty")
    void getAllGelombang_empty_returnsEmpty() {
        when(registrationPeriodRepository.findAll()).thenReturn(new java.util.ArrayList<>());

        List<Map<String, Object>> result = camabaRegistrationService.getAllGelombang();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getAllGelombang - period not yet open has displayStatus=notopen")
    void getAllGelombang_notYetOpen_displayStatusNotOpen() {
        RegistrationPeriod period = new RegistrationPeriod();
        period.setId(2L);
        period.setName("Gelombang 2");
        period.setStatus(RegistrationPeriod.Status.OPEN);
        period.setRegStartDate(LocalDateTime.now().plusDays(5));
        period.setRegEndDate(LocalDateTime.now().plusDays(15));
        when(registrationPeriodRepository.findAll()).thenReturn(new java.util.ArrayList<>(List.of(period)));

        List<Map<String, Object>> result = camabaRegistrationService.getAllGelombang();

        assertThat(result.get(0).get("displayStatus")).isEqualTo("notopen");
    }

    @Test
    @DisplayName("getAllGelombang - ended period has displayStatus=closed")
    void getAllGelombang_closed_displayStatusClosed() {
        RegistrationPeriod period = new RegistrationPeriod();
        period.setId(3L);
        period.setName("Gelombang 3");
        period.setStatus(RegistrationPeriod.Status.OPEN);
        period.setRegStartDate(LocalDateTime.now().minusDays(20));
        period.setRegEndDate(LocalDateTime.now().minusDays(5));
        when(registrationPeriodRepository.findAll()).thenReturn(new java.util.ArrayList<>(List.of(period)));

        List<Map<String, Object>> result = camabaRegistrationService.getAllGelombang();

        assertThat(result.get(0).get("displayStatus")).isEqualTo("closed");
    }

    // ===== getAllFormulas =====

    @Test
    @DisplayName("getAllFormulas - period not found returns empty list")
    void getAllFormulas_periodNotFound_returnsEmpty() {
        when(registrationPeriodRepository.findById(999L)).thenReturn(Optional.empty());

        List<Map<String, Object>> result = camabaRegistrationService.getAllFormulas(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getAllFormulas - null periodId calls findByIsActiveTrueOrderBySortOrder")
    void getAllFormulas_nullPeriodId_callsGlobalList() {
        JenisSeleksi js = new JenisSeleksi();
        js.setId(1L);
        js.setCode("SNBT");
        js.setNama("SNBT");
        js.setHarga(BigDecimal.valueOf(500000L));
        when(jenisSeleksiRepository.findByIsActiveTrueOrderBySortOrder()).thenReturn(List.of(js));

        List<Map<String, Object>> result = camabaRegistrationService.getAllFormulas(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("code")).isEqualTo("SNBT");
    }

    @Test
    @DisplayName("getAllFormulas - valid periodId returns linked jenis seleksi")
    void getAllFormulas_validPeriod_returnsLinkedFormulas() {
        RegistrationPeriod period = new RegistrationPeriod();
        period.setId(1L);
        JenisSeleksi js = new JenisSeleksi();
        js.setId(2L);
        js.setCode("UJIAN");
        js.setNama("Ujian Mandiri");
        js.setHarga(BigDecimal.valueOf(700000L));
        PeriodJenisSeleksi pjs = new PeriodJenisSeleksi();
        pjs.setJenisSeleksi(js);

        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(periodJenisSeleksiRepository.findByPeriod_IdAndIsActiveTrue(1L)).thenReturn(List.of(pjs));

        List<Map<String, Object>> result = camabaRegistrationService.getAllFormulas(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("code")).isEqualTo("UJIAN");
    }

    // ===== getProgramStudiByJenisSeleksi =====

    @Test
    @DisplayName("getProgramStudiByJenisSeleksi - jenis seleksi not found returns empty")
    void getProgramStudiByJenisSeleksi_notFound_returnsEmpty() {
        when(jenisSeleksiRepository.findById(999L)).thenReturn(Optional.empty());

        List<Map<String, Object>> result = camabaRegistrationService.getProgramStudiByJenisSeleksi(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getProgramStudiByJenisSeleksi - found with program studi returns list")
    void getProgramStudiByJenisSeleksi_found_returnsList() {
        JenisSeleksi js = new JenisSeleksi();
        js.setId(1L);
        ProgramStudi ps = new ProgramStudi();
        ps.setId(10L);
        ps.setNama("Teknik Informatika");
        ps.setKode("TI");
        ps.setIsMedical(false);
        ps.setHargaTotalPerTahun(15000000L);
        SelectionProgramStudi sps = new SelectionProgramStudi();
        sps.setProgramStudi(ps);

        when(jenisSeleksiRepository.findById(1L)).thenReturn(Optional.of(js));
        when(selectionProgramStudiRepository.findByJenisSeleksi_IdAndIsActiveTrue(1L)).thenReturn(List.of(sps));

        List<Map<String, Object>> result = camabaRegistrationService.getProgramStudiByJenisSeleksi(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("nama")).isEqualTo("Teknik Informatika");
    }

    // ===== getSelectionTypeDetail =====

    @Test
    @DisplayName("getSelectionTypeDetail - found returns detail map")
    void getSelectionTypeDetail_found_returnsMap() {
        SelectionType st = new SelectionType();
        st.setId(1L);
        st.setName("Reguler");
        st.setRequireTesting(true);
        st.setRequireRanking(false);
        st.setDescription("Seleksi reguler");
        st.setFormType(SelectionType.FormType.NON_MEDICAL);
        st.setPrice(BigDecimal.valueOf(500000L));

        when(selectionTypeRepository.findById(1L)).thenReturn(Optional.of(st));

        Map<String, Object> result = camabaRegistrationService.getSelectionTypeDetail(1L);

        assertThat(result.get("name")).isEqualTo("Reguler");
        assertThat(result.get("requireTesting")).isEqualTo(true);
    }

    @Test
    @DisplayName("getSelectionTypeDetail - not found throws RuntimeException")
    void getSelectionTypeDetail_notFound_throwsException() {
        when(selectionTypeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaRegistrationService.getSelectionTypeDetail(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("999");
    }

    // ===== getJenisSeleksiDetail =====

    @Test
    @DisplayName("getJenisSeleksiDetail - found returns detail map")
    void getJenisSeleksiDetail_found_returnsMap() {
        JenisSeleksi js = new JenisSeleksi();
        js.setId(1L);
        js.setCode("SNBT");
        js.setNama("SNBT");
        js.setDeskripsi("Seleksi nasional");
        js.setHarga(BigDecimal.valueOf(300000L));
        js.setFasilitas("Bimbel,Konsultasi");
        js.setIsActive(true);

        when(jenisSeleksiRepository.findById(1L)).thenReturn(Optional.of(js));

        Map<String, Object> result = camabaRegistrationService.getJenisSeleksiDetail(1L);

        assertThat(result.get("code")).isEqualTo("SNBT");
        assertThat(result.get("harga")).isEqualTo(BigDecimal.valueOf(300000L));
    }

    @Test
    @DisplayName("getJenisSeleksiDetail - not found throws RuntimeException")
    void getJenisSeleksiDetail_notFound_throwsException() {
        when(jenisSeleksiRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaRegistrationService.getJenisSeleksiDetail(999L))
                .isInstanceOf(RuntimeException.class);
    }

    // ===== getJenisSeleksiById =====

    @Test
    @DisplayName("getJenisSeleksiById - found returns basic map")
    void getJenisSeleksiById_found_returnsBasicMap() {
        JenisSeleksi js = new JenisSeleksi();
        js.setId(5L);
        js.setCode("PMDK");
        js.setNama("PMDK");
        js.setHarga(BigDecimal.ZERO);
        js.setIsActive(true);

        when(jenisSeleksiRepository.findById(5L)).thenReturn(Optional.of(js));

        Map<String, Object> result = camabaRegistrationService.getJenisSeleksiById(5L);

        assertThat(result.get("code")).isEqualTo("PMDK");
        assertThat(result.get("id")).isEqualTo(5L);
    }

    @Test
    @DisplayName("getJenisSeleksiById - not found throws RuntimeException")
    void getJenisSeleksiById_notFound_throwsException() {
        when(jenisSeleksiRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> camabaRegistrationService.getJenisSeleksiById(999L))
                .isInstanceOf(RuntimeException.class);
    }
}
