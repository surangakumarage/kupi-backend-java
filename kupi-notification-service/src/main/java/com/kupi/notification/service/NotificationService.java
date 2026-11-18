package com.kupi.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Notification Service
 */
@Service
@Slf4j
public class NotificationService {

    private static final String FAILED_TO_SEND_NOTIFICATION = "Sending notification was failed.";
    private static final String FAILED_TO_SAVE_NOTIFICATION_TOKEN = "Saving notification token to database was failed.";
    private static final String FAILED_TO_REMOVE_NOTIFICATION_TOKEN = "Removing notification token from database was failed.";
    private static final String FAILED_TO_AUTH_VALIDATION = "Validating user with auth service was failed.";
    private static final String INVALID_USER_ID = "Invalid userId: ";
    private static final String INVALID_RECIPIENT_ID = "Invalid recipientId.";
    private static final String REFERRAL_ID = "referralId";
    private static final String TYPE = "type";
    private static final String ID = "id";
    private static final String SEPARATOR = ", ";
    private static final String EMPTY_TEXT = "";
    private static final int ZERO = 0;
    private static final int TWO = 2;
    private static final String SORT_BY_CREATED_AT = "createdAt";
    private final AuthUserService authUserService;
    private final UserNotificationRepository fireBaseUserTokenRepository;
    private final TokenEncoder tokenEncoder;
    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationService(AuthUserService authUserService, UserNotificationRepository fireBaseUserTokenRepository,
                               TokenEncoder tokenEncoder, NotificationRepository notificationRepository) {
        this.authUserService = authUserService;
        this.fireBaseUserTokenRepository = fireBaseUserTokenRepository;
        this.tokenEncoder = tokenEncoder;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Add or update notification token
     *
     * @param userId            userId
     * @param userToken         token
     * @param notificationToken notificationToken
     */
    public void addToken(String userId, String userToken, String notificationToken,
                         AddTokenRequestDto addTokenRequestDto, String timeZone) {
        try {
            if (isValidUser(userId, userToken, timeZone)) {
                updateNotificationToken(addTokenRequestDto, notificationToken);
            } else {
                throw new InvalidUserException(INVALID_USER_ID + userId);
            }
        } catch (DataAccessException e) {
            throw new NotificationServiceException(FAILED_TO_SAVE_NOTIFICATION_TOKEN, e);
        }
    }

    /**
     * Remove device notification token
     *
     * @param userId    userId
     * @param userToken token
     * @param timeZone  timeZone
     */
    public void removeToken(String userId, String userToken, String timeZone) {
        try {
            if (isValidUser(userId, userToken, timeZone)) {
                Date updatedTimestamp = new Date();
                fireBaseUserTokenRepository.deleteNotificationTokenForUserId(updatedTimestamp, userId);
            } else {
                throw new InvalidUserException(INVALID_USER_ID + userId);
            }
        } catch (DataAccessException e) {
            throw new NotificationServiceException(FAILED_TO_REMOVE_NOTIFICATION_TOKEN, e);
        }
    }

    /**
     * This method sends the notification.
     *
     * @param pushNotificationRequestDto pushNotificationRequestDto
     */
    public void sendNotification(PushNotificationRequestDto pushNotificationRequestDto) {
        try {
            List<DealGroupRequestDto> dealGroup = pushNotificationRequestDto.getDealGroups();
            UserNotification userNotification = getTokenForValidRecipient(pushNotificationRequestDto.getUserId());

            if (userNotification.isNotificationEnabled() && isNotEmpty(userNotification.getToken())) {
                for (DealGroupRequestDto dealGroupRequestDto : dealGroup) {
                    NotificationRequestDto notificationRequestDto = createNotificationRequest(dealGroupRequestDto);
                    sendNotification(pushNotificationRequestDto.getUserId(),
                            tokenEncoder.decode(userNotification.getToken()), notificationRequestDto);
                }
            }
        } catch (DataAccessException e) {
            throw new NotificationServiceException(FAILED_TO_SEND_NOTIFICATION, e);
        }
    }

    /**
     * This method creates notification request.
     *
     * @param dealGroupRequestDto dealGroupRequestDto
     * @return notification request.
     */
    private NotificationRequestDto createNotificationRequest(DealGroupRequestDto dealGroupRequestDto) {

        List<BasicDealRequestDto> deals = dealGroupRequestDto.getDeals();
        if (dealGroupRequestDto.getDeals().size() == 1) {
            BasicDealRequestDto deal = deals.get(ZERO);
            NotificationType notificationType = deal.getType() == NotificationType.BANK ?
                    NotificationType.BANK_DEAL : NotificationType.MERCHANT_DEAL;
            return new NotificationRequestDto(deal.getTitle(), deal.getSubTitle(), notificationType, deal.getId(),
                    deal.getImageUrl());
        }
        StringBuilder title = new StringBuilder(EMPTY_TEXT);
        StringBuilder body = new StringBuilder(EMPTY_TEXT);
        for (int i = ZERO; i < 3; i++) {
            if (i < deals.size()) {
                title.append(deals.get(i).getTitle()).append(SEPARATOR);
                body.append(deals.get(i).getSubTitle()).append(SEPARATOR);
            }
        }
        return new NotificationRequestDto(String.valueOf(title.substring(ZERO, title.length() - TWO)),
                String.valueOf(body.substring(ZERO, body.length() - TWO)),
                NotificationType.MERCHANT, dealGroupRequestDto.getMerchant().getId(),
                dealGroupRequestDto.getMerchant().getImageUrl());
    }

    /**
     * This method sends the notification.
     *
     * @param userId                 userId
     * @param notificationToken      registrationToken
     * @param notificationRequestDto notificationRequestDto
     */
    private void sendNotification(String userId, String notificationToken,
                                  NotificationRequestDto notificationRequestDto) {
        log.info("Notification Token is : {} ", notificationToken );
        NotificationDetail notificationDetail = saveNotification(notificationRequestDto, userId);
        Message message =
                Message.builder()
                        .putData(ID, notificationDetail.getId())
                        .putData(TYPE, notificationRequestDto.getType().toString())
                        .putData(REFERRAL_ID, notificationRequestDto.getReferralId())
                        .setNotification(Notification.builder()
                                .setTitle(notificationRequestDto.getTitle())
                                .setBody(notificationRequestDto.getBody())
                                .setImage(notificationRequestDto.getImageUrl())
                                .build())
                        .setToken(notificationToken)
                        .build();
        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.debug("Notification was sent to the user. userId: {}, response: {}", userId, response);
        } catch (FirebaseMessagingException e) {
            throw new NotificationServiceException(FAILED_TO_SEND_NOTIFICATION, e);
        }
    }

