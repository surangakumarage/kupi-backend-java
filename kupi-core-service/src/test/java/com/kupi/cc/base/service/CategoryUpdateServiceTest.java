package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.Category;
import com.kupi.cc.base.domain.entity.DealOfTheDaySearchIndex;
import com.kupi.cc.base.domain.entity.DealSearchIndex;
import com.kupi.cc.base.domain.entity.RequestADealSearchIndex;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests {@link CategoryUpdateService} class
 */
class CategoryUpdateServiceTest {

    private static final String DEAL_ID = "did-123456789";
    private static final String CATEGORY_ID = "cid-123456789";
    private static final String CATEGORY_NAME = "CATEGORY_NAME";

    @Mock
    private DealSearchService dealSearchService;
    @Mock
    private RequestADealSearchIndexService requestADealSearchIndexService;
    @Mock
    private DealsOfTheDaySearchIndexService dealsOfTheDaySearchIndexService;
    private CategoryUpdateService categoryUpdateService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        categoryUpdateService = new CategoryUpdateService(250, dealSearchService, dealsOfTheDaySearchIndexService,
                requestADealSearchIndexService);
    }

    @AfterEach
    void tearDown() {
    }

    /**
     * Update category data
     */
    @Test
    void Should_UpdateCategoryData_When_UpdatingCategoryDataIsSuccessful() {
        when(dealSearchService.getDealSearchIndexByCategoryId(any(Pageable.class), anyString()))
                .thenReturn(getSampleDealSearchIndexPage());
        doNothing().when(dealSearchService).saveListOfDealSearchIndexes(anyList());
        when(requestADealSearchIndexService.getRequestADDealSearchIndexByCategoryId(any(Pageable.class), anyString()))
                .thenReturn(getSampleRequestADealSearchIndexPage());
        doNothing().when(requestADealSearchIndexService).saveListOfRequestADealIndex(anyList());
        when(dealsOfTheDaySearchIndexService.getDealsOfTheDaySearchIndexByCategoryId(anyString()))
                .thenReturn(getSampleDealOfTheDaySearchIndexList());
        doNothing().when(dealsOfTheDaySearchIndexService).saveAll(anyList());

        categoryUpdateService.updateCategoryData(getSampleCategory(), CATEGORY_NAME);
        verify(dealSearchService).saveListOfDealSearchIndexes(anyList());
        verify(requestADealSearchIndexService).saveListOfRequestADealIndex(anyList());
        verify(dealsOfTheDaySearchIndexService).saveAll(anyList());
    }

    /**
     * This method returns a sample DealOfTheDaySearchIndex list
     *
     * @return DealOfTheDaySearchIndex List
     */
    private List<DealOfTheDaySearchIndex> getSampleDealOfTheDaySearchIndexList() {
        List<DealOfTheDaySearchIndex> list = new ArrayList<>();
        list.add(getSampleDealOfTheDaySearchIndex());
        return list;
    }

    /**
     * This method returns a sample DealOfTheDaySearchIndex object
     *
     * @return DealOfTheDaySearchIndex
     */
    private DealOfTheDaySearchIndex getSampleDealOfTheDaySearchIndex() {
        DealOfTheDaySearchIndex index = new DealOfTheDaySearchIndex();
        index.setId(DEAL_ID);
        index.setCategoryNames(CATEGORY_NAME);
        return index;
    }

    /**
     * This method returns a sample RequestADealSearchIndex page
     *
     * @return RequestADealSearchIndex Page
     */
    private Page<RequestADealSearchIndex> getSampleRequestADealSearchIndexPage() {
        return new PageImpl<>(getSampleRequestADealSearchIndexList());
    }

    /**
     * This method returns a sample RequestADealSearchIndex list
     *
     * @return RequestADealSearchIndex List
     */
    private List<RequestADealSearchIndex> getSampleRequestADealSearchIndexList() {
        List<RequestADealSearchIndex> list = new ArrayList<>();
        list.add(getSampleRequestADealSearchIndex());
        return list;
    }

    /**
     * This method returns a sample RequestADealSearchIndex object
     *
     * @return RequestADealSearchIndex
     */
    private RequestADealSearchIndex getSampleRequestADealSearchIndex() {
        RequestADealSearchIndex index = new RequestADealSearchIndex();
        index.setId(DEAL_ID);
        return index;
    }

    /**
     * This method returns a sample DealSearchIndex page
     *
     * @return DealSearchIndex Page
     */
    private Page<DealSearchIndex> getSampleDealSearchIndexPage() {
        return new PageImpl<>(getSampleDealSearchIndexList());
    }

    /**
     * This method returns a sample DealSearchIndex list
     *
     * @return DealSearchIndex List
     */
    private List<DealSearchIndex> getSampleDealSearchIndexList() {
        List<DealSearchIndex> dealSearchIndexList = new ArrayList<>();
        dealSearchIndexList.add(getSampleDealSearchIndex());
        return dealSearchIndexList;
    }

    /**
     * This method returns a sample DealSearchIndex object
     *
     * @return DealSearchIndex
     */
    private DealSearchIndex getSampleDealSearchIndex() {
        DealSearchIndex dealSearchIndex = new DealSearchIndex();
        dealSearchIndex.setId(DEAL_ID);
        dealSearchIndex.setCategoryNames(CATEGORY_NAME);
        return dealSearchIndex;
    }

    /**
     * This method returns a sample Category object
     *
     * @return Category
     */
    private Category getSampleCategory() {
        Category category = new Category();
        category.setId(CATEGORY_ID);
        category.setName(CATEGORY_NAME);
        return category;
    }
}