package com.kupi.cc.base.domain.entity;

import com.kupi.cc.base.domain.request.PaymentBrokerRequestDto;
import com.kupi.cc.base.domain.request.PaymentBrokerUpdateRequestDto;
import com.kupi.cc.base.enums.CountryCode;
import com.kupi.cc.base.enums.CurrencyType;
import com.kupi.cc.base.enums.PaymentBrokerName;
import com.kupi.cc.base.enums.PaymentOptionType;
import lombok.*;

import jakarta.persistence.*;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "payment_broker")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentBroker extends Auditable {
    private static final long serialVersionUID = 4312684395770571981L;

    @Transient
    private static final String PAYMENT_BROKER_ID_PREFIX = "pbid-";

    @Id
    private String id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentBrokerName name;

    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CountryCode countryCode;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentOptionType paymentOptionType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CurrencyType currencyType;

    @OneToMany(mappedBy = "paymentBroker", fetch = FetchType.LAZY)
    private Set<OrderDetails> orderDetails;

    private boolean isDeleted;


    public PaymentBroker(PaymentBrokerRequestDto paymentBrokerRequestDto) {
        this.id = PAYMENT_BROKER_ID_PREFIX + UUID.randomUUID();
        this.name = paymentBrokerRequestDto.getName();
        this.isActive = true;
        this.countryCode = paymentBrokerRequestDto.getCountryCode();
        this.paymentOptionType = paymentBrokerRequestDto.getPaymentOptionType();
        this.currencyType = paymentBrokerRequestDto.getCurrencyType();
    }


    public void update(PaymentBrokerUpdateRequestDto paymentBrokerUpdateRequestDto) {
        this.name = paymentBrokerUpdateRequestDto.getName();
        this.currencyType = paymentBrokerUpdateRequestDto.getCurrencyType();
        this.paymentOptionType = paymentBrokerUpdateRequestDto.getPaymentOptionType();
        this.countryCode = paymentBrokerUpdateRequestDto.getCountryCode();
    }
}
