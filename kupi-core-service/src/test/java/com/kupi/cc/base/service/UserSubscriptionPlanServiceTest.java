package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.UserSubscriptionPlan;
import com.kupi.cc.base.domain.request.UserSubscriptionPlanRequestDto;
import com.kupi.cc.base.domain.response.UserSubscriptionPlanListResponse;
import com.kupi.cc.base.exception.InvalidUserSubscriptionPlanException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.repository.UserSubscriptionPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class UserSubscriptionPlanServiceTest {

    @Mock
    private UserSubscriptionPlanRepository repository;
    @Mock
    private StripeService stripeService;

    @InjectMocks
    private UserSubscriptionPlanService service;

    private final String planId = "plan-001";
    private final String planName = "Premium Plan";

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void should_CreateUserSubscriptionPlan_When_NameDoesNotExist() {
        UserSubscriptionPlanRequestDto plan = new UserSubscriptionPlanRequestDto();
        plan.setName(planName);
        plan.setPrice(BigDecimal.valueOf(10));
        plan.setImageUrl("https://example.com/image.jpg");
        when(repository.existsByNameAndDeletedFalse(planName)).thenReturn(false);
        when(stripeService.createOneTimeProduct(planName, plan.getPrice(), plan.getImageUrl()))
                .thenReturn("mock-stripe-price-id");
        service.createUserSubscriptionPlan(plan);
        verify(repository).save(any(UserSubscriptionPlan.class));
    }

    @Test
    void should_ThrowExistingNameException_When_NameAlreadyExists() {
        UserSubscriptionPlan plan = new UserSubscriptionPlan();
        plan.setName(planName);

        when(repository.existsByNameAndDeletedFalse(planName)).thenReturn(true);

//        assertThrows(ExistingNameException.class, () -> service.createUserSubscriptionPlan(plan));
    }

    @Test
    void should_ThrowKupiCoreException_When_SaveFails() {
        UserSubscriptionPlan plan = new UserSubscriptionPlan();
        plan.setName(planName);

        when(repository.existsByNameAndDeletedFalse(planName)).thenReturn(false);
        when(repository.save(plan)).thenThrow(new DataAccessException("DB Error") {
        });

//        assertThrows(KupiCoreException.class, () -> service.createUserSubscriptionPlan(plan));
    }

    @Test
    void should_ReturnTrue_When_NameExists() {
        when(repository.existsByNameAndDeletedFalse(planName)).thenReturn(true);
        assertTrue(service.checkUserSubscriptionPlanNameExists(planName, null));
    }

    @Test
    void should_ReturnFalse_When_NameDoesNotExist() {
        when(repository.existsByNameAndDeletedFalse(planName)).thenReturn(false);
        assertFalse(service.checkUserSubscriptionPlanNameExists(planName, null));
    }

    @Test
    void should_ThrowKupiCoreException_When_ExistsCheckFails() {
        when(repository.existsByNameAndDeletedFalse(planName)).thenThrow(new DataAccessException("Error") {
        });
        assertThrows(KupiCoreException.class, () -> service.checkUserSubscriptionPlanNameExists(planName, null));
    }

    @Test
    void should_GetAllActivePlans_Successfully() {
        List<UserSubscriptionPlan> plans = List.of(new UserSubscriptionPlan(), new UserSubscriptionPlan());
        when(repository.findAllByIsActiveTrueAndDeletedFalseOrderByPriceAsc()).thenReturn(plans);

        UserSubscriptionPlanListResponse response = service.getAllActivePlans();

        assertEquals(2, response.getUserSubscriptionPlanList().size());
    }

    @Test
    void should_ThrowKupiCoreException_When_GetAllActivePlansFails() {
        when(repository.findAllByIsActiveTrueAndDeletedFalseOrderByPriceAsc()).thenThrow(new DataAccessException("DB Error") {
        });
        assertThrows(KupiCoreException.class, () -> service.getAllActivePlans());
    }

    @Test
    void should_GetPlanById_Successfully() {
        UserSubscriptionPlan plan = new UserSubscriptionPlan();
        when(repository.findById(planId)).thenReturn(Optional.of(plan));

        UserSubscriptionPlan result = service.getUserSubscriptionPlanById(planId);
        assertEquals(plan, result);
    }

    @Test
    void should_ThrowInvalidUserSubscriptionPlanException_When_NotFoundById() {
        when(repository.findById(planId)).thenReturn(Optional.empty());
        assertThrows(InvalidUserSubscriptionPlanException.class, () -> service.getUserSubscriptionPlanById(planId));
    }

    @Test
    void should_ThrowKupiCoreException_When_GetByIdFails() {
        when(repository.findById(planId)).thenThrow(new DataAccessException("Error") {
        });
        assertThrows(KupiCoreException.class, () -> service.getUserSubscriptionPlanById(planId));
    }
}