package com.kupi.cc.location.enums;


import lombok.Getter;

/**
 * Success Response Status Type
 */
@Getter
public enum SuccessResponseStatusType {

    READ_BRANCH_LIST(2000, "Successfully returned the branch list."),
    BRANCH_LIST(2001, "Successfully returned the pending branch list."),
    APPROVED_BRANCH(2002, "Successfully approved the branch."),
    REJECTED_BRANCH(2003, "Successfully rejected the branch."),
    CREATE_BRANCH(2004, "Successfully created the branch."),
    DELETED_BRANCH(2005, "Successfully deleted the branch."),
    READ_BRANCH_DETAIL(2006, "Successfully returned the branch."),
    UPDATE_BRANCH(2007, "Successfully updated the branch."),
    VERIFIED(2008, "Successfully verified."),
    NOT_VERIFIED(2009, "Successfully removed verification."),
    READ_DEALS_BY_LOCATION(2010, "Successfully returned deals by location."),
    READ_DEALS_COUNT_BY_LOCATION(2011, "Successfully returned deals count by location."),
    READ_MERCHANTS_BY_LOCATION(2012, "Successfully returned merchants by location."),
    READ_FILTERED_DEALS_BY_LOCATION(2013, "Successfully returned filtered deals by location."),
    READ_NEARBY_ACTIVE_DEALS_COUNT(2014, "Successfully returned nearby active deals count.");

    private final String code;
    private final String message;

    SuccessResponseStatusType(int code, String message) {
        this.code = getCodeString(code);
        this.message = message;
    }

    /**
     * Return String value of code to match with resource-message property key
     *
     * @param code code
     * @return code
     */
    public String getCodeString(int code) {
        return String.valueOf(code);
    }
}
