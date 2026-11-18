package com.kupi.notification.controller;

import com.kupi.notification.configuration.Translator;
import com.kupi.notification.domain.entity.NotificationDetail;
import com.kupi.notification.domain.request.AddTokenRequestDto;
import com.kupi.notification.domain.request.BulkNotificationRequestDto;
import com.kupi.notification.domain.request.PushNotificationRequestDto;
import com.kupi.notification.domain.response.NotificationCountResponseDto;
import com.kupi.notification.domain.response.NotificationDetailResponseDto;
import com.kupi.notification.domain.response.NotificationStatusResponseDto;
import com.kupi.notification.enums.ErrorResponseStatusType;
import com.kupi.notification.enums.NotificationStatus;
import com.kupi.notification.enums.SuccessResponseStatusType;
import com.kupi.notification.exception.FirebaseInvalidTokenException;
import com.kupi.notification.exception.InvalidRecipientException;
import com.kupi.notification.exception.InvalidUserException;
import com.kupi.notification.exception.NotificationServiceException;
import com.kupi.notification.service.NotificationService;
import com.kupi.notification.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

@Slf4j
@RestController
@RequestMapping("/api/v1/notification")
public class NotificationController extends Controller {

    protected static final int PAGE_MAX_SIZE = 250;
    protected static final int DEFAULT_PAGE = 0;
    private static final String USER_ID = "User-Id";
    private static final String NOTIFICATION_TOKEN = "Notification-Token";
    private static final int MAX_NOTIFICATION_ID_COUNT = 250;
    private final NotificationService notificationService;

    @Autowired
    public NotificationController(Translator translator, NotificationService notificationService) {
        super(translator);
        this.notificationService = notificationService;
    }

