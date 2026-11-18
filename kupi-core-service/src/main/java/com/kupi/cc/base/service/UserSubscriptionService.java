package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.UserSubscription;
import com.kupi.cc.base.domain.entity.UserSubscriptionPlan;
import com.kupi.cc.base.domain.request.UserSubscriptionRequestDto;
import com.kupi.cc.base.domain.response.DateResponseDto;
import com.kupi.cc.base.domain.response.UserSubscriptionStatusResponse;
import com.kupi.cc.base.exception.*;
import com.kupi.cc.base.repository.UserSubscriptionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.sql.Date;

@Service
public class UserSubscriptionService {

    private final String defaultPlanName;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserSubscriptionPlanService userSubscriptionPlanService;
    private final UserSubscriptionRules userSubscriptionRules;

    public UserSubscriptionService(@Value("${userSubscription.default-plan-name}") String defaultPlanName, UserSubscriptionRepository userSubscriptionRepository,
                                   UserSubscriptionPlanService userSubscriptionPlanService, UserSubscriptionRules userSubscriptionRules) {
        this.defaultPlanName = defaultPlanName;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.userSubscriptionPlanService = userSubscriptionPlanService;
        this.userSubscriptionRules = userSubscriptionRules;
    }

    @PostConstruct
    void logIt() { System.out.println("Default plan = [" + defaultPlanName + "]"); }

    /**
     * This method is used to create a user subscription.
     *
     * @param userSubscriptionRequestDto userSubscriptionRequestDto
     */
    @Transactional
    public void createUserSubscription(UserSubscriptionRequestDto userSubscriptionRequestDto) {
        try {
            UserSubscriptionPlan userSubscriptionPlan = userSubscriptionPlanService
                    .getUserSubscriptionPlanById(userSubscriptionRequestDto.getSubscriptionPlanId());
            UserSubscription userSubscription = new UserSubscription(userSubscriptionRequestDto, userSubscriptionPlan);
            userSubscriptionRepository.save(userSubscription);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Creating user subscription in the database failed.", e);
        }
    }

    /**
     * This method increments the usedCouponCount for the active user subscription.
     *
     * @param userId user id
     */
    @Transactional
    public void incrementUsedCouponCount(String userId) {
        try {
            UserSubscription sub = userSubscriptionRules.getActiveSubscriptionOrThrow(userId);
            userSubscriptionRules.validateCouponAvailability(sub);
            sub.setUsedCouponCount(sub.getUsedCouponCount() + 1);
            sub.setUpdatedAt(System.currentTimeMillis());
            userSubscriptionRepository.save(sub);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Failed to increment coupon usage for user: " + userId, e);
        }
    }


    public void verifyCouponAvailability(String userId) {
        try {
            UserSubscription sub = userSubscriptionRules.getActiveSubscriptionOrThrow(userId);
            userSubscriptionRules.validateCouponAvailability(sub);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Failed to verify coupon availability for user: " + userId, e);
        }
    }


    /**
     * This method returns the userSubscriptionStatus for the given user id.
     *
     * @param userId   user id
     * @param timezone time zone
     * @return UserSubscriptionStatusResponse
     */
    public UserSubscriptionStatusResponse getUserSubscriptionStatus(String userId, String timezone) {
        try {
            UserSubscription subscription = userSubscriptionRepository
                    .findTopByUserIdOrderByCreatedAtDesc(userId)
                    .orElseThrow(() ->
                            new UserSubscriptionNotFound("No active subscription found for user: " + userId));

            long now = System.currentTimeMillis();
            boolean isExpired = subscription.getSubscriptionEnd() < now;
            int total = subscription.getUserSubscriptionPlan().getCouponLimit();
            int used = subscription.getUsedCouponCount();
            int available = Math.max(total - used, 0);
            long remainingDays = isExpired ? 0 :
                    (subscription.getSubscriptionEnd() - now) / (24 * 60 * 60 * 1000);

            DateResponseDto subscriptionEndDate =
                    new DateResponseDto(subscription.getSubscriptionEnd(), timezone, new Date(subscription.getSubscriptionEnd()));

            return new UserSubscriptionStatusResponse(isExpired, used, available, remainingDays,
                    subscriptionEndDate, subscription.getUserSubscriptionPlan().getName(), subscription.getUserSubscriptionPlan().getId());
        } catch (DataAccessException e) {
            throw new KupiCoreException("Failed to get coupon availability for user: " + userId, e);
        }
    }


    @Transactional
    public void assignDefaultSubscription(String userId) {
        try {
            UserSubscriptionPlan plan = userSubscriptionPlanService.getActivePlanByName(defaultPlanName);
            UserSubscriptionRequestDto subscriptionRequestDto = new UserSubscriptionRequestDto();
            subscriptionRequestDto.setUserId(userId);
            subscriptionRequestDto.setSubscriptionPlanId(plan.getId());
            createUserSubscription(subscriptionRequestDto);
        } catch (InvalidUserSubscriptionPlanException e) {
            throw new InvalidUserSubscriptionPlanException("Invalid user subscription plan: " + defaultPlanName + e);
        } catch (Exception e) {
            throw new KupiCoreException("Failed to assign default subscription for user: " + userId, e);
        }
    }
}
