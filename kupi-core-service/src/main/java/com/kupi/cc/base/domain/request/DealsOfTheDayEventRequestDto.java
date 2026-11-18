package com.kupi.cc.base.domain.request;

import lombok.Getter;
import lombok.Setter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

/**
 * Deals of the day event request dto
 */
@Getter
@Setter
public class DealsOfTheDayEventRequestDto extends RequestDto {

    private long validOn;
    private Set<String> dealIds;
    private String eventId;


    @Override
    public boolean isRequiredAvailable() {
        return !dealIds.isEmpty();
    }

    /**
     * Checks if correct valid on.
     *
     * @return true/false
     */
    public boolean isValidValidOn(String timeZone) {
        try {
            ZoneId zoneId = ZoneId.of(timeZone);

            ZonedDateTime startOfToday = ZonedDateTime.now(zoneId)
                    .toLocalDate()
                    .atStartOfDay(zoneId);

            long startOfTodayMillis = startOfToday.toInstant().toEpochMilli();

            return validOn >= startOfTodayMillis;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if required fields are available for update.
     *
     * @return true/false
     */
    public boolean isRequiredAvailableForUpdate() {
        return isNonEmpty(eventId);
    }

    @Override
    public String toLogJson() {
        return toJson();
    }
}
