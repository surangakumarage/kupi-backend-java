package com.kupi.cc.base.service;

import com.stripe.model.Price;
import com.kupi.cc.base.domain.entity.UserSubscriptionPlan;
import com.kupi.cc.base.domain.request.UpdatePlanStatusRequestDto;
import com.kupi.cc.base.domain.request.UserSubscriptionPlanRequestDto;
import com.kupi.cc.base.domain.response.MobileUserSubscriptionPlanResponseDto;
import com.kupi.cc.base.domain.response.UserSubscriptionPlanListResponse;
import com.kupi.cc.base.exception.*;
import com.kupi.cc.base.repository.UserSubscriptionPlanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;

@Service
@Slf4j
public class UserSubscriptionPlanService {

    private final UserSubscriptionPlanRepository userSubscriptionPlanRepository;
    private final StripeService stripeService;
    private final UserSubscriptionRules userSubscriptionRules;

    public UserSubscriptionPlanService(UserSubscriptionPlanRepository userSubscriptionPlanRepository, StripeService stripeService, UserSubscriptionRules userSubscriptionRules) {
        this.userSubscriptionPlanRepository = userSubscriptionPlanRepository;
        this.stripeService = stripeService;
        this.userSubscriptionRules = userSubscriptionRules;
    }

    /**
     * Creates a new user subscription plan based on the provided request details.
     * It first checks if a plan with the same name already exists. If not, it creates
     * a corresponding Stripe product and price, saves the plan to the database, and
     * returns the created plan wrapped in a response DTO.
     *
     * @param userSubscriptionPlanRequestDto the request object containing subscription plan details
     * @return the created subscription plan wrapped in a response DTO
     * @throws ExistingNameException if a plan with the same name already exists
     * @throws KupiCoreException     if there is an error interacting with the database or Stripe
     */
    @Transactional
    public MobileUserSubscriptionPlanResponseDto createUserSubscriptionPlan(
            UserSubscriptionPlanRequestDto userSubscriptionPlanRequestDto) {
        try {
            boolean existsByName =
                    checkUserSubscriptionPlanNameExists(userSubscriptionPlanRequestDto.getName(), null);
            if (existsByName) {
                throw new ExistingNameException("Existing user subscription plan name.");
            }
            UserSubscriptionPlan userSubscriptionPlan = new UserSubscriptionPlan(userSubscriptionPlanRequestDto);
            String stripePriceId = stripeService.createOneTimeProduct(userSubscriptionPlan.getName(),
                    userSubscriptionPlan.getPrice(), userSubscriptionPlan.getImageUrl());
            userSubscriptionPlan.setStripePriceId(stripePriceId);
            userSubscriptionPlanRepository.save(userSubscriptionPlan);
            return new MobileUserSubscriptionPlanResponseDto(userSubscriptionPlan);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Saving user subscription plan into the database failed.", e);
        }
    }

    /**
     * Checks whether a subscription plan name already exists in the database.
     * If the ID is provided, it excludes that specific plan from the check (useful for updates).
     *
     * @param name the name of the subscription plan to check
     * @param id   the ID of the existing plan to exclude from the check (can be null for create)
     * @return true if the name exists (excluding the given ID if present), false otherwise
     * @throws KupiCoreException if an error occurs while accessing the database
     */
    public boolean checkUserSubscriptionPlanNameExists(String name, String id) {
        try {
            return (id == null)
                    ? userSubscriptionPlanRepository.existsByNameAndDeletedFalse(name)
                    : userSubscriptionPlanRepository.existsByNameAndIdNotAndDeletedFalse(name, id);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Checking user subscription plan name availability for '" + name + "' failed.", e);
        }
    }

    /**
     * Retrieves all active user subscription plans from the database,
     * sorted by price in ascending order. This method excludes inactive plans.
     *
     * @return a response object containing the list of active subscription plans
     * @throws KupiCoreException if there is an error accessing the database
     */
    public UserSubscriptionPlanListResponse getAllActivePlans() {
        try {
            List<UserSubscriptionPlan> userSubscriptionPlans = userSubscriptionPlanRepository
                    .findAllByIsActiveTrueAndDeletedFalseOrderByPriceAsc();
            UserSubscriptionPlanListResponse userSubscriptionPlanListResponse = new UserSubscriptionPlanListResponse();
            userSubscriptionPlanListResponse.setUserSubscriptionPlanList(userSubscriptionPlans);
            return userSubscriptionPlanListResponse;
        } catch (DataAccessException e) {
            throw new KupiCoreException("Fetching user subscription plans from the database failed.", e);
        }
    }

    /**
     * Retrieves a user subscription plan by its ID.
     * If no plan is found for the given ID, it throws an exception.
     *
     * @param planId the ID of the subscription plan to retrieve
     * @return the corresponding {@link UserSubscriptionPlan} entity
     * @throws InvalidUserSubscriptionPlanException if no plan is found for the given ID
     * @throws KupiCoreException                    if there is an error accessing the database
     */
    public UserSubscriptionPlan getUserSubscriptionPlanById(String planId) {
        try {
            return userSubscriptionPlanRepository.findById(planId)
                    .orElseThrow(() ->
                            new InvalidUserSubscriptionPlanException("User subscription plan not found for ID: " + planId));
        } catch (DataAccessException e) {
            throw new KupiCoreException("Failed to fetch user subscription plan with ID: " + planId, e);
        }
    }

