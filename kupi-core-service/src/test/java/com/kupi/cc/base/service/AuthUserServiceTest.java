package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.AuthUser;
import com.kupi.cc.base.domain.entity.Deal;
import com.kupi.cc.base.domain.request.BulkUserRequestDto;
import com.kupi.cc.base.domain.response.BulkMerchantBusinessResponseDto;
import com.kupi.cc.base.domain.response.BusinessMerchantResponseDto;
import com.kupi.cc.base.domain.response.MerchantBusinessResponseDto;
import com.kupi.cc.base.domain.response.dashboard.AuthSummaryResponseDto;
import com.kupi.cc.base.enums.ApprovalStatus;
import com.kupi.cc.base.enums.ResponseStatusType;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.InvalidUserException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.wrapper.AuthResponseWrapper;
import com.kupi.cc.base.wrapper.AuthSummaryResponseWrapper;
import com.kupi.cc.base.wrapper.BulkMerchantResponseWrapper;
import com.kupi.cc.base.wrapper.MerchantBusinessResponseWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests the {@link AuthUserService} class
 */
class AuthUserServiceTest {
    private static final String TOKEN = "3d80eadf-b463-4b7b-ae5e-12e31b141267";
    private static final String MERCHANT_ID = "uid-fa2c6db9-1dda-47a1-a079-a1893a02ccac";
    private static final String BANK_ID = "bid-01fd1837-bb75-46c5-827c-73addbf95250";
    private static final String USER_ID = "uid-f8cf77cb-30e5-4a96-93d3-5b47e6dfddc5";
    private static final String FULL_NAME = "Karim Benzema";
    private static final long ANY_LONG = 20;

    @Mock
    private RestTemplate restTemplate;
    private AuthUserService authUserService;

