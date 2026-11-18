package com.kupi.cc.base.controller;

import com.kupi.cc.base.configuration.Translator;
import com.kupi.cc.base.domain.entity.OrderDetails;
import com.kupi.cc.base.domain.request.BulkOrderInfoRequestDto;
import com.kupi.cc.base.domain.request.OrderCreateRequestDto;
import com.kupi.cc.base.domain.request.OrderUpdateRequestDto;
import com.kupi.cc.base.domain.response.OrderListResponseDto;
import com.kupi.cc.base.domain.response.OrderResponseDto;
import com.kupi.cc.base.enums.ErrorResponseStatusType;
import com.kupi.cc.base.enums.SuccessResponseStatusType;
import com.kupi.cc.base.exception.InvalidCouponException;
import com.kupi.cc.base.exception.InvalidOrderException;
import com.kupi.cc.base.exception.InvalidOrderStateException;
import com.kupi.cc.base.exception.KupiOrderPaymentException;
import com.kupi.cc.base.service.OrderService;
import com.kupi.cc.base.wrapper.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * order and order items
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/order")
public class OrderController extends Controller {

    private final OrderService orderService;

    @Autowired
    public OrderController(Translator translator, OrderService orderService) {
        super(translator);
        this.orderService = orderService;
    }

    /**
     * This method place new order with items for given user details
     *
     * @param userId                user id
     * @param orderCreateRequestDto order create request DTO object
     * @return order create response DTO
     */
    @PostMapping(path = "", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> createOrder(
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @RequestHeader(name = HEADER_USER_ID) String userId,
            @RequestBody OrderCreateRequestDto orderCreateRequestDto) {
        try {
            if (isEmpty(userId)) {
                return getBadRequestError(ErrorResponseStatusType.INVALID_USER_ID);
            }
            if (!orderCreateRequestDto.isRequiredAvailable()) {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }

            OrderDetails order = new OrderDetails(orderCreateRequestDto, userId);
            OrderDetails savedOrder = orderService.placeOrder(order, userId);
            OrderResponseDto responseDto = new OrderResponseDto(savedOrder, timeZone);
            log.debug("Successfully saved the order: {} for user id: {}", order.getId(), userId);
            return getSuccessResponse(responseDto, SuccessResponseStatusType.CREATE_ORDER);

        } catch (InvalidOrderStateException e) {
            log.error("Failed to update existing order at current order-status for user id: {}", userId, e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_ORDER_STATUS);
        } catch (KupiOrderPaymentException e) {
            log.error("Saving an order was failed for userId: {}", userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This API update an order and coupons within it
     *
     * @param userId                    user id
     * @param orderUpdateRequestDto     order update request DTO
     * @return updated                  order response DTO
     */
    @PutMapping(path = "", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> updateOrder(
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @RequestHeader(name = HEADER_USER_ID) String userId,
            @RequestBody OrderUpdateRequestDto orderUpdateRequestDto) {
        String orderId = orderUpdateRequestDto.getId();

        try {
            if (isEmpty(userId)) {
                return getBadRequestError(ErrorResponseStatusType.INVALID_USER_ID);
            }
            if (!orderUpdateRequestDto.isRequiredAvailable()) {
                return getBadRequestError(ErrorResponseStatusType.MISSING_REQUIRED_FIELDS);
            }

            OrderDetails updatedOrder = orderService.updateOrder(orderUpdateRequestDto, userId);
            OrderResponseDto orderResponseDto = new OrderResponseDto(updatedOrder, timeZone);
            log.debug("Successfully updated the order: {} for user id: {}", orderId, userId);
            return getSuccessResponse(orderResponseDto, SuccessResponseStatusType.UPDATE_ORDER);

        } catch (InvalidCouponException e) {
            log.error("Failed to update order: {} due to invalid item-state for user id: {}", orderId, userId, e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_ITEM_ID);
        } catch (InvalidOrderStateException e) {
            log.error("Failed to update order: {} at current order-status for user id: {}", orderId, userId, e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_ORDER_STATUS);
        } catch (InvalidOrderException e) {
            log.error("Invalid order-id: {} to update order for user Id: {}", orderId, userId, e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_ORDER_ID);
        } catch (KupiOrderPaymentException e) {
            log.error("Failed to update order for order-id: {} for userId: {}.", orderId, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * Get order for given order-id
     *
     * @param userId  user id
     * @param orderId order id
     * @return order response DTO
     */
    @GetMapping(path = "/{orderId}", produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> getOrder(
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @RequestHeader(name = HEADER_USER_ID) String userId,
            @PathVariable String orderId) {
        try {
            if (isEmpty(userId)) {
                return getBadRequestError(ErrorResponseStatusType.INVALID_USER_ID);
            }
            if (isEmpty(orderId)) {
                return getBadRequestError(ErrorResponseStatusType.INVALID_ORDER_ID);
            }

            OrderDetails order = orderService.getOrderById(orderId);
            OrderResponseDto orderResponseDto = new OrderResponseDto(order, timeZone);
            log.debug("Successfully returned the order: {} for user id: {}", orderId, userId);
            return getSuccessResponse(orderResponseDto, SuccessResponseStatusType.READ_ORDER);

        } catch (InvalidOrderException e) {
            log.error("Invalid order-id: {} to return order details for user Id: {}", orderId, userId, e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_ORDER_ID);
        } catch (KupiOrderPaymentException e) {
            log.error("Failed to return order by order-id: {} for userId: {}.", orderId, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * get all orders for a given user id
     *
     * @param userId user id
     * @return list of order response DTOs
     */
    @GetMapping(path = "", produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> getAllOrders(
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @RequestHeader(name = HEADER_USER_ID) String userId) {
        try {
            if (isEmpty(userId)) {
                return getBadRequestError(ErrorResponseStatusType.INVALID_USER_ID);
            }

            List<OrderDetails> orderList = orderService.getAllOrders(userId);
            OrderListResponseDto orderListResponseDto = new OrderListResponseDto(orderList, timeZone);
            log.debug("Successfully returned the orders list for user id: {}", userId);
            return getSuccessResponse(orderListResponseDto, SuccessResponseStatusType.READ_ORDER_LIST);

        } catch (KupiOrderPaymentException e) {
            log.error("Failed to return orders for userId: {}.", userId, e);
            return getInternalServerError();
        }
    }

    /**
     * delete order by orderId
     *
     * @param userId  user id
     * @param orderId order id
     * @return empty response entity
     */
    @DeleteMapping(path = "/{orderId}", produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> cancelOrder(
            @RequestHeader(name = HEADER_USER_ID) String userId,
            @PathVariable String orderId) {
        try {
            if (isEmpty(userId)) {
                return getBadRequestError(ErrorResponseStatusType.INVALID_USER_ID);
            }
            if (isEmpty(orderId)) {
                return getBadRequestError(ErrorResponseStatusType.INVALID_ORDER_ID);
            }

            orderService.cancelOrder(orderId, userId);
            log.debug("Successfully cancelled the order: {} for user id: {}", orderId, userId);
            return getSuccessResponse(null, SuccessResponseStatusType.CANCEL_ORDER);

        } catch (InvalidOrderStateException e) {
            log.error("Failed to cancel order by order-id: {} at current order-status for userId: {}.", orderId, userId, e);
            return getBadRequestError(ErrorResponseStatusType.INVALID_ORDER_STATUS);
        } catch (KupiOrderPaymentException e) {
            log.error("Failed to delete order by order-id: {} for userId: {}.", orderId, userId, e);
            return getInternalServerError();
        }
    }

    /**
     * This method returns a list of orders filtered according to provided request body attributes
     *
     * @param timeZone                  timezone id
     * @param userId                    user id
     * @param page                      page
     * @param size                      size
     * @param bulkOrderInfoRequestDto   bulk order information request DTO
     * @return list of filtered orders
     */
    @PostMapping(path = "/bulk-info/{page}/{size}", consumes = APPLICATION_JSON_UTF_8, produces = APPLICATION_JSON_UTF_8)
    public ResponseEntity<ResponseWrapper> getBulkInfo(
            @RequestHeader(name = TIME_ZONE_HEADER) String timeZone,
            @RequestHeader(name = HEADER_USER_ID) String userId,
            @Min(DEFAULT_PAGE) @PathVariable("page") int page,
            @Positive @Max(PAGE_MAX_SIZE) @PathVariable("size") int size,
            @RequestBody BulkOrderInfoRequestDto bulkOrderInfoRequestDto) {
        try {
            if (!bulkOrderInfoRequestDto.isRequiredAvailable()) {
                return getBadRequestError(ErrorResponseStatusType.INVALID_LIST_INPUT_FIELD);
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by(DEFAULT_SORT).descending());
            List<OrderDetails> bulkOrderInfoDetails = orderService.getBulkOrderInfo(bulkOrderInfoRequestDto, pageable);
            OrderListResponseDto bulkOrderInfoResponseDto = new OrderListResponseDto(bulkOrderInfoDetails, timeZone);
            log.debug("Successfully returned the ");
            return getSuccessResponse(bulkOrderInfoResponseDto, SuccessResponseStatusType.READ_BULK_ORDER_INFO);

        } catch (KupiOrderPaymentException e) {
            log.error("Failed to return bulk order information for userId: {}.", userId, e);
            return getInternalServerError();
        }
    }
}
