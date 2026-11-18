package com.kupi.cc.location.service;

import com.kupi.cc.location.configuration.Translator;
import com.kupi.cc.location.domain.entity.AuthUser;
import com.kupi.cc.location.domain.request.ApprovalStatusUpdateRequestDto;
import com.kupi.cc.location.enums.ApprovalAction;
import com.kupi.cc.location.exception.KupiLocationException;
import com.kupi.cc.location.wrapper.SendEmailResponseWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests {@link MailService} class
 */
class MailServiceTest {

    private static final String USER_ID = "uid-123456789";
    private static final String AUTH_TOKEN = "Bearer192f0c96-dd19-404e-8217-a8fd1cc81d55";
    private static final String FULL_NAME = "FULL_NAME";
    private static final String EMAIL = "abc@ymail.com";

    private MailService mailService;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private Translator translator;
    @Mock
    private AuthUserService authUserService;

    @BeforeEach
    void setUp() {
        String baseUrl = "http://localhost:8085/kupi-util-service";
        String mailPath = "/api/v1/email/send";
        boolean isNotificationEnable = true;
        String appKey = "changeme-util-service-app-key";
        initMocks(this);
        mailService = new MailService(baseUrl, mailPath, isNotificationEnable, appKey, restTemplate, translator,
                authUserService);
    }

    @AfterEach
    void tearDown() {
    }

    /**
     * Send mail to user
     */
    @Test
    void Should_SendMailToUserForApprovedApprovalAction() {
        when(authUserService.getUserByUserId(anyString(), anyString())).thenReturn(getSampleAuthUser());
        when(translator.toLocale(anyString())).thenReturn("");
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(SendEmailResponseWrapper.class))).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mailService.sendMailToUser(USER_ID, getSampleApprovalStatusUpdateRequestDto(), AUTH_TOKEN);

        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class),
                any(HttpEntity.class), eq(SendEmailResponseWrapper.class));
    }

    @Test
    void Should_SendMailToUserForRejectedApprovalAction() {
        when(authUserService.getUserByUserId(anyString(), anyString())).thenReturn(getSampleAuthUser());
        when(translator.toLocale(anyString())).thenReturn("");
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(SendEmailResponseWrapper.class))).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        ApprovalStatusUpdateRequestDto approvalStatusUpdateRequestDto = getSampleApprovalStatusUpdateRequestDto();
        approvalStatusUpdateRequestDto.setAction(ApprovalAction.REJECT);
        mailService.sendMailToUser(USER_ID, approvalStatusUpdateRequestDto, AUTH_TOKEN);

        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class),
                any(HttpEntity.class), eq(SendEmailResponseWrapper.class));
    }

    @Test
    void Should_ThrowKupiLocationException_When_SendingMailToUserForSendingEmailIsFailed() {
        ApprovalStatusUpdateRequestDto approvalStatusUpdateRequestDto = getSampleApprovalStatusUpdateRequestDto();
        when(authUserService.getUserByUserId(anyString(), anyString())).thenReturn(getSampleAuthUser());
        when(translator.toLocale(anyString())).thenReturn("");
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(SendEmailResponseWrapper.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        KupiLocationException exception = assertThrows(KupiLocationException.class, () -> mailService
                .sendMailToUser(USER_ID, approvalStatusUpdateRequestDto, AUTH_TOKEN));
        assertEquals("Sending email was failed.", exception.getMessage());
    }

    private ApprovalStatusUpdateRequestDto getSampleApprovalStatusUpdateRequestDto() {
        ApprovalStatusUpdateRequestDto approvalStatusUpdateRequestDto = new ApprovalStatusUpdateRequestDto();
        approvalStatusUpdateRequestDto.setId(USER_ID);
        approvalStatusUpdateRequestDto.setAction(ApprovalAction.APPROVE);
        return approvalStatusUpdateRequestDto;
    }

    private AuthUser getSampleAuthUser() {
        AuthUser authUser = new AuthUser();
        authUser.setFullName(FULL_NAME);
        authUser.setEmail(EMAIL);
        return authUser;
    }
}