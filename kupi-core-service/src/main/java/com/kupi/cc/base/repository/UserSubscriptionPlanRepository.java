package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.UserSubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSubscriptionPlanRepository extends JpaRepository<UserSubscriptionPlan, String> {

    boolean existsByNameAndDeletedFalse(String name);

    boolean existsByNameAndIdNotAndDeletedFalse(String name, String id);

    List<UserSubscriptionPlan> findAllByIsActiveTrueAndDeletedFalseOrderByPriceAsc();

    Optional<UserSubscriptionPlan> findByNameIgnoreCaseAndIsActiveTrue(String name);



}
