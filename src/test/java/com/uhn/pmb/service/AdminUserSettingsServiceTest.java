package com.uhn.pmb.service;

import com.uhn.pmb.entity.SystemConfiguration;
import com.uhn.pmb.entity.User;
import com.uhn.pmb.repository.SystemConfigurationRepository;
import com.uhn.pmb.repository.UserRepository;
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
class AdminUserSettingsServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private SystemConfigurationRepository systemConfigRepository;

    @InjectMocks
    private AdminUserSettingsService adminUserSettingsService;

    @Test
    @DisplayName("getAllUsers - returns mapped list")
    void getAllUsers_returnsList() {
        User u = User.builder().id(1L).email("a@test.com").role(User.UserRole.CAMABA).build();
        when(userRepository.findAll()).thenReturn(List.of(u));

        List<Map<String, Object>> result = adminUserSettingsService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("email")).isEqualTo("a@test.com");
    }

    @Test
    @DisplayName("getAllUsers - empty returns empty list")
    void getAllUsers_emptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<Map<String, Object>> result = adminUserSettingsService.getAllUsers();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("updateUserRole - user not found throws RuntimeException")
    void updateUserRole_userNotFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserSettingsService.updateUserRole(99L, "ADMIN_PUSAT"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("updateUserRole - null role throws RuntimeException")
    void updateUserRole_nullRole_throws() {
        User u = User.builder().id(1L).email("a@test.com").role(User.UserRole.CAMABA).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> adminUserSettingsService.updateUserRole(1L, null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("deleteUser - self deletion throws RuntimeException")
    void deleteUser_selfDeletion_throws() {
        User u = User.builder().id(1L).email("self@test.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> adminUserSettingsService.deleteUser(1L, "self@test.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("deleteUser - user not found throws RuntimeException")
    void deleteUser_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserSettingsService.deleteUser(99L, "admin@test.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getActiveSettings - returns key-value map")
    void getActiveSettings_returnsMap() {
        SystemConfiguration sc = new SystemConfiguration();
        sc.setConfigKey("key1");
        sc.setConfigValue("val1");
        when(systemConfigRepository.findByIsActive(true)).thenReturn(List.of(sc));

        Map<String, String> result = adminUserSettingsService.getActiveSettings();

        assertThat(result).containsEntry("key1", "val1");
    }

    @Test
    @DisplayName("getActiveSetting - found returns optional")
    void getActiveSetting_found_returnsOptional() {
        SystemConfiguration sc = new SystemConfiguration();
        sc.setConfigKey("gform");
        sc.setConfigValue("http://example.com");
        sc.setIsActive(true);
        when(systemConfigRepository.findByConfigKey("gform")).thenReturn(Optional.of(sc));

        Optional<SystemConfiguration> result = adminUserSettingsService.getActiveSetting("gform");

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("deleteGformLink - not found throws RuntimeException")
    void deleteGformLink_notFound_throws() {
        when(systemConfigRepository.findByConfigKey("EXAM_GFORM_LINK")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserSettingsService.deleteGformLink())
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("saveGformLink - existing updates and returns")
    void saveGformLink_existing_updates() {
        SystemConfiguration sc = new SystemConfiguration();
        sc.setConfigKey("exam_gform_link");
        sc.setConfigValue("http://old.com");
        sc.setIsActive(true);
        when(systemConfigRepository.findByConfigKey("exam_gform_link")).thenReturn(Optional.of(sc));
        when(systemConfigRepository.save(any())).thenReturn(sc);

        SystemConfiguration result = adminUserSettingsService.saveGformLink("http://new.com");

        verify(systemConfigRepository).save(any());
    }

    @Test
    @DisplayName("getGformLink - returns optional")
    void getGformLink_returnsOptional() {
        when(systemConfigRepository.findByConfigKey("exam_gform_link")).thenReturn(Optional.empty());

        Optional<SystemConfiguration> result = adminUserSettingsService.getGformLink();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("saveSetting - saves configuration")
    void saveSetting_savesConfig() {
        SystemConfiguration sc = new SystemConfiguration();
        when(systemConfigRepository.save(any())).thenReturn(sc);
        when(systemConfigRepository.findByConfigKey("key1")).thenReturn(Optional.empty());

        SystemConfiguration result = adminUserSettingsService.saveSetting("key1", "val1");

        verify(systemConfigRepository).save(any());
    }

    @Test
    @DisplayName("updateUserRole - empty role string throws RuntimeException")
    void updateUserRole_emptyRole_throws() {
        User u = User.builder().id(1L).email("a@test.com").role(User.UserRole.CAMABA).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> adminUserSettingsService.updateUserRole(1L, ""))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("updateUserRole - valid role updates user")
    void updateUserRole_validRole_updatesUser() {
        User u = User.builder().id(1L).email("a@test.com").role(User.UserRole.CAMABA).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(userRepository.save(any())).thenReturn(u);

        adminUserSettingsService.updateUserRole(1L, "ADMIN_PUSAT");

        assertThat(u.getRole()).isEqualTo(User.UserRole.ADMIN_PUSAT);
        verify(userRepository).save(u);
    }

    @Test
    @DisplayName("deleteUser - different email deletes user successfully")
    void deleteUser_differentEmail_deletesUser() {
        User u = User.builder().id(1L).email("other@test.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        doNothing().when(userRepository).delete(u);

        adminUserSettingsService.deleteUser(1L, "admin@test.com");

        verify(userRepository).delete(u);
    }

    @Test
    @DisplayName("getActiveSetting - found but inactive returns empty Optional")
    void getActiveSetting_foundButInactive_returnsEmpty() {
        SystemConfiguration sc = new SystemConfiguration();
        sc.setConfigKey("key");
        sc.setConfigValue("val");
        sc.setIsActive(false);
        when(systemConfigRepository.findByConfigKey("key")).thenReturn(Optional.of(sc));

        Optional<SystemConfiguration> result = adminUserSettingsService.getActiveSetting("key");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("saveSetting - existing config updates value")
    void saveSetting_existingConfig_updatesValue() {
        SystemConfiguration existing = new SystemConfiguration();
        existing.setConfigKey("key1");
        existing.setConfigValue("old_val");
        when(systemConfigRepository.findByConfigKey("key1")).thenReturn(Optional.of(existing));
        when(systemConfigRepository.save(any())).thenReturn(existing);

        SystemConfiguration result = adminUserSettingsService.saveSetting("key1", "new_val");

        assertThat(existing.getConfigValue()).isEqualTo("new_val");
        verify(systemConfigRepository).save(existing);
    }

    @Test
    @DisplayName("saveGformLink - no existing config creates new entry")
    void saveGformLink_noExisting_createsNew() {
        when(systemConfigRepository.findByConfigKey("exam_gform_link")).thenReturn(Optional.empty());
        SystemConfiguration newSc = new SystemConfiguration();
        when(systemConfigRepository.save(any())).thenReturn(newSc);

        SystemConfiguration result = adminUserSettingsService.saveGformLink("http://new.com");

        verify(systemConfigRepository).save(any());
    }

    @Test
    @DisplayName("saveGformLink - existing with same value does not save")
    void saveGformLink_sameValue_doesNotSave() {
        SystemConfiguration sc = new SystemConfiguration();
        sc.setConfigKey("exam_gform_link");
        sc.setConfigValue("http://same.com");
        sc.setIsActive(true);
        when(systemConfigRepository.findByConfigKey("exam_gform_link")).thenReturn(Optional.of(sc));

        SystemConfiguration result = adminUserSettingsService.saveGformLink("http://same.com");

        verify(systemConfigRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteGformLink - found config is deleted")
    void deleteGformLink_found_deletesConfig() {
        SystemConfiguration sc = new SystemConfiguration();
        sc.setConfigKey("exam_gform_link");
        sc.setConfigValue("http://example.com");
        when(systemConfigRepository.findByConfigKey("exam_gform_link")).thenReturn(Optional.of(sc));
        doNothing().when(systemConfigRepository).delete(sc);

        adminUserSettingsService.deleteGformLink();

        verify(systemConfigRepository).delete(sc);
    }
}
