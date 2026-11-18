package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.CategoryBrandMerchantIndex;
import com.kupi.cc.base.domain.request.BulkUserRequestDto;
import com.kupi.cc.base.domain.response.BasicMerchantBusinessResponseDto;
import com.kupi.cc.base.domain.response.MerchantBusinessResponseDto;
import com.kupi.cc.base.enums.UserType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class BankUpdateService {

    private static final int STARTING_PAGE = 1;
    private static final int PAGE = 0;
    private static final String CRON = "0 0 */1 * * *";
    private static final String TIME_ZONE = "GMT +05:30";
    private final int size;
    private final CategoryBrandMerchantIndexService categoryBrandMerchantIndexService;
    private final CategoryBrandMerchantService categoryBrandMerchantService;
    private final RequestADealSearchIndexService requestADealSearchIndexService;
    private final DealsOfTheDaySearchIndexService dealsOfTheDaySearchIndexService;
    private final DealSearchService dealSearchService;
    private final AuthUserService authUserService;
    private int pageCount;

    public BankUpdateService(
            @Value("${dataUpdateScheduler.size}") int size,
            CategoryBrandMerchantIndexService categoryBrandMerchantIndexService,
            CategoryBrandMerchantService categoryBrandMerchantService,
            RequestADealSearchIndexService requestADealSearchIndexService,
            DealsOfTheDaySearchIndexService dealsOfTheDaySearchIndexService,
            DealSearchService dealSearchService, AuthUserService authUserService) {
        this.size = size;
        this.categoryBrandMerchantIndexService = categoryBrandMerchantIndexService;
        this.categoryBrandMerchantService = categoryBrandMerchantService;
        this.requestADealSearchIndexService = requestADealSearchIndexService;
        this.dealsOfTheDaySearchIndexService = dealsOfTheDaySearchIndexService;
        this.dealSearchService = dealSearchService;
        this.authUserService = authUserService;
    }

    /**
     * This method executes every hour to update bank data.
     */
    @Scheduled(cron = CRON, zone = TIME_ZONE)
    private void update() {
        log.debug("Bank data update service started..");
        var firstBulkOfBankData = getFirstBulkOfBankData();
        var bankDataBulkFromAuth =
                getBankDataBulkFromAuth(firstBulkOfBankData.keySet());
        var changedBankDataMap =
                checkChanges(firstBulkOfBankData, bankDataBulkFromAuth);
        updateBankDataInfo(changedBankDataMap);
        updateOtherBankData();
        log.debug("Bank data update service finished..");
    }

    /**
     * This method get first paginated data from categoryBrandMerchantIndex.
     */
    private Map<String, BasicMerchantBusinessResponseDto> getFirstBulkOfBankData() {
        Pageable pageable = PageRequest.of(PAGE, size);
        var allCategoryBrandMerchantIndex =
                categoryBrandMerchantIndexService.getAllCategoryBrandMerchantIndexByUserTypeMerchant(pageable, UserType.BANK);
        this.pageCount = allCategoryBrandMerchantIndex.getTotalPages();
        return crateBasicMerchantBusinessResponseDtoMap(allCategoryBrandMerchantIndex.getContent());
    }

    /**
     * This method will update all bank data by incrementing pageable.
     */
    private void updateOtherBankData() {
        if (this.pageCount > 1) {
            int currentPage = STARTING_PAGE;
            while (currentPage < pageCount) {
                Pageable pageable = PageRequest.of(currentPage, size);
                var allCategoryBrandMerchantIndex =
                        categoryBrandMerchantIndexService
                                .getAllCategoryBrandMerchantIndexByUserTypeMerchant(pageable, UserType.BANK);
                var bankBusinessFromDb =
                        crateBasicMerchantBusinessResponseDtoMap(allCategoryBrandMerchantIndex.getContent());
                var bankDataBulkFromAuth =
                        getBankDataBulkFromAuth(bankBusinessFromDb.keySet());
                var changedBankMap =
                        checkChanges(bankBusinessFromDb, bankDataBulkFromAuth);
                updateBankDataInfo(changedBankMap);
                currentPage++;
            }
        }
    }

    /**
     * This method returns a map from a list of categoryBrandMerchantIndex.
     *
     * @param categoryBrandMerchantIndexes categoryBrandMerchantIndexes list
     * @return Map
     */
    private Map<String, BasicMerchantBusinessResponseDto> crateBasicMerchantBusinessResponseDtoMap
    (List<CategoryBrandMerchantIndex>
             categoryBrandMerchantIndexes) {
        Map<String, BasicMerchantBusinessResponseDto> basicMerchantIndexListMapFromDb = new HashMap<>();
        for (var categoryBrandMerchantIndex : categoryBrandMerchantIndexes) {
            var basicMerchantBusinessResponseDto = new BasicMerchantBusinessResponseDto(categoryBrandMerchantIndex);
            basicMerchantIndexListMapFromDb.put(basicMerchantBusinessResponseDto.getId(), basicMerchantBusinessResponseDto);
        }
        return basicMerchantIndexListMapFromDb;
    }

    /**
     * This method used to get bank data from AuthService.
     *
     * @param bankIdSet bankIdSet
     * @return Map of String & MerchantBusinessResponseDto
     */
    private Map<String, MerchantBusinessResponseDto> getBankDataBulkFromAuth(Set<String> bankIdSet) {
        Map<String, MerchantBusinessResponseDto> authMerchantData = new HashMap<>();
        if (!bankIdSet.isEmpty()) {
            BulkUserRequestDto bulkUserRequestDto = new BulkUserRequestDto(new ArrayList<>(bankIdSet));
            authMerchantData = authUserService.getMerchantMap(bulkUserRequestDto, UserType.BANK);
        }
        return authMerchantData;
    }

    /**
     * This method checks differences between auth bulk merchant data and db merchant bulk data.
     *
     * @param dbBankBulkMap   dbBankBulkMap
     * @param authBankBulkMap authBankBulkMap
     * @return Map of String & MerchantBusinessResponseDto
     */
    private Map<String, MerchantBusinessResponseDto> checkChanges(Map<String, BasicMerchantBusinessResponseDto> dbBankBulkMap,
                                                                  Map<String, MerchantBusinessResponseDto> authBankBulkMap) {
        Map<String, MerchantBusinessResponseDto> changedMerchantDetailsMap = new HashMap<>();
        for (var authMerchant : authBankBulkMap.entrySet()) {
            var authMerchantData = authMerchant.getValue();
            var dbMerchantData = dbBankBulkMap.get(authMerchant.getKey());
            if (!authMerchantData.getName().equals(dbMerchantData.getName())
                    || !authMerchantData.getImageUrl().equals(dbMerchantData.getImageUrl())
                    || !authMerchantData.getApprovalStatus().equals(dbMerchantData.getApprovalStatus())
                    || authMerchantData.isActive() != dbMerchantData.isActive()) {
                changedMerchantDetailsMap.put(authMerchantData.getId(), authMerchant.getValue());
            }
        }
        return changedMerchantDetailsMap;
    }

    /**
     * This method will update merchant data by calling relevant services.
     *
     * @param changedBankDataMap merchantBusinessResponseDto map
     */
    private void updateBankDataInfo(Map<String, MerchantBusinessResponseDto> changedBankDataMap) {
        categoryBrandMerchantIndexService.updateMerchantInfo(changedBankDataMap);
        categoryBrandMerchantService.updateMerchantInfo(changedBankDataMap);
        dealSearchService.updateMerchantInfo(changedBankDataMap);
        dealsOfTheDaySearchIndexService.updateMerchantInfo(changedBankDataMap);
        requestADealSearchIndexService.updateMerchantInfo(changedBankDataMap);
    }
}
