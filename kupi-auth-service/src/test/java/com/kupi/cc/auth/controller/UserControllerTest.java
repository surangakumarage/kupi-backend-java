package com.kupi.cc.auth.controller;

import com.kupi.cc.auth.configuration.ResourceBundleMessageSourceBean;
import com.kupi.cc.auth.configuration.Translator;
import com.kupi.cc.auth.domain.entity.MerchantBankSearchIndex;
import com.kupi.cc.auth.domain.entity.Role;
import com.kupi.cc.auth.domain.entity.User;
import com.kupi.cc.auth.domain.entity.UserMobileNo;
import com.kupi.cc.auth.domain.request.*;
import com.kupi.cc.auth.domain.response.BasicUserResponseDto;
import com.kupi.cc.auth.domain.response.UpdatedMobileNoListResponseDto;
import com.kupi.cc.auth.domain.response.UpdatedMobileNoResponseDto;
import com.kupi.cc.auth.enums.*;
import com.kupi.cc.auth.exception.*;
import com.kupi.cc.auth.service.UserRoleService;
import com.kupi.cc.auth.service.UserService;
import com.kupi.cc.auth.util.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;

import java.util.*;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests the {@link UserController} class.
 */
class UserControllerTest {

    public static final String USER_ID_HEADER = "User-Id";
    private static final int PAGE = 0;
    private static final int SIZE = 250;
    private static final String SEARCH_TERM = "ALL";
    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String USER_ID = "uid-d9d069f9-2b07-446f-8117-e78ff14d01d8";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String USER_NAME = "Rendy Deraman";
    private static final String FULL_NAME = "John Dore";
    private static final String USER_EMAIL = "rengy@tokoin.io";
    private static final String USER_MOBILE_NO = "+62-81123123";
    private static final String COUNTRY_CODE = "+62";
    private static final String LOCAL_NUMBER = "81123123";
    private static final String USER_PASS = "Tokoin@123";
    private static final String USER_LANGUAGE = "ENGLISH";
    private static final String UPDATE_USER_URI = "/api/v1/users";
    private static final String GET_USER_BY_ID_URI = "/api/v1/users";
    private static final String GET_USER_BY_MOBILE_URI = "/api/v1/users/mobileNo/{mobileNo}";
    private static final String UPDATE_USER_MOBILE_NO_URI = "/api/v1/users/mobileNo";
    private static final String CREATE_UNREGISTERED_USER_URI = "/api/v1/users/mobileNo";
    private static final String UPDATE_USER_EMAIL_URI = "/api/v1/users/email";
    private static final String UPDATE_USER_PASSWORD_URI = "/api/v1/users/password";
    private static final String RESET_USER_PASSWORD_URI = "/api/v1/users/password/reset";
    private static final String GET_UPDATED_NUMBERS_URI = "/api/v1/users/mobile-no/updates/0/250";
    private static final String GET_BULK_USERS_URI = "/api/v1/users/bulk-info";
    private static final String DELETE_USER_URI = "/api/v1/users";
    private static final String CREATE_USER_URI_TYPE = "/api/v1/users/{type}";
    private static final String GET_RESET_TOKEN_FOR_FORGET_PASSWORD_URI = "/api/v1/users/forgot-password";
    private static final String GENERATE_API_KEY_FOR_MERCHANT_URI = "/api/v1/users/MERCHANT/{merchantId}/generate-api-access-key";
    private static final String GET_ALL_USERS_URI = "/api/v1/users/{type}/{page}/{size}/search/{searchTerm}";
    private static final String REGISTER_GUEST_USER_URI = "/api/v1/users/register/GUEST";
    private static final String GET_API_KEY_URI = "/api/v1/users/get-api-access-key/MERCHANT/{merchantId}";
    private static final RoleType CREATE_USER_URI_TYPE_VALID = RoleType.USER;
    private static final RoleType CREATE_USER_URI_TYPE_INVALID = RoleType.ADMIN;
    private static final String IMAGE_URL = "https://tokobook.s3.ap-southeast-1.amazonaws.com/test";
    private static final String APPLICATION_JSON_UTF_8 = "application/json;charset=UTF-8";
    private static final String APPLICATION_JSON = "application/json";
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String FAILED = "failed";
    private static final String BASIC_TOKEN = "changeme-auth-basic-token";

    private final UserRequestDto userRequestDto = getSampleUserRequestDto();
    private final UnregisterUserDto unregisterUserDto = getSampleUnregisteredUserDto();
    private final UserEmailUpdateRequestDto userEmailUpdateRequestDto = getSampleEmailUpdateRequestDto();
    private final UserMobileUpdateRequestDto userMobileUpdateRequestDto = getSampleMobileUpdateRequestDto();
    private final UserPasswordUpdateRequestDto userPasswordUpdateRequestDto = getSamplePasswordUpdateRequestDto();
    private final ResetPasswordRequestDto resetPasswordRequestDto = getSampleResetPasswordRequestDto();
    private final UserUpdateRequestDto userUpdateRequestDto = getSampleUserUpdateRequestDto();
    private final User user = getSampleUser();
    private final ResourceBundleMessageSourceBean resourceBundleMessageSourceBean = new ResourceBundleMessageSourceBean();
    Locale locale = LocaleContextHolder.getLocale();
    @Mock
    private UserService userService;
    @Mock
    private Validator validator;

    private MockMvc mockMvc;

    @Mock
    private DefaultTokenServices defaultTokenServices;
    @Mock
    private UserRoleService userRoleService;
    @Mock
    private Translator translator;
    private ResourceBundleMessageSource resourceBundleMessageSource;

    @BeforeEach
    void setUp() {
        initMocks(this);
        when(validator.isValidPassword(USER_PASS)).thenReturn(true);
        when(validator.isValidMobileNoWithCountryCode(USER_MOBILE_NO)).thenReturn(true);
        when(validator.isValidEmail(USER_EMAIL)).thenReturn(true);
        UserController userController = new UserController(userService, validator, defaultTokenServices, translator,
                userRoleService);

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

        // Create MockMvc with no-op validator to satisfy Spring framework requirements
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setValidator(noOpValidator)
                .build();

        // Mock defaultTokenServices for revokeToken calls to avoid OAuth2Authentication casting issues
        when(defaultTokenServices.getAccessToken(any())).thenReturn(null);

        // Spring Security 6.x: Use UsernamePasswordAuthenticationToken instead of deprecated OAuth2Authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser",
                "password",
                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        this.resourceBundleMessageSource = resourceBundleMessageSourceBean.messageSource();
    }

