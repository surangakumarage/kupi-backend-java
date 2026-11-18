package com.kupi.cc.base.configuration.filters;

import lombok.extern.slf4j.Slf4j;
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
 * Api key filter
 */
@Slf4j
@WebFilter(urlPatterns = {"/api/v1/deals/bulk-info", "/api/v1/deals/by-ids", "/api/v1/*/bulk-categories"})
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
        String path = request.getRequestURI().substring(request.getContextPath().length());
        
        // Check if this is a bulk-categories endpoint, bulk-info endpoint, or by-ids endpoint
        boolean isBulkCategories = path.contains("/bulk-categories");
        boolean isBulkInfo = path.contains("/api/v1/deals/bulk-info");
        boolean isDealsByIds = path.contains("/api/v1/deals/by-ids");
        
        if (isBulkCategories || isBulkInfo || isDealsByIds) {
            // Try both lowercase and mixed case header names (HTTP headers are case-insensitive but some implementations are strict)
            String appKey = request.getHeader(APPLICATION_HEADER);
            if (appKey == null) {
                appKey = request.getHeader("App-key"); // Try with capital A
            }
            if (appKey == null) {
                appKey = request.getHeader("App-Key"); // Try with both capitals
            }
            
            if (appKey == null) {
                log.warn("Missing app-key header for path: {}", path);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing app key");
                return;
            }
            if (!appKey.equals(applicationKey)) {
                log.warn("Invalid app-key for path: {}. Expected: {}, Got: {}", path, applicationKey, appKey);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid app key");
                return;
            }
            log.debug("App-key validated successfully for path: {}", path);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
