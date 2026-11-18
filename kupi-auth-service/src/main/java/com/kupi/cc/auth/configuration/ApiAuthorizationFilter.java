package com.kupi.cc.auth.configuration;

import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.TimeZone;


/**
 * ApiHeaderFilter for Authorization
 */
@Slf4j
@WebFilter(urlPatterns = "/api/v1/*")
public class ApiAuthorizationFilter implements Filter {

    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String INVALID_TIME_ZONE = "Invalid time zone.";

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

        var timeZone = request.getHeader(TIME_ZONE_HEADER);
        if (!isValidTimeZone(timeZone))
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, INVALID_TIME_ZONE);
        else
            filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * Validate the time zone
     *
     * @param timeZone timeZone
     * @return true / false
     */
    private boolean isValidTimeZone(String timeZone) {
        for (String tzId : TimeZone.getAvailableIDs()) {
            if (tzId.equals(timeZone)) {
                return true;
            }
        }
        return false;
    }
}