package com.kupi.cc.auth.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC Configuration for Spring Boot 3.x
 * Enables trailing slash matching to match Java 11 behavior
 *
 * In Spring Boot 2.x, /password matched both /password and /password/
 * In Spring Boot 3.x, exact matching is required unless configured
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseTrailingSlashMatch(true);
    }
}
