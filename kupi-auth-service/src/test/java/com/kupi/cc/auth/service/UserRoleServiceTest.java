package com.kupi.cc.auth.service;

import com.kupi.cc.auth.domain.entity.MerchantBankSearchIndex;
import com.kupi.cc.auth.domain.entity.Role;
import com.kupi.cc.auth.domain.entity.User;
import com.kupi.cc.auth.enums.RoleType;
import com.kupi.cc.auth.exception.AuthServiceException;
import com.kupi.cc.auth.exception.InvalidRoleException;
import com.kupi.cc.auth.exception.InvalidUserException;
import com.kupi.cc.auth.repository.MerchantBankSearchIndexRepository;
import com.kupi.cc.auth.repository.RoleRepository;
import com.kupi.cc.auth.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests {@link UserRoleService} class
 */
class UserRoleServiceTest {

    private static final int PAGE_NUMBER = 0;
    private static final int PAGE_SIZE = 250;
    private static final String JOINED_ON = "joinedOn";
    private static final String UPDATED_AT = "updatedAt";
    private static final String FULL_NAME = "fullName";
    private static final String USER_ID = "uid-1234567890";
    private static final String FAILED = "failed";
    private static final String SEARCH_TERM = "SINGER";
    private static final String ALL = "ALL";
    private static final int ROLE_ID = 46;
    private static final int USER_ROLE_ID = 2;
    private static final String BASIC_TOKEN = "changeme-auth-basic-token";

    private final User user = getSampleUser();

    private UserRoleService userRoleService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JdbcTokenStore jdbcTokenStore;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private DefaultTokenServices defaultTokenServices;
    @Mock
    private MerchantBankSearchIndexRepository merchantBankSearchIndexRepository;

    @BeforeEach
    void setUp() {
        initMocks(this);
        userRoleService = new UserRoleService(userRepository, jdbcTokenStore, roleRepository, defaultTokenServices,
                merchantBankSearchIndexRepository);
    }

    @AfterEach
    void tearDown() {
    }

