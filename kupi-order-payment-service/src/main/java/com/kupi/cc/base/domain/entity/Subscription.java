package com.kupi.cc.base.domain.entity;

import com.kupi.cc.base.enums.SubscriptionStatus;
import com.kupi.cc.base.enums.SubscriptionType;
import com.kupi.cc.base.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.*;

@Entity
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Subscription extends Payable {
    private static final long serialVersionUID = -9133600967334932033L;

    @Transient
    private static final String SUBSCRIPTION_ID_PREFIX = "sid-";

    @Column(nullable = false)
    private String subscriptionPlanId;

    @Column(nullable = false)
    private BigDecimal subscriptionFee;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionType subscriptionType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus subscriptionStatus;

    @Column(nullable = false)
    private String title;

    private String subTitle;

    public Subscription() {
        setId(SUBSCRIPTION_ID_PREFIX + UUID.randomUUID());
    }

    public boolean isTemporary() {
        return SubscriptionStatus.TEMPORARY.equals(subscriptionStatus);
    }

    public void updateWithPaymentRequest() {
        var payment = new Payment(this);
        payment.persistAuditor(getUserId());
        setPayments(new HashSet<>(List.of(payment)));
    }

    public void updateWithCompletedState() {
        this.subscriptionStatus = SubscriptionStatus.ACTIVE;
        getPayments().forEach(Payment::updateCapturedPayment);
    }

    @Override
    public void updateAuditor(String userId) {
        super.updateAuditor(userId);
        Optional.ofNullable(getPayments()).orElse(Collections.emptySet())
                .forEach(payment -> payment.updateAuditor(userId));
    }
}
