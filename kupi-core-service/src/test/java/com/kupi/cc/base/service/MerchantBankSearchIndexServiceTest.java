package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.BankMerchantSearchIndex;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.repository.BankMerchantSearchIndexRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This class tests {@link MerchantBankSearchIndexService} class
 */
class MerchantBankSearchIndexServiceTest {

    private static final String BANK_MERCHANT_ID = "bms-123456789";
    private static final String MERCHANT_ID = "uid-123456789";
    private static final String BANK_ID = "uid-123456789";
    private static final String SEARCH_TERM = "Softlogic";
    private static final String ALL = "ALL";
    private static final String FAILED = "FAILED";
    private static final int PAGE = 0;
    private static final int SIZE = 1;

    @Mock
    private BankMerchantSearchIndexRepository bankMerchantSearchIndexRepository;
    @Mock
    private AuthUserService authUserService;
    private MerchantBankSearchIndexService merchantBankSearchIndexService;

    @BeforeEach
    void setUp() {
        initMocks(this);
        merchantBankSearchIndexService = new MerchantBankSearchIndexService
                (bankMerchantSearchIndexRepository, authUserService);
    }

    @AfterEach
    void tearDown() {
    }

    /**
     * Get bank merchant search index by merchantId and searchTerm
     */
    @Test
    void Should_ReturnBankMerchantSearchIndexPage_When_GettingMerchantSearchIndexByMerchantIdAndSearchTermForActiveBankAndAllSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE);

        when(bankMerchantSearchIndexRepository.findAllByMerchantIdAndIsActiveBankTrue(any(Pageable.class), anyString()))
                .thenReturn(getSampleBankMerchantSearchIndexPage());

        assertEquals(BANK_MERCHANT_ID, merchantBankSearchIndexService
                .getBankMerchantSearchIndexByMerchantIdAndSearchTerm(pageable, MERCHANT_ID, ALL, true)
                .getContent().get(0).getId());
    }

    @Test
    void Should_ReturnBankMerchantSearchIndexPage_When_GettingMerchantSearchIndexByMerchantIdAndSearchTermForActiveBankAndSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE);

        when(bankMerchantSearchIndexRepository.findAllByMerchantIdAndBankNameContainingAndIsActiveBankTrue
                (any(Pageable.class), anyString(), anyString())).thenReturn(getSampleBankMerchantSearchIndexPage());

        assertEquals(BANK_MERCHANT_ID, merchantBankSearchIndexService
                .getBankMerchantSearchIndexByMerchantIdAndSearchTerm(pageable, MERCHANT_ID, SEARCH_TERM,
                        true).getContent().get(0).getId());
    }

    @Test
    void Should_ReturnBankMerchantSearchIndexPage_When_GettingMerchantSearchIndexByMerchantIdAndSearchTermForNonActiveBankAndAllSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE);

        when(bankMerchantSearchIndexRepository.findAllByMerchantId(any(Pageable.class), anyString()))
                .thenReturn(getSampleBankMerchantSearchIndexPage());

        assertEquals(BANK_MERCHANT_ID, merchantBankSearchIndexService
                .getBankMerchantSearchIndexByMerchantIdAndSearchTerm(pageable, MERCHANT_ID, ALL, false)
                .getContent().get(0).getId());
    }

    @Test
    void Should_ReturnBankMerchantSearchIndexPage_When_GettingMerchantSearchIndexByMerchantIdAndSearchTermForNonActiveBankAndSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE);

        when(bankMerchantSearchIndexRepository.findAllByMerchantIdAndBankNameContaining(pageable, MERCHANT_ID,
                SEARCH_TERM)).thenReturn(getSampleBankMerchantSearchIndexPage());

        assertEquals(BANK_MERCHANT_ID, merchantBankSearchIndexService
                .getBankMerchantSearchIndexByMerchantIdAndSearchTerm(pageable, MERCHANT_ID, SEARCH_TERM,
                        false).getContent().get(0).getId());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingMerchantSearchIndexByMerchantIdAndSearchTermIsFailed() {
        Pageable pageable = PageRequest.of(PAGE, SIZE);

        when(bankMerchantSearchIndexRepository.findAllByMerchantIdAndIsActiveBankTrue(any(Pageable.class), anyString()))
                .thenThrow(new DataAccessException(FAILED) {
                });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> merchantBankSearchIndexService
                .getBankMerchantSearchIndexByMerchantIdAndSearchTerm(pageable, MERCHANT_ID, ALL, true));

        assertEquals("Reading BankMerchantSearchIndex was failed", exception.getMessage());
    }

    /**
     * Get bank deal search index by bankId and searchTerm
     */
    @Test
    void Should_ReturnBankMerchantSearchIndexPage_When_GettingBankDealSearchIndexByBankIdAndSearchTermForActiveMerchantAndAllSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE);

        when(bankMerchantSearchIndexRepository.findAllByBankIdAndIsActiveMerchantTrue(any(Pageable.class), anyString()))
                .thenReturn(getSampleBankMerchantSearchIndexPage());

        assertEquals(BANK_MERCHANT_ID, merchantBankSearchIndexService.getBankDealSearchIndexByBankIdAndSearchTerm(
                pageable, BANK_ID, ALL, true).getContent().get(0).getId());
    }

    @Test
    void Should_ReturnBankMerchantSearchIndexPage_When_GettingBankDealSearchIndexByBankIdAndSearchTermForActiveMerchantAndSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE);

        when(bankMerchantSearchIndexRepository.findAllByBankIdAndIsActiveMerchantTrueAndMerchantNameContaining(
                any(Pageable.class), anyString(), anyString())).thenReturn(getSampleBankMerchantSearchIndexPage());

        assertEquals(BANK_MERCHANT_ID, merchantBankSearchIndexService.getBankDealSearchIndexByBankIdAndSearchTerm(
                pageable, BANK_ID, SEARCH_TERM, true).getContent().get(0).getId());
    }

    @Test
    void Should_ReturnBankMerchantSearchIndexPage_When_GettingBankDealSearchIndexByBankIdAndSearchTermForNonActiveMerchantAndAllSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE);

        when(bankMerchantSearchIndexRepository.findAllByBankId(any(Pageable.class), anyString()))
                .thenReturn(getSampleBankMerchantSearchIndexPage());

        assertEquals(BANK_MERCHANT_ID, merchantBankSearchIndexService.getBankDealSearchIndexByBankIdAndSearchTerm(
                pageable, BANK_ID, ALL, false).getContent().get(0).getId());
    }

    @Test
    void Should_ReturnBankMerchantSearchIndexPage_When_GettingBankDealSearchIndexByBankIdAndSearchTermForNonActiveMerchantAndSearchTerm() {
        Pageable pageable = PageRequest.of(PAGE, SIZE);

        when(bankMerchantSearchIndexRepository.findAllByBankIdAndMerchantNameContaining(any(Pageable.class), anyString()
                , anyString())).thenReturn(getSampleBankMerchantSearchIndexPage());

        assertEquals(BANK_MERCHANT_ID, merchantBankSearchIndexService.getBankDealSearchIndexByBankIdAndSearchTerm(
                pageable, BANK_ID, SEARCH_TERM, false).getContent().get(0).getId());
    }

    @Test
    void Should_ThrowKupiCoreException_When_GettingBankDealSearchIndexByBankIdAndSearchTermIsFailed() {
        Pageable pageable = PageRequest.of(PAGE, SIZE);

        when(bankMerchantSearchIndexRepository.findAllByBankIdAndIsActiveMerchantTrue(any(Pageable.class), anyString()))
                .thenThrow(new DataAccessException(FAILED) {
                });
        KupiCoreException exception = assertThrows(KupiCoreException.class, () -> merchantBankSearchIndexService
                .getBankDealSearchIndexByBankIdAndSearchTerm(pageable, BANK_ID, ALL, true));

        assertEquals("Reading BankMerchantSearchIndex was failed", exception.getMessage());
    }

    private Page<BankMerchantSearchIndex> getSampleBankMerchantSearchIndexPage() {
        BankMerchantSearchIndex bankMerchantSearchIndex = new BankMerchantSearchIndex();
        bankMerchantSearchIndex.setId(BANK_MERCHANT_ID);
        List<BankMerchantSearchIndex> bankMerchantSearchIndexList = new ArrayList<>();
        bankMerchantSearchIndexList.add(bankMerchantSearchIndex);
        return new PageImpl<>(bankMerchantSearchIndexList);
    }
}