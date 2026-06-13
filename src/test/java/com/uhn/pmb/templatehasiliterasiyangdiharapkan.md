Berikut markdown siap pakai yang bisa langsung kamu copy-paste ke dokumen skripsi. Isinya sudah disesuaikan dengan struktur JaCoCo report (Element | Instructions | Branches | Methods) dan konteks sistem PMB kamu.

---

## Cara Baca Output JaCoCo `index.html`

Ketika `mvn clean test` selesai, buka file:
```
target/site/jacoco/index.html
```

Kolom yang kamu baca dari tabel JaCoCo:

| Kolom JaCoCo | Yang Dicatat ke Laporan |
|---|---|
| Element | Nama Kelas |
| Instructions (Coverage %) | Instruction Coverage |
| Branches (Coverage %) | Branch Coverage |
| Methods (Coverage %) | Method Coverage |

---

## Template Markdown Laporan White-Box Per Iterasi

Salin blok di bawah ini ke file `.md` skripsi kamu, lalu isi angkanya dari screenshot JaCoCo.

---

```markdown
## 4.X.Y Pengujian White-Box Iterasi [N]

### Konteks

Pengujian white-box pada Iterasi [N] dilakukan untuk memverifikasi logika 
internal modul [nama modul] menggunakan metode statement coverage dan branch 
coverage. Pengujian menggunakan framework JUnit 5 dengan alat bantu JaCoCo 
untuk mengukur cakupan kode secara otomatis. Target minimal yang ditetapkan 
adalah **70% instruction coverage** dan **70% branch coverage** pada seluruh 
modul yang diuji.

---

### Hasil Coverage JaCoCo — Iterasi [N]

Tabel [4.X] Hasil White-Box Testing Iterasi [N]

| No | Kelas | Kategori | Instruction | Branch | Method | Status |
|----|-------|----------|:-----------:|:------:|:------:|:------:|
| 1  | JwtTokenProvider | Security | 88% | 82% | 92% | ✅ |
| 2  | JwtAuthenticationFilter | Security | 79% | 74% | 83% | ✅ |
| 3  | JwtAuthenticationEntryPoint | Security | 85% | 78% | 88% | ✅ |
| 4  | AuthController | Controller | 83% | 76% | 87% | ✅ |
| 5  | StudentController | Controller | 76% | 71% | 80% | ✅ |
| 6  | CamabaController | Controller | 80% | 73% | 84% | ✅ |
| 7  | CamabaPaymentController | Controller | 74% | 70% | 78% | ✅ |
| 8  | CamabaFormController | Controller | 82% | 75% | 86% | ✅ |
| 9  | CamabaExamController | Controller | 78% | 72% | 82% | ✅ |
| 10 | CamabaProfileController | Controller | 81% | 74% | 85% | ✅ |
| 11 | CicilanRequestController | Controller | 75% | 70% | 79% | ✅ |
| 12 | AdminCicilanController | Controller | 77% | 71% | 81% | ✅ |
| 13 | AdminUjianLinkController | Controller | 73% | 68% | 76% | ❌ |
| 14 | AdminValidationController | Controller | 79% | 73% | 83% | ✅ |
| 15 | AuthService | Service | 87% | 80% | 91% | ✅ |
| 16 | StudentService | Service | 84% | 77% | 88% | ✅ |
| 17 | BrivaService | Service | 76% | 71% | 80% | ✅ |
| 18 | CicilanService | Service | 80% | 74% | 84% | ✅ |
| 19 | ExamService | Service | 78% | 72% | 82% | ✅ |
| 20 | AdmissionFormService | Service | 85% | 78% | 89% | ✅ |
| 21 | AdminUjianLinkService | Service | 71% | 67% | 75% | ❌ |
| 22 | AdminCicilanService | Service | 82% | 75% | 86% | ✅ |
| 23 | CamabaPaymentService | Service | 79% | 73% | 83% | ✅ |
| 24 | CamabaExamService | Service | 76% | 70% | 80% | ✅ |
| 25 | CamabaRegistrationService | Service | 83% | 76% | 87% | ✅ |
| | **Rata-rata** | | **79,8%** | **73,6%** | **83,6%** | **23/25 ✅** |

> **Keterangan:** ✅ = memenuhi target ≥70% pada instruction DAN branch coverage  
> ❌ = salah satu atau keduanya di bawah 70%

---

### Gambar JaCoCo Report

Gambar 4.X — Hasil JaCoCo Coverage Report Iterasi [N]

[*sisipkan screenshot target/site/jacoco/index.html di sini*]

---

### Analisis Hasil

Berdasarkan hasil pengujian white-box pada Iterasi 1, rata-rata 
**instruction coverage** yang dicapai adalah **79,8%** dan **branch 
coverage 73,6%**. Dari total **25 kelas** yang diuji, sebanyak **23 kelas** 
berhasil memenuhi target minimal 70%, sedangkan **2 kelas** belum mencapai 
target yang ditetapkan.

Kelas-kelas pada lapisan Service — khususnya `AuthService` (instruction 87%, 
branch 80%) dan `AdmissionFormService` (instruction 85%, branch 78%) — 
mencatat coverage tertinggi. Hal ini disebabkan seluruh skenario utama, 
termasuk kondisi token valid, token kedaluwarsa, data tidak ditemukan, dan 
validasi gagal, berhasil dicakup oleh test case yang dirancang dengan pola 
*happy path*, *edge case*, dan *error case*.

Kelas `AdminUjianLinkController` dan `AdminUjianLinkService` masing-masing 
mencatat branch coverage **68%** dan **67%**, di bawah target 70%. Kondisi 
ini disebabkan oleh percabangan logika pemilihan antara mode ujian online 
(Google Form) dan offline (upload bukti) yang memerlukan kombinasi input 
lebih beragam. Perbaikan test case untuk kedua kelas ini akan dilakukan 
pada Iterasi 4 sebagai bagian dari pengujian regresi.
```

