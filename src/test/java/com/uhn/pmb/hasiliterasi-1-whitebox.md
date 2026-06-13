# Hasil Pengujian White-Box Iterasi 1

## Informasi Eksekusi

| Item | Detail |
|------|--------|
| Tanggal | 2026-05-05 |
| Framework | JUnit 5 + Mockito + JaCoCo 0.8.11 |
| Spring Boot | 3.3.0 |
| Java | 17 |
| Maven Surefire | 3.2.5 |
| Total Test Files | 62 |
| Total Tests Run | **955** |
| Failures | **0** |
| Errors | **0** |
| Build Status | **✅ BUILD SUCCESS** |

---

## 4.X.Y Pengujian White-Box Iterasi 1

### Konteks

Pengujian white-box pada Iterasi 1 dilakukan untuk memverifikasi logika internal
modul utama sistem PMB-UHN menggunakan metode statement coverage dan branch
coverage. Pengujian menggunakan framework JUnit 5 dengan alat bantu JaCoCo untuk
mengukur cakupan kode secara otomatis. Seluruh 62 file test berhasil dikompilasi
dan 955 test case berjalan tanpa kegagalan. Seluruh 26 kelas yang diukur JaCoCo
berhasil memenuhi target instruction coverage ≥75%.

---

### Hasil Coverage JaCoCo — Iterasi 1

**Tabel 4.X — Hasil White-Box Testing Iterasi 1**

| No | Kelas | Kategori | Instruction (%) | Branch (%) | Status |
|----|-------|----------|:---------------:|:----------:|:------:|
| 1  | JwtTokenProvider | Security | 78% | 57% | ✅ |
| 2  | JwtAuthenticationFilter | Security | 95% | 80% | ✅ |
| 3  | JwtAuthenticationEntryPoint | Security | 98% | 58% | ✅ |
| 4  | AuthController | Controller | 91% | 75% | ✅ |
| 5  | StudentController | Controller | 100% | — | ✅ |
| 6  | PaymentController | Controller | 100% | — | ✅ |
| 7  | CamabaRegistrationController | Controller | 89% | 75% | ✅ |
| 8  | AdminUjianLinkController | Controller | 78% | 77% | ✅ |
| 9  | CamabaProfileController | Controller | 79% | 27% | ✅ |
| 10 | AdminCicilanController | Controller | 78% | — | ✅ |
| 11 | CicilanRequestController | Controller | 79% | 67% | ✅ |
| 12 | CamabaExamController | Controller | 92% | 100% | ✅ |
| 13 | CamabaPaymentController | Controller | 99% | 50% | ✅ |
| 14 | CamabaFormController | Controller | 97% | 62% | ✅ |
| 15 | AdminValidationController | Controller | 78% | 41% | ✅ |
| 16 | AuthService | Service | 78% | 69% | ✅ |
| 17 | StudentService | Service | 99% | 75% | ✅ |
| 18 | BrivaService | Service | 89% | 88% | ✅ |
| 19 | CicilanService | Service | 82% | 60% | ✅ |
| 20 | ExamService | Service | 84% | 50% | ✅ |
| 21 | AdmissionFormService | Service | 88% | 54% | ✅ |
| 22 | CamabaRegistrationService | Service | 95% | 62% | ✅ |
| 23 | AdminUjianLinkService | Service | 94% | 73% | ✅ |
| 24 | AdminCicilanService | Service | 75% | 44% | ✅ |
| 25 | CamabaPaymentService | Service | 77% | 45% | ✅ |
| 26 | CamabaExamService | Service | 88% | 77% | ✅ |

> **Keterangan:** ✅ = memenuhi target ≥75% pada instruction coverage  
> — = branch coverage tidak terukur (tidak ada percabangan yang dapat diukur)

---

### Ringkasan Coverage Keseluruhan (JaCoCo — 26 Kelas Terukur)

| Metrik | Missed | Covered | Total | Coverage |
|--------|--------|---------|-------|----------|
| Instructions | 2,106 | 11,827 | 13,933 | **84.9%** |
| Branches | 443 | 589 | 1,032 | **57.1%** |

---

### Perbandingan Sebelum dan Sesudah Peningkatan (Kelas Target)

| Kelas | Coverage Awal | Coverage Akhir | Delta |
|-------|:-------------:|:--------------:|:-----:|
| AuthController | 26% | 91% | +65% |
| AuthService | 33% | 78% | +45% |
| BrivaService | 45% | 89% | +44% |
| CicilanService | 27% | 82% | +55% |
| ExamService | 52% | 84% | +32% |
| StudentController | 35% | 100% | +65% |
| PaymentController | 0% | 100% | +100% |
| AdminUjianLinkController | 59% | 78% | +19% |
| CamabaProfileController | 28% | 79% | +51% |
| AdminCicilanController | 37% | 78% | +41% |
| CicilanRequestController | 26% | 79% | +53% |
| CamabaExamController | 20% | 92% | +72% |
| CamabaPaymentController | 17% | 99% | +82% |
| CamabaFormController | 8% | 97% | +89% |
| AdminValidationController | 4% | 78% | +74% |
| AdminCicilanService | 9% | 75% | +66% |
| CamabaPaymentService | 6% | 77% | +71% |
| CamabaExamService | 9% | 88% | +79% |
| JwtTokenProvider | 78% | 78% | 0% |
| JwtAuthenticationFilter | 94% | 95% | +1% |
| StudentService | 99% | 99% | 0% |

