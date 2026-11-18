package com.kupi.notification.service;

import com.kupi.notification.domain.entity.AuthUser;
import com.kupi.notification.domain.entity.NotificationDetail;
import com.kupi.notification.domain.entity.UserNotification;
import com.kupi.notification.domain.request.*;
import com.kupi.notification.enums.NotificationStatus;
import com.kupi.notification.enums.NotificationType;
import com.kupi.notification.exception.InvalidRecipientException;
import com.kupi.notification.exception.InvalidUserException;
import com.kupi.notification.exception.NotificationServiceException;
import com.kupi.notification.repository.NotificationRepository;
import com.kupi.notification.repository.UserNotificationRepository;
import com.kupi.notification.util.TokenEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests {@link NotificationService} class
 */
class NotificationServiceTest {

    private static final String USER_ID = "uid-1";
    private static final String USER_NOTIFICATION_ID = "unid-12568-78544-7954";
    private static final String DEVICE_ID = "DEVICE-ID";
    private static final String NOTIFICATION_TOKEN = "NOTIFICATION-TOKEN";
    private static final String ENCODED_TOKEN = "ENCODED-TOKEN";
    private static final String DECODED_VALUE = "DECODED-VALUE";
    private static final String CREATED_AT = "createdAt";
    private static final int PAGE = 0;
    private static final int SIZE = 1;
    private static final int UNREAD_NOTIFICATION_COUNT = 1;
    private static final String ACCESS_TOKEN = "Bearer192f0c96-dd19-404e-8217-a8fd1cc81d55";
    private static final String TIME_ZONE = "Asia/Colombo";
    private final Pageable defaultPageable = getDefaultPageable();
    private NotificationService notificationService;
    @Mock
    private AuthUserService authUserService;
    @Mock
    private UserNotificationRepository fireBaseUserTokenRepository;
    @Mock
    private TokenEncoder tokenEncoder;
    @Mock
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        initMocks(this);
        notificationService = new NotificationService(authUserService, fireBaseUserTokenRepository,
                tokenEncoder, notificationRepository);
    }

    @Test
    void should_throwException_when_UpdatingNotificationReadStatus() {
        when(notificationRepository.findByUserIdAndIsDeletedFalse(anyString(), any(Pageable.class)))
                .thenThrow(new DataAccessException("failed") {
                });
        NotificationServiceException exception = assertThrows(NotificationServiceException.class, () ->
                notificationService.getNotificationItems(USER_ID, defaultPageable));
        assertEquals("Failed to get notifications", exception.getMessage());
    }

    @Test
    void Should_Return_UpdatedMobileNoList() {
        Page<NotificationDetail> page = new PageImpl<>(Collections.singletonList(getSampleNotificationDetail()),
                defaultPageable, 1);
        when(notificationRepository.findByUserIdAndIsDeletedFalse(USER_ID, defaultPageable)).thenReturn(page);
        notificationService.getNotificationItems(USER_ID, defaultPageable);
        verify(notificationRepository).findByUserIdAndIsDeletedFalse(anyString(), any(Pageable.class));
    }

    @Test
    void should_AddToken() {
        AddTokenRequestDto addTokenRequestDto = getSampleAddTokenRequestDto();
        AuthUser authUser = getSampleAuthUser();
        when(authUserService.getUserByUserId(USER_ID, ACCESS_TOKEN, TIME_ZONE)).thenReturn(authUser);
        when(fireBaseUserTokenRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(tokenEncoder.encode(NOTIFICATION_TOKEN)).thenReturn(ENCODED_TOKEN);
        notificationService.addToken(USER_ID, ACCESS_TOKEN, NOTIFICATION_TOKEN, addTokenRequestDto, TIME_ZONE);
        verify(fireBaseUserTokenRepository).save(any(UserNotification.class));
    }

    @Test
    void should_UpdateToken_When_UserNotificationAlreadyExists() {
        AddTokenRequestDto addTokenRequestDto = getSampleAddTokenRequestDto();
        AuthUser authUser = getSampleAuthUser();
        UserNotification userNotification = getSampleUserNotification();
        when(authUserService.getUserByUserId(USER_ID, ACCESS_TOKEN, TIME_ZONE)).thenReturn(authUser);
        when(fireBaseUserTokenRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userNotification));
        when(tokenEncoder.encode(NOTIFICATION_TOKEN)).thenReturn(ENCODED_TOKEN);
        notificationService.addToken(USER_ID, ACCESS_TOKEN, NOTIFICATION_TOKEN, addTokenRequestDto, TIME_ZONE);
        verify(fireBaseUserTokenRepository).save(userNotification);
    }

    @Test
    void Should_ThrowInvalidUserException_When_InvalidUserIdIsProvidedForAddToken() {
        AddTokenRequestDto addTokenRequestDto = getSampleAddTokenRequestDto();
        when(authUserService.getUserByUserId(USER_ID, ACCESS_TOKEN, TIME_ZONE)).thenReturn(null);
        InvalidUserException invalidUserException = assertThrows(InvalidUserException.class, () ->
                notificationService.addToken(USER_ID, ACCESS_TOKEN, NOTIFICATION_TOKEN, addTokenRequestDto, TIME_ZONE));
        assertEquals("Invalid userId: " + USER_ID, invalidUserException.getMessage());
    }

    @Test
    void Should_ThrowNotificationServiceException_When_ValidateAuthUserIsFailedForAddToken() {
        AddTokenRequestDto addTokenRequestDto = getSampleAddTokenRequestDto();
        when(authUserService.getUserByUserId(USER_ID, ACCESS_TOKEN, TIME_ZONE))
                .thenThrow(new DataAccessException("ERROR") {
                });
        NotificationServiceException notificationServiceException = assertThrows(NotificationServiceException.class, () ->
                notificationService.addToken(USER_ID, ACCESS_TOKEN, NOTIFICATION_TOKEN, addTokenRequestDto, TIME_ZONE));
        assertEquals("Validating user with auth service was failed.",
                notificationServiceException.getMessage());
    }

    @Test
    void Should_ThrowNotificationServiceException_When_SaveUserNotificationInToDataBaseIsFailedForAddToken() {
        AddTokenRequestDto addTokenRequestDto = getSampleAddTokenRequestDto();
        AuthUser authUser = getSampleAuthUser();
        when(authUserService.getUserByUserId(USER_ID, ACCESS_TOKEN, TIME_ZONE)).thenReturn(authUser);
        when(fireBaseUserTokenRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(tokenEncoder.encode(NOTIFICATION_TOKEN)).thenReturn(ENCODED_TOKEN);
        when(fireBaseUserTokenRepository.save(any(UserNotification.class)))
                .thenThrow(new DataAccessException("ERROR") {
                });
        NotificationServiceException notificationServiceException = assertThrows(NotificationServiceException.class, () ->
                notificationService.addToken(USER_ID, ACCESS_TOKEN, NOTIFICATION_TOKEN, addTokenRequestDto, TIME_ZONE));
        assertEquals("Saving notification token to database was failed.",
                notificationServiceException.getMessage());
    }

    @Test
    void Should_RemoveToken() {
        AuthUser authUser = getSampleAuthUser();
        when(authUserService.getUserByUserId(USER_ID, ACCESS_TOKEN, TIME_ZONE)).thenReturn(authUser);
        notificationService.removeToken(USER_ID, ACCESS_TOKEN, TIME_ZONE);
        verify(fireBaseUserTokenRepository).deleteNotificationTokenForUserId(any(Date.class), anyString());
    }

    @Test
    void Should_ThrowInvalidUserException_When_InvalidUserIdIsProvidedForRemoveToken() {
        when(authUserService.getUserByUserId(USER_ID, ACCESS_TOKEN, TIME_ZONE)).thenReturn(null);
        InvalidUserException invalidUserException = assertThrows(InvalidUserException.class, () ->
                notificationService.removeToken(USER_ID, ACCESS_TOKEN, TIME_ZONE));
        assertEquals("Invalid userId: " + USER_ID, invalidUserException.getMessage());
    }

    @Test
    void Should_ThrowNotificationServiceException_When_ValidateAuthUserIsFailedForRemoveToken() {
        when(authUserService.getUserByUserId(USER_ID, ACCESS_TOKEN, TIME_ZONE))
                .thenThrow(new DataAccessException("ERROR") {
                });
        NotificationServiceException notificationServiceException = assertThrows(NotificationServiceException.class, () ->
                notificationService.removeToken(USER_ID, ACCESS_TOKEN, TIME_ZONE));
        assertEquals("Validating user with auth service was failed.",
                notificationServiceException.getMessage());
    }

    @Test
    void Should_ThrowNotificationServiceException_When_SaveUserNotificationInToDataBaseIsFailedForRemoveToken() {
        AuthUser authUser = getSampleAuthUser();
        when(authUserService.getUserByUserId(USER_ID, ACCESS_TOKEN, TIME_ZONE)).thenReturn(authUser);
        doThrow(new DataAccessException("ERROR") {
        }).when(fireBaseUserTokenRepository).deleteNotificationTokenForUserId(any(Date.class), anyString());
        NotificationServiceException notificationServiceException = assertThrows(NotificationServiceException.class, () ->
                notificationService.removeToken(USER_ID, ACCESS_TOKEN, TIME_ZONE));
        assertEquals("Removing notification token from database was failed.",
                notificationServiceException.getMessage());
    }

    @Test
    void Should_UpdateNotificationReadStatus() {
        List<String> notificationIds = new ArrayList<>();
        notificationIds.add(USER_NOTIFICATION_ID);
        List<NotificationDetail> notificationDetailList = getSampleNotificationDetailList();
        when(notificationRepository.findAllById(notificationIds)).thenReturn(notificationDetailList);
        notificationService.updateNotificationReadStatus(notificationIds);
        verify(notificationRepository).save(any(NotificationDetail.class));
    }

    @Test
    void Should_ThrowNotificationServiceException_When_GetNotificationDetailsListFromDataBaseIsFailed() {
        List<String> notificationIds = new ArrayList<>();
        notificationIds.add(USER_NOTIFICATION_ID);
        when(notificationRepository.findAllById(notificationIds)).thenThrow(new DataAccessException("ERROR") {
        });
        NotificationServiceException notificationServiceException = assertThrows(NotificationServiceException.class, () ->
                notificationService.updateNotificationReadStatus(notificationIds));
        assertEquals("Failed to get notification details", notificationServiceException.getMessage());
    }

    @Test
    void Should_ThrowNotificationServiceException_When_UpdateNotificationDetailsIntoDataBaseIsFailed() {
        List<String> notificationIds = new ArrayList<>();
        notificationIds.add(USER_NOTIFICATION_ID);
        List<NotificationDetail> notificationDetailList = getSampleNotificationDetailList();
        when(notificationRepository.findAllById(notificationIds)).thenReturn(notificationDetailList);
        when(notificationRepository.save(any(NotificationDetail.class))).thenThrow(new DataAccessException("ERROR") {
        });
        NotificationServiceException notificationServiceException = assertThrows(NotificationServiceException.class, () ->
                notificationService.updateNotificationReadStatus(notificationIds));
        assertEquals("Failed to mark status", notificationServiceException.getMessage());
    }

    @Test
    void Should_ReturnNotificationDetailsPage_When_ValidUserIdIsProvidedForGetNotificationItems() {
        List<NotificationDetail> notificationDetailList = getSampleNotificationDetailList();
        Page<NotificationDetail> notificationDetailPage = new PageImpl<>(notificationDetailList);
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(CREATED_AT).descending());
        when(notificationRepository.findByUserIdAndIsDeletedFalse(USER_ID, pageable)).thenReturn(notificationDetailPage);
        assertEquals(notificationDetailPage, notificationService.getNotificationItems(USER_ID, pageable));
    }

    @Test
    void Should_ThrowNotificationServiceException_When_GetNotificationDetailPageFromDataBaseIsFailed() {
        Pageable pageable = getDefaultPageable();
        when(notificationRepository.findByUserIdAndIsDeletedFalse(USER_ID, pageable))
                .thenThrow(new DataAccessException("ERROR") {
                });
        NotificationServiceException notificationServiceException = assertThrows(NotificationServiceException.class, () ->
                notificationService.getNotificationItems(USER_ID, pageable));
        assertEquals("Failed to get notifications", notificationServiceException.getMessage());
    }

    @Test
    void Should_ReturnUnreadNotificationCount_When_ValidUserIdIsProvidedForGetUnreadNotificationCountByUserId() {
        when(notificationRepository.countByUserIdAndIsReadFalseAndIsDeletedFalse(USER_ID))
                .thenReturn(UNREAD_NOTIFICATION_COUNT);
        assertEquals(UNREAD_NOTIFICATION_COUNT, notificationService.getUnreadNotificationCountByUserId(USER_ID));
    }

    @Test
    void Should_ThrowNotificationServiceException_When_GetUnreadNotificationCountFromDataBaseIsFailed() {
        when(notificationRepository.countByUserIdAndIsReadFalseAndIsDeletedFalse(USER_ID))
                .thenThrow(new DataAccessException("ERROR") {
                });
        NotificationServiceException notificationServiceException = assertThrows(NotificationServiceException.class, () ->
                notificationService.getUnreadNotificationCountByUserId(USER_ID));
        assertEquals("Getting unread notification count from database was failed for userId: " + USER_ID,
                notificationServiceException.getMessage());
    }

    @Test
    void Should_DeleteNotification() {
        List<String> notificationIds = getSampleNotificationIds();
        notificationService.deleteNotifications(notificationIds);
        verify(notificationRepository, times(1)).setNotificationAsDeleted(notificationIds);
    }

    @Test
    void Should_ThrowNotificationServiceException_When_DeleteNotificationFromDataBaseIsFailed() {
        List<String> notificationIds = getSampleNotificationIds();
        doThrow(new DataAccessException("ERROR") {
        }).when(notificationRepository).setNotificationAsDeleted(notificationIds);
        NotificationServiceException notificationServiceException = assertThrows(NotificationServiceException.class, () ->
                notificationService.deleteNotifications(notificationIds));
        assertEquals("Failed to delete notifications.", notificationServiceException.getMessage());
    }

    @Test
    void Should_EnableOrDisableNotification() {
        notificationService.enableOrDisableNotification(USER_ID, NotificationStatus.ENABLE);
        verify(fireBaseUserTokenRepository).updateNotificationStatusForUserId(true, USER_ID);
    }

    @Test
    void Should_ThrowNotificationServiceException_When_UpdateNotificationStatusIntoDataBaseIsFailed() {
        doThrow(new DataAccessException("ERROR") {
        }).when(fireBaseUserTokenRepository).updateNotificationStatusForUserId(true, USER_ID);
        NotificationServiceException notificationServiceException = assertThrows(NotificationServiceException.class, () ->
                notificationService.enableOrDisableNotification(USER_ID, NotificationStatus.ENABLE));
        assertEquals("Failed to " + NotificationStatus.ENABLE.name() + " notification for userId: " + USER_ID,
                notificationServiceException.getMessage());
    }

    @Test
    void Should_ReturnNotificationStatus_When_ValidUserIdIsProvidedForIsNotificationEnabledOrDisabled() {
        UserNotification userNotification = getSampleUserNotification();
        userNotification.setNotificationEnabled(true);
        when(fireBaseUserTokenRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userNotification));
        assertTrue(notificationService.isNotificationEnabledOrDisabled(USER_ID));
    }

    @Test
    void Should_ReturnInvalidRecipientException_When_InvalidUserIdIsProvidedForIsNotificationEnabledOrDisabled() {
        when(fireBaseUserTokenRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        InvalidRecipientException invalidRecipientException = assertThrows(InvalidRecipientException.class, () ->
                notificationService.isNotificationEnabledOrDisabled(USER_ID));
        assertEquals("Invalid recipientId.", invalidRecipientException.getMessage());
    }

    @Test
    void Should_ThrowNotificationServiceException_When_GetUserNotificationFromDataBaseIsFailed() {
        when(fireBaseUserTokenRepository.findByUserId(USER_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        NotificationServiceException notificationServiceException = assertThrows(NotificationServiceException.class, () ->
                notificationService.isNotificationEnabledOrDisabled(USER_ID));
        assertEquals("Failed to get notification information for userId: " + USER_ID,
                notificationServiceException.getMessage());
    }

    @Test
    void Should_ThrowNotificationServiceException_When_SaveNotificationDetailIntoDataBaseForSendNotification() {
        PushNotificationRequestDto pushNotificationRequestDto = getSamplePushNotificationRequestDto();
        UserNotification userNotification = getSampleUserNotification();
        when(fireBaseUserTokenRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userNotification));
        when(tokenEncoder.decode(ACCESS_TOKEN)).thenReturn(DECODED_VALUE);
        when(notificationRepository.save(any(NotificationDetail.class))).thenThrow(new DataAccessException("ERROR") {
        });
        NotificationServiceException notificationServiceException = assertThrows(NotificationServiceException.class, () ->
                notificationService.sendNotification(pushNotificationRequestDto));
        assertEquals("Error while saving notification to database", notificationServiceException.getMessage());
    }

    @Test
    void Should_ThrowNotificationServiceException_When_GetUserNotificationFromDataBaseIsFailedForSendNotification() {
        PushNotificationRequestDto pushNotificationRequestDto = getSamplePushNotificationRequestDto();
        NotificationRequestDto notificationRequestDto = getSampleNotificationRequestDto();
        NotificationDetail notificationDetail = new NotificationDetail(notificationRequestDto, USER_ID);
        when(notificationRepository.save(any(NotificationDetail.class))).thenReturn(notificationDetail);
        when(tokenEncoder.decode(ACCESS_TOKEN)).thenReturn(DECODED_VALUE);
        when(fireBaseUserTokenRepository.findByUserId(USER_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        NotificationServiceException notificationServiceException = assertThrows(NotificationServiceException.class, () ->
                notificationService.sendNotification(pushNotificationRequestDto));
        assertEquals("Failed to get notification information for userId: " + USER_ID,
                notificationServiceException.getMessage());
    }

    /**
     * This method creates sample Notification id list
     *
     * @return notificationIds
     */
    private List<String> getSampleNotificationIds() {
        List<String> notificationIds = new ArrayList<>();
        notificationIds.add("nid-1");
        return notificationIds;
    }

    /**
     * This method creates sample NotificationDetail
     *
     * @return NotificationDetail
     */
    private NotificationDetail getSampleNotificationDetail() {
        NotificationDetail notificationDetail = new NotificationDetail();
        notificationDetail.setId("nid-1");
        notificationDetail.setRead(false);
        return notificationDetail;
    }

    /**
     * This method creates sample NotificationDetail list
     *
     * @return NotificationDetailList
     */
    private List<NotificationDetail> getSampleNotificationDetailList() {
        List<NotificationDetail> notificationDetails = new ArrayList<>();
        notificationDetails.add(getSampleNotificationDetail());
        return notificationDetails;
    }

    /**
     * This method creates default pageable
     *
     * @return Pageable
     */
    private Pageable getDefaultPageable() {
        return PageRequest.of(PAGE, SIZE, Sort.Direction.DESC, CREATED_AT);
    }

    /**
     * This method creates sample AddTokenRequestDto
     *
     * @return AddTokenRequestDto
     */
    private AddTokenRequestDto getSampleAddTokenRequestDto() {
        AddTokenRequestDto addTokenRequestDto = new AddTokenRequestDto();
        addTokenRequestDto.setUserId(USER_ID);
        addTokenRequestDto.setDeviceId(DEVICE_ID);
        return addTokenRequestDto;
    }

    /**
     * This method creates sample AuthUser
     *
     * @return AuthUser
     */
    private AuthUser getSampleAuthUser() {
        AuthUser authUser = new AuthUser();
        authUser.setUserId(USER_ID);
        authUser.setRegisteredUser(true);
        return authUser;
    }

    /**
     * This method creates sample UserNotification
     *
     * @return UserNotification
     */
    private UserNotification getSampleUserNotification() {
        UserNotification userNotification = new UserNotification();
        userNotification.setId(USER_NOTIFICATION_ID);
        userNotification.setUserId(USER_ID);
        userNotification.setToken(ACCESS_TOKEN);
        userNotification.setDeviceId(DEVICE_ID);
        userNotification.setNotificationEnabled(true);
        userNotification.setCreatedAt(new Date(System.currentTimeMillis()));
        userNotification.setUpdatedAt(new Date(System.currentTimeMillis()));
        return userNotification;
    }

    /**
     * This method creates sample PushNotificationRequestDto
     *
     * @return PushNotificationRequestDto
     */
    private PushNotificationRequestDto getSamplePushNotificationRequestDto() {
        BasicUserRequestDto merchant = new BasicUserRequestDto();
        merchant.setId(USER_ID);
        BasicDealRequestDto basicDealRequestDto = getSampleBasicDealRequestDto();
        List<BasicDealRequestDto> basicDealRequestDtos = new ArrayList<>();
        basicDealRequestDtos.add(basicDealRequestDto);
        DealGroupRequestDto dealGroupRequestDto = new DealGroupRequestDto();
        dealGroupRequestDto.setMerchant(merchant);
        dealGroupRequestDto.setDeals(basicDealRequestDtos);
        List<DealGroupRequestDto> dealGroupRequestDtos = new ArrayList<>();
        dealGroupRequestDtos.add(dealGroupRequestDto);
        PushNotificationRequestDto pushNotificationRequestDto = new PushNotificationRequestDto();
        pushNotificationRequestDto.setUserId(USER_ID);
        pushNotificationRequestDto.setDealGroups(dealGroupRequestDtos);
        return pushNotificationRequestDto;
    }

    /**
     * This method creates sample BasicDealRequestDto
     *
     * @return BasicDealRequestDto
     */
    private BasicDealRequestDto getSampleBasicDealRequestDto() {
        BasicDealRequestDto basicDealRequestDto = new BasicDealRequestDto();
        basicDealRequestDto.setId("ID");
        basicDealRequestDto.setTitle("Title");
        basicDealRequestDto.setType(NotificationType.MERCHANT);
        basicDealRequestDto.setSubTitle("Sub title");
        return basicDealRequestDto;
    }

    /**
     * This method creates sample NotificationRequestDto
     *
     * @return NotificationRequestDto
     */
    private NotificationRequestDto getSampleNotificationRequestDto() {
        NotificationRequestDto notificationRequestDto = new NotificationRequestDto();
        notificationRequestDto.setBody("Message body");
        notificationRequestDto.setReferralId("Referral Id");
        notificationRequestDto.setTitle("Title");
        notificationRequestDto.setImageUrl("image url");
        notificationRequestDto.setType(NotificationType.MERCHANT);
        return notificationRequestDto;
    }
}
