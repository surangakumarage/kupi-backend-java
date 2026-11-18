package com.kupi.cc.base.enums;


import lombok.Getter;

/**
 * Success Response Status Type
 */
@Getter
public enum SuccessResponseStatusType {

    UPDATE_USER(2000, "Successfully updated the approval status."),
    READ_BRAND_LIST(2001, "Successfully returned the brand list."),
    READ_CATEGORY_LIST(2002, "Successfully returned the category list."),
    CREATE_CATEGORY(2003, "Successfully created the category."),
    CREATE_BRAND(2004, "Successfully created the brand."),
    CREATE_DEAL(2005, "Successfully created the deal."),
    DELETE_DEAL(2006, "Successfully deleted the deal."),
    APPROVE_DEAL(2007, "Successfully approved the deal."),
    READ_DEAL_LIST(2008, "Successfully returned the deal list."),
    READ_DEAL(2009, "Successfully returned the deal."),
    READ_MERCHANT_LIST(2010, "Successfully returned the merchant list."),
    CREATE_CATEGORIES_BRANDS_FOR_MERCHANT(2011, "Successfully created mapping of categories and brands."),
    READ_CATEGORIES_BRANDS_FOR_MERCHANT(2012, "Successfully returned mapping of categories and brands."),
    CREATE_CATEGORIES_FOR_MERCHANT(2013, "Successfully created the categories for merchant."),
    READ_BRAND_DETAIL(2014, "Successfully returned the brand detail."),
    READ_CATEGORY_DETAIL(2015, "Successfully returned the category detail."),
    UPDATE_CATEGORIES_BRANDS_FOR_MERCHANT(2016, "Successfully updated mapping of categories and brands."),
    CREATE_REQUEST_A_DEAL(2017, "Successfully created the request a deal."),
    READ_REQUEST_DEAL_LIST(2018, "Successfully returned the request a deal list."),
    READ_REQUEST_A_DEAL(2019, "Successfully returned the request a deal groups list."),
    READ_REQUEST_DEAL_DETAIL(2020, "Successfully returned the request a deal detail."),
    CREATE_OFFER_TYPE(2021, "Successfully created the offer type."),
    READ_OFFER_TYPE_LIST(2022, "Successfully returned the offer type list."),
    UPDATE_OFFER_TYPE(2023, "Successfully updated the offer type."),
    UPDATE_CATEGORY(2024, "Successfully updated the category."),
    UPDATE_BRAND(2025, "Successfully updated the brand."),
    READ_CATEGORIES_FOR_MERCHANT(2026, "Successfully returned the category list for merchant."),
    READ_BRAND_FOR_MERCHANT(2027, "Successfully returned the brand list for merchant."),
    REQUEST_A_DEAL_COMBINATION_SUMMARY(2028, "Successfully returned the summary of the combination."),
    READ_DEAL_VIEW_GRAPH(2029, "Successfully returned top 10 list of deal's views."),
    READ_DEAL_VIEW_LIST(2030, "Successfully returned deal's views list."),
    UPDATE_DEAL(2031, "Successfully updated the deal."),
    DELETE_BRAND(2032, "Successfully deleted the brand."),
    CATEGORY_DELETE_SUCCESSFUL(2033, "Successfully deleted the category."),
    READ_RELATED_CATEGORIES(2034, "Successfully returned the related category list."),
    SUCCESSFULLY_RETURNED_SUMMARY(2035, "Successfully returned the summary."),
    CREATE_CREDIT_CARD_REQUEST(2036, "Successfully created the credit card request."),
    READ_CREDIT_CARD_REQUEST(2037, "Successfully returned the credit card request."),
    READ_CREDIT_CARD_REQUEST_LIST(2038, "Successfully returned the credit card request list."),
    DELETE_CREDIT_CARD_REQUEST(2039, "Successfully deleted the credit card request."),
    UPDATE_CREDIT_CARD_REQUEST(2040, "Successfully updated the credit card request."),
    READ_REQUEST_A_CREDIT_CARD_GROUP(2041, "Successfully returned the request a credit card groups list."),
    CATEGORY_REACH_LIST(2042, "Successfully returned category views list."),
    BANK_LIST(2043, "Successfully returned the banks list."),
    CREATE_SUBSCRIPTION_PLAN(2044, "Successfully created the subscription plan."),
    READ_SUBSCRIPTION_PLAN(2045, "Successfully returned the subscription plan by id."),
    READ_SUBSCRIPTION_PLAN_LIST(2046, "Successfully returned the subscription plan list."),
    UPDATE_SUBSCRIPTION_PLAN(2047, "Successfully updated the subscription plan."),
    UPDATE_SUBSCRIPTION_PLAN_STATUS(2048, "Successfully updated the status of subscription plan."),
    DELETE_SUBSCRIPTION_PLAN(2049, "Successfully deleted the subscription plan."),
    SUBSCRIBE_PLAN_USER(2052, "Successfully returned users list."),
    USER_SUBSCRIPTION_PLAN(2053, "Successfully returned user's subscription plan."),
    DELETE_OFFER_TYPE(2054, "Successfully deleted the offer type."),
    DEALS_OF_THE_DAY_EVENT_CREATED(2055, "Successfully created deals of the day event."),
    DEALS_OF_THE_DAY_EVENT_DELETED(2056, "Successfully deleted deals of the day event."),
    DEALS_OF_THE_DAY_EVENT_UPDATED(2057, "Successfully updated deals of the day event."),
    READ_DEALS_OF_THE_DAY(2058, "Successfully returned deals of the day event."),
    DEALS_OF_THE_DAY_EVENT_SUMMARY(2059, "Successfully returned deals of the day event summary."),
    UPCOMING_DEALS_OF_THE_DAY_EVENTS(2060, "Successfully returned upcoming deals of the day events."),
    REJECT_DEAL(2061, "Successfully rejected the deal."),
    SUBSCRIPTION_SUMMARY(2062, "Successfully returned subscription plan summary."),
    RETURNED_DEALS_BY_MERCHANT_IDS(2063, "Successfully returned deals by merchant id(s)."),
    READ_EXPIRED_CATEGORY_LIST(2064, "Successfully returned the expired category list."),
    FAVOURITE_CATEGORY_ADDED(2065, "Successfully added favorite category."),
    FAVOURITE_CATEGORY_REMOVED(2066, "Successfully removed favorite category."),
    SUCCESSFULLY_RETURNED_COUPON_STAT(2067, "Successfully returned coupon statistics."),
    SUCCESSFULLY_REDEEMED_COUPON(2068, "Successfully redeemed coupon."),
    UPLOAD_COUPONS(2069, "Successfully uploaded the coupons."),
    SUCCESSFULLY_RETURNED_COUPONS(2070, "Successfully returned coupon list."),
    DEAL_COUPON_STATUS_ACTIVE(2071, "Coupon status activated for deal"),
    DEAL_COUPON_STATUS_INACTIVE(2072, "Coupon status inactivated for deal"),
    COUPON_IS_ACTIVE(2073, "Coupon status activated"),
    COUPON_IS_INACTIVE(2074, "Coupon status inactivated"),
    READ_USER_COUPON_HISTORY(2075, "Successfully returned the coupon history list by user."),
    COUPON_PURCHASE_STATUS_UPDATE(2076, "Coupon purchase status updated."),
    SUBSCRIBE_REQUEST_VALIDATED(2085, "Valid request to subscribe."),
    SUBSCRIBE_PLAN(2086, "Successfully subscribed for subscription plan."),
    UNSUBSCRIBE_PLAN(2087, "Successfully un-subscribed the subscription plan."),
    READ_COUPON_UPLOAD_FILE_NAMES(2088, "Successfully returned coupon uploaded file names."),
    READ_COUPON_UPLOAD_FILE(2089, "Successfully returned the coupon upload details for the file."),
    READ_DEAL_SUMMARY_FOR_MERCHANT(2090, "Successfully returned the deal summary for merchant"),
    CREATE_MOBILE_USER_SUBSCRIPTION_PLAN(2091, "Successfully created the mobile users subscription plan."),
    SUCCESSFULLY_RETURNED_COUPON_VALIDITY(2092, "Successfully returned coupon validity."),
    SUCCESSFULLY_RETURNED_USER_SUBSCRIPTION_PLAN_LIST(2093, "Successfully returned user subscription plan list."),
    SUCCESSFULLY_RETURNED_STRIPE_SESSION_URL(2094, "Successfully returned the stripe session url."),
    SUCCESSFULLY_UPDATED_USER_SUBSCRIPTION_PLAN(2095, "Successfully updated user subscription plan."),
    SUCCESSFULLY_DEACTIVATED_USER_SUBSCRIPTION_PLAN(2096, "Successfully deactivated user subscription plan."),
    SUCCESSFULLY_RETURNED_STRIPE_SESSION_DETAILS(2097, "Successfully returned stripe session details."),
    FREE_PLAN_ACTIVATED(2098, "Free plan activated successfully."),
    SUCCESSFULLY_RETURNED_USER_SUBSCRIPTION_PLAN_DETAIL(2099, "Successfully returned user subscription plan detail."),
    SUCCESSFULLY_ACTIVATED_USER_SUBSCRIPTION_PLAN(2100, "Successfully activated user subscription plan."),
    SUCCESSFULLY_DELETED_USER_SUBSCRIPTION_PLAN(2101, "Successfully deleted user subscription plan."),
    ASSIGNED_DEFAULT_SUBSCRIPTION(2102, "Successfully assigned default subscription."),
    CREATE_PROMO_TYPE(2103, "Successfully created promo type."),
    UPDATE_PROMO_TYPE(2104, "Successfully updated promo type."),
    READ_PROMO_TYPES(2105, "Successfully retrieved promo types."),
    TRACK_DEAL_VIEW(2106, "Successfully tracked deal view."),
    PUBLISH_DEAL(2107, "Successfully published the deal."),
    UNPUBLISH_DEAL(2108, "Successfully unpublished the deal."),
    READ_BULK_MERCHANT_CATEGORIES(2109, "Successfully returned bulk merchant categories."),
    TOTAL_SAVINGS_ALL(2110, "Successfully returned total savings for user."),
    FAVOURITE_DEAL_ADDED(2111, "Successfully added favorite deal."),
    FAVOURITE_DEAL_REMOVED(2112, "Successfully removed favorite deal."),
    READ_CATEGORY_DEAL_COUNTS(2113, "Successfully returned the deal counts for categories."),
    SUCCESSFULLY_RETURNED(2114, "Returned successfully"),
    SUCCESSFULLY_RETURNED_DEAL_VALIDITY_PERIODS(2115,"Successfully returned deal validity periods.");


    private final int code;
    private final String message;

    SuccessResponseStatusType(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Success code covert into string to read display message from success property file
     *
     * @param successCode successCode
     * @return string code
     */
    public String getCodeString(int successCode) {
        return Integer.toString(successCode);
    }
}
