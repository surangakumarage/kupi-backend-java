package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.SubscriptionPlan;
import com.kupi.cc.base.domain.request.SubscriptionUpdateRequestDto;
import com.kupi.cc.base.enums.PlanType;
import com.kupi.cc.base.enums.SubscriptionStatus;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.ExistingNameException;
import com.kupi.cc.base.exception.InvalidSubscriptionPlanException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.repository.SubscriptionPlanRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests {@link SubscriptionPlanService} class.
 */
class SubscriptionPlanServiceTest {

    private static final String SUBSCRIPTION_PLAN_ID = "spid-fc2aaeeb-9fe4-4584-a253-1831c8610600";
    private static final String SEARCH_TERM = "SEARCH TERM";
    private static final String UPDATED_AT = "updatedAt";
    private static final String ALL = "ALL";
    private static final int PAGE = 0;
    private static final int SIZE = 1;

    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;
    private SubscriptionPlanService subscriptionPlanService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        subscriptionPlanService = new SubscriptionPlanService(subscriptionPlanRepository);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_Return_SaveSubscriptionPlan_When_GivenTitleNotExistsForCreateSubscriptionPlan() {
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        when(subscriptionPlanRepository.existsByTitleAndUserRole(subscriptionPlan.getTitle(), UserType.MERCHANT))
                .thenReturn(false);
        subscriptionPlanService.createSubscriptionPlan(subscriptionPlan, UserType.MERCHANT);
        verify(subscriptionPlanRepository).save(subscriptionPlan);
    }

