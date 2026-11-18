package com.kupi.cc.base.repository;

import com.kupi.cc.base.domain.pojo.PaymentOrderDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CachedRepository {

    @Autowired
    private RedisTemplate<String, PaymentOrderDto> paymentOrderTemplate;

    public void save(PaymentOrderDto paymentOrder) {
        paymentOrderTemplate.opsForValue().set(paymentOrder.getOrderId(), paymentOrder);
    }

    public PaymentOrderDto findById(String orderId) {
        return paymentOrderTemplate.opsForValue().get(orderId);
    }

    public boolean deleteById(String orderId) {
        return Boolean.TRUE.equals(paymentOrderTemplate.delete(orderId));
    }
}
