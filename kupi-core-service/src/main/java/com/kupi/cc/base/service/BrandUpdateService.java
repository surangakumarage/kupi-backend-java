package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.Brand;
import com.kupi.cc.base.domain.entity.DealSearchIndex;
import com.kupi.cc.base.domain.entity.RequestADealSearchIndex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Brand update service
 */
@Service
public class BrandUpdateService {

    private static final int FIRST_PAGE = 0;
    private final int size;
    private final DealSearchService dealSearchService;
    private final RequestADealSearchIndexService requestADealSearchIndexService;
    private final DealsOfTheDaySearchIndexService dealsOfTheDaySearchIndexService;

    public BrandUpdateService(@Value("${dataUpdateScheduler.size}") int size,
                              DealSearchService dealSearchService,
                              DealsOfTheDaySearchIndexService dealsOfTheDaySearchIndexService,
                              RequestADealSearchIndexService requestADealSearchIndexService) {
        this.dealSearchService = dealSearchService;
        this.dealsOfTheDaySearchIndexService = dealsOfTheDaySearchIndexService;
        this.requestADealSearchIndexService = requestADealSearchIndexService;
        this.size = size;
    }

    /**
     * This method updates all tables that the brand name stored in.
     *
     * @param brand             brand
     * @param previousBrandName brand name
     */
    @Async
    public void updateBrandData(Brand brand, String previousBrandName) {
        updateDealSearchIndex(brand, previousBrandName);
        updateRequestADealSearchIndex(brand);
        updateDealsOfTheDaySearchIndexByPage(brand, previousBrandName);
    }

    /**
     * This method updates the first page of dealSearchIndex by new brand id.
     *
     * @param brand             brand
     * @param previousBrandName previousBrandName
     */
    @Async
    public void updateDealSearchIndex(Brand brand, String previousBrandName) {
        var currentPage = FIRST_PAGE;
        int totalPage;
        do {
            Pageable pageable = PageRequest.of(currentPage, size);
            var dealsListByBrandIdPage = dealSearchService
                    .getDealSearchIndexByBrandId(pageable, brand.getId());
            totalPage = dealsListByBrandIdPage.getTotalPages();
            updateDealSearchIndexByPage(dealsListByBrandIdPage, previousBrandName, brand.getName());
            currentPage++;
        } while (currentPage < totalPage);
    }

    /**
     * This method replaces old brand name with new brand name in dealOfTheDayService index.
     *
     * @param brand             brand
     * @param previousBrandName previous brand name
     */
    @Async
    public void updateDealsOfTheDaySearchIndexByPage(Brand brand, String previousBrandName) {
        var dealsOfTheDaySearchIndexList =
                dealsOfTheDaySearchIndexService.getDealsOfTheDaySearchIndexByBrandId(brand.getId());
        for (var dealOfTheDaySearchIndex : dealsOfTheDaySearchIndexList) {
            var newBrandName = dealOfTheDaySearchIndex
                    .getBrandNames().replace(previousBrandName, brand.getName());
            dealOfTheDaySearchIndex.setBrandNames(newBrandName);
        }
        dealsOfTheDaySearchIndexService.saveAll(dealsOfTheDaySearchIndexList);
    }

    /**
     * This method updates brand in RequestADealSearchIndex.
     *
     * @param brand brand
     */
    @Async
    public void updateRequestADealSearchIndex(Brand brand) {
        var currentPage = FIRST_PAGE;
        int totalPage;
        do {
            Pageable pageable = PageRequest.of(currentPage, size);
            var requestADealSearchIndexPage = requestADealSearchIndexService
                    .getRequestADDealSearchIndexesByBrandId(pageable, brand.getId());
            totalPage = requestADealSearchIndexPage.getTotalPages();
            updateRequestADealSearchIndexByPage(requestADealSearchIndexPage, brand.getName());
            currentPage++;
        } while (currentPage < totalPage);
    }

    /**
     * This method replaces and saves the dealSearchIndexes by current name.
     *
     * @param dealSearchIndexPage dealSearchIndex page
     * @param previousBrandName   previous brand name
     * @param currentBrandName    current brand name
     */
    private void updateDealSearchIndexByPage(
            Page<DealSearchIndex> dealSearchIndexPage, String previousBrandName, String currentBrandName) {
        List<DealSearchIndex> dealSearchIndexList = new ArrayList<>();
        for (var dealSearchIndex : dealSearchIndexPage.getContent()) {
            String newBrandName = dealSearchIndex
                    .getBrandNames().replace(previousBrandName, currentBrandName);
            dealSearchIndex.setBrandNames(newBrandName);
            dealSearchIndexList.add(dealSearchIndex);
        }
        dealSearchService.saveListOfDealSearchIndexes(dealSearchIndexList);
    }

    /**
     * This method replaces old brand name with new brand name in requestADealSearchIndex.
     *
     * @param requestADealSearchIndexPage requestADealSearchIndex page
     * @param currentBrandName            currentBrandName
     */
    private void updateRequestADealSearchIndexByPage(
            Page<RequestADealSearchIndex> requestADealSearchIndexPage, String currentBrandName) {
        List<RequestADealSearchIndex> requestADealSearchIndexList = new ArrayList<>();
        for (var requestADealSearchIndex : requestADealSearchIndexPage.getContent()) {
            requestADealSearchIndex.setBrandName(currentBrandName);
            requestADealSearchIndexList.add(requestADealSearchIndex);
        }
        requestADealSearchIndexService.saveListOfRequestADealIndex(requestADealSearchIndexList);
    }
}