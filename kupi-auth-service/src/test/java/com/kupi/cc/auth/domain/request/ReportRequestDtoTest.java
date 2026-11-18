package com.kupi.cc.auth.domain.request;

import com.kupi.cc.auth.enums.ReportDateOption;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests {@link ReportRequestDto} class
 */
class ReportRequestDtoTest {

    private static final Long START_DATE = 1_659_292_200_000L;
    private static final Long END_DATE = 1_660_761_000_000L;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    /**
     * Required fields existence check
     */
    @Test
    void Should_ReturnTrue_When_RequiredFieldsExists() {
        ReportRequestDto reportRequestDto = getSampleReportRequestDto(ReportDateOption.WEEKLY, START_DATE, END_DATE);
        assertTrue(reportRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_ReportDateOptionIsNull() {
        ReportRequestDto reportRequestDto = getSampleReportRequestDto(null, START_DATE, END_DATE);
        assertFalse(reportRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_StartDateIsNull() {
        ReportRequestDto reportRequestDto = getSampleReportRequestDto(ReportDateOption.WEEKLY, null, END_DATE);
        assertFalse(reportRequestDto.isRequiredAvailable());
    }

    @Test
    void Should_ReturnFalse_When_EndDateIsNull() {
        ReportRequestDto reportRequestDto = getSampleReportRequestDto(ReportDateOption.WEEKLY, START_DATE, null);
        assertFalse(reportRequestDto.isRequiredAvailable());
    }

    /**
     * Supported date range check
     */
    @Test
    void Should_ReturnTrue_When_DateRangeIsSupported() {
        ReportRequestDto reportRequestDto = getSampleReportRequestDto(ReportDateOption.WEEKLY, START_DATE, END_DATE);
        assertTrue(reportRequestDto.isSupportedDateRange());
    }

    @Test
    void Should_ReturnFalse_When_DateRangeIsNotSupported() {
        ReportRequestDto reportRequestDto = getSampleReportRequestDto(ReportDateOption.MONTHLY, START_DATE, END_DATE);
        assertFalse(reportRequestDto.isSupportedDateRange());
    }

    /**
     * Is valid date range
     */
    @Test
    void Should_ReturnTrue_When_DateRangeIsValid() {
        ReportRequestDto reportRequestDto = getSampleReportRequestDto(ReportDateOption.DAILY, START_DATE, END_DATE);
        assertTrue(reportRequestDto.isValidDateRange());
    }

    @Test
    void Should_ReturnFalse_When_DateRangeIsNotValid() {
        ReportRequestDto reportRequestDto = getSampleReportRequestDto(ReportDateOption.DAILY, END_DATE, START_DATE);
        assertFalse(reportRequestDto.isValidDateRange());
    }

    private ReportRequestDto getSampleReportRequestDto(ReportDateOption option, Long startDate, Long endDate) {
        ReportRequestDto reportRequestDto = new ReportRequestDto();
        reportRequestDto.setOption(option);
        reportRequestDto.setStartDate(startDate);
        reportRequestDto.setEndDate(endDate);
        return reportRequestDto;
    }
}
