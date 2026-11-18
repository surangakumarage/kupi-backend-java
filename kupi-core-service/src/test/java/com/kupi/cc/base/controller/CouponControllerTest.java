package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.Coupon;
import com.kupi.cc.base.domain.entity.CouponStatistics;
import com.kupi.cc.base.domain.entity.Deal;
import com.kupi.cc.base.domain.entity.DealSearchIndex;
import com.kupi.cc.base.domain.request.BulkUserRequestDto;
import com.kupi.cc.base.enums.*;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests {@link CouponController} class
 */
class CouponControllerTest {
    private static final int DEAL_GAPS = 7;
    private static final int PAGE_NO = 0;
    private static final int SIZE = 1;
    private static final String COUPON_STATISTICS_ID = "cousid-1254-8545-7521";
    private static final String MERCHANT_ID = "uid-0017-8954";
    private static final String USER_ID = "uid-bb801b6a-ae7c-4f81-8876-a722f644fa11";
    private static final String DEAL_ID = "did-02104ca3-41e4-4cb2-8a06-8ae0805ef6dc";
    private static final String COUPON_ID = "couid-02104ca3-41e4-4cb2-8a06-8ae0805ef6dc";
    private static final String SEARCH_TERM = "SEARCH";
    private static final String GET_COUPON_STATISTICS_LIST_URI = "/api/v1/coupons/coupon-stat/{page}/{size}";
    private static final String GET_DEALS_WITH_COUPON_URI = "/api/v1/coupons/{userType}/{userId}/{page}/{size}/search/{searchTerm}/filter/{filter}";
    private static final String GET_COUPON_LIST_BY_DEAL_ID_URI = "/api/v1/coupons/deal/{dealId}/{page}/{size}/search/{searchTerm}";
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String USER_ID_HEADER = "User-Id";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String DEFAULT_SORT = "updatedAt";

    @Mock
    private Translator translator;
    @Mock
    private CouponService couponService;
    @Mock
    private DealService dealService;
    @Mock
    private AuthUserService authUserService;
    @Mock
    private CouponStatisticDetailService couponStatisticDetailService;
    @Mock
    private DealSearchService dealSearchService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        initMocks(this);
        CouponController couponController = new CouponController(translator, couponService, dealService,
                authUserService, DEAL_GAPS, couponStatisticDetailService,dealSearchService);
        org.springframework.validation.Validator noOpValidator = new org.springframework.validation.Validator() {
            @Override
            public boolean supports(Class<?> clazz) {
                return true;
            }

            @Override
            public void validate(Object target, Errors errors) {
                // No-op validator
            }
        };
        mockMvc = MockMvcBuilders.standaloneSetup(couponController)
                .setValidator(noOpValidator)
                .build();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnOk_When_GetCouponStatisticListSuccessfully() throws Exception {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE);
        CouponStatistics couponStatistics = getSampleCouponStatistics();
        List<CouponStatistics> couponStatisticsList = new ArrayList<>();
        couponStatisticsList.add(couponStatistics);
        Page<CouponStatistics> couponStatisticsPage = new PageImpl<>(couponStatisticsList, pageable, 1);
        when(couponService.listCoupons(pageable)).thenReturn(couponStatisticsPage);
        String url = GET_COUPON_STATISTICS_LIST_URI.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE));
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.SUCCESSFULLY_RETURNED_COUPON_STAT.getMessage()))
                .andExpect(jsonPath("$.data.couponStatistics[0].id").value(COUPON_STATISTICS_ID))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType
                        .SUCCESSFULLY_RETURNED_COUPON_STAT.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetCouponStatisticsListIsFailed() throws Exception {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE);
        doThrow(new KupiCoreException("ERROR")).when(couponService).listCoupons(pageable);
        String url = GET_COUPON_STATISTICS_LIST_URI.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE));
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    void Should_ReturnOk_When_GetDealsWithCouponListSuccessfully() throws Exception {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(DEFAULT_SORT).descending());
        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
        when(couponService.listDealsWithCoupons(USER_ID, SEARCH_TERM, pageable, CouponFilter.ALL))
                .thenReturn(dealSearchIndexPage);
        String url = GET_DEALS_WITH_COUPON_URI.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE)).replace("{userType}", UserType.MERCHANT.name())
                .replace("{userId}", USER_ID).replace("{searchTerm}", SEARCH_TERM)
                .replace("{filter}", CouponFilter.ALL.name());
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_DEAL_LIST.getMessage()))
                .andExpect(jsonPath("$.data.deals[0].id").value(DEAL_ID))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_DEAL_LIST.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetDealsWithCouponListIsFailed() throws Exception {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(DEFAULT_SORT).descending());
        doThrow(new KupiCoreException("ERROR")).when(couponService).listDealsWithCoupons(USER_ID, SEARCH_TERM, pageable, CouponFilter.ALL);
        String url = GET_DEALS_WITH_COUPON_URI.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE)).replace("{userType}", UserType.MERCHANT.name())
                .replace("{userId}", USER_ID).replace("{searchTerm}", SEARCH_TERM)
                .replace("{filter}", CouponFilter.ALL.name());
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

