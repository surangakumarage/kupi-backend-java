package com.kupi.cc.base.enums;

import lombok.Getter;

/**
 * Enum to contain google analytics report query attributes
 */
@Getter
public enum ReportQueryAttribute {
    EVENT_COUNT_METRIC("eventCount"),
    MERCHANT_DEAL_ID_DIMENSION("customEvent:merchant_deal_id"),
    BANK_DEAL_ID_DIMENSION("customEvent:bank_deal_id"),
    MERCHANT_ID_DIMENSION("customEvent:merchant_id"),
    BANK_ID_DIMENSION("customEvent:bank_id"),
    CATEGORY_ID_DIMENSION("customEvent:category_id");

    private final String queryAttribute;

    ReportQueryAttribute(String queryAttribute) {
        this.queryAttribute = queryAttribute;
    }
}
