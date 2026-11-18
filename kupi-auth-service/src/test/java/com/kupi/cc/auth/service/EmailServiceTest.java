package com.kupi.cc.auth.service;

import com.kupi.cc.auth.configuration.Translator;
import com.kupi.cc.auth.enums.ResponseStatusType;
import com.kupi.cc.auth.enums.SendEmailDetails;
import com.kupi.cc.auth.exception.AuthServiceException;
import com.kupi.cc.auth.wrapper.SendEmailResponseWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests {@link EmailService} class
 */
class EmailServiceTest {
    private static final String EMAIL_MESSAGE = "Please use the token: $$TOKEN$$ to complete the reset password process.\\nB/flbwT7A6e";
    private static final String EMAIL = "example@gmail.com";
    private static final String ID_TOKEN = "changeme-auth-basic-token";
    private static final String SEND_EMAIL_URI = "http://localhost:8082/otp-service/api/v1/email/send";
    private static final String API_KEY = "changeme-mail-api-key";
    private static final String LINK = "http://localhost:4000/password/reset/{token}";

    @Mock
    private Translator translator;
    @Mock
    private RestTemplate restTemplate;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        emailService = new EmailService(restTemplate, translator, SEND_EMAIL_URI, API_KEY, LINK);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnTrue_When_SendEmailSuccessfully() {
        SendEmailResponseWrapper sendEmailResponseWrapper = getSampleSendEmailResponseWrapper();
        when(translator.toLocale(SendEmailDetails.PASSWORD_RESET_MSG.getCode())).thenReturn(EMAIL_MESSAGE);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(SendEmailResponseWrapper.class)))
                .thenReturn(ResponseEntity.of(Optional.of(sendEmailResponseWrapper)));
        assertTrue(emailService.sendEmail(EMAIL, ID_TOKEN));
    }

    @Test
    void Should_ThrowAuthServiceException_When_SendEmailIsFailed() {
        when(translator.toLocale(SendEmailDetails.PASSWORD_RESET_MSG.getCode())).thenReturn(EMAIL_MESSAGE);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(SendEmailResponseWrapper.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                emailService.sendEmail(EMAIL, ID_TOKEN));
        assertEquals("failed", exception.getMessage());
    }

    /**
     * This method creates sample http headers
     *
     * @return HttpHeaders
     */
    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("app-key", API_KEY);
        return headers;
    }

    /**
     * This method creates sample send email response wrapper
     *
     * @return SendEmailResponseWrapper
     */
    private SendEmailResponseWrapper getSampleSendEmailResponseWrapper() {
        SendEmailResponseWrapper sendEmailResponseWrapper = new SendEmailResponseWrapper();
        sendEmailResponseWrapper.setMessage(EMAIL_MESSAGE);
        sendEmailResponseWrapper.setStatus(ResponseStatusType.SUCCESS);
        sendEmailResponseWrapper.setDisplayMessage(EMAIL_MESSAGE);
        return sendEmailResponseWrapper;
    }
}