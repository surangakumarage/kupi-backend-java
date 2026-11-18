package com.kupi.cc.auth.controller;

import com.kupi.cc.auth.configuration.ResourceBundleMessageSourceBean;
import com.kupi.cc.auth.configuration.Translator;
import com.kupi.cc.auth.domain.entity.User;
import com.kupi.cc.auth.domain.request.FacebookSignupRequestDto;
import com.kupi.cc.auth.domain.response.SocialSignInResponseDto;
import com.kupi.cc.auth.enums.ErrorResponseStatusType;
import com.kupi.cc.auth.enums.Language;
import com.kupi.cc.auth.enums.SuccessResponseStatusType;
import com.kupi.cc.auth.exception.*;
import com.kupi.cc.auth.service.FacebookAuthService;
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

import java.util.Locale;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests {@link FacebookController} class
 */
class FacebookControllerTest {

    private static final String FACEBOOK_SIGN_UP_URI = "/api/v1/facebook/signup";
    private static final String FACEBOOK_SIGN_IN_URI = "/api/v1/facebook/sign-in";
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String APPLICATION_JSON_UTF_8 = "application/json;charset=UTF-8";
    private static final String ACCESS_TOKEN = "Bearer192f0c96-dd19-404e-8217-a8fd1cc81d55";
    private static final String USER_ID = "uid-d9d069f9-2b07-446f-8117-e78ff14d01d8";
    private static final String FULL_NAME = "John Dore";
    private static final String USER_EMAIL = "rengy@tokoin.io";
    private static final String USER_MOBILE_NO = "+62-81123123";
    private final ResourceBundleMessageSourceBean resourceBundleMessageSourceBean = new ResourceBundleMessageSourceBean();
    private final Locale locale = LocaleContextHolder.getLocale();
    private ResourceBundleMessageSource resourceBundleMessageSource;
    @Mock
    private FacebookAuthService facebookAuthService;
    @Mock
    private Translator translator;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        initMocks(this);
        FacebookController facebookController = new FacebookController(facebookAuthService, translator);
        mockMvc= MockMvcBuilders.standaloneSetup(facebookController).build();
        resourceBundleMessageSource = resourceBundleMessageSourceBean.messageSource();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnOk_When_FacebookSignUpIsSuccessful() throws Exception {
        User user = getSampleUser();
        FacebookSignupRequestDto facebookSignupRequestDto = new FacebookSignupRequestDto();
        facebookSignupRequestDto.setAccessToken(ACCESS_TOKEN);
        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType.CREATE_USER.getCode(),
                null, locale);
        when(translator.toLocale(SuccessResponseStatusType.CREATE_USER.getCode()))
                .thenReturn(successMessage);
        when(facebookAuthService.createUser(ACCESS_TOKEN)).thenReturn(user);
        mockMvc.perform(MockMvcRequestBuilders.post(FACEBOOK_SIGN_UP_URI)
                        .content(facebookSignupRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(SUCCESS_STATUS)))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.CREATE_USER.getMessage()))
                .andExpect(jsonPath("$.data.userId").value(USER_ID))
                .andExpect(jsonPath("$.data.language").value("ENGLISH"))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_RequiredFieldsAreNotProvidedForFacebookSignUp() throws Exception {
        FacebookSignupRequestDto facebookSignupRequestDto = new FacebookSignupRequestDto();
        facebookSignupRequestDto.setAccessToken("");
        String errorMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .MISSING_REQUIRED_FIELDS.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode())))
                .thenReturn(errorMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(FACEBOOK_SIGN_UP_URI)
                        .content(facebookSignupRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.displayMessage").value(errorMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UnableToAccessUserEmailForFacebookSignUp() throws Exception {
        FacebookSignupRequestDto facebookSignupRequestDto = new FacebookSignupRequestDto();
        facebookSignupRequestDto.setAccessToken(ACCESS_TOKEN);
        String errorMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .NO_EMAIL_ACCESS.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.NO_EMAIL_ACCESS.getCode())))
                .thenReturn(errorMessage);
        doThrow(new NoEmailAccessException("ERROR")).when(facebookAuthService).createUser(anyString());
        mockMvc.perform(MockMvcRequestBuilders.post(FACEBOOK_SIGN_UP_URI)
                        .content(facebookSignupRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.NO_EMAIL_ACCESS.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.displayMessage").value(errorMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UserAlreadyExistsForFacebookSignUp() throws Exception {
        FacebookSignupRequestDto facebookSignupRequestDto = new FacebookSignupRequestDto();
        facebookSignupRequestDto.setAccessToken(ACCESS_TOKEN);
        String errorMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .EXISTING_USER.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.EXISTING_USER.getCode())))
                .thenReturn(errorMessage);
        doThrow(new UserAlreadyExistsException("ERROR")).when(facebookAuthService).createUser(anyString());
        mockMvc.perform(MockMvcRequestBuilders.post(FACEBOOK_SIGN_UP_URI)
                        .content(facebookSignupRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.EXISTING_USER.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.displayMessage").value(errorMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidUserTokenIsProvidedForFacebookSignUp() throws Exception {
        FacebookSignupRequestDto facebookSignupRequestDto = new FacebookSignupRequestDto();
        facebookSignupRequestDto.setAccessToken(ACCESS_TOKEN);
        String errorMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_USER_TOKEN.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_USER_TOKEN.getCode())))
                .thenReturn(errorMessage);
        doThrow(new InvalidUserException("ERROR")).when(facebookAuthService).createUser(anyString());
        mockMvc.perform(MockMvcRequestBuilders.post(FACEBOOK_SIGN_UP_URI)
                        .content(facebookSignupRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_TOKEN.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.displayMessage").value(errorMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_FacebookSignUpIsFailed() throws Exception {
        FacebookSignupRequestDto facebookSignupRequestDto = new FacebookSignupRequestDto();
        facebookSignupRequestDto.setAccessToken(ACCESS_TOKEN);
        String errorMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(errorMessage);
        doThrow(new AuthServiceException("ERROR")).when(facebookAuthService).createUser(anyString());
        mockMvc.perform(MockMvcRequestBuilders.post(FACEBOOK_SIGN_UP_URI)
                        .content(facebookSignupRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.displayMessage").value(errorMessage));
    }

    @Test
    void Should_ReturnOk_When_FacebookSignInIsSuccessful() throws Exception {
        SocialSignInResponseDto socialSignInResponseDto = getSampleSocialSignInResponseDto();
        FacebookSignupRequestDto facebookSignupRequestDto = new FacebookSignupRequestDto();
        facebookSignupRequestDto.setAccessToken(ACCESS_TOKEN);
        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType.LOGGED_IN.getCode(),
                null, locale);
        when(translator.toLocale(SuccessResponseStatusType.LOGGED_IN.getCode()))
                .thenReturn(successMessage);
        when(facebookAuthService.loginUser(ACCESS_TOKEN)).thenReturn(socialSignInResponseDto);
        mockMvc.perform(MockMvcRequestBuilders.post(FACEBOOK_SIGN_IN_URI)
                        .content(facebookSignupRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(SUCCESS_STATUS)))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.LOGGED_IN.getMessage()))
                .andExpect(jsonPath("$.data.userId").value(USER_ID))
                .andExpect(jsonPath("$.data.language").value("ENGLISH"))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_RequiredFieldsAreNotProvidedForFacebookSignIn() throws Exception {
        FacebookSignupRequestDto facebookSignupRequestDto = new FacebookSignupRequestDto();
        facebookSignupRequestDto.setAccessToken("");
        String errorMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .MISSING_REQUIRED_FIELDS.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode())))
                .thenReturn(errorMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(FACEBOOK_SIGN_IN_URI)
                        .content(facebookSignupRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.displayMessage").value(errorMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UserNotExistsForFacebookSignIn() throws Exception {
        FacebookSignupRequestDto facebookSignupRequestDto = new FacebookSignupRequestDto();
        facebookSignupRequestDto.setAccessToken(ACCESS_TOKEN);
        String errorMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .NO_USER.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.NO_USER.getCode())))
                .thenReturn(errorMessage);
        doThrow(new NoUserException("ERROR")).when(facebookAuthService).loginUser(ACCESS_TOKEN);
        mockMvc.perform(MockMvcRequestBuilders.post(FACEBOOK_SIGN_IN_URI)
                        .content(facebookSignupRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.NO_USER.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.displayMessage").value(errorMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidUserTokenIsProvidedForFacebookSignIn() throws Exception {
        FacebookSignupRequestDto facebookSignupRequestDto = new FacebookSignupRequestDto();
        facebookSignupRequestDto.setAccessToken(ACCESS_TOKEN);
        String errorMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_USER_TOKEN.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_USER_TOKEN.getCode())))
                .thenReturn(errorMessage);
        doThrow(new InvalidUserException("ERROR")).when(facebookAuthService).loginUser(ACCESS_TOKEN);
        mockMvc.perform(MockMvcRequestBuilders.post(FACEBOOK_SIGN_IN_URI)
                        .content(facebookSignupRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_TOKEN.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.displayMessage").value(errorMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_FacebookSignInIsFailed() throws Exception {
        FacebookSignupRequestDto facebookSignupRequestDto = new FacebookSignupRequestDto();
        facebookSignupRequestDto.setAccessToken(ACCESS_TOKEN);
        String errorMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(errorMessage);
        doThrow(new AuthServiceException("ERROR")).when(facebookAuthService).loginUser(ACCESS_TOKEN);
        mockMvc.perform(MockMvcRequestBuilders.post(FACEBOOK_SIGN_IN_URI)
                        .content(facebookSignupRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.displayMessage").value(errorMessage));
    }

    /**
     * This method creates sample user
     *
     * @return User
     */
    private User getSampleUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setFullName(FULL_NAME);
        user.setEmail(USER_EMAIL);
        user.setMobileNo(USER_MOBILE_NO);
        user.setImageUrl("");
        user.setLanguage(Language.valueOf("ENGLISH"));
        user.setEnabled(true);
        user.setRegisteredUser(true);
        return user;
    }

    /**
     * This method creates sample social sign in response dto
     *
     * @return SocialSignInResponseDto
     */
    private SocialSignInResponseDto getSampleSocialSignInResponseDto() {
        SocialSignInResponseDto socialSignInResponseDto = new SocialSignInResponseDto();
        socialSignInResponseDto.setLanguage(Language.ENGLISH);
        socialSignInResponseDto.setAccessToken(ACCESS_TOKEN);
        socialSignInResponseDto.setRefreshToken(ACCESS_TOKEN);
        socialSignInResponseDto.setUserId(USER_ID);
        return socialSignInResponseDto;
    }
}