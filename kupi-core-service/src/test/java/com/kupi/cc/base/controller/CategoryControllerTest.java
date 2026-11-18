package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.Category;
import com.kupi.cc.base.domain.request.CategoryRequestDto;
import com.kupi.cc.base.domain.request.CategoryUpdateRequestDto;
import com.kupi.cc.base.enums.CategoryType;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.*;
import com.kupi.cc.base.service.CategoryBrandMerchantService;
import com.kupi.cc.base.service.CategoryService;
import com.kupi.cc.base.service.DealSearchService;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class test for {@link  CategoryController} class
 */
class CategoryControllerTest {
    private static final int PAGE_ITEMS = 1;
    private static final int PAGE_PAGE = 1;
    private static final int PAGE_NO = 0;
    private static final int SIZE = 1;
    private static final String CATEGORY_ID = "cid-1235-1458-1785";
    private static final String CATEGORY_NAME = "New Year";
    private static final String CATEGORY_DESCRIPTION = "snacks and sweets";
    private static final String CATEGORY_IMAGE_URL = "https://picsum.photos/200/300";
    private static final long CATEGORY_START_DATE = 1665426600000L;
    private static final long CATEGORY_EXPIRY_DATE = 1670697000000L;
    private static final long MERCHANT_COUNT = 1L;
    private static final long ACTIVE_MERCHANT_COUNT = 0L;
    private static final long BANK_COUNT = 1L;
    private static final long ACTIVE_BANK_COUNT = 0L;
    private static final long DEALS_COUNT = 1L;
    private static final long ACTIVE_DEALS_COUNT = 0L;
    private static final String CREATE_CATEGORY_URL = "/api/v1/categories";
    private static final String UPDATE_CATEGORY_URL = "/api/v1/categories";
    private static final String GET_CATEGORY_BY_ID_URL = "/api/v1/categories/{categoryId}";
    private static final String GET_BASIC_CATEGORY_LIST_URL = "/api/v1/categories/basic/{page}/{size}";
    private static final String GET_CATEGORY_LIST_URL = "/api/v1/categories/{page}/{size}/search/{searchTerm}";
    private static final String DELETE_CATEGORY_URL = "/api/v1/categories/delete/{categoryId}";
    private static final String GET_ACTIVE_CATEGORY_LIST_URL =
            "/api/v1/categories/active/{page}/{size}/search/{searchTerm}";
    private static final String GET_POPULAR_CATEGORY_LIST_URL = "/api/v1/categories/popular/{page}/{size}";
    private static final String GET_CATEGORY_LIST_BY_TYPE_URL =
            "/api/v1/categories/{categoryType}/{page}/{size}/search/{searchTerm}";
    private static final String GET_CATEGORY_LIST_BY_TYPE_WITH_EXCLUDE_ID_URL =
            "/api/v1/categories/{categoryType}/{page}/{size}/search/{searchTerm}/{excludeId}";
    private static final String GET_RELATED_CATEGORY_LIST_URL = "/api/v1/categories/related/{categoryId}/{page}/{size}";
    private static final String GET_ACTIVE_CATEGORY_LIST_FOR_MOBILE_USER_URL =
            "/api/v1/categories/active/user/{userId}/{page}/{size}/search/{searchTerm}";
    private static final String GET_EXPIRED_CATEGORY_LIST_URL =
            "/api/v1/categories/expired/{page}/{size}/search/{searchTerm}";
    private static final String ADD_FAVOURITE_CATEGORY_URL = "/api/v1/categories/{categoryId}/favourite";
    private static final String REMOVE_FAVOURITE_CATEGORY_URL = "/api/v1/categories/{categoryId}/favourite/remove";
    private static final String GET_FAVOURITE_CATEGORY_LIST_URI = "/api/v1/categories/favourite/{page}/{size}";
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String USER_ID = "uid-bb801b6a-ae7c-4f81-8876-a722f644fa11";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String UPDATED_AT = "updatedAt";
    private static final String SEARCH_TERM = "Year";
    private static final String ALL = "ALL";
    private static final String EXCLUDE_ID = "cid-1235-1458-7856";
    private static final String NULL_EXCLUDE_ID = null;
    private static final String UPDATED_CATEGORY_NAME = "Christmas";
    private static final String UPDATED_CATEGORY_DESCRIPTION = "Cake";
    private static final String CATEGORY_TYPE = CategoryType.SEASONAL.name();
    private static final String CREATE_CATEGORY_SUCCESS_MESSAGE = "Successfully created the category.";
    private final Category category = getSampleCategory();
    private final CategoryRequestDto categoryRequestDto = getSampleCategoryRequestDto();
    private final Set<Category> categorySet = getSampleCategorySet();

