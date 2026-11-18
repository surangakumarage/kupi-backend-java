package com.kupi.cc.auth.service;

import com.kupi.cc.auth.exception.AuthServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * Core Service
 */
@Slf4j
@Service
public class CoreService {

    protected static final String TIME_ZONE_HEADER = "Time-Zone";
    protected static final String APP_KEY = "app-key";
    protected static final String TIME_ZONE_VALUE = "Asia/Colombo";
    private static final String USER_ID_HEADER = "User-Id";
    private final RestTemplate restTemplate;
    private final String assignDefaultSubscriptionUrl;
    private final String getAppKey;

    public CoreService(@Value("${core.baseUrl}") String baseUrl,
                       @Value("${core.uri.assignDefaultSubscription}") String assignDefaultUri,
                       @Value("${core.appKey}") String getAppKey,

                       RestTemplate restTemplate) {
        this.assignDefaultSubscriptionUrl = baseUrl + assignDefaultUri;
        this.restTemplate = restTemplate;
        this.getAppKey = getAppKey;
    }

    /**
     * This method used to assign default plan for user.
     *
     * @param userId string user id
     */
    public void assignDefaultSubscription(String userId) {
        try {
            HttpHeaders headers = getCoreServiceHeadersForDefaultSubscription();
            headers.set(USER_ID_HEADER, userId);
            HttpEntity<Void> request = new HttpEntity<>(headers);
            restTemplate.exchange(assignDefaultSubscriptionUrl, HttpMethod.POST, request, Void.class);
        } catch (Exception e) {
            throw new AuthServiceException("Failed to assign default package for mobile user for id :" + userId, e);
        }
    }

    /**
     * This method used to get core service headers for default subscription.
     *
     * @return http headers
     */
    private HttpHeaders getCoreServiceHeadersForDefaultSubscription() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(TIME_ZONE_HEADER, TIME_ZONE_VALUE);
        headers.set(APP_KEY, getAppKey);
        return headers;
    }
}
