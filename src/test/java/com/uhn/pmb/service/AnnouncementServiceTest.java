package com.uhn.pmb.service;

import com.uhn.pmb.entity.Announcement;
import com.uhn.pmb.repository.AnnouncementRepository;
import com.uhn.pmb.dto.CreateAnnouncementRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

    @Mock
    private AnnouncementRepository announcementRepository;

    @InjectMocks
    private AnnouncementService announcementService;

    private Announcement buildAnnouncement(Long id, String title) {
        Announcement a = new Announcement();
        a.setId(id);
        a.setTitle(title);
        a.setContent("Content");
        a.setIsActive(true);
        return a;
    }

    @Test
    @DisplayName("findAllActive - returns list of active announcements")
    void findAllActive_returnsActiveList() {
        when(announcementRepository.findAllActive())
                .thenReturn(List.of(buildAnnouncement(1L, "Test")));

        List<Announcement> result = announcementService.findAllActive();

        assertThat(result).hasSize(1);
        verify(announcementRepository).findAllActive();
    }

    @Test
    @DisplayName("findAllActive - empty returns empty list")
    void findAllActive_emptyReturnsEmpty() {
        when(announcementRepository.findAllActive())
                .thenReturn(List.of());

        List<Announcement> result = announcementService.findAllActive();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findById - existing id returns optional present")
    void findById_found_returnsOptional() {
        when(announcementRepository.findById(1L))
                .thenReturn(Optional.of(buildAnnouncement(1L, "Test")));

        Optional<Announcement> result = announcementService.findById(1L);

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("findById - not found returns empty")
    void findById_notFound_returnsEmpty() {
        when(announcementRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Announcement> result = announcementService.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findActiveById - not found throws RuntimeException")
    void findActiveById_notFound_throwsException() {
        when(announcementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> announcementService.findActiveById(99L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("findAllActivePaginated - returns page")
    void findAllActivePaginated_returnsPage() {
        Page<Announcement> page = new PageImpl<>(List.of(buildAnnouncement(1L, "A")));
        when(announcementRepository.findAllActive(any()))
                .thenReturn(page);

        Page<Announcement> result = announcementService.findAllActivePaginated(PageRequest.of(0, 10));

        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("save - saves and returns announcement")
    void save_persistsAnnouncement() {
        Announcement a = buildAnnouncement(1L, "Test");
        when(announcementRepository.save(a)).thenReturn(a);

        Announcement result = announcementService.save(a);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("delete - not found throws RuntimeException")
    void delete_notFound_throwsException() {
        when(announcementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> announcementService.delete(99L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("deactivate - found sets inactive")
    void deactivate_found_setsInactive() {
        Announcement a = buildAnnouncement(1L, "Test");
        when(announcementRepository.findById(1L)).thenReturn(Optional.of(a));
        when(announcementRepository.save(any())).thenReturn(a);

        Announcement result = announcementService.deactivate(1L);

        assertThat(result).isNotNull();
        verify(announcementRepository).save(any());
    }

    @Test
    @DisplayName("update - not found throws RuntimeException")
    void update_notFound_throwsException() {
        CreateAnnouncementRequest req = new CreateAnnouncementRequest();
        req.setTitle("Updated");
        when(announcementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> announcementService.update(99L, req))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("findById - found returns announcement object")
    void findById_found_returnsAnnouncement() {
        Announcement a = buildAnnouncement(1L, "Test");
        when(announcementRepository.findById(1L)).thenReturn(Optional.of(a));

        Optional<Announcement> result = announcementService.findById(1L);

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("findRecent - returns list")
    void findRecent_returnsList() {
        when(announcementRepository.findRecent()).thenReturn(List.of(buildAnnouncement(1L, "R")));

        List<Announcement> result = announcementService.findRecent();

        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("findUrgent - returns list")
    void findUrgent_returnsList() {
        when(announcementRepository.findUrgent()).thenReturn(List.of(buildAnnouncement(1L, "U")));

        List<Announcement> result = announcementService.findUrgent();

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("search - returns results")
    void search_returnsResults() {
        when(announcementRepository.searchByTitle("Hello")).thenReturn(List.of(buildAnnouncement(1L, "Hello World")));

        List<Announcement> result = announcementService.search("Hello");

        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("create - saves and returns announcement")
    void create_savesAnnouncement() {
        CreateAnnouncementRequest req = new CreateAnnouncementRequest();
        req.setTitle("Test Announcement");
        req.setContent("Content here");
        req.setAnnouncementType("GENERAL");
        Announcement saved = buildAnnouncement(1L, "Test Announcement");
        when(announcementRepository.save(any())).thenReturn(saved);

        Announcement result = announcementService.create(req, "admin");

        assertThat(result.getId()).isEqualTo(1L);
        verify(announcementRepository).save(any());
    }

    @Test
    @DisplayName("update - found updates and saves")
    void update_found_updatesAnnouncement() {
        CreateAnnouncementRequest req = new CreateAnnouncementRequest();
        req.setTitle("Updated Title");
        req.setContent("Updated Content");
        req.setAnnouncementType("GENERAL");
        Announcement existing = buildAnnouncement(1L, "Old Title");
        when(announcementRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(announcementRepository.save(any())).thenReturn(existing);

        Announcement result = announcementService.update(1L, req);

        assertThat(result).isNotNull();
        verify(announcementRepository).save(any());
    }

    @Test
    @DisplayName("delete - found deletes announcement")
    void delete_found_deletes() {
        Announcement a = buildAnnouncement(1L, "Test");
        when(announcementRepository.findById(1L)).thenReturn(Optional.of(a));
        doNothing().when(announcementRepository).delete(a);

        announcementService.delete(1L);

        verify(announcementRepository).delete(a);
    }

    @Test
    @DisplayName("findActiveById - found and active returns announcement")
    void findActiveById_found_returnsAnnouncement() {
        Announcement a = buildAnnouncement(1L, "Active");
        a.setIsActive(true);
        when(announcementRepository.findById(1L)).thenReturn(Optional.of(a));

        Announcement result = announcementService.findActiveById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }
}
