package com.kupi.cc.auth.service;

import com.kupi.cc.auth.domain.entity.*;
import com.kupi.cc.auth.domain.request.*;
import com.kupi.cc.auth.domain.response.BasicUserResponseDto;
import com.kupi.cc.auth.domain.response.TodaySummaryResponseDto;
import com.kupi.cc.auth.domain.response.UpdatedMobileNoListResponseDto;
import com.kupi.cc.auth.domain.response.UpdatedMobileNoResponseDto;
import com.kupi.cc.auth.enums.ApprovalStatus;
import com.kupi.cc.auth.enums.ErrorResponseStatusType;
import com.kupi.cc.auth.enums.Language;
import com.kupi.cc.auth.enums.RoleType;
import com.kupi.cc.auth.exception.*;
import com.kupi.cc.auth.repository.*;
import com.kupi.cc.auth.wrapper.GuestLoginResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * User Service
 */
@Service
@Slf4j
public class UserService {

    public static final String EXISTING_EMAIL_ADDRESS = "Already existing email address";
    public static final String EXISTING_MOBILE_NUMBER = "Already existing mobile number";
    private static final String FAILED_GET_USER = "Getting user from database was failed";
    private static final String FAILED_UPDATE_USER = "Updating user to database was failed";
    private static final String FAILED_TO_UPDATE_EMAIL = "Updating email was failed";
    private static final String FAILED_TO_UPDATE_MOBILE = "Updating mobileNo was failed";
    private static final String FAILED_TO_UPDATE_PSW = "Updating password was failed";
    private static final String FAILED_TO_RESET_PSW = "Reset password was failed";
    private static final String FAILED_TO_GET_TOKEN = "Getting token from database was failed";
    private static final String FAILED_TO_ADD_USER_MOBILE_NO = "Adding mobile number to user mobile table failed";
    private static final String FAILED_TO_GET_USER_MOBILE_NO = "Getting updated mobile numbers failed";
    private static final String CREATED_AT = "createdAt";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String TOKEN_NOT_FOUND = "User Token not found";
    private static final String EXPIRED_PASSWORD_RESET_TOKEN = "Password reset token was expired";
    private static final String REPLACE_STRING = "XXXXX";
    private static final String GUEST_USERNAME_PREFIX = "G#";
    private static final String GUEST_PASS_PREFIX = "PW#";
    private static final String PASSWORD_EXISTS = "The new password must be different from your current password.";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTokenStore tokenStore;
    private final UserMobileRepository userMobileRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RoleRepository roleRepository;
    private final NotificationService notificationService;
    private final MerchantService merchantService;
    private final MerchantBankSearchIndexRepository merchantBankSearchIndexRepository;
    private final ApprovedBankBusinessRepository approvedBankBusinessRepository;
    private final ApiAccessKeyRepository apiAccessKeyRepository;
    private final RestTemplate restTemplate;
    private final String oauthUrl;
    private final String basicToken;
    private final CoreService coreService;

    @Autowired
    public UserService(@Value("${oauth.uri}") String oauthUrl,
                       @Value("${oauth.basicToken}") String basicToken,
                       UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JdbcTokenStore tokenStore, UserMobileRepository userMobileRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository, RoleRepository roleRepository,
                       NotificationService notificationService, MerchantService merchantService,
                       MerchantBankSearchIndexRepository merchantBankSearchIndexRepository,
                       ApprovedBankBusinessRepository approvedBankBusinessRepository,
                       ApiAccessKeyRepository apiAccessKeyRepository, RestTemplate restTemplate, CoreService coreService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenStore = tokenStore;
        this.userMobileRepository = userMobileRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.roleRepository = roleRepository;
        this.notificationService = notificationService;
        this.merchantService = merchantService;
        this.merchantBankSearchIndexRepository = merchantBankSearchIndexRepository;
        this.approvedBankBusinessRepository = approvedBankBusinessRepository;
        this.apiAccessKeyRepository = apiAccessKeyRepository;
        this.restTemplate = restTemplate;
        this.oauthUrl = oauthUrl;
        this.basicToken = basicToken;
        this.coreService = coreService;
    }