    /**
     * Save notifications
     *
     * @param notificationRequestDto notificationRequestDto
     * @param userId                 userId
     * @return notificationDetail
     */
    private NotificationDetail saveNotification(NotificationRequestDto notificationRequestDto, String userId) {
        try {
            NotificationDetail notificationDetail = new NotificationDetail(notificationRequestDto, userId);
            return notificationRepository.save(notificationDetail);
        } catch (DataAccessException e) {
            throw new NotificationServiceException("Error while saving notification to database", e);
        }
    }

    /**
     * This method validate the recipientId.
     *
     * @param recipientId recipientId
     */
    private UserNotification getTokenForValidRecipient(String recipientId) {
        try {
            Optional<UserNotification> fireBaseUserTokenOptional = fireBaseUserTokenRepository.findByUserId(recipientId);
            if (fireBaseUserTokenOptional.isEmpty()) {
                throw new InvalidRecipientException(INVALID_RECIPIENT_ID);
            }
            return fireBaseUserTokenOptional.get();
        } catch (DataAccessException e) {
            throw new NotificationServiceException("Failed to get notification information for userId: " + recipientId, e);
        }
    }

    /**
     * This methods updates existing token or adds new token.
     *
     * @param addTokenRequestDto addTokenRequestDto
     * @param notificationToken  notificationToken
     */
    private void updateNotificationToken(AddTokenRequestDto addTokenRequestDto, String notificationToken) {
        Optional<UserNotification> fireBaseUserTokenOptional =
                fireBaseUserTokenRepository.findByUserId(addTokenRequestDto.getUserId());
        UserNotification fireBaseUserToken;
        String encodedToken = tokenEncoder.encode(notificationToken);
        if (fireBaseUserTokenOptional.isPresent()) {
            fireBaseUserToken = fireBaseUserTokenOptional.get();
            fireBaseUserToken.setToken(encodedToken);
            fireBaseUserToken.setUpdatedAt(new Date());
        } else {
            fireBaseUserToken = new UserNotification(addTokenRequestDto, encodedToken);
        }
        fireBaseUserTokenRepository.save(fireBaseUserToken);
    }

