package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.Coupon;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * UserCouponHistoryPageResponseDto
 */
@Setter
@Getter
public class UserCouponHistoryPageResponseDto extends PageResponseDto {

    private List<UserCouponHistoryResponseDto> history;

    public UserCouponHistoryPageResponseDto(Page<Coupon> page, String timeZone,
                                            Map<String, MerchantBusinessResponseDto> merchantMap) {
        super(page);
        this.history = convertToListResponseDto(page.getContent(), timeZone, merchantMap);
    }

    /**
     * This method used to convert List of coupon to UserCouponHistoryResponseDto.
     *
     * @param couponList  couponList
     * @param timeZone    timeZone
     * @param merchantMap merchantMap
     * @return UserCouponHistoryResponseDto List
     */
    private List<UserCouponHistoryResponseDto> convertToListResponseDto(List<Coupon> couponList, String timeZone,
                                                                        Map<String, MerchantBusinessResponseDto> merchantMap) {
        return couponList.stream()
                .map(coupon -> new UserCouponHistoryResponseDto(coupon, timeZone,
                        merchantMap.get(coupon.getDeal().getMerchantId()))).collect(Collectors.toList());
    }
}
