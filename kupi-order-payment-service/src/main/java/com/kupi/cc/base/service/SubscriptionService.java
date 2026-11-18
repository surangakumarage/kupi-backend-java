package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.Subscription;
import com.kupi.cc.base.domain.request.BulkUserRequestDto;
import com.kupi.cc.base.domain.request.SubscriptionValidationRequestDto;
import com.kupi.cc.base.domain.response.BasicBusinessResponseDto;
import com.kupi.cc.base.domain.response.BasicSubscriptionPlanResponseDto;
import com.kupi.cc.base.domain.response.SubscriptionPlanListResponseDto;
import com.kupi.cc.base.domain.response.SubscriptionValidationResponseDto;
import com.kupi.cc.base.enums.CurrencyType;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SubscriptionStatus;
import com.kupi.cc.base.exception.KupiOrderPaymentException;
import com.kupi.cc.base.repository.SubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * Subscription Service
 */

@Service
@Slf4j
public class SubscriptionService {

    private static final String ALL = "ALL";
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    @Lazy
    private CoreService coreService;
    @Autowired
    private AuthService authService;

    /**
     * create save temporary subscription
     *
     * @param subscription subscription object
     * @param userId       user id
     */
    public void saveTemporarySubscription(Subscription subscription, String userId) {
        subscription.setSubscriptionStatus(SubscriptionStatus.TEMPORARY);
        subscription.persistAuditor(userId);
        saveSubscription(subscription);
    }

    /**
     * save free subscription
     *
     * @param subscription subscription object
     * @param userId       user id
     */
    public void saveFreeSubscription(Subscription subscription, String userId) {
        subscription.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        subscription.persistAuditor(userId);
        cancelOldSubscriptions(userId);
        saveSubscription(subscription);
    }

    /**
     * update old subscription records status to INACTIVE before subscribe into new plan
     *
     * @param userId user id
     */
    private void cancelOldSubscriptions(String userId) {
        try {
            Optional<Subscription> oldSubscriptionOptional = subscriptionRepository.findByUserIdAndSubscriptionStatusActive(userId);
            if (oldSubscriptionOptional.isPresent()) {
                Subscription oldSubscription = oldSubscriptionOptional.get();
                oldSubscription.setSubscriptionStatus(SubscriptionStatus.INACTIVE);
                oldSubscription.updateAuditor(userId);
                subscriptionRepository.saveAndFlush(oldSubscription);
            }
        } catch (DataAccessException e) {
            log.error("Error updating old subscription details for user-id: {}", userId, e);
            throw new KupiOrderPaymentException();
        }
    }

    private void saveSubscription(Subscription subscription) {
        try {
            subscriptionRepository.saveAndFlush(subscription);
        } catch (DataAccessException | AssertionError e) {
            log.error("Saving subscription into database was failed.", e);
            throw new KupiOrderPaymentException();
        }
    }

    /**
     * find subscription object from id
     *
     * @param subscriptionId subscription  id
     * @return subscription object
     */
    public Subscription getSubscriptionFromId(String subscriptionId) {
        try {
            var subscriptionOptional = subscriptionRepository.findById(subscriptionId);
            if (subscriptionOptional.isPresent()) {
                return subscriptionOptional.get();
            }
            throw new KupiOrderPaymentException(ErrorResponseStatusType.INVALID_SUBSCRIPTION_REQUEST,
                    "Temporary subscription plan does not exist: " + subscriptionId);
        } catch (DataAccessException e) {
            log.error("Error while reading subscription details for subscription-id: {}", subscriptionId, e);
            throw new KupiOrderPaymentException();
        }
    }

    public void saveCompletedSubscription(Subscription temporarySubscription, String userId) {
        temporarySubscription.updateWithCompletedState();
        temporarySubscription.updateAuditor(userId);
        cancelOldSubscriptions(userId);
        saveSubscription(temporarySubscription);
    }

    public Subscription buildSubscriptionObject(
            SubscriptionValidationResponseDto validationResponseDto,
            SubscriptionValidationRequestDto subscriptionValidationRequestDto, String userId) {
        Subscription subscription = new Subscription();
        subscription.setUserId(userId);
        subscription.setUserType(subscriptionValidationRequestDto.getUserType());
        subscription.setCurrencyType(CurrencyType.USD);
        subscription.setSubscriptionPlanId(subscriptionValidationRequestDto.getSubscriptionPlanId());
        subscription.setSubscriptionFee(validationResponseDto.getAmount().getAmount());
        subscription.setSubscriptionType(validationResponseDto.getPlanType());
        subscription.setTitle(validationResponseDto.getTitle());
        subscription.setSubTitle(validationResponseDto.getSubTitle());
        return subscription;
    }

    /**
     * This method is returns subscription order list
     *
     * @param userId     userId
     * @param searchTerm searchTerm
     * @param pageable   pageable
     * @param filter     filter
     * @return subscription order list
     */
    public Page<Subscription> getSubscriptionListBySubscriptionId(String userId, String searchTerm, Pageable pageable,
                                                                  String filter) {
        try {
            if (searchTerm.equals(ALL) && filter.equals(ALL)) {
                return subscriptionRepository.findBySubscriptionStatus(SubscriptionStatus.ACTIVE, pageable);
            } else if (searchTerm.equals(ALL)) {
                return subscriptionRepository.findAllBySubscriptionStatusAndSubscriptionPlanId(
                        SubscriptionStatus.ACTIVE, filter, pageable);
            } else if (filter.equals(ALL)) {
                return subscriptionRepository.findAllBySubscriptionStatusAndTitleContains(
                        SubscriptionStatus.ACTIVE, searchTerm, pageable);
            } else {
                return subscriptionRepository.findAllBySubscriptionStatusAndSubscriptionPlanIdAndTitleContains(
                        SubscriptionStatus.ACTIVE, filter, searchTerm, pageable);
            }
        } catch (DataAccessException e) {
            throw new KupiOrderPaymentException("Getting subscription payment list was failed for userId: " + userId, e);
        }
    }

    /**
     * This method list merchant
     *
     * @param userIdList userIdList
     * @param timeZone   timeZone
     * @return BasicBusinessResponseDto
     */
    public Map<String, BasicBusinessResponseDto> getMerchantsList(List<String> userIdList, String timeZone) {
        try {
            return userIdList.isEmpty() ? new HashMap<>() :
                    authService.getMerchantMap(new BulkUserRequestDto(userIdList), timeZone);
        } catch (DataAccessException e) {
            throw new KupiOrderPaymentException("Getting merchant plan was failed: " + e);
        }
    }

    /**
     * this method will return subscription plan.
     *
     * @param userId    userId
     * @param authToken authToken
     * @param timeZone  timeZone
     * @return subscription plan.
     */
    public SubscriptionPlanListResponseDto getSubscriptionPlan(String userId, String authToken, String timeZone) {
        try {
            SubscriptionPlanListResponseDto subscriptionPlan = coreService.getSubscriptionPlanCount(authToken, userId, timeZone);
            for (BasicSubscriptionPlanResponseDto basicSubscriptionPlanResponseDto : subscriptionPlan.getPlans()) {
                basicSubscriptionPlanResponseDto.setCount(subscriptionRepository.countBySubscriptionPlanId(basicSubscriptionPlanResponseDto.getId()));
            }
            return subscriptionPlan;
        } catch (DataAccessException e) {
            throw new KupiOrderPaymentException("Getting subscription plan was failed: " + e);
        }
    }
}
