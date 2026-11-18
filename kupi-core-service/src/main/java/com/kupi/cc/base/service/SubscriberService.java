package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.Subscribe;
import com.kupi.cc.base.domain.entity.SubscriptionPlan;
import com.kupi.cc.base.domain.entity.SubscriptionRenewal;
import com.kupi.cc.base.domain.request.BulkUserRequestDto;
import com.kupi.cc.base.domain.request.MobileNoRequestDto;
import com.kupi.cc.base.domain.response.BusinessMerchantResponseDto;
import com.kupi.cc.base.domain.response.MerchantBusinessResponseDto;
import com.kupi.cc.base.enums.LimitExceededType;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.*;
import com.kupi.cc.base.repository.SubscriberRepository;
import com.kupi.cc.base.repository.SubscriptionPlanRepository;
import com.kupi.cc.base.repository.SubscriptionRenewalRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@Slf4j
public class SubscriberService {

    private static final int ZERO = 0;
    private static final int SIZE = 250;
    private static final int EXPIRY_NOTICE_PERIOD = 7;
    private static final String SRI_LANKA_TIME_ZONE = "GMT +05:30";
    private final SubscriberRepository subscriberRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscribeSearchIndexService subscribeSearchIndexService;
    private final SubscriptionRenewalRepository subscriptionRenewalRepository;
    private final AuthUserService authUserService;
    private final NotificationService notificationService;

    public SubscriberService(
            SubscriberRepository subscriberRepository,
            SubscriptionPlanRepository subscriptionPlanRepository,
            SubscribeSearchIndexService subscribeSearchIndexService,
            SubscriptionRenewalRepository subscriptionRenewalRepository,
            AuthUserService authUserService,
            NotificationService notificationService) {
        this.subscriberRepository = subscriberRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.subscribeSearchIndexService = subscribeSearchIndexService;
        this.subscriptionRenewalRepository = subscriptionRenewalRepository;
        this.authUserService = authUserService;
        this.notificationService = notificationService;
    }

    /**
     * This scheduler method updates subscriptionRenewal table.
     * Runs every day at 12.00.a.m.
     */
    @Scheduled(cron = "${subscription.renewalCron}", zone = SRI_LANKA_TIME_ZONE)
    void renewSubscriptions() {
        log.info("Started running scheduled subscription renewal job at: " + System.currentTimeMillis());
        cancelExpiredSubscriptions();
    }

    private void cancelExpiredSubscriptions() {
        LocalDate todayLocalDate = LocalDate.now();
        int today = todayLocalDate.getDayOfMonth();
        int lastDayOfMonth = todayLocalDate.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();

        try {
            int page = 0;
            int totalPages;
            do {
                Pageable pageable = PageRequest.of(page, SIZE);

                Page<SubscriptionRenewal> subscriptionRenewalPage;
                if (today == lastDayOfMonth) {
                    subscriptionRenewalPage = subscriptionRenewalRepository
                            .findByRenewOnDayOfEveryMonthGreaterThanEqualAndIsUnsubscribedFalse(today, pageable);
                    subscriptionRenewalRepository
                            .deleteByRenewOnDayOfEveryMonthGreaterThanEqualAndIsUnsubscribedTrue(today);
                } else {
                    subscriptionRenewalPage = subscriptionRenewalRepository
                            .findByRenewOnDayOfEveryMonthAndIsUnsubscribedFalse(today, pageable);
                    subscriptionRenewalRepository.deleteByRenewOnDayOfEveryMonthAndIsUnsubscribedTrue(today);
                }

                var subscriptionRenewalList = subscriptionRenewalPage.toList();

                subscriptionRenewalList.forEach(renewal -> {
                    var optionalSubscriptionPlan = subscriptionPlanRepository.findById(renewal.getSubscriptionPlanId());
                    optionalSubscriptionPlan.ifPresent(renewal::resetSubscriptionLimits);
                });
                subscriptionRenewalRepository.saveAll(subscriptionRenewalList);

                totalPages = subscriptionRenewalPage.getTotalPages();
                page++;
            } while (page < totalPages);

        } catch (DataAccessException e) {
            log.error("Reading subscriptionRenewal database was failed on: " + todayLocalDate, e);
        }
    }


    /**
     * This method is used to get subscription plan by id
     *
     * @param subscriptionPlanId subscription plan id
     * @return SubscriptionPlan
     */
    private SubscriptionPlan getSubscriptionPlanById(String subscriptionPlanId) {
        try {
            Optional<SubscriptionPlan> optionalSubscriptionPlan = subscriptionPlanRepository.findById(subscriptionPlanId);
            if (optionalSubscriptionPlan.isPresent()) {
                return optionalSubscriptionPlan.get();
            }
            throw new InvalidSubscriptionPlanException("Invalid subscription plan id: " + subscriptionPlanId);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Get subscription plan from database is failed for id: " + subscriptionPlanId, e);
        }
    }

