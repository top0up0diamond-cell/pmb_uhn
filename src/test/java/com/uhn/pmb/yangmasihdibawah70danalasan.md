# Analisis Kelas di Bawah 70% Coverage — Penyebab & Rekomendasi

---

## Ringkasan

Dari total 72 kelas yang diukur JaCoCo di semua iterasi, terdapat **8 kelas diuji tetapi masih <70% instruction coverage**. 
Analisis ini menentukan apakah gap disebabkan oleh:

1. **Production Code** — kompleksitas tinggi, banyak method/endpoint, banyak branch logic
2. **Test Coverage** — jumlah test case terbatas, scenario tidak lengkap
3. **Kombinasi** — keduanya berkontribusi

---

## Kelas CONTROLLER <70%

### 1. AdminMessagingController — **49.5%** (587 instr, 25 @Test)

**Analysis:**

| Aspek | Detail |
|-------|--------|
| Endpoints di production | 8+ endpoint dengan @PreAuthorize roles |
| Test cases | 25 @Test methods |
| Avg coverage/test | 49.5% / 25 = **1.98% per test** |
| Masalah utama | **MockMvc standalone tidak jalankan @PreAuthorize** |

**Diagnosis: PRODUCTION + TEST**
- Production: 8 endpoint dengan role-based authorization → setiap endpoint bisa punya 2-3 path (success, forbidden, error)
- Test: MockMvc standalone hanya test happy path, tidak test @PreAuthorize blocking
- Test standaloneSetup tidak menjalankan security filter, jadi @PreAuthorize branches tidak tercakup

**Rekomendasi:**
```
1. Tambah integration test dengan @SpringBootTest (jalankan security context)
2. Test unauthorized scenario (roles yang tidak sesuai)
3. Expected coverage increase: +15-20% (ke 65-70%)
```

---

### 2. AdminAnnouncementController — **56.87%** (371 instr, 11 @Test)

**Analysis:**

| Aspek | Detail |
|-------|--------|
| Endpoints di production | 7+ endpoint dengan @PreAuthorize |
| Test cases | 11 @Test methods |
| Avg coverage/test | 56.87% / 11 = **5.17% per test** |
| Masalah utama | **Test sangat minimal, banyak path tidak tercakup** |

**Diagnosis: TEST INSUFFICIENT**
- Production: kompleks tapi manageable
- Test: hanya 11 test untuk 7+ endpoint → banyak branch tidak ditest (error paths, edge cases, null handling)
- Setiap endpoint harus test: success, error, validation failure — belum semua ada

**Rekomendasi:**
```
1. Tambah test per endpoint: success, 400 error, 500 error
2. Tambah test untuk edge case (null input, empty list, etc)
3. Expected coverage increase: +15-20% (ke 70-77%)
```

---

### 3. AdminExportController — **52.62%** (363 instr, 9 @Test)

**Analysis:**

| Aspek | Detail |
|-------|--------|
| Endpoints di production | 5+ endpoint export data |
| Test cases | 9 @Test methods |
| Avg coverage/test | 52.62% / 9 = **5.85% per test** |
| Masalah utama | **Test terbatas, export/file handling logic belum tercakup** |

**Diagnosis: TEST INSUFFICIENT**
- Production: export endpoint dengan file generation (CSV, PDF, Excel) → kompleks
- Test: hanya test basic flow, tidak test file generation failure, encoding issues, empty result set
- File handling branch (success, file null, I/O error) tidak lengkap

**Rekomendasi:**
```
1. Tambah mock untuk file writing (pemicu IOException)
2. Tambah test untuk empty result → file kosong
3. Tambah test untuk data encoding (special chars)
4. Expected coverage increase: +15-20% (ke 68-73%)
```

---

### 4. AdminUserSettingsController — **53.99%** (326 instr, 12 @Test)

**Analysis:**

| Aspek | Detail |
|-------|--------|
| Endpoints di production | 6+ endpoint user settings management |
| Test cases | 12 @Test methods |
| Avg coverage/test | 53.99% / 12 = **4.50% per test** |
| Masalah utama | **Test coverage gap pada error handling & validation** |

**Diagnosis: TEST INSUFFICIENT + PRODUCTION COMPLEXITY**
- Production: CRUD + validation → banyak conditional logic
- Test: test happy path saja, belum test: invalid input, duplicate user, permission denied
- Validation failure branch tidak tercakup

**Rekomendasi:**
```
1. Tambah test untuk validation failure (email format, duplicate name)
2. Tambah test untuk authorization failure
3. Tambah test untuk service layer exception (500 error)
4. Expected coverage increase: +15-20% (ke 69-74%)
```

---

### 5. SystemSettingsController — **58.77%** (228 instr, 8 @Test)

**Analysis:**

