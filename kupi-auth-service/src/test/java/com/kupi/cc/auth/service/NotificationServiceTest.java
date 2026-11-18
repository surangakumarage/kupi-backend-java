package com.kupi.cc.auth.service;

import com.kupi.cc.auth.configuration.Translator;
import com.kupi.cc.auth.domain.request.MobileNoRequestDto;
import com.kupi.cc.auth.enums.SendEmailDetails;
import com.kupi.cc.auth.exception.AuthServiceException;
import com.kupi.cc.auth.exception.SendSmsFailedException;
import com.kupi.cc.auth.wrapper.SendEmailResponseWrapper;
import com.kupi.cc.auth.wrapper.SendSmsResponseWrapper;
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

    private static final String COUNTRY_CODE = "+62";
    private static final String LOCAL_NUMBER = "81123123";
    private static final String PASSWORD_RESET_MESSAGE_CODE = "e002";
    private static final String PASSWORD_RESET_TOKEN = "3d80eadf-b463-4b7b-ae5e-12e31b141267";
    private static final String RECIPIENT_ADDRESS = "recipient@gmail.com";
    private static final String TITLE = "title";
    private static final String SMS = "sms";
    private static final String CONTENT = "content";private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String APP_KEY = "app-key";
    private static final String BASE_URL = "http://localhost:8085/kupi-util-service";
    private static final String MAIL_PATH = "/api/v1/email/send";
    private static final String SMS_PATH = "/api/v1/sms/send";
    private static final String APP_KEY_VALUE = "changeme-util-service-app-key";
    private static final boolean NOTIFICATION_ENABLE = true;

    private NotificationService notificationService;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private Translator translator;

    @BeforeEach
    void setUp() {
        initMocks(this);
        notificationService = new NotificationService(BASE_URL, MAIL_PATH, SMS_PATH, NOTIFICATION_ENABLE, APP_KEY_VALUE,
                restTemplate, translator);
    }

    @AfterEach
    void tearDown() {
    }

    /**
     * send password reset token in sms
     */
    @Test
    void Should_SendPasswordResetSms() {
        when(translator.toLocale(SendEmailDetails.PASSWORD_RESET_MSG.getCode())).thenReturn(PASSWORD_RESET_MESSAGE_CODE);
        SendSmsResponseWrapper sendSmsResponseWrapper = new SendSmsResponseWrapper();
        ResponseEntity<SendSmsResponseWrapper> sendSmsResponseWrapperResponseEntity =
                new ResponseEntity<>(sendSmsResponseWrapper, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(SendSmsResponseWrapper.class))).thenReturn(sendSmsResponseWrapperResponseEntity);
        MobileNoRequestDto recipientNo = new MobileNoRequestDto(COUNTRY_CODE, LOCAL_NUMBER);
        notificationService.sendPasswordResetSms(recipientNo, PASSWORD_RESET_TOKEN);
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class),
                any(HttpEntity.class), eq(SendSmsResponseWrapper.class));
    }

    @Test
    void Should_ThrowSendSmsFailedException_When_FailedToSendPasswordResetToken() {
        MobileNoRequestDto recipientNo = new MobileNoRequestDto(COUNTRY_CODE, LOCAL_NUMBER);
        when(translator.toLocale(SendEmailDetails.PASSWORD_RESET_MSG.getCode())).thenReturn(PASSWORD_RESET_MESSAGE_CODE);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(SendSmsResponseWrapper.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        SendSmsFailedException exception = assertThrows(SendSmsFailedException.class, () ->
                notificationService.sendPasswordResetSms(recipientNo, PASSWORD_RESET_TOKEN));
        assertEquals("Failed to send password reset token", exception.getMessage());
    }

    @Test
    void Should_SendEmailNotification() {
        notificationService.sendMail(RECIPIENT_ADDRESS, TITLE, CONTENT, TIME_ZONE);
        verify(restTemplate).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(SendEmailResponseWrapper.class));
    }

    @Test
    void Should_ThrowAuthServiceException_When_SendMailIsFailed() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(SendEmailResponseWrapper.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                notificationService.sendMail(RECIPIENT_ADDRESS, TITLE, CONTENT, TIME_ZONE));
        assertEquals("Sending email was failed.", exception.getMessage());
    }

    @Test
    void Should_SendSmsNotification() {
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto(COUNTRY_CODE, LOCAL_NUMBER);
        notificationService.sendSms(mobileNoRequestDto, SMS, TIME_ZONE);
        verify(restTemplate).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(SendSmsResponseWrapper.class));
    }

    @Test
    void Should_ThrowAuthServiceException_When_SendSmsIsFailed() {
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto(COUNTRY_CODE, LOCAL_NUMBER);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(SendSmsResponseWrapper.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                notificationService.sendSms(mobileNoRequestDto, SMS, TIME_ZONE));
        assertEquals("Sending sms was failed.", exception.getMessage());
    }

    /**
     * This method generate headers for util service urls.
     *
     * @param timeZone time zone
     * @return HttpHeaders
     */
    private HttpHeaders getAuthHeaders(String timeZone) {
        var headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(TIME_ZONE_HEADER, timeZone);
        headers.set(APP_KEY, APP_KEY_VALUE);
        return headers;
    }
}