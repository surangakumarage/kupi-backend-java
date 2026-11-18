package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.*;
import com.kupi.cc.base.enums.CategoryType;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.repository.RequestADealSearchIndexRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests for {@link RequestADealSearchIndexService} class
 */
class RequestADealSearchIndexServiceTest {

    private static final String OFFER_TYPE_ID = "otid=1256-45875-8547-7852";
    private static final String CATEGORY_ID = "cid-1235-1458-1785";
    private static final String OFFER_TYPE_NAME = "OFFER_TYPE";
    private static final String BRAND_ID = "did-02a2022f-98c5-4237-a9c7-c1faf989a4a5";
    private static final String MERCHANT_ID = "uid-123456789";
    private static final String USER_ID = "uid-bb801b6a-ae7c-4f81-8876-a722f644fa11";
    private static final String MERCHANT_NAME = "MERCHANT NAME";

    @Mock
    private RequestADealSearchIndexRepository requestADealSearchIndexRepository;
    private RequestADealSearchIndexService requestADealSearchIndexService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        requestADealSearchIndexService = new RequestADealSearchIndexService(requestADealSearchIndexRepository);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_SaveRequestADealSearchIndex() {
        RequestADeal requestADeal = getSampleRequestADeal();
        requestADealSearchIndexService.save(requestADeal, MERCHANT_NAME);
        verify(requestADealSearchIndexRepository).save(any(RequestADealSearchIndex.class));
    }

    @Test
    void Should_ThrowKupiCoreException_When_SaveRequestADealSearchIndexIntoDataBaseIsFailed() {
        RequestADeal requestADeal = getSampleRequestADeal();
        when(requestADealSearchIndexRepository.save(any(RequestADealSearchIndex.class))).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                requestADealSearchIndexService.save(requestADeal, MERCHANT_NAME));
        assertEquals("Saving requestADealSearchIndex, requestADealId: " + requestADeal.getId() + " into database was failed.",
                kupiCoreException.getMessage());
    }

    /**
     * This method creates sample RequestADeal
     *
     * @return RequestADeal
     */
    private RequestADeal getSampleRequestADeal() {
        OfferType offerType = getSampleOfferType();
        Brand brand = getSampleBrand();
        Category category = getSampleCategory();
        RequestADeal requestADeal = new RequestADeal();
        requestADeal.setNote("Note");
        requestADeal.setToUserType(UserType.MERCHANT);
        requestADeal.setBrand(brand);
        requestADeal.setOfferType(offerType);
        requestADeal.setCategory(category);
        requestADeal.setCreatedAt(System.currentTimeMillis());
        requestADeal.setUpdatedAt(System.currentTimeMillis());
        requestADeal.setUserId(USER_ID);
        requestADeal.setMerchantId(MERCHANT_ID);
        return requestADeal;
    }

    /**
     * This method creates sample OfferType
     *
     * @return OfferType
     */
    private OfferType getSampleOfferType() {
        OfferType offerType = new OfferType();
        offerType.setId(OFFER_TYPE_ID);
        offerType.setName(OFFER_TYPE_NAME);
        offerType.setUpdatedAt(System.currentTimeMillis());
        offerType.setCreatedAt(System.currentTimeMillis());
        offerType.setDeleted(false);
        return offerType;
    }

    /**
     * This method creates sample category
     *
     * @return category
     */
    private Category getSampleCategory() {
        Set<Category> relationalCategories = new HashSet<>();

        Category category = new Category();
        category.setId(CATEGORY_ID);
        category.setName("New Year");
        category.setDescription("snacks and sweets");
        category.setImageUrl("https://picsum.photos/200/300");
        category.setCategoryType(CategoryType.SEASONAL);
        category.setStartDate(1654021800000L);
        category.setExpiryDate(1670697000000L);
        category.setRelatedCategories(relationalCategories);
        category.setPopular(true);
        return category;
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
}