    /**
     * Updates an existing user subscription plan with new details provided in the request DTO.
     * It first validates whether the plan exists and checks for name duplication.
     * If the price has changed, it creates a new Stripe price and updates the associated price ID.
     * The updated plan is then saved to the database and returned as a response DTO.
     *
     * @param planId ID of the subscription plan to update
     * @param dto    the new subscription plan details to be updated
     * @return the updated subscription plan wrapped in a response DTO
     * @throws ExistingNameException                if another active plan already has the same name
     * @throws InvalidUserSubscriptionPlanException if the given plan ID is not found
     * @throws KupiCoreException                    if any other error occurs during the update process
     */
    @Transactional
    public MobileUserSubscriptionPlanResponseDto updateUserSubscriptionPlan(String planId, UserSubscriptionPlanRequestDto dto) {
        try {
            UserSubscriptionPlan existingPlan = getUserSubscriptionPlanById(planId);
            boolean nameExists = checkUserSubscriptionPlanNameExists(dto.getName(), planId);
            if (nameExists) {
                throw new ExistingNameException("Subscription plan name already exists: " + dto.getName());
            }

            stripeService.updateProduct(existingPlan.getStripePriceId(), dto.getName(), dto.getImageUrl());

            boolean priceChanged = dto.getPrice() != null && dto.getPrice().compareTo(existingPlan.getPrice()) != 0;

            existingPlan.setName(dto.getName());
            existingPlan.setCouponLimit(dto.getCouponLimit());
            existingPlan.setValidDays(dto.getValidDays());
            existingPlan.setImageUrl(dto.getImageUrl());
            existingPlan.setPrice(dto.getPrice());
            if (priceChanged) {
                Price existingPrice = Price.retrieve(existingPlan.getStripePriceId());
                String productId = existingPrice.getProduct();
                String newPriceId = stripeService.createOneTimePrice(productId, dto.getPrice());
                existingPlan.setStripePriceId(newPriceId);
            }
            userSubscriptionPlanRepository.save(existingPlan);
            return new MobileUserSubscriptionPlanResponseDto(existingPlan);
        } catch (InvalidUserSubscriptionPlanException e) {
            throw e;
        } catch (Exception e) {
            throw new KupiCoreException("Failed to update user subscription plan.", e);
        }
    }

    /**
     * Deactivates a user subscription plan by setting its {@code isActive} flag to false.
     * This performs a soft delete, meaning the plan remains in the database but is marked inactive.
     *
     * @param planId the ID of the subscription plan to deactivate
     * @throws InvalidUserSubscriptionPlanException if the given plan ID does not exist
     */
    @Transactional
    public void deleteSubscriptionPlan(String planId) {
        try {
            UserSubscriptionPlan plan = getUserSubscriptionPlanById(planId);

            // NEW: ensure plan isn’t in use (no active subs)
            userSubscriptionRules.validateNoActiveSubscriptionsForPlan(planId);

            try {
                stripeService.deleteProduct(plan.getStripePriceId());
            } catch (StripeServiceException e) {
                throw new KupiCoreException("Stripe product deletion failed", e);
            }
            plan.setActive(false);
            plan.setDeleted(true);
            plan.setUpdatedAt(System.currentTimeMillis());
            userSubscriptionPlanRepository.save(plan);
        } catch (PlanHasActiveUsersException e) {
            throw new PlanHasActiveUsersException("Cannot delete the plan." + e);
        } catch (InvalidUserSubscriptionPlanException e) {
            throw e;
        } catch (Exception e) {
            throw new KupiCoreException("Failed to delete subscription plan.", e);
        }
    }


    /**
     * Updates the activation status of a user subscription plan.
     *
     * @param planId                     ID of the subscription plan to update
     * @param updatePlanStatusRequestDto Request DTO containing the new activation status
     * @throws InvalidUserSubscriptionPlanException if the subscription plan is not found
     * @throws KupiCoreException                    if any unexpected error occurs during update
     */
    public void updateActivationStatus(String planId, UpdatePlanStatusRequestDto updatePlanStatusRequestDto) {
        try {
            UserSubscriptionPlan plan = getUserSubscriptionPlanById(planId);
            plan.setActive(updatePlanStatusRequestDto.isActive());
            plan.setUpdatedAt(System.currentTimeMillis());
            userSubscriptionPlanRepository.save(plan);
            log.info("Subscription plan {} updated to {}",
                    planId, updatePlanStatusRequestDto.isActive() ? "active" : "inactive");
        } catch (InvalidUserSubscriptionPlanException e) {
            log.error("Invalid subscription plan ID: {}", planId, e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to update activation status for plan ID: {}", planId, e);
            throw new KupiCoreException("Failed to update activation status.", e);
        }
    }

    public UserSubscriptionPlan getActivePlanByName(String name) {
        try {
            return userSubscriptionPlanRepository.findByNameIgnoreCaseAndIsActiveTrue(name)
                    .orElseThrow(() -> new InvalidUserSubscriptionPlanException("Default plan not found: " + name));
        } catch (Exception e) {
            throw new KupiCoreException("Failed to get active plan by name.", e);
        }

    }
}
