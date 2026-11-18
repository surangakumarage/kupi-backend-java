package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.ResourceBundleMessageSourceBean;
import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.*;
import com.kupi.cc.base.domain.request.BulkUserRequestDto;
import com.kupi.cc.base.domain.request.CategoryBrandMerchantRequestDto;
import com.kupi.cc.base.domain.response.BasicMerchantBusinessResponseDto;
import com.kupi.cc.base.domain.response.BusinessMerchantResponseDto;
import com.kupi.cc.base.domain.response.MerchantBusinessResponseDto;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.InvalidBrandException;
import com.kupi.cc.base.exception.InvalidCategoryException;
import com.kupi.cc.base.exception.InvalidUserException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests {@link MerchantController} class
 */
class MerchantControllerTest {

    private static final UserType USER_TYPE_BANK = UserType.BANK;
    private static final UserType USER_TYPE_MERCHANT = UserType.MERCHANT;
    private static final UserType USER_TYPE_ADMIN = UserType.ADMIN;
    private static final String USER_ID_HEADER = "User-Id";
    private static final String MERCHANT_ID = "uid-123456789";
    private static final String CATEGORY_ID = "cid-123456789";
    private static final String CATEGORY_BRAND_MERCHANT_ID = "cbmid-123456789";
    private static final String BRAND_ID = "bid-452136";
    private static final String BANK_MERCHANT_ID = "bms-123456789";
    private static final String BANK_ID = "uid-123456789";
    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String AUTH_TOKEN_HEADER = "Auth-Token";
    private static final String AUTH_TOKEN = "Bearer192f0c96-dd19-404e-8217-a8fd1cc81d55";
    private static final String ALL = "ALL";
    private static final String SEARCH_TERM = "Softlogic";
    private static final int PAGE = 0;
    private static final int SIZE = 1;
    private static final String DEFAULT_SORT = "updatedAt";
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String FAILED = "failed";
    private static final String APPLICATION_JSON = "application/json";

    private static final String UPDATE_CATEGORIES_AND_BRANDS_FOR_MERCHANT_URI = "/api/v1/{userType}";
    private static final String GET_CATEGORIES_AND_BRANDS_FOR_MERCHANT_URI = "/api/v1/{userType}/{merchantId}";
    private static final String GET_MERCHANT_OR_BANK_LIST_BY_CATEGORY_ID_URI = "/api/v1/{userType}/{page}/{size}/category/{categoryId}";
    private static final String GET_MERCHANT_OR_BANK_LIST_BY_CATEGORY_ID_WITH_SEARCH_TERM_URI = "/api/v1/{userType}/{page}/{size}/category/{categoryId}/search/{searchTerm}";
    private static final String GET_MERCHANT_LIST_BY_BRAND_ID_URI = "/api/v1/{userType}/{page}/{size}/brand/{brandId}/search/{searchTerm}";
    private static final String GET_ACTIVE_MERCHANT_LIST_BY_CATEGORY_ID_AND_SEARCH_TERM_URI = "/api/v1/{userType}/active/{page}/{size}/category/{categoryId}/search/{searchTerm}";
    private static final String GET_ACTIVE_MERCHANT_LIST_BY_CATEGORY_ID_URI = "/api/v1/{userType}/active/{page}/{size}/category/{categoryId}";
    private static final String GET_BRAND_LIST_BY_MERCHANT_URI = "/api/v1/{userType}/{merchantId}/brand/{page}/{size}/search/{searchTerm}";
    private static final String GET_ALL_ACTIVE_CATEGORY_LIST_BY_MERCHANT_URI = "/api/v1/{userType}/{merchantId}/active/category/{page}/{size}/search/{searchTerm}";
    private static final String GET_ALL_CATEGORY_LIST_BY_MERCHANT_URI = "/api/v1/{userType}/{merchantId}/category/{page}/{size}/search/{searchTerm}";
    private static final String GET_BANKS_LIST_BY_MERCHANT_ID_URI = "/api/v1/{userType}/{merchantId}/bank/{page}/{size}/search/{searchTerm}";
    private static final String GET_ACTIVE_BANKS_LIST_BY_MERCHANT_ID_URI = "/api/v1/{userType}/active/{merchantId}/bank/{page}/{size}/search/{searchTerm}";
    private static final String GET_MERCHANT_LIST_BY_BANK_ID_URI = "/api/v1/{userType}/{bankId}/merchant/{page}/{size}/search/{searchTerm}";
    private static final String GET_ACTIVE_MERCHANT_LIST_BY_BANK_ID_URI = "/api/v1/{userType}/active/{bankId}/merchant/{page}/{size}/search/{searchTerm}";

