package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.SubscribeSearchIndex;
import com.kupi.cc.base.domain.entity.SubscriptionPlan;
import com.kupi.cc.base.domain.entity.SubscriptionRenewal;
import com.kupi.cc.base.domain.request.*;
import com.kupi.cc.base.domain.response.*;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SubscriptionStatus;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.*;
import com.kupi.cc.base.service.AuthUserService;
import com.kupi.cc.base.service.SubscribeSearchIndexService;
import com.kupi.cc.base.service.SubscriberService;
import com.kupi.cc.base.service.SubscriptionPlanService;
import com.kupi.cc.base.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Subscription Plan Controller
 */
@Slf4j
@RestController
@Validated
@RequestMapping("api/v1/subscription-plan")
public class SubscriptionPlanController extends Controller {

    private final SubscriptionPlanService subscriptionPlanService;
    private final SubscriberService subscriberService;
    private final AuthUserService authUserService;
    private final SubscribeSearchIndexService subscribeSearchIndexService;

    private static final String ALREADY_SUBSCRIBE_USER_ID_PREFIX = "Already subscribed user on user id: {}.";
    private static final String INVALID_BUSINESS_USER_ID_PREFIX = "Invalid business user Id: {} to set subscription.";

    public SubscriptionPlanController(SubscriptionPlanService subscriptionPlanService,
                                      Translator translator, SubscriberService subscriberService,
                                      AuthUserService authUserService,
                                      SubscribeSearchIndexService subscribeSearchIndexRepository) {
        super(translator);
        this.subscriptionPlanService = subscriptionPlanService;
        this.subscriberService = subscriberService;
        this.authUserService = authUserService;
        this.subscribeSearchIndexService = subscribeSearchIndexRepository;
    }

