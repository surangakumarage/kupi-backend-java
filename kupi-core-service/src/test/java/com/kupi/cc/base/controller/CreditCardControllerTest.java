package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.ResourceBundleMessageSourceBean;
import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.AuthUser;
import com.kupi.cc.base.domain.entity.CombinedCreditCardRequest;
import com.kupi.cc.base.domain.entity.CreditCardRequest;
import com.kupi.cc.base.domain.request.BulkUserRequestDto;
import com.kupi.cc.base.domain.request.CreditCardRequestCreateRequestDto;
import com.kupi.cc.base.domain.request.CreditCardRequestUpdateRequestDto;
import com.kupi.cc.base.domain.request.MobileNoRequestDto;
import com.kupi.cc.base.domain.response.BusinessMerchantResponseDto;
import com.kupi.cc.base.domain.response.MerchantBusinessResponseDto;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.InvalidCreditCardRequestException;
import com.kupi.cc.base.exception.InvalidUserException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.service.AuthUserService;
import com.kupi.cc.base.service.CreditCardService;
import com.kupi.cc.base.util.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests {@link CreditCardController} class
 */
class CreditCardControllerTest {

    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String USER_ID_HEADER = "User-Id";
    private static final String USER_ID = "uid-123456789";
    private static final String AUTH_TOKEN_HEADER = "Auth-Token";
    private static final String AUTH_TOKEN = "Bearer192f0c96-dd19-404e-8217-a8fd1cc81d55";
    private static final String COUNTRY_CODE = "+94";
    private static final String LOCAL_NUMBER = "123456789";
    private static final String EMAIL = "abc@yahoomail.com";
    private static final String NIC = "123456789v";
    private static final String BANK_ID = "uid-123456789";
    private static final String CREDIT_CARD_REQUEST_ID = "ccrid-123456789";
    private static final String SEARCH_TERM = "Softlogic";
    private static final int PAGE = 0;
    private static final int SIZE = 1;
    private static final long NO_OF_REQUEST = 1000;
    private static final String BUSINESS_NAME = "SkSolutions";
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String FAILED = "failed";
    private static final String APPLICATION_JSON = "application/json";
    private static final String CREDIT_CARD_REQUEST_URI = "/api/v1/banks/credit-cards";
    private static final String CREDIT_CARD_REQUEST_BY_REQUEST_ID_URI = "/api/v1/banks/credit-cards/requests/{request-id}";
    private static final String SEARCH_CREDIT_CARD_REQUEST_LIST_URI = "/api/v1/banks/credit-cards/{bankId}/{page}/{size}/search/{searchTerm}";
    private static final String LIST_CREDIT_CARD_REQUEST_FOR_A_USER_URI = "/api/v1/banks/credit-cards/user/{user-id}/{page}/{size}";
    private static final String GET_GROUP_CREDIT_CARD_REQUESTS_URI = "/api/v1/banks/credit-cards/combinations/{page}/{size}/bank/{bankId}/search/{searchTerm}";

    private final ResourceBundleMessageSourceBean resourceBundleMessageSourceBean = new ResourceBundleMessageSourceBean();
    Locale locale = LocaleContextHolder.getLocale();
    private MockMvc mockMvc;
    private ResourceBundleMessageSource resourceBundleMessageSource;

