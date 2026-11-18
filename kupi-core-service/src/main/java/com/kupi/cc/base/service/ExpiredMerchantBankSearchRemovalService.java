package com.kupi.cc.base.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ExpiredMerchantBankSearchRemovalService {

    private static final String CRON = "0 0 */1 * * *";
    private static final String TIME_ZONE = "GMT +05:30";
    private static final int PAGE = 0;
    private static final int SIZE = 250;
    private final MerchantBankSearchIndexService merchantBankSearchIndexService;
    private final DealService dealService;

    public ExpiredMerchantBankSearchRemovalService(MerchantBankSearchIndexService merchantBankSearchIndexService,
                                                   DealService dealService) {
        this.merchantBankSearchIndexService = merchantBankSearchIndexService;
        this.dealService = dealService;
    }

    /**
     * This method executes hour to remove expired bank deals from the bankDealSearchIndex.
     */
    @Scheduled(cron = CRON, zone = TIME_ZONE)
    private void removeExpiredSearchBankDeal() {
        log.debug("ExpiredMerchantBankSearchRemovalService started at: {}", System.currentTimeMillis());
        Pageable pageable = PageRequest.of(PAGE, SIZE);
        Page<String> allBankDealIdPage = merchantBankSearchIndexService.getAllBankDealIdList(pageable);
        List<String> allBankDealIdList = allBankDealIdPage.toList();
        for (int page = 1; page < allBankDealIdPage.getTotalPages(); page++) {
            pageable = PageRequest.of(page, SIZE);
            allBankDealIdList.addAll(merchantBankSearchIndexService.getAllBankDealIdList(pageable).toList());
        }
        var notFoundDealIds = getAllReadyDeletedBankDealIdsFromBankDeals(allBankDealIdList);
        merchantBankSearchIndexService.removeDeals(notFoundDealIds);
        var expiredDealIds = getExpiredDealIds(allBankDealIdList);
        merchantBankSearchIndexService.removeDeals(expiredDealIds);
        merchantBankSearchIndexService.removeInactiveBanks();
        merchantBankSearchIndexService.removeInactiveMerchants();
        log.debug("ExpiredMerchantBankSearchRemovalService finished at: {}", System.currentTimeMillis());
    }

    /**
     * This method returns the expired bank deal list.
     *
     * @param allBankDealIds bank dealIds list
     * @return expired bank deal list
     */
    private List<String> getExpiredDealIds(List<String> allBankDealIds) {
        List<String> expiredBankDealIds = new ArrayList<>();
        allBankDealIds.forEach(id -> {
            boolean isDealExpired = dealService.checkDealIsExpired(id);
            if (isDealExpired) {
                expiredBankDealIds.add(id);
            }
        });
        return expiredBankDealIds;
    }

    /**
     * This method returns a list bank deal ids not exist in bank deal table.
     *
     * @param allBankDealIds allBankDealIds
     * @return bank deal list
     */
    private List<String> getAllReadyDeletedBankDealIdsFromBankDeals(List<String> allBankDealIds) {
        List<String> notExistByDealIds = new ArrayList<>();
        allBankDealIds.forEach(id -> {
            boolean isDealExpired = dealService.checkBankDealExistByDealId(id);
            if (!isDealExpired) {
                notExistByDealIds.add(id);
            }
        });
        return notExistByDealIds;
    }
}