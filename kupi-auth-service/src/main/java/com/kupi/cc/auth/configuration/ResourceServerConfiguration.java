package com.kupi.cc.auth.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.client.RestTemplate;
import jakarta.validation.Validator;

/**
 * Resource Server Configuration for Auth Service - Spring Boot 3.4.4 + Java 21
 * Defines security rules for public and protected endpoints
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class ResourceServerConfiguration {

    // Public endpoints - accessible without authentication
    private static final String REGISTRATION_ENDPOINT = "/api/v1/users/{type}";
    private static final String GUEST_REGISTRATION_ENDPOINT = "/api/v1/users/register/GUEST";
    private static final String RESET_PASSWORD_ENDPOINT = "/api/v1/users/password/reset";
    private static final String GET_UPDATED_MOBILE_NO_ENDPOINT = "/api/v1/users/mobile-no/updates/**";
    private static final String SIGN_OUT = "/api/v1/users/sign-out";
    private static final String GOOGLE = "/api/v1/google/**";
    private static final String FACEBOOK = "/api/v1/facebook/sign-in";
    private static final String FACEBOOK_SIGNUP = "/api/v1/facebook/signup";
    private static final String FORGET_PASSWORD = "/api/v1/users/forgot-password";
    private static final String ADMIN_URLS = "/api/v1/admin/**";
    private static final String BULK_MERCHANT_ENDPOINT = "/api/v1/users/{userType}/bulk-info";
    private static final String MERCHANT_ACCESS_KEY_ENDPOINT = "/api/v1/users/get-api-access-key/MERCHANT/{merchantId}";
    private static final String VERIFY_MOBILE_AND_EMAIL_ENDPOINT_PREFIX = "/api/v1/users/verification/**";
    private static final String PREFERRED_MERCHANTS = "/api/v1/preferred-merchants/internal";
    private static final String GET_ALL_MERCHANTS_INFO = "/api/v1/users/MERCHANT/*/*/search/*";
    private static final String GET_SLIDER_IMAGES_INTERNAL = "/api/v1/users/MERCHANT/business/slider-images/internal/*";
    private static final String BULK_SLIDER_IMAGES_INTERNAL = "/api/v1/users/MERCHANT/business/slider-images/internal/bulk";
    // OTP endpoints - all accessible with proper header validation (app-key or Authorization)
    private static final String OTP_ENDPOINTS = "/api/v1/otp/**";
    private static final String OTP_ENDPOINTS_PREFIXED = "/kupi-auth-service/api/v1/otp/**";

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:http://localhost:8081/oauth/token_key}")
    private String jwkSetUri;

    /**
     * HTTP Security Filter Chain for Auth Service
     * Permits public endpoints and protects internal endpoints
     * All other requests require valid Bearer token (OAuth2)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // OAuth2 Token Endpoints - Public for token generation
                // Allow OPTIONS (preflight) requests for CORS
                .requestMatchers(HttpMethod.OPTIONS, "/oauth/token").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/oauth/check_token").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/kupi-auth-service/oauth/token").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/kupi-auth-service/oauth/check_token").permitAll()
                .requestMatchers(HttpMethod.POST, "/oauth/token").permitAll()
                .requestMatchers(HttpMethod.POST, "/oauth/check_token").permitAll()
                .requestMatchers(HttpMethod.POST, "/oauth/authorize").permitAll()
                .requestMatchers(HttpMethod.POST, "/kupi-auth-service/oauth/token").permitAll()
                .requestMatchers(HttpMethod.POST, "/kupi-auth-service/oauth/check_token").permitAll()
                // OTP endpoints - permitAll() - validation via filters (ApiHeaderFilter for request-otp, AuthorizationHeaderFilter for verify)
                .requestMatchers("/api/v1/otp/users/request-otp").permitAll()
                .requestMatchers("/api/v1/otp/users/verify-and-register").permitAll()
                .requestMatchers("/api/v1/otp/users/verify-and-login").permitAll()
                .requestMatchers("/kupi-auth-service/api/v1/otp/users/request-otp").permitAll()
                .requestMatchers("/kupi-auth-service/api/v1/otp/users/verify-and-register").permitAll()
                .requestMatchers("/kupi-auth-service/api/v1/otp/users/verify-and-login").permitAll()
                // Health and Actuator endpoints
                .requestMatchers(HttpMethod.GET, "/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()
                // Public user endpoints - matching Java 11 original workflow
                .requestMatchers(HttpMethod.POST, REGISTRATION_ENDPOINT).permitAll()
                .requestMatchers(HttpMethod.POST, GUEST_REGISTRATION_ENDPOINT).permitAll()
                .requestMatchers(HttpMethod.POST, GOOGLE).permitAll()
                .requestMatchers(HttpMethod.PUT, RESET_PASSWORD_ENDPOINT).permitAll()
                .requestMatchers(HttpMethod.GET, GET_UPDATED_MOBILE_NO_ENDPOINT).permitAll()
                .requestMatchers(HttpMethod.GET, SIGN_OUT).permitAll()
                .requestMatchers(HttpMethod.POST, FACEBOOK).permitAll()
                .requestMatchers(HttpMethod.POST, FACEBOOK_SIGNUP).permitAll()
                .requestMatchers(HttpMethod.POST, FORGET_PASSWORD).permitAll()
                .requestMatchers(HttpMethod.POST, BULK_MERCHANT_ENDPOINT).permitAll()
                .requestMatchers(HttpMethod.GET, MERCHANT_ACCESS_KEY_ENDPOINT).permitAll()
                .requestMatchers(HttpMethod.POST, VERIFY_MOBILE_AND_EMAIL_ENDPOINT_PREFIX).permitAll()
                .requestMatchers(HttpMethod.GET, PREFERRED_MERCHANTS).permitAll()
                .requestMatchers(HttpMethod.GET, GET_ALL_MERCHANTS_INFO).permitAll()
                .requestMatchers(HttpMethod.GET, GET_SLIDER_IMAGES_INTERNAL).permitAll()
                .requestMatchers(HttpMethod.POST, BULK_SLIDER_IMAGES_INTERNAL).permitAll()
                // Admin endpoints
                .requestMatchers(ADMIN_URLS).hasAuthority("ADMIN")
                // ALL OTHER REQUESTS PERMITTED (secured via servlet filters for OTP endpoints)
                .anyRequest().permitAll())
            .cors(cors -> cors.disable())
            .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * REST Template Bean
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Configure Validator bean for Spring Boot 3.x Jakarta Bean Validation
     * Required for method-level validation with @Valid and @Validated annotations
     * Note: Uses 'beanValidator' name to avoid conflict with custom Validator class if any
     */
    @Bean("beanValidator")
    public Validator beanValidator() {
        return new LocalValidatorFactoryBean();
    }
}
