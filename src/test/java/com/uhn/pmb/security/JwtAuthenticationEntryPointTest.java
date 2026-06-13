package com.uhn.pmb.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationEntryPointTest {

    @InjectMocks
    private JwtAuthenticationEntryPoint entryPoint;

    // ===== Helper =====

    private MockHttpServletResponse doCommence(String method, String path,
                                               String acceptHeader,
                                               AuthenticationException ex) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        if (acceptHeader != null) request.addHeader("Accept", acceptHeader);
        MockHttpServletResponse response = new MockHttpServletResponse();
        entryPoint.commence(request, response, ex);
        return response;
    }

    // ===== Public pages — isPublicPage() branch =====

    @Test
    @DisplayName("commence - /login.html (public page) returns 401 JSON without redirect")
    void commence_loginHtml_returns401NoRedirect() throws Exception {
        MockHttpServletResponse response = doCommence("GET", "/login.html", null,
                new InsufficientAuthenticationException("need auth"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getRedirectedUrl()).isNull();
        assertThat(response.getContentAsString()).contains("UNAUTHORIZED");
    }

    @Test
    @DisplayName("commence - /register.html (public page) returns 401 JSON without redirect")
    void commence_registerHtml_returns401NoRedirect() throws Exception {
        MockHttpServletResponse response = doCommence("GET", "/register.html", null,
                new InsufficientAuthenticationException("need auth"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getRedirectedUrl()).isNull();
    }

    @Test
    @DisplayName("commence - /forgot-password.html (public page) returns 401 JSON without redirect")
    void commence_forgotPasswordHtml_returns401NoRedirect() throws Exception {
        MockHttpServletResponse response = doCommence("GET", "/forgot-password.html", null,
                new InsufficientAuthenticationException("need auth"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getRedirectedUrl()).isNull();
    }

    @Test
    @DisplayName("commence - /index.html (public page) returns 401 JSON without redirect")
    void commence_indexHtml_returns401NoRedirect() throws Exception {
        MockHttpServletResponse response = doCommence("GET", "/index.html", null,
                new InsufficientAuthenticationException("need auth"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getRedirectedUrl()).isNull();
    }

    @Test
    @DisplayName("commence - / root path (public page) returns 401 JSON without redirect")
    void commence_rootPath_returns401NoRedirect() throws Exception {
        MockHttpServletResponse response = doCommence("GET", "/", null,
                new InsufficientAuthenticationException("need auth"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getRedirectedUrl()).isNull();
    }

    @Test
    @DisplayName("commence - /api/camaba/debug-auth (public API) returns 401 JSON without redirect")
    void commence_camabaDebugAuth_returns401NoRedirect() throws Exception {
        MockHttpServletResponse response = doCommence("GET", "/api/camaba/debug-auth", null,
                new InsufficientAuthenticationException("need auth"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getRedirectedUrl()).isNull();
        assertThat(response.getContentAsString()).contains("UNAUTHORIZED");
    }

    @Test
    @DisplayName("commence - /api/admin/debug-auth (public API) returns 401 JSON without redirect")
    void commence_adminDebugAuth_returns401NoRedirect() throws Exception {
        MockHttpServletResponse response = doCommence("GET", "/api/admin/debug-auth", null,
                new InsufficientAuthenticationException("need auth"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getRedirectedUrl()).isNull();
        assertThat(response.getContentAsString()).contains("UNAUTHORIZED");
    }

    @Test
    @DisplayName("commence - /ujian.html (public page) returns 401 JSON without redirect")
    void commence_ujianHtml_returns401NoRedirect() throws Exception {
        MockHttpServletResponse response = doCommence("GET", "/ujian.html", null,
                new InsufficientAuthenticationException("need auth"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getRedirectedUrl()).isNull();
    }

    // ===== HTML page redirect — isHtmlPageRequest() branch =====

    @Test
    @DisplayName("commence - non-public .html path with Accept text/html redirects to login")
    void commence_nonPublicHtmlWithAcceptHeader_redirectsToLogin() throws Exception {
        MockHttpServletResponse response = doCommence("GET", "/admin/dashboard.html",
                "text/html,application/xhtml+xml",
                new InsufficientAuthenticationException("need auth"));

        assertThat(response.getRedirectedUrl()).isEqualTo("/login.html");
    }

    @Test
    @DisplayName("commence - non-public .html path without Accept header redirects to login")
    void commence_nonPublicHtmlNoAcceptHeader_redirectsToLogin() throws Exception {
        MockHttpServletResponse response = doCommence("GET", "/some-page.html", null,
                new InsufficientAuthenticationException("need auth"));

        assertThat(response.getRedirectedUrl()).isEqualTo("/login.html");
    }

    @Test
    @DisplayName("commence - path containing /dashboard redirects to login")
    void commence_pathContainsDashboard_redirectsToLogin() throws Exception {
        MockHttpServletResponse response = doCommence("GET", "/admin/dashboard", null,
                new InsufficientAuthenticationException("need auth"));

        assertThat(response.getRedirectedUrl()).isEqualTo("/login.html");
    }

    @Test
    @DisplayName("commence - path containing /profile redirects to login")
    void commence_pathContainsProfile_redirectsToLogin() throws Exception {
        MockHttpServletResponse response = doCommence("GET", "/user/profile", null,
                new InsufficientAuthenticationException("need auth"));

        assertThat(response.getRedirectedUrl()).isEqualTo("/login.html");
    }

    @Test
    @DisplayName("commence - path containing /form-pendaftaran redirects to login")
    void commence_pathContainsFormPendaftaran_redirectsToLogin() throws Exception {
        MockHttpServletResponse response = doCommence("GET", "/form-pendaftaran/step1", null,
                new InsufficientAuthenticationException("need auth"));

        assertThat(response.getRedirectedUrl()).isEqualTo("/login.html");
    }

    @Test
    @DisplayName("commence - path containing /gelombang redirects to login")
    void commence_pathContainsGelombang_redirectsToLogin() throws Exception {
        MockHttpServletResponse response = doCommence("GET", "/gelombang/list", null,
                new InsufficientAuthenticationException("need auth"));

        assertThat(response.getRedirectedUrl()).isEqualTo("/login.html");
    }

    @Test
    @DisplayName("commence - path containing /formula redirects to login")
    void commence_pathContainsFormula_redirectsToLogin() throws Exception {
        MockHttpServletResponse response = doCommence("GET", "/formula/detail", null,
                new InsufficientAuthenticationException("need auth"));

        assertThat(response.getRedirectedUrl()).isEqualTo("/login.html");
    }

    @Test
    @DisplayName("commence - path with Accept header text/html (non-.html path) redirects to login")
    void commence_acceptHeaderHtml_nonHtmlPath_redirectsToLogin() throws Exception {
        MockHttpServletResponse response = doCommence("GET", "/admin/settings",
                "text/html",
                new InsufficientAuthenticationException("need auth"));

        assertThat(response.getRedirectedUrl()).isEqualTo("/login.html");
    }

    // ===== API JSON response — exception type branches =====

    @Test
    @DisplayName("commence - API + BadCredentialsException returns 'Invalid credentials'")
    void commence_apiRequest_badCredentials_returnsInvalidCredentials() throws Exception {
        MockHttpServletResponse response = doCommence("GET", "/api/test", null,
                new BadCredentialsException("bad creds"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).contains("application/json");
        assertThat(response.getContentAsString()).contains("Invalid credentials");
    }

    @Test
    @DisplayName("commence - API + InsufficientAuthenticationException returns 'Authentication required'")
    void commence_apiRequest_insufficientAuth_returnsAuthRequired() throws Exception {
        MockHttpServletResponse response = doCommence("GET", "/api/test", null,
                new InsufficientAuthenticationException("need auth"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Authentication required");
    }

    @Test
    @DisplayName("commence - API + generic AuthenticationException returns exception message")
    void commence_apiRequest_genericException_returnsExceptionMessage() throws Exception {
        MockHttpServletResponse response = doCommence("GET", "/api/data", null,
                new AuthenticationException("Custom error message") {});

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Custom error message");
    }

    @Test
    @DisplayName("commence - API request returns JSON with status, error, path fields")
    void commence_apiRequest_responseContainsRequiredFields() throws Exception {
        MockHttpServletResponse response = doCommence("POST", "/admin/api/ujian-links", null,
                new BadCredentialsException("bad creds"));

        String body = response.getContentAsString();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(body).contains("\"status\"");
        assertThat(body).contains("\"error\"");
        assertThat(body).contains("\"path\"");
        assertThat(body).contains("/admin/api/ujian-links");
    }

    @Test
    @DisplayName("commence - public page response contains status, error, path fields")
    void commence_publicPage_responseContainsRequiredFields() throws Exception {
        MockHttpServletResponse response = doCommence("GET", "/login.html", null,
                new BadCredentialsException("bad creds"));

        String body = response.getContentAsString();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(body).contains("\"status\"");
        assertThat(body).contains("UNAUTHORIZED");
        assertThat(body).contains("/login.html");
    }
}