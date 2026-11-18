package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.Subscribe;
import com.kupi.cc.base.domain.entity.SubscribeSearchIndex;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.repository.SubscribeSearchIndexRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * This class should replace from a search engine.
 */
@Service
public class SubscribeSearchIndexService {

    private static final String ALL = "ALL";
    private final SubscribeSearchIndexRepository subscribeSearchIndexRepository;

    public SubscribeSearchIndexService(SubscribeSearchIndexRepository subscribeSearchIndexRepository) {
        this.subscribeSearchIndexRepository = subscribeSearchIndexRepository;
    }

    /**
     * This method saves subscribeSearchIndex.
     *
     * @param subscribe    subscribe
     * @param businessName business name
     */
    public void save(Subscribe subscribe, String businessName) {
        try {
            var subscribeSearchIndex = new SubscribeSearchIndex(subscribe, businessName);
            subscribeSearchIndexRepository.save(subscribeSearchIndex);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Saving subscribeSearchIndex into database was failed.", e);
        }
    }

    /**
     * This method is used to get subscribe users by subscription plan id.
     *
     * @param pageable           pageable
     * @param subscriptionPlanId subscription plan id
     * @param searchTerm         search term
     * @return SubscribeSearchIndex page
     */
    public Page<SubscribeSearchIndex> getSubscribersBySubscriptionPlanId(Pageable pageable,
                                                                         String subscriptionPlanId, String searchTerm) {
        try {
            return (ALL.equals(searchTerm)) ?
                    subscribeSearchIndexRepository.getAllBySubscriptionPlanIdAndIsDeletedFalse(pageable, subscriptionPlanId) :
                    subscribeSearchIndexRepository
                            .getAllBySubscriptionPlanIdAndBusinessNameContainingAndIsDeletedFalse(pageable, subscriptionPlanId, searchTerm);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Returning subscribeSearchIndex by subscription id: " +
                    subscriptionPlanId + " into database was failed.", e);
        }
    }

    /**
     * This method used to remove subscribe data from the search index.
     *
     * @param subscribedId subscribedId
     */
    public void removeSubscribeDataFromSearchIndex(String subscribedId) {
        try {
            Optional<SubscribeSearchIndex> subscribeSearchIndexOptional =
                    subscribeSearchIndexRepository.findById(subscribedId);
            if (subscribeSearchIndexOptional.isPresent()) {
                SubscribeSearchIndex subscribeSearchIndex = subscribeSearchIndexOptional.get();
                subscribeSearchIndex.setDeleted(true);
                subscribeSearchIndexRepository.save(subscribeSearchIndex);
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException("Unsubscribe from the subscribeSearchIndex by subscribed id: "
                    + subscribedId + " was failed.");
        }
    }
}
