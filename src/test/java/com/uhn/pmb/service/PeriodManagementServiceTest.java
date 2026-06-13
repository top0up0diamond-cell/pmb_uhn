package com.uhn.pmb.service;

import com.uhn.pmb.dto.RegistrationPeriodRequest;
import com.uhn.pmb.dto.SelectionTypeRequest;
import com.uhn.pmb.entity.*;
import com.uhn.pmb.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PeriodManagementServiceTest {

    @Mock private RegistrationPeriodRepository registrationPeriodRepository;
    @Mock private PeriodJenisSeleksiRepository periodJenisSeleksiRepository;
    @Mock private JenisSeleksiRepository jenisSeleksiRepository;
    @Mock private SelectionTypeRepository selectionTypeRepository;
    @Mock private EntityManager entityManager;

    @InjectMocks
    private PeriodManagementService periodManagementService;

    @Test
    @DisplayName("createPeriod - saves and returns period")
    void createPeriod_savesNewPeriod() {
        RegistrationPeriodRequest req = new RegistrationPeriodRequest();
        req.setName("Gel 1");
        req.setJenisSeleksiIds(List.of());
        RegistrationPeriod saved = RegistrationPeriod.builder().id(1L).name("Gel 1").build();
        when(registrationPeriodRepository.save(any())).thenReturn(saved);
        when(jenisSeleksiRepository.findByCode("KEDOKTERAN")).thenReturn(Optional.empty());

        RegistrationPeriod result = periodManagementService.createPeriod(req);

        assertThat(result.getName()).isEqualTo("Gel 1");
    }

    @Test
    @DisplayName("updatePeriod - period not found throws RuntimeException")
    void updatePeriod_notFound_throws() {
        when(registrationPeriodRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> periodManagementService.updatePeriod(99L, new RegistrationPeriodRequest()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("deletePeriod - period not found throws RuntimeException")
    void deletePeriod_notFound_throws() {
        when(registrationPeriodRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> periodManagementService.deletePeriod(99L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getPeriodById - found returns period")
    void getPeriodById_found_returnsPeriod() {
        RegistrationPeriod p = RegistrationPeriod.builder().id(1L).build();
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(p));

        RegistrationPeriod result = periodManagementService.getPeriodById(1L);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("getPeriodById - not found throws RuntimeException")
    void getPeriodById_notFound_throws() {
        when(registrationPeriodRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> periodManagementService.getPeriodById(99L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getAllPeriods - returns list")
    void getAllPeriods_returnsList() {
        when(registrationPeriodRepository.findAll()).thenReturn(List.of(new RegistrationPeriod()));

        List<RegistrationPeriod> result = periodManagementService.getAllPeriods();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getAllAvailableJenisSeleksi - returns list")
    void getAllAvailableJenisSeleksi_returnsList() {
        when(jenisSeleksiRepository.findByIsActiveTrueOrderBySortOrder()).thenReturn(List.of());

        List<JenisSeleksi> result = periodManagementService.getAllAvailableJenisSeleksi();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getJenisSeleksiByPeriod - empty list returns empty")
    void getJenisSeleksiByPeriod_emptyList() {
        RegistrationPeriod p = RegistrationPeriod.builder().id(1L).build();
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(p));
        when(periodJenisSeleksiRepository.findByPeriod_IdAndIsActiveTrue(1L)).thenReturn(List.of());

        List<Map<String, Object>> result = periodManagementService.getJenisSeleksiByPeriod(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deleteSelectionType - not found throws RuntimeException")
    void deleteSelectionType_notFound_throws() {
        when(selectionTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> periodManagementService.deleteSelectionType(99L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("createSelectionType - saves and returns")
    void createSelectionType_savesOk() {
        SelectionTypeRequest req = new SelectionTypeRequest();
        req.setName("IPA");
        req.setPeriodId(1L);
        RegistrationPeriod p = RegistrationPeriod.builder().id(1L).build();
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(p));

        periodManagementService.createSelectionType(req);

        verify(selectionTypeRepository).save(any());
    }

    @Test
    @DisplayName("updatePeriod - found updates and returns period")
    void updatePeriod_found_updatesAndReturns() {
        RegistrationPeriodRequest req = new RegistrationPeriodRequest();
        req.setName("Updated Period");
        req.setJenisSeleksiIds(null);
        RegistrationPeriod period = RegistrationPeriod.builder().id(1L).name("Old").build();
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(registrationPeriodRepository.save(any())).thenReturn(period);
        doNothing().when(periodJenisSeleksiRepository).deleteByPeriod_Id(1L);
        doNothing().when(entityManager).flush();

        RegistrationPeriod result = periodManagementService.updatePeriod(1L, req);

        assertThat(result.getName()).isEqualTo("Updated Period");
        verify(registrationPeriodRepository).save(any());
    }

    @Test
    @DisplayName("updatePeriod - with jenisSeleksiIds links them")
    void updatePeriod_withJenisSeleksiIds_linksSeleksi() {
        RegistrationPeriodRequest req = new RegistrationPeriodRequest();
        req.setName("Updated");
        req.setJenisSeleksiIds(List.of(1L));
        RegistrationPeriod period = RegistrationPeriod.builder().id(1L).name("Old").build();
        JenisSeleksi js = JenisSeleksi.builder().id(1L).isActive(true).build();
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(registrationPeriodRepository.save(any())).thenReturn(period);
        doNothing().when(periodJenisSeleksiRepository).deleteByPeriod_Id(1L);
        doNothing().when(entityManager).flush();
        when(jenisSeleksiRepository.findById(1L)).thenReturn(Optional.of(js));
        when(periodJenisSeleksiRepository.save(any())).thenReturn(new PeriodJenisSeleksi());

        RegistrationPeriod result = periodManagementService.updatePeriod(1L, req);

        assertThat(result).isNotNull();
        verify(periodJenisSeleksiRepository).save(any());
    }

    @Test
    @DisplayName("deletePeriod - found deletes period")
    void deletePeriod_found_deletes() {
        RegistrationPeriod period = RegistrationPeriod.builder().id(1L).name("Gel 1").build();
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        doNothing().when(periodJenisSeleksiRepository).deleteByPeriod_Id(1L);
        doNothing().when(entityManager).flush();
        doNothing().when(registrationPeriodRepository).delete(period);

        periodManagementService.deletePeriod(1L);

        verify(registrationPeriodRepository).delete(period);
    }

    @Test
    @DisplayName("getSelectionTypesByPeriod - returns list")
    void getSelectionTypesByPeriod_returnsList() {
        RegistrationPeriod period = RegistrationPeriod.builder().id(1L).build();
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(selectionTypeRepository.findByPeriod_Id(1L)).thenReturn(List.of());

        List<Map<String, Object>> result = periodManagementService.getSelectionTypesByPeriod(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deleteSelectionType - found deletes type")
    void deleteSelectionType_found_deletes() {
        SelectionType type = new SelectionType();
        type.setId(1L);
        type.setName("IPA");
        when(selectionTypeRepository.findById(1L)).thenReturn(Optional.of(type));
        doNothing().when(selectionTypeRepository).delete(type);

        periodManagementService.deleteSelectionType(1L);

        verify(selectionTypeRepository).delete(type);
    }

    @Test
    @DisplayName("createPeriod - with both KEDOKTERAN and NON_KEDOKTERAN found, auto-links")
    void createPeriod_withBothJenisSeleksiFound_autoLinks() {
        RegistrationPeriodRequest req = new RegistrationPeriodRequest();
        req.setName("Gel 2");
        req.setJenisSeleksiIds(null);
        RegistrationPeriod saved = RegistrationPeriod.builder().id(1L).name("Gel 2").build();
        JenisSeleksi kedokteran = JenisSeleksi.builder().id(1L).code("KEDOKTERAN").isActive(true).build();
        JenisSeleksi nonKedokteran = JenisSeleksi.builder().id(2L).code("NON_KEDOKTERAN").isActive(true).build();

        when(registrationPeriodRepository.save(any())).thenReturn(saved);
        when(jenisSeleksiRepository.findByCode("KEDOKTERAN")).thenReturn(Optional.of(kedokteran));
        when(jenisSeleksiRepository.findByCode("NON_KEDOKTERAN")).thenReturn(Optional.of(nonKedokteran));
        when(jenisSeleksiRepository.findById(1L)).thenReturn(Optional.of(kedokteran));
        when(jenisSeleksiRepository.findById(2L)).thenReturn(Optional.of(nonKedokteran));
        when(periodJenisSeleksiRepository.save(any())).thenReturn(new PeriodJenisSeleksi());

        RegistrationPeriod result = periodManagementService.createPeriod(req);

        assertThat(result).isNotNull();
        verify(periodJenisSeleksiRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("createPeriod - when only one of KEDOKTERAN/NON_KEDOKTERAN found, skips auto-link")
    void createPeriod_partialJenisSeleksiFound_skipsAutoLink() {
        RegistrationPeriodRequest req = new RegistrationPeriodRequest();
        req.setName("Gel 3");
        req.setJenisSeleksiIds(null);
        RegistrationPeriod saved = RegistrationPeriod.builder().id(1L).name("Gel 3").build();
        JenisSeleksi kedokteran = JenisSeleksi.builder().id(1L).code("KEDOKTERAN").build();

        when(registrationPeriodRepository.save(any())).thenReturn(saved);
        when(jenisSeleksiRepository.findByCode("KEDOKTERAN")).thenReturn(Optional.of(kedokteran));
        when(jenisSeleksiRepository.findByCode("NON_KEDOKTERAN")).thenReturn(Optional.empty());

        RegistrationPeriod result = periodManagementService.createPeriod(req);

        assertThat(result).isNotNull();
        verify(periodJenisSeleksiRepository, never()).save(any());
    }

    @Test
    @DisplayName("createPeriod - with explicit jenisSeleksiIds links specified")
    void createPeriod_withExplicitIds_linksSpecified() {
        RegistrationPeriodRequest req = new RegistrationPeriodRequest();
        req.setName("Gel 4");
        req.setJenisSeleksiIds(List.of(5L, 6L));
        RegistrationPeriod saved = RegistrationPeriod.builder().id(1L).name("Gel 4").build();
        JenisSeleksi js5 = JenisSeleksi.builder().id(5L).code("SAINS").build();
        JenisSeleksi js6 = JenisSeleksi.builder().id(6L).code("SOSIAL").build();

        when(registrationPeriodRepository.save(any())).thenReturn(saved);
        when(jenisSeleksiRepository.findById(5L)).thenReturn(Optional.of(js5));
        when(jenisSeleksiRepository.findById(6L)).thenReturn(Optional.of(js6));
        when(periodJenisSeleksiRepository.save(any())).thenReturn(new PeriodJenisSeleksi());

        RegistrationPeriod result = periodManagementService.createPeriod(req);

        assertThat(result).isNotNull();
        verify(periodJenisSeleksiRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("linkJenisSeleksi - with explicit IDs saves each")
    void linkJenisSeleksi_withExplicitIds_savesEach() {
        RegistrationPeriod period = RegistrationPeriod.builder().id(1L).name("Gel").build();
        JenisSeleksi js1 = JenisSeleksi.builder().id(10L).code("JS1").build();
        JenisSeleksi js2 = JenisSeleksi.builder().id(20L).code("JS2").build();

        when(jenisSeleksiRepository.findById(10L)).thenReturn(Optional.of(js1));
        when(jenisSeleksiRepository.findById(20L)).thenReturn(Optional.of(js2));
        when(periodJenisSeleksiRepository.save(any())).thenReturn(new PeriodJenisSeleksi());

        periodManagementService.linkJenisSeleksi(period, List.of(10L, 20L));

        verify(periodJenisSeleksiRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("linkJenisSeleksi - when some IDs not found, skips null entries")
    void linkJenisSeleksi_someIdsNotFound_skipsNull() {
        RegistrationPeriod period = RegistrationPeriod.builder().id(1L).name("Gel").build();
        JenisSeleksi js1 = JenisSeleksi.builder().id(10L).code("JS1").build();

        when(jenisSeleksiRepository.findById(10L)).thenReturn(Optional.of(js1));
        when(jenisSeleksiRepository.findById(99L)).thenReturn(Optional.empty());
        when(periodJenisSeleksiRepository.save(any())).thenReturn(new PeriodJenisSeleksi());

        periodManagementService.linkJenisSeleksi(period, List.of(10L, 99L));

        verify(periodJenisSeleksiRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("updatePeriod - with waveType sets correctly")
    void updatePeriod_withWaveType_setsCorrectly() {
        RegistrationPeriodRequest req = new RegistrationPeriodRequest();
        req.setName("Updated");
        req.setWaveType("RANKING_NO_TEST");
        req.setJenisSeleksiIds(null);
        RegistrationPeriod period = RegistrationPeriod.builder().id(1L).name("Old")
                .waveType(RegistrationPeriod.WaveType.REGULAR_TEST).build();

        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(registrationPeriodRepository.save(any())).thenReturn(period);
        doNothing().when(periodJenisSeleksiRepository).deleteByPeriod_Id(1L);
        doNothing().when(entityManager).flush();

        RegistrationPeriod result = periodManagementService.updatePeriod(1L, req);

        assertThat(result.getWaveType()).isEqualTo(RegistrationPeriod.WaveType.RANKING_NO_TEST);
    }

    @Test
    @DisplayName("getJenisSeleksiByPeriod - with data returns mapped list with all fields")
    void getJenisSeleksiByPeriod_withData_returnsMappedList() {
        RegistrationPeriod period = RegistrationPeriod.builder().id(1L).build();
        JenisSeleksi js = JenisSeleksi.builder()
                .id(100L).code("TEST_CODE").nama("Test Nama").deskripsi("Desc").logoUrl("logo.png")
                .harga(new java.math.BigDecimal("100000")).isActive(true).build();
        PeriodJenisSeleksi pjs = PeriodJenisSeleksi.builder()
                .id(50L).period(period).jenisSeleksi(js).isActive(true).build();

        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(periodJenisSeleksiRepository.findByPeriod_IdAndIsActiveTrue(1L)).thenReturn(List.of(pjs));

        List<Map<String, Object>> result = periodManagementService.getJenisSeleksiByPeriod(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0))
                .containsEntry("code", "TEST_CODE")
                .containsEntry("nama", "Test Nama")
                .containsEntry("harga", new BigDecimal("100000"));
    }

    @Test
    @DisplayName("getSelectionTypesByPeriod - with data returns mapped list with all fields")
    void getSelectionTypesByPeriod_withData_returnsMappedList() {
        RegistrationPeriod period = RegistrationPeriod.builder().id(1L).build();
        SelectionType type = SelectionType.builder()
                .id(200L).name("Type Name").description("Desc").formType(SelectionType.FormType.MEDICAL)
                .requireRanking(true).requireTesting(false).price(new BigDecimal("50000")).isActive(true).build();

        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(selectionTypeRepository.findByPeriod_Id(1L)).thenReturn(List.of(type));

        List<Map<String, Object>> result = periodManagementService.getSelectionTypesByPeriod(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0))
                .containsEntry("name", "Type Name")
                .containsEntry("requireRanking", true)
                .containsEntry("requireTesting", false)
                .containsEntry("price", new BigDecimal("50000"));
    }

    @Test
    @DisplayName("createSelectionType - with all fields sets correctly")
    void createSelectionType_withAllFields_setsCorrectly() {
        SelectionTypeRequest req = new SelectionTypeRequest();
        req.setPeriodId(1L);
        req.setName("Science");
        req.setDescription("Science Track");
        req.setFormType(SelectionType.FormType.MEDICAL);
        req.setRequireRanking(true);
        req.setRequireTesting(true);
        req.setPrice(new BigDecimal("75000"));
        RegistrationPeriod period = RegistrationPeriod.builder().id(1L).build();

        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(selectionTypeRepository.save(any())).thenReturn(new SelectionType());

        periodManagementService.createSelectionType(req);

        verify(selectionTypeRepository).save(argThat(st ->
                st.getName().equals("Science") &&
                st.getDescription().equals("Science Track") &&
                st.getRequireRanking() &&
                st.getRequireTesting() &&
                st.getPrice().compareTo(new BigDecimal("75000")) == 0 &&
                st.getIsActive()
        ));
    }
}
