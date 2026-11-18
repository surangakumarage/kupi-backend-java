package com.kupi.cc.auth.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CORS Filter Configuration for Auth Service
 * DISABLED: CORS is now handled at the API Gateway level via GatewayConfiguration.corsWebFilter()
 * to avoid duplicate Access-Control-Allow-Origin headers.
 *
 * This was previously used to handle CORS at the service level, but with the API Gateway
 * handling CORS preflight requests, this is no longer needed.
 */
@Slf4j
@Configuration
public class CorsFilterConfiguration {

    // DISABLED - CORS is now handled at API Gateway level
    // @Bean
    public FilterRegistrationBean<CorsFilterImpl> corsFilter_DISABLED(@Value("${accessControl.allowedOrigin}") String allowedOrigins) {
        FilterRegistrationBean<CorsFilterImpl> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CorsFilterImpl(allowedOrigins));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        registration.setName("corsFilter");
        return registration;
    }

    /**
     * CORS Filter Implementation (Java 21 port from Java 11 SimpleCorsFilter)
     * Handles CORS headers at servlet filter level
     */
    @Slf4j
    public static class CorsFilterImpl implements Filter {

        private final String allowedOrigins;

        public CorsFilterImpl(String allowedOrigins) {
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
                // Do NOT call chain.doFilter() for OPTIONS - response is complete
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
}
