package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.response.UserSubscriptionStatusResponse;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.exception.InvalidUserSubscriptionPlanException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.exception.UserSubscriptionNotFound;
import com.kupi.cc.base.service.UserSubscriptionService;
import com.kupi.cc.base.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Validated
@RequestMapping("api/v1/user-subscription")
public class UserSubscriptionController extends Controller {

    private final UserSubscriptionService userSubscriptionService;

    public UserSubscriptionController(Translator translator, UserSubscriptionService userSubscriptionService) {
        super(translator);
        this.userSubscriptionService = userSubscriptionService;
    }

    /**
     * This method is used to retrieve the current subscription status for a given user.
     * It returns details such as subscription validity, remaining coupon count,
     * subscription end date (adjusted to the user's time zone), and the plan name.
     *
     * @param timeZone client's time zone from the request header
     * @param userId   ID of the user whose subscription status is being requested
     * @return subscription status details wrapped in a success response
     */
    @GetMapping("/status")
    public ResponseEntity<ResponseWrapper> getUserSubscriptionStatus(@RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                                     @RequestHeader(name = USER_ID_HEADER) String userId) {

        try {
            UserSubscriptionStatusResponse userSubscriptionStatus = userSubscriptionService
                    .getUserSubscriptionStatus(userId, timeZone);
            return getSuccessResponse(userSubscriptionStatus,
                    SuccessResponseStatusType.SUCCESSFULLY_RETURNED_USER_SUBSCRIPTION_PLAN_DETAIL);
        } catch (UserSubscriptionNotFound e) {
            log.error("Subscription not found for userId: {}", userId, e);
            return getErrorResponse(ErrorResponseStatusType.USER_SUBSCRIPTION_NOT_FOUND);
        } catch (KupiCoreException e) {
            log.error("Failed to fetch subscription status for userId: {}", userId, e);
            return getInternalServerError();
        }
    }

    @PostMapping("/assign-default")
    public ResponseEntity<ResponseWrapper> assignDefaultSubscriptionToUser(
            @RequestHeader(name = USER_ID_HEADER) String userId) {
        try {
            userSubscriptionService.assignDefaultSubscription(userId);
            return getSuccessResponse(null, SuccessResponseStatusType.ASSIGNED_DEFAULT_SUBSCRIPTION);
        } catch (InvalidUserSubscriptionPlanException e) {
            return getErrorResponse(ErrorResponseStatusType.USER_SUBSCRIPTION_NOT_FOUND);
        } catch (KupiCoreException e) {
            log.error("Failed to assign default subscription for userId: {}", userId, e);
            return getInternalServerError();
        }
    }

}
