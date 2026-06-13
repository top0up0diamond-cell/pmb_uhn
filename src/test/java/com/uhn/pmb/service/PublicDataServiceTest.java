package com.uhn.pmb.service;

import com.uhn.pmb.entity.JenisSeleksi;
import com.uhn.pmb.entity.ProgramStudi;
import com.uhn.pmb.entity.PublicationSchedule;
import com.uhn.pmb.entity.RegistrationPeriod;
import com.uhn.pmb.repository.JenisSeleksiRepository;
import com.uhn.pmb.repository.ProgramStudiRepository;
import com.uhn.pmb.repository.PublicationScheduleRepository;
import com.uhn.pmb.repository.RegistrationPeriodRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicDataServiceTest {

    @Mock private RegistrationPeriodRepository registrationPeriodRepository;
    @Mock private JenisSeleksiRepository jenisSeleksiRepository;
    @Mock private ProgramStudiRepository programStudiRepository;
    @Mock private PublicationScheduleRepository publicationScheduleRepository;

    @InjectMocks
    private PublicDataService publicDataService;

    @Test
    @DisplayName("getAllGelombang - returns sorted list")
    void getAllGelombang_returnsList() {
        RegistrationPeriod p = new RegistrationPeriod();
        p.setId(1L);
        p.setRegStartDate(LocalDateTime.now());
        when(registrationPeriodRepository.findAll()).thenReturn(List.of(p));

        List<RegistrationPeriod> result = publicDataService.getAllGelombang();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getAllGelombang - empty returns empty")
    void getAllGelombang_emptyReturnsEmpty() {
        when(registrationPeriodRepository.findAll()).thenReturn(List.of());

        List<RegistrationPeriod> result = publicDataService.getAllGelombang();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getAllActiveJenisSeleksi - returns active entries")
    void getAllActiveJenisSeleksi_returnsList() {
        JenisSeleksi js = new JenisSeleksi();
        js.setIsActive(true);
        when(jenisSeleksiRepository.findAll()).thenReturn(List.of(js));

        List<JenisSeleksi> result = publicDataService.getAllActiveJenisSeleksi();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getAllActiveProgramStudi - returns active entries")
    void getAllActiveProgramStudi_returnsList() {
        ProgramStudi ps = new ProgramStudi();
        ps.setIsActive(true);
        when(programStudiRepository.findAll()).thenReturn(List.of(ps));

        List<ProgramStudi> result = publicDataService.getAllActiveProgramStudi();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getProgramStudiById - found returns optional")
    void getProgramStudiById_found() {
        ProgramStudi ps = new ProgramStudi();
        when(programStudiRepository.findById(1L)).thenReturn(Optional.of(ps));

        Optional<ProgramStudi> result = publicDataService.getProgramStudiById(1L);

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("getPublicationStatus - no schedule returns hasSchedule false")
    void getPublicationStatus_noSchedule_returnsHasScheduleFalse() {
        when(publicationScheduleRepository.findByPeriodId(1L)).thenReturn(Optional.empty());

        Map<String, Object> result = publicDataService.getPublicationStatus(1L);

        assertThat(result.get("hasSchedule")).isEqualTo(false);
    }

    @Test
    @DisplayName("getAllFakultas - returns list of fakultas")
    void getAllFakultas_returnsList() {
        when(programStudiRepository.findDistinctFakultasActive()).thenReturn(List.of("FT"));

        List<String> result = publicDataService.getAllFakultas();

        assertThat(result).contains("FT");
    }

    @Test
    @DisplayName("getProgramStudiByFakultas - groups by fakultas")
    void getProgramStudiByFakultas_returnsGrouped() {
        ProgramStudi ps = new ProgramStudi();
        ps.setId(1L);
        ps.setKode("IF01");
        ps.setNama("Teknik Informatika");
        ps.setFakultas("Teknik");
        when(programStudiRepository.findByIsActiveTrueOrderBySortOrder()).thenReturn(List.of(ps));

        Map<String, List<Map<String, Object>>> result = publicDataService.getProgramStudiByFakultas();

        assertThat(result).containsKey("Teknik");
        assertThat(result.get("Teknik")).hasSize(1);
    }

    @Test
    @DisplayName("getProgramStudiByFakultas - null fakultas groups under Lainnya")
    void getProgramStudiByFakultas_nullFakultas_groupsUnderLainnya() {
        ProgramStudi ps = new ProgramStudi();
        ps.setId(2L);
        ps.setFakultas(null);
        when(programStudiRepository.findByIsActiveTrueOrderBySortOrder()).thenReturn(List.of(ps));

        Map<String, List<Map<String, Object>>> result = publicDataService.getProgramStudiByFakultas();

        assertThat(result).containsKey("Lainnya");
    }
}
