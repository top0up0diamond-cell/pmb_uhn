package com.uhn.pmb.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @InjectMocks
    private FileStorageService fileStorageService;

    // ===== saveFile =====

    @Test
    @DisplayName("saveFile - null file returns null")
    void saveFile_nullFile_returnsNull() throws Exception {
        String result = fileStorageService.saveFile(null, "admission-forms", 1L, "photo");
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("saveFile - empty file returns null")
    void saveFile_emptyFile_returnsNull() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);

        String result = fileStorageService.saveFile(file, "admission-forms", 1L, "photo");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("saveFile - valid file saves and returns relative path")
    void saveFile_validFile_returnsRelativePath() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", "fake-image-content".getBytes());

        String result = fileStorageService.saveFile(file, "test-dir", 99L, "photoId");

        try {
            assertThat(result).isNotNull();
            assertThat(result).contains("uploads/test-dir/99/");
            assertThat(result).contains("photoId_");
        } finally {
            // cleanup created test directory
            Path testDir = Path.of(System.getProperty("user.dir"), "uploads", "test-dir", "99");
            if (Files.exists(testDir)) {
                Files.list(testDir).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception ignore) {}
                });
                Files.deleteIfExists(testDir);
            }
        }
    }

    @Test
    @DisplayName("saveFile - file without extension uses .bin")
    void saveFile_fileWithoutExtension_usesBinExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "noextension", "application/octet-stream", "data".getBytes());

        String result = fileStorageService.saveFile(file, "test-dir2", 100L, "doc");

        try {
            assertThat(result).isNotNull();
            assertThat(result).endsWith(".bin");
        } finally {
            Path testDir = Path.of(System.getProperty("user.dir"), "uploads", "test-dir2", "100");
            if (Files.exists(testDir)) {
                Files.list(testDir).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception ignore) {}
                });
                Files.deleteIfExists(testDir);
            }
        }
    }

    // ===== convertPathToUrl =====

    @Test
    @DisplayName("convertPathToUrl - null input returns null")
    void convertPathToUrl_null_returnsNull() {
        String result = fileStorageService.convertPathToUrl(null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("convertPathToUrl - empty input returns null")
    void convertPathToUrl_empty_returnsNull() {
        String result = fileStorageService.convertPathToUrl("");
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("convertPathToUrl - absolute path with /uploads/ returns relative")
    void convertPathToUrl_absoluteWithUploads_returnsRelative() {
        String absolutePath = "/home/app/uploads/admission-forms/1/photo.jpg";

        String result = fileStorageService.convertPathToUrl(absolutePath);

        assertThat(result).isEqualTo("/uploads/admission-forms/1/photo.jpg");
    }

    @Test
    @DisplayName("convertPathToUrl - Windows path with backslashes normalizes")
    void convertPathToUrl_windowsPath_normalizes() {
        String windowsPath = "C:\\app\\uploads\\admission-forms\\1\\photo.jpg";

        String result = fileStorageService.convertPathToUrl(windowsPath);

        assertThat(result).contains("/uploads/");
    }

    @Test
    @DisplayName("convertPathToUrl - path starting with uploads/ gets / prefix")
    void convertPathToUrl_relativeUploads_getsPrefix() {
        String relativePath = "uploads/admission-forms/1/photo.jpg";

        String result = fileStorageService.convertPathToUrl(relativePath);

        assertThat(result).startsWith("/");
        assertThat(result).contains("uploads/");
    }

    @Test
    @DisplayName("convertPathToUrl - path without uploads/ returns with slash prefix")
    void convertPathToUrl_pathWithoutUploads_returnsWithSlash() {
        String path = "some/other/path/file.jpg";

        String result = fileStorageService.convertPathToUrl(path);

        assertThat(result).startsWith("/");
    }
}
