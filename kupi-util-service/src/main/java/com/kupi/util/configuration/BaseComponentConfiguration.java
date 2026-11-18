package com.kupi.util.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
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

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("KUPI Util Service")
                        .description("Util service of KUPI backend")
                        .version("0.0.1-SNAPSHOT"));
    }

    /**
     * Configure Validator bean for Spring Boot 3.x Jakarta Bean Validation
     * Required for method-level validation with @Valid and @Validated annotations
     * Note: Uses 'beanValidator' name to avoid conflict with custom Validator class
     */
    @Bean("beanValidator")
    public Validator beanValidator() {
        return new LocalValidatorFactoryBean();
    }
}
