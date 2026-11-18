package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.entity.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderDetails, String> {

    @Query("SELECT orderDetails from OrderDetails as orderDetails where orderDetails.userId = :userId")
    List<OrderDetails> findAllByUserId(String userId);
}
