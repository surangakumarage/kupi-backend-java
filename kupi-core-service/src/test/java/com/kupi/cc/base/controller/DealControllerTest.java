package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.*;
import com.kupi.cc.base.domain.request.BulkUserRequestDto;
import com.kupi.cc.base.domain.request.DealApprovalStatusUpdateRequestDto;
import com.kupi.cc.base.domain.request.DealRequestDto;
import com.kupi.cc.base.domain.request.DealUpdateRequestDto;
import com.kupi.cc.base.domain.response.BusinessMerchantResponseDto;
import com.kupi.cc.base.domain.response.MerchantBusinessResponseDto;
import com.kupi.cc.base.enums.*;
import com.kupi.cc.base.exception.*;
import com.kupi.cc.base.service.*;
import com.kupi.cc.base.repository.CouponRepository;
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

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests the {@link DealController} class.
 */
class DealControllerTest {
    private static final String CREATE_DEAL_URI = "/api/v1/deals/{userType}";
    private static final String GET_DEAL_DETAIL_BY_ID_URI = "/api/v1/deals/{userType}/{dealId}";
    private static final String GET_BASIC_DEAL_LIST_URI = "/api/v1/deals/{page}/{size}/search/ALL";
    private static final String GET_BASIC_SEARCH_DEAL_LIST_URI = "/api/v1/deals/{page}/{size}/{userType}/{merchantId}/search/{searchTerm}";
    private static final String GET_DEAL_LIST_FOR_MERCHANT_URI = "/api/v1/deals/{page}/{size}/merchant/{merchantId}";
    private static final String GET_PENDING_DEAL_LIST_URI = "/api/v1/deals/{userType}/PENDING/{page}/{size}/search/{searchTerm}";
    private static final String GET_RECENTLY_EXPIRE_DEAL_URI = "/api/v1/deals/recently-expire/{page}/{size}/category/{categoryId}/{userType}/{merchantId}/search/{searchTerm}";
    private static final String GET_SEARCH_DEAL_BY_CATEGORY_ID_URI = "/api/v1/deals/{page}/{size}/category/{categoryId}/{userType}/{merchantId}/search/{searchTerm}";
    private static final String GET_DEAL_BY_CATEGORY_ID_URI = "/api/v1/deals/{userType}/category/{categoryId}/{page}/{size}/search/{searchTerm}";
    private static final String GET_ACTIVE_DEALS_BY_BANK_MERCHANT_ID_URI = "/api/v1/deals/active/{page}/{size}/bank/{bankId}/merchant/{merchantId}";
    private static final String GET_DEAL_FOR_BULK_USER_URL = "/api/v1/deals/bulk-info";
    private static final String APPROVE_DEAL_URL = "/api/v1/deals/{userType}/approve";
    private static final String GET_DEAL_BY_BRAND_ID_URI = "/api/v1/deals/{userType}/brand/{brandId}/{page}/{size}/search/{searchTerm}";
    private static final String UPDATE_DEAL_URI = "/api/v1/deals/{userType}";
    private static final String DELETE_DEAL_URL = "/api/v1/deals/{userType}/{dealId}";
    private static final String GET_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT_URI = "/api/v1/deals/active/{page}/{size}/category/{categoryId}/{userType}/active/{merchantId}/search/{searchTerm}";
    private static final String GET_RECENTLY_EXPIRE_DEALS_FOR_MOBILE_URI = "/api/v1/deals/recently-expire/{page}/{size}/category/{categoryId}/{userType}/active/{merchantId}/search/{searchTerm}";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String ACCESS_TOKEN = "Bearer192f0c96-dd19-404e-8217-a8fd1cc81d55";
    private static final String MERCHANT_ID = "uid-001";
    private static final String CATEGORY_ID = "cid-1235-1458-1785";
    private static final String SHOP_ID = "sid-12589-9856-78514-8569";
    private static final String CATEGORY_NAME = "New Year";
    private static final String CATEGORY_DESCRIPTION = "snacks and sweets";
    private static final String CATEGORY_IMAGE_URL = "https://picsum.photos/200/300";
    private static final long INVALID_TIME_STAMP = 1234556;
    private static final double INVALID_ORIGINAL_PRICE = -250.00;
    private static final double ORIGINAL_PRICE = 250.00;
    private static final double INVALID_DEDUCTION_AMOUNT = -250.00;
    private static final double INVALID_DEDUCTION_PERCENTAGE = 110.00;
    private static final String USER_ID = "uid-bb801b6a-ae7c-4f81-8876-a722f644fa11";
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String DEAL_ID = "uid-001";
    private static final int SIZE = 1;
    private static final int PAGE = 0;
    private static final int TOTAL_ITEMS = 1;
    private static final int TOTAL_PAGE = 1;
    private static final String DEFAULT_SORT = "updated_at";
    private static final String SEARCH_TERM = "SEARCH";
    private static final String ALL = "ALL";
    private static final String BANK_ID = "uid-04af1815-988b-4fc7-b00b-14ee57a788df";
    private static final String BRAND_ID = "did-02a2022f-98c5-4237-a9c7-c1faf989a4a5";

