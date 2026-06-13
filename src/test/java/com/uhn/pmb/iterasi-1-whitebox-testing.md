# PANDUAN WHITE-BOX TESTING – ITERASI 1
# Modul Inti Sistem
# Untuk: GitHub Copilot / AI Assistant

---

## KONTEKS ITERASI 1

Iterasi pertama berfokus pada pembangunan alur utama sistem PMB. Modul yang dikembangkan:
1. Halaman pemilihan gelombang dan jenis seleksi/formula
2. Halaman pemilihan metode pembayaran (Virtual Account dan upload manual)
3. Halaman pembayaran BRIVA dan upload bukti pembayaran manual
4. Halaman pembayaran cicilan mahasiswa
5. Pengisian dan pengiriman formulir pendaftaran
6. Halaman revisi formulir setelah penolakan admin
7. Halaman profil mahasiswa dan ubah password
8. Mode ujian online (Google Form) dan offline (upload bukti)
9. Konfigurasi link ujian online/offline oleh admin
10. Verifikasi pembayaran manual dan approval cicilan oleh admin

Target: minimal 70% line coverage dan branch coverage pada modul inti.

---

## KONFIGURASI pom.xml (SUDAH FINAL — JANGAN DIUBAH)

Pastikan 2 plugin ini ada persis seperti ini:

```xml
<!-- Plugin 1: Maven Surefire -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.5</version>
    <configuration>
        <argLine>
            ${argLine}
            --add-opens java.base/java.lang=ALL-UNNAMED
            --add-opens java.base/java.util=ALL-UNNAMED
            --add-opens java.base/java.lang.reflect=ALL-UNNAMED
        </argLine>
        <useSystemClassLoader>false</useSystemClassLoader>
    </configuration>
</plugin>

<!-- Plugin 2: JaCoCo -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals><goal>report</goal></goals>
        </execution>
    </executions>
    <configuration>
        <!-- TIDAK ADA <propertyName> di sini — ini penting! -->
        <includes>
            <include>com/uhn/pmb/security/JwtTokenProvider.class</include>
            <include>com/uhn/pmb/security/JwtAuthenticationFilter.class</include>
            <include>com/uhn/pmb/security/JwtAuthenticationEntryPoint.class</include>
            <include>com/uhn/pmb/controller/AuthController.class</include>
            <include>com/uhn/pmb/controller/StudentController.class</include>
            <include>com/uhn/pmb/controller/CamabaController.class</include>
            <include>com/uhn/pmb/controller/CamabaPaymentController.class</include>
            <include>com/uhn/pmb/controller/CamabaFormController.class</include>
            <include>com/uhn/pmb/controller/CamabaExamController.class</include>
            <include>com/uhn/pmb/controller/CamabaProfileController.class</include>
            <include>com/uhn/pmb/controller/CicilanRequestController.class</include>
            <include>com/uhn/pmb/controller/AdminCicilanController.class</include>
            <include>com/uhn/pmb/controller/AdminUjianLinkController.class</include>
            <include>com/uhn/pmb/controller/AdminValidationController.class</include>
            <include>com/uhn/pmb/service/AuthService.class</include>
            <include>com/uhn/pmb/service/StudentService.class</include>
            <include>com/uhn/pmb/service/BrivaService.class</include>
            <include>com/uhn/pmb/service/CicilanService.class</include>
            <include>com/uhn/pmb/service/ExamService.class</include>
            <include>com/uhn/pmb/service/AdmissionFormService.class</include>
            <include>com/uhn/pmb/service/AdminUjianLinkService.class</include>
            <include>com/uhn/pmb/service/AdminCicilanService.class</include>
            <include>com/uhn/pmb/service/CamabaPaymentService.class</include>
            <include>com/uhn/pmb/service/CamabaExamService.class</include>
            <include>com/uhn/pmb/service/CamabaRegistrationService.class</include>
        </includes>
    </configuration>
</plugin>
```

**ATURAN WAJIB:**
- JANGAN tambahkan `<propertyName>` di dalam JaCoCo configuration
- Surefire HARUS pakai `${argLine}` bukan `${jacoco.agent.argLine}`
- Kedua nama harus sama: `argLine`

---

## POLA TEST YANG BENAR (WAJIB DIIKUTI)

### Pola untuk Service:

