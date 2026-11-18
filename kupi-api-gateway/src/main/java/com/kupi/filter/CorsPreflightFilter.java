package com.kupi.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Ensures OPTIONS preflight requests are properly forwarded to backend services
 * to allow service-level CORS filters (like CorsFilter) to handle CORS headers.
 * This matches Java 11 Zuul behavior where preflight requests were routed to services.
 */
@Slf4j
@Component
public class CorsPreflightFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Log OPTIONS request for debugging
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            log.debug("OPTIONS preflight request: {}", exchange.getRequest().getPath());
        }

        // Let the request pass through to the backend service
        // Backend service's CorsFilter will handle the CORS headers
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // Run very early so CORS preflight can be handled by backend services
        return -2;
    }
}