    /**.
     * This method is used to send notification to merchant
     */
    private void sendExpiryWarningNotification() {
        LocalDate afterNLocalDays = LocalDate.now().plusDays(EXPIRY_NOTICE_PERIOD);
        int dayAfterNDays = afterNLocalDays.getDayOfMonth();
        int lastDayOfMonth = afterNLocalDays.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();

        try {
            int page;
            int totalPages;
            do {
                page = 0;
                Pageable pageable = PageRequest.of(page, SIZE);

                Page<SubscriptionRenewal> subscriptionRenewalPage;
                if (dayAfterNDays == lastDayOfMonth) {
                    subscriptionRenewalPage = subscriptionRenewalRepository
                            .findByRenewOnDayOfEveryMonthGreaterThanEqualAndIsUnsubscribedFalse(dayAfterNDays, pageable);
                } else {
                    subscriptionRenewalPage = subscriptionRenewalRepository
                            .findByRenewOnDayOfEveryMonthAndIsUnsubscribedFalse(dayAfterNDays, pageable);
                }

                var subscriptionRenewalList = subscriptionRenewalPage.toList();

                Map<String, SubscriptionRenewal> subscriptionRenewalMap = new HashMap<>();
                List<String> userIds = new ArrayList<>();
                for (SubscriptionRenewal subscriptionRenewal : subscriptionRenewalList) {
                    subscriptionRenewalMap.put(subscriptionRenewal.getUserId(), subscriptionRenewal);
                    userIds.add(subscriptionRenewal.getUserId());
                }
                if (userIds.isEmpty()) {
                    return;
                }
                BulkUserRequestDto userRequestDto = new BulkUserRequestDto(userIds);
                Map<String, MerchantBusinessResponseDto> userMap = authUserService.getMerchantMap(userRequestDto, UserType.MERCHANT);

                for (Map.Entry<String,MerchantBusinessResponseDto> merchantEntry : userMap.entrySet()) {
                    var merchantBusinessResponseDto = merchantEntry.getValue();
                    var subscriptionRenewal = subscriptionRenewalMap.get(merchantEntry.getKey());
                    var subscriptionPlan = getSubscriptionPlanById(subscriptionRenewal.getSubscriptionPlanId());
                    var expireDate = subscriptionRenewal.getSubscribedDate().toLocalDate().plusMonths(1);
                    var mobileRequestDto = new MobileNoRequestDto(merchantBusinessResponseDto.getMobileNo().getCountryCode(),
                            merchantBusinessResponseDto.getMobileNo().getLocalNumber());
                    notificationService.sendSms(mobileRequestDto, subscriptionPlan.getTitle(), expireDate.toString());
                }

                totalPages = subscriptionRenewalPage.getTotalPages();
            } while (page < totalPages);

        } catch (DataAccessException e) {
            log.error("Reading from subscriptionRenewal database was failed", e);
        }
    }

    /**
     * This method is used to set subscription for merchant and bank.
     *
     * @param subscriptionPlan subscription plan
     * @param businessMerchant business merchant
     * @param userType         userType
     * @return Subscribe
     */
    @Transactional
    public Subscribe setSubscribe( String subscriptionId,
            SubscriptionPlan subscriptionPlan, BusinessMerchantResponseDto businessMerchant, UserType userType) {
        try {
            if (!subscriptionPlan.getUserRole().equals(userType)) {
                throw new UnsupportedUserTypeException("Unsupported user type to subscribe the plan id: "
                        + subscriptionPlan.getId());
            }

            var subscribe = new Subscribe(subscriptionId, subscriptionPlan, businessMerchant.getMerchantId(), userType);
            SubscriptionRenewal subscriptionRenewal = new SubscriptionRenewal(subscribe);
            subscriberRepository.save(subscribe);
            subscriptionRenewalRepository.save(subscriptionRenewal);
            subscribeSearchIndexService.save(subscribe, businessMerchant.getBusinessName());

            return subscribe;
        } catch (DataAccessException e) {
            throw new KupiCoreException("Setting Subscription was failed for userId: " +
                    businessMerchant.getMerchantId() + " and planId: " + subscriptionPlan.getId(), e);
        }
    }