---

### Kelas dengan Coverage Tinggi (≥75%)

| Kelas | Instruction | Branch | Catatan |
|-------|:-----------:|:------:|---------|
| StudentController | 100% | — | Seluruh branch tercakup |
| PaymentController | 100% | — | Endpoint bank accounts tercakup |
| JwtAuthenticationEntryPoint | 98% | 58% | Hampir seluruh path tercakup |
| CamabaPaymentController | 99% | 50% | |
| StudentService | 99% | 75% | Tertinggi di layer Service |
| CamabaFormController | 97% | 62% | |
| CamabaRegistrationService | 95% | 62% | |
| JwtAuthenticationFilter | 95% | 80% | Filter JWT terkover baik |
| AdminUjianLinkService | 94% | 73% | |
| CamabaExamController | 92% | 100% | |
| AuthController | 91% | 75% | Semua 9 endpoint tercakup |
| CamabaRegistrationController | 89% | 75% | |
| BrivaService | 89% | 88% | |
| CamabaExamService | 88% | 77% | |
| AdmissionFormService | 88% | 54% | |
| ExamService | 84% | 50% | |
| CicilanService | 82% | 60% | |
| JwtTokenProvider | 78% | 57% | |
| AuthService | 78% | 69% | |
| AdminValidationController | 78% | 41% | |
| AdminCicilanController | 78% | — | |
| AdminUjianLinkController | 78% | 77% | |
| CamabaPaymentService | 77% | 45% | |
| CicilanRequestController | 79% | 67% | |
| CamabaProfileController | 79% | 27% | |
| AdminCicilanService | 75% | 44% | |

---

### Analisis Hasil

Pengujian white-box Iterasi 1 berhasil menjalankan **955 test case dari 62 file
test** tanpa satu pun kegagalan (0 Failures, 0 Errors). Seluruh **26 kelas** yang
diukur oleh JaCoCo berhasil memenuhi target instruction coverage ≥75%.

**Kelas-kelas yang berhasil ditingkatkan ke ≥75% (dari kondisi awal):**
- `AdminValidationController`: 4% → 78% (penambahan 52 test mencakup semua 31 endpoint)
- `CamabaFormController`: 8% → 97% (penambahan 22 test, termasuk submitRevision 46-param)
- `CamabaPaymentController`: 17% → 99% (penambahan 13 test semua endpoint)
- `CamabaPaymentService`: 6% → 77% (penambahan buyForm, createVirtualAccount, checkPayment)
- `CamabaExamService`: 9% → 88% (penambahan triggerTokenGeneration + submitExamResults)
- `AdminCicilanController`: 37% → 78% (penambahan test exception path)
- `AdminCicilanService`: 9% → 75% (penambahan test branch approveCicilanRequest)
- `CamabaProfileController`: 28% → 79% (penambahan test semua 11 endpoint)
- `CicilanRequestController`: 26% → 79% (penambahan test path lengkap)
- `CamabaExamController`: 20% → 92% (penambahan 16 test semua endpoint)
- `AuthController`: 26% → 91% (penambahan 11 test endpoint baru)
- `AuthService`: 33% → 78% (penambahan 18 test untuk semua metode service)
- `BrivaService`: 45% → 89% (penambahan 7 test edge case dan error path)
- `CicilanService`: 27% → 82% (penambahan 9 test submit dan mark payment)
- `ExamService`: 52% → 84% (penambahan 5 test findResult dan generateToken)
- `StudentController`: 35% → 100% (penambahan test exception path)
- `PaymentController`: 0% → 100% (file test baru dibuat)

---

### Daftar File Test pada Iterasi 1 (26 Kelas JaCoCo Terukur)

**Security (3 file):**
1. `src/test/java/com/uhn/pmb/security/JwtTokenProviderTest.java` — 10 tests
2. `src/test/java/com/uhn/pmb/security/JwtAuthenticationFilterTest.java` — 5 tests
3. `src/test/java/com/uhn/pmb/security/JwtAuthenticationEntryPointTest.java` — 6 tests

**Service (11 file):**
4. `src/test/java/com/uhn/pmb/service/AuthServiceTest.java` — 25 tests
5. `src/test/java/com/uhn/pmb/service/StudentServiceTest.java` — 5 tests
6. `src/test/java/com/uhn/pmb/service/BrivaServiceTest.java` — 12 tests
7. `src/test/java/com/uhn/pmb/service/CicilanServiceTest.java` — 15 tests
8. `src/test/java/com/uhn/pmb/service/ExamServiceTest.java` — 13 tests
9. `src/test/java/com/uhn/pmb/service/AdmissionFormServiceTest.java` — 33 tests
10. `src/test/java/com/uhn/pmb/service/AdminUjianLinkServiceTest.java` — 16 tests
11. `src/test/java/com/uhn/pmb/service/AdminCicilanServiceTest.java` — 14 tests
12. `src/test/java/com/uhn/pmb/service/CamabaPaymentServiceTest.java` — 21 tests
13. `src/test/java/com/uhn/pmb/service/CamabaExamServiceTest.java` — 22 tests
14. `src/test/java/com/uhn/pmb/service/CamabaRegistrationServiceTest.java` — 15 tests