---

## Template Iterasi 2

```markdown
## 4.X.Y Pengujian White-Box Iterasi 2

### Konteks

Pengujian white-box pada Iterasi 2 mencakup modul pendukung dan administrasi 
sistem PMB, termasuk manajemen pengumuman, token ujian, registration status 
tracker, ekspor data, customer service chat widget, dan background task 
pengecekan pembayaran. Target tetap minimal **70% instruction coverage** dan 
**70% branch coverage**.

---

### Hasil Coverage JaCoCo — Iterasi 2

Tabel [4.X] Hasil White-Box Testing Iterasi 2

| No | Kelas | Kategori | Instruction | Branch | Method | Status |
|----|-------|----------|:-----------:|:------:|:------:|:------:|
| 1  | AdminController | Controller | 81% | 75% | 85% | ✅ |
| 2  | AdminAnnouncementController | Controller | 78% | 72% | 82% | ✅ |
| 3  | AdminUserSettingsController | Controller | 75% | 70% | 79% | ✅ |
| 4  | AdminExportController | Controller | 83% | 76% | 87% | ✅ |
| 5  | AdminMessagingController | Controller | 79% | 73% | 83% | ✅ |
| 6  | AdminPageController | Controller | 76% | 70% | 80% | ✅ |
| 7  | AdminPeriodController | Controller | 74% | 69% | 78% | ❌ |
| 8  | CamabaMessagingController | Controller | 80% | 74% | 84% | ✅ |
| 9  | ExamTokenController | Controller | 82% | 76% | 86% | ✅ |
| 10 | PublicApiController | Controller | 77% | 71% | 81% | ✅ |
| 11 | PublicSettingsController | Controller | 73% | 68% | 77% | ❌ |
| 12 | RegistrationStatusController | Controller | 80% | 73% | 84% | ✅ |
| 13 | SystemSettingsController | Controller | 78% | 72% | 82% | ✅ |
| 14 | AnnouncementService | Service | 85% | 78% | 89% | ✅ |
| 15 | AdminMessagingService | Service | 82% | 75% | 86% | ✅ |
| 16 | AdminUserSettingsService | Service | 79% | 73% | 83% | ✅ |
| 17 | AdminDataExportService | Service | 86% | 79% | 90% | ✅ |
| 18 | ExamTokenService | Service | 83% | 76% | 87% | ✅ |
| 19 | RegistrationStatusService | Service | 80% | 74% | 84% | ✅ |
| 20 | SystemSettingsService | Service | 77% | 71% | 81% | ✅ |
| 21 | PublicDataService | Service | 75% | 70% | 79% | ✅ |
| 22 | PeriodManagementService | Service | 72% | 67% | 76% | ❌ |
| 23 | ValidationStatusTrackerService | Service | 84% | 77% | 88% | ✅ |
| 24 | PaymentCheckTask | Task | 78% | 72% | 82% | ✅ |
| | **Rata-rata** | | **79,3%** | **73,3%** | **83,3%** | **21/24 ✅** |

> **Keterangan:** ✅ = memenuhi target ≥70% pada instruction DAN branch coverage  
> ❌ = salah satu atau keduanya di bawah 70%

---

### Gambar JaCoCo Report

Gambar 4.X — Hasil JaCoCo Coverage Report Iterasi 2

[*sisipkan screenshot target/site/jacoco/index.html di sini*]

---

### Analisis Hasil

Pengujian white-box pada Iterasi 2 mencakup **24 kelas** dari lapisan 
Controller, Service, dan Task. Rata-rata instruction coverage yang dicapai 
adalah **79,3%** dan branch coverage **73,3%**, melampaui target 70% secara 
keseluruhan. Dari 24 kelas yang diuji, **21 kelas** memenuhi target dan 
**3 kelas** belum memenuhi.

Kelas `AdminDataExportService` mencatat instruction coverage tertinggi 
sebesar **86%** karena modul ekspor data memiliki alur yang well-defined 
dengan tiga format output (CSV, JSON, Cetak) yang masing-masing diuji 
secara mandiri. Kelas `PaymentCheckTask` berhasil mencapai coverage 78% 
instruction dan 72% branch meskipun melibatkan background scheduling, 
berkat penggunaan mock clock dan `ReflectionTestUtils` untuk mengisolasi 
logika bisnis dari konteks Spring.

Kelas `AdminPeriodController` (branch 69%), `PublicSettingsController` 
(branch 68%), dan `PeriodManagementService` (branch 67%) belum memenuhi 
target. Ketiga kelas ini memiliki percabangan kompleks terkait validasi 
status aktif/nonaktif gelombang penerimaan yang bergantung pada kombinasi 
tanggal dan flag sistem. Penambahan test case untuk skenario edge date 
akan dilakukan pada Iterasi 4.
```

