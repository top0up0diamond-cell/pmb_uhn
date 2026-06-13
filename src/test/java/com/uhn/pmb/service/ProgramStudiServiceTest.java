package com.uhn.pmb.service;

import com.uhn.pmb.dto.ProgramStudiRequest;
import com.uhn.pmb.entity.ProgramStudi;
import com.uhn.pmb.repository.ProgramStudiRepository;
import com.uhn.pmb.repository.SelectionProgramStudiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgramStudiServiceTest {

    @Mock private ProgramStudiRepository programStudiRepository;
    @Mock private SelectionProgramStudiRepository selectionProgramStudiRepository;

    private ProgramStudiService programStudiService;

    @BeforeEach
    void setUp() {
        programStudiService = new ProgramStudiService(
                programStudiRepository,
                selectionProgramStudiRepository
        );
    }

    private ProgramStudi buildPs(Long id, String kode) {
        return ProgramStudi.builder()
                .id(id).kode(kode).nama("Informatika")
                .deskripsi("desc").isMedical(false).isActive(true)
                .sortOrder(1).hargaTotalPerTahun(5000000L)
                .cicilan1(2500000L).cicilan2(2500000L)
                .build();
    }

    private ProgramStudiRequest buildRequest(String kode) {
        ProgramStudiRequest req = new ProgramStudiRequest();
        req.setKode(kode);
        req.setNama("Informatika");
        req.setDeskripsi("desc");
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

    // ===== findAll =====

    @Test
    @DisplayName("findAll - returns ordered list")
    void findAll_returnsList() {
        when(programStudiRepository.findAllByOrderBySortOrder())
                .thenReturn(List.of(buildPs(1L, "informatika")));

        List<ProgramStudi> result = programStudiService.findAll();

        assertThat(result).hasSize(1);
    }

    // ===== findAllActive =====

    @Test
    @DisplayName("findAllActive - returns active list")
    void findAllActive_returnsList() {
        when(programStudiRepository.findByIsActiveTrueOrderBySortOrder())
                .thenReturn(List.of(buildPs(1L, "informatika")));

        List<ProgramStudi> result = programStudiService.findAllActive();

        assertThat(result).hasSize(1);
    }

    // ===== findById =====

    @Test
    @DisplayName("findById - found returns Optional with entity")
    void findById_found_returnsOptional() {
        when(programStudiRepository.findById(1L))
                .thenReturn(Optional.of(buildPs(1L, "informatika")));

        Optional<ProgramStudi> result = programStudiService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findById - not found returns empty Optional")
    void findById_notFound_returnsEmpty() {
        when(programStudiRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<ProgramStudi> result = programStudiService.findById(99L);

        assertThat(result).isEmpty();
    }

    // ===== create =====

    @Test
    @DisplayName("create - duplicate kode throws RuntimeException")
    void create_duplicateKode_throws() {
        when(programStudiRepository.existsByKode("informatika")).thenReturn(true);

        assertThatThrownBy(() -> programStudiService.create(buildRequest("informatika")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Kode program studi sudah digunakan");

        verify(programStudiRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - happy path saves and returns")
    void create_happyPath_saves() {
        when(programStudiRepository.existsByKode("informatika")).thenReturn(false);
        ProgramStudi saved = buildPs(1L, "informatika");
        when(programStudiRepository.save(any())).thenReturn(saved);

        ProgramStudi result = programStudiService.create(buildRequest("informatika"));

        assertThat(result).isNotNull();
        verify(programStudiRepository).save(any());
    }

    @Test
    @DisplayName("create - null optional fields default to safe values")
    void create_nullOptionalFields_defaults() {
        ProgramStudiRequest req = new ProgramStudiRequest();
        req.setKode("teknik-sipil");
        req.setNama("Teknik Sipil");
        req.setDeskripsi("desc");
        // isMedical, sortOrder, hargaTotalPerTahun, cicilan semua null

        when(programStudiRepository.existsByKode("teknik-sipil")).thenReturn(false);
        when(programStudiRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProgramStudi result = programStudiService.create(req);

        assertThat(result.getIsMedical()).isFalse();
        assertThat(result.getSortOrder()).isEqualTo(0);
        assertThat(result.getHargaTotalPerTahun()).isEqualTo(0L);
        assertThat(result.getCicilan1()).isEqualTo(0L);
        assertThat(result.getCicilan6()).isEqualTo(0L);
    }

    // ===== update =====

    @Test
    @DisplayName("update - not found throws RuntimeException")
    void update_notFound_throws() {
        when(programStudiRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> programStudiService.update(99L, buildRequest("informatika")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("update - kode changed and already used throws RuntimeException")
    void update_kodeChangedAndUsed_throws() {
        ProgramStudi existing = buildPs(1L, "old-kode");
        when(programStudiRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(programStudiRepository.existsByKode("informatika")).thenReturn(true);

        assertThatThrownBy(() -> programStudiService.update(1L, buildRequest("informatika")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Kode program studi sudah digunakan");

        verify(programStudiRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - same kode updates without checking existence")
    void update_sameKode_updatesWithoutCheck() {
        ProgramStudi existing = buildPs(1L, "informatika");
        when(programStudiRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(programStudiRepository.save(any())).thenReturn(existing);

        ProgramStudi result = programStudiService.update(1L, buildRequest("informatika"));

        assertThat(result.getNama()).isEqualTo("Informatika");
        verify(programStudiRepository, never()).existsByKode(any());
        verify(programStudiRepository).save(existing);
    }

    @Test
    @DisplayName("update - kode changed and not used updates successfully")
    void update_kodeChangedNotUsed_updates() {
        ProgramStudi existing = buildPs(1L, "old-kode");
        when(programStudiRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(programStudiRepository.existsByKode("informatika")).thenReturn(false);
        when(programStudiRepository.save(any())).thenReturn(existing);

        ProgramStudi result = programStudiService.update(1L, buildRequest("informatika"));

        assertThat(result.getKode()).isEqualTo("informatika");
        verify(programStudiRepository).save(existing);
    }

    @Test
    @DisplayName("update - null optional fields default to safe values")
    void update_nullOptionalFields_defaults() {
        ProgramStudi existing = buildPs(1L, "informatika");
        when(programStudiRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(programStudiRepository.save(any())).thenReturn(existing);

        ProgramStudiRequest req = new ProgramStudiRequest();
        req.setKode("informatika");
        req.setNama("Informatika Baru");
        req.setDeskripsi("desc");
        // isActive, isMedical, sortOrder, harga, cicilan semua null

        ProgramStudi result = programStudiService.update(1L, req);

        assertThat(result.getIsMedical()).isFalse();
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getSortOrder()).isEqualTo(0);
        assertThat(result.getHargaTotalPerTahun()).isEqualTo(0L);
    }

    // ===== delete =====

    @Test
    @DisplayName("delete - not found throws RuntimeException")
    void delete_notFound_throws() {
        when(programStudiRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> programStudiService.delete(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("delete - still used throws RuntimeException")
    void delete_stillUsed_throws() {
        ProgramStudi existing = buildPs(1L, "informatika");
        when(programStudiRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(selectionProgramStudiRepository.existsByProgramStudi_Id(1L)).thenReturn(true);

        assertThatThrownBy(() -> programStudiService.delete(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("masih digunakan");

        verify(programStudiRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete - happy path deletes entity")
    void delete_happyPath_deletes() {
        ProgramStudi existing = buildPs(1L, "informatika");
        when(programStudiRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(selectionProgramStudiRepository.existsByProgramStudi_Id(1L)).thenReturn(false);
        doNothing().when(programStudiRepository).delete(existing);

        programStudiService.delete(1L);

        verify(programStudiRepository).delete(existing);
    }
}