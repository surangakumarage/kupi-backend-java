package com.kupi.cc.auth.controller;

import com.kupi.cc.auth.configuration.ResourceBundleMessageSourceBean;
import com.kupi.cc.auth.configuration.Translator;
import com.kupi.cc.auth.domain.entity.*;
import com.kupi.cc.auth.domain.request.*;
import com.kupi.cc.auth.domain.response.MenuImageResponseDto;
import com.kupi.cc.auth.domain.response.PendingContactResponseDto;
import com.kupi.cc.auth.domain.response.SliderImageItemResponseDto;
import com.kupi.cc.auth.domain.response.SliderImageListResponseDto;
import com.kupi.cc.auth.enums.*;
import com.kupi.cc.auth.exception.*;
import com.kupi.cc.auth.service.BusinessMenuImageService;
import com.kupi.cc.auth.service.MerchantService;
import com.kupi.cc.auth.service.SliderImageService;
import com.kupi.cc.auth.service.UserService;
import com.kupi.cc.auth.util.Validator;
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

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests the {@link MerchantController } class.
 */
class MerchantControllerTest {

    protected static final String HEADER_USER_ID = "User-Id";
    private static final int PAGE = 0;
    private static final int SIZE = 1;
    private static final int TOTAL_ITEMS = 1;
    private static final int TOTAL_PAGE = 1;
    private static final String MERCHANT_ID = "uid-bd0c8894-8ddb-4b1d-8383-de44bfaa297f";
    private static final String USER_ID = "uid-bd0c8794-8ddb-4b1d-8381-de44bfaa297f";
    private static final String BUSINESS_ID = "bisid-123";
    private static final String BUSINESS_NAME = "Singer";
    private static final String OWNER_NAME = "Owner";
    private static final String COMMENT = "COMMENT";
    private static final String EMAIL = "singer@gmail.com";
    private static final String WEB_SITE = "https://www.singersl.com/";
    private static final String IMAGE_URL = "https://www.singersl.com/image.jpg";
    private static final String APPLICATION_JSON_UTF_8 = "application/json;charset=UTF-8";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String CREATE_OR_UPDATE_BUSINESS_URI = "/api/v1/users/MERCHANT/business";
    private static final String LIST_PENDING_MERCHANT_URI = "/api/v1/users/MERCHANT/PENDING/{page}/{size}/search/{searchTerm}";
    private static final String GET_MERCHANT_SUMMARY_URI = "/api/v1/users/MERCHANT/{merchantId}";
    private static final String CREATE_OR_UPDATE_CONTACT_INFO_URL = "/api/v1/users/{roleType}/contact";
    private static final String APPROVE_OR_REJECT_MERCHANT_URI = "/api/v1/users/MERCHANT/approve";
    private static final String GET_BUSINESS_BY_ID_URL = "/api/v1/users/{roleType}/business/{businessId}";
    private static final String GET_CONTACT_PROFILE = "/api/v1/users/MERCHANT/contact/merchant/{merchantId}";
    private static final String LIST_PENDING_BUSINESS_INFO_URI = "/api/v1/users/{roleType}/business/PENDING/{page}/{size}/search/{searchTerm}";
    private static final String APPROVE_OR_REJECT_BUSINESS_URI = "/api/v1/users/{roleType}/business/approve";
    private static final String GET_APPROVED_BUSINESS_PROFILE_URL = "/api/v1/users/{roleType}/business/merchant/{toUserId}/APPROVED";
    private static final String LIST_PENDING_CONTACT_INFO_URI = "/api/v1/users/MERCHANT/contact/PENDING/{page}/{size}/search/{searchTerm}";
    private static final String GET_BULK_MERCHANT_URI = "/api/v1/users/MERCHANT/bulk-info";
    private static final String GET_CONTACT_BY_CONTACT_ID_URI = "/api/v1/users/{roleType}/contact/{contactId}";
    private static final String APPROVE_OR_REJECT_CONTACT = "/api/v1/users/{roleType}/contact/approve";
    private static final String SEARCH_TERM = "SEARCH TERM";
    private static final String UPDATED_AT = "updatedAt";
    private static final String BLOCK_AND_UNBLOCK_URI = "/api/v1/users/MERCHANT/block";
    private static final String GET_BUSINESS_PROFILE_URL = "/api/v1/users/{roleType}/business/merchant/{toUserId}";
    private static final String LIST_ACTIVE_MERCHANT_OR_BANK_URI = "/api/v1/users/MERCHANT/active/{page}/{size}/search/{searchTerm}";
    private static final String SAVE_MENU_IMAGE_URI = "/api/v1/users/{roleType}/business/menu-image";
    private static final String GET_MENU_IMAGE_URI = "/api/v1/users/{roleType}/business/menu-image/{merchantId}";
    private static final String ADD_SLIDER_IMAGES_URI = "/api/v1/users/{roleType}/business/slider-images";
    private static final String GET_SLIDER_IMAGES_URI = "/api/v1/users/{roleType}/business/slider-images/{merchantId}";
    private final MobileNoRequestDto mobileNo = new MobileNoRequestDto("+94", "713321911");
    private final MobileNoRequestDto telephone = new MobileNoRequestDto("+94", "1123564889");
    private final Business business = getSampleBusiness();
    private final Locale locale = LocaleContextHolder.getLocale();
    private final BusinessRequestDto businessRequestDto = getSampleBusinessRequestDto();
    private final ResourceBundleMessageSourceBean resourceBundleMessageSourceBean = new ResourceBundleMessageSourceBean();
    private MockMvc mockMvc;
    private ResourceBundleMessageSource resourceBundleMessageSource;

    @Mock
    private MerchantService merchantService;
    @Mock
    private Validator validator;
    @Mock
    private UserService userService;
    @Mock
    private SliderImageService sliderImageService;
    @Mock
    private BusinessMenuImageService businessMenuImageService;
    @Mock
    private Translator translator;


