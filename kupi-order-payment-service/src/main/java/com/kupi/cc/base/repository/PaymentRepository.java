package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PaymentRepository extends JpaRepository<Payment, String> {
}
