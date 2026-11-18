package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.request.UpdatePlanStatusRequestDto;
import com.kupi.cc.base.domain.request.UserSubscriptionPlanRequestDto;
import com.kupi.cc.base.domain.response.MobileUserSubscriptionPlanResponseDto;
import com.kupi.cc.base.domain.response.UserSubscriptionPlanListResponse;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.exception.ExistingNameException;
import com.kupi.cc.base.exception.InvalidUserSubscriptionPlanException;
import com.kupi.cc.base.exception.PlanHasActiveUsersException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.service.UserSubscriptionPlanService;
import com.kupi.cc.base.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * UserSubscription Controller
 */
@Slf4j
@RestController
@Validated
@RequestMapping("api/v1/user-subscription-plans")
public class UserSubscriptionPlanController extends Controller {

    private final UserSubscriptionPlanService userSubscriptionPlanService;

    public UserSubscriptionPlanController(Translator translator, UserSubscriptionPlanService userSubscriptionPlanService) {
        super(translator);
        this.userSubscriptionPlanService = userSubscriptionPlanService;
    }

    /**
     * Creates a new user subscription plan using the provided request body.
     * Validates required fields and checks for existing plan name conflicts.
     * If valid, it creates the subscription plan, registers it in Stripe,
     * and returns the created plan details in a success response.
     *
     * @param timeZone                   client's time zone from the request header
     * @param subscriptionPlanRequestDto request body containing subscription plan details
     * @return the created subscription plan wrapped in a success response
     * @throws ExistingNameException if a plan with the same name already exists
     * @throws KupiCoreException     if an internal error occurs while creating the plan
     */
    @PostMapping("")
    public ResponseEntity<ResponseWrapper> createUserSubscriptionPlan(
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @RequestBody UserSubscriptionPlanRequestDto subscriptionPlanRequestDto) {
        try {
            if (!subscriptionPlanRequestDto.isRequiredAvailable()) {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            MobileUserSubscriptionPlanResponseDto userSubscriptionPlan =
                    userSubscriptionPlanService.createUserSubscriptionPlan(subscriptionPlanRequestDto);
            log.debug("Successfully create the user subscription plan for user subscription create request: {}."
                    , subscriptionPlanRequestDto.toLogJson());
            return getSuccessResponse(userSubscriptionPlan,
                    SuccessResponseStatusType.CREATE_MOBILE_USER_SUBSCRIPTION_PLAN);
        } catch (ExistingNameException e) {
            log.error("Existing user subscription plan title: {}.", subscriptionPlanRequestDto.getName(), e);
            return getErrorResponse(ErrorResponseStatusType.EXISTING_SUBSCRIPTION_PLAN_NAME);
        } catch (KupiCoreException e) {
            log.error("Creating new subscription plan was failed for time zone : {}.", timeZone, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to update an existing user subscription plan.
     * It validates the request, checks for name conflicts, and updates the plan details.
     *
     * @param planId                     ID of the subscription plan to update
     * @param subscriptionPlanRequestDto request body containing updated subscription plan details
     * @param timeZone                   client's time zone from the request header
     * @return updated subscription plan wrapped in a success response
     */
    @PutMapping("/{planId}")
    public ResponseEntity<ResponseWrapper> updateUserSubscriptionPlan(
            @PathVariable String planId,
            @RequestBody UserSubscriptionPlanRequestDto subscriptionPlanRequestDto,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone) {
        try {
            if (!subscriptionPlanRequestDto.isRequiredAvailable()) {
                return getErrorResponse(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }

            MobileUserSubscriptionPlanResponseDto updatedPlan =
                    userSubscriptionPlanService.updateUserSubscriptionPlan(planId, subscriptionPlanRequestDto);

            log.debug("Successfully updated the user subscription plan. PlanId: {}, Request: {}",
                    planId, subscriptionPlanRequestDto.toLogJson());

            return getSuccessResponse(updatedPlan,
                    SuccessResponseStatusType.SUCCESSFULLY_UPDATED_USER_SUBSCRIPTION_PLAN);
        } catch (ExistingNameException e) {
            log.error("Subscription plan name already exists: {}", subscriptionPlanRequestDto.getName(), e);
            return getErrorResponse(ErrorResponseStatusType.EXISTING_SUBSCRIPTION_PLAN_NAME);
        } catch (InvalidUserSubscriptionPlanException e) {
            log.error("Subscription plan not found: {}", subscriptionPlanRequestDto.getName(), e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN_ID);
        } catch (KupiCoreException e) {
            log.error("Failed to update subscription plan. PlanId: {}, TimeZone: {}", planId, timeZone, e);
            return getInternalServerError();
        }
    }


    /**
     * This method is used to fetch all active user subscription plans.
     * It returns a list of subscription plans ordered by price in ascending order.
     *
     * @param userId   ID of the user making the request (from header)
     * @param timeZone Client's time zone from the request header
     * @return list of active subscription plans wrapped in a success response
     */
    @GetMapping("")
    public ResponseEntity<ResponseWrapper> getAllUserSubscriptionPlans(@RequestHeader(name = USER_ID_HEADER) String userId,
                                                                       @RequestHeader(name = TIME_ZONE_HEADER) String timeZone) {
        try {
            UserSubscriptionPlanListResponse planListResponse = userSubscriptionPlanService.getAllActivePlans();
            return getSuccessResponse(planListResponse,
                    SuccessResponseStatusType.SUCCESSFULLY_RETURNED_USER_SUBSCRIPTION_PLAN_LIST);
        } catch (KupiCoreException e) {
            log.error("Fetching all user subscription plans failed for userId: {}, timeZone: {}", userId, timeZone, e);
            return getInternalServerError();
        }
    }

    /**
     * This method is used to deactivate a user subscription plan by its ID.
     * It marks the plan as inactive in the database (soft delete).
     *
     * @param planId   ID of the subscription plan to deactivate
     * @param timeZone Client's time zone from the request header
     * @return success response if the plan was successfully deactivated
     */
    @DeleteMapping("/{planId}")
    public ResponseEntity<ResponseWrapper> deleteUserSubscriptionPlan(
            @PathVariable String planId,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone) {
        try {
            userSubscriptionPlanService.deleteSubscriptionPlan(planId);
            log.debug("Successfully deactivated subscription plan: {}", planId);
            return getSuccessResponse(null,
                    SuccessResponseStatusType.SUCCESSFULLY_DELETED_USER_SUBSCRIPTION_PLAN);
        } catch (PlanHasActiveUsersException e) {
            log.error("Cannot delete plan: {} because it still has active subscriptions.", planId, e);
            return getErrorResponse(ErrorResponseStatusType.PLAN_HAS_ACTIVE_USERS);

        } catch (InvalidUserSubscriptionPlanException e) {
            log.error("Invalid plan ID: {}, TimeZone: {}", planId, timeZone, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN_ID);
        } catch (KupiCoreException e) {
            log.error("Deactivation failed for plan ID: {}, TimeZone: {}", planId, timeZone, e);
            return getInternalServerError();
        }
    }


    /**
     * This method is used to update the activation status of a user subscription plan by its ID.
     * It sets the plan as active or inactive in the database based on the request payload.
     *
     * @param planId                     ID of the subscription plan to update
     * @param updatePlanStatusRequestDto request body containing the isActive flag
     * @param timeZone                   Client's time zone from the request header
     * @return success response if the plan was successfully updated
     */

    @PutMapping("/{planId}/status")
    public ResponseEntity<ResponseWrapper> updateSubscriptionPlanStatus(
            @PathVariable String planId,
            @RequestBody UpdatePlanStatusRequestDto updatePlanStatusRequestDto,
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone) {
        try {
            userSubscriptionPlanService.updateActivationStatus(planId, updatePlanStatusRequestDto);
            return getSuccessResponse(null,
                    updatePlanStatusRequestDto.isActive()
                            ? SuccessResponseStatusType.SUCCESSFULLY_ACTIVATED_USER_SUBSCRIPTION_PLAN
                            : SuccessResponseStatusType.SUCCESSFULLY_DEACTIVATED_USER_SUBSCRIPTION_PLAN);
        } catch (InvalidUserSubscriptionPlanException e) {
            log.error("Invalid plan ID: {}, TimeZone: {}", planId, timeZone, e);
            return getErrorResponse(ErrorResponseStatusType.INVALID_SUBSCRIPTION_PLAN_ID);
        } catch (KupiCoreException e) {
            log.error("Failed to update activation status for plan ID: {}, TimeZone: {}", planId, timeZone, e);
            return getInternalServerError();
        }
    }
}