    private final CategoryBrandMerchantRequestDto categoryBrandMerchantRequestDto =
            getSampleCategoryBrandMerchantRequestDto();
    private final ResourceBundleMessageSourceBean resourceBundleMessageSourceBean = new ResourceBundleMessageSourceBean();
    Locale locale = LocaleContextHolder.getLocale();
    private MockMvc mockMvc;
    private ResourceBundleMessageSource resourceBundleMessageSource;
    @Mock
    private CategoryService categoryService;
    @Mock
    private BrandService brandService;
    @Mock
    private CategoryBrandMerchantService categoryBrandMerchantService;
    @Mock
    private CategoryBrandMerchantIndexService categoryBrandMerchantIndexService;
    @Mock
    private AuthUserService authUserService;
    @Mock
    private Translator translator;
    @Mock
    private MerchantBankSearchIndexService merchantBankSearchIndexService;
    @Mock
    private SummaryService summaryService;
    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        initMocks(this);
        MerchantController merchantController = new MerchantController(categoryService, brandService, categoryBrandMerchantService,
                categoryBrandMerchantIndexService, authUserService, translator, merchantBankSearchIndexService,
                summaryService);
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
        mockMvc = MockMvcBuilders.standaloneSetup(merchantController)
                .setValidator(noOpValidator)
                .build();
        this.resourceBundleMessageSource = resourceBundleMessageSourceBean.messageSource();
    }

    @AfterEach
    void tearDown() {
    }

    /**
     * Start of tests for update categories and brands for merchant
     * Api context: api/v1/{userType}
     */
    @Test
    void Should_ReturnOk_When_UpdatingCategoriesAndBrandsForMerchantIsSuccessful() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenReturn(new BusinessMerchantResponseDto());

        Set<Category> relatedCategories = new HashSet<>();
        Category category = new Category();
        category.setId(CATEGORY_ID);
        relatedCategories.add(category);
        when(categoryService.getCategorySetByIds(categoryBrandMerchantRequestDto.getCategories()))
                .thenReturn(relatedCategories);

        CategoryBrandMerchant categoryBrandMerchant = new CategoryBrandMerchant();
        categoryBrandMerchant.setMerchantId(MERCHANT_ID);
        when(categoryBrandMerchantService.updateCategoriesBrandsOfMerchant(any(CategoryBrandMerchant.class),
                any(BasicMerchantBusinessResponseDto.class))).thenReturn(categoryBrandMerchant);

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .UPDATE_CATEGORIES_BRANDS_FOR_MERCHANT.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.UPDATE_CATEGORIES_BRANDS_FOR_MERCHANT
                .getCode()))).thenReturn(successMessage);
        String uri = UPDATE_CATEGORIES_AND_BRANDS_FOR_MERCHANT_URI.replace("{userType}", USER_TYPE_BANK.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(categoryBrandMerchantRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.UPDATE_CATEGORIES_BRANDS_FOR_MERCHANT.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.UPDATE_CATEGORIES_BRANDS_FOR_MERCHANT.getCode()))
                .andExpect(jsonPath("$.data.categories[0].id").value(CATEGORY_ID))
                .andExpect(jsonPath("$.data.merchantId").value(MERCHANT_ID))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingCategoriesAndBrandsForMerchantForUnsupportedUserType()
            throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .UNSUPPORTED_USER_TYPE_ACTION.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode())))
                .thenReturn(invalidMessage);
        String uri = UPDATE_CATEGORIES_AND_BRANDS_FOR_MERCHANT_URI.replace("{userType}", USER_TYPE_ADMIN.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(categoryBrandMerchantRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingCategoriesAndBrandsForMerchantForMissingRequiredFields()
            throws Exception {
        categoryBrandMerchantRequestDto.setMerchantId(null);
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .MISSING_REQUIRED_FIELDS.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode())))
                .thenReturn(invalidMessage);
        String uri = UPDATE_CATEGORIES_AND_BRANDS_FOR_MERCHANT_URI.replace("{userType}", USER_TYPE_BANK.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(categoryBrandMerchantRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingCategoriesAndBrandsForMerchantForInvalidMerchantId()
            throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenThrow(new InvalidUserException(FAILED));
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_MERCHANT_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode())))
                .thenReturn(invalidMessage);
        String uri = UPDATE_CATEGORIES_AND_BRANDS_FOR_MERCHANT_URI.replace("{userType}", USER_TYPE_BANK.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(categoryBrandMerchantRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingCategoriesAndBrandsForMerchantForInvalidCategoryId()
            throws Exception {
        when(categoryService.getCategorySetByIds(categoryBrandMerchantRequestDto.getCategories()))
                .thenThrow(new InvalidCategoryException(FAILED));
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_CATEGORY_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_CATEGORY_ID.getCode())))
                .thenReturn(invalidMessage);
        String uri = UPDATE_CATEGORIES_AND_BRANDS_FOR_MERCHANT_URI.replace("{userType}", USER_TYPE_BANK.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(categoryBrandMerchantRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_CATEGORY_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_CATEGORY_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingCategoriesAndBrandsForMerchantInvalidBrandId() throws Exception {
        List<String> brands = new ArrayList<>();
        brands.add("bid-123");
        categoryBrandMerchantRequestDto.setBrands(brands);
        when(brandService.getBrandSetByIdList(categoryBrandMerchantRequestDto.getBrands()))
                .thenThrow(new InvalidBrandException(FAILED));
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_BRAND_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_BRAND_ID.getCode())))
                .thenReturn(invalidMessage);
        String uri = UPDATE_CATEGORIES_AND_BRANDS_FOR_MERCHANT_URI.replace("{userType}", USER_TYPE_BANK.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(categoryBrandMerchantRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_BRAND_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_BRAND_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_UpdatingCategoriesAndBrandsForMerchantIsFailed() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenThrow(new KupiCoreException(FAILED));
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = UPDATE_CATEGORIES_AND_BRANDS_FOR_MERCHANT_URI.replace("{userType}", USER_TYPE_BANK.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(categoryBrandMerchantRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for get categories and brands for merchant
     * Api context: api/v1/{userType}/{merchantId}
     */
    @Test
    void Should_ReturnOk_When_GettingCategoriesAndBrandsForMerchantIsSuccessful() throws Exception {
        CategoryBrandMerchant categoryBrandMerchant = new CategoryBrandMerchant();
        categoryBrandMerchant.setId(CATEGORY_BRAND_MERCHANT_ID);
        Set<Category> categorySet = new HashSet<>();
        Category category = new Category();
        category.setId(CATEGORY_ID);
        categorySet.add(category);
        Set<Brand> brandSet = new HashSet<>();
        Brand brand = new Brand();
        brand.setId(BRAND_ID);
        brandSet.add(brand);

        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenReturn(new BusinessMerchantResponseDto());
        when(categoryBrandMerchantService.getBrandsCategoryMapperByMerchantId(anyString()))
                .thenReturn(Optional.of(categoryBrandMerchant));
        when(categoryService.getCategorySetByIds(anyList())).thenReturn(categorySet);
        when(brandService.getBrandSetByIdList(anyList())).thenReturn(brandSet);

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .READ_CATEGORIES_BRANDS_FOR_MERCHANT.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.READ_CATEGORIES_BRANDS_FOR_MERCHANT
                .getCode()))).thenReturn(successMessage);
        String uri = GET_CATEGORIES_AND_BRANDS_FOR_MERCHANT_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{merchantId", MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_CATEGORIES_BRANDS_FOR_MERCHANT.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.READ_CATEGORIES_BRANDS_FOR_MERCHANT.getCode()))
                .andExpect(jsonPath("$.data.id").value(CATEGORY_BRAND_MERCHANT_ID))
                .andExpect(jsonPath("$.data.categories[0].id").value(CATEGORY_ID))
                .andExpect(jsonPath("$.data.brands[0].id").value(BRAND_ID))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingCategoriesAndBrandsForMerchantForUnsupportedUserType() throws
            Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .UNSUPPORTED_USER_TYPE_ACTION.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_CATEGORIES_AND_BRANDS_FOR_MERCHANT_URI.replace("{userType}", USER_TYPE_ADMIN.name())
                .replace("{merchantId", MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingCategoriesAndBrandsForMerchantForInvalidMerchantId() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenThrow(new InvalidUserException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_MERCHANT_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_MERCHANT_ID
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_CATEGORIES_AND_BRANDS_FOR_MERCHANT_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{merchantId", MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingCategoriesAndBrandsForMerchantIsFailed() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_CATEGORIES_AND_BRANDS_FOR_MERCHANT_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{merchantId", MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for get merchant or bank list by categoryId
     * Api context: api/v1/{userType}/{page}/{size}/category/{categoryId}
     */
    @Test
    void Should_ReturnOk_When_GettingMerchantOrBankListByCategoryIdIsSuccessful() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        List<String> merchantIdList = new ArrayList<>();
        merchantIdList.add(MERCHANT_ID);
        MerchantBusinessResponseDto merchantBusinessResponseDto = new MerchantBusinessResponseDto();
        merchantBusinessResponseDto.setId(MERCHANT_ID);
        Map<String, MerchantBusinessResponseDto> merchantResponseDtoHashMap = new HashMap<>();
        merchantResponseDtoHashMap.put(merchantBusinessResponseDto.getId(), merchantBusinessResponseDto);

        when(categoryService.getCategoryById(anyString())).thenReturn(new Category());
        when(categoryBrandMerchantService.findAllMerchantsByCategoryId(eq(pageable), anyString(), anyBoolean()))
                .thenReturn(merchantIdList);
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(merchantResponseDtoHashMap);

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .READ_MERCHANT_LIST.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.READ_MERCHANT_LIST
                .getCode()))).thenReturn(successMessage);
        String uri = GET_MERCHANT_OR_BANK_LIST_BY_CATEGORY_ID_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{categoryId}", CATEGORY_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_MERCHANT_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.READ_MERCHANT_LIST.getCode()))
                .andExpect(jsonPath("$.data.merchants[0].id").value(MERCHANT_ID))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingMerchantOrBankListByCategoryIdForInvalidCategoryId() throws Exception {
        when(categoryService.getCategoryById(anyString())).thenThrow(new InvalidCategoryException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_CATEGORY_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_CATEGORY_ID
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_MERCHANT_OR_BANK_LIST_BY_CATEGORY_ID_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{categoryId}", CATEGORY_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_CATEGORY_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_CATEGORY_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingMerchantOrBankListByCategoryIdIsFailed() throws Exception {
        when(categoryService.getCategoryById(anyString())).thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_MERCHANT_OR_BANK_LIST_BY_CATEGORY_ID_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{categoryId}", CATEGORY_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for get merchant or bank list by categoryId with search term
     * Api context: api/v1/{userType}/{page}/{size}/category/{categoryId}/search/{searchTerm}
     */
    @Test
    void Should_ReturnOk_When_GettingMerchantOrBankListByCategoryIdWithSearchTermIsSuccessful() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        List<CategoryBrandMerchantIndex> categoryBrandMerchantIndexList = new ArrayList<>();
        categoryBrandMerchantIndexList.add(new CategoryBrandMerchantIndex());
        Page<CategoryBrandMerchantIndex> categoryBrandMerchantIndexPage = new PageImpl<>(categoryBrandMerchantIndexList);
        Map<String, MerchantBusinessResponseDto> merchantResponseDtoHashMap = new HashMap<>();
        MerchantBusinessResponseDto merchantBusinessResponseDto = new MerchantBusinessResponseDto();
        merchantBusinessResponseDto.setId(MERCHANT_ID);
        merchantResponseDtoHashMap.put(merchantBusinessResponseDto.getId(), merchantBusinessResponseDto);

        doNothing().when(categoryService).validateCategoryId(CATEGORY_ID);
        when(categoryBrandMerchantIndexService.getCategoryBrandMerchantIndex(eq(pageable), anyString(), anyString(),
                anyBoolean(), any(UserType.class))).thenReturn(categoryBrandMerchantIndexPage);
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(merchantResponseDtoHashMap);

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .READ_MERCHANT_LIST.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.READ_MERCHANT_LIST
                .getCode()))).thenReturn(successMessage);
        String uri = GET_MERCHANT_OR_BANK_LIST_BY_CATEGORY_ID_WITH_SEARCH_TERM_URI.replace("{userType}",
                        USER_TYPE_BANK.name()).replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{categoryId}", CATEGORY_ID)
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_MERCHANT_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.READ_MERCHANT_LIST.getCode()))
                .andExpect(jsonPath("$.data.merchants[0].id").value(MERCHANT_ID))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingMerchantOrBankListByCategoryIdWithSearchTermForUnsupportedUserType() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .UNSUPPORTED_USER_TYPE_ACTION.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_MERCHANT_OR_BANK_LIST_BY_CATEGORY_ID_WITH_SEARCH_TERM_URI.replace("{userType}",
                        USER_TYPE_ADMIN.name()).replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{categoryId}", CATEGORY_ID)
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingMerchantOrBankListByCategoryIdWithSearchTermForInvalidCategoryId() throws Exception {
        doThrow(new InvalidCategoryException(FAILED)).when(categoryService).validateCategoryId(CATEGORY_ID);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_CATEGORY_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_CATEGORY_ID.getCode())))
                .thenReturn(invalidMessage);
        String uri = GET_MERCHANT_OR_BANK_LIST_BY_CATEGORY_ID_WITH_SEARCH_TERM_URI.replace("{userType}",
                        USER_TYPE_BANK.name()).replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{categoryId}", CATEGORY_ID)
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_CATEGORY_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_CATEGORY_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingMerchantOrBankListByCategoryIdWithSearchTermIsFailed() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        doNothing().when(categoryService).validateCategoryId(CATEGORY_ID);
        when(categoryBrandMerchantIndexService.getCategoryBrandMerchantIndex(eq(pageable), anyString(), anyString(),
                anyBoolean(), any(UserType.class))).thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = GET_MERCHANT_OR_BANK_LIST_BY_CATEGORY_ID_WITH_SEARCH_TERM_URI.replace("{userType}",
                        USER_TYPE_BANK.name()).replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{categoryId}", CATEGORY_ID)
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for get merchant list by brandId
     * Api context: api/v1/{userType}/{page}/{size}/brand/{brandId}/search/{searchTerm}
     */
    @Test
    void Should_ReturnOk_When_GettingMerchantListByBrandIdIsSuccessful() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        List<CategoryBrandMerchantIndex> categoryBrandMerchantIndexList = new ArrayList<>();
        categoryBrandMerchantIndexList.add(new CategoryBrandMerchantIndex());
        Page<CategoryBrandMerchantIndex> categoryBrandMerchantIndexPage = new PageImpl<>(categoryBrandMerchantIndexList);
        MerchantBusinessResponseDto merchantBusinessResponseDto = new MerchantBusinessResponseDto();
        merchantBusinessResponseDto.setId(MERCHANT_ID);
        Map<String, MerchantBusinessResponseDto> merchantResponseDtoHashMap = new HashMap<>();
        merchantResponseDtoHashMap.put(merchantBusinessResponseDto.getId(), merchantBusinessResponseDto);

        doNothing().when(brandService).validateBrandId(BRAND_ID);
        when(categoryBrandMerchantIndexService.getCategoryBrandMerchantIndexPageByBrandId(eq(pageable), anyString(),
                anyString(), any(UserType.class))).thenReturn(categoryBrandMerchantIndexPage);
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(merchantResponseDtoHashMap);

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .READ_MERCHANT_LIST.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.READ_MERCHANT_LIST
                .getCode()))).thenReturn(successMessage);
        String uri = GET_MERCHANT_LIST_BY_BRAND_ID_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{brandId}", BRAND_ID).replace("{searchTerm}", ALL);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_MERCHANT_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.READ_MERCHANT_LIST.getCode()))
                .andExpect(jsonPath("$.data.merchants[0].id").value(MERCHANT_ID))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingMerchantListByBrandIdForUnsupportedUserType() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .UNSUPPORTED_USER_TYPE_ACTION.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_MERCHANT_LIST_BY_BRAND_ID_URI.replace("{userType}", USER_TYPE_ADMIN.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{brandId}", BRAND_ID).replace("{searchTerm}", ALL);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingMerchantListByBrandIdForInvalidBrandId() throws Exception {
        doThrow(new InvalidBrandException(FAILED)).when(brandService).validateBrandId(BRAND_ID);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_BRAND_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_BRAND_ID
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_MERCHANT_LIST_BY_BRAND_ID_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{brandId}", BRAND_ID).replace("{searchTerm}", ALL);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_BRAND_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_BRAND_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingMerchantListByBrandIdIsFailed() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        doNothing().when(brandService).validateBrandId(BRAND_ID);
        when(categoryBrandMerchantIndexService.getCategoryBrandMerchantIndexPageByBrandId(eq(pageable), anyString(),
                anyString(), any(UserType.class))).thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_MERCHANT_LIST_BY_BRAND_ID_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{brandId}", BRAND_ID).replace("{searchTerm}", ALL);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for get active merchant list by category id and search term
     * Api context: api/v1/{userType}/active/{page}/{size}/category/{categoryId}/search/{searchTerm}
     */
    @Test
    void Should_ReturnOk_When_GettingActiveMerchantListByCategoryIdAndSearchTermIsSuccessful() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        doNothing().when(categoryService).validateCategoryId(CATEGORY_ID);
        when(categoryBrandMerchantIndexService.getCategoryBrandMerchantIndex(pageable, CATEGORY_ID, SEARCH_TERM,
                true, USER_TYPE_BANK)).thenReturn(getSampleCategoryBrandMerchantIndexPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .READ_MERCHANT_LIST.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.READ_MERCHANT_LIST
                .getCode()))).thenReturn(successMessage);
        String uri = GET_ACTIVE_MERCHANT_LIST_BY_CATEGORY_ID_AND_SEARCH_TERM_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{categoryId}", CATEGORY_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_MERCHANT_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.READ_MERCHANT_LIST.getCode()))
                .andExpect(jsonPath("$.data.merchants[0].id").value(MERCHANT_ID))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingActiveMerchantListByCategoryIdAndSearchTermForUnsupportedUserType() throws
            Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .UNSUPPORTED_USER_TYPE_ACTION.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_ACTIVE_MERCHANT_LIST_BY_CATEGORY_ID_AND_SEARCH_TERM_URI.replace("{userType}",
                        USER_TYPE_ADMIN.name()).replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{categoryId}", CATEGORY_ID)
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingActiveMerchantListByCategoryIdAndSearchTermForInvalidCategoryId() throws
            Exception {
        doThrow(new InvalidCategoryException(FAILED)).when(categoryService).validateCategoryId(CATEGORY_ID);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_CATEGORY_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_CATEGORY_ID
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_ACTIVE_MERCHANT_LIST_BY_CATEGORY_ID_AND_SEARCH_TERM_URI.replace("{userType}",
                        USER_TYPE_BANK.name()).replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{categoryId}", CATEGORY_ID)
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_CATEGORY_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_CATEGORY_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingActiveMerchantListByCategoryIdAndSearchTermIsFailed() throws
            Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        doNothing().when(categoryService).validateCategoryId(CATEGORY_ID);
        when(categoryBrandMerchantIndexService.getCategoryBrandMerchantIndex(pageable, CATEGORY_ID, SEARCH_TERM,
                true, USER_TYPE_BANK)).thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_ACTIVE_MERCHANT_LIST_BY_CATEGORY_ID_AND_SEARCH_TERM_URI.replace("{userType}",
                        USER_TYPE_BANK.name()).replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{categoryId}", CATEGORY_ID)
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for get active merchant list by categoryId
     * Api context: api/v1/{userType}/active/{page}/{size}/category/{categoryId}
     */
    @Test
    void Should_ReturnOk_When_GettingActiveMerchantListByCategoryIdIsSuccessful() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        List<String> merchantIdList = new ArrayList<>();
        merchantIdList.add(MERCHANT_ID);
        MerchantBusinessResponseDto merchantBusinessResponseDto = new MerchantBusinessResponseDto();
        merchantBusinessResponseDto.setId(MERCHANT_ID);
        Map<String, MerchantBusinessResponseDto> merchantResponseDtoHashMap = new HashMap<>();
        merchantResponseDtoHashMap.put(merchantBusinessResponseDto.getId(), merchantBusinessResponseDto);

        when(categoryService.getCategoryById(anyString())).thenReturn(new Category());
        when(categoryBrandMerchantService.findAllMerchantsByCategoryId(eq(pageable), anyString(), anyBoolean()))
                .thenReturn(merchantIdList);
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(merchantResponseDtoHashMap);

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .READ_MERCHANT_LIST.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.READ_MERCHANT_LIST
                .getCode()))).thenReturn(successMessage);
        String uri = GET_ACTIVE_MERCHANT_LIST_BY_CATEGORY_ID_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{categoryId}", CATEGORY_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_MERCHANT_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.READ_MERCHANT_LIST.getCode()))
                .andExpect(jsonPath("$.data.merchants[0].id").value(MERCHANT_ID))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingActiveMerchantListByCategoryIdForInvalidCategoryId() throws Exception {
        when(categoryService.getCategoryById(anyString())).thenThrow(new InvalidCategoryException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_CATEGORY_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_CATEGORY_ID
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_ACTIVE_MERCHANT_LIST_BY_CATEGORY_ID_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{categoryId}", CATEGORY_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_CATEGORY_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_CATEGORY_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingActiveMerchantListByCategoryIdIsFailed() throws Exception {
        when(categoryService.getCategoryById(anyString())).thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_ACTIVE_MERCHANT_LIST_BY_CATEGORY_ID_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{categoryId}", CATEGORY_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for get all active category list by merchant
     * Api context: api/v1/{userType}/{merchantId}/active/category/{page}/{size}/search/{searchTerm}
     */
    @Test
    void Should_ReturnOk_When_GettingAllActiveCategoryListByMerchantIsSuccessful() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenReturn(new BusinessMerchantResponseDto());
        when(categoryBrandMerchantService.getBrandsCategoryMapperByMerchantId(anyString()))
                .thenReturn(Optional.of(new CategoryBrandMerchant()));
        when(categoryBrandMerchantService.getActiveCategoriesByIdsAndSearchTerm(eq(pageable), anySet(), anyString(),
                anyBoolean())).thenReturn(getSampleCategoryPage());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .READ_CATEGORIES_FOR_MERCHANT.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.READ_CATEGORIES_FOR_MERCHANT
                .getCode()))).thenReturn(successMessage);
        String uri = GET_ALL_ACTIVE_CATEGORY_LIST_BY_MERCHANT_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_CATEGORIES_FOR_MERCHANT.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.READ_CATEGORIES_FOR_MERCHANT.getCode()))
                .andExpect(jsonPath("$.data.categories[0].id").value(CATEGORY_ID))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingAllActiveCategoryListByMerchantForUnsupportedUserType() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .UNSUPPORTED_USER_TYPE_ACTION.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_ALL_ACTIVE_CATEGORY_LIST_BY_MERCHANT_URI.replace("{userType}", USER_TYPE_ADMIN.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingAllActiveCategoryListByMerchantForInvalidMerchantId() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenThrow(new InvalidUserException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_MERCHANT_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_MERCHANT_ID
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_ALL_ACTIVE_CATEGORY_LIST_BY_MERCHANT_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingAllActiveCategoryListByMerchantIsFailed() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_ALL_ACTIVE_CATEGORY_LIST_BY_MERCHANT_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for get all category list by merchant
     * Api context: api/v1/{userType}/{merchantId}/category/{page}/{size}/search/{searchTerm}
     */
    @Test
    void Should_ReturnOk_When_GettingAllCategoryListByMerchantIsSuccessful() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());

        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenReturn(new BusinessMerchantResponseDto());
        when(categoryBrandMerchantService.getBrandsCategoryMapperByMerchantId(anyString()))
                .thenReturn(Optional.of(new CategoryBrandMerchant()));
        when(categoryBrandMerchantService.getActiveCategoriesByIdsAndSearchTerm(eq(pageable), anySet(), anyString(),
                anyBoolean())).thenReturn(getSampleCategoryPage());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .READ_CATEGORIES_FOR_MERCHANT.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.READ_CATEGORIES_FOR_MERCHANT
                .getCode()))).thenReturn(successMessage);
        String uri = GET_ALL_CATEGORY_LIST_BY_MERCHANT_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_CATEGORIES_FOR_MERCHANT.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.READ_CATEGORIES_FOR_MERCHANT.getCode()))
                .andExpect(jsonPath("$.data.categories[0].id").value(CATEGORY_ID))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingAllCategoryListByMerchantForUnsupportedUserType() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .UNSUPPORTED_USER_TYPE_ACTION.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_ALL_CATEGORY_LIST_BY_MERCHANT_URI.replace("{userType}", USER_TYPE_ADMIN.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingAllCategoryListByMerchantForInvalidMerchantId() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenThrow(new InvalidUserException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_MERCHANT_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_MERCHANT_ID
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_ALL_CATEGORY_LIST_BY_MERCHANT_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingAllCategoryListByMerchantIsFailed() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_ALL_CATEGORY_LIST_BY_MERCHANT_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for get brand list by merchant
     * Api context: api/v1/{userType}/{merchantId}/brand/{page}/{size}/search/{searchTerm}
     */
    @Test
    void Should_ReturnOk_When_GettingBrandListByMerchantIsSuccessful() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenReturn(new BusinessMerchantResponseDto());
        when(categoryBrandMerchantService.getBrandsCategoryMapperByMerchantId(anyString()))
                .thenReturn(Optional.of(new CategoryBrandMerchant()));
        when(categoryBrandMerchantService.getBrandsByIds(any(Pageable.class), anySet(), anyString()))
                .thenReturn(getSampleBrandPage());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .READ_BRAND_FOR_MERCHANT.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.READ_BRAND_FOR_MERCHANT
                .getCode()))).thenReturn(successMessage);
        String uri = GET_BRAND_LIST_BY_MERCHANT_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_BRAND_FOR_MERCHANT.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.READ_BRAND_FOR_MERCHANT.getCode()))
                .andExpect(jsonPath("$.data.brands[0].id").value(BRAND_ID))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingBrandListByMerchantForUnsupportedUserType() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .UNSUPPORTED_USER_TYPE_ACTION.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_BRAND_LIST_BY_MERCHANT_URI.replace("{userType}", USER_TYPE_ADMIN.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE_ACTION.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingBrandListByMerchantForInvalidMerchantId() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenThrow(new InvalidUserException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_MERCHANT_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_MERCHANT_ID
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_BRAND_LIST_BY_MERCHANT_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingBrandListByMerchantIsFailed() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_BRAND_LIST_BY_MERCHANT_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for get banks list by merchantId
     * Api context: api/v1/{userType}/{merchantId}/bank/{page}/{size}/search/{searchTerm}
     */
    @Test
    void Should_ReturnOk_When_GettingBanksListByMerchantIdIsSuccessful() throws Exception {
        when(authUserService.getMerchantBusinessByMerchantId(anyString(), anyString()))
                .thenReturn(new BusinessMerchantResponseDto());
        when(merchantBankSearchIndexService.getBankMerchantSearchIndexByMerchantIdAndSearchTerm(any(Pageable.class),
                anyString(), anyString(), anyBoolean())).thenReturn(getSampleBankMerchantSearchIndexPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .BANK_LIST.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.BANK_LIST.getCode())))
                .thenReturn(successMessage);
        String uri = GET_BANKS_LIST_BY_MERCHANT_ID_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.BANK_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.BANK_LIST.getCode()))
                .andExpect(jsonPath("$.data.merchants[0].id").value(MERCHANT_ID))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingBanksListByMerchantIdForInvalidMerchantId() throws Exception {
        when(authUserService.getMerchantBusinessByMerchantId(anyString(), anyString()))
                .thenThrow(new InvalidUserException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_MERCHANT_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode())))
                .thenReturn(invalidMessage);
        String uri = GET_BANKS_LIST_BY_MERCHANT_ID_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingBanksListByMerchantIdIsFailed() throws Exception {
        when(authUserService.getMerchantBusinessByMerchantId(anyString(), anyString()))
                .thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = GET_BANKS_LIST_BY_MERCHANT_ID_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for get active banks list by merchantId
     * Api context: api/v1/{userType}/active/{merchantId}/bank/{page}/{size}/search/{searchTerm}
     */
    @Test
    void Should_ReturnOk_When_GettingActiveBanksListByMerchantIdIsSuccessful() throws Exception {
        when(authUserService.getMerchantBusinessByMerchantId(anyString(), anyString()))
                .thenReturn(new BusinessMerchantResponseDto());
        when(merchantBankSearchIndexService.getBankMerchantSearchIndexByMerchantIdAndSearchTerm(any(Pageable.class),
                anyString(), anyString(), anyBoolean())).thenReturn(getSampleBankMerchantSearchIndexPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .BANK_LIST.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.BANK_LIST.getCode())))
                .thenReturn(successMessage);
        String uri = GET_ACTIVE_BANKS_LIST_BY_MERCHANT_ID_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.BANK_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.BANK_LIST.getCode()))
                .andExpect(jsonPath("$.data.merchants[0].id").value(MERCHANT_ID))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingActiveBanksListByMerchantIdForInvalidMerchantId() throws Exception {
        when(authUserService.getMerchantBusinessByMerchantId(anyString(), anyString()))
                .thenThrow(new InvalidUserException(FAILED));

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_MERCHANT_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode())))
                .thenReturn(successMessage);
        String uri = GET_ACTIVE_BANKS_LIST_BY_MERCHANT_ID_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingActiveBanksListByMerchantIdIsFailed() throws Exception {
        when(authUserService.getMerchantBusinessByMerchantId(anyString(), anyString()))
                .thenThrow(new KupiCoreException(FAILED));

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(successMessage);
        String uri = GET_ACTIVE_BANKS_LIST_BY_MERCHANT_ID_URI.replace("{userType}", USER_TYPE_BANK.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{merchantId}", MERCHANT_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    /**
     * Start of tests for get merchant list by bankId
     * Api context: api/v1/{userType}/{bankId}/merchant/{page}/{size}/search/{searchTerm}
     */
    @Test
    void Should_ReturnOk_When_GettingMerchantListByBankIdIsSuccessful() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenReturn(new BusinessMerchantResponseDto());
        when(merchantBankSearchIndexService.getBankDealSearchIndexByBankIdAndSearchTerm(any(Pageable.class), anyString()
                , anyString(), anyBoolean())).thenReturn(getSampleBankMerchantSearchIndexPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .READ_MERCHANT_LIST.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.READ_MERCHANT_LIST
                .getCode()))).thenReturn(successMessage);
        String uri = GET_MERCHANT_LIST_BY_BANK_ID_URI.replace("{userType}", USER_TYPE_MERCHANT.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{bankId}", BANK_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_MERCHANT_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.READ_MERCHANT_LIST.getCode()))
                .andExpect(jsonPath("$.data.merchants[0].id").value(MERCHANT_ID))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingMerchantListByBankIdForInvalidMerchantId() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenThrow(new InvalidUserException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_MERCHANT_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_MERCHANT_ID
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_MERCHANT_LIST_BY_BANK_ID_URI.replace("{userType}", USER_TYPE_MERCHANT.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{bankId}", BANK_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_MERCHANT_ID
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_MERCHANT_ID
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingMerchantListByBankIdIsFailed() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_MERCHANT_LIST_BY_BANK_ID_URI.replace("{userType}", USER_TYPE_MERCHANT.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{bankId}", BANK_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for get active merchant list by bankId
     * Api context: api/v1/{userType}/active/{bankId}/merchant/{page}/{size}/search/{searchTerm}
     */
    @Test
    void Should_ReturnOk_When_GettingActiveMerchantListByBankIdIsSuccessful() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenReturn(new BusinessMerchantResponseDto());
        when(merchantBankSearchIndexService.getBankDealSearchIndexByBankIdAndSearchTerm(any(Pageable.class), anyString()
                , anyString(), anyBoolean())).thenReturn(getSampleBankMerchantSearchIndexPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .READ_MERCHANT_LIST.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.READ_MERCHANT_LIST
                .getCode()))).thenReturn(successMessage);
        String uri = GET_ACTIVE_MERCHANT_LIST_BY_BANK_ID_URI.replace("{userType}", USER_TYPE_MERCHANT.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{bankId}", BANK_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_MERCHANT_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.READ_MERCHANT_LIST.getCode()))
                .andExpect(jsonPath("$.data.merchants[0].id").value(MERCHANT_ID))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingActiveMerchantListByBankIdForInvalidMerchantId() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenThrow(new InvalidUserException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_MERCHANT_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_MERCHANT_ID
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_ACTIVE_MERCHANT_LIST_BY_BANK_ID_URI.replace("{userType}", USER_TYPE_MERCHANT.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{bankId}", BANK_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_MERCHANT_ID
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_MERCHANT_ID
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingActiveMerchantListByBankIdIsFailed() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_ACTIVE_MERCHANT_LIST_BY_BANK_ID_URI.replace("{userType}", USER_TYPE_MERCHANT.name())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{bankId}", BANK_ID).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, MERCHANT_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    private CategoryBrandMerchantRequestDto getSampleCategoryBrandMerchantRequestDto() {
        List<String> categories = new ArrayList<>();
        categories.add(CATEGORY_ID);
        CategoryBrandMerchantRequestDto requestDto = new CategoryBrandMerchantRequestDto();
        requestDto.setMerchantId(MERCHANT_ID);
        requestDto.setCategories(categories);
        return requestDto;
    }

    private Page<CategoryBrandMerchantIndex> getSampleCategoryBrandMerchantIndexPage() {
        return new PageImpl<>(getSampleCategoryBrandMerchantIndexList());
    }

    private List<CategoryBrandMerchantIndex> getSampleCategoryBrandMerchantIndexList() {
        List<CategoryBrandMerchantIndex> categoryBrandMerchantIndexList = new ArrayList<>();
        categoryBrandMerchantIndexList.add(getSampleCategoryBrandMerchantIndex());
        return categoryBrandMerchantIndexList;
    }

    private CategoryBrandMerchantIndex getSampleCategoryBrandMerchantIndex() {
        CategoryBrandMerchantIndex categoryBrandMerchantIndex = new CategoryBrandMerchantIndex();
        categoryBrandMerchantIndex.setId(CATEGORY_BRAND_MERCHANT_ID);
        return categoryBrandMerchantIndex;
    }

    private Map<String, MerchantBusinessResponseDto> getSampleMerchantBusinessResponseDtoMap() {
        MerchantBusinessResponseDto dto = getSampleMerchantBusinessResponseDto();
        Map<String, MerchantBusinessResponseDto> merchantBusinessResponseDtoMap = new HashMap<>();
        merchantBusinessResponseDtoMap.put(dto.getId(), dto);
        return merchantBusinessResponseDtoMap;
    }

    private MerchantBusinessResponseDto getSampleMerchantBusinessResponseDto() {
        MerchantBusinessResponseDto merchantBusinessResponseDto = new MerchantBusinessResponseDto();
        merchantBusinessResponseDto.setId(MERCHANT_ID);
        return merchantBusinessResponseDto;
    }

    private Page<Category> getSampleCategoryPage() {
        return new PageImpl<>(getSampleCategoryList());
    }

    private List<Category> getSampleCategoryList() {
        List<Category> categoryList = new ArrayList<>();
        categoryList.add(getSampleCategory());
        return categoryList;
    }

    private Category getSampleCategory() {
        Category category = new Category();
        category.setId(CATEGORY_ID);
        return category;
    }

    private Page<Brand> getSampleBrandPage() {
        Brand brand = new Brand();
        brand.setId(BRAND_ID);
        var brandList = new ArrayList<Brand>();
        brandList.add(brand);
        return new PageImpl<>(brandList);
    }

    private Page<BankMerchantSearchIndex> getSampleBankMerchantSearchIndexPage() {
        BankMerchantSearchIndex bankMerchantSearchIndex = new BankMerchantSearchIndex();
        bankMerchantSearchIndex.setId(BANK_MERCHANT_ID);
        List<BankMerchantSearchIndex> bankMerchantSearchIndexList = new ArrayList<>();
        bankMerchantSearchIndexList.add(bankMerchantSearchIndex);
        return new PageImpl<>(bankMerchantSearchIndexList);
    }
}