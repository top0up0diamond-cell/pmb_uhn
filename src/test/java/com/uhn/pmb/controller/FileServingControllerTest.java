package com.uhn.pmb.controller;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FileServingControllerTest {

    private MockMvc mockMvc;

    private static final String TEST_DIR = "uploads/serving-test/1";

    @BeforeEach
    void setUp() throws Exception {
        FileServingController controller = new FileServingController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        // Create test files for file-found tests
        Files.createDirectories(Paths.get(TEST_DIR));
        Files.write(Paths.get(TEST_DIR, "photo.jpg"), new byte[]{(byte) 0xFF, (byte) 0xD8});
        Files.write(Paths.get(TEST_DIR, "photo.jpeg"), new byte[]{(byte) 0xFF, (byte) 0xD8});
        Files.write(Paths.get(TEST_DIR, "photo.png"), new byte[]{(byte) 0x89, 0x50});
        Files.write(Paths.get(TEST_DIR, "photo.gif"), new byte[]{'G', 'I', 'F'});
        Files.write(Paths.get(TEST_DIR, "photo.webp"), new byte[]{'R', 'I', 'F', 'F'});
        Files.write(Paths.get(TEST_DIR, "doc.pdf"), new byte[]{'%', 'P', 'D', 'F'});
        Files.write(Paths.get(TEST_DIR, "doc.bin"), new byte[]{0x00, 0x01});
    }

    @AfterEach
    void tearDown() throws Exception {
        for (String name : new String[]{"photo.jpg", "photo.jpeg", "photo.png", "photo.gif", "photo.webp", "doc.pdf", "doc.bin"}) {
            Files.deleteIfExists(Paths.get(TEST_DIR, name));
        }
        new File(TEST_DIR).delete();
        new File("uploads/serving-test").delete();
    }

    // ===== viewFile - security checks =====

    @Test
    @DisplayName("GET /api/files/view/** - file not found returns 404")
    void viewFile_fileNotFound_returns404() throws Exception {
        mockMvc.perform(get("/api/files/view/uploads/admission-forms/1/nonexistent.jpg"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/files/view/** - path with '..' blocked returns 400")
    void viewFile_pathTraversalDotDot_returns400() throws Exception {
        mockMvc.perform(get("/api/files/view/../etc/passwd"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/files/view/** - with download=true file not found returns 404")
    void viewFile_downloadTrue_fileNotFound_returns404() throws Exception {
        mockMvc.perform(get("/api/files/view/uploads/admission-forms/1/nofile.pdf")
                        .param("download", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/files/view/** - download=false file not found returns 404")
    void viewFile_downloadFalse_fileNotFound_returns404() throws Exception {
        mockMvc.perform(get("/api/files/view/uploads/admission-forms/2/nopdf.pdf")
                        .param("download", "false"))
                .andExpect(status().isNotFound());
    }

    // ===== viewFile - file found (various types) =====

    @Test
    @DisplayName("GET /api/files/view/uploads/serving-test/1/photo.jpg - jpg served inline returns 200")
    void viewFile_jpgFound_inline_returns200() throws Exception {
        mockMvc.perform(get("/api/files/view/uploads/serving-test/1/photo.jpg"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/files/view/uploads/serving-test/1/photo.jpg - jpg served as download returns 200")
    void viewFile_jpgFound_download_returns200() throws Exception {
        mockMvc.perform(get("/api/files/view/uploads/serving-test/1/photo.jpg")
                        .param("download", "true"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/files/view/uploads/serving-test/1/photo.jpeg - jpeg served returns 200")
    void viewFile_jpegFound_returns200() throws Exception {
        mockMvc.perform(get("/api/files/view/uploads/serving-test/1/photo.jpeg"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/files/view/uploads/serving-test/1/photo.png - png served returns 200")
    void viewFile_pngFound_returns200() throws Exception {
        mockMvc.perform(get("/api/files/view/uploads/serving-test/1/photo.png"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/files/view/uploads/serving-test/1/photo.gif - gif served returns 200")
    void viewFile_gifFound_returns200() throws Exception {
        mockMvc.perform(get("/api/files/view/uploads/serving-test/1/photo.gif"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/files/view/uploads/serving-test/1/photo.webp - webp served returns 200")
    void viewFile_webpFound_returns200() throws Exception {
        mockMvc.perform(get("/api/files/view/uploads/serving-test/1/photo.webp"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/files/view/uploads/serving-test/1/doc.pdf - pdf served returns 200")
    void viewFile_pdfFound_returns200() throws Exception {
        mockMvc.perform(get("/api/files/view/uploads/serving-test/1/doc.pdf"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/files/view/uploads/serving-test/1/doc.bin - unknown type served returns 200")
    void viewFile_unknownTypeFound_returns200() throws Exception {
        mockMvc.perform(get("/api/files/view/uploads/serving-test/1/doc.bin"))
                .andExpect(status().isOk());
    }

    // ===== showFile - security checks =====

    @Test
    @DisplayName("GET /api/files/show - path with '..' blocked returns 400")
    void showFile_pathTraversalDotDot_returns400() throws Exception {
        mockMvc.perform(get("/api/files/show").param("path", "../etc/passwd"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/files/show - path starting with '/' blocked returns 400")
    void showFile_absolutePath_returns400() throws Exception {
        mockMvc.perform(get("/api/files/show").param("path", "/etc/passwd"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/files/show - path with '~' blocked returns 400")
    void showFile_homePath_returns400() throws Exception {
        mockMvc.perform(get("/api/files/show").param("path", "~/.ssh/id_rsa"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/files/show - safe path file not found returns 404")
    void showFile_safePathNotFound_returns404() throws Exception {
        mockMvc.perform(get("/api/files/show").param("path", "uploads/nonexistent/file.jpg"))
                .andExpect(status().isNotFound());
    }

    // ===== showFile - file found =====

    @Test
    @DisplayName("GET /api/files/show - jpg file found returns 200")
    void showFile_jpgFound_returns200() throws Exception {
        mockMvc.perform(get("/api/files/show").param("path", "uploads/serving-test/1/photo.jpg"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/files/show - jpeg file found returns 200")
    void showFile_jpegFound_returns200() throws Exception {
        mockMvc.perform(get("/api/files/show").param("path", "uploads/serving-test/1/photo.jpeg"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/files/show - png file found returns 200")
    void showFile_pngFound_returns200() throws Exception {
        mockMvc.perform(get("/api/files/show").param("path", "uploads/serving-test/1/photo.png"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/files/show - gif file found returns 200")
    void showFile_gifFound_returns200() throws Exception {
        mockMvc.perform(get("/api/files/show").param("path", "uploads/serving-test/1/photo.gif"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/files/show - webp file found returns 200")
    void showFile_webpFound_returns200() throws Exception {
        mockMvc.perform(get("/api/files/show").param("path", "uploads/serving-test/1/photo.webp"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/files/show - pdf file found returns 200")
    void showFile_pdfFound_returns200() throws Exception {
        mockMvc.perform(get("/api/files/show").param("path", "uploads/serving-test/1/doc.pdf"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/files/show - unknown type file found returns 200")
    void showFile_unknownTypeFound_returns200() throws Exception {
        mockMvc.perform(get("/api/files/show").param("path", "uploads/serving-test/1/doc.bin"))
                .andExpect(status().isOk());
    }

    // ===== redirectUploads =====

    @Test
    @DisplayName("GET /api/files - redirectUploads handler is reachable")
    void redirectUploads_returns200() throws Exception {
        mockMvc.perform(get("/api/files"))
                .andExpect(status().isOk());
    }
}

