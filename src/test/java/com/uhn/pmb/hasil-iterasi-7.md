# Laporan Iterasi 7 — Peningkatan Branch Coverage

## Tujuan
Meningkatkan branch coverage pada kelas-kelas yang masih rendah berdasarkan hasil JaCoCo setelah Iterasi 6 (1104 tests, BUILD SUCCESS).

---

## Baseline vs Hasil Iterasi 7

| Kelas | Branch Missed (Sebelum) | Branch Covered (Sebelum) | Coverage (Sebelum) | Branch Missed (Sesudah) | Branch Covered (Sesudah) | Coverage (Sesudah) |
|---|---|---|---|---|---|---|
| AdminExportController | 0 | 2 | **100%** ✅ (skip) | 0 | 2 | **100%** |
| AdminMessagingService | 10 | 6 | 37.5% | **0** | **54** | **100%** ✅ |
| AdminUserSettingsService | 7 | 5 | 41.67% | **0** | **63** | **100%** ✅ |
| AdminMessagingController | 19 | 5 | 20.83% | **0** | **91** | **100%** ✅ |
| AdminCicilanService | 47 | 37 | 44.05% | **4** | **130** | **97.01%** |
| CamabaProfileController | 16 | 6 | 27.27% | **6** | **101** | **94.39%** |
| CamabaPaymentService | 21 | 17 | 44.74% | **45** | **169** | ~78.95% |
| AdminValidationController | 32 | 22 | 40.74% | **64** | **257** | ~80.06% |
| CamabaReenrollmentService | 59 | 79 | 57.25% | 41 | 241 | ~85.5% |
| AdminController | 81 | 123 | 60.29% | 191 | 690 | ~78.3% |

> Catatan: Kenaikan coverage AdminController dan CamabaReenrollmentService merupakan efek samping positif dari test yang dijalankan secara agregat JaCoCo.

---

## Ringkasan Perubahan

### Total Test
- **Sebelum Iterasi 7**: 1104 tests
- **Sesudah Iterasi 7**: **1145 tests** (+41 tests)
- **Status Build**: ✅ BUILD SUCCESS — Tests run: 1145, Failures: 0, Errors: 0, Skipped: 0

---

## Detail Test Baru Per Kelas

### 1. `AdminMessagingServiceTest.java` (+8 tests → 20 total)
| Test | Branch yang Dicakup |
|---|---|
| `send_withMessageType_usesProvidedType` | `messageType != null` true branch |
| `sendToStudent_nullContent_throwsException` | null content → throw |
| `sendToStudent_withMessageType_savesMessage` | `messageType != null` true dalam sendToStudent |
| `markAllRead_differentRecipient_notCounted` | recipient id tidak cocok → short-circuit false |
| `markAllRead_alreadyReadMessage_notCounted` | status == READ → tidak dihitung |
| `markConversationRead_unreadOwnMessage_savedAsRead` | UNREAD + own message → save dipanggil |
| `markConversationRead_readMessage_skipped` | READ message → save tidak dipanggil |
| `markConversationRead_differentUser_skipped` | recipient berbeda → short-circuit, save tidak dipanggil |

### 2. `AdminUserSettingsServiceTest.java` (+9 tests → 20 total)
| Test | Branch yang Dicakup |
|---|---|
| `updateUserRole_emptyRole_throws` | `roleStr.isEmpty()` → throw |
| `updateUserRole_validRole_updatesUser` | success path → save dipanggil |
| `deleteUser_differentEmail_deletesUser` | success path → delete dipanggil |
| `getActiveSetting_foundButInactive_returnsEmpty` | `filter(isActive)` → false → Optional.empty() |
| `saveSetting_existingConfig_updatesValue` | `setting != null` true → update value |
| `saveGformLink_noExisting_createsNew` | `existing == null` → buat baru |
| `saveGformLink_sameValue_doesNotSave` | `!equals(gformLink)` false → tidak save |
| `deleteGformLink_found_deletesConfig` | found → delete dipanggil |
| (dipertahankan) `saveSetting_savesConfig` | new config → save |

### 3. `AdminCicilanServiceTest.java` (+8 tests → 22 total)
| Test | Branch yang Dicakup |
|---|---|
| `approveCicilanRequest_jumlahCicilan6_allCicilansSet` | semua ternary cicilan2-6 TRUE (newJumlah >= N) |
| `approveCicilanRequest_jumlahCicilan0_skipsRecalculation` | `jumlahCicilan > 0` false → skip hargaPerCicilan |
| `approveCicilanRequest_jumlahCicilanSet_hargaCicilan1Null_usesPsFallback` | `else if (jumlahCicilan != null)` + `ps.getCicilan1() > 0` true |
| `approveCicilanRequest_emptyBriva_doesNotSetBriva` | `!getBriva().trim().isEmpty()` false → tidak set briva |
| `approveCicilanRequest_withBriva_hasilAkhirPresent` | `hasilAkhirOpt.isPresent()` true → update existing |
| `approveCicilanRequest_withBriva_hasilAkhirNotPresent` | `hasilAkhirOpt.isPresent()` false → buat baru |
| `rejectCicilanRequest_nullReason_throwsException` | `reason == null` true → throw |
| `rejectCicilanRequest_noStudentEmail_usesEntityEmail` | `overrideEmail != null` false → gunakan email dari entity |

