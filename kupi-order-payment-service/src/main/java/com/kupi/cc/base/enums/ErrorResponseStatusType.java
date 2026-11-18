package com.kupi.cc.base.enums;

import lombok.Getter;

/**
 * Error response status type
 */
@Getter
public enum ErrorResponseStatusType {

    INTERNAL_SERVER_ERROR(5000, "Internal server error."),
    MISSING_REQUIRED_FIELDS(4000, "Required fields are missing"),

    INVALID_ADDRESS_ID(4001, "Invalid address id."),

    INVALID_ORDER_ID(4011, "Invalid order id."),
    INVALID_ORDER_STATUS(4012, "Invalid order status."),
    ORDER_ALREADY_CREATED(4013, "Order already created."),
    INVALID_LIST_INPUT_FIELD(4014, "Invalid input field."),
    INVALID_ITEM_ID(4015, "Invalid item id."),

    INVALID_USER_ID(4021, "Invalid user id."),

    INVALID_PAYMENT_BROKER(4031, "Invalid payment broker id."),
    UNSUPPORTED_PAYMENT_BROKER(4032, "Unsupported payment broker name found."),

    INACTIVE_SUBSCRIPTION_PLAN(4051, "Inactive subscription plan."),
    INVALID_USER_ROLE(4052, "Invalid user role."),
    ALREADY_SUBSCRIBE_USER(4053, "Already subscribed user or subscription is yet to expire."),
    INVALID_SUBSCRIPTION_REQUEST(4054, "Invalid subscription request."),
    INVALID_SUBSCRIPTION_PLAN(4055, "Invalid subscription plan id."),
    INVALID_TEMPORARY_SUBSCRIPTION_ID(4056, "Invalid temporary subscription id."),
    INVALID_MERCHANT_ID(4057, "Invalid merchant id."),
    UNSUPPORTED_SUBSCRIPTION_USER_TYPE(4058, "Unsupported user role type for subscription."),
    UNPAID_SUBSCRIPTION(4059, "Payment not captured for subscription."),
    EXISTING_SUBSCRIPTION_PLAN(4060, "User has already subscribed to a plan."),
    SUBSCRIPTION_NOT_TEMPORARY(4061, "Subscription plan is not temporary."),
    COUPON_NOT_FOUND(4062, "Coupon not found."),
    NOT_FREE_SUBSCRIPTION(4072, "Requested plan is not free."),
    COUPON_ALREADY_PURCHASED(4071, "User already purchased a coupon."),
    ALREADY_CAPTURED_ORDER(4081, "Payment for this order is already captured."),
    INCORRECT_OR_EXPIRED_PAYMENT_TOKEN(4082, "Payment token used for this request might be invalid or already used."),
    DEAL_IS_NOT_AVAILABLE(4083, "Coupons are disabled for deal.");



    private final int code;
    private final String message;

    ErrorResponseStatusType(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Return String value of code to match with resource-message property key
     *
     * @param code code
     * @return code
     */
    public static String getCodeString(int code) {
        return Integer.toString(code);
    }
}
