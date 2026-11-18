package com.kupi.cc.base.domain.entity;

import com.kupi.cc.base.enums.RefundCode;
import com.kupi.cc.base.enums.RefundStatus;
import lombok.*;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "refund")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Refund extends Auditable {
    private static final long serialVersionUID = -7869198226552272013L;

    @Transient
    private static final String REFUND_ID_PREFIX = "rid-";

    @Id
    private String id;

    @Column(nullable = false)
    private BigDecimal amount;

    private String statusSequence;

    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RefundCode refundCode;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RefundStatus refundStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private OrderDetails orderDetails;

}
