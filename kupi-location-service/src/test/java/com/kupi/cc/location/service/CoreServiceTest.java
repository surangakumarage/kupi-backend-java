package com.kupi.cc.location.service;

import com.kupi.cc.location.domain.entity.GeoLocation;
import com.kupi.cc.location.domain.request.PushNotificationRequestDto;
import com.kupi.cc.location.domain.response.BulkMerchantDealsResponseDto;
import com.kupi.cc.location.domain.response.MerchantDealsResponseDto;
import com.kupi.cc.location.enums.ResponseStatusType;
import com.kupi.cc.location.enums.UserType;
import com.kupi.cc.location.wrapper.BulkDealResponseWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests {@link CoreService} class
 */
class CoreServiceTest {

    private static final String BASE_URL = "http://localhost:8084/kupi-core-service";
    private static final String GET_BULK_DEAL_URL = "/api/v1/deals/bulk-info";
    private static final String GET_DEALS_BY_IDS_URL = "/api/v1/deals/by-ids";
    private static final String GET_BULK_CATEGORIES_URL = "/api/v1/MERCHANT/bulk-categories";
    private static final String GET_RECENTLY_EXPIRE_DEALS_URL = "/api/v1/deals/recently-expire";
    private static final String GET_DEALS_OF_THE_DAY_URL = "/api/v1/deals/deals-of-the-day";
    private static final String GET_MERCHANT_SIZE = "20";
    private static final String GET_APP_KEY = "changeme-spring-application-app-key";
    private static final String USER_ID = "uid-c2283ade-ec71-4a56-891a-c5ebc099ad73";
    private static final String GEO_CODE = "6JRXWV94+23M";

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private BranchService branchService;
    @Mock
    private NotificationService notificationService;
    private CoreService coreService;
    @Mock
    private AuthUserService authUserService;

    private final double searchRadiusKm = 2.0;

    @BeforeEach
    void setUp() {
        initMocks(this);
        coreService = new CoreService(BASE_URL, GET_BULK_DEAL_URL, GET_DEALS_BY_IDS_URL, GET_BULK_CATEGORIES_URL,
                GET_RECENTLY_EXPIRE_DEALS_URL, GET_DEALS_OF_THE_DAY_URL, GET_MERCHANT_SIZE,
                GET_APP_KEY, restTemplate, branchService, notificationService, authUserService,
                searchRadiusKm, searchRadiusKm);
    }


    //    @Test
    void Should_GetMerchantIdList() {
        BulkDealResponseWrapper bulkDealResponseWrapper = getSampleBulkDealResponseWrapper();
        Pageable pageable = PageRequest.of(0, Integer.parseInt(GET_MERCHANT_SIZE));
        GeoLocation geoLocation = getSampleGeoLocation();
        List<GeoLocation> geoLocationList = new ArrayList<>();
        geoLocationList.add(geoLocation);
        Page<GeoLocation> geoLocationPage = new PageImpl<>(geoLocationList);
        when(branchService.getMerchantIdListByGeoCode(pageable, GEO_CODE)).thenReturn(geoLocationPage);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(BulkDealResponseWrapper.class)))
                .thenReturn(ResponseEntity.of(Optional.of(bulkDealResponseWrapper)));
        coreService.getMerchantIdList(anyDouble(), anyDouble(), USER_ID);
        verify(notificationService).sendNotification(any(PushNotificationRequestDto.class));
    }

    //    @Test
    void Should_ThrowCoreServiceHttpClientErrorException_When_GetBulkDealsFromCoreServiceIsFailed() {
        Pageable pageable = PageRequest.of(0, Integer.parseInt(GET_MERCHANT_SIZE));
        GeoLocation geoLocation = getSampleGeoLocation();
        List<GeoLocation> geoLocationList = new ArrayList<>();
        geoLocationList.add(geoLocation);
        Page<GeoLocation> geoLocationPage = new PageImpl<>(geoLocationList);
        when(branchService.getMerchantIdListByGeoCode(pageable, GEO_CODE)).thenReturn(geoLocationPage);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(BulkDealResponseWrapper.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * This method creates sample geolocation
     *
     * @return GeoLocation
     */
    private GeoLocation getSampleGeoLocation() {
        GeoLocation geoLocation = new GeoLocation();
        geoLocation.setUserId(USER_ID);
        geoLocation.setGlobalCode(GEO_CODE);
        geoLocation.setUserType(UserType.MERCHANT);
        return geoLocation;
    }

    /**
     * This method creates sample bulk deal ersponse wrapper
     *
     * @return BulkDealResponseWrapper
     */
    private BulkDealResponseWrapper getSampleBulkDealResponseWrapper() {
        List<MerchantDealsResponseDto> merchantDealsResponseDtoList = new ArrayList<>();
        BulkDealResponseWrapper bulkDealResponseWrapper = new BulkDealResponseWrapper();
        bulkDealResponseWrapper.setMessage("Successfully returned deals by merchant id(s).");
        bulkDealResponseWrapper.setStatus(ResponseStatusType.SUCCESS);
        bulkDealResponseWrapper.setDisplayMessage("Successfully returned deals by merchant id(s).");
        BulkMerchantDealsResponseDto bulkMerchantDealsResponseDto = new BulkMerchantDealsResponseDto();
        bulkMerchantDealsResponseDto.setDealGroups(merchantDealsResponseDtoList);
        bulkDealResponseWrapper.setData(bulkMerchantDealsResponseDto);
        return bulkDealResponseWrapper;
    }
}