| Aspek | Detail |
|-------|--------|
| Endpoints di production | 5+ endpoint setting management |
| Test cases | 8 @Test methods |
| Avg coverage/test | 58.77% / 8 = **7.35% per test** |
| Masalah utama | **Test coverage paling minimal di controller layer** |

**Diagnosis: TEST INSUFFICIENT**
- Production: CRUD untuk ContactInfo & SystemLink, tidak terlalu kompleks
- Test: paling sedikit (8 test), banyak path yang tidak tercakup
- GET, POST, PUT, DELETE — belum semuanya teruji lengkap

**Rekomendasi:**
```
1. Tambah test untuk semua CRUD operation (GET/POST/PUT/DELETE)
2. Tambah test untuk not found (404) & validation error (400)
3. Tambah test untuk empty result set
4. Expected coverage increase: +12-15% (ke 71-74%)
```

---

### 6. ExamTokenController — **62.43%** (338 instr, 12 @Test)

**Analysis:**

| Aspek | Detail |
|-------|--------|
| Endpoints di production | 5+ endpoint token management |
| Test cases | 12 @Test methods |
| Avg coverage/test | 62.43% / 12 = **5.20% per test** |
| Masalah utama | **Token generation & validation logic tidak semua tercakup** |

**Diagnosis: TEST INSUFFICIENT**
- Production: token creation, validation, expiration — punya logic internal
- Test: 12 test tapi masih banyak branch tidak tercakup (expired token, invalid token format, null handling)
- Exception path sedikit teruji

**Rekomendasi:**
```
1. Tambah test untuk expired token
2. Tambah test untuk invalid/malformed token
3. Tambah test untuk token regeneration
4. Expected coverage increase: +10-15% (ke 72-77%)
```

---

## Kelas SERVICE <70%

### 7. SystemSettingsService — **52.17%** (138 instr, 9 @Test)

**Analysis:**

| Aspek | Detail |
|-------|--------|
| Public methods | 7+ public methods (get, save, list by type/active) |
| Test cases | 9 @Test methods |
| Avg coverage/test | 52.17% / 9 = **5.80% per test** |
| Masalah utama | **Test sangat minimal untuk 7+ methods** |

**Diagnosis: TEST INSUFFICIENT**
- Production: service layer relatively simple (wrapper untuk repository)
- Test: hanya 9 test untuk 7+ methods → banyak method tanpa test, atau test shared
- Contoh: `getSystemLinksByType`, `getActiveSystemLinks` — kemungkinan tidak teruji

**Rekomendasi:**
```
1. Audit setiap public method — pastikan ada test
2. Tambah test untuk empty result (no matching links)
3. Tambah test untuk repository exception handling
4. Expected coverage increase: +15-20% (ke 67-72%)
```

---

### 8. ValidationStatusTrackerService — **51.61%** (155 instr, 6 @Test)

**Analysis:**

| Aspek | Detail |
|-------|--------|
| Public methods | 5+ tracking & status methods |
| Test cases | 6 @Test methods |
| Avg coverage/test | 51.61% / 6 = **8.60% per test** |
| Masalah utama | **Paling sedikit test (6), masalah utama coverage** |

**Diagnosis: TEST INSUFFICIENT (PRIMARY)**
- Production: status tracking logic, tidak terlalu kompleks
- Test: **paling sedikit test di service layer** (hanya 6 test)
- Banyak tracking method yang kemungkinan tidak teruji sama sekali

**Rekomendasi:**
```
1. Prioritas tinggi — tambah test coverage
2. Buat test untuk setiap tracking method
3. Tambah test untuk edge case (null tracking, invalid status)
4. Expected coverage increase: +20-25% (ke 72-77%)
```

---

## Kelas 0% — BUKAN SCOPE ITERASI APAPUN

Kelas berikut tidak diuji di iterasi 1-5, muncul hanya karena wildcard `com.uhn.pmb.controller.*`:

| Kelas | Instruksi | Alasan |
|-------|:---------:|--------|
| CamabaController | 3 | Tidak ada test file / tidak masuk batch iterasi |
| RootController | 7 | Tidak ada test file / belum ditargetkan |
| AdminExamController | 354 | Besar, belum ditargetkan |
| AdminExamSubmissionController | 385 | Besar, belum ditargetkan |
| AdminJenisSeleksiController | 156 | Belum ditargetkan |
| AdminProgramStudiController | 240 | Belum ditargetkan |
| PublicationScheduleController | 99 | Belum ditargetkan |
| AdminController.new HashMap() {...} | — | Artefak compiler, bukan kelas nyata |

**Status:** Tidak perlu diperbaiki — di luar scope iterasi 1-5. Hanya muncul karena JaCoCo wildcard includes.

---

## Summary Table — Diagnosis Sebab <70%

