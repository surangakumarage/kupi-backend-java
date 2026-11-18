package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.Payable;
import com.kupi.cc.base.domain.entity.Subscription;
import com.kupi.cc.base.domain.response.BasicBusinessResponseDto;
import com.kupi.cc.base.domain.response.SubscriptionListResponseDto;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.exception.KupiOrderPaymentException;
import com.kupi.cc.base.service.SubscriptionService;
import com.kupi.cc.base.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Subscription Controller
 */

@Slf4j
@RestController
@Validated
@RequestMapping("api/v1/subscription")
public class SubscriptionController extends Controller {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(Translator translator, SubscriptionService subscriptionService) {
        super(translator);
        this.subscriptionService = subscriptionService;
    }

    /**
     * This method returns Subscription payment order list.
     *
     * @param userId     userId
     * @param page       page
     * @param size       size
     * @param searchTerm searchTerm
     * @return Subscription list
     */
    @GetMapping(path = "/list-subscription-payment/{page}/{size}/search/{searchTerm}/filter/{filter}",
            consumes = APPLICATION_JSON_UTF_8,
            produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> getSubscriptionList(@RequestHeader(name = HEADER_USER_ID) String userId,
                                                               @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                               @PathVariable String filter,
                                                               @Min(DEFAULT_PAGE) @PathVariable("page") int page,
                                                               @Positive @Max(PAGE_MAX_SIZE) @PathVariable("size")
                                                               int size, @PathVariable String searchTerm) {
        if (isEmpty(userId)) {
            return getBadRequestError(ErrorResponseStatusType.INVALID_USER_ID);
        }
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(TITLE_SORT).ascending());
            Page<Subscription> subscriptionPage = subscriptionService.getSubscriptionListBySubscriptionId(userId,
                    searchTerm, pageable, filter);
            List<String> userIdList = subscriptionPage.stream().map(Payable::getUserId).collect(Collectors.toList());
            Map<String, BasicBusinessResponseDto> userMap = subscriptionService.getMerchantsList(userIdList, timeZone);
            var subscriptionSuccessDto = new SubscriptionListResponseDto(subscriptionPage, timeZone, userMap);
            log.debug("Successfully returned all subscription payment list for page: {}, size {} for userId: {}",
                    page, size, userId);
            return getSuccessResponse(subscriptionSuccessDto, SuccessResponseStatusType.READ_SUBSCRIBE_PAYMENT_LIST);
        } catch (KupiOrderPaymentException e) {
            log.error("Failed to return subscription list for userId: {}.", userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method will return subscription plan.
     *
     * @param userId   userId
     * @param timeZone timeZone
     * @param request  request
     * @return subscriptionPlan
     */
    @GetMapping(path = "/subscription-plan", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> getSubscriptionPlan(@RequestHeader(name = HEADER_USER_ID) String userId,
                                                               @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
                                                               HttpServletRequest request) {
        String authToken = request.getHeader(AUTH_TOKEN_HEADER);
        try {
            var subscriptionPlan = subscriptionService.getSubscriptionPlan(userId, authToken,
                    timeZone);
            log.debug("Successfully returned all subscription plan for userId: {}", userId);
            return getSuccessResponse(subscriptionPlan, SuccessResponseStatusType.READ_SUBSCRIBE_PAYMENT_COUNT);
        } catch (KupiOrderPaymentException e) {
            log.error("Failed to return subscription plan for userId: {}.", userId, e);
            return getInternalServerError();
        }
    }
}