package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, String> {

    Optional<UserSubscription> findTopByUserIdOrderByCreatedAtDesc(String userId);

    boolean existsByUserSubscriptionPlanIdAndSubscriptionEndGreaterThan(String planId, long now);

}
