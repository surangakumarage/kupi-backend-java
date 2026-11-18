package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.SubscriptionPlan;
import com.kupi.cc.base.domain.response.SubscriptionPlanListResponseDto;
import com.kupi.cc.base.enums.SubscriptionStatus;
import com.kupi.cc.base.enums.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, String> {

    /**
     * This method returns true when category name exist in database.
     *
     * @param title    title
     * @param userType userType
     * @return true/ false
     */
    boolean existsByTitleAndUserRole(String title, UserType userType);

    /**
     * This method returns subscription plan page by userType
     *
     * @param pageable pageable
     * @return Subscription plan page
     */
    Page<SubscriptionPlan> getAllByUserRole(Pageable pageable, UserType userType);

    /**
     * This method returns subscription plan page by user type and search term.
     *
     * @param pageable   pageable
     * @param searchTerm search Term
     * @param userType   userType
     * @return Subscription plan page
     */
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.userRole=:userType AND sp.title LIKE %:searchTerm%" +
            " OR sp.subTitle LIKE %:searchTerm%")
    Page<SubscriptionPlan> findAllBySearchTermContaining(Pageable pageable,
                                                         UserType userType, String searchTerm);

    /**
     * This method returns active subscription plan by user type and status.
     *
     * @param pageable pageable
     * @param userType user type
     * @param status   status
     * @return Subscription plan page
     */
    Page<SubscriptionPlan> getAllByUserRoleAndStatus(Pageable pageable,
                                                     UserType userType, SubscriptionStatus status);

    /**
     * This method returns active subscription plan by user type, status and search term.
     *
     * @param pageable   pageable
     * @param userType   user type
     * @param searchTerm search term
     * @param status     status
     * @return Subscription plan page
     */
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.userRole=:userType " +
            "AND sp.status=:status AND (sp.title LIKE %:searchTerm% OR sp.subTitle LIKE %:searchTerm%)")
    Page<SubscriptionPlan> findAllActiveStatusBySearchTermContaining(Pageable pageable, UserType userType,
                                                                     String searchTerm, SubscriptionStatus status);

    /**
     * This method returns true when name exist with other subscription plan.
     *
     * @param title    title
     * @param userType userType
     * @param id       subscription plan id
     * @return true/ false
     */
    boolean existsByTitleAndUserRoleAndIdNot(String title, UserType userType, String id);

    /**
     * This method returns count of subscription plan by Bank/Merchant.
     *
     * @param userType Bank/Merchant
     * @return count of subscription plan for userType
     */
    long countSubscriptionPlansByUserRole(UserType userType);

    /**
     * This method returns count of active subscription plan by Bank/Merchant.
     *
     * @param userType Bank/Merchant
     * @return count of active subscription plan for userType
     */
    long countSubscriptionPlansByUserRoleAndStatus(UserType userType, SubscriptionStatus status);

    /**
     * This method returns count of today's subscription plan by Bank/Merchant.
     *
     * @param userType     Bank/Merchant
     * @param dateInMillis dateInMillis
     * @return count of today's subscription plan for userType
     */
    long countSubscriptionPlansByUserRoleAndCreatedAtGreaterThanEqual(UserType userType, long dateInMillis);
}
