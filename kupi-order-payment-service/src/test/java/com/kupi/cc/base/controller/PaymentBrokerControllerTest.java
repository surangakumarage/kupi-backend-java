package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.ResourceBundleMessageSourceBean;
import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.PaymentBroker;
import com.kupi.cc.base.domain.request.PaymentBrokerRequestDto;
import com.kupi.cc.base.domain.request.PaymentBrokerUpdateRequestDto;
import com.kupi.cc.base.enums.*;
import com.kupi.cc.base.exception.KupiOrderPaymentException;
import com.kupi.cc.base.service.PaymentBrokerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests{@link PaymentBrokerController} class
 */
class PaymentBrokerControllerTest {

    private static final String USER_ID = "uid-123456789";
    private static final String USER_ID_HEADER = "User-Id";
    private static final String PAYMENT_BROKER_ID = "PAYMENT_BROKER_ID";
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String FAILED = "failed";
    private static final String APPLICATION_JSON = "application/json";
    private static final String PAYMENT_BROKER_URI = "/api/v1/payment-broker";
    private static final String PAYMENT_BROKER_BY_ID_URI = "/api/v1/payment-broker/{paymentBrokerId}";

    private final ResourceBundleMessageSourceBean resourceBundleMessageSourceBean = new ResourceBundleMessageSourceBean();
    Locale locale = LocaleContextHolder.getLocale();
    @Mock
    private Translator translator;
    @Mock
    private PaymentBrokerService paymentBrokerService;
    private MockMvc mockMvc;
    private ResourceBundleMessageSource resourceBundleMessageSource;

    @BeforeEach
    void setUp() {
        openMocks(this);
        PaymentBrokerController paymentBrokerController = new PaymentBrokerController(translator, paymentBrokerService);
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
        mockMvc = MockMvcBuilders.standaloneSetup(paymentBrokerController)
                .setValidator(noOpValidator)
                .build();
        resourceBundleMessageSource = resourceBundleMessageSourceBean.messageSource();
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks(this).close();
    }

