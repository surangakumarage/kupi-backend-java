package com.kupi.cc.base.domain.response;

import lombok.Getter;
import lombok.Setter;

/**
 * Subscriptions dto
 */
@Getter
@Setter
public class SubscriptionsDto {

    //TODO: Remove mock data
    private Plans plans = new Plans();
    private Subscribers subscribers = new Subscribers();
    private Unsubscribers unsubscribers = new Unsubscribers();

    @Getter
    class Plans {
        private final Long totalPlan = 30l;
        private final Long totalActivePlan = 20l;
        private final Long merchantPlans = 10l;
        private final Long merchantActivePlans = 10l;
        private final Long bankPlans = 20l;
        private final Long bankActivePlans = 10l;
    }

    @Getter
    class Subscribers {
        private final Long total = 30l;
        private final Long merchantSubscribers = 10l;
        private final Long bankSubscribers = 20l;
    }

    @Getter
    class Unsubscribers {
        private final Long total = 30l;
        private final Long merchantUnsubscribers = 10l;
        private final Long bankUnsubscribers = 20l;
    }
}