    /**
     * Start of tests for create user
     * Api context: /api/v1/users/{type}
     */
    @Test
    void Should_ReturnOk_When_CreatingFreshUserWithoutProvidingEmail() throws Exception {
        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType.CREATE_USER.getCode(),
                null, locale);
        when(translator.toLocale(SuccessResponseStatusType.CREATE_USER.getCode()))
                .thenReturn(successMessage);
        String mobileNo = userRequestDto.getMobileNo().getNo();
        userRequestDto.setEmail(null);
        userRequestDto.setLanguage("ENGLISH");
        when(validator.isValidMobileNoWithCountryCode(mobileNo)).thenReturn(true);
        when(validator.isValidPassword(userRequestDto.getPassword())).thenReturn(true);
        when(userService.isAlreadyRegisteredUser(USER_MOBILE_NO)).thenReturn(false);
        String uri = CREATE_USER_URI_TYPE.replace("{type}", CREATE_USER_URI_TYPE_VALID.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(userRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(SUCCESS_STATUS)))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.CREATE_USER.getMessage()))
                .andExpect(jsonPath("$.data.userId", startsWith("uid-")))
                .andExpect(jsonPath("$.data.language").value("ENGLISH"))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnOk_When_CreatingUserIsSuccessfulForUnregisteredUser() throws Exception {
        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType.CREATE_USER.getCode(),
                null, locale);
        when(translator.toLocale(SuccessResponseStatusType.CREATE_USER.getCode()))
                .thenReturn(successMessage);
        String mobileNo = userRequestDto.getMobileNo().getNo();
        userRequestDto.setEmail(null);
        userRequestDto.setLanguage("ENGLISH");
        when(validator.isValidMobileNoWithCountryCode(mobileNo)).thenReturn(true);
        when(validator.isValidPassword(userRequestDto.getPassword())).thenReturn(true);
        when(userService.isAlreadyRegisteredUser(USER_MOBILE_NO)).thenReturn(false);
        when(userService.isEmailExist(null)).thenThrow(new AuthServiceException("failed"));
        when(userService.isMobileNoExist(USER_MOBILE_NO)).thenReturn(true);
        User user = new User();
        user.setId(USER_ID);
        user.setCreatedAt(new Date());
        when(userService.getUserByMobileNo(mobileNo)).thenReturn(user);
        String uri = CREATE_USER_URI_TYPE.replace("{type}", CREATE_USER_URI_TYPE_VALID.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(userRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(SUCCESS_STATUS)))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.CREATE_USER.getMessage()))
                .andExpect(jsonPath("$.data.userId", startsWith("uid-")))
                .andExpect(jsonPath("$.data.language").value("ENGLISH"))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingUserForUnsupportedUserType() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                        .UNSUPPORTED_USER_TYPE.getCodeString(ErrorResponseStatusType
                                .UNSUPPORTED_USER_TYPE.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE
                .getCodeString(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE.getCode())))
                .thenReturn(invalidMessage);
        String uri = CREATE_USER_URI_TYPE.replace("{type}", CREATE_USER_URI_TYPE_INVALID.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(userRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingUserForExistingEmailForUnregisteredUser() throws Exception {
        when(userService.isAlreadyRegisteredUser(userRequestDto.getMobileNo().getNo())).thenReturn(false);
        when(userService.isEmailExist(userRequestDto.getEmail())).thenReturn(true);
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                        .EXISTING_EMAIL.getCodeString(ErrorResponseStatusType
                                .EXISTING_EMAIL.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.EXISTING_EMAIL
                .getCodeString(ErrorResponseStatusType.EXISTING_EMAIL.getCode())))
                .thenReturn(invalidMessage);
        String uri = CREATE_USER_URI_TYPE.replace("{type}", CREATE_USER_URI_TYPE_VALID.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(userRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.EXISTING_EMAIL
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.EXISTING_EMAIL
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingUserForExistingMobileNo() throws Exception {
        when(userService.isAlreadyRegisteredUser(userRequestDto.getMobileNo().getNo())).thenReturn(true);
        when(userService.isMobileNoExist(userRequestDto.getMobileNo().getNo())).thenReturn(true);
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                        .EXISTING_MOBILE_NO.getCodeString(ErrorResponseStatusType
                                .EXISTING_MOBILE_NO.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.EXISTING_MOBILE_NO
                .getCodeString(ErrorResponseStatusType.EXISTING_MOBILE_NO.getCode())))
                .thenReturn(invalidMessage);
        String uri = CREATE_USER_URI_TYPE.replace("{type}", CREATE_USER_URI_TYPE_VALID.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(userRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.EXISTING_MOBILE_NO
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.EXISTING_MOBILE_NO
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingUserForExistingEmail() throws Exception {
        when(userService.isAlreadyRegisteredUser(userRequestDto.getMobileNo().getNo())).thenReturn(true);
        when(userService.isEmailExist(userRequestDto.getEmail())).thenReturn(true);
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                        .EXISTING_EMAIL.getCodeString(ErrorResponseStatusType
                                .EXISTING_EMAIL.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.EXISTING_EMAIL
                .getCodeString(ErrorResponseStatusType.EXISTING_EMAIL.getCode())))
                .thenReturn(invalidMessage);
        String uri = CREATE_USER_URI_TYPE.replace("{type}", CREATE_USER_URI_TYPE_VALID.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(userRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.EXISTING_EMAIL
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.EXISTING_EMAIL
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingUserForExistingFieldsWhereMobileNoAndEmailNotExists()
            throws Exception {
        when(userService.isAlreadyRegisteredUser(userRequestDto.getMobileNo().getNo())).thenReturn(true);
        when(userService.isMobileNoExist(userRequestDto.getMobileNo().getNo())).thenReturn(false);
        when(userService.isEmailExist(userRequestDto.getEmail())).thenReturn(false);
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                        .INTERNAL_SERVER_ERROR.getCodeString(ErrorResponseStatusType
                                .INTERNAL_SERVER_ERROR.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                .getCodeString(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = CREATE_USER_URI_TYPE.replace("{type}", CREATE_USER_URI_TYPE_VALID.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(userRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingUserForInvalidMobileNo() throws Exception {
        when(validator.isValidMobileNoWithCountryCode(userRequestDto.getMobileNo().getNo())).thenReturn(false);
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                        .INVALID_MOBILE_NO.getCodeString(ErrorResponseStatusType
                                .INVALID_MOBILE_NO.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_MOBILE_NO
                .getCodeString(ErrorResponseStatusType.INVALID_MOBILE_NO.getCode())))
                .thenReturn(invalidMessage);
        String uri = CREATE_USER_URI_TYPE.replace("{type}", CREATE_USER_URI_TYPE_VALID.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(userRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_MOBILE_NO
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_MOBILE_NO
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingUserForInvalidEmail() throws Exception {
        when(validator.isValidEmail(userRequestDto.getEmail())).thenReturn(false);
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                        .INVALID_EMAIL.getCodeString(ErrorResponseStatusType
                                .INVALID_EMAIL.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_EMAIL
                .getCodeString(ErrorResponseStatusType.INVALID_EMAIL.getCode())))
                .thenReturn(invalidMessage);
        String uri = CREATE_USER_URI_TYPE.replace("{type}", CREATE_USER_URI_TYPE_VALID.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(userRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_EMAIL
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_EMAIL
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingUserForInvalidPassword() throws Exception {
        when(validator.isValidPassword(userRequestDto.getPassword())).thenReturn(false);
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                        .INVALID_PASSWORD.getCodeString(ErrorResponseStatusType
                                .INVALID_PASSWORD.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_PASSWORD
                .getCodeString(ErrorResponseStatusType.INVALID_PASSWORD.getCode())))
                .thenReturn(invalidMessage);
        String uri = CREATE_USER_URI_TYPE.replace("{type}", CREATE_USER_URI_TYPE_VALID.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(userRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_PASSWORD
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_PASSWORD
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingUserForInvalidLanguage() throws Exception {
        userRequestDto.setLanguage(null);
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                        .INVALID_LANGUAGE.getCodeString(ErrorResponseStatusType
                                .INVALID_LANGUAGE.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_LANGUAGE
                .getCodeString(ErrorResponseStatusType.INVALID_LANGUAGE.getCode())))
                .thenReturn(invalidMessage);
        String uri = CREATE_USER_URI_TYPE.replace("{type}", CREATE_USER_URI_TYPE_VALID.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(userRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_LANGUAGE
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_LANGUAGE
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingUserWithoutRequiredFields() throws Exception {
        userRequestDto.setFullName(null);
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                        .MISSING_REQUIRED_FIELDS.getCodeString(ErrorResponseStatusType
                                .MISSING_REQUIRED_FIELDS.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                .getCodeString(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode())))
                .thenReturn(invalidMessage);
        String uri = CREATE_USER_URI_TYPE.replace("{type}", CREATE_USER_URI_TYPE_VALID.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(userRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_CreatingUserWasFailed() throws Exception {
        when(userService.isAlreadyRegisteredUser(userRequestDto.getMobileNo().getNo())).thenReturn(false);
        doThrow(new AuthServiceException(FAILED)).when(userService).createUser(any(User.class));
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = CREATE_USER_URI_TYPE.replace("{type}", CREATE_USER_URI_TYPE_VALID.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(userRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for update user
     * Api context: /api/v1/users
     */
    @Test
    void Should_ReturnOk_When_UpdatingUserForValidUser() throws Exception {
        userUpdateRequestDto.setUserId(USER_ID);
        userUpdateRequestDto.setImageUrl(IMAGE_URL);
        when(validator.isValidUrl(userUpdateRequestDto.getImageUrl())).thenReturn(true);
        user.setRole(new Role(RoleType.USER));
        user.setImageUrl(IMAGE_URL);
        when(userService.updateUser(any(UserUpdateRequestDto.class))).thenReturn(user);
        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType.UPDATE_USER.getCode(),
                null, locale);
        when(translator.toLocale(SuccessResponseStatusType.UPDATE_USER.getCode()))
                .thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.patch(UPDATE_USER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(userUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(SUCCESS_STATUS)))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.UPDATE_USER.getMessage()))
                .andExpect(jsonPath("$.data.fullName").value(FULL_NAME))
                .andExpect(jsonPath("$.data.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.data.imageUrl").value(IMAGE_URL))
                .andExpect(jsonPath("$.data.language").value(USER_LANGUAGE))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingUserForInvalidImageUrl() throws Exception {
        userUpdateRequestDto.setUserId(USER_ID);
        userUpdateRequestDto.setImageUrl(IMAGE_URL);
        when(validator.isValidUrl(userUpdateRequestDto.getImageUrl())).thenReturn(false);
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                        .INVALID_IMAGE_URL.getCodeString(ErrorResponseStatusType
                                .INVALID_IMAGE_URL.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_IMAGE_URL
                .getCodeString(ErrorResponseStatusType.INVALID_IMAGE_URL.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.patch(UPDATE_USER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(userUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_IMAGE_URL
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_IMAGE_URL
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingUserForInvalidUserId() throws Exception {
        userUpdateRequestDto.setUserId(USER_ID);
        userUpdateRequestDto.setImageUrl(IMAGE_URL);
        when(validator.isValidUrl(userUpdateRequestDto.getImageUrl())).thenReturn(true);
        when(userService.updateUser(any(UserUpdateRequestDto.class))).thenThrow(new InvalidUserException(FAILED));
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                        .INVALID_USER_ID.getCodeString(ErrorResponseStatusType
                                .INVALID_USER_ID.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_USER_ID
                .getCodeString(ErrorResponseStatusType.INVALID_USER_ID.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.patch(UPDATE_USER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(userUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_ID
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_ID
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingUserForMissingRequiredFields() throws Exception {
        userUpdateRequestDto.setUserId("");
        userUpdateRequestDto.setImageUrl(IMAGE_URL);
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                        .MISSING_REQUIRED_FIELDS.getCodeString(ErrorResponseStatusType
                                .MISSING_REQUIRED_FIELDS.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                .getCodeString(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.patch(UPDATE_USER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(userUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_UpdatingUserIsFailed() throws Exception {
        userUpdateRequestDto.setUserId(USER_ID);
        userUpdateRequestDto.setImageUrl(IMAGE_URL);
        when(validator.isValidUrl(userUpdateRequestDto.getImageUrl())).thenReturn(true);
        doThrow(new AuthServiceException(FAILED)).when(userService).updateUser(any(UserUpdateRequestDto.class));
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.patch(UPDATE_USER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(userUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for delete user
     * Api context: /api/v1/users
     */
    @Test
    void Should_ReturnOk_When_DeletingUserIsSuccessful() throws Exception {
        doNothing().when(userService).deleteUser(USER_ID);
        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType.USER_DELETED.getCode(),
                null, locale);
        when(translator.toLocale(SuccessResponseStatusType.USER_DELETED.getCode()))
                .thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.delete(DELETE_USER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(SUCCESS_STATUS)))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.USER_DELETED.getMessage()))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_DeletingUserForInvalidUser() throws Exception {
        doThrow(new InvalidUserException(FAILED)).when(userService).deleteUser(USER_ID);
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType
                        .INVALID_USER_ID.getCodeString(ErrorResponseStatusType
                                .INVALID_USER_ID.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_USER_ID
                .getCodeString(ErrorResponseStatusType.INVALID_USER_ID.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.delete(DELETE_USER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_DeletingUserIsFailed() throws Exception {
        doThrow(new AuthServiceException(FAILED)).when(userService).deleteUser(USER_ID);
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.delete(DELETE_USER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for get user by user id
     * Api context: /api/v1/users
     */
    @Test
    void Should_ReturnOk_When_GettingUserByUserIdIsSuccessful() throws Exception {
        user.setRole(new Role(RoleType.USER));
        user.setImageUrl(IMAGE_URL);
        when(userService.getUserByUserId(USER_ID)).thenReturn(user);
        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType.GET_USER.getCode(),
                null, locale);
        when(translator.toLocale(SuccessResponseStatusType.GET_USER.getCode()))
                .thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.get(GET_USER_BY_ID_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(SUCCESS_STATUS)))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.GET_USER.getMessage()))
                .andExpect(jsonPath("$.data.fullName").value(FULL_NAME))
                .andExpect(jsonPath("$.data.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.data.imageUrl").value(IMAGE_URL))
                .andExpect(jsonPath("$.data.language").value(USER_LANGUAGE))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingUserByUserIdForInvalidUser() throws Exception {
        when(userService.getUserByUserId(USER_ID)).thenThrow(new InvalidUserException(FAILED));
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.INVALID_USER_ID
                        .getCodeString(ErrorResponseStatusType.INVALID_USER_ID.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_USER_ID.getCodeString(ErrorResponseStatusType
                .INVALID_USER_ID.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.get(GET_USER_BY_ID_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingUserByUserIdIsFailed() throws Exception {
        when(userService.getUserByUserId(USER_ID)).thenThrow(new AuthServiceException(FAILED));
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.get(GET_USER_BY_ID_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for get user by mobile no
     * Api context: /api/v1/users/mobileNo/{mobileNo}
     */
    @Test
    void Should_ReturnOk_When_GettingUserByMobileNoIsSuccessful() throws Exception {
        when(validator.isValidMobileNoWithCountryCode(USER_MOBILE_NO)).thenReturn(true);
        user.setImageUrl(IMAGE_URL);
        when(userService.getUserByMobileNo(USER_MOBILE_NO)).thenReturn(user);
        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType.GET_USER.getCode(),
                null, locale);
        when(translator.toLocale(SuccessResponseStatusType.GET_USER.getCode()))
                .thenReturn(successMessage);
        String uri = GET_USER_BY_MOBILE_URI.replace("{mobileNo}", USER_MOBILE_NO);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(SUCCESS_STATUS)))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.GET_USER.getMessage()))
                .andExpect(jsonPath("$.data.userId").value(USER_ID))
                .andExpect(jsonPath("$.data.fullName").value(FULL_NAME))
                .andExpect(jsonPath("$.data.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.data.imageUrl").value(IMAGE_URL))
                .andExpect(jsonPath("$.data.language").value(USER_LANGUAGE))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnNoContent_When_GettingUserByMobileNoForInvalidUser() throws Exception {
        when(userService.getUserByMobileNo(USER_MOBILE_NO)).thenThrow(new InvalidUserException(FAILED));
        String uri = GET_USER_BY_MOBILE_URI.replace("{mobileNo}", USER_MOBILE_NO);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void Should_ReturnBadRequest_When_GettingUserByMobileNoForInvalidMobileNo() throws Exception {
        when(validator.isValidMobileNoWithCountryCode(USER_MOBILE_NO)).thenReturn(false);
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.INVALID_MOBILE_NO
                        .getCodeString(ErrorResponseStatusType.INVALID_MOBILE_NO.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_MOBILE_NO.getCodeString(ErrorResponseStatusType
                .INVALID_MOBILE_NO.getCode())))
                .thenReturn(invalidMessage);
        String uri = GET_USER_BY_MOBILE_URI.replace("{mobileNo}", USER_MOBILE_NO);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_MOBILE_NO.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_MOBILE_NO.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingUserByMobileNoIsFailed() throws Exception {
        when(validator.isValidMobileNoWithCountryCode(USER_MOBILE_NO)).thenReturn(true);
        when(userService.getUserByMobileNo(USER_MOBILE_NO)).thenThrow(new AuthServiceException(FAILED));
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = GET_USER_BY_MOBILE_URI.replace("{mobileNo}", USER_MOBILE_NO);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for create unregistered user
     * Api context: /api/v1/users/mobileNo
     */
    @Test
    void Should_ReturnOk_When_CreatingUnregisteredUserIsSuccessful() throws Exception {
        when(validator.isValidMobileNoWithCountryCode(unregisterUserDto.getMobileNo().getNo())).thenReturn(true);
        doNothing().when(userService).createUnregisteredUser(user);
        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType.CREATE_USER.getCode(),
                null, locale);
        when(translator.toLocale(SuccessResponseStatusType.CREATE_USER.getCode()))
                .thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_UNREGISTERED_USER_URI)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(unregisterUserDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(SUCCESS_STATUS)))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.CREATE_USER.getMessage()))
                .andExpect(jsonPath("$.data.mobileNo.localNumber").value(LOCAL_NUMBER))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingUnregisteredUserForAlreadyExistingMobileNo() throws Exception {
        when(validator.isValidMobileNoWithCountryCode(unregisterUserDto.getMobileNo().getNo())).thenReturn(true);
        doThrow(new MobileNoAlreadyExistsException(FAILED)).when(userService).createUnregisteredUser(any(User.class));
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.EXISTING_MOBILE_NO
                        .getCodeString(ErrorResponseStatusType.EXISTING_MOBILE_NO.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.EXISTING_MOBILE_NO.getCodeString(ErrorResponseStatusType
                .EXISTING_MOBILE_NO.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_UNREGISTERED_USER_URI)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(unregisterUserDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.EXISTING_MOBILE_NO.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.EXISTING_MOBILE_NO.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingUnregisteredUserForInvalidMobileNoWithCountryCode() throws Exception {
        when(validator.isValidMobileNoWithCountryCode(unregisterUserDto.getMobileNo().getNo())).thenReturn(false);
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.INVALID_MOBILE_NO
                        .getCodeString(ErrorResponseStatusType.INVALID_MOBILE_NO.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_MOBILE_NO.getCodeString(ErrorResponseStatusType
                .INVALID_MOBILE_NO.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_UNREGISTERED_USER_URI)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(unregisterUserDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_MOBILE_NO.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_MOBILE_NO.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_CreatingUnregisteredUserForMissingRequiredFields() throws Exception {
        unregisterUserDto.setMobileNo(null);
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getCodeString(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCodeString(ErrorResponseStatusType
                .MISSING_REQUIRED_FIELDS.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_UNREGISTERED_USER_URI)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(unregisterUserDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_CreatingUnregisteredUserIsFailed() throws Exception {
        when(validator.isValidMobileNoWithCountryCode(unregisterUserDto.getMobileNo().getNo())).thenReturn(true);
        doThrow(new AuthServiceException(FAILED)).when(userService).createUnregisteredUser(any(User.class));
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_UNREGISTERED_USER_URI)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(unregisterUserDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for update user email
     * Api context: /api/v1/users/email
     */
    @Test
    void Should_ReturnOk_When_UpdatingUserEmailIsSuccessful() throws Exception {
        userEmailUpdateRequestDto.setUserId(USER_ID);
        when(validator.isValidEmail(USER_EMAIL)).thenReturn(true);
        user.setRole(new Role(RoleType.USER));
        when(userService.isFirstTimeUserEmail(userEmailUpdateRequestDto.getUserId())).thenReturn(true);
        when(userService.updateEmail(any(UserEmailUpdateRequestDto.class))).thenReturn(user);
        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType.UPDATE_EMAIL.getCode(),
                null, locale);
        when(translator.toLocale(SuccessResponseStatusType.UPDATE_EMAIL.getCode()))
                .thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_USER_EMAIL_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(userEmailUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(SUCCESS_STATUS)))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.UPDATE_EMAIL.getMessage()))
                .andExpect(jsonPath("$.data.fullName").value(FULL_NAME))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingUserEmailForInvalidUser() throws Exception {
        userEmailUpdateRequestDto.setUserId(USER_ID);
        when(validator.isValidEmail(USER_EMAIL)).thenReturn(true);
        when(userService.isFirstTimeUserEmail(userEmailUpdateRequestDto.getUserId())).thenReturn(false);
        when(userService.updateEmail(any(UserEmailUpdateRequestDto.class))).thenThrow(new InvalidUserException(FAILED));
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.INVALID_USER_ID
                        .getCodeString(ErrorResponseStatusType.INVALID_USER_ID.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_USER_ID.getCodeString(ErrorResponseStatusType
                .INVALID_USER_ID.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_USER_EMAIL_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(userEmailUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingUserEmailForEmailAlreadyExists() throws Exception {
        userEmailUpdateRequestDto.setUserId(USER_ID);
        when(validator.isValidEmail(USER_EMAIL)).thenReturn(true);
        when(userService.isFirstTimeUserEmail(userEmailUpdateRequestDto.getUserId())).thenReturn(false);
        when(userService.updateEmail(any(UserEmailUpdateRequestDto.class))).thenThrow(new EmailAlreadyExistsException(FAILED));
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.EXISTING_EMAIL
                        .getCodeString(ErrorResponseStatusType.EXISTING_EMAIL.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.EXISTING_EMAIL.getCodeString(ErrorResponseStatusType
                .EXISTING_EMAIL.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_USER_EMAIL_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(userEmailUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.EXISTING_EMAIL.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.EXISTING_EMAIL.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingUserEmailForInvalidEmail() throws Exception {
        userEmailUpdateRequestDto.setUserId(USER_ID);
        when(validator.isValidEmail(USER_EMAIL)).thenReturn(false);
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.INVALID_EMAIL
                        .getCodeString(ErrorResponseStatusType.INVALID_EMAIL.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_EMAIL.getCodeString(ErrorResponseStatusType
                .INVALID_EMAIL.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_USER_EMAIL_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(userEmailUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_EMAIL.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_EMAIL.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingUserEmailForMissingRequiredFields() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getCodeString(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCodeString(ErrorResponseStatusType
                .MISSING_REQUIRED_FIELDS.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_USER_EMAIL_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(userEmailUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_UpdatingUserEmailIsFailed() throws Exception {
        userEmailUpdateRequestDto.setUserId(USER_ID);
        when(validator.isValidEmail(USER_EMAIL)).thenReturn(true);
        when(userService.isFirstTimeUserEmail(userEmailUpdateRequestDto.getUserId())).thenReturn(false);
        when(userService.updateEmail(any(UserEmailUpdateRequestDto.class))).thenThrow(new AuthServiceException(FAILED));
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_USER_EMAIL_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(userEmailUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for update mobile no
     * Api context: /api/v1/users/mobileNo
     */
    @Test
    void Should_ReturnOk_When_UpdatingMobileNoIsSuccessful() throws Exception {
        user.setRole(new Role(RoleType.USER));
        userMobileUpdateRequestDto.setUserId(USER_ID);
        when(validator.isValidMobileNoWithCountryCode(userMobileUpdateRequestDto.getMobileNo().getNo()))
                .thenReturn(true);
        when(userService.updateMobileNo(any(UserMobileUpdateRequestDto.class))).thenReturn(user);
        UserMobileNo userMobileNo = new UserMobileNo(userMobileUpdateRequestDto.getMobileNo().getNo(),
                userMobileUpdateRequestDto.getUserId());
        doNothing().when(userService).addUpdatedNumber(userMobileNo);

        // DefaultTokenServices mock no longer needed - controller tests verify HTTP contract, not token internals
        when(defaultTokenServices.revokeToken(any(String.class))).thenReturn(true);

        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType.UPDATE_MOBILE_NO.getCode(),
                null, locale);
        when(translator.toLocale(SuccessResponseStatusType.UPDATE_MOBILE_NO.getCode()))
                .thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_USER_MOBILE_NO_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(userMobileUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(SUCCESS_STATUS)))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.UPDATE_MOBILE_NO.getMessage()))
                .andExpect(jsonPath("$.data.fullName").value(FULL_NAME))
                .andExpect(jsonPath("$.data.userType").value(RoleType.USER.toString()))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingMobileNoForInvalidUser() throws Exception {
        userMobileUpdateRequestDto.setUserId(USER_ID);
        when(validator.isValidMobileNoWithCountryCode(userMobileUpdateRequestDto.getMobileNo().getNo()))
                .thenReturn(true);
        when(userService.updateMobileNo(any(UserMobileUpdateRequestDto.class)))
                .thenThrow(new InvalidUserException(FAILED));
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.INVALID_USER_ID
                        .getCodeString(ErrorResponseStatusType.INVALID_USER_ID.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_USER_ID.getCodeString(ErrorResponseStatusType
                .INVALID_USER_ID.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_USER_MOBILE_NO_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(userMobileUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingMobileNoForExistingMobileNo() throws Exception {
        userMobileUpdateRequestDto.setUserId(USER_ID);
        when(validator.isValidMobileNoWithCountryCode(userMobileUpdateRequestDto.getMobileNo().getNo()))
                .thenReturn(true);
        when(userService.updateMobileNo(any(UserMobileUpdateRequestDto.class)))
                .thenThrow(new MobileNoAlreadyExistsException(FAILED));
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.EXISTING_MOBILE_NO
                        .getCodeString(ErrorResponseStatusType.EXISTING_MOBILE_NO.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.EXISTING_MOBILE_NO.getCodeString(ErrorResponseStatusType
                .EXISTING_MOBILE_NO.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_USER_MOBILE_NO_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(userMobileUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.EXISTING_MOBILE_NO.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.EXISTING_MOBILE_NO.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingMobileNoForInvalidMobileNo() throws Exception {
        userMobileUpdateRequestDto.setUserId(USER_ID);
        when(validator.isValidMobileNoWithCountryCode(userMobileUpdateRequestDto.getMobileNo().getNo()))
                .thenReturn(false);
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.INVALID_MOBILE_NO
                        .getCodeString(ErrorResponseStatusType.INVALID_MOBILE_NO.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_MOBILE_NO.getCodeString(ErrorResponseStatusType
                .INVALID_MOBILE_NO.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_USER_MOBILE_NO_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(userMobileUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_MOBILE_NO.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_MOBILE_NO.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingMobileNoForMissingRequiredFields() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getCodeString(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCodeString(ErrorResponseStatusType
                .MISSING_REQUIRED_FIELDS.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_USER_MOBILE_NO_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(userMobileUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_UpdatingMobileNoIsFailed() throws Exception {
        userMobileUpdateRequestDto.setUserId(USER_ID);
        when(validator.isValidMobileNoWithCountryCode(userMobileUpdateRequestDto.getMobileNo().getNo()))
                .thenReturn(true);
        when(userService.updateMobileNo(any(UserMobileUpdateRequestDto.class)))
                .thenThrow(new AuthServiceException(FAILED));
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_USER_MOBILE_NO_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(userMobileUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for update password
     * Api context: /api/v1/users/password
     */
    @Test
    void Should_ReturnOk_When_UpdatingPasswordIsSuccessful() throws Exception {
        userPasswordUpdateRequestDto.setUserId(USER_ID);
        when(validator.isValidPassword(userPasswordUpdateRequestDto.getNewPassword())).thenReturn(true);
        doNothing().when(userService).updatePassword(userPasswordUpdateRequestDto);

        // DefaultTokenServices mock no longer needed - controller tests verify HTTP contract, not token internals
        when(defaultTokenServices.revokeToken(any(String.class))).thenReturn(true);

        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType.UPDATE_PASSWORD.getCode(),
                null, locale);
        when(translator.toLocale(SuccessResponseStatusType.UPDATE_PASSWORD.getCode()))
                .thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_USER_PASSWORD_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(userPasswordUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(SUCCESS_STATUS)))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.UPDATE_PASSWORD.getMessage()))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingPasswordForInvalidUser() throws Exception {
        userPasswordUpdateRequestDto.setUserId(USER_ID);
        when(validator.isValidPassword(userPasswordUpdateRequestDto.getNewPassword())).thenReturn(true);
        doThrow(new InvalidUserException(FAILED)).when(userService)
                .updatePassword(any(UserPasswordUpdateRequestDto.class));

        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.INVALID_USER_ID
                        .getCodeString(ErrorResponseStatusType.INVALID_USER_ID.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_USER_ID.getCodeString(ErrorResponseStatusType
                .INVALID_USER_ID.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_USER_PASSWORD_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(userPasswordUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_ID.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_ID.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingPasswordForOldPasswordNotMatch() throws Exception {
        userPasswordUpdateRequestDto.setUserId(USER_ID);
        when(validator.isValidPassword(userPasswordUpdateRequestDto.getNewPassword())).thenReturn(true);
        doThrow(new InvalidPasswordException("Updating password was failed")).when(userService)
                .updatePassword(any(UserPasswordUpdateRequestDto.class));

        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.OLD_PASSWORD_DOES_NOT_MATCH
                        .getCodeString(ErrorResponseStatusType.OLD_PASSWORD_DOES_NOT_MATCH.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.OLD_PASSWORD_DOES_NOT_MATCH.getCodeString(ErrorResponseStatusType
                .OLD_PASSWORD_DOES_NOT_MATCH.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_USER_PASSWORD_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(userPasswordUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.OLD_PASSWORD_DOES_NOT_MATCH.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.OLD_PASSWORD_DOES_NOT_MATCH.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdateTheCurrentPasswordWithOldPassword() throws Exception {
        userPasswordUpdateRequestDto.setUserId(USER_ID);
        when(validator.isValidPassword(userPasswordUpdateRequestDto.getNewPassword())).thenReturn(true);
        doThrow(new InvalidPasswordException("The new password must be different from your current password."))
                .when(userService).updatePassword(any(UserPasswordUpdateRequestDto.class));
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.NEW_PASSWORD_EXIST
                        .getCodeString(ErrorResponseStatusType.NEW_PASSWORD_EXIST.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.NEW_PASSWORD_EXIST.getCodeString(ErrorResponseStatusType
                .NEW_PASSWORD_EXIST.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_USER_PASSWORD_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(userPasswordUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.NEW_PASSWORD_EXIST.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.NEW_PASSWORD_EXIST.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingPasswordForInvalidPassword() throws Exception {
        userPasswordUpdateRequestDto.setUserId(USER_ID);
        when(validator.isValidPassword(userPasswordUpdateRequestDto.getNewPassword())).thenReturn(false);

        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.INVALID_PASSWORD
                        .getCodeString(ErrorResponseStatusType.INVALID_PASSWORD.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_PASSWORD.getCodeString(ErrorResponseStatusType
                .INVALID_PASSWORD.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_USER_PASSWORD_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(userPasswordUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_PASSWORD.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_PASSWORD.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UpdatingPasswordForMissingRequiredFields() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getCodeString(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCodeString(ErrorResponseStatusType
                .MISSING_REQUIRED_FIELDS.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_USER_PASSWORD_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(userPasswordUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_RevokeTokenIsFailedForUpdatePassword() throws Exception {
        userPasswordUpdateRequestDto.setUserId(USER_ID);
        when(validator.isValidPassword(userPasswordUpdateRequestDto.getNewPassword())).thenReturn(true);
        doNothing().when(userService).updatePassword(userPasswordUpdateRequestDto);

        // With Spring Security 6.x, token revocation is skipped for non-OAuth2 authentication
        // so the request succeeds even if revokeToken would fail
        when(defaultTokenServices.revokeToken(any(String.class))).thenThrow(new DataAccessException("ERROR") {
        });

        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType
                .UPDATE_PASSWORD.getCode(), null, locale);
        when(translator.toLocale(SuccessResponseStatusType.UPDATE_PASSWORD.getCode()))
                .thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_USER_PASSWORD_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(userPasswordUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(SUCCESS_STATUS)))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.UPDATE_PASSWORD.getMessage()));
    }

    @Test
    void Should_ReturnInternalServerError_When_UpdatingPasswordIsFailed() throws Exception {
        userPasswordUpdateRequestDto.setUserId(USER_ID);
        when(validator.isValidPassword(userPasswordUpdateRequestDto.getNewPassword())).thenReturn(true);
        doThrow(new AuthServiceException(FAILED)).when(userService)
                .updatePassword(any(UserPasswordUpdateRequestDto.class));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_USER_PASSWORD_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .content(userPasswordUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for forget password
     * This method tests the method to get otp if user forgets the password
     * Api context: /api/v1/users/forgot-password
     */
    @Test
    void Should_ReturnOk_When_GettingOTPForForgetPasswordIsSuccessful() throws Exception {
        MobileNoRequestDto mobileNo = new MobileNoRequestDto(COUNTRY_CODE, LOCAL_NUMBER);
        ForgotPasswordRequestDto mobileNoRequestDto = new ForgotPasswordRequestDto();
        mobileNoRequestDto.setMobileNo(mobileNo);
        when(validator.isValidMobileNoWithCountryCode(mobileNoRequestDto.getMobileNo().getNo())).thenReturn(true);
        doNothing().when(userService).sendPasswordResetToken(mobileNoRequestDto.getMobileNo());

        String successMessage = resourceBundleMessageSource
                .getMessage(SuccessResponseStatusType.PASSWORD_RESET_TOKEN.getCode(), null, locale);
        when(translator.toLocale(SuccessResponseStatusType.PASSWORD_RESET_TOKEN.getCode()))
                .thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(GET_RESET_TOKEN_FOR_FORGET_PASSWORD_URI)
                        .content(mobileNoRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(SUCCESS_STATUS)))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.PASSWORD_RESET_TOKEN.getMessage()))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingOTPForForgetPasswordForInvalidMobileNo() throws Exception {
        MobileNoRequestDto mobileNo = new MobileNoRequestDto(COUNTRY_CODE, LOCAL_NUMBER);
        ForgotPasswordRequestDto mobileNoRequestDto = new ForgotPasswordRequestDto();
        mobileNoRequestDto.setMobileNo(mobileNo);
        when(validator.isValidMobileNoWithCountryCode(mobileNoRequestDto.getMobileNo().getNo())).thenReturn(false);

        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.INVALID_MOBILE_NO
                        .getCodeString(ErrorResponseStatusType.INVALID_MOBILE_NO.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.INVALID_MOBILE_NO.getCodeString(ErrorResponseStatusType
                .INVALID_MOBILE_NO.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(GET_RESET_TOKEN_FOR_FORGET_PASSWORD_URI)
                        .content(mobileNoRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_MOBILE_NO.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_MOBILE_NO.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingOTPForForgetPasswordForMissingRequiredFields() throws Exception {
        ForgotPasswordRequestDto mobileNoRequestDto = new ForgotPasswordRequestDto();
        mobileNoRequestDto.setMobileNo(null);

        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getCodeString(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCodeString(ErrorResponseStatusType
                .MISSING_REQUIRED_FIELDS.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(GET_RESET_TOKEN_FOR_FORGET_PASSWORD_URI)
                        .content(mobileNoRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingOTPForForgetPasswordForFailedToSendPasswordResetToken() throws Exception {
        MobileNoRequestDto mobileNo = new MobileNoRequestDto(COUNTRY_CODE, LOCAL_NUMBER);
        ForgotPasswordRequestDto mobileNoRequestDto = new ForgotPasswordRequestDto();
        mobileNoRequestDto.setMobileNo(mobileNo);
        when(validator.isValidMobileNoWithCountryCode(mobileNoRequestDto.getMobileNo().getNo())).thenReturn(true);
        doThrow(new SendSmsFailedException(FAILED)).when(userService)
                .sendPasswordResetToken(any(MobileNoRequestDto.class));

        String invalidMessage = resourceBundleMessageSource
                .getMessage(ErrorResponseStatusType.FAILED_TO_SEND_PW_RESET_TOKEN
                                .getCodeString(ErrorResponseStatusType.FAILED_TO_SEND_PW_RESET_TOKEN.getCode()),
                        null, locale);
        when(translator.toLocale(ErrorResponseStatusType.FAILED_TO_SEND_PW_RESET_TOKEN
                .getCodeString(ErrorResponseStatusType.FAILED_TO_SEND_PW_RESET_TOKEN.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(GET_RESET_TOKEN_FOR_FORGET_PASSWORD_URI)
                        .content(mobileNoRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.FAILED_TO_SEND_PW_RESET_TOKEN.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.FAILED_TO_SEND_PW_RESET_TOKEN.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingOTPForForgetPasswordForInvalidUser() throws Exception {
        MobileNoRequestDto mobileNo = new MobileNoRequestDto(COUNTRY_CODE, LOCAL_NUMBER);
        ForgotPasswordRequestDto mobileNoRequestDto = new ForgotPasswordRequestDto();
        mobileNoRequestDto.setMobileNo(mobileNo);
        when(validator.isValidMobileNoWithCountryCode(mobileNoRequestDto.getMobileNo().getNo())).thenReturn(true);
        doThrow(new InvalidUserException(FAILED)).when(userService)
                .sendPasswordResetToken(any(MobileNoRequestDto.class));

        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.NO_USER
                        .getCodeString(ErrorResponseStatusType.NO_USER.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.NO_USER.getCodeString(ErrorResponseStatusType
                .NO_USER.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(GET_RESET_TOKEN_FOR_FORGET_PASSWORD_URI)
                        .content(mobileNoRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.NO_USER.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.NO_USER.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingOTPForForgetPasswordIsFailed() throws Exception {
        MobileNoRequestDto mobileNo = new MobileNoRequestDto(COUNTRY_CODE, LOCAL_NUMBER);
        ForgotPasswordRequestDto mobileNoRequestDto = new ForgotPasswordRequestDto();
        mobileNoRequestDto.setMobileNo(mobileNo);
        when(validator.isValidMobileNoWithCountryCode(mobileNoRequestDto.getMobileNo().getNo())).thenReturn(true);
        doThrow(new AuthServiceException(FAILED)).when(userService)
                .sendPasswordResetToken(any(MobileNoRequestDto.class));

        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(GET_RESET_TOKEN_FOR_FORGET_PASSWORD_URI)
                        .content(mobileNoRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for get bulk users
     * Api context: /api/v1/users/bulk-info
     */
    @Test
    void Should_ReturnOk_When_GettingBulkUsersIsSuccessful() throws Exception {
        BulkUserRequestDto bulkUserRequestDto = new BulkUserRequestDto();
        List<String> userIds = new ArrayList<>();
        userIds.add("uid-1");
        bulkUserRequestDto.setUserIds(userIds);
        user.setApprovalStatus(ApprovalStatus.APPROVED);
        user.setCreatedAt(new Date());
        BasicUserResponseDto basicUserResponseDto = new BasicUserResponseDto(user, TIME_ZONE);
        List<BasicUserResponseDto> userList = new ArrayList<>();
        userList.add(basicUserResponseDto);
        when(userService.getUserList(bulkUserRequestDto.getUserIds(), TIME_ZONE)).thenReturn(userList);
        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType.GET_USERS.getCode(),
                null, locale);
        when(translator.toLocale(SuccessResponseStatusType.GET_USERS.getCode()))
                .thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(GET_BULK_USERS_URI)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(bulkUserRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(SUCCESS_STATUS)))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.GET_USERS.getMessage()))
                .andExpect(jsonPath("$.data.users[0].id").value(basicUserResponseDto.getId()))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingBulkUsersForMaximumUserRequestCount() throws Exception {
        BulkUserRequestDto bulkUserRequestDto = new BulkUserRequestDto();
        List<String> userIds = new ArrayList<>();
        for (int i = 0; i < 251; i++)
            userIds.add("uid-" + i + 1);
        bulkUserRequestDto.setUserIds(userIds);
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.MAX_USER_REQUEST_COUNT
                        .getCodeString(ErrorResponseStatusType.MAX_USER_REQUEST_COUNT.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.MAX_USER_REQUEST_COUNT.getCodeString(ErrorResponseStatusType
                .MAX_USER_REQUEST_COUNT.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(GET_BULK_USERS_URI)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(bulkUserRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MAX_USER_REQUEST_COUNT.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MAX_USER_REQUEST_COUNT.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingBulkUsersForMissingRequiredFields() throws Exception {
        BulkUserRequestDto bulkUserRequestDto = new BulkUserRequestDto();
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getCodeString(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()),
                null, locale);
        when(translator.toLocale(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCodeString(ErrorResponseStatusType
                .MISSING_REQUIRED_FIELDS.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(GET_BULK_USERS_URI)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(bulkUserRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingBulkUsersIsFailed() throws Exception {
        BulkUserRequestDto bulkUserRequestDto = new BulkUserRequestDto();
        List<String> userIds = new ArrayList<>();
        userIds.add("uid-1");
        bulkUserRequestDto.setUserIds(userIds);
        when(userService.getUserList(bulkUserRequestDto.getUserIds(), TIME_ZONE))
                .thenThrow(new AuthServiceException(FAILED));
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(GET_BULK_USERS_URI)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(bulkUserRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for get updated mobile no's
     * Api context: /api/v1/users/mobile-no/updates/{page}/{size}
     */
    @Test
    void Should_ReturnOk_When_GettingUpdatedMobileNumbersIsSuccessful() throws Exception {
        List<UpdatedMobileNoResponseDto> updatedMobileNoResponseDtoList = new ArrayList<>();
        updatedMobileNoResponseDtoList.add(new UpdatedMobileNoResponseDto(new UserMobileNo(USER_MOBILE_NO, USER_ID)));
        List<UserMobileNo> userMobileNoList = new ArrayList<>();
        userMobileNoList.add(new UserMobileNo(USER_MOBILE_NO, USER_ID));
        Pageable pageable = PageRequest.of(PAGE, SIZE);
        Page<UserMobileNo> userMobileNoPage = new PageImpl<>(userMobileNoList, pageable, userMobileNoList.size());
        UpdatedMobileNoListResponseDto updatedMobileNoListResponseDto = new UpdatedMobileNoListResponseDto(
                updatedMobileNoResponseDtoList, userMobileNoPage);
        when(userService.getUpdateMobileNoList(any(Pageable.class))).thenReturn(updatedMobileNoListResponseDto);
        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType
                        .GET_USER_UPDATED_MOBILE_NO.getCode(),
                null, locale);
        when(translator.toLocale(SuccessResponseStatusType.GET_USER_UPDATED_MOBILE_NO.getCode()))
                .thenReturn(successMessage);
        String uri = GET_UPDATED_NUMBERS_URI.replace("{userMobileNoPage}", String.valueOf(PAGE))
                .replace("{size}",
                        String.valueOf(SIZE));
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(SUCCESS_STATUS)))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType
                        .GET_USER_UPDATED_MOBILE_NO.getMessage()))
                .andExpect(jsonPath("$.data.updatedMobileNumbers[0].userId").value(USER_ID))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingUpdatedMobileNumbersIsFailed() throws Exception {
        when(userService.getUpdateMobileNoList(any(Pageable.class)))
                .thenThrow(new AuthServiceException(FAILED));
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = GET_UPDATED_NUMBERS_URI.replace("{page}", String.valueOf(PAGE)).replace("{size}",
                String.valueOf(SIZE));
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for list all users
     * Api context: /api/v1/users/{type}/{page}/{size}/search/{searchTerm}
     */
    @Test
    void Should_ReturnOk_When_GettingAllUsersForMerchants() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE);
        List<MerchantBankSearchIndex> merchantBankSearchIndexList = new ArrayList<>();
        user.setRole(new Role(RoleType.MERCHANT));
        user.setCreatedAt(new Date());
        merchantBankSearchIndexList.add(new MerchantBankSearchIndex(user));
        Page<MerchantBankSearchIndex> merchantBankSearchIndexPage = new PageImpl<>(merchantBankSearchIndexList, pageable,
                merchantBankSearchIndexList.size());
        when(userRoleService.getMerchantsOrBanks(SEARCH_TERM, pageable, RoleType.MERCHANT))
                .thenReturn(merchantBankSearchIndexPage);
        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType.GET_USERS.getCode(),
                null, locale);
        when(translator.toLocale(SuccessResponseStatusType.GET_USERS.getCode())).thenReturn(successMessage);
        String uri = GET_ALL_USERS_URI.replace("{type}", String.valueOf(RoleType.MERCHANT))
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(SUCCESS_STATUS)))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.GET_USERS.getMessage()))
                .andExpect(jsonPath("$.data.merchants[0].profile.id").value(USER_ID))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnOk_When_GettingAllUsersForUsers() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE);
        List<User> userList = new ArrayList<>();
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        userList.add(user);
        Page<User> userPage = new PageImpl<>(userList, pageable, userList.size());
        when(userRoleService.getUsers(RoleType.USER.getId(), SEARCH_TERM, pageable)).thenReturn(userPage);
        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType.GET_USERS.getCode(),
                null, locale);
        when(translator.toLocale(SuccessResponseStatusType.GET_USERS.getCode())).thenReturn(successMessage);
        String uri = GET_ALL_USERS_URI.replace("{type}", String.valueOf(RoleType.USER))
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(SUCCESS_STATUS)))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.GET_USERS.getMessage()))
                .andExpect(jsonPath("$.data.users[0].userId").value(USER_ID))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_GettingAllUsersForUnsupportedUserType() throws Exception {
        String invalidMessage = resourceBundleMessageSource.getMessage(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE
                .getCodeString(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE.getCode()), null, locale);
        when(translator.toLocale(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE.getCodeString(ErrorResponseStatusType
                .UNSUPPORTED_USER_TYPE.getCode()))).thenReturn(invalidMessage);
        String uri = GET_ALL_USERS_URI.replace("{type}", RoleType.ADMIN.toString())
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.UNSUPPORTED_USER_TYPE
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GettingAllUsersIsFailed() throws Exception {
        Pageable pageable = PageRequest.of(PAGE, SIZE);
        when(userRoleService.getUsers(RoleType.USER.getId(), SEARCH_TERM, pageable))
                .thenThrow(new AuthServiceException(FAILED));
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        String uri = GET_ALL_USERS_URI.replace("{type}", String.valueOf(RoleType.USER))
                .replace("{page}", String.valueOf(PAGE)).replace("{size}", String.valueOf(SIZE))
                .replace("{searchTerm}", SEARCH_TERM);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for register guest user
     * Api context: /api/v1/users/register/GUEST
     */
    @Test
    void Should_ReturnOk_When_RegisteringGuestUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_GUEST_USER_URI)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void Should_ReturnInternalServerError_When_RegisteringGuestUserWasFailed() throws Exception {
        when(userService.createGuestUser()).thenThrow(new AuthServiceException(FAILED));
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_GUEST_USER_URI)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    /**
     * Start of tests for reset password
     * Api context: /api/v1/users/password/reset
     */
    @Test
    void Should_ReturnOk_When_ResettingPasswordIsSuccessful() throws Exception {
        when(userService.resetPassword(any(ResetPasswordRequestDto.class))).thenReturn(user);
        when(userService.getTokenByUserId(user.getId())).thenReturn(new ArrayList<>());
        when(defaultTokenServices.revokeToken(anyString())).thenReturn(true);
        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType.RESET_PASSWORD
                .getCode(), null, locale);
        when(translator.toLocale(SuccessResponseStatusType.RESET_PASSWORD.getCode())).thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(RESET_USER_PASSWORD_URI)
                        .content(resetPasswordRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(SUCCESS_STATUS)))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.RESET_PASSWORD.getMessage()))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_ResettingPasswordForInvalidPassword() throws Exception {
        ReflectionTestUtils.setField(resetPasswordRequestDto, "password", "");
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INVALID_PASSWORD.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_PASSWORD.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(RESET_USER_PASSWORD_URI)
                        .content(resetPasswordRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_PASSWORD.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_PASSWORD.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_ResettingPasswordForMissingRequiredFields() throws Exception {
        ReflectionTestUtils.setField(resetPasswordRequestDto, "token", "");
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .MISSING_REQUIRED_FIELDS.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(RESET_USER_PASSWORD_URI)
                        .content(resetPasswordRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_ResettingPasswordForInvalidUser() throws Exception {
        when(userService.resetPassword(any(ResetPasswordRequestDto.class)))
                .thenThrow(new InvalidUserException(FAILED));
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .NO_USER.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.NO_USER.getCode()))).thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(RESET_USER_PASSWORD_URI)
                        .content(resetPasswordRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.NO_USER.getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.NO_USER.getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_ResettingPasswordForExpiredPasswordResetToken() throws Exception {
        when(userService.resetPassword(any(ResetPasswordRequestDto.class)))
                .thenThrow(new ExpiredPasswordResetTokenException(FAILED));
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .PASSWORD_RESET_TOKEN_WAS_EXPIRED.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.PASSWORD_RESET_TOKEN_WAS_EXPIRED.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(RESET_USER_PASSWORD_URI)
                        .content(resetPasswordRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.PASSWORD_RESET_TOKEN_WAS_EXPIRED
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.PASSWORD_RESET_TOKEN_WAS_EXPIRED
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_ResettingPasswordIsFailed() throws Exception {
        when(userService.resetPassword(any(ResetPasswordRequestDto.class)))
                .thenThrow(new AuthServiceException(FAILED));
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        mockMvc.perform(MockMvcRequestBuilders.put(RESET_USER_PASSWORD_URI)
                        .content(resetPasswordRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnOk_When_GenerateAApiAccessKeyForMerchantSuccessfully() throws Exception {
        User merchant = getSampleUser();
        merchant.setRole(new Role(RoleType.ADMIN));
        String uri = GENERATE_API_KEY_FOR_MERCHANT_URI.replace("{merchantId}", USER_ID);
        String message = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .SUCCESSFULLY_GENERATED_API_KEY.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.SUCCESSFULLY_GENERATED_API_KEY.getCode())))
                .thenReturn(message);
        when(userService.getUserByUserId(USER_ID)).thenReturn(merchant);
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(SUCCESS_STATUS)))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.SUCCESSFULLY_GENERATED_API_KEY.getMessage()))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.displayMessage").value(message));
    }

    @Test
    void Should_ReturnBadRequest_When_UserRoleNotEqualToAdminForGenerateApiKey() throws Exception {
        User merchant = getSampleUser();
        merchant.setRole(new Role(RoleType.MERCHANT));
        String uri = GENERATE_API_KEY_FOR_MERCHANT_URI.replace("{merchantId}", USER_ID);
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType.
                INVALID_ACTION.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_ACTION.getCode())))
                .thenReturn(invalidMessage);
        when(userService.getUserByUserId(USER_ID)).thenReturn(merchant);
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_ACTION
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_ACTION
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidUserTypeForGenerateApiKey() throws Exception {
        User merchant = getSampleUser();
        merchant.setRole(new Role(RoleType.ADMIN));
        String uri = GENERATE_API_KEY_FOR_MERCHANT_URI.replace("{merchantId}", USER_ID);
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType.
                INVALID_USER_TYPE.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_USER_TYPE.getCode())))
                .thenReturn(invalidMessage);
        when(userService.getUserByUserId(USER_ID)).thenReturn(merchant);
        doThrow(new InvalidActionException("ERROR")).when(userService).generateAccessKey(USER_ID);
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_TYPE
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_TYPE
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GenerateApiKeyIsFailed() throws Exception {
        User merchant = getSampleUser();
        merchant.setRole(new Role(RoleType.ADMIN));
        String uri = GENERATE_API_KEY_FOR_MERCHANT_URI.replace("{merchantId}", USER_ID);
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType.
                INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        when(userService.getUserByUserId(USER_ID)).thenReturn(merchant);
        doThrow(new AuthServiceException("ERROR")).when(userService).generateAccessKey(USER_ID);
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnOk_When_GetApiAccessKeyForMerchant() throws Exception {
        String uri = GET_API_KEY_URI.replace("{merchantId}", USER_ID);
        when(userService.getApiAccessKey(USER_ID)).thenReturn(BASIC_TOKEN);
        String successMessage = resourceBundleMessageSource.getMessage(String.valueOf(SuccessResponseStatusType
                .SUCCESSFULLY_RETURNED_KEY.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(SuccessResponseStatusType.SUCCESSFULLY_RETURNED_KEY.getCode())))
                .thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(SUCCESS_STATUS)))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.SUCCESSFULLY_RETURNED_KEY.getMessage()))
                .andExpect(jsonPath("$.data.key").value(BASIC_TOKEN))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidUserIdIsProvidedForGetApiKey() throws Exception {
        String uri = GET_API_KEY_URI.replace("{merchantId}", USER_ID);
        when(userService.getApiAccessKey(USER_ID)).thenReturn(BASIC_TOKEN);
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType.
                INVALID_USER_ID.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INVALID_USER_ID.getCode())))
                .thenReturn(invalidMessage);
        doThrow(new InvalidUserException("ERROR")).when(userService).getApiAccessKey(USER_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_ID
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_ID
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetApiKeyIsFailed() throws Exception {
        String uri = GET_API_KEY_URI.replace("{merchantId}", USER_ID);
        when(userService.getApiAccessKey(USER_ID)).thenReturn(BASIC_TOKEN);
        String invalidMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType.
                INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(invalidMessage);
        doThrow(new AuthServiceException("ERROR")).when(userService).getApiAccessKey(USER_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getMessage()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR
                        .getCode()))
                .andExpect(jsonPath("$.displayMessage").value(invalidMessage));
    }

    private UserUpdateRequestDto getSampleUserUpdateRequestDto() {
        UserUpdateRequestDto userUpdateRequestDto = new UserUpdateRequestDto();
        userUpdateRequestDto.setFullName(USER_NAME);
        return userUpdateRequestDto;
    }

    private UserPasswordUpdateRequestDto getSamplePasswordUpdateRequestDto() {
        UserPasswordUpdateRequestDto userPasswordUpdateRequestDto = new UserPasswordUpdateRequestDto();
        userPasswordUpdateRequestDto.setPassword("Tokoin@123");
        userPasswordUpdateRequestDto.setNewPassword("Tokoin@1234");
        return userPasswordUpdateRequestDto;
    }

    private ResetPasswordRequestDto getSampleResetPasswordRequestDto() {
        ResetPasswordRequestDto resetPasswordRequestDto = new ResetPasswordRequestDto();
        ReflectionTestUtils.setField(resetPasswordRequestDto, "password", USER_PASS);
        ReflectionTestUtils.setField(resetPasswordRequestDto, "token", "Tk-1");
        return resetPasswordRequestDto;
    }

    private UnregisterUserDto getSampleUnregisteredUserDto() {
        UnregisterUserDto unregisterUserDto = new UnregisterUserDto();
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto();
        mobileNoRequestDto.setLocalNumber(LOCAL_NUMBER);
        mobileNoRequestDto.setCountryCode(COUNTRY_CODE);
        unregisterUserDto.setMobileNo(mobileNoRequestDto);
        return unregisterUserDto;
    }

    private UserMobileUpdateRequestDto getSampleMobileUpdateRequestDto() {
        UserMobileUpdateRequestDto userMobileUpdateRequestDto = new UserMobileUpdateRequestDto();
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto();
        mobileNoRequestDto.setCountryCode(COUNTRY_CODE);
        mobileNoRequestDto.setLocalNumber(LOCAL_NUMBER);
        userMobileUpdateRequestDto.setMobileNo(mobileNoRequestDto);
        return userMobileUpdateRequestDto;
    }

    private UserEmailUpdateRequestDto getSampleEmailUpdateRequestDto() {
        UserEmailUpdateRequestDto userEmailUpdateRequestDto = new UserEmailUpdateRequestDto();
        userEmailUpdateRequestDto.setEmail(USER_EMAIL);
        return userEmailUpdateRequestDto;
    }

    private UserRequestDto getSampleUserRequestDto() {
        UserRequestDto userRequestDto = new UserRequestDto();
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto();
        mobileNoRequestDto.setCountryCode(COUNTRY_CODE);
        mobileNoRequestDto.setLocalNumber(LOCAL_NUMBER);
        userRequestDto.setFullName(USER_NAME);
        userRequestDto.setEmail(USER_EMAIL);
        userRequestDto.setPassword(USER_PASS);
        userRequestDto.setMobileNo(mobileNoRequestDto);
        userRequestDto.setLanguage(USER_LANGUAGE);
        return userRequestDto;
    }

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
}
