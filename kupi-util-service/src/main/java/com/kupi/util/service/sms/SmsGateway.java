package com.kupi.util.service.sms;

import com.kupi.util.domain.request.SmsRequestDto;
import com.kupi.util.domain.response.SendSmsResponseDto;

public interface SmsGateway {
    SendSmsResponseDto sendSms(SmsRequestDto smsRequestDto);
}