| No | Kelas | Coverage | Penyebab Utama | @Test | Rekomendasi |
|----|-------|:--------:|:--------------|:-----:|-------------|
| 1 | AdminMessagingController | 49.5% | @PreAuthorize (MockMvc standalone) + Test | 25 | Gunakan Integration Test / Add unauthorized path |
| 2 | AdminAnnouncementController | 56.87% | Test insufficient | 11 | Tambah test per endpoint + error path |
| 3 | AdminExportController | 52.62% | Test insufficient | 9 | Tambah test export failure + encoding |
| 4 | AdminUserSettingsController | 53.99% | Test insufficient | 12 | Tambah test validation + authorization fail |
| 5 | SystemSettingsController | 58.77% | Test insufficient (minimal) | 8 | Tambah test CRUD + empty result |
| 6 | ExamTokenController | 62.43% | Test insufficient | 12 | Tambah test expired/invalid token |
| 7 | SystemSettingsService | 52.17% | Test insufficient | 9 | Audit setiap method + empty result test |
| 8 | ValidationStatusTrackerService | 51.61% | **Test insufficient (CRITICAL)** | 6 | Prioritas: tambah test coverage drastis |

---

## Grand Summary

### Penyebab Coverage <70%

| Kategori | Jumlah Kelas | Persen |
|----------|:------------:|:-----:|
| **Test Insufficient** | 7 | 87.5% |
| **Test + Production (MockMvc limitation)** | 1 | 12.5% |
| **Production Code Complexity** | 0 | 0% |

**Kesimpulan:**
- **87.5% kelas <70% disebabkan test case yang belum cukup lengkap** (tidak karena production code error)
- 1 kelas (AdminMessagingController) adalah kombinasi: test ada tapi MockMvc standalone tidak jalankan @PreAuthorize
- **Tidak ada kelas yang masalah di production code sendiri** — semua kelas bisa ditingkatkan ke ≥70% dengan test tambahan

### Estimasi Total Effort

| Kelas | Estimasi Test Tambahan | Target Coverage |
|-------|:---------------------:|:----------------:|
| ValidationStatusTrackerService | +10-15 test | 72-77% |
| SystemSettingsController | +8-10 test | 71-74% |
| AdminExportController | +8-10 test | 68-73% |
| AdminAnnouncementController | +10-12 test | 70-77% |
| AdminUserSettingsController | +10-12 test | 69-74% |
| SystemSettingsService | +10-15 test | 67-72% |
| ExamTokenController | +8-10 test | 72-77% |
| AdminMessagingController | +5 integration test | 65-70% (atau gunakan Spring Security test) |

**Total effort:** +69-94 test case tambahan untuk semua kelas mencapai ≥70%.

---

## Rekomendasi Prioritas

### Tier 1 (Paling Mudah Ditingkatkan)
1. `ValidationStatusTrackerService` — hanya 6 test, banyak gap, effort minimal
2. `SystemSettingsController` — hanya 8 test, banyak gap, effort minimal
3. `AdminExportController` — test file handling patterns sudah ada, tinggal expand

### Tier 2 (Medium)
4. `SystemSettingsService` — 9 test, audit diperlukan
5. `AdminUserSettingsController` — 12 test, validation test masih diperlukan
6. `AdminAnnouncementController` — 11 test, per-endpoint coverage masih gap

### Tier 3 (Kompleks)
7. `ExamTokenController` — 12 test, token logic tidak lengkap
8. `AdminMessagingController` — 25 test tapi MockMvc limitation, butuh integration test

---

## File yang Perlu Dimodifikasi untuk Peningkatan

```
Untuk Tier 1 (Priority):
- src/test/java/com/uhn/pmb/service/ValidationStatusTrackerServiceTest.java
- src/test/java/com/uhn/pmb/controller/SystemSettingsControllerTest.java
- src/test/java/com/uhn/pmb/controller/AdminExportControllerTest.java

Untuk Tier 2:
- src/test/java/com/uhn/pmb/service/SystemSettingsServiceTest.java
- src/test/java/com/uhn/pmb/controller/AdminUserSettingsControllerTest.java
- src/test/java/com/uhn/pmb/controller/AdminAnnouncementControllerTest.java

Untuk Tier 3:
- src/test/java/com/uhn/pmb/controller/ExamTokenControllerTest.java
- src/test/java/com/uhn/pmb/controller/AdminMessagingControllerTest.java
```

---

## Catatan Akhir

**Ini BUKAN bug atau masalah production code.** Ini natural dalam evolusi test coverage — 8 kelas dari 63 kelas terukur (<15%) masih di bawah target, dan semua dapat ditingkatkan dengan test case tambahan yang straightforward.

Untuk laporan TA, rekomendasi:
1. Jelaskan bahwa <70% disebabkan test gap, bukan production defect
2. Prioritaskan 3 kelas Tier 1 untuk iterasi berikutnya jika ada waktu
3. Dokumentasikan effort estimate sehingga reviewer tahu feasibility
