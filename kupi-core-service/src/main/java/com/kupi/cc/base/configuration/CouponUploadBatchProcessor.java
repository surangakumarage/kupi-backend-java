package com.kupi.cc.base.configuration;

import com.kupi.cc.base.domain.entity.Coupon;
import com.kupi.cc.base.domain.entity.CouponCsv;
import com.kupi.cc.base.domain.entity.Deal;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.exception.CouponUploadFailedException;
import com.kupi.cc.base.service.CouponCodeGeneratorService;
import com.kupi.cc.base.util.DateParserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

import java.util.*;

/**
 * CouponUploadBatchProcessor - For automated coupon generation (expiryDate, couponCount)
 */
@Slf4j
public class CouponUploadBatchProcessor implements ItemProcessor<CouponCsv, List<Coupon>> {

    private final Deal deal;
    private final String fileName;
    private final String timeZone;
    private final Set<String> readCoupons = new HashSet<>();
    private final CouponCodeGeneratorService couponCodeGeneratorService;
    private final DateParserUtil dateParserUtil;

    public CouponUploadBatchProcessor(Deal deal, String fileName, String timeZone,
                                      CouponCodeGeneratorService couponCodeGeneratorService,
                                      DateParserUtil dateParserUtil) {
        this.deal = deal;
        this.fileName = fileName;
        this.timeZone = timeZone;
        this.couponCodeGeneratorService = couponCodeGeneratorService;
        this.dateParserUtil = dateParserUtil;
    }

    @Override
    public List<Coupon> process(CouponCsv couponCsv) {
        try {
            if (!couponCsv.isValid()) {
                log.warn("Invalid CSV row: {}", couponCsv);
                return new ArrayList<>();
            }

            long couponExpireDate = dateParserUtil.parseDateToMilliseconds(couponCsv.getExpiryDate(), timeZone);

//            if (!hasValidCouponExpireDatePeriod(deal.getExpiredOn(), couponExpireDate)) {
//                log.warn("Invalid expiry date for CSV row: {}. Coupon expiry date ({}) must be on or after deal expiry date ({}).",
//                        couponCsv, couponExpireDate, deal.getExpiredOn());
//                return new ArrayList<>();
//            }

            return processCouponGeneration(couponCsv, couponExpireDate);

        } catch (Exception e) {
            log.error("Coupon csv file processing failed for: {}", deal.getId(), e);
            throw new CouponUploadFailedException("Processing coupons was failed for deal id: " + deal.getId(), e);
        }
    }

    /**
     * Process CSV row and generate coupons (expiryDate, couponCount)
     */
    private List<Coupon> processCouponGeneration(CouponCsv couponCsv, long couponExpireDate) {
        List<Coupon> coupons = new ArrayList<>();

        try {
            log.info("Generating {} coupons for expiry date: {}", couponCsv.getCouponCount(), couponCsv.getExpiryDate());

            // Generate unique coupon codes
            List<String> generatedCodes = couponCodeGeneratorService.generateUniqueCodes(couponCsv.getCouponCount());

            // Create coupon entities
            for (String code : generatedCodes) {
                if (!readCoupons.contains(code)) {
                    readCoupons.add(code);
                    coupons.add(new Coupon(deal, UserType.MERCHANT, code, fileName, couponExpireDate));
                }
            }

            log.info("Successfully generated {} coupons", coupons.size());

        } catch (Exception e) {
            log.error("Failed to generate coupons for CSV row: {}", couponCsv, e);
            throw new CouponUploadFailedException("Failed to generate " + couponCsv.getCouponCount() + " coupons", e);
        }

        return coupons;
    }


    /**
     * This method returns true when coupon has a valid expiry date.
     * A coupon expiry date is valid if it is on or after the deal expiry date.
     * This allows coupons to be added even if the deal expires today.
     *
     * @param dealExpire   dealExpire
     * @param couponExpire couponExpire
     * @return true/ false
     */
    private boolean hasValidCouponExpireDatePeriod(long dealExpire, long couponExpire) {
        return couponExpire >= dealExpire;
    }
}
