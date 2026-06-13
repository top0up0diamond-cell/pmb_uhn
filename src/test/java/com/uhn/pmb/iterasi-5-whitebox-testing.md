# PANDUAN WHITE-BOX TESTING – ITERASI 5
# Peningkatan Coverage Kelas yang Belum Memenuhi Target (≥75%)
# Untuk: GitHub Copilot / AI Assistant

---

## KONTEKS ITERASI 5

Iterasi kelima berfokus pada peningkatan coverage 3 kelas production yang masih di bawah
target ≥75% instruction coverage setelah Iterasi 4. Ketiga kelas ini memiliki tantangan
masing-masing karena ketergantungan pada infrastruktur eksternal, signature method yang
kompleks, atau fitur Spring Security yang tidak aktif di unit test standalone.

**Status masuk Iterasi 5:**

| Kelas | Coverage Saat Ini | Target | Gap |
|-------|:-----------------:|:------:|:---:|
| AdminUjianLinkController | 58.72% | ≥75% | -16.28% |
| CamabaReenrollmentService | 67.56% | ≥75% | -7.44% |
| EmailService | 70.27% | ≥75% | -4.73% |

---

## KONFIGURASI includes pom.xml — GANTI UNTUK ITERASI 5

Bagian lain di pom.xml JANGAN diubah. Hanya ganti isi `<includes>`:

```xml
<includes>
    <include>com/uhn/pmb/controller/AdminUjianLinkController.class</include>
    <include>com/uhn/pmb/service/CamabaReenrollmentService.class</include>
    <include>com/uhn/pmb/service/EmailService.class</include>
</includes>
```

---

## KELAS TARGET 1: AdminUjianLinkController

**File production:** `src/main/java/com/uhn/pmb/controller/AdminUjianLinkController.java`  
**File test:** `src/test/java/com/uhn/pmb/controller/AdminUjianLinkControllerTest.java`  
**Coverage saat ini:** 58.72% (gap: -16.28%)

### Permasalahan

- Null authentication check di baris awal controller (`auth == null → 401`) tidak terpicu
  karena MockMvc standalone tidak memanggil Spring Security filter chain
- Beberapa exception path di `getUjianLinkByPeriod` dan offline exam endpoints belum tercakup
- Error response formatting (500 status) untuk exception selain `RuntimeException` belum diuji

### Rencana Penambahan Test

#### Endpoint yang perlu test tambahan:

1. **GET `/admin/api/ujian-link`** — tambah: exception path (throws RuntimeException → 400)
2. **GET `/admin/api/ujian-link/period/{periodId}`** — tambah: notFound, exception path
3. **POST `/admin/api/ujian-link`** — sudah ada, cek branch validation tambahan
4. **PUT `/admin/api/ujian-link/{id}`** — tambah: linkNotFound → 400
5. **DELETE `/admin/api/ujian-link/{id}`** — sudah ada
6. **POST `/admin/api/ujian-link/offline-exam`** — tambah: periodNotFound → 400
7. **DELETE `/admin/api/ujian-link/offline-exam/{id}`** — tambah: notFound → 400
8. **Null auth path** — gunakan `SecurityContextHolder.clearContext()` di @BeforeEach,
   atau test tanpa setup auth principal untuk memicu path null check

### Contoh Pola Test

```java
@Test
void getAllLinks_exception_returns400() throws Exception {
    when(adminUjianLinkService.getAllLinks()).thenThrow(new RuntimeException("DB error"));
    mockMvc.perform(get("/admin/api/ujian-link"))
            .andExpect(status().isBadRequest());
}

@Test
void getByPeriodId_notFound_returns400() throws Exception {
    when(adminUjianLinkService.getLinkByPeriod(999L))
            .thenThrow(new RuntimeException("Period not found"));
    mockMvc.perform(get("/admin/api/ujian-link/period/999"))
            .andExpect(status().isBadRequest());
}
```

---

## KELAS TARGET 2: CamabaReenrollmentService

**File production:** `src/main/java/com/uhn/pmb/service/CamabaReenrollmentService.java`  
**File test:** `src/test/java/com/uhn/pmb/service/CamabaReenrollmentServiceTest.java`  
**Coverage saat ini:** 67.56% (gap: -7.44%)

### Permasalahan