    /**
     * This method creates the user in the database.
     *
     * @param user user
     */
    public void createUser(User user) {
        try {
            userRepository.save(user);
            if (user.getRole().getId() == RoleType.USER.getId()) {
                coreService.assignDefaultSubscription(user.getId());
            }
            if (user.getRole().getId() == RoleType.MERCHANT.getId() || user.getRole().getId() == RoleType.BANK.getId()) {
                MerchantBankSearchIndex merchantBankSearchIndex = new MerchantBankSearchIndex(user);
                merchantBankSearchIndexRepository.save(merchantBankSearchIndex);
            }
        } catch (Exception e) {
            throw new AuthServiceException("Saving user to database was failed", e);
        }
    }

    /**
     * This method is used to create guest user.
     *
     * @return login response.
     */
    public GuestLoginResponseWrapper createGuestUser() {
        try {
            User user = new User();
            user.createGuestUser();
            String password = user.getMobileNoAsUserName().replace(GUEST_USERNAME_PREFIX, GUEST_PASS_PREFIX);
            userRepository.save(user);
            return login(user.getMobileNoAsUserName(), password);
        } catch (DataAccessException e) {
            throw new AuthServiceException("Saving guest user to db was failed.", e);
        }
    }

    /**
     * This method sends login request to oauth.
     *
     * @param username username
     * @param password password
     * @return login response.
     */
    public GuestLoginResponseWrapper login(String username, String password) {
        try {
            HttpHeaders headers = getAuthHeaders();
            MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
            multiValueMap.add("grant_type", "password");
            multiValueMap.add("username", username);
            multiValueMap.add("password", password);
            multiValueMap.add("user_role", RoleType.GUEST.toString());
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(multiValueMap, headers);
            log.debug("Calling oauth end point to login guest user. url: {}", oauthUrl);
            var result =
                    restTemplate.exchange(oauthUrl, HttpMethod.POST, entity, GuestLoginResponseWrapper.class);
            return result.getBody();
        } catch (HttpClientErrorException e) {
            throw new AuthServiceException("Calling oauth end point failed. Unable to login guest user. " + e);
        }
    }

