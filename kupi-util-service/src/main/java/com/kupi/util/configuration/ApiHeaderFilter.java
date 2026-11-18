package com.kupi.util.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ApiHeaderFilter - Validates app-key header for external requests
 * Excludes internal service-to-service endpoints (SMS, Email) from validation
 */
@WebFilter(urlPatterns = "/api/*")
public class ApiHeaderFilter extends GenericFilterBean {

    private static final String APPLICATION_HEADER = "app-key";
    private static final String[] EXEMPT_PATHS = {"/api/v1/sms/send", "/api/v1/email/send"};
    private final String applicationKey;

    public ApiHeaderFilter(@Value("${spring.application.app-key}") String applicationKey) {
        this.applicationKey = applicationKey;
    }

    /**
     * Validate the mandatory field app-key for external requests
     * Internal service endpoints (SMS, Email) bypass validation
     *
     * @param servletRequest  servletRequest
     * @param servletResponse servletResponse
     * @param filterChain     filterChain
     * @throws IOException      IOException
     * @throws ServletException ServletException
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String requestPath = request.getRequestURI();

        // Skip validation for internal service endpoints (service-to-service calls)
        for (String exemptPath : EXEMPT_PATHS) {
            if (requestPath.contains(exemptPath)) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
        }

        // Validate app-key for external requests
        String appKey = request.getHeader(APPLICATION_HEADER);

        if (appKey == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing app key");
            return;
        }
        if (!appKey.equals(applicationKey)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid app key");
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