```java
@ExtendWith(MockitoExtension.class)
class NamaServiceTest {

    @Mock
    private DependencyRepository dependencyRepository;

    @InjectMocks
    private NamaService namaService; // objek ASLI, bukan mock

    @Test
    @DisplayName("deskripsi test yang jelas")
    void namaMethod_kondisi_hasilYangDiharapkan() {
        // Arrange
        when(dependencyRepository.findById(1L)).thenReturn(Optional.of(someObject));

        // Act
        HasilDto result = namaService.namaMethod(input);

        // Assert
        assertThat(result).isNotNull();
        verify(dependencyRepository).findById(1L);
    }
}
```

### Pola untuk class dengan @Value:

```java
@ExtendWith(MockitoExtension.class)
class NamaClassTest {

    @InjectMocks
    private NamaClass namaClass;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(namaClass, "namaField", "nilai");
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
9. Secret untuk HMAC512 HARUS minimal 64 karakter

---

## LOKASI FILE TEST ITERASI 1

```
src/test/java/com/uhn/pmb/
├── security/
│   ├── JwtTokenProviderTest.java            ✅ SUDAH ADA — JANGAN DIUBAH
│   ├── JwtAuthenticationFilterTest.java     ← DARI FILE TEST DAN SESUAI NAMA
│   └── JwtAuthenticationEntryPointTest.java ← DARI FILE TEST DAN SESUAI NAMA
├── service/
│   ├── AuthServiceTest.java                 ← DARI FILE TEST DAN SESUAI NAMA
│   ├── StudentServiceTest.java              ← DARI FILE TEST DAN SESUAI NAMA
│   ├── BrivaServiceTest.java                ← DARI FILE TEST DAN SESUAI NAMA
│   ├── CicilanServiceTest.java              ← DARI FILE TEST DAN SESUAI NAMA
│   ├── ExamServiceTest.java                 ← DARI FILE TEST DAN SESUAI NAMA
│   ├── AdmissionFormServiceTest.java        ← DARI FILE TEST DAN SESUAI NAMA
│   ├── AdminUjianLinkServiceTest.java       ← DARI FILE TEST DAN SESUAI NAMA
│   ├── AdminCicilanServiceTest.java         ← DARI FILE TEST DAN SESUAI NAMA
│   ├── CamabaPaymentServiceTest.java        ← DARI FILE TEST DAN SESUAI NAMA
│   ├── CamabaExamServiceTest.java           ← DARI FILE TEST DAN SESUAI NAMA
│   └── CamabaRegistrationServiceTest.java   ← DARI FILE TEST DAN SESUAI NAMA
└── controller/
    ├── AuthControllerTest.java              ← DARI FILE TEST DAN SESUAI NAMA
    ├── StudentControllerTest.java           ← DARI FILE TEST DAN SESUAI NAMA
    ├── CamabaControllerTest.java            ← DARI FILE TEST DAN SESUAI NAMA
    ├── CamabaPaymentControllerTest.java     ← DARI FILE TEST DAN SESUAI NAMA
    ├── CamabaFormControllerTest.java        ← DARI FILE TEST DAN SESUAI NAMA
    ├── CamabaExamControllerTest.java        ← DARI FILE TEST DAN SESUAI NAMA
    ├── CamabaProfileControllerTest.java     ← DARI FILE TEST DAN SESUAI NAMA
    ├── CicilanRequestControllerTest.java    ← DARI FILE TEST DAN SESUAI NAMA
    ├── AdminCicilanControllerTest.java      ← DARI FILE TEST DAN SESUAI NAMA
    ├── AdminUjianLinkControllerTest.java    ← DARI FILE TEST DAN SESUAI NAMA
    └── AdminValidationControllerTest.java   ← DARI FILE TEST DAN SESUAI NAMA
```


---

## CHECKLIST SEBELUM MEMBUAT TEST

- [ ] Baca source code class yang akan ditest
- [ ] List semua public method
- [ ] Identifikasi setiap kondisi if/else
- [ ] Identifikasi exception yang bisa dilempar
- [ ] List semua dependency yang perlu di-mock
- [ ] Cek apakah ada @Value yang perlu ReflectionTestUtils

---

## CARA MENJALANKAN

```bash
mvn clean test
```

Laporan di: `target/site/jacoco/index.html`

Screenshot kolom: Element | Instructions Coverage | Branch Coverage | Method Coverage

lalu simpan nilai nilai nya dalam bentuk markdown baru hasiliterasi-1-whitebox.md, bentuk isi dari markdown ,sesuai dengan template templatehasiliterasiyangdiharapkan.md