    @Mock
    private Translator translator;
    @Mock
    private Validator validator;
    @Mock
    private AuthUserService authUserService;
    @Mock
    private CreditCardService creditCardService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        CreditCardController creditCardController = new CreditCardController(translator, authUserService,
                creditCardService, validator);
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
        mockMvc = MockMvcBuilders.standaloneSetup(creditCardController)
                .setValidator(noOpValidator)
                .build();
        this.resourceBundleMessageSource = resourceBundleMessageSourceBean.messageSource();
    }

    @AfterEach
    void tearDown() {
    }

    /**
     * Start of tests for create credit card request
     * Api context: api/v1/banks/credit-cards
     */
    @Test
    void Should_ReturnOk_When_CreatingCreditCardRequestIsSuccessful() throws Exception {
        when(validator.isValidMobileNoWithCountryCode(COUNTRY_CODE + "-" + LOCAL_NUMBER)).thenReturn(true);
        when(validator.isValidEmail(EMAIL)).thenReturn(true);
        when(validator.isValidNIC(NIC)).thenReturn(true);
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenReturn(getSampleBusinessMerchantResponseDto());
        when(authUserService.getUserByUserId(anyString(), anyString())).thenReturn(new AuthUser());
        when(creditCardService.saveCreditCardRequest(any(CreditCardRequestCreateRequestDto.class)))
                .thenReturn(getSampleCreditCardRequest());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .CREATE_CREDIT_CARD_REQUEST.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.CREATE_CREDIT_CARD_REQUEST
                .getCode()))).thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(CREDIT_CARD_REQUEST_URI)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleCreditCardRequestCreateRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.CREATE_CREDIT_CARD_REQUEST.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.CREATE_CREDIT_CARD_REQUEST.getCode()))
                .andExpect(jsonPath("$.data.id").value(CREDIT_CARD_REQUEST_ID))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingCreditCardRequestForInvalidMobileNo() throws Exception {
        when(validator.isValidMobileNoWithCountryCode(COUNTRY_CODE + "-" + LOCAL_NUMBER)).thenReturn(false);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_MOBILE_NUMBER.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_MOBILE_NUMBER
                .getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(CREDIT_CARD_REQUEST_URI)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleCreditCardRequestCreateRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_MOBILE_NUMBER.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_MOBILE_NUMBER.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingCreditCardRequestForInvalidEmail() throws Exception {
        when(validator.isValidMobileNoWithCountryCode(COUNTRY_CODE + "-" + LOCAL_NUMBER)).thenReturn(true);
        when(validator.isValidEmail(EMAIL)).thenReturn(false);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_EMAIL.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_EMAIL
                .getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(CREDIT_CARD_REQUEST_URI)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleCreditCardRequestCreateRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_EMAIL.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_EMAIL.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingCreditCardRequestForInvalidNIC() throws Exception {
        when(validator.isValidMobileNoWithCountryCode(COUNTRY_CODE + "-" + LOCAL_NUMBER)).thenReturn(true);
        when(validator.isValidEmail(EMAIL)).thenReturn(true);
        when(validator.isValidNIC(NIC)).thenReturn(false);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_NIC.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_NIC
                .getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(CREDIT_CARD_REQUEST_URI)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleCreditCardRequestCreateRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_NIC.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_NIC.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingCreditCardRequestForInvalidUserOrBank() throws Exception {
        when(validator.isValidMobileNoWithCountryCode(COUNTRY_CODE + "-" + LOCAL_NUMBER)).thenReturn(true);
        when(validator.isValidEmail(EMAIL)).thenReturn(true);
        when(validator.isValidNIC(NIC)).thenReturn(true);
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenThrow(new InvalidUserException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_USER_OR_BANK.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_USER_OR_BANK
                .getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(CREDIT_CARD_REQUEST_URI)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleCreditCardRequestCreateRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_USER_OR_BANK.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_USER_OR_BANK.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_CreatingCreditCardRequestIsFailed() throws Exception {
        when(validator.isValidMobileNoWithCountryCode(COUNTRY_CODE + "-" + LOCAL_NUMBER)).thenReturn(true);
        when(validator.isValidEmail(EMAIL)).thenReturn(true);
        when(validator.isValidNIC(NIC)).thenReturn(true);
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                .getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(CREDIT_CARD_REQUEST_URI)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleCreditCardRequestCreateRequestDto().toJson())
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
     * Start of tests for update credit card request
     * Api context: api/v1/banks/credit-cards
     */
    @Test
    void Should_ReturnOk_When_UpdatingCreditCardRequestIsSuccessful() throws Exception {
        when(validator.isValidMobileNoWithCountryCode(COUNTRY_CODE + "-" + LOCAL_NUMBER)).thenReturn(true);
        when(validator.isValidEmail(EMAIL)).thenReturn(true);
        when(validator.isValidNIC(NIC)).thenReturn(true);
        when(creditCardService.getCreditCardRequestById(anyString())).thenReturn(new CreditCardRequest());
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenReturn(getSampleBusinessMerchantResponseDto());
        when(authUserService.getUserByUserId(anyString(), anyString())).thenReturn(new AuthUser());
        when(creditCardService.updateCreditCardRequest(any(CreditCardRequestUpdateRequestDto.class)))
                .thenReturn(getSampleCreditCardRequest());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .UPDATE_CREDIT_CARD_REQUEST.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.UPDATE_CREDIT_CARD_REQUEST
                .getCode()))).thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(CREDIT_CARD_REQUEST_URI)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleCreditCardRequestUpdateRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.UPDATE_CREDIT_CARD_REQUEST.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.UPDATE_CREDIT_CARD_REQUEST.getCode()))
                .andExpect(jsonPath("$.data.id").value(CREDIT_CARD_REQUEST_ID))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingCreditCardRequestForInvalidMobileNo() throws Exception {
        when(validator.isValidMobileNoWithCountryCode(COUNTRY_CODE + "-" + LOCAL_NUMBER)).thenReturn(false);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_MOBILE_NUMBER.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_MOBILE_NUMBER
                .getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(CREDIT_CARD_REQUEST_URI)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleCreditCardRequestUpdateRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_MOBILE_NUMBER.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_MOBILE_NUMBER.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingCreditCardRequestForInvalidEmail() throws Exception {
        when(validator.isValidMobileNoWithCountryCode(COUNTRY_CODE + "-" + LOCAL_NUMBER)).thenReturn(true);
        when(validator.isValidEmail(EMAIL)).thenReturn(false);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_EMAIL.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_EMAIL
                .getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(CREDIT_CARD_REQUEST_URI)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleCreditCardRequestUpdateRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_EMAIL.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_EMAIL.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingCreditCardRequestForInvalidNIC() throws Exception {
        when(validator.isValidMobileNoWithCountryCode(COUNTRY_CODE + "-" + LOCAL_NUMBER)).thenReturn(true);
        when(validator.isValidEmail(EMAIL)).thenReturn(true);
        when(validator.isValidNIC(NIC)).thenReturn(false);

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_NIC.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_NIC
                .getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(CREDIT_CARD_REQUEST_URI)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleCreditCardRequestUpdateRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_NIC.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_NIC.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingCreditCardRequestForInvalidCreditCardRequest() throws Exception {
        when(validator.isValidMobileNoWithCountryCode(COUNTRY_CODE + "-" + LOCAL_NUMBER)).thenReturn(true);
        when(validator.isValidEmail(EMAIL)).thenReturn(true);
        when(validator.isValidNIC(NIC)).thenReturn(true);
        when(creditCardService.getCreditCardRequestById(anyString()))
                .thenThrow(new InvalidCreditCardRequestException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_CREDIT_CARD_REQUEST_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_CREDIT_CARD_REQUEST_ID
                .getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(CREDIT_CARD_REQUEST_URI)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleCreditCardRequestUpdateRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_CREDIT_CARD_REQUEST_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_CREDIT_CARD_REQUEST_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingCreditCardRequestForInvalidUserOrBank() throws Exception {
        when(validator.isValidMobileNoWithCountryCode(COUNTRY_CODE + "-" + LOCAL_NUMBER)).thenReturn(true);
        when(validator.isValidEmail(EMAIL)).thenReturn(true);
        when(validator.isValidNIC(NIC)).thenReturn(true);
        when(creditCardService.getCreditCardRequestById(anyString())).thenReturn(new CreditCardRequest());
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenThrow(new InvalidUserException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_USER_OR_BANK.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_USER_OR_BANK
                .getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(CREDIT_CARD_REQUEST_URI)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleCreditCardRequestUpdateRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_USER_OR_BANK.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_USER_OR_BANK.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_UpdatingCreditCardRequestIsFailed() throws Exception {
        when(validator.isValidMobileNoWithCountryCode(COUNTRY_CODE + "-" + LOCAL_NUMBER)).thenReturn(true);
        when(validator.isValidEmail(EMAIL)).thenReturn(true);
        when(validator.isValidNIC(NIC)).thenReturn(true);
        when(creditCardService.getCreditCardRequestById(anyString())).thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                .getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(CREDIT_CARD_REQUEST_URI)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .content(getSampleCreditCardRequestUpdateRequestDto().toJson())
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
     * Start of tests for get credit card request detail
     * Api context: api/v1/banks/credit-cards/requests/{request-id}
     */
    @Test
    void Should_ReturnOk_When_GettingCreditCardRequestDetailIsSuccessful() throws Exception {
        when(creditCardService.getCreditCardRequestById(anyString())).thenReturn(getSampleCreditCardRequest());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .READ_CREDIT_CARD_REQUEST.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.READ_CREDIT_CARD_REQUEST
                .getCode()))).thenReturn(successMessage);
        String uri = CREDIT_CARD_REQUEST_BY_REQUEST_ID_URI.replace("{request-id}", CREDIT_CARD_REQUEST_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_CREDIT_CARD_REQUEST.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.READ_CREDIT_CARD_REQUEST.getCode()))
                .andExpect(jsonPath("$.data.id").value(CREDIT_CARD_REQUEST_ID))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingCreditCardRequestDetailForInvalidCreditCardRequest() throws Exception {
        when(creditCardService.getCreditCardRequestById(anyString()))
                .thenThrow(new InvalidCreditCardRequestException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_CREDIT_CARD_REQUEST_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_CREDIT_CARD_REQUEST_ID
                .getCode()))).thenReturn(invalidMessage);
        String uri = CREDIT_CARD_REQUEST_BY_REQUEST_ID_URI.replace("{request-id}", CREDIT_CARD_REQUEST_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_CREDIT_CARD_REQUEST_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_CREDIT_CARD_REQUEST_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingCreditCardRequestDetailIsFailed() throws Exception {
        when(creditCardService.getCreditCardRequestById(anyString())).thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                .getCode()))).thenReturn(invalidMessage);
        String uri = CREDIT_CARD_REQUEST_BY_REQUEST_ID_URI.replace("{request-id}", CREDIT_CARD_REQUEST_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, USER_ID)
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
     * Start of tests for search credit card request list
     * Api context: api/v1/banks/credit-cards/{bankId}/{page}/{size}/search/{searchTerm}
     */
    @Test
    void Should_ReturnOk_When_SearchingCreditCardRequestListIsSuccessful() throws Exception {
        when(creditCardService.searchCreditCardRequests(any(Pageable.class), anyString(), anyString()))
                .thenReturn(getSampleCreditCardRequestPage());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .READ_CREDIT_CARD_REQUEST_LIST.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.READ_CREDIT_CARD_REQUEST_LIST
                .getCode()))).thenReturn(successMessage);
        String uri = SEARCH_CREDIT_CARD_REQUEST_LIST_URI.replace("{bankId}", BANK_ID)
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_CREDIT_CARD_REQUEST_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.READ_CREDIT_CARD_REQUEST_LIST.getCode()))
                .andExpect(jsonPath("$.data.creditCardRequests[0].id").value(CREDIT_CARD_REQUEST_ID))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_SearchingCreditCardRequestListIsFailed() throws Exception {
        when(creditCardService.searchCreditCardRequests(any(Pageable.class), anyString(), anyString()))
                .thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                .getCode()))).thenReturn(invalidMessage);
        String uri = SEARCH_CREDIT_CARD_REQUEST_LIST_URI.replace("{bankId}", BANK_ID)
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, USER_ID)
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
     * Start of tests for delete credit card request
     * Api context: api/v1/banks/credit-cards/requests/{request-id}
     */
    @Test
    void Should_ReturnOk_When_DeletingCreditCardRequestIsSuccessful() throws Exception {
        doNothing().when(creditCardService).deleteCreditCardRequest(anyString());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .DELETE_CREDIT_CARD_REQUEST.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.DELETE_CREDIT_CARD_REQUEST
                .getCode()))).thenReturn(successMessage);
        String uri = CREDIT_CARD_REQUEST_BY_REQUEST_ID_URI.replace("{request-id}", CREDIT_CARD_REQUEST_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.DELETE_CREDIT_CARD_REQUEST.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.DELETE_CREDIT_CARD_REQUEST.getCode()))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_DeletingCreditCardRequestForInvalidCreditCardRequestId() throws Exception {
        doThrow(new InvalidCreditCardRequestException(FAILED)).when(creditCardService)
                .deleteCreditCardRequest(anyString());

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_CREDIT_CARD_REQUEST_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_CREDIT_CARD_REQUEST_ID
                .getCode()))).thenReturn(invalidMessage);
        String uri = CREDIT_CARD_REQUEST_BY_REQUEST_ID_URI.replace("{request-id}", CREDIT_CARD_REQUEST_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_CREDIT_CARD_REQUEST_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_CREDIT_CARD_REQUEST_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_DeletingCreditCardRequestIsFailed() throws Exception {
        doThrow(new KupiCoreException(FAILED)).when(creditCardService).deleteCreditCardRequest(anyString());

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                .getCode()))).thenReturn(invalidMessage);
        String uri = CREDIT_CARD_REQUEST_BY_REQUEST_ID_URI.replace("{request-id}", CREDIT_CARD_REQUEST_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(USER_ID_HEADER, USER_ID)
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
     * Start of tests for list credit card request for a user
     * Api context: api/v1/banks/credit-cards/user/{user-id}/{page}/{size}
     */
    @Test
    void Should_ReturnOk_When_ListingCreditCardRequestForAUserIsSuccessful() throws Exception {
        when(authUserService.getUserByUserId(anyString(), anyString())).thenReturn(new AuthUser());
        when(creditCardService.getCreditCardRequestByUserId(any(Pageable.class), anyString()))
                .thenReturn(getSampleCreditCardRequestPage());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .READ_CREDIT_CARD_REQUEST_LIST.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.READ_CREDIT_CARD_REQUEST_LIST
                .getCode()))).thenReturn(successMessage);
        String uri = LIST_CREDIT_CARD_REQUEST_FOR_A_USER_URI.replace("{user-id}", USER_ID)
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE));
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_CREDIT_CARD_REQUEST_LIST.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.READ_CREDIT_CARD_REQUEST_LIST.getCode()))
                .andExpect(jsonPath("$.data.creditCardRequests[0].id").value(CREDIT_CARD_REQUEST_ID))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_ListingCreditCardRequestForAUserForInactiveMerchantForCreateDeal() throws Exception {
        when(authUserService.getUserByUserId(anyString(), anyString())).thenThrow(new InvalidUserException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INACTIVE_MERCHANT_FOR_CREATE_DEAL.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INACTIVE_MERCHANT_FOR_CREATE_DEAL
                .getCode()))).thenReturn(invalidMessage);
        String uri = LIST_CREDIT_CARD_REQUEST_FOR_A_USER_URI.replace("{user-id}", USER_ID)
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE));
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INACTIVE_MERCHANT_FOR_CREATE_DEAL.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INACTIVE_MERCHANT_FOR_CREATE_DEAL.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_ListingCreditCardRequestForAUserIsFailed() throws Exception {
        when(authUserService.getUserByUserId(anyString(), anyString())).thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                .getCode()))).thenReturn(invalidMessage);
        String uri = LIST_CREDIT_CARD_REQUEST_FOR_A_USER_URI.replace("{user-id}", USER_ID)
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE));
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
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
     * Start of tests for get group credit card requests
     * Api context: api/v1/banks/credit-cards/combinations/{page}/{size}/bank/{bankId}/search/{searchTerm}
     */
    @Test
    void Should_ReturnOk_When_GettingGroupCreditCardRequestsIsSuccessful() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenReturn(new BusinessMerchantResponseDto());
        when(creditCardService.getGroupCreditCardRequest(any(Pageable.class), anyString(), anyString()))
                .thenReturn(getSampleCombinedCreditCardRequestPage());
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .READ_REQUEST_A_CREDIT_CARD_GROUP.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.READ_REQUEST_A_CREDIT_CARD_GROUP
                .getCode()))).thenReturn(successMessage);
        String uri = GET_GROUP_CREDIT_CARD_REQUESTS_URI.replace("{bankId}", BANK_ID)
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_REQUEST_A_CREDIT_CARD_GROUP.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.READ_REQUEST_A_CREDIT_CARD_GROUP.getCode()))
                .andExpect(jsonPath("$.data.combinedCreditCardRequestList[0].bank.id").value(USER_ID))
                .andExpect(jsonPath("$.data.combinedCreditCardRequestList[0].numberOfRequests")
                        .value(NO_OF_REQUEST))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnOk_When_GettingGroupCreditCardRequestsForEmptyGroupCreditCardRequest() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenReturn(new BusinessMerchantResponseDto());
        when(creditCardService.getGroupCreditCardRequest(any(Pageable.class), anyString(), anyString()))
                .thenReturn(new PageImpl<>(new ArrayList<>()));
        when(authUserService.getMerchantMap(any(BulkUserRequestDto.class), any(UserType.class)))
                .thenReturn(getSampleMerchantBusinessResponseDtoMap());

        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .READ_REQUEST_A_CREDIT_CARD_GROUP.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.READ_REQUEST_A_CREDIT_CARD_GROUP
                .getCode()))).thenReturn(successMessage);
        String uri = GET_GROUP_CREDIT_CARD_REQUESTS_URI.replace("{bankId}", BANK_ID)
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_REQUEST_A_CREDIT_CARD_GROUP.getMessage()))
                .andExpect(jsonPath("$.statusCode")
                        .value(SuccessResponseStatusType.READ_REQUEST_A_CREDIT_CARD_GROUP.getCode()))
                .andExpect(jsonPath("$.data.totalItems").value(0))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(0))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingGroupCreditCardRequestsForInvalidBankId() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenThrow(new InvalidUserException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_BANK_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_BANK_ID
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_GROUP_CREDIT_CARD_REQUESTS_URI.replace("{bankId}", BANK_ID)
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .header(AUTH_TOKEN_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_BANK_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_BANK_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingGroupCreditCardRequestsIsFailed() throws Exception {
        when(authUserService.getBankBusinessByBankId(anyString(), anyString()))
                .thenThrow(new KupiCoreException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                .getCode()))).thenReturn(invalidMessage);
        String uri = GET_GROUP_CREDIT_CARD_REQUESTS_URI.replace("{bankId}", BANK_ID)
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
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

    private CreditCardRequestCreateRequestDto getSampleCreditCardRequestCreateRequestDto() {
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto(COUNTRY_CODE, LOCAL_NUMBER);

        CreditCardRequestCreateRequestDto creditCardRequestCreateRequestDto = new CreditCardRequestCreateRequestDto();
        creditCardRequestCreateRequestDto.setUserId(USER_ID);
        creditCardRequestCreateRequestDto.setBankId(BANK_ID);
        creditCardRequestCreateRequestDto.setEmail(EMAIL);
        creditCardRequestCreateRequestDto.setNic(NIC);
        creditCardRequestCreateRequestDto.setMobileNumber(mobileNoRequestDto);
        return creditCardRequestCreateRequestDto;
    }

    private BusinessMerchantResponseDto getSampleBusinessMerchantResponseDto() {
        BusinessMerchantResponseDto businessMerchantResponseDto = new BusinessMerchantResponseDto();
        businessMerchantResponseDto.setBusinessName(BUSINESS_NAME);
        return businessMerchantResponseDto;
    }

    private CreditCardRequest getSampleCreditCardRequest() {
        CreditCardRequest creditCardRequest = new CreditCardRequest();
        creditCardRequest.setId(CREDIT_CARD_REQUEST_ID);
        return creditCardRequest;
    }

    private CreditCardRequestUpdateRequestDto getSampleCreditCardRequestUpdateRequestDto() {
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto(COUNTRY_CODE, LOCAL_NUMBER);

        CreditCardRequestUpdateRequestDto creditCardRequestUpdateRequestDto = new CreditCardRequestUpdateRequestDto();
        creditCardRequestUpdateRequestDto.setUserId(USER_ID);
        creditCardRequestUpdateRequestDto.setBankId(BANK_ID);
        creditCardRequestUpdateRequestDto.setId(USER_ID);
        creditCardRequestUpdateRequestDto.setEmail(EMAIL);
        creditCardRequestUpdateRequestDto.setNic(NIC);
        creditCardRequestUpdateRequestDto.setMobileNumber(mobileNoRequestDto);

        return creditCardRequestUpdateRequestDto;
    }

    private Page<CreditCardRequest> getSampleCreditCardRequestPage() {
        List<CreditCardRequest> creditCardRequestList = new ArrayList<>();
        creditCardRequestList.add(getSampleCreditCardRequest());
        return new PageImpl<>(creditCardRequestList);
    }

    private Page<CombinedCreditCardRequest> getSampleCombinedCreditCardRequestPage() {
        List<CombinedCreditCardRequest> combinedCreditCardRequestList = new ArrayList<>();
        combinedCreditCardRequestList.add(getSampleCombinedCreditCardRequest());
        return new PageImpl<>(combinedCreditCardRequestList);
    }

    private CombinedCreditCardRequest getSampleCombinedCreditCardRequest() {
        CombinedCreditCardRequest combinedCreditCardRequest = new CombinedCreditCardRequest();
        combinedCreditCardRequest.setBankId(BANK_ID);
        combinedCreditCardRequest.setCount(NO_OF_REQUEST);
        return combinedCreditCardRequest;
    }

    private Map<String, MerchantBusinessResponseDto> getSampleMerchantBusinessResponseDtoMap() {
        Map<String, MerchantBusinessResponseDto> merchantBusinessResponseDtoMap = new HashMap<>();
        merchantBusinessResponseDtoMap.put(BANK_ID, getSampleMerchantBusinessResponseDto());
        return merchantBusinessResponseDtoMap;
    }

    private MerchantBusinessResponseDto getSampleMerchantBusinessResponseDto() {
        MerchantBusinessResponseDto merchantBusinessResponseDto = new MerchantBusinessResponseDto();
        merchantBusinessResponseDto.setId(USER_ID);
        return merchantBusinessResponseDto;
    }
}