### 4. `CamabaPaymentServiceTest.java` (+5 tests → 25 total)
| Test | Branch yang Dicakup |
|---|---|
| `checkPaymentStatus_expiredActiveVA_setsExpired` | `expiredAt.isBefore(now)` + ACTIVE → set EXPIRED, save |
| `checkPaymentStatus_paidVAWithAdmissionForm_updatesForm` | PAID + admissionForm != null → admissionFormRepository.save |
| `checkPaymentStatus_paidVANoAdmissionForm_noFormUpdate` | PAID + admissionForm == null → tidak save form |
| `confirmCicilanPayment_existingDaftarUlangSelesai_staysSelesai` | `status != SELESAI` false → status tidak diubah |
| (existing) `buyForm_success_returnsVaInfo` | VERIFIED form + studentRegistrationService.buyFormAndCreateVA |

### 5. `AdminMessagingControllerTest.java` (+4 tests → 19 total)
| Test | Branch yang Dicakup |
|---|---|
| `getMessages_withNullSenderMessage_returns200` | `sender != null` false → senderId=null, senderEmail="Unknown", senderType="ADMIN" |
| `getMessages_withCambaSender_returns200` | `sender.getRole() == CAMABA` true → senderType="STUDENT"; recipient=null |
| `getMessages_withAdminSenderAndRecipient_returns200` | role != CAMABA → senderType="ADMIN"; recipient != null |
| `sendReminderToStudent_withUserDetailsPrincipal_returns200` | `auth.getPrincipal() instanceof UserDetails` true |

### 6. `CamabaProfileControllerTest.java` (+5 tests → 16 total)
| Test | Branch yang Dicakup |
|---|---|
| `getProfile_studentWithUser_returns200` | `student.getUser() != null` true → email dari user |
| `debugAuth_withNoHeader_returns200` | authHeader == null → header false branch |
| `debugAuth_withShortHeader_returns200` | authHeader != null, length ≤ 10 → full value |
| `debugAuth_withLongHeader_returns200` | authHeader != null, length > 10 → truncated value |
| `debugAuth_withNullAuth_returns200` | auth == null → fallback false values |

### 7. `AdminValidationControllerTest.java` (+5 tests → 60 total)
| Test | Branch yang Dicakup |
|---|---|
| `getAllReenrollments_withNullStatus_returnsPendingFallback` | `re.getStatus() != null` false → "PENDING" |
| `getAllReenrollments_withNullStudent_skipsStudentFields` | `re.getStudent() != null` false → skip student fields |
| `getAllReenrollments_withStudentNullUser_skipsEmail` | `re.getStudent().getUser() != null` false → skip email |
| `getStudentListForExam_nullStatusExcluded_returns200` | `result.getStatus() != null` false → filter excludes null |
| (efek samping: test existing `getAllReenrollments_withStudentData_returns200`) | student + user path |

---

## Teknik Branch Testing yang Digunakan

1. **Ternary null/non-null** — Uji kondisi `x != null ? value : default` dari kedua sisi
2. **Short-circuit evaluation** — Pada AND conditions, pastikan tidak stub hal yang tidak akan dipanggil (STRICT_STUBS compliance)
3. **Status enum branches** — Uji setiap nilai enum (UNREAD vs READ, ACTIVE vs PAID vs EXPIRED, SELESAI vs non-SELESAI)
4. **Optional.isPresent() true/false** — Uji path found dan not-found secara terpisah
5. **Whitespace/empty string** — Uji string kosong vs string valid (`"  ".trim().isEmpty()`)
6. **instanceof check** — Uji `instanceof UserDetails` principal vs UsernamePasswordAuthenticationToken biasa
7. **Null entity field traversal** — Uji null parent object (`student.getUser()`, `msg.getSender()`) untuk memastikan null-check branches terjangkau

---

## Kelas yang Sudah 100% Branch Coverage

| Kelas | Iterasi |
|---|---|
| AdminExportController | sudah 100% sebelum Iterasi 7 |
| AdminMessagingService | ✅ Iterasi 7 |
| AdminUserSettingsService | ✅ Iterasi 7 |
| AdminMessagingController | ✅ Iterasi 7 |

---

*Build timestamp: Iterasi 7 selesai — 1145 tests, 0 failures*
