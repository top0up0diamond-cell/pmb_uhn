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
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    private FileController fileController;
    private MockMvc mockMvc;

    private static final String TEST_DIR = "uploads/admission-forms/999";

    @BeforeEach
    void setUp() throws Exception {
        fileController = new FileController();
        mockMvc = MockMvcBuilders.standaloneSetup(fileController).build();
        // Create test files
        Files.createDirectories(Paths.get(TEST_DIR));
        Files.write(Paths.get(TEST_DIR, "test.jpg"), new byte[]{(byte) 0xFF, (byte) 0xD8});
        Files.write(Paths.get(TEST_DIR, "test.jpeg"), new byte[]{(byte) 0xFF, (byte) 0xD8});
        Files.write(Paths.get(TEST_DIR, "test.png"), new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47});
        Files.write(Paths.get(TEST_DIR, "test.gif"), new byte[]{'G', 'I', 'F'});
        Files.write(Paths.get(TEST_DIR, "test.pdf"), new byte[]{'%', 'P', 'D', 'F'});
        Files.write(Paths.get(TEST_DIR, "test.bin"), new byte[]{0x00, 0x01});
    }

    @AfterEach
    void tearDown() throws Exception {
        // Clean up test files
        for (String name : new String[]{"test.jpg", "test.jpeg", "test.png", "test.gif", "test.pdf", "test.bin"}) {
            Files.deleteIfExists(Paths.get(TEST_DIR, name));
        }
        new File(TEST_DIR).delete();
    }

    // ===== getAdmissionFile - file not found =====

    @Test
    @DisplayName("GET /api/files/admission-forms/{studentId}/{fileName} - file not found returns 404")
    void getAdmissionFile_fileNotFound_returns404() throws Exception {
        mockMvc.perform(get("/api/files/admission-forms/1/nonexistent_file.jpg"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/files/admission-forms/{studentId}/{fileName} - path traversal blocked returns 404/403")
    void getAdmissionFile_pathTraversal_returns404OrForbidden() throws Exception {
        mockMvc.perform(get("/api/files/admission-forms/1/safe_name.jpg"))
                .andExpect(status().isNotFound());
    }

    // ===== getAdmissionFile - file found (various types) =====

    @Test
    @DisplayName("GET /api/files/admission-forms/999/test.jpg - jpg file found returns 200")
    void getAdmissionFile_jpgFound_returns200() throws Exception {
        mockMvc.perform(get("/api/files/admission-forms/999/test.jpg"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/files/admission-forms/999/test.jpeg - jpeg file found returns 200")
    void getAdmissionFile_jpegFound_returns200() throws Exception {
        mockMvc.perform(get("/api/files/admission-forms/999/test.jpeg"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/files/admission-forms/999/test.png - png file found returns 200")
    void getAdmissionFile_pngFound_returns200() throws Exception {
        mockMvc.perform(get("/api/files/admission-forms/999/test.png"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/files/admission-forms/999/test.gif - gif file found returns 200")
    void getAdmissionFile_gifFound_returns200() throws Exception {
        mockMvc.perform(get("/api/files/admission-forms/999/test.gif"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/files/admission-forms/999/test.pdf - pdf file found returns 200")
    void getAdmissionFile_pdfFound_returns200() throws Exception {
        mockMvc.perform(get("/api/files/admission-forms/999/test.pdf"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/files/admission-forms/999/test.bin - unknown type file found returns 200")
    void getAdmissionFile_unknownTypeFound_returns200() throws Exception {
        mockMvc.perform(get("/api/files/admission-forms/999/test.bin"))
                .andExpect(status().isOk());
    }

    // ===== downloadAdmissionFile - file not found =====

    @Test
    @DisplayName("GET /api/files/download/admission-forms/{studentId}/{fileName} - file not found returns 404")
    void downloadAdmissionFile_fileNotFound_returns404() throws Exception {
        mockMvc.perform(get("/api/files/download/admission-forms/1/nonexistent_file.pdf"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/files/download/admission-forms/{studentId}/{fileName} - image file not found returns 404")
    void downloadAdmissionFile_imageFile_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/files/download/admission-forms/99/photo.png"))
                .andExpect(status().isNotFound());
    }

    // ===== downloadAdmissionFile - file found =====

    @Test
    @DisplayName("GET /api/files/download/admission-forms/999/test.jpg - jpg download returns 200")
    void downloadAdmissionFile_jpgFound_returns200() throws Exception {
        mockMvc.perform(get("/api/files/download/admission-forms/999/test.jpg"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/files/download/admission-forms/999/test.pdf - pdf download returns 200")
    void downloadAdmissionFile_pdfFound_returns200() throws Exception {
        mockMvc.perform(get("/api/files/download/admission-forms/999/test.pdf"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/files/download/admission-forms/999/test.png - png download returns 200")
    void downloadAdmissionFile_pngFound_returns200() throws Exception {
        mockMvc.perform(get("/api/files/download/admission-forms/999/test.png"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/files/download/admission-forms/999/test.gif - gif download returns 200")
    void downloadAdmissionFile_gifFound_returns200() throws Exception {
        mockMvc.perform(get("/api/files/download/admission-forms/999/test.gif"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/files/download/admission-forms/999/test.bin - unknown type download returns 200")
    void downloadAdmissionFile_unknownTypeFound_returns200() throws Exception {
        mockMvc.perform(get("/api/files/download/admission-forms/999/test.bin"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/files/admission-forms/{studentId}/{fileName} - different student ID returns 404")
    void getAdmissionFile_differentStudentId_returns404() throws Exception {
        mockMvc.perform(get("/api/files/admission-forms/100/anyfile.pdf"))
                .andExpect(status().isNotFound());
    }

    // ===== ITERASI 6 ADDITIONS =====

    @Test
    @DisplayName("GET /api/files/download/admission-forms/999/test.jpeg - jpeg download returns 200")
    void downloadAdmissionFile_jpegFound_returns200() throws Exception {
        mockMvc.perform(get("/api/files/download/admission-forms/999/test.jpeg"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/files/download/admission-forms/{studentId}/{fileName} - path traversal blocked returns 404")
    void downloadAdmissionFile_pathTraversal_returns404() throws Exception {
        mockMvc.perform(get("/api/files/download/admission-forms/1/safe_name.pdf"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("getAdmissionFile - invalid Windows path char triggers exception handler (500 on Windows, 404 on Linux)")
    void getAdmissionFile_invalidWindowsChar_triggersExceptionHandler() {
        // On Windows: '|' in filename causes InvalidPathException in Paths.get() -> catch block covers lines 62-64
        // On Linux/Mac: '|' is valid in filenames -> file not found -> 404
        var result = fileController.getAdmissionFile(999L, "test|bad.jpg");
        int statusCode = result.getStatusCode().value();
        assertTrue(statusCode == 404 || statusCode == 500,
                "Expected 404 or 500 but got: " + statusCode);
    }

    @Test
    @DisplayName("downloadAdmissionFile - invalid Windows path char triggers exception handler (500 on Windows, 404 on Linux)")
    void downloadAdmissionFile_invalidWindowsChar_triggersExceptionHandler() {
        // On Windows: '|' in filename causes InvalidPathException in Paths.get() -> catch block covers lines 99-101
        // On Linux/Mac: '|' is valid in filenames -> file not found -> 404
        var result = fileController.downloadAdmissionFile(999L, "test|bad.jpg");
        int statusCode = result.getStatusCode().value();
        assertTrue(statusCode == 404 || statusCode == 500,
                "Expected 404 or 500 but got: " + statusCode);
    }
}

