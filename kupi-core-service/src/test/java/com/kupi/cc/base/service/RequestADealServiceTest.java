package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.*;
import com.kupi.cc.base.domain.response.BusinessMerchantResponseDto;
import com.kupi.cc.base.enums.ApprovalStatus;
import com.kupi.cc.base.enums.CategoryType;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.InvalidRequestADealException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.repository.RequestADealRepository;
import com.kupi.cc.base.repository.RequestADealSearchIndexRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests for {@link RequestADealService} class
 */
class RequestADealServiceTest {

    private static final String OFFER_TYPE_ID = "otid=1256-45875-8547-7852";
    private static final String CATEGORY_ID = "cid-1235-1458-1785";
    private static final String OFFER_TYPE_NAME = "OFFER_TYPE";
    private static final String BRAND_ID = "did-02a2022f-98c5-4237-a9c7-c1faf989a4a5";
    private static final String MERCHANT_ID = "uid-123456789";
    private static final String USER_ID = "uid-bb801b6a-ae7c-4f81-8876-a722f644fa11";
    private static final String REQUEST_A_DEAL_ID = "rdid-08beec5e-e905-4917-851f-278efaebb55f";
    private static final String UPDATED_AT = "updatedAt";
    private static final String SEARCH_TERM = "SEARCH TERM";
    private static final int PAGE = 0;
    private static final int SIZE = 1;
    private static final long COMBINED_REQUEST_A_DEAL_COUNT = 1;

    @Mock
    private RequestADealRepository requestADealRepository;
    @Mock
    private RequestADealSearchIndexRepository requestADealSearchIndexRepository;
    @Mock
    private RequestADealSearchIndexService requestADealSearchIndexService;
    @Mock
    private AuthUserService authUserService;
    private RequestADealService requestADealService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        requestADealService = new RequestADealService(requestADealRepository, authUserService,
                requestADealSearchIndexRepository, requestADealSearchIndexService);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_ReturnRequestADeal_When_UserTypeIsMerchantForSaveRequestADeal() {
        RequestADeal requestADeal = getSampleRequestADeal();
        BusinessMerchantResponseDto merchant = getSampleBusinessMerchantResponseDto();
        when(requestADealRepository.save(requestADeal)).thenReturn(requestADeal);
        assertEquals(requestADeal, requestADealService.saveRequestADeal(requestADeal, merchant, UserType.MERCHANT));
    }

    @Test
    void Should_ReturnRequestADeal_When_UserTypeIsBankForSaveRequestADeal() {
        RequestADeal requestADeal = getSampleRequestADeal();
        BusinessMerchantResponseDto merchant = getSampleBusinessMerchantResponseDto();
        when(requestADealRepository.save(requestADeal)).thenReturn(requestADeal);
        assertEquals(requestADeal, requestADealService.saveRequestADeal(requestADeal, merchant, UserType.BANK));
    }

