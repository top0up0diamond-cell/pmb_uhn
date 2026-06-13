package com.uhn.pmb.service;

import com.uhn.pmb.dto.JenisSeleksiRequest;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JenisSeleksiServiceTest {

    @Mock private JenisSeleksiRepository jenisSeleksiRepository;
    @Mock private PeriodJenisSeleksiRepository periodJenisSeleksiRepository;
    @Mock private SelectionProgramStudiRepository selectionProgramStudiRepository;
    @Mock private ProgramStudiRepository programStudiRepository;
    @Mock private EntityManager entityManager;

    @InjectMocks
    private JenisSeleksiService jenisSeleksiService;

    // ===== Helpers =====

    private JenisSeleksi buildJenisSeleksi(Long id, String code) {
        return JenisSeleksi.builder()
                .id(id)
                .code(code)
                .nama("Seleksi " + code)
                .isActive(true)
                .sortOrder(0)
                .build();
    }

    private JenisSeleksiRequest buildRequest(String code, List<Long> programStudiIds) {
        JenisSeleksiRequest req = new JenisSeleksiRequest();
        req.setCode(code);
        req.setNama("Seleksi " + code);
        req.setDeskripsi("Deskripsi " + code);
        req.setFasilitas("Fasilitas");
        req.setLogoUrl("http://logo.test");
        req.setHarga(new java.math.BigDecimal(500000));
        req.setSortOrder(1);
        req.setIsActive(true);
        req.setProgramStudiIds(programStudiIds);
        return req;
    }

    private ProgramStudi buildProgramStudi(Long id, String nama) {
        ProgramStudi ps = new ProgramStudi();
        ps.setId(id);
        ps.setNama(nama);
        return ps;
    }

    // ===== getAllActive =====

    @Test
    @DisplayName("getAllActive - returns only active jenis seleksi ordered by sortOrder")
    void getAllActive_returnsActiveList() {
        JenisSeleksi js = buildJenisSeleksi(1L, "REGULER");
        when(jenisSeleksiRepository.findByIsActiveTrueOrderBySortOrder()).thenReturn(List.of(js));

        List<JenisSeleksi> result = jenisSeleksiService.getAllActive();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("REGULER");
    }

    @Test
    @DisplayName("getAllActive - returns empty list when none active")
    void getAllActive_noneActive_returnsEmpty() {
        when(jenisSeleksiRepository.findByIsActiveTrueOrderBySortOrder()).thenReturn(List.of());

        List<JenisSeleksi> result = jenisSeleksiService.getAllActive();

        assertThat(result).isEmpty();
    }

    // ===== getAll =====

    @Test
    @DisplayName("getAll - returns all jenis seleksi ordered by sortOrder")
    void getAll_returnsAllList() {
        JenisSeleksi js1 = buildJenisSeleksi(1L, "REGULER");
        JenisSeleksi js2 = buildJenisSeleksi(2L, "MEDICAL");
        when(jenisSeleksiRepository.findAllByOrderBySortOrder()).thenReturn(List.of(js1, js2));

        List<JenisSeleksi> result = jenisSeleksiService.getAll();

        assertThat(result).hasSize(2);
    }

    // ===== getById =====

    @Test
    @DisplayName("getById - found returns Optional with entity")
    void getById_found_returnsOptional() {
        JenisSeleksi js = buildJenisSeleksi(1L, "REGULER");
        when(jenisSeleksiRepository.findById(1L)).thenReturn(Optional.of(js));

        Optional<JenisSeleksi> result = jenisSeleksiService.getById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getById - not found returns empty Optional")
    void getById_notFound_returnsEmpty() {
        when(jenisSeleksiRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<JenisSeleksi> result = jenisSeleksiService.getById(99L);

        assertThat(result).isEmpty();
    }

    // ===== getByCode =====

    @Test
    @DisplayName("getByCode - found returns Optional with entity")
    void getByCode_found_returnsOptional() {
        JenisSeleksi js = buildJenisSeleksi(1L, "REGULER");
        when(jenisSeleksiRepository.findByCode("REGULER")).thenReturn(Optional.of(js));

        Optional<JenisSeleksi> result = jenisSeleksiService.getByCode("REGULER");

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("REGULER");
    }

    @Test
    @DisplayName("getByCode - not found returns empty Optional")
    void getByCode_notFound_returnsEmpty() {
        when(jenisSeleksiRepository.findByCode("UNKNOWN")).thenReturn(Optional.empty());

        Optional<JenisSeleksi> result = jenisSeleksiService.getByCode("UNKNOWN");

        assertThat(result).isEmpty();
    }

    // ===== create =====

    @Test
    @DisplayName("create - duplicate code throws IllegalArgumentException")
    void create_duplicateCode_throwsException() {
        JenisSeleksi js = buildJenisSeleksi(null, "REGULER");
        when(jenisSeleksiRepository.existsByCode("REGULER")).thenReturn(true);

        assertThatThrownBy(() -> jenisSeleksiService.create(js))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Code sudah digunakan");
    }

    @Test
    @DisplayName("create - new code saves and returns entity")
    void create_newCode_savesAndReturns() {
        JenisSeleksi js = buildJenisSeleksi(null, "REGULER");
        js.setSortOrder(null);
        js.setIsActive(null);
        when(jenisSeleksiRepository.existsByCode("REGULER")).thenReturn(false);
        when(jenisSeleksiRepository.findAll()).thenReturn(List.of());
        when(jenisSeleksiRepository.save(any())).thenReturn(buildJenisSeleksi(1L, "REGULER"));

        JenisSeleksi result = jenisSeleksiService.create(js);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(jenisSeleksiRepository).save(any());
    }

    @Test
    @DisplayName("create - sortOrder null defaults to list size")
    void create_sortOrderNull_defaultsToListSize() {
        JenisSeleksi js = buildJenisSeleksi(null, "BARU");
        js.setSortOrder(null);
        js.setIsActive(true);
        when(jenisSeleksiRepository.existsByCode("BARU")).thenReturn(false);
        when(jenisSeleksiRepository.findAll()).thenReturn(List.of(
                buildJenisSeleksi(1L, "A"),
                buildJenisSeleksi(2L, "B")
        ));
        when(jenisSeleksiRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        JenisSeleksi result = jenisSeleksiService.create(js);

        assertThat(result.getSortOrder()).isEqualTo(2);
    }

    @Test
    @DisplayName("create - isActive null defaults to true")
    void create_isActiveNull_defaultsToTrue() {
        JenisSeleksi js = buildJenisSeleksi(null, "BARU");
        js.setSortOrder(1);
        js.setIsActive(null);
        when(jenisSeleksiRepository.existsByCode("BARU")).thenReturn(false);
        when(jenisSeleksiRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        JenisSeleksi result = jenisSeleksiService.create(js);

        assertThat(result.getIsActive()).isTrue();
    }

    // ===== createWithProgramStudi =====

    @Test
    @DisplayName("createWithProgramStudi - with explicit programStudiIds links them")
    void createWithProgramStudi_explicitIds_linksAll() {
        JenisSeleksiRequest req = buildRequest("REGULER", List.of(1L, 2L));
        JenisSeleksi saved = buildJenisSeleksi(1L, "REGULER");

        when(jenisSeleksiRepository.existsByCode("REGULER")).thenReturn(false);
        when(jenisSeleksiRepository.save(any())).thenReturn(saved);
        when(programStudiRepository.findById(1L)).thenReturn(Optional.of(buildProgramStudi(1L, "Teknik Informatika")));
        when(programStudiRepository.findById(2L)).thenReturn(Optional.of(buildProgramStudi(2L, "Sistem Informasi")));

        JenisSeleksi result = jenisSeleksiService.createWithProgramStudi(req);

        assertThat(result).isNotNull();
        verify(selectionProgramStudiRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("createWithProgramStudi - null programStudiIds auto-links non-dokter programs for non-kedokteran code")
    void createWithProgramStudi_nullIds_nonKedokteran_autoLinksNonDokter() {
        JenisSeleksiRequest req = buildRequest("REGULER", null);
        JenisSeleksi saved = buildJenisSeleksi(1L, "REGULER");

        ProgramStudi nonDokter = buildProgramStudi(1L, "Teknik Informatika");
        ProgramStudi dokter = buildProgramStudi(2L, "Pendidikan Dokter");

        when(jenisSeleksiRepository.existsByCode("REGULER")).thenReturn(false);
        when(jenisSeleksiRepository.save(any())).thenReturn(saved);
        when(programStudiRepository.findAll()).thenReturn(List.of(nonDokter, dokter));
        when(programStudiRepository.findById(1L)).thenReturn(Optional.of(nonDokter));

        JenisSeleksi result = jenisSeleksiService.createWithProgramStudi(req);

        assertThat(result).isNotNull();
        // Only the non-dokter prodi should be linked
        verify(selectionProgramStudiRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("createWithProgramStudi - null programStudiIds auto-links dokter programs for KEDOKTERAN code")
    void createWithProgramStudi_nullIds_kedokteranCode_autoLinksDokter() {
        JenisSeleksiRequest req = buildRequest("KEDOKTERAN", null);
        JenisSeleksi saved = buildJenisSeleksi(2L, "KEDOKTERAN");

        ProgramStudi nonDokter = buildProgramStudi(1L, "Teknik Informatika");
        ProgramStudi dokter = buildProgramStudi(2L, "Pendidikan Dokter");

        when(jenisSeleksiRepository.existsByCode("KEDOKTERAN")).thenReturn(false);
        when(jenisSeleksiRepository.save(any())).thenReturn(saved);
        when(programStudiRepository.findAll()).thenReturn(List.of(nonDokter, dokter));
        when(programStudiRepository.findById(2L)).thenReturn(Optional.of(dokter));

        JenisSeleksi result = jenisSeleksiService.createWithProgramStudi(req);

        assertThat(result).isNotNull();
        // Only the dokter prodi should be linked
        verify(selectionProgramStudiRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("createWithProgramStudi - empty programStudiIds auto-links (same as null)")
    void createWithProgramStudi_emptyIds_autoLinks() {
        JenisSeleksiRequest req = buildRequest("REGULER", List.of());
        JenisSeleksi saved = buildJenisSeleksi(1L, "REGULER");

        when(jenisSeleksiRepository.existsByCode("REGULER")).thenReturn(false);
        when(jenisSeleksiRepository.save(any())).thenReturn(saved);
        when(programStudiRepository.findAll()).thenReturn(List.of());

        JenisSeleksi result = jenisSeleksiService.createWithProgramStudi(req);

        assertThat(result).isNotNull();
        verify(selectionProgramStudiRepository, never()).save(any());
    }

    @Test
    @DisplayName("createWithProgramStudi - programStudi not found by id is skipped")
    void createWithProgramStudi_programStudiNotFound_skipped() {
        JenisSeleksiRequest req = buildRequest("REGULER", List.of(99L));
        JenisSeleksi saved = buildJenisSeleksi(1L, "REGULER");

        when(jenisSeleksiRepository.existsByCode("REGULER")).thenReturn(false);
        when(jenisSeleksiRepository.save(any())).thenReturn(saved);
        when(programStudiRepository.findById(99L)).thenReturn(Optional.empty());

        JenisSeleksi result = jenisSeleksiService.createWithProgramStudi(req);

        assertThat(result).isNotNull();
        verify(selectionProgramStudiRepository, never()).save(any());
    }

    @Test
    @DisplayName("createWithProgramStudi - code containing DOKTER triggers kedokteran branch")
    void createWithProgramStudi_codeContainsDokter_triggersKedokteranBranch() {
        JenisSeleksiRequest req = buildRequest("JALUR_DOKTER_SPESIALIS", null);
        JenisSeleksi saved = buildJenisSeleksi(3L, "JALUR_DOKTER_SPESIALIS");

        ProgramStudi dokter = buildProgramStudi(1L, "Pendidikan Dokter Spesialis");
        ProgramStudi non = buildProgramStudi(2L, "Farmasi");

        when(jenisSeleksiRepository.existsByCode("JALUR_DOKTER_SPESIALIS")).thenReturn(false);
        when(jenisSeleksiRepository.save(any())).thenReturn(saved);
        when(programStudiRepository.findAll()).thenReturn(List.of(dokter, non));
        when(programStudiRepository.findById(1L)).thenReturn(Optional.of(dokter));

        JenisSeleksi result = jenisSeleksiService.createWithProgramStudi(req);

        assertThat(result).isNotNull();
        verify(selectionProgramStudiRepository, times(1)).save(any());
    }

    // ===== update =====

    @Test
    @DisplayName("update - not found throws IllegalArgumentException")
    void update_notFound_throwsException() {
        when(jenisSeleksiRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jenisSeleksiService.update(99L, buildJenisSeleksi(null, "X")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Jenis Seleksi tidak ditemukan");
    }

    @Test
    @DisplayName("update - code changed to existing code throws IllegalArgumentException")
    void update_codeChangedToDuplicate_throwsException() {
        JenisSeleksi existing = buildJenisSeleksi(1L, "REGULER");
        JenisSeleksi updates = buildJenisSeleksi(null, "MEDICAL");

        when(jenisSeleksiRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(jenisSeleksiRepository.existsByCode("MEDICAL")).thenReturn(true);

        assertThatThrownBy(() -> jenisSeleksiService.update(1L, updates))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Code sudah digunakan");
    }

    @Test
    @DisplayName("update - same code does not check for duplicate")
    void update_sameCode_doesNotCheckDuplicate() {
        JenisSeleksi existing = buildJenisSeleksi(1L, "REGULER");
        JenisSeleksi updates = buildJenisSeleksi(null, "REGULER");
        updates.setNama("Updated Nama");
        updates.setIsActive(false);
        updates.setSortOrder(5);

        when(jenisSeleksiRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(jenisSeleksiRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        JenisSeleksi result = jenisSeleksiService.update(1L, updates);

        assertThat(result.getNama()).isEqualTo("Updated Nama");
        assertThat(result.getIsActive()).isFalse();
        assertThat(result.getSortOrder()).isEqualTo(5);
        verify(jenisSeleksiRepository, never()).existsByCode(any());
        verify(jenisSeleksiRepository).save(any());
    }

    @Test
    @DisplayName("update - all fields updated and saved")
    void update_allFields_updatedAndSaved() {
        JenisSeleksi existing = buildJenisSeleksi(1L, "REGULER");
        JenisSeleksi updates = JenisSeleksi.builder()
                .code("BARU")
                .nama("Baru Nama")
                .deskripsi("Deskripsi Baru")
                .fasilitas("Fasilitas Baru")
                .logoUrl("http://new-logo.test")
                .harga(new java.math.BigDecimal(750000))
                .isActive(false)
                .sortOrder(3)
                .build();

        when(jenisSeleksiRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(jenisSeleksiRepository.existsByCode("BARU")).thenReturn(false);
        when(jenisSeleksiRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        JenisSeleksi result = jenisSeleksiService.update(1L, updates);

        assertThat(result.getCode()).isEqualTo("BARU");
        assertThat(result.getNama()).isEqualTo("Baru Nama");
        assertThat(result.getHarga()).isEqualTo(new java.math.BigDecimal(750000));
        assertThat(result.getIsActive()).isFalse();
        assertThat(result.getSortOrder()).isEqualTo(3);
    }

    // ===== updateWithProgramStudi =====

    @Test
    @DisplayName("updateWithProgramStudi - deletes old links and creates new ones")
    void updateWithProgramStudi_replacesLinks() {
        JenisSeleksiRequest req = buildRequest("REGULER", List.of(1L));
        req.setIsActive(true);
        JenisSeleksi existing = buildJenisSeleksi(1L, "REGULER");

        when(jenisSeleksiRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(jenisSeleksiRepository.save(any())).thenReturn(existing);
        when(programStudiRepository.findById(1L)).thenReturn(
                Optional.of(buildProgramStudi(1L, "Teknik Informatika")));

        JenisSeleksi result = jenisSeleksiService.updateWithProgramStudi(1L, req);

        assertThat(result).isNotNull();
        verify(selectionProgramStudiRepository).deleteByJenisSeleksi_Id(1L);
        verify(selectionProgramStudiRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("updateWithProgramStudi - null programStudiIds skips re-linking")
    void updateWithProgramStudi_nullProgramStudiIds_skipsLinking() {
        JenisSeleksiRequest req = buildRequest("REGULER", null);
        req.setProgramStudiIds(null);
        req.setIsActive(true);
        JenisSeleksi existing = buildJenisSeleksi(1L, "REGULER");

        when(jenisSeleksiRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(jenisSeleksiRepository.save(any())).thenReturn(existing);

        JenisSeleksi result = jenisSeleksiService.updateWithProgramStudi(1L, req);

        assertThat(result).isNotNull();
        verify(selectionProgramStudiRepository).deleteByJenisSeleksi_Id(1L);
        verify(selectionProgramStudiRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateWithProgramStudi - isActive null defaults to true")
    void updateWithProgramStudi_isActiveNull_defaultsToTrue() {
        JenisSeleksiRequest req = buildRequest("REGULER", List.of());
        req.setIsActive(null);
        JenisSeleksi existing = buildJenisSeleksi(1L, "REGULER");

        when(jenisSeleksiRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(jenisSeleksiRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        JenisSeleksi result = jenisSeleksiService.updateWithProgramStudi(1L, req);

        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("updateWithProgramStudi - sortOrder null defaults to 0")
    void updateWithProgramStudi_sortOrderNull_defaultsToZero() {
        JenisSeleksiRequest req = buildRequest("REGULER", List.of());
        req.setSortOrder(null);
        req.setIsActive(true);
        JenisSeleksi existing = buildJenisSeleksi(1L, "REGULER");

        when(jenisSeleksiRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(jenisSeleksiRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        JenisSeleksi result = jenisSeleksiService.updateWithProgramStudi(1L, req);

        assertThat(result.getSortOrder()).isEqualTo(0);
    }

    // ===== delete =====

    @Test
    @DisplayName("delete - not found throws IllegalArgumentException")
    void delete_notFound_throwsException() {
        when(jenisSeleksiRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> jenisSeleksiService.delete(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Jenis Seleksi tidak ditemukan");
    }

    @Test
    @DisplayName("delete - found deletes period links, program studi links, then entity")
    void delete_found_deletesAllLinksAndEntity() {
        when(jenisSeleksiRepository.existsById(1L)).thenReturn(true);
        doNothing().when(periodJenisSeleksiRepository).deleteByJenisSeleksi_Id(1L);
        doNothing().when(selectionProgramStudiRepository).deleteByJenisSeleksi_Id(1L);
        doNothing().when(entityManager).flush();
        doNothing().when(jenisSeleksiRepository).deleteById(1L);

        jenisSeleksiService.delete(1L);

        verify(periodJenisSeleksiRepository).deleteByJenisSeleksi_Id(1L);
        verify(selectionProgramStudiRepository).deleteByJenisSeleksi_Id(1L);
        verify(entityManager).flush();
        verify(jenisSeleksiRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete - flush is called before deleteById (order matters for FK constraints)")
    void delete_flushCalledBeforeDeleteById() {
        when(jenisSeleksiRepository.existsById(1L)).thenReturn(true);

        var inOrder = inOrder(selectionProgramStudiRepository, entityManager, jenisSeleksiRepository);

        jenisSeleksiService.delete(1L);

        inOrder.verify(selectionProgramStudiRepository).deleteByJenisSeleksi_Id(1L);
        inOrder.verify(entityManager).flush();
        inOrder.verify(jenisSeleksiRepository).deleteById(1L);
    }

    // ===== toggleActive =====

    @Test
    @DisplayName("toggleActive - not found throws IllegalArgumentException")
    void toggleActive_notFound_throwsException() {
        when(jenisSeleksiRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jenisSeleksiService.toggleActive(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Jenis Seleksi tidak ditemukan");
    }

    @Test
    @DisplayName("toggleActive - active becomes inactive")
    void toggleActive_activeBecomesInactive() {
        JenisSeleksi js = buildJenisSeleksi(1L, "REGULER");
        js.setIsActive(true);

        when(jenisSeleksiRepository.findById(1L)).thenReturn(Optional.of(js));
        when(jenisSeleksiRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        JenisSeleksi result = jenisSeleksiService.toggleActive(1L);

        assertThat(result.getIsActive()).isFalse();
        verify(jenisSeleksiRepository).save(any());
    }

    @Test
    @DisplayName("toggleActive - inactive becomes active")
    void toggleActive_inactiveBecomesActive() {
        JenisSeleksi js = buildJenisSeleksi(1L, "REGULER");
        js.setIsActive(false);

        when(jenisSeleksiRepository.findById(1L)).thenReturn(Optional.of(js));
        when(jenisSeleksiRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        JenisSeleksi result = jenisSeleksiService.toggleActive(1L);

        assertThat(result.getIsActive()).isTrue();
        verify(jenisSeleksiRepository).save(any());
    }

    @Test
    @DisplayName("toggleActive - updatedAt is set on toggle")
    void toggleActive_setsUpdatedAt() {
        JenisSeleksi js = buildJenisSeleksi(1L, "REGULER");
        js.setIsActive(true);

        when(jenisSeleksiRepository.findById(1L)).thenReturn(Optional.of(js));
        when(jenisSeleksiRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        JenisSeleksi result = jenisSeleksiService.toggleActive(1L);

        assertThat(result.getUpdatedAt()).isNotNull();
    }
}