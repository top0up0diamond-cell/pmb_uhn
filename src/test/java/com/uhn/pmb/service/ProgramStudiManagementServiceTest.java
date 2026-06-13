package com.uhn.pmb.service;

import com.uhn.pmb.dto.ProgramStudiRequest;
import com.uhn.pmb.entity.JenisSeleksi;
import com.uhn.pmb.entity.ProgramStudi;
import com.uhn.pmb.entity.SelectionProgramStudi;
import com.uhn.pmb.repository.JenisSeleksiRepository;
import com.uhn.pmb.repository.ProgramStudiRepository;
import com.uhn.pmb.repository.SelectionProgramStudiRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgramStudiManagementServiceTest {

    @Mock private ProgramStudiRepository programStudiRepository;
    @Mock private SelectionProgramStudiRepository selectionProgramStudiRepository;
    @Mock private JenisSeleksiRepository jenisSeleksiRepository;
    @BeforeEach
    void setUp() {
        programStudiManagementService = new ProgramStudiManagementService(
            programStudiRepository,
            selectionProgramStudiRepository,
            jenisSeleksiRepository
        );
    }
    @InjectMocks
    private ProgramStudiManagementService programStudiManagementService;

    private ProgramStudiRequest buildRequest() {
        ProgramStudiRequest req = new ProgramStudiRequest();
        req.setKode("informatika");
        req.setNama("Informatika");
        req.setDeskripsi("Program studi informatika");
        req.setIsMedical(false);
        req.setIsActive(true);
        req.setSortOrder(1);
        req.setHargaTotalPerTahun(5000000L);
        req.setCicilan1(2500000L);
        req.setCicilan2(2500000L);
        req.setCicilan3(0L);
        req.setCicilan4(0L);
        req.setCicilan5(0L);
        req.setCicilan6(0L);
        return req;
    }

    private ProgramStudi buildProgramStudi(Long id, String kode) {
        return ProgramStudi.builder()
                .id(id)
                .kode(kode)
                .nama("Informatika")
                .deskripsi("desc")
                .isMedical(false)
                .isActive(true)
                .sortOrder(1)
                .hargaTotalPerTahun(5000000L)
                .cicilan1(2500000L)
                .cicilan2(2500000L)
                .build();
    }

    // ===== createProgramStudi =====

    @Test
    @DisplayName("createProgramStudi - jenis seleksi not found throws exception")
    void createProgramStudi_jenisSeleksiNotFound_throws() {
        when(jenisSeleksiRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> programStudiManagementService.createProgramStudi(buildRequest(), 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Jenis Seleksi tidak ditemukan");
    }

    @Test
    @DisplayName("createProgramStudi - happy path saves program studi")
    void createProgramStudi_happyPath_saves() {
        JenisSeleksi jenisSeleksi = new JenisSeleksi();
        jenisSeleksi.setId(1L);
        when(jenisSeleksiRepository.findById(1L)).thenReturn(Optional.of(jenisSeleksi));
        ProgramStudi saved = buildProgramStudi(1L, "informatika");
        when(programStudiRepository.save(any())).thenReturn(saved);

        ProgramStudi result = programStudiManagementService.createProgramStudi(buildRequest(), 1L);

        assertThat(result).isNotNull();
        assertThat(result.getKode()).isEqualTo("informatika");
        verify(programStudiRepository).save(any());
    }

    // ===== createProgramStudiFull =====

    @Test
    @DisplayName("createProgramStudiFull - duplicate kode throws IllegalArgumentException")
    void createProgramStudiFull_duplicateKode_throws() {
        when(programStudiRepository.existsByKode("informatika")).thenReturn(true);

        assertThatThrownBy(() -> programStudiManagementService.createProgramStudiFull(buildRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Kode program studi sudah digunakan");

        verify(programStudiRepository, never()).save(any());
    }

    @Test
    @DisplayName("createProgramStudiFull - happy path saves program studi")
    void createProgramStudiFull_happyPath_saves() {
        when(programStudiRepository.existsByKode("informatika")).thenReturn(false);
        ProgramStudi saved = buildProgramStudi(1L, "informatika");
        when(programStudiRepository.save(any())).thenReturn(saved);

        ProgramStudi result = programStudiManagementService.createProgramStudiFull(buildRequest());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(programStudiRepository).save(any());
    }

    // ===== updateProgramStudiById =====

    @Test
    @DisplayName("updateProgramStudiById - not found throws RuntimeException")
    void updateProgramStudiById_notFound_throws() {
        when(programStudiRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> programStudiManagementService.updateProgramStudiById(99L, buildRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Program Studi tidak ditemukan");
    }

    @Test
    @DisplayName("updateProgramStudiById - kode changed and already used throws IllegalArgumentException")
    void updateProgramStudiById_kodeChangedAndUsed_throws() {
        ProgramStudi existing = buildProgramStudi(1L, "old-kode");
        when(programStudiRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(programStudiRepository.existsByKode("informatika")).thenReturn(true);

        assertThatThrownBy(() -> programStudiManagementService.updateProgramStudiById(1L, buildRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Kode program studi sudah digunakan");

        verify(programStudiRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProgramStudiById - same kode updates successfully")
    void updateProgramStudiById_sameKode_updates() {
        ProgramStudi existing = buildProgramStudi(1L, "informatika");
        when(programStudiRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(programStudiRepository.save(any())).thenReturn(existing);

        ProgramStudi result = programStudiManagementService.updateProgramStudiById(1L, buildRequest());

        assertThat(result.getNama()).isEqualTo("Informatika");
        verify(programStudiRepository, never()).existsByKode(any());
        verify(programStudiRepository).save(existing);
    }

    @Test
    @DisplayName("updateProgramStudiById - kode changed and not used updates successfully")
    void updateProgramStudiById_kodeChangedNotUsed_updates() {
        ProgramStudi existing = buildProgramStudi(1L, "old-kode");
        when(programStudiRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(programStudiRepository.existsByKode("informatika")).thenReturn(false);
        when(programStudiRepository.save(any())).thenReturn(existing);

        ProgramStudi result = programStudiManagementService.updateProgramStudiById(1L, buildRequest());

        assertThat(result.getKode()).isEqualTo("informatika");
        verify(programStudiRepository).save(existing);
    }

    @Test
    @DisplayName("updateProgramStudiById - null optional fields default correctly")
    void updateProgramStudiById_nullFields_defaultsApplied() {
        ProgramStudi existing = buildProgramStudi(1L, "informatika");
        when(programStudiRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(programStudiRepository.save(any())).thenReturn(existing);

        ProgramStudiRequest req = new ProgramStudiRequest();
        req.setKode("informatika");
        req.setNama("Informatika Baru");
        req.setDeskripsi("desc baru");
        // isMedical, isActive, sortOrder, hargaTotalPerTahun, cicilanN all null

        ProgramStudi result = programStudiManagementService.updateProgramStudiById(1L, req);

        assertThat(result.getIsMedical()).isFalse();
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getSortOrder()).isEqualTo(0);
        assertThat(result.getHargaTotalPerTahun()).isEqualTo(0L);
        assertThat(result.getCicilan1()).isEqualTo(0L);
        assertThat(result.getCicilan6()).isEqualTo(0L);
    }

    // ===== deleteProgramStudiById =====

    @Test
    @DisplayName("deleteProgramStudiById - not found throws RuntimeException")
    void deleteProgramStudiById_notFound_throws() {
        when(programStudiRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> programStudiManagementService.deleteProgramStudiById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Program Studi tidak ditemukan");
    }

    @Test
    @DisplayName("deleteProgramStudiById - still used throws IllegalStateException")
    void deleteProgramStudiById_stillUsed_throws() {
        ProgramStudi existing = buildProgramStudi(1L, "informatika");
        when(programStudiRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(selectionProgramStudiRepository.existsByProgramStudi_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> programStudiManagementService.deleteProgramStudiById(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("masih digunakan");

        verify(programStudiRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteProgramStudiById - happy path deletes")
    void deleteProgramStudiById_happyPath_deletes() {
        ProgramStudi existing = buildProgramStudi(1L, "informatika");
        when(programStudiRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(selectionProgramStudiRepository.existsByProgramStudi_Id(1L)).thenReturn(false);
        doNothing().when(programStudiRepository).delete(existing);

        programStudiManagementService.deleteProgramStudiById(1L);

        verify(programStudiRepository).delete(existing);
    }

    // ===== linkToJenisSeleksi =====

    @Test
    @DisplayName("linkToJenisSeleksi - null list does nothing")
    void linkToJenisSeleksi_nullList_doesNothing() {
        ProgramStudi ps = buildProgramStudi(1L, "informatika");

        programStudiManagementService.linkToJenisSeleksi(ps, null);

        verify(selectionProgramStudiRepository, never()).save(any());
    }

    @Test
    @DisplayName("linkToJenisSeleksi - empty list does nothing")
    void linkToJenisSeleksi_emptyList_doesNothing() {
        ProgramStudi ps = buildProgramStudi(1L, "informatika");

        programStudiManagementService.linkToJenisSeleksi(ps, List.of());

        verify(selectionProgramStudiRepository, never()).save(any());
    }

    @Test
    @DisplayName("linkToJenisSeleksi - valid ids saves links")
    void linkToJenisSeleksi_validIds_savesLinks() {
        ProgramStudi ps = buildProgramStudi(1L, "informatika");
        JenisSeleksi js1 = new JenisSeleksi();
        js1.setId(1L);
        when(jenisSeleksiRepository.findById(1L)).thenReturn(Optional.of(js1));
        when(jenisSeleksiRepository.findById(2L)).thenReturn(Optional.empty());

        programStudiManagementService.linkToJenisSeleksi(ps, List.of(1L, 2L));

        verify(selectionProgramStudiRepository, times(1)).save(any());
    }

    // ===== unlinkFromJenisSeleksi =====

    @Test
    @DisplayName("unlinkFromJenisSeleksi - existing link is deleted")
    void unlinkFromJenisSeleksi_existing_deletes() {
        ProgramStudi ps = buildProgramStudi(1L, "informatika");
        SelectionProgramStudi sps = SelectionProgramStudi.builder().id(1L).build();
        when(selectionProgramStudiRepository.findByJenisSeleksi_IdAndProgramStudi_Id(1L, 1L))
                .thenReturn(Optional.of(sps));

        programStudiManagementService.unlinkFromJenisSeleksi(ps, 1L);

        verify(selectionProgramStudiRepository).delete(sps);
    }

    @Test
    @DisplayName("unlinkFromJenisSeleksi - no existing link does nothing")
    void unlinkFromJenisSeleksi_notExisting_doesNothing() {
        ProgramStudi ps = buildProgramStudi(1L, "informatika");
        when(selectionProgramStudiRepository.findByJenisSeleksi_IdAndProgramStudi_Id(1L, 1L))
                .thenReturn(Optional.empty());

        programStudiManagementService.unlinkFromJenisSeleksi(ps, 1L);

        verify(selectionProgramStudiRepository, never()).delete(any());
    }

    // ===== updateProgramStudi =====

    @Test
    @DisplayName("updateProgramStudi - updates fields and saves")
    void updateProgramStudi_updatesAndSaves() {
        ProgramStudi existing = buildProgramStudi(1L, "old-kode");
        when(programStudiRepository.save(any())).thenReturn(existing);

        ProgramStudi result = programStudiManagementService.updateProgramStudi(existing, buildRequest());

        assertThat(result.getKode()).isEqualTo("informatika");
        assertThat(result.getNama()).isEqualTo("Informatika");
        verify(programStudiRepository).save(existing);
    }

    // ===== getProgramStudiById =====

    @Test
    @DisplayName("getProgramStudiById - found returns entity")
    void getProgramStudiById_found_returnsEntity() {
        ProgramStudi ps = buildProgramStudi(1L, "informatika");
        when(programStudiRepository.findById(1L)).thenReturn(Optional.of(ps));

        ProgramStudi result = programStudiManagementService.getProgramStudiById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getProgramStudiById - not found throws RuntimeException")
    void getProgramStudiById_notFound_throws() {
        when(programStudiRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> programStudiManagementService.getProgramStudiById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Program Studi tidak ditemukan");
    }

    // ===== getAllProgramStudi =====

    @Test
    @DisplayName("getAllProgramStudi - returns list from repository")
    void getAllProgramStudi_returnsList() {
        when(programStudiRepository.findAll()).thenReturn(List.of(buildProgramStudi(1L, "informatika")));

        List<ProgramStudi> result = programStudiManagementService.getAllProgramStudi();

        assertThat(result).hasSize(1);
    }

    // ===== getAllProgramStudiWithDetails =====

    @Test
    @DisplayName("getAllProgramStudiWithDetails - maps fields correctly")
    void getAllProgramStudiWithDetails_mapsFields() {
        ProgramStudi ps = buildProgramStudi(1L, "informatika");
        when(programStudiRepository.findAllByOrderBySortOrder()).thenReturn(List.of(ps));

        List<Map<String, Object>> result = programStudiManagementService.getAllProgramStudiWithDetails();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("kode")).isEqualTo("informatika");
        assertThat(result.get(0).get("cicilan3")).isEqualTo(0L);
    }

    @Test
    @DisplayName("getAllProgramStudiWithDetails - handles null optional fields")
    void getAllProgramStudiWithDetails_handlesNullFields() {
        ProgramStudi ps = ProgramStudi.builder()
                .id(2L)
                .kode("teknik-sipil")
                .nama("Teknik Sipil")
                .isActive(true)
                .sortOrder(2)
                .build();
        when(programStudiRepository.findAllByOrderBySortOrder()).thenReturn(List.of(ps));

        List<Map<String, Object>> result = programStudiManagementService.getAllProgramStudiWithDetails();

        assertThat(result.get(0).get("deskripsi")).isEqualTo("");
        assertThat(result.get(0).get("hargaTotalPerTahun")).isEqualTo(0L);
        assertThat(result.get(0).get("cicilan1")).isEqualTo(0L);
    }

    // ===== getActiveProgramStudiSimple =====

    @Test
    @DisplayName("getActiveProgramStudiSimple - returns simplified map")
    void getActiveProgramStudiSimple_returnsSimplifiedMap() {
        ProgramStudi ps = buildProgramStudi(1L, "informatika");
        when(programStudiRepository.findByIsActiveTrueOrderBySortOrder()).thenReturn(List.of(ps));

        List<Map<String, Object>> result = programStudiManagementService.getActiveProgramStudiSimple();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("nama")).isEqualTo("Informatika");
        assertThat(result.get(0)).doesNotContainKey("hargaTotalPerTahun");
    }

    @Test
    @DisplayName("getActiveProgramStudiSimple - handles null deskripsi")
    void getActiveProgramStudiSimple_handlesNullDeskripsi() {
        ProgramStudi ps = ProgramStudi.builder()
                .id(1L)
                .kode("informatika")
                .nama("Informatika")
                .isMedical(false)
                .build();
        when(programStudiRepository.findByIsActiveTrueOrderBySortOrder()).thenReturn(List.of(ps));

        List<Map<String, Object>> result = programStudiManagementService.getActiveProgramStudiSimple();

        assertThat(result.get(0).get("deskripsi")).isEqualTo("");
    }

    // ===== getActiveProgramStudi =====

    @Test
    @DisplayName("getActiveProgramStudi - returns list from repository")
    void getActiveProgramStudi_returnsList() {
        when(programStudiRepository.findByIsActiveTrueOrderBySortOrder())
                .thenReturn(List.of(buildProgramStudi(1L, "informatika")));

        List<ProgramStudi> result = programStudiManagementService.getActiveProgramStudi();

        assertThat(result).hasSize(1);
    }

    // ===== getProgramStudiByJenisSeleksi =====

    @Test
    @DisplayName("getProgramStudiByJenisSeleksi - jenis seleksi not found throws")
    void getProgramStudiByJenisSeleksi_notFound_throws() {
        when(jenisSeleksiRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> programStudiManagementService.getProgramStudiByJenisSeleksi(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Jenis Seleksi tidak ditemukan");
    }

    @Test
    @DisplayName("getProgramStudiByJenisSeleksi - returns mapped list")
    void getProgramStudiByJenisSeleksi_returnsMappedList() {
        JenisSeleksi js = new JenisSeleksi();
        js.setId(1L);
        when(jenisSeleksiRepository.findById(1L)).thenReturn(Optional.of(js));

        ProgramStudi ps = buildProgramStudi(2L, "informatika");
        SelectionProgramStudi sps = SelectionProgramStudi.builder()
                .id(5L)
                .programStudi(ps)
                .jenisSeleksi(js)
                .isActive(true)
                .build();
        when(selectionProgramStudiRepository.findByJenisSeleksi_IdAndIsActiveTrue(1L)).thenReturn(List.of(sps));

        List<Map<String, Object>> result = programStudiManagementService.getProgramStudiByJenisSeleksi(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("programStudiId")).isEqualTo(2L);
        assertThat(result.get(0).get("kode")).isEqualTo("informatika");
    }

    // ===== bulkInitializeProgramStudi =====

    @Test
    @DisplayName("bulkInitializeProgramStudi - all new inserts all programs")
    void bulkInitializeProgramStudi_allNew_insertsAll() {
        when(programStudiRepository.existsByKode(any())).thenReturn(false);
        when(programStudiRepository.save(any())).thenReturn(buildProgramStudi(1L, "x"));

        Map<String, Object> result = programStudiManagementService.bulkInitializeProgramStudi();

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("inserted")).isEqualTo(18);
        assertThat(result.get("total")).isEqualTo(18);
        verify(programStudiRepository, times(18)).save(any());
    }

    @Test
    @DisplayName("bulkInitializeProgramStudi - all existing skips all")
    void bulkInitializeProgramStudi_allExisting_skipsAll() {
        when(programStudiRepository.existsByKode(any())).thenReturn(true);

        Map<String, Object> result = programStudiManagementService.bulkInitializeProgramStudi();

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("inserted")).isEqualTo(0);
        assertThat(result.get("total")).isEqualTo(18);
        verify(programStudiRepository, never()).save(any());
    }

    @Test
    @DisplayName("bulkInitializeProgramStudi - mixed existing and new")
    void bulkInitializeProgramStudi_mixed_insertsOnlyNew() {
        when(programStudiRepository.existsByKode("pendidikan-dokter")).thenReturn(true);
        when(programStudiRepository.existsByKode("profesi-dokter")).thenReturn(true);
        when(programStudiRepository.existsByKode(argThat(k -> !k.equals("pendidikan-dokter") && !k.equals("profesi-dokter"))))
                .thenReturn(false);
        when(programStudiRepository.save(any())).thenReturn(buildProgramStudi(1L, "x"));

        Map<String, Object> result = programStudiManagementService.bulkInitializeProgramStudi();

        assertThat(result.get("inserted")).isEqualTo(16);
        verify(programStudiRepository, times(16)).save(any());
    }
}