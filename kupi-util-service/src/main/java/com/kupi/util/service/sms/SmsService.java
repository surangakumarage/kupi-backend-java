package com.kupi.util.service.sms;

import com.kupi.util.domain.request.SmsRequestDto;
import com.kupi.util.domain.response.SendSmsResponseDto;
import com.kupi.util.enums.SmsProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    private final String localCountryCode;
    private final SmsGatewayFactory smsGatewayFactory;

    public SmsService(@Value("${sms.localCountryCode}") String localCountryCode,
                      SmsGatewayFactory smsGatewayFactory) {
        this.localCountryCode = localCountryCode;
        this.smsGatewayFactory = smsGatewayFactory;
    }

    public SendSmsResponseDto sendSms(SmsRequestDto dto) {

        SmsProvider provider = resolveProvider(dto);
        SmsGateway gateway = smsGatewayFactory.getProvider(provider);
        return gateway.sendSms(dto);
    }

    private SmsProvider resolveProvider(SmsRequestDto dto) {
        String number = dto.getRecipientNo().getNo();
        if (number.startsWith(localCountryCode)) {
            return SmsProvider.HTTP;
        }
        return SmsProvider.WHATSAPP;
    }
}