    @BeforeEach
    void setUp() {
        initMocks(this);
        MerchantController merchantController = new MerchantController(translator, merchantService, validator, userService, sliderImageService, businessMenuImageService);

        // Create a no-op Spring Validator for MockMvc setup
        org.springframework.validation.Validator noOpValidator = new org.springframework.validation.Validator() {
            @Override
            public boolean supports(Class<?> clazz) {
                return true;
            }

            @Override
            public void validate(Object target, Errors errors) {
                // No-op validator - doesn't validate anything
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(merchantController)
                .setValidator(noOpValidator)
                .build();
        this.resourceBundleMessageSource = resourceBundleMessageSourceBean.messageSource();

        when(validator.isValidMobileNoWithCountryCode("+94-1123564889")).thenReturn(true);
        when(validator.isValidMobileNoWithCountryCode("+94-713321911")).thenReturn(true);
        when(validator.isValidEmail(EMAIL)).thenReturn(true);
        when(validator.isValidUrl(WEB_SITE)).thenReturn(true);
    }

    @Test
    void Should_ReturnOk_When_CreatingOrUpdatingBusinessInfo() throws Exception {
        String successMessage = resourceBundleMessageSource
                .getMessage(SuccessResponseStatusType.UPDATED_BUSINESS_PROFILE.getCode(), null, locale);
        when(translator.toLocale(SuccessResponseStatusType.UPDATED_BUSINESS_PROFILE.getCode()))
                .thenReturn(successMessage);
        businessRequestDto.setDescription("Description");
        when(merchantService.createOrUpdateBusiness(any(BusinessRequestDto.class), eq(RoleType.MERCHANT)))
                .thenReturn(business);
        mockMvc.perform(MockMvcRequestBuilders.put(CREATE_OR_UPDATE_BUSINESS_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .content(businessRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.UPDATED_BUSINESS_PROFILE.getMessage()))
                .andExpect(jsonPath("$.data.businessId", startsWith("bisid-")))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingOrUpdatingBusinessInfoWithoutRequiredFields() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                .getCodeString(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                .getCodeString(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode())))
                .thenReturn(invalidMessage);
        businessRequestDto.setMerchantId(null);
        mockMvc.perform(MockMvcRequestBuilders.put(CREATE_OR_UPDATE_BUSINESS_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .content(businessRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message", is("Required fields are missing")))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .MISSING_REQUIRED_FIELDS.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingOrUpdatingBusinessInfoWithInvalidTelephoneNo() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.INVALID_TELEPHONE_NUMBER
                .getCodeString(ErrorResponseStatusType.INVALID_TELEPHONE_NUMBER.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_TELEPHONE_NUMBER
                .getCodeString(ErrorResponseStatusType.INVALID_TELEPHONE_NUMBER.getCode())))
                .thenReturn(invalidMessage);
        businessRequestDto.setTelephone(new MobileNoRequestDto("+94", "11235648e9"));
        mockMvc.perform(MockMvcRequestBuilders.put(CREATE_OR_UPDATE_BUSINESS_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .content(businessRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message", is("Invalid telephone number.")))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .INVALID_TELEPHONE_NUMBER.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingOrUpdatingBusinessInfoWithInvalidEmailAddress() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.INVALID_EMAIL
                .getCodeString(ErrorResponseStatusType.INVALID_EMAIL.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_EMAIL
                .getCodeString(ErrorResponseStatusType.INVALID_EMAIL.getCode())))
                .thenReturn(invalidMessage);
        businessRequestDto.setEmail("singer#gmail.com");
        businessRequestDto.setDescription("Description");
        mockMvc.perform(MockMvcRequestBuilders.put(CREATE_OR_UPDATE_BUSINESS_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .content(businessRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message", is("Invalid email address")))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .INVALID_EMAIL.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingOrUpdatingBusinessInfoWithInvalidUrls() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.INVALID_URLS
                .getCodeString(ErrorResponseStatusType.INVALID_URLS.getCode()), null, locale);
        businessRequestDto.setDescription("Description");
        when(translator.toLocale(ErrorResponseStatusType.INVALID_URLS
                .getCodeString(ErrorResponseStatusType.INVALID_URLS.getCode())))
                .thenReturn(invalidMessage);
        businessRequestDto.setWebSite("htt://www.singersl.com/");
        mockMvc.perform(MockMvcRequestBuilders.put(CREATE_OR_UPDATE_BUSINESS_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .content(businessRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message", is("Invalid URL for website or facebook or instagram.")))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .INVALID_URLS.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingOrUpdatingBusinessInfoWithInvalidMobileNo() throws Exception {
        businessRequestDto.setMobileNo(null);
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.INVALID_MOBILE_NUMBER
                .getCodeString(ErrorResponseStatusType.INVALID_MOBILE_NUMBER.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_MOBILE_NUMBER
                .getCodeString(ErrorResponseStatusType.INVALID_MOBILE_NUMBER.getCode())))
                .thenReturn(invalidMessage);
        businessRequestDto.setDescription("Description");
        mockMvc.perform(MockMvcRequestBuilders.put(CREATE_OR_UPDATE_BUSINESS_URI)
                        .header("Time-Zone", "AsiaColombo")
                        .content(businessRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_MOBILE_NUMBER
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .INVALID_MOBILE_NUMBER.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingOrUpdatingBusinessInfoForInvalidMerchantId() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.INVALID_MERCHANT_ID
                .getCodeString(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_MERCHANT_ID
                .getCodeString(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode())))
                .thenReturn(invalidMessage);
        when(merchantService.createOrUpdateBusiness(any(BusinessRequestDto.class), eq(RoleType.MERCHANT)))
                .thenThrow(new InvalidUserException("Failed") {
                });
        businessRequestDto.setDescription("Description");
        mockMvc.perform(MockMvcRequestBuilders.put(CREATE_OR_UPDATE_BUSINESS_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .content(businessRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message", is("Invalid merchant id")))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .INVALID_MERCHANT_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_CreatingOrUpdatingBusinessInfo() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                .getCodeString(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                .getCodeString(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        when(merchantService.createOrUpdateBusiness(any(BusinessRequestDto.class), eq(RoleType.MERCHANT)))
                .thenThrow(new AuthServiceException("Failed") {
                });
        businessRequestDto.setDescription("Description");
        mockMvc.perform(MockMvcRequestBuilders.put(CREATE_OR_UPDATE_BUSINESS_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .content(businessRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message", is("Failed due to an internal server error")))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnOk_When_ValidFieldsAreProvidedForListPendingMerchant() throws Exception {
        Page<User> merchantPage = getSampleMerchantPage();
        when(merchantService.getPendingMerchant(eq(PAGE), eq(SIZE), anyString())).thenReturn(merchantPage);
        String url = LIST_PENDING_MERCHANT_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.GET_PENDING_MERCHANTS.getMessage()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE));
    }

    @Test
    void Should_ReturnInternalServerError_When_ListPendingMerchantIsFailed() throws Exception {
        Page<User> merchantPage = getSampleMerchantPage();
        when(merchantService.getPendingMerchant(eq(PAGE), eq(SIZE), anyString())).thenReturn(merchantPage);
        doThrow(new AuthServiceException("ERROR")).when(merchantService).getPendingMerchant(PAGE, SIZE, SEARCH_TERM);
        String url = LIST_PENDING_MERCHANT_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_MerchantSummeryReturnSuccessfully() throws Exception {
        User merchant = getSampleMerchant();
        when(userService.getUserByUserId(MERCHANT_ID)).thenReturn(merchant);
        String uri = GET_MERCHANT_SUMMARY_URI.replace("{merchantId}", MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.GET_MERCHANT.getMessage()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidMerchantIdIsProvidedForGetMerchantSummary() throws Exception {
        doThrow(new InvalidUserException("ERROR")).when(userService).getUserByUserId(MERCHANT_ID);
        String uri = GET_MERCHANT_SUMMARY_URI.replace("{merchantId}", MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetMerchantSummaryIsFailed() throws Exception {
        doThrow(new AuthServiceException("ERROR")).when(userService).getUserByUserId(MERCHANT_ID);
        String uri = GET_MERCHANT_SUMMARY_URI.replace("{merchantId}", MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_BusinessProfileIsAvailable() throws Exception {
        Business business = getSampleBusiness();
        Optional<Business> optionalBusiness = Optional.of(business);
        when(merchantService.getLatestBusinessInfoByUserId(MERCHANT_ID, RoleType.MERCHANT)).thenReturn(optionalBusiness);
        String url = GET_BUSINESS_PROFILE_URL.replace("{toUserId}", MERCHANT_ID)
                .replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.GET_BUSINESS_PROFILE.getMessage()));
    }

    @Test
    void Should_ReturnOk_When_BusinessProfileIsUnavailable() throws Exception {
        Business business = getSampleBusiness();
        Optional<Business> optionalBusiness = Optional.empty();
        when(merchantService.getLatestBusinessInfoByUserId(MERCHANT_ID, RoleType.MERCHANT)).thenReturn(optionalBusiness);
        String url = GET_BUSINESS_PROFILE_URL.replace("{toUserId}", MERCHANT_ID)
                .replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.NO_BUSINESS_INFO_FOUND.getMessage()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidRoleTypeIsProvidedForGetBusinessProfile() throws Exception {
        String url = GET_BUSINESS_PROFILE_URL.replace("{toUserId}", MERCHANT_ID)
                .replace("{roleType}", RoleType.ADMIN.name());
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_TYPE.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .INVALID_USER_TYPE.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidMerchantIdIsProvidedForGetBusinessProfile() throws Exception {
        doThrow(new InvalidUserException("ERROR")).when(merchantService).getLatestBusinessInfoByUserId(MERCHANT_ID, RoleType.MERCHANT);
        String url = GET_BUSINESS_PROFILE_URL.replace("{toUserId}", MERCHANT_ID)
                .replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .INVALID_MERCHANT_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetBusinessProfileIsFailed() throws Exception {
        doThrow(new AuthServiceException("ERROR")).when(merchantService).getLatestBusinessInfoByUserId(MERCHANT_ID, RoleType.MERCHANT);
        String url = GET_BUSINESS_PROFILE_URL.replace("{toUserId}", MERCHANT_ID)
                .replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_MerchantActionIsBlockForBlockOrUnblockMerchant() throws Exception {
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        merchantStatusUpdateRequestDto.setAction(ApprovalAction.BLOCK);
        mockMvc.perform(MockMvcRequestBuilders.put(BLOCK_AND_UNBLOCK_URI)
                        .header(HEADER_USER_ID, USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(merchantStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.BLOCKED_USER.getMessage()))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    void Should_ReturnOk_When_MerchantActionIsUnblockForBlockOrUnblockMerchant() throws Exception {
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        merchantStatusUpdateRequestDto.setAction(ApprovalAction.UNBLOCK);
        mockMvc.perform(MockMvcRequestBuilders.put(BLOCK_AND_UNBLOCK_URI)
                        .header(HEADER_USER_ID, USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(merchantStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.UNBLOCKED_USER.getMessage()))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    void Should_ReturnBadRequest_When_RequiredFieldsAreMissingForBlockOrUnblockMerchant() throws Exception {
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        merchantStatusUpdateRequestDto.setMerchantId(null);
        mockMvc.perform(MockMvcRequestBuilders.put(BLOCK_AND_UNBLOCK_URI)
                        .header(HEADER_USER_ID, USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(merchantStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .MISSING_REQUIRED_FIELDS.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidStatusActionIsProvidedForBlockOrUnblockMerchant() throws Exception {
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        doThrow(new InvalidActionException("ERROR")).when(merchantService).updateMerchantApprovalStatus(any(MerchantStatusUpdateRequestDto.class), anyString());
        mockMvc.perform(MockMvcRequestBuilders.put(BLOCK_AND_UNBLOCK_URI)
                        .header(HEADER_USER_ID, USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(merchantStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_ACTION
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .INVALID_ACTION.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidMerchantIdIsProvidedForBlockOrUnblockMerchant() throws Exception {
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        doThrow(new InvalidUserException("ERROR")).when(merchantService).updateMerchantApprovalStatus(any(MerchantStatusUpdateRequestDto.class), anyString());
        mockMvc.perform(MockMvcRequestBuilders.put(BLOCK_AND_UNBLOCK_URI)
                        .header(HEADER_USER_ID, USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(merchantStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_ID
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .INVALID_USER_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_BlockOrUnblockMerchantIsFailed() throws Exception {
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        doThrow(new AuthServiceException("ERROR")).when(merchantService).updateMerchantApprovalStatus(any(MerchantStatusUpdateRequestDto.class), anyString());
        mockMvc.perform(MockMvcRequestBuilders.put(BLOCK_AND_UNBLOCK_URI)
                        .header(HEADER_USER_ID, USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .content(merchantStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidFieldsAreProvidedForListActiveMerchantOrBank() throws Exception {
        String uri = LIST_ACTIVE_MERCHANT_OR_BANK_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{searchTerm}", SEARCH_TERM);
        Page<Business> businesses = getSampleBusinessPage();
        when(merchantService.getActiveBusinessProfiles(any(RoleType.class), any(Pageable.class), anyString())).thenReturn(businesses);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.ACTIVE_BUSINESS_PROFILES.getMessage()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE));

    }

    @Test
    void Should_ReturnInternalServer_When_ListActiveMerchantOrBankIsFailed() throws Exception {
        String uri = LIST_ACTIVE_MERCHANT_OR_BANK_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{searchTerm}", SEARCH_TERM);
        doThrow(new AuthServiceException("ERROR")).when(merchantService)
                .getActiveBusinessProfiles(any(RoleType.class), any(Pageable.class), anyString());
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidRequiredFieldsAreProvidedForCreateOrUpdateContactInfo() throws Exception {
        ContactRequestDto contactRequestDto = getSampleContactRequestDto();
        Contact contact = getSampleContact();
        when(validator.isValidMobileNoWithCountryCode(contactRequestDto.getTelephone().getNo())).thenReturn(true);
        when(validator.isValidEmail(contactRequestDto.getEmail())).thenReturn(true);
        when(merchantService.createOrUpdateContact(any(ContactRequestDto.class), any(RoleType.class))).thenReturn(contact);
        String url = CREATE_OR_UPDATE_CONTACT_INFO_URL.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("Time-Zone", TIME_ZONE)
                        .content(contactRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.UPDATED_CONTACT_PROFILE.getMessage()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidRoleTypeIsProvidedForCreateOrUpdateContactInfo() throws Exception {
        ContactRequestDto contactRequestDto = getSampleContactRequestDto();
        String url = CREATE_OR_UPDATE_CONTACT_INFO_URL.replace("{roleType}", RoleType.ADMIN.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("Time-Zone", TIME_ZONE)
                        .content(contactRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_TYPE.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_TYPE.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_MissingRequiredFieldsForCreateOrUpdateContactInfo() throws Exception {
        ContactRequestDto contactRequestDto = getSampleContactRequestDto();
        contactRequestDto.setMerchantId(null);
        String url = CREATE_OR_UPDATE_CONTACT_INFO_URL.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("Time-Zone", TIME_ZONE)
                        .content(contactRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidMobileNumberIsProvidedForCreateOrUpdateContactInfo() throws Exception {
        ContactRequestDto contactRequestDto = getSampleContactRequestDto();
        contactRequestDto.getTelephone().setCountryCode("65");
        when(validator.isValidMobileNoWithCountryCode(contactRequestDto.getTelephone().getNo())).thenReturn(false);
        String url = CREATE_OR_UPDATE_CONTACT_INFO_URL.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("Time-Zone", TIME_ZONE)
                        .content(contactRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_TELEPHONE_NUMBER.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_TELEPHONE_NUMBER.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidEmailIsProvidedForCreateOrUpdateContactInfo() throws Exception {
        ContactRequestDto contactRequestDto = getSampleContactRequestDto();
        contactRequestDto.setEmail("danu@gmail12.com");
        when(validator.isValidMobileNoWithCountryCode(contactRequestDto.getTelephone().getNo())).thenReturn(true);
        when(validator.isValidEmail(contactRequestDto.getEmail())).thenReturn(false);
        String url = CREATE_OR_UPDATE_CONTACT_INFO_URL.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("Time-Zone", TIME_ZONE)
                        .content(contactRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_EMAIL.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_EMAIL.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidMerchantIdIsProvidedForCreateOrUpdateContactInfo() throws Exception {
        ContactRequestDto contactRequestDto = getSampleContactRequestDto();
        when(validator.isValidMobileNoWithCountryCode(contactRequestDto.getTelephone().getNo())).thenReturn(true);
        when(validator.isValidEmail(contactRequestDto.getEmail())).thenReturn(true);
        doThrow(new InvalidUserException("ERROR")).when(merchantService)
                .createOrUpdateContact(any(ContactRequestDto.class), any(RoleType.class));
        String url = CREATE_OR_UPDATE_CONTACT_INFO_URL.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("Time-Zone", TIME_ZONE)
                        .content(contactRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_CreateOrUpdateContactInfoIsFailed() throws Exception {
        ContactRequestDto contactRequestDto = getSampleContactRequestDto();
        when(validator.isValidMobileNoWithCountryCode(contactRequestDto.getTelephone().getNo())).thenReturn(true);
        when(validator.isValidEmail(contactRequestDto.getEmail())).thenReturn(true);
        doThrow(new AuthServiceException("ERROR")).when(merchantService)
                .createOrUpdateContact(any(ContactRequestDto.class), any(RoleType.class));
        String url = CREATE_OR_UPDATE_CONTACT_INFO_URL.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(url)
                        .header("Time-Zone", TIME_ZONE)
                        .content(contactRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_ApproveOrRejectMerchant() throws Exception {
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        mockMvc.perform(MockMvcRequestBuilders.put(APPROVE_OR_REJECT_MERCHANT_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .content(merchantStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.APPROVED_USER.getMessage()));
    }

    @Test
    void Should_ReturnBadRequest_When_MissingRequiredFieldsAreProvidedForApproveOrRejectMerchant() throws Exception {
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        merchantStatusUpdateRequestDto.setMerchantId(null);
        mockMvc.perform(MockMvcRequestBuilders.put(APPROVE_OR_REJECT_MERCHANT_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .content(merchantStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidActionIsProvidedForApproveOrRejectMerchant() throws Exception {
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        doThrow(new InvalidActionException("ERROR")).when(merchantService)
                .updateMerchantApprovalStatus(any(MerchantStatusUpdateRequestDto.class), anyString());
        mockMvc.perform(MockMvcRequestBuilders.put(APPROVE_OR_REJECT_MERCHANT_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .content(merchantStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_ACTION.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_ACTION.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidMerchantIdIsProvidedForApproveOrRejectMerchant() throws Exception {
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        doThrow(new InvalidUserException("ERROR")).when(merchantService)
                .updateMerchantApprovalStatus(any(MerchantStatusUpdateRequestDto.class), anyString());
        mockMvc.perform(MockMvcRequestBuilders.put(APPROVE_OR_REJECT_MERCHANT_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .content(merchantStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_USER_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_USER_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_ApproveOrRejectMerchantIsFailed() throws Exception {
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = getSampleMerchantStatusUpdateRequestDto();
        doThrow(new AuthServiceException("ERROR")).when(merchantService)
                .updateMerchantApprovalStatus(any(MerchantStatusUpdateRequestDto.class), anyString());
        mockMvc.perform(MockMvcRequestBuilders.put(APPROVE_OR_REJECT_MERCHANT_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .content(merchantStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidBusinessIdIsProvidedForGetBusinessByBusinessId() throws Exception {
        Business business = getSampleBusiness();
        when(merchantService.getBusinessInfoByBusinessId(BUSINESS_ID, RoleType.MERCHANT)).thenReturn(business);
        String uri = GET_BUSINESS_BY_ID_URL.replace("{businessId}", BUSINESS_ID)
                .replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.GET_BUSINESS_PROFILE.getMessage()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidUserTypeIsProvidedForGetBusinessByBusinessId() throws Exception {
        String uri = GET_BUSINESS_BY_ID_URL.replace("{businessId}", BUSINESS_ID)
                .replace("{roleType}", RoleType.ADMIN.name());
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_TYPE.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .INVALID_USER_TYPE.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidBusinessIdIsProvidedForGetBusinessByBusinessId() throws Exception {
        doThrow(new BusinessProfileException("ERROR")).when(merchantService).getBusinessInfoByBusinessId(BUSINESS_ID, RoleType.MERCHANT);
        String uri = GET_BUSINESS_BY_ID_URL.replace("{businessId}", BUSINESS_ID)
                .replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_BUSINESS_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_BUSINESS_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetBusinessByBusinessIdIsFailed() throws Exception {
        doThrow(new AuthServiceException("ERROR")).when(merchantService).getBusinessInfoByBusinessId(BUSINESS_ID, RoleType.MERCHANT);
        String uri = GET_BUSINESS_BY_ID_URL.replace("{businessId}", BUSINESS_ID)
                .replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidContactIdIsProvidedForGetContactByContactId() throws Exception {
        Contact contact = getSampleContact();
        when(merchantService.getContactInfoByContactId(MERCHANT_ID)).thenReturn(contact);
        String uri = GET_CONTACT_BY_CONTACT_ID_URI.replace("{contactId}", MERCHANT_ID)
                .replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.GET_CONTACT_PROFILE.getMessage()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidUserTypeIsProvidedForGetContactByContactId() throws Exception {
        String uri = GET_CONTACT_BY_CONTACT_ID_URI.replace("{contactId}", MERCHANT_ID)
                .replace("{roleType}", RoleType.ADMIN.name());
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_TYPE.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_TYPE.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidContactIdIsProvidedForGetContactByContactId() throws Exception {
        doThrow(new ContactProfileException("ERROR")).when(merchantService).getContactInfoByContactId(MERCHANT_ID);
        String uri = GET_CONTACT_BY_CONTACT_ID_URI.replace("{contactId}", MERCHANT_ID)
                .replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_CONTACT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_CONTACT_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetContactByContactIdIsFailed() throws Exception {
        doThrow(new AuthServiceException("ERROR")).when(merchantService).getContactInfoByContactId(MERCHANT_ID);
        String uri = GET_CONTACT_BY_CONTACT_ID_URI.replace("{contactId}", MERCHANT_ID)
                .replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_RoleTypeIsMerchantForListPendingBusinessInfo() throws Exception {
        Page<Business> businesses = getSampleBusinessPage();
        when(merchantService.getPendingBusinessInfoList(PAGE, SIZE, SEARCH_TERM)).thenReturn(businesses);
        String uri = LIST_PENDING_BUSINESS_INFO_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{searchTerm}", SEARCH_TERM)
                .replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.GET_PENDING_BUSINESS_INFO.getMessage()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE));
    }

    @Test
    void Should_ReturnOk_When_RoleTypeIsBankForListPendingBusinessInfo() throws Exception {
        Page<BankBusiness> bankBusinessPage = getSampleBankBusinessPage();
        when(merchantService.getPendingBankBusinessInfoList(PAGE, SIZE, SEARCH_TERM)).thenReturn(bankBusinessPage);
        String uri = LIST_PENDING_BUSINESS_INFO_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{searchTerm}", SEARCH_TERM)
                .replace("{roleType}", RoleType.BANK.name());
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.GET_PENDING_BUSINESS_INFO.getMessage()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGE))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidRoleTypeIsProvidedForListPendingBusinessInfo() throws Exception {
        String uri = LIST_PENDING_BUSINESS_INFO_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{searchTerm}", SEARCH_TERM)
                .replace("{roleType}", RoleType.ADMIN.name());
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_TYPE.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_TYPE.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_ListPendingBusinessInfo() throws Exception {
        doThrow(new AuthServiceException("ERROR")).when(merchantService).getPendingBusinessInfoList(PAGE, SIZE, SEARCH_TERM);
        String uri = LIST_PENDING_BUSINESS_INFO_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{searchTerm}", SEARCH_TERM)
                .replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_ContactIsAvaliableForGetContactProfile() throws Exception {
        Contact contact = getSampleContact();
        Optional<Contact> optionalContact = Optional.of(contact);
        when(merchantService.getContactInfoByMerchantId(MERCHANT_ID)).thenReturn(optionalContact);
        String uri = GET_CONTACT_PROFILE.replace("{merchantId}", MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.GET_CONTACT_PROFILE.getMessage()));
    }

    @Test
    void Should_ReturnOk_When_ContactIsUnavaliableForGetContactProfile() throws Exception {
        when(merchantService.getContactInfoByMerchantId(MERCHANT_ID)).thenReturn(Optional.empty());
        String uri = GET_CONTACT_PROFILE.replace("{merchantId}", MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.NO_CONTACT_INFO_FOUND.getMessage()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidMerchantIdIsProvidedForGetContactProfile() throws Exception {
        doThrow(new InvalidUserException("ERROR")).when(merchantService).validateMerchantId(MERCHANT_ID);
        String uri = GET_CONTACT_PROFILE.replace("{merchantId}", MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_MERCHANT_ID
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .INVALID_MERCHANT_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetContactProfileIsFailed() throws Exception {
        doThrow(new AuthServiceException("ERROR")).when(merchantService).validateMerchantId(MERCHANT_ID);
        String uri = GET_CONTACT_PROFILE.replace("{merchantId}", MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_ApprovalActionIsEqualToApproveForApproveOrRejectBusiness() throws Exception {
        MerchantInfoStatusUpdateRequestDto merchantInfoStatusUpdateRequestDto = getSampleMerchantInfoStatusUpdateRequestDto();
        merchantInfoStatusUpdateRequestDto.setAction(ApprovalAction.APPROVE);
        String uri = APPROVE_OR_REJECT_BUSINESS_URI.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .content(merchantInfoStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.APPROVED_BUSINESS_INFO.getMessage()));
    }

    @Test
    void Should_ReturnOk_When_ApprovalActionIsEqualToRejectForApproveOrRejectBusiness() throws Exception {
        MerchantInfoStatusUpdateRequestDto merchantInfoStatusUpdateRequestDto = getSampleMerchantInfoStatusUpdateRequestDto();
        merchantInfoStatusUpdateRequestDto.setAction(ApprovalAction.REJECT);
        String uri = APPROVE_OR_REJECT_BUSINESS_URI.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .content(merchantInfoStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.REJECTED_BUSINESS_INFO.getMessage()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidUserTypeIsProvidedForApproveOrRejectBusiness() throws Exception {
        MerchantInfoStatusUpdateRequestDto merchantInfoStatusUpdateRequestDto = getSampleMerchantInfoStatusUpdateRequestDto();
        String uri = APPROVE_OR_REJECT_BUSINESS_URI.replace("{roleType}", RoleType.ADMIN.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .content(merchantInfoStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_TYPE.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .INVALID_USER_TYPE.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_MissingRequiredFieldsAreProvidedForApproveOrRejectBusiness() throws Exception {
        MerchantInfoStatusUpdateRequestDto merchantInfoStatusUpdateRequestDto = getSampleMerchantInfoStatusUpdateRequestDto();
        merchantInfoStatusUpdateRequestDto.setId(null);
        String uri = APPROVE_OR_REJECT_BUSINESS_URI.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .content(merchantInfoStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .MISSING_REQUIRED_FIELDS.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidApprovalActionIsProvidedForApproveOrRejectBusiness() throws Exception {
        MerchantInfoStatusUpdateRequestDto merchantInfoStatusUpdateRequestDto = getSampleMerchantInfoStatusUpdateRequestDto();
        doThrow(new InvalidActionException("ERROR")).when(merchantService).updateMerchantBusinessApprovalStatus(
                any(RejectedProfileUpdates.class), any(ApprovalAction.class), anyString(), any(RoleType.class));
        String uri = APPROVE_OR_REJECT_BUSINESS_URI.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .content(merchantInfoStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_ACTION.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .INVALID_ACTION.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidBusinessIdIsProvidedForApproveOrRejectBusiness() throws Exception {
        MerchantInfoStatusUpdateRequestDto merchantInfoStatusUpdateRequestDto = getSampleMerchantInfoStatusUpdateRequestDto();
        doThrow(new InvalidUserException("ERROR")).when(merchantService).updateMerchantBusinessApprovalStatus(
                any(RejectedProfileUpdates.class), any(ApprovalAction.class), anyString(), any(RoleType.class));
        String uri = APPROVE_OR_REJECT_BUSINESS_URI.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .content(merchantInfoStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_BUSINESS_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .INVALID_BUSINESS_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_ApproveOrRejectBusinessIsFailed() throws Exception {
        MerchantInfoStatusUpdateRequestDto merchantInfoStatusUpdateRequestDto = getSampleMerchantInfoStatusUpdateRequestDto();
        doThrow(new AuthServiceException("ERROR")).when(merchantService).updateMerchantBusinessApprovalStatus(
                any(RejectedProfileUpdates.class), any(ApprovalAction.class), anyString(), any(RoleType.class));
        String uri = APPROVE_OR_REJECT_BUSINESS_URI.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .content(merchantInfoStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidMerchantIdIsProvidedForGetApprovedBusinessProfile() throws Exception {
        Business business = getSampleBusiness();
        when(merchantService.getApprovedBusinessInfoByUserId(MERCHANT_ID, RoleType.MERCHANT)).thenReturn(business);
        String uri = GET_APPROVED_BUSINESS_PROFILE_URL.replace("{roleType}", RoleType.MERCHANT.name())
                .replace("{toUserId}", MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.GET_BUSINESS_PROFILE.getMessage()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidUserTypeIsProvidedForGetApprovedBusinessProfile() throws Exception {
        String uri = GET_APPROVED_BUSINESS_PROFILE_URL.replace("{roleType}", RoleType.ADMIN.name())
                .replace("{toUserId}", MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_TYPE.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .INVALID_USER_TYPE.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_ApprovedBusinessNotFoundForGetApprovedBusinessProfile() throws Exception {
        when(merchantService.getApprovedBusinessInfoByUserId(MERCHANT_ID, RoleType.MERCHANT)).thenReturn(null);
        String uri = GET_APPROVED_BUSINESS_PROFILE_URL.replace("{roleType}", RoleType.MERCHANT.name())
                .replace("{toUserId}", MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.NO_APPROVED_BUSINESS_INFO_FOUND.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .NO_APPROVED_BUSINESS_INFO_FOUND.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidMerchantIdIsProvidedForGetApprovedBusinessProfile() throws Exception {
        doThrow(new InvalidUserException("ERROR")).when(merchantService).getApprovedBusinessInfoByUserId(MERCHANT_ID, RoleType.MERCHANT);
        String uri = GET_APPROVED_BUSINESS_PROFILE_URL.replace("{roleType}", RoleType.MERCHANT.name())
                .replace("{toUserId}", MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .INVALID_MERCHANT_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetApprovedBusinessProfileIsFailed() throws Exception {
        doThrow(new AuthServiceException("ERROR")).when(merchantService).getApprovedBusinessInfoByUserId(MERCHANT_ID, RoleType.MERCHANT);
        String uri = GET_APPROVED_BUSINESS_PROFILE_URL.replace("{roleType}", RoleType.MERCHANT.name())
                .replace("{toUserId}", MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_when_ValidFieldsAreProvidedForListPendingContactInfo() throws Exception {
        Page<Contact> contactPage = getSampleContactPage();
        PendingContactResponseDto pendingContactResponseDto = getSamplePendingContactResponseDto();
        List<PendingContactResponseDto> pendingContactResponseDtoList = new ArrayList<>();
        pendingContactResponseDtoList.add(pendingContactResponseDto);
        when(merchantService.getPendingContactInfoList(PAGE, SIZE, SEARCH_TERM, RoleType.MERCHANT))
                .thenReturn(contactPage);
        when(merchantService.getBusinessDetailsForContact(contactPage, RoleType.MERCHANT.name(), TIME_ZONE))
                .thenReturn(pendingContactResponseDtoList);
        String uri = LIST_PENDING_CONTACT_INFO_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.GET_PENDING_CONTACT_PROFILE.getMessage()));
    }

    @Test
    void Should_ReturnInternalServerError_When_ListPendingContactInfoIsFailed() throws Exception {
        doThrow(new AuthServiceException("ERROR")).when(merchantService)
                .getPendingContactInfoList(PAGE, SIZE, SEARCH_TERM, RoleType.MERCHANT);
        String uri = LIST_PENDING_CONTACT_INFO_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE)).replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidFieldsAreProvidedForGetBulkMerchants() throws Exception {
        List<Business> businessList = getSampleBusinessList();
        BulkUserRequestDto bulkUserRequestDto = getSampleBulkUserRequestDto();
        when(merchantService.getBulkMerchantList(bulkUserRequestDto.getUserIds(), RoleType.MERCHANT)).thenReturn(businessList);
        mockMvc.perform(MockMvcRequestBuilders.post(GET_BULK_MERCHANT_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .content(bulkUserRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.GET_MERCHANT.getMessage()));
    }

    @Test
    void Should_ReturnBadRequest_When_MissingRequiredFieldsAreProvidedForGetBulkMerchants() throws Exception {
        BulkUserRequestDto bulkUserRequestDto = getSampleBulkUserRequestDto();
        bulkUserRequestDto.setUserIds(null);
        mockMvc.perform(MockMvcRequestBuilders.post(GET_BULK_MERCHANT_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .content(bulkUserRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()));

    }

    @Test
    void Should_ReturnBadRequest_When_UserRequestLimitExceededForGetBulkMerchants() throws Exception {
        BulkUserRequestDto bulkUserRequestDto = getSampleBulkUserRequestDto();
        List<String> merchantIdList = new ArrayList<>();
        for (int i = 0; i < 255; i++) {
            merchantIdList.add(MERCHANT_ID);
        }
        bulkUserRequestDto.setUserIds(merchantIdList);
        mockMvc.perform(MockMvcRequestBuilders.post(GET_BULK_MERCHANT_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .content(bulkUserRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MAX_USER_REQUEST_COUNT.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MAX_USER_REQUEST_COUNT.getCode()));

    }

    @Test
    void Should_ReturnInternalServerError_When_GetBulkMerchantsIsFailed() throws Exception {
        List<Business> businessList = getSampleBusinessList();
        BulkUserRequestDto bulkUserRequestDto = getSampleBulkUserRequestDto();
        doThrow(new AuthServiceException("ERROR")).when(merchantService).getBulkMerchantList(bulkUserRequestDto.getUserIds(), RoleType.MERCHANT);
        mockMvc.perform(MockMvcRequestBuilders.post(GET_BULK_MERCHANT_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .content(bulkUserRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));

    }

    @Test
    void Should_ReturnOk_When_ApprovalActionIsEqualToApproveForApproveOrRejectContact() throws Exception {
        MerchantInfoStatusUpdateRequestDto merchantInfoStatusUpdateRequestDto = getSampleMerchantInfoStatusUpdateRequestDto();
        merchantInfoStatusUpdateRequestDto.setAction(ApprovalAction.APPROVE);
        String uri = APPROVE_OR_REJECT_CONTACT.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .content(merchantInfoStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.APPROVED_CONTACT_PROFILE.getMessage()));
    }

    @Test
    void Should_ReturnOk_When_ApprovalActionIsEqualToRejectForApproveOrRejectContact() throws Exception {
        MerchantInfoStatusUpdateRequestDto merchantInfoStatusUpdateRequestDto = getSampleMerchantInfoStatusUpdateRequestDto();
        merchantInfoStatusUpdateRequestDto.setAction(ApprovalAction.REJECT);
        String uri = APPROVE_OR_REJECT_CONTACT.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .content(merchantInfoStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.REJECTED_CONTACT_PROFILE.getMessage()));
    }

    @Test
    void Should_ReturnBadRequest_When_MissingRequiredFieldsAreProvidedForApproveOrRejectContact() throws Exception {
        MerchantInfoStatusUpdateRequestDto merchantInfoStatusUpdateRequestDto = getSampleMerchantInfoStatusUpdateRequestDto();
        merchantInfoStatusUpdateRequestDto.setId(null);
        String uri = APPROVE_OR_REJECT_CONTACT.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .content(merchantInfoStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidUserTypeIsProvidedForApproveOrRejectContact() throws Exception {
        MerchantInfoStatusUpdateRequestDto merchantInfoStatusUpdateRequestDto = getSampleMerchantInfoStatusUpdateRequestDto();
        String uri = APPROVE_OR_REJECT_CONTACT.replace("{roleType}", RoleType.ADMIN.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .content(merchantInfoStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_TYPE.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_TYPE.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidApprovalActionIsProvidedForApproveOrRejectContact() throws Exception {
        MerchantInfoStatusUpdateRequestDto merchantInfoStatusUpdateRequestDto = getSampleMerchantInfoStatusUpdateRequestDto();
        doThrow(new InvalidActionException("ERROR")).when(merchantService).updateMerchantContactApprovalStatus(any(RejectedProfileUpdates.class), any(ApprovalAction.class), anyString());
        String uri = APPROVE_OR_REJECT_CONTACT.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .content(merchantInfoStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_ACTION.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_ACTION.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidContactIdIsProvidedForApproveOrRejectContact() throws Exception {
        MerchantInfoStatusUpdateRequestDto merchantInfoStatusUpdateRequestDto = getSampleMerchantInfoStatusUpdateRequestDto();
        doThrow(new InvalidUserException("ERROR")).when(merchantService).updateMerchantContactApprovalStatus(any(RejectedProfileUpdates.class), any(ApprovalAction.class), anyString());
        String uri = APPROVE_OR_REJECT_CONTACT.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .content(merchantInfoStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_CONTACT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_CONTACT_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_ApproveOrRejectContactIsFailed() throws Exception {
        MerchantInfoStatusUpdateRequestDto merchantInfoStatusUpdateRequestDto = getSampleMerchantInfoStatusUpdateRequestDto();
        doThrow(new AuthServiceException("ERROR")).when(merchantService).updateMerchantContactApprovalStatus(any(RejectedProfileUpdates.class), any(ApprovalAction.class), anyString());
        String uri = APPROVE_OR_REJECT_CONTACT.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .header(HEADER_USER_ID, USER_ID)
                        .content(merchantInfoStatusUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_SavingMenuImageWithValidData() throws Exception {
        MenuImageRequestDto requestDto = getSampleMenuImageRequestDto();
        MenuImageResponseDto responseDto = getSampleMenuImageResponseDto();
        String successMessage = resourceBundleMessageSource
                .getMessage(SuccessResponseStatusType.MERCHANT_MENU_IMAGE_SAVED.getCode(), null, locale);
        when(translator.toLocale(SuccessResponseStatusType.MERCHANT_MENU_IMAGE_SAVED.getCode()))
                .thenReturn(successMessage);
        when(validator.isValidUrl(IMAGE_URL)).thenReturn(true);
        when(businessMenuImageService.saveMenuImage(any(MenuImageRequestDto.class))).thenReturn(responseDto);
        String uri = SAVE_MENU_IMAGE_URI.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .content(requestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.MERCHANT_MENU_IMAGE_SAVED.getMessage()))
                .andExpect(jsonPath("$.data.menuImageUrl", is(IMAGE_URL)))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_SavingMenuImageWithoutRequiredFields() throws Exception {
        MenuImageRequestDto requestDto = getSampleMenuImageRequestDto();
        requestDto.setMerchantId(null);
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                .getCodeString(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                .getCodeString(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode())))
                .thenReturn(invalidMessage);
        String uri = SAVE_MENU_IMAGE_URI.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .content(requestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message", is("Required fields are missing")))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .MISSING_REQUIRED_FIELDS.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_SavingMenuImageWithInvalidUrl() throws Exception {
        MenuImageRequestDto requestDto = getSampleMenuImageRequestDto();
        requestDto.setImageUrl("invalid-url");
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.INVALID_URLS
                .getCodeString(ErrorResponseStatusType.INVALID_URLS.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_URLS
                .getCodeString(ErrorResponseStatusType.INVALID_URLS.getCode())))
                .thenReturn(invalidMessage);
        when(validator.isValidUrl("invalid-url")).thenReturn(false);
        String uri = SAVE_MENU_IMAGE_URI.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .content(requestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message", is("Invalid URL for website or facebook or instagram.")))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_URLS.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_SavingMenuImageWithInvalidMerchantId() throws Exception {
        MenuImageRequestDto requestDto = getSampleMenuImageRequestDto();
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.INVALID_MERCHANT_ID
                .getCodeString(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_MERCHANT_ID
                .getCodeString(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode())))
                .thenReturn(invalidMessage);
        when(validator.isValidUrl(IMAGE_URL)).thenReturn(true);
        doThrow(new InvalidUserException("ERROR")).when(businessMenuImageService)
                .saveMenuImage(any(MenuImageRequestDto.class));
        String uri = SAVE_MENU_IMAGE_URI.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .content(requestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_SavingMenuImageFails() throws Exception {
        MenuImageRequestDto requestDto = getSampleMenuImageRequestDto();
        when(validator.isValidUrl(IMAGE_URL)).thenReturn(true);
        doThrow(new AuthServiceException("ERROR")).when(businessMenuImageService)
                .saveMenuImage(any(MenuImageRequestDto.class));
        String uri = SAVE_MENU_IMAGE_URI.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .content(requestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_GettingMenuImageWithValidMerchantId() throws Exception {
        MenuImageResponseDto responseDto = getSampleMenuImageResponseDto();
        String successMessage = resourceBundleMessageSource
                .getMessage(SuccessResponseStatusType.GET_MENU_IMAGE.getCode(), null, locale);
        when(translator.toLocale(SuccessResponseStatusType.GET_MENU_IMAGE.getCode()))
                .thenReturn(successMessage);
        when(businessMenuImageService.getMenuImage(MERCHANT_ID)).thenReturn(responseDto);
        String uri = GET_MENU_IMAGE_URI.replace("{roleType}", RoleType.MERCHANT.name())
                .replace("{merchantId}", MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.GET_MENU_IMAGE.getMessage()))
                .andExpect(jsonPath("$.data.menuImageUrl", is(IMAGE_URL)))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingMenuImageWithInvalidMerchantId() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.INVALID_MERCHANT_ID
                .getCodeString(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_MERCHANT_ID
                .getCodeString(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode())))
                .thenReturn(invalidMessage);
        doThrow(new InvalidUserException("ERROR")).when(businessMenuImageService).getMenuImage(MERCHANT_ID);
        String uri = GET_MENU_IMAGE_URI.replace("{roleType}", RoleType.MERCHANT.name())
                .replace("{merchantId}", MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingMenuImageFails() throws Exception {
        doThrow(new AuthServiceException("ERROR")).when(businessMenuImageService).getMenuImage(MERCHANT_ID);
        String uri = GET_MENU_IMAGE_URI.replace("{roleType}", RoleType.MERCHANT.name())
                .replace("{merchantId}", MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_AddingSliderImagesWithValidData() throws Exception {
        SliderImageCreateRequestDto requestDto = getSampleSliderImageCreateRequestDto();
        SliderImageListResponseDto responseDto = getSampleSliderImageListResponseDto();
        String successMessage = resourceBundleMessageSource
                .getMessage(SuccessResponseStatusType.SLIDER_IMAGES_ADDED.getCode(), null, locale);
        when(translator.toLocale(SuccessResponseStatusType.SLIDER_IMAGES_ADDED.getCode()))
                .thenReturn(successMessage);
        when(validator.isValidUrl(IMAGE_URL)).thenReturn(true);
        when(sliderImageService.addSliderImages(any(SliderImageCreateRequestDto.class))).thenReturn(responseDto);
        String uri = ADD_SLIDER_IMAGES_URI.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .content(sliderImageCreateRequestDtoToJson(requestDto))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.SLIDER_IMAGES_ADDED.getMessage()))
                .andExpect(jsonPath("$.data.merchantId", is(MERCHANT_ID)))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_AddingSliderImagesWithNullImages() throws Exception {
        SliderImageCreateRequestDto requestDto = getSampleSliderImageCreateRequestDto();
        requestDto.setImages(null);
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                .getCodeString(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                .getCodeString(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode())))
                .thenReturn(invalidMessage);
        String uri = ADD_SLIDER_IMAGES_URI.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .content(sliderImageCreateRequestDtoToJson(requestDto))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message", is("Required fields are missing")))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType
                        .MISSING_REQUIRED_FIELDS.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_AddingMoreThanThreeSliderImages() throws Exception {
        SliderImageCreateRequestDto requestDto = getSampleSliderImageCreateRequestDto();
        List<SliderImageItemDto> images = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            SliderImageItemDto item = new SliderImageItemDto();
            item.setImageUrl(IMAGE_URL);
            item.setDisplayOrder(i);
            images.add(item);
        }
        requestDto.setImages(images);
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.MAX_SLIDER_IMAGES_EXCEEDED
                .getCodeString(ErrorResponseStatusType.MAX_SLIDER_IMAGES_EXCEEDED.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.MAX_SLIDER_IMAGES_EXCEEDED
                .getCodeString(ErrorResponseStatusType.MAX_SLIDER_IMAGES_EXCEEDED.getCode())))
                .thenReturn(invalidMessage);
        when(validator.isValidUrl(IMAGE_URL)).thenReturn(true);
        String uri = ADD_SLIDER_IMAGES_URI.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .content(sliderImageCreateRequestDtoToJson(requestDto))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MAX_SLIDER_IMAGES_EXCEEDED.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MAX_SLIDER_IMAGES_EXCEEDED.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_AddingSliderImagesWithInvalidUrl() throws Exception {
        SliderImageCreateRequestDto requestDto = getSampleSliderImageCreateRequestDto();
        requestDto.getImages().get(0).setImageUrl("invalid-url");
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.INVALID_URLS
                .getCodeString(ErrorResponseStatusType.INVALID_URLS.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_URLS
                .getCodeString(ErrorResponseStatusType.INVALID_URLS.getCode())))
                .thenReturn(invalidMessage);
        when(validator.isValidUrl("invalid-url")).thenReturn(false);
        String uri = ADD_SLIDER_IMAGES_URI.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .content(sliderImageCreateRequestDtoToJson(requestDto))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message", is("Invalid URL for website or facebook or instagram.")))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_URLS.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_AddingSliderImagesWithInvalidMerchantId() throws Exception {
        SliderImageCreateRequestDto requestDto = getSampleSliderImageCreateRequestDto();
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.INVALID_MERCHANT_ID
                .getCodeString(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_MERCHANT_ID
                .getCodeString(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode())))
                .thenReturn(invalidMessage);
        when(validator.isValidUrl(IMAGE_URL)).thenReturn(true);
        doThrow(new InvalidUserException("ERROR")).when(sliderImageService)
                .addSliderImages(any(SliderImageCreateRequestDto.class));
        String uri = ADD_SLIDER_IMAGES_URI.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .content(sliderImageCreateRequestDtoToJson(requestDto))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_AddingSliderImagesExceedsLimit() throws Exception {
        SliderImageCreateRequestDto requestDto = getSampleSliderImageCreateRequestDto();
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.MAX_SLIDER_IMAGES_EXCEEDED
                .getCodeString(ErrorResponseStatusType.MAX_SLIDER_IMAGES_EXCEEDED.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.MAX_SLIDER_IMAGES_EXCEEDED
                .getCodeString(ErrorResponseStatusType.MAX_SLIDER_IMAGES_EXCEEDED.getCode())))
                .thenReturn(invalidMessage);
        when(validator.isValidUrl(IMAGE_URL)).thenReturn(true);
        doThrow(new SliderImageLimitExceededException("ERROR")).when(sliderImageService)
                .addSliderImages(any(SliderImageCreateRequestDto.class));
        String uri = ADD_SLIDER_IMAGES_URI.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .content(sliderImageCreateRequestDtoToJson(requestDto))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MAX_SLIDER_IMAGES_EXCEEDED.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MAX_SLIDER_IMAGES_EXCEEDED.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_AddingSliderImagesFails() throws Exception {
        SliderImageCreateRequestDto requestDto = getSampleSliderImageCreateRequestDto();
        when(validator.isValidUrl(IMAGE_URL)).thenReturn(true);
        doThrow(new AuthServiceException("ERROR")).when(sliderImageService)
                .addSliderImages(any(SliderImageCreateRequestDto.class));
        String uri = ADD_SLIDER_IMAGES_URI.replace("{roleType}", RoleType.MERCHANT.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .content(sliderImageCreateRequestDtoToJson(requestDto))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_GettingSliderImagesWithValidMerchantId() throws Exception {
        SliderImageListResponseDto responseDto = getSampleSliderImageListResponseDto();
        String successMessage = resourceBundleMessageSource
                .getMessage(SuccessResponseStatusType.GET_SLIDER_IMAGES.getCode(), null, locale);
        when(translator.toLocale(SuccessResponseStatusType.GET_SLIDER_IMAGES.getCode()))
                .thenReturn(successMessage);
        doNothing().when(merchantService).validateMerchantId(MERCHANT_ID);
        when(sliderImageService.getSliderImagesByMerchantId(MERCHANT_ID)).thenReturn(responseDto);
        String uri = GET_SLIDER_IMAGES_URI.replace("{roleType}", RoleType.MERCHANT.name())
                .replace("{merchantId}", MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.GET_SLIDER_IMAGES.getMessage()))
                .andExpect(jsonPath("$.data.merchantId", is(MERCHANT_ID)))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingSliderImagesWithInvalidMerchantId() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.INVALID_MERCHANT_ID
                .getCodeString(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_MERCHANT_ID
                .getCodeString(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode())))
                .thenReturn(invalidMessage);
        doThrow(new InvalidUserException("ERROR")).when(merchantService).validateMerchantId(MERCHANT_ID);
        String uri = GET_SLIDER_IMAGES_URI.replace("{roleType}", RoleType.MERCHANT.name())
                .replace("{merchantId}", MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_MERCHANT_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingSliderImagesFails() throws Exception {
        doNothing().when(merchantService).validateMerchantId(MERCHANT_ID);
        doThrow(new AuthServiceException("ERROR")).when(sliderImageService).getSliderImagesByMerchantId(MERCHANT_ID);
        String uri = GET_SLIDER_IMAGES_URI.replace("{roleType}", RoleType.MERCHANT.name())
                .replace("{merchantId}", MERCHANT_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    private BusinessRequestDto getSampleBusinessRequestDto() {
        BusinessRequestDto businessRequestDto = new BusinessRequestDto();
        businessRequestDto.setMerchantId(MERCHANT_ID);
        businessRequestDto.setBusinessName(BUSINESS_NAME);
        businessRequestDto.setOwnerName(OWNER_NAME);
        businessRequestDto.setMobileNo(mobileNo);
        businessRequestDto.setTelephone(telephone);
        businessRequestDto.setImageUrl(IMAGE_URL);
        businessRequestDto.setEmail(EMAIL);
        businessRequestDto.setWebSite(WEB_SITE);
        OpeningHoursRequestDto opening = new OpeningHoursRequestDto();
        opening.setMonday("9-5");
        opening.setTuesday("9-5");
        opening.setWednesday("9-5");
        opening.setThursday("9-5");
        opening.setFriday("9-5");
        opening.setSaturday("9-5");
        opening.setSunday("9-5");
        businessRequestDto.setOpeningHours(opening);
        return businessRequestDto;
    }

    private Business getSampleBusiness() {
        User merchant = new User();
        merchant.setId(MERCHANT_ID);
        merchant.setRole(new Role(RoleType.MERCHANT));
        merchant.setCreatedAt(new Date(System.currentTimeMillis()));
        merchant.setUpdatedAt(new Date(System.currentTimeMillis()));

        Business business = new Business();
        business.setId(BUSINESS_ID);
        business.setMerchant(merchant);
        business.setBusinessName(BUSINESS_NAME);
        business.setOwnerName(OWNER_NAME);
        business.setMobileNo(mobileNo.getNo());
        business.setTelephone(telephone.getNo());
        business.setImageUrl(IMAGE_URL);
        business.setEmail(EMAIL);
        business.setWebSite(WEB_SITE);
        business.setCreatedAt(new Date());
        business.setUpdatedAt(new Date());
        business.setOpeningHours(new OpeningHours());
        return business;
    }

    /**
     * This method creates sample Merchant
     *
     * @return User
     */
    private User getSampleMerchant() {
        User merchant = new User();
        merchant.setId(MERCHANT_ID);
        merchant.setFullName("Full name");
        merchant.setEmail("merchant@kupi.lk");
        merchant.setMobileNo("94-771109101");
        merchant.setApprovalStatus(ApprovalStatus.PENDING);
        merchant.setRegisteredUser(true);
        merchant.setCreatedAt(new Date(System.currentTimeMillis()));
        merchant.setUpdatedAt(new Date(System.currentTimeMillis()));
        Role role = new Role();
        role.setId(123);
        role.setName(RoleType.MERCHANT.name());
        merchant.setRole(role);
        return merchant;
    }

    /**
     * This method creates sample merchant page
     *
     * @return MerchantPage
     */
    private Page<User> getSampleMerchantPage() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.Direction.DESC, UPDATED_AT);
        User merchant = getSampleMerchant();
        List<User> merchantList = new ArrayList<>();
        merchantList.add(merchant);
        return new PageImpl<>(merchantList, pageable, 1);
    }

    /**
     * This method creates sample MerchantStatusUpdateRequestDto
     *
     * @return MerchantStatusUpdateRequestDto
     */
    private MerchantStatusUpdateRequestDto getSampleMerchantStatusUpdateRequestDto() {
        MerchantStatusUpdateRequestDto merchantStatusUpdateRequestDto = new MerchantStatusUpdateRequestDto();
        merchantStatusUpdateRequestDto.setMerchantId(MERCHANT_ID);
        merchantStatusUpdateRequestDto.setAction(ApprovalAction.APPROVE);
        merchantStatusUpdateRequestDto.setComment(COMMENT);
        return merchantStatusUpdateRequestDto;
    }

    /**
     * This method creates sample business page
     *
     * @return BusinessPage
     */
    Page<Business> getSampleBusinessPage() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        Business business = getSampleBusiness();
        List<Business> businesses = new ArrayList<>();
        businesses.add(business);
        return new PageImpl<>(businesses, pageable, 1);
    }

    /**
     * This method creates sample ContactRequestDto
     *
     * @return ContactRequestDto
     */
    private ContactRequestDto getSampleContactRequestDto() {
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto("+94", "123456789");
        return new ContactRequestDto(MERCHANT_ID, "NAME", "DESIGNATION", mobileNoRequestDto, "contact@kupi.lk");
    }

    /**
     * This method creates sample Contact
     *
     * @return Contact
     */
    private Contact getSampleContact() {
        ContactRequestDto contactRequestDto = getSampleContactRequestDto();
        User merchant = getSampleMerchant();
        return new Contact(contactRequestDto, merchant, RoleType.MERCHANT);
    }

    /**
     * This method creates sample BankBusiness
     *
     * @return BankBusiness
     */
    private BankBusiness getSampleBankBusiness() {
        Business business = getSampleBusiness();
        return new BankBusiness(business);
    }

    /**
     * This method creates sample BankBusinessPage
     *
     * @return BankBusinessPage
     */
    private Page<BankBusiness> getSampleBankBusinessPage() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        BankBusiness bankBusiness = getSampleBankBusiness();
        List<BankBusiness> bankBusinessList = new ArrayList<>();
        bankBusinessList.add(bankBusiness);
        return new PageImpl<>(bankBusinessList, pageable, 1);
    }

    /**
     * This method creates sample MerchantInfoStatusUpdateRequestDto
     *
     * @return MerchantInfoStatusUpdateRequestDto
     */
    private MerchantInfoStatusUpdateRequestDto getSampleMerchantInfoStatusUpdateRequestDto() {
        MerchantInfoStatusUpdateRequestDto merchantInfoStatusUpdateRequestDto = new MerchantInfoStatusUpdateRequestDto();
        merchantInfoStatusUpdateRequestDto.setId(MERCHANT_ID);
        merchantInfoStatusUpdateRequestDto.setAction(ApprovalAction.APPROVE);
        merchantInfoStatusUpdateRequestDto.setComment(COMMENT);
        return merchantInfoStatusUpdateRequestDto;
    }

    /**
     * THis method created sample PendingContactResponseDto
     *
     * @return PendingContactResponseDto
     */
    private PendingContactResponseDto getSamplePendingContactResponseDto() {
        Contact contact = getSampleContact();
        Business business = getSampleBusiness();
        return new PendingContactResponseDto(contact, TIME_ZONE, business);
    }

    /**
     * This method creates sample Contact Page
     *
     * @return ContactPage
     */
    private Page<Contact> getSampleContactPage() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.Direction.ASC, UPDATED_AT);
        Contact contact = getSampleContact();
        List<Contact> contactList = new ArrayList<>();
        contactList.add(contact);
        return new PageImpl<>(contactList, pageable, 1);
    }

    /**
     * This method creates sample BusinessList
     *
     * @return BusinessList
     */
    private List<Business> getSampleBusinessList() {
        Business business = getSampleBusiness();
        List<Business> businessList = new ArrayList<>();
        businessList.add(business);
        return businessList;
    }

    /**
     * This method creates sample BulkUserRequestDto
     *
     * @return BulkUserRequestDto
     */
    private BulkUserRequestDto getSampleBulkUserRequestDto() {
        List<String> merchantIdList = new ArrayList<>();
        merchantIdList.add(MERCHANT_ID);
        BulkUserRequestDto bulkUserRequestDto = new BulkUserRequestDto();
        bulkUserRequestDto.setUserIds(merchantIdList);
        return bulkUserRequestDto;
    }

    /**
     * This method creates sample MenuImageRequestDto
     *
     * @return MenuImageRequestDto
     */
    private MenuImageRequestDto getSampleMenuImageRequestDto() {
        MenuImageRequestDto requestDto = new MenuImageRequestDto();
        requestDto.setMerchantId(MERCHANT_ID);
        requestDto.setImageUrl(IMAGE_URL);
        return requestDto;
    }

    /**
     * This method creates sample MenuImageResponseDto
     *
     * @return MenuImageResponseDto
     */
    private MenuImageResponseDto getSampleMenuImageResponseDto() {
        BusinessMenuImage businessMenuImage = new BusinessMenuImage(MERCHANT_ID, IMAGE_URL);
        return new MenuImageResponseDto(businessMenuImage);
    }

    /**
     * This method creates sample SliderImageCreateRequestDto
     *
     * @return SliderImageCreateRequestDto
     */
    private SliderImageCreateRequestDto getSampleSliderImageCreateRequestDto() {
        SliderImageCreateRequestDto requestDto = new SliderImageCreateRequestDto();
        requestDto.setMerchantId(MERCHANT_ID);
        List<SliderImageItemDto> images = new ArrayList<>();
        SliderImageItemDto item = new SliderImageItemDto();
        item.setImageUrl(IMAGE_URL);
        item.setDisplayOrder(1);
        images.add(item);
        requestDto.setImages(images);
        return requestDto;
    }

    /**
     * This method creates sample SliderImageListResponseDto
     *
     * @return SliderImageListResponseDto
     */
    private SliderImageListResponseDto getSampleSliderImageListResponseDto() {
        BusinessSliderImage businessSliderImage = new BusinessSliderImage();
        businessSliderImage.setImageUrl(IMAGE_URL);
        businessSliderImage.setDisplayOrder(1);
        SliderImageItemResponseDto item = new SliderImageItemResponseDto(businessSliderImage);
        List<SliderImageItemResponseDto> responseItems = new ArrayList<>();
        responseItems.add(item);
        return new SliderImageListResponseDto(MERCHANT_ID, responseItems);
    }

    /**
     * This method creates JSON string from SliderImageCreateRequestDto
     *
     * @param requestDto SliderImageCreateRequestDto
     * @return JSON string
     */
    private String sliderImageCreateRequestDtoToJson(SliderImageCreateRequestDto requestDto) {
        StringBuilder json = new StringBuilder();
        json.append("{\"merchantId\":\"").append(requestDto.getMerchantId() != null ? requestDto.getMerchantId() : "").append("\",");
        json.append("\"images\":");
        if (requestDto.getImages() == null) {
            json.append("null");
        } else {
            json.append("[");
            for (int i = 0; i < requestDto.getImages().size(); i++) {
                SliderImageItemDto item = requestDto.getImages().get(i);
                if (i > 0) json.append(",");
                json.append("{\"imageUrl\":\"").append(item.getImageUrl() != null ? item.getImageUrl() : "").append("\",");
                json.append("\"displayOrder\":").append(item.getDisplayOrder()).append("}");
            }
            json.append("]");
        }
        json.append("}");
        return json.toString();
    }
}