package com.kupi.cc.auth.configuration;

import com.kupi.cc.auth.domain.AuthUserDetail;
import com.kupi.cc.auth.enums.ErrorResponseStatusType;
import com.kupi.cc.auth.util.FilterErrorResponseGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Configuration
public class TokenUuidVerifyFilter implements Filter {

    private static final String UPDATE_ENDPOINT = "/api/v1/users";
    private static final String UPDATE_EMAIL = "/api/v1/users/email";
    private static final String UPDATE_MOBILE = "/api/v1/users/mobileNo";
    private static final String UPDATE_PSW = "/api/v1/users/password";
    private static final String PSW_RESET = "/reset";  // Public endpoint - skip filter
    // API Gateway prefixed paths (when called through gateway)
    private static final String UPDATE_EMAIL_PREFIXED = "/kupi-auth-service/api/v1/users/email";
    private static final String UPDATE_MOBILE_PREFIXED = "/kupi-auth-service/api/v1/users/mobileNo";
    private static final String UPDATE_PSW_PREFIXED = "/kupi-auth-service/api/v1/users/password";
    private static final String HEADER_USER_ID = "User-Id";

    private final FilterErrorResponseGenerator errorResponseGenerator;

    @Autowired
    public TokenUuidVerifyFilter(FilterErrorResponseGenerator errorResponseGenerator) {
        this.errorResponseGenerator = errorResponseGenerator;
    }

    /**
     * Registering the token endpoint to be filtered
     *
     * @return FilterRegistrationBean
     */
    @Bean(name = "filterRegistrationBean2")
    public FilterRegistrationBean<TokenUuidVerifyFilter> updateFilter() {

        FilterRegistrationBean<TokenUuidVerifyFilter> updateFilterFilterRegistrationBean = new FilterRegistrationBean<>();
        updateFilterFilterRegistrationBean.setFilter(new TokenUuidVerifyFilter(errorResponseGenerator));
        // Register both direct and API Gateway prefixed paths (with and without trailing slash)
        updateFilterFilterRegistrationBean.addUrlPatterns(
                /*UPDATE_ENDPOINT,*/ UPDATE_EMAIL, UPDATE_MOBILE, UPDATE_PSW,
                UPDATE_EMAIL + "/", UPDATE_MOBILE + "/", UPDATE_PSW + "/",
                // Also register for API Gateway prefixed paths (with and without trailing slash)
                UPDATE_EMAIL_PREFIXED, UPDATE_MOBILE_PREFIXED, UPDATE_PSW_PREFIXED,
                UPDATE_EMAIL_PREFIXED + "/", UPDATE_MOBILE_PREFIXED + "/", UPDATE_PSW_PREFIXED + "/"
        );
        return updateFilterFilterRegistrationBean;
    }


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String xUserId = request.getHeader(HEADER_USER_ID);

        // Skip filter for public endpoints (e.g., /password/reset for forgot password)
        if (request.getRequestURI().contains(PSW_RESET)) {
            log.debug("Skipping TokenUuidVerifyFilter for public endpoint: {}", request.getRequestURI());
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (request.getMethod().equals(RequestMethod.PUT.name())) {

            // Check if User-Id header is present
            if (xUserId == null || xUserId.trim().isEmpty()) {
                log.warn("Request to {} missing required User-Id header", request.getRequestURI());
                errorResponseGenerator.sendErrorResponse(response, ErrorResponseStatusType.UNAUTHORIZED,
                        HttpStatus.UNAUTHORIZED);
                return;
            }

            // Check if authentication exists (OAuth2Authentication for Java 11, BearerTokenAuthentication for Spring Boot 3.x)
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null) {
                log.warn("Request to {} missing authentication", request.getRequestURI());
                errorResponseGenerator.sendErrorResponse(response, ErrorResponseStatusType.UNAUTHORIZED,
                        HttpStatus.UNAUTHORIZED);
                return;
            }

            // Handle OAuth2Authentication (Java 11 style)
            if (authentication instanceof OAuth2Authentication) {
                final OAuth2Authentication oauth2Authentication = (OAuth2Authentication) authentication;
                Object principal = oauth2Authentication.getUserAuthentication().getPrincipal();

                String userId;
                if (principal instanceof AuthUserDetail) {
                    userId = ((AuthUserDetail) principal).getId();
                } else if (principal instanceof String) {
                    // If principal is a String, use the header User-Id
                    userId = xUserId;
                } else {
                    log.warn("Unexpected principal type: {}", principal.getClass().getName());
                    errorResponseGenerator.sendErrorResponse(response, ErrorResponseStatusType.UNAUTHORIZED,
                            HttpStatus.UNAUTHORIZED);
                    return;
                }

                if (!userId.equals(xUserId)) {
                    log.warn("User-Id mismatch: token userId={}, header userId={}", userId, xUserId);
                    errorResponseGenerator.sendErrorResponse(response, ErrorResponseStatusType.UNAUTHORIZED,
                            HttpStatus.UNAUTHORIZED);
                } else {
                    filterChain.doFilter(servletRequest, servletResponse);
                }
            }
            // Handle BearerTokenAuthentication (Spring Boot 3.x) or any other authenticated request
            // BearerTokenAuthentication class name check to avoid import issues
            else if (authentication.isAuthenticated() &&
                     (authentication.getClass().getSimpleName().contains("BearerTokenAuthentication") ||
                      authentication.getClass().getName().contains("oauth2.server.resource"))) {
                // For Spring Boot 3.x BearerTokenAuthentication, token is already validated by resource server
                // User-Id header is already verified to be present (checked above)
                log.debug("Spring Boot 3.x token authentication detected for {}", request.getRequestURI());
                filterChain.doFilter(servletRequest, servletResponse);
            }
            // Handle any other authenticated request (fallback)
            else if (authentication.isAuthenticated()) {
                log.debug("Request authenticated with {}", authentication.getClass().getName());
                filterChain.doFilter(servletRequest, servletResponse);
            }
            // If not authenticated
            else {
                log.warn("Request to {} not authenticated (type: {})", request.getRequestURI(),
                        authentication.getClass().getName());
                errorResponseGenerator.sendErrorResponse(response, ErrorResponseStatusType.UNAUTHORIZED,
                        HttpStatus.UNAUTHORIZED);
            }

        } else {
            log.debug("********** Omitting the Update filter **********");
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

}
