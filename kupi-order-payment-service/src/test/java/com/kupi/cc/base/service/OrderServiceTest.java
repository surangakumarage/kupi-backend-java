package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.Coupon;
import com.kupi.cc.base.domain.entity.OrderDetails;
import com.kupi.cc.base.domain.entity.PaymentBroker;
import com.kupi.cc.base.domain.request.BulkOrderInfoRequestDto;
import com.kupi.cc.base.domain.request.CouponUpdateRequestDto;
import com.kupi.cc.base.domain.request.OrderUpdateRequestDto;
import com.kupi.cc.base.enums.*;
import com.kupi.cc.base.exception.InvalidOrderException;
import com.kupi.cc.base.exception.InvalidOrderStateException;
import com.kupi.cc.base.exception.KupiOrderPaymentException;
import com.kupi.cc.base.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * This class tests {@link OrderService} class
 */
class OrderServiceTest {
    private static final String ORDER_DETAILS_ID = "oid-1524-8569-4852";
    private static final String USER_ID = "uid-bb801b6a-ae7c-4f81-8876-a722f644fa11";
    private static final long CREATED_DAY = 1667891596284L;
    private static final long START_CREATE_DAY = 1667241000000L;
    private static final long END_CREATE_DAY = 11698777000000L;
    private static final String COUPON_ID = "couid-03d448b3-3f89-411a-8570-f49efbecc9da";
    private static final String DEAL_ID = "did-d47fc18c-4c1b-4a68-9384-968a2613f385";
    private static final String PAYMENT_BROKER_ID = "pbid-20afe799-db4d-4bc1-adf7-f994c1f24b13";
    @Mock
    private OrderRepository orderRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        orderService = new OrderService(orderRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks(this).close();
    }

