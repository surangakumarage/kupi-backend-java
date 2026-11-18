package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.FavouriteDeal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Favourite deal repository
 */
public interface FavouriteDealRepository extends JpaRepository<FavouriteDeal, String> {

    boolean existsByUserIdAndDealId(String userId, String dealId);

    void deleteByUserIdAndDealId(String userId, String dealId);

    List<FavouriteDeal> findByUserIdAndDealIdIn(String userId, List<String> dealIds);

    /**
     * Find all favourite deals for a given user.
     *
     * @param userId user id
     * @return list of FavouriteDeal
     */
    List<FavouriteDeal> findByUserId(String userId);
}