//    @Test
    void Should_ReturnOk_When_GetCouponListByDealIdIsSuccessfully() throws Exception {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE);
        Page<Coupon> couponPage = getSampleCouponPage();
        when(couponService.getCouponsByDealId(pageable, DEAL_ID, SEARCH_TERM)).thenReturn(couponPage);
        when(authUserService.getUserMap(anyString(), any(BulkUserRequestDto.class))).thenReturn(new HashMap<>());
        String url = GET_COUPON_LIST_BY_DEAL_ID_URI.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE)).replace("{dealId}", DEAL_ID)
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.SUCCESSFULLY_RETURNED_COUPONS.getMessage()))
                .andExpect(jsonPath("$.data.coupons[0].id").value(COUPON_ID))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.SUCCESSFULLY_RETURNED_COUPONS.getCode()));
    }

//    @Test
    void Should_ReturnInternalServerError_When_GetCouponListByDealIdIsFailed() throws Exception {
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE);
        doThrow(new KupiCoreException("ERROR")).when(couponService).getCouponsByDealId(pageable, DEAL_ID, SEARCH_TERM);
        String url = GET_COUPON_LIST_BY_DEAL_ID_URI.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE)).replace("{dealId}", DEAL_ID)
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    /**
     * This method creates sample coupon statistics
     *
     * @return CouponStatistics
     */
    private CouponStatistics getSampleCouponStatistics() {
        Deal deal = new Deal();
        deal.setCouponActive(true);
        CouponStatistics couponStatistics = new CouponStatistics();
        couponStatistics.setId(COUPON_STATISTICS_ID);
        couponStatistics.setMerchantId(MERCHANT_ID);
        couponStatistics.setRedeemedCoupons(5);
        couponStatistics.setTotalCoupons(10);
        couponStatistics.setRemainingCoupons(5);
        couponStatistics.setDeal(deal);
        return couponStatistics;
    }

    /**
     * This method creates sample DealSearchIndex page
     *
     * @return dealSearchIndexPage
     */
    private Page<DealSearchIndex> getSampleDealSearchIndexPage() {
        DealSearchIndex dealSearchIndex = new DealSearchIndex();
        dealSearchIndex.setId(DEAL_ID);
        dealSearchIndex.setApprovalStatus(ApprovalStatus.APPROVED);
        dealSearchIndex.setDeleted(false);
        dealSearchIndex.setTitle("Bank deal to be approved");
        dealSearchIndex.setActiveMerchant(true);
        dealSearchIndex.setBrandNames("Brand");
        dealSearchIndex.setDealSource(UserType.MERCHANT);
        dealSearchIndex.setCategoryNames("category");
        dealSearchIndex.setSearchImageUrls("[https://objects-kupi-dev.s3.ap-southeast-1.amazonaws.com/fid-a61e4565-dd03-4bb1-a123-90460ca3def5.png]");
        dealSearchIndex.setSubTitle("bank deal");
        dealSearchIndex.setExpiredOn(System.currentTimeMillis() + 10000);
        dealSearchIndex.setTermsAndConditions("no conditions ");
        List<DealSearchIndex> dealSearchIndexList = new ArrayList<>();
        dealSearchIndexList.add(dealSearchIndex);
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(DEFAULT_SORT).descending());
        return new PageImpl<>(dealSearchIndexList, pageable, 1);
    }

    /**
     * This method creates sample coupon page
     *
     * @return CouponPage
     */
    private Page<Coupon> getSampleCouponPage() {
        Deal deal = new Deal();
        deal.setId(DEAL_ID);
        deal.setTitle("TITLE");
        Coupon coupon = new Coupon();
        coupon.setId(COUPON_ID);
        coupon.setCouponCode("asdcf2");
        coupon.setDeal(deal);
        coupon.setCouponActive(true);
        coupon.setRedeemed(true);
        coupon.setRedeemedOn(System.currentTimeMillis() + 1000);
        coupon.setExpiryDate(System.currentTimeMillis() + 10000);
        coupon.setCouponPurchaseStatus(CouponPurchaseStatus.PURCHASED);
        coupon.setPrice(1.00);
        List<Coupon> couponList = new ArrayList<>();
        couponList.add(coupon);
        return new PageImpl<>(couponList);
    }
}