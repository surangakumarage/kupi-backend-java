package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.CombinedCreditCardRequest;
import com.kupi.cc.base.domain.entity.CreditCardRequest;
import com.kupi.cc.base.domain.request.CreditCardRequestCreateRequestDto;
import com.kupi.cc.base.domain.request.CreditCardRequestUpdateRequestDto;
import com.kupi.cc.base.domain.request.MobileNoRequestDto;
import com.kupi.cc.base.exception.InvalidCreditCardRequestException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.repository.CreditCardRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests {@link CreditCardService} class
 */
class CreditCardServiceTest {

    private static final String CC_REQUEST_ID = "ccrid-123456789";
    private static final String USER_ID = "uid-123456789";
    private static final String BANK_ID = "uid-123456789";
    private static final String BANK_NAME = "Commercial Bank";
    private static final String FULL_NAME = "Karim Benzema";
    private static final String COUNTRY_CODE = "+94";
    private static final String LOCAL_NUMBER = "771234567";
    private static final String EMAIL = "abc@yahoomail.com";
    private static final String NIC = "123456789v";
    private static final String CITY = "Paris";
    private static final String FAILED = "failed";
    private static final String REQUEST_ID = "123456789";
    private static final String SEARCH_TERM = "Softlogic";
    private static final String ALL = "ALL";
    private static final String DEFAULT_SORT = "createdAt";
    private static final int PAGE = 0;
    private static final int SIZE = 1;

