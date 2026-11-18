package com.kupi.cc.base.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * OAuth2 Resource Server Configuration for Spring Boot 3.4.4 + Java 21
 * Validates Bearer tokens (Opaque JDBC tokens) via introspection endpoint
 * Matches Java 11 original workflow with public endpoints and JDBC token support
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class ResourceServerConfiguration {

    // Public endpoints - no Bearer token required (matching Java 11 original workflow)
    private static final String LIST_BRAND_BASIC_INFO = "/api/v1/brands/basic/**";
    private static final String LIST_CATEGORY_BASIC_INFO = "/api/v1/categories/basic/**";
    private static final String BULK_MERCHANT_DEALS = "/api/v1/deals/bulk-info";
    private static final String DEALS_BY_IDS = "/api/v1/deals/by-ids";
    private static final String BULK_MERCHANT_CATEGORIES = "/api/v1/*/bulk-categories";
    private static final String RECENTLY_EXPIRE_DEALS = "/api/v1/deals/recently-expire/**";
    private static final String DEALS_OF_THE_DAY = "/api/v1/deals/deals-of-the-day/**";
    private static final String SWAGGER_UI = "/swagger-ui/**";
    private static final String SWAGGER_INDEX = "/swagger-ui.html";
    private static final String OPEN_API_DOCS = "/v3/api-docs/**";
    private static final String STRIPE_WEB_HOOK = "/api/v1/stripe/webhook";
    private static final String ASSIGN_DEFAULT_USER_PACKAGE = "/api/v1/user-subscription/assign-default";

    // JWT/Opaque token configuration moved to OpaqueTokenConfig.java
    // Opaque token introspection is configured via OpaqueTokenIntrospector bean

    /**
     * HTTP Security Filter Chain for Resource Server
     * Permits public endpoints matching Java 11 original workflow
     * All other requests require Bearer token (validated via opaque token introspection)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()
                .requestMatchers(HttpMethod.GET, LIST_BRAND_BASIC_INFO).permitAll()
                .requestMatchers(HttpMethod.GET, LIST_CATEGORY_BASIC_INFO).permitAll()
                .requestMatchers(HttpMethod.POST, BULK_MERCHANT_DEALS).permitAll()
                .requestMatchers(HttpMethod.GET, DEALS_BY_IDS).permitAll()
                .requestMatchers(HttpMethod.POST, BULK_MERCHANT_CATEGORIES).permitAll()
                .requestMatchers(HttpMethod.GET, RECENTLY_EXPIRE_DEALS).permitAll()
                .requestMatchers(HttpMethod.GET, DEALS_OF_THE_DAY).permitAll()
                .requestMatchers(HttpMethod.GET, SWAGGER_UI).permitAll()
                .requestMatchers(HttpMethod.GET, SWAGGER_INDEX).permitAll()
                .requestMatchers(HttpMethod.GET, OPEN_API_DOCS).permitAll()
                .requestMatchers(HttpMethod.POST, STRIPE_WEB_HOOK).permitAll()
                .requestMatchers(HttpMethod.POST, ASSIGN_DEFAULT_USER_PACKAGE).permitAll()
                .anyRequest().authenticated())
            // Use opaque token introspection for JDBC-backed OAuth2 tokens (Java 11 compatibility)
            .oauth2ResourceServer(oauth2 -> oauth2.opaqueToken(opaque -> {}))
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable());

        return http.build();
    }
}
