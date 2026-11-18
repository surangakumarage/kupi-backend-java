package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.Category;
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
 * Category update service
 */
@Service
public class CategoryUpdateService {

    private static final int FIRST_PAGE = 0;
    private final int size;
    private final DealSearchService dealSearchService;
    private final RequestADealSearchIndexService requestADealSearchIndexService;
    private final DealsOfTheDaySearchIndexService dealsOfTheDaySearchIndexService;

    public CategoryUpdateService(@Value("${dataUpdateScheduler.size}") int size,
                                 DealSearchService dealSearchService,
                                 DealsOfTheDaySearchIndexService dealsOfTheDaySearchIndexService,
                                 RequestADealSearchIndexService requestADealSearchIndexService) {
        this.dealSearchService = dealSearchService;
        this.dealsOfTheDaySearchIndexService = dealsOfTheDaySearchIndexService;
        this.requestADealSearchIndexService = requestADealSearchIndexService;
        this.size = size;
    }

    /**
     * This method updates all tables that the category name stored in.
     *
     * @param category             category
     * @param previousCategoryName category name
     */
    @Async
    public void updateCategoryData(Category category, String previousCategoryName) {
        updateDealSearchIndex(category, previousCategoryName);
        updateRequestADealSearchIndex(category);
        updateDealsOfTheDaySearchIndex(category, previousCategoryName);
    }

    /**
     * This method will retrieve date for requestADealSearchIndex and update them.
     *
     * @param category             category
     * @param previousCategoryName previousCategoryName
     */
    @Async
    public void updateDealSearchIndex(Category category, String previousCategoryName) {
        var currentPage = FIRST_PAGE;
        int totalPage;
        do {
            Pageable pageable = PageRequest.of(currentPage, size);
            var dealsListByCategoryIdPage = dealSearchService
                    .getDealSearchIndexByCategoryId(pageable, category.getId());
            totalPage = dealsListByCategoryIdPage.getTotalPages();
            updateDealSearchIndexByPage(dealsListByCategoryIdPage, previousCategoryName, category.getName());
            currentPage++;
        } while (currentPage < totalPage);
    }

    /**
     * This method replaces old category name with new category name in dealOfTheDaySearchIndex.
     *
     * @param category             category
     * @param previousCategoryName previous category name
     */
    @Async
    public void updateDealsOfTheDaySearchIndex(Category category, String previousCategoryName) {
        var dealsOfTheDaySearchIndexList =
                dealsOfTheDaySearchIndexService.getDealsOfTheDaySearchIndexByCategoryId(category.getId());
        for (var dealOfTheDaySearchIndex : dealsOfTheDaySearchIndexList) {
            var newCategoryName = dealOfTheDaySearchIndex
                    .getCategoryNames().replace(previousCategoryName, category.getName());
            dealOfTheDaySearchIndex.setCategoryNames(newCategoryName);
        }
        dealsOfTheDaySearchIndexService.saveAll(dealsOfTheDaySearchIndexList);
    }

    /**
     * This method updates category in of requestADealSearchIndex.
     *
     * @param category category
     */
    @Async
    public void updateRequestADealSearchIndex(Category category) {
        var currentPage = FIRST_PAGE;
        int totalPage;
        do {
            Pageable pageable = PageRequest.of(currentPage, size);
            var requestADealSearchIndexPage = requestADealSearchIndexService
                    .getRequestADDealSearchIndexByCategoryId(pageable, category.getId());
            totalPage = requestADealSearchIndexPage.getTotalPages();
            updateRequestADealSearchIndexesByPage(requestADealSearchIndexPage, category.getName());
            currentPage++;
        } while (currentPage < totalPage);
    }

    /**
     * This method replaces categoryName with new category name and update them in dealSearchIndex.
     *
     * @param previousCategoryName previousCategoryName
     * @param currentCategoryName  currentCategoryName
     */
    private void updateDealSearchIndexByPage(Page<DealSearchIndex> dealSearchIndexPage,
                                             String previousCategoryName, String currentCategoryName) {
        List<DealSearchIndex> dealSearchIndexList = new ArrayList<>();
        for (var dealSearchIndex : dealSearchIndexPage.getContent()) {
            var newCategoryNames = dealSearchIndex
                    .getCategoryNames().replace(previousCategoryName, currentCategoryName);
            dealSearchIndex.setCategoryNames(newCategoryNames);
            dealSearchIndexList.add(dealSearchIndex);
        }
        dealSearchService.saveListOfDealSearchIndexes(dealSearchIndexList);
    }

    /**
     * This method replaces old category name with new category name and update them in RequestADealIndex.
     *
     * @param requestADealSearchIndexPage dealSearchIndexPage
     * @param currentCategoryName         currentCategoryName
     */
    private void updateRequestADealSearchIndexesByPage(
            Page<RequestADealSearchIndex> requestADealSearchIndexPage, String currentCategoryName) {
        List<RequestADealSearchIndex> requestADealSearchIndexList = new ArrayList<>();
        for (var requestADealSearchIndex : requestADealSearchIndexPage.getContent()) {
            requestADealSearchIndex.setCategoryName(currentCategoryName);
            requestADealSearchIndexList.add(requestADealSearchIndex);
        }
        requestADealSearchIndexService.saveListOfRequestADealIndex(requestADealSearchIndexList);
    }
}
