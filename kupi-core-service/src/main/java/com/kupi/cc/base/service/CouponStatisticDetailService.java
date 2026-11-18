package com.kupi.cc.base.service;

import com.kupi.cc.base.domain.entity.CouponStatisticDetail;
import com.kupi.cc.base.domain.entity.Deal;
import com.kupi.cc.base.domain.response.UploadFailedCouponResponseDto;
import com.kupi.cc.base.enums.CouponRejection;
import com.kupi.cc.base.exception.CouponUploadFailedException;
import com.kupi.cc.base.exception.KupiCoreException;
import com.kupi.cc.base.repository.CouponStatisticDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * CouponStatisticDetailService
 */
@Service
@Slf4j
public class CouponStatisticDetailService {

    private final CouponStatisticDetailRepository couponStatisticDetailRepository;

    public CouponStatisticDetailService(CouponStatisticDetailRepository couponStatisticDetailRepository) {
        this.couponStatisticDetailRepository = couponStatisticDetailRepository;
    }

    /**
     * This method used to save a couponStatisticDetail.
     *
     * @param dealCode        dealCode
     * @param couponRejection couponRejection
     * @param fileName        fileName
     * @param deal            deal
     */
    public void saveDetails(String dealCode, CouponRejection couponRejection, String fileName, Deal deal) {
        try {
            var couponStatisticDetail = new CouponStatisticDetail(dealCode, couponRejection, fileName, deal);
            couponStatisticDetailRepository.save(couponStatisticDetail);
        } catch (DataAccessException e) {
            log.error("Failed to save coupon statistic detail for deal id: {}", deal.getId(), e);
            throw new CouponUploadFailedException("Failed to save coupon statistic detail for deal id " + deal.getId(), e);
        }
    }

    /**
     * This method used to get upload failed coupon list by deal id and file name.
     *
     * @param dealId   dealId
     * @param fileName fileName
     * @return failed coupons id list
     */
    public List<String> getUploadFailedCouponIds(String dealId, String fileName) {
        try {
            return couponStatisticDetailRepository.getUploadFailCoupon(dealId, fileName);
        } catch (DataAccessException e) {
            log.error("Failed to get upload failed coupons for deal ids for dealId:{}, fileName:{}", dealId, fileName, e);
            throw new KupiCoreException("Failed to get upload failed coupons for deal id"
                    + dealId + " and fileName: " + fileName, e);
        }
    }

    /**
     * This method used to get list of upload failed coupon detail list by deal id and file name.
     *
     * @param dealId   dealId
     * @param fileName fileName
     * @return list of UploadFailedCouponResponseDto
     */
    public List<UploadFailedCouponResponseDto> getUploadFailedCouponDetails(String dealId, String fileName) {
        try {
            var uploadFailedCouponIds = getUploadFailedCouponIds(dealId, fileName);
            var failedCouponDetailList =
                    couponStatisticDetailRepository.findAllByIdIn(uploadFailedCouponIds);
            List<UploadFailedCouponResponseDto> uploadFailedCoupons = new ArrayList<>();
            failedCouponDetailList.forEach(couponStatisticDetail -> {
                var uploadFailedCouponResponseDto = new UploadFailedCouponResponseDto(couponStatisticDetail);
                uploadFailedCoupons.add(uploadFailedCouponResponseDto);
            });
            return uploadFailedCoupons;
        } catch (DataAccessException e) {
            log.error("Failed to get upload failed coupon details for deal id :{}, fileName: " + dealId, fileName, e);
            throw new KupiCoreException("Failed to get upload failed coupon details for deal id"
                    + dealId + " and fileName: " + fileName, e);
        }
    }

    /**
     * This method used to get coupon uploaded failed file name list by deal id.
     *
     * @param dealId dealId
     * @return fileName list
     */
    public List<String> getUploadedFailedCouponFileListByDealId(String dealId) {
        try {
            return couponStatisticDetailRepository.getDistinctByFileName(dealId);
        } catch (DataAccessException e) {
            throw new KupiCoreException("Getting coupon upload failed files by deal is: " + dealId + " was failed.", e);
        }
    }
}
