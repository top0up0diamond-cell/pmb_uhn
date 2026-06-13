# PANDUAN WHITE-BOX TESTING – ITERASI 3
# Integrasi Data Eksternal dan Optimalisasi Sistem
# Untuk: GitHub Copilot / AI Assistant

---

## KONTEKS ITERASI 3

Iterasi ketiga berfokus pada integrasi data eksternal serta optimalisasi sistem secara keseluruhan. Modul yang dikembangkan:
1. Integrasi data asal sekolah melalui API eksternal (autocomplete)
2. Fitur input manual data sekolah oleh admin pusat
3. Penyimpanan dan manajemen data sekolah dalam sistem
4. Optimasi form pendaftaran dengan fallback input manual
5. Peningkatan validasi input asal sekolah
6. Optimasi proses ekspor data dan pengunduhan dokumen dalam bentuk bundle (ZIP)

Target: minimal 70% line coverage dan branch coverage. Fokus pada integrasi sistem, validasi data, serta proses ekspor dan kompresi file.

---

## KONFIGURASI includes pom.xml — GANTI UNTUK ITERASI 3

Bagian lain di pom.xml JANGAN diubah. Hanya ganti isi `<includes>`:

```xml
<includes>
    <include>com/uhn/pmb/controller/SmaController.class</include>
    <include>com/uhn/pmb/controller/CamabaRegistrationController.class</include>
    <include>com/uhn/pmb/controller/FileController.class</include>
    <include>com/uhn/pmb/controller/FileServingController.class</include>
    <include>com/uhn/pmb/service/SmaService.class</include>
    <include>com/uhn/pmb/service/CamabaRegistrationService.class</include>
    <include>com/uhn/pmb/service/AdmissionFormService.class</include>
    <include>com/uhn/pmb/service/FileStorageService.class</include>
    <include>com/uhn/pmb/service/FormValidationService.class</include>
    <include>com/uhn/pmb/service/AdminDataExportService.class</include>
</includes>
```

---

## POLA TEST YANG BENAR (WAJIB DIIKUTI)

### Pola untuk Service:

```java
@ExtendWith(MockitoExtension.class)
class NamaServiceTest {

    @Mock
    private DependencyRepository dependencyRepository;

    @InjectMocks
    private NamaService namaService;

    @Test
    @DisplayName("deskripsi test yang jelas")
    void namaMethod_kondisi_hasilYangDiharapkan() {
        // Arrange
        when(dependencyRepository.findAll()).thenReturn(List.of(someObject));

        // Act
        List<HasilDto> result = namaService.getAll();

        // Assert
        assertThat(result).isNotEmpty();
        verify(dependencyRepository).findAll();
    }
}
```

### Pola untuk Controller:

```java
@WebMvcTest(NamaController.class)
class NamaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NamaService namaService;

    @Test
    void endpoint_kondisi_httpStatus() throws Exception {
        when(namaService.getData()).thenReturn(someData);

        mockMvc.perform(get("/api/endpoint")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
```

### Pola untuk Service yang memanggil API eksternal:

```java
@ExtendWith(MockitoExtension.class)
class SmaServiceTest {

    @Mock
    private SmaRepository smaRepository;

    @Mock
    private RestTemplate restTemplate; // mock HTTP client kalau ada

    @InjectMocks
    private SmaService smaService;

    @Test
    void searchSekolah_keywordValid_shouldReturnResults() {
        // Arrange
        when(smaRepository.findByNamaContainingIgnoreCase("SMA"))
                .thenReturn(List.of(new Sma("SMA Negeri 1")));

        // Act
        List<SmaDto> result = smaService.search("SMA");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNama()).contains("SMA");
    }

    @Test
    void searchSekolah_keywordKosong_shouldReturnEmpty() {
        when(smaRepository.findByNamaContainingIgnoreCase(""))
                .thenReturn(List.of());

        List<SmaDto> result = smaService.search("");

        assertThat(result).isEmpty();
    }

    @Test
    void simpanSekolah_dataValid_shouldSaveAndReturn() {
        SmaRequest request = new SmaRequest("SMA Baru", "Kota");
        Sma saved = new Sma("SMA Baru");
        when(smaRepository.save(any())).thenReturn(saved);

        SmaDto result = smaService.simpan(request);

        assertThat(result).isNotNull();
        verify(smaRepository).save(any());
    }
}
```

