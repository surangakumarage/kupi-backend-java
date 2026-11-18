package com.kupi.otp.configuration;

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
 * OAuth2 Resource Server Configuration for OTP Service
 * Validates Bearer tokens (Opaque JDBC tokens) via introspection
 * Permits public OTP endpoints without authentication
 * Matches Java 11 original behavior
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class ResourceServerConfiguration {

    // Public OTP endpoints (no authentication required)
    private static final String OTP_GENERATE = "/api/v1/otp/generate";
    private static final String OTP_VERIFY = "/api/v1/otp/verify/**";
    private static final String OTP_VALIDATE = "/api/v1/otp/validate/**";
    private static final String SWAGGER_UI = "/swagger-ui/**";
    private static final String SWAGGER_INDEX = "/swagger-ui.html";
    private static final String OPEN_API_DOCS = "/v3/api-docs/**";
    private static final String HEALTH = "/health";
    private static final String ACTUATOR = "/actuator/**";

    

    

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST, OTP_GENERATE).permitAll()  // Internal service-to-service (app-key header)
                .requestMatchers(HttpMethod.POST, OTP_VERIFY).permitAll()    // Public OTP verification endpoint
                .requestMatchers(HttpMethod.GET, OTP_VALIDATE).permitAll()   // Public OTP validation endpoint
                .requestMatchers(HttpMethod.GET, "/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.opaqueToken(opaque -> {}))
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable());

        return http.build();
    }
}
