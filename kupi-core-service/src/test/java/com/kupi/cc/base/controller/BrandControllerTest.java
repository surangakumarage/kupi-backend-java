package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.Brand;
import com.kupi.cc.base.domain.request.BrandRequestDto;
import com.kupi.cc.base.domain.request.BrandUpdateRequestDto;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.InvalidBrandException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.exception.UnsupportedDeleteAction;
import com.kupi.cc.base.service.BrandService;
import com.kupi.cc.base.service.CategoryBrandMerchantService;
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
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests the {@link BrandController} class.
 */
class BrandControllerTest {

    private static final String BRAND_ID = "bid-02a2022f-98c5-4237-a9c7-c1faf989a4a5";
    private static final String USER_ID = "uid-bb801b6a-ae7c-4f81-8876-a722f644fa11";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String BRAND_NAME = "DELL";
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String CREATE_BRAND = "/api/v1/brands";
    private static final int PAGE = 0;
    private static final int SIZE = 1;
    private static final int TOTAL_ITEMS = 1;
    private static final int TOTAL_PAGE = 1;
    private static final long DEAL_COUNT = 1L;
    private static final long MERCHANT_COUNT = 1L;
    private static final long BANK_COUNT = 1L;
    private static final long ACTIVE_DEAL_COUNT = 1L;
    private static final long ACTIVE_MERCHANT_COUNT = 1L;
    private static final long ACTIVE_BANK_COUNT = 1L;
    private static final String SEARCH_TERM = "SEARCH_TERM";
    private static final String ALL = "ALL";
    private static final String UPDATED_AT = "updatedAt";
    private static final String UPDATED_BRAND_NAME = "DELL";
    private static final String GET_BRAND_LIST_URI = "/api/v1/brands/{page}/{size}/search/{searchTerm}";
    private static final String DELETE_BRAND_URI = "/api/v1/brands/delete/{brandId}";
    private static final String GET_BASIC_BRAND_LIST_URI = "/api/v1/brands/basic/{page}/{size}";
    private static final String GET_DETAIL_BRAND_URI = "/api/v1/brands/{brandId}";
    private static final String UPDATE_BRAND_URI = "/api/v1/brands";

