package com.kupi.notification.controller;

import com.kupi.notification.configuration.Translator;
import com.kupi.notification.domain.entity.NotificationDetail;
import com.kupi.notification.domain.request.*;
import com.kupi.notification.enums.ErrorResponseStatusType;
import com.kupi.notification.enums.NotificationStatus;
import com.kupi.notification.enums.NotificationType;
import com.kupi.notification.enums.SuccessResponseStatusType;
import com.kupi.notification.exception.FirebaseInvalidTokenException;
import com.kupi.notification.exception.InvalidRecipientException;
import com.kupi.notification.exception.InvalidUserException;
import com.kupi.notification.exception.NotificationServiceException;
import com.kupi.notification.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests {@link NotificationController} class.
 */
class NotificationControllerTest {

    private static final String USER_ID = "User-Id";
    private static final String NOTIFICATION_TOKEN = "Notification-Token";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String APPLICATION_JSON_UTF_8 = "application/json;charset=UTF-8";
    private static final String ACCESS_TOKEN = "Bearer192f0c96-dd19-404e-8217-a8fd1cc81d55";
    private static final String CREATED_AT = "createdAt";
    private static final int PAGE = 0;
    private static final int SIZE = 1;
    private static final int TOTAL_PAGES = 1;
    private static final int TOTAL_ITEMS = 1;
    private static final int UNREAD_NOTIFICATION_COUNT = 1;
    private static final String USER_NOTIFICATION_ID = "unid-12568-78544-7954";
    private static final String ADD_TOKEN_URI = "/api/v1/notification/token";
    private static final String REMOVE_TOKEN_URI = "/api/v1/notification/token";
    private static final String PUSH_BULK_NOTIFICATION_URI = "/api/v1/notification/push";
    private static final String MARK_READ_NOTIFICATION_URI = "/api/v1/notification/mark-read";
    private static final String GET_NOTIFICATION_SUMMARY_URI = "/api/v1/notification/summary/{page}/{size}";
    private static final String GET_UNREAD_NOTIFICATION_COUNT_URI = "/api/v1/notification/count/UNREAD";
    private static final String DELETE_NOTIFICATION_URI = "/api/v1/notification";
    private static final String ENABLE_OR_DISABLE_NOTIFICATION_URI = "/api/v1/notification/notification-status/{status}";
    private static final String GET_NOTIFICATION_STATUS_URI = "/api/v1/notification/status";

