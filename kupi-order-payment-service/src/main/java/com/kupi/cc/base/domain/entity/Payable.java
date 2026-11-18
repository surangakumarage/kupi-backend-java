package com.kupi.cc.base.domain.entity;

import com.kupi.cc.base.enums.CurrencyType;
import com.kupi.cc.base.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Payable extends Auditable {
    private static final long serialVersionUID = 1179071240242282729L;

    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CurrencyType currencyType;

    @ManyToOne(fetch = FetchType.EAGER)
    private PaymentBroker paymentBroker;

    @OneToMany(mappedBy = "payable", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Payment> payments;

    public Payable(String id) {
        this.id = id;
    }
}
