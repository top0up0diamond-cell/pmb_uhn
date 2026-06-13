package com.uhn.pmb.service;

import com.uhn.pmb.dto.CicilanRequestDTO;
import com.uhn.pmb.entity.*;
import com.uhn.pmb.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.uhn.pmb.request.CicilanRequestSubmitRequest;

@ExtendWith(MockitoExtension.class)
class CicilanServiceTest {

    @Mock private CicilanRequestRepository cicilanRequestRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private AdmissionFormRepository admissionFormRepository;
    @Mock private ProgramStudiRepository programStudiRepository;
    @Mock private UserRepository userRepository;
    @Mock private RegistrationStatusRepository registrationStatusRepository;

    @InjectMocks
    private CicilanService cicilanService;

    private CicilanRequest buildCicilan(Long id, Long admFormId) {
        AdmissionForm af = new AdmissionForm();
        af.setId(admFormId);
        Student student = Student.builder().id(10L).fullName("Test Student").user(User.builder().id(1L).email("s@test.com").build()).build();
        ProgramStudi ps = new ProgramStudi();
        ps.setId(1L);
        CicilanRequest c = new CicilanRequest();
        c.setId(id);
        c.setAdmissionForm(af);
        c.setStudent(student);
        c.setProgramStudi(ps);
        c.setJumlahCicilan(3);
        c.setHargaCicilan1(500000L);
        c.setStatus(CicilanRequest.CicilanRequestStatus.PENDING);
        return c;
    }

    @Test
    @DisplayName("getCicilanByAdmissionFormId - found returns DTO")
    void getCicilanByAdmissionFormId_found_returnsDTO() {
        CicilanRequest c = buildCicilan(1L, 5L);
        when(cicilanRequestRepository.findAll()).thenReturn(List.of(c));

        Optional<CicilanRequestDTO> result = cicilanService.getCicilanByAdmissionFormId(5L);

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("getCicilanByAdmissionFormId - not found returns empty")
    void getCicilanByAdmissionFormId_notFound_returnsEmpty() {
        when(cicilanRequestRepository.findAll()).thenReturn(Collections.emptyList());

        Optional<CicilanRequestDTO> result = cicilanService.getCicilanByAdmissionFormId(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getMyCicilan - user not found returns empty")
    void getMyCicilan_userNotFound_returnsEmpty() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        Optional<CicilanRequestDTO> result = cicilanService.getMyCicilan("none@test.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getMyCicilan - student not found returns empty")
    void getMyCicilan_studentNotFound_returnsEmpty() {
        User user = User.builder().id(1L).email("u@test.com").build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        Optional<CicilanRequestDTO> result = cicilanService.getMyCicilan("u@test.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getMyCicilan - no cicilans returns empty")
    void getMyCicilan_noCicilans_returnsEmpty() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(cicilanRequestRepository.findByStudentId(10L)).thenReturn(Collections.emptyList());

        Optional<CicilanRequestDTO> result = cicilanService.getMyCicilan("u@test.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getMyCicilan - found returns DTO")
    void getMyCicilan_found_returnsDTO() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).fullName("Student").user(User.builder().id(1L).email("u@test.com").build()).build();
        CicilanRequest c = buildCicilan(1L, 5L);
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));
        when(cicilanRequestRepository.findByStudentId(10L)).thenReturn(List.of(c));

        Optional<CicilanRequestDTO> result = cicilanService.getMyCicilan("u@test.com");

        assertThat(result).isPresent();
    }

    // ===== submitCicilanRequest =====

