package com.kupi.fileuploader.configurations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.SpringOpaqueTokenIntrospector;

/**
 * Opaque Token Configuration for File Manager Service
 * Validates JDBC-backed OAuth2 tokens from Auth Service
 * Maintains compatibility with Java 11 token store
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

    @Bean
    public OpaqueTokenIntrospector opaqueTokenIntrospector() {
        String introspectionUri = authServiceUrl + "/oauth/check_token";
        log.info("Configuring opaque token introspection endpoint: {}", introspectionUri);
        return new SpringOpaqueTokenIntrospector(introspectionUri, clientId, clientSecret);
    }
}
