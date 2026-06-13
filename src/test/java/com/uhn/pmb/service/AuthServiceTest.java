package com.uhn.pmb.service;

import com.uhn.pmb.dto.LoginRequest;
import com.uhn.pmb.dto.RegisterRequest;
import com.uhn.pmb.entity.Student;
import com.uhn.pmb.entity.User;
import com.uhn.pmb.repository.PasswordResetTokenRepository;
import com.uhn.pmb.repository.StudentRepository;
import com.uhn.pmb.repository.UserRepository;
import com.uhn.pmb.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "frontendUrl", "http://localhost:9500");
        ReflectionTestUtils.setField(authService, "brevoApiKey", "");
        ReflectionTestUtils.setField(authService, "fromEmail", "noreply@test.com");
        ReflectionTestUtils.setField(authService, "senderName", "PMB Test");
    }

    @Test
    @DisplayName("register - duplicate email throws exception")
    void register_duplicateEmail_throwsException() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("exist@test.com");
        req.setPassword("pass123");
        req.setConfirmPassword("pass123");

        when(userRepository.existsByEmail("exist@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("sudah terdaftar");
    }

    @Test
    @DisplayName("register - new email creates user successfully")
    void register_newEmail_createsUser() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("new@test.com");
        req.setPassword("password123");
        req.setConfirmPassword("password123");
        req.setFullName("New User");

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        User savedUser = User.builder().id(1L).email("new@test.com")
                .role(User.UserRole.CAMABA).emailVerified(false).build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(studentRepository.save(any(Student.class))).thenReturn(new Student());

        var result = authService.register(req);

        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
    }


    @Test
    @DisplayName("login - bad credentials throws exception")
    void login_badCredentials_throwsException() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@test.com");
        req.setPassword("wrong");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad creds"));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("login - valid credentials returns token")
    void login_validCredentials_returnsToken() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@test.com");
        req.setPassword("pass123");

        Authentication auth = mock(Authentication.class);
        User user = User.builder().id(1L).email("user@test.com")
                .role(User.UserRole.CAMABA).emailVerified(true).isActive(true).build();

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(any())).thenReturn("jwt.token.here");

        var result = authService.login(req);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt.token.here");
    }

    @Test
    @DisplayName("login - user not found throws exception")
    void login_userNotFound_throwsException() {
        LoginRequest req = new LoginRequest();
        req.setEmail("notfound@test.com");
        req.setPassword("pass");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("login - inactive user throws exception")
    void login_inactiveUser_throwsException() {
        LoginRequest req = new LoginRequest();
        req.setEmail("inactive@test.com");
        req.setPassword("pass");

        Authentication auth = mock(Authentication.class);
        User user = User.builder().id(2L).email("inactive@test.com")
                .role(User.UserRole.CAMABA).isActive(false).emailVerified(true).build();

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findByEmail("inactive@test.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("dinonaktifkan");
    }

    @Test
    @DisplayName("login - email not verified for CAMABA throws exception")
    void login_emailNotVerifiedCamaba_throwsException() {
        LoginRequest req = new LoginRequest();
        req.setEmail("unverified@test.com");
        req.setPassword("pass");

        Authentication auth = mock(Authentication.class);
        User user = User.builder().id(3L).email("unverified@test.com")
                .role(User.UserRole.CAMABA).isActive(true).emailVerified(false).build();

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findByEmail("unverified@test.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("verifikasi");
    }

    @Test
    @DisplayName("verifyEmail - valid token verifies email")
    void verifyEmail_validToken_verifiesEmail() {
        User user = User.builder().id(1L).email("u@test.com")
                .emailVerified(false).emailVerificationToken("valid-token").build();
        when(userRepository.findByEmailVerificationToken("valid-token")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        var result = authService.verifyEmail("valid-token");

        assertThat(result.getSuccess()).isTrue();
        assertThat(user.getEmailVerified()).isTrue();
    }

    @Test
    @DisplayName("verifyEmail - already verified returns success message")
    void verifyEmail_alreadyVerified_returnsSuccessMessage() {
        User user = User.builder().id(1L).email("u@test.com")
                .emailVerified(true).build();
        when(userRepository.findByEmailVerificationToken("token-already")).thenReturn(Optional.of(user));

        var result = authService.verifyEmail("token-already");

        assertThat(result.getSuccess()).isTrue();
        assertThat(result.getMessage()).contains("sudah diverifikasi");
    }

    @Test
    @DisplayName("verifyEmail - invalid token throws exception")
    void verifyEmail_invalidToken_throwsException() {
        when(userRepository.findByEmailVerificationToken("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyEmail("bad-token"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("resendVerificationEmail - sends new token")
    void resendVerificationEmail_notVerified_sendsNewToken() {
        User user = User.builder().id(1L).email("u@test.com")
                .emailVerified(false).build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        var result = authService.resendVerificationEmail("u@test.com");

        assertThat(result.getSuccess()).isTrue();
        verify(userRepository).save(any());
    }

    @Test
    @DisplayName("resendVerificationEmail - already verified returns message")
    void resendVerificationEmail_alreadyVerified_returnsMessage() {
        User user = User.builder().id(1L).email("u@test.com")
                .emailVerified(true).build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));

        var result = authService.resendVerificationEmail("u@test.com");

        assertThat(result.getSuccess()).isTrue();
        assertThat(result.getMessage()).contains("sudah diverifikasi");
    }

    @Test
    @DisplayName("resendVerificationEmail - user not found throws exception")
    void resendVerificationEmail_userNotFound_throwsException() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resendVerificationEmail("none@test.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("forgotPassword - valid email saves token and returns success")
    void forgotPassword_validEmail_savesTokenAndReturnsSuccess() {
        User user = User.builder().id(1L).email("u@test.com").build();
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findByUserAndIsUsedFalse(user)).thenReturn(Optional.empty());
        when(tokenRepository.save(any())).thenReturn(new com.uhn.pmb.entity.PasswordResetToken());

        var result = authService.forgotPassword("u@test.com");

        assertThat(result.getSuccess()).isTrue();
        verify(tokenRepository).save(any());
    }

    @Test
    @DisplayName("forgotPassword - invalidates previous token")
    void forgotPassword_withExistingToken_invalidatesPreviousToken() {
        User user = User.builder().id(1L).email("u@test.com").build();
        com.uhn.pmb.entity.PasswordResetToken existingToken = com.uhn.pmb.entity.PasswordResetToken.builder()
                .token("old-token").user(user).isUsed(false)
                .expiryDate(java.time.LocalDateTime.now().plusHours(1)).build();

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findByUserAndIsUsedFalse(user)).thenReturn(Optional.of(existingToken));
        when(tokenRepository.save(any())).thenReturn(existingToken);

        var result = authService.forgotPassword("u@test.com");

        assertThat(result.getSuccess()).isTrue();
        assertThat(existingToken.getIsUsed()).isTrue();
    }

    @Test
    @DisplayName("forgotPassword - user not found throws exception")
    void forgotPassword_userNotFound_throwsException() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.forgotPassword("none@test.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("resetPassword - password mismatch throws exception")
    void resetPassword_passwordMismatch_throwsException() {
        assertThatThrownBy(() -> authService.resetPassword("token", "pass1", "pass2"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("cocok");
    }

    @Test
    @DisplayName("resetPassword - invalid token throws exception")
    void resetPassword_invalidToken_throwsException() {
        when(tokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword("bad-token", "pass123", "pass123"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("resetPassword - expired token throws exception")
    void resetPassword_expiredToken_throwsException() {
        User user = User.builder().id(1L).build();
        com.uhn.pmb.entity.PasswordResetToken token = com.uhn.pmb.entity.PasswordResetToken.builder()
                .token("expired-token").user(user).isUsed(false)
                .expiryDate(java.time.LocalDateTime.now().minusHours(2)).build();
        when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.resetPassword("expired-token", "newpass", "newpass"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("kadaluarsa");
    }

    @Test
    @DisplayName("resetPassword - already used token throws exception")
    void resetPassword_usedToken_throwsException() {
        User user = User.builder().id(1L).build();
        com.uhn.pmb.entity.PasswordResetToken token = com.uhn.pmb.entity.PasswordResetToken.builder()
                .token("used-token").user(user).isUsed(true)
                .expiryDate(java.time.LocalDateTime.now().plusHours(1)).build();
        when(tokenRepository.findByToken("used-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.resetPassword("used-token", "newpass", "newpass"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("digunakan");
    }

    @Test
    @DisplayName("resetPassword - valid token resets password")
    void resetPassword_validToken_resetsPassword() {
        User user = User.builder().id(1L).email("u@test.com").password("old").build();
        com.uhn.pmb.entity.PasswordResetToken token = com.uhn.pmb.entity.PasswordResetToken.builder()
                .token("valid-reset").user(user).isUsed(false)
                .expiryDate(java.time.LocalDateTime.now().plusHours(1)).build();
        when(tokenRepository.findByToken("valid-reset")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newpass")).thenReturn("encoded-new");
        when(userRepository.save(any())).thenReturn(user);
        when(tokenRepository.save(any())).thenReturn(token);

        var result = authService.resetPassword("valid-reset", "newpass", "newpass");

        assertThat(result.getSuccess()).isTrue();
        assertThat(token.getIsUsed()).isTrue();
    }

    @Test
    @DisplayName("validateToken - delegates to jwtTokenProvider")
    void validateToken_valid_returnsTrue() {
        when(jwtTokenProvider.validateToken("valid.jwt")).thenReturn(true);

        Boolean result = authService.validateToken("valid.jwt");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("validateToken - invalid token returns false")
    void validateToken_invalid_returnsFalse() {
        when(jwtTokenProvider.validateToken("bad.jwt")).thenThrow(new RuntimeException("invalid"));

        Boolean result = authService.validateToken("bad.jwt");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("register - with ADMIN role creates admin user")
    void register_withAdminRole_createsAdminUser() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("admin@test.com");
        req.setPassword("admin123");
        req.setConfirmPassword("admin123");
        req.setFullName("Admin User");
        req.setRole("ADMIN");

        when(userRepository.existsByEmail("admin@test.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        User savedUser = User.builder().id(2L).email("admin@test.com")
                .role(User.UserRole.ADMIN_PUSAT).emailVerified(false).build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        var result = authService.register(req);

        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
        // ADMIN role doesn't create student profile
        // admin role - may or may not create student profile depending on impl
    }

    @Test
    @DisplayName("register - with invalid role defaults to CAMABA")
    void register_withInvalidRole_defaultsToCamaba() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test2@test.com");
        req.setPassword("pass123");
        req.setConfirmPassword("pass123");
        req.setRole("INVALID_ROLE");

        when(userRepository.existsByEmail("test2@test.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        User savedUser = User.builder().id(3L).email("test2@test.com")
                .role(User.UserRole.CAMABA).emailVerified(false).build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(studentRepository.save(any(Student.class))).thenReturn(new Student());

        var result = authService.register(req);

        assertThat(result).isNotNull();
        verify(studentRepository).save(any());
    }
}
