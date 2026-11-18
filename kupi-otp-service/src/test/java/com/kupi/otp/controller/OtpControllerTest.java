package com.kupi.otp.controller;

import com.kupi.otp.configuration.Translator;
import com.kupi.otp.domain.request.MobileNoRequestDto;
import com.kupi.otp.domain.request.OtpRequestDto;
import com.kupi.otp.domain.request.OtpVerifyRequestDto;
import com.kupi.otp.enums.ErrorResponseStatusType;
import com.kupi.otp.enums.SuccessResponseStatusType;
import com.kupi.otp.exception.InvalidOtpKeyException;
import com.kupi.otp.exception.OTPServiceException;
import com.kupi.otp.service.AuthService;
import com.kupi.otp.service.EmailOtpService;
import com.kupi.otp.service.RateLimitService;
import com.kupi.otp.service.SmsOtpService;
import com.kupi.otp.util.OtpGenerator;
import com.kupi.otp.util.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests {@link OtpController} class
 */
class OtpControllerTest {

    private static final String EMAIL_ADDRESS = "example@kupi.lk";
    private static final String COUNTRY_CODE = "+94";
    private static final String LOCAL_NUMBER = "771109101";
    private static final int OTP = 123456;
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String GENERATE_OTP_URI = "/api/v1/otp/generate";
    private static final String VERIFY_OTP_URI = "/api/v1/otp/verify";

    @Mock
    private SmsOtpService smsOtpService;
    @Mock
    private EmailOtpService emailOtpService;
    @Mock
    private Validator validator;
    @Mock
    private OtpGenerator otpGenerator;
    @Mock
    private RateLimitService rateLimitService;
    @Mock
    private Translator translator;
    private MockMvc mockMvc;

    @Mock
    private AuthService authService;


    @BeforeEach
    void setUp() {
        initMocks(this);
        OtpController otpController = new OtpController(smsOtpService, emailOtpService, validator, otpGenerator,
                translator, authService,rateLimitService);
        mockMvc = MockMvcBuilders.standaloneSetup(otpController).build();
    }


