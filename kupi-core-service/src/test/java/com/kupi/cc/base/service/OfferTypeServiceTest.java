package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.OfferType;
import com.kupi.cc.base.exception.ExistingNameException;
import com.kupi.cc.base.exception.InvalidOfferTypeException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.exception.UnsupportedDeleteAction;
import com.kupi.cc.base.repository.OfferTypeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests for {@link OfferTypeService} class
 */
class OfferTypeServiceTest {

    private static final String OFFER_TYPE_ID = "otid=1256-45875-8547-7852";
    private static final String OFFER_TYPE_NAME = "OFFER_TYPE";

    @Mock
    private OfferTypeRepository offerTypeRepository;
    @Mock
    private RequestADealService requestADealService;
    private OfferTypeService offerTypeService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        offerTypeService = new OfferTypeService(offerTypeRepository, requestADealService);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Should_SaveOfferType_When_GivenOfferTypeNameNotExists() {
        OfferType offerType = getSampleOfferType();
        when(offerTypeRepository.existsByName(OFFER_TYPE_NAME)).thenReturn(false);
        offerTypeService.saveOfferType(offerType);
        verify(offerTypeRepository).save(offerType);
    }

    @Test
    void Should_ThrowExistingNameException_WHen_CheckExistOfferTypeNameInDataBaseIsFailed() {
        OfferType offerType = getSampleOfferType();
        when(offerTypeRepository.existsByName(OFFER_TYPE_NAME)).thenReturn(true);
        ExistingNameException existingNameException = assertThrows(ExistingNameException.class, () ->
                offerTypeService.saveOfferType(offerType));
        assertEquals("Offer type name: " + OFFER_TYPE_NAME + " exists in the database",
                existingNameException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_WHen_CheckExistOfferTypeNameInDataBaseIsFailed() {
        OfferType offerType = getSampleOfferType();
        when(offerTypeRepository.existsByName(OFFER_TYPE_NAME)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                offerTypeService.saveOfferType(offerType));
        assertEquals("Checking offer type name in database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_WHen_SavingOfferTypeIntoDataBaseIsFailed() {
        OfferType offerType = getSampleOfferType();
        when(offerTypeRepository.existsByName(OFFER_TYPE_NAME)).thenReturn(false);
        when(offerTypeRepository.save(offerType)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                offerTypeService.saveOfferType(offerType));
        assertEquals("Saving offer type into database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnOfferType_When_ValidOfferTypeIdIsProvidedForGetOfferTypeById() {
        OfferType offerType = getSampleOfferType();
        when(offerTypeRepository.findById(OFFER_TYPE_ID)).thenReturn(Optional.of(offerType));
        assertEquals(offerType, offerTypeService.getOfferTypeById(OFFER_TYPE_ID));
    }

    @Test
    void Should_ThrowInvalidOfferTypeException_When_InValidOfferTypeIdIsProvidedForGetOfferTypeById() {
        when(offerTypeRepository.findById(OFFER_TYPE_ID)).thenReturn(Optional.empty());
        InvalidOfferTypeException invalidOfferTypeException = assertThrows(InvalidOfferTypeException.class, () ->
                offerTypeService.getOfferTypeById(OFFER_TYPE_ID));
        assertEquals("Invalid Offer Type Id: " + OFFER_TYPE_ID, invalidOfferTypeException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetOfferTypeFromDataBaseIsFailed() {
        when(offerTypeRepository.findById(OFFER_TYPE_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                offerTypeService.getOfferTypeById(OFFER_TYPE_ID));
        assertEquals("Reading Offer Type from database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_UpdateOfferType_When_GivenNameNotExistForUpdateOfferType() {
        OfferType offerType = getSampleOfferType();
        when(offerTypeRepository.existsByNameAndIdNot(OFFER_TYPE_NAME, OFFER_TYPE_ID)).thenReturn(false);
        offerTypeService.updateOfferType(offerType, OFFER_TYPE_NAME);
        verify(offerTypeRepository).save(offerType);
    }

    @Test
    void Should_ThrowExistingNameException_When_GivenNameAlreadyExistsForUpdateOfferType() {
        OfferType offerType = getSampleOfferType();
        when(offerTypeRepository.existsByNameAndIdNot(OFFER_TYPE_NAME, OFFER_TYPE_ID)).thenReturn(true);
        ExistingNameException existingNameException = assertThrows(ExistingNameException.class, () ->
                offerTypeService.updateOfferType(offerType, OFFER_TYPE_NAME));
        assertEquals("Offer type name: " + OFFER_TYPE_NAME + " exists in the database",
                existingNameException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_CheckExistingOfferTypeNameIsFailed() {
        OfferType offerType = getSampleOfferType();
        when(offerTypeRepository.existsByNameAndIdNot(OFFER_TYPE_NAME, OFFER_TYPE_ID))
                .thenThrow(new DataAccessException("ERROR") {
                });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                offerTypeService.updateOfferType(offerType, OFFER_TYPE_NAME));
        assertEquals("Checking offer type name in database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_UpdateOfferTypeToDatabaseIsFailed() {
        OfferType offerType = getSampleOfferType();
        when(offerTypeRepository.existsByNameAndIdNot(OFFER_TYPE_NAME, OFFER_TYPE_ID)).thenReturn(false);
        when(offerTypeRepository.save(offerType)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                offerTypeService.updateOfferType(offerType, OFFER_TYPE_NAME));
        assertEquals("updating offer type into database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_ReturnOfferTypeList() {
        List<OfferType> offerTypeList = getSampleOfferTypeList();
        when(offerTypeRepository.findAllByOrderByUpdatedAtDesc()).thenReturn(offerTypeList);
        assertEquals(offerTypeList, offerTypeService.getAllOfferTypes());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetAllOffeTypeListFromDataBaseIsFailed() {
        when(offerTypeRepository.findAllByOrderByUpdatedAtDesc()).thenThrow(new DataAccessException("ERROR") {
        });
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                offerTypeService.getAllOfferTypes());
        assertEquals("Getting offer type list from database was failed.", kupiCoreException.getMessage());
    }

    @Test
    void Should_DeleteOfferType_When_OfferTypeNotMapWithRequestADeal() {
        OfferType offerType = getSampleOfferType();
        when(offerTypeRepository.findById(OFFER_TYPE_ID)).thenReturn(Optional.of(offerType));
        when(requestADealService.isOfferTypeHasRequestADeals(offerType)).thenReturn(false);
        offerTypeService.deleteOfferType(OFFER_TYPE_ID);
        verify(offerTypeRepository).delete(offerType);
    }

    @Test
    void Should_ThrowInvalidOfferTypeException_When_InvalidOfferTypeIdIsProvided() {
        when(offerTypeRepository.findById(OFFER_TYPE_ID)).thenReturn(Optional.empty());
        InvalidOfferTypeException invalidOfferTypeException = assertThrows(InvalidOfferTypeException.class, () ->
                offerTypeService.deleteOfferType(OFFER_TYPE_ID));
        assertEquals("Invalid Offer Type Id: " + OFFER_TYPE_ID, invalidOfferTypeException.getMessage());
    }

    @Test
    void Should_ThrowUnsupportedDeleteAction_When_OfferTypeIsMapWithRrquestADeal() {
        OfferType offerType = getSampleOfferType();
        when(offerTypeRepository.findById(OFFER_TYPE_ID)).thenReturn(Optional.of(offerType));
        when(requestADealService.isOfferTypeHasRequestADeals(offerType)).thenReturn(true);
        UnsupportedDeleteAction unsupportedDeleteAction = assertThrows(UnsupportedDeleteAction.class, () ->
                offerTypeService.deleteOfferType(OFFER_TYPE_ID));
        assertEquals("Offer Type id: " + OFFER_TYPE_ID + " has request a deals", unsupportedDeleteAction.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_DeleteOfferTypeIsFailed() {
        OfferType offerType = getSampleOfferType();
        when(offerTypeRepository.findById(OFFER_TYPE_ID)).thenReturn(Optional.of(offerType));
        when(requestADealService.isOfferTypeHasRequestADeals(offerType)).thenReturn(false);
        doThrow(new DataAccessException("ERROR") {
        }).when(offerTypeRepository).delete(offerType);
        KupiCoreException kupiCoreException = assertThrows(KupiCoreException.class, () ->
                offerTypeService.deleteOfferType(OFFER_TYPE_ID));
        assertEquals("Deleting Offer Type by offerTypeId: " + OFFER_TYPE_ID + " from database was failed.",
                kupiCoreException.getMessage());
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
     * This method creates sample OfferType list
     *
     * @return offerTypeList
     */
    private List<OfferType> getSampleOfferTypeList() {
        OfferType offerType = getSampleOfferType();
        List<OfferType> offerTypeList = new ArrayList<>();
        offerTypeList.add(offerType);
        return offerTypeList;
    }
}