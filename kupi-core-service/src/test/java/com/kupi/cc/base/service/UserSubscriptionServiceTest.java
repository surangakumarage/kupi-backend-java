package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.UserSubscription;
import com.kupi.cc.base.domain.entity.UserSubscriptionPlan;
import com.kupi.cc.base.domain.request.UserSubscriptionRequestDto;
import com.kupi.cc.base.domain.response.UserSubscriptionStatusResponse;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.exception.UserSubscriptionNotFound;
import com.kupi.cc.base.repository.UserSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class UserSubscriptionServiceTest {

    private UserSubscriptionService userSubscriptionService;

    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;

    @Mock
    private UserSubscriptionPlanService userSubscriptionPlanService;

    @Mock
    private UserSubscriptionRules userSubscriptionRules;

    private static final String DEFAULT_PLAN_NAME = "FREE";

    @BeforeEach
    void setUp() {
        initMocks(this);
        userSubscriptionService = new UserSubscriptionService(DEFAULT_PLAN_NAME,
                userSubscriptionRepository, userSubscriptionPlanService, userSubscriptionRules);
    }

    @Test
    void shouldCreateUserSubscription() {
        UserSubscriptionRequestDto dto = new UserSubscriptionRequestDto("user1", "plan1", "stripeSubId");
        UserSubscriptionPlan plan = new UserSubscriptionPlan();
        plan.setId("plan1");
        plan.setCouponLimit(10);
        plan.setValidDays(30);
        plan.setName("Premium Plan");

        when(userSubscriptionPlanService.getUserSubscriptionPlanById("plan1")).thenReturn(plan);

        assertDoesNotThrow(() -> userSubscriptionService.createUserSubscription(dto));
        verify(userSubscriptionRepository, times(1)).save(any(UserSubscription.class));
    }

    @Test
    void shouldThrowKupiCoreExceptionWhenCreateFails() {
        UserSubscriptionRequestDto dto = new UserSubscriptionRequestDto("user1", "plan1", "stripeSubId");
        when(userSubscriptionPlanService.getUserSubscriptionPlanById("plan1"))
                .thenThrow(new DataAccessException("error") {
                });

        assertThrows(KupiCoreException.class, () -> userSubscriptionService.createUserSubscription(dto));
    }

    @Test
    void shouldReturnUserSubscriptionStatus() {
        String userId = "user1";
        long now = System.currentTimeMillis();
        long end = now + (30L * 24 * 60 * 60 * 1000);

        UserSubscriptionPlan plan = new UserSubscriptionPlan();
        plan.setCouponLimit(10);
        plan.setName("Premium");

        UserSubscription subscription = new UserSubscription();
        subscription.setUserId(userId);
        subscription.setSubscriptionEnd(end);
        subscription.setUsedCouponCount(2);
        subscription.setUserSubscriptionPlan(plan);

        when(userSubscriptionRepository.findTopByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(Optional.of(subscription));

        UserSubscriptionStatusResponse response = userSubscriptionService.getUserSubscriptionStatus(userId, "Asia/Colombo");

        assertFalse(response.isExpired());
        assertEquals(2, response.getUsedCoupons());
        assertEquals(8, response.getRemainingCoupons());
        assertTrue(response.getRemainingDays() <= 30);
        assertEquals("Premium", response.getPlanName());
    }

    @Test
    void shouldThrowWhenNoSubscriptionFound() {
        when(userSubscriptionRepository.findTopByUserIdOrderByCreatedAtDesc("user1"))
                .thenReturn(Optional.empty());

        assertThrows(UserSubscriptionNotFound.class, () -> userSubscriptionService.getUserSubscriptionStatus("user1", "Asia/Colombo"));
    }

    @Test
    void shouldThrowKupiCoreExceptionWhenGettingStatusFails() {
        when(userSubscriptionRepository.findTopByUserIdOrderByCreatedAtDesc("user1"))
                .thenThrow(new DataAccessException("error") {
                });

        assertThrows(KupiCoreException.class, () -> userSubscriptionService.getUserSubscriptionStatus("user1", "timeZone"));
    }
}
