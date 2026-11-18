package com.kupi.cc.base.service;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.request.MailRequestDto;
import com.kupi.cc.base.domain.request.MobileNoRequestDto;
import com.kupi.cc.base.domain.request.SmsRequestDto;
import com.kupi.cc.base.enums.ApprovalStatus;
import com.kupi.cc.base.enums.NotificationTemplateType;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.wrapper.SendEmailResponseWrapper;
import com.kupi.cc.base.wrapper.SendSmsResponseWrapper;
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

@Service
@Slf4j
public class NotificationService {

    private static final String USER = "<USER>";
    private static final String SUBSCRIPTION_PLAN = "<SUBSCRIPTION-PLAN>";
    private static final String EXPIRE_DATE = "<EXPIRE-DATE>";
    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String TIME_ZONE_VALUE = "Asia/Colombo";
    private static final String APP_KEY = "app-key";
    private final String utilServiceAppKey;
    private final String mailSendUri;
    private final String smsSendUri;
    private final RestTemplate restTemplate;
    private final Translator translator;
    private final boolean enableNotification;

    @Autowired
    public NotificationService(@Value("${util.baseUrl}") String baseUrl,
                               @Value("${util.uri.sendMail}") String mailPath,
                               @Value("${util.uri.sendSms}") String smsPath,
                               @Value("${notification.enableNotification}") boolean isNotificationEnable,
                               @Value("${util.utilServiceAppKey}") String appKey,
                               RestTemplate restTemplate, Translator translator) {
        this.translator = translator;
        this.enableNotification = isNotificationEnable;
        this.mailSendUri = baseUrl + mailPath;
        this.smsSendUri = baseUrl + smsPath;
        this.utilServiceAppKey = appKey;
        this.restTemplate = restTemplate;
    }

    /**
     * This method send mail
     *
     * @param receiver       receiver email
     * @param approvalStatus APPROVED/REJECTED
     * @param userName       receiver name
     */
    @Async
    public void sendMail(String receiver, ApprovalStatus approvalStatus, String userName) {
        if (enableNotification) {
            var headers = getAuthHeaders();
            var mailRequestDto = approvalStatus == ApprovalStatus.APPROVED ?
                    new MailRequestDto(receiver,
                            translator.toLocale(NotificationTemplateType.APPROVED.getEmailHeader()),
                            translator.toLocale(NotificationTemplateType.APPROVED.getEmailBody().
                                    replace(USER, userName)))
                    : new MailRequestDto(receiver,
                    translator.toLocale(NotificationTemplateType.REJECTED.getEmailHeader()),
                    translator.toLocale(NotificationTemplateType.REJECTED.getEmailBody()).replace(USER, userName));

            HttpEntity<MailRequestDto> entity = new HttpEntity<>(mailRequestDto, headers);
            try {
                log.debug("Calling util service to send the email to: {}", receiver);
                restTemplate.exchange(mailSendUri, HttpMethod.POST, entity, SendEmailResponseWrapper.class);
                log.debug("Sending email was success to: {}", receiver);
            } catch (HttpClientErrorException e) {
                log.error("Sending email was failed to: {}", receiver, e);
                throw new KupiCoreException("Sending email was failed.", e);
            }
        }
    }

    /**
     * This method send sms
     *
     * @param recipientNo    recipient mobile number
     * @param approvalStatus APPROVED/REJECTED
     */
    @Async
    public void sendSms(MobileNoRequestDto recipientNo, ApprovalStatus approvalStatus) {
        if (enableNotification) {
            var headers = getAuthHeaders();
            var smsRequestDto = approvalStatus == ApprovalStatus.APPROVED ?
                    new SmsRequestDto(recipientNo, translator.toLocale(NotificationTemplateType.APPROVED.getSms())) :
                    new SmsRequestDto(recipientNo, translator.toLocale(NotificationTemplateType.REJECTED.getSms()));
            HttpEntity<SmsRequestDto> entity = new HttpEntity<>(smsRequestDto, headers);
            try {
                log.debug("Calling util service to send the sms to: {}", recipientNo.toLogJson());
                restTemplate.exchange(smsSendUri, HttpMethod.POST, entity, SendSmsResponseWrapper.class);
                log.debug("Sending sms was success to: {}", recipientNo.toLogJson());
            } catch (HttpClientErrorException e) {
                log.error("Sending sms was failed to: {}", recipientNo.toLogJson(), e);
                throw new KupiCoreException("Sending sms was failed.", e);
            }
        }
    }

    /**
     * This method send sms notification for subscription
     *
     * @param recipientNo recipient mobile number
     * @param subscriptionPlanName subscription plan name
     * @param expireDate expire date
     */
    public void sendSms(MobileNoRequestDto recipientNo, String subscriptionPlanName, String expireDate) {
        if (enableNotification) {
            var headers = getAuthHeaders();
            var smsRequestDto = new SmsRequestDto(recipientNo,
                    translator.toLocale(NotificationTemplateType.RENEW_SUBSCRIPTION.getSms())
                            .replace(SUBSCRIPTION_PLAN, subscriptionPlanName).replace(EXPIRE_DATE, expireDate));
            HttpEntity<SmsRequestDto> entity = new HttpEntity<>(smsRequestDto, headers);
            try {
                log.debug("Calling util service to send the sms to: {}", recipientNo.toLogJson());
                restTemplate.exchange(smsSendUri, HttpMethod.POST, entity, SendSmsResponseWrapper.class);
                log.debug("Sending sms was success to: {}", recipientNo.toLogJson());
            } catch (HttpClientErrorException e) {
                log.error("Sending sms was failed to: {}", recipientNo.toLogJson(), e);
                throw new KupiCoreException("Sending sms was failed.", e);
            }
        }
    }

    /**
     * This method returns headers for util service urls.
     */
    private HttpHeaders getAuthHeaders() {
        var headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(TIME_ZONE_HEADER, TIME_ZONE_VALUE);
        headers.set(APP_KEY, utilServiceAppKey);
        return headers;
    }
}
