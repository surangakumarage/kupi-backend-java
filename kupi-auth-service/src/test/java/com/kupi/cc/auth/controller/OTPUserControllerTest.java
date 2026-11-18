package com.kupi.cc.auth.controller;

import com.kupi.cc.auth.configuration.Translator;
import com.kupi.cc.auth.domain.entity.Permission;
import com.kupi.cc.auth.domain.entity.Role;
import com.kupi.cc.auth.domain.entity.User;
import com.kupi.cc.auth.domain.request.MobileNoRequestDto;
import com.kupi.cc.auth.domain.request.OTPLoginDto;
import com.kupi.cc.auth.domain.request.OTPUserRegistrationDto;
import com.kupi.cc.auth.enums.ErrorResponseStatusType;
import com.kupi.cc.auth.enums.Language;
import com.kupi.cc.auth.enums.RoleType;
import com.kupi.cc.auth.enums.SuccessResponseStatusType;
import com.kupi.cc.auth.exception.InvalidTokenException;
import com.kupi.cc.auth.exception.InvalidUserException;
import com.kupi.cc.auth.service.OTPTokenService;
import com.kupi.cc.auth.service.UserService;
import com.kupi.cc.auth.util.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.mockito.ArgumentMatchers;

/**
 * Unit Tests for OTPUserController
 * Tests cover all three OTP endpoints:
 * - POST /api/v1/otp/users/request-otp
 * - POST /api/v1/otp/users/verify-and-register
 * - POST /api/v1/otp/users/verify-and-login
 */
class OTPUserControllerTest {

    // ======================== Constants ========================
    private static final String APPLICATION_JSON_UTF_8 = "application/json;charset=UTF-8";
    private static final String VALID_MOBILE_NO = "+94712417927";
    private static final String INVALID_MOBILE_NO = "invalid-mobile";
    private static final String VALID_EMAIL = "john@example.com";
    private static final String INVALID_EMAIL = "invalid-email";
    private static final String VALID_PASSWORD = "Kupi@123";
    private static final String INVALID_PASSWORD = "weak";
    private static final String USER_ID = "user-123";
    private static final String USER_NAME = "John Doe";
    private static final String FULL_NAME = "John Doe";
    private static final String OTP_TOKEN = "123456";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String REQUEST_OTP_URI = "/api/v1/otp/users/request-otp";
    private static final String VERIFY_REGISTER_URI = "/api/v1/otp/users/verify-and-register";
    private static final String VERIFY_LOGIN_URI = "/api/v1/otp/users/verify-and-login";
    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String CLIENT_ID = "tokomobile";
    private static final String LANGUAGE = "ENGLISH";

    // ======================== Mocks ========================
    @Mock
    private UserService userService;

    @Mock
    private OTPTokenService otpTokenService;

    @Mock
    private Translator translator;

    @Mock
    private Validator validator;

    @Mock
    private DefaultTokenServices defaultTokenServices;

