package com.kupi.cc.auth.service;

import com.kupi.cc.auth.domain.ResourcePermission;
import com.kupi.cc.auth.domain.TokenResponse;
import com.kupi.cc.auth.domain.entity.Role;
import com.kupi.cc.auth.domain.entity.User;
import com.kupi.cc.auth.domain.response.SocialSignInResponseDto;
import com.kupi.cc.auth.enums.Language;
import com.kupi.cc.auth.enums.RoleType;
import com.kupi.cc.auth.exception.AuthServiceException;
import com.kupi.cc.auth.exception.InvalidUserException;
import com.kupi.cc.auth.exception.NoUserException;
import com.kupi.cc.auth.exception.UserAlreadyExistsException;
import com.kupi.cc.auth.repository.UserRepository;
import com.kupi.cc.auth.wrapper.GoogleResponseWrapper;
import com.kupi.cc.auth.wrapper.RoleResponseWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests {@link GoogleAuthService} class
 */
class GoogleAuthServiceTest {
    private static final String USER_EMAIL = "rengy@tokoin.io";
    private static final String USER_MOBILE_NO = "+62-81123123";
    private static final String USER_ID = "uid-1234567890";
    private static final String EMAIL = "example@gmail.com";
    private static final String NAME = "david";
    private static final String VALIDATE_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=##ID_TOKEN##";
    private static final String ID_TOKEN_KEY = "##ID_TOKEN##";
    private static final String ID_TOKEN = "changeme-auth-basic-token";

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @Mock
    private CustomTokenService customTokenService;
    @Mock
    private RestTemplate restTemplate;
    private GoogleAuthService googleAuthService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        googleAuthService = new GoogleAuthService(userRepository, userService, customTokenService, restTemplate);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnUser_When_CreateAUser() {
        Role role = new Role(RoleType.USER);
        GoogleResponseWrapper googleResponseWrapper = getSampleGoogleResponseWrapper();
        String url = VALIDATE_URL.replace(ID_TOKEN_KEY, ID_TOKEN);
        HttpHeaders headers = getAuthHeaders();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        when(restTemplate.exchange(url, HttpMethod.GET, entity, GoogleResponseWrapper.class))
                .thenReturn(ResponseEntity.of(Optional.of(googleResponseWrapper)));
        when(userService.isEmailExist(EMAIL)).thenReturn(false);
        when(userService.getDefaultRole()).thenReturn(role);
        User user = googleAuthService.createUser(ID_TOKEN);
        verify(userRepository).save(any(User.class));
        assertEquals(EMAIL, user.getEmail());
    }

