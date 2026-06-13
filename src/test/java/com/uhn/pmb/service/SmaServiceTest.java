package com.uhn.pmb.service;

import com.uhn.pmb.entity.Sma;
import com.uhn.pmb.repository.SmaRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmaServiceTest {

    @Mock
    private SmaRepository smaRepository;

    @InjectMocks
    private SmaService smaService;

    private Sma buildSma(Long id, String nama) {
        Sma sma = new Sma();
        sma.setId(id);
        sma.setNama(nama);
        sma.setIsActive(true);
        return sma;
    }

    // ===== search =====

    @Test
    @DisplayName("search - keyword valid returns results")
    void search_validKeyword_returnsResults() {
        Sma sma = buildSma(1L, "SMA Negeri 1 Medan");
        when(smaRepository.searchByQuery("SMA")).thenReturn(List.of(sma));

        List<Sma> result = smaService.search("SMA");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNama()).contains("SMA");
        verify(smaRepository).searchByQuery("SMA");
    }

    @Test
    @DisplayName("search - keyword too short (< 2 chars) returns empty")
    void search_keywordTooShort_returnsEmpty() {
        List<Sma> result = smaService.search("A");

        assertThat(result).isEmpty();
        verifyNoInteractions(smaRepository);
    }

    @Test
    @DisplayName("search - keyword null returns empty")
    void search_nullKeyword_returnsEmpty() {
        List<Sma> result = smaService.search(null);

        assertThat(result).isEmpty();
        verifyNoInteractions(smaRepository);
    }

    @Test
    @DisplayName("search - keyword empty string returns empty")
    void search_emptyKeyword_returnsEmpty() {
        List<Sma> result = smaService.search("");

        assertThat(result).isEmpty();
        verifyNoInteractions(smaRepository);
    }

    @Test
    @DisplayName("search - keyword not found returns empty list")
    void search_notFound_returnsEmpty() {
        when(smaRepository.searchByQuery("XYZNOTFOUND")).thenReturn(List.of());

        List<Sma> result = smaService.search("XYZNOTFOUND");

        assertThat(result).isEmpty();
    }

    // ===== findAllActive =====

    @Test
    @DisplayName("findAllActive - returns only active schools")
    void findAllActive_returnsActiveOnly() {
        Sma sma = buildSma(1L, "SMA Aktif");
        when(smaRepository.findByIsActiveTrue()).thenReturn(List.of(sma));

        List<Sma> result = smaService.findAllActive();

        assertThat(result).hasSize(1);
        verify(smaRepository).findByIsActiveTrue();
    }

    // ===== findAll =====

    @Test
    @DisplayName("findAll - returns all schools including inactive")
    void findAll_returnsAll() {
        when(smaRepository.findAll()).thenReturn(List.of(buildSma(1L, "A"), buildSma(2L, "B")));

        List<Sma> result = smaService.findAll();

        assertThat(result).hasSize(2);
    }

    // ===== create =====

    @Test
    @DisplayName("create - valid data saves and returns SMA")
    void create_validData_savesSma() {
        Map<String, String> body = Map.of("nama", "SMK Baru", "bentuk", "SMK");
        Sma saved = buildSma(10L, "SMK Baru");
        when(smaRepository.save(any(Sma.class))).thenReturn(saved);

        Sma result = smaService.create(body);

        assertThat(result).isNotNull();
        assertThat(result.getNama()).isEqualTo("SMK Baru");
        verify(smaRepository).save(any(Sma.class));
    }

    @Test
    @DisplayName("create - nama null throws IllegalArgumentException")
    void create_nullNama_throwsException() {
        Map<String, String> body = Map.of("bentuk", "SMA");

        assertThatThrownBy(() -> smaService.create(body))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nama sekolah");
    }

    @Test
    @DisplayName("create - nama blank throws IllegalArgumentException")
    void create_blankNama_throwsException() {
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("nama", "   ");

        assertThatThrownBy(() -> smaService.create(body))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("create - duplicate NPSN throws IllegalArgumentException")
    void create_duplicateNpsn_throwsException() {
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("nama", "SMA Test");
        body.put("npsn", "12345678");
        when(smaRepository.existsByNpsn("12345678")).thenReturn(true);

        assertThatThrownBy(() -> smaService.create(body))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NPSN");
    }

    @Test
    @DisplayName("create - with all fields saves correctly")
    void create_allFields_savesSma() {
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("nama", "SMA Negeri 1");
        body.put("npsn", "99988877");
        body.put("kota", "Medan");
        body.put("provinsi", "Sumatera Utara");
        body.put("bentuk", "SMA");
        when(smaRepository.existsByNpsn("99988877")).thenReturn(false);
        Sma saved = buildSma(5L, "SMA Negeri 1");
        when(smaRepository.save(any())).thenReturn(saved);

        Sma result = smaService.create(body);

        assertThat(result).isNotNull();
    }

    // ===== update =====

    @Test
    @DisplayName("update - existing SMA updates and returns")
    void update_existingSma_updates() {
        Sma existing = buildSma(1L, "SMA Lama");
        when(smaRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(smaRepository.save(any())).thenReturn(existing);

        Sma result = smaService.update(1L, Map.of("nama", "SMA Baru"));

        assertThat(result).isNotNull();
        verify(smaRepository).save(any());
    }

    @Test
    @DisplayName("update - not found throws RuntimeException")
    void update_notFound_throwsException() {
        when(smaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> smaService.update(999L, Map.of("nama", "X")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("tidak ditemukan");
    }

    @Test
    @DisplayName("update - duplicate NPSN throws IllegalArgumentException")
    void update_duplicateNpsn_throwsException() {
        Sma existing = buildSma(1L, "SMA Lama");
        existing.setNpsn("11111111");
        when(smaRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(smaRepository.existsByNpsn("22222222")).thenReturn(true);

        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("npsn", "22222222");

        assertThatThrownBy(() -> smaService.update(1L, body))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NPSN");
    }

    @Test
    @DisplayName("update - all optional fields updated")
    void update_allFields_savesChanges() {
        Sma existing = buildSma(1L, "SMA Lama");
        existing.setNpsn("11111111");
        when(smaRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(smaRepository.save(any())).thenReturn(existing);

        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("nama", "SMA Baru");
        body.put("bentuk", "SMK");
        body.put("npsn", "11111111"); // same NPSN - should not trigger duplicate check
        body.put("kota", "Medan");
        body.put("provinsi", "Sumut");
        body.put("isActive", "false");

        Sma result = smaService.update(1L, body);

        assertThat(result).isNotNull();
    }

    // ===== deactivate =====

    @Test
    @DisplayName("deactivate - existing SMA sets isActive to false")
    void deactivate_existingSma_setsInactive() {
        Sma sma = buildSma(1L, "SMA");
        sma.setIsActive(true);
        when(smaRepository.findById(1L)).thenReturn(Optional.of(sma));
        when(smaRepository.save(any())).thenReturn(sma);

        smaService.deactivate(1L);

        assertThat(sma.getIsActive()).isFalse();
        verify(smaRepository).save(sma);
    }

    @Test
    @DisplayName("deactivate - not found throws RuntimeException")
    void deactivate_notFound_throwsException() {
        when(smaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> smaService.deactivate(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("tidak ditemukan");
    }
}
