package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.UserSubscription;
import com.kupi.cc.base.exception.*;
import com.kupi.cc.base.repository.UserSubscriptionRepository;
import org.springframework.stereotype.Service;

@Service
public class UserSubscriptionRules {

    private final UserSubscriptionRepository userSubscriptionRepository;

    public UserSubscriptionRules(UserSubscriptionRepository userSubscriptionRepository) {
        this.userSubscriptionRepository = userSubscriptionRepository;
    }

    /** Find latest subscription for a user and ensure it’s not expired. */
    public UserSubscription getActiveSubscriptionOrThrow(String userId) {
        UserSubscription sub = userSubscriptionRepository
                .findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new UserSubscriptionNotFound("No subscription found for user: " + userId));

        if (sub.getSubscriptionEnd() < System.currentTimeMillis()) {
            throw new UserSubscriptionExpiredException("Subscription expired for user: " + userId);
        }
        return sub;
    }

    /** Ensure coupons are still available on the subscription. */
    public void validateCouponAvailability(UserSubscription sub) {
        int limit = sub.getUserSubscriptionPlan().getCouponLimit();
        if (sub.getUsedCouponCount() >= limit) {
            throw new ExceededCouponLimitException("Coupon limit exceeded for user: " + sub.getUserId());
        }
    }

    /** True if at least one active (unexpired) subscription exists for this plan. */
    public boolean hasActiveSubscriptionsForPlan(String planId) {
        long now = System.currentTimeMillis();
        return userSubscriptionRepository
                .existsByUserSubscriptionPlanIdAndSubscriptionEndGreaterThan(planId, now);
    }

    /** Throw if the plan is still in use by any active subscription. */
    public void validateNoActiveSubscriptionsForPlan(String planId) {
        if (hasActiveSubscriptionsForPlan(planId)) {
            throw new PlanHasActiveUsersException("Cannot delete the plan. active subscriptions exist for plan: " + planId);
        }
    }
}
