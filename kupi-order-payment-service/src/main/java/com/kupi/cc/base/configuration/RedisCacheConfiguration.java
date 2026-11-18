package com.kupi.cc.base.configuration;

import com.kupi.cc.base.domain.pojo.PaymentOrderDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisCacheConfiguration {

    @Bean
    public RedisTemplate<String, PaymentOrderDto> redisPaymentOrderTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, PaymentOrderDto> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}