    // ======================== Test Fixtures ========================
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        initMocks(this);
        OTPUserController otpUserController = new OTPUserController(
                userService,
                otpTokenService,
                validator,
                defaultTokenServices,
                CLIENT_ID,
                translator
        );
        mockMvc = MockMvcBuilders.standaloneSetup(otpUserController).build();
    }

    // ======================== REQUEST-OTP Endpoint Tests ========================

    @Test
    void Should_ReturnOk_When_RequestOTPWithValidMobileNo() throws Exception {
        // Arrange
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        when(validator.isValidMobileNoWithCountryCode(mobileNoRequestDto.getNo())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post(REQUEST_OTP_URI)
                .header(TIME_ZONE_HEADER, TIME_ZONE)
                .content(mobileNoRequestDto.toJson())
                .contentType(APPLICATION_JSON_UTF_8)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
            .andExpect(jsonPath("$.status", is(SUCCESS_STATUS)))
            .andExpect(jsonPath("$.message", is(SuccessResponseStatusType.OTP_SENT.getMessage())))
            .andExpect(jsonPath("$.data", nullValue()));

        verify(otpTokenService, times(1)).requestOTP(ArgumentMatchers.any(MobileNoRequestDto.class));
    }

    @Test
    void Should_ReturnBadRequest_When_RequestOTPWithInvalidMobileNo() throws Exception {
        // Arrange
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        mobileNoRequestDto.setLocalNumber(INVALID_MOBILE_NO);
        when(validator.isValidMobileNoWithCountryCode(mobileNoRequestDto.getNo())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post(REQUEST_OTP_URI)
                .header(TIME_ZONE_HEADER, TIME_ZONE)
                .content(mobileNoRequestDto.toJson())
                .contentType(APPLICATION_JSON_UTF_8))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
            .andExpect(jsonPath("$.message", is(ErrorResponseStatusType.INVALID_MOBILE_NO.getMessage())));

        verify(otpTokenService, never()).requestOTP(any());
    }

    @Test
    void Should_ReturnBadRequest_When_RequestOTPWithMissingFields() throws Exception {
        // Arrange
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto();
        // Don't set any fields - missing required fields

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post(REQUEST_OTP_URI)
                .header(TIME_ZONE_HEADER, TIME_ZONE)
                .content(mobileNoRequestDto.toJson())
                .contentType(APPLICATION_JSON_UTF_8))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
            .andExpect(jsonPath("$.message", is(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage())));

        verify(otpTokenService, never()).requestOTP(any());
    }

    @Test
    void Should_ReturnInternalServerError_When_RequestOTPThrowsException() throws Exception {
        // Arrange
        MobileNoRequestDto mobileNoRequestDto = getSampleMobileNoRequestDto();
        when(validator.isValidMobileNoWithCountryCode(mobileNoRequestDto.getNo())).thenReturn(true);
        doThrow(new RuntimeException("Service error")).when(otpTokenService).requestOTP(any());

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post(REQUEST_OTP_URI)
                .header(TIME_ZONE_HEADER, TIME_ZONE)
                .content(mobileNoRequestDto.toJson())
                .contentType(APPLICATION_JSON_UTF_8))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status", is(ERROR_STATUS)));
    }

    // ======================== VERIFY-AND-REGISTER Endpoint Tests ========================

    @Test
    void Should_ReturnOk_When_VerifyAndRegisterWithValidData() throws Exception {
        // Arrange
        OTPUserRegistrationDto registrationDto = getSampleOTPUserRegistrationDto();
        MobileNoRequestDto mobileNo = registrationDto.getMobileNo();

        when(validator.isValidMobileNoWithCountryCode(mobileNo.getNo())).thenReturn(true);
        when(validator.isValidPassword(registrationDto.getPassword())).thenReturn(true);
        when(validator.isValidEmail(registrationDto.getEmail())).thenReturn(true);
        when(userService.isMobileNoExist(mobileNo.getNo())).thenReturn(false);
        when(userService.isEmailExist(registrationDto.getEmail())).thenReturn(false);

        User createdUser = getSampleUser();
        when(userService.createOTPUser(ArgumentMatchers.any(OTPUserRegistrationDto.class), eq(TIME_ZONE))).thenReturn(createdUser);

        OAuth2AccessToken accessToken = getOAuth2AccessToken();
        when(defaultTokenServices.createAccessToken(ArgumentMatchers.any())).thenReturn(accessToken);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_REGISTER_URI)
                .header(TIME_ZONE_HEADER, TIME_ZONE)
                .content(registrationDto.toJson())
                .contentType(APPLICATION_JSON_UTF_8))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is(SUCCESS_STATUS)))
            .andExpect(jsonPath("$.message", is(SuccessResponseStatusType.CREATE_USER.getMessage())))
            .andExpect(jsonPath("$.data.user.userId", is(USER_ID)))
            .andExpect(jsonPath("$.data.user.fullName", is(FULL_NAME)))
            .andExpect(jsonPath("$.data.accessToken", notNullValue()))
            .andExpect(jsonPath("$.data.tokenType", is("Bearer")));

        verify(otpTokenService, times(1)).verifyOTP(OTP_TOKEN);
        verify(otpTokenService, times(1)).deleteOTP(OTP_TOKEN);
        verify(userService, times(1)).createOTPUser(ArgumentMatchers.any(OTPUserRegistrationDto.class), eq(TIME_ZONE));
        verify(defaultTokenServices, times(1)).createAccessToken(ArgumentMatchers.any());
    }

    @Test
    void Should_ReturnBadRequest_When_VerifyAndRegisterWithInvalidOTP() throws Exception {
        // Arrange
        OTPUserRegistrationDto registrationDto = getSampleOTPUserRegistrationDto();
        doThrow(new InvalidTokenException("OTP expired")).when(otpTokenService).verifyOTP(OTP_TOKEN);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_REGISTER_URI)
                .header(TIME_ZONE_HEADER, TIME_ZONE)
                .content(registrationDto.toJson())
                .contentType(APPLICATION_JSON_UTF_8))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
            .andExpect(jsonPath("$.message", is(ErrorResponseStatusType.INVALID_OTP.getMessage())));

        verify(userService, never()).createOTPUser(any(), any());
        verify(otpTokenService, never()).deleteOTP(any());
    }

    @Test
    void Should_ReturnBadRequest_When_VerifyAndRegisterWithInvalidMobileNo() throws Exception {
        // Arrange
        OTPUserRegistrationDto registrationDto = getSampleOTPUserRegistrationDto();
        MobileNoRequestDto mobileNo = registrationDto.getMobileNo();

        when(validator.isValidMobileNoWithCountryCode(mobileNo.getNo())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_REGISTER_URI)
                .header(TIME_ZONE_HEADER, TIME_ZONE)
                .content(registrationDto.toJson())
                .contentType(APPLICATION_JSON_UTF_8))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
            .andExpect(jsonPath("$.message", is(ErrorResponseStatusType.INVALID_MOBILE_NO.getMessage())));

        verify(userService, never()).createOTPUser(any(), any());
    }

    @Test
    void Should_ReturnBadRequest_When_VerifyAndRegisterWithInvalidPassword() throws Exception {
        // Arrange
        OTPUserRegistrationDto registrationDto = getSampleOTPUserRegistrationDto();
        registrationDto.setPassword(INVALID_PASSWORD);
        MobileNoRequestDto mobileNo = registrationDto.getMobileNo();

        when(validator.isValidMobileNoWithCountryCode(mobileNo.getNo())).thenReturn(true);
        when(validator.isValidPassword(INVALID_PASSWORD)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_REGISTER_URI)
                .header(TIME_ZONE_HEADER, TIME_ZONE)
                .content(registrationDto.toJson())
                .contentType(APPLICATION_JSON_UTF_8))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
            .andExpect(jsonPath("$.message", is(ErrorResponseStatusType.INVALID_PASSWORD.getMessage())));

        verify(userService, never()).createOTPUser(any(), any());
    }

    @Test
    void Should_ReturnBadRequest_When_VerifyAndRegisterWithInvalidEmail() throws Exception {
        // Arrange
        OTPUserRegistrationDto registrationDto = getSampleOTPUserRegistrationDto();
        registrationDto.setEmail(INVALID_EMAIL);
        MobileNoRequestDto mobileNo = registrationDto.getMobileNo();

        when(validator.isValidMobileNoWithCountryCode(mobileNo.getNo())).thenReturn(true);
        when(validator.isValidPassword(registrationDto.getPassword())).thenReturn(true);
        when(validator.isValidEmail(INVALID_EMAIL)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_REGISTER_URI)
                .header(TIME_ZONE_HEADER, TIME_ZONE)
                .content(registrationDto.toJson())
                .contentType(APPLICATION_JSON_UTF_8))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
            .andExpect(jsonPath("$.message", is(ErrorResponseStatusType.INVALID_EMAIL.getMessage())));

        verify(userService, never()).createOTPUser(any(), any());
    }

    @Test
    void Should_ReturnBadRequest_When_VerifyAndRegisterWithExistingMobileNo() throws Exception {
        // Arrange
        OTPUserRegistrationDto registrationDto = getSampleOTPUserRegistrationDto();
        MobileNoRequestDto mobileNo = registrationDto.getMobileNo();

        when(validator.isValidMobileNoWithCountryCode(mobileNo.getNo())).thenReturn(true);
        when(validator.isValidPassword(registrationDto.getPassword())).thenReturn(true);
        when(validator.isValidEmail(registrationDto.getEmail())).thenReturn(true);
        when(userService.isMobileNoExist(mobileNo.getNo())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_REGISTER_URI)
                .header(TIME_ZONE_HEADER, TIME_ZONE)
                .content(registrationDto.toJson())
                .contentType(APPLICATION_JSON_UTF_8))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
            .andExpect(jsonPath("$.message", is(ErrorResponseStatusType.EXISTING_MOBILE_NO.getMessage())));

        verify(userService, never()).createOTPUser(any(), any());
    }

    @Test
    void Should_ReturnBadRequest_When_VerifyAndRegisterWithExistingEmail() throws Exception {
        // Arrange
        OTPUserRegistrationDto registrationDto = getSampleOTPUserRegistrationDto();
        MobileNoRequestDto mobileNo = registrationDto.getMobileNo();

        when(validator.isValidMobileNoWithCountryCode(mobileNo.getNo())).thenReturn(true);
        when(validator.isValidPassword(registrationDto.getPassword())).thenReturn(true);
        when(validator.isValidEmail(registrationDto.getEmail())).thenReturn(true);
        when(userService.isMobileNoExist(mobileNo.getNo())).thenReturn(false);
        when(userService.isEmailExist(registrationDto.getEmail())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_REGISTER_URI)
                .header(TIME_ZONE_HEADER, TIME_ZONE)
                .content(registrationDto.toJson())
                .contentType(APPLICATION_JSON_UTF_8))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
            .andExpect(jsonPath("$.message", is(ErrorResponseStatusType.EXISTING_EMAIL.getMessage())));

        verify(userService, never()).createOTPUser(any(), any());
    }

    @Test
    void Should_ReturnBadRequest_When_VerifyAndRegisterWithMissingFields() throws Exception {
        // Arrange
        OTPUserRegistrationDto registrationDto = new OTPUserRegistrationDto();
        // Don't set any fields - missing required fields

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_REGISTER_URI)
                .header(TIME_ZONE_HEADER, TIME_ZONE)
                .content(registrationDto.toJson())
                .contentType(APPLICATION_JSON_UTF_8))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
            .andExpect(jsonPath("$.message", is(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage())));

        verify(userService, never()).createOTPUser(any(), any());
    }

    // ======================== VERIFY-AND-LOGIN Endpoint Tests ========================

    @Test
    void Should_ReturnOk_When_VerifyAndLoginWithValidData() throws Exception {
        // Arrange
        OTPLoginDto loginDto = getSampleOTPLoginDto();
        MobileNoRequestDto mobileNo = loginDto.getMobileNo();

        when(validator.isValidMobileNoWithCountryCode(mobileNo.getNo())).thenReturn(true);

        User user = getSampleUser();
        when(userService.getUserByMobileNo(mobileNo.getNo())).thenReturn(user);

        OAuth2AccessToken accessToken = getOAuth2AccessToken();
        when(defaultTokenServices.createAccessToken(ArgumentMatchers.any())).thenReturn(accessToken);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_LOGIN_URI)
                .header(TIME_ZONE_HEADER, TIME_ZONE)
                .content(loginDto.toJson())
                .contentType(APPLICATION_JSON_UTF_8))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is(SUCCESS_STATUS)))
            .andExpect(jsonPath("$.message", is(SuccessResponseStatusType.LOGGED_IN.getMessage())))
            .andExpect(jsonPath("$.data.user.userId", is(USER_ID)))
            .andExpect(jsonPath("$.data.user.fullName", is(FULL_NAME)))
            .andExpect(jsonPath("$.data.accessToken", notNullValue()))
            .andExpect(jsonPath("$.data.tokenType", is("Bearer")));

        verify(otpTokenService, times(1)).verifyOTP(OTP_TOKEN);
        verify(otpTokenService, times(1)).deleteOTP(OTP_TOKEN);
        verify(userService, times(1)).getUserByMobileNo(mobileNo.getNo());
        verify(defaultTokenServices, times(1)).createAccessToken(ArgumentMatchers.any());
    }

    @Test
    void Should_ReturnBadRequest_When_VerifyAndLoginWithInvalidOTP() throws Exception {
        // Arrange
        OTPLoginDto loginDto = getSampleOTPLoginDto();
        doThrow(new InvalidTokenException("OTP expired")).when(otpTokenService).verifyOTP(OTP_TOKEN);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_LOGIN_URI)
                .header(TIME_ZONE_HEADER, TIME_ZONE)
                .content(loginDto.toJson())
                .contentType(APPLICATION_JSON_UTF_8))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
            .andExpect(jsonPath("$.message", is(ErrorResponseStatusType.INVALID_OTP.getMessage())));

        verify(userService, never()).getUserByMobileNo(any());
        verify(otpTokenService, never()).deleteOTP(any());
    }

    @Test
    void Should_ReturnBadRequest_When_VerifyAndLoginWithInvalidMobileNo() throws Exception {
        // Arrange
        OTPLoginDto loginDto = getSampleOTPLoginDto();
        MobileNoRequestDto mobileNo = loginDto.getMobileNo();

        when(validator.isValidMobileNoWithCountryCode(mobileNo.getNo())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_LOGIN_URI)
                .header(TIME_ZONE_HEADER, TIME_ZONE)
                .content(loginDto.toJson())
                .contentType(APPLICATION_JSON_UTF_8))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
            .andExpect(jsonPath("$.message", is(ErrorResponseStatusType.INVALID_MOBILE_NO.getMessage())));

        verify(userService, never()).getUserByMobileNo(any());
    }

    @Test
    void Should_ReturnBadRequest_When_VerifyAndLoginWithNonExistentUser() throws Exception {
        // Arrange
        OTPLoginDto loginDto = getSampleOTPLoginDto();
        MobileNoRequestDto mobileNo = loginDto.getMobileNo();

        when(validator.isValidMobileNoWithCountryCode(mobileNo.getNo())).thenReturn(true);
        when(userService.getUserByMobileNo(mobileNo.getNo())).thenThrow(new InvalidUserException("User not found"));

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_LOGIN_URI)
                .header(TIME_ZONE_HEADER, TIME_ZONE)
                .content(loginDto.toJson())
                .contentType(APPLICATION_JSON_UTF_8))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
            .andExpect(jsonPath("$.message", is(ErrorResponseStatusType.NO_USER.getMessage())));

        verify(otpTokenService, times(1)).verifyOTP(OTP_TOKEN);
        verify(otpTokenService, never()).deleteOTP(any());
    }

    @Test
    void Should_ReturnBadRequest_When_VerifyAndLoginWithMissingFields() throws Exception {
        // Arrange
        OTPLoginDto loginDto = new OTPLoginDto();
        // Don't set any fields - missing required fields

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_LOGIN_URI)
                .header(TIME_ZONE_HEADER, TIME_ZONE)
                .content(loginDto.toJson())
                .contentType(APPLICATION_JSON_UTF_8))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
            .andExpect(jsonPath("$.message", is(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage())));

        verify(userService, never()).getUserByMobileNo(any());
    }

    // ======================== Test Data Builders ========================

    /**
     * Build sample MobileNoRequestDto with valid data
     */
    private MobileNoRequestDto getSampleMobileNoRequestDto() {
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto();
        mobileNoRequestDto.setCountryCode("+94");
        mobileNoRequestDto.setLocalNumber("712417927");
        return mobileNoRequestDto;
    }

    /**
     * Build sample OTPUserRegistrationDto with valid data
     */
    private OTPUserRegistrationDto getSampleOTPUserRegistrationDto() {
        OTPUserRegistrationDto registrationDto = new OTPUserRegistrationDto();
        registrationDto.setToken(OTP_TOKEN);
        registrationDto.setMobileNo(getSampleMobileNoRequestDto());
        registrationDto.setFullName(FULL_NAME);
        registrationDto.setEmail(VALID_EMAIL);
        registrationDto.setPassword(VALID_PASSWORD);
        registrationDto.setLanguage(LANGUAGE);
        return registrationDto;
    }

    /**
     * Build sample OTPLoginDto with valid data
     */
    private OTPLoginDto getSampleOTPLoginDto() {
        OTPLoginDto loginDto = new OTPLoginDto();
        loginDto.setToken(OTP_TOKEN);
        loginDto.setMobileNo(getSampleMobileNoRequestDto());
        return loginDto;
    }

    /**
     * Build sample User entity
     */
    private User getSampleUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setFullName(FULL_NAME);
        user.setEmail(VALID_EMAIL);
        user.setPassword(VALID_PASSWORD);
        user.setMobileNo(VALID_MOBILE_NO);
        user.setLanguage(Language.valueOf(LANGUAGE));
        user.setEnabled(true);
        user.setImageUrl("");
        Date now = new Date();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setRegisteredUser(true);

        Role role = new Role(RoleType.USER);
        user.setRole(role);

        return user;
    }

    /**
     * Build sample OAuth2AccessToken
     */
    private OAuth2AccessToken getOAuth2AccessToken() {
        DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken("test-access-token");
        token.setTokenType("Bearer");
        token.setExpiration(new Date(System.currentTimeMillis() + 3600000)); // 1 hour from now
        return token;
    }
}
