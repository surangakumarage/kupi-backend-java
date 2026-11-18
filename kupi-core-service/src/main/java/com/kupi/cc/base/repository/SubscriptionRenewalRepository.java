package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.SubscriptionRenewal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * SubscriptionRenewal Repository
 */
public interface SubscriptionRenewalRepository extends JpaRepository<SubscriptionRenewal, String> {

    /**
     * This method find entries by renewal date greater than or equal to today.
     *
     * @param renewalDay renewalDay
     * @param pageable   pageable
     * @return subscription renewal page.
     */
    Page<SubscriptionRenewal> findByRenewOnDayOfEveryMonthGreaterThanEqualAndIsUnsubscribedFalse(int renewalDay,
                                                                                                 Pageable pageable);

    /**
     * This method removes unsubscribed subscriptions where renewal day is greater than or equal to given day.
     *
     * @param renewalDay renewalDay
     */
    @Transactional
    void deleteByRenewOnDayOfEveryMonthGreaterThanEqualAndIsUnsubscribedTrue(int renewalDay);

    /**
     * This method find entries by renewal date.
     *
     * @param renewalDay renewalDay
     * @param pageable   pageable
     * @return subscription renewal page.
     */
    Page<SubscriptionRenewal> findByRenewOnDayOfEveryMonthAndIsUnsubscribedFalse(int renewalDay, Pageable pageable);

    /**
     * This method removes unsubscribed subscriptions where renewal day is given day.
     *
     * @param renewalDay renewalDay
     */
    @Transactional
    void deleteByRenewOnDayOfEveryMonthAndIsUnsubscribedTrue(int renewalDay);

    /**
     * This method gets renewal entries by userId.
     *
     * @param userId userId
     * @return subscription renewal.
     */
    Optional<SubscriptionRenewal> findByUserId(String userId);
}
