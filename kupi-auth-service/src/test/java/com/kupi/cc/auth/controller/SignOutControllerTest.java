package com.kupi.cc.auth.controller;

import com.kupi.cc.auth.configuration.ResourceBundleMessageSourceBean;
import com.kupi.cc.auth.configuration.Translator;
import com.kupi.cc.auth.enums.ErrorResponseStatusType;
import com.kupi.cc.auth.enums.SuccessResponseStatusType;
import com.kupi.cc.auth.exception.AuthServiceException;
import com.kupi.cc.auth.exception.InvalidUserException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
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
 * This class tests {@link SignOutController} class
 */
class SignOutControllerTest {

    private static final String HEADER_USER_ID = "User-Id";
    private static final String AUTHORIZATION = "Authorization";
    private static final String ACCESS_TOKEN = "Bearer192f0c96-dd19-404e-8217-a8fd1cc81d55";
    private static final String USER_ID = "uid-d9d069f9-2b07-446f-8117-e78ff14d01d8";
    private static final String SIGN_OUT_URI = "/api/v1/users/sign-out";
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String APPLICATION_JSON_UTF_8 = "application/json;charset=UTF-8";
    private final ResourceBundleMessageSourceBean resourceBundleMessageSourceBean = new ResourceBundleMessageSourceBean();
    private final Locale locale = LocaleContextHolder.getLocale();
    @Mock
    private Translator translator;
    @Mock
    private DefaultTokenServices defaultTokenServices;
    private MockMvc mockMvc;
    private ResourceBundleMessageSource resourceBundleMessageSource;

    @BeforeEach
    void setUp() {
        initMocks(this);
        SignOutController signOutController = new SignOutController(translator, defaultTokenServices);
        mockMvc = MockMvcBuilders.standaloneSetup(signOutController).build();
        resourceBundleMessageSource = resourceBundleMessageSourceBean.messageSource();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnOk_When_SignOutIsSuccessful() throws Exception {
        when(defaultTokenServices.revokeToken(anyString())).thenReturn(true);
        String successMessage = resourceBundleMessageSource.getMessage(SuccessResponseStatusType.LOGGED_OUT.getCode(),
                null, locale);
        when(translator.toLocale(SuccessResponseStatusType.LOGGED_OUT.getCode()))
                .thenReturn(successMessage);
        mockMvc.perform(MockMvcRequestBuilders.get(SIGN_OUT_URI)
                        .header(AUTHORIZATION, ACCESS_TOKEN)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(SUCCESS_STATUS)))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.LOGGED_OUT.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.displayMessage").value(successMessage));
    }

    @Test
    void Should_ReturnBadRequest_When_UserIsUnauthorized() throws Exception {
        doThrow(new InvalidUserException("ERROR")).when(defaultTokenServices).revokeToken(anyString());
        String errorMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .UNAUTHORIZED.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.UNAUTHORIZED.getCode())))
                .thenReturn(errorMessage);
        mockMvc.perform(MockMvcRequestBuilders.get(SIGN_OUT_URI)
                        .header(AUTHORIZATION, ACCESS_TOKEN)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.UNAUTHORIZED.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.displayMessage").value(errorMessage));
    }

    @Test
    void Should_ReturnInternalServerError_When_SignOutIsFailed() throws Exception {
        doThrow(new AuthServiceException("ERROR")).when(defaultTokenServices).revokeToken(anyString());
        String errorMessage = resourceBundleMessageSource.getMessage(String.valueOf(ErrorResponseStatusType
                .INTERNAL_SERVER_ERROR.getCode()), null, locale);
        when(translator.toLocale(String.valueOf(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode())))
                .thenReturn(errorMessage);
        mockMvc.perform(MockMvcRequestBuilders.get(SIGN_OUT_URI)
                        .header(AUTHORIZATION, ACCESS_TOKEN)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status", is(ERROR_STATUS)))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.displayMessage").value(errorMessage));
    }
}