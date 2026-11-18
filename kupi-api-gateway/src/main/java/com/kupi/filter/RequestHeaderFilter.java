package com.kupi.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global request header transformation filter
 * Adds gateway headers and passes through authentication headers
 */
@Slf4j
@Component
public class RequestHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Add gateway identifier header to track routing
        exchange.getRequest().mutate()
            .header("X-Forwarded-Host", exchange.getRequest().getHeaders().getHost() != null ?
                exchange.getRequest().getHeaders().getHost().getHostName() : "api-gateway")
            .header("X-Request-ID", exchange.getRequest().getId())
            .build();

        // Log forwarded headers for debugging
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null) {
            log.debug("Forwarded from: {}", forwardedFor);
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
