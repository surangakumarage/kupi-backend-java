package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.Subscribe;
import com.kupi.cc.base.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Subscriber repository.
 */
public interface SubscriberRepository extends JpaRepository<Subscribe, String> {

    /**
     * This method returns false when no user associated with subscription plan.
     *
     * @param subscriptionPlanId subscriptionPlanId
     * @return true/ false
     */
    boolean existsBySubscriptionPlanIdAndIsDeletedFalse(String subscriptionPlanId);

    /**
     * This method returns the user's latest subscribe.
     *
     * @param userId userId
     * @return subscribe
     */
    Optional<Subscribe> getByUserIdAndIsDeletedFalse(String userId);

    /**
     * This method will get user subscription by userId.
     *
     * @param userId userId
     * @return optional subscription
     */
    Optional<Subscribe> findByUserIdAndIsDeletedFalse(String userId);

    /**
     * This method is used to get number of subscribers for subscription plan.
     *
     * @param planId subscription plan id
     * @return subscribers count
     */
    long countSubscribeBySubscriptionPlanIdAndIsDeletedFalse(String planId);

    /**
     * This method will get the count of today's subscription by Bank/Merchant.
     *
     * @param userType     Bank/Merchant
     * @param currentMills currentMills
     * @return count of today's subscription by userType
     */
    long countSubscribesByUserTypeAndCreatedAtGreaterThanEqualAndIsDeletedFalse(UserType userType, long currentMills);

    /**
     * This query will get the count of unsubscription by Bank/Merchant.
     *
     * @param userType Bank/Merchant
     * @return count of unsubscription by userType
     */
    long countSubscribesByUserTypeAndIsDeletedFalse(UserType userType);

    /**
     * This method will get the count of unsubscription by Bank/Merchant.
     *
     * @param userType Bank/Merchant
     * @return count of unsubscription by userType
     */
    long countSubscribesByUserTypeAndIsDeletedTrue(UserType userType);

    /**
     * This method will get the count of today's subscription by Bank/Merchant.
     *
     * @param userType     Bank/Merchant
     * @param currentMills currentMills
     * @return count of today's subscription by userType
     */
    long countSubscribesByUserTypeAndUpdatedAtGreaterThanEqualAndIsDeletedTrue(UserType userType, long currentMills);

}