    @BeforeEach
    void setUp() {
        String baseUrl = "http://localhost:8080/kupi-auth-service";
        String getUserUri = "/api/v1/users";
        String getBulkMerchantInfo = "/api/v1/users/##TO-USER-TYPE##/bulk-info";
        String getBulkUserInfo = "/api/v1/users/bulk-info";
        String getMerchantInfo = "/api/v1/users/MERCHANT/business/merchant/##MERCHANT-ID##/APPROVED";
        String getBankInfo = "/api/v1/users/BANK/business/merchant/##BANK-ID##/APPROVED";
        String getTodaySummaryUrl = "/api/v1/summary/##TO-USER-TYPE##";
        String getMerchantAccessKeyUrl = "/api/v1/users/get-api-access-key/MERCHANT/##MERCHANT-ID##";
        String getAppKey = "changeme-spring-application-app-key";
        String getAllMerchantEndpoint = "/api/v1/users/MERCHANT/##PAGE##/##SIZE##/search/##SEARCH##";
        String getSliderImages = "/api/v1/users/MERCHANT/business/slider-images/internal/##MERCHANT-ID##";
        String getBulkSliderImages = "/api/v1/users/MERCHANT/business/slider-images/internal/bulk";
        initMocks(this);
        authUserService = new AuthUserService(baseUrl, getUserUri, getBulkMerchantInfo, getBulkUserInfo,
                getMerchantInfo, getBankInfo, getTodaySummaryUrl, getMerchantAccessKeyUrl,
                getAppKey, getAllMerchantEndpoint, getSliderImages, getBulkSliderImages, restTemplate);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnBusinessMerchantResponseDto_When_ValidTokenAndMerchantIdAreProvided() {
        ResponseEntity<MerchantBusinessResponseWrapper> merchantBusinessResponseWrapperResponse =
                getSampleResponse();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(MerchantBusinessResponseWrapper.class))).thenReturn(merchantBusinessResponseWrapperResponse);
        assertEquals(businessMerchantResponseDto.getMerchantId(),
                authUserService.getMerchantBusinessByMerchantId(TOKEN, MERCHANT_ID).getMerchantId());
        assertEquals(businessMerchantResponseDto.getBusinessName(),
                authUserService.getMerchantBusinessByMerchantId(TOKEN, MERCHANT_ID).getBusinessName());
    }

    @Test
    void Should_ThrowInvalidUserException_When_InvalidMerchantIdIsProvided() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(MerchantBusinessResponseWrapper.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        InvalidUserException invalidUserException = assertThrows(InvalidUserException.class, () ->
                authUserService.getMerchantBusinessByMerchantId(TOKEN, MERCHANT_ID));
        assertEquals("Failed to get approved business info from auth service. userId: " + MERCHANT_ID,
                invalidUserException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_InvalidMerchantIdIsProvided() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(MerchantBusinessResponseWrapper.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                authUserService.getMerchantBusinessByMerchantId(TOKEN, MERCHANT_ID));
        assertEquals("Failed to get approved business info from auth service. userId: " + MERCHANT_ID,
                kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnBusinessMerchantResponseDto_When_ValidTokenAndBankIdAreProvided() {
        ResponseEntity<MerchantBusinessResponseWrapper> merchantBusinessResponseWrapperResponse =
                getSampleResponse();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(MerchantBusinessResponseWrapper.class))).thenReturn(merchantBusinessResponseWrapperResponse);
        assertEquals(businessMerchantResponseDto.getMerchantId(),
                authUserService.getBankBusinessByBankId(TOKEN, BANK_ID).getMerchantId());
        assertEquals(businessMerchantResponseDto.getBusinessName(),
                authUserService.getBankBusinessByBankId(TOKEN, BANK_ID).getBusinessName());
    }

    @Test
    void Should_ThrowInvalidUserException_When_InvalidBankIdIsProvided() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(MerchantBusinessResponseWrapper.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        InvalidUserException invalidUserException = assertThrows(InvalidUserException.class, () ->
                authUserService.getBankBusinessByBankId(TOKEN, BANK_ID));
        assertEquals("Failed to get approved business info from auth service. userId: " + BANK_ID,
                invalidUserException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_InvalidBankIdIsProvided() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(MerchantBusinessResponseWrapper.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                authUserService.getBankBusinessByBankId(TOKEN, BANK_ID));
        assertEquals("Failed to get approved business info from auth service. userId: " + BANK_ID,
                kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnMerchantMap_When_ProvidedMerchantAsUserTypeForBulkUserResponse() {
        ResponseEntity<BulkMerchantResponseWrapper> bulkMerchantResponseWrapperResponseEntity = getSampleBulkMerchantResponseEntity();
        Map<String, MerchantBusinessResponseDto> merchantBusinessResponseDtoMap = getSampleMerchantBusinessResponseDtoMap();
        BulkUserRequestDto userIds = getSampleBulkUserRequestDto();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(BulkMerchantResponseWrapper.class))).thenReturn(bulkMerchantResponseWrapperResponseEntity);
        assertEquals(merchantBusinessResponseDtoMap.get(MERCHANT_ID).getName(),
                authUserService.getMerchantMap(userIds, UserType.MERCHANT).get(MERCHANT_ID).getName());
    }

    @Test
    void Should_ReturnMerchantMap_When_ProvidedBankAsUserTypeForBulkUserResponse() {
        ResponseEntity<BulkMerchantResponseWrapper> bulkMerchantResponseWrapperResponseEntity = getSampleBulkMerchantResponseEntity();
        Map<String, MerchantBusinessResponseDto> merchantBusinessResponseDtoMap = getSampleMerchantBusinessResponseDtoMap();
        BulkUserRequestDto userIds = getSampleBulkUserRequestDto();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(BulkMerchantResponseWrapper.class))).thenReturn(bulkMerchantResponseWrapperResponseEntity);
        assertEquals(merchantBusinessResponseDtoMap.get(MERCHANT_ID).getName(),
                authUserService.getMerchantMap(userIds, UserType.BANK).get(MERCHANT_ID).getName());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingDateFromAuthServiceIsFailed() {
        BulkUserRequestDto userIds = getSampleBulkUserRequestDto();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(BulkMerchantResponseWrapper.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                authUserService.getMerchantMap(userIds, UserType.MERCHANT));
        assertEquals("Getting user by id was failed from auth service", kupiCoreException.getMessage());
    }

    /**
     * Get user by userId
     */
    @Test
    void Should_ReturnAuthUser_When_GettingUserByUserIdIsSuccessful() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(AuthResponseWrapper.class))).thenReturn(getSampleAuthResponseWrapperResponseEntity());
        assertEquals(FULL_NAME, authUserService.getUserByUserId(USER_ID, TOKEN).getFullName());
    }

    @Test
    void Should_ThrowInvalidUserException_When_GettingUserByUserIdForInvalidUserId() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(AuthResponseWrapper.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        InvalidUserException exception = assertThrows(InvalidUserException.class, () ->
                authUserService.getUserByUserId(USER_ID, TOKEN));
        assertEquals("Invalid userId: " + USER_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingUserByUserIdIsFailed() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(AuthResponseWrapper.class))).thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        KupiCoreException exception = assertThrows(KupiCoreException.class, () ->
                authUserService.getUserByUserId(USER_ID, TOKEN));
        assertEquals("Requesting user data from auth service was failed.", exception.getMessage());
    }

    /**
     * Get merchant map for deals
     */
    @Test
    void Should_ReturnMerchantBusinessResponseDtoMap_When_GettingMerchantMapForDealsForValidDealPage() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(BulkMerchantResponseWrapper.class))).thenReturn(getSampleBulkMerchantResponseEntity());
        assertEquals(MERCHANT_ID, authUserService.getMerchantMapForDeals(getSampleDealPage(), UserType.MERCHANT)
                .get(MERCHANT_ID).getId());
    }

    @Test
    void Should_ReturnEmptyHashMap_When_GettingMerchantMapForDealsForEmptyDealPage() {
        assertEquals(new HashMap<>(), authUserService.getMerchantMapForDeals(new PageImpl<>(new ArrayList<>()),
                UserType.MERCHANT));
    }

    /**
     * Get today summary response
     */
    @Test
    void Should_ReturnAuthSummaryResponseDto_When_GettingTodaySummaryResponseIsSuccessful() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(AuthSummaryResponseWrapper.class))).thenReturn(getSampleAuthSummaryResponseWrapperResponseEntity());
        assertEquals(ANY_LONG, authUserService.getTodaySummaryResponse(USER_ID, TOKEN, UserType.MERCHANT)
                .getNoOfNewMerchants());
        assertEquals(ANY_LONG, authUserService.getTodaySummaryResponse(USER_ID, TOKEN, UserType.MERCHANT)
                .getTotalMerchants());
        assertEquals(ANY_LONG, authUserService.getTodaySummaryResponse(USER_ID, TOKEN, UserType.MERCHANT)
                .getTotalActiveMerchants());
        assertEquals(ANY_LONG, authUserService.getTodaySummaryResponse(USER_ID, TOKEN, UserType.MERCHANT)
                .getNoOfNewMobileUsers());
        assertEquals(ANY_LONG, authUserService.getTodaySummaryResponse(USER_ID, TOKEN, UserType.MERCHANT)
                .getTotalMobileUsers());
        assertEquals(ANY_LONG, authUserService.getTodaySummaryResponse(USER_ID, TOKEN, UserType.MERCHANT)
                .getNoOfNewBanks());
        assertEquals(ANY_LONG, authUserService.getTodaySummaryResponse(USER_ID, TOKEN, UserType.MERCHANT)
                .getTotalBanks());
        assertEquals(ANY_LONG, authUserService.getTodaySummaryResponse(USER_ID, TOKEN, UserType.MERCHANT)
                .getTotalActiveBanks());
    }

    @Test
    void Should_ThrowInvalidUserException_When_GettingTodaySummaryResponseForInvalidUserIdOrUserType() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(AuthSummaryResponseWrapper.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        InvalidUserException exception = assertThrows(InvalidUserException.class, () -> authUserService
                .getTodaySummaryResponse(USER_ID, TOKEN, UserType.MERCHANT));
        assertEquals("Invalid userId: " + USER_ID + " or Invalid userType: " + UserType.MERCHANT,
                exception.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingTodaySummaryResponseIsFailed() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(AuthSummaryResponseWrapper.class))).thenThrow(new HttpClientErrorException(HttpStatus
                .INTERNAL_SERVER_ERROR));
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> authUserService
                .getTodaySummaryResponse(USER_ID, TOKEN, UserType.MERCHANT));
        assertEquals("Failed to get today's summary from auth service. ", exception.getMessage());
    }

    /**
     * This method creates the sample response for merchant business response wrapper.
     *
     * @return MerchantBusinessResponseWrapper Response
     */
    private ResponseEntity<MerchantBusinessResponseWrapper> getSampleResponse() {
        BusinessMerchantResponseDto businessMerchantResponseDto = new BusinessMerchantResponseDto();
        businessMerchantResponseDto.setMerchantId(MERCHANT_ID);
        businessMerchantResponseDto.setBusinessId("bisid-a8df965a-84e2-435b-8bb2-2473e2729242");
        businessMerchantResponseDto.setBusinessName("madusanka business");
        businessMerchantResponseDto.setApprovalStatus(ApprovalStatus.APPROVED);
        businessMerchantResponseDto.setProfileType("MERCHANT");
        businessMerchantResponseDto.setActive(true);
        businessMerchantResponseDto.setImageUrl("https://objects-kupi-dev." +
                "s3.ap-southeast-1.amazonaws.com/fid-76b07696-" +
                "feac-49c3-a8f8-723db8f26c32.jpg");
        MerchantBusinessResponseWrapper merchantBusinessResponseWrapper = new MerchantBusinessResponseWrapper();
        merchantBusinessResponseWrapper.setStatus(ResponseStatusType.SUCCESS);
        merchantBusinessResponseWrapper.setMessage("Successfully returned the business profile.");
        merchantBusinessResponseWrapper.setData(businessMerchantResponseDto);
        merchantBusinessResponseWrapper.setDisplayMessage("Successfully returned the business profile.");
        return new ResponseEntity<>(merchantBusinessResponseWrapper, HttpStatus.OK);
    }

    /**
     * This method creates the sample business merchant response dto
     *
     * @return businessMerchantResponseDto
     */
    private BusinessMerchantResponseDto getSampleBusinessMerchantResponseDto() {
        BusinessMerchantResponseDto businessMerchantResponseDto = new BusinessMerchantResponseDto();
        businessMerchantResponseDto.setMerchantId(MERCHANT_ID);
        businessMerchantResponseDto.setBusinessId("bisid-a8df965a-84e2-435b-8bb2-2473e2729242");
        businessMerchantResponseDto.setBusinessName("madusanka business");
        businessMerchantResponseDto.setApprovalStatus(ApprovalStatus.APPROVED);
        businessMerchantResponseDto.setProfileType("MERCHANT");
        businessMerchantResponseDto.setActive(true);
        businessMerchantResponseDto.setImageUrl("https://objects-kupi-dev." +
                "s3.ap-southeast-1.amazonaws.com/fid-76b07696-" +
                "feac-49c3-a8f8-723db8f26c32.jpg");
        MerchantBusinessResponseWrapper merchantBusinessResponseWrapper = new MerchantBusinessResponseWrapper();
        merchantBusinessResponseWrapper.setStatus(ResponseStatusType.SUCCESS);
        merchantBusinessResponseWrapper.setMessage("Successfully returned the business profile.");
        merchantBusinessResponseWrapper.setData(businessMerchantResponseDto);
        merchantBusinessResponseWrapper.setDisplayMessage("Successfully returned the business profile.");
        return businessMerchantResponseDto;
    }

    /**
     * This method creates sample MerchantBusinessResponseDto
     *
     * @return MerchantBusinessResponseDto
     */
    private MerchantBusinessResponseDto getSampleMerchantBusinessResponseDto() {
        MerchantBusinessResponseDto merchantBusinessResponseDto = new MerchantBusinessResponseDto();
        merchantBusinessResponseDto.setId(MERCHANT_ID);
        merchantBusinessResponseDto.setName("madusanka business");
        merchantBusinessResponseDto.setApprovalStatus(ApprovalStatus.APPROVED);
        merchantBusinessResponseDto.setUserType("MERCHANT");
        merchantBusinessResponseDto.setActive(true);
        merchantBusinessResponseDto.setImageUrl("https://objects-kupi-dev." +
                "s3.ap-southeast-1.amazonaws.com/fid-76b07696-" +
                "feac-49c3-a8f8-723db8f26c32.jpg");
        return merchantBusinessResponseDto;
    }

    /**
     * This method creates sample BulkMerchantBusinessResponseDto
     *
     * @return BulkMerchantBusinessResponseDto
     */
    private BulkMerchantBusinessResponseDto getSampleBulkMerchantBusinessResponseDto() {
        MerchantBusinessResponseDto merchantBusinessResponseDto = getSampleMerchantBusinessResponseDto();
        BulkMerchantBusinessResponseDto bulkMerchantBusinessResponseDto = new BulkMerchantBusinessResponseDto();
        List<MerchantBusinessResponseDto> merchantBusinessResponseDtoList = new ArrayList<>();
        merchantBusinessResponseDtoList.add(merchantBusinessResponseDto);
        bulkMerchantBusinessResponseDto.setMerchants(merchantBusinessResponseDtoList);
        return bulkMerchantBusinessResponseDto;
    }

    /**
     * This method creates sample bulk user request dto.
     *
     * @return BulkUserRequestDto
     */
    private BulkUserRequestDto getSampleBulkUserRequestDto() {
        BulkUserRequestDto bulkUserRequestDto = new BulkUserRequestDto();
        List<String> userIds = new ArrayList<>();
        userIds.add(USER_ID);
        bulkUserRequestDto.setUserIds(userIds);
        return bulkUserRequestDto;
    }

    /**
     * This method creates sample bulk merchant response entity
     *
     * @return ResponseEntity<BulkMerchantResponseWrapper>
     */
    private ResponseEntity<BulkMerchantResponseWrapper> getSampleBulkMerchantResponseEntity() {
        BulkMerchantBusinessResponseDto bulkMerchantBusinessResponseDto = getSampleBulkMerchantBusinessResponseDto();
        BulkMerchantResponseWrapper bulkMerchantResponseWrapper = new BulkMerchantResponseWrapper();
        bulkMerchantResponseWrapper.setData(bulkMerchantBusinessResponseDto);
        return new ResponseEntity<>(bulkMerchantResponseWrapper, HttpStatus.OK);
    }

    /**
     * This method creates sample merchant business response map
     *
     * @return MerchantBusinessResponseDto map
     */
    private Map<String, MerchantBusinessResponseDto> getSampleMerchantBusinessResponseDtoMap() {
        MerchantBusinessResponseDto merchantBusinessResponseDto = getSampleMerchantBusinessResponseDto();
        Map<String, MerchantBusinessResponseDto> merchantBusinessResponseDtoMap = new HashMap<>();
        merchantBusinessResponseDtoMap.put(merchantBusinessResponseDto.getId(), merchantBusinessResponseDto);
        return merchantBusinessResponseDtoMap;
    }

    private ResponseEntity<AuthResponseWrapper> getSampleAuthResponseWrapperResponseEntity() {
        AuthResponseWrapper authResponseWrapper = new AuthResponseWrapper();
        authResponseWrapper.setData(getSampleAuthUser());
        return new ResponseEntity<>(authResponseWrapper, HttpStatus.OK);
    }

    private AuthUser getSampleAuthUser() {
        AuthUser authUser = new AuthUser();
        authUser.setFullName(FULL_NAME);
        return authUser;
    }

    private Page<Deal> getSampleDealPage() {
        Deal deal = new Deal();
        deal.setMerchantId(MERCHANT_ID);
        List<Deal> dealList = new ArrayList<>();
        dealList.add(deal);
        return new PageImpl<>(dealList);
    }

    private ResponseEntity<AuthSummaryResponseWrapper> getSampleAuthSummaryResponseWrapperResponseEntity() {
        AuthSummaryResponseWrapper authSummaryResponseWrapper = new AuthSummaryResponseWrapper();
        authSummaryResponseWrapper.setData(getSampleAuthSummaryResponseDto());
        return new ResponseEntity<>(authSummaryResponseWrapper, HttpStatus.OK);
    }

    private AuthSummaryResponseDto getSampleAuthSummaryResponseDto() {
        return new AuthSummaryResponseDto(ANY_LONG, ANY_LONG, ANY_LONG, ANY_LONG, ANY_LONG, ANY_LONG, ANY_LONG,
                ANY_LONG);
    }
}