package com.kupi.util.controller;

import com.kupi.util.configuration.Translator;
import com.kupi.util.domain.request.MailRequestDto;
import com.kupi.util.enums.ErrorResponseStatusType;
import com.kupi.util.enums.SuccessResponseStatusType;
import com.kupi.util.exception.UtilServiceException;
import com.kupi.util.service.SesService;
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
 * This class tests {@link EmailController} class
 */
class EmailControllerTest {

    private static final String SEND_EMAIL_URI = "/api/v1/email/send";
    private static final String RECIPIENT_ADDRESS = "recipient@kupi.lk";
    private static final String TITLE = "Mail title";
    private static final String CONTENT = "Mail body";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";

    @Mock
    private SesService sesService;
    @Mock
    private Validator validator;
    @Mock
    private Translator translator;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        initMocks(this);
        EmailController emailController = new EmailController(sesService, validator, translator);
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
        mockMvc = MockMvcBuilders.standaloneSetup(emailController)
                .setValidator(noOpValidator)
                .build();
    }

    @Test
    void Should_ReturnOk_When_RequiredFieldsAreProvidedForSendEmail() throws Exception {
        MailRequestDto mailRequestDto = new MailRequestDto(RECIPIENT_ADDRESS, TITLE, CONTENT, false);
        when(validator.isValidEmail(mailRequestDto.getRecipientAddress())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post(SEND_EMAIL_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .content(mailRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.EMAIL_SUCCESS_MESSAGE.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidTimeZoneIsProvidedForSendEmail() throws Exception {
        MailRequestDto mailRequestDto = new MailRequestDto(RECIPIENT_ADDRESS, TITLE, CONTENT, false);
        mockMvc.perform(MockMvcRequestBuilders.post(SEND_EMAIL_URI)
                        .header("Time-Zone", "Asia")
                        .content(mailRequestDto.toJson())
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
    void Should_ReturnBadRequest_When_MissingRequiredFieldsAreProvidedForSendEmail() throws Exception {
        MailRequestDto mailRequestDto = new MailRequestDto(RECIPIENT_ADDRESS, "", CONTENT, false);
        mockMvc.perform(MockMvcRequestBuilders.post(SEND_EMAIL_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .content(mailRequestDto.toJson())
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
    void Should_ReturnBadRequest_When_InvalidEmailAddressIsProvidedForSendEmail() throws Exception {
        MailRequestDto mailRequestDto = new MailRequestDto(RECIPIENT_ADDRESS, TITLE, CONTENT, false);
        when(validator.isValidEmail(mailRequestDto.getRecipientAddress())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post(SEND_EMAIL_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .content(mailRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_EMAIL.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_EMAIL.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_SendEmailIsFailed() throws Exception {
        MailRequestDto mailRequestDto = new MailRequestDto(RECIPIENT_ADDRESS, TITLE, CONTENT, false);
        when(validator.isValidEmail(mailRequestDto.getRecipientAddress())).thenReturn(true);
        doThrow(new UtilServiceException("ERROR")).when(sesService).sendEmail(any(MailRequestDto.class));
        mockMvc.perform(MockMvcRequestBuilders.post(SEND_EMAIL_URI)
                        .header("Time-Zone", TIME_ZONE)
                        .content(mailRequestDto.toJson())
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

    @AfterEach
    void tearDown() {
    }
}