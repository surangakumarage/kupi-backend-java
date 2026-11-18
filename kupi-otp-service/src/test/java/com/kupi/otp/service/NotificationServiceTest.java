package com.kupi.otp.service;

import com.kupi.otp.configuration.Translator;
import com.kupi.otp.domain.request.MobileNoRequestDto;
import com.kupi.otp.enums.NotificationTemplateType;
import com.kupi.otp.exception.OTPServiceException;
import com.kupi.otp.wrapper.SendEmailWrapper;
import com.kupi.otp.wrapper.SendSmsWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests {@link NotificationService} class
 */
class NotificationServiceTest {

    private static final String EMAIL_HEADER = "kupi Email Verification";
    private static final String EMAIL_BODY = "Hi,\\n\\nPlease use the following OTP to complete your email verification.\\n\\n<OTP>\\n\\nThis is a system generated email. Please do not reply.\\n\\nThank you,\\nkupi Team";
    private static final String SMS_BODY = "Please use this OTP <OTP> to complete your mobile number verification.\\nB/flbwT7A6e";
    private static final int OTP = 123456;
    private static final String COUNTRY_CODE = "+94";
    private static final String LOCAL_NUMBER = "771109101";
    private static final String RECIPIENT = "recipient@kupi.lk";
    private static final String APP_KEY_HEADER = "app-key";
    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String TIME_ZONE_VALUE = "Asia/Colombo";
    private static final String BASE_URL = "http://localhost:8080/kupi-util-service";
    private static final boolean IS_ENABLE_NOTIFICATION = true;
    private static final String UTIL_SERVICE_APP_KEY = "changeme-util-service-app-key";
    private static final String MAIL_SENDER = "/api/v1/email/send";
    private static final String SMS_SENDER = "/api/v1/sms/send";
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private Translator translator;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {

        initMocks(this);
        notificationService = new NotificationService(APP_KEY_HEADER, TIME_ZONE_HEADER, TIME_ZONE_VALUE, BASE_URL,
                IS_ENABLE_NOTIFICATION, MAIL_SENDER, SMS_SENDER, UTIL_SERVICE_APP_KEY, restTemplate, translator);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_SendMail() {
        String emailBody = EMAIL_BODY.replace("<OTP>", String.valueOf(OTP));
        when(translator.toLocale(NotificationTemplateType.SEND_OTP.getEmailHeader())).thenReturn(EMAIL_HEADER);
        when(translator.toLocale(NotificationTemplateType.SEND_OTP.getEmailBody())).thenReturn(emailBody);
        notificationService.sendMail(RECIPIENT, String.valueOf(OTP));
        verify(restTemplate, times(1))
                .exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(SendEmailWrapper.class));
    }

    @Test
    void Should_ThrowOTPServiceException_When_RestTemplateIsFailedForSendMail() {
        String otp = String.valueOf(OTP);
        String emailBody = EMAIL_BODY.replace("<OTP>", otp);
        when(translator.toLocale(NotificationTemplateType.SEND_OTP.getEmailHeader())).thenReturn(EMAIL_HEADER);
        when(translator.toLocale(NotificationTemplateType.SEND_OTP.getEmailBody())).thenReturn(emailBody);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(SendEmailWrapper.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        OTPServiceException otpServiceException = assertThrows(OTPServiceException.class, () -> notificationService.
                sendMail(RECIPIENT, otp));
        assertEquals(HttpStatus.BAD_REQUEST.toString(), otpServiceException.getMessage());
    }

    @Test
    void Should_SendSms() {
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        String smsBody = SMS_BODY.replace("<OTP>", String.valueOf(OTP));
        when(translator.toLocale(NotificationTemplateType.SEND_OTP.getSms())).thenReturn(smsBody);
        notificationService.sendSms(mobileNoRequestDto, String.valueOf(OTP));
        verify(restTemplate, times(1))
                .exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(SendSmsWrapper.class));
    }

    @Test
    void Should_ThrowOTPServiceException_When_RestTemplateIsFailedForSendSms() {
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        String otp = String.valueOf(OTP);
        String smsBody = SMS_BODY.replace("<OTP>", otp);
        when(translator.toLocale(NotificationTemplateType.SEND_OTP.getSms())).thenReturn(smsBody);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(SendSmsWrapper.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        OTPServiceException otpServiceException = assertThrows(OTPServiceException.class, () ->
                notificationService.sendSms(mobileNoRequestDto, otp));
        assertEquals(HttpStatus.BAD_REQUEST.toString(), otpServiceException.getMessage());
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
     * This method returns headers for util service urls.
     */
    private HttpHeaders getAuthHeaders() {
        var headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(TIME_ZONE_HEADER, TIME_ZONE_VALUE);
        headers.set(APP_KEY_HEADER, UTIL_SERVICE_APP_KEY);
        return headers;
    }
}