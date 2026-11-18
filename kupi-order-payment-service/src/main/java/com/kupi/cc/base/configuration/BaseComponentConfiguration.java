package com.kupi.cc.base.configuration;

import com.paypal.base.rest.APIContext;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

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
                        .title("KUPI Order Payment Service")
                        .description("Order and Payment service of KUPI backend")
                        .version("0.0.1-SNAPSHOT"));
    }

    @Bean
    public APIContext paypalApiContext(@Value("${paypal.clientId}") String clientId,
                                       @Value("${paypal.clientSecret}") String clientSecret,
                                       @Value("${paypal.mode}") String mode) {
        return new APIContext(clientId, clientSecret, mode);
    }

    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("REQUEST DATA : ");
        return filter;
    }
}
