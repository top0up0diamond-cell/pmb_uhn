package com.uhn.pmb.service;

import com.uhn.pmb.entity.*;
import com.uhn.pmb.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamTokenServiceTest {

    @Mock private ExamTokenRepository tokenRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private ExamSubmissionRepository submissionRepository;
    @Mock private AdmissionFormRepository formRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;
    @Mock private WhatsAppService whatsAppService;

    @InjectMocks
    private ExamTokenService examTokenService;

    @Test
    @DisplayName("generateToken - student not found throws RuntimeException")
    void generateToken_studentNotFound_throws() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> examTokenService.generateToken(99L, 1L, 120))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("generateToken - student found generates token")
    void generateToken_studentFound_generatesToken() {
        User u = User.builder().id(1L).email("s@test.com").build();
        Student student = Student.builder().id(1L).fullName("John").user(u).build();
        ExamToken saved = ExamToken.builder().id(1L).student(student).build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(tokenRepository.findAllByStudentId(1L)).thenReturn(List.of());
        when(tokenRepository.save(any())).thenReturn(saved);

        ExamToken result = examTokenService.generateToken(1L, 1L, 120);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("revokeToken - token not found throws RuntimeException")
    void revokeToken_notFound_throws() {
        when(tokenRepository.findByTokenValue("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> examTokenService.revokeToken("invalid", "test"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getValidatedStudentsWithTokens - returns list")
    void getValidatedStudentsWithTokens_returnsList() {
        when(tokenRepository.findByStatus(ExamToken.TokenStatus.ACTIVE)).thenReturn(List.of());

        List<?> result = examTokenService.getValidatedStudentsWithTokens();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getExamStatistics - returns statistics map")
    void getExamStatistics_returnsMap() {
        when(tokenRepository.count()).thenReturn(10L);
        when(tokenRepository.countByStatus(ExamToken.TokenStatus.ACTIVE)).thenReturn(5L);
        when(tokenRepository.countByStatus(ExamToken.TokenStatus.USED)).thenReturn(3L);
        when(tokenRepository.countByStatus(ExamToken.TokenStatus.EXPIRED)).thenReturn(1L);
        when(tokenRepository.countByStatus(ExamToken.TokenStatus.REVOKED)).thenReturn(1L);
        when(submissionRepository.count()).thenReturn(3L);
        when(submissionRepository.countByStatus(ExamSubmission.SubmissionStatus.COMPLETED)).thenReturn(2L);

        Map<String, Object> result = examTokenService.getExamStatistics();

        assertThat(result).containsKey("totalTokens");
        assertThat(result.get("totalTokens")).isEqualTo(10L);
    }

    @Test
    @DisplayName("validateToken - valid token returns response")
    void validateToken_valid_returnsResponse() {
        ExamToken token = mock(ExamToken.class);
        User user = User.builder().id(1L).email("s@test.com").build();
        Student student = Student.builder().id(1L).fullName("John Doe").user(user).build();

        when(tokenRepository.findByTokenValue("test-token")).thenReturn(Optional.of(token));
        when(token.getStudent()).thenReturn(student);
        when(token.isActive()).thenReturn(true);
        when(token.isExpired()).thenReturn(false);
        when(token.getExpiresAt()).thenReturn(LocalDateTime.now().plusHours(2));

        com.uhn.pmb.dto.ExamTokenDTO.ValidateTokenResponse result =
                examTokenService.validateToken("test-token", 1L);

        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("validateToken - wrong student throws RuntimeException")
    void validateToken_wrongStudent_throws() {
        ExamToken token = mock(ExamToken.class);
        User user = User.builder().id(2L).email("other@test.com").build();
        Student student = Student.builder().id(2L).user(user).build();
        when(tokenRepository.findByTokenValue("test-token")).thenReturn(Optional.of(token));
        when(token.getStudent()).thenReturn(student);

        assertThatThrownBy(() -> examTokenService.validateToken("test-token", 1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("validateToken - inactive token throws RuntimeException")
    void validateToken_inactiveToken_throws() {
        ExamToken token = mock(ExamToken.class);
        Student student = Student.builder().id(1L).build();
        when(tokenRepository.findByTokenValue("test-token")).thenReturn(Optional.of(token));
        when(token.getStudent()).thenReturn(student);
        when(token.isActive()).thenReturn(false);
        when(token.getStatus()).thenReturn(ExamToken.TokenStatus.REVOKED);

        assertThatThrownBy(() -> examTokenService.validateToken("test-token", 1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("revokeToken - found token revokes successfully")
    void revokeToken_found_revokesToken() {
        ExamToken token = new ExamToken();
        token.setStatus(ExamToken.TokenStatus.ACTIVE);
        when(tokenRepository.findByTokenValue("test-token")).thenReturn(Optional.of(token));
        when(tokenRepository.save(any())).thenReturn(token);

        examTokenService.revokeToken("test-token", "Test reason");

        verify(tokenRepository).save(any());
        assertThat(token.getStatus()).isEqualTo(ExamToken.TokenStatus.REVOKED);
    }

    @Test
    @DisplayName("submitExamResult - duplicate submission throws RuntimeException")
    void submitExamResult_duplicateSubmission_throws() {
        ExamToken token = mock(ExamToken.class);
        Student student = Student.builder().id(1L).build();
        when(tokenRepository.findByTokenValue("test-token")).thenReturn(Optional.of(token));
        when(token.getStudent()).thenReturn(student);
        when(token.isActive()).thenReturn(true);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(submissionRepository.findByStudentId(1L)).thenReturn(Optional.of(new ExamSubmission()));

        com.uhn.pmb.dto.ExamTokenDTO.SubmitResultRequest req = new com.uhn.pmb.dto.ExamTokenDTO.SubmitResultRequest();
        req.setToken("test-token");
        req.setStudentId(1L);

        assertThatThrownBy(() -> examTokenService.submitExamResult(req))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("submitExamResult - valid request creates submission")
    void submitExamResult_valid_returnsSubmission() {
        ExamToken token = mock(ExamToken.class);
        User user = User.builder().id(1L).email("s@test.com").build();
        Student student = Student.builder().id(1L).fullName("John").user(user).build();
        ExamSubmission sub = new ExamSubmission();
        sub.setId(1L);
        sub.setSubmittedAt(LocalDateTime.now());

        when(tokenRepository.findByTokenValue("test-token")).thenReturn(Optional.of(token));
        when(token.getStudent()).thenReturn(student);
        when(token.isActive()).thenReturn(true);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(submissionRepository.findByStudentId(1L)).thenReturn(Optional.empty());
        when(submissionRepository.save(any())).thenReturn(sub);
        when(tokenRepository.save(any())).thenReturn(token);

        com.uhn.pmb.dto.ExamTokenDTO.SubmitResultRequest req = new com.uhn.pmb.dto.ExamTokenDTO.SubmitResultRequest();
        req.setToken("test-token");
        req.setStudentId(1L);
        req.setScore(85);
        req.setPassed(true);

        ExamSubmission result = examTokenService.submitExamResult(req);

        assertThat(result.getId()).isEqualTo(1L);
        verify(submissionRepository).save(any());
    }

    @Test
    @DisplayName("generateToken - existing active tokens get revoked")
    void generateToken_existingActiveTokens_revokesOld() {
        User u = User.builder().id(1L).email("s@test.com").build();
        Student student = Student.builder().id(1L).fullName("John").user(u).build();
        ExamToken oldToken = new ExamToken();
        oldToken.setStatus(ExamToken.TokenStatus.ACTIVE);
        ExamToken saved = ExamToken.builder().id(2L).student(student).build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(tokenRepository.findAllByStudentId(1L)).thenReturn(List.of(oldToken));
        when(tokenRepository.save(any())).thenReturn(saved);

        examTokenService.generateToken(1L, 1L, 120);

        assertThat(oldToken.getStatus()).isEqualTo(ExamToken.TokenStatus.REVOKED);
    }

    @Test
    @DisplayName("generateToken - null expirationMinutes uses default 120")
    void generateToken_nullExpiration_uses120() {
        User u = User.builder().id(1L).email("s@test.com").build();
        Student student = Student.builder().id(1L).fullName("John").user(u).build();
        ExamToken saved = ExamToken.builder().id(1L).student(student)
                .expiresAt(LocalDateTime.now().plusMinutes(120)).build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(tokenRepository.findAllByStudentId(1L)).thenReturn(List.of());
        when(tokenRepository.save(any())).thenReturn(saved);

        ExamToken result = examTokenService.generateToken(1L, 1L, null);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("validateToken - expired token marks EXPIRED and throws")
    void validateToken_expired_marksExpiredAndThrows() {
        ExamToken token = mock(ExamToken.class);
        Student student = Student.builder().id(1L).build();
        when(tokenRepository.findByTokenValue("test-token")).thenReturn(Optional.of(token));
        when(token.getStudent()).thenReturn(student);
        when(token.isActive()).thenReturn(true);
        when(token.isExpired()).thenReturn(true);
        when(tokenRepository.save(any())).thenReturn(token);

        assertThatThrownBy(() -> examTokenService.validateToken("test-token", 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("getValidatedStudentsWithTokens - with tokens returns list with info")
    void getValidatedStudentsWithTokens_withTokens_returnsList() {
        User u = User.builder().id(1L).email("s@test.com").build();
        Student student = Student.builder().id(1L).fullName("Jane").user(u).build();
        ExamToken token = ExamToken.builder()
                .id(1L).student(student).status(ExamToken.TokenStatus.ACTIVE)
                .createdAt(LocalDateTime.now()).expiresAt(LocalDateTime.now().plusHours(2)).build();

        when(tokenRepository.findByStatus(ExamToken.TokenStatus.ACTIVE)).thenReturn(List.of(token));
        when(submissionRepository.findByStudentId(1L)).thenReturn(Optional.empty());

        List<?> result = examTokenService.getValidatedStudentsWithTokens();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getExamStatistics - no tokens returns zeros")
    void getExamStatistics_noTokens_returnsZeros() {
        when(tokenRepository.count()).thenReturn(0L);
        when(tokenRepository.countByStatus(ExamToken.TokenStatus.ACTIVE)).thenReturn(0L);
        when(tokenRepository.countByStatus(ExamToken.TokenStatus.USED)).thenReturn(0L);
        when(tokenRepository.countByStatus(ExamToken.TokenStatus.EXPIRED)).thenReturn(0L);
        when(tokenRepository.countByStatus(ExamToken.TokenStatus.REVOKED)).thenReturn(0L);
        when(submissionRepository.count()).thenReturn(0L);
        when(submissionRepository.countByStatus(ExamSubmission.SubmissionStatus.COMPLETED)).thenReturn(0L);

        Map<String, Object> result = examTokenService.getExamStatistics();

        assertThat(result.get("totalTokens")).isEqualTo(0L);
    }

    @Test
    @DisplayName("submitExamResult - token not found throws RuntimeException")
    void submitExamResult_tokenNotFound_throws() {
        when(tokenRepository.findByTokenValue("bad-token")).thenReturn(Optional.empty());

        com.uhn.pmb.dto.ExamTokenDTO.SubmitResultRequest req = new com.uhn.pmb.dto.ExamTokenDTO.SubmitResultRequest();
        req.setToken("bad-token");
        req.setStudentId(1L);

        assertThatThrownBy(() -> examTokenService.submitExamResult(req))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("submitExamResult - wrong student ID throws RuntimeException")
    void submitExamResult_wrongStudentId_throws() {
        ExamToken token = mock(ExamToken.class);
        Student student = Student.builder().id(99L).build();
        when(tokenRepository.findByTokenValue("test-token")).thenReturn(Optional.of(token));
        when(token.getStudent()).thenReturn(student);

        com.uhn.pmb.dto.ExamTokenDTO.SubmitResultRequest req = new com.uhn.pmb.dto.ExamTokenDTO.SubmitResultRequest();
        req.setToken("test-token");
        req.setStudentId(1L);

        assertThatThrownBy(() -> examTokenService.submitExamResult(req))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("revokeToken - already revoked token still saves")
    void revokeToken_alreadyRevoked_stillSaves() {
        ExamToken token = new ExamToken();
        token.setStatus(ExamToken.TokenStatus.REVOKED);
        when(tokenRepository.findByTokenValue("test-token")).thenReturn(Optional.of(token));
        when(tokenRepository.save(any())).thenReturn(token);

        examTokenService.revokeToken("test-token", "Test reason");

        verify(tokenRepository).save(any());
        assertThat(token.getStatus()).isEqualTo(ExamToken.TokenStatus.REVOKED);
    }
}
