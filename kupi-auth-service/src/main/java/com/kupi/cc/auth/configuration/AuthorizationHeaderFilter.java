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
 * AuthorizationHeaderFilter - Validates Authorization header for OTP verify endpoints
 *
 * OTP verify endpoints (/api/v1/otp/users/verify-and-register and /api/v1/otp/users/verify-and-login)
 * require Authorization header validation with Basic auth token.
 */
@WebFilter(urlPatterns = {
        "/api/v1/otp/users/verify-and-register",
        "/api/v1/otp/users/verify-and-login",
        "/kupi-auth-service/api/v1/otp/users/verify-and-register",
        "/kupi-auth-service/api/v1/otp/users/verify-and-login"
})
public class AuthorizationHeaderFilter extends GenericFilterBean {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final String expectedAuthorizationToken;

    public AuthorizationHeaderFilter(@Value("${auth.basic-token:}") String expectedAuthorizationToken) {
        this.expectedAuthorizationToken = expectedAuthorizationToken;
    }

    /**
     * Filter logic - validates Authorization header
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

        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authorizationHeader == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing Authorization header");
            return;
        }

        if (!authorizationHeader.equals("Basic " + expectedAuthorizationToken)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Authorization header");
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
