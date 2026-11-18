package com.kupi.notification.util;


import org.springframework.stereotype.Component;

import java.util.TimeZone;

/**
 * This class is used to validate fields.
 */
@Component
public class Validator {

    /**
     * This method checks the validity of the time zone id.
     *
     * @param timeZoneId timeZoneId
     * @return true/ false
     */
    public boolean isValidTimeZoneId(String timeZoneId) {
        if (timeZoneId != null) {
            String[] timeZoneIds = TimeZone.getAvailableIDs();
            for (String t : timeZoneIds) {
                if (t.equalsIgnoreCase(timeZoneId.trim())) {
                    return true;
                }
            }
        }
        return false;
    }
}