- Method `submitReenrollment` memiliki signature sangat panjang (10+ parameter + `MultipartFile`)
  dan logika kondisional yang kompleks (status transitions, file processing)
- Path untuk dokumen upload (save ke disk, validasi tipe file) belum tercakup
- Beberapa branch pada status transition (`SUBMITTED → APPROVED → REJECTED`) belum diuji
- Method `getReenrollmentDetails` dengan data nested student/dokumen belum semua path tercakup

### Rencana Penambahan Test

#### Method yang perlu test tambahan:

1. **`submitReenrollment`** — test dengan `MultipartFile` mock:
   - File null vs ada file → branch coverage
   - Status sebelumnya REJECTED → re-submit allowed
   - Student tidak ditemukan → throws exception

2. **`getReenrollmentDetails`** — test dengan data lengkap (student + documents):
   - ReEnrollment dengan dokumen list kosong
   - ReEnrollment dengan dokumen > 0

3. **`getMyReenrollment`** — test exception path (user tidak ditemukan)

4. **`approveReenrollment` / `rejectReenrollment`** — test path ketika:
   - ReEnrollment tidak ditemukan → throws
   - Status sudah APPROVED → throws atau idempotent

### Contoh Pola Test

```java
@Test
void submitReenrollment_studentNotFound_throwsException() {
    when(userRepository.findByEmail("x@test.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> camabaReenrollmentService.submitReenrollment(
            "x@test.com", null, null, null, null, null, null, null, null, null, null))
            .isInstanceOf(RuntimeException.class);
}

@Test
void getMyReenrollment_userNotFound_throwsException() {
    when(userRepository.findByEmail("x@test.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> camabaReenrollmentService.getMyReenrollment("x@test.com"))
            .isInstanceOf(RuntimeException.class);
}
```

> **Catatan:** Cek signature exact dari `submitReenrollment` di production code sebelum membuat
> mock — pastikan jumlah parameter sesuai.

---

## KELAS TARGET 3: EmailService

**File production:** `src/main/java/com/uhn/pmb/service/EmailService.java`  
**File test:** `src/test/java/com/uhn/pmb/service/EmailServiceTest.java`  
**Coverage saat ini:** 70.27% (gap: -4.73%)

### Permasalahan

- Method `sendViaBrevo` menggunakan `RestTemplate` untuk memanggil Brevo REST API —
  exception handling branch (`catch (Exception e)`) belum tercakup
- Branch pengecekan `apiKey == null / empty` belum diuji
- `RestTemplate` di-inject via constructor/field sehingga bisa di-mock dengan Mockito

### Rencana Penambahan Test

#### Path yang perlu test tambahan:

1. **`sendViaBrevo` — apiKey null/empty** → method tidak memanggil API, langsung return/log
2. **`sendViaBrevo` — RestTemplate throws** → branch `catch (Exception e)` tercakup
3. **`sendSimpleEmail` — Brevo throws** → exception ditangkap/dilempar ke caller
4. **`sendHtmlEmail` — dengan konten HTML khusus** → branch template rendering

### Contoh Pola Test

```java
@Test
void sendViaBrevo_restTemplateThrows_handlesGracefully() {
    when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
            .thenThrow(new RuntimeException("Network error"));

    // Tidak throws — exception ditangkap di dalam method
    assertDoesNotThrow(() -> emailService.sendSimpleEmail("to@test.com", "Subject", "Body"));
}
```

> **Catatan:** Jika `RestTemplate` di-new secara internal (bukan di-inject), perlu refaktor
> kecil di production code agar bisa di-mock, atau gunakan `@Spy` + refleksi field.

---

## URUTAN PENGERJAAN YANG DISARANKAN

1. **EmailService** (gap terkecil: -4.73%) — mulai dari sini, paling cepat selesai
2. **CamabaReenrollmentService** (gap -7.44%) — perlu baca signature method dulu
3. **AdminUjianLinkController** (gap terbesar: -16.28%) — perlu paling banyak test baru

---

## LANGKAH EKSEKUSI

