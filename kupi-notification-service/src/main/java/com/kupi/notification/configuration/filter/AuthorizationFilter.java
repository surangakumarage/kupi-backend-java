package com.kupi.notification.configuration.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.TimeZone;


/**
 * ApiHeaderFilter
 */
@Slf4j
@WebFilter(urlPatterns = "/api/*")
public class AuthorizationFilter implements Filter {

    private static final String UID_HEADER = "User-Id";
    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String AUTH_TOKEN = "Auth-Token";
    private static final String AUTH_HEADER = "authorization";
    private static final String BEARER_PREFIX = "bearer";
    private static final String BASIC_PREFIX = "basic";
    private static final String EMPTY_STRING = "";
    private static final String INVALID_TIME_ZONE = "Invalid time zone.";
    private static final String PUSH_NOTIFICATION = "/api/v1/notification/push";

    /**
     * This method validates the mandatory x-api-key header.
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
        var path = stripTrailingSlashes(
                request.getRequestURI().substring(request.getContextPath().length()));

        var ignorePath = path.contains(BASIC_PREFIX);

        var timeZone = request.getHeader(TIME_ZONE_HEADER);
        if (!isValidTimeZone(timeZone)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, INVALID_TIME_ZONE);
        } else {
            var token = request.getHeader(AUTH_HEADER);
            var mutableRequest = new MutableHttpServletRequest(request);
            if (ignorePath) {
                mutableRequest.putHeader(AUTH_TOKEN, token.toLowerCase().replace(BASIC_PREFIX, EMPTY_STRING).trim());
            } else if (path.contains(PUSH_NOTIFICATION)) {
                filterChain.doFilter(mutableRequest, servletResponse);
                return;
            } else {
                final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null) {
                    var authUserId = authentication.getName();
                    mutableRequest.putHeader(UID_HEADER, authUserId);
                    mutableRequest.putHeader(AUTH_TOKEN, token.toLowerCase().replace(BEARER_PREFIX, EMPTY_STRING).trim());
                } else {
                    // Fallback if authentication is not available
                    mutableRequest.putHeader(AUTH_TOKEN, token.toLowerCase().replace(BEARER_PREFIX, EMPTY_STRING).trim());
                }
            }
            filterChain.doFilter(mutableRequest, servletResponse);
        }
    }

    /**
     * Validate the time zone
     *
     * @param timeZone timeZone
     * @return true / false
     */
    protected boolean isValidTimeZone(String timeZone) {
        for (String tzId : TimeZone.getAvailableIDs()) {
            if (tzId.equals(timeZone)) {
                return true;
            }
        }
        return false;
    }

    private static String stripTrailingSlashes(String s) {
        if (s == null || s.isEmpty()) return "";
        int end = s.length();
        while (end > 1 && s.charAt(end - 1) == '/') end--; // keep "/" as "/"
        return s.substring(0, end);
    }
}
