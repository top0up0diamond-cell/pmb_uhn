package com.uhn.pmb.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uhn.pmb.dto.ApiResponse;
import com.uhn.pmb.dto.ChangePasswordRequest;
import com.uhn.pmb.dto.StudentProfileRequest;
import com.uhn.pmb.repository.StudentRepository;
import com.uhn.pmb.repository.UserRepository;
import com.uhn.pmb.service.CamabaProfileService;
import com.uhn.pmb.service.FormValidationService;
import com.uhn.pmb.service.ValidationStatusTrackerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles CAMABA student profile management: get profile, update profile, change password.
 * Extracted from CamabaController for Single Responsibility Principle.
 */
@RestController
@RequestMapping("/api/camaba")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('CAMABA')")
public class CamabaProfileController {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final ValidationStatusTrackerService validationStatusTrackerService;
    private final FormValidationService formValidationService;
    private final CamabaProfileService CamabaProfileService;
    /**
     * Get student profile with user email included
     * GET /api/camaba/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            return ResponseEntity.ok(CamabaProfileService.getProfileForCurrentUser());
        } catch (Exception e) {
            log.error("Error getting profile: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * Update student profile
     * PUT /api/camaba/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody StudentProfileRequest request) {
        try {
            return ResponseEntity.ok(CamabaProfileService.updateProfileForCurrentUser(request));
        } catch (Exception e) {
            log.error("Error updating profile: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * Change student password
     * POST /api/camaba/change-password
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            return ResponseEntity.ok(CamabaProfileService.changePasswordForCurrentUser(request));
        } catch (Exception e) {
            log.error("Error changing password: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * Get validation status for the current CAMABA student
     * GET /api/camaba/validation-status
     */
    @GetMapping("/validation-status")
    public ResponseEntity<?> getMyValidationStatus() {
        try {
            return ResponseEntity.ok(validationStatusTrackerService.getValidationStatusForCurrentUser());
        } catch (Exception e) {
            log.error("Error fetching validation status: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Mark revision as complete (SUDAH_PERBAIKAN) for the current CAMABA student
     * PUT /api/camaba/repair-status
     */
    @PutMapping("/repair-status")
    public ResponseEntity<?> markRepairComplete() {
        try {
            return ResponseEntity.ok(CamabaProfileService.markRepairCompleteForCurrentUser());
        } catch (Exception e) {
            log.error("Error marking repair complete: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Debug authentication info
     * GET /api/camaba/debug-auth
     */
    @GetMapping("/debug-auth")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> debugAuth(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            Map<String, Object> debug = new HashMap<>();
            debug.put("auth_header_received", authHeader != null && !authHeader.isEmpty());
            debug.put("auth_header_value", authHeader != null ? (authHeader.length() > 10 ? authHeader.substring(0, 10) + "..." : authHeader) : "NONE");
            debug.put("security_context_principal", auth != null ? auth.getPrincipal() : "null");
            debug.put("security_context_authenticated", auth != null ? auth.isAuthenticated() : false);
            debug.put("security_context_authorities", auth != null ? auth.getAuthorities().toString() : "none");
            return ResponseEntity.ok(debug);
        } catch (Exception e) {
            log.error("Error in debug endpoint: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("error", e.getMessage(), "timestamp", java.time.LocalDateTime.now()));
        }
    }
}