---

## ATURAN WAJIB UNTUK SEMUA TEST

1. JANGAN pakai `@SpringBootTest` untuk unit test service
2. GUNAKAN `@ExtendWith(MockitoExtension.class)` untuk service test
3. GUNAKAN `@InjectMocks` untuk class yang ditest (objek ASLI)
4. GUNAKAN `@Mock` untuk semua dependency termasuk HTTP client / RestTemplate
5. JANGAN buat test yang hanya `assertNotNull(service)` — tidak menghasilkan coverage
6. SETIAP test HARUS memanggil method production code
7. TARGET minimal 3 test per method: happy path, edge case, error case
8. GUNAKAN `ReflectionTestUtils.setField()` untuk inject `@Value` fields
9. Untuk service yang ada logika ZIP/export: test dengan input valid, input kosong, dan simulasi IOException

---

## LOKASI FILE TEST ITERASI 3

```
src/test/java/com/uhn/pmb/
├── service/
│   ├── SmaServiceTest.java                  ← DARI FILE TEST DAN SESUAI NAMA
│   ├── CamabaRegistrationServiceTest.java   ← DARI FILE TEST DAN SESUAI NAMA (update dari iterasi 1)
│   ├── AdmissionFormServiceTest.java        ← DARI FILE TEST DAN SESUAI NAMA (update dari iterasi 1)
│   ├── FileStorageServiceTest.java          ← DARI FILE TEST DAN SESUAI NAMA
│   ├── FormValidationServiceTest.java       ← DARI FILE TEST DAN SESUAI NAMA
│   └── AdminDataExportServiceTest.java      ← DARI FILE TEST DAN SESUAI NAMA (update dari iterasi 2)
└── controller/
    ├── SmaControllerTest.java               ← DARI FILE TEST DAN SESUAI NAMA
    ├── CamabaRegistrationControllerTest.java ← DARI FILE TEST DAN SESUAI NAMA
    ├── FileControllerTest.java              ← DARI FILE TEST DAN SESUAI NAMA
    └── FileServingControllerTest.java       ← DARI FILE TEST DAN SESUAI NAMA
```

---

## FOKUS TEST KHUSUS ITERASI 3

### SmaService — Hal yang harus ditest:
- Search sekolah dengan keyword valid → return hasil
- Search sekolah dengan keyword kosong → return empty
- Search sekolah tidak ditemukan → return empty
- Simpan data sekolah baru → berhasil disimpan
- Simpan data sekolah duplikat → throw exception
- Update data sekolah → berhasil diupdate
- Delete data sekolah → berhasil dihapus

### AdmissionFormService — Fokus pada validasi fallback:
- Input sekolah dari autocomplete (ada di database) → valid
- Input sekolah manual (tidak ada di database) → tetap valid dengan flag manual
- Input sekolah kosong → throw ValidationException

### AdminDataExportService — Fokus pada export/ZIP:
- Export data dengan list tidak kosong → file berhasil dibuat
- Export data dengan list kosong → return file kosong atau throw exception
- Export ZIP dengan multiple file → semua file masuk ZIP

---

## CHECKLIST SEBELUM MEMBUAT TEST

- [ ] Baca source code class yang akan ditest
- [ ] List semua public method
- [ ] Identifikasi setiap kondisi if/else termasuk validasi input
- [ ] Identifikasi exception yang bisa dilempar (termasuk IOException)
- [ ] List semua dependency yang perlu di-mock (termasuk RestTemplate jika ada)
- [ ] Cek apakah ada @Value yang perlu ReflectionTestUtils
- [ ] Kalau nama kelas tidak ditemukan di project, skip dan lanjut ke kelas berikutnya

---

## CARA MENJALANKAN

```bash
mvn clean test
```

Laporan di: `target/site/jacoco/index.html`

Screenshot kolom: Element | Instructions Coverage | Branch Coverage | Method Coverage

lalu simpan nilai nilai nya dalam bentuk markdown baru hasiliterasi-1-whitebox.md, bentuk isi dari markdown ,sesuai dengan template templatehasiliterasiyangdiharapkan.md