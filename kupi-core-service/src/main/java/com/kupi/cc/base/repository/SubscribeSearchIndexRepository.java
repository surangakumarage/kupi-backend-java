package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.SubscribeSearchIndex;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * This interface should replace from a search engine.
 */
public interface SubscribeSearchIndexRepository extends JpaRepository<SubscribeSearchIndex, String> {

    /**
     * This method is used to get all subscription users for subscription plan by id and isDelete False.
     *
     * @param pageable       pageable
     * @param subscriptionId subscription id
     * @return SubscribeSearchIndex page
     */
    Page<SubscribeSearchIndex> getAllBySubscriptionPlanIdAndIsDeletedFalse(Pageable pageable, String subscriptionId);

    /**
     * This method used to get all subscription users for subscription plan by id, search term and isDelete False.
     *
     * @param pageable       pageable
     * @param subscriptionId subscription id
     * @param searchTerm     search term
     * @return SubscribeSearchIndex page
     */
    Page<SubscribeSearchIndex> getAllBySubscriptionPlanIdAndBusinessNameContainingAndIsDeletedFalse(Pageable pageable,
                                                                                                    String subscriptionId, String searchTerm);
}
