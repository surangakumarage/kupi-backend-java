package com.kupi.otp.service;

import com.kupi.otp.domain.request.MobileNoRequestDto;
import com.kupi.otp.domain.request.VerifyMobileRequest;
import com.kupi.otp.exception.OTPServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class AuthService {
    protected static final String TIME_ZONE_VALUE = "Asia/Colombo";
    protected static final String TIME_ZONE_HEADER = "Time-Zone";
    protected static final String APP_KEY = "app-key";


    private final String verifyEmailUrl;
    private final String verifyMobileUrl;
    private final RestTemplate restTemplate;
    private final String getAppKey;


    public AuthService(@Value("${auth.baseUrl}") String baseUrl,
                       @Value("${auth.uri.verifyEmail}") String verifyEmailUrl,
                       @Value("${auth.uri.verifyMobile}") String verifyMobileUrl,
                       RestTemplate restTemplate,
                       @Value("${auth.appKey}") String getAppKey) {
        this.verifyEmailUrl = baseUrl + verifyEmailUrl;
        this.verifyMobileUrl = baseUrl + verifyMobileUrl;
        this.restTemplate = restTemplate;
        this.getAppKey = getAppKey;
    }


    public void notifyEmailVerified(String email) {
        HttpHeaders headers = buildHeaders();
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(Map.of("email", email), headers);
        try {
            log.debug("Calling Auth to mark email verified. url: {}, email: {}", verifyEmailUrl, email);

            ResponseEntity<String> result =
                    restTemplate.exchange(verifyEmailUrl, HttpMethod.POST, entity, String.class);

            if (!result.getStatusCode().is2xxSuccessful()) {
                log.error("Auth email verification non-2xx. status: {}, body: {}", result.getStatusCode(), result.getBody());
                throw new OTPServiceException("Auth email verification failed with non-2xx");
            }

            log.debug("Auth email verification OK. status: {}", result.getStatusCode());
        } catch (HttpClientErrorException e) {
            log.error("Auth email verification failed. status: {}, body: {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new OTPServiceException("Failed to call auth email verification endpoint", e);
        } catch (Exception e) {
            throw new OTPServiceException("Unexpected error calling auth email verification endpoint", e);
        }
    }

    public void notifyMobileVerified(MobileNoRequestDto mobile) {
        HttpHeaders headers = buildHeaders();
        VerifyMobileRequest body = new VerifyMobileRequest(mobile);

        HttpEntity<VerifyMobileRequest> entity = new HttpEntity<>(body, headers);


        try {
            log.debug("Calling Auth to mark mobile verified. url: {}, mobile: {}", verifyMobileUrl, mobile.getNo());

            ResponseEntity<String> result =
                    restTemplate.exchange(verifyMobileUrl, HttpMethod.POST, entity, String.class);

            if (!result.getStatusCode().is2xxSuccessful()) {
                log.error("Auth mobile verification non-2xx. status: {}, body: {}", result.getStatusCode(), result.getBody());
                throw new OTPServiceException("Auth mobile verification failed with non-2xx");
            }

            log.debug("Auth mobile verification OK. status: {}", result.getStatusCode());
        } catch (HttpClientErrorException e) {
            log.error("Auth mobile verification failed. status: {}, body: {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new OTPServiceException("Failed to call auth mobile verification endpoint", e);
        } catch (Exception e) {
            throw new OTPServiceException("Unexpected error calling auth mobile verification endpoint", e);
        }
    }


    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(TIME_ZONE_HEADER, TIME_ZONE_VALUE);
        headers.set(APP_KEY, getAppKey);
        return headers;
    }
}
