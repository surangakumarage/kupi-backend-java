package com.kupi.cc.base.domain.response;

import com.kupi.cc.base.domain.entity.Subscription;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * SubscriptionOrder ResponseDto
 */

@Getter
@Setter
public class SubscriptionOrderResponseDto extends ResponseDto {
    private String id;
    private String merchantImageUrl;
    private String merchantName;
    private String title;
    private DateResponseDto createdAt;

    public SubscriptionOrderResponseDto(Subscription subscription, String timeZone,
                                        BasicBusinessResponseDto basicBusinessResponseDto) {
        this.id = subscription.getSubscriptionPlanId();
        this.title = subscription.getTitle();
        this.createdAt = new DateResponseDto(subscription.getCreatedDate(), timeZone,
                new Date(subscription.getCreatedDate()));
        this.merchantImageUrl = basicBusinessResponseDto.getImageUrl();
        this.merchantName = basicBusinessResponseDto.getName();
    }

    /**
     * This method converts object to json string for logging purpose.
     * PII data should be obfuscated.
     *
     * @return json string
     */
    @Override
    public String toLogJson() {
        return toJson();
    }
}