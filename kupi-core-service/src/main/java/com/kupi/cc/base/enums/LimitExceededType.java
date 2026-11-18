package com.kupi.cc.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Limit Exceeded Type
 */
@Getter
@AllArgsConstructor
public enum LimitExceededType {

    DEALS_PER_DOD(ErrorResponseStatusType.DEALS_PER_DOD_EXCEEDED),
    DEALS_PER_DOD_EVENT(ErrorResponseStatusType.DEALS_PER_DOD_EVENT_EXCEEDED),
    DOD_EVENTS_PER_PLAN(ErrorResponseStatusType.DOD_EVENTS_PER_PLAN_EXCEEDED),
    DEALS_PER_MONTH(ErrorResponseStatusType.DEALS_PER_MONTH_LIMIT_EXCEEDED);

    private final ErrorResponseStatusType errorResponseStatusType;
}
