package com.kupi.cc.base.domain.entity;

import com.kupi.cc.base.domain.request.SubscriptionPlanRequestDto;
import com.kupi.cc.base.domain.request.SubscriptionUpdateRequestDto;
import com.kupi.cc.base.enums.PlanType;
import com.kupi.cc.base.enums.SubscriptionStatus;
import com.kupi.cc.base.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * SubscriptionPlan entity
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "subscription_plan")
public class SubscriptionPlan {

    @Transient
    private static final String SUBSCRIPTION_PLAN_ID_PREFIX = "spid-";
    @Transient
    private static final int LONG_TEXT_LENGTH = 500;

    @Id
    private String id;
    @Column(nullable = false)
    private String title;
    private String subTitle;
    @Size(max = LONG_TEXT_LENGTH)
    private String description;
    @Column(columnDefinition = "TEXT")
    private String termsAndCondition;
    private String imageUrl;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserType userRole;
    @Column(nullable = false)
    private PlanType planType;
    @Column(nullable = false)
    private int noOfDealsPerMonth;
    @Column(nullable = false)
    private int noOfDealsOfTheDayEventsPerMonth;
    @Column(nullable = false)
    private int noOfDealsOfTheDayPerEvent;
    @Column(nullable = false)
    private boolean isFree;
    @Column(nullable = false)
    private BigDecimal amount;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;
    private boolean isActive;
    private long createdAt;
    private long updatedAt;

    public SubscriptionPlan(SubscriptionPlanRequestDto subscriptionPlanRequestDto) {
        this.id = SUBSCRIPTION_PLAN_ID_PREFIX + UUID.randomUUID();
        this.title = subscriptionPlanRequestDto.getTitle();
        this.subTitle = subscriptionPlanRequestDto.getSubTitle();
        this.description = subscriptionPlanRequestDto.getDescription();
        this.termsAndCondition = subscriptionPlanRequestDto.getTermsAndCondition();
        this.imageUrl = subscriptionPlanRequestDto.getImageUrl();
        this.userRole = subscriptionPlanRequestDto.getUserRole();
        this.noOfDealsPerMonth = subscriptionPlanRequestDto.getNoOfDealsPerMonth();
        this.noOfDealsOfTheDayEventsPerMonth = subscriptionPlanRequestDto.getNoOfDealsOfTheDayEventsPerMonth();
        this.noOfDealsOfTheDayPerEvent = subscriptionPlanRequestDto.getNoOfDealsOfTheDayPerEvent();
        this.planType = subscriptionPlanRequestDto.getPlanType();
        this.isFree = subscriptionPlanRequestDto.getIsFree();
        this.amount = subscriptionPlanRequestDto.getAmount();
        this.isActive = true;
        this.status = SubscriptionStatus.INACTIVE;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * This method updates subscription plan by subscriptionUpdateRequestDto.
     *
     * @param subscriptionUpdateRequestDto subscriptionUpdateRequestDto
     */
    public void update(SubscriptionUpdateRequestDto subscriptionUpdateRequestDto) {
        this.title = subscriptionUpdateRequestDto.getTitle();
        this.subTitle = subscriptionUpdateRequestDto.getSubTitle();
        this.description = subscriptionUpdateRequestDto.getDescription();
        this.termsAndCondition = subscriptionUpdateRequestDto.getTermsAndCondition();
        this.imageUrl = subscriptionUpdateRequestDto.getImageUrl();
        this.noOfDealsPerMonth = subscriptionUpdateRequestDto.getNoOfDealsPerMonth();
        this.noOfDealsOfTheDayEventsPerMonth = subscriptionUpdateRequestDto.getNoOfDealsOfTheDayEventsPerMonth();
        this.noOfDealsOfTheDayPerEvent = subscriptionUpdateRequestDto.getNoOfDealsOfTheDayPerEvent();
        this.isFree = subscriptionUpdateRequestDto.getIsFree();
        this.amount = subscriptionUpdateRequestDto.getAmount();
        this.updatedAt = System.currentTimeMillis();
    }
}
