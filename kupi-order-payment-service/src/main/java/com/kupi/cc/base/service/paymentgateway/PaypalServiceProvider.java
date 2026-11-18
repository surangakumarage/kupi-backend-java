package com.kupi.cc.base.service.paymentgateway;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import com.kupi.cc.base.domain.pojo.PaymentOrderDto;
import com.kupi.cc.base.enums.CurrencyType;
import com.kupi.cc.base.exception.KupiOrderPaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class PaypalServiceProvider implements PaymentServiceProvider {

    private static final String APPROVAL_LINK_KEY = "approval_url";
    private static final String PAYMENT_CREATED_STATE = "created";
    private static final String PAYMENT_APPROVED_STATE = "approved";

    @Autowired
    private APIContext apiContext;


    @Override
    public PaymentOrderDto createOrder(BigDecimal fee, CurrencyType currency, String returnUrl, String cancelUrl) {
        Amount amount = new Amount(currency.toString(), fee.toString());
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        List<Transaction> transactionList = List.of(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setReturnUrl(returnUrl);
        redirectUrls.setCancelUrl(cancelUrl);

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactionList);
        payment.setRedirectUrls(redirectUrls);

        return executePayment(payment);
    }

    private PaymentOrderDto executePayment(Payment payment) {
        try {
            Payment createdPayment = payment.create(apiContext);
            if (createdPayment != null && PAYMENT_CREATED_STATE.equals(createdPayment.getState())) {

                List<Links> links = createdPayment.getLinks();
                var approvalLink = links.stream().filter(link -> APPROVAL_LINK_KEY.equals(link.getRel())).findAny()
                        .orElseThrow(() -> {
                            log.error("Creating an order in Paypal PSP failed. Cannot find approval link provided by PSP");
                            throw new KupiOrderPaymentException();
                        });

                return new PaymentOrderDto(createdPayment.getId(), approvalLink.getHref());
            }
            log.error("Creating an order in paypal PSP failed. PSP does not return created payment with correct status.");
        } catch (PayPalRESTException e) {
            log.error("Creating an order in paypal PSP failed.", e);
        }
        throw new KupiOrderPaymentException();
    }

    @Override
    public void captureOrder(String paymentId, String payerId) {
        Payment payment = new Payment();
        payment.setId(paymentId);

        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);
        try {
            Payment createdPayment = payment.execute(apiContext, paymentExecution);
            if(createdPayment != null && PAYMENT_APPROVED_STATE.equals(createdPayment.getState())){
                return;
            }
            log.error("Capturing an order for order-id: {} in paypal PSP failed. " +
                    "PSP does not return created payment with correct status", paymentId );
        } catch (PayPalRESTException e) {
            log.error("Capturing an order for order-id: {} in paypal PSP failed.", paymentId, e);
        }
        throw new KupiOrderPaymentException();
    }
}
