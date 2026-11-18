package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.*;
import com.kupi.cc.base.domain.request.BulkUserRequestDto;
import com.kupi.cc.base.domain.request.RequestADealCreateRequestDto;
import com.kupi.cc.base.domain.response.BusinessMerchantResponseDto;
import com.kupi.cc.base.domain.response.MerchantBusinessResponseDto;
import com.kupi.cc.base.enums.*;
import com.kupi.cc.base.exception.*;
import com.kupi.cc.base.service.*;
import com.kupi.cc.base.util.DateParserUtil;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests for {@link RequestADealController} class
 */
class RequestADealControllerTest {

    private static final String CREATE_REQUEST_A_DEAL_URI = "/api/v1/request-a-deal/{toUserType}";
    private static final String GET_REQUEST_DEALS_BY_USER_ID_URI = "/api/v1/request-a-deal/{toUserType}/{page}/{size}/user/{mobileUserId}";
    private static final String GET_REQUEST_DEALS_BY_ID = "/api/v1/request-a-deal/{requestADealId}";
    private static final String GET_ALL_REQUESTS_DEALS_URI = "/api/v1/request-a-deal/combinations/{page}/{size}/{toUserType}/{merchantId}/search/{searchTerm}";
    private static final String GET_SUMMARY_FOR_SINGLE_REQUEST_A_DEAL_COMBINATION_URL = "/api/v1/request-a-deal/combination-summary/{toUserType}/{merchantId}/category/{categoryId}/brand/{brandId}";
    private static final String GET_ALL_REQUESTS_DEALS_DETAILS_URI = "/api/v1/request-a-deal/{page}/{size}/{toUserType}/{merchantId}/category/{categoryId}/brand/{brandId}";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String ACCESS_TOKEN = "Bearer192f0c96-dd19-404e-8217-a8fd1cc81d55";
    private static final String OFFER_TYPE_ID = "otid=1256-45875-8547-7852";
    private static final String CATEGORY_ID = "cid-1235-1458-1785";
    private static final String OFFER_TYPE_NAME = "OFFER_TYPE";
    private static final String BRAND_ID = "did-02a2022f-98c5-4237-a9c7-c1faf989a4a5";
    private static final String MERCHANT_ID = "uid-123456789";
    private static final String USER_ID = "uid-bb801b6a-ae7c-4f81-8876-a722f644fa11";
    private static final String REQUEST_A_DEAL_ID = "rdid-08beec5e-e905-4917-851f-278efaebb55f";
    private static final String UPDATED_AT = "updatedAt";
    private static final String SEARCH_TERM = "SEARCH TERM";
    private static final int PAGE = 0;
    private static final int SIZE = 1;
    private static final long COMBINED_REQUEST_A_DEAL_COUNT = 1;

