package com.kupi.util.service;

import com.kupi.util.domain.request.MobileNoRequestDto;
import com.kupi.util.domain.request.SmsRequestDto;
import com.kupi.util.domain.response.SendSmsResponseDto;
import com.kupi.util.enums.SmsProvider;
import com.kupi.util.exception.UtilServiceException;
import com.kupi.util.service.sms.SmsGateway;
import com.kupi.util.service.sms.SmsGatewayFactory;
import com.kupi.util.service.sms.SmsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests {@link SmsService} class
 */
class SmsServiceTest {

    private static final String COUNTRY_CODE = "+94";
    private static final String LOCAL_NUMBER = "771109101";
    
    private SmsService smsService;
    @Mock
    private SmsGatewayFactory smsGatewayFactory;
    @Mock
    private SmsGateway smsGateway;

    @BeforeEach
    void setUp() {
        initMocks(this);
        smsService = new SmsService(COUNTRY_CODE,smsGatewayFactory);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnSendSmsResponseDto_When_SmsSentSuccessfully() {
        SmsRequestDto smsRequestDto = getSampleSmsRequestDto();
        SendSmsResponseDto expected = getSampleSendSmsResponseDto();
        
        // Mock the factory to return the gateway, and the gateway to return the expected response
        // Note: Numbers starting with "+94" resolve to HTTP provider, not WHATSAPP
        when(smsGatewayFactory.getProvider(SmsProvider.HTTP)).thenReturn(smsGateway);
        when(smsGateway.sendSms(smsRequestDto)).thenReturn(expected);
        
        SendSmsResponseDto actual = smsService.sendSms(smsRequestDto);
        
        assertEquals(expected.getRequest(), actual.getRequest());
        assertEquals(expected.getStatus(), actual.getStatus());
    }

    @Test
    void Should_Throw_When_GatewayThrows() {
        SmsRequestDto dto = getSampleSmsRequestDto();
        
        // Mock the factory to return the gateway, and the gateway to throw an exception
        // Note: Numbers starting with "+94" resolve to HTTP provider
        when(smsGatewayFactory.getProvider(SmsProvider.HTTP)).thenReturn(smsGateway);
        when(smsGateway.sendSms(dto))
                .thenThrow(new UtilServiceException("Send SMS was failed."));
        
        UtilServiceException ex = assertThrows(UtilServiceException.class,
                () -> smsService.sendSms(dto));
        assertEquals("Send SMS was failed.", ex.getMessage());
    }

    @Test
    void Should_Throw_When_JsonToObjectConversionIsFailedForSendSms() {
        SmsRequestDto dto = getSampleSmsRequestDto();
        
        // Mock the factory to return the gateway, and the gateway to throw an exception
        // Note: Numbers starting with "+94" resolve to HTTP provider
        when(smsGatewayFactory.getProvider(SmsProvider.HTTP)).thenReturn(smsGateway);
        when(smsGateway.sendSms(dto))
                .thenThrow(new UtilServiceException("Converting SMS response to SendSmsResponseDto was failed."));
        
        UtilServiceException ex = assertThrows(UtilServiceException.class,
                () -> smsService.sendSms(dto));
        assertEquals("Converting SMS response to SendSmsResponseDto was failed.", ex.getMessage());
    }

    /**
     * This method creates sample MobileNoRequestDto
     *
     * @return MobileNoRequestDto
     */
    private MobileNoRequestDto getSampleMobileNoRequestDto() {
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto();
        mobileNoRequestDto.setCountryCode(COUNTRY_CODE);
        mobileNoRequestDto.setLocalNumber(LOCAL_NUMBER);
        return mobileNoRequestDto;
    }

    /**
     * This method creates sample SmsRequestDto
     *
     * @return SmsRequestDto
     */
    private SmsRequestDto getSampleSmsRequestDto() {
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        return new SmsRequestDto(mobileNoRequestDto, "Sms Message");
    }

    /**
     * This method creates sample SendSmsResponseDto
     * Note: HTTP gateway typically returns different status/request values
     *
     * @return SendSmsResponseDto
     */
    private SendSmsResponseDto getSampleSendSmsResponseDto() {
        SendSmsResponseDto sendSmsResponseDto = new SendSmsResponseDto();
        sendSmsResponseDto.setStatus("200 OK");
        sendSmsResponseDto.setRequest("sms");
        return sendSmsResponseDto;
    }
}