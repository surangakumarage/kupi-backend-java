package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.Subscription;
import com.kupi.cc.base.enums.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Subscription Repository
 */

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

    @Query("SELECT s FROM Subscription s WHERE " +
            "s.subscriptionStatus = com.kupi.cc.base.enums.SubscriptionStatus.ACTIVE " +
            "AND s.userId = :userId")
    Optional<Subscription> findByUserIdAndSubscriptionStatusActive(@Param("userId") String userId);

    /**
     * This method list subscription payment by id
     *
     * @param subscriptionStatus subscription status
     * @param subscriptionPlanId subscriptionPlanId
     * @param pageable           pageable
     * @return Subscription order page
     */
    Page<Subscription> findAllBySubscriptionStatusAndSubscriptionPlanId(
            SubscriptionStatus subscriptionStatus, String subscriptionPlanId, Pageable pageable);

    /**
     * This method list subscription payment by id and searchTerm
     *
     * @param subscriptionStatus subscription status
     * @param id                 id
     * @param searchTerm         searchTerm
     * @param pageable           pageable
     * @return Subscription order page
     */
    Page<Subscription> findAllBySubscriptionStatusAndSubscriptionPlanIdAndTitleContains(
            SubscriptionStatus subscriptionStatus, String id, String searchTerm, Pageable pageable);

    /**
     * This method list subscription payment by searchTerm
     *
     * @param subscriptionStatus subscription status
     * @param searchTerm         searchTerm
     * @param pageable           pageable
     * @return Subscription order page
     */
    Page<Subscription> findAllBySubscriptionStatusAndTitleContains(
            SubscriptionStatus subscriptionStatus, String searchTerm, Pageable pageable);

    /**
     * This method returns count of subscription plan.
     *
     * @param subscriptionPlanId subscriptionPlanId
     * @return count of subscription plan.
     */
    long countBySubscriptionPlanId(String subscriptionPlanId);

    /**
     * This method returns subscription payment by status
     *
     * @param subscriptionStatus subscription status
     * @param pageable           pageable
     * @return subscription order page
     */
    Page<Subscription> findBySubscriptionStatus(SubscriptionStatus subscriptionStatus, Pageable pageable);
}