    @Mock
    private CategoryService categoryService;
    @Mock
    private BrandService brandService;
    @Mock
    private AuthUserService authUserService;
    @Mock
    private RequestADealService requestADealService;
    @Mock
    private Translator translator;
    @Mock
    private OfferTypeService offerTypeService;
    @Mock
    private DateParserUtil dateParserUtil;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        initMocks(this);
        RequestADealController requestADealController = new RequestADealController(categoryService, brandService,
                authUserService, requestADealService, translator, offerTypeService, dateParserUtil);
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
        mockMvc = MockMvcBuilders.standaloneSetup(requestADealController)
                .setValidator(noOpValidator)
                .build();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnOk_When_UserTypeIsMerchantForCreateRequestADeal() throws Exception {
        RequestADealCreateRequestDto requestADealCreateRequestDto = getSampleRequestADealCreateRequestDto();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        Brand brand = getSampleBrand();
        OfferType offerType = getSampleOfferType();
        Category category = getSampleCategory();
        RequestADeal requestADeal = getSampleRequestADeal();
        when(categoryService.getCategoryById(anyString())).thenReturn(category);
        when(authUserService.getMerchantBusinessByMerchantId(anyString(), anyString())).thenReturn(businessMerchantResponseDto);
        when(brandService.getBrandById(anyString())).thenReturn(brand);
        when(offerTypeService.getOfferTypeById(anyString())).thenReturn(offerType);
        when(requestADealService.saveRequestADeal(any(RequestADeal.class), any(BusinessMerchantResponseDto.class), any(UserType.class)))
                .thenReturn(requestADeal);
        String uri = CREATE_REQUEST_A_DEAL_URI.replace("{toUserType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(requestADealCreateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.CREATE_REQUEST_A_DEAL.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.CREATE_REQUEST_A_DEAL.getCode()));
    }

    @Test
    void Should_ReturnOk_When_UserTypeIsBankForCreateRequestADeal() throws Exception {
        RequestADealCreateRequestDto requestADealCreateRequestDto = getSampleRequestADealCreateRequestDto();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        Brand brand = getSampleBrand();
        OfferType offerType = getSampleOfferType();
        Category category = getSampleCategory();
        RequestADeal requestADeal = getSampleRequestADeal();
        when(categoryService.getCategoryById(anyString())).thenReturn(category);
        when(authUserService.getBankBusinessByBankId(anyString(), anyString())).thenReturn(businessMerchantResponseDto);
        when(brandService.getBrandById(anyString())).thenReturn(brand);
        when(offerTypeService.getOfferTypeById(anyString())).thenReturn(offerType);
        when(requestADealService.saveRequestADeal(any(RequestADeal.class), any(BusinessMerchantResponseDto.class), any(UserType.class)))
                .thenReturn(requestADeal);
        String uri = CREATE_REQUEST_A_DEAL_URI.replace("{toUserType}", UserType.BANK.name());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(requestADealCreateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.CREATE_REQUEST_A_DEAL.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.CREATE_REQUEST_A_DEAL.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_MissingRequiredFieldsAreProvidedForCreateRequestADeal() throws Exception {
        RequestADealCreateRequestDto requestADealCreateRequestDto = getSampleRequestADealCreateRequestDto();
        requestADealCreateRequestDto.setMerchantId(null);
        String uri = CREATE_REQUEST_A_DEAL_URI.replace("{toUserType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(requestADealCreateRequestDto.toJson())
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
    void Should_ReturnBadRequest_When_InvalidMerchantIdIsProvidedForCreateRequestADeal() throws Exception {
        RequestADealCreateRequestDto requestADealCreateRequestDto = getSampleRequestADealCreateRequestDto();
        Category category = getSampleCategory();
        when(categoryService.getCategoryById(anyString())).thenReturn(category);
        doThrow(new InvalidUserException("ERROR")).when(authUserService).getMerchantBusinessByMerchantId(anyString(), anyString());
        String uri = CREATE_REQUEST_A_DEAL_URI.replace("{toUserType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(requestADealCreateRequestDto.toJson())
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
    void Should_ReturnBadRequest_When_InvalidCategoryIdIsProvidedForCreateRequestADeal() throws Exception {
        RequestADealCreateRequestDto requestADealCreateRequestDto = getSampleRequestADealCreateRequestDto();
        doThrow(new InvalidCategoryException("ERROR")).when(categoryService).getCategoryById(anyString());
        String uri = CREATE_REQUEST_A_DEAL_URI.replace("{toUserType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(requestADealCreateRequestDto.toJson())
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
    void Should_ReturnBadRequest_When_InvalidBrandIdIsProvidedForCreateRequestADeal() throws Exception {
        RequestADealCreateRequestDto requestADealCreateRequestDto = getSampleRequestADealCreateRequestDto();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        Category category = getSampleCategory();
        when(categoryService.getCategoryById(anyString())).thenReturn(category);
        when(authUserService.getMerchantBusinessByMerchantId(anyString(), anyString())).thenReturn(businessMerchantResponseDto);
        doThrow(new InvalidBrandException("ERROR")).when(brandService).getBrandById(anyString());
        String uri = CREATE_REQUEST_A_DEAL_URI.replace("{toUserType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(requestADealCreateRequestDto.toJson())
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
    void Should_ReturnBadRequest_When_InvalidOfferTypeIdIsProvidedForCreateRequestADeal() throws Exception {
        RequestADealCreateRequestDto requestADealCreateRequestDto = getSampleRequestADealCreateRequestDto();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        Brand brand = getSampleBrand();
        Category category = getSampleCategory();
        when(categoryService.getCategoryById(anyString())).thenReturn(category);
        when(authUserService.getMerchantBusinessByMerchantId(anyString(), anyString())).thenReturn(businessMerchantResponseDto);
        when(brandService.getBrandById(anyString())).thenReturn(brand);
        doThrow(new InvalidOfferTypeException("ERROR")).when(offerTypeService).getOfferTypeById(anyString());
        String uri = CREATE_REQUEST_A_DEAL_URI.replace("{toUserType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(requestADealCreateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_OFFER_TYPE_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_OFFER_TYPE_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_CreateRequestADealIsFailed() throws Exception {
        RequestADealCreateRequestDto requestADealCreateRequestDto = getSampleRequestADealCreateRequestDto();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        Brand brand = getSampleBrand();
        OfferType offerType = getSampleOfferType();
        Category category = getSampleCategory();
        when(categoryService.getCategoryById(anyString())).thenReturn(category);
        when(authUserService.getMerchantBusinessByMerchantId(anyString(), anyString())).thenReturn(businessMerchantResponseDto);
        when(brandService.getBrandById(anyString())).thenReturn(brand);
        when(offerTypeService.getOfferTypeById(anyString())).thenReturn(offerType);
        doThrow(new KupiCoreException("ERROR")).when(requestADealService).saveRequestADeal(any(RequestADeal.class), any(BusinessMerchantResponseDto.class), any(UserType.class));
        String uri = CREATE_REQUEST_A_DEAL_URI.replace("{toUserType}", UserType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(requestADealCreateRequestDto.toJson())
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
    void Should_ReturnOk_When_RequestADealPageNotEmptyForGetRequestDealsByUserId() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        Page<RequestADeal> requestADealPage = getSampleRequestADealPage();
        BulkUserRequestDto bulkUserRequestDto = getSampleBulkUserRequestDto();
        Map<String, MerchantBusinessResponseDto> merchantBusinessResponseDtoMap = getSampleMerchantBusinessResponseDtoMap();
        when(requestADealService.getRequestDealsByUserIdAndToUserType(pageable, MERCHANT_ID, UserType.MERCHANT))
                .thenReturn(requestADealPage);
        when(authUserService.getMerchantMap(bulkUserRequestDto, UserType.MERCHANT)).thenReturn(merchantBusinessResponseDtoMap);
        String uri = GET_REQUEST_DEALS_BY_USER_ID_URI.replace("{toUserType}", UserType.MERCHANT.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{mobileUserId}", MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_REQUEST_DEAL_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_REQUEST_DEAL_LIST.getCode()));
    }

    @Test
    void Should_ReturnOk_When_RequestADealPageIsEmptyForGetRequestDealsByUserId() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        List<RequestADeal> requestADealList = new ArrayList<>();
        Page<RequestADeal> emptyPage = new PageImpl<>(requestADealList);
        when(requestADealService.getRequestDealsByUserIdAndToUserType(pageable, MERCHANT_ID, UserType.MERCHANT))
                .thenReturn(emptyPage);
        String uri = GET_REQUEST_DEALS_BY_USER_ID_URI.replace("{toUserType}", UserType.MERCHANT.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{mobileUserId}", MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_REQUEST_DEAL_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_REQUEST_DEAL_LIST.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidMerchantIdIsProvidedForGetRequestDealsByUserId() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        doThrow(new InvalidUserException("ERROR")).when(requestADealService)
                .getRequestDealsByUserIdAndToUserType(pageable, MERCHANT_ID, UserType.MERCHANT);
        String uri = GET_REQUEST_DEALS_BY_USER_ID_URI.replace("{toUserType}", UserType.MERCHANT.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{mobileUserId}", MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
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
    void Should_ReturnInternalServerError_When_GetRequestDealsByUserIdIsFailed() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        doThrow(new KupiCoreException("ERROR")).when(requestADealService)
                .getRequestDealsByUserIdAndToUserType(pageable, MERCHANT_ID, UserType.MERCHANT);
        String uri = GET_REQUEST_DEALS_BY_USER_ID_URI.replace("{toUserType}", UserType.MERCHANT.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{mobileUserId}", MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
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
    void Should_ReturnOk_When_UserTypeIsMerchantAndBrandIdIsNullProvidedForGetSummaryForSingleRequestADealCombination() throws Exception {
        Category category = getSampleCategory();
        BusinessMerchantResponseDto merchant = getSampleBusinessMerchantResponseDto();
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID)).thenReturn(merchant);
        when(categoryService.getCategoryById(CATEGORY_ID)).thenReturn(category);
        when(requestADealService.getCountForCombinedRequestADeal(MERCHANT_ID, category, null, UserType.MERCHANT))
                .thenReturn(COMBINED_REQUEST_A_DEAL_COUNT);
        String uri = GET_SUMMARY_FOR_SINGLE_REQUEST_A_DEAL_COMBINATION_URL.replace("{toUserType}", UserType.MERCHANT.name())
                .replace("{merchantId}", MERCHANT_ID).replace("{categoryId}", CATEGORY_ID)
                .replace("{brandId}", "null");
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.REQUEST_A_DEAL_COMBINATION_SUMMARY.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.REQUEST_A_DEAL_COMBINATION_SUMMARY.getCode()));
    }

    @Test
    void Should_ReturnOk_When_UserTypeIsBankAndBrandIdIsNotNullProvidedForGetSummaryForSingleRequestADealCombination() throws Exception {
        Category category = getSampleCategory();
        Brand brand = getSampleBrand();
        BusinessMerchantResponseDto merchant = getSampleBusinessMerchantResponseDto();
        when(authUserService.getBankBusinessByBankId(ACCESS_TOKEN, MERCHANT_ID)).thenReturn(merchant);
        when(categoryService.getCategoryById(CATEGORY_ID)).thenReturn(category);
        when(brandService.getBrandById(BRAND_ID)).thenReturn(brand);
        when(requestADealService.getCountForCombinedRequestADeal(MERCHANT_ID, category, brand, UserType.MERCHANT))
                .thenReturn(COMBINED_REQUEST_A_DEAL_COUNT);
        String uri = GET_SUMMARY_FOR_SINGLE_REQUEST_A_DEAL_COMBINATION_URL.replace("{toUserType}", UserType.BANK.name())
                .replace("{merchantId}", MERCHANT_ID).replace("{categoryId}", CATEGORY_ID)
                .replace("{brandId}", BRAND_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.REQUEST_A_DEAL_COMBINATION_SUMMARY.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.REQUEST_A_DEAL_COMBINATION_SUMMARY.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidMerchantIdIsProvidedForGetSummaryForSingleRequestADealCombination() throws Exception {
        doThrow(new InvalidUserException("ERROR")).when(authUserService)
                .getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID);
        String uri = GET_SUMMARY_FOR_SINGLE_REQUEST_A_DEAL_COMBINATION_URL.replace("{toUserType}", UserType.MERCHANT.name())
                .replace("{merchantId}", MERCHANT_ID).replace("{categoryId}", CATEGORY_ID)
                .replace("{brandId}", BRAND_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
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
    void Should_ReturnBadRequest_When_InvalidCategoryIdIsProvidedForGetSummaryForSingleRequestADealCombination() throws Exception {
        doThrow(new InvalidCategoryException("ERROR")).when(categoryService)
                .getCategoryById(CATEGORY_ID);
        BusinessMerchantResponseDto merchant = getSampleBusinessMerchantResponseDto();
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID)).thenReturn(merchant);
        String uri = GET_SUMMARY_FOR_SINGLE_REQUEST_A_DEAL_COMBINATION_URL.replace("{toUserType}", UserType.MERCHANT.name())
                .replace("{merchantId}", MERCHANT_ID).replace("{categoryId}", CATEGORY_ID)
                .replace("{brandId}", BRAND_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
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
    void Should_ReturnReturnInternalServerError_When_InvalidBrandIdIsProvidedForGetSummaryForSingleRequestADealCombination() throws Exception {
        Category category = getSampleCategory();
        BusinessMerchantResponseDto merchant = getSampleBusinessMerchantResponseDto();
        when(authUserService.getBankBusinessByBankId(ACCESS_TOKEN, MERCHANT_ID)).thenReturn(merchant);
        when(categoryService.getCategoryById(CATEGORY_ID)).thenReturn(category);
        doThrow(new InvalidBrandException("ERROR")).when(brandService).getBrandById(BRAND_ID);
        String uri = GET_SUMMARY_FOR_SINGLE_REQUEST_A_DEAL_COMBINATION_URL.replace("{toUserType}", UserType.MERCHANT.name())
                .replace("{merchantId}", MERCHANT_ID).replace("{categoryId}", CATEGORY_ID)
                .replace("{brandId}", BRAND_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
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
    void Should_ReturnReturnInternalServerError_ForGetSummaryForSingleRequestADealCombinationIsFailed() throws Exception {
        Category category = getSampleCategory();
        Brand brand = getSampleBrand();
        BusinessMerchantResponseDto merchant = getSampleBusinessMerchantResponseDto();
        when(authUserService.getBankBusinessByBankId(ACCESS_TOKEN, MERCHANT_ID)).thenReturn(merchant);
        when(categoryService.getCategoryById(CATEGORY_ID)).thenReturn(category);
        when(brandService.getBrandById(BRAND_ID)).thenReturn(brand);
        doThrow(new KupiCoreException("ERROR")).when(requestADealService)
                .getCountForCombinedRequestADeal(MERCHANT_ID, category, brand, UserType.MERCHANT);
        String uri = GET_SUMMARY_FOR_SINGLE_REQUEST_A_DEAL_COMBINATION_URL.replace("{toUserType}", UserType.MERCHANT.name())
                .replace("{merchantId}", MERCHANT_ID).replace("{categoryId}", CATEGORY_ID)
                .replace("{brandId}", BRAND_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
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
    void Should_ReturnOk_When_UserTypeIsMerchantForGetRequestDealsById() throws Exception {
        RequestADeal requestADeal = getSampleRequestADeal();
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        when(requestADealService.getRequestADealById(REQUEST_A_DEAL_ID)).thenReturn(requestADeal);
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID))
                .thenReturn(businessMerchantResponseDto);
        String uri = GET_REQUEST_DEALS_BY_ID.replace("{requestADealId}", REQUEST_A_DEAL_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_REQUEST_DEAL_DETAIL.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.READ_REQUEST_DEAL_DETAIL.getCode()));
    }

    @Test
    void Should_ReturnOk_When_UserTypeIsBankForGetRequestDealsById() throws Exception {
        RequestADeal requestADeal = getSampleRequestADeal();
        requestADeal.setToUserType(UserType.BANK);
        BusinessMerchantResponseDto businessMerchantResponseDto = getSampleBusinessMerchantResponseDto();
        when(requestADealService.getRequestADealById(REQUEST_A_DEAL_ID)).thenReturn(requestADeal);
        when(authUserService.getBankBusinessByBankId(ACCESS_TOKEN, MERCHANT_ID)).thenReturn(businessMerchantResponseDto);
        String uri = GET_REQUEST_DEALS_BY_ID.replace("{requestADealId}", REQUEST_A_DEAL_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_REQUEST_DEAL_DETAIL.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.READ_REQUEST_DEAL_DETAIL.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidRequestADealIdIsProvidedForGetRequestDealsById() throws Exception {
        doThrow(new InvalidRequestADealException("ERROR"))
                .when(requestADealService).getRequestADealById(REQUEST_A_DEAL_ID);
        String uri = GET_REQUEST_DEALS_BY_ID.replace("{requestADealId}", REQUEST_A_DEAL_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_REQUEST_DEAL_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_REQUEST_DEAL_ID.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidMerchantIsProvidedForGetRequestDealsById() throws Exception {
        RequestADeal requestADeal = getSampleRequestADeal();
        when(requestADealService.getRequestADealById(REQUEST_A_DEAL_ID)).thenReturn(requestADeal);
        doThrow(new InvalidUserException("ERROR")).when(authUserService)
                .getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID);
        String uri = GET_REQUEST_DEALS_BY_ID.replace("{requestADealId}", REQUEST_A_DEAL_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
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
    void Should_ReturnInternalServerError_When_GetRequestDealsByIdIsFailed() throws Exception {
        doThrow(new KupiCoreException("ERROR")).when(requestADealService).getRequestADealById(REQUEST_A_DEAL_ID);
        String uri = GET_REQUEST_DEALS_BY_ID.replace("{requestADealId}", REQUEST_A_DEAL_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
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
    void Should_ReturnOk_When_UserTypeIsMerchantAndAllCombinedDealRequestsIsNotEmptyForGetAllRequestsDeals() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        Page<CombinedDealRequest> combinedDealRequestPage = getSampleCombinedDealRequestPage();
        when(requestADealService.getAllCombinedDealRequests(any(Pageable.class), anyString(), anyString(), any(UserType.class)))
                .thenReturn(combinedDealRequestPage);
        String uri = GET_ALL_REQUESTS_DEALS_URI.replace("{toUserType}", UserType.MERCHANT.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_REQUEST_A_DEAL.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.READ_REQUEST_A_DEAL.getCode()));
    }

    @Test
    void Should_ReturnOk_When_UserTypeIsBankAndAllCombinedDealRequestsIsEmptyForGetAllRequestsDeals() throws Exception {
        List<CombinedDealRequest> combinedDealRequestList = new ArrayList<>();
        when(requestADealService.getAllCombinedDealRequests(any(Pageable.class), anyString(), anyString(), any(UserType.class)))
                .thenReturn(new PageImpl<>(combinedDealRequestList));
        String uri = GET_ALL_REQUESTS_DEALS_URI.replace("{toUserType}", UserType.BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_REQUEST_A_DEAL.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.READ_REQUEST_A_DEAL.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidMerchantIdIsProvidedForGetAllRequestsDeals() throws Exception {
        doThrow(new InvalidUserException("ERROR")).when(authUserService)
                .getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID);
        String uri = GET_ALL_REQUESTS_DEALS_URI.replace("{toUserType}", UserType.MERCHANT.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
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
    void Should_ReturnInternalServerError_When_GetAllRequestsDealsIsFailed() throws Exception {
        doThrow(new KupiCoreException("ERROR")).when(requestADealService)
                .getAllCombinedDealRequests(any(Pageable.class), anyString(), anyString(), any(UserType.class));
        String uri = GET_ALL_REQUESTS_DEALS_URI.replace("{toUserType}", UserType.MERCHANT.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
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
    void Should_ReturnOk_When_UserTypeIsMerchantAndBrandIdIsNullForGetAllRequestsDealsDetails() throws Exception {
        BusinessMerchantResponseDto merchant = getSampleBusinessMerchantResponseDto();
        Category category = getSampleCategory();
        Page<RequestADeal> requestADealPage = getSampleRequestADealPage();
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID)).thenReturn(merchant);
        when(categoryService.getCategoryById(CATEGORY_ID)).thenReturn(category);
        when(requestADealService.getAllRequestADealDetail(any(Pageable.class), anyString(), anyString(), anyString(),
                any(UserType.class))).thenReturn(requestADealPage);
        String uri = GET_ALL_REQUESTS_DEALS_DETAILS_URI.replace("{toUserType}", UserType.MERCHANT.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{categoryId}", CATEGORY_ID)
                .replace("{brandId}", "null");
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_REQUEST_DEAL_DETAIL.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_REQUEST_DEAL_DETAIL.getCode()));
    }

    @Test
    void Should_ReturnOk_When_UserTypeIsBankAndBrandIdIsNotNullForGetAllRequestsDealsDetails() throws Exception {
        BusinessMerchantResponseDto merchant = getSampleBusinessMerchantResponseDto();
        Category category = getSampleCategory();
        Brand brand = getSampleBrand();
        List<RequestADeal> requestADealList = new ArrayList<>();
        when(authUserService.getBankBusinessByBankId(ACCESS_TOKEN, MERCHANT_ID)).thenReturn(merchant);
        when(categoryService.getCategoryById(CATEGORY_ID)).thenReturn(category);
        when(brandService.getBrandById(BRAND_ID)).thenReturn(brand);
        when(requestADealService.getAllRequestADealDetail(any(Pageable.class), anyString(), anyString(), anyString(),
                any(UserType.class))).thenReturn(new PageImpl<>(requestADealList));
        String uri = GET_ALL_REQUESTS_DEALS_DETAILS_URI.replace("{toUserType}", UserType.BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{categoryId}", CATEGORY_ID)
                .replace("{brandId}", BRAND_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_REQUEST_DEAL_DETAIL.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_REQUEST_DEAL_DETAIL.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidMerchantIdIsProvidedForGetAllRequestsDealsDetails() throws Exception {
        doThrow(new InvalidUserException("ERROR")).when(authUserService)
                .getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID);
        String uri = GET_ALL_REQUESTS_DEALS_DETAILS_URI.replace("{toUserType}", UserType.MERCHANT.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{categoryId}", CATEGORY_ID)
                .replace("{brandId}", BRAND_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidCategoryIdIsProvidedForGetAllRequestsDealsDetails() throws Exception {
        BusinessMerchantResponseDto merchant = getSampleBusinessMerchantResponseDto();
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID)).thenReturn(merchant);
        doThrow(new InvalidCategoryException("ERROR")).when(categoryService).getCategoryById(CATEGORY_ID);
        String uri = GET_ALL_REQUESTS_DEALS_DETAILS_URI.replace("{toUserType}", UserType.MERCHANT.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{categoryId}", CATEGORY_ID)
                .replace("{brandId}", BRAND_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_CATEGORY_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_CATEGORY_ID.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidBrandIdIsProvidedForGetAllRequestsDealsDetails() throws Exception {
        BusinessMerchantResponseDto merchant = getSampleBusinessMerchantResponseDto();
        Category category = getSampleCategory();
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID)).thenReturn(merchant);
        when(categoryService.getCategoryById(CATEGORY_ID)).thenReturn(category);
        doThrow(new InvalidBrandException("ERROR")).when(brandService).getBrandById(BRAND_ID);
        String uri = GET_ALL_REQUESTS_DEALS_DETAILS_URI.replace("{toUserType}", UserType.MERCHANT.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{categoryId}", CATEGORY_ID)
                .replace("{brandId}", BRAND_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_BRAND_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_BRAND_ID.getCode()));
    }

    @Test
    void Should_ReturnReturnInternalServerError_When_GetAllRequestsDealsDetailsIsFailed() throws Exception {
        Category category = getSampleCategory();
        Brand brand = getSampleBrand();
        BusinessMerchantResponseDto merchant = getSampleBusinessMerchantResponseDto();
        when(authUserService.getMerchantBusinessByMerchantId(ACCESS_TOKEN, MERCHANT_ID)).thenReturn(merchant);
        when(categoryService.getCategoryById(CATEGORY_ID)).thenReturn(category);
        when(brandService.getBrandById(BRAND_ID)).thenReturn(brand);
        doThrow(new KupiCoreException("ERROR")).when(requestADealService)
                .getAllRequestADealDetail(any(Pageable.class), anyString(), anyString(), anyString(), any(UserType.class));
        String uri = GET_ALL_REQUESTS_DEALS_DETAILS_URI.replace("{toUserType}", UserType.MERCHANT.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{categoryId}", CATEGORY_ID)
                .replace("{brandId}", BRAND_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    /**
     * This method creates sample RequestADealCreateRequestDto
     *
     * @return RequestADealCreateRequestDto
     */
    private RequestADealCreateRequestDto getSampleRequestADealCreateRequestDto() {
        RequestADealCreateRequestDto requestADealCreateRequestDto = new RequestADealCreateRequestDto();
        requestADealCreateRequestDto.setBrandId("bid-3bb83176-1361-44cb-a33d-9aba63266017");
        requestADealCreateRequestDto.setCategoryId("cid-f061574f-ce13-496f-bc21-01b9c7a40e24");
        requestADealCreateRequestDto.setMerchantId("uid-92b0983c-534e-401f-af15-5f8bcbcddebb");
        requestADealCreateRequestDto.setUserId("uid-ded6ca72-f64f-46c7-a36a-05893ca27c62");
        requestADealCreateRequestDto.setNote("Notes");
        requestADealCreateRequestDto.setOfferTypeId("otid-20503137-25a6-4e72-950f-4c8bad24b530");
        return requestADealCreateRequestDto;
    }

    /**
     * This method creates BusinessMerchantResponseDto
     *
     * @return BusinessMerchantResponseDto
     */
    private BusinessMerchantResponseDto getSampleBusinessMerchantResponseDto() {
        BusinessMerchantResponseDto businessMerchantResponseDto = new BusinessMerchantResponseDto();
        businessMerchantResponseDto.setBusinessId("uid-001");
        businessMerchantResponseDto.setBusinessName("Roy");
        businessMerchantResponseDto.setImageUrl("goo.le/image");
        businessMerchantResponseDto.setOwnerName("Roy Owner");
        businessMerchantResponseDto.setAddress("Colombo");
        businessMerchantResponseDto.setWebSite("goo.le/image2");
        businessMerchantResponseDto.setApprovalStatus(ApprovalStatus.APPROVED);
        businessMerchantResponseDto.setActive(true);
        return businessMerchantResponseDto;
    }

    /**
     * This method creates sample RequestADeal
     *
     * @return RequestADeal
     */
    private RequestADeal getSampleRequestADeal() {
        OfferType offerType = getSampleOfferType();
        Brand brand = getSampleBrand();
        Category category = getSampleCategory();
        RequestADeal requestADeal = new RequestADeal();
        requestADeal.setNote("Note");
        requestADeal.setToUserType(UserType.MERCHANT);
        requestADeal.setBrand(brand);
        requestADeal.setOfferType(offerType);
        requestADeal.setCategory(category);
        requestADeal.setCreatedAt(System.currentTimeMillis());
        requestADeal.setUpdatedAt(System.currentTimeMillis());
        requestADeal.setUserId(USER_ID);
        requestADeal.setMerchantId(MERCHANT_ID);
        requestADeal.setId(REQUEST_A_DEAL_ID);
        return requestADeal;
    }

    /**
     * This method creates sample OfferType
     *
     * @return OfferType
     */
    private OfferType getSampleOfferType() {
        OfferType offerType = new OfferType();
        offerType.setId(OFFER_TYPE_ID);
        offerType.setName(OFFER_TYPE_NAME);
        offerType.setUpdatedAt(System.currentTimeMillis());
        offerType.setCreatedAt(System.currentTimeMillis());
        offerType.setDeleted(false);
        return offerType;
    }

    /**
     * This method creates sample category
     *
     * @return category
     */
    private Category getSampleCategory() {
        Set<Category> relationalCategories = new HashSet<>();

        Category category = new Category();
        category.setId(CATEGORY_ID);
        category.setName("New Year");
        category.setDescription("snacks and sweets");
        category.setImageUrl("https://picsum.photos/200/300");
        category.setCategoryType(CategoryType.SEASONAL);
        category.setStartDate(1654021800000L);
        category.setExpiryDate(1670697000000L);
        category.setRelatedCategories(relationalCategories);
        category.setPopular(true);
        return category;
    }

    /**
     * This method creates sample brand
     *
     * @return Brand
     */
    private Brand getSampleBrand() {
        Brand brand = new Brand();
        brand.setId(BRAND_ID);
        brand.setName("Brand name");
        brand.setDescription("Brand description");
        brand.setImageUrl("https://objects-kupi-dev.s3.ap-southeast-1.amazonaws.com/fid-3338ebed-e3d3-4f86-8b3e-92887f27e51d.jpg");
        return brand;
    }

    /**
     * This method creates sample RequestADeal page
     *
     * @return RequestADealPage
     */
    private Page<RequestADeal> getSampleRequestADealPage() {
        RequestADeal requestADeal = getSampleRequestADeal();
        List<RequestADeal> requestADealList = new ArrayList<>();
        requestADealList.add(requestADeal);
        return new PageImpl<>(requestADealList);
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

    /**
     * This method creates sample CombinedDealRequest
     *
     * @return CombinedDealRequest
     */
    private CombinedDealRequest getSampleCombinedDealRequest() {
        CombinedDealRequest combinedDealRequest = new CombinedDealRequest();
        combinedDealRequest.setBrandId(BRAND_ID);
        combinedDealRequest.setCategoryId(CATEGORY_ID);
        combinedDealRequest.setMerchantId(MERCHANT_ID);
        combinedDealRequest.setCount(1L);
        combinedDealRequest.setBrandName("DELL");
        combinedDealRequest.setMerchantName("Merchant");
        return combinedDealRequest;
    }

    /**
     * This method creates sample CombinedDealRequest page
     *
     * @return CombinedDealRequestPage
     */
    private Page<CombinedDealRequest> getSampleCombinedDealRequestPage() {
        CombinedDealRequest combinedDealRequest = getSampleCombinedDealRequest();
        List<CombinedDealRequest> combinedDealRequestList = new ArrayList<>();
        combinedDealRequestList.add(combinedDealRequest);
        return new PageImpl<>(combinedDealRequestList);
    }
}