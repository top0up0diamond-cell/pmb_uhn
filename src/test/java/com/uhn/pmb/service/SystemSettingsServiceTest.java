package com.uhn.pmb.service;

import com.uhn.pmb.entity.SystemLink;
import com.uhn.pmb.entity.ContactInfo;
import com.uhn.pmb.repository.ContactInfoRepository;
import com.uhn.pmb.repository.SystemLinkRepository;
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
class SystemSettingsServiceTest {

    @Mock private ContactInfoRepository contactInfoRepository;
    @Mock private SystemLinkRepository systemLinkRepository;

    @InjectMocks
    private SystemSettingsService systemSettingsService;

    @Test
    @DisplayName("getContactInfo - existing returns present optional")
    void getContactInfo_existing_returnsPresent() {
        ContactInfo ci = new ContactInfo();
        when(contactInfoRepository.findAll()).thenReturn(List.of(ci));

        Optional<ContactInfo> result = systemSettingsService.getContactInfo();

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("getContactInfo - empty returns empty optional")
    void getContactInfo_empty_returnsEmpty() {
        when(contactInfoRepository.findAll()).thenReturn(List.of());

        Optional<ContactInfo> result = systemSettingsService.getContactInfo();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("saveContactInfo - saves and returns")
    void saveContactInfo_saves() {
        ContactInfo ci = new ContactInfo();
        when(contactInfoRepository.findAll()).thenReturn(List.of());
        when(contactInfoRepository.save(any())).thenReturn(ci);

        ContactInfo result = systemSettingsService.saveContactInfo(ci);

        assertThat(result).isNotNull();
        verify(contactInfoRepository).save(any());
    }

    @Test
    @DisplayName("getAllSystemLinks - returns all links")
    void getAllSystemLinks_returnsList() {
        SystemLink link = new SystemLink();
        when(systemLinkRepository.findAll()).thenReturn(List.of(link));

        List<SystemLink> result = systemSettingsService.getAllSystemLinks();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getActiveSystemLinks - returns active links")
    void getActiveSystemLinks_returnsList() {
        when(systemLinkRepository.findByIsActiveTrue()).thenReturn(List.of());

        List<SystemLink> result = systemSettingsService.getActiveSystemLinks();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getSystemLinkByName - returns optional")
    void getSystemLinkByName_returnsOptional() {
        SystemLink link = new SystemLink();
        when(systemLinkRepository.findByLinkName("portal")).thenReturn(Optional.of(link));

        Optional<SystemLink> result = systemSettingsService.getSystemLinkByName("portal");

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("updateSystemLink - not found throws RuntimeException")
    void updateSystemLink_notFound_throws() {
        when(systemLinkRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> systemSettingsService.updateSystemLink(99, new SystemLink()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("deleteSystemLink - not found throws RuntimeException")
    void deleteSystemLink_notFound_throws() {
        when(systemLinkRepository.existsById(99)).thenReturn(false);

        assertThatThrownBy(() -> systemSettingsService.deleteSystemLink(99))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("createSystemLink - saves new link")
    void createSystemLink_savesLink() {
        SystemLink link = new SystemLink();
        when(systemLinkRepository.save(link)).thenReturn(link);

        SystemLink result = systemSettingsService.createSystemLink(link);

        assertThat(result).isNotNull();
    }

    // ===== ITERASI 6 ADDITIONS =====

    @Test
    @DisplayName("saveContactInfo - existing present updates and saves")
    void saveContactInfo_existingPresent_updatesAndSaves() {
        ContactInfo existing = new ContactInfo();
        existing.setId(1);
        existing.setAddress("Old Address");
        existing.setPhone("0812");
        existing.setEmail("old@test.com");
        existing.setOperatingHours("08:00-17:00");

        ContactInfo update = new ContactInfo();
        update.setAddress("New Address");
        update.setPhone("0813");
        update.setEmail("new@test.com");
        update.setOperatingHours("09:00-18:00");

        when(contactInfoRepository.findAll()).thenReturn(List.of(existing));
        when(contactInfoRepository.save(existing)).thenReturn(existing);

        ContactInfo result = systemSettingsService.saveContactInfo(update);

        assertThat(result).isNotNull();
        verify(contactInfoRepository).save(existing);
        assertThat(existing.getAddress()).isEqualTo("New Address");
    }

    @Test
    @DisplayName("getSystemLinkById - found returns present optional")
    void getSystemLinkById_found_returnsPresent() {
        SystemLink link = new SystemLink();
        when(systemLinkRepository.findById(1)).thenReturn(Optional.of(link));

        Optional<SystemLink> result = systemSettingsService.getSystemLinkById(1);

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("getSystemLinkById - not found returns empty optional")
    void getSystemLinkById_notFound_returnsEmpty() {
        when(systemLinkRepository.findById(99)).thenReturn(Optional.empty());

        Optional<SystemLink> result = systemSettingsService.getSystemLinkById(99);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getSystemLinksByType - returns matching links")
    void getSystemLinksByType_returnsList() {
        SystemLink link = new SystemLink();
        when(systemLinkRepository.findByLinkType("social")).thenReturn(List.of(link));

        List<SystemLink> result = systemSettingsService.getSystemLinksByType("social");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("updateSystemLink - found saves updated link")
    void updateSystemLink_found_savesUpdated() {
        SystemLink existing = new SystemLink();
        existing.setId(1);

        SystemLink update = new SystemLink();
        update.setLinkName("New Name");
        update.setLinkUrl("http://new-url.com");
        update.setIsActive(true);

        when(systemLinkRepository.findById(1)).thenReturn(Optional.of(existing));
        when(systemLinkRepository.save(existing)).thenReturn(existing);

        SystemLink result = systemSettingsService.updateSystemLink(1, update);

        assertThat(result).isNotNull();
        verify(systemLinkRepository).save(existing);
    }

    @Test
    @DisplayName("deleteSystemLink - found deletes successfully")
    void deleteSystemLink_found_deletes() {
        when(systemLinkRepository.existsById(1)).thenReturn(true);
        doNothing().when(systemLinkRepository).deleteById(1);

        systemSettingsService.deleteSystemLink(1);

        verify(systemLinkRepository).deleteById(1);
    }
}
