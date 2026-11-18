package com.kupi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * KUPI API Gateway Application
 * Spring Cloud Gateway (Java 21 / Spring Boot 3.4.4)
 *
 * This gateway provides:
 * - Service discovery via Eureka
 * - Dynamic route configuration to microservices
 * - Request/response filtering and transformation
 * - Load balancing across service instances
 * - CORS support for frontend applications
 */
@EnableDiscoveryClient
@SpringBootApplication
public class KupiApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(KupiApiGatewayApplication.class, args);
    }

}