```powershell
# 1. Ganti <includes> di pom.xml ke 3 kelas target
# 2. Tambah/edit test file
# 3. Compile test dulu
cd "d:\all code\aa - Copy (3)\tugasakhir"
mvn test-compile --no-transfer-progress -q

# 4. Jalankan full verify + cek coverage
mvn verify --no-transfer-progress -q
Import-Csv "target/site/jacoco/jacoco.csv" | ForEach-Object {
    $m=[int]$_.INSTRUCTION_MISSED; $c=[int]$_.INSTRUCTION_COVERED; $t=$m+$c
    if($t -gt 0){ $pct=[math]::Round($c/$t*100,1)
        [PSCustomObject]@{Class=$_.CLASS; Pct=$pct; Status=if($pct -ge 75){"✅"}else{"❌"}} }
} | Sort-Object Pct | Format-Table -AutoSize
```

---

---

## KELAS TAMBAHAN: ITERASI 5+ (Opsional — Jika Ada Waktu)

Berdasarkan analisis `yangmasihdibawah70danalasan.md`, berikut 3 kelas prioritas Tier 1 
yang masih <70% karena **test insufficient** (bukan production issue):

### Tier 1 — Prioritas Tinggi (Effort Minimal)

#### 1. ValidationStatusTrackerService — **51.61%** (6 @Test → +10-15 test)

**Masalah:** Hanya 6 test untuk 5+ public method → banyak method belum teruji.

**Test yang perlu ditambah:**
```java
@Test void trackFormSubmission_newSubmission_createsTracking() { }
@Test void trackFormSubmission_alreadySubmitted_updatesTimestamp() { }
@Test void trackValidationStatus_statusChanged_logsChange() { }
@Test void trackReviewStatus_reviewCompleted_marksReviewed() { }
@Test void getTrackingHistory_existingData_returnsCorrectSequence() { }
@Test void getTrackingHistory_noData_returnsEmptyList() { }
// ... +9-10 lebih
```

**Expected gain:** +20-25% → **target 72-77%**

---

#### 2. SystemSettingsController — **58.77%** (8 @Test → +10-12 test)

**Masalah:** Hanya 8 test untuk 5+ CRUD endpoint → banyak path belum tercakup.

**Test yang perlu ditambah:**
```java
@Test void getContactInfo_existing_returns200() { }
@Test void getContactInfo_notFound_returns404() { }
@Test void updateContactInfo_validData_returns200() { }
@Test void updateContactInfo_invalidData_returns400() { }
@Test void updateContactInfo_exception_returns500() { }
@Test void getAllSystemLinks_exists_returns200() { }
@Test void getAllSystemLinks_empty_returnsEmptyList() { }
@Test void getSystemLinkByName_notFound_throws() { }
// ... +4-5 lebih
```

**Expected gain:** +12-15% → **target 71-74%**

---

#### 3. AdminExportController — **52.62%** (9 @Test → +10-12 test)

**Masalah:** File export handling edge case belum tercakup (IOException, empty result).

**Test yang perlu ditambah:**
```java
@Test void exportFormData_success_returns200WithFile() { }
@Test void exportFormData_exception_returns500() { }
@Test void exportFormData_emptyResult_returnsEmptyFile() { }
@Test void exportPaymentData_ioException_handled() { }
// ... +8-9 lebih
```

**Expected gain:** +15-20% → **target 68-73%**

---

### Tier 2 — Medium Priority (Jika masih ada waktu setelah Tier 1)

- `SystemSettingsService` — 52.17% (9 test)
- `AdminUserSettingsController` — 53.99% (12 test)
- `AdminAnnouncementController` — 56.87% (11 test)

---

## TARGET AKHIR ITERASI 5

| Kelas | Coverage Saat Ini | Target | Status |
|-------|:-----------------:|:------:|:------:|
| AdminUjianLinkController | 58.72% | ≥75% | ⏳ |
| CamabaReenrollmentService | 67.56% | ≥75% | ⏳ |
| EmailService | 70.27% | ≥75% | ⏳ |
| *(Opsional) ValidationStatusTrackerService | 51.61% | ≥70% | ⏳ |
| *(Opsional) SystemSettingsController | 58.77% | ≥70% | ⏳ |
| *(Opsional) AdminExportController | 52.62% | ≥70% | ⏳ |

**Minimal target Iterasi 5:** 3/3 kelas utama ≥75%  
**Extended target (jika ada waktu):** +3 kelas dari Tier 1 ≥70%
