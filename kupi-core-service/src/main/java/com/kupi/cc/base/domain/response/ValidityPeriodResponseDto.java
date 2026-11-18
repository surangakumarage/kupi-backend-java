
package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.request.ValidityPeriodDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Validity Period Response DTO
 * Contains formatted date and time information for a validity period
 */
@Getter
@Setter
@NoArgsConstructor
public class ValidityPeriodResponseDto extends ResponseDto {

    private static final String DATE_WITH_DAY_FORMAT = "MMM d, yyyy (EEE)"; // Eg: Feb 3, 2026 (Mon)
    private static final String TIME_FORMAT = "h:mm a"; // Eg: 4:00 PM

    private String date; // Formatted date with day of week, e.g., "Feb 3, 2026 (Mon)"
    private String startTime; // Formatted start time, e.g., "4:00 PM"
    private String endTime; // Formatted end time, e.g., "6:00 PM"
    private DateResponseDto dateResponse; // DateResponseDto for the date
//    private DateResponseDto startTimeResponse; // DateResponseDto for start time
//    private DateResponseDto endTimeResponse; // DateResponseDto for end time

    /**
     * Constructor that creates a formatted validity period response from ValidityPeriodDto
     *
     * @param validityPeriodDto the validity period DTO
     * @param timeZone          timezone for date formatting
     */
    public ValidityPeriodResponseDto(ValidityPeriodDto validityPeriodDto, String timeZone) {
        if (validityPeriodDto == null) {
            return;
        }

        try {
            // Parse the date string (format: yyyy-MM-dd)
            LocalDate localDate = LocalDate.parse(validityPeriodDto.getDate());
            ZoneId zoneId = ZoneId.of(timeZone);
            ZonedDateTime zonedDateTime = localDate.atStartOfDay(zoneId);

            // Format date with day of week: "Feb 3, 2026 (Mon)"
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_WITH_DAY_FORMAT, Locale.ENGLISH);
            this.date = zonedDateTime.format(dateFormatter);

            // Create DateResponseDto for the date (using start of day)
            long dateMillis = zonedDateTime.toInstant().toEpochMilli();
            this.dateResponse = new DateResponseDto(dateMillis, timeZone, new java.sql.Date(dateMillis));

            // Parse and format start time
            this.startTime = formatTime(validityPeriodDto.getStartTime(), validityPeriodDto.getDate(), timeZone);


            // Parse and format end time
            this.endTime = formatTime(validityPeriodDto.getEndTime(), validityPeriodDto.getDate(), timeZone);


        } catch (Exception e) {
            // If parsing fails, set basic values
            this.date = validityPeriodDto.getDate();
            this.startTime = validityPeriodDto.getStartTime();
            this.endTime = validityPeriodDto.getEndTime();
        }
    }

    /**
     * Format time string to "h:mm a" format (e.g., "4:00 PM")
     *
     * @param timeStr  time string in HH:mm format
     * @param dateStr  date string in yyyy-MM-dd format
     * @param timeZone timezone
     * @return formatted time string
     */
    private String formatTime(String timeStr, String dateStr, String timeZone) {
        try {
            // Parse the time (format: HH:mm)
            String[] timeParts = timeStr.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            // Create a calendar instance for formatting
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone(timeZone));
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            // Format using SimpleDateFormat
            SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
            sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
            return sdf.format(cal.getTime());
        } catch (Exception e) {
            // If parsing fails, return original time string
            return timeStr;
        }
    }

}