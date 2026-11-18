package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.Brand;
import com.kupi.cc.base.domain.entity.DealOfTheDaySearchIndex;
import com.kupi.cc.base.domain.entity.DealSearchIndex;
import com.kupi.cc.base.domain.entity.RequestADealSearchIndex;
import com.kupi.cc.base.enums.ApprovalStatus;
import com.kupi.cc.base.enums.UserType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests for {@link  BrandUpdateService} class
 */
class BrandUpdateServiceTest {

    private static final int PAGE = 0;
    private static final int SIZE = 1;
    private static final String UPDATED_AT = "updatedAt";
    private static final String BRAND_ID = "bid-02a2022f-98c5-4237-a9c7-c1faf989a4a5";
    private static final String BRAND_NAME = "DELL";

    @Mock
    private DealSearchService dealSearchService;
    @Mock
    private RequestADealSearchIndexService requestADealSearchIndexService;
    @Mock
    private DealsOfTheDaySearchIndexService dealsOfTheDaySearchIndexService;

    private BrandUpdateService brandUpdateService;

    @BeforeEach
    void setUp() {
        int size = 250;
        initMocks(this);
        brandUpdateService = new BrandUpdateService(size, dealSearchService, dealsOfTheDaySearchIndexService,
                requestADealSearchIndexService);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_UpdateBrandData() {
        Brand brand = getSampleBrand();
        Page<DealSearchIndex> dealSearchIndexPage = getSampleDealSearchIndexPage();
        Page<RequestADealSearchIndex> requestADealSearchIndexPage = getSampleRequestADealSearchIndexPage();
        List<DealOfTheDaySearchIndex> dealOfTheDaySearchIndexList = getSampleDealOfTheDaySearchIndexList();
        when(dealSearchService.getDealSearchIndexByBrandId(any(Pageable.class), anyString()))
                .thenReturn(dealSearchIndexPage);
        when(requestADealSearchIndexService.getRequestADDealSearchIndexesByBrandId(any(Pageable.class), anyString()))
                .thenReturn(requestADealSearchIndexPage);
        when(dealsOfTheDaySearchIndexService.getDealsOfTheDaySearchIndexByBrandId(anyString()))
                .thenReturn(dealOfTheDaySearchIndexList);
        brandUpdateService.updateBrandData(brand, BRAND_NAME);
        verify(dealsOfTheDaySearchIndexService).saveAll(dealOfTheDaySearchIndexList);
    }

    /**
     * This method creates sample brand
     *
     * @return Brand
     */
    private Brand getSampleBrand() {
        Brand brand = new Brand();
        brand.setId(BRAND_ID);
        brand.setName("Brand name");
        brand.setDescription("Brand description");
        brand.setImageUrl("https://objects-kupi-dev.s3.ap-southeast-1.amazonaws.com/fid-3338ebed-e3d3-4f86-8b3e-92887f27e51d.jpg");
        return brand;
    }

    /**
     * This method creates sample DealSearchIndex
     *
     * @return DealSearchIndex
     */
    private DealSearchIndex getSampleDealSearchIndex() {
        DealSearchIndex dealSearchIndex = new DealSearchIndex();
        dealSearchIndex.setId("did-02104ca3-41e4-4cb2-8a06-8ae0805ef6dc");
        dealSearchIndex.setApprovalStatus(ApprovalStatus.APPROVED);
        dealSearchIndex.setDeleted(false);
        dealSearchIndex.setTitle("Bank deal to be approved");
        dealSearchIndex.setActiveMerchant(true);
        dealSearchIndex.setBrandNames("Brand");
        dealSearchIndex.setDealSource(UserType.MERCHANT);
        dealSearchIndex.setCategoryNames("category");
        dealSearchIndex.setSearchImageUrls("[https://objects-kupi-dev.s3.ap-southeast-1.amazonaws.com/fid-a61e4565-dd03-4bb1-a123-90460ca3def5.png]");
        dealSearchIndex.setSubTitle("bank deal");
        dealSearchIndex.setTermsAndConditions("no conditions ");
        return dealSearchIndex;
    }

    /**
     * This method creates sample DealSearchIndex page
     *
     * @return dealSearchIndexPage
     */
    private Page<DealSearchIndex> getSampleDealSearchIndexPage() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        DealSearchIndex dealSearchIndex = getSampleDealSearchIndex();
        List<DealSearchIndex> dealSearchIndexList = new ArrayList<>();
        dealSearchIndexList.add(dealSearchIndex);
        return new PageImpl<>(dealSearchIndexList, pageable, 1);
    }

    /**
     * This method creates sample RequestADealSearchIndex
     *
     * @return RequestADealSearchIndex
     */
    private RequestADealSearchIndex getSampleRequestADealSearchIndex() {
        RequestADealSearchIndex requestADealSearchIndex = new RequestADealSearchIndex();
        requestADealSearchIndex.setId("rdid-1235-6854-7854-2569");
        requestADealSearchIndex.setBrandId(BRAND_ID);
        requestADealSearchIndex.setBrandName(BRAND_NAME);
        requestADealSearchIndex.setCategoryId("cid-f061574f-ce13-496f-bc21-01b9c7a40e24");
        requestADealSearchIndex.setCategoryName("New Year");
        requestADealSearchIndex.setMerchantId("uid-92b0983c-534e-401f-af15-5f8bcbcddebb");
        requestADealSearchIndex.setMerchantName("Sampath");
        requestADealSearchIndex.setUserId("uid-eb7af634-69d4-4d67-9e58-8e21704571a2");
        requestADealSearchIndex.setToUserType(UserType.BANK);
        requestADealSearchIndex.setNote("Note");
        requestADealSearchIndex.setCreatedAt(1644301756863L);
        requestADealSearchIndex.setUpdatedAt(1644301756863L);
        return requestADealSearchIndex;
    }

    /**
     * This method creates sample RequestADealSearchIndexPage
     *
     * @return RequestADealSearchIndexPage
     */
    private Page<RequestADealSearchIndex> getSampleRequestADealSearchIndexPage() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        RequestADealSearchIndex requestADealSearchIndex = getSampleRequestADealSearchIndex();
        List<RequestADealSearchIndex> requestADealSearchIndexList = new ArrayList<>();
        requestADealSearchIndexList.add(requestADealSearchIndex);
        return new PageImpl<>(requestADealSearchIndexList, pageable, 1);
    }

    /**
     * This method creates sample DealOfTheDaySearchIndex
     *
     * @return DealOfTheDaySearchIndex
     */
    private DealOfTheDaySearchIndex getSampleDealOfTheDaySearchIndex() {
        DealSearchIndex dealSearchIndex = getSampleDealSearchIndex();
        return new DealOfTheDaySearchIndex(dealSearchIndex);
    }

    /**
     * This method creates sample DealOfTheDaySearchIndex list
     *
     * @return DealOfTheDaySearchIndexList
     */
    private List<DealOfTheDaySearchIndex> getSampleDealOfTheDaySearchIndexList() {
        DealOfTheDaySearchIndex dealOfTheDaySearchIndex = getSampleDealOfTheDaySearchIndex();
        List<DealOfTheDaySearchIndex> dealOfTheDaySearchIndexList = new ArrayList<>();
        dealOfTheDaySearchIndexList.add(dealOfTheDaySearchIndex);
        return dealOfTheDaySearchIndexList;
    }
}