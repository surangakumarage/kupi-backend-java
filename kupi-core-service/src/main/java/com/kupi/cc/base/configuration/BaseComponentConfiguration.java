package com.kupi.cc.base.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.client.RestTemplate;
import jakarta.validation.Validator;

@Configuration
public class BaseComponentConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Configure Validator bean for Spring Boot 3.x Jakarta Bean Validation
     * Required for method-level validation with @Valid and @Validated annotations.
     * Note: Uses 'beanValidator' name to avoid conflict with custom Validator class
     */
    @Bean("beanValidator")
    public Validator beanValidator() {
        return new LocalValidatorFactoryBean();
    }

}
