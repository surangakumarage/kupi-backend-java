package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.OrderDetails;
import com.kupi.cc.base.domain.entity.PaymentBroker;
import com.kupi.cc.base.domain.request.PaymentBrokerUpdateRequestDto;
import com.kupi.cc.base.enums.CountryCode;
import com.kupi.cc.base.enums.PaymentBrokerName;
import com.kupi.cc.base.enums.PaymentOptionType;
import com.kupi.cc.base.exception.KupiOrderPaymentException;
import com.kupi.cc.base.repository.PaymentBrokerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * This class tests {@link PaymentBrokerService} class
 */
class PaymentBrokerServiceTest {
    private static final String PAYMENT_BROKER_ID = "pbid-20afe799-db4d-4bc1-adf7-f994c1f24b13";
    private static final String USER_ID = "uid-bb801b6a-ae7c-4f81-8876-a722f644fa11";
    @Mock
    private PaymentBrokerRepository paymentBrokerRepository;
    private PaymentBrokerService paymentBrokerService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        paymentBrokerService = new PaymentBrokerService(paymentBrokerRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks(this).close();
    }

    @Test
    void Should_SavePaymentBroker() {
        PaymentBroker paymentBroker = getSamplePaymentBroker();
        paymentBrokerService.savePaymentBroker(paymentBroker, USER_ID);
        verify(paymentBrokerRepository, times(1)).save(paymentBroker);
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_SavingPaymentBrokerIntoDataBaseIsFailed() {
        PaymentBroker paymentBroker = getSamplePaymentBroker();
        when(paymentBrokerRepository.save(paymentBroker)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                paymentBrokerService.savePaymentBroker(paymentBroker, USER_ID));
        assertEquals("Saving payment broker info into database was failed.", exception.getMessage());
    }

    @Test
    void Should_ReturnPaymentBroker_When_ValidPaymentBrokerIdIsProvidedForGetPaymentBrokerById() {
        PaymentBroker paymentBroker = getSamplePaymentBroker();
        when(paymentBrokerRepository.findById(PAYMENT_BROKER_ID)).thenReturn(Optional.of(paymentBroker));
        assertEquals(paymentBroker, paymentBrokerService.getPaymentBrokerById(PAYMENT_BROKER_ID));
        assertFalse(paymentBrokerService.getPaymentBrokerById(PAYMENT_BROKER_ID).isDeleted());
        assertTrue(paymentBrokerService.getPaymentBrokerById(PAYMENT_BROKER_ID).isActive());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_InvalidPaymentBrokerIdIsProvidedForGetPaymentBrokerById() {
        when(paymentBrokerRepository.findById(PAYMENT_BROKER_ID)).thenReturn(Optional.empty());
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                paymentBrokerService.getPaymentBrokerById(PAYMENT_BROKER_ID));
        assertEquals("Invalid Payment Broker Id: " + PAYMENT_BROKER_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_GetPaymentBrokerFromDataBaseIsFailed() {
        when(paymentBrokerRepository.findById(PAYMENT_BROKER_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                paymentBrokerService.getPaymentBrokerById(PAYMENT_BROKER_ID));
        assertEquals("Reading payment broker info from database was failed.", exception.getMessage());
    }

    @Test
    void Should_ReturnPaymentBroker_When_UpdatePaymentBrokerIsSuccessful() {
        PaymentBrokerUpdateRequestDto paymentBrokerUpdateRequestDto = getSamplePaymentBrokerUpdateRequestDto();
        PaymentBroker paymentBroker = getSamplePaymentBroker();
        when(paymentBrokerRepository.findById(PAYMENT_BROKER_ID)).thenReturn(Optional.of(paymentBroker));
        when(paymentBrokerRepository.save(paymentBroker)).thenReturn(paymentBroker);
        assertEquals(paymentBroker, paymentBrokerService.updatePaymentBroker(paymentBrokerUpdateRequestDto, USER_ID));
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_InvalidPaymentBrokerIdIsProvidedForUpdatePaymentBroker() {
        PaymentBrokerUpdateRequestDto paymentBrokerUpdateRequestDto = getSamplePaymentBrokerUpdateRequestDto();
        when(paymentBrokerRepository.findById(PAYMENT_BROKER_ID)).thenReturn(Optional.empty());
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                paymentBrokerService.updatePaymentBroker(paymentBrokerUpdateRequestDto, USER_ID));
        assertEquals("Invalid Payment Broker Id: " + PAYMENT_BROKER_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_GetPaymentBrokerFromDataBaseIsFailedForUpdatePaymentBroker() {
        PaymentBrokerUpdateRequestDto paymentBrokerUpdateRequestDto = getSamplePaymentBrokerUpdateRequestDto();
        when(paymentBrokerRepository.findById(PAYMENT_BROKER_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                paymentBrokerService.updatePaymentBroker(paymentBrokerUpdateRequestDto, USER_ID));
        assertEquals("Reading payment broker info from database was failed.", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_UpdatePaymentBrokerToDataBaseIsFailed() {
        PaymentBrokerUpdateRequestDto paymentBrokerUpdateRequestDto = getSamplePaymentBrokerUpdateRequestDto();
        PaymentBroker paymentBroker = getSamplePaymentBroker();
        when(paymentBrokerRepository.findById(PAYMENT_BROKER_ID)).thenReturn(Optional.of(paymentBroker));
        when(paymentBrokerRepository.save(paymentBroker)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                paymentBrokerService.updatePaymentBroker(paymentBrokerUpdateRequestDto, USER_ID));
        assertEquals("Updating payment broker by id: " + paymentBrokerUpdateRequestDto.getId() + " was failed.",
                exception.getMessage());
    }

    @Test
    void Should_DeletePaymentBrokerSuccessful() {
        PaymentBroker paymentBroker = getSamplePaymentBroker();
        paymentBroker.setDeleted(true);
        when(paymentBrokerRepository.findById(PAYMENT_BROKER_ID)).thenReturn(Optional.of(paymentBroker));
        paymentBrokerService.deletePaymentBroker(PAYMENT_BROKER_ID, USER_ID);
        verify(paymentBrokerRepository, times(1)).save(paymentBroker);
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_InvalidPaymentBrokerIdIsProvidedForDeletePaymentBroker() {
        when(paymentBrokerRepository.findById(PAYMENT_BROKER_ID)).thenReturn(Optional.empty());
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                paymentBrokerService.deletePaymentBroker(PAYMENT_BROKER_ID, USER_ID));
        assertEquals("Invalid Payment Broker Id: " + PAYMENT_BROKER_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_GetPaymentBrokerFromDataBaseIsFailedForDeletePaymentBroker() {
        when(paymentBrokerRepository.findById(PAYMENT_BROKER_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                paymentBrokerService.deletePaymentBroker(PAYMENT_BROKER_ID, USER_ID));
        assertEquals("Reading payment broker info from database was failed.", exception.getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_DeletePaymentBrokerFromDataBaseIsFailed() {
        PaymentBroker paymentBroker = getSamplePaymentBroker();
        when(paymentBrokerRepository.findById(PAYMENT_BROKER_ID)).thenReturn(Optional.of(paymentBroker));
        when(paymentBrokerRepository.save(paymentBroker)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                paymentBrokerService.deletePaymentBroker(PAYMENT_BROKER_ID, USER_ID));
        assertEquals("Deleting payment broker by id: " + PAYMENT_BROKER_ID + " was failed.",
                exception.getMessage());
    }

    @Test
    void Should_ReturnAllPaymentBrokersList() {
        List<PaymentBroker> paymentBrokerList = getSamplePaymentBrokerList();
        when(paymentBrokerRepository.findByIsActiveTrueAndIsDeletedFalse()).thenReturn(paymentBrokerList);
        assertEquals(paymentBrokerList, paymentBrokerService.getAllPaymentBrokers(USER_ID));
        assertFalse(paymentBrokerService.getAllPaymentBrokers(USER_ID).get(0).isDeleted());
        assertTrue(paymentBrokerService.getAllPaymentBrokers(USER_ID).get(0).isActive());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_GetAllPaymentBrokersFromDataBaseIsFailed() {
        when(paymentBrokerRepository.findByIsActiveTrueAndIsDeletedFalse()).thenThrow(new DataAccessException("ERROR") {
        });
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                paymentBrokerService.getAllPaymentBrokers(USER_ID));
        assertEquals("Error accessing repository to find active payment broker details for user-id: "
                + USER_ID, exception.getMessage());
    }

    /**
     * This method creates sample PaymentBroker
     *
     * @return PaymentBroker
     */
    private PaymentBroker getSamplePaymentBroker() {
        PaymentBroker paymentBroker = new PaymentBroker();
        paymentBroker.setName(PaymentBrokerName.PAYPAL);
        paymentBroker.setId(PAYMENT_BROKER_ID);
        paymentBroker.setPaymentOptionType(PaymentOptionType.EXTERNAL);
        paymentBroker.setCountryCode(CountryCode.LKA);
        paymentBroker.setCreatedBy(USER_ID);
        paymentBroker.setLastModifiedBy(USER_ID);
        paymentBroker.setLastModifiedDate(1664872106178L);
        paymentBroker.setActive(true);
        paymentBroker.setDeleted(false);
        OrderDetails orderDetails = new OrderDetails();
        Set<OrderDetails> orderDetailsSet = new HashSet<>();
        orderDetailsSet.add(orderDetails);
        paymentBroker.setOrderDetails(orderDetailsSet);
        return paymentBroker;
    }

    /**
     * This method creates sample paymentBrokerUpdateRequestDto
     *
     * @return PaymentBrokerUpdateRequestDto
     */
    private PaymentBrokerUpdateRequestDto getSamplePaymentBrokerUpdateRequestDto() {
        PaymentBrokerUpdateRequestDto paymentBrokerUpdateRequestDto = new PaymentBrokerUpdateRequestDto();
        paymentBrokerUpdateRequestDto.setId(PAYMENT_BROKER_ID);
        return paymentBrokerUpdateRequestDto;
    }

    /**
     * This method creates sample paymentBrokerList
     *
     * @return PaymentBrokerList
     */
    private List<PaymentBroker> getSamplePaymentBrokerList() {
        PaymentBroker paymentBroker = getSamplePaymentBroker();
        List<PaymentBroker> paymentBrokerList = new ArrayList<>();
        paymentBrokerList.add(paymentBroker);
        return paymentBrokerList;
    }
}