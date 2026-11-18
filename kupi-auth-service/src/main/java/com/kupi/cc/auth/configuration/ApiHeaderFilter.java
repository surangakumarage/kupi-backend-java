package com.kupi.cc.auth.configuration;

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
 * ApiHeaderFilter - Validates app-key header for request-otp endpoint only
 */
@WebFilter(urlPatterns = {
        "/api/v1/otp/users/request-otp",
        "/kupi-auth-service/api/v1/otp/users/request-otp"
})
public class ApiHeaderFilter extends GenericFilterBean {

    private static final String APPLICATION_HEADER = "app-key";
    private final String applicationKey;

    public ApiHeaderFilter(@Value("${spring.application.app-key}") String applicationKey) {
        this.applicationKey = applicationKey;
    }

    /**
     * Validate the mandatory field app-key for /api/v1/otp/users/request-otp endpoint
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
