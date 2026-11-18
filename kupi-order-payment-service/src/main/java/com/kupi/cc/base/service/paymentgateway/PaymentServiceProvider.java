package com.kupi.cc.base.service.paymentgateway;

import com.kupi.cc.base.domain.pojo.PaymentOrderDto;
import com.kupi.cc.base.domain.response.ServiceResponse;
import com.kupi.cc.base.enums.CurrencyType;
import com.kupi.cc.base.exception.KupiOrderPaymentException;

import java.math.BigDecimal;
import java.util.Properties;

public interface PaymentServiceProvider {

    default ServiceResponse charge() {
        throw new KupiOrderPaymentException("Not yet implemented");
    }

    default ServiceResponse refund() {
        throw new KupiOrderPaymentException("Not yet implemented");
    }

    default ServiceResponse reverse() {
        throw new KupiOrderPaymentException("Not yet implemented");
    }

    default ServiceResponse deferredResponseHandle() {
        throw new KupiOrderPaymentException("Not yet implemented");
    }

    default ServiceResponse paymentResponseConverter() {
        throw new KupiOrderPaymentException("Not yet implemented");
    }

    default ServiceResponse paymentRequestConverter() {
        throw new KupiOrderPaymentException("Not yet implemented");
    }

    default Properties getBrokerProperties() {
        throw new KupiOrderPaymentException("Not yet implemented");
    }



    default PaymentOrderDto createOrder(BigDecimal amount, CurrencyType currency, String returnUrl, String cancelUrl) {
        throw new KupiOrderPaymentException("Not yet implemented");
    }

    default void captureOrder(String paymentId, String payerId) {
        throw new KupiOrderPaymentException("Not yet implemented");
    }

}
