package com.kupi.cc.location.service;

import com.kupi.cc.location.domain.entity.AuthUser;
import com.kupi.cc.location.domain.request.BulkBusinessProfileRequestDto;
import com.kupi.cc.location.domain.response.BulkBusinessResponseDto;
import com.kupi.cc.location.domain.response.BusinessResponseDto;
import com.kupi.cc.location.enums.UserType;
import com.kupi.cc.location.exception.InvalidUserException;
import com.kupi.cc.location.exception.KupiLocationException;
import com.kupi.cc.location.wrapper.AuthResponseWrapper;
import com.kupi.cc.location.wrapper.BulkBusinessProfileResponseWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests {@link AuthUserService} class
 */
class AuthUserServiceTest {

    private static final String USER_ID = "uid-123456789";
    private static final String BUSINESS_ID = "bid-123456789";
    private static final String EMAIL = "abc@ymail.com";
    private static final String AUTH_TOKEN = "Bearer192f0c96-dd19-404e-8217-a8fd1cc81d55";

    private AuthUserService authUserService;
    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        String getUserUrl = "/api/v1/users";
        String baseUrl = "http://localhost:8080/kupi-auth-service";
        String getBulkMerchantInfo = "/api/v1/users/##TO-USER-TYPE##/bulk-info";
        String getAppKey = "changeme-spring-application-app-key";
        String getPreferredMerchantUrl = "/api/v1/preferred-merchants";
        initMocks(this);
        authUserService = new AuthUserService(getUserUrl,
                baseUrl, getBulkMerchantInfo, getPreferredMerchantUrl, getAppKey, restTemplate);
    }


    /**
     * Get bulk business response
     */
    @Test
    void Should_ReturnBulkBusinessResponseDto_When_GettingBulkBusinessResponseForMerchantIsSuccessful() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(BulkBusinessProfileResponseWrapper.class)))
                .thenReturn(getSampleBulkBusinessProfileResponseWrapperResponseEntity());
        assertEquals(BUSINESS_ID, authUserService.getBulkBusinessResponse(USER_ID, getSampleBulkBusinessProfileRequestDto(),
                UserType.MERCHANT).getMerchants().get(0).getId());
    }

    @Test
    void Should_ReturnBulkBusinessResponseDto_When_GettingBulkBusinessResponseForBankIsSuccessful() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(BulkBusinessProfileResponseWrapper.class)))
                .thenReturn(getSampleBulkBusinessProfileResponseWrapperResponseEntity());
        assertEquals(BUSINESS_ID, authUserService.getBulkBusinessResponse(USER_ID, getSampleBulkBusinessProfileRequestDto(),
                UserType.BANK).getMerchants().get(0).getId());
    }

    @Test
    void Should_ThrowKupiLocationException_When_GettingBulkBusinessResponseIsFailed() {
        BulkBusinessProfileRequestDto bulkBusinessProfileRequestDto = getSampleBulkBusinessProfileRequestDto();
        HttpClientErrorException httpClientErrorException = new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(BulkBusinessProfileResponseWrapper.class))).thenThrow(httpClientErrorException);
        KupiLocationException exception = assertThrows(KupiLocationException.class, () -> authUserService
                .getBulkBusinessResponse(USER_ID, bulkBusinessProfileRequestDto, UserType.MERCHANT));
        assertEquals("Getting bulk business profile was failed from auth service", exception.getMessage());
    }

    /**
     * Get user by user id
     */
    @Test
    void Should_ReturnAuthUser_When_GettingUserByUserIdIsSuccessful() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(AuthResponseWrapper.class))).thenReturn(getSampleAuthResponseWrapperResponseEntity());
        assertEquals(EMAIL, authUserService.getUserByUserId(USER_ID, AUTH_TOKEN).getEmail());
    }

    @Test
    void Should_ThrowInvalidUserException_When_GettingUserByUserIdForInvalidUserId() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(AuthResponseWrapper.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        InvalidUserException exception = assertThrows(InvalidUserException.class, () -> authUserService
                .getUserByUserId(USER_ID, AUTH_TOKEN));
        assertEquals("Invalid userId: " + USER_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiLocationException_When_GettingUserByUserIdIsFailed() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(AuthResponseWrapper.class))).thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        KupiLocationException exception = assertThrows(KupiLocationException.class, () -> authUserService
                .getUserByUserId(USER_ID, AUTH_TOKEN));
        assertEquals("Requesting user data from auth service was failed.", exception.getMessage());
    }

    private BulkBusinessProfileRequestDto getSampleBulkBusinessProfileRequestDto() {
        List<String> userIdList = new ArrayList<>();
        userIdList.add(USER_ID);
        return new BulkBusinessProfileRequestDto(userIdList);
    }

    private ResponseEntity<BulkBusinessProfileResponseWrapper> getSampleBulkBusinessProfileResponseWrapperResponseEntity() {
        BulkBusinessProfileResponseWrapper bulkBusinessProfileResponseWrapper = new BulkBusinessProfileResponseWrapper();
        bulkBusinessProfileResponseWrapper.setData(getSampleBulkBusinessResponseDto());
        return new ResponseEntity<>(bulkBusinessProfileResponseWrapper, HttpStatus.OK);
    }

    private BulkBusinessResponseDto getSampleBulkBusinessResponseDto() {
        BulkBusinessResponseDto bulkBusinessResponseDto = new BulkBusinessResponseDto();
        bulkBusinessResponseDto.setMerchants(getSampleBusinessResponseDtoList());
        return bulkBusinessResponseDto;
    }

    private List<BusinessResponseDto> getSampleBusinessResponseDtoList() {
        List<BusinessResponseDto> businessResponseDtoList = new ArrayList<>();
        businessResponseDtoList.add(getSampleBusinessResponseDto());
        return businessResponseDtoList;
    }

    private BusinessResponseDto getSampleBusinessResponseDto() {
        BusinessResponseDto businessResponseDto = new BusinessResponseDto();
        businessResponseDto.setId(BUSINESS_ID);
        return businessResponseDto;
    }

    private AuthUser getSampleAuthUser() {
        AuthUser authUser = new AuthUser();
        authUser.setEmail(EMAIL);
        return authUser;
    }

    private ResponseEntity<AuthResponseWrapper> getSampleAuthResponseWrapperResponseEntity() {
        AuthResponseWrapper authResponseWrapper = new AuthResponseWrapper();
        authResponseWrapper.setData(getSampleAuthUser());
        return new ResponseEntity<>(authResponseWrapper, HttpStatus.OK);
    }
}