    /**
     * Get merchant or banks
     */
    @Test
    void Should_ReturnMerchantBankSearchIndexPage_When_GettingMerchantsOrBanksForAllSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
        RoleType userRole = RoleType.USER;
        Sort sort = Sort.by(Sort.Order.desc(JOINED_ON), Sort.Order.asc(FULL_NAME));
        Pageable defaultSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        List<MerchantBankSearchIndex> merchantBankSearchIndexList = new ArrayList<>();
        user.setRole(new Role(RoleType.USER));
        merchantBankSearchIndexList.add(new MerchantBankSearchIndex(user));
        Page<MerchantBankSearchIndex> merchantBankSearchIndexPage = new PageImpl<>(merchantBankSearchIndexList, pageable,
                merchantBankSearchIndexList.size());
        when(merchantBankSearchIndexRepository.findByUserRole(defaultSort, userRole))
                .thenReturn(merchantBankSearchIndexPage);
        assertEquals(USER_ID, userRoleService.getMerchantsOrBanks(ALL, pageable, userRole)
                .getContent().get(0).getUserId());
    }

    @Test
    void Should_ReturnMerchantBankSearchIndexPage_When_GettingMerchantsOrBanksWithSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
        RoleType userRole = RoleType.USER;
        List<MerchantBankSearchIndex> merchantBankSearchIndexList = new ArrayList<>();
        user.setRole(new Role(RoleType.USER));
        user.setFullName(SEARCH_TERM);
        merchantBankSearchIndexList.add(new MerchantBankSearchIndex(user));
        Page<MerchantBankSearchIndex> merchantBankSearchIndexPage = new PageImpl<>(merchantBankSearchIndexList, pageable,
                merchantBankSearchIndexList.size());
        when(merchantBankSearchIndexRepository.getUsersByUserRoleAndSearchTerm(pageable, SEARCH_TERM, userRole))
                .thenReturn(merchantBankSearchIndexPage);
        assertEquals(USER_ID, userRoleService.getMerchantsOrBanks(SEARCH_TERM, pageable, userRole)
                .getContent().get(0).getUserId());
        assertEquals(SEARCH_TERM, userRoleService.getMerchantsOrBanks(SEARCH_TERM, pageable, userRole)
                .getContent().get(0).getFullName());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GettingMerchantsOrBanksFailed() {
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
        RoleType userRole = RoleType.USER;
        when(merchantBankSearchIndexRepository.getUsersByUserRoleAndSearchTerm(pageable, SEARCH_TERM, userRole))
                .thenThrow(new DataAccessException(FAILED) {
                });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userRoleService.getMerchantsOrBanks(SEARCH_TERM, pageable, userRole));
        assertEquals("Getting users from database failed", exception.getMessage());
    }

    /**
     * Get users
     */
    @Test
    void Should_ReturnUsersPage_When_GettingUsersForAllSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
        Sort sort = Sort.by(Sort.Order.desc(UPDATED_AT), Sort.Order.asc(FULL_NAME));
        Pageable defaultSort =  PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        List<User> userList = new ArrayList<>();
        userList.add(user);
        Page<User> userPage = new PageImpl<>(userList, pageable, userList.size());
        when(userRepository.findByRoleIdAndEnabledTrue(ROLE_ID, defaultSort)).thenReturn(userPage);
        assertEquals(USER_ID, userRoleService.getUsers(ROLE_ID, ALL, pageable).getContent().get(0).getId());
    }

    @Test
    void Should_ReturnUsersPage_When_GettingUsersWithSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
        Sort sort = Sort.by(Sort.Order.desc(UPDATED_AT), Sort.Order.asc(FULL_NAME));
        Pageable defaultSort =  PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        List<User> userList = new ArrayList<>();
        user.setFullName(SEARCH_TERM);
        userList.add(user);
        Page<User> userPage = new PageImpl<>(userList, pageable, userList.size());
        when(userRepository.findByRoleIdAndEnabledTrueAndFullNameContaining(ROLE_ID, SEARCH_TERM, defaultSort))
                .thenReturn(userPage);
        assertEquals(USER_ID, userRoleService.getUsers(ROLE_ID, SEARCH_TERM, pageable).getContent().get(0).getId());
        assertEquals(SEARCH_TERM, userRoleService.getUsers(ROLE_ID, SEARCH_TERM, pageable).getContent().get(0)
                .getFullName());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GettingUsersIsFailed() {
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);
        Sort sort = Sort.by(Sort.Order.desc(UPDATED_AT), Sort.Order.asc(FULL_NAME));
        Pageable defaultSort =  PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        when(userRepository.findByRoleIdAndEnabledTrueAndFullNameContaining(ROLE_ID, SEARCH_TERM, defaultSort))
                .thenThrow(new DataAccessException(FAILED) {
                });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userRoleService.getUsers(ROLE_ID, SEARCH_TERM, pageable));
        assertEquals("Getting users from database failed", exception.getMessage());
    }


    @Test
    void Should_UpdateUserRole() {
        User user = getSampleUser();
        Role role = new Role(RoleType.USER);
        OAuth2AccessToken auth2AccessToken = getSampleOAuth2AccessToken();
        Collection<OAuth2AccessToken> auth2AccessTokenCollection = new ArrayList<>();
        auth2AccessTokenCollection.add(auth2AccessToken);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(roleRepository.findById(USER_ROLE_ID)).thenReturn(Optional.of(role));
        when(jdbcTokenStore.findTokensByUserName(USER_ID)).thenReturn(auth2AccessTokenCollection);
        when(defaultTokenServices.revokeToken(BASIC_TOKEN)).thenReturn(true);
        userRoleService.updateUserRole(USER_ID, USER_ROLE_ID);
        verify(userRepository).save(user);
    }

    @Test
    void Should_ThrowInvalidUserException_When_ReceivedUserFromDataBaseIsEmpty() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
        InvalidUserException exception = assertThrows(InvalidUserException.class, () ->
                userRoleService.updateUserRole(USER_ID, USER_ROLE_ID));
        assertEquals("Invalid user.", exception.getMessage());
    }

    @Test
    void Should_ThrowInvalidRoleException_When_ReceivedRoleFromDataBaseIsEmpty() {
        User user = getSampleUser();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(roleRepository.findById(USER_ROLE_ID)).thenReturn(Optional.empty());
        InvalidRoleException exception = assertThrows(InvalidRoleException.class, () ->
                userRoleService.updateUserRole(USER_ID, USER_ROLE_ID));
        assertEquals("Invalid role.", exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GetAuth2TokenCollectionFromDataBase() {
        User user = getSampleUser();
        Role role = new Role(RoleType.USER);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(roleRepository.findById(USER_ROLE_ID)).thenReturn(Optional.of(role));
        when(jdbcTokenStore.findTokensByUserName(USER_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userRoleService.updateUserRole(USER_ID, USER_ROLE_ID));
        assertEquals("Getting token from database was failed", exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_SaveUserForUpdateUserRoleIsFailed() {
        User user = getSampleUser();
        Role role = new Role(RoleType.USER);
        OAuth2AccessToken auth2AccessToken = getSampleOAuth2AccessToken();
        Collection<OAuth2AccessToken> auth2AccessTokenCollection = new ArrayList<>();
        auth2AccessTokenCollection.add(auth2AccessToken);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(roleRepository.findById(USER_ROLE_ID)).thenReturn(Optional.of(role));
        when(jdbcTokenStore.findTokensByUserName(USER_ID)).thenReturn(auth2AccessTokenCollection);
        when(userRepository.save(user)).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userRoleService.updateUserRole(USER_ID, USER_ROLE_ID));
        assertEquals("Updating user role to database was failed", exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GetUserFromDataBaseIsFailed() {
        when(userRepository.findById(USER_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userRoleService.updateUserRole(USER_ID, USER_ROLE_ID));
        assertEquals("Getting user or role from database was failed", exception.getMessage());
    }

    /**
     * This method creates sample user
     *
     * @return User
     */
    private User getSampleUser() {
        User user = new User();
        user.setId(USER_ID);
        return user;
    }

    /**
     * This method creates sample OAuth2AccessToken
     *
     * @return OAuth2AccessToken
     */
    private OAuth2AccessToken getSampleOAuth2AccessToken() {
        return new DefaultOAuth2AccessToken(BASIC_TOKEN);
    }
}