    @Mock
    private CategoryService categoryService;
    @Mock
    private CategoryBrandMerchantService categoryBrandMerchantService;
    @Mock
    private DealSearchService dealSearchService;
    @Mock
    private Translator translator;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        initMocks(this);
        CategoryController categoryController = new CategoryController(categoryService, categoryBrandMerchantService,
                translator, dealSearchService);
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
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
                .setValidator(noOpValidator)
                .build();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnOk_When_CreatingACategory() throws Exception {
        List<String> categoryIdsSet = new ArrayList<>();
        categoryIdsSet.add(CATEGORY_ID);
        when(categoryService.getCategorySetByIds(categoryIdsSet)).thenReturn(categorySet);
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_CATEGORY_URL)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(categoryRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(CREATE_CATEGORY_SUCCESS_MESSAGE))
                .andExpect(jsonPath("$.data.id", startsWith("cid-")))
                .andExpect(jsonPath("$.data.name").value(CATEGORY_NAME))
                .andExpect(jsonPath("$.data.description").value(CATEGORY_DESCRIPTION))
                .andExpect(jsonPath("$.data.imageUrl").value(CATEGORY_IMAGE_URL))
                .andExpect(jsonPath("$.data.categoryType").value(CategoryType.SEASONAL.name()))
                .andExpect(jsonPath("$.data.popular").value(true))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType
                        .CREATE_CATEGORY.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingACategoryWithoutRequiredFields() throws Exception {
        categoryRequestDto.setName(null);
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_CATEGORY_URL)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(categoryRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getCode()));
    }

