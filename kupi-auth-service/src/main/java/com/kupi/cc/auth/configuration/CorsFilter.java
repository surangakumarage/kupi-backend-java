package com.kupi.cc.auth.configuration;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;

/**
 * CORS Filter for Auth Service (Java 21 port from Java 11 SimpleCorsFilter)
 * This filter has been moved to CorsFilterConfiguration.java for proper registration
 * via FilterRegistrationBean to ensure execution BEFORE Spring Security.
 *
 * Kept for reference - implementation is now in CorsFilterConfiguration.CorsFilterImpl
 *
 * @deprecated Use CorsFilterConfiguration instead for proper servlet filter ordering
 */
@Deprecated
@Slf4j
public class CorsFilter implements Filter {

    private final String allowedOrigins;

    public CorsFilter(String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        String method = request.getMethod();
        String path = request.getRequestURI();
        log.debug("CorsFilter: {} {}", method, path);

        response.setHeader("Access-Control-Allow-Origin", allowedOrigins);
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, PATCH, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "*");

        log.debug("CorsFilter: Setting CORS headers - Origin: {}", allowedOrigins);

        if ("OPTIONS".equalsIgnoreCase(method)) {
            log.debug("CorsFilter: Handling OPTIONS preflight request for {}", path);
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(req, res);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
