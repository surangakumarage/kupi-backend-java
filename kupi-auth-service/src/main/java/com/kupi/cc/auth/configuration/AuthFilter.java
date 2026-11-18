package com.kupi.cc.auth.configuration;

import com.kupi.cc.auth.domain.entity.User;
import com.kupi.cc.auth.enums.ErrorResponseStatusType;
import com.kupi.cc.auth.enums.RoleType;
import com.kupi.cc.auth.service.UserDetailService;
import com.kupi.cc.auth.util.FilterErrorResponseGenerator;
import com.kupi.cc.auth.util.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * AuthFilter
 */
@Configuration
@Slf4j
public class AuthFilter implements Filter {

    private static final String FACEBOOK_TOKEN_ENDPOINT = "/oauth/token/facebook";
    private static final String TOKEN_ENDPOINT = "/oauth/token";
    private static final String USER_NAME = "username";
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String PSW_FIELD = "password";
    private static final String GRANT_TYPE = "grant_type";
    private static final String USER_ROLE = "user_role";

    private final Validator validator;
    private final FilterErrorResponseGenerator errorResponseGenerator;
    private final UserDetailService userDetailService;

    @Autowired
    public AuthFilter(Validator validator, FilterErrorResponseGenerator errorResponseGenerator,
                      UserDetailService userDetailService) {
        this.validator = validator;
        this.errorResponseGenerator = errorResponseGenerator;
        this.userDetailService = userDetailService;
    }

    /**
     * Registering the token endpoint to be filtered
     *
     * @return FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean<AuthFilter> loginFilter() {
        FilterRegistrationBean<AuthFilter> authFilterFilterRegistrationBean = new FilterRegistrationBean<>();
        authFilterFilterRegistrationBean.setFilter(new AuthFilter(validator, errorResponseGenerator, userDetailService));
        authFilterFilterRegistrationBean.addUrlPatterns(FACEBOOK_TOKEN_ENDPOINT, TOKEN_ENDPOINT);
        return authFilterFilterRegistrationBean;
    }

    /**
     * Validates the login request fields
     *
     * @param servletRequest  request
     * @param servletResponse response
     * @param filterChain     filterChain
     * @throws IOException      IOException
     * @throws ServletException ServletException
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        log.debug("Executing the login filter");

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (request.getServletPath().equals(FACEBOOK_TOKEN_ENDPOINT)) {
            if (isNonEmpty(request.getParameter(ACCESS_TOKEN))) {
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                errorResponseGenerator.sendErrorResponse(response,
                        ErrorResponseStatusType.MISSING_REQUIRED_FIELDS, HttpStatus.BAD_REQUEST);
            }
        }

        if (request.getServletPath().equals(TOKEN_ENDPOINT) && request.getParameter(GRANT_TYPE).equals(PSW_FIELD)) {
            if (isNonEmpty(request.getParameter(USER_NAME)) && isNonEmpty(request.getParameter(PSW_FIELD))) {
                if (validator.isValidPassword(request.getParameter(PSW_FIELD))) {
                    try {
                        if (isValidUserRole(request)) {
                            filterChain.doFilter(servletRequest, servletResponse);
                        } else {
                            errorResponseGenerator.sendErrorResponse(response,
                                    ErrorResponseStatusType.NO_PERMISSION_TO_ACCESS, HttpStatus.FORBIDDEN);
                        }
                    } catch (UsernameNotFoundException ex) {
                        // For non-existing users, return the same structure as invalid login (wrong password).
                        errorResponseGenerator.sendErrorResponse(response,
                                ErrorResponseStatusType.INVALID_LOGIN, HttpStatus.BAD_REQUEST);
                    }
                } else {
                    errorResponseGenerator.sendErrorResponse(response,
                            ErrorResponseStatusType.INVALID_USERNAME_OR_PASS, HttpStatus.BAD_REQUEST);
                }
            } else {
                errorResponseGenerator.sendErrorResponse(response,
                        ErrorResponseStatusType.MISSING_REQUIRED_FIELDS, HttpStatus.BAD_REQUEST);
            }
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    /**
     * This method checks if user role is valid for login.
     *
     * @param request HttpServletRequest
     * @return boolean.
     */
    private boolean isValidUserRole(HttpServletRequest request) {
        User user = (User) userDetailService.loadUserByUsername(request.getParameter(USER_NAME));
        String actualUserRole = user.getRole().getName();
        String requestUserRole = request.getParameter(USER_ROLE);
        List<String> roleTypeList = List.of(RoleType.ADMIN.toString(),
                RoleType.MERCHANT.toString(), RoleType.BANK.toString());

        // If user_role is not provided, allow login for any role
        if (requestUserRole == null) {
            return true;
        }

        if (!requestUserRole.equals(RoleType.GUEST.toString())) {
            if (requestUserRole.equals(RoleType.USER.toString()) && requestUserRole.equals(actualUserRole)) {
                return true;
            }
            return roleTypeList.contains(requestUserRole) && roleTypeList.contains(actualUserRole);
        }
        return true;
    }

    /**
     * Check for empty fields
     *
     * @param field field
     * @return true/false
     */
    protected boolean isNonEmpty(String field) {
        log.debug("Executing the login filter - isNonEmpty");
        return field != null && !field.trim().isEmpty();
    }

}
