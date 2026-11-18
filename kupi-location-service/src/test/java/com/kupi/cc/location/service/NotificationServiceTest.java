package com.kupi.cc.location.service;

import com.kupi.cc.location.domain.request.PushNotificationRequestDto;
import com.kupi.cc.location.exception.NotificationServiceClientErrorException;
import com.kupi.cc.location.wrapper.BulkNotificationResponseWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests {@link NotificationService} class
 */
class NotificationServiceTest {

    private static final String API_KEY = "changeme-spring-application-app-key";
    private static final String BASE_URL = "http://localhost:3000";
    private static final String PUSH_NOTIFICATION_URL = "/api/v3/notifications/push";
    private static final String APP_KEY_HEADER = "App-key";
    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String USER_ID = "uid-123456789";
    @Mock
    private RestTemplate restTemplate;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        notificationService = new NotificationService(BASE_URL, PUSH_NOTIFICATION_URL, API_KEY, restTemplate);
    }


    @Test
    void Should_SendPushNotification() {
        PushNotificationRequestDto pushNotificationRequestDto = new PushNotificationRequestDto();
        pushNotificationRequestDto.setUserId(USER_ID);
        var headers = getHeaders();
        var entity = new HttpEntity<>(pushNotificationRequestDto, headers);
        notificationService.sendNotification(pushNotificationRequestDto);
        verify(restTemplate).exchange(BASE_URL + PUSH_NOTIFICATION_URL, HttpMethod.POST, entity,
                BulkNotificationResponseWrapper.class);
    }

    @Test
    void Should_ThrowNotificationServiceClientErrorException_When_sendNotificationIsFailed() {
        PushNotificationRequestDto pushNotificationRequestDto = new PushNotificationRequestDto();
        pushNotificationRequestDto.setUserId(USER_ID);
        var headers = getHeaders();
        var entity = new HttpEntity<>(pushNotificationRequestDto, headers);
        when(restTemplate.exchange(BASE_URL + PUSH_NOTIFICATION_URL, HttpMethod.POST, entity,
                BulkNotificationResponseWrapper.class)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        NotificationServiceClientErrorException exception = assertThrows(NotificationServiceClientErrorException.class, () ->
                notificationService.sendNotification(pushNotificationRequestDto));
        assertEquals("400 Failed to send push notification. ", exception.getMessage());
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
        headers.set(TIME_ZONE_HEADER, TIME_ZONE);
        headers.set(APP_KEY_HEADER, API_KEY);
        return headers;
    }
}
