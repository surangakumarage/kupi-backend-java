package com.kupi.util.service.sms;

import com.kupi.util.enums.SmsProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmsGatewayFactory {

    private final HttpSmsGateway httpSmsGateway;
    private final WhatsAppSmsGateway whatsappSmsGateway;
    private final SnsSmsGateway snsSmsGateway;

    @Autowired
    public SmsGatewayFactory(
            HttpSmsGateway httpSmsGateway,
            WhatsAppSmsGateway whatsappSmsGateway,
            SnsSmsGateway snsSmsGateway
    ) {
        this.httpSmsGateway = httpSmsGateway;
        this.whatsappSmsGateway = whatsappSmsGateway;
        this.snsSmsGateway = snsSmsGateway;
    }

    public SmsGateway getProvider(SmsProvider provider) {
        log.error("=== SMS DEBUG: Getting SMS gateway for provider: {} ===", provider);
        SmsGateway gateway;
        switch (provider) {
            case HTTP:
                gateway = httpSmsGateway;
                break;
            case WHATSAPP:
                gateway = whatsappSmsGateway;
                break;
            case SNS:
                gateway = snsSmsGateway;
                break;
            default:
                throw new IllegalArgumentException("Unknown SMS provider: " + provider);
        }
        log.error("=== SMS DEBUG: Returning gateway: {} for provider: {} ===", gateway.getClass().getSimpleName(), provider);
        return gateway;
    }

}

