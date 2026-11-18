package com.kupi.cc.auth.service;

import com.kupi.cc.auth.configuration.Translator;
import com.kupi.cc.auth.domain.request.EmailRequestDto;
import com.kupi.cc.auth.domain.request.MobileNoRequestDto;
import com.kupi.cc.auth.domain.request.SmsRequestDto;
import com.kupi.cc.auth.enums.SendEmailDetails;
import com.kupi.cc.auth.exception.AuthServiceException;
import com.kupi.cc.auth.exception.SendSmsFailedException;
import com.kupi.cc.auth.wrapper.SendEmailResponseWrapper;
import com.kupi.cc.auth.wrapper.SendSmsResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
@Slf4j
public class NotificationService {
    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String APP_KEY = "app-key";
    private static final String TOKEN_TARGET = "$$TOKEN$$";
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
        this.mailSendUri = baseUrl + mailPath;
        this.smsSendUri = baseUrl + smsPath;
        this.utilServiceAppKey = appKey;
        this.restTemplate = restTemplate;
        this.enableNotification = isNotificationEnable;
    }


    /**
     * This method allows sending email.
     *
     * @param receiver receiver email
     * @param subject  email subject
     * @param body     email body
     * @param timeZone time zone
     */
    @Async
    public void sendMail(String receiver, String subject, String body, String timeZone) {
        if (enableNotification) {
            var headers = getAuthHeaders(timeZone);
            var mailRequestDto = new EmailRequestDto(receiver, subject, body);
            HttpEntity<EmailRequestDto> entity = new HttpEntity<>(mailRequestDto, headers);
            try {
                log.debug("Calling util service to send the email to: {}", receiver);
                restTemplate.exchange(mailSendUri, HttpMethod.POST, entity, SendEmailResponseWrapper.class);
                log.debug("Sending email was success to: {}", receiver);
            } catch (HttpClientErrorException e) {
                log.error("Sending email was failed to: {}", receiver, e);
                throw new AuthServiceException("Sending email was failed.", e);
            }
        }
    }


    /**
     * This method allows sending email.
     *
     * @param recipientNo recipient number
     * @param sms         sms
     * @param timeZone    time zone
     */
    @Async
    public void sendSms(MobileNoRequestDto recipientNo, String sms, String timeZone) {
        if (enableNotification) {
            var headers = getAuthHeaders(timeZone);
            var smsRequestDto = new SmsRequestDto(sms, recipientNo);
            HttpEntity<SmsRequestDto> entity = new HttpEntity<>(smsRequestDto, headers);

            try {
                log.debug("Calling util service to send the sms to: {}", recipientNo.toLogJson());
                restTemplate.exchange(smsSendUri, HttpMethod.POST, entity, SendSmsResponseWrapper.class);
                log.debug("Sending sms was success to: {}", recipientNo.toLogJson());
            } catch (HttpClientErrorException e) {
                log.error("Sending sms was failed to: {}", recipientNo.toLogJson(), e);
                throw new AuthServiceException("Sending sms was failed.", e);
            }
        }
    }

    /**
     * This method is used to send password reset token in sms.
     *
     * @param recipientNo        recipient number
     * @param passwordResetToken password reset token
     */
    public void sendPasswordResetSms(MobileNoRequestDto recipientNo, String passwordResetToken) {
        String message = translator
                .toLocale(SendEmailDetails.PASSWORD_RESET_MSG.getCode()).replace(TOKEN_TARGET, passwordResetToken);
        HttpHeaders headers = getAuthHeaders(TIME_ZONE);
        SmsRequestDto smsRequestDto = new SmsRequestDto(message, recipientNo);
        HttpEntity<SmsRequestDto> entity = new HttpEntity<>(smsRequestDto, headers);
        try {
            log.debug("Sending reset password token: {} for mobileNo: {}", passwordResetToken, recipientNo.getNo());
            ResponseEntity<SendSmsResponseWrapper> result =
                    restTemplate.exchange(smsSendUri, HttpMethod.POST, entity, SendSmsResponseWrapper.class);
            log.info("Successfully sent password reset token sms for mobileNo: {} httpStatusCode: {}",
                    recipientNo.getNo(), result.getStatusCode());
        } catch (HttpClientErrorException e) {
            log.debug("Sending reset password token: {} was failed for mobileNo: {}, httpStatusCode: {}",
                    passwordResetToken, recipientNo.getNo(), e.getStatusCode());
            throw new SendSmsFailedException("Failed to send password reset token", e);
        }
    }

    /**
     * Send OTP SMS message.
     *
     * @param recipientNo recipient mobile number
     * @param otpToken OTP token to send
     */
    public void sendOtpSms(MobileNoRequestDto recipientNo, String otpToken) {
        String message = translator
                .toLocale(SendEmailDetails.OTP_MSG.getCode()).replace(TOKEN_TARGET, otpToken);
        HttpHeaders headers = getAuthHeaders(TIME_ZONE);
        SmsRequestDto smsRequestDto = new SmsRequestDto(message, recipientNo);
        HttpEntity<SmsRequestDto> entity = new HttpEntity<>(smsRequestDto, headers);
        try {
            log.debug("Sending OTP token: {} for mobileNo: {}", otpToken, recipientNo.getNo());
            ResponseEntity<SendSmsResponseWrapper> result =
                    restTemplate.exchange(smsSendUri, HttpMethod.POST, entity, SendSmsResponseWrapper.class);
            log.info("Successfully sent OTP sms for mobileNo: {} httpStatusCode: {}",
                    recipientNo.getNo(), result.getStatusCode());
        } catch (HttpClientErrorException e) {
            log.debug("Sending OTP token: {} was failed for mobileNo: {}, httpStatusCode: {}",
                    otpToken, recipientNo.getNo(), e.getStatusCode());
            throw new SendSmsFailedException("Failed to send OTP token", e);
        }
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
        headers.set(APP_KEY, utilServiceAppKey);
        return headers;
    }
}
