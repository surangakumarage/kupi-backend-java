package com.kupi.cc.auth.service;

import com.kupi.cc.auth.domain.entity.*;
import com.kupi.cc.auth.domain.request.*;
import com.kupi.cc.auth.domain.response.BasicUserResponseDto;
import com.kupi.cc.auth.enums.ApprovalStatus;
import com.kupi.cc.auth.enums.Language;
import com.kupi.cc.auth.enums.RoleType;
import com.kupi.cc.auth.exception.*;
import com.kupi.cc.auth.repository.*;
import com.kupi.cc.auth.wrapper.GuestLoginResponseWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests the {@link UserService } class.
 */
class UserServiceTest {

    private static final String USER_EMAIL = "rengy@tokoin.io";
    private static final String USER_PASS = "Tokoin@123";
    private static final String USER_MOBILE_NO = "+62-81123123";
    private static final String USER_ID = "uid-1234567890";
    private static final String MERCHANT_ID = "uid-bd0c8894-8ddb-4b1d-8383-de44bfaa297f";
    private static final String IMAGE_URL = "http://localhost:8080/image.png";
    private static final String COUNTRY_CODE = "+62";
    private static final String USER_NAME = "Rendy Deraman";
    private static final String LOCAL_NUMBER = "81123123";
    private static final String ROLE_TYPE_USER = RoleType.USER.toString();
    private static final int TOKEN_EXPIRATION_TIME = 1;
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String FAILED = "failed";
    private static final int PAGE_NUMBER = 0;
    private static final int PAGE_SIZE = 250;
    private static final int ANY_INT = 100;
    private static final String CREATED_AT = "createdAt";
    private static final String O_AUTH_URL = "http://localhost:8081/oauth/token";
    private static final String BASIC_TOKEN = "changeme-auth-basic-token";
    private final UserUpdateRequestDto userUpdateRequestDto = getSampleUserUpdateRequestDto();
    private final User user = getSampleUser();
    private final ResetPasswordRequestDto resetPasswordRequestDto = getSampleResetPasswordRequestDto();
    @Mock
    PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JdbcTokenStore jdbcTokenStore;
    @Mock
    private OAuth2AccessToken oAuth2AccessToken;
    @Mock
    private UserMobileRepository userMobileRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private MerchantService merchantService;
    @Mock
    private MerchantBankSearchIndexRepository merchantBankSearchIndexRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ApprovedBankBusinessRepository approvedBankBusinessRepository;
    @Mock
    private ApiAccessKeyRepository apiAccessKeyRepository;
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CoreService coreService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        userService = new UserService(O_AUTH_URL, BASIC_TOKEN, userRepository, passwordEncoder, jdbcTokenStore,
                userMobileRepository, passwordResetTokenRepository, roleRepository, notificationService,
                merchantService, merchantBankSearchIndexRepository, approvedBankBusinessRepository,
                apiAccessKeyRepository, restTemplate, coreService);
    }

    @AfterEach
    void tearDown() {
    }

    /**
     * Create user
     */
    @Test
    void Should_SaveUser() {
        when(userRepository.save(user)).thenReturn(user);
        user.getRole().setId(RoleType.MERCHANT.getId());
        userService.createUser(user);
        verify(userRepository).save(user);
        verify(merchantBankSearchIndexRepository).save(any(MerchantBankSearchIndex.class));
    }

    @Test
    void Should_ThrowAuthServiceException_When_SavingUserIsFailed() {
        when(userRepository.save(user)).thenThrow(new DataAccessException(FAILED) {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.createUser(user));
        assertEquals("Saving user to database was failed", exception.getMessage());
    }

    /**
     * Getting user by user id
     */
    @Test
    void Should_ReturnUser_When_GettingUserByUserIdIsSuccessful() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        assertEquals(user, userService.getUserByUserId(USER_ID));
    }

    @Test
    void Should_ThrowInvalidUserException_When_GettingUserByUserIdForInvalidUser() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
        InvalidUserException exception = assertThrows(InvalidUserException.class, () ->
                userService.getUserByUserId(USER_ID));
        assertEquals("Invalid user id", exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GettingUserByUserIdIsFailed() {
        when(userRepository.findById(USER_ID)).thenThrow(new DataAccessException(FAILED) {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.getUserByUserId(USER_ID));
        assertEquals("Getting user from database was failed", exception.getMessage());
    }

    /**
     * Update user
     */
    @Test
    void Should_UpdateUser_When_UpdatingUserIsSuccessful() {
        when(userRepository.findById(userUpdateRequestDto.getUserId())).thenReturn(Optional.of(user));
        userUpdateRequestDto.setImageUrl(IMAGE_URL);
        when(userRepository.save(user)).thenReturn(user);
        userService.updateUser(userUpdateRequestDto);
        verify(userRepository).save(user);
    }

    @Test
    void Should_UpdateUserInMerchantBankSearchIndex_When_UpdatingUserIsSuccessful() {
        when(userRepository.findById(userUpdateRequestDto.getUserId())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        user.getRole().setId(RoleType.MERCHANT.getId());
        MerchantBankSearchIndex merchantBankSearchIndex = new MerchantBankSearchIndex();
        merchantBankSearchIndex.setUserId(USER_ID);
        when(merchantBankSearchIndexRepository.findById(USER_ID)).thenReturn(Optional.of(merchantBankSearchIndex));
        when(merchantBankSearchIndexRepository.save(merchantBankSearchIndex)).thenReturn(merchantBankSearchIndex);
        userService.updateUser(userUpdateRequestDto);
        verify(merchantBankSearchIndexRepository).save(merchantBankSearchIndex);
    }

    @Test
    void Should_ThrowAuthServiceException_When_UpdatingUserIsFailed() {
        when(userRepository.findById(userUpdateRequestDto.getUserId())).thenReturn(Optional.of(user));
        userUpdateRequestDto.setImageUrl(IMAGE_URL);
        when(userRepository.save(user)).thenThrow(new DataAccessException(FAILED) {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.updateUser(userUpdateRequestDto));
        assertEquals("Updating user to database was failed", exception.getMessage());
    }

    /**
     * Delete user
     */
    @Test
    void Should_DeleteUser() {
        when(userRepository.findByIdAndEnabledTrue(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        userService.deleteUser(USER_ID);
        verify(userRepository).save(user);
    }

    @Test
    void Should_ThrowInvalidUserException_When_DeletingUserForInvalidUser() {
        when(userRepository.findByIdAndEnabledTrue(USER_ID)).thenReturn(Optional.empty());
        InvalidUserException exception = assertThrows(InvalidUserException.class, () ->
                userService.deleteUser(USER_ID));
        assertEquals("User not found for userId: " + USER_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_DeletingUserIsFailed() {
        when(userRepository.findByIdAndEnabledTrue(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenThrow(new DataAccessException(FAILED) {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.deleteUser(USER_ID));
        assertEquals("Deleting user from db was failed for userId: " + USER_ID, exception.getMessage());
    }

    /**
     * Getting user by mobile no
     */
    @Test
    void Should_ReturnUser_When_GettingUserByMobileNoIsSuccessful() {
        when(userRepository.findByMobileNo(USER_MOBILE_NO)).thenReturn(user);
        assertEquals(user, userService.getUserByMobileNo(USER_MOBILE_NO));
    }

    @Test
    void Should_ThrowInvalidUserException_When_GettingUserByMobileNoForInvalidMobileNo() {
        when(userRepository.findByMobileNo(USER_MOBILE_NO)).thenReturn(null);
        InvalidUserException exception = assertThrows(InvalidUserException.class, () ->
                userService.getUserByMobileNo(USER_MOBILE_NO));
        assertEquals("Invalid mobile number", exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GettingUserByMobileNoIsFailed() {
        when(userRepository.findByMobileNo(USER_MOBILE_NO)).thenThrow(new DataAccessException(FAILED) {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.getUserByMobileNo(USER_MOBILE_NO));
        assertEquals("Getting user from database was failed", exception.getMessage());
    }

    /**
     * Create unregistered user
     */
    @Test
    void Should_CreateUnregisteredUser() {
        when(userRepository.findByMobileNo(user.getMobileNo())).thenReturn(null);
        when(userRepository.save(user)).thenReturn(user);
        userService.createUnregisteredUser(user);
        verify(userRepository).save(user);
    }

    @Test
    void Should_ThrowAuthServiceException_When_CreatingUnregisteredUserMobileNoExistenceCheckWasFailed() {
        when(userRepository.findByMobileNo(user.getMobileNo())).thenThrow(new DataAccessException(FAILED) {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.createUnregisteredUser(user));
        assertEquals("Checking mobile no existence was failed", exception.getMessage());
    }

    @Test
    void Should_ThrowMobileNoAlreadyExistsException_When_CreatingUnregisteredUserForExistingMobileNo() {
        when(userRepository.findByMobileNo(user.getMobileNo())).thenReturn(user);
        MobileNoAlreadyExistsException exception = assertThrows(MobileNoAlreadyExistsException.class, () ->
                userService.createUnregisteredUser(user));
        assertEquals("Already existing mobile number", exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_CreatingUnregisteredUserWasFailed() {
        when(userRepository.findByMobileNo(user.getMobileNo())).thenReturn(null);
        when(userRepository.save(user)).thenThrow(new DataAccessException(FAILED) {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.createUnregisteredUser(user));
        assertEquals("Saving user to database was failed", exception.getMessage());
    }

    /**
     * Update email
     */
    @Test
    void Should_UpdateEmail() {
        UserEmailUpdateRequestDto userEmailUpdateRequestDto = new UserEmailUpdateRequestDto();
        userEmailUpdateRequestDto.setUserId(USER_ID);
        userEmailUpdateRequestDto.setEmail("abc@ymail.com");
        user.setUpdatedAt(new Date());
        user.setEmail(null);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        userService.updateEmail(userEmailUpdateRequestDto);
        verify(userRepository).save(user);
    }

    @Test
    void Should_ThrowEmailAlreadyExistsException_When_UpdatingEmailForNullEmail() {
        UserEmailUpdateRequestDto userEmailUpdateRequestDto = new UserEmailUpdateRequestDto();
        userEmailUpdateRequestDto.setUserId(USER_ID);
        userEmailUpdateRequestDto.setEmail("abc@ymail.com");
        user.setEmail(null);
        when(userRepository.findById(userEmailUpdateRequestDto.getUserId())).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(userEmailUpdateRequestDto.getEmail())).thenReturn(user);
        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class, () ->
                userService.updateEmail(userEmailUpdateRequestDto));
        assertEquals("Already existing email address", exception.getMessage());
    }

    @Test
    void Should_ThrowEmailAlreadyExistsException_When_UpdatingEmailForExistingEmail() {
        UserEmailUpdateRequestDto userEmailUpdateRequestDto = new UserEmailUpdateRequestDto();
        userEmailUpdateRequestDto.setUserId(USER_ID);
        userEmailUpdateRequestDto.setEmail("abc@ymail.com");
        user.setEmail(USER_EMAIL);
        when(userRepository.findById(userEmailUpdateRequestDto.getUserId())).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(userEmailUpdateRequestDto.getEmail())).thenReturn(user);
        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class, () ->
                userService.updateEmail(userEmailUpdateRequestDto));
        assertEquals("Already existing email address", exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_UpdatingEmailWasFailed() {
        UserEmailUpdateRequestDto userEmailUpdateRequestDto = new UserEmailUpdateRequestDto();
        userEmailUpdateRequestDto.setUserId(USER_ID);
        userEmailUpdateRequestDto.setEmail("abc@ymail.com");
        user.setUpdatedAt(new Date());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenThrow(new DataAccessException(FAILED) {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.updateEmail(userEmailUpdateRequestDto));
        assertEquals("Updating email was failed", exception.getMessage());
    }

    /**
     * Update mobile no
     */
    @Test
    void Should_UpdateMobileNo() {
        MobileNoRequestDto mobileNo = new MobileNoRequestDto(COUNTRY_CODE, LOCAL_NUMBER);
        UserMobileUpdateRequestDto userMobileUpdateRequestDto = new UserMobileUpdateRequestDto();
        userMobileUpdateRequestDto.setUserId(USER_ID);
        userMobileUpdateRequestDto.setMobileNo(mobileNo);
        user.setMobileNo("+94-775263846");
        user.setUpdatedAt(new Date());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.findByMobileNo(mobileNo.getNo())).thenReturn(null);
        when(userRepository.save(user)).thenReturn(user);
        userService.updateMobileNo(userMobileUpdateRequestDto);
        verify(userRepository).save(user);
    }

    @Test
    void Should_ThrowInvalidUserException_When_UpdatingMobileNoForInvalidUser() {
        UserMobileUpdateRequestDto userMobileUpdateRequestDto = new UserMobileUpdateRequestDto();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
        InvalidUserException exception = assertThrows(InvalidUserException.class, () ->
                userService.updateMobileNo(userMobileUpdateRequestDto));
        assertEquals("Invalid user id", exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_UpdatingMobileNoForFailedToGetUser() {
        UserMobileUpdateRequestDto userMobileUpdateRequestDto = new UserMobileUpdateRequestDto();
        userMobileUpdateRequestDto.setUserId(USER_ID);
        when(userRepository.findById(USER_ID)).thenThrow(new DataAccessException(FAILED) {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.updateMobileNo(userMobileUpdateRequestDto));
        assertEquals("Getting user from database was failed", exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_UpdatingMobileNoForFailedToCheckMobileNo() {
        MobileNoRequestDto mobileNo = new MobileNoRequestDto(COUNTRY_CODE, LOCAL_NUMBER);
        UserMobileUpdateRequestDto userMobileUpdateRequestDto = new UserMobileUpdateRequestDto();
        userMobileUpdateRequestDto.setUserId(USER_ID);
        userMobileUpdateRequestDto.setMobileNo(mobileNo);
        user.setMobileNo("+94-775263846");
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.findByMobileNo(mobileNo.getNo())).thenThrow(new DataAccessException(FAILED) {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.updateMobileNo(userMobileUpdateRequestDto));
        assertEquals("Checking mobile no existence was failed", exception.getMessage());
    }

    @Test
    void Should_ThrowMobileNoAlreadyExistsException_When_UpdatingMobileNoForAlreadyExistingMobileNo() {
        MobileNoRequestDto mobileNo = new MobileNoRequestDto(COUNTRY_CODE, LOCAL_NUMBER);
        UserMobileUpdateRequestDto userMobileUpdateRequestDto = new UserMobileUpdateRequestDto();
        userMobileUpdateRequestDto.setUserId(USER_ID);
        userMobileUpdateRequestDto.setMobileNo(mobileNo);
        user.setMobileNo("+94-775263846");
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.findByMobileNo(mobileNo.getNo())).thenReturn(user);
        MobileNoAlreadyExistsException exception = assertThrows(MobileNoAlreadyExistsException.class, () ->
                userService.updateMobileNo(userMobileUpdateRequestDto));
        assertEquals("Already existing mobile no", exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_UpdatingMobileNoWasFailed() {
        MobileNoRequestDto mobileNo = new MobileNoRequestDto(COUNTRY_CODE, LOCAL_NUMBER);
        UserMobileUpdateRequestDto userMobileUpdateRequestDto = new UserMobileUpdateRequestDto();
        userMobileUpdateRequestDto.setUserId(USER_ID);
        userMobileUpdateRequestDto.setMobileNo(mobileNo);
        user.setMobileNo("+94-775263846");
        user.setUpdatedAt(new Date());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.findByMobileNo(mobileNo.getNo())).thenReturn(null);
        when(userRepository.save(user)).thenThrow(new DataAccessException(FAILED) {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.updateMobileNo(userMobileUpdateRequestDto));
        assertEquals("Updating mobileNo was failed", exception.getMessage());
    }

    /**
     * Save updated mobile no
     */
    @Test
    void Should_SaveUpdatedMobileNo() {
        MobileNoRequestDto mobileNo = new MobileNoRequestDto(COUNTRY_CODE, LOCAL_NUMBER);
        UserMobileUpdateRequestDto userMobileUpdateRequestDto = new UserMobileUpdateRequestDto();
        userMobileUpdateRequestDto.setUserId(USER_ID);
        userMobileUpdateRequestDto.setMobileNo(mobileNo);
        UserMobileNo userMobileNo = new UserMobileNo(userMobileUpdateRequestDto.getMobileNo().getNo(),
                userMobileUpdateRequestDto.getUserId());
        when(userMobileRepository.save(userMobileNo)).thenReturn(userMobileNo);
        userService.addUpdatedNumber(userMobileNo);
        verify(userMobileRepository).save(userMobileNo);
    }

    @Test
    void Should_ThrowAuthServiceException_When_SavingUpdatedMobileNoWasFailed() {
        MobileNoRequestDto mobileNo = new MobileNoRequestDto(COUNTRY_CODE, LOCAL_NUMBER);
        UserMobileUpdateRequestDto userMobileUpdateRequestDto = new UserMobileUpdateRequestDto();
        userMobileUpdateRequestDto.setUserId(USER_ID);
        userMobileUpdateRequestDto.setMobileNo(mobileNo);
        UserMobileNo userMobileNo = new UserMobileNo(userMobileUpdateRequestDto.getMobileNo().getNo(),
                userMobileUpdateRequestDto.getUserId());
        when(userMobileRepository.save(userMobileNo)).thenThrow(new DataAccessException(FAILED) {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.addUpdatedNumber(userMobileNo));
        assertEquals("Adding mobile number to user mobile table failed", exception.getMessage());
    }

    /**
     * Update password
     */
    @Test
    void Should_UpdatePassword() {
        UserPasswordUpdateRequestDto userPasswordUpdateRequestDto = new UserPasswordUpdateRequestDto();
        userPasswordUpdateRequestDto.setUserId(USER_ID);
        userPasswordUpdateRequestDto.setPassword(USER_PASS);
        user.setPassword(USER_PASS);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(USER_PASS, USER_PASS)).thenReturn(true);
        when(userRepository.save(user)).thenReturn(user);
        userService.updatePassword(userPasswordUpdateRequestDto);
        verify(userRepository).save(user);
    }

    @Test
    void Should_ThrowInvalidUserException_When_UpdatingPasswordForInvalidUser() {
        UserPasswordUpdateRequestDto userPasswordUpdateRequestDto = new UserPasswordUpdateRequestDto();
        userPasswordUpdateRequestDto.setUserId(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
        InvalidUserException exception = assertThrows(InvalidUserException.class, () ->
                userService.updatePassword(userPasswordUpdateRequestDto));
        assertEquals("Invalid user id", exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_UpdatingPasswordForFailedToGetUser() {
        UserPasswordUpdateRequestDto userPasswordUpdateRequestDto = new UserPasswordUpdateRequestDto();
        userPasswordUpdateRequestDto.setUserId(USER_ID);
        when(userRepository.findById(USER_ID)).thenThrow(new DataAccessException(FAILED) {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.updatePassword(userPasswordUpdateRequestDto));
        assertEquals("Getting user from database was failed", exception.getMessage());
    }

    @Test
    void Should_ThrowInvalidPasswordException_When_UpdatingPasswordForFailedToUpdatePassword() {
        UserPasswordUpdateRequestDto userPasswordUpdateRequestDto = new UserPasswordUpdateRequestDto();
        userPasswordUpdateRequestDto.setUserId(USER_ID);
        userPasswordUpdateRequestDto.setPassword(USER_PASS);
        user.setPassword(USER_PASS);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(USER_PASS, USER_PASS)).thenReturn(false);
        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class, () ->
                userService.updatePassword(userPasswordUpdateRequestDto));
        assertEquals("Updating password was failed", exception.getMessage());
    }

    @Test
    void Should_ThrowInvalidPasswordException_When_NewPasswordMatchWithOldPasswordForUpdatePassword() {
        UserPasswordUpdateRequestDto userPasswordUpdateRequestDto = new UserPasswordUpdateRequestDto();
        userPasswordUpdateRequestDto.setUserId(USER_ID);
        userPasswordUpdateRequestDto.setNewPassword(USER_PASS);
        user.setPassword(USER_PASS);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(USER_PASS, USER_PASS)).thenReturn(true);
        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class, () ->
                userService.updatePassword(userPasswordUpdateRequestDto));
        assertEquals("The new password must be different from your current password.", exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_UpdatingPasswordForFailedToUpdatePassword() {
        UserPasswordUpdateRequestDto userPasswordUpdateRequestDto = new UserPasswordUpdateRequestDto();
        userPasswordUpdateRequestDto.setUserId(USER_ID);
        userPasswordUpdateRequestDto.setPassword(USER_PASS);
        user.setPassword(USER_PASS);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(USER_PASS, USER_PASS)).thenReturn(true);
        doThrow(new DataAccessException(FAILED) {
        }).when(userRepository).save(user);
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.updatePassword(userPasswordUpdateRequestDto));
        assertEquals("Updating password was failed", exception.getMessage());
    }

    /**
     * Send password reset token
     */
    @Test
    void Should_SendPasswordResetToken() {
        user.setId(USER_ID);
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto(COUNTRY_CODE, LOCAL_NUMBER);
        when(userRepository.findByMobileNoOrEmailAndIsRegisteredUserTrue(mobileNoRequestDto.getNo(),
                mobileNoRequestDto.getNo())).thenReturn(user);
        PasswordResetToken existingToken = new PasswordResetToken();
        when(passwordResetTokenRepository.findByUserId(USER_ID)).thenReturn(existingToken);
        PasswordResetToken passwordResetToken = new PasswordResetToken(user);
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenReturn(passwordResetToken);

        userService.sendPasswordResetToken(mobileNoRequestDto);
        verify(passwordResetTokenRepository).delete(any(PasswordResetToken.class));
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(notificationService).sendPasswordResetSms(any(MobileNoRequestDto.class), any(String.class));
    }

    @Test
    void Should_ThrowInvalidUserException_When_SendingPasswordResetTokenForInvalidUser() {
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto(COUNTRY_CODE, LOCAL_NUMBER);
        when(userRepository.findByMobileNoOrEmailAndIsRegisteredUserTrue(mobileNoRequestDto.getNo(),
                mobileNoRequestDto.getNo())).thenReturn(null);
        InvalidUserException exception = assertThrows(InvalidUserException.class, () ->
                userService.sendPasswordResetToken(mobileNoRequestDto));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_SendingPasswordResetTokenForFailedToGetUser() {
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto(COUNTRY_CODE, LOCAL_NUMBER);
        when(userRepository.findByMobileNoOrEmailAndIsRegisteredUserTrue(mobileNoRequestDto.getNo(),
                mobileNoRequestDto.getNo())).thenThrow(new DataAccessException(FAILED) {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.sendPasswordResetToken(mobileNoRequestDto));
        assertEquals("Getting user from database was failed", exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_SendingPasswordResetTokenForFailedToAccessDBForPasswordResetToken() {
        user.setId(USER_ID);
        MobileNoRequestDto mobileNoRequestDto = new MobileNoRequestDto(COUNTRY_CODE, LOCAL_NUMBER);
        when(userRepository.findByMobileNoOrEmailAndIsRegisteredUserTrue(mobileNoRequestDto.getNo(),
                mobileNoRequestDto.getNo())).thenReturn(user);
        when(passwordResetTokenRepository.findByUserId(USER_ID)).thenThrow(new DataAccessException(FAILED) {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.sendPasswordResetToken(mobileNoRequestDto));
        assertEquals("Failed to access DB for password reset token.", exception.getMessage());
    }

    /**
     * Get user list
     */
    @Test
    void Should_ReturnUserList_When_GettingUserListIsSuccessful() {
        List<String> userIds = new ArrayList<>();
        userIds.add(USER_ID);
        List<User> users = new ArrayList<>();
        user.setCreatedAt(new Date());
        users.add(user);
        when(userRepository.findByIdInAndEnabledTrue(userIds)).thenReturn(users);
        List<BasicUserResponseDto> basicUserResponseDtoList = new ArrayList<>();
        for (User user : users)
            basicUserResponseDtoList.add(new BasicUserResponseDto(user, TIME_ZONE));
        assertEquals(basicUserResponseDtoList.get(0).getId(), userService.getUserList(userIds, TIME_ZONE).get(0).getId());
    }

    @Test
    void Should_ReturnEmptyUserList_When_GettingUserListForEmptyUserIds() {
        List<String> userIds = new ArrayList<>();
        when(userRepository.findByIdInAndEnabledTrue(userIds)).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(), userService.getUserList(userIds, TIME_ZONE));
    }

    @Test
    void Should_ThrowAuthServiceException_When_ErrorGettingUserList() {
        List<String> userIds = new ArrayList<>();
        userIds.add(USER_ID);
        when(userRepository.findByIdInAndEnabledTrue(userIds)).thenThrow(new DataAccessException(FAILED) {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.getUserList(userIds, TIME_ZONE));
        assertEquals("Error getting user list", exception.getMessage());
    }

    /**
     * Get update mobile no list
     */
    @Test
    void Should_ReturnUpdatedMobileNoListResponseDto_When_GettingUpdateMobileNoListIsSuccessful() {
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE, Sort.Direction.ASC, CREATED_AT);
        List<UserMobileNo> userMobileNoList = new ArrayList<>();
        userMobileNoList.add(new UserMobileNo(USER_MOBILE_NO, USER_ID));
        Page<UserMobileNo> userMobileNoPage = new PageImpl<>(userMobileNoList, pageable, userMobileNoList.size());
        when(userMobileRepository.findAll(pageable)).thenReturn(userMobileNoPage);
        doNothing().when(userMobileRepository).deleteAll(userMobileNoPage);
        assertEquals(USER_ID, userService.getUpdateMobileNoList(pageable).getUpdatedMobileNumbers().get(0).getUserId());
    }

    @Test
    void Should_ReturnEmptyUpdatedMobileNoListResponseDto_When_GettingUpdatedMobileNoForEmptyUserMobileNoList() {
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE, Sort.Direction.ASC, CREATED_AT);
        List<UserMobileNo> userMobileNoList = new ArrayList<>();
        Page<UserMobileNo> userMobileNoPage = new PageImpl<>(userMobileNoList, pageable, 1);
        when(userMobileRepository.findAll(pageable)).thenReturn(userMobileNoPage);
        doNothing().when(userMobileRepository).deleteAll(userMobileNoPage);
        assertEquals(Collections.emptyList(), userService.getUpdateMobileNoList(pageable).getUpdatedMobileNumbers());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GettingUpdateMobileNoListForFailedToGetUserMobileNo() {
        Pageable pageable = PageRequest.of(PAGE_NUMBER, PAGE_SIZE, Sort.Direction.ASC, CREATED_AT);
        when(userMobileRepository.findAll(pageable)).thenThrow(new DataAccessException(FAILED) {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.getUpdateMobileNoList(pageable));
        assertEquals("Getting updated mobile numbers failed", exception.getMessage());
    }

    /**
     * create guest user
     */
    @Test
    void Should_ReturnGuestLoginResponseWrapper_When_CreatingGuestUser() {
        GuestLoginResponseWrapper guestLoginResponseWrapper = new GuestLoginResponseWrapper();
        ResponseEntity<GuestLoginResponseWrapper> responseEntity = new ResponseEntity<>(guestLoginResponseWrapper,
                HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(GuestLoginResponseWrapper.class))).thenReturn(responseEntity);
        assertEquals(guestLoginResponseWrapper, userService.createGuestUser());
    }

    @Test
    void Should_ThrowAuthServiceException_When_CreatingGuestUserForFailingToSaveGuestUserToDatabase() {
        when(userRepository.save(any(User.class))).thenThrow(new DataAccessException(FAILED) {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.createGuestUser());
        assertEquals("Saving guest user to db was failed.", exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_CreatingGuestUserForCallingOAuthEndPointFailed() {
        HttpClientErrorException e = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                eq(GuestLoginResponseWrapper.class))).thenThrow(e);
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.createGuestUser());
        assertEquals("Calling oauth end point failed. Unable to login guest user. " + e, exception.getMessage());
    }

    /**
     * Reset password
     */
    @Test
    void Should_ResetPassword() {
        PasswordResetToken passwordResetToken = new PasswordResetToken(user);
        passwordResetToken.setExpiration(TOKEN_EXPIRATION_TIME);
        when(passwordResetTokenRepository.findByToken(resetPasswordRequestDto.getToken()))
                .thenReturn(passwordResetToken);
        assertEquals(user, userService.resetPassword(resetPasswordRequestDto));
    }

    @Test
    void Should_ThrowInvalidUserException_When_ResettingPasswordForInvalidUser() {
        when(passwordResetTokenRepository.findByToken(resetPasswordRequestDto.getToken()))
                .thenReturn(null);
        InvalidTokenException exception = assertThrows(InvalidTokenException.class, () ->
                userService.resetPassword(resetPasswordRequestDto));
        assertEquals("User Token not found", exception.getMessage());
    }

    @Test
    void Should_ThrowExpiredPasswordResetTokenException_When_ResettingPasswordForExpiredPasswordResetToken() {
        PasswordResetToken passwordResetToken = new PasswordResetToken(user);
        passwordResetToken.setExpiration(-TOKEN_EXPIRATION_TIME);
        when(passwordResetTokenRepository.findByToken(resetPasswordRequestDto.getToken()))
                .thenReturn(passwordResetToken);
        ExpiredPasswordResetTokenException exception = assertThrows(ExpiredPasswordResetTokenException.class, () ->
                userService.resetPassword(resetPasswordRequestDto));
        assertEquals("Password reset token was expired", exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_ResettingPasswordIsFailed() {
        PasswordResetToken passwordResetToken = new PasswordResetToken(user);
        passwordResetToken.setExpiration(TOKEN_EXPIRATION_TIME);
        when(passwordResetTokenRepository.findByToken(resetPasswordRequestDto.getToken()))
                .thenReturn(passwordResetToken);
        when(userRepository.save(user)).thenThrow(new DataAccessException(FAILED) {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.resetPassword(resetPasswordRequestDto));
        assertEquals("Reset password was failed", exception.getMessage());
    }

    /**
     * Get token by user id
     */
    @Test
    void Should_ReturnTokenCollection_When_GettingTokenByUserIdIsSuccessful() {
        Collection<OAuth2AccessToken> oAuth2AccessTokens = new ArrayList<>();
        oAuth2AccessTokens.add(oAuth2AccessToken);
        when(jdbcTokenStore.findTokensByUserName(USER_ID)).thenReturn(oAuth2AccessTokens);
        assertEquals(oAuth2AccessTokens, userService.getTokenByUserId(USER_ID));
    }

    @Test
    void Should_ThrowAuthServiceException_When_GettingTokenByUserIdIsFailed() {
        when(jdbcTokenStore.findTokensByUserName(USER_ID)).thenThrow(new DataAccessException(FAILED) {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.getTokenByUserId(USER_ID));
        assertEquals("Getting token from database was failed", exception.getMessage());
    }

    /**
     * Is email exist
     */
    @Test
    void Should_ReturnTrue_When_EmailIsExisted() {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(new User());
        assertTrue(userService.isEmailExist(USER_EMAIL));
    }

    @Test
    void Should_ReturnFalse_When_EmailIsNotExisted() {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(null);
        assertFalse(userService.isEmailExist(USER_EMAIL));
    }

    @Test
    void Should_ThrowException_When_GettingEmailIsFailed() {
        when(userRepository.findByEmail(USER_EMAIL)).thenThrow(new DataAccessException(FAILED) {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.isEmailExist(USER_EMAIL));
        assertEquals("Checking email existence was failed", exception.getMessage());
    }

    /**
     * Is already registered user
     */
    @Test
    void Should_ReturnTrue_When_UserIsAlreadyRegistered() {
        user.setRegisteredUser(true);
        when(userRepository.findByMobileNo(USER_MOBILE_NO)).thenReturn(user);
        assertTrue(userService.isAlreadyRegisteredUser(USER_MOBILE_NO));
    }

    @Test
    void Should_ReturnFalse_When_UserIsNotRegistered() {
        user.setRegisteredUser(false);
        when(userRepository.findByMobileNo(USER_MOBILE_NO)).thenReturn(user);
        assertFalse(userService.isAlreadyRegisteredUser(USER_MOBILE_NO));
    }

    @Test
    void Should_ReturnFalse_When_MobileNoNotExists() {
        when(userRepository.findByMobileNo(USER_MOBILE_NO)).thenReturn(null);
        assertFalse(userService.isAlreadyRegisteredUser(USER_MOBILE_NO));
    }

    /**
     * Get default role
     */
    @Test
    void Should_ReturnDefaultRole_When_GettingDefaultRoleIsSuccessful() {
        Role role = new Role(RoleType.USER);
        when(roleRepository.findByName(role.getName())).thenReturn(role);
        assertEquals(role, userService.getDefaultRole());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GettingDefaultRoleFailed() {
        Role role = new Role(RoleType.USER);
        when(roleRepository.findByName(role.getName())).thenThrow(new DataAccessException(FAILED) {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.getDefaultRole());
        assertEquals("Failed to get role", exception.getMessage());
    }

    /**
     * Is first time user email
     */
    @Test
    void Should_ReturnTrue_When_IsFirstTimeUserEmail() {
        user.setEmail(null);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        assertTrue(userService.isFirstTimeUserEmail(USER_ID));
    }

    @Test
    void Should_ReturnFalse_When_IsNotFirstTimeUserEmail() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        assertFalse(userService.isFirstTimeUserEmail(USER_ID));
    }

    /**
     * Get today summary
     */
    @Test
    void Should_ReturnTodaySummaryResponseDto_When_GettingTodaySummaryIsSuccessful() {
        when(userRepository.findById(anyString())).thenReturn(Optional.of(getSampleUser()));
        when(userRepository.countByRoleIdAndEnabledTrueAndCreatedAtGreaterThanEqual(anyInt(), any(Date.class)))
                .thenReturn(ANY_INT);
        when(userRepository.countByRoleIdAndEnabledTrue(anyInt())).thenReturn(ANY_INT);
        when(merchantService.getActiveMerchantCount()).thenReturn(ANY_INT);
        when(approvedBankBusinessRepository.getActiveBankCount()).thenReturn(ANY_INT);

        assertEquals(ANY_INT, userService.getTodaySummary(TIME_ZONE, USER_ID, RoleType.USER.toString())
                .getNoOfNewMerchants());
    }

    @Test
    void Should_ThrowInvalidUserException_When_GettingTodaySummaryForInvalidUserId() {
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());
        InvalidUserException exception = assertThrows(InvalidUserException.class, () -> userService
                .getTodaySummary(TIME_ZONE, USER_ID, ROLE_TYPE_USER));
        assertEquals("Invalid user id", exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GettingTodaySummaryForFailedToGetUserFromDB() {
        when(userRepository.findById(anyString())).thenThrow(new DataAccessException(FAILED) {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () -> userService
                .getTodaySummary(TIME_ZONE, USER_ID, ROLE_TYPE_USER));
        assertEquals("Getting user from database was failed", exception.getMessage());
    }

    @Test
    void Should_ThrowInvalidUserException_When_GettingTodaySummaryForUserIdDontMatchWithRoleType() {
        User user = getSampleUser();
        user.setRole(new Role(RoleType.MERCHANT));
        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
        InvalidUserException exception = assertThrows(InvalidUserException.class, () -> userService
                .getTodaySummary(TIME_ZONE, USER_ID, ROLE_TYPE_USER));
        assertEquals("User id didn't match with role type.", exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GettingTodaySummaryForFailedToGetActiveBankCount() {
        when(userRepository.findById(anyString())).thenReturn(Optional.of(getSampleUser()));
        when(userRepository.countByRoleIdAndEnabledTrueAndCreatedAtGreaterThanEqual(anyInt(), any(Date.class)))
                .thenReturn(ANY_INT);
        when(userRepository.countByRoleIdAndEnabledTrue(anyInt())).thenReturn(ANY_INT);
        when(merchantService.getActiveMerchantCount()).thenReturn(ANY_INT);
        when(approvedBankBusinessRepository.getActiveBankCount()).thenThrow(new DataAccessException(FAILED) {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () -> userService
                .getTodaySummary(TIME_ZONE, USER_ID, ROLE_TYPE_USER));
        assertEquals("Failed to get active banks count from the db.", exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GettingTodaySummaryIsFailed() {
        when(userRepository.findById(anyString())).thenReturn(Optional.of(getSampleUser()));
        when(userRepository.countByRoleIdAndEnabledTrueAndCreatedAtGreaterThanEqual(anyInt(), any(Date.class)))
                .thenThrow(new DataAccessException(FAILED) {
                });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () -> userService
                .getTodaySummary(TIME_ZONE, USER_ID, ROLE_TYPE_USER));
        assertEquals("Failed to get data from database for today's summary.", exception.getMessage());
    }

    @Test
    void Should_ReturnTrue_When_MerchantStatusIsPending() {
        User merchanat = getSampleUser();
        merchanat.setApprovalStatus(ApprovalStatus.PENDING);
        when(userRepository.findByMobileNoAsUserNameOrEmail(USER_NAME, USER_NAME)).thenReturn(merchanat);
        assertTrue(userService.isPendingMerchant(USER_NAME));
    }

    @Test
    void Should_ThrowAuthServiceException_When_GetUserFromDataBaseIsFailed() {
        when(userRepository.findByMobileNoAsUserNameOrEmail(USER_NAME, USER_NAME)).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.isPendingMerchant(USER_NAME));
        assertEquals("Getting user from database was failed", exception.getMessage());
    }

    @Test
    void Should_GenerateAccessKey() {
        Business business = getSampleBusiness();
        when(merchantService.getApprovedBusinessInfoByUserId(MERCHANT_ID, RoleType.MERCHANT)).thenReturn(business);
        userService.generateAccessKey(MERCHANT_ID);
        verify(apiAccessKeyRepository).save(any(ApiAccessKey.class));
    }

    @Test
    void Should_ThrowInvalidActionException_When_MerchantIsBlocked() {
        Business business = getSampleBusiness();
        business.getMerchant().setApprovalStatus(ApprovalStatus.BLOCKED);
        when(merchantService.getApprovedBusinessInfoByUserId(MERCHANT_ID, RoleType.MERCHANT)).thenReturn(business);
        InvalidActionException exception = assertThrows(InvalidActionException.class, () ->
                userService.generateAccessKey(MERCHANT_ID));
        assertEquals("Business profile not found or blocked for merchantId: " + MERCHANT_ID,
                exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_SaveAccessKeyIsFailed() {
        Business business = getSampleBusiness();
        when(merchantService.getApprovedBusinessInfoByUserId(MERCHANT_ID, RoleType.MERCHANT)).thenReturn(business);
        when(apiAccessKeyRepository.save(any(ApiAccessKey.class))).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.generateAccessKey(MERCHANT_ID));
        assertEquals("Failed to generate api access key for merchantId: " + MERCHANT_ID, exception.getMessage());
    }

    @Test
    void Should_ReturnAccessKey_When_MerchantIdIsProvided() {
        ApiAccessKey apiAccessKey = new ApiAccessKey();
        apiAccessKey.setUserId(MERCHANT_ID);
        apiAccessKey.setAccessKey(BASIC_TOKEN);
        when(apiAccessKeyRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(apiAccessKey));
        assertEquals(BASIC_TOKEN, userService.getApiAccessKey(MERCHANT_ID));
    }

    @Test
    void Should_ThrowInvalidUserException_When_InvalidMerchantIdIsProvided() {
        when(apiAccessKeyRepository.findById(MERCHANT_ID)).thenReturn(Optional.empty());
        InvalidUserException exception = assertThrows(InvalidUserException.class, () ->
                userService.getApiAccessKey(MERCHANT_ID));
        assertEquals("Access key not found for merchantId: " + MERCHANT_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowAuthServiceException_When_GetApiAccessKeyFromDataBaseIsFailed() {
        when(apiAccessKeyRepository.findById(MERCHANT_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        AuthServiceException exception = assertThrows(AuthServiceException.class, () ->
                userService.getApiAccessKey(MERCHANT_ID));
        assertEquals("Failed to get api access key from database for merchantId: " + MERCHANT_ID,
                exception.getMessage());
    }

    @Test
    void Should_ReturnFalse_When_AccessTokenIsNotExpired() {
        OAuth2AccessToken auth2AccessToken = getSampleOAuth2AccessToken();
        when(jdbcTokenStore.readAccessToken(BASIC_TOKEN)).thenReturn(auth2AccessToken);
        assertFalse(userService.readAccessToken(BASIC_TOKEN));

    }

    private ResetPasswordRequestDto getSampleResetPasswordRequestDto() {
        ResetPasswordRequestDto resetPasswordRequestDto = new ResetPasswordRequestDto();
        ReflectionTestUtils.setField(resetPasswordRequestDto, "password", USER_PASS);
        ReflectionTestUtils.setField(resetPasswordRequestDto, "token", "Tk-1");
        return resetPasswordRequestDto;
    }

    private UserUpdateRequestDto getSampleUserUpdateRequestDto() {
        UserUpdateRequestDto userUpdateRequestDto = new UserUpdateRequestDto();
        userUpdateRequestDto.setUserId(USER_ID);
        userUpdateRequestDto.setImageUrl(IMAGE_URL);
        userUpdateRequestDto.setFullName(USER_NAME);
        return userUpdateRequestDto;
    }

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
     * This method creates sample Business
     *
     * @return Business
     */
    private Business getSampleBusiness() {
        BusinessRequestDto businessRequestDto = getSampleBusinessRequestDto();
        User merchant = getSampleMerchant();
        return new Business(businessRequestDto, merchant);
    }

    /**
     * This method creates sample Merchant
     *
     * @return User
     */
    private User getSampleMerchant() {
        User merchant = new User();
        merchant.setId(MERCHANT_ID);
        merchant.setFullName("Full name");
        merchant.setEmail("merchant@kupi.lk");
        merchant.setMobileNo("94-771109101");
        merchant.setApprovalStatus(ApprovalStatus.PENDING);
        merchant.setRegisteredUser(true);
        merchant.setCreatedAt(new Date(System.currentTimeMillis()));
        Role role = new Role();
        role.setId(3);
        role.setName(RoleType.MERCHANT.name());
        merchant.setRole(role);
        return merchant;
    }

    /**
     * This method creates sample BusinessRequestDto
     *
     * @return BusinessRequestDto
     */
    private BusinessRequestDto getSampleBusinessRequestDto() {
        BusinessRequestDto businessRequestDto = new BusinessRequestDto();
        businessRequestDto.setMerchantId(MERCHANT_ID);
        businessRequestDto.setBusinessName("Singer");
        businessRequestDto.setOwnerName("Owner");
        businessRequestDto.setMobileNo(new MobileNoRequestDto("+94", "713321911"));
        businessRequestDto.setTelephone(new MobileNoRequestDto("+94", "72993391"));
        businessRequestDto.setImageUrl("https://www.singersl.com/image.jpg");
        businessRequestDto.setOpeningHours(new OpeningHoursRequestDto());
        return businessRequestDto;
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