    @Mock
    private BrandService brandService;
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
        BrandController brandController = new BrandController(brandService, categoryBrandMerchantService, translator, dealSearchService);
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
        mockMvc = MockMvcBuilders.standaloneSetup(brandController)
                .setValidator(noOpValidator)
                .build();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnOk_When_ValidFieldsAreProvidedForGetBrandList() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        Page<Brand> brandPage = getSampleBrandPage();
        when(brandService.listAllBrands(pageable, SEARCH_TERM)).thenReturn(brandPage);
        String uri = GET_BRAND_LIST_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_BRAND_LIST.getMessage()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_BRAND_LIST.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetBrandListIsFailed() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        doThrow(new KupiCoreException("ERROR")).when(brandService).listAllBrands(pageable, SEARCH_TERM);
        String uri = GET_BRAND_LIST_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
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
    void Should_ReturnOk_When_ValidBrandIdProvidedForGetDetailBrand() throws Exception {
        Brand brand = getSampleBrand();
        when(brandService.getBrandById(BRAND_ID)).thenReturn(brand);
        when(dealSearchService.getDealsCountByBrandId(BRAND_ID)).thenReturn(DEAL_COUNT);
        when(dealSearchService.getActiveDealsCountByBrandId(BRAND_ID)).thenReturn(ACTIVE_DEAL_COUNT);
        when(categoryBrandMerchantService.getMerchantCountByBrandIdAndUserType(BRAND_ID, UserType.MERCHANT)).thenReturn(MERCHANT_COUNT);
        when(categoryBrandMerchantService.getActiveMerchantCountByBrandIdAndUserType(BRAND_ID, UserType.MERCHANT)).thenReturn(ACTIVE_MERCHANT_COUNT);
        when(categoryBrandMerchantService.getMerchantCountByBrandIdAndUserType(BRAND_ID, UserType.BANK)).thenReturn(BANK_COUNT);
        when(categoryBrandMerchantService.getActiveMerchantCountByBrandIdAndUserType(BRAND_ID, UserType.BANK)).thenReturn(ACTIVE_BANK_COUNT);
        String uri = GET_DETAIL_BRAND_URI.replace("{brandId}", BRAND_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_BRAND_DETAIL.getMessage()))
                .andExpect(jsonPath("$.data.id").value(BRAND_ID))
                .andExpect(jsonPath("$.data.noOfActiveMerchants").value(ACTIVE_MERCHANT_COUNT))
                .andExpect(jsonPath("$.data.noOfActiveDeals").value(ACTIVE_DEAL_COUNT))
                .andExpect(jsonPath("$.data.noOfMerchants").value(MERCHANT_COUNT))
                .andExpect(jsonPath("$.data.noOfDeals").value(DEAL_COUNT))
                .andExpect(jsonPath("$.data.noOfBanks").value(BANK_COUNT))
                .andExpect(jsonPath("$.data.noOfActiveBanks").value(ACTIVE_BANK_COUNT))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_BRAND_DETAIL.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidBrandIdIsProvidedForGetDetailBrand() throws Exception {
        doThrow(new InvalidBrandException("ERROR")).when(brandService).getBrandById(BRAND_ID);
        String uri = GET_DETAIL_BRAND_URI.replace("{brandId}", BRAND_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_BRAND_ID
                        .getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_BRAND_ID
                        .getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetDetailBrandIsFailed() throws Exception {
        doThrow(new KupiCoreException("ERROR")).when(brandService).getBrandById(BRAND_ID);
        String uri = GET_DETAIL_BRAND_URI.replace("{brandId}", BRAND_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
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
    void Should_ReturnOk_When_ValidPageAndSizeAreProvidedForGetBasicBrandList() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        Page<Brand> brandPage = getSampleBrandPage();
        when(brandService.listAllBrands(pageable, ALL)).thenReturn(brandPage);
        String uri = GET_BASIC_BRAND_LIST_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE));
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_BRAND_LIST.getMessage()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.READ_BRAND_LIST.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetBasicBrandListIsFailed() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        doThrow(new KupiCoreException("ERROR")).when(brandService).listAllBrands(pageable, ALL);
        String uri = GET_BASIC_BRAND_LIST_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE));
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
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
    void Should_ReturnOk_When_CreateBrandSuccessfully() throws Exception {
        BrandRequestDto brandRequestDto = getSampleBrandRequestDto();
        when(brandService.checkBrandNameExist(BRAND_NAME)).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_BRAND)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(brandRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.CREATE_BRAND.getMessage()))
                .andExpect(jsonPath("$.data.id", startsWith("bid-")))
                .andExpect(jsonPath("$.data.name").value(BRAND_NAME))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.CREATE_BRAND.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_MissingRequiredFieldsAreProvidedForCreateBrand() throws Exception {
        BrandRequestDto brandRequestDto = getSampleBrandRequestDto();
        brandRequestDto.setName(null);
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_BRAND)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(brandRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_ExistingBrandNameIsProvidedForCreateBrand() throws Exception {
        BrandRequestDto brandRequestDto = getSampleBrandRequestDto();
        when(brandService.checkBrandNameExist(BRAND_NAME)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_BRAND)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(brandRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.EXISTING_BRAND_NAME.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.EXISTING_BRAND_NAME.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_CreateBrandIsFailed() throws Exception {
        BrandRequestDto brandRequestDto = getSampleBrandRequestDto();
        when(brandService.checkBrandNameExist(BRAND_NAME)).thenReturn(false);
        doThrow(new KupiCoreException("ERROR")).when(brandService).createBrand(any(Brand.class));
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_BRAND)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(brandRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_BrandDeletedSuccessfully() throws Exception {
        String uri = DELETE_BRAND_URI.replace("{brandId}", BRAND_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.DELETE_BRAND.getMessage()))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.DELETE_BRAND.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidBrandIdIsProvidedForDeleteBrand() throws Exception {
        doThrow(new InvalidBrandException("ERROR")).when(brandService).deleteBrand(BRAND_ID);
        String uri = DELETE_BRAND_URI.replace("{brandId}", BRAND_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_BRAND_ID.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_BRAND_ID.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_GivenBrandMapWIthCategoryBrandMerchantForDeleteBrand() throws Exception {
        doThrow(new UnsupportedDeleteAction("ERROR")).when(brandService).deleteBrand(BRAND_ID);
        String uri = DELETE_BRAND_URI.replace("{brandId}", BRAND_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.UNSUPPORTED_BRAND_DELETE.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.UNSUPPORTED_BRAND_DELETE.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_DeleteBrandIsFailed() throws Exception {
        doThrow(new KupiCoreException("ERROR")).when(brandService).deleteBrand(BRAND_ID);
        String uri = DELETE_BRAND_URI.replace("{brandId}", BRAND_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_UpdateBrandSuccessfully() throws Exception {
        BrandUpdateRequestDto brandUpdateRequestDto = getSampleBrandUpdateRequestDto();
        Brand brand = getSampleBrand();
        when(brandService.checkBrandNameNotExistForOtherIDs(BRAND_ID, BRAND_NAME)).thenReturn(false);
        when(brandService.updateBrand(any(BrandUpdateRequestDto.class))).thenReturn(brand);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_BRAND_URI)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(brandUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.UPDATE_BRAND.getMessage()))
                .andExpect(jsonPath("$.data.id", startsWith("bid-")))
                .andExpect(jsonPath("$.statusCode").value(SuccessResponseStatusType.UPDATE_BRAND.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_MissingRequiredFieldsAreProvidedForUpdateBrand() throws Exception {
        BrandUpdateRequestDto brandUpdateRequestDto = getSampleBrandUpdateRequestDto();
        brandUpdateRequestDto.setId(null);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_BRAND_URI)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(brandUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_GivenBrandNameIsAlreadyExistsForUpdateBrand() throws Exception {
        BrandUpdateRequestDto brandUpdateRequestDto = getSampleBrandUpdateRequestDto();
        when(brandService.checkBrandNameNotExistForOtherIDs(BRAND_ID, BRAND_NAME)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_BRAND_URI)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(brandUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.EXISTING_BRAND_NAME.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.EXISTING_BRAND_NAME.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidBrandIdIsProvidedForUpdateBrand() throws Exception {
        BrandUpdateRequestDto brandUpdateRequestDto = getSampleBrandUpdateRequestDto();
        when(brandService.checkBrandNameNotExistForOtherIDs(BRAND_ID, BRAND_NAME)).thenReturn(false);
        doThrow(new InvalidBrandException("ERROR")).when(brandService).updateBrand(any(BrandUpdateRequestDto.class));
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_BRAND_URI)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(brandUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_BRAND_ID.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_BRAND_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_UpdateBrandIsFailed() throws Exception {
        BrandUpdateRequestDto brandUpdateRequestDto = getSampleBrandUpdateRequestDto();
        when(brandService.checkBrandNameNotExistForOtherIDs(BRAND_ID, BRAND_NAME)).thenReturn(false);
        doThrow(new KupiCoreException("ERROR")).when(brandService).updateBrand(any(BrandUpdateRequestDto.class));
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_BRAND_URI)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(brandUpdateRequestDto.toJson())
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
     * This method creates BrandPage
     *
     * @return BrandPage
     */
    private Page<Brand> getSampleBrandPage() {
        Brand brand = getSampleBrand();
        List<Brand> brandList = new ArrayList<>();
        brandList.add(brand);
        return new PageImpl<>(brandList);
    }

    /**
     * This method creates sample BrandRequestDto
     *
     * @return BrandRequestDto
     */
    private BrandRequestDto getSampleBrandRequestDto() {
        BrandRequestDto brandRequestDto = new BrandRequestDto();
        brandRequestDto.setName(BRAND_NAME);
        brandRequestDto.setDescription("Brand Description");
        brandRequestDto.setImageUrl("https://picsum.photos/200/300");
        return brandRequestDto;
    }

    /**
     * This method creates sample BrandUpdateRequestDto
     *
     * @return BrandUpdateRequestDto
     */
    private BrandUpdateRequestDto getSampleBrandUpdateRequestDto() {
        BrandUpdateRequestDto brandUpdateRequestDto = new BrandUpdateRequestDto();
        brandUpdateRequestDto.setId(BRAND_ID);
        brandUpdateRequestDto.setName(UPDATED_BRAND_NAME);
        brandUpdateRequestDto.setDescription("Updated Brand description");
        brandUpdateRequestDto.setImageUrl("https://objects-kupi-dev.s3.ap-southeast-1.amazonaws.com/fid-3338ebed-e3d3-4f86-8b3e-92887f27e51d.jpg");
        return brandUpdateRequestDto;
    }
}