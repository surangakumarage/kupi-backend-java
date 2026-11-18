package com.kupi.cc.location.service;

import com.kupi.cc.location.domain.request.PushNotificationRequestDto;
import com.kupi.cc.location.exception.NotificationServiceClientErrorException;
import com.kupi.cc.location.wrapper.BulkNotificationResponseWrapper;
import lombok.extern.slf4j.Slf4j;
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
 * Notification Service
 */
@Slf4j
@Service
public class NotificationService {

    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String APP_KEY = "App-key";
    private static final String FAILED_TO_SEND_PUSH_NOTIFICATION = "Failed to send push notification. ";
    protected static final String TIME_ZONE_VALUE = "Asia/Colombo";
    private final String appKey;
    private final RestTemplate restTemplate;
    private final String pushNotificationUrl;

    public NotificationService(@Value("${notification.baseUrl}") String baseUrl,
                               @Value("${notification.pushNotification}") String pushNotificationUrl,
                               @Value("${auth.appKey}") String getAppKey,
                               RestTemplate restTemplate) {
        this.appKey = getAppKey;
        this.restTemplate = restTemplate;
        this.pushNotificationUrl = baseUrl + pushNotificationUrl;
    }

    /**
     * This method sends deals push notifications.
     *
     * @param pushNotificationRequestDto pushNotificationRequestDto
     */
    @Async("notificationExecutor")
    public void sendNotification(PushNotificationRequestDto pushNotificationRequestDto) {
        try {
            HttpHeaders headers = getHeaders();
            HttpEntity<PushNotificationRequestDto> entity = new HttpEntity<>(pushNotificationRequestDto, headers);
            log.debug("Calling notification service to push notification. url: {}", pushNotificationUrl);
            restTemplate.exchange(pushNotificationUrl, HttpMethod.POST, entity, BulkNotificationResponseWrapper.class);
            log.debug("successfully sent the push notifications to user :{}", pushNotificationRequestDto.getUserId());
        } catch (HttpClientErrorException e) {
            throw new NotificationServiceClientErrorException(e.getStatusCode(), FAILED_TO_SEND_PUSH_NOTIFICATION, e);
        }
    }

    /**
     * This method returns header for send bulk deal push notification.
     *
     * @return headers
     */
    private HttpHeaders getHeaders() {
        var headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(TIME_ZONE_HEADER, TIME_ZONE_VALUE);
        headers.set(APP_KEY, appKey);
        return headers;
    }
}
