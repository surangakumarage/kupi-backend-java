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
import com.kupi.cc.auth.wrapper.FacebookResponseWrapper;
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
 * This class tests {@link FacebookAuthService} class
 */
class FacebookAuthServiceTest {
    private static final String USER_EMAIL = "rengy@tokoin.io";
    private static final String USER_MOBILE_NO = "+62-81123123";
    private static final String USER_ID = "uid-1234567890";
    private static final String ID_TOKEN = "changeme-auth-basic-token";
    private static final String VALIDATE_URL = "https://graph.facebook.com/v8.0/me?fields=id,name,email&access_token=##ACCESS_TOKEN##";
    private static final String ACCESS_TOKEN_KEY = "##ACCESS_TOKEN##";

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private CustomTokenService customTokenService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    private FacebookAuthService facebookAuthService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        facebookAuthService = new FacebookAuthService(customTokenService, userRepository, userService, restTemplate);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnUser_When_UserCreatedSuccessfully() {
        FacebookResponseWrapper facebookResponseWrapper = getSampleFacebookResponseWrapper();
        Role role = new Role(RoleType.USER);
        String url = VALIDATE_URL.replace(ACCESS_TOKEN_KEY, ID_TOKEN);
        var headers = getAuthHeaders();
        var entity = new HttpEntity<String>(null, headers);
        when(restTemplate.exchange(url, HttpMethod.GET, entity, FacebookResponseWrapper.class))
                .thenReturn(ResponseEntity.of(Optional.of(facebookResponseWrapper)));
        when(userService.isEmailExist(USER_EMAIL)).thenReturn(false);
        when(userService.getDefaultRole()).thenReturn(role);
        User user = facebookAuthService.createUser(ID_TOKEN);
        verify(userRepository).save(any(User.class));
        assertEquals(USER_EMAIL, user.getEmail());
    }

    @Test
    void Should_ThrowInvalidUserException_When_UserIsNotExists() {
        String url = VALIDATE_URL.replace(ACCESS_TOKEN_KEY, ID_TOKEN);
        HttpHeaders headers = getAuthHeaders();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        when(restTemplate.exchange(url, HttpMethod.GET, entity, FacebookResponseWrapper.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        InvalidUserException exception = assertThrows(InvalidUserException.class, () ->
                facebookAuthService.createUser(ID_TOKEN));
        assertEquals("Invalid user token", exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GetUserFromGoogleAuthIsFailed() {
        String url = VALIDATE_URL.replace(ACCESS_TOKEN_KEY, ID_TOKEN);
        HttpHeaders headers = getAuthHeaders();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        when(restTemplate.exchange(url, HttpMethod.GET, entity, FacebookResponseWrapper.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                facebookAuthService.createUser(ID_TOKEN));
        assertEquals("Getting user by mobile no was failed from auth service", exception.getMessage());
    }

    @Test
    void Should_ThrowUserAlreadyExistsException_When_UserEmailAlreadyExists() {
        FacebookResponseWrapper facebookResponseWrapper = getSampleFacebookResponseWrapper();
        String url = VALIDATE_URL.replace(ACCESS_TOKEN_KEY, ID_TOKEN);
        var headers = getAuthHeaders();
        var entity = new HttpEntity<String>(null, headers);
        when(restTemplate.exchange(url, HttpMethod.GET, entity, FacebookResponseWrapper.class))
                .thenReturn(ResponseEntity.of(Optional.of(facebookResponseWrapper)));
        when(userService.isEmailExist(USER_EMAIL)).thenReturn(true);
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () ->
                facebookAuthService.createUser(ID_TOKEN));
        assertEquals("Already existing user.", exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_SaveUserIsFailed() {
        FacebookResponseWrapper facebookResponseWrapper = getSampleFacebookResponseWrapper();
        String url = VALIDATE_URL.replace(ACCESS_TOKEN_KEY, ID_TOKEN);
        var headers = getAuthHeaders();
        var entity = new HttpEntity<String>(null, headers);
        when(restTemplate.exchange(url, HttpMethod.GET, entity, FacebookResponseWrapper.class))
                .thenReturn(ResponseEntity.of(Optional.of(facebookResponseWrapper)));
        when(userService.isEmailExist(USER_EMAIL)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                facebookAuthService.createUser(ID_TOKEN));
        assertEquals("Checking email existence was failed", exception.getMessage());
    }

    @Test
    void Should_ReturnSocialSignInResponseDto_When_UserLogInSuccessfully() {
        User user = getSampleUser();
        TokenResponse tokenResponse = getSampleTokenResponse();
        FacebookResponseWrapper facebookResponseWrapper = getSampleFacebookResponseWrapper();
        SocialSignInResponseDto socialSignInResponseDto = new SocialSignInResponseDto(user, tokenResponse);
        String url = VALIDATE_URL.replace(ACCESS_TOKEN_KEY, ID_TOKEN);
        HttpHeaders headers = getAuthHeaders();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        when(restTemplate.exchange(url, HttpMethod.GET, entity, FacebookResponseWrapper.class))
                .thenReturn(ResponseEntity.of(Optional.of(facebookResponseWrapper)));
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(user);
        when(customTokenService.generateToken(user)).thenReturn(tokenResponse);
        assertEquals(socialSignInResponseDto.getUserId(), facebookAuthService.loginUser(ID_TOKEN).getUserId());
    }

    @Test
    void Should_ThrowNoUserException_When_UserNotFound() {
        FacebookResponseWrapper facebookResponseWrapper = getSampleFacebookResponseWrapper();
        String url = VALIDATE_URL.replace(ACCESS_TOKEN_KEY, ID_TOKEN);
        HttpHeaders headers = getAuthHeaders();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        when(restTemplate.exchange(url, HttpMethod.GET, entity, FacebookResponseWrapper.class))
                .thenReturn(ResponseEntity.of(Optional.of(facebookResponseWrapper)));
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(null);
        NoUserException exception = assertThrows(NoUserException.class, () ->
                facebookAuthService.loginUser(ID_TOKEN));
        assertEquals("Invalid user", exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GetUserByEmailIsFailed() {
        FacebookResponseWrapper facebookResponseWrapper = getSampleFacebookResponseWrapper();
        String url = VALIDATE_URL.replace(ACCESS_TOKEN_KEY, ID_TOKEN);
        HttpHeaders headers = getAuthHeaders();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        when(restTemplate.exchange(url, HttpMethod.GET, entity, FacebookResponseWrapper.class))
                .thenReturn(ResponseEntity.of(Optional.of(facebookResponseWrapper)));
        when(userRepository.findByEmail(USER_EMAIL)).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                facebookAuthService.loginUser(ID_TOKEN));
        assertEquals("Checking email existence was failed", exception.getMessage());
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
     * This method creates sample facebook response wrapper
     *
     * @return FacebookResponseWrapper
     */
    private FacebookResponseWrapper getSampleFacebookResponseWrapper() {
        FacebookResponseWrapper facebookResponseWrapper = new FacebookResponseWrapper();
        facebookResponseWrapper.setEmail(USER_EMAIL);
        facebookResponseWrapper.setName("facebook");
        facebookResponseWrapper.setId(USER_ID);
        return facebookResponseWrapper;
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