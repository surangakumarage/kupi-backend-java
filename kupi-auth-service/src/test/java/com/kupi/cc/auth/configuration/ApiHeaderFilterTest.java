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
 * Unit tests for ApiHeaderFilter
 */
@ExtendWith(MockitoExtension.class)
class ApiHeaderFilterTest {

    private static final String VALID_APP_KEY = "changeme-spring-application-app-key";
    private static final String INVALID_APP_KEY = "invalid-app-key";
    private static final String APP_KEY_HEADER = "app-key";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private ApiHeaderFilter filter;

    @BeforeEach
    void setUp() {
        filter = new ApiHeaderFilter(VALID_APP_KEY);
    }

    @Test
    void testDoFilter_WithValidAppKey_ShouldContinueFilterChain() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(APP_KEY_HEADER)).thenReturn(VALID_APP_KEY);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void testDoFilter_WithMissingAppKey_ShouldReturnUnauthorized() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(APP_KEY_HEADER)).thenReturn(null);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing app key");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testDoFilter_WithInvalidAppKey_ShouldReturnUnauthorized() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(APP_KEY_HEADER)).thenReturn(INVALID_APP_KEY);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid app key");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testDoFilter_WithEmptyAppKey_ShouldReturnUnauthorized() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(APP_KEY_HEADER)).thenReturn("");

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid app key");
        verify(filterChain, never()).doFilter(request, response);
    }
}