    @Test
    void Should_ThrowExistingNameException_When_GivenTitleAlreadyExistsForCreateSubscriptionPlan() {
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        when(subscriptionPlanRepository.existsByTitleAndUserRole(subscriptionPlan.getTitle(), UserType.MERCHANT))
                .thenReturn(true);
        ExistingNameException existingNameException = assertThrows(ExistingNameException.class, () ->
                subscriptionPlanService.createSubscriptionPlan(subscriptionPlan, UserType.MERCHANT));
        assertEquals("Existing subscription plan title.", existingNameException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_CheckExistingTitleForCreateSubscriptionPlanIsFailed() {
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        when(subscriptionPlanRepository.existsByTitleAndUserRole(subscriptionPlan.getTitle(), UserType.MERCHANT))
                .thenThrow(new DataAccessException("ERROR") {
                });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                subscriptionPlanService.createSubscriptionPlan(subscriptionPlan, UserType.MERCHANT));
        assertEquals("checking subscription plan title availability for" + subscriptionPlan.getTitle()
                + " is exist from the database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_CheckSaveSubscriptionPlanIsFailed() {
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        when(subscriptionPlanRepository.save(subscriptionPlan)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                subscriptionPlanService.createSubscriptionPlan(subscriptionPlan, UserType.MERCHANT));
        assertEquals("Saving subscription plan into the database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnSubscriptionPlan_When_GivenValidSubscriptionIdForGetSubscriptionPlanById() {
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        when(subscriptionPlanRepository.findById(SUBSCRIPTION_PLAN_ID)).thenReturn(Optional.of(subscriptionPlan));
        assertEquals(subscriptionPlan, subscriptionPlanService.getSubscriptionPlanById(SUBSCRIPTION_PLAN_ID));
    }

    @Test
    void Should_ThrowInvalidSubscriptionPlanException_When_GivenInvalidSubscriptionIdForGetSubscriptionPlanById() {
        when(subscriptionPlanRepository.findById(SUBSCRIPTION_PLAN_ID)).thenReturn(Optional.empty());
        InvalidSubscriptionPlanException subscriptionPlanException = assertThrows(InvalidSubscriptionPlanException.class, () ->
                subscriptionPlanService.getSubscriptionPlanById(SUBSCRIPTION_PLAN_ID));
        assertEquals("Invalid subscription plan id: " + SUBSCRIPTION_PLAN_ID,
                subscriptionPlanException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetSubscriptionFromDataBaseIsFailedForGetSubscriptionPlanById() {
        when(subscriptionPlanRepository.findById(SUBSCRIPTION_PLAN_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                subscriptionPlanService.getSubscriptionPlanById(SUBSCRIPTION_PLAN_ID));
        assertEquals("Reading subscription plan by id: " + SUBSCRIPTION_PLAN_ID + " from database was failed.",
                kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnSubscriptionPlanPage_When_SearchTermIsAllForGetSubscriptionPlanPage() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        Page<SubscriptionPlan> subscriptionPlanPage = getSampleSubscriptionPlanPage();
        when(subscriptionPlanRepository.getAllByUserRole(pageable, UserType.MERCHANT)).thenReturn(subscriptionPlanPage);
        assertEquals(subscriptionPlanPage,
                subscriptionPlanService.getSubscriptionPlanPage(pageable, ALL, UserType.MERCHANT));
    }

    @Test
    void Should_ReturnSubscriptionPlanPage_When_SearchTermIsNotAllForGetSubscriptionPlanPage() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        Page<SubscriptionPlan> subscriptionPlanPage = getSampleSubscriptionPlanPage();
        when(subscriptionPlanRepository.findAllBySearchTermContaining(pageable, UserType.MERCHANT, SEARCH_TERM))
                .thenReturn(subscriptionPlanPage);
        assertEquals(subscriptionPlanPage,
                subscriptionPlanService.getSubscriptionPlanPage(pageable, SEARCH_TERM, UserType.MERCHANT));
    }

    @Test
    void Should_Throw_When_GetSubscriptionPlanPageFromDatabaseIsFailedForGetSubscriptionPlanPage() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        when(subscriptionPlanRepository.getAllByUserRole(pageable, UserType.MERCHANT))
                .thenThrow(new DataAccessException("ERROR") {
                });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                subscriptionPlanService.getSubscriptionPlanPage(pageable, ALL, UserType.MERCHANT));
        assertEquals("Reading subscription plans from database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnSubscriptionPlanPage_When_SearchTermIsAllForGetActiveSubscriptionPlan() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        Page<SubscriptionPlan> subscriptionPlanPage = getSampleSubscriptionPlanPage();
        when(subscriptionPlanRepository.getAllByUserRoleAndStatus(pageable, UserType.MERCHANT, SubscriptionStatus.ACTIVE))
                .thenReturn(subscriptionPlanPage);
        assertEquals(subscriptionPlanPage,
                subscriptionPlanService.getActiveSubscriptionPlan(pageable, ALL, UserType.MERCHANT));
    }

    @Test
    void Should_ReturnSubscriptionPlanPage_When_SearchTermIsNotAllForGetActiveSubscriptionPlan() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        Page<SubscriptionPlan> subscriptionPlanPage = getSampleSubscriptionPlanPage();
        when(subscriptionPlanRepository.findAllActiveStatusBySearchTermContaining(pageable, UserType.MERCHANT, SEARCH_TERM,
                SubscriptionStatus.ACTIVE))
                .thenReturn(subscriptionPlanPage);
        assertEquals(subscriptionPlanPage,
                subscriptionPlanService.getActiveSubscriptionPlan(pageable, SEARCH_TERM, UserType.MERCHANT));
    }

    @Test
    void Should_Throw_When_GetSubscriptionPlanPageFromDatabaseIsFailedForGetActiveSubscriptionPlan() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        when(subscriptionPlanRepository.getAllByUserRoleAndStatus(pageable, UserType.MERCHANT, SubscriptionStatus.ACTIVE))
                .thenThrow(new DataAccessException("ERROR") {
                });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                subscriptionPlanService.getActiveSubscriptionPlan(pageable, ALL, UserType.MERCHANT));
        assertEquals("Reading active subscription plans from database was failed.",
                kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnSubscriptionPlan_When_SubscriptionPlanTitleNotExistsForUpdateSubscriptionPlan() {
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        when(subscriptionPlanRepository.findById(SUBSCRIPTION_PLAN_ID)).thenReturn(Optional.of(subscriptionPlan));
        when(subscriptionPlanRepository.existsByTitleAndUserRoleAndIdNot(subscriptionUpdateRequestDto.getTitle(),
                UserType.MERCHANT, SUBSCRIPTION_PLAN_ID)).thenReturn(false);
        when(subscriptionPlanRepository.save(subscriptionPlan)).thenReturn(subscriptionPlan);
        assertEquals(subscriptionPlan, subscriptionPlanService.updateSubscriptionPlan(subscriptionUpdateRequestDto));
    }

    @Test
    void Should_ThrowInvalidSubscriptionPlanException_When_InvalidSubscriptionIdIsProvidedForUpdateSubscriptionPlan() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        when(subscriptionPlanRepository.findById(SUBSCRIPTION_PLAN_ID)).thenReturn(Optional.empty());
        InvalidSubscriptionPlanException invalidSubscriptionPlanException = assertThrows(InvalidSubscriptionPlanException.class, () ->
                subscriptionPlanService.updateSubscriptionPlan(subscriptionUpdateRequestDto));
        assertEquals("Invalid subscription plan id: " + SUBSCRIPTION_PLAN_ID,
                invalidSubscriptionPlanException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetSubscriptionPlanFromDataBaseIsFailedForUpdateSubscriptionPlan() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        when(subscriptionPlanRepository.findById(SUBSCRIPTION_PLAN_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                subscriptionPlanService.updateSubscriptionPlan(subscriptionUpdateRequestDto));
        assertEquals("Reading subscription plan by id: " + SUBSCRIPTION_PLAN_ID + " from database was failed.",
                kupiCoreException.getMessage());
    }

    @Test
    void Should_ThrowExistingNameException_When_ExistingSubscriptionPlanTitleIsProvidedForUpdateSubscriptionPlan() {
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        when(subscriptionPlanRepository.findById(SUBSCRIPTION_PLAN_ID)).thenReturn(Optional.of(subscriptionPlan));
        when(subscriptionPlanRepository.existsByTitleAndUserRoleAndIdNot(subscriptionUpdateRequestDto.getTitle(),
                UserType.MERCHANT, SUBSCRIPTION_PLAN_ID)).thenReturn(true);
        ExistingNameException existingNameException = assertThrows(ExistingNameException.class, () ->
                subscriptionPlanService.updateSubscriptionPlan(subscriptionUpdateRequestDto));
        assertEquals("Existing subscription plan title.", existingNameException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_CheckExistingSubscriptionPlanTitleIsFailedForUpdateSubscriptionPlan() {
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        when(subscriptionPlanRepository.findById(SUBSCRIPTION_PLAN_ID)).thenReturn(Optional.of(subscriptionPlan));
        when(subscriptionPlanRepository.existsByTitleAndUserRoleAndIdNot(subscriptionUpdateRequestDto.getTitle(),
                UserType.MERCHANT, SUBSCRIPTION_PLAN_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                subscriptionPlanService.updateSubscriptionPlan(subscriptionUpdateRequestDto));
        assertEquals("checking subscription plan title availability for" + subscriptionUpdateRequestDto.getTitle() +
                " is exist from the database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_SaveSubscriptionPlanIsFailedForUpdateSubscriptionPlan() {
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = getSampleSubscriptionUpdateRequestDto();
        when(subscriptionPlanRepository.findById(SUBSCRIPTION_PLAN_ID)).thenReturn(Optional.of(subscriptionPlan));
        when(subscriptionPlanRepository.existsByTitleAndUserRoleAndIdNot(subscriptionUpdateRequestDto.getTitle(),
                UserType.MERCHANT, SUBSCRIPTION_PLAN_ID)).thenReturn(false);
        when(subscriptionPlanRepository.save(subscriptionPlan)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                subscriptionPlanService.updateSubscriptionPlan(subscriptionUpdateRequestDto));
        assertEquals("Updating subscription plan by id: " + SUBSCRIPTION_PLAN_ID + " from database was failed.",
                kupiCoreException.getMessage());
    }

    @Test
    void Should_UpdateSubscriptionPlanStatus_When_ValidSubscriptionPlanIdIsProvidedForUpdateSubscriptionPlanStatus() {
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        when(subscriptionPlanRepository.findById(SUBSCRIPTION_PLAN_ID)).thenReturn(Optional.of(subscriptionPlan));
        subscriptionPlanService.updateSubscriptionPlanStatus(SUBSCRIPTION_PLAN_ID, SubscriptionStatus.ACTIVE);
        verify(subscriptionPlanRepository).save(subscriptionPlan);
    }

    @Test
    void Should_ThrowInvalidSubscriptionPlanException_When_InvalidSubscriptionPlanIdIsProvidedForUpdateSubscriptionPlanStatus() {
        when(subscriptionPlanRepository.findById(SUBSCRIPTION_PLAN_ID)).thenReturn(Optional.empty());
        InvalidSubscriptionPlanException invalidSubscriptionPlanException = assertThrows(InvalidSubscriptionPlanException.class, () ->
                subscriptionPlanService.updateSubscriptionPlanStatus(SUBSCRIPTION_PLAN_ID, SubscriptionStatus.ACTIVE));
        assertEquals("Invalid subscription plan id: " + SUBSCRIPTION_PLAN_ID,
                invalidSubscriptionPlanException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetSubscriptionPlanFromDataBaseIsFailedForUpdateSubscriptionPlanStatus() {
        when(subscriptionPlanRepository.findById(SUBSCRIPTION_PLAN_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                subscriptionPlanService.updateSubscriptionPlanStatus(SUBSCRIPTION_PLAN_ID, SubscriptionStatus.ACTIVE));
        assertEquals("Reading subscription plan by id: " + SUBSCRIPTION_PLAN_ID + " from database was failed.",
                kupiCoreException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_UpdateSubscriptionPlanStatusFromDataBaseIsFailed() {
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        when(subscriptionPlanRepository.findById(SUBSCRIPTION_PLAN_ID)).thenReturn(Optional.of(subscriptionPlan));
        when(subscriptionPlanRepository.save(subscriptionPlan)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                subscriptionPlanService.updateSubscriptionPlanStatus(SUBSCRIPTION_PLAN_ID, SubscriptionStatus.ACTIVE));
        assertEquals("Updating status of subscription plan from database was failed.",
                kupiCoreException.getMessage());
    }

    @Test
    void Should_DeleteSubscriptionPlan_When_ValidSubscriptionPlanIdIsProvidedForDeleteSubscriptionPlanById() {
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        when(subscriptionPlanRepository.findById(SUBSCRIPTION_PLAN_ID)).thenReturn(Optional.of(subscriptionPlan));
        subscriptionPlanService.deleteSubscriptionPlanById(SUBSCRIPTION_PLAN_ID);
        verify(subscriptionPlanRepository).delete(subscriptionPlan);
    }

    @Test
    void Should_ThrowInvalidSubscriptionPlanException_When_InvalidSubscriptionPlanIdIsProvidedForDeleteSubscriptionPlanById() {
        when(subscriptionPlanRepository.findById(SUBSCRIPTION_PLAN_ID)).thenReturn(Optional.empty());
        InvalidSubscriptionPlanException invalidSubscriptionPlanException = assertThrows(InvalidSubscriptionPlanException.class, () ->
                subscriptionPlanService.deleteSubscriptionPlanById(SUBSCRIPTION_PLAN_ID));
        assertEquals("Invalid subscription plan id: " + SUBSCRIPTION_PLAN_ID,
                invalidSubscriptionPlanException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetSubscriptionPlanFromDataBaseIsFailedForDeleteSubscriptionPlanById() {
        when(subscriptionPlanRepository.findById(SUBSCRIPTION_PLAN_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                subscriptionPlanService.deleteSubscriptionPlanById(SUBSCRIPTION_PLAN_ID));
        assertEquals("Reading subscription plan by id: " + SUBSCRIPTION_PLAN_ID + " from database was failed.",
                kupiCoreException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_DeleteSubscriptionPlanFromDataBaseIsFailed() {
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        when(subscriptionPlanRepository.findById(SUBSCRIPTION_PLAN_ID)).thenReturn(Optional.of(subscriptionPlan));
        doThrow(new DataAccessException("ERROR") {
        }).when(subscriptionPlanRepository).delete(subscriptionPlan);
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                subscriptionPlanService.deleteSubscriptionPlanById(SUBSCRIPTION_PLAN_ID));
        assertEquals("Deleting subscription plans by id: " + SUBSCRIPTION_PLAN_ID + " from database was failed.",
                kupiCoreException.getMessage());
    }

    /**
     * This method creates sample SubscriptionPlan
     *
     * @return SubscriptionPlan
     */
    private SubscriptionPlan getSampleSubscriptionPlan() {
        SubscriptionPlan subscriptionPlan = new SubscriptionPlan();
        subscriptionPlan.setId(SUBSCRIPTION_PLAN_ID);
        subscriptionPlan.setTitle("Title");
        subscriptionPlan.setSubTitle("Subtitle");
        subscriptionPlan.setPlanType(PlanType.MONTHLY);
        subscriptionPlan.setNoOfDealsOfTheDayEventsPerMonth(1);
        subscriptionPlan.setNoOfDealsPerMonth(1);
        subscriptionPlan.setNoOfDealsOfTheDayPerEvent(1);
        subscriptionPlan.setUserRole(UserType.MERCHANT);
        subscriptionPlan.setFree(false);
        subscriptionPlan.setAmount(BigDecimal.ONE);
        subscriptionPlan.setDescription("Description");
        subscriptionPlan.setTermsAndCondition("terms and conditions");
        return subscriptionPlan;
    }

    /**
     * This method creates sample SubscriptionPlan Page
     *
     * @return SubscriptionPlanPage
     */
    private Page<SubscriptionPlan> getSampleSubscriptionPlanPage() {
        SubscriptionPlan subscriptionPlan = getSampleSubscriptionPlan();
        List<SubscriptionPlan> subscriptionPlanList = new ArrayList<>();
        subscriptionPlanList.add(subscriptionPlan);
        return new PageImpl<>(subscriptionPlanList);
    }

    /**
     * This method creates sample SubscriptionUpdateRequestDto
     *
     * @return SubscriptionUpdateRequestDto
     */
    private SubscriptionUpdateRequestDto getSampleSubscriptionUpdateRequestDto() {
        SubscriptionUpdateRequestDto subscriptionUpdateRequestDto = new SubscriptionUpdateRequestDto();
        subscriptionUpdateRequestDto.setId(SUBSCRIPTION_PLAN_ID);
        subscriptionUpdateRequestDto.setTitle("Title");
        subscriptionUpdateRequestDto.setSubTitle("Subtitle");
        subscriptionUpdateRequestDto.setPlanType(PlanType.MONTHLY);
        subscriptionUpdateRequestDto.setNoOfDealsOfTheDayEventsPerMonth(1);
        subscriptionUpdateRequestDto.setNoOfDealsPerMonth(1);
        subscriptionUpdateRequestDto.setNoOfDealsOfTheDayPerEvent(1);
        subscriptionUpdateRequestDto.setUserRole(UserType.MERCHANT);
        subscriptionUpdateRequestDto.setIsFree(false);
        subscriptionUpdateRequestDto.setAmount(BigDecimal.ONE);
        subscriptionUpdateRequestDto.setDescription("Description");
        subscriptionUpdateRequestDto.setTermsAndCondition("terms and conditions");
        return subscriptionUpdateRequestDto;
    }
}