package com.uhn.pmb.service;

import com.uhn.pmb.entity.PublicationSchedule;
import com.uhn.pmb.entity.RegistrationPeriod;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicationScheduleServiceTest {

    @Mock private PublicationScheduleRepository publicationScheduleRepository;
    @Mock private RegistrationPeriodRepository registrationPeriodRepository;

    @InjectMocks
    private PublicationScheduleService publicationScheduleService;

    // ===== Helpers =====

    private RegistrationPeriod buildPeriod(Long id, String name) {
        RegistrationPeriod period = new RegistrationPeriod();
        period.setId(id);
        period.setName(name);
        return period;
    }

    private PublicationSchedule buildSchedule(Long id, RegistrationPeriod period, boolean isPublished) {
        PublicationSchedule s = PublicationSchedule.builder()
                .id(id)
                .period(period)
                .publishDateTime(LocalDateTime.now().plusDays(1))
                .isPublished(isPublished)
                .createdBy("admin")
                .notes("Test notes")
                .build();
        return s;
    }

    // ===== getAllSchedules =====

    @Test
    @DisplayName("getAllSchedules - returns mapped list from repository")
    void getAllSchedules_returnsMappedList() {
        RegistrationPeriod period = buildPeriod(1L, "Gelombang 1");
        PublicationSchedule s = buildSchedule(1L, period, false);
        s.setPublishDateTime(LocalDateTime.now().plusDays(1));

        when(publicationScheduleRepository.findAllByOrderByPublishDateTimeDesc()).thenReturn(List.of(s));

        List<Map<String, Object>> result = publicationScheduleService.getAllSchedules();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("id")).isEqualTo(1L);
        assertThat(result.get(0).get("periodId")).isEqualTo(1L);
        assertThat(result.get(0).get("periodName")).isEqualTo("Gelombang 1");
    }

    @Test
    @DisplayName("getAllSchedules - empty repository returns empty list")
    void getAllSchedules_empty_returnsEmptyList() {
        when(publicationScheduleRepository.findAllByOrderByPublishDateTimeDesc()).thenReturn(List.of());

        List<Map<String, Object>> result = publicationScheduleService.getAllSchedules();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getAllSchedules - schedule with publishedAt maps publishedAt field")
    void getAllSchedules_withPublishedAt_mapsField() {
        RegistrationPeriod period = buildPeriod(1L, "Gelombang 1");
        PublicationSchedule s = buildSchedule(1L, period, true);
        s.setPublishedAt(LocalDateTime.of(2024, 6, 1, 10, 0));

        when(publicationScheduleRepository.findAllByOrderByPublishDateTimeDesc()).thenReturn(List.of(s));

        List<Map<String, Object>> result = publicationScheduleService.getAllSchedules();

        assertThat(result.get(0).get("publishedAt")).isNotNull();
        assertThat(result.get(0).get("isPublished")).isEqualTo(true);
    }

    @Test
    @DisplayName("getAllSchedules - schedule with null publishedAt maps to null")
    void getAllSchedules_withNullPublishedAt_mapsToNull() {
        RegistrationPeriod period = buildPeriod(1L, "Gelombang 1");
        PublicationSchedule s = buildSchedule(1L, period, false);
        s.setPublishedAt(null);

        when(publicationScheduleRepository.findAllByOrderByPublishDateTimeDesc()).thenReturn(List.of(s));

        List<Map<String, Object>> result = publicationScheduleService.getAllSchedules();

        assertThat(result.get(0).get("publishedAt")).isNull();
    }

    // ===== getScheduleByPeriod =====

    @Test
    @DisplayName("getScheduleByPeriod - not found returns exists=false")
    void getScheduleByPeriod_notFound_returnsExistsFalse() {
        when(publicationScheduleRepository.findByPeriodId(99L)).thenReturn(Optional.empty());

        Map<String, Object> result = publicationScheduleService.getScheduleByPeriod(99L);

        assertThat(result.get("exists")).isEqualTo(false);
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getScheduleByPeriod - found returns map with exists=true")
    void getScheduleByPeriod_found_returnsMapWithExistsTrue() {
        RegistrationPeriod period = buildPeriod(1L, "Gelombang 1");
        PublicationSchedule s = buildSchedule(1L, period, false);

        when(publicationScheduleRepository.findByPeriodId(1L)).thenReturn(Optional.of(s));

        Map<String, Object> result = publicationScheduleService.getScheduleByPeriod(1L);

        assertThat(result.get("exists")).isEqualTo(true);
        assertThat(result.get("id")).isEqualTo(1L);
        assertThat(result.get("periodName")).isEqualTo("Gelombang 1");
    }

    @Test
    @DisplayName("getScheduleByPeriod - found includes all mapped fields")
    void getScheduleByPeriod_found_includesAllFields() {
        RegistrationPeriod period = buildPeriod(2L, "Gelombang 2");
        PublicationSchedule s = buildSchedule(2L, period, true);
        s.setPublishedAt(LocalDateTime.of(2024, 7, 1, 8, 0));

        when(publicationScheduleRepository.findByPeriodId(2L)).thenReturn(Optional.of(s));

        Map<String, Object> result = publicationScheduleService.getScheduleByPeriod(2L);

        assertThat(result).containsKeys("id", "periodId", "periodName", "publishDateTime",
                "isPublished", "publishedAt", "createdBy", "notes", "resultsVisible", "exists");
    }

    // ===== createOrUpdate =====

    @Test
    @DisplayName("createOrUpdate - period not found throws RuntimeException")
    void createOrUpdate_periodNotFound_throwsException() {
        when(registrationPeriodRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> publicationScheduleService.createOrUpdate(
                99L, "2025-12-01T10:00:00", "Notes", "admin"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Period not found");
    }

    @Test
    @DisplayName("createOrUpdate - no existing schedule creates new one")
    void createOrUpdate_noExistingSchedule_createsNew() {
        RegistrationPeriod period = buildPeriod(1L, "Gelombang 1");
        PublicationSchedule saved = buildSchedule(10L, period, false);
        saved.setId(10L);

        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(publicationScheduleRepository.findByPeriodId(1L)).thenReturn(Optional.empty());
        when(publicationScheduleRepository.save(any())).thenReturn(saved);

        Map<String, Object> result = publicationScheduleService.createOrUpdate(
                1L, "2099-12-01T10:00:00", "Catatan", "admin");

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("id")).isEqualTo(10L);
        verify(publicationScheduleRepository).save(any());
    }

    @Test
    @DisplayName("createOrUpdate - existing schedule updates it")
    void createOrUpdate_existingSchedule_updatesFields() {
        RegistrationPeriod period = buildPeriod(1L, "Gelombang 1");
        PublicationSchedule existing = buildSchedule(5L, period, false);
        existing.setId(5L);

        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(publicationScheduleRepository.findByPeriodId(1L)).thenReturn(Optional.of(existing));
        when(publicationScheduleRepository.save(any())).thenReturn(existing);

        Map<String, Object> result = publicationScheduleService.createOrUpdate(
                1L, "2099-12-01T10:00:00", "Notes baru", "admin2");

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(existing.getNotes()).isEqualTo("Notes baru");
        assertThat(existing.getCreatedBy()).isEqualTo("admin2");
    }

    @Test
    @DisplayName("createOrUpdate - publishDateTime in the past and not yet published auto-publishes")
    void createOrUpdate_pastDateTime_notPublished_autoPublishes() {
        RegistrationPeriod period = buildPeriod(1L, "Gelombang 1");
        PublicationSchedule existing = buildSchedule(5L, period, false);

        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(publicationScheduleRepository.findByPeriodId(1L)).thenReturn(Optional.of(existing));
        when(publicationScheduleRepository.save(any())).thenReturn(existing);

        // Use a past datetime to trigger auto-publish
        String pastDateTime = LocalDateTime.now().minusHours(1).toString();
        publicationScheduleService.createOrUpdate(1L, pastDateTime, "Notes", "admin");

        assertThat(existing.getIsPublished()).isTrue();
        assertThat(existing.getPublishedAt()).isNotNull();
    }

    @Test
    @DisplayName("createOrUpdate - publishDateTime in the past but already published does not reset publishedAt")
    void createOrUpdate_pastDateTime_alreadyPublished_doesNotResetPublishedAt() {
        RegistrationPeriod period = buildPeriod(1L, "Gelombang 1");
        PublicationSchedule existing = buildSchedule(5L, period, true);
        LocalDateTime originalPublishedAt = LocalDateTime.of(2024, 1, 1, 8, 0);
        existing.setPublishedAt(originalPublishedAt);

        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(publicationScheduleRepository.findByPeriodId(1L)).thenReturn(Optional.of(existing));
        when(publicationScheduleRepository.save(any())).thenReturn(existing);

        String pastDateTime = LocalDateTime.now().minusHours(1).toString();
        publicationScheduleService.createOrUpdate(1L, pastDateTime, "Notes", "admin");

        // publishedAt should remain unchanged (not overwritten)
        assertThat(existing.getPublishedAt()).isEqualTo(originalPublishedAt);
    }

    @Test
    @DisplayName("createOrUpdate - future publishDateTime does not auto-publish")
    void createOrUpdate_futureDateTime_doesNotAutoPublish() {
        RegistrationPeriod period = buildPeriod(1L, "Gelombang 1");
        PublicationSchedule existing = buildSchedule(5L, period, false);

        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(publicationScheduleRepository.findByPeriodId(1L)).thenReturn(Optional.of(existing));
        when(publicationScheduleRepository.save(any())).thenReturn(existing);

        String futureDateTime = LocalDateTime.now().plusDays(7).toString();
        publicationScheduleService.createOrUpdate(1L, futureDateTime, "Notes", "admin");

        assertThat(existing.getIsPublished()).isFalse();
        assertThat(existing.getPublishedAt()).isNull();
    }

    @Test
    @DisplayName("createOrUpdate - returns success message")
    void createOrUpdate_returnsSuccessMessage() {
        RegistrationPeriod period = buildPeriod(1L, "Gelombang 1");
        PublicationSchedule saved = buildSchedule(1L, period, false);

        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(publicationScheduleRepository.findByPeriodId(1L)).thenReturn(Optional.empty());
        when(publicationScheduleRepository.save(any())).thenReturn(saved);

        Map<String, Object> result = publicationScheduleService.createOrUpdate(
                1L, "2099-01-01T00:00:00", null, "admin");

        assertThat(result.get("message")).isEqualTo("Jadwal publikasi berhasil disimpan");
    }

    // ===== publishNow =====

    @Test
    @DisplayName("publishNow - period not found throws RuntimeException")
    void publishNow_periodNotFound_throwsException() {
        when(registrationPeriodRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> publicationScheduleService.publishNow(99L, "admin"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Period not found");
    }

    @Test
    @DisplayName("publishNow - existing schedule sets isPublished and publishedAt")
    void publishNow_existingSchedule_setsPublished() {
        RegistrationPeriod period = buildPeriod(1L, "Gelombang 1");
        PublicationSchedule existing = buildSchedule(5L, period, false);

        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(publicationScheduleRepository.findByPeriodId(1L)).thenReturn(Optional.of(existing));
        when(publicationScheduleRepository.save(any())).thenReturn(existing);

        Map<String, Object> result = publicationScheduleService.publishNow(1L, "admin");

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(existing.getIsPublished()).isTrue();
        assertThat(existing.getPublishedAt()).isNotNull();
        assertThat(existing.getCreatedBy()).isEqualTo("admin");
        verify(publicationScheduleRepository).save(any());
    }

    @Test
    @DisplayName("publishNow - no existing schedule creates new one and publishes")
    void publishNow_noExistingSchedule_createsAndPublishes() {
        RegistrationPeriod period = buildPeriod(1L, "Gelombang 1");

        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(publicationScheduleRepository.findByPeriodId(1L)).thenReturn(Optional.empty());
        when(publicationScheduleRepository.save(any())).thenAnswer(inv -> {
            PublicationSchedule s = inv.getArgument(0);
            s.setId(99L);
            return s;
        });

        Map<String, Object> result = publicationScheduleService.publishNow(1L, "superadmin");

        assertThat(result.get("success")).isEqualTo(true);
        verify(publicationScheduleRepository).save(argThat(s ->
                s.getIsPublished() != null && s.getIsPublished()
                        && s.getPublishedAt() != null
                        && "superadmin".equals(s.getCreatedBy())
        ));
    }

    @Test
    @DisplayName("publishNow - returns correct success message")
    void publishNow_returnsSuccessMessage() {
        RegistrationPeriod period = buildPeriod(1L, "Gelombang 1");
        PublicationSchedule existing = buildSchedule(1L, period, false);

        when(registrationPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(publicationScheduleRepository.findByPeriodId(1L)).thenReturn(Optional.of(existing));
        when(publicationScheduleRepository.save(any())).thenReturn(existing);

        Map<String, Object> result = publicationScheduleService.publishNow(1L, "admin");

        assertThat(result.get("message")).isEqualTo("Hasil kelulusan berhasil dipublikasikan");
    }

    // ===== deleteSchedule =====

    @Test
    @DisplayName("deleteSchedule - calls deleteById with correct id")
    void deleteSchedule_callsDeleteById() {
        doNothing().when(publicationScheduleRepository).deleteById(5L);

        publicationScheduleService.deleteSchedule(5L);

        verify(publicationScheduleRepository).deleteById(5L);
    }

    @Test
    @DisplayName("deleteSchedule - different ids each call separate deleteById")
    void deleteSchedule_differentIds_callsSeparately() {
        doNothing().when(publicationScheduleRepository).deleteById(any());

        publicationScheduleService.deleteSchedule(1L);
        publicationScheduleService.deleteSchedule(2L);

        verify(publicationScheduleRepository).deleteById(1L);
        verify(publicationScheduleRepository).deleteById(2L);
    }
}