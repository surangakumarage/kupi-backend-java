package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.PaymentBroker;
import com.kupi.cc.base.domain.request.PaymentBrokerRequestDto;
import com.kupi.cc.base.domain.request.PaymentBrokerUpdateRequestDto;
import com.kupi.cc.base.domain.response.PaymentBrokerListResponseDto;
import com.kupi.cc.base.domain.response.PaymentBrokerResponseDto;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.exception.KupiOrderPaymentException;
import com.kupi.cc.base.service.PaymentBrokerService;
import com.kupi.cc.base.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/v1/payment-broker")
public class PaymentBrokerController extends Controller {

    private final PaymentBrokerService paymentBrokerService;

    @Autowired
    public PaymentBrokerController(Translator translator, PaymentBrokerService paymentBrokerService) {
        super(translator);
        this.paymentBrokerService = paymentBrokerService;
    }

    /**
     * This endpoint used to save a payment broker.
     *
     * @param paymentBrokerRequestDto paymentBrokerRequestDto
     * @return paymentBrokerResponseDto
     */
    @PostMapping("")
    public ResponseEntity<ResponseWrapper> savePaymentBroker(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestBody PaymentBrokerRequestDto paymentBrokerRequestDto) {
        try {
            if (!paymentBrokerRequestDto.isRequiredAvailable()) {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            var paymentBroker = new PaymentBroker(paymentBrokerRequestDto);
            paymentBrokerService.savePaymentBroker(paymentBroker, userId);
            var paymentBrokerResponseDto = new PaymentBrokerResponseDto(paymentBroker);
            return getSuccessResponse(paymentBrokerResponseDto, SuccessResponseStatusType.SAVE_PAYMENT_BROKER);
        } catch (KupiOrderPaymentException e) {
            log.error("Saving payment broker info was failed.", e);
            return getInternalServerError();
        }
    }


    /**
     * This endpoint used to update payment broker.
     *
     * @param paymentBrokerUpdateRequestDto paymentBrokerUpdateRequestDto
     * @return paymentBrokerResponseDto
     */
    @PutMapping("")
    public ResponseEntity<ResponseWrapper> updatePaymentBroker(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @RequestBody PaymentBrokerUpdateRequestDto paymentBrokerUpdateRequestDto) {
        try {
            if (!paymentBrokerUpdateRequestDto.isRequiredAvailable()) {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }
            PaymentBroker paymentBroker = paymentBrokerService.updatePaymentBroker(paymentBrokerUpdateRequestDto, userId);
            var paymentBrokerResponseDto = new PaymentBrokerResponseDto(paymentBroker);
            return getSuccessResponse(paymentBrokerResponseDto, SuccessResponseStatusType.UPDATE_PAYMENT_BROKER);

        } catch (KupiOrderPaymentException e) {
            log.error("Error updating payment broker details for user-id: {}. Reason: {}", userId, e.getMessage());
            return getBadRequestError(e.getErrorResponseType());
        }
    }

    /**
     * This endpoint used to get payment broker by id.
     *
     * @param paymentBrokerId paymentBrokerId
     * @return paymentBrokerResponseDto
     */
    @GetMapping("/{paymentBrokerId}")
    public ResponseEntity<ResponseWrapper> getPaymentBrokerById(
            @PathVariable String paymentBrokerId,
            @RequestHeader(name = USER_ID_HEADER) String userId) {
        try {
            var paymentBrokerById = paymentBrokerService.getPaymentBrokerById(paymentBrokerId);
            var paymentBrokerResponseDto = new PaymentBrokerResponseDto(paymentBrokerById);
            return getSuccessResponse(paymentBrokerResponseDto, SuccessResponseStatusType.READ_PAYMENT_BROKER);

        } catch (KupiOrderPaymentException e) {
            log.error("Error retrieving payment broker details for user-id: {}.", userId, e);
            return getBadRequestError(e.getErrorResponseType());
        }
    }

    /**
     * This endpoint used to delete payment broker by id.
     *
     * @param paymentBrokerId paymentBrokerId
     * @return SuccessResponse/ ErrorResponse
     */
    @DeleteMapping("/{paymentBrokerId}")
    public ResponseEntity<ResponseWrapper> deletePaymentBroker(
            @RequestHeader(name = USER_ID_HEADER) String userId,
            @PathVariable String paymentBrokerId) {
        try {
            paymentBrokerService.deletePaymentBroker(paymentBrokerId, userId);
            return getSuccessResponse(null, SuccessResponseStatusType.DELETE_ADDRESS);

        } catch (KupiOrderPaymentException e) {
            log.error("Error deleting payment broker details for user-id: {}. Reason: {}", userId, e.getMessage());
            return getBadRequestError(e.getErrorResponseType());
        }
    }

    /**
     * This method used to get all payment brokers
     *
     * @param userId userId
     * @return paymentBrokerListDto
     */
    @GetMapping("")
    public ResponseEntity<ResponseWrapper> getPaymentBrokerDetails(
            @RequestHeader(name = USER_ID_HEADER) String userId) {
        try {
            // It should be return by country code currency type.
            List<PaymentBroker> paymentBrokers = paymentBrokerService.getAllPaymentBrokers(userId);
            var paymentBrokerListDto = new PaymentBrokerListResponseDto(paymentBrokers);
            return getSuccessResponse(paymentBrokerListDto, SuccessResponseStatusType.READ_PAYMENT_BROKER);
        } catch (KupiOrderPaymentException e) {
            log.error("Failed to retrieve payment broker details for user-id: {}", userId, e);
            return getInternalServerError();
        }
    }
}