**Controller (12 file):**
15. `src/test/java/com/uhn/pmb/controller/AuthControllerTest.java` — 15 tests
16. `src/test/java/com/uhn/pmb/controller/StudentControllerTest.java` — 6 tests
17. `src/test/java/com/uhn/pmb/controller/PaymentControllerTest.java` — 3 tests
18. `src/test/java/com/uhn/pmb/controller/CamabaRegistrationControllerTest.java` — 24 tests
19. `src/test/java/com/uhn/pmb/controller/CamabaPaymentControllerTest.java` — 13 tests
20. `src/test/java/com/uhn/pmb/controller/CamabaFormControllerTest.java` — 22 tests
21. `src/test/java/com/uhn/pmb/controller/CamabaExamControllerTest.java` — 16 tests
22. `src/test/java/com/uhn/pmb/controller/CamabaProfileControllerTest.java` — 11 tests
23. `src/test/java/com/uhn/pmb/controller/CicilanRequestControllerTest.java` — 11 tests
24. `src/test/java/com/uhn/pmb/controller/AdminCicilanControllerTest.java` — 10 tests
25. `src/test/java/com/uhn/pmb/controller/AdminUjianLinkControllerTest.java` — 28 tests
26. `src/test/java/com/uhn/pmb/controller/AdminValidationControllerTest.java` — 56 tests

---

### Output Terminal `mvn verify`

```
[INFO] Tests run:  5, Failures: 0, Errors: 0 -- JwtAuthenticationFilterTest
[INFO] Tests run:  6, Failures: 0, Errors: 0 -- JwtAuthenticationEntryPointTest
[INFO] Tests run: 10, Failures: 0, Errors: 0 -- JwtTokenProviderTest
[INFO] Tests run: 14, Failures: 0, Errors: 0 -- AdminCicilanServiceTest
[INFO] Tests run: 16, Failures: 0, Errors: 0 -- AdminUjianLinkServiceTest
[INFO] Tests run: 33, Failures: 0, Errors: 0 -- AdmissionFormServiceTest
[INFO] Tests run: 25, Failures: 0, Errors: 0 -- AuthServiceTest
[INFO] Tests run: 12, Failures: 0, Errors: 0 -- BrivaServiceTest
[INFO] Tests run: 22, Failures: 0, Errors: 0 -- CamabaExamServiceTest
[INFO] Tests run: 21, Failures: 0, Errors: 0 -- CamabaPaymentServiceTest
[INFO] Tests run: 15, Failures: 0, Errors: 0 -- CamabaRegistrationServiceTest
[INFO] Tests run: 15, Failures: 0, Errors: 0 -- CicilanServiceTest
[INFO] Tests run: 13, Failures: 0, Errors: 0 -- ExamServiceTest
[INFO] Tests run:  5, Failures: 0, Errors: 0 -- StudentServiceTest
[INFO] Tests run: 10, Failures: 0, Errors: 0 -- AdminCicilanControllerTest
[INFO] Tests run: 56, Failures: 0, Errors: 0 -- AdminValidationControllerTest
[INFO] Tests run: 28, Failures: 0, Errors: 0 -- AdminUjianLinkControllerTest
[INFO] Tests run: 15, Failures: 0, Errors: 0 -- AuthControllerTest
[INFO] Tests run: 22, Failures: 0, Errors: 0 -- CamabaFormControllerTest
[INFO] Tests run: 16, Failures: 0, Errors: 0 -- CamabaExamControllerTest
[INFO] Tests run: 13, Failures: 0, Errors: 0 -- CamabaPaymentControllerTest
[INFO] Tests run: 11, Failures: 0, Errors: 0 -- CamabaProfileControllerTest
[INFO] Tests run: 24, Failures: 0, Errors: 0 -- CamabaRegistrationControllerTest
[INFO] Tests run: 11, Failures: 0, Errors: 0 -- CicilanRequestControllerTest
[INFO] Tests run:  3, Failures: 0, Errors: 0 -- PaymentControllerTest
[INFO] Tests run:  6, Failures: 0, Errors: 0 -- StudentControllerTest

[INFO] Tests run: 955, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

### Gambar JaCoCo Report

*[Screenshot target/site/jacoco/index.html — sisipkan di sini]*

Coverage 26 kelas yang diukur oleh JaCoCo (kondisi akhir):
- Instructions: **84.9%** (11,827 of 13,933 covered)
- Branches: **57.1%** (589 of 1,032 covered)

**26 dari 26 kelas memenuhi target ≥75% instruction coverage pada Iterasi 1.** ✅
