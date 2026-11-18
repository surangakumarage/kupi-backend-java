package com.kupi.cc.location.service;

import com.kupi.cc.location.configuration.Translator;
import com.kupi.cc.location.domain.request.ApprovalStatusUpdateRequestDto;
import com.kupi.cc.location.domain.request.MailRequestDto;
import com.kupi.cc.location.enums.ApprovalAction;
import com.kupi.cc.location.enums.MailTemplateType;
import com.kupi.cc.location.exception.KupiLocationException;
import com.kupi.cc.location.wrapper.SendEmailResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * Mail Service
 */
@Service
@Slf4j
public class MailService {
    private static final String USER = "<USER>";
    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String TIME_ZONE_VALUE = "Asia/Colombo";
    private static final String APP_KEY = "app-key";
    private final String utilServiceAppKey;
    private final String mailSendUri;
    private final RestTemplate restTemplate;
    private final Translator translator;
    private final boolean enableNotification;
    private final AuthUserService authUserService;

    @Autowired
    public MailService(@Value("${util.baseUrl}") String baseUrl,
                       @Value("${util.uri.sendMail}") String mailPath,
                       @Value("${notification.enableNotification}") boolean isNotificationEnable,
                       @Value("${util.utilServiceAppKey}") String appKey,
                       RestTemplate restTemplate, Translator translator, AuthUserService authUserService) {
        this.translator = translator;
        this.enableNotification = isNotificationEnable;
        this.authUserService = authUserService;
        this.mailSendUri = baseUrl + mailPath;
        this.utilServiceAppKey = appKey;
        this.restTemplate = restTemplate;
    }

    @Async
    public void sendMail(String receiver, ApprovalAction approvalAction, String userName) {
        if (enableNotification) {
            var headers = getAuthHeaders();
            MailRequestDto mailRequestDto = approvalAction == ApprovalAction.APPROVE ?
                    new MailRequestDto(receiver,
                            translator.toLocale(MailTemplateType.APPROVED.getEmailHeader()),
                            translator.toLocale(MailTemplateType.APPROVED.getEmailBody()).replace(USER, userName))
                    : new MailRequestDto(receiver,
                    translator.toLocale(MailTemplateType.REJECTED.getEmailHeader()),
                    translator.toLocale(MailTemplateType.REJECTED.getEmailBody()).replace(USER, userName));

            HttpEntity<MailRequestDto> entity = new HttpEntity<>(mailRequestDto, headers);
            try {
                log.debug("Calling util service to send the email to: {}", receiver);
                restTemplate.exchange(mailSendUri, HttpMethod.POST, entity, SendEmailResponseWrapper.class);
                log.debug("Sending email was success to: {}", receiver);
            } catch (HttpClientErrorException e) {
                log.error("Sending email was failed to: {}", receiver, e);
                throw new KupiLocationException("Sending email was failed.", e);
            }
        }
    }

    /**
     * This method gets AuthHeaders
     *
     * @return headers
     */
    private HttpHeaders getAuthHeaders() {
        var headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(TIME_ZONE_HEADER, TIME_ZONE_VALUE);
        headers.set(APP_KEY, utilServiceAppKey);
        return headers;
    }

    /**
     * This method sends mail to user
     *
     * @param userId                         userId
     * @param approvalStatusUpdateRequestDto approvalStatusUpdateRequestDto
     * @param authToken                      authToken
     */
    public void sendMailToUser(String userId, ApprovalStatusUpdateRequestDto approvalStatusUpdateRequestDto,
                               String authToken) {
        var authUser = authUserService.getUserByUserId(userId, authToken);
        if (authUser.getEmail() != null) {
            sendMail(authUser.getEmail(), approvalStatusUpdateRequestDto.getAction(),
                    authUser.getFullName());
        }
    }
}