package com.kupi.notification.service;

import com.kupi.notification.domain.entity.AuthUser;
import com.kupi.notification.exception.NotificationServiceException;
import com.kupi.notification.wrapper.AuthResponseWrapper;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class AuthUserServiceTest {

    private static final String TOKEN = "60a3ff04-0704-45c2-bfd7-84a96da05013";
    private static final String USER_ID = "uid-60a3ff04-0704-45c2-bfd7-84a96da05013";
    private static final String TIME_ZONE_VALUE = "Asia/Colombo";
    private static final String BASE_URL = "http://localhost:8085/tokobook-auth-service";
    private static final String GET_USER_BY_ID__URI = "/api/v1/users";
    private static final String GET_USER_BY_ID_URL = "http://localhost:8085/tokobook-auth-service/api/v1/users";

    @Mock
    private RestTemplate restTemplate;
    private AuthUserService authUserService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        authUserService = new AuthUserService(BASE_URL,
                GET_USER_BY_ID__URI,
                restTemplate);
    }

    @Test
    void Should_ReturnUser_When_UserIdIsGiven() {
        AuthUser authUser = new AuthUser();
        authUser.setUserId(USER_ID);
        AuthResponseWrapper authResponseWrapper = new AuthResponseWrapper();
        authResponseWrapper.setData(authUser);
        ResponseEntity<AuthResponseWrapper> responseEntity = new ResponseEntity<>(authResponseWrapper, HttpStatus.OK);
        when(restTemplate.exchange(eq(GET_USER_BY_ID_URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(AuthResponseWrapper.class)))
                .thenReturn(responseEntity);
        AuthUser result = authUserService.getUserByUserId(USER_ID, TOKEN, TIME_ZONE_VALUE);
        assertEquals(USER_ID, result.getUserId());
    }

    @Test
    void Should_ThrowException_When_GettingUserByIdFromAuthServiceIsFailed() {
        AuthUser authUser = new AuthUser();
        AuthResponseWrapper authResponseWrapper = new AuthResponseWrapper();
        authResponseWrapper.setData(authUser);
        when(restTemplate.exchange(eq(GET_USER_BY_ID_URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(AuthResponseWrapper.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        NotificationServiceException exception = assertThrows(NotificationServiceException.class, () ->
                authUserService.getUserByUserId(USER_ID, TOKEN, TIME_ZONE_VALUE)
        );
        assertEquals("Getting user by id was failed from auth service", exception.getMessage());
    }
}
