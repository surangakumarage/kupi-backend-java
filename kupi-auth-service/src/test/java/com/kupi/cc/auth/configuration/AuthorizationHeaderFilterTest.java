package com.kupi.cc.auth.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthorizationHeaderFilter
 */
@ExtendWith(MockitoExtension.class)
class AuthorizationHeaderFilterTest {

    private static final String VALID_BASIC_TOKEN = "changeme-auth-basic-token";
    private static final String INVALID_BASIC_TOKEN = "invalid-token";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private AuthorizationHeaderFilter filter;

    @BeforeEach
    void setUp() {
        filter = new AuthorizationHeaderFilter(VALID_BASIC_TOKEN);
    }

    @Test
    void testDoFilter_WithValidAuthorizationHeader_ShouldContinueFilterChain() throws ServletException, IOException {
        // Arrange
        String validHeader = "Basic " + VALID_BASIC_TOKEN;
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(validHeader);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void testDoFilter_WithMissingAuthorizationHeader_ShouldReturnUnauthorized() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(null);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing Authorization header");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testDoFilter_WithInvalidAuthorizationHeader_ShouldReturnUnauthorized() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn("Basic " + INVALID_BASIC_TOKEN);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Authorization header");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testDoFilter_WithMissingBasicPrefix_ShouldReturnUnauthorized() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(VALID_BASIC_TOKEN); // Missing "Basic " prefix

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Authorization header");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testDoFilter_WithBearerInsteadOfBasic_ShouldReturnUnauthorized() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn("Bearer " + VALID_BASIC_TOKEN);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Authorization header");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testDoFilter_WithEmptyAuthorizationHeader_ShouldReturnUnauthorized() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn("");

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Authorization header");
        verify(filterChain, never()).doFilter(request, response);
    }
}
