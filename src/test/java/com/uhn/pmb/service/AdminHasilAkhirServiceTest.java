package com.uhn.pmb.service;

import com.uhn.pmb.entity.HasilAkhir;
import com.uhn.pmb.entity.Student;
import com.uhn.pmb.entity.User;
import com.uhn.pmb.repository.HasilAkhirRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminHasilAkhirServiceTest {

    @Mock private HasilAkhirRepository hasilAkhirRepository;

    private AdminHasilAkhirService adminHasilAkhirService;

    @BeforeEach
    void setUp() {
        adminHasilAkhirService = new AdminHasilAkhirService(hasilAkhirRepository);
    }

    private HasilAkhir buildHasilAkhir(Long id) {
        User user = User.builder().id(1L).email("student@test.com").build();
        Student student = Student.builder().id(10L).fullName("Budi").user(user).build();
        HasilAkhir ha = new HasilAkhir();
        ha.setId(id);
        ha.setStudent(student);
        ha.setNomorRegistrasi("REG-001");
        ha.setProgramStudiName("Informatika");
        ha.setStatus(HasilAkhir.HasilAkhirStatus.ACTIVE);
        return ha;
    }

    // ===== uploadDokumenSementara =====

    @Test
    @DisplayName("uploadDokumenSementara - not found throws RuntimeException")
    void uploadDokumenSementara_notFound_throws() {
        when(hasilAkhirRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminHasilAkhirService.uploadDokumenSementara(99L, null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("tidak ditemukan");
    }

    @Test
    @DisplayName("uploadDokumenSementara - both files null throws IllegalArgumentException")
    void uploadDokumenSementara_bothFilesNull_throws() {
        when(hasilAkhirRepository.findById(1L)).thenReturn(Optional.of(buildHasilAkhir(1L)));

        assertThatThrownBy(() -> adminHasilAkhirService.uploadDokumenSementara(1L, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Minimal satu file");
    }

    @Test
    @DisplayName("uploadDokumenSementara - NPM non-PDF throws IllegalArgumentException")
    void uploadDokumenSementara_npmNonPdf_throws() {
        when(hasilAkhirRepository.findById(1L)).thenReturn(Optional.of(buildHasilAkhir(1L)));

        MockMultipartFile npmFile = new MockMultipartFile(
                "npm", "npm.jpg", "image/jpeg", "data".getBytes());

        assertThatThrownBy(() -> adminHasilAkhirService.uploadDokumenSementara(1L, npmFile, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NPM Sementara harus berformat PDF");
    }

    @Test
    @DisplayName("uploadDokumenSementara - KTM non-PDF throws IllegalArgumentException")
    void uploadDokumenSementara_ktmNonPdf_throws() {
        when(hasilAkhirRepository.findById(1L)).thenReturn(Optional.of(buildHasilAkhir(1L)));

        MockMultipartFile ktmFile = new MockMultipartFile(
                "ktm", "ktm.docx", "application/octet-stream", "data".getBytes());

        assertThatThrownBy(() -> adminHasilAkhirService.uploadDokumenSementara(1L, null, ktmFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("KTM Sementara harus berformat PDF");
    }

    @Test
    @DisplayName("uploadDokumenSementara - valid NPM PDF uploads and saves")
    void uploadDokumenSementara_validNpmPdf_uploadsSuccessfully() throws Exception {
        HasilAkhir ha = buildHasilAkhir(1L);
        when(hasilAkhirRepository.findById(1L)).thenReturn(Optional.of(ha));
        when(hasilAkhirRepository.save(any())).thenReturn(ha);

        MockMultipartFile npmFile = new MockMultipartFile(
                "npm", "npm_sementara.pdf", "application/pdf", "pdf-content".getBytes());

        Map<String, Object> result = adminHasilAkhirService.uploadDokumenSementara(1L, npmFile, null);

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("message").toString()).contains("berhasil");
        verify(hasilAkhirRepository).save(ha);
    }

    @Test
    @DisplayName("uploadDokumenSementara - valid KTM PDF uploads and saves")
    void uploadDokumenSementara_validKtmPdf_uploadsSuccessfully() throws Exception {
        HasilAkhir ha = buildHasilAkhir(1L);
        when(hasilAkhirRepository.findById(1L)).thenReturn(Optional.of(ha));
        when(hasilAkhirRepository.save(any())).thenReturn(ha);

        MockMultipartFile ktmFile = new MockMultipartFile(
                "ktm", "ktm_sementara.pdf", "application/pdf", "pdf-content".getBytes());

        Map<String, Object> result = adminHasilAkhirService.uploadDokumenSementara(1L, null, ktmFile);

        assertThat(result.get("success")).isEqualTo(true);
        verify(hasilAkhirRepository).save(ha);
    }

    @Test
    @DisplayName("uploadDokumenSementara - both valid PDF files uploads both")
    void uploadDokumenSementara_bothValidPdfs_uploadsBoth() throws Exception {
        HasilAkhir ha = buildHasilAkhir(1L);
        when(hasilAkhirRepository.findById(1L)).thenReturn(Optional.of(ha));
        when(hasilAkhirRepository.save(any())).thenReturn(ha);

        MockMultipartFile npmFile = new MockMultipartFile(
                "npm", "npm.pdf", "application/pdf", "pdf".getBytes());
        MockMultipartFile ktmFile = new MockMultipartFile(
                "ktm", "ktm.pdf", "application/pdf", "pdf".getBytes());

        Map<String, Object> result = adminHasilAkhirService.uploadDokumenSementara(1L, npmFile, ktmFile);

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(ha.getNpmSementaraFile()).isNotNull();
        assertThat(ha.getKtmSementaraFile()).isNotNull();
    }

    @Test
    @DisplayName("uploadDokumenSementara - empty npm file with valid ktm uploads only ktm")
    void uploadDokumenSementara_emptyNpmValidKtm_uploadsOnlyKtm() throws Exception {
        HasilAkhir ha = buildHasilAkhir(1L);
        when(hasilAkhirRepository.findById(1L)).thenReturn(Optional.of(ha));
        when(hasilAkhirRepository.save(any())).thenReturn(ha);

        MockMultipartFile emptyNpm = new MockMultipartFile(
                "npm", "npm.pdf", "application/pdf", new byte[0]);
        MockMultipartFile ktmFile = new MockMultipartFile(
                "ktm", "ktm.pdf", "application/pdf", "pdf".getBytes());

        Map<String, Object> result = adminHasilAkhirService.uploadDokumenSementara(1L, emptyNpm, ktmFile);

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(ha.getNpmSementaraFile()).isNull();
        assertThat(ha.getKtmSementaraFile()).isNotNull();
    }

    // ===== getAllHasilAkhirForAdmin =====

    @Test
    @DisplayName("getAllHasilAkhirForAdmin - empty list returns empty")
    void getAllHasilAkhirForAdmin_emptyList_returnsEmpty() {
        when(hasilAkhirRepository.findAll()).thenReturn(List.of());

        List<Map<String, Object>> result = adminHasilAkhirService.getAllHasilAkhirForAdmin();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getAllHasilAkhirForAdmin - maps all fields correctly")
    void getAllHasilAkhirForAdmin_mapsFields() {
        HasilAkhir ha = buildHasilAkhir(1L);
        ha.setNpmSementaraFile("uploads/npm.pdf");
        ha.setKtmSementaraFile("uploads/ktm.pdf");
        when(hasilAkhirRepository.findAll()).thenReturn(List.of(ha));

        List<Map<String, Object>> result = adminHasilAkhirService.getAllHasilAkhirForAdmin();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("id")).isEqualTo(1L);
        assertThat(result.get(0).get("studentName")).isEqualTo("Budi");
        assertThat(result.get(0).get("nomorRegistrasi")).isEqualTo("REG-001");
        assertThat(result.get(0).get("programStudi")).isEqualTo("Informatika");
        assertThat(result.get(0).get("status")).isEqualTo("ACTIVE");
        assertThat(result.get(0).get("npmSementaraFile")).isEqualTo("uploads/npm.pdf");
        assertThat(result.get(0).get("ktmSementaraFile")).isEqualTo("uploads/ktm.pdf");
    }

    @Test
    @DisplayName("getAllHasilAkhirForAdmin - null file paths included as null in map")
    void getAllHasilAkhirForAdmin_nullFiles_includedAsNull() {
        HasilAkhir ha = buildHasilAkhir(2L);
        // npmSementaraFile dan ktmSementaraFile null by default
        when(hasilAkhirRepository.findAll()).thenReturn(List.of(ha));

        List<Map<String, Object>> result = adminHasilAkhirService.getAllHasilAkhirForAdmin();

        assertThat(result.get(0).get("npmSementaraFile")).isNull();
        assertThat(result.get(0).get("ktmSementaraFile")).isNull();
    }
}