    @Mock
    private NotificationService notificationService;
    @Mock
    private Translator translator;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        initMocks(this);
        NotificationController notificationController = new NotificationController(translator, notificationService);
        org.springframework.validation.Validator noOpValidator = new org.springframework.validation.Validator() {
            @Override
            public boolean supports(Class<?> clazz) {
                return true;
            }

            @Override
            public void validate(Object target, Errors errors) {
                // No-op validator
            }
        };
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
                .setValidator(noOpValidator)
                .build();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnOk_When_TokenAddedSuccessfully() throws Exception {
        AddTokenRequestDto addTokenRequestDto = getSampleAddTokenRequestDto();
        mockMvc.perform(MockMvcRequestBuilders.post(ADD_TOKEN_URI)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Notification-Token", NOTIFICATION_TOKEN)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(addTokenRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.ADD_TOKEN.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    void Should_ReturnBadRequest_When_MissingRequiredFieldsAreProvidedForAddToken() throws Exception {
        AddTokenRequestDto addTokenRequestDto = getSampleAddTokenRequestDto();
        addTokenRequestDto.setUserId("");
        mockMvc.perform(MockMvcRequestBuilders.post(ADD_TOKEN_URI)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Notification-Token", NOTIFICATION_TOKEN)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(addTokenRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidUserIdIsProvidedForAddToken() throws Exception {
        AddTokenRequestDto addTokenRequestDto = getSampleAddTokenRequestDto();
        doThrow(new InvalidUserException("ERROR")).when(notificationService)
                .addToken(anyString(), anyString(), anyString(), any(AddTokenRequestDto.class), anyString());
        mockMvc.perform(MockMvcRequestBuilders.post(ADD_TOKEN_URI)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Notification-Token", NOTIFICATION_TOKEN)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(addTokenRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_USER.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_USER.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidNotificationTokenIsProvidedForAddToken() throws Exception {
        AddTokenRequestDto addTokenRequestDto = getSampleAddTokenRequestDto();
        doThrow(new FirebaseInvalidTokenException("ERROR")).when(notificationService)
                .addToken(anyString(), anyString(), anyString(), any(AddTokenRequestDto.class), anyString());
        mockMvc.perform(MockMvcRequestBuilders.post(ADD_TOKEN_URI)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Notification-Token", NOTIFICATION_TOKEN)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(addTokenRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_FIREBASE_TOKEN.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_FIREBASE_TOKEN.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_AddTokenIsFailed() throws Exception {
        AddTokenRequestDto addTokenRequestDto = getSampleAddTokenRequestDto();
        doThrow(new NotificationServiceException("ERROR")).when(notificationService)
                .addToken(anyString(), anyString(), anyString(), any(AddTokenRequestDto.class), anyString());
        mockMvc.perform(MockMvcRequestBuilders.post(ADD_TOKEN_URI)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Notification-Token", NOTIFICATION_TOKEN)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .content(addTokenRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_TokenRemovedSuccessfully() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(REMOVE_TOKEN_URI)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.REMOVE_TOKEN.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidUserIdIsProvidedForRemoveToken() throws Exception {
        doThrow(new InvalidUserException("ERROR")).when(notificationService)
                .removeToken(USER_ID, ACCESS_TOKEN, TIME_ZONE);
        mockMvc.perform(MockMvcRequestBuilders.delete(REMOVE_TOKEN_URI)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INVALID_USER.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INVALID_USER.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_RemoveTokenIsFailed() throws Exception {
        doThrow(new NotificationServiceException("ERROR")).when(notificationService)
                .removeToken(USER_ID, ACCESS_TOKEN, TIME_ZONE);
        mockMvc.perform(MockMvcRequestBuilders.delete(REMOVE_TOKEN_URI)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .header("Auth-Token", ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_NotificationReadSuccessfully() throws Exception {
        BulkNotificationRequestDto bulkNotificationRequestDto = getSampleBulkNotificationRequestDto();
        mockMvc.perform(MockMvcRequestBuilders.post(MARK_READ_NOTIFICATION_URI)
                        .header("User-Id", USER_ID)
                        .content(bulkNotificationRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.MARK_READ.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    void Should_ReturnBadRequest_When_MissingRequiredFieldsAreProvidedForMarkReadNotification() throws Exception {
        BulkNotificationRequestDto bulkNotificationRequestDto = getSampleBulkNotificationRequestDto();
        bulkNotificationRequestDto.setNotificationIds(null);
        mockMvc.perform(MockMvcRequestBuilders.post(MARK_READ_NOTIFICATION_URI)
                        .header("User-Id", USER_ID)
                        .content(bulkNotificationRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_NotificationIdCountExceedLimitForMarkReadNotification() throws Exception {
        BulkNotificationRequestDto bulkNotificationRequestDto = getSampleBulkNotificationRequestDto();
        List<String> notificationIds = new ArrayList<>();
        for (int i = 0; i < 251; i++) {
            notificationIds.add(USER_NOTIFICATION_ID);
        }
        bulkNotificationRequestDto.setNotificationIds(notificationIds);
        mockMvc.perform(MockMvcRequestBuilders.post(MARK_READ_NOTIFICATION_URI)
                        .header("User-Id", USER_ID)
                        .content(bulkNotificationRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MAX_ID_COUNT.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MAX_ID_COUNT.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_MarkReadNotificationIsFailed() throws Exception {
        BulkNotificationRequestDto bulkNotificationRequestDto = getSampleBulkNotificationRequestDto();
        doThrow(new NotificationServiceException("ERROR")).when(notificationService)
                .updateNotificationReadStatus(bulkNotificationRequestDto.getNotificationIds());
        mockMvc.perform(MockMvcRequestBuilders.post(MARK_READ_NOTIFICATION_URI)
                        .header("User-Id", USER_ID)
                        .content(bulkNotificationRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_NotificationDetailsPageReturnedSuccessfully() throws Exception {
        List<NotificationDetail> notificationDetailList = getSampleNotificationDetailList();
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(CREATED_AT).descending());
        Page<NotificationDetail> notificationDetailPage = new PageImpl<>(notificationDetailList, pageable, 1);
        when(notificationService.getNotificationItems(anyString(), any(Pageable.class)))
                .thenReturn(notificationDetailPage);
        String uri = GET_NOTIFICATION_SUMMARY_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE));
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.NOTIFICATION_SUMMARY.getMessage()))
                .andExpect(jsonPath("$.data.totalItems").value(TOTAL_ITEMS))
                .andExpect(jsonPath("$.data.totalPages").value(TOTAL_PAGES))
                .andExpect(jsonPath("$.data.page").value(PAGE))
                .andExpect(jsonPath("$.data.size").value(SIZE));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetNotificationSummaryIsFailed() throws Exception {
        doThrow(new NotificationServiceException("ERROR")).when(notificationService)
                .getNotificationItems(anyString(), any(Pageable.class));
        String uri = GET_NOTIFICATION_SUMMARY_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE));
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header("User-Id", USER_ID)
                        .header("Time-Zone", TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_UnreadNotificationCountReturnedSuccessfully() throws Exception {
        when(notificationService.getUnreadNotificationCountByUserId(USER_ID)).thenReturn(UNREAD_NOTIFICATION_COUNT);
        mockMvc.perform(MockMvcRequestBuilders.get(GET_UNREAD_NOTIFICATION_COUNT_URI)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.UNREAD_NOTIFICATION_COUNT.getMessage()))
                .andExpect(jsonPath("$.data.count").value(UNREAD_NOTIFICATION_COUNT));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetUnreadNotificationCountIsFailed() throws Exception {
        doThrow(new NotificationServiceException("ERROR")).when(notificationService)
                .getUnreadNotificationCountByUserId(USER_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(GET_UNREAD_NOTIFICATION_COUNT_URI)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_NotificationDeletedSuccessfully() throws Exception {
        BulkNotificationRequestDto bulkNotificationRequestDto = getSampleBulkNotificationRequestDto();
        mockMvc.perform(MockMvcRequestBuilders.delete(DELETE_NOTIFICATION_URI)
                        .content(bulkNotificationRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.DELETE_NOTIFICATIONS.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    void Should_ReturnBadRequest_When_MissingRequiredFieldsAreProvidedForDeleteNotifications() throws Exception {
        BulkNotificationRequestDto bulkNotificationRequestDto = getSampleBulkNotificationRequestDto();
        bulkNotificationRequestDto.setNotificationIds(null);
        mockMvc.perform(MockMvcRequestBuilders.delete(DELETE_NOTIFICATION_URI)
                        .content(bulkNotificationRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()));
    }


    @Test
    void Should_ReturnBadRequest_When_NotificationIdCountExceedLimitForDeleteNotifications() throws Exception {
        BulkNotificationRequestDto bulkNotificationRequestDto = getSampleBulkNotificationRequestDto();
        List<String> notificationIds = new ArrayList<>();
        for (int i = 0; i < 251; i++) {
            notificationIds.add(USER_NOTIFICATION_ID);
        }
        bulkNotificationRequestDto.setNotificationIds(notificationIds);
        mockMvc.perform(MockMvcRequestBuilders.delete(DELETE_NOTIFICATION_URI)
                        .content(bulkNotificationRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MAX_ID_COUNT.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MAX_ID_COUNT.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_DeleteNotificationIsFailed() throws Exception {
        BulkNotificationRequestDto bulkNotificationRequestDto = getSampleBulkNotificationRequestDto();
        doThrow(new NotificationServiceException("ERROR")).when(notificationService)
                .deleteNotifications(bulkNotificationRequestDto.getNotificationIds());
        mockMvc.perform(MockMvcRequestBuilders.delete(DELETE_NOTIFICATION_URI)
                        .content(bulkNotificationRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_EnableNotificationStatus() throws Exception {
        String uri = ENABLE_OR_DISABLE_NOTIFICATION_URI.replace("{status}", NotificationStatus.ENABLE.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.NOTIFICATIONS_ENABLED.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    void Should_ReturnOk_When_DisableNotificationStatus() throws Exception {
        String uri = ENABLE_OR_DISABLE_NOTIFICATION_URI.replace("{status}", NotificationStatus.DISABLE.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.NOTIFICATIONS_DISABLED.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    void Should_ReturnInternalServerError_When_EnableOrDisableNotificationIsFailed() throws Exception {
        doThrow(new NotificationServiceException("ERROR")).when(notificationService)
                .enableOrDisableNotification(USER_ID, NotificationStatus.ENABLE);
        String uri = ENABLE_OR_DISABLE_NOTIFICATION_URI.replace("{status}", NotificationStatus.ENABLE.name());
        mockMvc.perform(MockMvcRequestBuilders.put(uri)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_ValidUserIdIsProvidedForGetNotificationStatus() throws Exception {
        when(notificationService.isNotificationEnabledOrDisabled(USER_ID)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.get(GET_NOTIFICATION_STATUS_URI)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.NOTIFICATION_STATUS.getMessage()))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetNotificationStatusIsFailed() throws Exception {
        doThrow(new NotificationServiceException("ERROR")).when(notificationService)
                .isNotificationEnabledOrDisabled(USER_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(GET_NOTIFICATION_STATUS_URI)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidUserIdIsProvidedForGetNotificationStatus() throws Exception {
        doThrow(new InvalidRecipientException("ERROR")).when(notificationService)
                .isNotificationEnabledOrDisabled(USER_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(GET_NOTIFICATION_STATUS_URI)
                        .header("User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER.getCode()));
    }

    @Test
    void Should_ReturnOk_When_SendBulkNotification() throws Exception {
        PushNotificationRequestDto pushNotificationRequestDto = getSamplePushNotificationRequestDto();
        mockMvc.perform(MockMvcRequestBuilders.post(PUSH_BULK_NOTIFICATION_URI)
                        .content(pushNotificationRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(SuccessResponseStatusType.SEND_NOTIFICATION.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    void Should_ReturnBadRequest_When_MissingRequiredFieldsAreProvidedForPushBulkNotification() throws Exception {
        PushNotificationRequestDto pushNotificationRequestDto = getSamplePushNotificationRequestDto();
        pushNotificationRequestDto.setUserId("");
        mockMvc.perform(MockMvcRequestBuilders.post(PUSH_BULK_NOTIFICATION_URI)
                        .content(pushNotificationRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidRecipientIdIsProvidedForPushBulkNotification() throws Exception {
        PushNotificationRequestDto pushNotificationRequestDto = getSamplePushNotificationRequestDto();
        doThrow(new InvalidRecipientException("ERROR")).when(notificationService)
                .sendNotification(any(PushNotificationRequestDto.class));
        mockMvc.perform(MockMvcRequestBuilders.post(PUSH_BULK_NOTIFICATION_URI)
                        .content(pushNotificationRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_RECIPIENT.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_RECIPIENT.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_PushBulkNotificationIsFailed() throws Exception {
        PushNotificationRequestDto pushNotificationRequestDto = getSamplePushNotificationRequestDto();
        doThrow(new NotificationServiceException("ERROR")).when(notificationService)
                .sendNotification(any(PushNotificationRequestDto.class));
        mockMvc.perform(MockMvcRequestBuilders.post(PUSH_BULK_NOTIFICATION_URI)
                        .content(pushNotificationRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode")
                        .value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    /**
     * This method creates sample AddTokenRequestDto
     *
     * @return AddTokenRequestDto
     */
    private AddTokenRequestDto getSampleAddTokenRequestDto() {
        AddTokenRequestDto addTokenRequestDto = new AddTokenRequestDto();
        addTokenRequestDto.setUserId("uid-1256-7854-8569-785");
        addTokenRequestDto.setDeviceId("DEVICE-ID");
        return addTokenRequestDto;
    }

    /**
     * This method creates sample BulkNotificationRequestDto
     *
     * @return BulkNotificationRequestDto
     */
    private BulkNotificationRequestDto getSampleBulkNotificationRequestDto() {
        BulkNotificationRequestDto bulkNotificationRequestDto = new BulkNotificationRequestDto();
        List<String> notificationIds = getSampleNotificationIds();
        bulkNotificationRequestDto.setNotificationIds(notificationIds);
        return bulkNotificationRequestDto;
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
        notificationDetail.setCreatedAt(new Date(System.currentTimeMillis()));
        notificationDetail.setUpdatedAt(new Date(System.currentTimeMillis()));
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
     * This method creates sample Notification id list
     *
     * @return notificationIds
     */
    private List<String> getSampleNotificationIds() {
        List<String> notificationIds = new ArrayList<>();
        notificationIds.add(USER_NOTIFICATION_ID);
        return notificationIds;
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
}