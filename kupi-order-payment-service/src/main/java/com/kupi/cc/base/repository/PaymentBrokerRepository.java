package com.kupi.cc.base.repository;


import com.kupi.cc.base.domain.entity.PaymentBroker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * PaymentBrokerRepository
 */
public interface PaymentBrokerRepository extends JpaRepository<PaymentBroker, String> {

    List<PaymentBroker> findByIsActiveTrueAndIsDeletedFalse();
}