    @Test
    void Should_ReturnOrderDetails_When_PlaceAOrder() {
        OrderDetails orderDetails = getSampleOrderDetails();
        when(orderRepository.saveAndFlush(orderDetails)).thenReturn(orderDetails);
        assertEquals(orderDetails, orderService.placeOrder(orderDetails, USER_ID));
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_SaveOrderDetailsIsFailed() {
        OrderDetails orderDetails = getSampleOrderDetails();
        when(orderRepository.saveAndFlush(orderDetails)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                orderService.placeOrder(orderDetails, USER_ID));
        assertEquals("Saving orderDetails into database was failed.", exception.getMessage());
    }

    @Test
    void Should_ReturnOrderDetails_When_UpdatedSuccessfully() {
        OrderDetails orderDetails = getSampleOrderDetails();
        OrderUpdateRequestDto orderUpdateRequestDto = getSampleOrderUpdateRequestDto();
        when(orderRepository.findById(ORDER_DETAILS_ID)).thenReturn(Optional.of(orderDetails));
        when(orderRepository.saveAndFlush(orderDetails)).thenReturn(orderDetails);
        assertEquals(orderDetails, orderService.updateOrder(orderUpdateRequestDto, USER_ID));
    }

    @Test
    void Should_ThrowInvalidOrderException_When_InvalidOrderDetailsIdIsProvided() {
        OrderUpdateRequestDto orderUpdateRequestDto = getSampleOrderUpdateRequestDto();
        when(orderRepository.findById(ORDER_DETAILS_ID)).thenReturn(Optional.empty());
        InvalidOrderException exception = assertThrows(InvalidOrderException.class, () ->
                orderService.updateOrder(orderUpdateRequestDto, USER_ID));
        assertEquals("Invalid orderDetails id: " + ORDER_DETAILS_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_GetOrderDetailsFromDataBaseIsFailedForUpdateOrderDetails() {
        OrderUpdateRequestDto orderUpdateRequestDto = getSampleOrderUpdateRequestDto();
        when(orderRepository.findById(ORDER_DETAILS_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                orderService.updateOrder(orderUpdateRequestDto, USER_ID));
        assertEquals("Returning orderDetails from database was failed.", exception.getMessage());
    }

    @Test
    void Should_ThrowInvalidOrderStateException_When_OrderDetailsStatusIsNotEqualToCreated() {
        OrderDetails orderDetails = getSampleOrderDetails();
        orderDetails.setStatus(OrderStatus.UPDATED);
        OrderUpdateRequestDto orderUpdateRequestDto = getSampleOrderUpdateRequestDto();
        when(orderRepository.findById(ORDER_DETAILS_ID)).thenReturn(Optional.of(orderDetails));
        InvalidOrderStateException exception = assertThrows(InvalidOrderStateException.class, () ->
                orderService.updateOrder(orderUpdateRequestDto, USER_ID));
        assertEquals("OrderDetails: " + orderUpdateRequestDto.getId() + " cannot be updated while in state: "
                + orderDetails.getStatus().toString(), exception.getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_UpdateOrderDetailsIsFailed() {
        OrderDetails orderDetails = getSampleOrderDetails();
        OrderUpdateRequestDto orderUpdateRequestDto = getSampleOrderUpdateRequestDto();
        when(orderRepository.findById(ORDER_DETAILS_ID)).thenReturn(Optional.of(orderDetails));
        when(orderRepository.saveAndFlush(orderDetails)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                orderService.updateOrder(orderUpdateRequestDto, USER_ID));
        assertEquals("Updating order into database was failed.", exception.getMessage());
    }

    @Test
    void Should_ReturnAllOllOrderDetails() {
        OrderDetails orderDetails = getSampleOrderDetails();
        List<OrderDetails> orderDetailsList = new ArrayList<>();
        orderDetailsList.add(orderDetails);
        when(orderRepository.findAllByUserId(USER_ID)).thenReturn(orderDetailsList);
        assertEquals(orderDetailsList, orderService.getAllOrders(USER_ID));
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_GetAllOrderDetailsListIsFailed() {
        when(orderRepository.findAllByUserId(USER_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                orderService.getAllOrders(USER_ID));
        assertEquals("Returning orderDetails from database was failed.", exception.getMessage());
    }

    @Test
    void Should_ReturnOrderDetails_When_ValidOrderDetailsIdIsProvided() {
        OrderDetails orderDetails = getSampleOrderDetails();
        when(orderRepository.findById(ORDER_DETAILS_ID)).thenReturn(Optional.of(orderDetails));
        assertEquals(orderDetails, orderService.getOrderById(ORDER_DETAILS_ID));
    }

    @Test
    void Should_ThrowInvalidOrderException_When_InvalidIdIsProvidedToGetOrderDetails() {
        when(orderRepository.findById(ORDER_DETAILS_ID)).thenReturn(Optional.empty());
        InvalidOrderException exception = assertThrows(InvalidOrderException.class, () ->
                orderService.getOrderById(ORDER_DETAILS_ID));
        assertEquals("Invalid orderDetails id: " + ORDER_DETAILS_ID, exception.getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_GetOrderDetailsIsFailed() {
        when(orderRepository.findById(ORDER_DETAILS_ID)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                orderService.getOrderById(ORDER_DETAILS_ID));
        assertEquals("Returning orderDetails from database was failed.", exception.getMessage());
    }

    @Test
    void Should_CancelOrder() {
        OrderDetails orderDetails = getSampleOrderDetails();
        when(orderRepository.findById(ORDER_DETAILS_ID)).thenReturn(Optional.of(orderDetails));
        orderService.cancelOrder(ORDER_DETAILS_ID, USER_ID);
        verify(orderRepository).save(orderDetails);
    }

    @Test
    void Should_ThrowInvalidOrderStateException_When_StatusIsNotEqualToCreated() {
        OrderDetails orderDetails = getSampleOrderDetails();
        orderDetails.setStatus(OrderStatus.UPDATED);
        when(orderRepository.findById(ORDER_DETAILS_ID)).thenReturn(Optional.of(orderDetails));
        InvalidOrderStateException exception = assertThrows(InvalidOrderStateException.class, () ->
                orderService.cancelOrder(ORDER_DETAILS_ID, USER_ID));
        assertEquals("OrderDetails: " + ORDER_DETAILS_ID + " cannot be cancelled while in state: "
                + orderDetails.getStatus().toString(), exception.getMessage());
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_WhenCancelTheOrderIsFailed() {
        OrderDetails orderDetails = getSampleOrderDetails();
        when(orderRepository.findById(ORDER_DETAILS_ID)).thenReturn(Optional.of(orderDetails));
        when(orderRepository.save(orderDetails)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                orderService.cancelOrder(ORDER_DETAILS_ID, USER_ID));
        assertEquals("Returning orderDetails from database was failed.", exception.getMessage());
    }

    @Test
    void Should_ReturnBulkOrderInfo_When_RequiredFieldsAreProvided() {
        OrderDetails orderDetails = getSampleOrderDetails();
        orderDetails.setCreatedDate(CREATED_DAY);
        BulkOrderInfoRequestDto bulkOrderInfoRequestDto = getSampleBulkOrderInfoRequestDto();
        Pageable pageable = PageRequest.of(0, 1, Sort.by("updateAt").descending());
        List<OrderDetails> orderDetailsList = new ArrayList<>();
        orderDetailsList.add(orderDetails);
        Page<OrderDetails> orderDetailsPage = new PageImpl<>(orderDetailsList, pageable, 1);
        when(orderRepository.findAll(pageable)).thenReturn(orderDetailsPage);
        assertEquals(orderDetailsList, orderService.getBulkOrderInfo(bulkOrderInfoRequestDto, pageable));
    }

    @Test
    void Should_ThrowKupiOrderPaymentException_When_GetBulkOrderDetailsListIsFailed() {
        BulkOrderInfoRequestDto bulkOrderInfoRequestDto = getSampleBulkOrderInfoRequestDto();
        Pageable pageable = PageRequest.of(0, 1, Sort.by("updateAt").descending());
        when(orderRepository.findAll(pageable)).thenThrow(new DataAccessException("ERROR") {
        });
        KupiOrderPaymentException exception = assertThrows(KupiOrderPaymentException.class, () ->
                orderService.getBulkOrderInfo(bulkOrderInfoRequestDto, pageable));
        assertEquals("Returning orders from database was failed.", exception.getMessage());
    }

    @Test
    void Should_ReturnBulkOrderInfo_When_CustomerIdListIsNull() {
        OrderDetails orderDetails = getSampleOrderDetails();
        BulkOrderInfoRequestDto bulkOrderInfoRequestDto = getSampleBulkOrderInfoRequestDto();
        bulkOrderInfoRequestDto.setCustomerIdList(null);
        Pageable pageable = PageRequest.of(0, 1, Sort.by("updateAt").descending());
        List<OrderDetails> orderDetailsList = new ArrayList<>();
        orderDetailsList.add(orderDetails);
        Page<OrderDetails> orderDetailsPage = new PageImpl<>(orderDetailsList, pageable, 1);
        when(orderRepository.findAll(pageable)).thenReturn(orderDetailsPage);
        assertEquals(orderDetailsList, orderService.getBulkOrderInfo(bulkOrderInfoRequestDto, pageable));
    }

    @Test
    void Should_ReturnBulkOrderInfo_When_CurrencyTypeListIsNull() {
        OrderDetails orderDetails = getSampleOrderDetails();
        BulkOrderInfoRequestDto bulkOrderInfoRequestDto = getSampleBulkOrderInfoRequestDto();
        bulkOrderInfoRequestDto.setCurrencyTypeList(null);
        Pageable pageable = PageRequest.of(0, 1, Sort.by("updateAt").descending());
        List<OrderDetails> orderDetailsList = new ArrayList<>();
        orderDetailsList.add(orderDetails);
        Page<OrderDetails> orderDetailsPage = new PageImpl<>(orderDetailsList, pageable, 1);
        when(orderRepository.findAll(pageable)).thenReturn(orderDetailsPage);
        assertEquals(orderDetailsList, orderService.getBulkOrderInfo(bulkOrderInfoRequestDto, pageable));
    }

    @Test
    void Should_ReturnBulkOrderInfo_When_CreateDayIsZero() {
        OrderDetails orderDetails = getSampleOrderDetails();
        BulkOrderInfoRequestDto bulkOrderInfoRequestDto = getSampleBulkOrderInfoRequestDto();
        bulkOrderInfoRequestDto.setCreatedDate(0);
        Pageable pageable = PageRequest.of(0, 1, Sort.by("updateAt").descending());
        List<OrderDetails> orderDetailsList = new ArrayList<>();
        orderDetailsList.add(orderDetails);
        Page<OrderDetails> orderDetailsPage = new PageImpl<>(orderDetailsList, pageable, 1);
        when(orderRepository.findAll(pageable)).thenReturn(orderDetailsPage);
        assertEquals(orderDetailsList, orderService.getBulkOrderInfo(bulkOrderInfoRequestDto, pageable));
    }

    @Test
    void Should_ReturnBulkOrderInfo_When_OrderCreatedDateRangeStartIsZero() {
        OrderDetails orderDetails = getSampleOrderDetails();
        BulkOrderInfoRequestDto bulkOrderInfoRequestDto = getSampleBulkOrderInfoRequestDto();
        bulkOrderInfoRequestDto.setCreatedDate(0);
        bulkOrderInfoRequestDto.setCreatedDateRangeStart(0);
        Pageable pageable = PageRequest.of(0, 1, Sort.by("updateAt").descending());
        List<OrderDetails> orderDetailsList = new ArrayList<>();
        orderDetailsList.add(orderDetails);
        Page<OrderDetails> orderDetailsPage = new PageImpl<>(orderDetailsList, pageable, 1);
        when(orderRepository.findAll(pageable)).thenReturn(orderDetailsPage);
        assertEquals(orderDetailsList, orderService.getBulkOrderInfo(bulkOrderInfoRequestDto, pageable));
    }

    @Test
    void Should_ReturnBulkOrderInfo_When_OrderNotExpired() {
        OrderDetails orderDetails = getSampleOrderDetails();
        BulkOrderInfoRequestDto bulkOrderInfoRequestDto = getSampleBulkOrderInfoRequestDto();
        Coupon coupon = getSampleCoupon();
        coupon.setDealExpiryDate(1671042600000L);
        Set<Coupon> couponSet = new HashSet<>();
        couponSet.add(coupon);
        orderDetails.setCoupons(couponSet);
        Pageable pageable = PageRequest.of(0, 1, Sort.by("updateAt").descending());
        List<OrderDetails> orderDetailsList = new ArrayList<>();
        orderDetailsList.add(orderDetails);
        Page<OrderDetails> orderDetailsPage = new PageImpl<>(orderDetailsList, pageable, 1);
        when(orderRepository.findAll(pageable)).thenReturn(orderDetailsPage);
        assertEquals(orderDetailsList, orderService.getBulkOrderInfo(bulkOrderInfoRequestDto, pageable));
    }

    @Test
    void Should_ReturnBulkOrderInfo_When_DealIdListIsNull() {
        OrderDetails orderDetails = getSampleOrderDetails();
        BulkOrderInfoRequestDto bulkOrderInfoRequestDto = getSampleBulkOrderInfoRequestDto();
        bulkOrderInfoRequestDto.setDealIdList(null);
        Pageable pageable = PageRequest.of(0, 1, Sort.by("updateAt").descending());
        List<OrderDetails> orderDetailsList = new ArrayList<>();
        orderDetailsList.add(orderDetails);
        Page<OrderDetails> orderDetailsPage = new PageImpl<>(orderDetailsList, pageable, 1);
        when(orderRepository.findAll(pageable)).thenReturn(orderDetailsPage);
        assertEquals(orderDetailsList, orderService.getBulkOrderInfo(bulkOrderInfoRequestDto, pageable));
    }

    @Test
    void Should_ReturnBulkOrderInfo_When_MerchantIdListIsNull() {
        OrderDetails orderDetails = getSampleOrderDetails();
        BulkOrderInfoRequestDto bulkOrderInfoRequestDto = getSampleBulkOrderInfoRequestDto();
        bulkOrderInfoRequestDto.setMerchantIdList(null);
        Pageable pageable = PageRequest.of(0, 1, Sort.by("updateAt").descending());
        List<OrderDetails> orderDetailsList = new ArrayList<>();
        orderDetailsList.add(orderDetails);
        Page<OrderDetails> orderDetailsPage = new PageImpl<>(orderDetailsList, pageable, 1);
        when(orderRepository.findAll(pageable)).thenReturn(orderDetailsPage);
        assertEquals(orderDetailsList, orderService.getBulkOrderInfo(bulkOrderInfoRequestDto, pageable));
    }

    @Test
    void Should_ReturnBulkOrderInfo_When_OrderStatusListIsNull() {
        OrderDetails orderDetails = getSampleOrderDetails();
        BulkOrderInfoRequestDto bulkOrderInfoRequestDto = getSampleBulkOrderInfoRequestDto();
        bulkOrderInfoRequestDto.setOrderStatusList(null);
        Pageable pageable = PageRequest.of(0, 1, Sort.by("updateAt").descending());
        List<OrderDetails> orderDetailsList = new ArrayList<>();
        orderDetailsList.add(orderDetails);
        Page<OrderDetails> orderDetailsPage = new PageImpl<>(orderDetailsList, pageable, 1);
        when(orderRepository.findAll(pageable)).thenReturn(orderDetailsPage);
        assertEquals(orderDetailsList, orderService.getBulkOrderInfo(bulkOrderInfoRequestDto, pageable));
    }

    @Test
    void Should_ReturnBulkOrderInfo_When_OrderDetailsNotHavePaymentBroker() {
        OrderDetails orderDetails = getSampleOrderDetails();
        BulkOrderInfoRequestDto bulkOrderInfoRequestDto = getSampleBulkOrderInfoRequestDto();
        orderDetails.setPaymentBroker(null);
        Pageable pageable = PageRequest.of(0, 1, Sort.by("updateAt").descending());
        List<OrderDetails> orderDetailsList = new ArrayList<>();
        orderDetailsList.add(orderDetails);
        Page<OrderDetails> orderDetailsPage = new PageImpl<>(orderDetailsList, pageable, 1);
        when(orderRepository.findAll(pageable)).thenReturn(orderDetailsPage);
        assertEquals(0, orderService.getBulkOrderInfo(bulkOrderInfoRequestDto, pageable).size());
    }

    @Test
    void Should_ReturnZero_When_PaymentBrokerIdListIsNull() {
        OrderDetails orderDetails = getSampleOrderDetails();
        BulkOrderInfoRequestDto bulkOrderInfoRequestDto = getSampleBulkOrderInfoRequestDto();
        bulkOrderInfoRequestDto.setPaymentBrokerIdList(null);
        Pageable pageable = PageRequest.of(0, 1, Sort.by("updateAt").descending());
        List<OrderDetails> orderDetailsList = new ArrayList<>();
        orderDetailsList.add(orderDetails);
        Page<OrderDetails> orderDetailsPage = new PageImpl<>(orderDetailsList, pageable, 1);
        when(orderRepository.findAll(pageable)).thenReturn(orderDetailsPage);
        assertEquals(orderDetailsList, orderService.getBulkOrderInfo(bulkOrderInfoRequestDto, pageable));
    }

    /**
     * This method creates sample order details
     *
     * @return OrderDetails
     */
    private OrderDetails getSampleOrderDetails() {
        PaymentBroker paymentBroker = getSamplePaymentBroker();
        OrderDetails orderDetails = new OrderDetails();
        Coupon coupon = getSampleCoupon();
        Set<Coupon> couponSet = new HashSet<>();
        couponSet.add(coupon);
        orderDetails.setId(ORDER_DETAILS_ID);
        orderDetails.setUserId(USER_ID);
        orderDetails.setCreatedDate(CREATED_DAY);
        orderDetails.setStatus(OrderStatus.CREATED);
        orderDetails.setCurrencyType(CurrencyType.USD);
        orderDetails.setStateSequence("StateSequence");
        orderDetails.setCoupons(couponSet);
        orderDetails.setPaymentBroker(paymentBroker);
        return orderDetails;
    }

    /**
     * This method creates sample coupon
     *
     * @return Coupon
     */
    private Coupon getSampleCoupon() {
        Coupon coupon = new Coupon();
        coupon.setId("ID");
        coupon.setCouponId(COUPON_ID);
        coupon.setDealId(DEAL_ID);
        coupon.setDeleted(false);
        coupon.setMerchantId(USER_ID);
        coupon.setDealExpiryDate(1652553000000L);
        coupon.setCouponPrice(BigDecimal.valueOf(150));
        return coupon;
    }

    /**
     * This method creates sample order update request dto
     *
     * @return OrderUpdateRequestDto
     */
    private OrderUpdateRequestDto getSampleOrderUpdateRequestDto() {
        OrderUpdateRequestDto orderUpdateRequestDto = new OrderUpdateRequestDto();
        CouponUpdateRequestDto couponUpdateRequestDto = getSampleCouponUpdateRequestDto();
        List<CouponUpdateRequestDto> couponUpdateRequestDtos = new ArrayList<>();
        couponUpdateRequestDtos.add(couponUpdateRequestDto);
        orderUpdateRequestDto.setId(ORDER_DETAILS_ID);
        orderUpdateRequestDto.setCurrencyType(CurrencyType.USD);
        orderUpdateRequestDto.setCoupons(couponUpdateRequestDtos);
        return orderUpdateRequestDto;
    }

    /**
     * This method creates sample coupon update request dto
     *
     * @return CouponUpdateRequestDto
     */
    private CouponUpdateRequestDto getSampleCouponUpdateRequestDto() {
        CouponUpdateRequestDto couponUpdateRequestDto = new CouponUpdateRequestDto();
        couponUpdateRequestDto.setId("ID");
        couponUpdateRequestDto.setCouponId(COUPON_ID);
        couponUpdateRequestDto.setCouponPrice(BigDecimal.valueOf(200));
        couponUpdateRequestDto.setDealId(DEAL_ID);
        couponUpdateRequestDto.setMerchantId(USER_ID);
        couponUpdateRequestDto.setDeleted(false);
        return couponUpdateRequestDto;
    }

    /**
     * This method creates sample bulk order info request dto
     *
     * @return BulkOrderInfoRequestDto
     */
    private BulkOrderInfoRequestDto getSampleBulkOrderInfoRequestDto() {
        BulkOrderInfoRequestDto bulkOrderInfoRequestDto = new BulkOrderInfoRequestDto();
        List<String> customerIdString = new ArrayList<>();
        customerIdString.add(USER_ID);
        List<CurrencyType> currencyTypeList = new ArrayList<>();
        currencyTypeList.add(CurrencyType.USD);
        currencyTypeList.add(CurrencyType.LKR);
        List<String> dealIdList = new ArrayList<>();
        dealIdList.add(DEAL_ID);
        List<String> merchantIdList = new ArrayList<>();
        merchantIdList.add(USER_ID);
        List<OrderStatus> orderStatusList = new ArrayList<>();
        orderStatusList.add(OrderStatus.CREATED);
        orderStatusList.add(OrderStatus.CANCELLED);
        List<String> paymentBrokerIdList = new ArrayList<>();
        paymentBrokerIdList.add(PAYMENT_BROKER_ID);
        bulkOrderInfoRequestDto.setCreatedDate(CREATED_DAY);
        bulkOrderInfoRequestDto.setCreatedDateRangeEnd(END_CREATE_DAY);
        bulkOrderInfoRequestDto.setCreatedDateRangeStart(START_CREATE_DAY);
        bulkOrderInfoRequestDto.setCustomerIdList(customerIdString);
        bulkOrderInfoRequestDto.setCurrencyTypeList(currencyTypeList);
        bulkOrderInfoRequestDto.setIncludeExpired(true);
        bulkOrderInfoRequestDto.setIncludeNotExpired(true);
        bulkOrderInfoRequestDto.setOrderStatusList(orderStatusList);
        bulkOrderInfoRequestDto.setPaymentBrokerIdList(paymentBrokerIdList);
        bulkOrderInfoRequestDto.setDealIdList(dealIdList);
        bulkOrderInfoRequestDto.setMerchantIdList(merchantIdList);
        return bulkOrderInfoRequestDto;
    }

    /**
     * This method creates sample payment broker
     *
     * @return PaymentBroker
     */
    private PaymentBroker getSamplePaymentBroker() {
        PaymentBroker paymentBroker = new PaymentBroker();
        paymentBroker.setId(PAYMENT_BROKER_ID);
        paymentBroker.setPaymentOptionType(PaymentOptionType.EXTERNAL);
        paymentBroker.setDeleted(false);
        paymentBroker.setActive(true);
        paymentBroker.setCountryCode(CountryCode.LKA);
        paymentBroker.setCurrencyType(CurrencyType.USD);
        paymentBroker.setName(PaymentBrokerName.PAYPAL);
        return paymentBroker;
    }
}