package com.uhn.pmb.service;

import com.uhn.pmb.dto.ChangePasswordRequest;
import com.uhn.pmb.dto.StudentProfileRequest;
import com.uhn.pmb.entity.Student;
import com.uhn.pmb.entity.User;
import com.uhn.pmb.repository.StudentRepository;
import com.uhn.pmb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CamabaProfileService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FormValidationService formValidationService;

    // ===== HELPER =====
    private String getCurrentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private User getCurrentUser() {
        return userRepository.findByEmail(getCurrentEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Student getCurrentStudent() {
        return studentRepository.findByUser_Id(getCurrentUser().getId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
    }

    // ===== GET PROFILE =====
    public StudentProfileRequest getProfileForCurrentUser() {
        Student student = getCurrentStudent();
        return StudentProfileRequest.builder()
                .id(student.getId()) 
                .fullName(student.getFullName())
                .nik(student.getNik())
                .birthDate(student.getBirthDate())
                .birthPlace(student.getBirthPlace())
                .gender(student.getGender())
                .address(student.getAddress())
                .phoneNumber(student.getPhoneNumber())
                .parentName(student.getParentName())
                .parentPhone(student.getParentPhone())
                .schoolOrigin(student.getSchoolOrigin())
                .schoolYear(student.getSchoolYear())
                .build();
    }

    // ===== UPDATE PROFILE =====
    public StudentProfileRequest updateProfileForCurrentUser(StudentProfileRequest request) {
        Student student = getCurrentStudent();

        student.setFullName(request.getFullName());
        student.setNik(request.getNik());
        student.setBirthDate(request.getBirthDate());
        student.setBirthPlace(request.getBirthPlace());
        student.setGender(request.getGender());
        student.setAddress(request.getAddress());
        student.setPhoneNumber(request.getPhoneNumber());
        student.setParentName(request.getParentName());
        student.setParentPhone(request.getParentPhone());
        student.setSchoolOrigin(request.getSchoolOrigin());
        student.setSchoolYear(request.getSchoolYear());

        studentRepository.save(student);

        return StudentProfileRequest.builder()
                .id(student.getId())
                .fullName(student.getFullName())
                .nik(student.getNik())
                .birthDate(student.getBirthDate())
                .birthPlace(student.getBirthPlace())
                .gender(student.getGender())
                .address(student.getAddress())
                .phoneNumber(student.getPhoneNumber())
                .parentName(student.getParentName())
                .parentPhone(student.getParentPhone())
                .schoolOrigin(student.getSchoolOrigin())
                .schoolYear(student.getSchoolYear())
                .build();
    }

    // ===== CHANGE PASSWORD =====
    public String changePasswordForCurrentUser(ChangePasswordRequest request) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return "Password updated successfully";
    }

    // ===== MARK REPAIR COMPLETE =====
    public String markRepairCompleteForCurrentUser() {
        Student student = getCurrentStudent();
        String repairStatus = "SUDAH_PERBAIKAN"; // ✅ definisikan variable dulu
        formValidationService.updateRepairStatus(student.getId(), repairStatus); // ✅ tidak perlu simpan result
        log.info("Repair marked as completed for student: {}, status: {}", student.getId(), repairStatus);
        return "Repair marked as completed";
    }
}