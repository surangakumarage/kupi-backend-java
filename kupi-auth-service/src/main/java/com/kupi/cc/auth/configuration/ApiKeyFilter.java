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

@WebFilter(urlPatterns = {"/api/v1/users/mobile-no/updates/*", "/api/v1/users/MERCHANT/bulk-info",
        "/api/v1/users/bulk-info", "/api/v1/users/get-api-access-key/MERCHANT/*", "/api/v1/users/verification/*",
        "/api/v1/preferred-merchants/internal","/api/v1/users/MERCHANT/*/*/search/*",
        "/api/v1/users/MERCHANT/business/slider-images/internal/*",
        "/api/v1/users/MERCHANT/business/slider-images/internal/bulk"})
public class ApiKeyFilter extends GenericFilterBean {

    private static final String APPLICATION_HEADER = "app-key";
    private final String applicationKey;

    public ApiKeyFilter(@Value("${spring.application.app-key}") String applicationKey) {
        this.applicationKey = applicationKey;
    }

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