    /**
     * This method sets headers for login request.
     *
     * @return login request headers.
     */
    private HttpHeaders getAuthHeaders() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(basicToken);
        return headers;
    }

    /**
     * This method returns the default role
     *
     * @return role
     */
    public Role getDefaultRole() {
        try {
            return roleRepository.findByName(RoleType.USER.name());
        } catch (DataAccessException e) {
            throw new AuthServiceException("Failed to get role", e);
        }
    }

    /**
     * This method creates an unregistered user
     *
     * @param user user
     */
    public void createUnregisteredUser(User user) {
        try {
            if (!isMobileNoExist(user.getMobileNo())) {
                userRepository.save(user);
            } else {
                throw new MobileNoAlreadyExistsException(EXISTING_MOBILE_NUMBER);
            }
        } catch (DataAccessException e) {
            throw new AuthServiceException("Saving user to database was failed", e);
        }
    }

    /**
     * This method checks the user is already registered with the given mobileNo.
     *
     * @param mobileNo mobileNo
     * @return true/false
     */
    public boolean isAlreadyRegisteredUser(String mobileNo) {
        if (isMobileNoExist(mobileNo)) {
            User user = userRepository.findByMobileNo(mobileNo);
            return user.isRegisteredUser();
        }
        return false;
    }

    /**
     * This method checks the existence of the given mobile no.
     *
     * @param mobileNo mobile no
     * @return true/false
     */
    public boolean isMobileNoExist(String mobileNo) {
        try {
            return userRepository.findByMobileNo(mobileNo) != null;
        } catch (DataAccessException e) {
            throw new AuthServiceException("Checking mobile no existence was failed", e);
        }
    }

    /**
     * This method returns existing user by mobile no.
     *
     * @param mobileNo mobileNo
     * @return user
     */
    public User getUserByMobileNo(String mobileNo) {

        try {
            User user = userRepository.findByMobileNo(mobileNo);
            if (user != null) {
                return user;
            } else {
                throw new InvalidUserException(ErrorResponseStatusType.INVALID_MOBILE_NO.getMessage());
            }
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_GET_USER, e);
        }
    }

    /**
     * This method returns any user (registered or guest) by username
     * Used for password grant authentication to allow both registered and guest users to login
     * Java 11 compatibility: matches behavior of guest user authentication
     *
     * @param userName userName (mobileNo, mobileNoAsUserName, or email)
     * @return user
     */
    public User getUser(String userName) {
        try {
            // Normalize phone number by removing spaces and hyphens to match stored format
            // Java 11 compatibility: handles phone numbers with spaces/hyphens in requests
            String normalizedUserName = userName;
            if (userName != null && userName.contains("+")) {
                // Phone number: remove spaces and hyphens
                normalizedUserName = userName.replaceAll("[\\s-]", "");
            }

            // Search by mobileNo, mobileNoAsUserName, or email (both registered and guest users)
            User user = userRepository.findUserByMobileNoOrMobileNoAsUserNameOrEmail(normalizedUserName);
            if (user != null) {
                return user;
            } else {
                throw new InvalidUserException(USER_NOT_FOUND);
            }
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_GET_USER, e);
        }
    }

    /**
     * This method returns existing registered user by mobile no
     *
     * @param userName userName
     * @return user
     */
    public User getRegisteredUser(String userName) {
        try {
            // Normalize phone number by removing spaces and hyphens to match stored format
            // Java 11 compatibility: handles phone numbers with spaces/hyphens in requests
            String normalizedUserName = userName;
            if (userName != null && userName.contains("+")) {
                // Phone number: remove spaces and hyphens
                normalizedUserName = userName.replaceAll("[\\s-]", "");
            }

            // Search by mobileNo, mobileNoAsUserName, or email for registered users
            User user = userRepository.findRegisteredUserByMobileNoOrMobileNoAsUserNameOrEmail(normalizedUserName);
            if (user != null) {
                return user;
            } else {
                throw new InvalidUserException(USER_NOT_FOUND);
            }
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_GET_USER, e);
        }
    }

    /**
     * This method returns existing user by user Id.
     *
     * @param userId userId
     * @return user
     */
    public User getUserByUserId(String userId) {
        try {
            Optional<User> user = userRepository.findById(userId);
            if (user.isPresent()) {
                return user.get();
            } else {
                throw new InvalidUserException(ErrorResponseStatusType.INVALID_USER_ID.getMessage());
            }
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_GET_USER, e);
        }
    }

    /**
     * This method checks the existence of the given email address.
     *
     * @param email email address
     * @return true/false
     */
    public boolean isEmailExist(String email) {
        try {
            return email != null && userRepository.findByEmail(email) != null;
        } catch (DataAccessException e) {
            throw new AuthServiceException("Checking email existence was failed", e);
        }
    }

    /**
     * This method updates existing user
     *
     * @param userUpdateRequestDto userUpdateRequestDto
     * @return user
     */
    public User updateUser(UserUpdateRequestDto userUpdateRequestDto) {
        try {
            User userByUserId = getUserByUserId(userUpdateRequestDto.getUserId());
            if (userUpdateRequestDto.getFullName() != null) {
                userByUserId.setFullName(userUpdateRequestDto.getFullName());
            }
            if (userUpdateRequestDto.getImageUrl() != null) {
                userByUserId.setImageUrl(userUpdateRequestDto.getImageUrl());
            }
            userByUserId.setUpdatedAt(new Date());
            userRepository.save(userByUserId);
            return updateMerchantBankSearchIndex(userByUserId);
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_UPDATE_USER, e);
        }
    }

    /**
     * This method is used to soft delete user.
     *
     * @param userId userId
     */
    public void deleteUser(String userId) {
        try {
            Optional<User> optionalUser = userRepository.findByIdAndEnabledTrue(userId);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                user.setEmail(REPLACE_STRING);
                user.setFullName(REPLACE_STRING);
                user.setImageUrl(REPLACE_STRING);
                user.setMobileNo(user.getMobileNo().replaceAll("-.*", REPLACE_STRING));
                user.setMobileNoAsUserName(REPLACE_STRING);
                user.setPassword(REPLACE_STRING);
                user.setEnabled(false);
                user.setRegisteredUser(false);
                user.setUpdatedAt(new Date());
                userRepository.save(user);
            } else
                throw new InvalidUserException("User not found for userId: " + userId);
        } catch (DataAccessException e) {
            throw new AuthServiceException("Deleting user from db was failed for userId: " + userId, e);
        }
    }

    /**
     * This method updates user in MerchantBankSearchIndex.
     *
     * @param user user
     * @return user
     */
    private User updateMerchantBankSearchIndex(User user) {
        if (user.getRole().getId() == RoleType.MERCHANT.getId() ||
                user.getRole().getId() == RoleType.BANK.getId()) {
            Optional<MerchantBankSearchIndex> merchantBankSearchIndex =
                    merchantBankSearchIndexRepository.findById(user.getId());
            if (merchantBankSearchIndex.isPresent()) {
                merchantBankSearchIndex.get().updateMerchant(user);
                merchantBankSearchIndexRepository.save(merchantBankSearchIndex.get());
            }
        }
        return user;
    }

    /**
     * This method updates existing user's email
     *
     * @param userEmailUpdateRequestDto userEmailUpdateRequestDto
     * @return user
     */
    public User updateEmail(UserEmailUpdateRequestDto userEmailUpdateRequestDto) {
        try {
            User userByUserId = getUserByUserId(userEmailUpdateRequestDto.getUserId());

            if (updateOnlyForNullEmail(userEmailUpdateRequestDto, userByUserId))
                throw new EmailAlreadyExistsException(EXISTING_EMAIL_ADDRESS);

            if (updateOnlyExistingEmail(userEmailUpdateRequestDto, userByUserId))
                throw new EmailAlreadyExistsException(EXISTING_EMAIL_ADDRESS);

            userByUserId.setUpdatedAt(new Date());
            userRepository.save(userByUserId);
            return userByUserId;
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_TO_UPDATE_EMAIL, e);
        }
    }

    /**
     * This method updates existing user's demoVideoWatched field
     *
     * @param userId userId
     * @param demoVideoWatched demoVideoWatched
     * @return user
     */
    public User updateDemoVideoWatched(String userId, boolean demoVideoWatched) {
        try {
            User userByUserId = getUserByUserId(userId);
            userByUserId.setDemoVideoWatched(demoVideoWatched);
            userByUserId.setUpdatedAt(new Date());
            userRepository.save(userByUserId);
            return userByUserId;
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_UPDATE_USER, e);
        }
    }

    /**
     * This method updates existing user's hasSeenEarlyBirdOffer field
     * Only available for mobile users (RoleType.USER)
     * This method only sets the field to true (one-way update)
     *
     * @param userId userId
     * @return user
     */
    public User updateEarlyBirdOfferSeen(String userId) {
        try {
            User userByUserId = getUserByUserId(userId);
            // Only allow for mobile users (RoleType.USER)
            if (!RoleType.USER.name().equalsIgnoreCase(userByUserId.getRole().getName())) {
                throw new InvalidUserException("This feature is only available for mobile users");
            }
            // Only set to true (one-way update)
            userByUserId.setHasSeenEarlyBirdOffer(true);
            userByUserId.setUpdatedAt(new Date());
            userRepository.save(userByUserId);
            return userByUserId;
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_UPDATE_USER, e);
        }
    }

    /**
     * Check the user updating the email for the first time
     *
     * @param userId userId
     * @return true/ false
     */
    public boolean isFirstTimeUserEmail(String userId) {
        User userByUserId = getUserByUserId(userId);
        return userByUserId.getEmail() == null;
    }

    /**
     * Read access token
     *
     * @param accessToken accessToken
     * @return true/false
     */
    public boolean readAccessToken(String accessToken) {
        return tokenStore.readAccessToken(accessToken).isExpired();
    }

    /**
     * This method updates mobile number in a existing user
     *
     * @param mobileUpdateRequestDto mobileUpdateRequestDto
     * @return user
     */
    public User updateMobileNo(UserMobileUpdateRequestDto mobileUpdateRequestDto) {
        try {
            User userByUserId = getUserByUserId(mobileUpdateRequestDto.getUserId());
            String mobileNo = mobileUpdateRequestDto.getMobileNo().getNo();

            if (!userByUserId.getMobileNo().equals(mobileNo)) {
                if (!isMobileNoExist(mobileNo)) {
                    userByUserId.setMobileNo(mobileNo);
                    userByUserId.setMobileNoAsUserName(mobileNo.replace("-", ""));
                } else {
                    throw new MobileNoAlreadyExistsException("Already existing mobile no");
                }
            }
            userByUserId.setUpdatedAt(new Date());
            userRepository.save(userByUserId);
            return userByUserId;
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_TO_UPDATE_MOBILE, e);
        }
    }

    /**
     * This method updates password of a existing user
     *
     * @param userPasswordUpdateRequestDto userPasswordUpdateRequestDto
     */
    public void updatePassword(UserPasswordUpdateRequestDto userPasswordUpdateRequestDto) {
        try {
            User userByUserId = getUserByUserId(userPasswordUpdateRequestDto.getUserId());
            if (passwordEncoder.matches(userPasswordUpdateRequestDto.getNewPassword(), userByUserId.getPassword()))
                throw new InvalidPasswordException(PASSWORD_EXISTS);
            if (passwordEncoder.matches(userPasswordUpdateRequestDto.getPassword(), userByUserId.getPassword())) {
                userByUserId.setPassword(passwordEncoder.encode(userPasswordUpdateRequestDto.getNewPassword()));
                userByUserId.setUpdatedAt(new Date());
                userRepository.save(userByUserId);
            } else {
                throw new InvalidPasswordException(FAILED_TO_UPDATE_PSW);
            }
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_TO_UPDATE_PSW, e);
        }
    }

    /**
     * Sent password reset link via sms service
     *
     * @param mobileNoRequestDto mobileNoRequestDto
     */
    public void sendPasswordResetToken(MobileNoRequestDto mobileNoRequestDto) {
        // Step 1: Get registered user by mobile number or email
        User user;
        String userName = mobileNoRequestDto.getNo();
        try {
            user = userRepository.findByMobileNoOrEmailAndIsRegisteredUserTrue(userName, userName);
        } catch (DataAccessException e) {
            throw new AuthServiceException("Getting user from database was failed", e);
        }

        if (user == null) {
            throw new InvalidUserException("User not found");
        }

        // Step 2: Manage password reset token and send SMS
        try {
            PasswordResetToken existingToken = passwordResetTokenRepository.findByUserId(user.getId());
            if (existingToken != null) {
                passwordResetTokenRepository.delete(existingToken);
            }
            PasswordResetToken passwordResetToken = new PasswordResetToken(user);
            passwordResetTokenRepository.save(passwordResetToken);
            notificationService.sendPasswordResetSms(mobileNoRequestDto, passwordResetToken.getToken());
        } catch (DataAccessException e) {
            throw new AuthServiceException("Failed to access DB for password reset token.", e);
        }
    }

    /**
     * This method reset the password of the user
     *
     * @param resetPasswordRequestDto resetPasswordRequestDto
     */
    public User resetPassword(ResetPasswordRequestDto resetPasswordRequestDto) {
        try {

            PasswordResetToken passwordResetToken =
                    passwordResetTokenRepository.findByToken(resetPasswordRequestDto.getToken());

            if (passwordResetToken == null) {
                throw new InvalidTokenException(TOKEN_NOT_FOUND);
            }

            if (isTokenExpired(passwordResetToken)) {
                throw new ExpiredPasswordResetTokenException(EXPIRED_PASSWORD_RESET_TOKEN); //request new token
            }

            User user = passwordResetToken.getUser();
            user.setPassword(passwordEncoder.encode(resetPasswordRequestDto.getPassword()));
            user.setUpdatedAt(new Date());
            userRepository.save(user);
            passwordResetTokenRepository.delete(passwordResetToken);
            return user;
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_TO_RESET_PSW, e);
        }
    }

    /**
     * Check token expiration
     *
     * @param passToken passToken
     * @return true/false
     */
    private boolean isTokenExpired(PasswordResetToken passToken) {
        final Calendar cal = Calendar.getInstance();
        return passToken.getExpiryDate().before(cal.getTime());
    }

    /**
     * Save updated mobile numbers
     *
     * @param userMobileNo userMobileNo
     */
    public void addUpdatedNumber(UserMobileNo userMobileNo) {
        try {
            userMobileRepository.save(userMobileNo);
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_TO_ADD_USER_MOBILE_NO, e);
        }
    }

    /**
     * Return the list of updated mobile numbers
     *
     * @param pageable pageable
     * @return updatedMobileNoListResponse
     */
    public UpdatedMobileNoListResponseDto getUpdateMobileNoList(Pageable pageable) {
        try {
            pageable = PageRequest.of(pageable.getPageNumber(),
                    pageable.getPageSize(), Sort.Direction.ASC, CREATED_AT);
            Page<UserMobileNo> updatedUserMobileNumbers = userMobileRepository.findAll(pageable);
            userMobileRepository.deleteAll(updatedUserMobileNumbers);
            List<UpdatedMobileNoResponseDto> updatedMobileNoList = new ArrayList<>();
            for (UserMobileNo userMobileNo : updatedUserMobileNumbers) {
                updatedMobileNoList.add(new UpdatedMobileNoResponseDto(userMobileNo));
            }
            return new UpdatedMobileNoListResponseDto(updatedMobileNoList, updatedUserMobileNumbers);
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_TO_GET_USER_MOBILE_NO, e);
        }
    }

    /**
     * This method returns bulk user list
     *
     * @param userId userId
     * @return List<BasicUserResponseDto>
     */
    public List<BasicUserResponseDto> getUserList(List<String> userId, String timeZone) {
        try {
            List<BasicUserResponseDto> basicUserResponseDtoList = new ArrayList<>();
            List<User> users = userRepository.findByIdInAndEnabledTrue(userId);
            for (User user : users) {
                basicUserResponseDtoList.add(new BasicUserResponseDto(user, timeZone));
            }
            return basicUserResponseDtoList;
        } catch (DataAccessException e) {
            throw new AuthServiceException("Error getting user list", e);
        }
    }


    /**
     * This method return the token of a user
     *
     * @param userId userId
     * @return OAuth2AccessToken
     */
    public Collection<OAuth2AccessToken> getTokenByUserId(String userId) {
        try {
            return tokenStore.findTokensByUserName(userId);
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_TO_GET_TOKEN, e);
        }
    }

    private boolean updateOnlyForNullEmail(UserEmailUpdateRequestDto userEmailUpdateRequestDto, User userByUserId) {
        if (userByUserId.getEmail() == null && userEmailUpdateRequestDto.getEmail() != null) {
            if (!isEmailExist(userEmailUpdateRequestDto.getEmail())) {
                userByUserId.setEmail(userEmailUpdateRequestDto.getEmail());
            } else {
                return true;
            }
        }
        return false;
    }

    private boolean updateOnlyExistingEmail(UserEmailUpdateRequestDto userEmailUpdateRequestDto, User userByUserId) {
        if (userEmailUpdateRequestDto.getEmail() == null
                || !userByUserId.getEmail().equals(userEmailUpdateRequestDto.getEmail())) {
            if (!isEmailExist(userEmailUpdateRequestDto.getEmail())) {
                userByUserId.setEmail(userEmailUpdateRequestDto.getEmail());
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * This method checks if a merchant status is pending.
     *
     * @param userName mobileNo/ email
     * @return true/false
     */
    public boolean isPendingMerchant(String userName) {
        try {
            User user = userRepository.findByMobileNoAsUserNameOrEmail(userName, userName);
            return user != null && user.getApprovalStatus() == ApprovalStatus.PENDING;
        } catch (DataAccessException e) {
            throw new AuthServiceException(FAILED_GET_USER, e);
        }
    }

    /**
     * This method is used to get active banks count.
     *
     * @return active banks count.
     */
    private int getActiveBankCount() {
        try {
            return approvedBankBusinessRepository.getActiveBankCount();
        } catch (DataAccessException e) {
            throw new AuthServiceException("Failed to get active banks count from the db.", e);
        }
    }

    /**
     * This method is used to get today's summary.
     *
     * @param timeZone timeZone
     * @return today's summary.
     */
    public TodaySummaryResponseDto getTodaySummary(String timeZone, String userId, String roleType) {
        User user = getUserByUserId(userId);
        if (!user.getRole().getName().equalsIgnoreCase(roleType))
            throw new InvalidUserException("User id didn't match with role type.");
        LocalDate systemLocalDate = LocalDate.now(ZoneId.of(timeZone));
        Date today = Date.from(systemLocalDate.atStartOfDay(ZoneId.of(timeZone)).toInstant());
        try {
            int noOfNewMerchants = userRepository
                    .countByRoleIdAndEnabledTrueAndCreatedAtGreaterThanEqual(RoleType.MERCHANT.getId(), today);
            int totalMerchants = userRepository.countByRoleIdAndEnabledTrue(RoleType.MERCHANT.getId());
            int totalActiveMerchants = merchantService.getActiveMerchantCount();
            int noOfNewMobileUsers = userRepository
                    .countByRoleIdAndEnabledTrueAndCreatedAtGreaterThanEqual(RoleType.USER.getId(), today);
            int totalMobileUsers = userRepository.countByRoleIdAndEnabledTrue(RoleType.USER.getId());
            int noOfNewBanks = userRepository
                    .countByRoleIdAndEnabledTrueAndCreatedAtGreaterThanEqual(RoleType.BANK.getId(), today);
            int totalBanks = userRepository.countByRoleIdAndEnabledTrue(RoleType.BANK.getId());
            int totalActiveBanks = getActiveBankCount();
            return new TodaySummaryResponseDto(noOfNewMerchants, totalMerchants, totalActiveMerchants,
                    noOfNewMobileUsers, totalMobileUsers, noOfNewBanks, totalBanks, totalActiveBanks);
        } catch (DataAccessException e) {
            throw new AuthServiceException("Failed to get data from database for today's summary.", e);
        }
    }

    /**
     * This method is used to generate api access key for merchants.
     *
     * @param merchantId merchantId
     */
    public void generateAccessKey(String merchantId) {
        try {
            Business businessProfile = merchantService.getApprovedBusinessInfoByUserId(merchantId, RoleType.MERCHANT);
            if (businessProfile == null || businessProfile.getMerchant().getApprovalStatus() == ApprovalStatus.BLOCKED)
                throw new InvalidActionException("Business profile not found or blocked for merchantId: " + merchantId);
            ApiAccessKey apiAccessKey = new ApiAccessKey(merchantId);
            apiAccessKeyRepository.save(apiAccessKey);
        } catch (DataAccessException e) {
            throw new AuthServiceException("Failed to generate api access key for merchantId: " + merchantId);
        }
    }

    /**
     * This method is used to get api access key for merchantId.
     *
     * @param merchantId merchantId
     * @return access key.
     */
    public String getApiAccessKey(String merchantId) {
        try {
            ApiAccessKey apiAccessKey = apiAccessKeyRepository.findById(merchantId).orElseThrow(() ->
                    new InvalidUserException("Access key not found for merchantId: " + merchantId));
            return apiAccessKey.getAccessKey();
        } catch (DataAccessException e) {
            throw new AuthServiceException("Failed to get api access key from database for merchantId: " + merchantId);
        }
    }

    /**
     * Create user from OTP registration data.
     *
     * @param otpUserRegistrationDto OTP registration request DTO
     * @param timeZone user's time zone
     * @return created User entity
     * @throws AuthServiceException if user creation fails
     */
    public User createOTPUser(OTPUserRegistrationDto otpUserRegistrationDto, String timeZone) {
        try {
            User user = new User();
            user.setId("uid-" + java.util.UUID.randomUUID());
            user.setFullName(otpUserRegistrationDto.getFullName().trim());
            user.setEmail(otpUserRegistrationDto.getEmail() != null && !otpUserRegistrationDto.getEmail().isEmpty() ?
                    otpUserRegistrationDto.getEmail().trim() : null);
            // Generate random password if not provided (OTP-based registration)
            String password = otpUserRegistrationDto.getPassword() != null && !otpUserRegistrationDto.getPassword().isEmpty() ?
                    otpUserRegistrationDto.getPassword() : java.util.UUID.randomUUID().toString();
            user.setPassword(passwordEncoder.encode(password));
            user.setMobileNo(otpUserRegistrationDto.getMobileNo().getNo());
            user.setMobileNoAsUserName(otpUserRegistrationDto.getMobileNo().getNo().replace("-", ""));
            user.setLanguage(Language.getLanguage(otpUserRegistrationDto.getLanguage()));
            user.setRole(new Role(otpUserRegistrationDto.getRoleType()));
            user.setRegisteredUser(true);
            user.setEnabled(true);
            user.setCreatedAt(new Date());
            user.setUpdatedAt(new Date());
            user.setApprovalStatus(ApprovalStatus.APPROVED);
            user.setEmailVerified(false);
            user.setMobileVerified(true);

            userRepository.save(user);

            // Assign default subscription for user role
            if (user.getRole().getId() == RoleType.USER.getId()) {
                coreService.assignDefaultSubscription(user.getId());
            }
            // Create search index for merchant/bank roles
            if (user.getRole().getId() == RoleType.MERCHANT.getId() || user.getRole().getId() == RoleType.BANK.getId()) {
                MerchantBankSearchIndex merchantBankSearchIndex = new MerchantBankSearchIndex(user);
                merchantBankSearchIndexRepository.save(merchantBankSearchIndex);
            }

            log.debug("Created OTP user: {}", user.getId());
            return user;
        } catch (Exception e) {
            log.error("Failed to create OTP user", e);
            throw new AuthServiceException("Creating OTP user was failed", e);
        }
    }
}
