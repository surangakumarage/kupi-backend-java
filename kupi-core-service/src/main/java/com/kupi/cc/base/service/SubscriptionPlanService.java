package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.SubscriptionPlan;
import com.kupi.cc.base.domain.request.SubscriptionUpdateRequestDto;
import com.kupi.cc.base.domain.response.SubscriptionPlanListResponseDto;
import com.kupi.cc.base.enums.SubscriptionStatus;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.ExistingNameException;
import com.kupi.cc.base.exception.InvalidSubscriptionPlanException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.repository.SubscriptionPlanRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubscriptionPlanService {

    private static final String ALL = "ALL";
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public SubscriptionPlanService(SubscriptionPlanRepository subscriptionPlanRepository) {
        this.subscriptionPlanRepository = subscriptionPlanRepository;
    }

    /**
     * This method saves the subscription plan.
     *
     * @param subscriptionPlan SubscriptionPlan
     */
    public void createSubscriptionPlan(SubscriptionPlan subscriptionPlan, UserType userType) {
        try {
            boolean existsByTitle = checkSubscriptionPlanTitleExists(subscriptionPlan.getTitle(), null, userType);
            if (existsByTitle) {
                throw new ExistingNameException("Existing subscription plan title.");
            }

            subscriptionPlanRepository.save(subscriptionPlan);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Saving subscription plan into the database was failed.", e);
        }
    }

    /**
     * This method checks the subscription name exist in the database.
     *
     * @param subscriptionTitle title
     * @param userType          user Type
     */
    public boolean checkSubscriptionPlanTitleExists(String subscriptionTitle, String id, UserType userType) {
        try {
            return (id == null) ? subscriptionPlanRepository.existsByTitleAndUserRole(subscriptionTitle, userType) :
                    subscriptionPlanRepository.existsByTitleAndUserRoleAndIdNot(subscriptionTitle, userType, id);
        } catch (DataAccessException e) {
            throw new KupiCoreException("checking subscription plan title availability for" + subscriptionTitle +
                    " is exist from the database was failed.", e);
        }
    }

    /**
     * This method is used to get subscription plan by id.
     *
     * @param subscriptionId subscription id
     * @return subscription plan
     */
    public SubscriptionPlan getSubscriptionPlanById(String subscriptionId) {
        try {
            return subscriptionPlanRepository.findById(subscriptionId).orElseThrow(
                    () -> new InvalidSubscriptionPlanException("Invalid subscription plan id: " + subscriptionId));
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading subscription plan by id: "
                    + subscriptionId + " from database was failed.", e);
        }
    }

    /**
     * This method used to get all subscription plans.
     *
     * @param pageable   pageable
     * @param searchTerm search term
     * @param userType   userType
     * @return userType
     */
    public Page<SubscriptionPlan> getSubscriptionPlanPage(Pageable pageable,
                                                          String searchTerm, UserType userType) {
        try {
            if (searchTerm.equals(ALL))
                return subscriptionPlanRepository.getAllByUserRole(pageable, userType);
            else
                return subscriptionPlanRepository.findAllBySearchTermContaining(pageable, userType, searchTerm);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading subscription plans from database was failed.", e);
        }
    }

    /**
     * This method used to get all active subscription plans.
     *
     * @param pageable   pageable
     * @param searchTerm search term
     * @param userType   userType
     * @return Subscription plan page
     */
    public Page<SubscriptionPlan> getActiveSubscriptionPlan(Pageable pageable,
                                                            String searchTerm, UserType userType) {
        try {
            if (searchTerm.equals(ALL))
                return subscriptionPlanRepository
                        .getAllByUserRoleAndStatus(pageable, userType, SubscriptionStatus.ACTIVE);
            else
                return subscriptionPlanRepository
                        .findAllActiveStatusBySearchTermContaining(pageable, userType, searchTerm, SubscriptionStatus.ACTIVE);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading active subscription plans from database was failed.", e);
        }
    }

    /**
     * This method used to update a subscription plan by id.
     *
     * @param subscriptionUpdateRequestDto subscriptionUpdateRequestDto
     * @return SubscriptionPlan
     */
    public SubscriptionPlan updateSubscriptionPlan(SubscriptionUpdateRequestDto subscriptionUpdateRequestDto) {
        try {
            var subscriptionPlan = getSubscriptionPlanById(subscriptionUpdateRequestDto.getId());
            subscriptionPlan.update(subscriptionUpdateRequestDto);
            boolean existsByTitle = checkSubscriptionPlanTitleExists(subscriptionPlan.getTitle(),
                    subscriptionPlan.getId(), subscriptionUpdateRequestDto.getUserRole());
            if (existsByTitle) {
                throw new ExistingNameException("Existing subscription plan title.");
            }
            return subscriptionPlanRepository.save(subscriptionPlan);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Updating subscription plan by id: "
                    + subscriptionUpdateRequestDto.getId() + " from database was failed.", e);
        }
    }

    /**
     * This method used to update the status of a subscription plan.
     *
     * @param id     subscriptionPlan id
     * @param status subscriptionPlan status
     */
    public void updateSubscriptionPlanStatus(String id, SubscriptionStatus status) {
        try {
            var subscriptionPlan = getSubscriptionPlanById(id);
            subscriptionPlan.setStatus(status);
            subscriptionPlanRepository.save(subscriptionPlan);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Updating status of subscription plan from database was failed.", e);
        }
    }

    /**
     * This method used to delete a subscription by id.
     *
     * @param subscriptionId subscription id
     */
    public void deleteSubscriptionPlanById(String subscriptionId) {
        try {
            SubscriptionPlan subscriptionPlanById = getSubscriptionPlanById(subscriptionId);
            subscriptionPlanRepository.delete(subscriptionPlanById);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Deleting subscription plans by id: "
                    + subscriptionId + " from database was failed.", e);
        }
    }

    /**
     * This method used to return active subscription plan list
     *
     * @return active subscription plan List
     */

    public List<SubscriptionPlan> getSubscriptionPlanListResponseDto() {
        try {
            return subscriptionPlanRepository.findAll();
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading subscription plans from database was failed.", e);
        }
    }
}
