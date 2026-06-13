package com.uhn.pmb.service;

import com.uhn.pmb.dto.UjianLinkRequest;
import com.uhn.pmb.entity.GelombangLinkUjian;
import com.uhn.pmb.entity.RegistrationPeriod;
import com.uhn.pmb.repository.GelombangLinkUjianRepository;
import com.uhn.pmb.repository.RegistrationPeriodRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUjianLinkServiceTest {

    @Mock private GelombangLinkUjianRepository ujianLinkRepository;
    @Mock private RegistrationPeriodRepository registrationPeriodRepository;

    @InjectMocks
    private AdminUjianLinkService adminUjianLinkService;

    @Test
    @DisplayName("getAllLinks - returns all links")
    void getAllLinks_returnsAll() {
        GelombangLinkUjian link = new GelombangLinkUjian();
        when(ujianLinkRepository.findAllByOrderByUpdatedAtDesc()).thenReturn(List.of(link));

        List<GelombangLinkUjian> result = adminUjianLinkService.getAllLinks();

        assertThat(result).hasSize(1);
        verify(ujianLinkRepository).findAllByOrderByUpdatedAtDesc();
    }

    @Test
    @DisplayName("getByPeriodId - returns link for period")
    void getByPeriodId_returnsLink() {
        GelombangLinkUjian link = new GelombangLinkUjian();
        when(ujianLinkRepository.findByRegistrationPeriodId(1L)).thenReturn(Optional.of(link));

        Optional<GelombangLinkUjian> result = adminUjianLinkService.getByPeriodId(1L);

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("getByPeriodId - not found returns empty")
    void getByPeriodId_notFound_returnsEmpty() {
        when(ujianLinkRepository.findByRegistrationPeriodId(999L)).thenReturn(Optional.empty());

        Optional<GelombangLinkUjian> result = adminUjianLinkService.getByPeriodId(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("createLink - duplicate period throws exception")
    void createLink_duplicatePeriod_throwsException() {
        UjianLinkRequest req = new UjianLinkRequest();
        req.setPeriodId(1L);
        req.setLinkUjian("https://forms.google.com/abc");
        RegistrationPeriod period = new RegistrationPeriod();
        period.setId(1L);
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(ujianLinkRepository.findByRegistrationPeriodId(1L)).thenReturn(Optional.of(new GelombangLinkUjian()));

        assertThatThrownBy(() -> adminUjianLinkService.createLink(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("createLink - new link creates successfully")
    void createLink_new_success() {
        UjianLinkRequest req = new UjianLinkRequest();
        req.setPeriodId(1L);
        req.setLinkUjian("https://forms.google.com/abc");
        RegistrationPeriod period = new RegistrationPeriod();
        period.setId(1L);
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(ujianLinkRepository.findByRegistrationPeriodId(1L)).thenReturn(Optional.empty());
        when(ujianLinkRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        GelombangLinkUjian result = adminUjianLinkService.createLink(req);

        assertThat(result).isNotNull();
        assertThat(result.getLinkUjian()).isEqualTo("https://forms.google.com/abc");
    }

    @Test
    @DisplayName("updateLink - existing link updated")
    void updateLink_existing_updated() {
        UjianLinkRequest req = new UjianLinkRequest();
        req.setPeriodId(1L);
        req.setLinkUjian("https://forms.google.com/new");
        GelombangLinkUjian link = new GelombangLinkUjian();
        when(ujianLinkRepository.findByRegistrationPeriodId(1L)).thenReturn(Optional.of(link));
        when(ujianLinkRepository.save(any())).thenReturn(link);

        GelombangLinkUjian result = adminUjianLinkService.updateLink(req);

        assertThat(result).isNotNull();
        assertThat(link.getLinkUjian()).isEqualTo("https://forms.google.com/new");
    }

    @Test
    @DisplayName("updateLink - not found throws exception")
    void updateLink_notFound_throwsException() {
        UjianLinkRequest req = new UjianLinkRequest();
        req.setPeriodId(999L);
        req.setLinkUjian("https://test.com");
        when(ujianLinkRepository.findByRegistrationPeriodId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUjianLinkService.updateLink(req))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("deleteByPeriodId - found deletes successfully")
    void deleteByPeriodId_found_deletes() {
        GelombangLinkUjian link = new GelombangLinkUjian();
        when(ujianLinkRepository.findByRegistrationPeriodId(1L)).thenReturn(Optional.of(link));
        doNothing().when(ujianLinkRepository).deleteByRegistrationPeriodId(1L);

        adminUjianLinkService.deleteByPeriodId(1L);

        verify(ujianLinkRepository).deleteByRegistrationPeriodId(1L);
    }

    @Test
    @DisplayName("deleteByPeriodId - not found throws RuntimeException")
    void deleteByPeriodId_notFound_throws() {
        when(ujianLinkRepository.findByRegistrationPeriodId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUjianLinkService.deleteByPeriodId(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("createOfflineExam - success creates offline exam")
    void createOfflineExam_success_creates() {
        UjianLinkRequest req = new UjianLinkRequest();
        req.setPeriodId(1L);
        req.setExamDate("2024-12-31");
        req.setExamPlace("Gedung A");
        req.setExamTime("09:00");
        RegistrationPeriod period = new RegistrationPeriod();
        period.setId(1L);
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(ujianLinkRepository.findByRegistrationPeriodId(1L)).thenReturn(Optional.empty());
        when(ujianLinkRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        GelombangLinkUjian result = adminUjianLinkService.createOfflineExam(req);

        assertThat(result).isNotNull();
        assertThat(result.getExamPlace()).isEqualTo("Gedung A");
    }

    @Test
    @DisplayName("createOfflineExam - missing fields throws RuntimeException")
    void createOfflineExam_missingFields_throws() {
        UjianLinkRequest req = new UjianLinkRequest();
        req.setPeriodId(1L);
        // examDate, examPlace, examTime are null

        assertThatThrownBy(() -> adminUjianLinkService.createOfflineExam(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("required");
    }

    @Test
    @DisplayName("createOfflineExam - already exists throws RuntimeException")
    void createOfflineExam_alreadyExists_throws() {
        UjianLinkRequest req = new UjianLinkRequest();
        req.setPeriodId(1L);
        req.setExamDate("2024-12-31");
        req.setExamPlace("Gedung A");
        req.setExamTime("09:00");
        RegistrationPeriod period = new RegistrationPeriod();
        period.setId(1L);
        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(ujianLinkRepository.findByRegistrationPeriodId(1L))
                .thenReturn(Optional.of(new GelombangLinkUjian()));

        assertThatThrownBy(() -> adminUjianLinkService.createOfflineExam(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("deleteOfflineExam - found deletes successfully")
    void deleteOfflineExam_found_deletes() {
        GelombangLinkUjian link = new GelombangLinkUjian();
        when(ujianLinkRepository.findByRegistrationPeriodId(1L)).thenReturn(Optional.of(link));
        doNothing().when(ujianLinkRepository).deleteByRegistrationPeriodId(1L);

        adminUjianLinkService.deleteOfflineExam(1L);

        verify(ujianLinkRepository).deleteByRegistrationPeriodId(1L);
    }

    @Test
    @DisplayName("deleteOfflineExam - not found throws RuntimeException")
    void deleteOfflineExam_notFound_throws() {
        when(ujianLinkRepository.findByRegistrationPeriodId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUjianLinkService.deleteOfflineExam(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("createLink - period not found throws RuntimeException")
    void createLink_periodNotFound_throws() {
        UjianLinkRequest req = new UjianLinkRequest();
        req.setPeriodId(999L);
        req.setLinkUjian("https://forms.google.com/abc");
        when(registrationPeriodRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUjianLinkService.createLink(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("getAllLinks - empty list returns empty")
    void getAllLinks_emptyList_returnsEmpty() {
        when(ujianLinkRepository.findAllByOrderByUpdatedAtDesc()).thenReturn(List.of());

        List<GelombangLinkUjian> result = adminUjianLinkService.getAllLinks();

        assertThat(result).isEmpty();
    }
}
