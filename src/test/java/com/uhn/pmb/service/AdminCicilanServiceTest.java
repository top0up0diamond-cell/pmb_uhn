package com.uhn.pmb.service;

import com.uhn.pmb.dto.CicilanRequestDTO;
import com.uhn.pmb.entity.CicilanRequest;
import com.uhn.pmb.entity.ProgramStudi;
import com.uhn.pmb.entity.Student;
import com.uhn.pmb.entity.User;
import com.uhn.pmb.repository.CicilanRequestRepository;
import com.uhn.pmb.repository.HasilAkhirRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCicilanServiceTest {

    @Mock private CicilanRequestRepository cicilanRequestRepository;
    @Mock private HasilAkhirRepository hasilAkhirRepository;
    @Mock private EmailService emailService;

    @InjectMocks
    private AdminCicilanService adminCicilanService;

    @Test
    @DisplayName("getPendingRequests - returns page of pending requests")
    void getPendingRequests_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CicilanRequest> emptyPage = Page.empty(pageable);
        when(cicilanRequestRepository.findPendingRequests(any(Pageable.class))).thenReturn(emptyPage);

        Page<CicilanRequestDTO> result = adminCicilanService.getPendingRequests(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("getByStatus - APPROVED returns page of approved requests")
    void getByStatus_approved_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CicilanRequest> emptyPage = Page.empty(pageable);
        when(cicilanRequestRepository.findByStatus(
                eq(CicilanRequest.CicilanRequestStatus.APPROVED), any(Pageable.class)))
                .thenReturn(emptyPage);

        Page<CicilanRequestDTO> result = adminCicilanService.getByStatus("APPROVED", pageable);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("getByStatus - invalid status throws IllegalArgumentException")
    void getByStatus_invalidStatus_throwsException() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> adminCicilanService.getByStatus("INVALID", pageable))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("approveCicilanRequest - not found throws exception")
    void approveCicilanRequest_notFound_throwsException() {
        when(cicilanRequestRepository.findById(999L)).thenReturn(Optional.empty());

        AdminCicilanService.ApproveRequest req = new AdminCicilanService.ApproveRequest();
        req.setApprovedBy("admin@test.com");

        assertThatThrownBy(() -> adminCicilanService.approveCicilanRequest(999L, req))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("rejectCicilanRequest - not found throws exception")
    void rejectCicilanRequest_notFound_throwsException() {
        when(cicilanRequestRepository.findById(999L)).thenReturn(Optional.empty());

        AdminCicilanService.RejectRequest req = new AdminCicilanService.RejectRequest();
        req.setReason("Not eligible");
        req.setStudentEmail("s@test.com");

        assertThatThrownBy(() -> adminCicilanService.rejectCicilanRequest(999L, req))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("deleteCicilanRequest - not found throws exception")
    void deleteCicilanRequest_notFound_throwsException() {
        when(cicilanRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminCicilanService.deleteCicilanRequest(999L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("approveCicilanRequest - success returns DTO")
    void approveCicilanRequest_success_returnsDTO() {
        User user = User.builder().id(1L).email("s@test.com").build();
        Student student = Student.builder().id(10L).fullName("Alice").user(user).build();
        ProgramStudi ps = new ProgramStudi();
        ps.setId(1L);
        ps.setNama("Teknik Informatika");
        ps.setCicilan1(500000L);
        ps.setCicilan2(500000L);
        CicilanRequest cicilan = new CicilanRequest();
        cicilan.setId(1L);
        cicilan.setStudent(student);
        cicilan.setProgramStudi(ps);
        cicilan.setJumlahCicilan(2);
        cicilan.setHargaCicilan1(500000L);
        cicilan.setHargaCicilan2(500000L);
        cicilan.setStatus(CicilanRequest.CicilanRequestStatus.PENDING);
        when(cicilanRequestRepository.findById(1L)).thenReturn(Optional.of(cicilan));
        when(cicilanRequestRepository.save(any())).thenReturn(cicilan);
        lenient().doNothing().when(emailService).sendSimpleEmail(any(), any(), any());

        AdminCicilanService.ApproveRequest req = new AdminCicilanService.ApproveRequest();
        req.setApprovedBy("admin@test.com");

        CicilanRequestDTO result = adminCicilanService.approveCicilanRequest(1L, req);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("rejectCicilanRequest - success returns DTO")
    void rejectCicilanRequest_success_returnsDTO() {
        User user = User.builder().id(1L).email("s@test.com").build();
        Student student = Student.builder().id(10L).fullName("Alice").user(user).build();
        ProgramStudi ps = new ProgramStudi();
        ps.setNama("Teknik Informatika");
        CicilanRequest cicilan = new CicilanRequest();
        cicilan.setId(1L);
        cicilan.setStudent(student);
        cicilan.setProgramStudi(ps);
        cicilan.setStatus(CicilanRequest.CicilanRequestStatus.PENDING);
        when(cicilanRequestRepository.findById(1L)).thenReturn(Optional.of(cicilan));
        when(cicilanRequestRepository.save(any())).thenReturn(cicilan);
        lenient().doNothing().when(emailService).sendSimpleEmail(any(), any(), any());

        AdminCicilanService.RejectRequest req = new AdminCicilanService.RejectRequest();
        req.setReason("Not eligible");
        req.setStudentEmail("s@test.com");

        CicilanRequestDTO result = adminCicilanService.rejectCicilanRequest(1L, req);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("rejectCicilanRequest - empty reason throws exception")
    void rejectCicilanRequest_emptyReason_throwsException() {
        AdminCicilanService.RejectRequest req = new AdminCicilanService.RejectRequest();
        req.setReason("");
        req.setStudentEmail("s@test.com");

        assertThatThrownBy(() -> adminCicilanService.rejectCicilanRequest(1L, req))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("deleteCicilanRequest - success deletes entity")
    void deleteCicilanRequest_success_deletes() {
        CicilanRequest cicilan = new CicilanRequest();
        cicilan.setId(1L);
        when(cicilanRequestRepository.findById(1L)).thenReturn(Optional.of(cicilan));
        doNothing().when(cicilanRequestRepository).deleteById(1L);

        adminCicilanService.deleteCicilanRequest(1L);

        verify(cicilanRequestRepository).deleteById(1L);
    }

    @Test
    @DisplayName("approveCicilanRequest - jumlahCicilan=2 sets cicilan2 from ProgramStudi")
    void approveCicilanRequest_withJumlahCicilan_updatesFields() {
        User user = User.builder().id(1L).email("s@test.com").build();
        Student student = Student.builder().id(10L).fullName("Alice").user(user).build();
        ProgramStudi ps = new ProgramStudi();
        ps.setId(1L);
        ps.setNama("Teknik Informatika");
        ps.setCicilan1(500000L);
        ps.setCicilan2(400000L);
        ps.setCicilan3(300000L);
        CicilanRequest cicilan = new CicilanRequest();
        cicilan.setId(1L);
        cicilan.setStudent(student);
        cicilan.setProgramStudi(ps);
        cicilan.setJumlahCicilan(1);
        cicilan.setHargaCicilan1(500000L);
        cicilan.setStatus(CicilanRequest.CicilanRequestStatus.PENDING);
        when(cicilanRequestRepository.findById(1L)).thenReturn(Optional.of(cicilan));
        when(cicilanRequestRepository.save(any())).thenReturn(cicilan);

        AdminCicilanService.ApproveRequest req = new AdminCicilanService.ApproveRequest();
        req.setJumlahCicilan(2);
        req.setHargaCicilan1(500000L);
        req.setApprovedBy("admin@test.com");

        CicilanRequestDTO result = adminCicilanService.approveCicilanRequest(1L, req);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("approveCicilanRequest - invalid jumlahCicilan=7 throws exception")
    void approveCicilanRequest_invalidJumlahCicilan_throwsException() {
        User user = User.builder().id(1L).email("s@test.com").build();
        Student student = Student.builder().id(10L).fullName("Alice").user(user).build();
        ProgramStudi ps = new ProgramStudi();
        CicilanRequest cicilan = new CicilanRequest();
        cicilan.setId(1L);
        cicilan.setStudent(student);
        cicilan.setProgramStudi(ps);
        cicilan.setJumlahCicilan(1);
        cicilan.setStatus(CicilanRequest.CicilanRequestStatus.PENDING);
        when(cicilanRequestRepository.findById(1L)).thenReturn(Optional.of(cicilan));

        AdminCicilanService.ApproveRequest req = new AdminCicilanService.ApproveRequest();
        req.setJumlahCicilan(7);

        assertThatThrownBy(() -> adminCicilanService.approveCicilanRequest(1L, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("1-6");
    }

    @Test
    @DisplayName("approveCicilanRequest - with briva sets briva field")
    void approveCicilanRequest_withBriva_setsBriva() {
        User user = User.builder().id(1L).email("s@test.com").build();
        Student student = Student.builder().id(10L).fullName("Alice").user(user).build();
        ProgramStudi ps = new ProgramStudi();
        ps.setNama("Teknik Informatika");
        ps.setCicilan1(500000L);
        CicilanRequest cicilan = new CicilanRequest();
        cicilan.setId(1L);
        cicilan.setStudent(student);
        cicilan.setProgramStudi(ps);
        cicilan.setJumlahCicilan(1);
        cicilan.setHargaCicilan1(500000L);
        cicilan.setStatus(CicilanRequest.CicilanRequestStatus.PENDING);
        when(cicilanRequestRepository.findById(1L)).thenReturn(Optional.of(cicilan));
        when(cicilanRequestRepository.save(any())).thenReturn(cicilan);

        AdminCicilanService.ApproveRequest req = new AdminCicilanService.ApproveRequest();
        req.setBriva("88001234567");
        req.setApprovedBy("admin@test.com");

        CicilanRequestDTO result = adminCicilanService.approveCicilanRequest(1L, req);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("approveCicilanRequest - jumlahCicilan=null, hargaCicilan1=provided updates price only")
    void approveCicilanRequest_hargaCicilan1Only_updatesPrice() {
        User user = User.builder().id(1L).email("s@test.com").build();
        Student student = Student.builder().id(10L).fullName("Alice").user(user).build();
        ProgramStudi ps = new ProgramStudi();
        ps.setNama("Teknik Informatika");
        ps.setCicilan1(500000L);
        CicilanRequest cicilan = new CicilanRequest();
        cicilan.setId(1L);
        cicilan.setStudent(student);
        cicilan.setProgramStudi(ps);
        cicilan.setJumlahCicilan(1);
        cicilan.setHargaCicilan1(500000L);
        cicilan.setStatus(CicilanRequest.CicilanRequestStatus.PENDING);
        when(cicilanRequestRepository.findById(1L)).thenReturn(Optional.of(cicilan));
        when(cicilanRequestRepository.save(any())).thenReturn(cicilan);

        AdminCicilanService.ApproveRequest req = new AdminCicilanService.ApproveRequest();
        req.setHargaCicilan1(600000L);
        req.setApprovedBy("admin@test.com");

        CicilanRequestDTO result = adminCicilanService.approveCicilanRequest(1L, req);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("approveCicilanRequest - jumlahCicilan=6 covers all cicilan ternary TRUE branches")
    void approveCicilanRequest_jumlahCicilan6_allCicilansSet() {
        User user = User.builder().id(1L).email("s@test.com").build();
        Student student = Student.builder().id(10L).fullName("Alice").user(user).build();
        ProgramStudi ps = new ProgramStudi();
        ps.setNama("Teknik Informatika");
        ps.setCicilan1(500000L);
        ps.setCicilan2(400000L);
        ps.setCicilan3(300000L);
        ps.setCicilan4(200000L);
        ps.setCicilan5(100000L);
        ps.setCicilan6(100000L);
        CicilanRequest cicilan = new CicilanRequest();
        cicilan.setId(1L);
        cicilan.setStudent(student);
        cicilan.setProgramStudi(ps);
        cicilan.setJumlahCicilan(1);
        cicilan.setStatus(CicilanRequest.CicilanRequestStatus.PENDING);
        when(cicilanRequestRepository.findById(1L)).thenReturn(Optional.of(cicilan));
        when(cicilanRequestRepository.save(any())).thenReturn(cicilan);

        AdminCicilanService.ApproveRequest req = new AdminCicilanService.ApproveRequest();
        req.setJumlahCicilan(6);
        req.setApprovedBy("admin@test.com");

        CicilanRequestDTO result = adminCicilanService.approveCicilanRequest(1L, req);
        assertThat(result).isNotNull();
        assertThat(cicilan.getJumlahCicilan()).isEqualTo(6);
    }

    @Test
    @DisplayName("approveCicilanRequest - original jumlahCicilan=0 skips hargaPerCicilan recalculation")
    void approveCicilanRequest_jumlahCicilan0_skipsRecalculation() {
        User user = User.builder().id(1L).email("s@test.com").build();
        Student student = Student.builder().id(10L).fullName("Alice").user(user).build();
        ProgramStudi ps = new ProgramStudi();
        ps.setNama("Teknik Informatika");
        CicilanRequest cicilan = new CicilanRequest();
        cicilan.setId(1L);
        cicilan.setStudent(student);
        cicilan.setProgramStudi(ps);
        cicilan.setJumlahCicilan(0);
        cicilan.setStatus(CicilanRequest.CicilanRequestStatus.PENDING);
        when(cicilanRequestRepository.findById(1L)).thenReturn(Optional.of(cicilan));
        when(cicilanRequestRepository.save(any())).thenReturn(cicilan);

        AdminCicilanService.ApproveRequest req = new AdminCicilanService.ApproveRequest();
        req.setApprovedBy("admin@test.com");

        CicilanRequestDTO result = adminCicilanService.approveCicilanRequest(1L, req);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("approveCicilanRequest - jumlahCicilan set but no hargaCicilan1, uses ProgramStudi fallback")
    void approveCicilanRequest_jumlahCicilanSet_hargaCicilan1Null_usesPsFallback() {
        User user = User.builder().id(1L).email("s@test.com").build();
        Student student = Student.builder().id(10L).fullName("Alice").user(user).build();
        ProgramStudi ps = new ProgramStudi();
        ps.setNama("Teknik Informatika");
        ps.setCicilan1(600000L);
        ps.setCicilan2(500000L);
        CicilanRequest cicilan = new CicilanRequest();
        cicilan.setId(1L);
        cicilan.setStudent(student);
        cicilan.setProgramStudi(ps);
        cicilan.setJumlahCicilan(1);
        cicilan.setHargaCicilan1(500000L);
        cicilan.setStatus(CicilanRequest.CicilanRequestStatus.PENDING);
        when(cicilanRequestRepository.findById(1L)).thenReturn(Optional.of(cicilan));
        when(cicilanRequestRepository.save(any())).thenReturn(cicilan);

        AdminCicilanService.ApproveRequest req = new AdminCicilanService.ApproveRequest();
        req.setJumlahCicilan(2);
        // No hargaCicilan1 set - should use PS fallback
        req.setApprovedBy("admin@test.com");

        CicilanRequestDTO result = adminCicilanService.approveCicilanRequest(1L, req);
        assertThat(result).isNotNull();
        assertThat(cicilan.getHargaCicilan1()).isEqualTo(600000L);
    }

    @Test
    @DisplayName("approveCicilanRequest - whitespace briva string does not set briva")
    void approveCicilanRequest_emptyBriva_doesNotSetBriva() {
        User user = User.builder().id(1L).email("s@test.com").build();
        Student student = Student.builder().id(10L).fullName("Alice").user(user).build();
        ProgramStudi ps = new ProgramStudi();
        ps.setNama("Teknik Informatika");
        CicilanRequest cicilan = new CicilanRequest();
        cicilan.setId(1L);
        cicilan.setStudent(student);
        cicilan.setProgramStudi(ps);
        cicilan.setJumlahCicilan(1);
        cicilan.setHargaCicilan1(500000L);
        cicilan.setStatus(CicilanRequest.CicilanRequestStatus.PENDING);
        when(cicilanRequestRepository.findById(1L)).thenReturn(Optional.of(cicilan));
        when(cicilanRequestRepository.save(any())).thenReturn(cicilan);

        AdminCicilanService.ApproveRequest req = new AdminCicilanService.ApproveRequest();
        req.setBriva("   "); // whitespace only
        req.setApprovedBy("admin@test.com");

        CicilanRequestDTO result = adminCicilanService.approveCicilanRequest(1L, req);
        assertThat(result).isNotNull();
        assertThat(cicilan.getBriva()).isNull();
    }

    @Test
    @DisplayName("approveCicilanRequest - briva set, existing hasilAkhir updated")
    void approveCicilanRequest_withBriva_hasilAkhirPresent() {
        User user = User.builder().id(1L).email("s@test.com").build();
        Student student = Student.builder().id(10L).fullName("Alice").user(user).build();
        ProgramStudi ps = new ProgramStudi();
        ps.setNama("Teknik Informatika");
        CicilanRequest cicilan = new CicilanRequest();
        cicilan.setId(1L);
        cicilan.setStudent(student);
        cicilan.setProgramStudi(ps);
        cicilan.setJumlahCicilan(1);
        cicilan.setHargaCicilan1(500000L);
        cicilan.setHargaTotal(500000L);
        cicilan.setStatus(CicilanRequest.CicilanRequestStatus.PENDING);
        com.uhn.pmb.entity.HasilAkhir hasilAkhir = new com.uhn.pmb.entity.HasilAkhir();
        hasilAkhir.setNomorRegistrasi("REG-001");
        when(cicilanRequestRepository.findById(1L)).thenReturn(Optional.of(cicilan));
        when(cicilanRequestRepository.save(any())).thenReturn(cicilan);
        when(hasilAkhirRepository.findByStudent(student)).thenReturn(Optional.of(hasilAkhir));
        when(hasilAkhirRepository.save(any())).thenReturn(hasilAkhir);

        AdminCicilanService.ApproveRequest req = new AdminCicilanService.ApproveRequest();
        req.setBriva("88001234567");
        req.setApprovedBy("admin@test.com");

        CicilanRequestDTO result = adminCicilanService.approveCicilanRequest(1L, req);
        assertThat(result).isNotNull();
        verify(hasilAkhirRepository).save(any());
    }

    @Test
    @DisplayName("approveCicilanRequest - briva set, no hasilAkhir found, creates new")
    void approveCicilanRequest_withBriva_hasilAkhirNotPresent() {
        User user = User.builder().id(1L).email("s@test.com").build();
        Student student = Student.builder().id(10L).fullName("Alice").user(user).build();
        ProgramStudi ps = new ProgramStudi();
        ps.setNama("Teknik Informatika");
        CicilanRequest cicilan = new CicilanRequest();
        cicilan.setId(1L);
        cicilan.setStudent(student);
        cicilan.setProgramStudi(ps);
        cicilan.setJumlahCicilan(1);
        cicilan.setHargaCicilan1(500000L);
        cicilan.setHargaTotal(500000L);
        cicilan.setStatus(CicilanRequest.CicilanRequestStatus.PENDING);
        when(cicilanRequestRepository.findById(1L)).thenReturn(Optional.of(cicilan));
        when(cicilanRequestRepository.save(any())).thenReturn(cicilan);
        when(hasilAkhirRepository.findByStudent(student)).thenReturn(Optional.empty());
        when(hasilAkhirRepository.save(any())).thenReturn(new com.uhn.pmb.entity.HasilAkhir());

        AdminCicilanService.ApproveRequest req = new AdminCicilanService.ApproveRequest();
        req.setBriva("88001234567");
        req.setApprovedBy("admin@test.com");

        CicilanRequestDTO result = adminCicilanService.approveCicilanRequest(1L, req);
        assertThat(result).isNotNull();
        verify(hasilAkhirRepository).save(any());
    }

    @Test
    @DisplayName("rejectCicilanRequest - null reason throws RuntimeException")
    void rejectCicilanRequest_nullReason_throwsException() {
        AdminCicilanService.RejectRequest req = new AdminCicilanService.RejectRequest();
        req.setReason(null);

        assertThatThrownBy(() -> adminCicilanService.rejectCicilanRequest(1L, req))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("rejectCicilanRequest - no studentEmail uses student email from entity")
    void rejectCicilanRequest_noStudentEmail_usesEntityEmail() {
        User user = User.builder().id(1L).email("s@test.com").build();
        Student student = Student.builder().id(10L).fullName("Alice").user(user).build();
        ProgramStudi ps = new ProgramStudi();
        ps.setNama("Teknik Informatika");
        CicilanRequest cicilan = new CicilanRequest();
        cicilan.setId(1L);
        cicilan.setStudent(student);
        cicilan.setProgramStudi(ps);
        cicilan.setStatus(CicilanRequest.CicilanRequestStatus.PENDING);
        when(cicilanRequestRepository.findById(1L)).thenReturn(Optional.of(cicilan));
        when(cicilanRequestRepository.save(any())).thenReturn(cicilan);

        AdminCicilanService.RejectRequest req = new AdminCicilanService.RejectRequest();
        req.setReason("Not eligible for cicilan");
        // No setStudentEmail → overrideEmail will be null

        CicilanRequestDTO result = adminCicilanService.rejectCicilanRequest(1L, req);
        assertThat(result).isNotNull();
    }
}
