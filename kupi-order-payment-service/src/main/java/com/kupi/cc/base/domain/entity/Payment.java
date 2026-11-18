package com.kupi.cc.base.domain.entity;

import com.kupi.cc.base.enums.PaymentStatus;
import lombok.*;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payment")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends Auditable {
    private static final long serialVersionUID = -1507943746339425558L;

    @Transient
    private static final String PAYMENT_ID_PREFIX = "pid-";

    @Id
    private String id;

    @Column(nullable = false)
    private BigDecimal amount;

    private String request;
    private String response;

    @Column(nullable = false)
    private String stateSequence;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Payable payable;

    /**
     * make the payment object for a subscription
     * (subscriptions can have multiple payment requests, but only one should complete
     * @param subscription      subscription this payment belongs to
     */
    public Payment(Subscription subscription) {
        this.id = PAYMENT_ID_PREFIX + UUID.randomUUID();
        this.amount = subscription.getSubscriptionFee();
        this.paymentStatus = PaymentStatus.ORDER_CREATED;
        this.payable = subscription;
        updatedStateSequence(PaymentStatus.ORDER_CREATED);
    }

    public void updatedStateSequence(PaymentStatus state) {
        if (PaymentStatus.ORDER_CREATED.equals(state)) {
            stateSequence = state.getValue();
        } else {
            assert stateSequence != null && !stateSequence.isBlank() : "State sequence cannot be empty or null";
            stateSequence = stateSequence + "->" + state.getValue();
        }
    }

    public void updateCapturedPayment() {
        this.paymentStatus = PaymentStatus.CAPTURED;
        updatedStateSequence(PaymentStatus.CAPTURED);
    }
}
