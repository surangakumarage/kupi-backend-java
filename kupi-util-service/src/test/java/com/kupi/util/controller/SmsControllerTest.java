package com.kupi.util.controller;

import com.kupi.util.configuration.Translator;
import com.kupi.util.domain.request.MobileNoRequestDto;
import com.kupi.util.domain.request.SmsRequestDto;
import com.kupi.util.domain.response.SendSmsResponseDto;
import com.kupi.util.enums.ErrorResponseStatusType;
import com.kupi.util.enums.SuccessResponseStatusType;
import com.kupi.util.exception.UtilServiceException;
import com.kupi.util.service.sms.SmsService;
import com.kupi.util.util.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests {@link SmsController} class
 */
class SmsControllerTest {

    private static final String SEND_SMS_URI = "/api/v1/sms/send";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String COUNTRY_CODE = "+94";
    private static final String LOCAL_NUMBER = "771109101";

    @Mock
    private Translator translator;
    @Mock
    private Validator validator;
    @Mock
    private SmsService smsService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        initMocks(this);
        SmsController smsController = new SmsController(translator, validator, smsService);
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
        mockMvc = MockMvcBuilders.standaloneSetup(smsController)
                .setValidator(noOpValidator)
                .build();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnOk_When_RequiredFieldsAreProvidedForSendSms() throws Exception {
        SmsRequestDto smsRequestDto = getSampleSmsRequestDto();
        when(validator.isValidMobileNo(smsRequestDto.getRecipientNo().getNo())).thenReturn(true);
        
        // Mock the SmsService to return a response
        SendSmsResponseDto smsResponse = new SendSmsResponseDto();
        smsResponse.setStatus("SUCCESS");
        smsResponse.setRequest("WHATSAPP");
        when(smsService.sendSms(any(SmsRequestDto.class))).thenReturn(smsResponse);
        
        mockMvc.perform(MockMvcRequestBuilders.post(SEND_SMS_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .content(smsRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.SMS_SUCCESS_MESSAGE.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidTimeZoneIsProvidedForSendSms() throws Exception {
        SmsRequestDto smsRequestDto = getSampleSmsRequestDto();
        mockMvc.perform(MockMvcRequestBuilders.post(SEND_SMS_URI)
                        .header("Time-Zone", "Asia")
                        .content(smsRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_TIMEZONE.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_TIMEZONE.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_MissingRequiredFieldsAreProvidedForSendSms() throws Exception {
        SmsRequestDto smsRequestDto = getSampleSmsRequestDto();
        smsRequestDto.setMessage("");
        mockMvc.perform(MockMvcRequestBuilders.post(SEND_SMS_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .content(smsRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidEmailAddressIsProvidedForSendSms() throws Exception {
        SmsRequestDto smsRequestDto = getSampleSmsRequestDto();
        when(validator.isValidMobileNo(smsRequestDto.getRecipientNo().getNo())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post(SEND_SMS_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .content(smsRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_MOBILE_NO.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_MOBILE_NO.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_SendSmsIsFailed() throws Exception {
        SmsRequestDto smsRequestDto = getSampleSmsRequestDto();
        when(validator.isValidMobileNo(smsRequestDto.getRecipientNo().getNo())).thenReturn(true);
        doThrow(new UtilServiceException("ERROR")).when(smsService).sendSms(any(SmsRequestDto.class));
        mockMvc.perform(MockMvcRequestBuilders.post(SEND_SMS_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .content(smsRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    /**
     * This method creates sample MobileNoRequestDto
     *
     * @return MobileNoRequestDto
     */
    private MobileNoRequestDto getSampleMobileNoRequestDto() {
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto();
        mobileNoRequestDto.setCountryCode(COUNTRY_CODE);
        mobileNoRequestDto.setLocalNumber(LOCAL_NUMBER);
        return mobileNoRequestDto;
    }

    /**
     * This method creates sample SmsRequestDto
     *
     * @return SmsRequestDto
     */
    private SmsRequestDto getSampleSmsRequestDto() {
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        return new SmsRequestDto(mobileNoRequestDto, "Sms Message");
    }
}