    @Mock
    private CreditCardRepository creditCardRepository;
    private CreditCardService creditCardService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        creditCardService = new CreditCardService(creditCardRepository);
    }

    @AfterEach
    void tearDown() {
    }

    /**
     * Save credit card request
     */
    @Test
    void Should_ReturnCreditCardRequest_When_SaveCreditCardRequestIsSuccessful() {
        CreditCardRequest creditCardRequest = new CreditCardRequest(getSampleCreditCardRequestCreateRequestDto());
        when(creditCardRepository.save(any(CreditCardRequest.class))).thenReturn(creditCardRequest);
        assertEquals(USER_ID, creditCardService.saveCreditCardRequest(getSampleCreditCardRequestCreateRequestDto())
                .getUserId());
    }

    @Test
    void Should_ThrowKupiCoreException_When_SaveCreditCardRequestIsFailed() {
        CreditCardRequestCreateRequestDto creditCardRequestCreateRequestDto = getSampleCreditCardRequestCreateRequestDto();
        when(creditCardRepository.save(any(CreditCardRequest.class))).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> creditCardService
                .saveCreditCardRequest(creditCardRequestCreateRequestDto));
        assertEquals("Saving credit card request to database was failed", exception.getMessage());
    }

    /**
     * Get credit card request by id
     */
    @Test
    void Should_ReturnCreditCardRequest_When_GettingCreditCardRequestByIdIsSuccessful() {
        when(creditCardRepository.findById(anyString())).thenReturn(Optional.of(getSampleCreditCardRequest()));
        assertEquals(USER_ID, creditCardService.getCreditCardRequestById(REQUEST_ID).getId());
    }

    @Test
    void Should_ThrowInvalidCreditCardRequestException_When_GettingCreditCardRequestByIdForInvalidRequestId() {
        when(creditCardRepository.findById(anyString())).thenReturn(Optional.empty());
        InvalidCreditCardRequestException exception = assertThrows(InvalidCreditCardRequestException.class, () ->
                creditCardService.getCreditCardRequestById(REQUEST_ID));
        assertEquals("Invalid Credit card request Id: " + REQUEST_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingCreditCardRequestByIdIsFailed() {
        when(creditCardRepository.findById(anyString())).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> creditCardService
                .getCreditCardRequestById(REQUEST_ID));
        assertEquals("Reading credit card request from the database was failed.", exception.getMessage());
    }

    /**
     * Update credit card request
     */
    @Test
    void Should_ReturnCreditCardRequest_When_UpdatingCreditCardRequestIsSuccessful() {
        CreditCardRequest creditCardRequest = new CreditCardRequest(getSampleCreditCardRequestUpdateRequestDto());
        when(creditCardRepository.save(any(CreditCardRequest.class))).thenReturn(creditCardRequest);
        assertEquals(CC_REQUEST_ID, creditCardService.updateCreditCardRequest(getSampleCreditCardRequestUpdateRequestDto()).getId());
    }

    @Test
    void Should_ThrowKupiCoreException_When_UpdatingCreditCardRequestIsFailed() {
        CreditCardRequestUpdateRequestDto creditCardRequestUpdateRequestDto = getSampleCreditCardRequestUpdateRequestDto();
        when(creditCardRepository.save(any(CreditCardRequest.class))).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> creditCardService
                .updateCreditCardRequest(creditCardRequestUpdateRequestDto));
        assertEquals("Updating credit card request to database was failed", exception.getMessage());
    }

    /**
     * Search credit card requests
     */
    @Test
    void Should_ReturnCreditCardRequestPage_When_SearchCreditCardRequestsForAllBankIdAllSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        when(creditCardRepository.findAll(any(Pageable.class))).thenReturn(getSampleCreditCardRequestPage());

        assertEquals(USER_ID, creditCardService.searchCreditCardRequests(pageable, ALL, ALL).getContent().get(0).getId());
    }

    @Test
    void Should_ReturnCreditCardRequestPage_When_SearchCreditCardRequestsForAllBankIdSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        when(creditCardRepository.findAllBySearchTerm(any(Pageable.class), anyString()))
                .thenReturn(getSampleCreditCardRequestPage());

        assertEquals(USER_ID, creditCardService.searchCreditCardRequests(pageable, ALL, SEARCH_TERM).getContent()
                .get(0).getId());
    }

    @Test
    void Should_ReturnCreditCardRequestPage_When_SearchCreditCardRequestsForBankIdAllSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        when(creditCardRepository.findAllByBankId(any(Pageable.class), anyString()))
                .thenReturn(getSampleCreditCardRequestPage());

        assertEquals(USER_ID, creditCardService.searchCreditCardRequests(pageable, BANK_ID, ALL).getContent()
                .get(0).getId());
    }

    @Test
    void Should_ReturnCreditCardRequestPage_When_SearchCreditCardRequestsForBankIdSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        when(creditCardRepository.findAllByBankIdWithSearch(any(Pageable.class), anyString(), anyString()))
                .thenReturn(getSampleCreditCardRequestPage());

        assertEquals(USER_ID, creditCardService.searchCreditCardRequests(pageable, BANK_ID, SEARCH_TERM).getContent()
                .get(0).getId());
    }

    @Test
    void Should_ThrowKupiCoreException_When_SearchCreditCardRequestsIsFailed() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        when(creditCardRepository.findAll(any(Pageable.class))).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> creditCardService
                .searchCreditCardRequests(pageable, ALL, ALL));
        assertEquals("Reading credit card request from the database was failed.", exception.getMessage());
    }

    /**
     * Delete credit card request
     */
    @Test
    void Should_DeleteCreditCardRequest() {
        when(creditCardRepository.findById(anyString())).thenReturn(Optional.of(getSampleCreditCardRequest()));
        doNothing().when(creditCardRepository).delete(any(CreditCardRequest.class));
        creditCardService.deleteCreditCardRequest(REQUEST_ID);
        verify(creditCardRepository).delete(any(CreditCardRequest.class));
    }

    @Test
    void Should_ThrowKupiCoreException_When_DeleteCreditCardRequestIsFailed() {
        when(creditCardRepository.findById(anyString())).thenReturn(Optional.of(getSampleCreditCardRequest()));
        doThrow(new DataAccessException(FAILED) {
        }).when(creditCardRepository).delete(any(CreditCardRequest.class));
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> creditCardService
                .deleteCreditCardRequest(REQUEST_ID));
        assertEquals("Deleting credit card request from the database was failed.", exception.getMessage());
    }

    /**
     * Get credit card request by userId
     */
    @Test
    void Should_ReturnCreditCardRequestPage_When_GetCreditCardRequestByUserIdIsSuccessful() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        when(creditCardRepository.findAllByUserId(any(Pageable.class), anyString()))
                .thenReturn(getSampleCreditCardRequestPage());
        assertEquals(USER_ID, creditCardService.getCreditCardRequestByUserId(pageable, USER_ID).getContent().get(0)
                .getId());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GetCreditCardRequestByUserIdIsFailed() {
        Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(DEFAULT_SORT).descending());
        when(creditCardRepository.findAllByUserId(any(Pageable.class), anyString()))
                .thenThrow(new DataAccessException(FAILED) {
                });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> creditCardService
                .getCreditCardRequestByUserId(pageable, USER_ID));
        assertEquals("Reading credit card request from the database was failed.", exception.getMessage());
    }

    /**
     * Get group credit card request
     */
    @Test
    void Should_ReturnCombinedCreditCardRequestPage_When_getGroupCreditCardRequestForAllBankIdSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE);
        when(creditCardRepository.getAllGroupCreditCardRequestBySearchTerm(any(Pageable.class), anyString()))
                .thenReturn(getSampleCombinedCreditCardRequestPage());
        assertEquals(BANK_ID, creditCardService.getGroupCreditCardRequest(pageable, ALL, SEARCH_TERM).getContent()
                .get(0).getBankId());
    }

    @Test
    void Should_ReturnCombinedCreditCardRequestPage_When_getGroupCreditCardRequestForAllBankIdAllSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE);
        when(creditCardRepository.getAllGroupCreditCardRequests(any(Pageable.class)))
                .thenReturn(getSampleCombinedCreditCardRequestPage());
        assertEquals(BANK_ID, creditCardService.getGroupCreditCardRequest(pageable, ALL, ALL).getContent()
                .get(0).getBankId());
    }

    @Test
    void Should_ReturnCombinedCreditCardRequestPage_When_getGroupCreditCardRequestForBankIdAllSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE);
        when(creditCardRepository.getAllGroupCreditCardRequestByMerchantId(any(Pageable.class), anyString()))
                .thenReturn(getSampleCombinedCreditCardRequestPage());
        assertEquals(BANK_ID, creditCardService.getGroupCreditCardRequest(pageable, BANK_ID, ALL).getContent()
                .get(0).getBankId());
    }

    @Test
    void Should_ReturnCombinedCreditCardRequestPage_When_getGroupCreditCardRequestForBankIdSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE);
        when(creditCardRepository.getAllGroupCreditCardRequestByMerchantIdAndSearchTerm(any(Pageable.class),
                anyString(), anyString())).thenReturn(getSampleCombinedCreditCardRequestPage());
        assertEquals(BANK_ID, creditCardService.getGroupCreditCardRequest(pageable, BANK_ID, SEARCH_TERM).getContent()
                .get(0).getBankId());
    }

    @Test
    void Should_ThrowKupiCoreException_When_getGroupCreditCardRequestIsFailed() {
        Pageable pageable = PageRequest.of(PAGE, SIZE);
        when(creditCardRepository.getAllGroupCreditCardRequestByMerchantIdAndSearchTerm(any(Pageable.class),
                anyString(), anyString())).thenThrow(new DataAccessException(FAILED) {
        });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> creditCardService
                .getGroupCreditCardRequest(pageable, BANK_ID, SEARCH_TERM));
        assertEquals("fetching combinedCreditCardRequests from the database was failed.", exception.getMessage());
    }

    private CreditCardRequestCreateRequestDto getSampleCreditCardRequestCreateRequestDto() {
        CreditCardRequestCreateRequestDto creditCardRequestCreateRequestDto = new CreditCardRequestCreateRequestDto();
        creditCardRequestCreateRequestDto.setUserId(USER_ID);
        creditCardRequestCreateRequestDto.setBankId(BANK_ID);
        creditCardRequestCreateRequestDto.setBankName(BANK_NAME);
        creditCardRequestCreateRequestDto.setFullName(FULL_NAME);
        creditCardRequestCreateRequestDto.setMobileNumber(new MobileNoRequestDto(COUNTRY_CODE, LOCAL_NUMBER));
        creditCardRequestCreateRequestDto.setEmail(EMAIL);
        creditCardRequestCreateRequestDto.setNic(NIC);
        creditCardRequestCreateRequestDto.setCity(CITY);
        return creditCardRequestCreateRequestDto;
    }

    private CreditCardRequestUpdateRequestDto getSampleCreditCardRequestUpdateRequestDto() {
        CreditCardRequestUpdateRequestDto creditCardRequestUpdateRequestDto = new CreditCardRequestUpdateRequestDto();
        creditCardRequestUpdateRequestDto.setId(CC_REQUEST_ID);
        creditCardRequestUpdateRequestDto.setUserId(USER_ID);
        creditCardRequestUpdateRequestDto.setBankId(BANK_ID);
        creditCardRequestUpdateRequestDto.setBankName(BANK_NAME);
        creditCardRequestUpdateRequestDto.setFullName(FULL_NAME);
        creditCardRequestUpdateRequestDto.setMobileNumber(new MobileNoRequestDto(COUNTRY_CODE, LOCAL_NUMBER));
        creditCardRequestUpdateRequestDto.setEmail(EMAIL);
        creditCardRequestUpdateRequestDto.setNic(NIC);
        creditCardRequestUpdateRequestDto.setCity(CITY);
        return creditCardRequestUpdateRequestDto;
    }

    private CreditCardRequest getSampleCreditCardRequest() {
        CreditCardRequest creditCardRequest = new CreditCardRequest();
        creditCardRequest.setId(USER_ID);
        return creditCardRequest;
    }

    private Page<CreditCardRequest> getSampleCreditCardRequestPage() {
        List<CreditCardRequest> creditCardRequestList = new ArrayList<>();
        creditCardRequestList.add(getSampleCreditCardRequest());
        return new PageImpl<>(creditCardRequestList);
    }

    private Page<CombinedCreditCardRequest> getSampleCombinedCreditCardRequestPage() {
        List<CombinedCreditCardRequest> combinedCreditCardRequestList = new ArrayList<>();
        combinedCreditCardRequestList.add(getSampleCombinedCreditCardRequest());
        return new PageImpl<>(combinedCreditCardRequestList);
    }

    private CombinedCreditCardRequest getSampleCombinedCreditCardRequest() {
        CombinedCreditCardRequest combinedCreditCardRequest = new CombinedCreditCardRequest();
        combinedCreditCardRequest.setBankId(BANK_ID);
        return combinedCreditCardRequest;
    }
}