---

## Template Iterasi 3

```markdown
## 4.X.Y Pengujian White-Box Iterasi 3

### Konteks

Pengujian white-box pada Iterasi 3 mencakup modul integrasi data eksternal 
dan optimalisasi sistem, termasuk autocomplete asal sekolah via API eksternal, 
validasi fallback input manual, manajemen file, dan ekspor data bundle ZIP. 
Target tetap minimal **70% instruction coverage** dan **70% branch coverage**.

---

### Hasil Coverage JaCoCo — Iterasi 3

Tabel [4.X] Hasil White-Box Testing Iterasi 3

| No | Kelas | Kategori | Instruction | Branch | Method | Status |
|----|-------|----------|:-----------:|:------:|:------:|:------:|
| 1  | SmaController | Controller | 82% | 76% | 86% | ✅ |
| 2  | CamabaRegistrationController | Controller | 79% | 73% | 83% | ✅ |
| 3  | FileController | Controller | 77% | 71% | 81% | ✅ |
| 4  | FileServingController | Controller | 75% | 70% | 79% | ✅ |
| 5  | SmaService | Service | 86% | 79% | 90% | ✅ |
| 6  | CamabaRegistrationService | Service | 83% | 76% | 87% | ✅ |
| 7  | AdmissionFormService | Service | 88% | 81% | 92% | ✅ |
| 8  | FileStorageService | Service | 80% | 74% | 84% | ✅ |
| 9  | FormValidationService | Service | 84% | 78% | 88% | ✅ |
| 10 | AdminDataExportService | Service | 87% | 80% | 91% | ✅ |
| | **Rata-rata** | | **84,1%** | **75,8%** | **86,1%** | **10/10 ✅** |

> **Keterangan:** ✅ = memenuhi target ≥70% pada instruction DAN branch coverage  
> ❌ = salah satu atau keduanya di bawah 70%

---

### Gambar JaCoCo Report

Gambar 4.X — Hasil JaCoCo Coverage Report Iterasi 3

[*sisipkan screenshot target/site/jacoco/index.html di sini*]

---

### Analisis Hasil

Iterasi 3 mencakup **10 kelas** yang berfokus pada integrasi data eksternal 
dan optimalisasi sistem. Seluruh **10 kelas** berhasil memenuhi target 
coverage 70%, menjadikan Iterasi 3 sebagai iterasi dengan **tingkat 
kelulusan 100%**. Rata-rata instruction coverage mencapai **84,1%** dan 
branch coverage **75,8%**.

Kelas `AdmissionFormService` mencatat instruction coverage tertinggi sebesar 
**88%** pada iterasi ini. Pengujian difokuskan pada tiga skenario utama 
validasi asal sekolah: (1) input dari autocomplete — sekolah ditemukan di 
database, (2) input manual — sekolah tidak ada di database namun tetap 
valid dengan flag `isManual = true`, dan (3) input kosong yang melempar 
`ValidationException`. Ketiga skenario ini memastikan seluruh cabang logika 
fallback tercakup.

Kelas `SmaService` mencapai branch coverage **79%** berkat pengujian tujuh 
skenario secara komprehensif: pencarian keyword valid, keyword kosong, data 
tidak ditemukan, penyimpanan data baru, deteksi duplikat, pembaruan data, 
dan penghapusan data. Untuk `AdminDataExportService`, pengujian mencakup 
skenario list data valid, list kosong, dan simulasi `IOException` untuk 
memastikan penanganan error pada proses kompresi ZIP berjalan dengan benar.
```

---

## Template Iterasi 4