    /**
     * This method checks the user has not any active subscribe plan.
     * FIXME: is it possible to have subscriptionRenewal record while being marked as unsubscribed
     * @param businessUserId merchant/ bank user id.
     */
    public void checkUserIsNotInAPlan(String businessUserId) {
        try {
            Optional<SubscriptionRenewal> optionalSubscriptionRenewal =
                    subscriptionRenewalRepository.findByUserId(businessUserId);
            if (optionalSubscriptionRenewal.isPresent()) {
                throw new ExistingSubscribedUserException("User already subscribe a plan. userId: " + businessUserId);
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException("Reading Subscription was failed", e);
        }
    }

    /**
     * This method returns true when subscription id associate with any user.
     *
     * @param subscriptionPlanId subscription plan id
     */
    public void checkSubscriptionPlanHasUsers(String subscriptionPlanId) {
        try {
            var existUsers = subscriberRepository
                    .existsBySubscriptionPlanIdAndIsDeletedFalse(subscriptionPlanId);
            if (existUsers) {
                throw new UnsupportedDeleteSubscriptionPlanException("Subscription Plan has users.");
            }
        } catch (DataAccessException e) {
            throw new KupiCoreException("Checking subscription plan id: "
                    + subscriptionPlanId + " from subscription table was failed", e);
        }
    }

    /**
     * This method used to get subscribe-object by subscribe id.
     *
     * @param subscribeId subscribeId
     * @return Subscribe
     */
    public Subscribe getSubscribeById(String subscribeId) {
        try {
            return subscriberRepository.findById(subscribeId).orElseThrow(
                    () -> new InvalidSubscribeException("Invalid Subscribe id: " + subscribeId));
        } catch (DataAccessException e) {
            throw new KupiCoreException("Returning subscribe by id: " + subscribeId + " from database was failed", e);
        }
    }

    /**
     * This method is used to unsubscribe the subscription.
     *
     * @param subscribedId subscribe id
     * @param userType     userType
     */
    public void unsubscribeAPlan(String subscribedId, UserType userType) {
        try {
            var subscribe = getSubscribeById(subscribedId);
            if (!subscribe.getUserType().equals(userType)) {
                throw new UnsupportedUserTypeException("Unsupported user type to unsubscribe the plan.");
            }
            SubscriptionRenewal subscriptionRenewal = getSubscriptionRenewalById(subscribedId);
            subscriptionRenewal.setUnsubscribed(true);
            subscriptionRenewal.setUpdatedAt(System.currentTimeMillis());
            saveSubscriptionRenewal(subscriptionRenewal);
            subscribe.setDeleted(true);
            subscribe.setUpdatedAt(System.currentTimeMillis());
            subscriberRepository.save(subscribe);
            subscribeSearchIndexService.removeSubscribeDataFromSearchIndex(subscribedId);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Unsubscribe the subscription plan for subscribed id: "
                    + subscribedId + " was failed.");
        }
    }

    /**
     * This method is used to get current subscribe by user id.
     *
     * @param userId user id
     * @return optional subscribe
     */
    public Optional<Subscribe> getSubscribeByUserId(String userId) {
        try {
            return subscriberRepository.getByUserIdAndIsDeletedFalse(userId);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Returning subscribe from database was failed for user-id: " + userId, e);
        }
    }

    /**
     * This method is used to get number of subscribers for subscription plan.
     *
     * @param subscriptionPlanId subscription plan id
     * @return subscription user count
     */
    public long getSubscribersCountBySubscriptionPlanId(String subscriptionPlanId) {
        try {
            return subscriberRepository.countSubscribeBySubscriptionPlanIdAndIsDeletedFalse(subscriptionPlanId);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Returning subscribers count by subscription plan id: "
                    + subscriptionPlanId + " from database was failed", e);
        }
    }

    /**
     * This method checks if a subscription plan available for a user.
     *
     * @param userId userId
     * @return subscription renewal entity.
     */
    public SubscriptionRenewal checkSubscriptionPlanForDeal(String userId) {
        try {
            SubscriptionRenewal subscriptionRenewal = getSubscriptionRenewalByUserId(userId);
            if (subscriptionRenewal.getRemainingDealsPerMonth() == ZERO)
                throw new LimitExceededException("Maximum deal quota for month exceeded for userId: " + userId,
                        LimitExceededType.DEALS_PER_MONTH);
            return subscriptionRenewal;
        } catch (DataAccessException e) {
            throw new KupiCoreException("Checking subscription plan was failed for userId: " + userId, e);
        }
    }

    /**
     * This method returns subscription renewal entity by userId.
     *
     * @param userId userId
     * @return subscription renewal entity.
     */
    public SubscriptionRenewal getSubscriptionRenewalByUserId(String userId) {
        try {
            Optional<SubscriptionRenewal> optionalSubscription = subscriptionRenewalRepository.findByUserId(userId);
            if (optionalSubscription.isEmpty())
                throw new InvalidSubscribeException("Subscription not found for userId: " + userId);
            return optionalSubscription.get();
        } catch (DataAccessException e) {
            throw new KupiCoreException("Checking subscription plan was failed for userId: " + userId, e);
        }
    }

    /**
     * This method saves subscription renewal entity on database.
     *
     * @param subscriptionRenewal saveSubscriptionRenewal
     */
    public void saveSubscriptionRenewal(SubscriptionRenewal subscriptionRenewal) {
        try {
            subscriptionRenewalRepository.save(subscriptionRenewal);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Saving subscriptionRenewal to database was failed for id: " +
                    subscriptionRenewal.getId(), e);
        }
    }

    /**
     * This method returns subscription renewal entity by subscriptionId.
     *
     * @param subscriptionId subscriptionId
     * @return subscription renewal entity.
     */
    public SubscriptionRenewal getSubscriptionRenewalById(String subscriptionId) {
        try {
            Optional<SubscriptionRenewal> optionalSubscription = subscriptionRenewalRepository.findById(subscriptionId);
            if (optionalSubscription.isEmpty())
                throw new InvalidSubscribeException("Subscription not found for subscriptionId: " + subscriptionId);
            return optionalSubscription.get();
        } catch (DataAccessException e) {
            throw new KupiCoreException("Checking subscription plan was failed for subscriptionId: " + subscriptionId, e);
        }
    }
}