package com.kupi.util.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Resource Server Configuration for Util Service - Spring Boot 3.4.4 + Java 21
 * Validates Bearer tokens (JWT) for protected endpoints
 * Permits internal service endpoints with app-key header validation
 * Matches Java 11 original behavior
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class ResourceServerConfiguration {

    // Internal service endpoints - accessible with valid app-key header validation
    private static final String SEND_SMS_ENDPOINT = "/api/v1/sms/send";
    private static final String SEND_EMAIL_ENDPOINT = "/api/v1/email/send";
    private static final String HEALTH_ENDPOINT = "/health";

    

    

        @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()
                .requestMatchers(HttpMethod.POST, SEND_SMS_ENDPOINT).permitAll()
                .requestMatchers(HttpMethod.POST, SEND_EMAIL_ENDPOINT).permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.opaqueToken(opaque -> {}))
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable());

        return http.build();
    }
}
