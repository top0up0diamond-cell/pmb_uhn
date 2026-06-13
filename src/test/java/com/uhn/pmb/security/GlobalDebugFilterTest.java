package com.uhn.pmb.security;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalDebugFilterTest {

    private final GlobalDebugFilter filter = new GlobalDebugFilter();

    @Test
    void doFilterInternal_success() throws ServletException, IOException {

        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/api/test");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain, times(1))
                .doFilter(request, response);
    }

    @Test
    void doFilterInternal_filterChainThrowsException()
            throws ServletException, IOException {

        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/api/test");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain chain = mock(FilterChain.class);

        doThrow(new ServletException("Filter Error"))
                .when(chain)
                .doFilter(request, response);

        assertThrows(
                ServletException.class,
                () -> filter.doFilterInternal(request, response, chain)
        );

        verify(chain, times(1))
                .doFilter(request, response);
    }
}