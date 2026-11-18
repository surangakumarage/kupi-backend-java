package com.kupi.cc.base.domain.entity;

import com.kupi.cc.base.enums.PlanType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.sql.Date;
import java.time.LocalDate;

/**
 * SubscriptionRenewal entity: represents renewed subscription every month
 *
 */
@Getter
@Setter
@Entity
@Table(name = "subscription_renewal")
@NoArgsConstructor
public class SubscriptionRenewal {

    @Id
    private String id;
    private String userId;
    private Date subscribedDate;
    private int renewOnDayOfEveryMonth;
    private String subscriptionPlanId;
    private int remainingDealsPerMonth;
    private int remainingDealsOfTheDayEventsPerMonth;
    private int noOfDealsPerEvent;
    @Enumerated(EnumType.STRING)
    private PlanType planType;
    private long createdAt;
    private long updatedAt;
    private boolean isUnsubscribed = false;

    public SubscriptionRenewal(Subscribe subscribe) {
        this.id = subscribe.getId();
        this.userId = subscribe.getUserId();
        this.subscribedDate = new Date(subscribe.getCreatedAt());
        this.renewOnDayOfEveryMonth = LocalDate.now().getDayOfMonth();
        SubscriptionPlan subscriptionPlan = subscribe.getSubscriptionPlan();
        this.subscriptionPlanId = subscriptionPlan.getId();
        this.remainingDealsPerMonth = subscriptionPlan.getNoOfDealsPerMonth();
        this.remainingDealsOfTheDayEventsPerMonth = subscriptionPlan.getNoOfDealsOfTheDayEventsPerMonth();
        this.noOfDealsPerEvent = subscriptionPlan.getNoOfDealsOfTheDayPerEvent();
        this.planType = subscriptionPlan.getPlanType();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
    }

    /**
     * This method resets subscription plan limits.
     *
     * @param subscriptionPlan subscriptionPlan
     */
    public void resetSubscriptionLimits(SubscriptionPlan subscriptionPlan) {
        this.remainingDealsOfTheDayEventsPerMonth = subscriptionPlan.getNoOfDealsOfTheDayEventsPerMonth();
        this.remainingDealsPerMonth = subscriptionPlan.getNoOfDealsPerMonth();
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * This method updates remaining deals of the day events per month.
     *
     * @param increment increment value.
     */
    public void updateRemainingDealsOfTheDayEventsPerMonth(int increment) {
        this.remainingDealsOfTheDayEventsPerMonth += increment;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * This method updates remaining deals per month.
     *
     * @param increment increment value.
     */
    public void updateRemainingDealsPerMonth(int increment) {
        this.remainingDealsPerMonth += increment;
        this.updatedAt = System.currentTimeMillis();
    }
}
