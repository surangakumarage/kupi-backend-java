package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.PaymentBroker;
import com.kupi.cc.base.domain.request.PaymentBrokerUpdateRequestDto;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.exception.KupiOrderPaymentException;
import com.kupi.cc.base.repository.PaymentBrokerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * PaymentServiceProvider
 */
@Slf4j
@Service
public class PaymentBrokerService {
    private final PaymentBrokerRepository paymentBrokerRepository;

    @Autowired
    public PaymentBrokerService(PaymentBrokerRepository paymentBrokerRepository) {
        this.paymentBrokerRepository = paymentBrokerRepository;
    }

    /**
     * This method used to save a payment broker.
     *
     * @param userId        user id
     * @param paymentBroker paymentBroker
     */
    public void savePaymentBroker(PaymentBroker paymentBroker, String userId) {
        try {
            paymentBroker.persistAuditor(userId);
            paymentBrokerRepository.save(paymentBroker);
        } catch (DataAccessException e) {
            log.error("Saving payment broker info into database was failed for user-id {}.", userId, e);
            throw new KupiOrderPaymentException("Saving payment broker info into database was failed.");
        }
    }

    /**
     * This method used to get payment broker by id.
     *
     * @param id paymentBrokerId
     * @return PaymentBroker
     */
    public PaymentBroker getPaymentBrokerById(String id) {
        try {
            return paymentBrokerRepository.findById(id).orElseThrow(() -> {
                log.error("Cannot find payment broker for provided id: {}", id);
                throw new KupiOrderPaymentException(ErrorResponseStatusType.INVALID_PAYMENT_BROKER,
                        "Invalid Payment Broker Id: " + id);
            });
        } catch (DataAccessException e) {
            log.error("Reading payment broker info from database was failed.", e);
            throw new KupiOrderPaymentException("Reading payment broker info from database was failed.");
        }
    }

    /**
     * This method used to update payment broker.
     *
     * @param userId                        user id
     * @param paymentBrokerUpdateRequestDto paymentBrokerUpdateRequestDto
     * @return PaymentBroker
     */
    public PaymentBroker updatePaymentBroker(PaymentBrokerUpdateRequestDto paymentBrokerUpdateRequestDto, String userId) {
        try {
            var paymentBrokerById = getPaymentBrokerById(paymentBrokerUpdateRequestDto.getId());
            paymentBrokerById.update(paymentBrokerUpdateRequestDto);
            paymentBrokerById.updateAuditor(userId);
            return paymentBrokerRepository.save(paymentBrokerById);
        } catch (DataAccessException e) {
            log.error("Updating payment broker by id: {} was failed.", paymentBrokerUpdateRequestDto.getId(), e);
            throw new KupiOrderPaymentException("Updating payment broker by id: "
                    + paymentBrokerUpdateRequestDto.getId() + " was failed.");
        }
    }

    /**
     * This method used to delete paymentBroker by id.
     *
     * @param userId          user id
     * @param paymentBrokerId paymentBrokerId
     */
    public void deletePaymentBroker(String paymentBrokerId, String userId) {
        try {
            PaymentBroker paymentBrokerById = getPaymentBrokerById(paymentBrokerId);
            paymentBrokerById.setDeleted(true);
            paymentBrokerById.updateAuditor(userId);
            paymentBrokerRepository.save(paymentBrokerById);
        } catch (DataAccessException e) {
            log.error("Deleting payment broker by id: {} was failed.", paymentBrokerId, e);
            throw new KupiOrderPaymentException("Deleting payment broker by id: "+ paymentBrokerId + " was failed.");
        }
    }

    /**
     * This method return payment broker list for given userId
     *
     * @param userId userId
     * @return paymentBrokerList
     */
    public List<PaymentBroker> getAllPaymentBrokers(String userId) {
        try {
            return paymentBrokerRepository.findByIsActiveTrueAndIsDeletedFalse();
        } catch (DataAccessException e) {
            log.error("Error accessing repository to find active payment broker details for user-id: " + userId, e);
            throw new KupiOrderPaymentException("Error accessing repository to find active payment broker details for user-id: " +
                    userId);
        }
    }

}