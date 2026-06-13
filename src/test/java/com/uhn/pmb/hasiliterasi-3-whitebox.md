# White-Box Testing Hasil Iterasi 3

**Project:** PMB System - HKBP Nommensen  
**Test Framework:** JUnit 5, Mockito, MockMvc  
**Date:** 2025-05-04  
**Status:** ✅ BUILD SUCCESS — All 10 target classes ≥70%

---

## 📊 Test Execution Summary

| Metric | Value |
|--------|-------|
| **Total Tests** | 598 |
| **Passed** | 598 |
| **Failed** | 0 |
| **Errors** | 0 |
| **Skipped** | 0 |
| **Success Rate** | 100% |

---

## 📋 Target Classes Coverage (Iterasi 3)

### Threshold: ≥70% Instruction Coverage

| Class | Covered | Total | % | Status |
|-------|---------|-------|---|--------|
| SmaController | 152 | 162 | 93.8% | ✅ |
| CamabaRegistrationController | 326 | 367 | 88.8% | ✅ |
| FileController | 163 | 219 | 74.4% | ✅ |
| FileServingController | 284 | 326 | 87.1% | ✅ |
| SmaService | 291 | 293 | 99.3% | ✅ |
| CamabaRegistrationService | 751 | 790 | 95.1% | ✅ |
| AdmissionFormService | 2233 | 2538 | 88.0% | ✅ |
| FileStorageService | 117 | 126 | 92.9% | ✅ |
| FormValidationService | 1864 | 2112 | 88.3% | ✅ |
| AdminDataExportService | 923 | 994 | 92.9% | ✅ |

**Result: 10/10 classes ≥70% ✅**

---

## 📁 Test Files Created/Updated

### Service Tests (6)
1. ✅ **SmaServiceTest.java** — 18 tests
   - searchSma, searchExternalSekolah, getAllActiveSma, createSma, updateSma, deleteSma

2. ✅ **FileStorageServiceTest.java** — 10 tests
   - saveFile, getFile, deleteFile, fileExists, getAllFiles operations

3. ✅ **FormValidationServiceTest.java** — 30 tests
   - findAll, findById, approve, reject, markRevisionNeeded, getFormsForValidationDashboard
   - Added: getFormDetails (found/notFound), getAdmissionFormStudentDetails (found/notFound/withPeriod)
   - Added: updateRepairStatus (noValidations/existingRecord/createsNew)
   - Added: markRevisionNeeded with student email triggers sendRevisionNeededEmail
   - Added: approve with reenroll status SELESAI / REJECTED

4. ✅ **AdminDataExportServiceTest.java** — 20 tests
   - exportFormAndPayment, exportReEnrollmentData, exportHasilAkhirData

5. ✅ **CamabaRegistrationServiceTest.java** — 15 tests
   - getAllGelombang, getAllFormulas, getProgramStudiByJenisSeleksi

6. ✅ **AdmissionFormServiceTest.java** — 33 tests
   - checkSubmissionStatus, getAdmissionFormData, getCurrentAdmissionFormData, getAdmissionStatus
   - Added: updateAdmissionFormData (noForm throws, multipart with all params, non-multipart)
   - Added: submitAdmissionForm (userNotFound, nullJenisSeleksiId, notFound, happyPath, MEDICAL)
   - Added: updateAdmissionFormSelection (noForms, withForms, gelombangId, selectionTypeId)
   - Added: registerForAdmission (periodNotFound, selectionTypeNotFound, success)
   - Added: submitRevision (formNotFound, wrongStudent, happyPath, withFormValidation+repairStatus)

### Controller Tests (4)
1. ✅ **SmaControllerTest.java** — 14 tests

2. ✅ **FileControllerTest.java** — 16 tests (real files created in @BeforeEach)
   - getAdmissionFile: notFound + all media types (jpg, jpeg, png, gif, pdf, bin)
   - downloadAdmissionFile: notFound + all media types

3. ✅ **FileServingControllerTest.java** — 27 tests (real files created in @BeforeEach)
   - viewFile: traversal-blocked (400), notFound (404), all media types (200), download mode
   - showFile: traversal/absolute/home-blocked (400), notFound (404), all media types (200)
   - redirectUploads: reachable (200)

4. ✅ **CamabaRegistrationControllerTest.java** — 20 tests
   - Added: exception tests for 5 endpoints (returns 400)

---

## 🔧 Test Configuration

- **JUnit**: 5.10.1 | **Mockito**: 5.7.1 | **MockMvc**: Standalone
- **JaCoCo**: 0.8.11 | **Java**: 17 | **Spring Boot**: 3.3.0
- File-based tests create/delete real files in uploads/ subdirs
- MockMultipartHttpServletRequest for multipart upload coverage
- markAsCompleted returns RegistrationStatus (not void) — use when().thenReturn()

---

## 📈 Iterasi Progress Comparison

| Iterasi | Total Tests | Passed | Target Classes ≥ Threshold |
|---------|------------|--------|---------------------------|
| Iterasi 1 | 245 | 245 | ≥50%: 10/10 ✅ |
| Iterasi 2 | 350 | 350 | ≥50%: 10/10 ✅ |
| Iterasi 3 | 598 | 598 | ≥70%: 10/10 ✅ |

---

## 📊 JaCoCo Report Locations

- **Full HTML Report**: 	arget/site/jacoco/index.html
- **CSV Report**: 	arget/site/jacoco/jacoco.csv
- **XML Report**: 	arget/site/jacoco/jacoco.xml