    /**
     * This method creates a subscription plan.
     *
     * @param timeZone                   timezone
     * @param userId                     user id
     * @param subscriptionPlanRequestDto subscriptionPlanRequestDto
     * @return subscriptionPlanResponseDto
     */
    @PostMapping("")
    public ResponseEntity<ResponseWrapper> createSubscriptionPlan(
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestBody SubscriptionPlanRequestDto subscriptionPlanRequestDto) {
        try {
            if (!subscriptionPlanRequestDto.isRequiredAvailable()) {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            if (!subscriptionPlanRequestDto.isValidCharacterLimit()) {
                return getErrorResponse(ErrorResponseStatusType.CHARACTER_LIMIT_EXCEEDED_FOR_PLAN);
            }
            if (!subscriptionPlanRequestDto.isValidAmount()) {
                return getErrorResponse(ErrorResponseStatusType.INVALID_AMOUNT);
            }
            if (!subscriptionPlanRequestDto.isValidNoOfDealsOfTheDayEventsPerMonth()) {
                return getErrorResponse(ErrorResponseStatusType.INVALID_NO_OF_DOD_EVENTS_PER_MONTH);
            }
            if (!subscriptionPlanRequestDto.isValidNoOfDealsPerMonth()) {
                return getErrorResponse(ErrorResponseStatusType.INVALID_NO_OF_DEALS_PER_MONTH);
            }
            if (!subscriptionPlanRequestDto.isValidNoOfDealsOfTheDayPerEvent()) {
                return getErrorResponse(ErrorResponseStatusType.INVALID_NO_OF_DOD_PER_EVENT);
            }
            if (!(UserType.MERCHANT.equals(subscriptionPlanRequestDto.getUserRole()) ||
                    UserType.BANK.equals(subscriptionPlanRequestDto.getUserRole()))) {
                return getErrorResponse(ErrorResponseStatusType.INVALID_USER_ROLE);
            }
            var subscriptionPlan = new SubscriptionPlan(subscriptionPlanRequestDto);
            subscriptionPlanService.createSubscriptionPlan(subscriptionPlan, subscriptionPlanRequestDto.getUserRole());
            var subscriptionPlanResponseDto = new SubscriptionPlanResponseDto(subscriptionPlan);
            log.debug("Successfully create the subscription plan for userId: {}, " +
                            "subscription create request: {}, timeZone: {}.",
                    userId, subscriptionPlanRequestDto.toLogJson(), timeZone);
            return getSuccessResponse(subscriptionPlanResponseDto,
                    SuccessResponseStatusType.CREATE_SUBSCRIPTION_PLAN);
        } catch (ExistingNameException e) {
            log.error("Existing subscription plan title: {}.", subscriptionPlanRequestDto.getTitle(), e);
            return getErrorResponse(ErrorResponseStatusType.EXISTING_SUBSCRIPTION_PLAN_NAME);
        } catch (KupiCoreException e) {
            log.error("Creating new subscription plan was failed for userId: {}, time zone : {}.", userId, timeZone, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to get subscription plan by id.
     *
     * @param userId         user id
     * @param timeZone       time zone
     * @param subscriptionId subscription id
     * @return subscriptionPlanResponseDto
     */
    @GetMapping("/{subscriptionId}")
    public ResponseEntity<ResponseWrapper> getSubscriptionPlanById(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                                   @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                                   @PathVariable String subscriptionId) {
        try {
            var subscriptionPlan = subscriptionPlanService.getSubscriptionPlanById(subscriptionId);
            var subscriptionPlanResponseDto = new SubscriptionPlanResponseDto(subscriptionPlan);
            log.debug("Successfully returned the subscription plan by subscription id: {} for userId: {}, timeZone: {}",
                    subscriptionId, userId, timeZone);
            return getSuccessResponse(subscriptionPlanResponseDto, SuccessResponseStatusType.READ_SUBSCRIPTION_PLAN);
        } catch (InvalidSubscriptionPlanException e) {
            log.error("Invalid subscription id: {} to request subscription plan data for user id :{} and timezone: {}.",
                    subscriptionId, userId, timeZone, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN);
        } catch (KupiCoreException e) {
            log.error("Returning subscription plan by subscription id was failed for subscription id : {}," +
                    " user id :{} and timezone: {}.", subscriptionId, userId, timeZone, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to list all subscription plans.
     *
     * @param userId     user id
     * @param timeZone   time zone
     * @param page       page
     * @param size       size
     * @param searchTerm search term
     * @param userType   user type
     * @return subscriptionPlanListResponseDto
     */
    @GetMapping("/{userType}/{page}/{size}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> searchSubscriptionPlanList(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                                      @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                                      @Min(DEFAULT_PAGE) @PathVariable int page,
                                                                      @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
                                                                      @PathVariable String searchTerm,
                                                                      @PathVariable UserType userType) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT).descending());
        return getSubscriptionPlanList(userId, timeZone, pageable, searchTerm, userType, false);
    }

    /**
     * This method is used to list all active subscription plans.
     *
     * @param userId     user id
     * @param timeZone   time zone
     * @param page       page
     * @param size       size
     * @param searchTerm search term
     * @param userType   user type
     * @return subscriptionPlanListResponseDto
     */
    @GetMapping("/ACTIVE/{userType}/{page}/{size}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> searchActiveSubscriptionPlanList(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                                            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                                            @Min(DEFAULT_PAGE) @PathVariable int page,
                                                                            @Positive @Max(PAGE_MAX_SIZE) @PathVariable int size,
                                                                            @PathVariable String searchTerm,
                                                                            @PathVariable UserType userType) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT).descending());
        return getSubscriptionPlanList(userId, timeZone, pageable, searchTerm, userType, true);
    }

    /**
     * This method is used to get all subscription plan list.
     *
     * @param userId     user id
     * @param timeZone   time zone
     * @param pageable   pageable
     * @param searchTerm search term
     * @param userType   user type
     * @param isActive   isActive
     * @return subscriptionPlanListResponseDto
     */
    public ResponseEntity<ResponseWrapper> getSubscriptionPlanList(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                                   @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                                   Pageable pageable,
                                                                   String searchTerm,
                                                                   UserType userType,
                                                                   boolean isActive) {
        try {
            if (!(UserType.MERCHANT.equals(userType) || UserType.BANK.equals(userType))) {
                return getErrorResponse(ErrorResponseStatusType.INVALID_USER_ROLE);
            }
            Page<SubscriptionPlan> allSubscription = (isActive) ?
                    subscriptionPlanService.getActiveSubscriptionPlan(pageable, searchTerm, userType) :
                    subscriptionPlanService.getSubscriptionPlanPage(pageable, searchTerm, userType);
            var subscriptionPlanListResponseDto = new SubscriptionPlanListResponseDto(allSubscription);
            log.debug("Successfully returned the subscription plan list by search term: {} for user id: {} and timeZone: {}",
                    searchTerm, userId, timeZone);
            return getSuccessResponse(subscriptionPlanListResponseDto,
                    SuccessResponseStatusType.READ_SUBSCRIPTION_PLAN_LIST);
        } catch (KupiCoreException e) {
            log.error("Returning subscription plan list was failed for search term: {}, user id :{} and timezone: {}.",
                    searchTerm, userId, timeZone, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to get all active subscription plan list.
     *
     * @param userId   userId
     * @param timeZone timeZone
     * @return active subscription plan list.
     */
    @GetMapping("/subscription-plan-count")
    public ResponseEntity<ResponseWrapper> getSubscriptionPlanList(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                                   @RequestHeader(name = TIME_ZONE_HEADER) String timeZone
    ) {
        try {
            var subscriptionPlan = subscriptionPlanService.getSubscriptionPlanListResponseDto();
            var subscriptionPlanListResponseDto = new SubscriptionPlanListResponseDto(new PageImpl<>(subscriptionPlan));
            log.debug("Successfully returned the subscription plan for userId: {}, timeZone: {}",
                    userId, timeZone);
            return getSuccessResponse(subscriptionPlanListResponseDto, SuccessResponseStatusType.READ_SUBSCRIPTION_PLAN_LIST);
        } catch (KupiCoreException e) {
            log.error("Returning subscription plan list was failed for user id :{} and timezone: {}.",
                    userId, timeZone, e);
            return getInternalServerError();
        }
    }

    /**
     * This method updates an existing subscription plan.
     *
     * @param timeZone                     time zone
     * @param userId                       user id
     * @param subscriptionUpdateRequestDto subscriptionUpdateRequestDto
     * @return subscriptionPlanResponseDto
     */
    @PutMapping("")
    public ResponseEntity<ResponseWrapper> updateSubscriptionPlan(@RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                                  @RequestHeader(name = USER_ID_HEADER) String userId,
                                                                  @RequestBody SubscriptionUpdateRequestDto subscriptionUpdateRequestDto) {
        try {
            if (!(UserType.MERCHANT.equals(subscriptionUpdateRequestDto.getUserRole())
                    || UserType.BANK.equals(subscriptionUpdateRequestDto.getUserRole()))) {
                return getErrorResponse(ErrorResponseStatusType.INVALID_USER_ROLE);
            }
            if (!subscriptionUpdateRequestDto.isRequiredAvailable()) {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            if (!subscriptionUpdateRequestDto.isValidCharacterLimit()) {
                return getErrorResponse(ErrorResponseStatusType.CHARACTER_LIMIT_EXCEEDED_FOR_PLAN);
            }
            if (!subscriptionUpdateRequestDto.isValidAmount()) {
                return getErrorResponse(ErrorResponseStatusType.INVALID_AMOUNT);
            }
            var subscriptionPlan = subscriptionPlanService.updateSubscriptionPlan(subscriptionUpdateRequestDto);
            var subscriptionPlanResponseDto = new SubscriptionPlanResponseDto(subscriptionPlan);
            log.debug("Successfully update the subscription plan by id: {} for userId: {} and timeZone: {}.",
                    subscriptionUpdateRequestDto.getId(), userId, timeZone);
            return getSuccessResponse(subscriptionPlanResponseDto,
                    SuccessResponseStatusType.UPDATE_SUBSCRIPTION_PLAN);
        } catch (ExistingNameException e) {
            log.error("Existing subscription plan name to update subscription plan id: {}",
                    subscriptionUpdateRequestDto.getId(), e);
            return getErrorResponse(ErrorResponseStatusType.EXISTING_SUBSCRIPTION_PLAN_NAME);
        } catch (InvalidSubscriptionPlanException e) {
            log.error("Invalid subscription id: {} to update subscription plan for user id :{} and timezone: {}.",
                    subscriptionUpdateRequestDto.getId(), userId, timeZone, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN);
        } catch (KupiCoreException e) {
            log.error("Updating subscription plan for id: {} was failed for user id :{} and timezone: {}.",
                    subscriptionUpdateRequestDto.getId(), userId, timeZone, e);
            return getInternalServerError();
        }
    }

    /**
     * This method changes status of the subscription plan.
     *
     * @param timeZone                         time zone
     * @param userId                           user id
     * @param subscriptionPlanStatusRequestDto subscriptionPlanStatusRequestDto
     * @return SuccessResponse / ErrorResponse
     */
    @PutMapping("/status")
    public ResponseEntity<ResponseWrapper> changeStatus(@RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                        @RequestHeader(name = USER_ID_HEADER) String userId,
                                                        @RequestBody SubscriptionPlanStatusRequestDto subscriptionPlanStatusRequestDto) {
        try {
            if (!subscriptionPlanStatusRequestDto.isRequiredAvailable()) {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            subscriptionPlanService
                    .updateSubscriptionPlanStatus(subscriptionPlanStatusRequestDto.getId(), subscriptionPlanStatusRequestDto.getStatus());
            log.debug("Successfully update the status of subscription plan by id: {} for userId: {} and timeZone: {}.",
                    subscriptionPlanStatusRequestDto.getId(), userId, timeZone);
            return getSuccessResponse(null,
                    SuccessResponseStatusType.UPDATE_SUBSCRIPTION_PLAN_STATUS);
        } catch (InvalidSubscriptionPlanException e) {
            log.error("Invalid subscription id: {} to update status of subscription plan for user id :{} and timezone: {}.",
                    subscriptionPlanStatusRequestDto.getId(), userId, timeZone, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN);
        } catch (KupiCoreException e) {
            log.error("Updating subscription plan status for id: {} was failed for user id :{} and timezone: {}.",
                    subscriptionPlanStatusRequestDto.getId(), userId, timeZone, e);
            return getInternalServerError();
        }
    }

    /**
     * This method deletes the subscription plan by id.
     *
     * @param timeZone       timezone
     * @param userId         user id
     * @param subscriptionId subscription id
     * @return SuccessResponse / ErrorResponse
     */
    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<ResponseWrapper> deleteSubscriptionPlan(@RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                                  @RequestHeader(name = USER_ID_HEADER) String userId,
                                                                  @PathVariable String subscriptionId) {
        try {
            subscriptionPlanService.getSubscriptionPlanById(subscriptionId);
            subscriberService.checkSubscriptionPlanHasUsers(subscriptionId);
            subscriptionPlanService.deleteSubscriptionPlanById(subscriptionId);
            log.debug("Successfully deleted the subscription plan by id: {} for userId: {} and timeZone: {}.",
                    subscriptionId, userId, timeZone);
            return getSuccessResponse(null,
                    SuccessResponseStatusType.DELETE_SUBSCRIPTION_PLAN);
        } catch (InvalidSubscriptionPlanException e) {
            log.error("Invalid subscription id: {} to delete subscription plan for user id :{} and timezone: {}.",
                    subscriptionId, userId, timeZone, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN);
        } catch (UnsupportedDeleteSubscriptionPlanException e) {
            log.error("Unsupported action delete subscription plan id:{}" +
                    " hence the subscription plan has users.", subscriptionId, e);
            return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_SUBSCRIPTION_PLAN_DELETE);
        } catch (KupiCoreException e) {
            log.error("Deleting subscription plan by id: {} was failed for user id :{} and timezone: {}.",
                    subscriptionId, userId, timeZone, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is called by order-payment service to subscribe to already paid subscription plan
     *
     * @param timeZone time zone
     * @param userId   user id
     * @return subscribeResponseDto
     */
    @PostMapping("/subscribe")
    public ResponseEntity<ResponseWrapper> subscribe(@RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                     @RequestHeader(name = USER_ID_HEADER) String userId,
                                                     @RequestBody SubscribeRequestDto subscribeRequestDto,
                                                     HttpServletRequest request) {
        try {
            if (!subscribeRequestDto.isRequiredAvailable()) {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            var userType = subscribeRequestDto.getUserType();
            if (!(UserType.MERCHANT.equals(userType) || UserType.BANK.equals(userType))) {
                return getErrorResponse(ErrorResponseStatusType.INVALID_USER_ROLE);
            }

            var subscriptionPlan = subscriptionPlanService
                    .getSubscriptionPlanById(subscribeRequestDto.getSubscriptionPlanId());
            if (subscriptionPlan.getStatus().equals(SubscriptionStatus.INACTIVE)) {
                return getErrorResponse(ErrorResponseStatusType.INACTIVE_SUBSCRIPTION_PLAN);
            }
            subscriberService.checkUserIsNotInAPlan(userId);
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            var businessMerchantResponseDto =
                    (userType.equals(UserType.BANK)) ?
                            authUserService.getBankBusinessByBankId(authToken, userId) :
                            authUserService.getMerchantBusinessByMerchantId(authToken, userId);

            subscriberService.setSubscribe(subscribeRequestDto.getTempSubscriptionId(),
                    subscriptionPlan, businessMerchantResponseDto, userType);
            return getSuccessResponse(null, SuccessResponseStatusType.SUBSCRIBE_PLAN);

        } catch (UnsupportedUserTypeException e) {
            log.error("Unsupported user type:{} to subscribe the plan id: {} for user id: {}.",
                    subscribeRequestDto.getUserType(), subscribeRequestDto.getSubscriptionPlanId(), userId, e);
            return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_SUBSCRIPTION_USER_TYPE);
        } catch (ExistingSubscribedUserException e) {
            log.error(ALREADY_SUBSCRIBE_USER_ID_PREFIX, userId, e);
            return getErrorResponse(ErrorResponseStatusType.ALREADY_SUBSCRIBE_USER);
        } catch (InvalidUserException e) {
            log.error(INVALID_BUSINESS_USER_ID_PREFIX, userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (InvalidSubscriptionPlanException e) {
            log.error("Invalid subscription plan id: {} to set subscription for user id :{} and timezone: {}.",
                    subscribeRequestDto.getSubscriptionPlanId(), userId, timeZone, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN);
        } catch (KupiCoreException e) {
            log.error("Setting subscription was failed for user id :{}, subscriptionId: {} and timezone: {}.",
                    userId, subscribeRequestDto.getSubscriptionPlanId(), timeZone, e);
            return getInternalServerError();
        }
    }

    /**
     * This method validates a request to subscribe for a particular subscription plan
     * if it is a free plan, then subscribe
     *
     * @param timeZone time zone
     * @return subscribeResponseDto
     */
    @PostMapping("/subscribe/validate")
    public ResponseEntity<ResponseWrapper> validateSubscriptionRequest(
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestBody SubscribeValidationRequestDto subscribeValidationRequestDto,
            HttpServletRequest request) {
        try {
            if (!subscribeValidationRequestDto.isRequiredAvailable()) {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            var userType = subscribeValidationRequestDto.getUserType();
            if (!(UserType.MERCHANT.equals(userType) || UserType.BANK.equals(userType))) {
                return getErrorResponse(ErrorResponseStatusType.INVALID_USER_ROLE);
            }
            var subscriptionPlan = subscriptionPlanService.getSubscriptionPlanById(
                    subscribeValidationRequestDto.getSubscriptionPlanId());
            if (subscriptionPlan.getStatus().equals(SubscriptionStatus.INACTIVE)) {
                return getErrorResponse(ErrorResponseStatusType.INACTIVE_SUBSCRIPTION_PLAN);
            }

            subscriberService.checkUserIsNotInAPlan(userId);
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            if (UserType.BANK.equals(userType)) {
                authUserService.getBankBusinessByBankId(authToken, userId);
            } else {
                authUserService.getMerchantBusinessByMerchantId(authToken, userId);
            }

            var validationResponseDto = new SubscriptionValidationResponseDto(subscriptionPlan);
            return getSuccessResponse(validationResponseDto, SuccessResponseStatusType.SUBSCRIBE_REQUEST_VALIDATED);

        } catch (ExistingSubscribedUserException e) {
            log.error(ALREADY_SUBSCRIBE_USER_ID_PREFIX, userId, e);
            return getErrorResponse(ErrorResponseStatusType.ALREADY_SUBSCRIBE_USER);
        } catch (InvalidSubscriptionPlanException e) {
            log.error("Invalid subscription id: {} to set subscription for user id :{} and timezone: {}.",
                    subscribeValidationRequestDto.getSubscriptionPlanId(), userId, timeZone, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN);
        } catch (InvalidUserException e) {
            log.error(INVALID_BUSINESS_USER_ID_PREFIX, userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (KupiCoreException e) {
            log.error("Validating subscription was failed for user id :{}, subscriptionId: {} and timezone: {}.",
                    userId, subscribeValidationRequestDto.getSubscriptionPlanId(), timeZone, e);
            return getInternalServerError();
        }
    }

    /**
     * This method validates a request to subscribe for a particular subscription plan
     * if it is a free plan, then subscribe
     *
     * @param timeZone time zone
     * @return subscribeResponseDto
     */
    @PostMapping("/subscribe/free")
    public ResponseEntity<ResponseWrapper> subscribeFreeSubscription(
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestBody SubscribeValidationRequestDto subscribeValidationRequestDto,
            HttpServletRequest request) {
        try {
            if (!subscribeValidationRequestDto.isRequiredAvailable()) {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            var userType = subscribeValidationRequestDto.getUserType();
            if (!(UserType.MERCHANT.equals(userType) || UserType.BANK.equals(userType))) {
                return getErrorResponse(ErrorResponseStatusType.INVALID_USER_ROLE);
            }
            var subscriptionPlan = subscriptionPlanService.getSubscriptionPlanById(
                    subscribeValidationRequestDto.getSubscriptionPlanId());
            if (subscriptionPlan.getStatus().equals(SubscriptionStatus.INACTIVE)) {
                return getErrorResponse(ErrorResponseStatusType.INACTIVE_SUBSCRIPTION_PLAN);
            }

            subscriberService.checkUserIsNotInAPlan(userId);
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            var businessMerchantResponseDto =
                    (userType.equals(UserType.BANK)) ?
                            authUserService.getBankBusinessByBankId(authToken, userId) :
                            authUserService.getMerchantBusinessByMerchantId(authToken, userId);

            var validationResponseDto = new SubscriptionValidationResponseDto(subscriptionPlan);
            if (subscriptionPlan.isFree()) {
                subscriberService.setSubscribe(null, subscriptionPlan, businessMerchantResponseDto, userType);
                validationResponseDto.setSubscribed(true);
                return getSuccessResponse(validationResponseDto, SuccessResponseStatusType.SUBSCRIBE_PLAN);
            } else {
                return getErrorResponse(ErrorResponseStatusType.NOT_FREE_SUBSCRIPTION);
            }

        } catch (ExistingSubscribedUserException e) {
            log.error(ALREADY_SUBSCRIBE_USER_ID_PREFIX, userId, e);
            return getErrorResponse(ErrorResponseStatusType.ALREADY_SUBSCRIBE_USER);
        } catch (InvalidSubscriptionPlanException e) {
            log.error("Invalid subscription id: {} to set subscription for user id :{} and timezone: {}.",
                    subscribeValidationRequestDto.getSubscriptionPlanId(), userId, timeZone, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN);
        } catch (InvalidUserException e) {
            log.error(INVALID_BUSINESS_USER_ID_PREFIX, userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (KupiCoreException e) {
            log.error("Subscribing into free plan was failed for user id :{}, subscriptionId: {} and timezone: {}.",
                    userId, subscribeValidationRequestDto.getSubscriptionPlanId(), timeZone, e);
            return getInternalServerError();
        }
    }

    /**
     * This method unsubscribes a subscription plan.
     * @param userId       user id
     * @param subscribedId subscribedId
     * @param userType     userType
     * @return SuccessResponse / ErrorResponse
     */
    @DeleteMapping("{userType}/unsubscribe/{subscribedId}")
    public ResponseEntity<ResponseWrapper> unsubscribe(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                       @PathVariable String subscribedId,
                                                       @PathVariable UserType userType) {
        try {
            if (!(UserType.MERCHANT.equals(userType) || UserType.BANK.equals(userType))) {
                return getErrorResponse(ErrorResponseStatusType.INVALID_USER_ROLE);
            }
            subscriberService.unsubscribeAPlan(subscribedId, userType);
            return getSuccessResponse(null, SuccessResponseStatusType.UNSUBSCRIBE_PLAN);
        } catch (InvalidSubscribeException e) {
            log.error("Invalid subscribe id: {} to un-subscribe the subscription plan for user id :{} ",
                    subscribedId, userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN);
        } catch (UnsupportedUserTypeException e) {
            log.error("Invalid user type: {} to un-subscribe the subscribed id: {}.",
                    subscribedId, subscribedId, e);
            return getErrorResponse(ErrorResponseStatusType.UNSUPPORTED_UNSUBSCRIBE_USER_TYPE);
        } catch (KupiCoreException e) {
            log.error("Unsubscribe for subscribe id: {} was failed for user id :{}.", subscribedId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method lists subscribed users for a subscription plan by id.
     *
     * @param userId             user id
     * @param subscriptionPlanId subscription plan id
     * @param page               page
     * @param size               size
     * @param searchTerm         search term
     * @return merchantListResponseDto
     */
    @GetMapping("/{subscriptionPlanId}/subscribers/{page}/{size}/search/{searchTerm}")
    public ResponseEntity<ResponseWrapper> searchSubscribers(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                             @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                             @PathVariable String subscriptionPlanId,
                                                             @Min(DEFAULT_PAGE) @PathVariable("page") int page,
                                                             @Positive @Max(PAGE_MAX_SIZE) @PathVariable("size") int size,
                                                             @PathVariable String searchTerm) {
        try {
            SubscriptionPlan subscriptionPlan = subscriptionPlanService.getSubscriptionPlanById(subscriptionPlanId);
            Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT).descending());
            var subscribePage = subscribeSearchIndexService
                    .getSubscribersBySubscriptionPlanId(pageable, subscriptionPlanId, searchTerm);
            List<String> userIdList = subscribePage
                    .getContent()
                    .stream().map(SubscribeSearchIndex::getUserId).collect(Collectors.toList());
            var bulkUserRequestDto = new BulkUserRequestDto(userIdList);
            BulkUserResponseDto bulkUserResponseDto = new BulkUserResponseDto();
            SubscribersListResponseDto subscribersListResponseDto;
            if (!subscribePage.isEmpty()) {
                List<BasicMerchantBusinessResponseDto> merchantBusinessResponseDtoList = authUserService
                        .getMerchantMap(bulkUserRequestDto, subscriptionPlan.getUserRole()).values().stream().map(
                                BasicMerchantBusinessResponseDto::new).collect(Collectors.toList());
                bulkUserResponseDto.setUsers(merchantBusinessResponseDtoList);
                Map<String, Long> merchantIdVsSubscribedOnMap = new HashMap<>();
                subscribePage.getContent().forEach(subscribeSearchIndex ->
                        merchantIdVsSubscribedOnMap
                                .put(subscribeSearchIndex.getUserId(), subscribeSearchIndex.getCreatedAt()));
                subscribersListResponseDto =
                        new SubscribersListResponseDto(subscribePage,
                                merchantIdVsSubscribedOnMap, bulkUserResponseDto, timeZone);
            } else {
                subscribersListResponseDto =
                        new SubscribersListResponseDto(subscribePage,
                                new HashMap<>(), new BulkUserResponseDto(new ArrayList<>()), timeZone);
            }
            return getSuccessResponse(subscribersListResponseDto, SuccessResponseStatusType.SUBSCRIBE_PLAN_USER);
        } catch (InvalidSubscriptionPlanException e) {
            log.error("Invalid subscription id: {} to get subscribed users.", subscriptionPlanId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN);
        } catch (KupiCoreException e) {
            log.error("Failed to return subscribed user for subscription plan id: {}.", subscriptionPlanId, e);
            return getInternalServerError();
        }
    }

    /**
     * Get Subscription plan by user id.
     *
     * @param timeZone        timezone
     * @param userId          userId
     * @param userType        userType
     * @param subscribeUserId subscribeUserId
     * @param request         request
     * @return userSubscriptionPlanResponseDto
     */
    @GetMapping("/{userType}/{subscribeUserId}")
    public ResponseEntity<ResponseWrapper> getSubscriptionPlanByUserId(@RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                                       @RequestHeader(name = USER_ID_HEADER) String userId,
                                                                       @PathVariable UserType userType,
                                                                       @PathVariable String subscribeUserId,
                                                                       HttpServletRequest request) {
        try {
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            if (UserType.MERCHANT.equals(userType)) {
                authUserService.getMerchantBusinessByMerchantId(authToken, subscribeUserId);
            } else {
                authUserService.getBankBusinessByBankId(authToken, subscribeUserId);
            }
            var subscribeByUserIdOptional = subscriberService.getSubscribeByUserId(subscribeUserId);
            SubscriptionRenewal subscriptionRenewal = subscriberService.checkSubscriptionPlanForDeal(subscribeUserId);
            var userSubscriptionPlanResponseDto =
                    new SubscriptionPlanDetailResponseDto(subscriptionRenewal, subscribeByUserIdOptional, timeZone);
            return getSuccessResponse(userSubscriptionPlanResponseDto, SuccessResponseStatusType.USER_SUBSCRIPTION_PLAN);
        } catch (InvalidUserException e) {
            log.error("Invalid merchant id: {} to get subscription plan.", userId, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_MERCHANT_ID);
        } catch (InvalidSubscribeException e) {
            log.error("Subscription not found for userId: {}.", userId, e);
            return getErrorResponse(ErrorResponseStatusType.SUBSCRIPTION_PLAN_NOT_FOUND_FOR_USER);
        } catch (KupiCoreException e) {
            log.error("Failed to return merchant's subscription plan for user id: {}, userType: {} and timeZone: {}.",
                    subscribeUserId, userType, timeZone, e);
            return getInternalServerError();
        }
    }

    /**
     * Get subscription plan summary by plan id.
     *
     * @param timeZone           timeZone
     * @param userId             user id
     * @param subscriptionPlanId subscription plan id
     * @return subscriptionPlanSummaryResponseDto
     */
    @GetMapping("/{subscriptionPlanId}/summary")
    public ResponseEntity<ResponseWrapper> getSubscriptionPlanSummary(@RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                                      @RequestHeader(name = USER_ID_HEADER) String userId,
                                                                      @PathVariable String subscriptionPlanId) {
        try {
            var subscriptionPlan = subscriptionPlanService.getSubscriptionPlanById(subscriptionPlanId);
            long subscriptionCount = subscriberService.getSubscribersCountBySubscriptionPlanId(subscriptionPlanId);
            var subscriptionPlanSummaryResponseDto = new SubscriptionPlanSummaryResponseDto(subscriptionPlan, subscriptionCount);
            return getSuccessResponse(subscriptionPlanSummaryResponseDto, SuccessResponseStatusType.SUBSCRIPTION_SUMMARY);
        } catch (InvalidSubscriptionPlanException e) {
            log.error("Invalid subscription id: {} to get subscription plan summary for user id :{} and timezone: {}.",
                    subscriptionPlanId, userId, timeZone, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN);
        } catch (KupiCoreException e) {
            log.error("Failed to return subscription plan summary for subscription plan id: {}, user id: {}, and timeZone: {}.",
                    subscriptionPlanId, userId, timeZone, e);
            return getInternalServerError();
        }
    }
}