    /**
     * Check user exists
     *
     * @param userId userId
     * @param token  token
     * @return true/false
     */
    private boolean isValidUser(String userId, String token, String timeZone) {
        try {
            AuthUser userByUserId = authUserService.getUserByUserId(userId, token, timeZone);
            return userByUserId != null;
        } catch (DataAccessException e) {
            throw new NotificationServiceException(FAILED_TO_AUTH_VALIDATION, e);
        }
    }

    /**
     * Get notification details by id
     *
     * @param notificationIds notificationIds
     * @return List of NotificationDetails
     */
    private List<NotificationDetail> getNotificationDetails(List<String> notificationIds) {
        try {
            return notificationRepository.findAllById(notificationIds);
        } catch (DataAccessException e) {
            throw new NotificationServiceException("Failed to get notification details", e);
        }
    }

    /**
     * This method mark notification as read
     *
     * @param notificationIds notificationIds
     */
    public void updateNotificationReadStatus(List<String> notificationIds) {
        try {
            List<NotificationDetail> notificationDetails = getNotificationDetails(notificationIds);
            for (NotificationDetail notificationDetail : notificationDetails) {
                notificationDetail.setRead(true);
                notificationRepository.save(notificationDetail);
            }
        } catch (DataAccessException e) {
            throw new NotificationServiceException("Failed to mark status", e);
        }
    }

    /**
     * This method return notification summary
     *
     * @param userId   userId
     * @param pageable pageable
     * @return page
     */
    public Page<NotificationDetail> getNotificationItems(String userId, Pageable pageable) {
        try {
            Pageable defaultPageable = getDefaultSortedPageable(pageable);
            return notificationRepository.findByUserIdAndIsDeletedFalse(userId, defaultPageable);
        } catch (DataAccessException e) {
            throw new NotificationServiceException("Failed to get notifications", e);
        }
    }

    /**
     * Return the default sort options
     *
     * @param pageable pageable
     * @return default pageable
     */
    private Pageable getDefaultSortedPageable(Pageable pageable) {
        Sort sort = Sort.by(
                Sort.Order.desc(SORT_BY_CREATED_AT));
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    /**
     * This method returns unread notification count for userId.
     *
     * @param userId userId
     * @return unread notification count.
     */
    public int getUnreadNotificationCountByUserId(String userId) {
        try {
            return notificationRepository.countByUserIdAndIsReadFalseAndIsDeletedFalse(userId);
        } catch (DataAccessException e) {
            throw new NotificationServiceException("Getting unread notification count from database was failed" +
                    " for userId: " + userId, e);
        }
    }

    /**
     * This method soft deletes notifications.
     *
     * @param notificationIds notificationIds
     */
    public void deleteNotifications(List<String> notificationIds) {
        try {
            notificationRepository.setNotificationAsDeleted(notificationIds);
        } catch (DataAccessException e) {
            throw new NotificationServiceException("Failed to delete notifications.", e);
        }
    }

    /**
     * This method enables or disables notifications for a given userId.
     *
     * @param userId             userId
     * @param notificationStatus notificationStatus
     */
    public void enableOrDisableNotification(String userId, NotificationStatus notificationStatus) {
        try {
            boolean isEnabled = notificationStatus == NotificationStatus.ENABLE;
            fireBaseUserTokenRepository.updateNotificationStatusForUserId(isEnabled, userId);
        } catch (DataAccessException e) {
            throw new NotificationServiceException("Failed to " + notificationStatus
                    + " notification for userId: " + userId, e);
        }
    }

    /**
     * This method is used to check if notification is enabled/disabled.
     *
     * @param userId userId
     * @return true/false.
     */
    public boolean isNotificationEnabledOrDisabled(String userId) {
        UserNotification userNotification = getTokenForValidRecipient(userId);
        return userNotification.isNotificationEnabled();
    }
}
