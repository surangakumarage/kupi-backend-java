package com.kupi.notification.service;

import com.kupi.notification.domain.entity.AuthUser;
import com.kupi.notification.exception.NotificationServiceException;
import com.kupi.notification.wrapper.AuthResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Objects;


/**
 * Auth user Service
 */
@Slf4j
@Service
public class AuthUserService {

    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String USER_ID_HEADER = "User-Id";
    private static final String FAILED_GET_USER_BY_ID = "Getting user by id was failed from auth service";
    private static final String FAILED_LOG_MESSAGE = "{}. statusCode: {}, body: {}";
    private final String getUserUrl;
    private final RestTemplate restTemplate;

    @Autowired
    public AuthUserService(@Value("${auth.baseUrl}") String baseUrl,
                           @Value("${auth.uri.getUser}") String getUserUri,
                           RestTemplate restTemplate) {
        this.getUserUrl = baseUrl + getUserUri;
        this.restTemplate = restTemplate;
    }

    /**
     * This method get auth user by id.
     *
     * @param userId   userId
     * @param token    token
     * @param timeZone timeZone
     * @return auth user
     */
    public AuthUser getUserByUserId(String userId, String token, String timeZone) {
        HttpHeaders headers = getAuthHeaders(token, userId, timeZone);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        try {
            log.debug("Calling auth service to get the user by Id. url: {}, userId: {}", getUserUrl, userId);
            ResponseEntity<AuthResponseWrapper> result =
                    restTemplate.exchange(getUserUrl, HttpMethod.GET, entity, AuthResponseWrapper.class);
            String responseBody = result.getBody() == null ? "" : Objects.requireNonNull(result.getBody()).toLogJson();

            log.debug("Getting user by id was successful. statusCode: {}, response: {}",
                    result.getStatusCode(), responseBody);
            return Objects.requireNonNull(result.getBody()).getData();
        } catch (HttpClientErrorException e) {
            log.error(FAILED_LOG_MESSAGE, FAILED_GET_USER_BY_ID, e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new NotificationServiceException(FAILED_GET_USER_BY_ID, e);
        }
    }


    /**
     * This method returns headers for auth service urls.
     *
     * @param token    token
     * @param userId   userId
     * @param timeZone timeZone
     * @return headers
     */
    private HttpHeaders getAuthHeaders(String token, String userId, String timeZone) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token.trim());
        headers.set(TIME_ZONE_HEADER, timeZone);
        headers.set(USER_ID_HEADER, userId);
        return headers;
    }

}
