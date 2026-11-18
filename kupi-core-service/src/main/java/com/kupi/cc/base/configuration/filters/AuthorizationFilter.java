package com.kupi.cc.base.configuration.filters;

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
 * ApiHeaderFilter for Authorization
 */
@Slf4j
@WebFilter(urlPatterns = {"/api/v1/*", "/api/v2/*"})
public class AuthorizationFilter implements Filter {

    private static final String UID_HEADER = "User-Id";
    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String AUTH_TOKEN = "Auth-Token";
    private static final String AUTH_HEADER = "authorization";
    private static final String BEARER_PREFIX = "bearer";
    private static final String BASIC_PREFIX = "basic";
    private static final String EMPTY_STRING = "";
    private static final String INVALID_TIME_ZONE = "Invalid time zone.";
    private static final String BULK_MERCHANT_DEALS = "/api/v1/deals/bulk-info";
    private static final String DEALS_BY_IDS = "/api/v1/deals/by-ids";
    private static final String BULK_MERCHANT_CATEGORIES = "/bulk-categories";
    private static final String RECENTLY_EXPIRE_DEALS = "/api/v1/deals/recently-expire";
    private static final String DEALS_OF_THE_DAY = "/api/v1/deals/deals-of-the-day";
    private static final String MERCHANT_COUPON_REDEEM_REQUEST = "/api/v1/coupons/redeem";
    private static final String STRIPE_WEB_HOOK = "/api/v1/stripe/webhook";
    private static final String ASSIGN_DEFAULT_SUBSCRIPTION = "/api/v1/user-subscription/assign-default";


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
        var raw = request.getRequestURI().substring(request.getContextPath().length());
        int end = raw.length();
        while (end > 0 && raw.charAt(end - 1) == '/') {
            end--;
        }
        var path = raw.substring(0, end);


        // Check for paths that don't require OAuth2 authentication
        if (path.contains(STRIPE_WEB_HOOK) || path.contains(ASSIGN_DEFAULT_SUBSCRIPTION)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Check for bulk endpoints that use app-key authentication
        // Note: For deals-of-the-day we only bypass OAuth2 for non-event (public/bulk) endpoints.
        // Event-related endpoints (paths containing "/event/") must still go through OAuth2 so that
        // User-Id and Auth-Token headers are populated for downstream calls.
        boolean isDealsOfTheDayBulkEndpoint = path.contains(DEALS_OF_THE_DAY) && !path.contains("/event/");
        if (path.contains(BULK_MERCHANT_DEALS) || path.contains(DEALS_BY_IDS) || path.contains(BULK_MERCHANT_CATEGORIES) ||
            path.contains(RECENTLY_EXPIRE_DEALS) || isDealsOfTheDayBulkEndpoint ||
            path.contains(MERCHANT_COUPON_REDEEM_REQUEST)) {
            // These endpoints use app-key, so bypass OAuth2 check
            filterChain.doFilter(request, servletResponse);
            return;
        }
        
        var ignorePath = path.contains(BASIC_PREFIX);

        var timeZone = request.getHeader(TIME_ZONE_HEADER);
        if (!isValidTimeZone(timeZone)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, INVALID_TIME_ZONE);
        } else {
            var token = request.getHeader(AUTH_HEADER);
            var mutableRequest = new MutableHttpServletRequest(request);
            if (ignorePath) {
                mutableRequest.putHeader(AUTH_TOKEN, token != null ? token.toLowerCase().replace(BASIC_PREFIX, EMPTY_STRING).trim() : EMPTY_STRING);
            } else {
                final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                var authUserId = authentication != null ? authentication.getName() : EMPTY_STRING;
                mutableRequest.putHeader(UID_HEADER, authUserId);
                mutableRequest.putHeader(AUTH_TOKEN, token != null ? token.toLowerCase().replace(BEARER_PREFIX, EMPTY_STRING).trim() : EMPTY_STRING);
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

}