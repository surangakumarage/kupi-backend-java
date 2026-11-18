package com.kupi.cc.base.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.SpringOpaqueTokenIntrospector;
import org.springframework.web.client.RestTemplate;

/**
 * Opaque Token Configuration
 * Configures introspection of JDBC-backed OAuth2 tokens from Auth Service
 * Maintains compatibility with Java 11 JDBC token store
 */
@Slf4j
@Configuration
public class OpaqueTokenConfig {

    @Value("${auth-service.url:http://localhost:8081}")
    private String authServiceUrl;

    @Value("${auth-service.client-id:tokoinmobile}")
    private String clientId;

    @Value("${auth-service.client-secret:${CLIENT_SECRET}}")
    private String clientSecret;

    /**
     * Opaque Token Introspector Bean
     * Validates JDBC opaque tokens by calling Auth Service introspection endpoint
     * Uses SpringOpaqueTokenIntrospector which doesn't require Nimbus SDK
     */
    @Bean
    public OpaqueTokenIntrospector opaqueTokenIntrospector() {
        String introspectionUri = authServiceUrl + "/oauth/check_token";
        log.info("Configuring opaque token introspection endpoint: {}", introspectionUri);

        // Use Spring's own OpaqueTokenIntrospector with client credentials
        return new SpringOpaqueTokenIntrospector(introspectionUri, clientId, clientSecret);
    }
}