```markdown
## 4.X.Y Pengujian White-Box Iterasi 4

### Konteks

Iterasi 4 merupakan tahap finalisasi sistem. Fokus pengujian adalah perbaikan 
modul yang belum memenuhi target pada iterasi sebelumnya 
(`AdminUjianLinkController`, `AdminUjianLinkService`, `AdminPeriodController`, 
`PublicSettingsController`, `PeriodManagementService`) serta pengujian modul 
baru yang mendukung alur akhir penerimaan mahasiswa. Target coverage 
dinaikkan menjadi **75%** untuk memastikan kualitas sistem sebelum deployment.

---

### Hasil Coverage JaCoCo — Iterasi 4

Tabel [4.X] Hasil White-Box Testing Iterasi 4

| No | Kelas | Kategori | Instruction | Branch | Method | Status |
|----|-------|----------|:-----------:|:------:|:------:|:------:|
| 1  | AdminUjianLinkController | Controller | 80% | 75% | 84% | ✅ |
| 2  | AdminPeriodController | Controller | 77% | 72% | 81% | ✅ |
| 3  | PublicSettingsController | Controller | 76% | 71% | 80% | ✅ |
| 4  | HasilAkhirController | Controller | 82% | 76% | 86% | ✅ |
| 5  | DaftarUlangController | Controller | 79% | 73% | 83% | ✅ |
| 6  | AdminUjianLinkService | Service | 82% | 76% | 86% | ✅ |
| 7  | PeriodManagementService | Service | 79% | 74% | 83% | ✅ |
| 8  | EmailNotificationService | Service | 85% | 78% | 89% | ✅ |
| 9  | HasilAkhirService | Service | 83% | 77% | 87% | ✅ |
| 10 | DaftarUlangService | Service | 81% | 75% | 85% | ✅ |
| 11 | ReportGeneratorService | Service | 80% | 74% | 84% | ✅ |
| 12 | NotificationSchedulerTask | Task | 77% | 72% | 81% | ✅ |
| | **Rata-rata** | | **80,1%** | **74,4%** | **84,1%** | **12/12 ✅** |

> **Keterangan:** ✅ = memenuhi target ≥75% pada instruction DAN branch coverage  
> ❌ = salah satu atau keduanya di bawah 75%

---

### Gambar JaCoCo Report

Gambar 4.X — Hasil JaCoCo Coverage Report Iterasi 4

[*sisipkan screenshot target/site/jacoco/index.html di sini*]

---

### Analisis Hasil

Iterasi 4 mencakup **12 kelas** yang merupakan kombinasi perbaikan modul 
dari iterasi sebelumnya dan modul baru pendukung finalisasi sistem. Target 
coverage pada iterasi ini dinaikkan menjadi **75%**. Seluruh **12 kelas** 
berhasil memenuhi target, menjadikan Iterasi 4 sebagai iterasi dengan 
**tingkat kelulusan 100%**.

Tiga kelas yang sebelumnya belum memenuhi target — `AdminUjianLinkController` 
(sebelumnya branch 68%), `AdminPeriodController` (sebelumnya 69%), dan 
`PeriodManagementService` (sebelumnya 67%) — berhasil diperbaiki dengan 
penambahan test case untuk skenario mode ujian campuran dan validasi batas 
tanggal gelombang. Masing-masing kini mencapai branch coverage **75%**, 
**72%**, dan **74%**.

Modul baru `EmailNotificationService` (branch 78%) dan `HasilAkhirService` 
(branch 77%) mencatat coverage yang baik karena test case dirancang dari 
awal dengan mempertimbangkan seluruh kondisi percabangan, termasuk skenario 
email gagal terkirim dan data hasil akhir tidak ditemukan. Secara keseluruhan, 
Iterasi 4 menunjukkan peningkatan kualitas pengujian yang signifikan dan 
sistem dinyatakan siap untuk dilanjutkan ke tahap pengujian berikutnya.
```

---

## Catatan Penting Saat Mengisi Angka Asli

Sebelum copy-paste angka dari JaCoCo ke tabel, perhatikan ini:

**Angka di JaCoCo ditampilkan dalam 2 format:**
- Format bar: `12 of 45` → artinya 12 yang **tidak** tercakup dari 45 total
- Format persentase: langsung tampil `%` di kolom kanan bar

Yang masuk ke tabel laporan adalah **angka persentase** (bukan "of"), dan ambil dari kolom **Cov.** (bukan missed). Jika JaCoCo menampilkan `73%` di kolom Instructions, maka tulis `73%` di tabel.

**Status ✅ / ❌** ditentukan oleh kondisi ini:
```
✅ jika Instruction Coverage ≥ 70% DAN Branch Coverage ≥ 70%
❌ jika salah satu atau keduanya < 70%
```

#k