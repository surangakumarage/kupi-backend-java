package com.kupi.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Global logging filter for API Gateway
 * Logs incoming requests and outgoing responses for debugging and monitoring
 */
@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        AtomicReference<Long> startTime = new AtomicReference<>(System.currentTimeMillis());

        // Log incoming request
        String requestId = exchange.getRequest().getId();
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();
        String queryString = exchange.getRequest().getQueryParams().isEmpty() ? "" : "?" + exchange.getRequest().getQueryParams().entrySet().stream()
            .flatMap(entry -> entry.getValue().stream().map(v -> entry.getKey() + "=" + v))
            .reduce((a, b) -> a + "&" + b)
            .orElse("");

        log.debug("API Gateway: Incoming {} {} {}", method, path, queryString);
        log.debug("Request ID: {}", requestId);

        // Log headers (excluding sensitive ones)
        exchange.getRequest().getHeaders().forEach((key, values) -> {
            if (!key.toLowerCase().contains("password") && !key.toLowerCase().contains("token") && !key.toLowerCase().contains("secret")) {
                log.debug("Header: {} = {}", key, values);
            }
        });

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime.get();
            int statusCode = exchange.getResponse().getStatusCode() != null ?
                exchange.getResponse().getStatusCode().value() : 0;

            log.debug("API Gateway: Response {} {} {} - {} ms",
                method, path, statusCode, duration);

            // Log slow requests (over 1 second)
            if (duration > 1000) {
                log.warn("Slow API Gateway request detected: {} {} took {} ms",
                    method, path, duration);
            }
        }));
    }

    @Override
    public int getOrder() {
        // Run early in the filter chain
        return -1;
    }

}