    @Test
    @DisplayName("submitCicilanRequest - user not found throws exception")
    void submitCicilanRequest_userNotFound_throwsException() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        CicilanRequestSubmitRequest req = new CicilanRequestSubmitRequest();
        assertThatThrownBy(() -> cicilanService.submitCicilanRequest("none@test.com", req))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("submitCicilanRequest - invalid jumlahCicilan throws exception")
    void submitCicilanRequest_invalidJumlahCicilan_throwsException() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));

        CicilanRequestSubmitRequest req = new CicilanRequestSubmitRequest();
        req.setJumlahCicilan(10); // invalid: > 6

        assertThatThrownBy(() -> cicilanService.submitCicilanRequest("u@test.com", req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("1-6");
    }

    @Test
    @DisplayName("submitCicilanRequest - invalid programStudiId throws exception")
    void submitCicilanRequest_invalidProgramStudiId_throwsException() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));

        CicilanRequestSubmitRequest req = new CicilanRequestSubmitRequest();
        req.setJumlahCicilan(3);
        req.setProgramStudiId(0L); // invalid

        assertThatThrownBy(() -> cicilanService.submitCicilanRequest("u@test.com", req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Program studi");
    }

    @Test
    @DisplayName("submitCicilanRequest - existing pending cicilan throws exception")
    void submitCicilanRequest_existingPendingCicilan_throwsException() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));

        ProgramStudi ps = new ProgramStudi();
        ps.setId(1L);
        when(programStudiRepository.findById(1L)).thenReturn(Optional.of(ps));

        CicilanRequest existing = buildCicilan(1L, 5L); // status PENDING
        when(cicilanRequestRepository.findAll()).thenReturn(List.of(existing));

        CicilanRequestSubmitRequest req = new CicilanRequestSubmitRequest();
        req.setJumlahCicilan(3);
        req.setProgramStudiId(1L);

        assertThatThrownBy(() -> cicilanService.submitCicilanRequest("u@test.com", req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("sudah pernah dibuat");
    }

    @Test
    @DisplayName("submitCicilanRequest - happy path creates new cicilan")
    void submitCicilanRequest_happyPath_createsCicilan() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).user(user).fullName("Test").build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));

        ProgramStudi ps = new ProgramStudi();
        ps.setId(1L);
        ps.setNama("Teknik Informatika");
        ps.setCicilan1(2500000L);
        ps.setCicilan2(2500000L);
        ps.setHargaTotalPerTahun(5000000L);
        when(programStudiRepository.findById(1L)).thenReturn(Optional.of(ps));

        when(cicilanRequestRepository.findAll()).thenReturn(Collections.emptyList());
        when(admissionFormRepository.findByStudent_Id(10L)).thenReturn(Collections.emptyList());

        CicilanRequest saved = buildCicilan(2L, null);
        when(cicilanRequestRepository.save(any())).thenReturn(saved);

        CicilanRequestSubmitRequest req = new CicilanRequestSubmitRequest();
        req.setJumlahCicilan(2);
        req.setProgramStudiId(1L);
        req.setPaymentMethod("SIMULATION");

        CicilanRequestDTO result = cicilanService.submitCicilanRequest("u@test.com", req);

        assertThat(result).isNotNull();
        verify(cicilanRequestRepository).save(any());
    }

    @Test
    @DisplayName("submitCicilanRequest - with selectedCicilans list creates cicilan")
    void submitCicilanRequest_withSelectedCicilansList_createsCicilan() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).user(user).fullName("Test").build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));

        ProgramStudi ps = new ProgramStudi();
        ps.setId(1L);
        ps.setNama("Teknik Informatika");
        when(programStudiRepository.findById(1L)).thenReturn(Optional.of(ps));

        when(cicilanRequestRepository.findAll()).thenReturn(Collections.emptyList());
        when(admissionFormRepository.findByStudent_Id(10L)).thenReturn(Collections.emptyList());

        CicilanRequest saved = buildCicilan(3L, null);
        when(cicilanRequestRepository.save(any())).thenReturn(saved);

        CicilanRequestSubmitRequest req = new CicilanRequestSubmitRequest();
        req.setSelectedCicilans(List.of(1, 2, 3));
        req.setProgramStudiId(1L);

        CicilanRequestDTO result = cicilanService.submitCicilanRequest("u@test.com", req);

        assertThat(result).isNotNull();
    }

    // ===== markPaymentSubmitted =====

    @Test
    @DisplayName("markPaymentSubmitted - user not found throws exception")
    void markPaymentSubmitted_userNotFound_throwsException() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cicilanService.markPaymentSubmitted(1L, "none@test.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("markPaymentSubmitted - cicilan not found throws exception")
    void markPaymentSubmitted_cicilanNotFound_throwsException() {
        User user = User.builder().id(1L).email("u@test.com").build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(cicilanRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cicilanService.markPaymentSubmitted(999L, "u@test.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("markPaymentSubmitted - wrong student throws exception")
    void markPaymentSubmitted_wrongStudent_throwsException() {
        User user = User.builder().id(1L).email("u@test.com").build();
        Student student = Student.builder().id(10L).build();
        Student otherStudent = Student.builder().id(99L).build();
        CicilanRequest cicilan = buildCicilan(1L, 5L);
        cicilan.setStudent(otherStudent);

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(cicilanRequestRepository.findById(1L)).thenReturn(Optional.of(cicilan));
        when(studentRepository.findByUser_Id(1L)).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> cicilanService.markPaymentSubmitted(1L, "u@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("bukan milik");
    }
}