    /**
     * This method adds notification tokens.
     *
     * @param userId            userId
     * @param notificationToken notificationToken
     * @param request           http request header
     * @param timeZone          timeZone
     * @return response entity
     */
    @PostMapping(path = "/token", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> addToken(@RequestHeader(value = USER_ID) String userId,
                                                    @RequestHeader(value = NOTIFICATION_TOKEN) String notificationToken,
                                                    @RequestHeader(value = TIMEZONE_HEADER) String timeZone,
                                                    @RequestBody AddTokenRequestDto addTokenRequestDto,
                                                    HttpServletRequest request) {
        try {
            if (addTokenRequestDto.isRequiredAvailable() && notificationToken != null && !notificationToken.isEmpty()) {
                String token = getToken(request);
                notificationService.addToken(userId, token, notificationToken, addTokenRequestDto, timeZone);
                return getSuccessResponse(SuccessResponseStatusType.ADD_TOKEN, null);
            } else {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
        } catch (InvalidUserException e) {
            log.error("Invalid UserId: {}", userId, e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_USER);
        } catch (FirebaseInvalidTokenException e) {
            log.error("Invalid notification token for userId: {}", addTokenRequestDto.getUserId(), e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_FIREBASE_TOKEN);
        } catch (NotificationServiceException e) {
            log.error("Adding notification token was failed for userId: {}", userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method removes the device notification when user logged out from the device
     *
     * @param userId   userId
     * @param request  http request header
     * @param timeZone timeZone
     * @return response entity
     */
    @DeleteMapping(path = "/token", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> removeToken(
            @RequestHeader(value = USER_ID) String userId,
            @RequestHeader(value = TIMEZONE_HEADER) String timeZone,
            HttpServletRequest request) {
        try {
            String token = getToken(request);
            notificationService.removeToken(userId, token, timeZone);
            return getSuccessResponse(SuccessResponseStatusType.REMOVE_TOKEN, null);

        } catch (InvalidUserException e) {
            log.error("Invalid UserId: {}", userId, e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_USER);
        } catch (NotificationServiceException e) {
            log.error("Adding notification token was failed for userId: {}", userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method push notifications to the given recipient.
     *
     * @param pushNotificationRequestDto pushNotificationRequestDto
     * @return response entity
     */
    @PostMapping(path = "/push", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> pushBulkNotification(
            @RequestBody PushNotificationRequestDto pushNotificationRequestDto
    ) {
        try {
            if (pushNotificationRequestDto.isRequiredAvailable()) {
                notificationService.sendNotification(pushNotificationRequestDto);
                return getSuccessResponse(SuccessResponseStatusType.SEND_NOTIFICATION, null);
            } else {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
        } catch (InvalidRecipientException e) {
            log.error("Invalid recipient userId: {} ", pushNotificationRequestDto.getUserId(), e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_RECIPIENT);
        } catch (NotificationServiceException e) {
            log.error("Failed to send the notification to the recipient.  userId: {}",
                    pushNotificationRequestDto.getUserId(), e);
            return getInternalServerError();
        }
    }

    /**
     * This method marks notification as read
     *
     * @param userId                     userId
     * @param bulkNotificationRequestDto bulkNotificationRequestDto
     * @return responseWrapper
     */
    @PostMapping(path = "mark-read", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> markReadNotification(@RequestHeader(value = USER_ID) String userId,
                                                                @RequestBody BulkNotificationRequestDto
                                                                        bulkNotificationRequestDto) {
        try {
            if (bulkNotificationRequestDto.isRequiredAvailable()) {
                if (bulkNotificationRequestDto.getNotificationIds().size() > MAX_NOTIFICATION_ID_COUNT) {
                    return getBadRequestError(ErrorResponseStatusType.MAX_ID_COUNT);
                }
                notificationService.updateNotificationReadStatus(bulkNotificationRequestDto.getNotificationIds());
                return getSuccessResponse(SuccessResponseStatusType.MARK_READ, null);
            } else {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
        } catch (NotificationServiceException e) {
            log.error("Failed to mark notification ", e);
            return getInternalServerError();
        }
    }

    /**
     * This method returns notification summary by user
     *
     * @param userId  userId
     * @param page    page
     * @param size    size
     * @param request request
     * @return responseWrapper
     */
    @GetMapping(path = "/summary/{page}/{size}", produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> getNotificationSummary(@RequestHeader(value = USER_ID) String userId,
                                                                  @Min(DEFAULT_PAGE) @PathVariable("page") Integer page,
                                                                  @Min(DEFAULT_PAGE) @Max(PAGE_MAX_SIZE)
                                                                  @Positive @PathVariable("size") Integer size,
                                                                  HttpServletRequest request) {
        try {
            String timeZoneId = getTimeZoneId(request);
            Pageable pageable = PageRequest.of(page, size);
            Page<NotificationDetail> notificationItems = notificationService.getNotificationItems(userId, pageable);
            NotificationDetailResponseDto notificationDetailResponseDto =
                    new NotificationDetailResponseDto(notificationItems, timeZoneId);
            return getSuccessResponse(SuccessResponseStatusType.NOTIFICATION_SUMMARY, notificationDetailResponseDto);
        } catch (NotificationServiceException e) {
            log.error("Failed to get notification summary", e);
            return getInternalServerError();
        }
    }

    /**
     * This method returns unread notification count for userId.
     *
     * @param userId userId
     * @return success/error response.
     */
    @GetMapping(path = "/count/UNREAD", produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> getUnreadNotificationCount(@RequestHeader(value = USER_ID) String userId) {
        try {
            int notificationCount = notificationService.getUnreadNotificationCountByUserId(userId);
            return getSuccessResponse(SuccessResponseStatusType.UNREAD_NOTIFICATION_COUNT,
                    new NotificationCountResponseDto(notificationCount));
        } catch (NotificationServiceException e) {
            log.error("Getting unread notification count was failed for userId: {}.", userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method deletes notifications.
     *
     * @param bulkNotificationRequestDto bulkNotificationRequestDto
     * @return response entity.
     */
    @DeleteMapping(path = "", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> deleteNotifications(@RequestBody BulkNotificationRequestDto
                                                                       bulkNotificationRequestDto) {
        try {
            if (bulkNotificationRequestDto.isRequiredAvailable()) {
                if (bulkNotificationRequestDto.getNotificationIds().size() > MAX_NOTIFICATION_ID_COUNT) {
                    return getBadRequestError(ErrorResponseStatusType.MAX_ID_COUNT);
                }
                notificationService.deleteNotifications(bulkNotificationRequestDto.getNotificationIds());
                return getSuccessResponse(SuccessResponseStatusType.DELETE_NOTIFICATIONS, null);
            } else {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
        } catch (NotificationServiceException e) {
            log.error("Failed to delete notifications. ", e);
            return getInternalServerError();
        }
    }

    /**
     * This method enables or disables notifications for a given userId.
     *
     * @param userId userId
     * @param status enable/disable
     * @return success/error response.
     */
    @PutMapping(path = "/notification-status/{status}", consumes = APPLICATION_JSON_UTF_8,
            produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> enableOrDisableNotifications(@RequestHeader(value = USER_ID) String userId,
                                                                        @PathVariable NotificationStatus status) {
        try {
            notificationService.enableOrDisableNotification(userId, status);
            log.debug("Notification status updated to: {} for userId: {}.", status, userId);
            SuccessResponseStatusType successResponse = status == NotificationStatus.ENABLE ?
                    SuccessResponseStatusType.NOTIFICATIONS_ENABLED : SuccessResponseStatusType.NOTIFICATIONS_DISABLED;
            return getSuccessResponse(successResponse, null);
        } catch (NotificationServiceException e) {
            log.error("Notification status update was failed for userId: {}, status: {}.", userId, status, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to get notification status for user.
     *
     * @param userId userId
     * @return success/error response.
     */
    @GetMapping(path = "/status", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> getNotificationStatus(@RequestHeader(value = USER_ID) String userId) {
        try {
            boolean isEnabled = notificationService.isNotificationEnabledOrDisabled(userId);
            log.debug("Successfully returned notification status for userId: {}.", userId);
            return getSuccessResponse(SuccessResponseStatusType.NOTIFICATION_STATUS,
                    new NotificationStatusResponseDto(isEnabled));
        } catch (InvalidRecipientException e) {
            log.error("Registered user not found for userId: {}.", userId, e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_USER);
        } catch (NotificationServiceException e) {
            log.error("Getting notification status was failed for userId: {}.", userId, e);
            return getInternalServerError();
        }
    }
}
