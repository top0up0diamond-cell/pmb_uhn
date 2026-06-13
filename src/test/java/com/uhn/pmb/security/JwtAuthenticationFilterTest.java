package com.uhn.pmb.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("doFilterInternal - valid token with authorities sets security context")
    void doFilterInternal_validTokenWithAuthorities_setsSecurityContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validtoken123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtTokenProvider.validateToken("validtoken123")).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken("validtoken123")).thenReturn("user@test.com");
        when(jwtTokenProvider.getAuthoritiesFromToken("validtoken123")).thenReturn("ROLE_CAMABA");

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("user@test.com");
    }

    @Test
    @DisplayName("doFilterInternal - valid token without authorities loads from DB")
    void doFilterInternal_validTokenNoAuthorities_loadsFromDb() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validtoken123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        UserDetails userDetails = User.builder()
                .username("user@test.com")
                .password("pass")
                .roles("CAMABA")
                .build();

        when(jwtTokenProvider.validateToken("validtoken123")).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken("validtoken123")).thenReturn("user@test.com");
        when(jwtTokenProvider.getAuthoritiesFromToken("validtoken123")).thenReturn("");
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(userDetailsService).loadUserByUsername("user@test.com");
    }

    @Test
    @DisplayName("doFilterInternal - invalid token does not set security context")
    void doFilterInternal_invalidToken_doesNotSetSecurityContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalidtoken");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtTokenProvider.validateToken("invalidtoken")).thenReturn(false);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("doFilterInternal - no token in header proceeds without authentication")
    void doFilterInternal_noToken_proceedsWithoutAuth() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    @DisplayName("doFilterInternal - bearer prefix without token text proceeds")
    void doFilterInternal_bearerWithoutToken_proceeds() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer ");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