//    @Test
    void Should_ReturnBadRequest_When_CreatingASeasonalCategoryWithoutValidDateRange() throws Exception {
        categoryRequestDto.setStartDate(System.currentTimeMillis());
        categoryRequestDto.setExpiryDate(System.currentTimeMillis() - 10000);
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_CATEGORY_URL)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(categoryRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_START_END_DATE
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_START_END_DATE
                        .getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingACategoryWithExistingName() throws Exception {
        List<String> categoryIdsSet = new ArrayList<>();
        categoryIdsSet.add(CATEGORY_ID);
        when(categoryService.getCategorySetByIds(categoryIdsSet)).thenReturn(categorySet);
        doThrow(new ExistingNameException("ERROR")).when(categoryService).createCategory(any(Category.class));
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_CATEGORY_URL)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(categoryRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.EXISTING_CATEGORY_NAME
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.EXISTING_CATEGORY_NAME
                        .getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidCategoryIdsAreProvidedForRelatedCategories() throws Exception {
        doThrow(new InvalidCategoryException("ERROR")).when(categoryService).getCategorySetByIds(anyList());
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_CATEGORY_URL)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(categoryRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_CATEGORY_ID
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_CATEGORY_ID
                        .getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_CreatingACategoryIsFailed() throws Exception {
        List<String> categoryIdsSet = new ArrayList<>();
        categoryIdsSet.add(CATEGORY_ID);
        when(categoryService.getCategorySetByIds(categoryIdsSet)).thenReturn(categorySet);
        doThrow(new KupiCoreException("ERROR")).when(categoryService).createCategory(any(Category.class));
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_CATEGORY_URL)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(categoryRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidCategoryIdIsProvided() throws Exception {
        String url = GET_CATEGORY_BY_ID_URL.replace("{categoryId}", CATEGORY_ID);
        when(categoryService.getCategoryById(CATEGORY_ID)).thenReturn(category);
        when(dealSearchService.getDealsCountByCategoryId(CATEGORY_ID)).thenReturn(DEALS_COUNT);
        when(dealSearchService.getActiveDealsCountByCategoryId(CATEGORY_ID)).thenReturn(ACTIVE_DEALS_COUNT);
        when(categoryBrandMerchantService.getMerchantCountByCategoryIdAndUserType(CATEGORY_ID, UserType.MERCHANT))
                .thenReturn(MERCHANT_COUNT);
        when(categoryBrandMerchantService.getActiveMerchantCountByCategoryIdAndUserType(CATEGORY_ID, UserType.MERCHANT))
                .thenReturn(ACTIVE_MERCHANT_COUNT);
        when(categoryBrandMerchantService.getMerchantCountByCategoryIdAndUserType(CATEGORY_ID, UserType.BANK))
                .thenReturn(BANK_COUNT);
        when(categoryBrandMerchantService.getActiveMerchantCountByCategoryIdAndUserType(CATEGORY_ID, UserType.BANK))
                .thenReturn(ACTIVE_BANK_COUNT);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_CATEGORY_DETAIL
                        .getMessage()))
                .andExpect(jsonPath("$.data.id", startsWith("cid-")))
                .andExpect(jsonPath("$.data.name").value(CATEGORY_NAME))
                .andExpect(jsonPath("$.data.description").value(CATEGORY_DESCRIPTION))
                .andExpect(jsonPath("$.data.startDate").value(CATEGORY_START_DATE))
                .andExpect(jsonPath("$.data.expiryDate").value(CATEGORY_EXPIRY_DATE))
                .andExpect(jsonPath("$.data.imageUrl").value(CATEGORY_IMAGE_URL))
                .andExpect(jsonPath("$.data.categoryType").value(CategoryType.SEASONAL.name()))
                .andExpect(jsonPath("$.data.popular").value(true))
                .andExpect(jsonPath("$.data.noOfMerchants").value(MERCHANT_COUNT))
                .andExpect(jsonPath("$.data.noOfDeals").value(DEALS_COUNT))
                .andExpect(jsonPath("$.data.noOfActiveDeals").value(ACTIVE_DEALS_COUNT))
                .andExpect(jsonPath("$.data.noOfActiveMerchants").value(ACTIVE_MERCHANT_COUNT))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType
                        .READ_CATEGORY_DETAIL.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidCategoryIdIsProvidedForGetCategoryById() throws Exception {
        String url = GET_CATEGORY_BY_ID_URL.replace("{categoryId}", CATEGORY_ID);
        doThrow(new InvalidCategoryException("ERROR")).when(categoryService).getCategoryById(CATEGORY_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_CATEGORY_ID
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_CATEGORY_ID
                        .getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetDetailCategoryIsFailed() throws Exception {
        String url = GET_CATEGORY_BY_ID_URL.replace("{categoryId}", CATEGORY_ID);
        doThrow(new KupiCoreException("ERROR")).when(categoryService).getCategoryById(CATEGORY_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidPageableIsProvidedForGetBasicCategoryList() throws Exception {
        String url = GET_BASIC_CATEGORY_LIST_URL.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE));
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        Page<Category> categoryPage = getSampleCategoryPage();
        when(categoryService.listAllCategories(pageable, ALL)).thenReturn(categoryPage);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_CATEGORY_LIST
                        .getMessage()))
                .andExpect(jsonPath("$.data.totalItems").value(PAGE_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(PAGE_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE_NO))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.data.categories[0].id").value(CATEGORY_ID))
                .andExpect(jsonPath("$.data.categories[0].name").value(CATEGORY_NAME))
                .andExpect(jsonPath("$.data.categories[0].imageUrl").value(CATEGORY_IMAGE_URL))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType
                        .READ_CATEGORY_LIST.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetBasicCategoryListIsFailed() throws Exception {
        String url = GET_BASIC_CATEGORY_LIST_URL.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE));
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        doThrow(new KupiCoreException("ERROR")).when(categoryService).listAllCategories(pageable, ALL);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidPageableAndSearchTermAreProvidedForGetCategoryList() throws Exception {
        String url = GET_CATEGORY_LIST_URL.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE)).replace("{searchTerm}", SEARCH_TERM);
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        Page<Category> categoryPage = getSampleCategoryPage();
        when(categoryService.listAllCategories(pageable, SEARCH_TERM)).thenReturn(categoryPage);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_CATEGORY_LIST
                        .getMessage()))
                .andExpect(jsonPath("$.data.totalItems").value(PAGE_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(PAGE_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE_NO))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.data.categories[0].id").value(CATEGORY_ID))
                .andExpect(jsonPath("$.data.categories[0].name").value(CATEGORY_NAME))
                .andExpect(jsonPath("$.data.categories[0].imageUrl").value(CATEGORY_IMAGE_URL))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType
                        .READ_CATEGORY_LIST.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetCategoryListIsFailed() throws Exception {
        String url = GET_CATEGORY_LIST_URL.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE)).replace("{searchTerm}", SEARCH_TERM);
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        doThrow(new KupiCoreException("ERROR")).when(categoryService).listAllCategories(pageable, SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidPageableAndSearchTermAreProvidedForGetActiveCategoryList() throws Exception {
        String url = GET_ACTIVE_CATEGORY_LIST_URL.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE)).replace("{searchTerm}", SEARCH_TERM);
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        Page<Category> categoryPage = getSampleCategoryPage();
        when(categoryService.getActiveCategories(pageable, SEARCH_TERM)).thenReturn(categoryPage);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_CATEGORY_LIST
                        .getMessage()))
                .andExpect(jsonPath("$.data.totalItems").value(PAGE_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(PAGE_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE_NO))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.data.categories[0].id").value(CATEGORY_ID))
                .andExpect(jsonPath("$.data.categories[0].name").value(CATEGORY_NAME))
                .andExpect(jsonPath("$.data.categories[0].imageUrl").value(CATEGORY_IMAGE_URL))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType
                        .READ_CATEGORY_LIST.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetActiveCategoryListIsFailed() throws Exception {
        String url = GET_ACTIVE_CATEGORY_LIST_URL.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE)).replace("{searchTerm}", SEARCH_TERM);
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        doThrow(new KupiCoreException("ERROR")).when(categoryService).getActiveCategories(pageable, SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidPageableIsProvidedForGetPopularCategoryList() throws Exception {
        String url = GET_POPULAR_CATEGORY_LIST_URL.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE));
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).ascending());
        Page<Category> categoryPage = getSampleCategoryPage();
        when(categoryService.listAllPopularCategories(pageable)).thenReturn(categoryPage);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_CATEGORY_LIST
                        .getMessage()))
                .andExpect(jsonPath("$.data.totalItems").value(PAGE_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(PAGE_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE_NO))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.data.categories[0].id").value(CATEGORY_ID))
                .andExpect(jsonPath("$.data.categories[0].name").value(CATEGORY_NAME))
                .andExpect(jsonPath("$.data.categories[0].imageUrl").value(CATEGORY_IMAGE_URL))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType
                        .READ_CATEGORY_LIST.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetPopularCategoryListIsFailed() throws Exception {
        String url = GET_POPULAR_CATEGORY_LIST_URL.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE));
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).ascending());
        doThrow(new KupiCoreException("ERROR")).when(categoryService).listAllPopularCategories(pageable);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()));
    }

    @Test
    void Should_ReturnOk_When_ExcludeIdIsNotProvidedForGetCategoryListByType() throws Exception {
        String url = GET_CATEGORY_LIST_BY_TYPE_URL.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE)).replace("{categoryType}", CATEGORY_TYPE)
                .replace("{searchTerm}", ALL);
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        Page<Category> categoryPage = getSampleCategoryPage();
        when(categoryService.getCategoriesForCategoryType(CategoryType.SEASONAL, pageable, ALL, NULL_EXCLUDE_ID))
                .thenReturn(categoryPage);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType
                        .READ_CATEGORY_LIST.getMessage()))
                .andExpect(jsonPath("$.data.totalItems").value(PAGE_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(PAGE_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE_NO))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.data.categories[0].id").value(CATEGORY_ID))
                .andExpect(jsonPath("$.data.categories[0].name").value(CATEGORY_NAME))
                .andExpect(jsonPath("$.data.categories[0].imageUrl").value(CATEGORY_IMAGE_URL))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType
                        .READ_CATEGORY_LIST.getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidExcludeIdIsProvidedForGetCategoryListByType() throws Exception {
        String url = GET_CATEGORY_LIST_BY_TYPE_WITH_EXCLUDE_ID_URL.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE)).replace("{categoryType}", CATEGORY_TYPE)
                .replace("{searchTerm}", ALL).replace("{excludeId}", EXCLUDE_ID);
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        Page<Category> categoryPage = getSampleCategoryPage();
        when(categoryService.getCategoriesForCategoryType(CategoryType.SEASONAL, pageable, ALL, EXCLUDE_ID))
                .thenReturn(categoryPage);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType
                        .READ_CATEGORY_LIST.getMessage()))
                .andExpect(jsonPath("$.data.totalItems").value(PAGE_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(PAGE_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE_NO))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.data.categories[0].id").value(CATEGORY_ID))
                .andExpect(jsonPath("$.data.categories[0].name").value(CATEGORY_NAME))
                .andExpect(jsonPath("$.data.categories[0].imageUrl").value(CATEGORY_IMAGE_URL))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType
                        .READ_CATEGORY_LIST.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetCategoryListByTypeIsFailed() throws Exception {
        String url = GET_CATEGORY_LIST_BY_TYPE_URL.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE)).replace("{categoryType}", CATEGORY_TYPE)
                .replace("{searchTerm}", ALL);
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        doThrow(new KupiCoreException("ERROR")).when(categoryService).getCategoriesForCategoryType(
                CategoryType.SEASONAL, pageable, ALL, NULL_EXCLUDE_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidCategoryIdIsProvidedForGetRelatedCategoryList() throws Exception {
        String url = GET_RELATED_CATEGORY_LIST_URL.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE)).replace("{categoryId}", CATEGORY_ID);
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        Page<Category> categoryPage = getSampleCategoryPage();
        when(categoryService.getRelatedCategoryListForCategory(pageable, CATEGORY_ID)).thenReturn(categoryPage);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType
                        .READ_RELATED_CATEGORIES.getMessage()))
                .andExpect(jsonPath("$.data.totalItems").value(PAGE_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(PAGE_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE_NO))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.data.categories[0].id").value(CATEGORY_ID))
                .andExpect(jsonPath("$.data.categories[0].name").value(CATEGORY_NAME))
                .andExpect(jsonPath("$.data.categories[0].imageUrl").value(CATEGORY_IMAGE_URL))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType
                        .READ_RELATED_CATEGORIES.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_CategoryTypeIsNotEqualToSeasonalForGetRelatedCategoryList() throws Exception {
        String url = GET_RELATED_CATEGORY_LIST_URL.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE)).replace("{categoryId}", CATEGORY_ID);
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        doThrow(new UnsupportedCategoryTypeException("ERROR")).when(categoryService)
                .getRelatedCategoryListForCategory(pageable, CATEGORY_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(categoryRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType
                        .INVALID_CATEGORY_FOR_RELATED_CATEGORIES.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .INVALID_CATEGORY_FOR_RELATED_CATEGORIES.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidCategoryIdIsProvidedForGetRelatedCategoryList() throws Exception {
        String url = GET_RELATED_CATEGORY_LIST_URL.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE)).replace("{categoryId}", CATEGORY_ID);
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        doThrow(new InvalidCategoryException("ERROR")).when(categoryService)
                .getRelatedCategoryListForCategory(pageable, CATEGORY_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(categoryRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_CATEGORY_ID
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_CATEGORY_ID
                        .getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetRelatedCategoryListIsFailed() throws Exception {
        String url = GET_RELATED_CATEGORY_LIST_URL.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE)).replace("{categoryId}", CATEGORY_ID);
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        doThrow(new KupiCoreException("ERROR")).when(categoryService)
                .getRelatedCategoryListForCategory(pageable, CATEGORY_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidRequiredFieldsAreProvided() throws Exception {
        CategoryUpdateRequestDto updateRequestDto = getSampleUpdateCategoryRequestDto();
        Category updatedCategory = getSampleUpdatedCategory();
        when(categoryService.updateCategory(any(CategoryUpdateRequestDto.class))).thenReturn(updatedCategory);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_CATEGORY_URL)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(updateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType
                        .UPDATE_CATEGORY.getMessage()))
                .andExpect(jsonPath("$.data.id", startsWith("cid-")))
                .andExpect(jsonPath("$.data.name").value(UPDATED_CATEGORY_NAME))
                .andExpect(jsonPath("$.data.description").value(UPDATED_CATEGORY_DESCRIPTION))
                .andExpect(jsonPath("$.data.startDate").value(CATEGORY_START_DATE))
                .andExpect(jsonPath("$.data.expiryDate").value(CATEGORY_EXPIRY_DATE))
                .andExpect(jsonPath("$.data.imageUrl").value(CATEGORY_IMAGE_URL))
                .andExpect(jsonPath("$.data.categoryType").value(CategoryType.SEASONAL.name()))
                .andExpect(jsonPath("$.data.popular").value(true))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType
                        .UPDATE_CATEGORY.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_RequiredFieldsAreNotProvided() throws Exception {
        CategoryUpdateRequestDto updateRequestDto = getSampleUpdateCategoryRequestDto();
        updateRequestDto.setId(null);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_CATEGORY_URL)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(updateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_RequiredFieldsAreNotProvidedForUpdateCategory() throws Exception {
        CategoryUpdateRequestDto updateRequestDto = getSampleUpdateCategoryRequestDto();
        updateRequestDto.setId(null);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_CATEGORY_URL)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(updateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidDateRangeIsProvidedForUpdateCategory() throws Exception {
        CategoryUpdateRequestDto updateRequestDto = getSampleUpdateCategoryRequestDto();
        updateRequestDto.setStartDate(1670697000000L);
        updateRequestDto.setExpiryDate(1665426600000L);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_CATEGORY_URL)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(updateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_START_END_DATE
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_START_END_DATE
                        .getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_ExistingCategoryNameIsProvidedForUpdateCategory() throws Exception {
        CategoryUpdateRequestDto updateRequestDto = getSampleUpdateCategoryRequestDto();
        doThrow(new ExistingNameException("ERROR")).when(categoryService).updateCategory(any(CategoryUpdateRequestDto.class));
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_CATEGORY_URL)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(updateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.EXISTING_CATEGORY_NAME
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.EXISTING_CATEGORY_NAME
                        .getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidCategoryIdIsProvidedForUpdateCategory() throws Exception {
        CategoryUpdateRequestDto updateRequestDto = getSampleUpdateCategoryRequestDto();
        doThrow(new InvalidCategoryException("ERROR")).when(categoryService).updateCategory(any(CategoryUpdateRequestDto.class));
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_CATEGORY_URL)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(updateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_CATEGORY_ID
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_CATEGORY_ID
                        .getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_UpdateCategoryIsFailed() throws Exception {
        CategoryUpdateRequestDto updateRequestDto = getSampleUpdateCategoryRequestDto();
        doThrow(new KupiCoreException("ERROR")).when(categoryService).updateCategory(any(CategoryUpdateRequestDto.class));
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_CATEGORY_URL)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(updateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()));
    }

    @Test
    void Should_ReturnOk_When_DeleteCategoryIsSuccessful() throws Exception {
        String url = DELETE_CATEGORY_URL.replace("{categoryId}", CATEGORY_ID);
        when(categoryBrandMerchantService.checkCategoryIdMapWithCategoryBrandMerchant(CATEGORY_ID)).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.delete(url)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType
                        .CATEGORY_DELETE_SUCCESSFUL.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType
                        .CATEGORY_DELETE_SUCCESSFUL.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidCategoryIdIsProvidedForDeleteCategory() throws Exception {
        String url = DELETE_CATEGORY_URL.replace("{categoryId}", CATEGORY_ID);
        doThrow(new InvalidCategoryException("ERROR")).when(categoryService)
                .deleteCategory(CATEGORY_ID, false);
        mockMvc.perform(MockMvcRequestBuilders.delete(url)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_CATEGORY_ID
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_CATEGORY_ID
                        .getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_CategoryMapWithCategoryBranchMerchant() throws Exception {
        String url = DELETE_CATEGORY_URL.replace("{categoryId}", CATEGORY_ID);
        doThrow(new UnsupportedDeleteAction("ERROR")).when(categoryService)
                .deleteCategory(CATEGORY_ID, false);
        mockMvc.perform(MockMvcRequestBuilders.delete(url)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.UNSUPPORTED_CATEGORY_DELETE
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.UNSUPPORTED_CATEGORY_DELETE
                        .getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_DeleteCategoryIsFailed() throws Exception {
        String url = DELETE_CATEGORY_URL.replace("{categoryId}", CATEGORY_ID);
        when(categoryBrandMerchantService.checkCategoryIdMapWithCategoryBrandMerchant(CATEGORY_ID)).thenReturn(false);
        doThrow(new KupiCoreException("ERROR")).when(categoryService).deleteCategory(CATEGORY_ID, false);
        mockMvc.perform(MockMvcRequestBuilders.delete(url)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()));
    }

    @Test
    void Should_ReturnOk_When_GetActiveCategoryListForMobileUserSuccessfully() throws Exception {
        String url = GET_ACTIVE_CATEGORY_LIST_FOR_MOBILE_USER_URL.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE)).replace("{searchTerm}", SEARCH_TERM)
                .replace("{userId}", USER_ID);
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        Page<Category> categoryPage = getSampleCategoryPage();
        Set<String> favouriteCategoryIds = getSampleCategoryIds();
        when(categoryService.getActiveCategories(pageable, SEARCH_TERM)).thenReturn(categoryPage);
        when(categoryService.getFavouriteCategoryIdList(USER_ID)).thenReturn(favouriteCategoryIds);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType
                        .READ_CATEGORY_LIST.getMessage()))
                .andExpect(jsonPath("$.data.totalItems").value(PAGE_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(PAGE_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE_NO))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.data.categories[0].id").value(CATEGORY_ID))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType
                        .READ_CATEGORY_LIST.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetActiveCategoryListForMobileUserIsFailed() throws Exception {
        String url = GET_ACTIVE_CATEGORY_LIST_FOR_MOBILE_USER_URL.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE)).replace("{searchTerm}", SEARCH_TERM)
                .replace("{userId}", USER_ID);
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        Page<Category> categoryPage = getSampleCategoryPage();
        when(categoryService.getActiveCategories(pageable, SEARCH_TERM)).thenReturn(categoryPage);
        doThrow(new KupiCoreException("ERROR")).when(categoryService).getFavouriteCategoryIdList(USER_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()));
    }

    @Test
    void Should_ReturnOk_When_GetExpiredCategoryListSuccessfully() throws Exception {
        String url = GET_EXPIRED_CATEGORY_LIST_URL.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE)).replace("{searchTerm}", SEARCH_TERM);
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        Page<Category> categoryPage = getSampleCategoryPage();
        when(categoryService.getExpiredCategories(pageable, SEARCH_TERM)).thenReturn(categoryPage);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType
                        .READ_EXPIRED_CATEGORY_LIST.getMessage()))
                .andExpect(jsonPath("$.data.totalItems").value(PAGE_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(PAGE_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE_NO))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.data.categories[0].id").value(CATEGORY_ID))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType
                        .READ_EXPIRED_CATEGORY_LIST.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetExpiredCategoryListIsFailed() throws Exception {
        String url = GET_EXPIRED_CATEGORY_LIST_URL.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE)).replace("{searchTerm}", SEARCH_TERM);
        Pageable pageable = PageRequest.of(PAGE_NO, SIZE, Sort.by(UPDATED_AT).descending());
        doThrow(new KupiCoreException("ERROR")).when(categoryService).getExpiredCategories(pageable, SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()));
    }

    @Test
    void Should_ReturnOk_When_FavouriteCategoryAddedSuccessfully() throws Exception {
        String url = ADD_FAVOURITE_CATEGORY_URL.replace("{categoryId}", CATEGORY_ID);
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType
                        .FAVOURITE_CATEGORY_ADDED.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType
                        .FAVOURITE_CATEGORY_ADDED.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidCategoryIdIsProvidedForAddFavouriteCategory() throws Exception {
        String url = ADD_FAVOURITE_CATEGORY_URL.replace("{categoryId}", CATEGORY_ID);
        doThrow(new InvalidCategoryException("ERROR")).when(categoryService).addFavouriteCategory(USER_ID, CATEGORY_ID);
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_CATEGORY_ID
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_CATEGORY_ID
                        .getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_AddFavouriteCategoryIsFailed() throws Exception {
        String url = ADD_FAVOURITE_CATEGORY_URL.replace("{categoryId}", CATEGORY_ID);
        doThrow(new KupiCoreException("ERROR")).when(categoryService).addFavouriteCategory(USER_ID, CATEGORY_ID);
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()));
    }

    @Test
    void Should_ReturnOk_When_FavouriteCategoryRemovedSuccessfully() throws Exception {
        String url = REMOVE_FAVOURITE_CATEGORY_URL.replace("{categoryId}", CATEGORY_ID);
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType
                        .FAVOURITE_CATEGORY_REMOVED.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType
                        .FAVOURITE_CATEGORY_REMOVED.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_RemoveFavouriteCategoryIsFailed() throws Exception {
        String url = REMOVE_FAVOURITE_CATEGORY_URL.replace("{categoryId}", CATEGORY_ID);
        doThrow(new KupiCoreException("ERROR")).when(categoryService).removeFavouriteCategory(USER_ID, CATEGORY_ID);
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()));
    }

    @Test
    void Should_ReturnOk_When_GetFavouriteCategoryListSuccessfully() throws Exception {
        String url = GET_FAVOURITE_CATEGORY_LIST_URI.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE));
        Page<Category> categoryPage = getSampleCategoryPage();
        when(categoryService.getFavouriteCategory(anyString(), any(Pageable.class))).thenReturn(categoryPage);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType
                        .READ_CATEGORY_LIST.getMessage()))
                .andExpect(jsonPath("$.data.totalItems").value(PAGE_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(PAGE_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE_NO))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.data.categories[0].id").value(CATEGORY_ID))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType
                        .READ_CATEGORY_LIST.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetFavouriteCategoryListIsFailed() throws Exception {
        String url = GET_FAVOURITE_CATEGORY_LIST_URI.replace("{page}", String.valueOf(PAGE_NO))
                .replace("{size}", String.valueOf(SIZE));
        doThrow(new KupiCoreException("ERROR")).when(categoryService).getFavouriteCategory(anyString(), any(Pageable.class));
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()));
    }

    /**
     * This method creates sample category request dto
     *
     * @return CategoryRequestDto
     */
    private CategoryRequestDto getSampleCategoryRequestDto() {
        List<String> relatedCategories = new ArrayList<>();
        long currentTimeStamp = System.currentTimeMillis();

        CategoryRequestDto categoryRequestDto = new CategoryRequestDto();
        categoryRequestDto.setCategoryType(CategoryType.SEASONAL);
        categoryRequestDto.setDescription(CATEGORY_DESCRIPTION);
        categoryRequestDto.setName(CATEGORY_NAME);
        categoryRequestDto.setPopular(true);
        categoryRequestDto.setStartDate(currentTimeStamp + 86400000);
        categoryRequestDto.setExpiryDate(currentTimeStamp + 172800000);
        categoryRequestDto.setRelatedCategories(relatedCategories);
        categoryRequestDto.setImageUrl(CATEGORY_IMAGE_URL);
        return categoryRequestDto;
    }

    /**
     * This method creates sample category
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
        category.setStartDate(CATEGORY_START_DATE);
        category.setExpiryDate(CATEGORY_EXPIRY_DATE);
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
        Set<Category> categorySet = new HashSet<>();
        categorySet.add(category);
        return categorySet;
    }

    /**
     * This method creates sample category page
     *
     * @return categoryPage
     */
    private Page<Category> getSampleCategoryPage() {
        List<Category> categoryList = new ArrayList<>();
        Category sampleCategory = getSampleCategory();
        categoryList.add(sampleCategory);
        return new PageImpl<>(categoryList);
    }

    /**
     * This method creates sample update category request dto
     *
     * @return CategoryUpdateRequestDto
     */
    private CategoryUpdateRequestDto getSampleUpdateCategoryRequestDto() {
        List<String> relatedCategories = new ArrayList<>();

        CategoryUpdateRequestDto categoryRequestDto = new CategoryUpdateRequestDto();
        categoryRequestDto.setId(CATEGORY_ID);
        categoryRequestDto.setCategoryType(CategoryType.SEASONAL);
        categoryRequestDto.setDescription(UPDATED_CATEGORY_DESCRIPTION);
        categoryRequestDto.setName(UPDATED_CATEGORY_NAME);
        categoryRequestDto.setPopular(true);
        categoryRequestDto.setStartDate(CATEGORY_START_DATE);
        categoryRequestDto.setExpiryDate(System.currentTimeMillis() + CATEGORY_EXPIRY_DATE);
        categoryRequestDto.setRelatedCategories(relatedCategories);
        categoryRequestDto.setImageUrl("https://picsum.photos/200/300");
        return categoryRequestDto;
    }

    private Category getSampleUpdatedCategory() {
        Set<Category> relationalCategories = new HashSet<>();

        Category category = new Category();
        category.setId(CATEGORY_ID);
        category.setName(UPDATED_CATEGORY_NAME);
        category.setDescription(UPDATED_CATEGORY_DESCRIPTION);
        category.setImageUrl("https://picsum.photos/200/300");
        category.setCategoryType(CategoryType.SEASONAL);
        category.setStartDate(CATEGORY_START_DATE);
        category.setExpiryDate(CATEGORY_EXPIRY_DATE);
        category.setRelatedCategories(relationalCategories);
        category.setPopular(true);
        return category;
    }

    /**
     * This method creates sample CategoryIds set
     *
     * @return CategoryIdsSet
     */
    private Set<String> getSampleCategoryIds() {
        Set<String> categoryIds = new HashSet<>();
        categoryIds.add(CATEGORY_ID);
        return categoryIds;
    }
}