    /**
     * Start of tests for save payment broker
     * Api context: /api/v1/payment-broker
     */
    @Test
    void Should_ReturnOk_When_SavingPaymentBrokerIsSuccessful() throws Exception {
        doNothing().when(paymentBrokerService).savePaymentBroker(any(PaymentBroker.class), anyString());

        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType
                .getCodeString(SuccessResponseStatusType.SAVE_PAYMENT_BROKER.getCode()), null, locale);
        when(translator.toLocale(SuccessResponseStatusType.getCodeString(SuccessResponseStatusType.SAVE_PAYMENT_BROKER
                .getCode()))).thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(PAYMENT_BROKER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(getSamplePaymentBrokerRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.SAVE_PAYMENT_BROKER.getMessage()))
                .andExpect(jsonPath("$.data.name").value(PaymentBrokerName.PAYPAL.name()))
                .andExpect(jsonPath("$.data.countyCode").value(CountryCode.ARE.name()))
                .andExpect(jsonPath("$.data.paymentOptionType").value(PaymentOptionType.EXTERNAL.name()))
                .andExpect(jsonPath("$.data.currencyType").value(CurrencyType.USD.name()))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_SavingPaymentBrokerForMissingRequiredFields() throws Exception {
        PaymentBrokerRequestDto paymentBrokerRequestDto = getSamplePaymentBrokerRequestDto();
        paymentBrokerRequestDto.setPaymentOptionType(null);

        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                .getCodeString(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.getCodeString(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                .getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(PAYMENT_BROKER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(paymentBrokerRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_SavingPaymentBrokerIsFailed() throws Exception {
        doThrow(new KupiOrderPaymentException(FAILED)).when(paymentBrokerService)
                .savePaymentBroker(any(PaymentBroker.class), anyString());

        mockMvc.perform(MockMvcRequestBuilders.post(PAYMENT_BROKER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(getSamplePaymentBrokerRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()));
    }

    /**
     * Start of tests for update payment broker
     * Api context: /api/v1/payment-broker
     */
    @Test
    void Should_ReturnOk_When_UpdatingPaymentBrokerIsSuccessful() throws Exception {
        when(paymentBrokerService.updatePaymentBroker(any(PaymentBrokerUpdateRequestDto.class), anyString()))
                .thenReturn(getSamplePaymentBroker());

        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType
                .getCodeString(SuccessResponseStatusType.UPDATE_PAYMENT_BROKER.getCode()), null, locale);
        when(translator.toLocale(SuccessResponseStatusType.getCodeString(SuccessResponseStatusType.UPDATE_PAYMENT_BROKER
                .getCode()))).thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(PAYMENT_BROKER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(getSamplePaymentBrokerUpdateRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.UPDATE_PAYMENT_BROKER.getMessage()))
                .andExpect(jsonPath("$.data.id").value(PAYMENT_BROKER_ID))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingPaymentBrokerForMissingRequiredFields() throws Exception {
        PaymentBrokerUpdateRequestDto paymentBrokerUpdateRequestDto = getSamplePaymentBrokerUpdateRequestDto();
        paymentBrokerUpdateRequestDto.setPaymentOptionType(null);

        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                .getCodeString(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.getCodeString(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                .getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(PAYMENT_BROKER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(paymentBrokerUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingPaymentBrokerIsFailed() throws Exception {
        when(paymentBrokerService.updatePaymentBroker(any(PaymentBrokerUpdateRequestDto.class), anyString()))
                .thenThrow(new KupiOrderPaymentException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                .getCodeString(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.getCodeString(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                .getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(PAYMENT_BROKER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(getSamplePaymentBrokerUpdateRequestDto().toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage")
                        .value(invalidMessage));
    }

    /**
     * Start of tests for get payment broker by id
     * Api context: /api/v1/payment-broker/{paymentBrokerId}
     */
    @Test
    void Should_ReturnOk_When_GettingPaymentBrokerByIdIsSuccessful() throws Exception {
        when(paymentBrokerService.getPaymentBrokerById(anyString())).thenReturn(getSamplePaymentBroker());

        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType
                .getCodeString(SuccessResponseStatusType.READ_PAYMENT_BROKER.getCode()), null, locale);
        when(translator.toLocale(SuccessResponseStatusType.getCodeString(SuccessResponseStatusType.READ_PAYMENT_BROKER
                .getCode()))).thenReturn(successMessage);
        String uri = PAYMENT_BROKER_BY_ID_URI.replace("{paymentBrokerId}", PAYMENT_BROKER_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_PAYMENT_BROKER.getMessage()))
                .andExpect(jsonPath("$.data.id").value(PAYMENT_BROKER_ID))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingPaymentBrokerByIdIsFailed() throws Exception {
        when(paymentBrokerService.getPaymentBrokerById(anyString())).thenThrow(new KupiOrderPaymentException(FAILED));

        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                .getCodeString(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.getCodeString(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                .getCode()))).thenReturn(invalidMessage);
        String uri = PAYMENT_BROKER_BY_ID_URI.replace("{paymentBrokerId}", PAYMENT_BROKER_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage")
                        .value(invalidMessage));
    }

    /**
     * Start of tests for delete payment broker
     * Api context: /api/v1/payment-broker/{paymentBrokerId}
     */
    @Test
    void Should_ReturnOk_When_DeletingPaymentBrokerIsSuccessful() throws Exception {
        doNothing().when(paymentBrokerService).deletePaymentBroker(anyString(), anyString());

        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType
                .getCodeString(SuccessResponseStatusType.DELETE_ADDRESS.getCode()), null, locale);
        when(translator.toLocale(SuccessResponseStatusType.getCodeString(SuccessResponseStatusType.DELETE_ADDRESS
                .getCode()))).thenReturn(successMessage);
        String uri = PAYMENT_BROKER_BY_ID_URI.replace("{paymentBrokerId}", PAYMENT_BROKER_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.DELETE_ADDRESS.getMessage()))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_DeletingPaymentBrokerIsFailed() throws Exception {
        doThrow(new KupiOrderPaymentException(FAILED)).when(paymentBrokerService)
                .deletePaymentBroker(anyString(), anyString());

        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                .getCodeString(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.getCodeString(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                .getCode()))).thenReturn(invalidMessage);
        String uri = PAYMENT_BROKER_BY_ID_URI.replace("{paymentBrokerId}", PAYMENT_BROKER_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage")
                        .value(invalidMessage));
    }

    /**
     * Start of tests for get payment broker details
     * Api context: /api/v1/payment-broker
     */
    @Test
    void Should_ReturnOk_When_GettingPaymentBrokerDetailsIsSuccessful() throws Exception {
        when(paymentBrokerService.getAllPaymentBrokers(anyString())).thenReturn(getSamplePaymentBrokerList());

        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType
                .getCodeString(SuccessResponseStatusType.READ_PAYMENT_BROKER.getCode()), null, locale);
        when(translator.toLocale(SuccessResponseStatusType.getCodeString(SuccessResponseStatusType.READ_PAYMENT_BROKER
                .getCode()))).thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.get(PAYMENT_BROKER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.READ_PAYMENT_BROKER.getMessage()))
                .andExpect(jsonPath("$.data.paymentBrokerList[0].id").value(PAYMENT_BROKER_ID))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingPaymentBrokerDetailsIsFailed() throws Exception {
        when(paymentBrokerService.getAllPaymentBrokers(anyString())).thenThrow(new KupiOrderPaymentException(FAILED));

        mockMvc.perform(MockMvcRequestBuilders.get(PAYMENT_BROKER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()));
    }

    /**
     * This method returns a sample payment broker request dto
     *
     * @return PaymentBrokerRequestDto
     */
    private PaymentBrokerRequestDto getSamplePaymentBrokerRequestDto() {
        PaymentBrokerRequestDto paymentBrokerRequestDto = new PaymentBrokerRequestDto();
        paymentBrokerRequestDto.setName(PaymentBrokerName.PAYPAL);
        paymentBrokerRequestDto.setCountryCode(CountryCode.ARE);
        paymentBrokerRequestDto.setPaymentOptionType(PaymentOptionType.EXTERNAL);
        paymentBrokerRequestDto.setCurrencyType(CurrencyType.USD);
        return paymentBrokerRequestDto;
    }

    /**
     * This method returns a sample payment broker update request dto
     *
     * @return PaymentBrokerUpdateRequestDto
     */
    private PaymentBrokerUpdateRequestDto getSamplePaymentBrokerUpdateRequestDto() {
        PaymentBrokerUpdateRequestDto paymentBrokerUpdateRequestDto = new PaymentBrokerUpdateRequestDto();
        paymentBrokerUpdateRequestDto.setId(PAYMENT_BROKER_ID);
        paymentBrokerUpdateRequestDto.setName(PaymentBrokerName.PAYPAL);
        paymentBrokerUpdateRequestDto.setCountryCode(CountryCode.ARE);
        paymentBrokerUpdateRequestDto.setPaymentOptionType(PaymentOptionType.EXTERNAL);
        paymentBrokerUpdateRequestDto.setCurrencyType(CurrencyType.USD);
        return paymentBrokerUpdateRequestDto;
    }

    /**
     * This method returns a sample payment broker
     *
     * @return PaymentBroker
     */
    private PaymentBroker getSamplePaymentBroker() {
        PaymentBroker paymentBroker = new PaymentBroker();
        paymentBroker.setId(PAYMENT_BROKER_ID);
        return paymentBroker;
    }

    /**
     * This method returns a sample payment broker list
     *
     * @return list of PaymentBroker
     */
    private List<PaymentBroker> getSamplePaymentBrokerList() {
        List<PaymentBroker> paymentBrokerList = new ArrayList<>();
        paymentBrokerList.add(getSamplePaymentBroker());
        return paymentBrokerList;
    }
}