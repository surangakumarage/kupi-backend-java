package com.kupi.cc.base.domain.entity;

import com.kupi.cc.base.domain.request.CouponUpdateRequestDto;
import com.kupi.cc.base.domain.request.OrderCreateRequestDto;
import com.kupi.cc.base.domain.request.OrderUpdateRequestDto;
import com.kupi.cc.base.enums.OrderStatus;
import com.kupi.cc.base.exception.InvalidCouponException;
import lombok.*;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetails extends Payable {
    private static final long serialVersionUID = -632831846936788063L;

    @Transient
    private static final String ORDER_ID_PREFIX = "oid-";

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(mappedBy = "orderDetails", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Coupon> coupons;

    @OneToMany(mappedBy = "orderDetails", fetch = FetchType.EAGER)
    private Set<Refund> refunds;

    @Column(nullable = false)
    private BigDecimal grossTotal;

    @Column(nullable = false)
    private BigDecimal netTotal;

    @Column(nullable = false)
    private String stateSequence;


    public OrderDetails(OrderCreateRequestDto orderCreateRequestDto, String userId) {
        super(ORDER_ID_PREFIX + UUID.randomUUID());
        Set<Coupon> couponSet = orderCreateRequestDto.getCoupons().stream()
                .map(couponCreateRequestDto -> new Coupon(couponCreateRequestDto, this))
                .collect(Collectors.toSet());

        setUserId(userId);
        setCurrencyType(orderCreateRequestDto.getCurrencyType());
        this.status = OrderStatus.CREATED;
        this.coupons = couponSet;

        contactStateSequence(OrderStatus.CREATED);
    }

    private boolean isNonEmpty(String value) {
        return value != null && !value.trim().isBlank();
    }

    /**
     * update this order entity with orderUpdateRequestDto
     * items within this order are updated by the item list in orderUpdateRequestDto, there are 3 cases
     * 1. item list of orderUpdateRequestDto does not have some items of this order -> mark them as deleted
     * 2. orderUpdateRequestDto and this order, both has same item -> update item
     * 3. orderUpdateRequestDto has new items -> create new items in this order
     *
     * @param orderUpdateRequestDto order update request DTO
     */
    public void update(OrderUpdateRequestDto orderUpdateRequestDto) {
        setCurrencyType(orderUpdateRequestDto.getCurrencyType());

        List<String> updateRequestedCouponIds = orderUpdateRequestDto.getCoupons().stream()
                .map(CouponUpdateRequestDto::getId)
                .filter(this::isNonEmpty)
                .collect(Collectors.toList());
        coupons.stream().filter(coupon -> !coupon.isDeleted() && !updateRequestedCouponIds.contains(coupon.getId()))
                .forEach(coupon -> coupon.setDeleted(true));

        orderUpdateRequestDto.getCoupons().forEach(couponUpdate -> {
            Optional<Coupon> optionalCoupon = coupons.stream()
                    .filter(coupon -> !coupon.isDeleted() && coupon.getId().equals(couponUpdate.getId())).findAny();

            if (optionalCoupon.isPresent()) {
                var coupon = optionalCoupon.get();
                coupon.update(couponUpdate);
            } else {
                if (!isNonEmpty(couponUpdate.getId())) {
                    coupons.add(new Coupon(couponUpdate, this));
                } else {
                    throw new InvalidCouponException("Item with id: " + couponUpdate.getId()
                            + " is not found within the order: " + orderUpdateRequestDto.getId());
                }
            }
        });

        contactStateSequence(OrderStatus.UPDATED);
    }

    private void contactStateSequence(OrderStatus state) {
        if (OrderStatus.CREATED.equals(state)) {
            stateSequence = state.getValue();
        } else {
            assert stateSequence != null && !stateSequence.isBlank() : "State sequence cannot be empty or null";
            stateSequence = stateSequence + "->" + state.getValue();
        }

    }

    /**
     * MIGHT NEED IMPROVEMENTS
     */
    public void cancelOrder() {
        this.status = OrderStatus.CANCELLED;
        contactStateSequence(OrderStatus.CANCELLED);
    }

    /**
     * check whether the order is expired
     * if any of the items in the order is expired, whole order is considered to be expired
     *
     * @return is expired
     */
    public boolean isExpired() {
        long now = System.currentTimeMillis();
        return coupons.stream().map(coupon -> coupon.getDealExpiryDate() <= now)
                .reduce(false, Boolean::logicalOr);
    }

    @Override
    public void persistAuditor(String userId) {
        super.persistAuditor(userId);
        this.coupons.forEach(coupon -> coupon.persistAuditor(userId));
    }

    @Override
    public void updateAuditor(String userId) {
        super.updateAuditor(userId);
        this.coupons.forEach(coupon -> {
            if (isNonEmpty(coupon.getCreatedBy())) {
                coupon.updateAuditor(userId);
            } else {
                coupon.persistAuditor(userId);
            }
        });
    }
}
