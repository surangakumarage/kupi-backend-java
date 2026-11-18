package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.Coupon;
import com.kupi.cc.base.domain.entity.OrderDetails;
import com.kupi.cc.base.domain.entity.PaymentBroker;
import com.kupi.cc.base.domain.request.*;
import com.kupi.cc.base.enums.*;
import com.kupi.cc.base.exception.InvalidCouponException;
import com.kupi.cc.base.exception.InvalidOrderException;
import com.kupi.cc.base.exception.InvalidOrderStateException;
import com.kupi.cc.base.exception.KupiOrderPaymentException;
import com.kupi.cc.base.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class tests {@link OrderController} class
 */
class OrderControllerTest {

    private static final String CREATE_ORDER_URI = "/api/v1/order";
    private static final String UPDATE_ORDER_URI = "/api/v1/order";
    private static final String Get_All_ORDER_URI = "/api/v1/order";
    private static final String Get_BULK_ORDER_INFO_URI = "/api/v1/order/bulk-info/{page}/{size}";
    private static final String GET_ORDER_URI = "/api/v1/order/{orderId}";
    private static final String CANCEL_ORDER_URI = "/api/v1/order/{orderId}";
    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String USER_ID_HEADER = "User-Id";
    private static final String APPLICATION_JSON_UTF_8 = "application/json;charset=UTF-8";
    private static final String ORDER_DETAILS_ID = "oid-1524-8569-4852";
    private static final String USER_ID = "uid-bb801b6a-ae7c-4f81-8876-a722f644fa11";
    private static final String TIME_ZONE = "Asia/Colombo";
    private static final long CREATED_DAY = 1667891596284L;
    private static final long START_CREATE_DAY = System.currentTimeMillis();
    private static final long END_CREATE_DAY = System.currentTimeMillis();
    private static final int PAGE = 0;
    private static final int SIZE = 1;
    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String ERROR_STATUS = "ERROR";
    private static final String COUPON_ID = "couid-03d448b3-3f89-411a-8570-f49efbecc9da";
    private static final String DEAL_ID = "did-d47fc18c-4c1b-4a68-9384-968a2613f385";
    private static final String PAYMENT_BROKER_ID = "pbid-20afe799-db4d-4bc1-adf7-f994c1f24b13";
    private static final BigDecimal COUPON_PRICE = BigDecimal.valueOf(1000);
    private static final long DEAL_EXPIRE_DATE = System.currentTimeMillis() + 10000;
    private static final long COUPON_EXPIRE_DATE = System.currentTimeMillis() + 10000;