    @Test
    void Should_ThrowKupiCoreException_When_SaveRequestADealIntoDataBaseIsFailed() {
        RequestADeal requestADeal = getSampleRequestADeal();
        BusinessMerchantResponseDto merchant = getSampleBusinessMerchantResponseDto();
        when(requestADealRepository.save(requestADeal)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                requestADealService.saveRequestADeal(requestADeal, merchant, UserType.BANK));
        assertEquals("Saving request deal into database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnTrue_When_OfferTypeMapWithRequestADeal() {
        OfferType offerType = getSampleOfferType();
        when(requestADealRepository.existsByOfferType(offerType)).thenReturn(true);
        assertTrue(requestADealService.isOfferTypeHasRequestADeals(offerType));
    }

    @Test
    void Should_ThrowKupiCoreException_When_CheckOfferTypeMapWithRequestADealIsFailed() {
        OfferType offerType = getSampleOfferType();
        when(requestADealRepository.existsByOfferType(offerType)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                requestADealService.isOfferTypeHasRequestADeals(offerType));
        assertEquals("Reading request a deals by offer type, offer type id: " + OFFER_TYPE_ID
                + " from database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnRequestADealPage_When_UserTypeIsMerchant() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        Page<RequestADeal> requestADealPage = getSampleRequestADealPage();
        when(requestADealRepository.findAllByUserIdAndToUserType(pageable, USER_ID, UserType.MERCHANT))
                .thenReturn(requestADealPage);
        assertEquals(requestADealPage,
                requestADealService.getRequestDealsByUserIdAndToUserType(pageable, USER_ID, UserType.MERCHANT));
    }

    @Test
    void Should_ReturnRequestADealPage_When_UserTypeIsBank() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        Page<RequestADeal> requestADealPage = getSampleRequestADealPage();
        when(requestADealRepository.findAllByUserIdAndToUserType(pageable, USER_ID, UserType.BANK))
                .thenReturn(requestADealPage);
        assertEquals(requestADealPage,
                requestADealService.getRequestDealsByUserIdAndToUserType(pageable, USER_ID, UserType.BANK));
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetRequestDealsByUserIdAndToUserTypeIsFailed() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        when(requestADealRepository.findAllByUserIdAndToUserType(pageable, USER_ID, UserType.MERCHANT))
                .thenThrow(new DataAccessException("ERROR") {
                });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                requestADealService.getRequestDealsByUserIdAndToUserType(pageable, USER_ID, UserType.MERCHANT));
        assertEquals("Reading request a deal by user id from database was failed", kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnRequestADeal_When_ValidRequestADealIdIsProvidedForGetRequestADealById() {
        RequestADeal requestADeal = getSampleRequestADeal();
        when(requestADealRepository.findById(REQUEST_A_DEAL_ID)).thenReturn(Optional.of(requestADeal));
        assertEquals(requestADeal, requestADealService.getRequestADealById(REQUEST_A_DEAL_ID));
    }

    @Test
    void Should_ThrowInvalidRequestADealException_When_InvalidRequestADealIdIsProvidedForGetRequestADealById() {
        when(requestADealRepository.findById(REQUEST_A_DEAL_ID)).thenReturn(Optional.empty());
        InvalidRequestADealException invalidRequestADealException = assertThrows(InvalidRequestADealException.class, () ->
                requestADealService.getRequestADealById(REQUEST_A_DEAL_ID));
        assertEquals("Invalid request a deal Id: " + REQUEST_A_DEAL_ID, invalidRequestADealException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetRequestADealFromDataBaseIsFailedForGetRequestADealById() {
        when(requestADealRepository.findById(REQUEST_A_DEAL_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                requestADealService.getRequestADealById(REQUEST_A_DEAL_ID));
        assertEquals("Reading request a deal by id from database was failed", kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnCombinedDealRequestPage_When_ValidFieldsAreProvidedForGetAllCombinedDealRequests() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        Page<CombinedDealRequest> combinedDealRequestPage = getSampleCombinedDealRequestPage();
        when(requestADealSearchIndexService.getAllCombinedDealRequests(pageable, MERCHANT_ID, SEARCH_TERM, UserType.MERCHANT))
                .thenReturn(combinedDealRequestPage);
        assertEquals(combinedDealRequestPage, requestADealService.getAllCombinedDealRequests(pageable, MERCHANT_ID, SEARCH_TERM, UserType.MERCHANT));
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetCombinedDealRequestPageFromDataBaseIsFailed() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        when(requestADealSearchIndexService.getAllCombinedDealRequests(pageable, MERCHANT_ID, SEARCH_TERM, UserType.MERCHANT))
                .thenThrow(new DataAccessException("ERROR") {
                });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                requestADealService.getAllCombinedDealRequests(pageable, MERCHANT_ID, SEARCH_TERM, UserType.MERCHANT));
        assertEquals("fetching all combinedDealRequest from database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnCombinedRequestADealCount_When_GivenBrandIsNullForGetCountForCombinedRequestADeal() {
        Category category = getSampleCategory();
        when(requestADealRepository.countRequestADealsByMerchantIdAndCategoryAndBrandAndToUserType(MERCHANT_ID,
                category, null, UserType.MERCHANT)).thenReturn(COMBINED_REQUEST_A_DEAL_COUNT);
        assertEquals(COMBINED_REQUEST_A_DEAL_COUNT, requestADealService.getCountForCombinedRequestADeal(MERCHANT_ID,
                category, null, UserType.MERCHANT));
    }

    @Test
    void Should_ReturnCombinedRequestADealCount_When_GivenBrandIsNotNullForGetCountForCombinedRequestADeal() {
        Category category = getSampleCategory();
        Brand brand = getSampleBrand();
        when(requestADealRepository.countRequestADealsByMerchantIdAndCategoryAndBrandAndToUserType(MERCHANT_ID,
                category, brand, UserType.MERCHANT)).thenReturn(COMBINED_REQUEST_A_DEAL_COUNT);
        assertEquals(COMBINED_REQUEST_A_DEAL_COUNT, requestADealService.getCountForCombinedRequestADeal(MERCHANT_ID,
                category, brand, UserType.MERCHANT));
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetCombinedRequestADealCountFromDataBaseIsFailed() {
        Category category = getSampleCategory();
        Brand brand = getSampleBrand();
        when(requestADealRepository.countRequestADealsByMerchantIdAndCategoryAndBrandAndToUserType(MERCHANT_ID,
                category, brand, UserType.MERCHANT)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                requestADealService.getCountForCombinedRequestADeal(MERCHANT_ID, category, brand, UserType.MERCHANT));
        assertEquals("returning count for the combined request a deal from database was failed.",
                kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnRequestADealPage_When_BrandIdIsNullForGetAllRequestADealDetail() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        Page<RequestADeal> requestADealPage = getSampleRequestADealPage();
        when(requestADealRepository.getAllByMerchantIdCategoryIdAndUserType(pageable, MERCHANT_ID, CATEGORY_ID,
                UserType.MERCHANT)).thenReturn(requestADealPage);
        assertEquals(requestADealPage, requestADealService.getAllRequestADealDetail(pageable, MERCHANT_ID, CATEGORY_ID,
                null, UserType.MERCHANT));
    }

    @Test
    void Should_ReturnRequestADealPage_When_BrandIdIsNotNullForGetAllRequestADealDetail() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        Page<RequestADeal> requestADealPage = getSampleRequestADealPage();
        when(requestADealRepository.getAllByMerchantIdAndBrandIdCategoryIdAndUserType(pageable, MERCHANT_ID, BRAND_ID,
                CATEGORY_ID, UserType.MERCHANT)).thenReturn(requestADealPage);
        assertEquals(requestADealPage, requestADealService.getAllRequestADealDetail(pageable, MERCHANT_ID, CATEGORY_ID,
                BRAND_ID, UserType.MERCHANT));
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetRequestADealPageFromDataBaseIsFailed() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(UPDATED_AT).descending());
        when(requestADealRepository.getAllByMerchantIdAndBrandIdCategoryIdAndUserType(pageable, MERCHANT_ID, BRAND_ID,
                CATEGORY_ID, UserType.MERCHANT)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                requestADealService.getAllRequestADealDetail(pageable, MERCHANT_ID, CATEGORY_ID, BRAND_ID, UserType.MERCHANT));
        assertEquals("fetching combined request a deal note list from database was failed.", kupiCoreException.getMessage());
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

    /**
     * This method creates BusinessMerchantResponseDto
     *
     * @return BusinessMerchantResponseDto
     */
    private BusinessMerchantResponseDto getSampleBusinessMerchantResponseDto() {
        BusinessMerchantResponseDto businessMerchantResponseDto = new BusinessMerchantResponseDto();
        businessMerchantResponseDto.setBusinessId("uid-001");
        businessMerchantResponseDto.setBusinessName("Roy");
        businessMerchantResponseDto.setImageUrl("goo.le/image");
        businessMerchantResponseDto.setOwnerName("Roy Owner");
        businessMerchantResponseDto.setAddress("Colombo");
        businessMerchantResponseDto.setWebSite("goo.le/image2");
        businessMerchantResponseDto.setApprovalStatus(ApprovalStatus.APPROVED);
        businessMerchantResponseDto.setActive(true);
        return businessMerchantResponseDto;
    }

    /**
     * This method creates sample RequestADeal page
     *
     * @return RequestADealPage
     */
    private Page<RequestADeal> getSampleRequestADealPage() {
        RequestADeal requestADeal = getSampleRequestADeal();
        List<RequestADeal> requestADealList = new ArrayList<>();
        requestADealList.add(requestADeal);
        return new PageImpl<>(requestADealList);
    }

    /**
     * This method creates sample CombinedDealRequest
     *
     * @return CombinedDealRequest
     */
    private CombinedDealRequest getSampleCombinedDealRequest() {
        CombinedDealRequest combinedDealRequest = new CombinedDealRequest();
        combinedDealRequest.setBrandId(BRAND_ID);
        combinedDealRequest.setCategoryId(CATEGORY_ID);
        combinedDealRequest.setMerchantId(MERCHANT_ID);
        combinedDealRequest.setCount(1L);
        combinedDealRequest.setBrandName("DELL");
        combinedDealRequest.setMerchantName("Merchant");
        return combinedDealRequest;
    }

    /**
     * This method creates sample CombinedDealRequest page
     *
     * @return CombinedDealRequestPage
     */
    private Page<CombinedDealRequest> getSampleCombinedDealRequestPage() {
        CombinedDealRequest combinedDealRequest = getSampleCombinedDealRequest();
        List<CombinedDealRequest> combinedDealRequestList = new ArrayList<>();
        combinedDealRequestList.add(combinedDealRequest);
        return new PageImpl<>(combinedDealRequestList);
    }
}