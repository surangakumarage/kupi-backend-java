package com.kupi.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Spring Cloud Gateway Configuration with CORS Support
 * Handles CORS preflight (OPTIONS) requests at the gateway level
 * Applies to all routes and origins
 */
@Slf4j
@Configuration
public class GatewayConfiguration {

    /**
     * Global CORS WebFilter for handling preflight requests
     * Responds to OPTIONS requests with appropriate CORS headers
     */
    @Bean
    public WebFilter corsWebFilter(CorsConfigurationSource corsConfigurationSource) {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {
            org.springframework.http.server.reactive.ServerHttpRequest request = exchange.getRequest();

            if ("OPTIONS".equalsIgnoreCase(request.getMethod().name())) {
                log.debug("CORS preflight request: {} {}", request.getMethod().name(), request.getPath());

                org.springframework.http.server.reactive.ServerHttpResponse response = exchange.getResponse();

                // Set CORS headers for preflight response
                response.getHeaders().add("Access-Control-Allow-Origin", "*");
                response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
                response.getHeaders().add("Access-Control-Allow-Headers", "*");
                response.getHeaders().add("Access-Control-Max-Age", "3600");

                log.debug("CORS headers added to response");
                response.setStatusCode(org.springframework.http.HttpStatus.OK);
                return response.setComplete();
            }

            return chain.filter(exchange);
        };
    }

    /**
     * CORS Configuration for gateway routes
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}
