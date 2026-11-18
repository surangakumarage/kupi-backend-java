package com.kupi.cc.base.enums;


import lombok.Getter;

/**
 * Success Response Status Type
 */
@Getter
public enum SuccessResponseStatusType {

    UPDATE_USER(2000, "Successfully updated."),
    SAVE_ADDRESS(2001, "Successfully saved the address."),
    UPDATE_ADDRESS(2002, "Successfully updated the address."),
    DELETE_ADDRESS(2003, "Successfully deleted the address."),
    READ_ADDRESS(2004, "Successfully returned the address."),
    READ_ADDRESS_LIST(2005, "Successfully returned the address list."),

    CREATE_ORDER(2011, "Successfully created the order."),
    READ_ORDER(2012, "Successfully returned the order"),
    READ_ORDER_LIST(2013, "Successfully returned the order list."),
    CANCEL_ORDER(2014, "Successfully cancelled the order."),
    UPDATE_ORDER(2015, "Successfully updated the order."),
    READ_BULK_ORDER_INFO(2016, "Successfully returned bulk order information."),

    SAVE_CUSTOMER_DETAILS(2021, "Successfully saved customer detail."),
    UPDATE_CUSTOMER_DETAIL(2022, "Successfully update the customer detail."),
    READ_CUSTOMER_DETAIL(2023, "Successfully returned the customer detail."),

    SAVE_PAYMENT_BROKER(2031, "Successfully saved the payment broker."),
    UPDATE_PAYMENT_BROKER(2032, "Successfully updated the payment broker."),
    READ_PAYMENT_BROKER(2033, "Successfully read the payment broker."),
    SUBSCRIPTION_PAYMENT_INITIATED(2041, "Subscription payment request initiated"),
    SUBSCRIBE_PLAN(2042, "Successfully subscribed for subscription plan."),
    UNSUBSCRIBE_PLAN(2043, "Successfully un-subscribed the subscription plan."),
    COUPON_PAYMENT_INITIATED(2044, "Coupon payment request initiated."),
    COUPON_PAYMENT_CONFIRMED(2045, "Coupon payment confirmed."),
    READ_SUBSCRIBE_PAYMENT_LIST(2046,"Successfully returned the subscription plan order list." ),
    READ_SUBSCRIBE_PAYMENT_COUNT(2047,"Successfully returned the subscription plan count." );


    private final int code;
    private final String message;

    SuccessResponseStatusType(int code, String message) {
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
        return String.valueOf(code);
    }
}
