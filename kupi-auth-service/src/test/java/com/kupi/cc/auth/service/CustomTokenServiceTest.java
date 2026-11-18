package com.kupi.cc.auth.service;

import com.kupi.cc.auth.domain.ResourcePermission;
import com.kupi.cc.auth.domain.TokenResponse;
import com.kupi.cc.auth.domain.entity.Permission;
import com.kupi.cc.auth.domain.entity.Resource;
import com.kupi.cc.auth.domain.entity.Role;
import com.kupi.cc.auth.domain.entity.User;
import com.kupi.cc.auth.enums.Language;
import com.kupi.cc.auth.enums.RoleType;
import com.kupi.cc.auth.exception.AuthServiceException;
import com.kupi.cc.auth.repository.ResourceRepository;
import com.kupi.cc.auth.wrapper.RoleResponseWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests {@link CustomTokenService} class.
 */
class CustomTokenServiceTest {

    private static final String CLIENT_ID = "tokomobile";
    private static final int TOKEN_VALIDITY = 3600;
    private static final String ID_TOKEN = "changeme-auth-basic-token";
    private static final String USER_EMAIL = "rengy@tokoin.io";
    private static final String USER_MOBILE_NO = "+62-81123123";
    private static final String USER_ID = "uid-1234567890";

    @Mock
    private DefaultTokenServices authTokenServices;
    @Mock
    private JdbcTokenStore jdbcTokenStore;
    @Mock
    private ResourceRepository resourceRepository;
    private CustomTokenService customTokenService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        customTokenService = new CustomTokenService(authTokenServices, jdbcTokenStore, CLIENT_ID, TOKEN_VALIDITY,
                resourceRepository);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnTokenResponse_When_GenerateTokenSuccessfully() {
        User user = getSampleUser();
        Resource resource = getSampleResource();
        List<Resource> resourceList = new ArrayList<>();
        TokenResponse tokenResponse = getSampleTokenResponse();
        OAuth2AccessToken accessToken = getSampleOAuth2AccessToken();
        when(authTokenServices.createAccessToken(any(OAuth2Authentication.class))).thenReturn(accessToken);
        when(resourceRepository.findAll()).thenReturn(resourceList);
        assertEquals(tokenResponse.getAccessToken(), customTokenService.generateToken(user).getAccessToken());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GetResourceListFromDatabaseIsFailed() {
        User user = getSampleUser();
        OAuth2AccessToken accessToken = getSampleOAuth2AccessToken();
        when(authTokenServices.createAccessToken(any(OAuth2Authentication.class))).thenReturn(accessToken);
        when(resourceRepository.findAll()).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                customTokenService.generateToken(user));
        assertEquals("ERrror", exception.getMessage());
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

    /**
     * This method creates sample user
     *
     * @return User
     */
    private User getSampleUser() {
        Permission permission = getSamplePermission();
        User user = new User();
        user.setId(USER_ID);
        user.setFullName("user1");
        user.setEmail(USER_EMAIL);
        user.setMobileNo(USER_MOBILE_NO);
        Role role = new Role();
        role.setId(1);
        role.setDeletable(false);
        role.setName("role");
        List<Permission> permissionList = new ArrayList<>();
        permissionList.add(permission);
        role.setPermissions(permissionList);
        user.setRole(role);
        user.setImageUrl("");
        user.setLanguage(Language.valueOf("ENGLISH"));
        user.setEnabled(true);
        user.setRegisteredUser(true);
        return user;
    }

    /**
     * This method creates sample OAuth2AccessToken
     *
     * @return OAuth2AccessToken
     */
    private OAuth2AccessToken getSampleOAuth2AccessToken() {
        OAuth2RefreshToken refreshToken = new DefaultOAuth2RefreshToken(ID_TOKEN);
        DefaultOAuth2AccessToken accessToken = new DefaultOAuth2AccessToken(ID_TOKEN);
        accessToken.setRefreshToken(refreshToken);
        return accessToken;
    }

    private Permission getSamplePermission() {
        Permission permission = new Permission();
        Resource resource = getSampleResource();
        permission.setId(1);
        permission.setMethod("Admin");
        permission.setName("Admin");
        permission.setResource(resource);
        return permission;
    }

    private Resource getSampleResource() {
        Resource resource = new Resource();
        resource.setDefault(true);
        resource.setName("resource");
        resource.setId(1);
        return resource;
    }
}