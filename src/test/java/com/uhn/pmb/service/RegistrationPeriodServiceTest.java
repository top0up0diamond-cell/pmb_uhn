package com.uhn.pmb.service;

import com.uhn.pmb.dto.RegistrationPeriodRequest;
import com.uhn.pmb.entity.*;
import com.uhn.pmb.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationPeriodServiceTest {

    @Mock private RegistrationPeriodRepository registrationPeriodRepository;
    @Mock private PeriodJenisSeleksiRepository periodJenisSeleksiRepository;
    @Mock private JenisSeleksiRepository jenisSeleksiRepository;
    @Mock private EntityManager entityManager;

    private RegistrationPeriodService registrationPeriodService;

    @BeforeEach
    void setUp() {
        registrationPeriodService = new RegistrationPeriodService(
                registrationPeriodRepository,
                periodJenisSeleksiRepository,
                jenisSeleksiRepository,
                entityManager
        );
    }

    private RegistrationPeriod buildPeriod(Long id, String name) {
        return RegistrationPeriod.builder()
                .id(id)
                .name(name)
                .status(RegistrationPeriod.Status.OPEN)
                .waveType(RegistrationPeriod.WaveType.REGULAR_TEST)
                .build();
    }

    private RegistrationPeriodRequest buildRequest(String name) {
        RegistrationPeriodRequest req = new RegistrationPeriodRequest();
        req.setName(name);
        req.setRegStartDate(LocalDateTime.of(2026, 1, 1, 0, 0));
        req.setRegEndDate(LocalDateTime.of(2026, 2, 1, 0, 0));
        req.setExamDate(LocalDateTime.of(2026, 3, 1, 0, 0));
        req.setExamEndDate(LocalDateTime.of(2026, 3, 2, 0, 0));
        req.setAnnouncementDate(LocalDateTime.of(2026, 4, 1, 0, 0));
        req.setReenrollmentStartDate(LocalDateTime.of(2026, 4, 5, 0, 0));
        req.setReenrollmentEndDate(LocalDateTime.of(2026, 4, 15, 0, 0));
        req.setDescription("Periode Reguler");
        req.setRequirements("Persyaratan lengkap");
        req.setWaveType("REGULAR_TEST");
        req.setJenisSeleksiIds(List.of());
        return req;
    }

    // ===== findAll =====

    @Test
    @DisplayName("findAll - returns all periods")
    void findAll_returnsList() {
        when(registrationPeriodRepository.findAll())
                .thenReturn(List.of(buildPeriod(1L, "Periode 1")));

        List<RegistrationPeriod> result = registrationPeriodService.findAll();

        assertThat(result).hasSize(1);
    }

    // ===== findById =====

    @Test
    @DisplayName("findById - found returns Optional with entity")
    void findById_found_returnsOptional() {
        when(registrationPeriodRepository.findById(1L))
                .thenReturn(Optional.of(buildPeriod(1L, "Periode 1")));

        Optional<RegistrationPeriod> result = registrationPeriodService.findById(1L);

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("findById - not found returns empty")
    void findById_notFound_returnsEmpty() {
        when(registrationPeriodRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<RegistrationPeriod> result = registrationPeriodService.findById(99L);

        assertThat(result).isEmpty();
    }

    // ===== create =====

    @Test
    @DisplayName("create - happy path saves period with empty jenisSeleksi auto-resolve")
    void create_happyPath_savesWithAutoResolve() {
        RegistrationPeriodRequest req = buildRequest("Periode Baru");
        req.setJenisSeleksiIds(List.of());

        when(registrationPeriodRepository.save(any())).thenAnswer(inv -> {
            RegistrationPeriod p = inv.getArgument(0);
            return p;
        });
        when(jenisSeleksiRepository.findByCode("KEDOKTERAN")).thenReturn(Optional.empty());
        when(jenisSeleksiRepository.findByCode("NON_KEDOKTERAN")).thenReturn(Optional.empty());

        RegistrationPeriod result = registrationPeriodService.create(req);

        assertThat(result.getName()).isEqualTo("Periode Baru");
        assertThat(result.getStatus()).isEqualTo(RegistrationPeriod.Status.OPEN);
        verify(registrationPeriodRepository).save(any());
    }

    @Test
    @DisplayName("create - with explicit jenisSeleksi ids links them")
    void create_withJenisSeleksiIds_linksAll() {
        RegistrationPeriodRequest req = buildRequest("Periode Baru");
        req.setJenisSeleksiIds(List.of(1L, 2L));

        when(registrationPeriodRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        JenisSeleksi js1 = new JenisSeleksi(); js1.setId(1L);
        JenisSeleksi js2 = new JenisSeleksi(); js2.setId(2L);
        when(jenisSeleksiRepository.findById(1L)).thenReturn(Optional.of(js1));
        when(jenisSeleksiRepository.findById(2L)).thenReturn(Optional.of(js2));
        when(periodJenisSeleksiRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegistrationPeriod result = registrationPeriodService.create(req);

        assertThat(result).isNotNull();
        verify(periodJenisSeleksiRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("create - null waveType defaults to REGULAR_TEST")
    void create_nullWaveType_defaultsToRegularTest() {
        RegistrationPeriodRequest req = buildRequest("Periode Baru");
        req.setWaveType(null);
        req.setJenisSeleksiIds(List.of());

        when(registrationPeriodRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jenisSeleksiRepository.findByCode(any())).thenReturn(Optional.empty());

        RegistrationPeriod result = registrationPeriodService.create(req);

        assertThat(result.getWaveType()).isEqualTo(RegistrationPeriod.WaveType.REGULAR_TEST);
    }

    @Test
    @DisplayName("create - jenisSeleksi id not found is silently skipped")
    void create_jenisSeleksiNotFound_skips() {
        RegistrationPeriodRequest req = buildRequest("Periode Baru");
        req.setJenisSeleksiIds(List.of(99L));

        when(registrationPeriodRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jenisSeleksiRepository.findById(99L)).thenReturn(Optional.empty());

        RegistrationPeriod result = registrationPeriodService.create(req);

        assertThat(result).isNotNull();
        verify(periodJenisSeleksiRepository, never()).save(any());
    }

    // ===== update =====

    @Test
    @DisplayName("update - not found throws RuntimeException")
    void update_notFound_throws() {
        when(registrationPeriodRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationPeriodService.update(99L, buildRequest("X")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("tidak ditemukan");
    }

    @Test
    @DisplayName("update - happy path re-links jenis seleksi")
    void update_happyPath_relinksJenisSeleksi() {
        RegistrationPeriod existing = buildPeriod(1L, "Periode Lama");
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(registrationPeriodRepository.save(any())).thenReturn(existing);
        doNothing().when(periodJenisSeleksiRepository).deleteByPeriod_Id(1L);
        doNothing().when(entityManager).flush();

        RegistrationPeriodRequest req = buildRequest("Periode Updated");
        req.setJenisSeleksiIds(List.of(1L));
        JenisSeleksi js = new JenisSeleksi(); js.setId(1L);
        when(jenisSeleksiRepository.findById(1L)).thenReturn(Optional.of(js));
        when(periodJenisSeleksiRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegistrationPeriod result = registrationPeriodService.update(1L, req);

        assertThat(result.getName()).isEqualTo("Periode Updated");
        verify(periodJenisSeleksiRepository).deleteByPeriod_Id(1L);
        verify(periodJenisSeleksiRepository).save(any());
    }

    @Test
    @DisplayName("update - null waveType keeps existing waveType")
    void update_nullWaveType_keepsExisting() {
        RegistrationPeriod existing = buildPeriod(1L, "Periode");
        existing.setWaveType(RegistrationPeriod.WaveType.REGULAR_TEST);
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(registrationPeriodRepository.save(any())).thenReturn(existing);
        doNothing().when(periodJenisSeleksiRepository).deleteByPeriod_Id(1L);
        doNothing().when(entityManager).flush();

        RegistrationPeriodRequest req = buildRequest("Periode");
        req.setWaveType(null);
        req.setJenisSeleksiIds(List.of());
        when(jenisSeleksiRepository.findByCode(any())).thenReturn(Optional.empty());

        RegistrationPeriod result = registrationPeriodService.update(1L, req);

        assertThat(result.getWaveType()).isEqualTo(RegistrationPeriod.WaveType.REGULAR_TEST);
    }

    // ===== delete =====

    @Test
    @DisplayName("delete - not found throws RuntimeException")
    void delete_notFound_throws() {
        when(registrationPeriodRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationPeriodService.delete(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("tidak ditemukan");
    }

    @Test
    @DisplayName("delete - happy path deletes and flushes")
    void delete_happyPath_deletesAndFlushes() {
        RegistrationPeriod existing = buildPeriod(1L, "Periode 1");
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(existing));
        doNothing().when(periodJenisSeleksiRepository).deleteByPeriod_Id(1L);
        doNothing().when(entityManager).flush();
        doNothing().when(registrationPeriodRepository).delete(existing);

        registrationPeriodService.delete(1L);

        verify(periodJenisSeleksiRepository).deleteByPeriod_Id(1L);
        verify(entityManager).flush();
        verify(registrationPeriodRepository).delete(existing);
    }
}