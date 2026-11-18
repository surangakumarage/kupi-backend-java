package com.kupi.cc.base.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for parsing various date formats
 */
@Component
@Slf4j
public class DateParserUtil {

    // Supported date formats
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy h:mm:ss a"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy h:mm:ss a"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy H:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy H:mm:ss"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm:ss a"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy")
    );

    /**
     * Parse date string using multiple supported formats
     *
     * @param dateString the date string to parse
     * @param timeZone   the timezone to use
     * @return milliseconds since epoch
     * @throws IllegalArgumentException if date cannot be parsed
     */
    public long parseDateToMilliseconds(String dateString, String timeZone) {
        if (dateString == null || dateString.trim().isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be null or empty");
        }

        String trimmedDate = dateString.trim();
        log.debug("Attempting to parse date: {} with timezone: {}", trimmedDate, timeZone);

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                // First try parsing as LocalDateTime
                LocalDateTime localDateTime = LocalDateTime.parse(trimmedDate, formatter);
                long milliseconds = localDateTime.atZone(ZoneId.of(timeZone)).toInstant().toEpochMilli();
                log.debug("Successfully parsed date as LocalDateTime: {} using format: {} -> {}",
                        trimmedDate, formatter, milliseconds);
                return milliseconds;
            } catch (DateTimeParseException e1) {
                // If that fails, try parsing as LocalDate (for date-only formats like yyyy-MM-dd)
                try {
                    LocalDate localDate = LocalDate.parse(trimmedDate, formatter);
                    long milliseconds = localDate.atStartOfDay(ZoneId.of(timeZone)).toInstant().toEpochMilli();
                    log.debug("Successfully parsed date as LocalDate: {} using format: {} -> {}",
                            trimmedDate, formatter, milliseconds);
                    return milliseconds;
                } catch (DateTimeParseException e2) {
                    log.debug("Failed to parse with format {}: {}", formatter.toString(), e2.getMessage());
                    // Continue to next format
                }
            }
        }

        log.error("Failed to parse date string: {} with any supported format", trimmedDate);
        throw new IllegalArgumentException("Unable to parse date: " + trimmedDate +
                ". Supported formats: " + getSupportedFormats());
    }

    /**
     * Get list of supported date formats for error messages
     *
     * @return list of supported format strings
     */
    public List<String> getSupportedFormats() {
        return Arrays.asList(
                "dd-MM-yyyy hh:mm:ss a (e.g., 27-12-2025 10:15:55 AM)",
                "dd-MM-yyyy HH:mm:ss (e.g., 27-12-2025 10:15:55)",
                "dd/MM/yyyy hh:mm:ss a (e.g., 27/12/2025 10:15:55 AM)",
                "dd/MM/yyyy HH:mm:ss (e.g., 27/12/2025 10:15:55)",
                "yyyy-MM-dd HH:mm:ss (e.g., 2025-12-27 10:15:55)",
                "yyyy-MM-dd hh:mm:ss a (e.g., 2025-12-27 10:15:55 AM)",
                "dd-MM-yyyy (e.g., 27-12-2025)",
                "dd/MM/yyyy (e.g., 27/12/2025)",
                "yyyy-MM-dd (e.g., 2025-12-27)",
                "dd-MM-yyyy h:mm:ss a (e.g., 27-12-2025 1:15:55 AM)",
                "dd-MM-yyyy hh:mm a (e.g., 27-12-2025 10:15 AM)",
                "dd-MM-yyyy H:mm:ss (e.g., 27-12-2025 1:15:55)",
                "dd-MM-yyyy HH:mm (e.g., 27-12-2025 10:15)",
                "MM/dd/yyyy HH:mm (e.g., 10/11/2025 10:15)",
                "MM/dd/yyyy hh:mm a (e.g., 10/11/2025 10:15 AM)",
                "MM/dd/yyyy h:mm:ss a (e.g., 10/11/2025 1:15:55 AM)",
                "MM/dd/yyyy HH:mm:ss (e.g., 10/11/2025 10:15:55)",
                "MM/dd/yyyy hh:mm:ss a (e.g., 10/11/2025 10:15:55 AM)",
                "MM/dd/yyyy (e.g., 10/11/2025)"
        );
    }

    /**
     * Validate if a date string can be parsed
     *
     * @param dateString the date string to validate
     * @return true if parseable, false otherwise
     */
    public boolean isValidDateFormat(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return false;
        }

        String trimmedDate = dateString.trim();
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                LocalDateTime.parse(trimmedDate, formatter);
                return true;
            } catch (DateTimeParseException e) {
                // Continue to next format
            }
        }
        return false;
    }

    /**
     * Get the specific format that successfully parsed the date
     *
     * @param dateString the date string
     * @return the format string that worked, or null if none worked
     */
    public String getUsedFormat(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        String trimmedDate = dateString.trim();
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                LocalDateTime.parse(trimmedDate, formatter);
                return formatter.toString();
            } catch (DateTimeParseException e) {
                // Continue to next format
            }
        }
        return null;
    }
}
