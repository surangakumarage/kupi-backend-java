package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.CouponStatistics;
import com.kupi.cc.base.domain.entity.DealSearchIndex;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;


/**
 * DealCouponResponseDto
 */
@Setter
@Getter
public class DealCouponResponseDto extends ResponseDto {

    private static final String DATE_FORMAT = "dd MMM yyyy"; //Eg: 24 Feb 2020

    private CouponPriceResponseDto price;
    private int totalCoupons;
    private int redeemedCoupons;
    private int remainingCoupons;
    private int soldCouponCount;
    private String couponExpireOn;
    private String validPeriod;
    private boolean couponActive;

    public DealCouponResponseDto(Double couponPrice,
                                 CouponStatistics couponStatistics, String timeZone, long dealExpiry, long validFrom) {
        this.price = new CouponPriceResponseDto(couponPrice);
        if (couponStatistics != null) {
            this.totalCoupons = couponStatistics.getTotalCoupons();
            this.redeemedCoupons = couponStatistics.getRedeemedCoupons();
            this.remainingCoupons = couponStatistics.getRemainingCoupons();
            this.soldCouponCount = this.totalCoupons - this.remainingCoupons;
        }
        this.couponExpireOn = getLastValidDate(timeZone, dealExpiry);
        this.validPeriod = getFirstValidDate(timeZone, validFrom) + " - " + couponExpireOn;
    }

    public DealCouponResponseDto(DealSearchIndex dealSearchIndex, String timeZone) {
        this.price = new CouponPriceResponseDto(dealSearchIndex.getCouponPrice());
        this.totalCoupons = dealSearchIndex.getTotalCoupons();
        this.remainingCoupons = dealSearchIndex.getRemainingCoupons();
        this.redeemedCoupons = totalCoupons - remainingCoupons;
        this.soldCouponCount = this.totalCoupons - this.remainingCoupons;
        this.couponExpireOn = getLastValidDate(timeZone, dealSearchIndex.getExpiredOn());
        this.validPeriod = getFirstValidDate(timeZone, dealSearchIndex.getValidFrom()) + " - " + couponExpireOn;
        this.couponActive = dealSearchIndex.isCouponActive();
    }

    /**
     * This method used to get last valid date on coupon.
     *
     * @param timezoneId timezoneId
     * @param dealExpiry dealExpiry
     * @return String date
     */
    private String getLastValidDate(String timezoneId, long dealExpiry) {
        var calendar = Calendar.getInstance();
        calendar.setTime(new Date(dealExpiry));
        calendar.add(Calendar.DATE, 7);
        var sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(timezoneId));
        return sdf.format(calendar.getTime());
    }

    /**
     * This method used to get first valid date on coupon.
     *
     * @param timezoneId timezoneId
     * @param validFrom  validFrom
     * @return String date
     */
    private String getFirstValidDate(String timezoneId, long validFrom) {
        var calendar = Calendar.getInstance();
        calendar.setTime(new Date(validFrom));
        var sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(timezoneId));
        return sdf.format(calendar.getTime());
    }
}
