package com.kupi.cc.base.enums;

import lombok.Getter;

/**
 * Error response status type
 */
@Getter
public enum ErrorResponseStatusType {

    INTERNAL_SERVER_ERROR(5000, "Internal server error."),
    MISSING_REQUIRED_FIELDS(4000, "Required fields are missing."),
    INVALID_EXPIRY_DATE(4001, "Invalid expiry date."),
    INVALID_CATEGORY_ID(4002, "Invalid category id."),
    INVALID_BRAND_ID(4003, "Invalid brand id."),
    INVALID_VALID_FROM_DATE(4004, "Invalid validFrom date."),
    INVALID_EXPIRED_ON_DATE(4005, "Invalid expiredOn date."),
    INVALID_DEAL_ID(4006, "Invalid deal id."),
    INVALID_TIMEZONE(4100, "Invalid time zone."),
    INVALID_MERCHANT_ID(4101, "Invalid merchant Id."),
    INVALID_PRICE(4007, "Invalid price/ amount."),
    INVALID_REQUEST_DEAL_ID(4008, "Invalid request a deal Id."),
    INVALID_OFFER_TYPE_ID(4009, "Invalid offerType id."),
    INVALID_DATE_OPTION(4010, "Invalid date option."),
    INVALID_DATE_RANGE(4011, "Start date should be less than or equal to end date."),
    UNSUPPORTED_OPTION_FOR_DATE_RANGE(4012, "Unsupported option for date range."),
    INVALID_NOTIFICATION_TEMPLATE_TYPE(4013, "Invalid notification template type."),
    INACTIVE_MERCHANT_FOR_CREATE_DEAL(4014, "Invalid/ Inactive merchant cannot create any deals."),
    UNSUPPORTED_DEAL_UPDATE(4015, "Approved Deals cannot be updated."),
    EXISTING_CATEGORY_NAME(4016, "Existing Category Name."),
    EXISTING_BRAND_NAME(4017, "Existing Brand Name."),
    UNSUPPORTED_BRAND_DELETE(4018, "The band can not be deleted because it is already mapped with merchants."),
    UNSUPPORTED_CATEGORY_DELETE(4019, "The category can not be deleted because it is already mapped with merchants or another category."),
    INVALID_CATEGORY_FOR_RELATED_CATEGORIES(4020, "Normal category is not allowed to have related categories."),
    INVALID_USER_ROLE(4021, "Invalid user role."),
    INVALID_CREDIT_CARD_REQUEST_ID(4022, "Invalid Credit card request id."),
    INVALID_BANK_ID(4023, "Invalid bank id."),
    INVALID_MOBILE_NUMBER(4024, "Invalid Mobile Number."),
    INVALID_EMAIL(4025, "Invalid Email."),
    INVALID_NIC(4026, "Invalid NIC."),
    INVALID_USER_OR_BANK(4027, "Invalid Bank or User."),
    UNSUPPORTED_ROLE_TYPE_ACTION(4028, "This user type cannot perform this action."),
    INACTIVE_MERCHANT(4029, "Inactive merchant."),
    BUSINESS_PROFILE_NOT_FOUND(4030, "Approved business profile not found."),
    INVALID_USER_ID_OR_TYPE(4031, "Invalid userId or userType."),
    UNSUPPORTED_USER_TYPE_ACTION(4032, "Unsupported user type for the action."),
    INVALID_DEDUCTION_AMOUNT(4033, "Invalid deduction amount."),
    INVALID_DEDUCTION_PERCENTAGE(4034, "Invalid deduction percentage."),
    INVALID_START_END_DATE(4035, "Invalid start date or expire date."),
    EXISTING_OFFER_TYPE_NAME(4036, "Offer type name is already existing."),
    DEAL_DATES_DOESNT_MATCH_SEASONAL_CATEGORIES(4037, "Seasonal category(s) date range doesn't " +
            "match with deal date range."),
    EXPIRED_DEAL_APPROVAL(4038, "Expired deal cannot be approved."),
    EXISTING_SUBSCRIPTION_PLAN_NAME(4039, "Existing subscription plan title."),
    INVALID_SUBSCRIPTION_PLAN(4040, "Invalid subscription id."),
    INVALID_AMOUNT(4041, "Invalid amount for the subscription plan."),
    UNSUPPORTED_SUBSCRIPTION_USER_TYPE(4042, "Unsupported user type to subscribe the plan."),
    ALREADY_SUBSCRIBE_USER(4043, "Already subscribed user or subscription is yet to expire."),
    UNSUPPORTED_SUBSCRIPTION_PLAN_DELETE(4044, "This plan has subscribers. Hence this plan can not be deleted."),
    UNSUPPORTED_UNSUBSCRIBE_USER_TYPE(4045, "Unsupported user type to unsubscribe the plan."),
    INACTIVE_SUBSCRIPTION_PLAN(4046, "Inactive subscription plan."),
    UNSUPPORTED_OFFER_TYPE_DELETE(4047, "This offer type has Deal Requests. Hence this offer type can not be deleted."),
    EVENT_ALREADY_EXIST(4048, "An event is already existing for the date."),
    INVALID_EVENT_ID(4049, "Invalid deals of the day eventId."),
    DEALS_PER_DOD_EVENT_EXCEEDED(4050, "Deals per DOD event exceeded the subscription plan limit."),
    SUBSCRIPTION_PLAN_NOT_FOUND(4051, "Subscription plan not found."),
    INACTIVE_DEAL_ID(4052, "Deal not approved or inactive."),
    INVALID_NO_OF_DEALS_PER_MONTH(4053, "Invalid amount for no of deal per month."),
    INVALID_NO_OF_DOD_EVENTS_PER_MONTH(4054, "Invalid amount for no of deal of the day events per month."),
    INVALID_NO_OF_DOD_PER_EVENT(4055, "Invalid amount for no of deals of the day per event."),
    DEALS_PER_DOD_EXCEEDED(4056, "Deals of the day slot is not available."),
    DOD_EVENTS_PER_PLAN_EXCEEDED(4057, "DOD event exceeded the subscription plan limit."),
    CHARACTER_LIMIT_EXCEEDED_FOR_PLAN(4058, "Maximum character limit is exceeded for Subtitle," +
            " Description or Terms & Conditions"),
    MAX_USER_ID_LIMIT_EXCEEDED(4059, "Maximum number of userIds exceeded."),
    DEALS_PER_MONTH_LIMIT_EXCEEDED(4060, "Maximum number of deals per month limit exceeded."),
    SUBSCRIPTION_PLAN_NOT_FOUND_FOR_USER(4061, "Subscription plan not found for the user."),
    COUPON_NOT_FOUND(4062, "Coupon not found."),
    INVALID_TIMESTAMP(4063, "Invalid timestamp"),
    ADD_COUPON_FOR_RECENTLY_EXPIRING_DEAL(4064, "Deal is expired or about to expire."),
    INVALID_COUPON_STATUS(4065, "Invalid coupon status"),
    INVALID_COUPON_ID(4066, "Invalid coupon id"),
    INACTIVE_COUPON(4067, "Coupon is inactive."),
    REDEEMED_COUPON(4068, "Coupon already redeemed."),
    EXPIRED_DEAL(4069, "Deal is expired."),
    MERCHANT_DOESNT_MATCH(4070, "MerchantId doesn't match."),
    COUPON_ALREADY_PURCHASED(4071, "User already purchased a coupon."),
    NOT_FREE_SUBSCRIPTION(4072, "Requested plan is not free."),
    INVALID_ACCESS_KEY(4073, "Invalid access key."),
    COUPON_UPLOAD_FAILED(4074, "Coupon upload process failed."),
    PURCHASED_COUPON_CANT_UPDATED(4075, "Purchased coupon can't be updated."),
    DEAL_IS_NOT_AVAILABLE(4076, "Coupons are disabled for deal."),
    COUPON_NOT_CLAIMED_BY_USER(4077, "Coupon is not claimed by user."),
    STRIPE_SESSION_CREATION_FAILED(4078, "Stripe session creation failed."),
    USER_SUBSCRIPTION_EXPIRED(4079, "User subscription has expired."),
    INVALID_SUBSCRIPTION_PLAN_ID(4080, "Invalid subscription plan id."),
    COUPON_LIMIT_EXCEEDED(4081, "User has exceeded the allowed coupon limit."),
    USER_SUBSCRIPTION_NOT_FOUND(4082, "User subscription not found."),
    PLAN_HAS_ACTIVE_USERS(4083, "Cannot delete.Plan has active users."),
    EXISTING_PROMO_TYPE_NAME(4084, "Existing Promo Type Name."),
    CANNOT_REPUBLISH_UNPUBLISHED_DEAL(4085, "Deactivate deal cannot be re publish."),
    INVALID_DEAL_STATUS_FOR_DOD(4086, "Expired, deactivated, or draft deals cannot be used for deals of the day."),
    INVALID_SORT_PARAMETER(4087, "Invalid sort parameter. Valid sortBy: purchasedOn, expiryDate, redeemedOn, createdAt, price. Valid sortOrder: ASC, DESC."),;


    private final int code;
    private final String message;

    ErrorResponseStatusType(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Error code covert into string to read display message from error property file
     *
     * @param errorCode errorCode
     * @return errorCode as string
     */
    public static String getCodeString(int errorCode) {
        return Integer.toString(errorCode);
    }

}