    @Mock
    private OrderService orderService;
    @Mock
    private Translator translator;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        openMocks(this);
        OrderController orderController = new OrderController(translator, orderService);
        org.springframework.validation.Validator noOpValidator = new org.springframework.validation.Validator() {
            @Override
            public boolean supports(Class<?> clazz) {
                return true;
            }

            @Override
            public void validate(Object target, Errors errors) {
                // No-op validator
            }
        };
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setValidator(noOpValidator)
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks(this).close();
    }

    @Test
    void Should_ReturnOk_When_PlaceOrderSuccessfully() throws Exception {
        OrderDetails orderDetails = getSampleOrderDetails();
        OrderCreateRequestDto orderCreateRequestDto = getSampleOrderCreateRequestDto();
        when(orderService.placeOrder(any(OrderDetails.class), anyString())).thenReturn(orderDetails);
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_ORDER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(orderCreateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.CREATE_ORDER.getMessage()))
                .andExpect(jsonPath("$.data.id").value(orderDetails.getId()))
                .andExpect(jsonPath("$.data.userId").value(orderDetails.getUserId()))
                .andExpect(jsonPath("$.data.status").value(orderDetails.getStatus().toString()))
                .andExpect(jsonPath("$.successCode").value(SuccessResponseStatusType.CREATE_ORDER.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_UserIdIsEmpty() throws Exception {
        OrderCreateRequestDto orderCreateRequestDto = getSampleOrderCreateRequestDto();
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_ORDER_URI)
                        .header(USER_ID_HEADER, "")
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(orderCreateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_ID.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_ID.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_RequiredFieldsAreNotProvidedForCreateOrder() throws Exception {
        OrderCreateRequestDto orderCreateRequestDto = getSampleOrderCreateRequestDto();
        orderCreateRequestDto.setCoupons(null);
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_ORDER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(orderCreateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidOrderStatusIsProvidedForCreateOrder() throws Exception {
        OrderCreateRequestDto orderCreateRequestDto = getSampleOrderCreateRequestDto();
        doThrow(new InvalidOrderStateException("ERROR")).when(orderService).placeOrder(any(OrderDetails.class), anyString());
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_ORDER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(orderCreateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_ORDER_STATUS.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_ORDER_STATUS.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_CreateOrderIsFailed() throws Exception {
        OrderCreateRequestDto orderCreateRequestDto = getSampleOrderCreateRequestDto();
        doThrow(new KupiOrderPaymentException("ERROR")).when(orderService).placeOrder(any(OrderDetails.class), anyString());
        mockMvc.perform(MockMvcRequestBuilders.post(CREATE_ORDER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(orderCreateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_UpdateOrderDetailsSuccessfully() throws Exception {
        OrderUpdateRequestDto orderUpdateRequestDto = getSampleOrderUpdateRequestDto();
        OrderDetails orderDetails = getSampleOrderDetails();
        when(orderService.updateOrder(any(OrderUpdateRequestDto.class), anyString())).thenReturn(orderDetails);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_ORDER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(orderUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.UPDATE_ORDER.getMessage()))
                .andExpect(jsonPath("$.data.id").value(orderDetails.getId()))
                .andExpect(jsonPath("$.data.userId").value(orderDetails.getUserId()))
                .andExpect(jsonPath("$.data.status").value(orderDetails.getStatus().toString()))
                .andExpect(jsonPath("$.successCode").value(SuccessResponseStatusType.UPDATE_ORDER.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_UserIdIsEmptyForUpdateOrderDetails() throws Exception {
        OrderUpdateRequestDto orderUpdateRequestDto = getSampleOrderUpdateRequestDto();
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_ORDER_URI)
                        .header(USER_ID_HEADER, "")
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(orderUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_ID.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_ID.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_MissingRequiredAvailableForUpdateOrderDetails() throws Exception {
        OrderUpdateRequestDto orderUpdateRequestDto = getSampleOrderUpdateRequestDto();
        orderUpdateRequestDto.setCoupons(null);
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_ORDER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(orderUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidCouponProvidedForUpdateOrderDetails() throws Exception {
        OrderUpdateRequestDto orderUpdateRequestDto = getSampleOrderUpdateRequestDto();
        doThrow(new InvalidCouponException("ERROR")).when(orderService).updateOrder(any(OrderUpdateRequestDto.class),
                anyString());
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_ORDER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(orderUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_ITEM_ID.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_ITEM_ID.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidOrderStatusIsProvidedForUpdateOrderDetails() throws Exception {
        OrderUpdateRequestDto orderUpdateRequestDto = getSampleOrderUpdateRequestDto();
        doThrow(new InvalidOrderStateException("ERROR")).when(orderService).updateOrder(any(OrderUpdateRequestDto.class),
                anyString());
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_ORDER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(orderUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_ORDER_STATUS.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_ORDER_STATUS.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidOrderIdIsProvidedForUpdateOrderDetails() throws Exception {
        OrderUpdateRequestDto orderUpdateRequestDto = getSampleOrderUpdateRequestDto();
        doThrow(new InvalidOrderException("ERROR")).when(orderService).updateOrder(any(OrderUpdateRequestDto.class),
                anyString());
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_ORDER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(orderUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_ORDER_ID.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_ORDER_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_UpdateOrderDetailsIsFailed() throws Exception {
        OrderUpdateRequestDto orderUpdateRequestDto = getSampleOrderUpdateRequestDto();
        doThrow(new KupiOrderPaymentException("ERROR")).when(orderService).updateOrder(any(OrderUpdateRequestDto.class),
                anyString());
        mockMvc.perform(MockMvcRequestBuilders.put(UPDATE_ORDER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(orderUpdateRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_GetOrderDetailsByIdSuccessfully() throws Exception {
        OrderDetails orderDetails = getSampleOrderDetails();
        when(orderService.getOrderById(ORDER_DETAILS_ID)).thenReturn(orderDetails);
        String uri = GET_ORDER_URI.replace("{orderId}", ORDER_DETAILS_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_ORDER.getMessage()))
                .andExpect(jsonPath("$.data.id").value(orderDetails.getId()))
                .andExpect(jsonPath("$.data.userId").value(orderDetails.getUserId()))
                .andExpect(jsonPath("$.data.status").value(orderDetails.getStatus().toString()))
                .andExpect(jsonPath("$.successCode").value(SuccessResponseStatusType.READ_ORDER.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_UserIdIsEmptyForGetOrderDetailById() throws Exception {
        String uri = GET_ORDER_URI.replace("{orderId}", ORDER_DETAILS_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, "")
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_ID.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_ID.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_OrderIdIsEmptyForGetOrderDetailById() throws Exception {
        String uri = GET_ORDER_URI.replace("{orderId}", " ");
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_ORDER_ID.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_ORDER_ID.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidOrderIdIsProvidedForGetOrderDetailById() throws Exception {
        doThrow(new InvalidOrderException("ERROR")).when(orderService).getOrderById(ORDER_DETAILS_ID);
        String uri = GET_ORDER_URI.replace("{orderId}", ORDER_DETAILS_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_ORDER_ID.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_ORDER_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetOrderDetailByIdIsFailed() throws Exception {
        doThrow(new KupiOrderPaymentException("ERROR")).when(orderService).getOrderById(ORDER_DETAILS_ID);
        String uri = GET_ORDER_URI.replace("{orderId}", ORDER_DETAILS_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_GetAllOrderDetailsSuccessfully() throws Exception {
        OrderDetails orderDetails = getSampleOrderDetails();
        List<OrderDetails> orderDetailsList = new ArrayList<>();
        orderDetailsList.add(orderDetails);
        when(orderService.getAllOrders(USER_ID)).thenReturn(orderDetailsList);
        mockMvc.perform(MockMvcRequestBuilders.get(Get_All_ORDER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_ORDER_LIST.getMessage()))
                .andExpect(jsonPath("$.data.orderList[0].id").value(orderDetails.getId()))
                .andExpect(jsonPath("$.data.orderList[0].userId").value(orderDetails.getUserId()))
                .andExpect(jsonPath("$.data.orderList[0].status").value(orderDetails.getStatus().toString()))
                .andExpect(jsonPath("$.successCode").value(SuccessResponseStatusType.READ_ORDER_LIST.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_UserIdIsEmptyForGetAllOrderDetails() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(Get_All_ORDER_URI)
                        .header(USER_ID_HEADER, " ")
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_ID.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_ID.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetAllOrderDetailsIsFailed() throws Exception {
        doThrow(new KupiOrderPaymentException("ERROR")).when(orderService).getAllOrders(USER_ID);
        mockMvc.perform(MockMvcRequestBuilders.get(Get_All_ORDER_URI)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_CancelOrderDetailsSuccessfully() throws Exception {
        String uri = CANCEL_ORDER_URI.replace("{orderId}", ORDER_DETAILS_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.CANCEL_ORDER.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.successCode").value(SuccessResponseStatusType.CANCEL_ORDER.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_UserIdIsEmptyForCancelOrderDetails() throws Exception {
        String uri = CANCEL_ORDER_URI.replace("{orderId}", ORDER_DETAILS_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header(USER_ID_HEADER, " ")
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_USER_ID.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_USER_ID.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_OrderIdIsEmptyForCancelOrderDetails() throws Exception {
        String uri = CANCEL_ORDER_URI.replace("{orderId}", " ");
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_ORDER_ID.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_ORDER_ID.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_InvalidOrderStatusIsProvidedForCancelOrderDetails() throws Exception {
        doThrow(new InvalidOrderStateException("ERROR")).when(orderService).cancelOrder(ORDER_DETAILS_ID, USER_ID);
        String uri = CANCEL_ORDER_URI.replace("{orderId}", ORDER_DETAILS_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_ORDER_STATUS.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_ORDER_STATUS.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_CancelOrderDetailsIsFailed() throws Exception {
        doThrow(new KupiOrderPaymentException("ERROR")).when(orderService).cancelOrder(ORDER_DETAILS_ID, USER_ID);
        String uri = CANCEL_ORDER_URI.replace("{orderId}", ORDER_DETAILS_ID);
        mockMvc.perform(MockMvcRequestBuilders.delete(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    void Should_ReturnOk_When_GetBulkOrdersInfoSuccessfully() throws Exception {
        BulkOrderInfoRequestDto bulkOrderInfoRequestDto = getSampleBulkOrderInfoRequestDto();
        OrderDetails orderDetails = getSampleOrderDetails();
        List<OrderDetails> orderDetailsList = new ArrayList<>();
        orderDetailsList.add(orderDetails);
        when(orderService.getBulkOrderInfo(any(BulkOrderInfoRequestDto.class), any(Pageable.class))).thenReturn(orderDetailsList);
        String uri = Get_BULK_ORDER_INFO_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE));
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(bulkOrderInfoRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(SUCCESS_STATUS))
                .andExpect(jsonPath("$.message").value(SuccessResponseStatusType.READ_BULK_ORDER_INFO.getMessage()))
                .andExpect(jsonPath("$.data.orderList[0].id").value(orderDetails.getId()))
                .andExpect(jsonPath("$.data.orderList[0].userId").value(orderDetails.getUserId()))
                .andExpect(jsonPath("$.data.orderList[0].status").value(orderDetails.getStatus().toString()))
                .andExpect(jsonPath("$.successCode").value(SuccessResponseStatusType.READ_BULK_ORDER_INFO.getCode()));
    }

    @Test
    void Should_ReturnBadRequest_When_RequiredFieldsAreNotAvailableForGetBulkOrderInfo() throws Exception {
        BulkOrderInfoRequestDto bulkOrderInfoRequestDto = getSampleBulkOrderInfoRequestDto();
        List<String> dealIdLish = new ArrayList<>();
        dealIdLish.add(null);
        bulkOrderInfoRequestDto.setDealIdList(dealIdLish);
        String uri = Get_BULK_ORDER_INFO_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE));
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(bulkOrderInfoRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INVALID_LIST_INPUT_FIELD.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INVALID_LIST_INPUT_FIELD.getCode()));
    }

    @Test
    void Should_ReturnInternalServerError_When_GetBulkOrderInfoIsFailed() throws Exception {
        BulkOrderInfoRequestDto bulkOrderInfoRequestDto = getSampleBulkOrderInfoRequestDto();
        doThrow(new KupiOrderPaymentException("ERROR"))
                .when(orderService).getBulkOrderInfo(any(BulkOrderInfoRequestDto.class), any(Pageable.class));
        String uri = Get_BULK_ORDER_INFO_URI.replace("{page}", String.valueOf(PAGE))
                .replace("{size}", String.valueOf(SIZE));
        mockMvc.perform(MockMvcRequestBuilders.post(uri)
                        .header(USER_ID_HEADER, USER_ID)
                        .header(TIME_ZONE_HEADER, TIME_ZONE)
                        .content(bulkOrderInfoRequestDto.toJson())
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(APPLICATION_JSON_UTF_8))
                .andExpect(jsonPath("$.status").value(ERROR_STATUS))
                .andExpect(jsonPath("$.message").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.data", nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorResponseStatusType.INTERNAL_SERVER_ERROR.getCode()));
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

    /**
     * This method creates sample order create request dto
     *
     * @return OrderCreateRequestDto
     */
    private OrderCreateRequestDto getSampleOrderCreateRequestDto() {
        CouponCreateRequestDto couponCreateRequestDto = new CouponCreateRequestDto(COUPON_ID, DEAL_ID, USER_ID,
                COUPON_PRICE, DEAL_EXPIRE_DATE, COUPON_EXPIRE_DATE);
        List<CouponCreateRequestDto> couponCreateRequestDtoList = new ArrayList<>();
        couponCreateRequestDtoList.add(couponCreateRequestDto);
        return new OrderCreateRequestDto(CurrencyType.USD, couponCreateRequestDtoList);
    }

    /**
     * This method creates sample order update request dto
     *
     * @return OrderCreateRequestDto
     */
    private OrderUpdateRequestDto getSampleOrderUpdateRequestDto() {
        CouponUpdateRequestDto couponUpdateRequestDto = new CouponUpdateRequestDto();
        couponUpdateRequestDto.setCouponId(COUPON_ID);
        couponUpdateRequestDto.setCouponPrice(COUPON_PRICE);
        couponUpdateRequestDto.setDeleted(false);
        couponUpdateRequestDto.setMerchantId(USER_ID);
        couponUpdateRequestDto.setDealId(DEAL_ID);
        couponUpdateRequestDto.setCouponExpiryDate(COUPON_EXPIRE_DATE);
        couponUpdateRequestDto.setDealExpiryDate(DEAL_EXPIRE_DATE);
        couponUpdateRequestDto.setId(ORDER_DETAILS_ID);
        List<CouponUpdateRequestDto> couponCreateRequestDtoList = new ArrayList<>();
        couponCreateRequestDtoList.add(couponUpdateRequestDto);
        OrderUpdateRequestDto orderUpdateRequestDto = new OrderUpdateRequestDto();
        orderUpdateRequestDto.setId(ORDER_DETAILS_ID);
        orderUpdateRequestDto.setCoupons(couponCreateRequestDtoList);
        orderUpdateRequestDto.setCurrencyType(CurrencyType.USD);
        return orderUpdateRequestDto;
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
}