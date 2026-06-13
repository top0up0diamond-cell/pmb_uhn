# PANDUAN WHITE-BOX TESTING – ITERASI 2
# Modul Pendukung dan Administrasi
# Untuk: GitHub Copilot / AI Assistant

---

## KONTEKS ITERASI 2

Iterasi kedua berfokus pada pengembangan fitur pendukung serta modul administrasi sistem. Modul yang dikembangkan:
1. Manajemen system links dan informasi kontak
2. Modul pengumuman (CRUD)
3. Sistem token ujian (generate dan validasi)
4. Registration status tracker multi-tahap
5. Halaman ekspor data (formulir, daftar ulang, hasil akhir)
6. Fitur customer service (chat widget)
7. Manajemen akun admin dan pengguna
8. Bulk download PDF dan bulk initialize data
9. Komponen UI bersama (header, footer, dan elemen antarmuka lainnya)
10. Background task pengecekan pembayaran dan publikasi hasil otomatis

Target: minimal 70% line coverage dan branch coverage pada modul pendukung dan administrasi.

---

## KONFIGURASI includes pom.xml — GANTI UNTUK ITERASI 2

Bagian lain di pom.xml JANGAN diubah. Hanya ganti isi `<includes>`:

```xml
<includes>
    <include>com/uhn/pmb/controller/AdminController.class</include>
    <include>com/uhn/pmb/controller/AdminAnnouncementController.class</include>
    <include>com/uhn/pmb/controller/AdminUserSettingsController.class</include>
    <include>com/uhn/pmb/controller/AdminExportController.class</include>
    <include>com/uhn/pmb/controller/AdminMessagingController.class</include>
    <include>com/uhn/pmb/controller/AdminPageController.class</include>
    <include>com/uhn/pmb/controller/AdminPeriodController.class</include>
    <include>com/uhn/pmb/controller/CamabaMessagingController.class</include>
    <include>com/uhn/pmb/controller/ExamTokenController.class</include>
    <include>com/uhn/pmb/controller/PublicApiController.class</include>
    <include>com/uhn/pmb/controller/PublicSettingsController.class</include>
    <include>com/uhn/pmb/controller/RegistrationStatusController.class</include>
    <include>com/uhn/pmb/controller/SystemSettingsController.class</include>
    <include>com/uhn/pmb/service/AnnouncementService.class</include>
    <include>com/uhn/pmb/service/AdminMessagingService.class</include>
    <include>com/uhn/pmb/service/AdminUserSettingsService.class</include>
    <include>com/uhn/pmb/service/AdminDataExportService.class</include>
    <include>com/uhn/pmb/service/ExamTokenService.class</include>
    <include>com/uhn/pmb/service/RegistrationStatusService.class</include>
    <include>com/uhn/pmb/service/SystemSettingsService.class</include>
    <include>com/uhn/pmb/service/PublicDataService.class</include>
    <include>com/uhn/pmb/service/PeriodManagementService.class</include>
    <include>com/uhn/pmb/service/ValidationStatusTrackerService.class</include>
    <include>com/uhn/pmb/task/PaymentCheckTask.class</include>
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

---

## ATURAN WAJIB UNTUK SEMUA TEST

1. JANGAN pakai `@SpringBootTest` untuk unit test service
2. GUNAKAN `@ExtendWith(MockitoExtension.class)` untuk service test
3. GUNAKAN `@InjectMocks` untuk class yang ditest (objek ASLI)
4. GUNAKAN `@Mock` untuk semua dependency
5. JANGAN buat test yang hanya `assertNotNull(service)` — tidak menghasilkan coverage
6. SETIAP test HARUS memanggil method production code
7. TARGET minimal 3 test per method: happy path, edge case, error case
8. GUNAKAN `ReflectionTestUtils.setField()` untuk inject `@Value` fields

---

## LOKASI FILE TEST ITERASI 2

```
src/test/java/com/uhn/pmb/
├── service/
│   ├── AnnouncementServiceTest.java          ← DARI FILE TEST DAN SESUAI NAMA
│   ├── AdminMessagingServiceTest.java        ← DARI FILE TEST DAN SESUAI NAMA
│   ├── AdminUserSettingsServiceTest.java     ← DARI FILE TEST DAN SESUAI NAMA
│   ├── AdminDataExportServiceTest.java       ← DARI FILE TEST DAN SESUAI NAMA
│   ├── ExamTokenServiceTest.java             ← DARI FILE TEST DAN SESUAI NAMA
│   ├── RegistrationStatusServiceTest.java    ← DARI FILE TEST DAN SESUAI NAMA
│   ├── SystemSettingsServiceTest.java        ← DARI FILE TEST DAN SESUAI NAMA
│   ├── PublicDataServiceTest.java            ← DARI FILE TEST DAN SESUAI NAMA
│   ├── PeriodManagementServiceTest.java      ← DARI FILE TEST DAN SESUAI NAMA
│   └── ValidationStatusTrackerServiceTest.java ← DARI FILE TEST DAN SESUAI NAMA
├── controller/
│   ├── AdminControllerTest.java              ← DARI FILE TEST DAN SESUAI NAMA
│   ├── AdminAnnouncementControllerTest.java  ← DARI FILE TEST DAN SESUAI NAMA
│   ├── AdminUserSettingsControllerTest.java  ← DARI FILE TEST DAN SESUAI NAMA
│   ├── AdminExportControllerTest.java        ← DARI FILE TEST DAN SESUAI NAMA
│   ├── AdminMessagingControllerTest.java     ← DARI FILE TEST DAN SESUAI NAMA
│   ├── AdminPageControllerTest.java          ← DARI FILE TEST DAN SESUAI NAMA
│   ├── AdminPeriodControllerTest.java        ← DARI FILE TEST DAN SESUAI NAMA
│   ├── CamabaMessagingControllerTest.java    ← DARI FILE TEST DAN SESUAI NAMA
│   ├── ExamTokenControllerTest.java          ← DARI FILE TEST DAN SESUAI NAMA
│   ├── PublicApiControllerTest.java          ← DARI FILE TEST DAN SESUAI NAMA
│   ├── PublicSettingsControllerTest.java     ← DARI FILE TEST DAN SESUAI NAMA
│   ├── RegistrationStatusControllerTest.java ← DARI FILE TEST DAN SESUAI NAMA
│   └── SystemSettingsControllerTest.java     ← DARI FILE TEST DAN SESUAI NAMA
└── task/
    └── PaymentCheckTaskTest.java             ← DARI FILE TEST DAN SESUAI NAMA
```

---

## CHECKLIST SEBELUM MEMBUAT TEST

- [ ] Baca source code class yang akan ditest
- [ ] List semua public method
- [ ] Identifikasi setiap kondisi if/else
- [ ] Identifikasi exception yang bisa dilempar
- [ ] List semua dependency yang perlu di-mock
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