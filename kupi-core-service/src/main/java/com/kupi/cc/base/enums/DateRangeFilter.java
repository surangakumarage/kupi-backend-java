package com.kupi.cc.base.enums;

import java.util.Calendar;

/**
 * Date range filter options for payment history
 */
public enum DateRangeFilter {
    ALL_TIME("ALL_TIME", 0),                    // No filter
    LAST_7_DAYS("LAST_7_DAYS", -7),            // Last 7 days
    LAST_30_DAYS("LAST_30_DAYS", -30),         // Last 30 days
    LAST_3_MONTHS("LAST_3_MONTHS", -90),       // Last 3 months
    LAST_6_MONTHS("LAST_6_MONTHS", -180),      // Last 6 months
    LAST_YEAR("LAST_YEAR", -365);              // Last year

    private final String displayName;
    private final int dayOffset;

    DateRangeFilter(String displayName, int dayOffset) {
        this.displayName = displayName;
        this.dayOffset = dayOffset;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDayOffset() {
        return dayOffset;
    }

    /**
     * Get the start date timestamp (milliseconds) based on the filter
     * Returns 0 for ALL_TIME (no filtering), otherwise returns milliseconds from the past
     */
    public long getStartDateTimestamp() {
        if (dayOffset == 0) {
            return 0;  // ALL_TIME - no filter
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, dayOffset);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    /**
     * Parse string to DateRangeFilter enum
     */
    public static DateRangeFilter fromString(String value) {
        if (value == null || value.isEmpty()) {
            return ALL_TIME;
        }
        try {
            return DateRangeFilter.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ALL_TIME;  // Default to ALL_TIME if invalid
        }
    }
}