    @Test
    void Should_ThrowInvalidUserException_When_UserIsNotExists() {
        String url = VALIDATE_URL.replace(ID_TOKEN_KEY, ID_TOKEN);
        HttpHeaders headers = getAuthHeaders();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        when(restTemplate.exchange(url, HttpMethod.GET, entity, GoogleResponseWrapper.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        InvalidUserException exception = assertThrows(InvalidUserException.class, () ->
                googleAuthService.createUser(ID_TOKEN));
        assertEquals("Invalid user token", exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GetUserFromGoogleAuthIsFailed() {
        String url = VALIDATE_URL.replace(ID_TOKEN_KEY, ID_TOKEN);
        HttpHeaders headers = getAuthHeaders();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        when(restTemplate.exchange(url, HttpMethod.GET, entity, GoogleResponseWrapper.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                googleAuthService.createUser(ID_TOKEN));
        assertEquals("Getting user by mobile no was failed from auth service", exception.getMessage());
    }

    @Test
    void Should_ThrowUserAlreadyExistsException_When_EmailAddressAlreadyExists() {
        GoogleResponseWrapper googleResponseWrapper = getSampleGoogleResponseWrapper();
        String url = VALIDATE_URL.replace(ID_TOKEN_KEY, ID_TOKEN);
        HttpHeaders headers = getAuthHeaders();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        when(restTemplate.exchange(url, HttpMethod.GET, entity, GoogleResponseWrapper.class))
                .thenReturn(ResponseEntity.of(Optional.of(googleResponseWrapper)));
        when(userService.isEmailExist(EMAIL)).thenReturn(true);
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () ->
                googleAuthService.createUser(ID_TOKEN));
        assertEquals("Already existing user.", exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_CheckEmailExistanceIsFailed() {
        GoogleResponseWrapper googleResponseWrapper = getSampleGoogleResponseWrapper();
        String url = VALIDATE_URL.replace(ID_TOKEN_KEY, ID_TOKEN);
        HttpHeaders headers = getAuthHeaders();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        when(restTemplate.exchange(url, HttpMethod.GET, entity, GoogleResponseWrapper.class))
                .thenReturn(ResponseEntity.of(Optional.of(googleResponseWrapper)));
        when(userService.isEmailExist(EMAIL)).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                googleAuthService.createUser(ID_TOKEN));
        assertEquals("Checking email existence was failed", exception.getMessage());
    }

    @Test
    void Should_ReturnSocialSignInResponseDto_When_UserLogInSuccessfully() {
        User user = getSampleUser();
        TokenResponse tokenResponse = getSampleTokenResponse();
        GoogleResponseWrapper googleResponseWrapper = getSampleGoogleResponseWrapper();
        SocialSignInResponseDto socialSignInResponseDto = new SocialSignInResponseDto(user, tokenResponse);
        String url = VALIDATE_URL.replace(ID_TOKEN_KEY, ID_TOKEN);
        HttpHeaders headers = getAuthHeaders();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        when(restTemplate.exchange(url, HttpMethod.GET, entity, GoogleResponseWrapper.class))
                .thenReturn(ResponseEntity.of(Optional.of(googleResponseWrapper)));
        when(userRepository.findByEmail(EMAIL)).thenReturn(user);
        when(customTokenService.generateToken(user)).thenReturn(tokenResponse);
        assertEquals(socialSignInResponseDto.getUserId(), googleAuthService.loginUser(ID_TOKEN).getUserId());
    }

    @Test
    void Should_ThrowNoUserException_When_UserNotFound() {
        GoogleResponseWrapper googleResponseWrapper = getSampleGoogleResponseWrapper();
        String url = VALIDATE_URL.replace(ID_TOKEN_KEY, ID_TOKEN);
        HttpHeaders headers = getAuthHeaders();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        when(restTemplate.exchange(url, HttpMethod.GET, entity, GoogleResponseWrapper.class))
                .thenReturn(ResponseEntity.of(Optional.of(googleResponseWrapper)));
        when(userRepository.findByEmail(EMAIL)).thenReturn(null);
        NoUserException exception = assertThrows(NoUserException.class, () ->
                googleAuthService.loginUser(ID_TOKEN));
        assertEquals("Invalid user", exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GetUserByEmailIsFailed() {
        GoogleResponseWrapper googleResponseWrapper = getSampleGoogleResponseWrapper();
        String url = VALIDATE_URL.replace(ID_TOKEN_KEY, ID_TOKEN);
        HttpHeaders headers = getAuthHeaders();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        when(restTemplate.exchange(url, HttpMethod.GET, entity, GoogleResponseWrapper.class))
                .thenReturn(ResponseEntity.of(Optional.of(googleResponseWrapper)));
        when(userRepository.findByEmail(EMAIL)).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                googleAuthService.loginUser(ID_TOKEN));
        assertEquals("Checking email existence was failed", exception.getMessage());
    }

    /**
     * This method creates sample google response wrapper
     *
     * @return GoogleResponseWrapper
     */
    private GoogleResponseWrapper getSampleGoogleResponseWrapper() {
        GoogleResponseWrapper googleResponseWrapper = new GoogleResponseWrapper();
        googleResponseWrapper.setEmail(EMAIL);
        googleResponseWrapper.setName(NAME);
        googleResponseWrapper.setLocale("en");
        googleResponseWrapper.setPicture("https://media.ragalahari.com/profilephotos/dulquer-salmaan-poster.jpg");
        googleResponseWrapper.setEmail_verified(true);
        return googleResponseWrapper;
    }

    /**
     * This method creates sample http headers
     *
     * @return HttpHeaders
     */
    private HttpHeaders getAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * This method creates sample user
     *
     * @return User
     */
    private User getSampleUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setFullName("user1");
        user.setEmail(USER_EMAIL);
        user.setMobileNo(USER_MOBILE_NO);
        user.setRole(new Role(RoleType.USER));
        user.setImageUrl("");
        user.setLanguage(Language.valueOf("ENGLISH"));
        user.setEnabled(true);
        user.setRegisteredUser(true);
        return user;
    }

    /**
     * This method creates sample token response
     *
     * @return TokenResponse
     */
    private TokenResponse getSampleTokenResponse() {
        TokenResponse tokenResponse = new TokenResponse();
        RoleResponseWrapper roleResponseWrapper = getSampleRoleResponseWrapper();
        tokenResponse.setRefreshToken(ID_TOKEN);
        tokenResponse.setAccessToken(ID_TOKEN);
        tokenResponse.setRole(roleResponseWrapper);
        return tokenResponse;
    }

    /**
     * This method creats sample role response wrapper
     *
     * @return RoleResponseWrapper
     */
    private RoleResponseWrapper getSampleRoleResponseWrapper() {
        RoleResponseWrapper roleResponseWrapper = new RoleResponseWrapper();
        ResourcePermission resourcePermission = new ResourcePermission(1, "resource");
        List<ResourcePermission> resourcePermissionList = new ArrayList<>();
        resourcePermissionList.add(resourcePermission);
        roleResponseWrapper.setId(1);
        roleResponseWrapper.setPermissions(resourcePermissionList);
        return roleResponseWrapper;
    }
}