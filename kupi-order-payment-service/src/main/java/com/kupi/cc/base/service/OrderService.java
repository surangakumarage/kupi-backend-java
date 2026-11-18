package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.Coupon;
import com.kupi.cc.base.domain.entity.OrderDetails;
import com.kupi.cc.base.domain.request.BulkOrderInfoRequestDto;
import com.kupi.cc.base.domain.request.OrderUpdateRequestDto;
import com.kupi.cc.base.enums.CurrencyType;
import com.kupi.cc.base.enums.OrderStatus;
import com.kupi.cc.base.exception.InvalidOrderException;
import com.kupi.cc.base.exception.InvalidOrderStateException;
import com.kupi.cc.base.exception.KupiOrderPaymentException;
import com.kupi.cc.base.repository.OrderRepository;
import com.kupi.cc.base.util.DomainUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class OrderService {
    private static final String INVALID_ORDER_ID = "Invalid orderDetails id: ";
    private static final String FAILED_TO_RETURN_ORDER = "Returning orderDetails from database was failed.";

    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * save new order with coupon details
     * calculate tax and net total ???
     *
     * @param orderDetails      orderDetails
     * @param userId            user id
     * @return                  orderDetails with customer details
     */
    public OrderDetails placeOrder(OrderDetails orderDetails, String userId) {
        try {
            calculateOrderTotal(orderDetails);
            orderDetails.persistAuditor(userId);

            orderDetails = orderRepository.saveAndFlush(orderDetails);
            return orderDetails;
        } catch (DataAccessException | AssertionError e) {
            throw new KupiOrderPaymentException("Saving orderDetails into database was failed.", e);
        }
    }

    private void calculateOrderTotal(OrderDetails order) {
        var grossCouponTotal = order.getCoupons().stream()
                .filter(coupon -> !coupon.isDeleted())
                .map(Coupon::getCouponPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setGrossTotal(grossCouponTotal);
        order.setNetTotal(order.getGrossTotal());
    }

    /**
     * update order entity and coupons within it
     * @param orderUpdateRequestDto     order update request DTO
     * @param userId                    user id
     * @return                          updated order entity
     */
    public OrderDetails updateOrder(OrderUpdateRequestDto orderUpdateRequestDto, String userId) {
        try {
            var order = getOrderById(orderUpdateRequestDto.getId());
            if (isUpdatableOrderStatus(order.getStatus())) {
                order.update(orderUpdateRequestDto);
                calculateOrderTotal(order);
                order.updateAuditor(userId);

                orderRepository.saveAndFlush(order);
                return order;
            } else {
                throw new InvalidOrderStateException("OrderDetails: " + orderUpdateRequestDto.getId() + " cannot be updated while in state: "
                        + order.getStatus().toString());
            }
        } catch (DataAccessException | AssertionError e) {
            throw new KupiOrderPaymentException("Updating order into database was failed.", e);
        }
    }

    private boolean isUpdatableOrderStatus(OrderStatus status) {
        return status != null && status.equals(OrderStatus.CREATED);
    }

    /**
     * Get all orders for given user id
     *
     * @param userId user id
     * @return list of order entities
     */
    public List<OrderDetails> getAllOrders(String userId) {
        try {
            return orderRepository.findAllByUserId(userId);
        } catch (DataAccessException e) {
            throw new KupiOrderPaymentException(FAILED_TO_RETURN_ORDER, e);
        }
    }

    /**
     * get order for given order id
     *
     * @param orderId order id
     * @return order entity object
     */
    public OrderDetails getOrderById(String orderId) {
        try {
            return orderRepository.findById(orderId)
                    .orElseThrow(() -> new InvalidOrderException(INVALID_ORDER_ID + orderId));
        } catch (DataAccessException e) {
            throw new KupiOrderPaymentException(FAILED_TO_RETURN_ORDER, e);
        }
    }

    /**
     * mark the order as cancelled
     *
     * @param orderId   order id
     * @param userId    user id
     */
    public void cancelOrder(String orderId, String userId) {
        try {
            OrderDetails order = getOrderById(orderId);
            if (isUpdatableOrderStatus(order.getStatus())) {
                order.cancelOrder();
                order.updateAuditor(userId);
                orderRepository.save(order);
            } else {
                throw new InvalidOrderStateException("OrderDetails: " + orderId + " cannot be cancelled while in state: "
                        + order.getStatus().toString());
            }
        } catch (DataAccessException | AssertionError e) {
            throw new KupiOrderPaymentException(FAILED_TO_RETURN_ORDER, e);
        }
    }

    /**
     * get bulk list of orders filtered according to request DTO attributes
     *
     * @param requestDto    bulk orderDetails information request DTO
     * @param pageable      pageable
     * @return              list of filtered orders
     */
    public List<OrderDetails> getBulkOrderInfo(BulkOrderInfoRequestDto requestDto, Pageable pageable) {

        boolean customerDetailFilterProvided = isNonEmptyList(requestDto.getCustomerIdList());
        boolean currencyTypeFilterProvided = isNonEmptyList(requestDto.getCurrencyTypeList());
        boolean orderCreatedDateFilterProvided = requestDto.getCreatedDate() > 0;
        boolean orderCreatedDateRangeFilterProvided = requestDto.getCreatedDateRangeStart() > 0
                && requestDto.getCreatedDateRangeStart() <= requestDto.getCreatedDateRangeEnd();
        boolean dealIdFilterProvided = isNonEmptyList(requestDto.getDealIdList());
        boolean merchantIdFilterProvided = isNonEmptyList(requestDto.getMerchantIdList());
        boolean orderStatusFilterProvided = isNonEmptyList(requestDto.getOrderStatusList());
        boolean paymentBrokerFilterProvided = isNonEmptyList(requestDto.getPaymentBrokerIdList());

        try {
            return orderRepository.findAll(pageable).stream()
                    .filter(order -> filterOrderFromCustomerId(customerDetailFilterProvided, order, requestDto))
                    .filter(order -> filterOrderFromCurrencyType(currencyTypeFilterProvided, order, requestDto))
                    .filter(order -> filterOrderFromCreatedDate(orderCreatedDateFilterProvided, orderCreatedDateRangeFilterProvided, order, requestDto))
                    .filter(order -> filterOrderFromExpiryStatus(order, requestDto))
                    .filter(order -> filterOrderFromDealIds(dealIdFilterProvided, order, requestDto))
                    .filter(order -> filterOrderFromMerchantIds(merchantIdFilterProvided, order, requestDto))
                    .filter(order -> filterOrderFromOrderStatus(orderStatusFilterProvided, order, requestDto))
                    .filter(order -> filterOrderFromPaymentBroker(paymentBrokerFilterProvided, order, requestDto))
                    .collect(Collectors.toList());

        } catch (DataAccessException e) {
            throw new KupiOrderPaymentException("Returning orders from database was failed.", e);
        }
    }

    private boolean filterOrderFromExpiryStatus(OrderDetails orderDetails, BulkOrderInfoRequestDto requestDto) {
        if (orderDetails.isExpired()) {
            return requestDto.isIncludeExpired();
        } else {
            return requestDto.isIncludeNotExpired();
        }
    }

    private boolean filterOrderFromPaymentBroker(
            boolean paymentBrokerFilterProvided, OrderDetails orderDetails, BulkOrderInfoRequestDto requestDto) {

        if (paymentBrokerFilterProvided) {
            if (orderDetails.getPaymentBroker() != null) {
                String paymentBrokerId = orderDetails.getPaymentBroker().getId();
                return requestDto.getPaymentBrokerIdList().contains(paymentBrokerId);
            }
            return false;
        }
        return true;
    }


    private boolean filterOrderFromOrderStatus(
            boolean orderStatusFilterProvided, OrderDetails orderDetails, BulkOrderInfoRequestDto requestDto) {

        if (orderStatusFilterProvided) {
            OrderStatus orderStatus = orderDetails.getStatus();
            return requestDto.getOrderStatusList().contains(orderStatus);
        }
        return true;
    }

    private boolean filterOrderFromDealIds(
            boolean dealIdFilterProvided, OrderDetails orderDetails, BulkOrderInfoRequestDto requestDto) {

        if (dealIdFilterProvided) {
            return orderDetails.getCoupons().stream()
                    .map(coupon -> requestDto.getDealIdList().contains(coupon.getDealId()))
                    .reduce(false, Boolean::logicalOr);
        }
        return true;
    }

    private boolean filterOrderFromMerchantIds(
            boolean dealIdFilterProvided, OrderDetails orderDetails, BulkOrderInfoRequestDto requestDto) {

        if (dealIdFilterProvided) {
            return orderDetails.getCoupons().stream()
                    .map(coupon -> requestDto.getMerchantIdList().contains(coupon.getMerchantId()))
                    .reduce(false, Boolean::logicalOr);
        }
        return true;
    }

    private boolean filterOrderFromCreatedDate(
            boolean createdDateFilterProvided, boolean createdDateRangeFilterProvided,
            OrderDetails orderDetails, BulkOrderInfoRequestDto requestDto) {

        if (createdDateFilterProvided) {
            return DomainUtils.isSameDate(orderDetails.getCreatedDate(), requestDto.getCreatedDate());
        } else if (createdDateRangeFilterProvided) {
            return isWithinDatePeriod(orderDetails.getCreatedDate(), requestDto.getCreatedDateRangeStart(), requestDto.getCreatedDateRangeEnd());
        }
        return true;
    }

    private boolean filterOrderFromCurrencyType(
            boolean currencyTypeFilterProvided, OrderDetails orderDetails, BulkOrderInfoRequestDto requestDto) {

        if (currencyTypeFilterProvided) {
            CurrencyType currencyCode = orderDetails.getCurrencyType();
            return requestDto.getCurrencyTypeList().contains(currencyCode);
        }
        return true;
    }

    private boolean filterOrderFromCustomerId(
            boolean customerDetailFilterProvided, OrderDetails orderDetails, BulkOrderInfoRequestDto requestDto) {

        if (customerDetailFilterProvided) {
            String userId = orderDetails.getUserId();
            return requestDto.getCustomerIdList().contains(userId);
        }
        return true;
    }

    private boolean isWithinDatePeriod(long createdDate, long createdDateRangeStart, long createdDateRangeEnd) {
        return createdDate >= createdDateRangeStart && createdDate < createdDateRangeEnd;
    }

    private boolean isNonEmptyList(List<?> list) {
        return list != null && !list.isEmpty();
    }
}
