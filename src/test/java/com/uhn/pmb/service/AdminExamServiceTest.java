package com.uhn.pmb.service;

import com.uhn.pmb.dto.ApproveExamQuestionRequest;
import com.uhn.pmb.entity.*;
import com.uhn.pmb.repository.ExamQuestionRepository;
import com.uhn.pmb.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminExamServiceTest {

    @Mock private ExamQuestionRepository examQuestionRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private AdminExamService adminExamService;

    // ===== Helpers =====

    private User buildUser(Long id, String email) {
        return User.builder().id(id).email(email).build();
    }

    private ExamQuestion buildQuestion(Long id, ApprovalStatus status) {
        ExamQuestion q = new ExamQuestion();
        q.setId(id);
        q.setApprovalStatus(status);
        q.setCategory(QuestionCategory.BAHASA);
        q.setSubject("Bahasa Indonesia");
        q.setDifficulty(QuestionDifficulty.EASY);
        q.setQuestionText("Soal test " + id);
        q.setOptionA("Pilihan A");
        q.setOptionB("Pilihan B");
        q.setOptionC("Pilihan C");
        q.setOptionD("Pilihan D");
        q.setCorrectAnswer("A");
        q.setCreatedBy(buildUser(99L, "creator@test.com"));
        return q;
    }

    private ApproveExamQuestionRequest buildApproveRequest(boolean approved, String rejectionReason) {
        ApproveExamQuestionRequest req = new ApproveExamQuestionRequest();
        req.setApproved(approved);
        req.setRejectionReason(rejectionReason);
        return req;
    }

    // ===== getPendingQuestions =====

    @Test
    @DisplayName("getPendingQuestions - returns list from repository")
    void getPendingQuestions_returnsList() {
        ExamQuestion q = buildQuestion(1L, ApprovalStatus.PENDING);
        when(examQuestionRepository.findAllPending()).thenReturn(List.of(q));

        List<ExamQuestion> result = adminExamService.getPendingQuestions();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
    }

    @Test
    @DisplayName("getPendingQuestions - empty repository returns empty list")
    void getPendingQuestions_empty_returnsEmptyList() {
        when(examQuestionRepository.findAllPending()).thenReturn(List.of());

        List<ExamQuestion> result = adminExamService.getPendingQuestions();

        assertThat(result).isEmpty();
    }

    // ===== getQuestionsByCategory =====

    @Test
    @DisplayName("getQuestionsByCategory - valid category returns approved questions")
    void getQuestionsByCategory_validCategory_returnsList() {
        ExamQuestion q = buildQuestion(1L, ApprovalStatus.APPROVED);
        when(examQuestionRepository.findByCategoryApproved(QuestionCategory.BAHASA))
                .thenReturn(List.of(q));

        List<ExamQuestion> result = adminExamService.getQuestionsByCategory("bahasa");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo(QuestionCategory.BAHASA);
    }

    @Test
    @DisplayName("getQuestionsByCategory - uppercase category input works correctly")
    void getQuestionsByCategory_uppercaseInput_works() {
        when(examQuestionRepository.findByCategoryApproved(QuestionCategory.BAHASA))
                .thenReturn(List.of());

        List<ExamQuestion> result = adminExamService.getQuestionsByCategory("BAHASA");

        assertThat(result).isEmpty();
        verify(examQuestionRepository).findByCategoryApproved(QuestionCategory.BAHASA);
    }

    @Test
    @DisplayName("getQuestionsByCategory - invalid category throws IllegalArgumentException")
    void getQuestionsByCategory_invalidCategory_throwsException() {
        assertThatThrownBy(() -> adminExamService.getQuestionsByCategory("INVALID_CATEGORY"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("getQuestionsByCategory - empty result for valid category returns empty list")
    void getQuestionsByCategory_noResults_returnsEmpty() {
        when(examQuestionRepository.findByCategoryApproved(QuestionCategory.BAHASA))
                .thenReturn(List.of());

        List<ExamQuestion> result = adminExamService.getQuestionsByCategory("bahasa");

        assertThat(result).isEmpty();
    }

    // ===== approveQuestion =====

    @Test
    @DisplayName("approveQuestion - question not found throws RuntimeException")
    void approveQuestion_questionNotFound_throwsException() {
        when(examQuestionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminExamService.approveQuestion(
                99L, buildApproveRequest(true, null), "admin@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Soal tidak ditemukan");
    }

    @Test
    @DisplayName("approveQuestion - user not found throws RuntimeException")
    void approveQuestion_userNotFound_throwsException() {
        ExamQuestion q = buildQuestion(1L, ApprovalStatus.PENDING);
        when(examQuestionRepository.findById(1L)).thenReturn(Optional.of(q));
        when(userRepository.findByEmail("bad@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminExamService.approveQuestion(
                1L, buildApproveRequest(true, null), "bad@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User tidak ditemukan");
    }

    @Test
    @DisplayName("approveQuestion - approved=true sets APPROVED status and approvedAt and approvedBy")
    void approveQuestion_approvedTrue_setsApprovedFields() {
        ExamQuestion q = buildQuestion(1L, ApprovalStatus.PENDING);
        User approver = buildUser(2L, "admin@test.com");

        when(examQuestionRepository.findById(1L)).thenReturn(Optional.of(q));
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(approver));
        when(examQuestionRepository.save(any(ExamQuestion.class))).thenReturn(q);

        ExamQuestion result = adminExamService.approveQuestion(
                1L, buildApproveRequest(true, null), "admin@test.com");

        assertThat(result.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(result.getApprovedAt()).isNotNull();
        assertThat(result.getApprovedBy()).isEqualTo(approver);
        verify(examQuestionRepository).save(q);
    }

    @Test
    @DisplayName("approveQuestion - approved=false sets REJECTED status and rejectionReason")
    void approveQuestion_approvedFalse_setsRejectedFields() {
        ExamQuestion q = buildQuestion(1L, ApprovalStatus.PENDING);
        User approver = buildUser(2L, "admin@test.com");

        when(examQuestionRepository.findById(1L)).thenReturn(Optional.of(q));
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(approver));
        when(examQuestionRepository.save(eq(q))).thenReturn(q);

        ExamQuestion result = adminExamService.approveQuestion(
                1L, buildApproveRequest(false, "Soal tidak relevan"), "admin@test.com");

        assertThat(result.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
        assertThat(result.getRejectionReason()).isEqualTo("Soal tidak relevan");
        verify(examQuestionRepository).save(q);
    }

    @Test
    @DisplayName("approveQuestion - rejected does not set approvedAt or approvedBy")
    void approveQuestion_rejected_doesNotSetApprovedFields() {
        ExamQuestion q = buildQuestion(1L, ApprovalStatus.PENDING);
        User approver = buildUser(2L, "admin@test.com");

        when(examQuestionRepository.findById(1L)).thenReturn(Optional.of(q));
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(approver));
        when(examQuestionRepository.save(eq(q))).thenReturn(q);

        ExamQuestion result = adminExamService.approveQuestion(
                1L, buildApproveRequest(false, "Duplikat"), "admin@test.com");

        assertThat(result.getApprovedAt()).isNull();
        assertThat(result.getApprovedBy()).isNull();
    }

    @Test
    @DisplayName("approveQuestion - approved=false with null rejectionReason saves null")
    void approveQuestion_rejectedWithNullReason_savesNull() {
        ExamQuestion q = buildQuestion(1L, ApprovalStatus.PENDING);
        User approver = buildUser(2L, "admin@test.com");

        when(examQuestionRepository.findById(1L)).thenReturn(Optional.of(q));
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(approver));
        when(examQuestionRepository.save(eq(q))).thenReturn(q);

        ExamQuestion result = adminExamService.approveQuestion(
                1L, buildApproveRequest(false, null), "admin@test.com");

        assertThat(result.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
        assertThat(result.getRejectionReason()).isNull();
    }

    // ===== deleteQuestion =====

    @Test
    @DisplayName("deleteQuestion - not found throws RuntimeException")
    void deleteQuestion_notFound_throwsException() {
        when(examQuestionRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> adminExamService.deleteQuestion(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Soal tidak ditemukan");
    }

    @Test
    @DisplayName("deleteQuestion - found calls deleteById")
    void deleteQuestion_found_callsDeleteById() {
        when(examQuestionRepository.existsById(1L)).thenReturn(true);
        doNothing().when(examQuestionRepository).deleteById(1L);

        adminExamService.deleteQuestion(1L);

        verify(examQuestionRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteQuestion - existsById checked before deleteById (order matters)")
    void deleteQuestion_existsCheckedBeforeDelete() {
        when(examQuestionRepository.existsById(1L)).thenReturn(true);

        var inOrder = inOrder(examQuestionRepository);
        adminExamService.deleteQuestion(1L);

        inOrder.verify(examQuestionRepository).existsById(1L);
        inOrder.verify(examQuestionRepository).deleteById(1L);
    }

    // ===== getQuestion =====

    @Test
    @DisplayName("getQuestion - found returns entity")
    void getQuestion_found_returnsEntity() {
        ExamQuestion q = buildQuestion(1L, ApprovalStatus.APPROVED);
        when(examQuestionRepository.findById(1L)).thenReturn(Optional.of(q));

        ExamQuestion result = adminExamService.getQuestion(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getQuestion - not found throws RuntimeException")
    void getQuestion_notFound_throwsException() {
        when(examQuestionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminExamService.getQuestion(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Soal tidak ditemukan");
    }

    // ===== getAllQuestions =====

    @Test
    @DisplayName("getAllQuestions - null status returns all questions")
    void getAllQuestions_nullStatus_returnsAll() {
        ExamQuestion q1 = buildQuestion(1L, ApprovalStatus.APPROVED);
        ExamQuestion q2 = buildQuestion(2L, ApprovalStatus.PENDING);
        when(examQuestionRepository.findAll()).thenReturn(List.of(q1, q2));

        List<ExamQuestion> result = adminExamService.getAllQuestions(null);

        assertThat(result).hasSize(2);
        verify(examQuestionRepository).findAll();
        verify(examQuestionRepository, never()).findByApprovalStatus(any());
    }

    @Test
    @DisplayName("getAllQuestions - empty string status returns all questions")
    void getAllQuestions_emptyStatus_returnsAll() {
        when(examQuestionRepository.findAll()).thenReturn(List.of());

        List<ExamQuestion> result = adminExamService.getAllQuestions("");

        verify(examQuestionRepository).findAll();
        verify(examQuestionRepository, never()).findByApprovalStatus(any());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getAllQuestions - APPROVED status filters by approval status")
    void getAllQuestions_approvedStatus_filtersApproved() {
        ExamQuestion q = buildQuestion(1L, ApprovalStatus.APPROVED);
        when(examQuestionRepository.findByApprovalStatus(ApprovalStatus.APPROVED))
                .thenReturn(List.of(q));

        List<ExamQuestion> result = adminExamService.getAllQuestions("APPROVED");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
        verify(examQuestionRepository, never()).findAll();
    }

    @Test
    @DisplayName("getAllQuestions - lowercase status is normalised to uppercase")
    void getAllQuestions_lowercaseStatus_normalisedCorrectly() {
        when(examQuestionRepository.findByApprovalStatus(ApprovalStatus.REJECTED))
                .thenReturn(List.of());

        List<ExamQuestion> result = adminExamService.getAllQuestions("rejected");

        verify(examQuestionRepository).findByApprovalStatus(ApprovalStatus.REJECTED);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getAllQuestions - invalid status throws IllegalArgumentException")
    void getAllQuestions_invalidStatus_throwsException() {
        assertThatThrownBy(() -> adminExamService.getAllQuestions("INVALID_STATUS"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("getAllQuestions - PENDING status filters by PENDING")
    void getAllQuestions_pendingStatus_filtersPending() {
        ExamQuestion q = buildQuestion(1L, ApprovalStatus.PENDING);
        when(examQuestionRepository.findByApprovalStatus(ApprovalStatus.PENDING))
                .thenReturn(List.of(q));

        List<ExamQuestion> result = adminExamService.getAllQuestions("PENDING");

        assertThat(result).hasSize(1);
    }

    // ===== getStatistics =====

    @Test
    @DisplayName("getStatistics - returns correct counts for all statuses")
    void getStatistics_returnsCorrectCounts() {
        when(examQuestionRepository.countPendingQuestions()).thenReturn(3L);
        when(examQuestionRepository.findByApprovalStatus(ApprovalStatus.APPROVED))
                .thenReturn(List.of(buildQuestion(1L, ApprovalStatus.APPROVED),
                        buildQuestion(2L, ApprovalStatus.APPROVED)));
        when(examQuestionRepository.findByApprovalStatus(ApprovalStatus.REJECTED))
                .thenReturn(List.of(buildQuestion(3L, ApprovalStatus.REJECTED)));

        Map<String, Object> result = adminExamService.getStatistics();

        assertThat(result.get("pending")).isEqualTo(3L);
        assertThat(result.get("approved")).isEqualTo(2);
        assertThat(result.get("rejected")).isEqualTo(1);
        assertThat(result.get("total")).isEqualTo(6L);
    }

    @Test
    @DisplayName("getStatistics - all zeros when no questions exist")
    void getStatistics_allZeros_whenNoQuestions() {
        when(examQuestionRepository.countPendingQuestions()).thenReturn(0L);
        when(examQuestionRepository.findByApprovalStatus(ApprovalStatus.APPROVED))
                .thenReturn(List.of());
        when(examQuestionRepository.findByApprovalStatus(ApprovalStatus.REJECTED))
                .thenReturn(List.of());

        Map<String, Object> result = adminExamService.getStatistics();

        assertThat(result.get("pending")).isEqualTo(0L);
        assertThat(result.get("approved")).isEqualTo(0);
        assertThat(result.get("rejected")).isEqualTo(0);
        assertThat(result.get("total")).isEqualTo(0L);
    }

    @Test
    @DisplayName("getStatistics - total equals sum of pending + approved + rejected")
    void getStatistics_totalEqualsSum() {
        when(examQuestionRepository.countPendingQuestions()).thenReturn(5L);
        when(examQuestionRepository.findByApprovalStatus(ApprovalStatus.APPROVED))
                .thenReturn(List.of(buildQuestion(1L, ApprovalStatus.APPROVED)));
        when(examQuestionRepository.findByApprovalStatus(ApprovalStatus.REJECTED))
                .thenReturn(List.of(buildQuestion(2L, ApprovalStatus.REJECTED),
                        buildQuestion(3L, ApprovalStatus.REJECTED)));

        Map<String, Object> result = adminExamService.getStatistics();

        long pending = ((Number) result.get("pending")).longValue();
        long approved = ((Number) result.get("approved")).longValue();
        long rejected = ((Number) result.get("rejected")).longValue();
        long total = ((Number) result.get("total")).longValue();

        assertThat(total).isEqualTo(pending + approved + rejected);
    }

    @Test
    @DisplayName("getStatistics - returns all required keys")
    void getStatistics_containsAllKeys() {
        when(examQuestionRepository.countPendingQuestions()).thenReturn(0L);
        when(examQuestionRepository.findByApprovalStatus(any())).thenReturn(List.of());

        Map<String, Object> result = adminExamService.getStatistics();

        assertThat(result).containsKeys("pending", "approved", "rejected", "total");
    }
}