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
public class MerchantUpdateService {
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

    public MerchantUpdateService(
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
     * This method executes every hour to update merchant data.
     */
    @Scheduled(cron = CRON, zone = TIME_ZONE)
    private void update() {
        log.debug("Merchant data update service started..");
        var firstBulkOfMerchantData = getFirstBulkOfMerchantData();
        var merchantDataBulkFromAuth =
                getMerchantDataBulkFromAuth(firstBulkOfMerchantData.keySet());
        var changedMerchantDataMap =
                checkChanges(firstBulkOfMerchantData, merchantDataBulkFromAuth);
        updateMerchantDataInfo(changedMerchantDataMap);
        updateOtherMerchantData();
        log.debug("Merchant data update service finished..");
    }

    /**
     * This method get first paginated data from categoryBrandMerchantIndex.
     */
    private Map<String, BasicMerchantBusinessResponseDto> getFirstBulkOfMerchantData() {
        Pageable pageable = PageRequest.of(PAGE, size);
        var allCategoryBrandMerchantIndex =
                categoryBrandMerchantIndexService
                        .getAllCategoryBrandMerchantIndexByUserTypeMerchant(pageable, UserType.MERCHANT);
        this.pageCount = allCategoryBrandMerchantIndex.getTotalPages();
        return crateBasicMerchantBusinessResponseDtoMap(allCategoryBrandMerchantIndex.getContent());
    }

    /**
     * This method will update all merchant data by incrementing pageable.
     */
    private void updateOtherMerchantData() {
        if (this.pageCount > 1) {
            int currentPage = STARTING_PAGE;
            while (currentPage < pageCount) {
                Pageable pageable = PageRequest.of(currentPage, size);
                var allCategoryBrandMerchantIndex =
                        categoryBrandMerchantIndexService
                                .getAllCategoryBrandMerchantIndexByUserTypeMerchant(pageable, UserType.MERCHANT);
                var merchantBusinessFromDb =
                        crateBasicMerchantBusinessResponseDtoMap(allCategoryBrandMerchantIndex.getContent());
                var merchantDataBulkFromAuth =
                        getMerchantDataBulkFromAuth(merchantBusinessFromDb.keySet());
                var changedMerchantMap =
                        checkChanges(merchantBusinessFromDb, merchantDataBulkFromAuth);
                updateMerchantDataInfo(changedMerchantMap);
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
     * This method used to get merchant data from AuthService.
     *
     * @param merchantIdSet merchantIdSet
     * @return Map of String & MerchantBusinessResponseDto
     */
    private Map<String, MerchantBusinessResponseDto> getMerchantDataBulkFromAuth(Set<String> merchantIdSet) {
        Map<String, MerchantBusinessResponseDto> authMerchantData = new HashMap<>();
        if (!merchantIdSet.isEmpty()) {
            BulkUserRequestDto bulkUserRequestDto = new BulkUserRequestDto(new ArrayList<>(merchantIdSet));
            authMerchantData = authUserService.getMerchantMap(bulkUserRequestDto, UserType.MERCHANT);
        }
        return authMerchantData;
    }

    /**
     * This method checks differences between auth bulk merchant data and db merchant bulk data.
     *
     * @param dbMerchantBulkMap   dbMerchantBulkMap
     * @param authMerchantBulkMap authMerchantBulkMap
     * @return Map of String & MerchantBusinessResponseDto
     */
    private Map<String, MerchantBusinessResponseDto> checkChanges
    (Map<String, BasicMerchantBusinessResponseDto> dbMerchantBulkMap,
     Map<String, MerchantBusinessResponseDto> authMerchantBulkMap) {
        Map<String, MerchantBusinessResponseDto> changedMerchantDetailsMap = new HashMap<>();
        for (var authMerchant : authMerchantBulkMap.entrySet()) {
            var authMerchantData = authMerchant.getValue();
            var dbMerchantData = dbMerchantBulkMap.get(authMerchant.getKey());
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
     * @param changedMerchantDataMap merchantBusinessResponseDto map
     */
    private void updateMerchantDataInfo(Map<String, MerchantBusinessResponseDto> changedMerchantDataMap) {
        categoryBrandMerchantIndexService.updateMerchantInfo(changedMerchantDataMap);
        categoryBrandMerchantService.updateMerchantInfo(changedMerchantDataMap);
        dealSearchService.updateMerchantInfo(changedMerchantDataMap);
        dealsOfTheDaySearchIndexService.updateMerchantInfo(changedMerchantDataMap);
        requestADealSearchIndexService.updateMerchantInfo(changedMerchantDataMap);
    }
}