    @Mock
    private DealService dealService;
    @Mock
    private BrandService brandService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private AuthUserService authUserService;
    @Mock
    private Translator translator;
    @Mock
    private DealCodeService dealCodeService;
    private MockMvc mockMvc;
    @Mock
    private SubscriberService subscriberService;
    @Mock
    private CouponStatisticsService couponStatisticsService;
    @Mock
    private PromoTypeService promoTypeService;
    @Mock
    private CouponService couponService;
    @Mock
    private CouponRepository couponRepository;
    @Mock
    private DealViewService dealViewService;
    @Mock
    private DealSearchService dealSearchService;
    @Mock
    private FavouriteDealService favouriteDealService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        DealController dealController = new DealController(dealService, brandService, categoryService,
                authUserService, translator, dealCodeService, subscriberService, couponStatisticsService,
                promoTypeService, couponService, dealViewService, dealSearchService, favouriteDealService);
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
        mockMvc = MockMvcBuilders.standaloneSetup(dealController)
                .setValidator(noOpValidator)
                .build();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnOk_When_CreatingADeal() throws Exception {
        DealRequestDto dealRequestDto = getSampleDealRequestDto();
        dealRequestDto.setShopId(SHOP_ID);
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        businessMerchantResponseDto.setProfileType(UserType.BANK.name());
        when(authUserService.getBankBusinessByBankId(ACCESS_TOKEN, MERCHANT_ID))
                .thenReturn(businessMerchantResponseDto);
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, SHOP_ID))
                .thenReturn(businessMerchantResponseDto);
        String url = CREATE_DEAL_URI.replace("{userType}", UserType.BANK.name());
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.CREATE_DEAL.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.CREATE_DEAL.getCode()))
                .andExpect(jsonPath("$.data.id", startsWith("did-")));
    }

    @Test
    void Should_ReturnBadRequest_When_UserTypeIsNeitherMerchantNorBank() throws Exception {
        DealRequestDto dealRequestDto = getSampleDealRequestDto();
        String url = CREATE_DEAL_URI.replace("{userType}", UserType.ADMIN.name());
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingADealWithoutRequiredFields() throws Exception {
        DealRequestDto dealRequestDto = getSampleDealRequestDto();
        dealRequestDto.setTitle(null);
        String url = CREATE_DEAL_URI.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingADealWithAInvalidStartDate() throws Exception {
        DealRequestDto dealRequestDto = getSampleDealRequestDto();
        dealRequestDto.setValidFrom(INVALID_TIME_STAMP);
        String url = CREATE_DEAL_URI.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_VALID_FROM_DATE.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_VALID_FROM_DATE.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingADealWithAInvalidOriginalPrice() throws Exception {
        DealRequestDto dealRequestDto = getSampleDealRequestDto();
        dealRequestDto.setOriginalPrice(INVALID_ORIGINAL_PRICE);
        String url = CREATE_DEAL_URI.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_PRICE.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_PRICE.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingADealWithAInvalidExpireDate() throws Exception {
        DealRequestDto dealRequestDto = getSampleDealRequestDto();
        dealRequestDto.setExpiredOn(INVALID_TIME_STAMP);
        String url = CREATE_DEAL_URI.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_EXPIRED_ON_DATE.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_EXPIRED_ON_DATE.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingADealWithDeductionTypeIsAmountAndInvalidDeductionAmount()
            throws Exception {
        DealRequestDto dealRequestDto = getSampleDealRequestDto();
        dealRequestDto.setDeductionType(DeductionType.AMOUNT);
        dealRequestDto.setOriginalPrice(null);
        dealRequestDto.setDeductionAmount(INVALID_DEDUCTION_AMOUNT);
        String url = CREATE_DEAL_URI.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_PRICE.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_PRICE.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingADealWithDeductionTypeIsPercentageAndInvalidOriginalPrice()
            throws Exception {
        DealRequestDto dealRequestDto = getSampleDealRequestDto();
        dealRequestDto.setDeductionType(DeductionType.PERCENTAGE);
        dealRequestDto.setOriginalPrice(ORIGINAL_PRICE);
        dealRequestDto.setDeductionPercentage(INVALID_DEDUCTION_PERCENTAGE);
        String url = CREATE_DEAL_URI.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_DEDUCTION_PERCENTAGE.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_DEDUCTION_PERCENTAGE.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_SubscriptionPlanLimitExceeded() throws Exception {
        DealRequestDto dealRequestDto = getSampleDealRequestDto();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID))
                .thenReturn(businessMerchantResponseDto);
        doThrow(new LimitExceededException("ERROR", LimitExceededType.DEALS_PER_MONTH)).when(subscriberService).checkSubscriptionPlanForDeal(MERCHANT_ID);
        String url = CREATE_DEAL_URI.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.DEALS_PER_MONTH_LIMIT_EXCEEDED.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.DEALS_PER_MONTH_LIMIT_EXCEEDED.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_SubscriptionPlanNotFoundForUser() throws Exception {
        DealRequestDto dealRequestDto = getSampleDealRequestDto();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID))
                .thenReturn(businessMerchantResponseDto);
        doThrow(new InvalidSubscribeException("ERROR")).when(subscriberService).checkSubscriptionPlanForDeal(MERCHANT_ID);
        String url = CREATE_DEAL_URI.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.SUBSCRIPTION_PLAN_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.SUBSCRIPTION_PLAN_NOT_FOUND.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingADealWithAInActiveMerchant() throws Exception {
        DealRequestDto dealRequestDto = getSampleDealRequestDto();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        businessMerchantResponseDto.setActive(false);
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID))
                .thenReturn(businessMerchantResponseDto);
        String url = CREATE_DEAL_URI.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INACTIVE_MERCHANT_FOR_CREATE_DEAL.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INACTIVE_MERCHANT_FOR_CREATE_DEAL.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingADealWithInvalidProfile() throws Exception {
        DealRequestDto dealRequestDto = getSampleDealRequestDto();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        businessMerchantResponseDto.setProfileType(UserType.BANK.name());
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID))
                .thenReturn(businessMerchantResponseDto);
        String url = CREATE_DEAL_URI.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INACTIVE_MERCHANT_FOR_CREATE_DEAL.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INACTIVE_MERCHANT_FOR_CREATE_DEAL.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingDealWithInvalidCategoryIds() throws Exception {
        DealRequestDto dealRequestDto = getSampleDealRequestDto();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID))
                .thenReturn(businessMerchantResponseDto);
        doThrow(new InvalidCategoryException("ERROR"))
                .when(categoryService).getCategorySetByIds(dealRequestDto.getCategoryIds());
        String url = CREATE_DEAL_URI.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_CATEGORY_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_CATEGORY_ID.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingDealWithInvalidBrandIds() throws Exception {
        DealRequestDto dealRequestDto = getSampleDealRequestDto();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID))
                .thenReturn(businessMerchantResponseDto);
        doThrow(new InvalidBrandException("ERROR"))
                .when(brandService).getBrandSetByIdList(dealRequestDto.getBrandIds());
        String url = CREATE_DEAL_URI.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_BRAND_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_BRAND_ID.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingDealWithDealStartDateLessThanCategoryStartDate() throws Exception {
        DealRequestDto dealRequestDto = getSampleDealRequestDto();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        Set<Category> categorySet = getSampleCategorySet();
        Category sampleCategory = getSampleCategory();
        categorySet.add(sampleCategory);
        sampleCategory.setStartDate(System.currentTimeMillis() + 5800);
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID))
                .thenReturn(businessMerchantResponseDto);
        when(categoryService.getCategorySetByIds(dealRequestDto.getCategoryIds())).thenReturn(categorySet);
        String url = CREATE_DEAL_URI.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.DEAL_DATES_DOESNT_MATCH_SEASONAL_CATEGORIES.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.DEAL_DATES_DOESNT_MATCH_SEASONAL_CATEGORIES.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingDealWithDealExpireDateGreaterThanCategoryExpireDate() throws Exception {
        DealRequestDto dealRequestDto = getSampleDealRequestDto();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        Set<Category> categorySet = getSampleCategorySet();
        Category sampleCategory = getSampleCategory();
        sampleCategory.setExpiryDate(System.currentTimeMillis() + 700000);
        categorySet.add(sampleCategory);
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID))
                .thenReturn(businessMerchantResponseDto);
        when(categoryService.getCategorySetByIds(dealRequestDto.getCategoryIds())).thenReturn(categorySet);
        String url = CREATE_DEAL_URI.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.DEAL_DATES_DOESNT_MATCH_SEASONAL_CATEGORIES.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.DEAL_DATES_DOESNT_MATCH_SEASONAL_CATEGORIES.getCode()));
    }

    @Test
    void Should_InternalServerError_When_CreatingADeal() throws Exception {
        DealRequestDto dealRequestDto = getSampleDealRequestDto();
        List<String> brandIds = new ArrayList<>();
        brandIds.add("bid-01");
        brandIds.add("bid-02");
        dealRequestDto.setBrandIds(brandIds);
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID))
                .thenReturn(businessMerchantResponseDto);
        when(brandService.getBrandSetByIdList(dealRequestDto.getBrandIds())).thenThrow(new KupiCoreException("ERROR") {
        });
        String url = CREATE_DEAL_URI.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", MERCHANT_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message", is("Internal server error.")))
                .andExpect(jsonPath("$.errorCode", is(5000)));
    }

    @Test
    void Should_ReturnOk_When_UserTypeIsMerchantAndDealIdIsProvided() throws Exception {
        Deal deal = getSampleDeal();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        when(dealService.getDeal(DEAL_ID, UserType.MERCHANT)).thenReturn(deal);
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID))
                .thenReturn(businessMerchantResponseDto);
        String url = GET_DEAL_DETAIL_BY_ID_URI.replace("{userType}", UserType.MERCHANT.name())
                .replace("{dealId}", DEAL_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_DEAL.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_DEAL.getCode()))
                .andExpect(jsonPath("$.data.id", startsWith("did-")));
    }

    @Test
    void Should_ReturnOk_When_UserTypeIsBankAndDealIdIsProvided() throws Exception {
        Deal deal = getSampleDeal();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        when(dealService.getDeal(DEAL_ID, UserType.BANK)).thenReturn(deal);
        when(authUserService.getBankBusinessByBankId(ACCESS_TOKEN, MERCHANT_ID))
                .thenReturn(businessMerchantResponseDto);
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, SHOP_ID))
                .thenReturn(businessMerchantResponseDto);
        String url = GET_DEAL_DETAIL_BY_ID_URI.replace("{userType}", UserType.BANK.name())
                .replace("{dealId}", DEAL_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_DEAL.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_DEAL.getCode()))
                .andExpect(jsonPath("$.data.id", startsWith("did-")));
    }

    @Test
    void Should_ReturnBadRequest_When_UserTypeIsNeitherMerchantNorBankForGetDetailDeal() throws Exception {
        String url = GET_DEAL_DETAIL_BY_ID_URI.replace("{userType}", UserType.ADMIN.name())
                .replace("{dealId}", DEAL_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidDealIdIsProvidedForGetDetailDeal() throws Exception {
        String url = GET_DEAL_DETAIL_BY_ID_URI.replace("{userType}", UserType.MERCHANT.name())
                .replace("{dealId}", DEAL_ID);
        doThrow(new InvalidDealException("ERROR")).when(dealService).getDeal(DEAL_ID, UserType.MERCHANT);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_DEAL_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_DEAL_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetDetailDealIsFailed() throws Exception {
        String url = GET_DEAL_DETAIL_BY_ID_URI.replace("{userType}", UserType.MERCHANT.name())
                .replace("{dealId}", DEAL_ID);
        doThrow(new KupiCoreException("ERROR")).when(dealService).getDeal(DEAL_ID, UserType.MERCHANT);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_DealPageIsNotEmptyForGetBasicDealList() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        String url = GET_BASIC_DEAL_LIST_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE));
        Page<Deal> dealPage = getSampleDealPage();
        var merchantBusinessResponseDtoMap = getSampleMerchantBusinessResponseDtoMap();
        when(dealService.listAllDeals(pageable)).thenReturn(dealPage);
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(merchantBusinessResponseDtoMap);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_DEAL_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_DEAL_LIST.getCode()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.data.deals[0].id", startsWith("did-")));
    }

    @Test
    void Should_ReturnOk_When_DealPageIsEmptyForGetBasicDealList() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        String url = GET_BASIC_DEAL_LIST_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE));
        List<Deal> emptyDeal = new ArrayList<>();
        Page<Deal> dealPage = new PageImpl<>(emptyDeal);
        when(dealService.listAllDeals(pageable)).thenReturn(dealPage);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_DEAL_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_DEAL_LIST.getCode()))
                .andExpect(jsonPath("$.data.totalItems").value(0))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(0));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingBasicDealListIsFailed() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        String url = GET_BASIC_DEAL_LIST_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE));
        doThrow(new KupiCoreException("ERROR")).when(dealService).listAllDeals(pageable);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidFieldsAreProvidedForSearchBasicGetDealList() throws Exception {
        Page<DealSearchIndex> dealPage = getSampleDealSearchIndexPage();
        when(dealService.searchDeals(any(Pageable.class), anyString(), anyString(), any(UserType.class)))
                .thenReturn(dealPage);
        String url = GET_BASIC_SEARCH_DEAL_LIST_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE))
                .replace("{userType}", UserType.BANK.toString())
                .replace("{merchantId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_DEAL_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_DEAL_LIST.getCode()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.data.deals[0].id").value("did-02104ca3-41e4-4cb2-8a06-8ae0805ef6dc"));

    }

    @Test
    void Should_BadRequest_When_UserTypeIsNeitherBankNorMerchantForSearchBasicGetDealList() throws Exception {
        String url = GET_BASIC_SEARCH_DEAL_LIST_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE))
                .replace("{userType}", UserType.ADMIN.toString())
                .replace("{merchantId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_SearchBasicGetDealListIsFailed() throws Exception {
        String url = GET_BASIC_SEARCH_DEAL_LIST_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE))
                .replace("{userType}", UserType.BANK.toString())
                .replace("{merchantId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        doThrow(new KupiCoreException("ERROR")).when(dealService)
                .searchDeals(any(Pageable.class), anyString(), anyString(), any(UserType.class));
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_SearchTermIsEqualToAllForGetPendingDealList() throws Exception {
        String url = GET_PENDING_DEAL_LIST_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE))
                .replace("{userType}", UserType.BANK.toString())
                .replace("{searchTerm}", ALL);
        Page<DealSearchIndex> dealPage = getSampleDealSearchIndexPage();
        dealPage.getContent().get(0).setApprovalStatus(ApprovalStatus.PENDING);
        when(dealService.getPendingDeals(any(Pageable.class), anyString(), any(UserType.class))).thenReturn(dealPage);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_DEAL_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_DEAL_LIST.getCode()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.data.deals[0].id", startsWith("did-")))
                .andExpect(jsonPath("$.data.deals[0].approvalStatus").value(ApprovalStatus.PENDING.getStatus()));
    }

    @Test
    void Should_ReturnOk_When_SearchTermIsNotEqualToAllForGetPendingDealList() throws Exception {
        String url = GET_PENDING_DEAL_LIST_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE))
                .replace("{userType}", UserType.BANK.toString())
                .replace("{searchTerm}", SEARCH_TERM);
        Page<DealSearchIndex> dealPage = getSampleDealSearchIndexPage();
        dealPage.getContent().get(0).setApprovalStatus(ApprovalStatus.PENDING);
        when(dealService.getPendingDeals(any(Pageable.class), anyString(), any(UserType.class))).thenReturn(dealPage);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_DEAL_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_DEAL_LIST.getCode()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.data.deals[0].id", startsWith("did-")))
                .andExpect(jsonPath("$.data.deals[0].approvalStatus").value(ApprovalStatus.PENDING.getStatus()));
    }

    @Test
    void Should_BadRequest_When_UserTypeIsNeitherBankNorMerchantForGetPendingDealList() throws Exception {
        String url = GET_PENDING_DEAL_LIST_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE))
                .replace("{userType}", UserType.ADMIN.toString())
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetPendingDealListIsFailed() throws Exception {
        String url = GET_PENDING_DEAL_LIST_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE))
                .replace("{userType}", UserType.BANK.toString())
                .replace("{searchTerm}", SEARCH_TERM);
        doThrow(new KupiCoreException("ERROR")).when(dealService)
                .getPendingDeals(any(Pageable.class), anyString(), any(UserType.class));
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidMerchantIdForGetDealListForMerchant() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        String url = GET_DEAL_LIST_FOR_MERCHANT_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID);
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        Page<Deal> dealPage = getSampleDealPage();
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID))
                .thenReturn(businessMerchantResponseDto);
        when(dealService.listAllDealsByMerchant(pageable, MERCHANT_ID)).thenReturn(dealPage);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_DEAL_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_DEAL_LIST.getCode()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.data.deals[0].id", startsWith("did-")));

    }

    @Test
    void Should_BadRequest_When_InvalidMerchantIdIsProvidedForGetDealListForMerchant() throws Exception {
        String url = GET_DEAL_LIST_FOR_MERCHANT_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID);
        doThrow(new InvalidUserException("ERROR"))
                .when(authUserService).getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetDealListForMerchantIsFailed() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        String url = GET_DEAL_LIST_FOR_MERCHANT_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID);
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID))
                .thenReturn(businessMerchantResponseDto);
        doThrow(new KupiCoreException("ERROR")).when(dealService).listAllDealsByMerchant(pageable, MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidFieldsAreProvidedForGetRecentlyExpireDeals() throws Exception {
        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
        String url = GET_RECENTLY_EXPIRE_DEAL_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID)
                .replace("{categoryId}", ALL).replace("{searchTerm}", SEARCH_TERM)
                .replace("{userType}", UserType.MERCHANT.name());
        CouponStatistics couponStatistics = getSampleCouponStatistics();
        List<CouponStatistics> couponStatisticsList = new ArrayList<>();
        couponStatisticsList.add(couponStatistics);
        var dealIdList = dealSearchIndexPage.get().map(DealSearchIndex::getId).collect(Collectors.toList());
        when(dealService.getRecentlyExpireDeals(any(Pageable.class), anyString(), anyString(), anyString(), anyString(),
                any(UserType.class))).thenReturn(dealSearchIndexPage);
        when(couponStatisticsService.getCouponStatisticsByDealIdList(dealIdList)).thenReturn(couponStatisticsList);
        when(dealService.removeCouponNotAvailableDeals(any(Page.class), anyList())).thenReturn(dealSearchIndexPage);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_DEAL_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_DEAL_LIST.getCode()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.data.deals[0].id", startsWith("did-")));
    }

    @Test
    void Should_ReturnOk_When_CategoryIdIsNotEqualToAllForGetRecentlyExpireDeals() throws Exception {
        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
        String url = GET_RECENTLY_EXPIRE_DEAL_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID)
                .replace("{categoryId}", CATEGORY_ID).replace("{searchTerm}", SEARCH_TERM)
                .replace("{userType}", UserType.MERCHANT.name());
        CouponStatistics couponStatistics = getSampleCouponStatistics();
        List<CouponStatistics> couponStatisticsList = new ArrayList<>();
        couponStatisticsList.add(couponStatistics);
        var dealIdList = dealSearchIndexPage.get().map(DealSearchIndex::getId).collect(Collectors.toList());
        when(dealService.getRecentlyExpireDeals(any(Pageable.class), anyString(), anyString(), anyString(), anyString(),
                any(UserType.class))).thenReturn(dealSearchIndexPage);
        when(couponStatisticsService.getCouponStatisticsByDealIdList(dealIdList)).thenReturn(couponStatisticsList);
        when(dealService.removeCouponNotAvailableDeals(any(Page.class), anyList())).thenReturn(dealSearchIndexPage);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_DEAL_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_DEAL_LIST.getCode()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.data.deals[0].id", startsWith("did-")));
    }

    @Test
    void Should_BadRequest_When_UserTypeIsNeitherBankNorMerchantForGetRecentlyExpireDeals() throws Exception {
        String url = GET_RECENTLY_EXPIRE_DEAL_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID)
                .replace("{categoryId}", CATEGORY_ID).replace("{searchTerm}", SEARCH_TERM)
                .replace("{userType}", UserType.ADMIN.name());
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidCategoryIdIsProvidedForGetRecentlyExpireDeals() throws Exception {
        String url = GET_RECENTLY_EXPIRE_DEAL_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID)
                .replace("{categoryId}", CATEGORY_ID).replace("{searchTerm}", SEARCH_TERM)
                .replace("{userType}", UserType.MERCHANT.name());
        doThrow(new InvalidCategoryException("ERROR")).when(categoryService).validateCategoryId(CATEGORY_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_CATEGORY_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_CATEGORY_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetRecentlyExpireDealsIsFailed() throws Exception {
        String url = GET_RECENTLY_EXPIRE_DEAL_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID)
                .replace("{categoryId}", CATEGORY_ID).replace("{searchTerm}", SEARCH_TERM)
                .replace("{userType}", UserType.MERCHANT.name());
        doThrow(new KupiCoreException("ERROR")).when(dealService).getRecentlyExpireDeals(any(Pageable.class), anyString(),
                anyString(), anyString(), anyString(), any(UserType.class));
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidFieldsAreProvidedForSearchDeals() throws Exception {
        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
        String url = GET_SEARCH_DEAL_BY_CATEGORY_ID_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID)
                .replace("{categoryId}", ALL).replace("{searchTerm}", SEARCH_TERM)
                .replace("{userType}", UserType.MERCHANT.name());
        CouponStatistics couponStatistics = getSampleCouponStatistics();
        List<CouponStatistics> couponStatisticsList = new ArrayList<>();
        couponStatisticsList.add(couponStatistics);
        var dealIdList = dealSearchIndexPage.get().map(DealSearchIndex::getId).collect(Collectors.toList());
        when(dealService.getAllSearchDeals(any(Pageable.class), anyString(), anyString(), anyString(), anyString(),
                anyBoolean(), anyBoolean(), any(UserType.class))).thenReturn(dealSearchIndexPage);
        when(couponStatisticsService.getCouponStatisticsByDealIdList(dealIdList)).thenReturn(couponStatisticsList);
        when(dealService.removeCouponNotAvailableDeals(any(Page.class), anyList())).thenReturn(dealSearchIndexPage);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_DEAL_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_DEAL_LIST.getCode()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.data.deals[0].id", startsWith("did-")));
    }

    @Test
    void Should_ReturnOk_When_CategoryIdIsNotEqualToAllForSearchDeals() throws Exception {
        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
        String url = GET_SEARCH_DEAL_BY_CATEGORY_ID_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID)
                .replace("{categoryId}", CATEGORY_ID).replace("{searchTerm}", SEARCH_TERM)
                .replace("{userType}", UserType.MERCHANT.name());
        CouponStatistics couponStatistics = getSampleCouponStatistics();
        List<CouponStatistics> couponStatisticsList = new ArrayList<>();
        couponStatisticsList.add(couponStatistics);
        var dealIdList = dealSearchIndexPage.get().map(DealSearchIndex::getId).collect(Collectors.toList());
        when(dealService.getAllSearchDeals(any(Pageable.class), anyString(), anyString(), anyString(), anyString(),
                anyBoolean(), anyBoolean(), any(UserType.class))).thenReturn(dealSearchIndexPage);
        when(couponStatisticsService.getCouponStatisticsByDealIdList(dealIdList)).thenReturn(couponStatisticsList);
        when(dealService.removeCouponNotAvailableDeals(any(Page.class), anyList())).thenReturn(dealSearchIndexPage);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_DEAL_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_DEAL_LIST.getCode()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.data.deals[0].id", startsWith("did-")));
    }

    @Test
    void Should_BadRequest_When_UserTypeIsNeitherBankNorMerchantForSearchDeals() throws Exception {
        String url = GET_SEARCH_DEAL_BY_CATEGORY_ID_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID)
                .replace("{categoryId}", CATEGORY_ID).replace("{searchTerm}", SEARCH_TERM)
                .replace("{userType}", UserType.ADMIN.name());
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidCategoryIdIsProvidedForSearchDeals() throws Exception {
        String url = GET_SEARCH_DEAL_BY_CATEGORY_ID_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID)
                .replace("{categoryId}", CATEGORY_ID).replace("{searchTerm}", SEARCH_TERM)
                .replace("{userType}", UserType.MERCHANT.name());
        doThrow(new InvalidCategoryException("ERROR")).when(categoryService).validateCategoryId(CATEGORY_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_CATEGORY_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_CATEGORY_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_SearchDealsIsFailed() throws Exception {
        String url = GET_SEARCH_DEAL_BY_CATEGORY_ID_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID)
                .replace("{categoryId}", CATEGORY_ID).replace("{searchTerm}", SEARCH_TERM)
                .replace("{userType}", UserType.MERCHANT.name());
        doThrow(new KupiCoreException("ERROR")).when(dealService).getAllSearchDeals(any(Pageable.class), anyString(),
                anyString(), anyString(), anyString(),
                anyBoolean(), anyBoolean(), any(UserType.class));
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidBankAndMerchantIdAreProvidedForGetActiveDealsByBankIdAndMerchantId() throws Exception {
        String url = GET_ACTIVE_DEALS_BY_BANK_MERCHANT_ID_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID)
                .replace("{bankId}", BANK_ID);
        BusinessMerchantResponseDto merchantProfile = getSampleBusinessMerchantResponseDto();
        BusinessMerchantResponseDto bankProfile = getSampleBusinessMerchantResponseDto();
        merchantProfile.setProfileType(UserType.MERCHANT.name());
        merchantProfile.setActive(true);
        bankProfile.setProfileType(UserType.BANK.name());
        bankProfile.setMerchantId(BANK_ID);
        Page<BankDeal> bankDeals = getSampleBankDealPage();
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID))
                .thenReturn(merchantProfile);
        when(authUserService.getBankBusinessByBankId(ACCESS_TOKEN, BANK_ID))
                .thenReturn(bankProfile);
        when(dealService.getActiveDealsForActiveBankAndMerchant(any(Pageable.class), anyString(), anyString()))
                .thenReturn(bankDeals);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_DEAL_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_DEAL_LIST.getCode()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.data.deals[0].id", startsWith("did-")));
    }

    @Test
    void Should_ReturnBadRequest_When_MerchantIsInactiveForGetActiveDealsByBankIdAndMerchantId() throws Exception {
        String url = GET_ACTIVE_DEALS_BY_BANK_MERCHANT_ID_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID)
                .replace("{bankId}", BANK_ID);
        BusinessMerchantResponseDto merchantProfile = getSampleBusinessMerchantResponseDto();
        BusinessMerchantResponseDto bankProfile = getSampleBusinessMerchantResponseDto();
        merchantProfile.setProfileType(UserType.MERCHANT.name());
        merchantProfile.setActive(false);
        bankProfile.setProfileType(UserType.BANK.name());
        bankProfile.setMerchantId(BANK_ID);
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID))
                .thenReturn(merchantProfile);
        when(authUserService.getBankBusinessByBankId(ACCESS_TOKEN, BANK_ID))
                .thenReturn(bankProfile);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INACTIVE_MERCHANT.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INACTIVE_MERCHANT.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidMerchantIdIsProvidedForGetActiveDealsByBankIdAndMerchantId() throws Exception {
        String url = GET_ACTIVE_DEALS_BY_BANK_MERCHANT_ID_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID)
                .replace("{bankId}", BANK_ID);
        BusinessMerchantResponseDto merchantProfile = getSampleBusinessMerchantResponseDto();
        BusinessMerchantResponseDto bankProfile = getSampleBusinessMerchantResponseDto();
        merchantProfile.setProfileType(UserType.MERCHANT.name());
        merchantProfile.setActive(true);
        bankProfile.setProfileType(UserType.BANK.name());
        bankProfile.setMerchantId(BANK_ID);
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID))
                .thenReturn(merchantProfile);
        when(authUserService.getBankBusinessByBankId(ACCESS_TOKEN, BANK_ID))
                .thenReturn(bankProfile);
        doThrow(new InvalidUserException("ERROR")).when(dealService).getActiveDealsForActiveBankAndMerchant(
                any(Pageable.class), anyString(), anyString());
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetActiveDealsByBankIdAndMerchantIdIsFailed() throws Exception {
        String url = GET_ACTIVE_DEALS_BY_BANK_MERCHANT_ID_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID)
                .replace("{bankId}", BANK_ID);
        BusinessMerchantResponseDto merchantProfile = getSampleBusinessMerchantResponseDto();
        BusinessMerchantResponseDto bankProfile = getSampleBusinessMerchantResponseDto();
        merchantProfile.setProfileType(UserType.MERCHANT.name());
        merchantProfile.setActive(true);
        bankProfile.setProfileType(UserType.BANK.name());
        bankProfile.setMerchantId(BANK_ID);
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID))
                .thenReturn(merchantProfile);
        when(authUserService.getBankBusinessByBankId(ACCESS_TOKEN, BANK_ID))
                .thenReturn(bankProfile);
        doThrow(new KupiCoreException("ERROR")).when(dealService).getActiveDealsForActiveBankAndMerchant(
                any(Pageable.class), anyString(), anyString());
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidCategoryIdAndSearchTermAreProvidedForGetDealsByCategoryId() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        String url = GET_DEAL_BY_CATEGORY_ID_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{categoryId}", CATEGORY_ID)
                .replace("{searchTerm}", SEARCH_TERM).replace("{userType}", UserType.MERCHANT.name());
        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
        when(dealService.getAllSearchDeals(pageable, CATEGORY_ID, ALL, ALL, SEARCH_TERM,
                false, false, UserType.MERCHANT)).thenReturn(dealSearchIndexPage);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_DEAL_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_DEAL_LIST.getCode()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.data.deals[0].id", startsWith("did-")));
    }

    @Test
    void Should_BadRequest_When_UserTypeIsNeitherBankNorMerchantForGetDealsByCategoryId() throws Exception {
        String url = GET_DEAL_BY_CATEGORY_ID_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{categoryId}", CATEGORY_ID)
                .replace("{searchTerm}", SEARCH_TERM).replace("{userType}", UserType.ADMIN.name());
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidCategoryIdIsProvidedForGetDealsByCategoryId() throws Exception {
        String url = GET_DEAL_BY_CATEGORY_ID_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{categoryId}", CATEGORY_ID)
                .replace("{searchTerm}", SEARCH_TERM).replace("{userType}", UserType.MERCHANT.name());
        doThrow(new InvalidCategoryException("ERROR")).when(categoryService).validateCategoryId(CATEGORY_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_CATEGORY_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_CATEGORY_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetDealsByCategoryIdIsFailed() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        String url = GET_DEAL_BY_CATEGORY_ID_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{categoryId}", CATEGORY_ID)
                .replace("{searchTerm}", SEARCH_TERM).replace("{userType}", UserType.MERCHANT.name());
        doThrow(new KupiCoreException("ERROR")).when(dealService).getAllSearchDeals(pageable, CATEGORY_ID, ALL, ALL, SEARCH_TERM,
                false, false, UserType.MERCHANT);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidFieldsAreProvidedForGetDealsForBulkUser() throws Exception {
        BulkUserRequestDto bulkUserRequestDto = getSampleBulkUserRequestDto();
        List<DealSearchIndex> dealSearchIndexList = getSampleDealList();
        Map<String, MerchantBusinessResponseDto> merchantBusinessResponseDtoMap = getSampleMerchantBusinessResponseDtoMap();
        List<String> merchantIdsList = new ArrayList<>();
        merchantIdsList.add(MERCHANT_ID);

        // Extract deal IDs and create coupon statistics
        var dealIdList = dealSearchIndexList.stream()
                .map(DealSearchIndex::getId)
                .collect(Collectors.toList());
        CouponStatistics couponStatistics = getSampleCouponStatistics();
        // Set the deal ID to match the deal search index ID
        couponStatistics.getDeal().setId(dealSearchIndexList.get(0).getId());
        List<CouponStatistics> couponStatisticsList = new ArrayList<>();
        couponStatisticsList.add(couponStatistics);

        // Create a Page from the list for filtering
        Page<DealSearchIndex> dealSearchIndexPage = new PageImpl<>(dealSearchIndexList);

        when(dealService.getDealsForBulkMerchantIds(merchantIdsList, TIME_ZONE)).thenReturn(dealSearchIndexList);
        when(couponStatisticsService.getCouponStatisticsByDealIdList(dealIdList)).thenReturn(couponStatisticsList);
        when(dealService.removeCouponNotAvailableDeals(any(Page.class), anyList())).thenReturn(dealSearchIndexPage);
        when(authUserService.getMerchantMap(bulkUserRequestDto, UserType.MERCHANT))
                .thenReturn(merchantBusinessResponseDtoMap);
        mockMvc.perform(MockMvcRequestBuilders.post(GET_DEAL_FOR_BULK_USER_URL)
                        .content(bulkUserRequestDto.toJson())
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.RETURNED_DEALS_BY_MERCHANT_IDS.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.RETURNED_DEALS_BY_MERCHANT_IDS.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_UserIdsIsNullForGetDealsForBulkUser() throws Exception {
        BulkUserRequestDto bulkUserRequestDto = getSampleBulkUserRequestDto();
        bulkUserRequestDto.getUserIds().remove(0);
        mockMvc.perform(MockMvcRequestBuilders.post(GET_DEAL_FOR_BULK_USER_URL)
                        .content(bulkUserRequestDto.toJson())
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_UserIdsSizeIsNotAcceptableForGetDealsForBulkUser() throws Exception {
        BulkUserRequestDto bulkUserRequestDto = getSampleBulkUserRequestDtoWithTwentySixValues();
        mockMvc.perform(MockMvcRequestBuilders.post(GET_DEAL_FOR_BULK_USER_URL)
                        .content(bulkUserRequestDto.toJson())
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MAX_USER_ID_LIMIT_EXCEEDED.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MAX_USER_ID_LIMIT_EXCEEDED.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetDealsForBulkUserIsFailed() throws Exception {
        BulkUserRequestDto bulkUserRequestDto = getSampleBulkUserRequestDto();
        doThrow(new KupiCoreException("ERROR")).when(dealService).getDealsForBulkMerchantIds(anyList(), anyString());
        mockMvc.perform(MockMvcRequestBuilders.post(GET_DEAL_FOR_BULK_USER_URL)
                        .content(bulkUserRequestDto.toJson())
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidBrandIdAndSearchTermAreProvidedForGetDealsByBrandId() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        String url = GET_DEAL_BY_BRAND_ID_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{brandId}", BRAND_ID)
                .replace("{searchTerm}", SEARCH_TERM).replace("{userType}", UserType.MERCHANT.name());
        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
        when(dealService.getAllSearchDeals(pageable, ALL, ALL, BRAND_ID, SEARCH_TERM,
                false, false, UserType.MERCHANT)).thenReturn(dealSearchIndexPage);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_DEAL_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_DEAL_LIST.getCode()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.data.deals[0].id", startsWith("did-")));
    }

    @Test
    void Should_BadRequest_When_UserTypeIsNeitherBankNorMerchantForGetDealsByBrandId() throws Exception {
        String url = GET_DEAL_BY_BRAND_ID_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{brandId}", BRAND_ID)
                .replace("{searchTerm}", SEARCH_TERM).replace("{userType}", UserType.ADMIN.name());
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidBrandIdIsProvidedForGetDealsByBrandId() throws Exception {
        String url = GET_DEAL_BY_BRAND_ID_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{brandId}", BRAND_ID)
                .replace("{searchTerm}", SEARCH_TERM).replace("{userType}", UserType.MERCHANT.name());
        doThrow(new InvalidBrandException("ERROR")).when(brandService).validateBrandId(BRAND_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_BRAND_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_BRAND_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetDealsByBrandIdIsFailed() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        String url = GET_DEAL_BY_BRAND_ID_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{brandId}", BRAND_ID)
                .replace("{searchTerm}", SEARCH_TERM).replace("{userType}", UserType.MERCHANT.name());
        doThrow(new KupiCoreException("ERROR")).when(dealService).getAllSearchDeals(pageable, ALL, ALL, BRAND_ID, SEARCH_TERM,
                false, false, UserType.MERCHANT);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_DealIsApprovedProvidedForApproveDeal() throws Exception {
        DealApprovalStatusUpdateRequestDto dealApprovalStatusUpdateRequestDto = getSampleDealApprovalStatusUpdateRequestDto();
        String url = APPROVE_DEAL_URL.replace("{userType}", UserType.MERCHANT.name());
        doNothing().when(dealService).updateApprovalOfDeal(dealApprovalStatusUpdateRequestDto, ACCESS_TOKEN, UserType.MERCHANT);
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealApprovalStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.APPROVE_DEAL.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.APPROVE_DEAL.getCode()));
    }

    @Test
    void Should_ReturnOk_When_DealIsRejectedProvidedForApproveDeal() throws Exception {
        DealApprovalStatusUpdateRequestDto dealApprovalStatusUpdateRequestDto = getSampleDealApprovalStatusUpdateRequestDto();
        dealApprovalStatusUpdateRequestDto.setApprovalStatus(ApprovalStatus.REJECTED);
        String url = APPROVE_DEAL_URL.replace("{userType}", UserType.MERCHANT.name());
        doNothing().when(dealService).updateApprovalOfDeal(dealApprovalStatusUpdateRequestDto, ACCESS_TOKEN, UserType.MERCHANT);
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealApprovalStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.REJECT_DEAL.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.REJECT_DEAL.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_UserTypeIsNeitherBankNorMerchantForApproveDeal() throws Exception {
        DealApprovalStatusUpdateRequestDto dealApprovalStatusUpdateRequestDto = getSampleDealApprovalStatusUpdateRequestDto();
        String url = APPROVE_DEAL_URL.replace("{userType}", UserType.ADMIN.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealApprovalStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_RequiredFieldsAreNotProvidedForApproveDeal() throws Exception {
        DealApprovalStatusUpdateRequestDto dealApprovalStatusUpdateRequestDto = getSampleDealApprovalStatusUpdateRequestDto();
        dealApprovalStatusUpdateRequestDto.setApprovalStatus(null);
        String url = APPROVE_DEAL_URL.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealApprovalStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_SubscriptionPlanLimitExceededForApproveDeal() throws Exception {
        DealApprovalStatusUpdateRequestDto dealApprovalStatusUpdateRequestDto = getSampleDealApprovalStatusUpdateRequestDto();
        doThrow(new LimitExceededException("ERROR", LimitExceededType.DEALS_PER_MONTH)).when(dealService)
                .updateApprovalOfDeal(any(DealApprovalStatusUpdateRequestDto.class), anyString(), any(UserType.class));
        String url = APPROVE_DEAL_URL.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealApprovalStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.DEALS_PER_MONTH_LIMIT_EXCEEDED.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.DEALS_PER_MONTH_LIMIT_EXCEEDED.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_DealIsAlreadyExpiredForApproveDeal() throws Exception {
        DealApprovalStatusUpdateRequestDto dealApprovalStatusUpdateRequestDto = getSampleDealApprovalStatusUpdateRequestDto();
        doThrow(new ExpiredDealApprovalException("ERROR")).when(dealService)
                .updateApprovalOfDeal(any(DealApprovalStatusUpdateRequestDto.class), anyString(), any(UserType.class));
        String url = APPROVE_DEAL_URL.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealApprovalStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.EXPIRED_DEAL_APPROVAL.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.EXPIRED_DEAL_APPROVAL.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidMerchantIdIsProvidedForApproveDeal() throws Exception {
        DealApprovalStatusUpdateRequestDto dealApprovalStatusUpdateRequestDto = getSampleDealApprovalStatusUpdateRequestDto();
        doThrow(new InvalidUserException("ERROR")).when(dealService)
                .updateApprovalOfDeal(any(DealApprovalStatusUpdateRequestDto.class), anyString(), any(UserType.class));
        String url = APPROVE_DEAL_URL.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealApprovalStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidDealIdIsProvidedForApproveDeal() throws Exception {
        DealApprovalStatusUpdateRequestDto dealApprovalStatusUpdateRequestDto = getSampleDealApprovalStatusUpdateRequestDto();
        doThrow(new InvalidDealException("ERROR")).when(dealService)
                .updateApprovalOfDeal(any(DealApprovalStatusUpdateRequestDto.class), anyString(), any(UserType.class));
        String url = APPROVE_DEAL_URL.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealApprovalStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_DEAL_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_DEAL_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_ApproveDealIsFailed() throws Exception {
        DealApprovalStatusUpdateRequestDto dealApprovalStatusUpdateRequestDto = getSampleDealApprovalStatusUpdateRequestDto();
        doThrow(new KupiCoreException("ERROR")).when(dealService)
                .updateApprovalOfDeal(any(DealApprovalStatusUpdateRequestDto.class), anyString(), any(UserType.class));
        String url = APPROVE_DEAL_URL.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealApprovalStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }


    void Should_ReturnOk_When_DealUpdatedSuccessfully() throws Exception {
        Deal deal = getSampleDeal();
        DealUpdateRequestDto dealUpdateRequestDto = getSampleDealUpdateRequestDto();
        dealUpdateRequestDto.setValidFrom(System.currentTimeMillis() + 10000);
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        when(dealService.getDeal(DEAL_ID, UserType.MERCHANT)).thenReturn(deal);
        when(authUserService.getMerchantBusinessByMerchantId(anyString(), anyString()))
                .thenReturn(businessMerchantResponseDto);
        when(dealService.updateDeal(any(DealUpdateRequestDto.class), any(UserType.class), any(Deal.class), any(BusinessMerchantResponseDto.class)))
                .thenReturn(deal);
        String url = UPDATE_DEAL_URI.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.UPDATE_DEAL.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.UPDATE_DEAL.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_UserTypeIsNeitherBankNorMerchantForUpdateDeal() throws Exception {
        DealUpdateRequestDto dealUpdateRequestDto = getSampleDealUpdateRequestDto();
        String url = UPDATE_DEAL_URI.replace("{userType}", UserType.ADMIN.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_RequiredFieldsAreMissingForUpdateDeal() throws Exception {
        DealUpdateRequestDto dealUpdateRequestDto = getSampleDealUpdateRequestDto();
        dealUpdateRequestDto.setShopId(null);
        String url = UPDATE_DEAL_URI.replace("{userType}", UserType.BANK.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_ValidDateIsAlreadyExpiredForUpdateDeal() throws Exception {
        DealUpdateRequestDto dealUpdateRequestDto = getSampleDealUpdateRequestDto();
        dealUpdateRequestDto.setValidFrom(System.currentTimeMillis() - 10000);
        String url = UPDATE_DEAL_URI.replace("{userType}", UserType.BANK.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_VALID_FROM_DATE.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_VALID_FROM_DATE.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidOriginalPriceIsProvidedForUpdateDeal() throws Exception {
        DealUpdateRequestDto dealUpdateRequestDto = getSampleDealUpdateRequestDto();
        dealUpdateRequestDto.setValidFrom(System.currentTimeMillis() + 10000);
        dealUpdateRequestDto.setOriginalPrice(-10.00);
        String url = UPDATE_DEAL_URI.replace("{userType}", UserType.BANK.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_PRICE.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_PRICE.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidExpiredDateIsProvidedForUpdateDeal() throws Exception {
        DealUpdateRequestDto dealUpdateRequestDto = getSampleDealUpdateRequestDto();
        dealUpdateRequestDto.setValidFrom(System.currentTimeMillis() + 10000);
        dealUpdateRequestDto.setExpiredOn(System.currentTimeMillis() - 40000);
        String url = UPDATE_DEAL_URI.replace("{userType}", UserType.BANK.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_EXPIRED_ON_DATE.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_EXPIRED_ON_DATE.getCode()));
    }


    void Should_ReturnBadRequest_When_InvalidDeductionAmountIsProvidedForUpdateDeal() throws Exception {
        DealUpdateRequestDto dealUpdateRequestDto = getSampleDealUpdateRequestDto();
        dealUpdateRequestDto.setValidFrom(System.currentTimeMillis() + 10000);
        dealUpdateRequestDto.setDeductionAmount(-10.00);
        String url = UPDATE_DEAL_URI.replace("{userType}", UserType.BANK.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_DEDUCTION_AMOUNT.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_DEDUCTION_AMOUNT.getCode()));
    }


    void Should_ReturnBadRequest_When_InvalidDeductionPercentageIsProvidedForUpdateDeal() throws Exception {
        DealUpdateRequestDto dealUpdateRequestDto = getSampleDealUpdateRequestDto();
        dealUpdateRequestDto.setValidFrom(System.currentTimeMillis() + 10000);
        dealUpdateRequestDto.setDeductionType(DeductionType.PERCENTAGE);
        dealUpdateRequestDto.setDeductionPercentage(-10.00);
        String url = UPDATE_DEAL_URI.replace("{userType}", UserType.BANK.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_DEDUCTION_PERCENTAGE.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_DEDUCTION_PERCENTAGE.getCode()));
    }


    void Should_ReturnBadRequest_When_DealApprovalIsPendingForUpdateDeal() throws Exception {
        Deal deal = getSampleDeal();
        DealUpdateRequestDto dealUpdateRequestDto = getSampleDealUpdateRequestDto();
        dealUpdateRequestDto.setValidFrom(System.currentTimeMillis() + 10000);
        when(dealService.getDeal(DEAL_ID, UserType.MERCHANT)).thenReturn(deal);
        deal.setApprovalStatus(ApprovalStatus.APPROVED);
        String url = UPDATE_DEAL_URI.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_DEAL_UPDATE.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_DEAL_UPDATE.getCode()));
    }


    void Should_ReturnBadRequest_When_InvalidDealIdIsProvidedForUpdateDeal() throws Exception {
        DealUpdateRequestDto dealUpdateRequestDto = getSampleDealUpdateRequestDto();
        dealUpdateRequestDto.setValidFrom(System.currentTimeMillis() + 10000);
        doThrow(new InvalidDealException("ERROR")).when(dealService).getDeal(DEAL_ID, UserType.MERCHANT);
        String url = UPDATE_DEAL_URI.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_DEAL_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_DEAL_ID.getCode()));
    }


    void Should_ReturnBadRequest_When_InvalidMerchantIdIsProvidedForUpdateDeal() throws Exception {
        Deal deal = getSampleDeal();
        DealUpdateRequestDto dealUpdateRequestDto = getSampleDealUpdateRequestDto();
        dealUpdateRequestDto.setValidFrom(System.currentTimeMillis() + 10000);
        when(dealService.getDeal(DEAL_ID, UserType.MERCHANT)).thenReturn(deal);
        doThrow(new InvalidUserException("ERROR")).when(authUserService).getMerchantBusinessByMerchantId(anyString(), anyString());
        String url = UPDATE_DEAL_URI.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode()));
    }


    void Should_ReturnBadRequest_When_InvalidCategoryIdIsProvidedForUpdateDeal() throws Exception {
        Deal deal = getSampleDeal();
        DealUpdateRequestDto dealUpdateRequestDto = getSampleDealUpdateRequestDto();
        dealUpdateRequestDto.setValidFrom(System.currentTimeMillis() + 10000);
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        when(dealService.getDeal(DEAL_ID, UserType.MERCHANT)).thenReturn(deal);
        when(authUserService.getMerchantBusinessByMerchantId(anyString(), anyString()))
                .thenReturn(businessMerchantResponseDto);
        doThrow(new InvalidCategoryException("ERROR")).when(dealService).updateDeal(any(DealUpdateRequestDto.class),
                any(UserType.class), any(Deal.class), any(BusinessMerchantResponseDto.class));
        String url = UPDATE_DEAL_URI.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_CATEGORY_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_CATEGORY_ID.getCode()));
    }


    void Should_ReturnBadRequest_When_InvalidBrandIdIsProvidedForUpdateDeal() throws Exception {
        Deal deal = getSampleDeal();
        DealUpdateRequestDto dealUpdateRequestDto = getSampleDealUpdateRequestDto();
        dealUpdateRequestDto.setValidFrom(System.currentTimeMillis() + 10000);
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        when(dealService.getDeal(DEAL_ID, UserType.MERCHANT)).thenReturn(deal);
        when(authUserService.getMerchantBusinessByMerchantId(anyString(), anyString()))
                .thenReturn(businessMerchantResponseDto);
        doThrow(new InvalidBrandException("ERROR")).when(dealService).updateDeal(any(DealUpdateRequestDto.class),
                any(UserType.class), any(Deal.class), any(BusinessMerchantResponseDto.class));
        String url = UPDATE_DEAL_URI.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_BRAND_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_BRAND_ID.getCode()));
    }


    void Should_ReturnBadRequest_When_InvalidDateOptionIsProvidedForUpdateDeal() throws Exception {
        Deal deal = getSampleDeal();
        DealUpdateRequestDto dealUpdateRequestDto = getSampleDealUpdateRequestDto();
        dealUpdateRequestDto.setValidFrom(System.currentTimeMillis() + 10000);
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        when(dealService.getDeal(DEAL_ID, UserType.MERCHANT)).thenReturn(deal);
        when(authUserService.getMerchantBusinessByMerchantId(anyString(), anyString()))
                .thenReturn(businessMerchantResponseDto);
        doThrow(new InvalidDateOptionException("ERROR")).when(dealService).updateDeal(any(DealUpdateRequestDto.class),
                any(UserType.class), any(Deal.class), any(BusinessMerchantResponseDto.class));
        String url = UPDATE_DEAL_URI.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.DEAL_DATES_DOESNT_MATCH_SEASONAL_CATEGORIES.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.DEAL_DATES_DOESNT_MATCH_SEASONAL_CATEGORIES.getCode()));
    }


    void Should_ReturnInternalServerError_When_UpdateDealIsFailed() throws Exception {
        Deal deal = getSampleDeal();
        DealUpdateRequestDto dealUpdateRequestDto = getSampleDealUpdateRequestDto();
        dealUpdateRequestDto.setValidFrom(System.currentTimeMillis() + 10000);
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        when(dealService.getDeal(DEAL_ID, UserType.MERCHANT)).thenReturn(deal);
        when(authUserService.getMerchantBusinessByMerchantId(anyString(), anyString()))
                .thenReturn(businessMerchantResponseDto);
        doThrow(new KupiCoreException("ERROR")).when(dealService).updateDeal(any(DealUpdateRequestDto.class),
                any(UserType.class), any(Deal.class), any(BusinessMerchantResponseDto.class));
        String url = UPDATE_DEAL_URI.replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(dealUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_DeleteDealSuccessfully() throws Exception {
        String url = DELETE_DEAL_URL.replace("{dealId}", DEAL_ID)
                .replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.delete(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.DELETE_DEAL.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.DELETE_DEAL.getCode()));

    }

    @Test
    void Should_ReturnBadRequest_When_UserTypeIsNeitherBankNorMerchantForDeleteDeal() throws Exception {
        String url = DELETE_DEAL_URL.replace("{dealId}", DEAL_ID)
                .replace("{userType}", UserType.ADMIN.name());
        mockMvc.perform(MockMvcRequestBuilders.delete(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidDealIdIsProvidedForDeleteDeal() throws Exception {
        doThrow(new InvalidDealException("ERROR")).when(dealService).deleteDeal(DEAL_ID, UserType.MERCHANT);
        String url = DELETE_DEAL_URL.replace("{dealId}", DEAL_ID)
                .replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.delete(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_DEAL_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_DEAL_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_DeleteDealIsFailed() throws Exception {
        doThrow(new KupiCoreException("ERROR")).when(dealService).deleteDeal(DEAL_ID, UserType.MERCHANT);
        String url = DELETE_DEAL_URL.replace("{dealId}", DEAL_ID)
                .replace("{userType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.delete(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_SearchingActiveDealsForActiveMerchant() throws Exception {
        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
        String url = GET_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID)
                .replace("{categoryId}", ALL).replace("{searchTerm}", SEARCH_TERM)
                .replace("{userType}", UserType.MERCHANT.name());
        CouponStatistics couponStatistics = getSampleCouponStatistics();
        List<CouponStatistics> couponStatisticsList = new ArrayList<>();
        couponStatisticsList.add(couponStatistics);
        var dealIdList = dealSearchIndexPage.get().map(DealSearchIndex::getId).collect(Collectors.toList());
        when(dealService.getAllSearchDeals(any(Pageable.class), anyString(), anyString(), anyString(), anyString(),
                anyBoolean(), anyBoolean(), any(UserType.class))).thenReturn(dealSearchIndexPage);
        when(couponStatisticsService.getCouponStatisticsByDealIdList(dealIdList)).thenReturn(couponStatisticsList);
        when(dealService.removeCouponNotAvailableDeals(any(Page.class), anyList())).thenReturn(dealSearchIndexPage);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_DEAL_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_DEAL_LIST.getCode()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.data.deals[0].id", startsWith("did-")));
    }

    @Test
    void Should_ReturnOk_When_CategoryIdIsNotEqualToAllForSearchDealsForActiveMerchant() throws Exception {
        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
        String url = GET_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID)
                .replace("{categoryId}", CATEGORY_ID).replace("{searchTerm}", SEARCH_TERM)
                .replace("{userType}", UserType.MERCHANT.name());
        CouponStatistics couponStatistics = getSampleCouponStatistics();
        List<CouponStatistics> couponStatisticsList = new ArrayList<>();
        couponStatisticsList.add(couponStatistics);
        var dealIdList = dealSearchIndexPage.get().map(DealSearchIndex::getId).collect(Collectors.toList());
        when(dealService.getAllSearchDeals(any(Pageable.class), anyString(), anyString(), anyString(), anyString(),
                anyBoolean(), anyBoolean(), any(UserType.class))).thenReturn(dealSearchIndexPage);
        when(couponStatisticsService.getCouponStatisticsByDealIdList(dealIdList)).thenReturn(couponStatisticsList);
        when(dealService.removeCouponNotAvailableDeals(any(Page.class), anyList())).thenReturn(dealSearchIndexPage);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_DEAL_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_DEAL_LIST.getCode()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.data.deals[0].id", startsWith("did-")));
    }

    @Test
    void Should_BadRequest_When_UserTypeIsNeitherBankNorMerchantForSearchDealsForActiveMerchant() throws Exception {
        String url = GET_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID)
                .replace("{categoryId}", CATEGORY_ID).replace("{searchTerm}", SEARCH_TERM)
                .replace("{userType}", UserType.ADMIN.name());
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidCategoryIdIsProvidedForSearchDealsForActiveMerchant() throws Exception {
        String url = GET_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID)
                .replace("{categoryId}", CATEGORY_ID).replace("{searchTerm}", SEARCH_TERM)
                .replace("{userType}", UserType.MERCHANT.name());
        doThrow(new InvalidCategoryException("ERROR")).when(categoryService).validateCategoryId(CATEGORY_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_CATEGORY_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_CATEGORY_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_SearchDealsForActiveMerchantIsFailed() throws Exception {
        String url = GET_ACTIVE_DEALS_FOR_ACTIVE_MERCHANT_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID)
                .replace("{categoryId}", CATEGORY_ID).replace("{searchTerm}", SEARCH_TERM)
                .replace("{userType}", UserType.MERCHANT.name());
        doThrow(new KupiCoreException("ERROR")).when(dealService).getAllSearchDeals(any(Pageable.class), anyString(),
                anyString(), anyString(), anyString(),
                anyBoolean(), anyBoolean(), any(UserType.class));
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidFieldsAreProvidedForGetRecentlyExpireDealsForMobile() throws Exception {
        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
        String url = GET_RECENTLY_EXPIRE_DEALS_FOR_MOBILE_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID)
                .replace("{categoryId}", ALL).replace("{searchTerm}", SEARCH_TERM)
                .replace("{userType}", UserType.MERCHANT.name());
        CouponStatistics couponStatistics = getSampleCouponStatistics();
        List<CouponStatistics> couponStatisticsList = new ArrayList<>();
        couponStatisticsList.add(couponStatistics);
        var dealIdList = dealSearchIndexPage.get().map(DealSearchIndex::getId).collect(Collectors.toList());
        when(dealService.getRecentlyExpireDeals(any(Pageable.class), anyString(), anyString(), anyString(), anyString(),
                any(UserType.class))).thenReturn(dealSearchIndexPage);
        when(couponStatisticsService.getCouponStatisticsByDealIdList(dealIdList)).thenReturn(couponStatisticsList);
        when(dealService.removeCouponNotAvailableDeals(any(Page.class), anyList())).thenReturn(dealSearchIndexPage);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_DEAL_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_DEAL_LIST.getCode()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.data.deals[0].id", startsWith("did-")));
    }

    @Test
    void Should_ReturnOk_When_CategoryIdIsNotEqualToAllForGetRecentlyExpireDealsForMobile() throws Exception {
        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
        String url = GET_RECENTLY_EXPIRE_DEALS_FOR_MOBILE_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID)
                .replace("{categoryId}", CATEGORY_ID).replace("{searchTerm}", SEARCH_TERM)
                .replace("{userType}", UserType.MERCHANT.name());
        CouponStatistics couponStatistics = getSampleCouponStatistics();
        List<CouponStatistics> couponStatisticsList = new ArrayList<>();
        couponStatisticsList.add(couponStatistics);
        var dealIdList = dealSearchIndexPage.get().map(DealSearchIndex::getId).collect(Collectors.toList());
        when(dealService.getRecentlyExpireDeals(any(Pageable.class), anyString(), anyString(), anyString(), anyString(),
                any(UserType.class))).thenReturn(dealSearchIndexPage);
        when(couponStatisticsService.getCouponStatisticsByDealIdList(dealIdList)).thenReturn(couponStatisticsList);
        when(dealService.removeCouponNotAvailableDeals(any(Page.class), anyList())).thenReturn(dealSearchIndexPage);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_DEAL_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_DEAL_LIST.getCode()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.data.deals[0].id", startsWith("did-")));
    }

    @Test
    void Should_BadRequest_When_UserTypeIsNeitherBankNorMerchantForGetRecentlyExpireDealsForMobile() throws Exception {
        String url = GET_RECENTLY_EXPIRE_DEALS_FOR_MOBILE_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID)
                .replace("{categoryId}", CATEGORY_ID).replace("{searchTerm}", SEARCH_TERM)
                .replace("{userType}", UserType.ADMIN.name());
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidCategoryIdIsProvidedForGetRecentlyExpireDealsForMobile() throws Exception {
        String url = GET_RECENTLY_EXPIRE_DEALS_FOR_MOBILE_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID)
                .replace("{categoryId}", CATEGORY_ID).replace("{searchTerm}", SEARCH_TERM)
                .replace("{userType}", UserType.MERCHANT.name());
        doThrow(new InvalidCategoryException("ERROR")).when(categoryService).validateCategoryId(CATEGORY_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_CATEGORY_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_CATEGORY_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetRecentlyExpireDealsIsFailedForMobile() throws Exception {
        String url = GET_RECENTLY_EXPIRE_DEALS_FOR_MOBILE_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{merchantId}", MERCHANT_ID)
                .replace("{categoryId}", CATEGORY_ID).replace("{searchTerm}", SEARCH_TERM)
                .replace("{userType}", UserType.MERCHANT.name());
        doThrow(new KupiCoreException("ERROR")).when(dealService).getRecentlyExpireDeals(any(Pageable.class), anyString(),
                anyString(), anyString(), anyString(), any(UserType.class));
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    /**
     * This method creates sample deal request dto
     *
     * @return DealRequestDto
     */
    private DealRequestDto getSampleDealRequestDto() {
        DealRequestDto dealRequestDto = new DealRequestDto();
        dealRequestDto.setTitle("New Year Deal.");
        dealRequestDto.setSubTitle("New Year 2022");
        dealRequestDto.setDescription("The New Year is near");
        dealRequestDto.setTermsAndConditions("Buy 1500LKR and have the discount");
        List<String> imgUrls = new ArrayList<>();
        imgUrls.add("http://goo.lk/");
        imgUrls.add("http://goo.lk/");
        dealRequestDto.setImageUrls(imgUrls);
        dealRequestDto.setCoverImage("http://goo.lk/");
        dealRequestDto.setOriginalPrice(52.00);
        dealRequestDto.setQuantity(25);
        dealRequestDto.setDeductionType(DeductionType.PERCENTAGE);
        dealRequestDto.setDeductionPercentage(25.25);
        dealRequestDto.setValidFrom(System.currentTimeMillis() + 86400000);
        dealRequestDto.setExpiredOn(System.currentTimeMillis() + 432000000);
        dealRequestDto.setMerchantId("uid-001");
        dealRequestDto.setShopId(SHOP_ID);
        List<String> brandId = new ArrayList<>();
        brandId.add("bid-001");
        dealRequestDto.setBrandIds(brandId);
        List<String> categoryId = new ArrayList<>();
        categoryId.add("cid-001");
        dealRequestDto.setCategoryIds(categoryId);
        return dealRequestDto;
    }

    /**
     * This method creates sample BusinessMerchantResponseDto
     *
     * @return BusinessMerchantResponseDto
     */
    private BusinessMerchantResponseDto getSampleBusinessMerchantResponseDto() {
        BusinessMerchantResponseDto businessMerchantResponseDto = new BusinessMerchantResponseDto();
        businessMerchantResponseDto.setBusinessId("001");
        businessMerchantResponseDto.setMerchantId(MERCHANT_ID);
        businessMerchantResponseDto.setBusinessName("Kupi");
        businessMerchantResponseDto.setOwnerName("Kupi");
        businessMerchantResponseDto.setApprovalStatus(ApprovalStatus.APPROVED);
        businessMerchantResponseDto.setAddress("Dehiwala");
        businessMerchantResponseDto.setEmail("leel@gmail.com");
        businessMerchantResponseDto.setProfileType("MERCHANT");
        businessMerchantResponseDto.setActive(true);
        businessMerchantResponseDto.setProfileType(UserType.MERCHANT.name());
        return businessMerchantResponseDto;
    }

    /**
     * This method creates sample Category
     *
     * @return Category
     */
    private Category getSampleCategory() {
        Set<Category> relationalCategories = new HashSet<>();
        Category category = new Category();
        category.setId(CATEGORY_ID);
        category.setName(CATEGORY_NAME);
        category.setDescription(CATEGORY_DESCRIPTION);
        category.setImageUrl(CATEGORY_IMAGE_URL);
        category.setCategoryType(CategoryType.SEASONAL);
        category.setStartDate(System.currentTimeMillis() - 3800);
        category.setExpiryDate(System.currentTimeMillis() - 100000);
        category.setRelatedCategories(relationalCategories);
        category.setPopular(true);
        return category;
    }

    /**
     * This method creates sample category set
     *
     * @return categorySet
     */
    private Set<Category> getSampleCategorySet() {
        Category category = getSampleCategory();
        Set<Category> categorySet = new HashSet<>();
        categorySet.add(category);
        return categorySet;
    }

    /**
     * This method creates sample brand set
     *
     * @return brandSet
     */
    private Set<Brand> getSampleBrandSet() {
        Brand brand = new Brand();
        brand.setId("bid-33b8bc1f-4966-420c-85d0-8626b774987a");
        brand.setDescription("description");
        Set<Brand> brandSet = new HashSet<>();
        brandSet.add(brand);
        return brandSet;
    }

    /**
     * This method creates sample Deal
     *
     * @return Deal
     */
    private Deal getSampleDeal() {
        Set<Category> categories = getSampleCategorySet();
        Set<Brand> brands = getSampleBrandSet();
        DealRequestDto dealRequestDto = getSampleDealRequestDto();
        return new Deal(dealRequestDto, brands, categories,null, null);
    }

    /**
     * This method creates sample deal page
     *
     * @return dealPage
     */
    private Page<Deal> getSampleDealPage() {
        Deal deal = getSampleDeal();
        List<Deal> dealList = new ArrayList<>();
        dealList.add(deal);
        return new PageImpl<>(dealList);
    }

    /**
     * This method creates sample MerchantBusinessResponseDto
     *
     * @return MerchantBusinessResponseDto
     */
    private MerchantBusinessResponseDto getSampleMerchantBusinessResponseDto() {
        MerchantBusinessResponseDto merchantBusinessResponseDto = new MerchantBusinessResponseDto();
        merchantBusinessResponseDto.setId(MERCHANT_ID);
        merchantBusinessResponseDto.setActive(true);
        merchantBusinessResponseDto.setImageUrl("http://goo.lk/");
        merchantBusinessResponseDto.setApprovalStatus(ApprovalStatus.APPROVED);
        merchantBusinessResponseDto.setUserType(UserType.MERCHANT.name());
        return merchantBusinessResponseDto;
    }

    /**
     * This method creates sample map with MerchantBusinessResponseDto
     *
     * @return merchantBusinessResponseDtoMap
     */
    private Map<String, MerchantBusinessResponseDto> getSampleMerchantBusinessResponseDtoMap() {
        MerchantBusinessResponseDto merchantBusinessResponseDto = getSampleMerchantBusinessResponseDto();
        Map<String, MerchantBusinessResponseDto> merchantBusinessResponseDtoMap = new HashMap<>();
        merchantBusinessResponseDtoMap.put(merchantBusinessResponseDto.getId(), merchantBusinessResponseDto);
        return merchantBusinessResponseDtoMap;
    }

    /**
     * This method creates sample DealSearchIndex page
     *
     * @return dealSearchIndexPage
     */
    private Page<DealSearchIndex> getSampleDealSearchIndexPage() {
        DealSearchIndex dealSearchIndex = new DealSearchIndex();
        dealSearchIndex.setId("did-02104ca3-41e4-4cb2-8a06-8ae0805ef6dc");
        dealSearchIndex.setApprovalStatus(ApprovalStatus.APPROVED);
        dealSearchIndex.setDeleted(false);
        dealSearchIndex.setTitle("Bank deal to be approved");
        dealSearchIndex.setActiveMerchant(true);
        dealSearchIndex.setBrandNames("Brand");
        dealSearchIndex.setDealSource(UserType.MERCHANT);
        dealSearchIndex.setCategoryNames("category");
        dealSearchIndex.setSearchImageUrls("[https://objects-kupi-dev.s3.ap-southeast-1.amazonaws.com/fid-a61e4565-dd03-4bb1-a123-90460ca3def5.png]");
        dealSearchIndex.setSubTitle("bank deal");
        dealSearchIndex.setTermsAndConditions("no conditions ");
        List<DealSearchIndex> dealSearchIndexList = new ArrayList<>();
        dealSearchIndexList.add(dealSearchIndex);
        return new PageImpl<>(dealSearchIndexList);
    }

    /**
     * This method creates the sample bank deal page
     *
     * @return bankDeal page
     */
    private Page<BankDeal> getSampleBankDealPage() {
        List<BankDeal> bankDealList = new ArrayList<>();
        Deal deal = getSampleDeal();
        bankDealList.add(new BankDeal(deal));
        return new PageImpl<>(bankDealList);
    }

    /**
     * This method creates the deal list
     *
     * @return DealSearchIndex list
     */
    private List<DealSearchIndex> getSampleDealList() {
        DealSearchIndex dealSearchIndex = new DealSearchIndex();
        dealSearchIndex.setId("did-02104ca3-41e4-4cb2-8a06-8ae0805ef6dc");
        dealSearchIndex.setApprovalStatus(ApprovalStatus.APPROVED);
        dealSearchIndex.setDeleted(false);
        dealSearchIndex.setTitle("Bank deal to be approved");
        dealSearchIndex.setActiveMerchant(true);
        dealSearchIndex.setSearchImageUrls("[https://objects-kupi-dev.s3.ap-southeast-1.amazonaws.com/fid-a61e4565-dd03-4bb1-a123-90460ca3def5.png]");
        dealSearchIndex.setSubTitle("bank deal");
        dealSearchIndex.setTermsAndConditions("no conditions ");
        List<DealSearchIndex> dealSearchIndexList = new ArrayList<>();
        dealSearchIndexList.add(dealSearchIndex);
        return dealSearchIndexList;
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
     * This method creates sample bulk user request dto with exceeded size .
     *
     * @return BulkUserRequestDto
     */
    private BulkUserRequestDto getSampleBulkUserRequestDtoWithTwentySixValues() {
        BulkUserRequestDto bulkUserRequestDto = new BulkUserRequestDto();
        List<String> userIds = new ArrayList<>();
        for (int i = 0; i < 26; i++) {
            userIds.add(USER_ID);
        }
        bulkUserRequestDto.setUserIds(userIds);
        return bulkUserRequestDto;
    }

    /**
     * This method creates the sample DealApprovalStatusUpdateRequestDto
     *
     * @return DealApprovalStatusUpdateRequestDto
     */
    private DealApprovalStatusUpdateRequestDto getSampleDealApprovalStatusUpdateRequestDto() {
        DealApprovalStatusUpdateRequestDto dealApprovalStatusUpdateRequestDto = new DealApprovalStatusUpdateRequestDto();
        dealApprovalStatusUpdateRequestDto.setApprovalStatus(ApprovalStatus.APPROVED);
        dealApprovalStatusUpdateRequestDto.setId(DEAL_ID);
        dealApprovalStatusUpdateRequestDto.setComment("Comment");
        return dealApprovalStatusUpdateRequestDto;
    }

    /**
     * This method creates sample deal update request dto,
     *
     * @return DealUpdateRequestDto
     */
    private DealUpdateRequestDto getSampleDealUpdateRequestDto() {
        List<String> brandList = new ArrayList<>();
        brandList.add("bid-2225");
        List<String> imgUrls = new ArrayList<>();
        imgUrls.add("https://github.com/1.png");
        imgUrls.add("https://github.com/2.png");
        List<String> categoryList = new ArrayList<>();
        categoryList.add("cid-266565");
        DealUpdateRequestDto dealUpdateRequestDto = new DealUpdateRequestDto(DEAL_ID);
        dealUpdateRequestDto.setTitle("New Year Deal");
        dealUpdateRequestDto.setSubTitle("sub title");
        dealUpdateRequestDto.setTermsAndConditions("Nothing");
        dealUpdateRequestDto.setOriginalPrice(12.25);
        dealUpdateRequestDto.setDeductionAmount(2.00);
        dealUpdateRequestDto.setQuantity(50);
        dealUpdateRequestDto.setDeductionType(DeductionType.AMOUNT);
        dealUpdateRequestDto.setValidFrom(1667217882000L);
        dealUpdateRequestDto.setExpiredOn(1672488282000L);
        dealUpdateRequestDto.setMerchantId("uid-0022");
        dealUpdateRequestDto.setBrandIds(brandList);
        dealUpdateRequestDto.setCategoryIds(categoryList);
        dealUpdateRequestDto.setImageUrls(imgUrls);
        dealUpdateRequestDto.setCoverImage("https://github.com/3.png");
        dealUpdateRequestDto.setDeductionPercentage(0.05);
        dealUpdateRequestDto.setShopId(SHOP_ID);
        return dealUpdateRequestDto;
    }


    /**
     * This method creates sample coupon statistics
     *
     * @return CouponStatistics
     */
    private CouponStatistics getSampleCouponStatistics() {
        Deal deal = getSampleDeal();
        CouponStatistics couponStatistics = new CouponStatistics();
        couponStatistics.setTotalCoupons(10);
        couponStatistics.setRemainingCoupons(5);
        couponStatistics.setId("cousid-12589-9658-7856");
        couponStatistics.setDeal(deal);
        couponStatistics.setMerchantId(MERCHANT_ID);
        couponStatistics.setRedeemedCoupons(4);
        return couponStatistics;
    }
}