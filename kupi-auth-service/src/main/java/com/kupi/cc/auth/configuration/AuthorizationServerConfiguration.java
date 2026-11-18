package com.kupi.cc.auth.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;

/**
 * Authorization Server Configuration for Spring Boot 3.4.4
 *
 * Provides token storage and token service beans required by service classes.
 * Token endpoints (/oauth/token, /oauth/check_token) are handled by
 * Spring Security OAuth2 Authorization Server auto-configuration.
 */
@Configuration
public class AuthorizationServerConfiguration {

    /**
     * Token Store Bean - JDBC-based token storage
     * Used by UserService, CustomTokenService, and UserRoleService
     */
    @Bean
    public TokenStore tokenStore(DataSource dataSource) {
        return new JdbcTokenStore(dataSource);
    }

    /**
     * JDBC Token Store Bean - explicit bean for token storage
     * Used for direct access to JDBC-specific operations
     */
    @Bean
    public JdbcTokenStore jdbcTokenStore(DataSource dataSource) {
        return new JdbcTokenStore(dataSource);
    }

    /**
     * Default Token Services Bean - handles token generation, validation, and refresh
     * Used by controllers and services for OAuth2 token management
     */
    @Bean
    @Primary
    public DefaultTokenServices tokenServices(TokenStore tokenStore) {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore);
        defaultTokenServices.setSupportRefreshToken(true);
        return defaultTokenServices;
    }
}
