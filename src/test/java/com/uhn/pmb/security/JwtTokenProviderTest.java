package com.uhn.pmb.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private static final String SECRET = "supersecretjwtkeyforhkbpnommensenpmbsystem1234567890abcdefghijklmnopqrstuvwxyz";
    private static final long EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", EXPIRATION);
    }

    // ===== generateToken =====

    @Test
    @DisplayName("generateToken - authentication with no authorities returns valid token")
    void generateToken_noAuthorities_returnsToken() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@example.com");
        Collection<GrantedAuthority> authorities = Collections.emptyList();
        doReturn(authorities).when(auth).getAuthorities();

        String token = jwtTokenProvider.generateToken(auth);

        assertThat(token).isNotNull().isNotEmpty();
        // No authorities claim embedded — getAuthoritiesFromToken should return empty
        assertThat(jwtTokenProvider.getAuthoritiesFromToken(token)).isEmpty();
    }

    @Test
    @DisplayName("generateToken - authentication with single authority embeds authority in token")
    void generateToken_singleAuthority_embedsAuthorityInToken() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin@example.com");
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN_PUSAT"));
        doReturn(authorities).when(auth).getAuthorities();

        String token = jwtTokenProvider.generateToken(auth);

        assertThat(token).isNotNull().isNotEmpty();
        assertThat(jwtTokenProvider.getAuthoritiesFromToken(token)).isEqualTo("ROLE_ADMIN_PUSAT");
    }

    @Test
    @DisplayName("generateToken - authentication with multiple authorities concatenates all")
    void generateToken_multipleAuthorities_concatenatesAll() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@example.com");
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN_PUSAT"),
                new SimpleGrantedAuthority("ROLE_ADMIN_VALIDASI")
        );
        doReturn(authorities).when(auth).getAuthorities();

        String token = jwtTokenProvider.generateToken(auth);

        assertThat(token).isNotNull().isNotEmpty();
        String extractedAuthorities = jwtTokenProvider.getAuthoritiesFromToken(token);
        assertThat(extractedAuthorities).contains("ROLE_ADMIN_PUSAT");
        assertThat(extractedAuthorities).contains("ROLE_ADMIN_VALIDASI");
        assertThat(extractedAuthorities).contains(",");
    }

    // ===== generateTokenFromEmail =====

    @Test
    @DisplayName("generateTokenFromEmail - valid email returns token with correct subject")
    void generateTokenFromEmail_validEmail_returnsToken() {
        String token = jwtTokenProvider.generateTokenFromEmail("user@test.com");

        assertThat(token).isNotNull().isNotEmpty();
        assertThat(jwtTokenProvider.getEmailFromToken(token)).isEqualTo("user@test.com");
    }

    // ===== generateTokenFromEmailWithAuthorities =====

    @Test
    @DisplayName("generateTokenFromEmailWithAuthorities - with role returns token containing role")
    void generateTokenFromEmailWithAuthorities_withRole_returnsToken() {
        String token = jwtTokenProvider.generateTokenFromEmailWithAuthorities("admin@test.com", "ROLE_ADMIN");

        assertThat(token).isNotNull().isNotEmpty();
        assertThat(jwtTokenProvider.getAuthoritiesFromToken(token)).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("generateTokenFromEmailWithAuthorities - empty authorities does not embed claim")
    void generateTokenFromEmailWithAuthorities_emptyAuthorities_noAuthorityClaim() {
        String token = jwtTokenProvider.generateTokenFromEmailWithAuthorities("user@test.com", "");

        assertThat(token).isNotNull().isNotEmpty();
        assertThat(jwtTokenProvider.getAuthoritiesFromToken(token)).isEmpty();
    }

    @Test
    @DisplayName("generateTokenFromEmailWithAuthorities - null authorities does not embed claim")
    void generateTokenFromEmailWithAuthorities_nullAuthorities_noAuthorityClaim() {
        String token = jwtTokenProvider.generateTokenFromEmailWithAuthorities("user@test.com", null);

        assertThat(token).isNotNull().isNotEmpty();
        assertThat(jwtTokenProvider.getAuthoritiesFromToken(token)).isEmpty();
    }

    @Test
    @DisplayName("generateTokenFromEmailWithAuthorities - null secret throws RuntimeException")
    void generateTokenFromEmailWithAuthorities_nullSecret_throwsRuntimeException() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", null);

        assertThatThrownBy(() ->
                jwtTokenProvider.generateTokenFromEmailWithAuthorities("user@test.com", "ROLE_ADMIN"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("JWT secret is not configured");
    }

    @Test
    @DisplayName("generateTokenFromEmailWithAuthorities - empty secret throws RuntimeException")
    void generateTokenFromEmailWithAuthorities_emptySecret_throwsRuntimeException() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "");

        assertThatThrownBy(() ->
                jwtTokenProvider.generateTokenFromEmailWithAuthorities("user@test.com", "ROLE_ADMIN"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("JWT secret is not configured");
    }

    // ===== getEmailFromToken =====

    @Test
    @DisplayName("getEmailFromToken - valid token returns correct email")
    void getEmailFromToken_validToken_returnsEmail() {
        String token = jwtTokenProvider.generateTokenFromEmail("user@test.com");

        String email = jwtTokenProvider.getEmailFromToken(token);

        assertThat(email).isEqualTo("user@test.com");
    }

    @Test
    @DisplayName("getEmailFromToken - invalid token returns null")
    void getEmailFromToken_invalidToken_returnsNull() {
        String email = jwtTokenProvider.getEmailFromToken("invalid.token.here");

        assertThat(email).isNull();
    }

    @Test
    @DisplayName("getEmailFromToken - null secret returns null")
    void getEmailFromToken_nullSecret_returnsNull() {
        String token = jwtTokenProvider.generateTokenFromEmail("user@test.com");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", null);

        String email = jwtTokenProvider.getEmailFromToken(token);

        assertThat(email).isNull();
    }

    @Test
    @DisplayName("getEmailFromToken - empty secret returns null")
    void getEmailFromToken_emptySecret_returnsNull() {
        String token = jwtTokenProvider.generateTokenFromEmail("user@test.com");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "");

        String email = jwtTokenProvider.getEmailFromToken(token);

        assertThat(email).isNull();
    }

    @Test
    @DisplayName("getEmailFromToken - token signed with different secret returns null")
    void getEmailFromToken_differentSecret_returnsNull() {
        String token = jwtTokenProvider.generateTokenFromEmail("user@test.com");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret",
                "completelydifferentsecretkey1234567890abcdefghijklmnopqrstuvwxyzextra");

        String email = jwtTokenProvider.getEmailFromToken(token);

        assertThat(email).isNull();
    }

    // ===== getAuthoritiesFromToken =====

    @Test
    @DisplayName("getAuthoritiesFromToken - token with authority returns authority string")
    void getAuthoritiesFromToken_tokenWithAuthority_returnsAuthority() {
        String token = jwtTokenProvider.generateTokenFromEmailWithAuthorities("user@test.com", "ROLE_CAMABA");

        String authorities = jwtTokenProvider.getAuthoritiesFromToken(token);

        assertThat(authorities).isEqualTo("ROLE_CAMABA");
    }

    @Test
    @DisplayName("getAuthoritiesFromToken - token without authority returns empty string")
    void getAuthoritiesFromToken_tokenWithoutAuthority_returnsEmpty() {
        String token = jwtTokenProvider.generateTokenFromEmail("user@test.com");

        String authorities = jwtTokenProvider.getAuthoritiesFromToken(token);

        assertThat(authorities).isEmpty();
    }

    @Test
    @DisplayName("getAuthoritiesFromToken - invalid token returns empty string")
    void getAuthoritiesFromToken_invalidToken_returnsEmpty() {
        String authorities = jwtTokenProvider.getAuthoritiesFromToken("not.a.valid.jwt");

        assertThat(authorities).isEmpty();
    }

    @Test
    @DisplayName("getAuthoritiesFromToken - null secret returns empty string")
    void getAuthoritiesFromToken_nullSecret_returnsEmpty() {
        String token = jwtTokenProvider.generateTokenFromEmail("user@test.com");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", null);

        String authorities = jwtTokenProvider.getAuthoritiesFromToken(token);

        assertThat(authorities).isEmpty();
    }

    @Test
    @DisplayName("getAuthoritiesFromToken - empty secret returns empty string")
    void getAuthoritiesFromToken_emptySecret_returnsEmpty() {
        String token = jwtTokenProvider.generateTokenFromEmail("user@test.com");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "");

        String authorities = jwtTokenProvider.getAuthoritiesFromToken(token);

        assertThat(authorities).isEmpty();
    }

    // ===== validateToken =====

    @Test
    @DisplayName("validateToken - valid token returns true")
    void validateToken_validToken_returnsTrue() {
        String token = jwtTokenProvider.generateTokenFromEmail("user@test.com");

        Boolean valid = jwtTokenProvider.validateToken(token);

        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("validateToken - invalid token returns false")
    void validateToken_invalidToken_returnsFalse() {
        Boolean valid = jwtTokenProvider.validateToken("not.a.valid.token");

        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("validateToken - null secret returns false")
    void validateToken_nullSecret_returnsFalse() {
        String token = jwtTokenProvider.generateTokenFromEmail("user@test.com");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", null);

        Boolean valid = jwtTokenProvider.validateToken(token);

        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("validateToken - empty secret returns false")
    void validateToken_emptySecret_returnsFalse() {
        String token = jwtTokenProvider.generateTokenFromEmail("user@test.com");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "");

        Boolean valid = jwtTokenProvider.validateToken(token);

        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("validateToken - token signed with different secret returns false")
    void validateToken_tokenFromDifferentSecret_returnsFalse() {
        String token = jwtTokenProvider.generateTokenFromEmail("user@test.com");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret",
                "anothertotallydifferentsecretkey1234567890abcdefghijklmnopqrstuvwxyz");

        Boolean valid = jwtTokenProvider.validateToken(token);

        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("validateToken - malformed token string returns false")
    void validateToken_malformedToken_returnsFalse() {
        Boolean valid = jwtTokenProvider.validateToken("completelynotajwt");

        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("generateTokenFromEmail - null secret throws RuntimeException with message")
    void generateTokenFromEmail_nullSecret_throwsExceptionWithMessage() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", null);

        assertThatThrownBy(() -> jwtTokenProvider.generateTokenFromEmail("user@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("JWT secret is not configured");
    }
}