    @Test
    void Should_ReturnOk_When_WhenMobileNoIsNull() throws Exception {
        OtpRequestDto otpRequestDto = getSampleOtpRequestDto();
        otpRequestDto.setMobileNo(null);
        when(validator.isValidEmail(EMAIL_ADDRESS)).thenReturn(true);
        when(rateLimitService.isEmailOtpGenerationAllowed(EMAIL_ADDRESS)).thenReturn(true);
        when(otpGenerator.generateOtp()).thenReturn(OTP);
        mockMvc.perform(MockMvcRequestBuilders.post(GENERATE_OTP_URI)
                        .content(otpRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.SENT_OTP.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    void Should_ReturnOk_When_WhenEmailAddressIsNull() throws Exception {
        OtpRequestDto otpRequestDto = getSampleOtpRequestDto();
        otpRequestDto.setEmail(null);
        String mobileNo = otpRequestDto.getMobileNo().getNo().replace("-", "");
        when(validator.isValidMobileNo(otpRequestDto.getMobileNo().getNo())).thenReturn(true);
        when(rateLimitService.isMobileOtpGenerationAllowed(mobileNo)).thenReturn(true);
        when(otpGenerator.generateOtp()).thenReturn(OTP);
        mockMvc.perform(MockMvcRequestBuilders.post(GENERATE_OTP_URI)
                        .content(otpRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.SENT_OTP.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    void Should_ReturnBadRequest_When_BothEmailAddressAndMobileNoAreProvidedForGenerateOtp() throws Exception {
        OtpRequestDto otpRequestDto = getSampleOtpRequestDto();
        mockMvc.perform(MockMvcRequestBuilders.post(GENERATE_OTP_URI)
                        .content(otpRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MULTIPLE_TYPES_NOT_SUPPORTED.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MULTIPLE_TYPES_NOT_SUPPORTED.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_MissingRequiredFieldsAreProvidedForGenerateOtp() throws Exception {
        OtpRequestDto otpRequestDto = getSampleOtpRequestDto();
        otpRequestDto.setEmail("");
        otpRequestDto.setMobileNo(null);
        mockMvc.perform(MockMvcRequestBuilders.post(GENERATE_OTP_URI)
                        .content(otpRequestDto.toJson())
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
    void Should_ReturnBadRequest_When_InvalidMobileNoIsProvidedForGenerateOtp() throws Exception {
        OtpRequestDto otpRequestDto = getSampleOtpRequestDto();
        otpRequestDto.setEmail(null);
        otpRequestDto.setMobileNo(new MobileNoRequestDto("94", "0771109101"));
        when(validator.isValidMobileNo(otpRequestDto.getMobileNo().getNo())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post(GENERATE_OTP_URI)
                        .content(otpRequestDto.toJson())
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
    void Should_ReturnBadRequest_When_InvalidEmailAddressIsProvidedForGenerateOtp() throws Exception {
        OtpRequestDto otpRequestDto = getSampleOtpRequestDto();
        otpRequestDto.setEmail("examplekupi.lk");
        otpRequestDto.setMobileNo(null);
        when(validator.isValidEmail(otpRequestDto.getEmail())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post(GENERATE_OTP_URI)
                        .content(otpRequestDto.toJson())
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
    void Should_ReturnInternalServerError_When_GenerateOtpIsFailed() throws Exception {
        OtpRequestDto otpRequestDto = getSampleOtpRequestDto();
        otpRequestDto.setMobileNo(null);
        String email = otpRequestDto.getEmail().trim();
        when(validator.isValidEmail(email)).thenReturn(true);
        when(rateLimitService.isEmailOtpGenerationAllowed(email)).thenReturn(true);
        doThrow(new OTPServiceException("ERROR")).when(otpGenerator).generateOtp();
        mockMvc.perform(MockMvcRequestBuilders.post(GENERATE_OTP_URI)
                        .content(otpRequestDto.toJson())
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

    @Test
    void Should_ReturnOk_When_MobileNoIsNullForVerifyOtp() throws Exception {
        OtpVerifyRequestDto otpVerifyRequestDto = getSampleOtpVerifyRequestDto();
        otpVerifyRequestDto.setMobileNo(null);
        when(validator.isValidEmail(otpVerifyRequestDto.getEmail())).thenReturn(true);
        when(validator.isValidOtp(otpVerifyRequestDto.getOtp())).thenReturn(true);
        when(emailOtpService.verifyOtp(otpVerifyRequestDto.getEmail(), otpVerifyRequestDto.getOtp())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_OTP_URI)
                        .content(otpVerifyRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.VERIFIED_OTP.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    void Should_ReturnOk_When_MobileNoAndOtpAreNotNullForVerifyOtp() throws Exception {
        OtpVerifyRequestDto otpVerifyRequestDto = getSampleOtpVerifyRequestDto();
        otpVerifyRequestDto.setEmail(null);
        String mobileNo = otpVerifyRequestDto.getMobileNo().getNo().replace("-", "");
        when(validator.isValidMobileNo(otpVerifyRequestDto.getMobileNo().getNo())).thenReturn(true);
        when(validator.isValidOtp(otpVerifyRequestDto.getOtp())).thenReturn(true);
        when(smsOtpService.verifyOtp(mobileNo, String.valueOf(OTP))).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_OTP_URI)
                        .content(otpVerifyRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.VERIFIED_OTP.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    void Should_ReturnBadRequest_When_BothEmailAddressAndMobileNoAreProvidedForVerifyOtp() throws Exception {
        OtpVerifyRequestDto otpVerifyRequestDto = getSampleOtpVerifyRequestDto();
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_OTP_URI)
                        .content(otpVerifyRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MULTIPLE_TYPES_NOT_SUPPORTED.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MULTIPLE_TYPES_NOT_SUPPORTED.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_MissingRequiredFieldsAreProvidedForVerifyOtp() throws Exception {
        OtpVerifyRequestDto otpVerifyRequestDto = getSampleOtpVerifyRequestDto();
        otpVerifyRequestDto.setOtp("");
        otpVerifyRequestDto.setMobileNo(null);
        otpVerifyRequestDto.setEmail(EMAIL_ADDRESS);
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_OTP_URI)
                        .content(otpVerifyRequestDto.toJson())
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
    void Should_ReturnBadRequest_When_InvalidMobileNoIsProvidedForVerifyOtp() throws Exception {
        OtpVerifyRequestDto otpVerifyRequestDto = getSampleOtpVerifyRequestDto();
        otpVerifyRequestDto.setEmail(null);
        otpVerifyRequestDto.setMobileNo(new MobileNoRequestDto("94", "0771109101"));
        when(validator.isValidMobileNo(otpVerifyRequestDto.getMobileNo().getNo())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_OTP_URI)
                        .content(otpVerifyRequestDto.toJson())
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
    void Should_ReturnBadRequest_When_InvalidEmailAddressIsProvidedForVerifyOtp() throws Exception {
        OtpVerifyRequestDto otpVerifyRequestDto = getSampleOtpVerifyRequestDto();
        otpVerifyRequestDto.setEmail("examplekupi.lk");
        otpVerifyRequestDto.setMobileNo(null);
        when(validator.isValidEmail(otpVerifyRequestDto.getEmail())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_OTP_URI)
                        .content(otpVerifyRequestDto.toJson())
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
    void Should_ReturnBadRequest_When_InvalidEmailOtpIsProvidedForVerifyOtp() throws Exception {
        OtpVerifyRequestDto otpVerifyRequestDto = getSampleOtpVerifyRequestDto();
        otpVerifyRequestDto.setMobileNo(null);
        when(validator.isValidEmail(otpVerifyRequestDto.getEmail())).thenReturn(true);
        when(validator.isValidOtp(otpVerifyRequestDto.getOtp())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_OTP_URI)
                        .content(otpVerifyRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_OTP.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_OTP.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidSmsOtpIsProvidedForVerifyOtp() throws Exception {
        OtpVerifyRequestDto otpVerifyRequestDto = getSampleOtpVerifyRequestDto();
        otpVerifyRequestDto.setEmail(null);
        when(validator.isValidMobileNo(otpVerifyRequestDto.getMobileNo().getNo())).thenReturn(true);
        when(validator.isValidOtp(otpVerifyRequestDto.getOtp())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_OTP_URI)
                        .content(otpVerifyRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_OTP.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_OTP.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_EmailOtpVerificationIsFailedForVerifyOtp() throws Exception {
        OtpVerifyRequestDto otpVerifyRequestDto = getSampleOtpVerifyRequestDto();
        otpVerifyRequestDto.setMobileNo(null);
        when(validator.isValidEmail(otpVerifyRequestDto.getEmail())).thenReturn(true);
        when(validator.isValidOtp(otpVerifyRequestDto.getOtp())).thenReturn(true);
        when(emailOtpService.verifyOtp(otpVerifyRequestDto.getEmail(), otpVerifyRequestDto.getOtp())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_OTP_URI)
                        .content(otpVerifyRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_OTP.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_OTP.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_SmsOtpVerificationIsFailedForVerifyOtp() throws Exception {
        OtpVerifyRequestDto otpVerifyRequestDto = getSampleOtpVerifyRequestDto();
        otpVerifyRequestDto.setEmail(null);
        String mobileNo = otpVerifyRequestDto.getMobileNo().getNo().replace("-", "");
        when(validator.isValidMobileNo(otpVerifyRequestDto.getMobileNo().getNo())).thenReturn(true);
        when(validator.isValidOtp(otpVerifyRequestDto.getOtp())).thenReturn(true);
        when(smsOtpService.verifyOtp(mobileNo, otpVerifyRequestDto.getOtp())).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_OTP_URI)
                        .content(otpVerifyRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_OTP.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_OTP.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidKeyIsProvidedForEmailOtp() throws Exception {
        OtpVerifyRequestDto otpVerifyRequestDto = getSampleOtpVerifyRequestDto();
        otpVerifyRequestDto.setMobileNo(null);
        when(validator.isValidEmail(otpVerifyRequestDto.getEmail())).thenReturn(true);
        when(validator.isValidOtp(otpVerifyRequestDto.getOtp())).thenReturn(true);
        when(emailOtpService.verifyOtp(otpVerifyRequestDto.getEmail(), otpVerifyRequestDto.getOtp()))
                .thenThrow(new InvalidOtpKeyException("ERROR"));
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_OTP_URI)
                        .content(otpVerifyRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_EMAIL_OR_EXPIRED_OTP.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_EMAIL_OR_EXPIRED_OTP.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidKeyIsProvidedForSmsOtp() throws Exception {
        OtpVerifyRequestDto otpVerifyRequestDto = getSampleOtpVerifyRequestDto();
        otpVerifyRequestDto.setEmail(null);
        String mobileNo = otpVerifyRequestDto.getMobileNo().getNo().replace("-", "");
        when(validator.isValidMobileNo(otpVerifyRequestDto.getMobileNo().getNo())).thenReturn(true);
        when(validator.isValidOtp(otpVerifyRequestDto.getOtp())).thenReturn(true);
        when(smsOtpService.verifyOtp(mobileNo, otpVerifyRequestDto.getOtp()))
                .thenThrow(new InvalidOtpKeyException("ERROR"));
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_OTP_URI)
                        .content(otpVerifyRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_MOBILE_NUMBER_OR_EXPIRED_OTP.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_MOBILE_NUMBER_OR_EXPIRED_OTP.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_VerifyOtpIsFailed() throws Exception {
        OtpVerifyRequestDto otpVerifyRequestDto = getSampleOtpVerifyRequestDto();
        otpVerifyRequestDto.setEmail(null);
        String mobileNo = otpVerifyRequestDto.getMobileNo().getNo().replace("-", "");
        when(validator.isValidMobileNo(otpVerifyRequestDto.getMobileNo().getNo())).thenReturn(true);
        when(validator.isValidOtp(otpVerifyRequestDto.getOtp())).thenReturn(true);
        doThrow(new OTPServiceException("ERROR")).when(smsOtpService).verifyOtp(mobileNo, otpVerifyRequestDto.getOtp());
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_OTP_URI)
                        .content(otpVerifyRequestDto.toJson())
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
     * This method creates sample OtpRequestDto
     *
     * @return OtpRequestDto
     */
    private OtpRequestDto getSampleOtpRequestDto() {
        MobileNoRequestDto mobileNo = getSampleMobileNoRequestDto();
        OtpRequestDto otpRequestDto = new OtpRequestDto();
        otpRequestDto.setMobileNo(mobileNo);
        otpRequestDto.setEmail(EMAIL_ADDRESS);
        return otpRequestDto;
    }

    /**
     * This method creates sample OtpVerifyRequestDto
     *
     * @return OtpVerifyRequestDto
     */
    private OtpVerifyRequestDto getSampleOtpVerifyRequestDto() {
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        OtpVerifyRequestDto otpVerifyRequestDto = new OtpVerifyRequestDto();
        otpVerifyRequestDto.setOtp(String.valueOf(OTP));
        otpVerifyRequestDto.setEmail(EMAIL_ADDRESS);
        otpVerifyRequestDto.setMobileNo(mobileNoRequestDto);
        return otpVerifyRequestDto;
    }
}